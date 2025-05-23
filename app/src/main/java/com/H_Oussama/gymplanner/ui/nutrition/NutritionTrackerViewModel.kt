package com.H_Oussama.gymplanner.ui.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.H_Oussama.gymplanner.data.model.FoodItem
import com.H_Oussama.gymplanner.data.model.Meal
import com.H_Oussama.gymplanner.data.model.NutritionEntry
import com.H_Oussama.gymplanner.data.model.normalizeDate
import com.H_Oussama.gymplanner.data.repositories.CalorieService
import com.H_Oussama.gymplanner.data.repositories.ConfigRepository
import com.H_Oussama.gymplanner.data.repositories.DailyNutritionSummary
import com.H_Oussama.gymplanner.data.repositories.NutritionRepository
import com.H_Oussama.gymplanner.data.repositories.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

data class FoodWithEntry(
    val food: FoodItem,
    val entry: NutritionEntry
)

data class MealWithEntries(
    val meal: Meal,
    val entries: List<FoodWithEntry> = emptyList()
)

data class NutritionGoals(
    val calories: Int = 2000,
    val carbs: Float = 250f,
    val protein: Float = 125f,
    val fat: Float = 70f,
    val water: Int = 8
)

data class NutritionUiState(
    val date: Date = Calendar.getInstance().time,
    val meals: List<MealWithEntries> = emptyList(),
    val summary: DailyNutritionSummary = DailyNutritionSummary(),
    val recentFoods: List<FoodItem> = emptyList(),
    val goals: NutritionGoals = NutritionGoals(),
    val waterIntake: Int = 0,
    val caloriesBurned: Int = 0,
    val calculatedGoal: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showEditGoalDialog: Boolean = false
)

@HiltViewModel
class NutritionTrackerViewModel @Inject constructor(
    private val nutritionRepository: NutritionRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val calorieService: CalorieService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NutritionUiState())
    val uiState: StateFlow<NutritionUiState> = _uiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NutritionUiState(isLoading = true)
    )
    
    // Use a normalized date for the current date
    private val currentDate = MutableStateFlow(Date().normalizeDate())
    
    init {
        viewModelScope.launch {
            nutritionRepository.initializeMeals()
            
            // Initialize Gemini model if API key is available
            val apiKey = userPreferencesRepository.getGeminiApiKey()
            if (apiKey.isNotEmpty()) {
                nutritionRepository.initializeGeminiModel(apiKey)
            }

            // Load nutrition data for the normalized current date
            println("DEBUG: Initial data load with normalized date: ${currentDate.value}")
            loadDataForDate(currentDate.value)
            
            // Set up a flow to observe the current date and load data accordingly
            currentDate.collect { date ->
                loadDataForDate(date)
            }
        }
        
        // Observe burned calories changes
        viewModelScope.launch {
            calorieService.caloriesBurnedToday.collect { burnedCalories ->
                // Only update if it's for today
                if (currentDate.value.time == Date().normalizeDate().time) {
                    _uiState.value = _uiState.value.copy(
                        caloriesBurned = burnedCalories
                    )
                }
            }
        }
    }

    private suspend fun loadDataForDate(date: Date) {
        // Normalize the date to ensure consistent comparison
        val normalizedDate = date.normalizeDate()
        println("DEBUG: Loading nutrition data for normalized date: $normalizedDate")
        
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        try {
            // Get all meals
            val meals = nutritionRepository.getAllMeals().firstOrNull() ?: emptyList()
            
            // Get nutrition entries for the selected date
            val entries = nutritionRepository.getNutritionEntriesForDate(normalizedDate).firstOrNull() ?: emptyList()
            println("DEBUG: Found ${entries.size} nutrition entries for date: $normalizedDate")
            
            // Create a map of food items by ID
            val foodItemsMap = mutableMapOf<String, FoodItem>()
            entries.forEach { entry ->
                val foodItem = nutritionRepository.getFoodItemById(entry.foodItemId).firstOrNull()
                if (foodItem != null) {
                    foodItemsMap[entry.foodItemId] = foodItem
                }
            }
            
            // Create FoodWithEntry objects
            val entriesWithFood = entries.mapNotNull { entry -> 
                val food = foodItemsMap[entry.foodItemId] ?: return@mapNotNull null
                FoodWithEntry(food, entry)
            }
            
            // Group entries by meal
            val mealsWithEntries = meals.map { meal ->
                MealWithEntries(meal, entriesWithFood.filter { it.entry.mealId == meal.id })
            }
            
            // Get nutrition summary data
            val calories = nutritionRepository.getTotalCaloriesForDate(normalizedDate).firstOrNull() ?: 0
            val carbs = nutritionRepository.getTotalCarbsForDate(normalizedDate).firstOrNull() ?: 0.0
            val protein = nutritionRepository.getTotalProteinForDate(normalizedDate).firstOrNull() ?: 0.0
            val fat = nutritionRepository.getTotalFatForDate(normalizedDate).firstOrNull() ?: 0.0
            
            // Get recent food items
            val recentFoods = nutritionRepository.getRecentFoodItems().firstOrNull() ?: emptyList()
            
            // Get water intake for the day
            val waterIntake = userPreferencesRepository.getWaterIntake(normalizedDate) ?: 0
            
            // Get calories burned for the date - convert Date to LocalDate
            val localDate = convertToLocalDate(normalizedDate)
            val caloriesBurned = calorieService.getCaloriesBurnedForDate(localDate)
            
            // Calculate dynamic calorie goal based on user profile
            val calculatedGoal = userPreferencesRepository.calculateDailyCalorieGoal()
            
            // Get user-set goals (if available) or use calculated goals
            val calorieGoal = userPreferencesRepository.getCalorieGoal() ?: calculatedGoal
            val carbGoal = userPreferencesRepository.getCarbGoal() ?: (calculatedGoal * 0.5 / 4).toFloat() // 50% carbs, 4 calories per gram
            val proteinGoal = userPreferencesRepository.getProteinGoal() ?: (calculatedGoal * 0.25 / 4).toFloat() // 25% protein, 4 calories per gram
            val fatGoal = userPreferencesRepository.getFatGoal() ?: (calculatedGoal * 0.25 / 9).toFloat() // 25% fat, 9 calories per gram
            val waterGoal = userPreferencesRepository.getWaterIntakeGoal() ?: 8
            
            _uiState.value = _uiState.value.copy(
                date = normalizedDate,
                meals = mealsWithEntries,
                summary = DailyNutritionSummary(
                    calories = calories,
                    carbs = carbs.toFloat(),
                    protein = protein.toFloat(),
                    fat = fat.toFloat()
                ),
                recentFoods = recentFoods,
                goals = NutritionGoals(
                    calories = calorieGoal,
                    carbs = carbGoal,
                    protein = proteinGoal,
                    fat = fatGoal,
                    water = waterGoal
                ),
                waterIntake = waterIntake,
                caloriesBurned = caloriesBurned,
                calculatedGoal = calculatedGoal,
                isLoading = false,
                errorMessage = null
            )
        } catch (e: Exception) {
            println("ERROR: Failed to load data for date: ${e.message}")
            e.printStackTrace()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Error loading nutrition data: ${e.localizedMessage}"
            )
        }
    }
    
    // Utility method to convert java.util.Date to java.time.LocalDate
    private fun convertToLocalDate(date: Date): LocalDate {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }
    
    fun setDate(date: Date) {
        // Always normalize the date to ensure consistent behavior
        val normalizedDate = date.normalizeDate()
        println("DEBUG: Setting date to normalized date: $normalizedDate")
        currentDate.value = normalizedDate
    }
    
    fun getFormattedDate(): String {
        val formatter = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        return formatter.format(currentDate.value)
    }
    
    fun addFoodEntry(foodItem: FoodItem, mealId: String, servingSize: Float) {
        viewModelScope.launch {
            try {
                println("DEBUG: Starting food entry addition process")
                println("DEBUG: Food item: ${foodItem.name}, ID: ${foodItem.id}")
                println("DEBUG: Meal ID: $mealId, Serving size: $servingSize")
                
                // Verify meal exists
                val mealData = nutritionRepository.getMealById(mealId)
                if (mealData == null) {
                    println("ERROR: Meal with ID $mealId does not exist in database!")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Error: Selected meal does not exist. Please try again."
                    )
                    return@launch
                }
                
                println("DEBUG: Found meal: ${mealData.name}")
                
                // Calculate nutrition based on the serving size
                val servingRatio = servingSize / foodItem.servingSize
                val calories = (foodItem.calories * servingRatio).toInt()
                val carbs = foodItem.carbs * servingRatio
                val protein = foodItem.protein * servingRatio
                val fat = foodItem.fat * servingRatio
                
                // Ensure the date is normalized (midnight time)
                val today = Date().normalizeDate()
                
                println("DEBUG: Using normalized date: $today for current date")
                
                val entry = NutritionEntry(
                    id = UUID.randomUUID().toString(), // Ensure we have a unique ID
                    foodItemId = foodItem.id,
                    mealId = mealId,
                    date = today, // Use normalized today date
                    servingSize = foodItem.servingSize,
                    numberOfServings = servingSize / foodItem.servingSize,
                    calories = calories,
                    carbs = carbs,
                    protein = protein,
                    fat = fat,
                    notes = "Added manually", // Add a note for tracking
                    createdAt = Date() // Current timestamp
                )
                
                // Debug info
                println("DEBUG: Adding nutrition entry: $entry")
                println("DEBUG: For food: ${foodItem.name}, mealId: $mealId")
                
                // Use a direct database method for reliable insertion
                val entryId = nutritionRepository.addNutritionEntry(entry)
                println("DEBUG: Entry added with ID: $entryId")
                
                // Explicitly refresh our date to ensure we reload with the same date
                setDate(today)
                
                // Explicitly load data for this date again to refresh the UI
                loadDataForDate(today)
                
                println("DEBUG: Data reloaded after adding entry")
            } catch (e: Exception) {
                println("ERROR: Failed to add food entry: ${e.message}")
                e.printStackTrace()
                
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error adding food entry: ${e.localizedMessage}"
                )
            }
        }
    }
    
    fun deleteEntry(entry: NutritionEntry) {
        viewModelScope.launch {
            try {
                nutritionRepository.deleteNutritionEntry(entry)
                loadDataForDate(currentDate.value)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error deleting entry: ${e.localizedMessage}"
                )
            }
        }
    }
    
    fun incrementWaterIntake() {
        viewModelScope.launch {
            try {
                val currentDate = _uiState.value.date
                val currentIntake = _uiState.value.waterIntake
                val newIntake = currentIntake + 1
                
                // Convert to LocalDate and update in preferences
                val localDate = convertToLocalDate(currentDate)
                userPreferencesRepository.setWaterIntake(localDate, newIntake)
                
                // Update UI state
                _uiState.value = _uiState.value.copy(
                    waterIntake = newIntake
                )
            } catch (e: Exception) {
                println("ERROR: Failed to increment water intake: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error updating water intake: ${e.localizedMessage}"
                )
            }
        }
    }
    
    fun decrementWaterIntake() {
        viewModelScope.launch {
            val currentIntake = _uiState.value.waterIntake
            if (currentIntake > 0) {
                val localDate = convertToLocalDate(currentDate.value)
                userPreferencesRepository.setWaterIntake(localDate, currentIntake - 1)
                _uiState.value = _uiState.value.copy(
                    waterIntake = currentIntake - 1
                )
            }
        }
    }
    
    fun getFoodInfoFromGemini(foodDescription: String, mealId: String, servingSize: Float = 1.0f) {
        viewModelScope.launch {
            try {
                println("DEBUG: Starting Gemini API request for food: '$foodDescription', mealId: $mealId")
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val foodItem = nutritionRepository.createFoodItemFromGemini(foodDescription)
                
                if (foodItem != null) {
                    println("DEBUG: Gemini API returned food item: ${foodItem.name} with ${foodItem.calories} calories")
                    
                    // Normalize today's date to ensure consistent date comparison
                    val today = Date().normalizeDate()
                    
                    // Add the food entry with the current date
                    addFoodEntry(foodItem, mealId, servingSize)
                    
                    // Force refresh data for today
                    currentDate.value = today
                    loadDataForDate(today)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                } else {
                    println("ERROR: Gemini API returned null food item for '$foodDescription'")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Could not analyze the food description. Please try with more details."
                    )
                }
            } catch (e: Exception) {
                println("ERROR: Exception in getFoodInfoFromGemini: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error processing food information: ${e.localizedMessage}"
                )
            }
        }
    }
    
    fun updateNutritionGoals(calories: Int, carbs: Float, protein: Float, fat: Float) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.setCalorieGoal(calories)
                userPreferencesRepository.setCarbGoal(carbs)
                userPreferencesRepository.setProteinGoal(protein)
                userPreferencesRepository.setFatGoal(fat)
                
                _uiState.value = _uiState.value.copy(
                    goals = NutritionGoals(
                        calories = calories,
                        carbs = carbs,
                        protein = protein,
                        fat = fat,
                        water = _uiState.value.goals.water
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error updating nutrition goals: ${e.localizedMessage}"
                )
            }
        }
    }
    
    fun updateWaterIntakeGoal(goal: Int) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.setWaterIntakeGoal(goal)
                
                _uiState.value = _uiState.value.copy(
                    goals = _uiState.value.goals.copy(water = goal)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error updating water intake goal: ${e.localizedMessage}"
                )
            }
        }
    }
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun setErrorMessage(message: String) {
        println("DEBUG: Setting error message: $message")
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    // Add a method to search for food items
    fun searchFoodItems(query: String): Flow<List<FoodItem>> {
        return nutritionRepository.searchFoodItems(query)
    }

    fun loadMeals() {
        viewModelScope.launch {
            try {
                println("DEBUG: Loading meals in ViewModel")
                // This will ensure meals are initialized and loaded
                nutritionRepository.initializeMeals()
                
                // Get all meals
                val meals = nutritionRepository.getAllMeals().firstOrNull() ?: emptyList()
                println("DEBUG: Loaded ${meals.size} meals from repository")
                
                // Update UI state with meals
                _uiState.value = _uiState.value.copy(
                    meals = meals.map { MealWithEntries(it) }
                )
                
                // If no meals were loaded, try to initialize default ones
                if (meals.isEmpty()) {
                    println("DEBUG: No meals found, initializing defaults")
                    initializeDefaultMeals()
                }
            } catch (e: Exception) {
                println("ERROR: Failed to load meals: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error loading meals: ${e.localizedMessage}"
                )
            }
        }
    }

    fun initializeDefaultMeals() {
        viewModelScope.launch {
            try {
                println("DEBUG: Explicitly initializing default meals")
                nutritionRepository.initializeMeals(forceCreate = true)
                
                // Reload meals after initialization
                val meals = nutritionRepository.getAllMeals().firstOrNull() ?: emptyList()
                println("DEBUG: Created ${meals.size} default meals")
                
                _uiState.value = _uiState.value.copy(
                    meals = meals.map { MealWithEntries(it) }
                )
            } catch (e: Exception) {
                println("ERROR: Failed to initialize default meals: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error initializing default meals: ${e.localizedMessage}"
                )
            }
        }
    }

    fun debugPrintMealInfo() {
        viewModelScope.launch {
            try {
                println("DEBUG: === MEAL DEBUGGING INFO ===")
                val meals = nutritionRepository.getAllMeals().firstOrNull() ?: emptyList()
                println("DEBUG: Found ${meals.size} meals:")
                meals.forEach { meal ->
                    println("DEBUG: Meal: ${meal.name}, ID: ${meal.id}, Order: ${meal.displayOrder}")
                }
                
                // Also print recent foods
                val recentFoods = nutritionRepository.getRecentFoodItems().firstOrNull() ?: emptyList()
                println("DEBUG: Found ${recentFoods.size} recent foods:")
                recentFoods.take(5).forEach { food ->
                    println("DEBUG: Food: ${food.name}, ID: ${food.id}, Calories: ${food.calories}")
                }
                
                // Check nutrition entries
                val entries = nutritionRepository.getNutritionEntriesForDate(currentDate.value).firstOrNull() ?: emptyList()
                println("DEBUG: Found ${entries.size} nutrition entries for today:")
                entries.forEach { entry ->
                    println("DEBUG: Entry: ${entry.id}, Food: ${entry.foodItemId}, Meal: ${entry.mealId}")
                }
                
                println("DEBUG: === END MEAL DEBUGGING INFO ===")
            } catch (e: Exception) {
                println("ERROR in debug function: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun testGeminiApi() {
        viewModelScope.launch {
            try {
                println("DEBUG: Testing Gemini API from UI...")
                nutritionRepository.testGeminiApi()
            } catch (e: Exception) {
                println("ERROR: Failed to test Gemini API: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error testing Gemini API: ${e.localizedMessage}"
                )
            }
        }
    }

    fun openEditGoalDialog() {
        _uiState.value = _uiState.value.copy(
            showEditGoalDialog = true
        )
    }

    fun closeEditGoalDialog() {
        _uiState.value = _uiState.value.copy(
            showEditGoalDialog = false
        )
    }
} 