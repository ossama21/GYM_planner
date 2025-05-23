package com.H_Oussama.gymplanner.data.repositories

import com.H_Oussama.gymplanner.data.database.FoodItemDao
import com.H_Oussama.gymplanner.data.database.MealDao
import com.H_Oussama.gymplanner.data.database.NutritionEntryDao
import com.H_Oussama.gymplanner.data.model.FoodItem
import com.H_Oussama.gymplanner.data.model.Meal
import com.H_Oussama.gymplanner.data.model.NutritionEntry
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class FoodNutritionInfo(
    val name: String,
    val servingSize: String,
    val calories: Int,
    val carbs: Double,
    val protein: Double,
    val fat: Double,
    val description: String = ""
)

/**
 * Repository for managing nutrition data including food items, meals, and nutrition entries
 */
@Singleton
class NutritionRepository @Inject constructor(
    private val nutritionEntryDao: NutritionEntryDao,
    private val foodItemDao: FoodItemDao,
    private val mealDao: MealDao
) {
    private val json = Json { ignoreUnknownKeys = true }
    private var generativeModel: GenerativeModel? = null
    private var apiKey: String = "" // Store the API key for potential model recreation
    
    fun initializeGeminiModel(apiKey: String) {
        if (apiKey.isNotEmpty()) {
            this.apiKey = apiKey // Cache the API key
            generativeModel = GenerativeModel(
                modelName = "gemini-2.0-flash", // Updated model name
                apiKey = apiKey
            )
        }
    }

    // Food Items
    suspend fun addFoodItem(foodItem: FoodItem): Long {
        return foodItemDao.insert(foodItem)
    }
    
    suspend fun updateFoodItem(foodItem: FoodItem) {
        foodItemDao.update(foodItem)
    }
    
    suspend fun deleteFoodItem(foodItem: FoodItem) {
        foodItemDao.delete(foodItem)
    }
    
    fun getFoodItemById(id: String): Flow<FoodItem?> {
        return foodItemDao.getFoodItemById(id)
    }
    
    fun searchFoodItems(query: String): Flow<List<FoodItem>> {
        return foodItemDao.searchFoodItems(query)
    }
    
    fun getRecentFoodItems(): Flow<List<FoodItem>> {
        return foodItemDao.getRecentFoodItems(10)
    }
    
    // Meals
    fun getAllMeals(): Flow<List<Meal>> {
        return mealDao.getAllMeals()
    }
    
    suspend fun getMealById(id: String): Meal? {
        return mealDao.getMealById(id)
    }
    
    suspend fun initializeMeals(forceCreate: Boolean = false) {
        println("DEBUG: Initializing meals, forceCreate=$forceCreate")
        val count = mealDao.getMealCount()
        println("DEBUG: Current meal count: $count")
        
        if (count == 0 || forceCreate) {
            println("DEBUG: Creating default meals")
            
            // If forcing creation and meals exist, delete existing meals first
            if (forceCreate && count > 0) {
                println("DEBUG: Deleting existing meals before recreating")
                mealDao.deleteAllMeals()
            }
            
            val defaultMeals = listOf(
                Meal(name = "Breakfast", displayOrder = 0),
                Meal(name = "Lunch", displayOrder = 1),
                Meal(name = "Dinner", displayOrder = 2),
                Meal(name = "Snack", displayOrder = 3)
            )
            
            val result = mealDao.insertAll(defaultMeals)
            println("DEBUG: Inserted meals with result: $result")
        } else {
            println("DEBUG: Meals already exist, skipping initialization")
        }
    }
    
    // Nutrition Entries
    suspend fun addNutritionEntry(entry: NutritionEntry): Long {
        println("DEBUG: Adding nutrition entry to database: foodId=${entry.foodItemId}, mealId=${entry.mealId}, date=${entry.date}")
        try {
            val result = nutritionEntryDao.insert(entry)
            println("DEBUG: Successfully added entry with result: $result")
            return result
        } catch (e: Exception) {
            println("ERROR: Failed to add nutrition entry: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    suspend fun updateNutritionEntry(entry: NutritionEntry) {
        try {
            nutritionEntryDao.update(entry)
            println("DEBUG: Successfully updated entry: ${entry.id}")
        } catch (e: Exception) {
            println("ERROR: Failed to update nutrition entry: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    suspend fun deleteNutritionEntry(entry: NutritionEntry) {
        try {
            nutritionEntryDao.delete(entry)
            println("DEBUG: Successfully deleted entry: ${entry.id}")
        } catch (e: Exception) {
            println("ERROR: Failed to delete nutrition entry: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    fun getNutritionEntryById(id: String): Flow<NutritionEntry?> {
        return nutritionEntryDao.getEntryById(id)
    }
    
    fun getNutritionEntriesForDate(date: Date): Flow<List<NutritionEntry>> {
        return nutritionEntryDao.getEntriesForDate(date)
    }
    
    fun getTotalCaloriesForDate(date: Date): Flow<Int?> {
        return nutritionEntryDao.getTotalCaloriesForDate(date)
    }
    
    fun getTotalCarbsForDate(date: Date): Flow<Double?> {
        return nutritionEntryDao.getTotalCarbsForDate(date)
    }
    
    fun getTotalProteinForDate(date: Date): Flow<Double?> {
        return nutritionEntryDao.getTotalProteinForDate(date)
    }
    
    fun getTotalFatForDate(date: Date): Flow<Double?> {
        return nutritionEntryDao.getTotalFatForDate(date)
    }

    // Gemini AI integration
    suspend fun getFoodNutritionFromGemini(foodDescription: String): FoodNutritionInfo? {
        if (generativeModel == null) {
            println("ERROR: Gemini model is null in getFoodNutritionFromGemini - API key configured?")
            // Attempt re-initialization
            if (apiKey.isNotEmpty()) {
                initializeGeminiModel(apiKey)
                if (generativeModel == null) {
                    println("ERROR: Re-initialization failed in getFoodNutritionFromGemini.")
                    return null
                }
                println("DEBUG: Re-initialized model successfully in getFoodNutritionFromGemini.")
            } else {
                 println("ERROR: Cannot re-initialize model, API key is empty.")
                 return null
            }
        }
        
        val currentModelName = generativeModel?.modelName ?: "unknown_model"
        println("DEBUG: Sending request to Gemini API ($currentModelName) with description: '$foodDescription'")
        
        val prompt = """
            Analyze this food description: "$foodDescription"
            
            Return ONLY a JSON object with this exact structure (no extra text, explanations, or markdown):
            {
              "name": "Concise food name",
              "servingSize": "Common serving size (e.g., 100g, 1 cup, 1 slice)",
              "calories": calories_per_serving (integer),
              "carbs": carbs_in_grams_per_serving (float),
              "protein": protein_in_grams_per_serving (float),
              "fat": fat_in_grams_per_serving (float),
              "description": "Optional short description"
            }
        """.trimIndent()
        
        return try {
            // *** Launch API call in GlobalScope to prevent cancellation by ViewModel scope ***
            val deferredResponse = GlobalScope.async(Dispatchers.IO) { 
                 println("DEBUG: Executing Gemini generateContent within GlobalScope coroutine")
                 generativeModel!!.generateContent(prompt)
            }
            
            // Await the result from the GlobalScope job
            println("DEBUG: Awaiting response from GlobalScope job...")
            val response = deferredResponse.await()
            println("DEBUG: Received response object from GlobalScope job.")

            val responseText = response.text?.trim() ?: run {
                println("ERROR: Gemini ($currentModelName) returned null or empty response text for '$foodDescription'")
                return null
            }
            
            println("DEBUG: Received raw response text from Gemini ($currentModelName): $responseText")
            
            // Extract JSON content (handle potential markdown backticks)
            val jsonContent = responseText
                .replaceFirst("```json", "")
                .replaceFirst("```", "")
                .trim()
            
            println("DEBUG: Attempting to parse JSON: $jsonContent")
            val result = json.decodeFromString<FoodNutritionInfo>(jsonContent)
            println("DEBUG: Successfully parsed Gemini response to FoodNutritionInfo: $result")
            result
        } catch (e: CancellationException) {
             println("ERROR: Coroutine scope was cancelled during Gemini API call for '$foodDescription': ${e::class.simpleName}")
             null // Return null on cancellation
        } catch (e: Exception) {
            println("ERROR: Exception during Gemini API call or parsing for '$foodDescription': ${e.message}")
            e.printStackTrace()
            null // Return null on other exceptions
        }
    }
    
    /**
     * Test the Gemini API with a simple prompt to see if it works
     * @return true if the API is working, false otherwise
     */
    suspend fun testGeminiApi(): Boolean {
        return try {
            var model = generativeModel
            
            if (model == null) {
                println("ERROR: generativeModel is null. API key might not be set or properly initialized.")
                return false
            }
            
            println("DEBUG: Starting Gemini API test with model: ${model.modelName}")
            
            val prompt = "Respond with just the word 'SUCCESS' if you can read this message."
            println("DEBUG: Sending test prompt to Gemini API: '$prompt'")
            
            try {
                val response = model.generateContent(content {
                    text(prompt)
                }).text
                
                println("DEBUG: Received response from Gemini API: '$response'")
                
                // Check if response contains "SUCCESS"
                val isSuccess = response?.trim()?.contains("SUCCESS", ignoreCase = true) ?: false
                println("DEBUG: API test result: ${if (isSuccess) "SUCCESS" else "FAILED"}")
                
                return isSuccess
            } catch (e: Exception) {
                println("DEBUG: First attempt failed: ${e.message}")
                // Try with different model versions if we have an API key
                if (apiKey.isNotEmpty()) {
                    // List of model names to try
                    val modelNames = if (model.modelName == "gemini-2.0-flash") {
                        // If we already tried gemini-2.0-flash, try these alternatives
                        listOf("gemini-1.5-pro", "gemini-pro")
                    } else if (model.modelName == "gemini-1.5-pro") {
                        // If we already tried gemini-1.5-pro, try these alternatives
                        listOf("gemini-2.0-flash", "gemini-pro")
                    } else if (model.modelName == "gemini-pro") {
                        // If we already tried gemini-pro, try these alternatives
                        listOf("gemini-2.0-flash", "gemini-1.5-pro")
                    } else {
                        // For any other model, try these in order
                        listOf("gemini-2.0-flash", "gemini-1.5-pro", "gemini-pro")
                    }
                    
                    // Try each model name
                    for (modelName in modelNames) {
                        try {
                            println("DEBUG: Trying with alternative model: $modelName")
                            
                            // Create model with this name
                            generativeModel = GenerativeModel(
                                modelName = modelName,
                                apiKey = apiKey
                            )
                            model = generativeModel
                            
                            if (model != null) {
                                val response = model.generateContent(content {
                                    text(prompt)
                                }).text
                                
                                println("DEBUG: Received response from Gemini API (alternative model): '$response'")
                                
                                // Check if response contains "SUCCESS"
                                val isSuccess = response?.trim()?.contains("SUCCESS", ignoreCase = true) ?: false
                                println("DEBUG: API test with $modelName result: ${if (isSuccess) "SUCCESS" else "FAILED"}")
                                
                                if (isSuccess) {
                                    // If this model works, keep using it
                                    println("DEBUG: Found working model: $modelName")
                                    return true
                                }
                            }
                        } catch (modelError: Exception) {
                            println("DEBUG: Model $modelName failed: ${modelError.message}")
                            // Continue to the next model if this one fails
                        }
                    }
                }
                throw e // Rethrow the original exception if all fallbacks didn't work
            }
        } catch (e: Exception) {
            println("ERROR: Gemini API test failed after trying all models: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    suspend fun createFoodItemFromGemini(foodDescription: String): FoodItem? {
        val nutritionInfo = getFoodNutritionFromGemini(foodDescription) ?: return null
        
        // Parse servingSize string into a number
        val servingSizeValue = try {
            nutritionInfo.servingSize.replace(Regex("[^0-9.]"), "").toFloatOrNull() ?: 1.0f
        } catch (e: Exception) {
            1.0f
        }
        
        // Extract unit from servingSize string
        val servingSizeUnit = nutritionInfo.servingSize.replace(Regex("[0-9.]"), "").trim()
        
        val foodItem = FoodItem(
            name = nutritionInfo.name,
            description = nutritionInfo.description,
            servingSize = servingSizeValue,
            servingUnit = servingSizeUnit.ifEmpty { "serving" }, // Default unit if empty
            calories = nutritionInfo.calories,
            carbs = nutritionInfo.carbs,
            protein = nutritionInfo.protein,
            fat = nutritionInfo.fat,
            isCustom = false // Indicates it came from AI/API
        )
        
        // Add the created food item to the database before returning
        try {
            addFoodItem(foodItem)
            println("DEBUG: Successfully added Gemini-created food item '${foodItem.name}' to database.")
            return foodItem
        } catch (e: Exception) {
            println("ERROR: Failed to add Gemini-created food item '${foodItem.name}' to DB: ${e.message}")
            e.printStackTrace()
            return null // Return null if DB insertion fails
        }
    }
}

data class DailyNutritionSummary(
    val calories: Int = 0,
    val carbs: Float = 0f,    // in grams
    val protein: Float = 0f,  // in grams
    val fat: Float = 0f       // in grams
) 