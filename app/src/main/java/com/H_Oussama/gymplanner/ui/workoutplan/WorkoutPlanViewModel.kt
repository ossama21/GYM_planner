package com.H_Oussama.gymplanner.ui.workoutplan

import android.app.Application // Need Application context for repository
import androidx.compose.runtime.* // Keep for Preview annotations if added later
import androidx.lifecycle.ViewModel // Use regular ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.H_Oussama.gymplanner.data.database.AppDatabase // Import AppDatabase to get DAO
import com.H_Oussama.gymplanner.data.model.ExerciseDefinition // Import model
import com.H_Oussama.gymplanner.data.model.WorkoutPlan
import com.H_Oussama.gymplanner.data.model.WorkoutDay
import com.H_Oussama.gymplanner.data.model.Weekday
import com.H_Oussama.gymplanner.data.parser.WorkoutPlanParser
import com.H_Oussama.gymplanner.data.repositories.ExerciseDefinitionRepository // Import new repo
import com.H_Oussama.gymplanner.data.repositories.WorkoutPlanRepository // Import WorkoutPlanRepository
import com.H_Oussama.gymplanner.util.EnhancedImageMatcher // Import EnhancedImageMatcher
import com.H_Oussama.gymplanner.utils.ExerciseNameNormalizer // Import ExerciseNameNormalizer
import kotlinx.coroutines.flow.* // Import flow operators
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel // Import Hilt annotation
import javax.inject.Inject // Import Inject annotation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext // Correct import

data class WorkoutPlanUiState(
    val workoutDays: List<WorkoutDay> = emptyList(),
    // Add a map to hold fetched definitions
    val exerciseDefinitions: Map<String, ExerciseDefinition> = emptyMap(),
    val parseError: String? = null,
    val isLoading: Boolean = true,
    val isParsing: Boolean = false,
    val currentDayIndex: Int = 0,
    val selectedWeekday: Weekday = Weekday.getCurrent(),
    val error: String? = null,
    val isEditing: Boolean = false,
    val planName: String? = null,
    val isNormalizingNames: Boolean = false,
    val normalizationError: String? = null,
    val normalizationProgress: Float = 0f
)

@HiltViewModel
class WorkoutPlanViewModel @Inject constructor(
    private val exerciseDefinitionRepository: ExerciseDefinitionRepository,
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val enhancedImageMatcher: EnhancedImageMatcher, // Inject EnhancedImageMatcher
    private val exerciseNameNormalizer: ExerciseNameNormalizer, // Inject ExerciseNameNormalizer
    @ApplicationContext private val context: Context // Add context for network check
) : ViewModel() { // Extend ViewModel

    private val _uiState = MutableStateFlow(WorkoutPlanUiState())
    val uiState: StateFlow<WorkoutPlanUiState> = _uiState.asStateFlow()

    init {
        // Load workout plan from repository - with lazy loading
        viewModelScope.launch {
            // First show loading state
            _uiState.update { it.copy(isLoading = true) }
            
            // Ensure plan is loaded before continuing
            workoutPlanRepository.ensurePlanLoaded()
            
            // Collect plan updates
            workoutPlanRepository.workoutPlanFlow
                .collect { plan ->
                    if (plan != null) {
                        loadPlanData(plan)
                    } else {
                        // No plan available, update state with empty workout days
                        _uiState.update { 
                            it.copy(
                                workoutDays = emptyList(),
                                isLoading = false
                            )
                        }
                    }
                }
        }
        
        // Collect normalization progress updates
        viewModelScope.launch {
            exerciseNameNormalizer.normalizationProgress.collect { progress ->
                _uiState.update { it.copy(normalizationProgress = progress) }
            }
        }
    }
    
    // Separate function to load plan data in background
    private suspend fun loadPlanData(plan: WorkoutPlan) = withContext(Dispatchers.Default) {
        // Extract exercise IDs efficiently
        val exerciseIds = plan.days
            .flatMap { day -> day.exercises }
            .map { it.exerciseId }
            .toSet()
        
        // Load definitions in parallel
        val definitions = exerciseIds.mapNotNull { id ->
            val definition = exerciseDefinitionRepository.getDefinitionByIdOnce(id) 
            definition?.let { id to it }
        }.toMap()
        
        // Update UI state once with all data
        _uiState.update { 
            it.copy(
                workoutDays = plan.days,
                exerciseDefinitions = definitions,
                planName = plan.planName,
                isLoading = false
            )
        }
    }

    /**
     * Check if the device has internet connection
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun importWorkoutPlan(inputText: String) {
        if (inputText.isBlank()) {
            _uiState.update { it.copy(parseError = "Input text is empty.") }
            return
        }
        
        // Update state to show parsing is in progress
        _uiState.update { it.copy(isParsing = true, parseError = null, normalizationError = null) }

        viewModelScope.launch(Dispatchers.Default) {
            try {
                // Check for internet connectivity first
                val hasInternet = isNetworkAvailable()
                
                // Check if input is potentially JSON
                val isJson = inputText.trim().startsWith("{") && inputText.trim().endsWith("}")
                
                // Process based on format
                val textToParse = if (isJson) {
                    try {
                        // Convert JSON to text format
                        com.H_Oussama.gymplanner.data.parser.JsonWorkoutPlanParser.parseJsonToTextFormat(inputText)
                    } catch (e: Exception) {
                        _uiState.update { 
                            it.copy(
                                isParsing = false,
                                parseError = "Error parsing JSON: ${e.message ?: "Invalid JSON format"}"
                            )
                        }
                        return@launch
                    }
                } else {
                    // Use as-is for text format
                    inputText
                }
                
                // Parse the text format (either original or converted from JSON)
                val result = WorkoutPlanParser.parseWorkoutPlan(textToParse, exerciseDefinitionRepository)
                
                result.onSuccess { parsedPlan ->
                    // Show warning if no internet connection is available
                    if (!hasInternet) {
                        _uiState.update {
                            it.copy(
                                isParsing = false,
                                normalizationError = "No internet connection. The plan was imported but exercise names could not be normalized."
                            )
                        }
                        
                        // Save the plan as-is without normalization
                        workoutPlanRepository.saveWorkoutPlan(parsedPlan)
                        loadPlanData(parsedPlan)
                        
                        _uiState.update { 
                            it.copy(
                                isNormalizingNames = false,
                                workoutDays = parsedPlan.days,
                                planName = parsedPlan.planName
                            )
                        }
                        return@onSuccess
                    }
                
                    // Update to show normalizing state
                    _uiState.update { it.copy(isParsing = false, isNormalizingNames = true) }
                    
                    // Normalize exercise names using Gemini AI and the reference file
                    when (val normalizationResult = exerciseNameNormalizer.normalizeWorkoutPlanExerciseNames(parsedPlan)) {
                        is ExerciseNameNormalizer.NormalizationResult.Success -> {
                            val normalizedPlan = normalizationResult.plan
                            
                            // Save plan to repository
                            workoutPlanRepository.saveWorkoutPlan(normalizedPlan)
                            
                            // Update UI state to reflect successful import
                            _uiState.update { 
                                it.copy(
                                    isParsing = false,
                                    isNormalizingNames = false,
                                    parseError = null,
                                    normalizationError = null,
                                    // Update with newly loaded data
                                    workoutDays = normalizedPlan.days,
                                    planName = normalizedPlan.planName
                                )
                            }
                            
                            // Load exercise definitions for the imported plan
                            loadPlanData(normalizedPlan)
                        }
                        
                        is ExerciseNameNormalizer.NormalizationResult.Error -> {
                            // We still save the plan even if normalization fails
                            workoutPlanRepository.saveWorkoutPlan(parsedPlan)
                            
                            // Load the plan data
                            loadPlanData(parsedPlan)
                            
                            // Show the error but don't block plan import
                            _uiState.update {
                                it.copy(
                                    isParsing = false,
                                    isNormalizingNames = false,
                                    normalizationError = normalizationResult.message,
                                    // Update with the non-normalized plan data
                                    workoutDays = parsedPlan.days,
                                    planName = parsedPlan.planName
                                )
                            }
                        }
                    }
                }.onFailure { error ->
                    // Update the UI state with the error
                    _uiState.update { 
                        it.copy(
                            isParsing = false,
                            isNormalizingNames = false,
                            parseError = error.message ?: "Unknown parsing error"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isParsing = false,
                        isNormalizingNames = false,
                        parseError = "Error processing input: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun clearWorkoutPlan() {
        viewModelScope.launch {
            // Clear from repository
            workoutPlanRepository.clearWorkoutPlan()
            
            // UI state will be updated by the flow collector in init
        }
    }

    fun deleteWorkoutPlan() {
        viewModelScope.launch {
            // Clear the workout plan
            _uiState.update { currentState ->
                currentState.copy(
                    workoutDays = emptyList(),
                    isLoading = false,
                    error = null
                )
            }
            
            // Clear the workout plan in the repository
            workoutPlanRepository.clearWorkoutPlan()
        }
    }

    /**
     * Get the workout day for the selected weekday, if available
     */
    fun getWorkoutDayForSelectedWeekday(): WorkoutDay? {
        val workoutDays = uiState.value.workoutDays
        if (workoutDays.isEmpty()) return null
        
        // For now, just return the first day as a placeholder
        // This would need to be enhanced with actual weekday associations
        return workoutDays.firstOrNull()
    }

    /**
     * Get the workout day corresponding to today
     */
    fun getTodaysWorkout(): WorkoutDay? {
        return getWorkoutDayForSelectedWeekday()
    }

    /**
     * Update the selected weekday
     */
    fun selectWeekday(weekday: Weekday) {
        _uiState.update { currentState ->
            currentState.copy(selectedWeekday = weekday)
        }
    }

    fun editWorkoutPlan() {
        // Set a flag to enable editing mode or navigate to editor
        _uiState.update { currentState ->
            currentState.copy(isEditing = true)
        }
    }

    fun clearEditingState() {
        _uiState.update { currentState ->
            currentState.copy(isEditing = false)
        }
    }

    /**
     * Refreshes all images by clearing caches and re-initializing the image matcher.
     */
    fun refreshImages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) } // Show loading indicator
            enhancedImageMatcher.clearCaches()
            enhancedImageMatcher.initialize() // Re-initialize to preload images
            // Re-emit the current state to trigger recomposition if necessary
            // This ensures UI elements using the matcher get updated.
            // If images are loaded directly in composables that observe this state, 
            // they should recompose. If not, further state changes might be needed.
            val currentPlan = workoutPlanRepository.workoutPlanFlow.firstOrNull()
            if (currentPlan != null) {
                loadPlanData(currentPlan) // This will update isLoading to false
            } else {
                _uiState.update { it.copy(isLoading = false, workoutDays = emptyList()) }
            }
        }
    }

    /**
     * Clear the normalization error message
     */
    fun clearNormalizationError() {
        _uiState.update { it.copy(normalizationError = null) }
    }
} 