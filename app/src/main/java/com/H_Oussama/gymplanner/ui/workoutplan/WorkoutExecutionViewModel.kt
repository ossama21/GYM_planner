package com.H_Oussama.gymplanner.ui.workoutplan

import android.app.Application
import android.os.CountDownTimer // Import standard Android timer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.H_Oussama.gymplanner.data.database.AppDatabase
import com.H_Oussama.gymplanner.data.model.ExerciseDefinition
import com.H_Oussama.gymplanner.data.model.ExerciseInstance
import com.H_Oussama.gymplanner.data.model.SetLog
import com.H_Oussama.gymplanner.data.model.WorkoutDay
import com.H_Oussama.gymplanner.data.model.WorkoutPlan
import com.H_Oussama.gymplanner.data.repositories.ExerciseDefinitionRepository
import com.H_Oussama.gymplanner.data.repositories.SetLogRepository
import com.H_Oussama.gymplanner.data.repositories.WorkoutPlanRepository
import com.H_Oussama.gymplanner.data.repositories.UserPreferencesRepository
import com.H_Oussama.gymplanner.ui.navigation.Routes
import com.H_Oussama.gymplanner.utils.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.* // For Date
import java.util.regex.Pattern
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import androidx.lifecycle.ViewModel // Use regular ViewModel
import android.content.Context // Import standard Context
import dagger.hilt.android.qualifiers.ApplicationContext // Import Hilt qualifier
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import dagger.assisted.Assisted
import android.content.pm.PackageManager
import com.H_Oussama.gymplanner.data.model.CompletedWorkout
import com.H_Oussama.gymplanner.data.repositories.CompletedWorkoutRepository

/**
 * Data class to store exercise state when navigating between exercises
 */
data class ExerciseData(
    val loggedReps: String = "",
    val loggedWeight: String = "",
    val currentSet: Int = 1,
    val previousSetData: Map<Int, Double> = emptyMap()
)

// Represents the state of the workout execution screen
data class WorkoutExecutionUiState(
    val workoutDay: WorkoutDay? = null,
    val currentExerciseDefinition: ExerciseDefinition? = null,
    val currentExerciseIndex: Int = 0,
    val currentSet: Int = 1,
    val targetSets: Int? = null,
    val loggedReps: String = "",
    val loggedWeight: String = "",
    val timerState: TimerState = TimerState.Idle,
    val remainingTimeSeconds: Int = 0,
    val initialDurationSeconds: Int? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val workoutComplete: Boolean = false,
    val pausedState: TimerState? = null,
    val currentExerciseInstance: ExerciseInstance? = null,
    // Workout summary stats
    val totalSetsCompleted: Int = 0,
    val totalWeightLifted: Double = 0.0,
    val totalReps: Int = 0,
    val workoutStartTime: Long? = null,
    val workoutDurationSeconds: Int = 0,
    val completedExercises: Int = 0,
    val isSoundEnabled: Boolean = true,
    val shouldNavigateBack: Boolean = false,
    val previousSetData: Map<Int, Pair<Int, Double>> = emptyMap(),
    val totalCaloriesBurned: Double = 0.0,
    val currentSetStartTime: Long? = null,
    val currentSetDurationSeconds: Int = 0
)

// Represents the state of the timer
enum class TimerState {
    Idle, // Not running
    RunningExercise, // Timer for exercise duration (if applicable)
    RunningRest, // Timer for rest period
    Paused,
    Countdown // Countdown before starting an exercise
}

@HiltViewModel
class WorkoutExecutionViewModel @Inject constructor(
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val setLogRepository: SetLogRepository,
    private val exerciseDefinitionRepository: ExerciseDefinitionRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val appContext: Context?,
    private val completedWorkoutRepository: CompletedWorkoutRepository
) : ViewModel() { // Extend ViewModel

    private val dayIndex: Int = savedStateHandle.get<Int>(Routes.WORKOUT_DAY_INDEX_ARG) ?: -1

    private val _uiState = MutableStateFlow(WorkoutExecutionUiState())
    val uiState: StateFlow<WorkoutExecutionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var workoutTimerJob: Job? = null
    
    // Cache for exercise definitions to avoid repeated database lookups
    private val exerciseDefinitionCache = mutableMapOf<String, ExerciseDefinition?>()

    // Add countdown related properties
    private val COUNTDOWN_SECONDS = 3 // 3 second countdown

    // Set timer job
    private var setTimerJob: Job? = null
    
    // Map to store exercise data between navigations
    private val exerciseDataMap = mutableMapOf<Int, ExerciseData>()

    init {
        loadWorkoutDayAndExerciseDefinition()
    }

    private fun loadWorkoutDayAndExerciseDefinition() {
        viewModelScope.launch {
            try {
                // First ensure the plan is loaded
                workoutPlanRepository.ensurePlanLoaded()
                
                // Get the plan
                val plan = workoutPlanRepository.workoutPlanFlow.firstOrNull()
                if (plan == null) {
                    _uiState.value = WorkoutExecutionUiState(isLoading = false, error = "Workout plan not loaded.")
                    return@launch
                }
                if (dayIndex < 0 || dayIndex >= plan.days.size) {
                     _uiState.value = WorkoutExecutionUiState(isLoading = false, error = "Invalid workout day selected.")
                    return@launch
                }
                
                // Process plan data on a background thread
                withContext(Dispatchers.Default) {
                    val day = plan.days[dayIndex]
                    val firstExerciseInstance = day.exercises.firstOrNull()
                    
                    if (firstExerciseInstance == null) {
                        _uiState.value = WorkoutExecutionUiState(isLoading = false, error = "No exercises found for this day.")
                        return@withContext
                    }
                    
                    // Prefetch all exercise definitions for this day
                    prefetchExerciseDefinitions(day)
                    
                    val firstExerciseDefinition = getExerciseDefinition(firstExerciseInstance.exerciseId)
                    
                    // Update UI state on the main thread
                    withContext(Dispatchers.Main) {
                        _uiState.value = WorkoutExecutionUiState(
                            workoutDay = day,
                            currentExerciseInstance = firstExerciseInstance,
                            currentExerciseDefinition = firstExerciseDefinition,
                            isLoading = false,
                            targetSets = parseTargetSets(firstExerciseInstance.setsDescription),
                            workoutStartTime = System.currentTimeMillis()
                        )
                        
                        // Start workout timer to track total duration
                        startWorkoutTimer()
                    }
                }
            } catch (e: Exception) {
                _uiState.value = WorkoutExecutionUiState(isLoading = false, error = "Failed to load workout: ${e.message}")
            }
        }
    }
    
    // Prefetch all exercise definitions for better performance
    private suspend fun prefetchExerciseDefinitions(day: WorkoutDay) {
        // Get all unique exercise IDs
        val exerciseIds = day.exercises.map { it.exerciseId }.toSet()
        
        // Fetch all exercise definitions in parallel and cache them
        exerciseIds.forEach { id ->
            if (!exerciseDefinitionCache.containsKey(id)) {
                exerciseDefinitionCache[id] = exerciseDefinitionRepository.getDefinitionByIdOnce(id)
            }
        }
    }
    
    // Get exercise definition from cache or repository
    private suspend fun getExerciseDefinition(exerciseId: String): ExerciseDefinition? {
        return try {
            exerciseDefinitionCache[exerciseId] ?: 
                exerciseDefinitionRepository.getDefinitionByIdOnce(exerciseId)?.also {
                    exerciseDefinitionCache[exerciseId] = it
                }
        } catch (e: Exception) {
            Log.e("WorkoutExecution", "Error fetching exercise definition: ${e.message}")
            null
        }
    }

    // Start a timer to track overall workout duration
    private fun startWorkoutTimer() {
        workoutTimerJob = viewModelScope.launch {
            while (currentCoroutineContext().isActive) {
                delay(1000) // Update every second
                val startTime = _uiState.value.workoutStartTime ?: continue
                val currentTime = System.currentTimeMillis()
                val durationSeconds = ((currentTime - startTime) / 1000).toInt()
                
                _uiState.update { 
                    it.copy(workoutDurationSeconds = durationSeconds)
                }
            }
        }
    }

    // --- Input Handling ---
    fun onRepsChanged(reps: String) {
        val filteredReps = reps.filter { it.isDigit() }
        if (filteredReps != _uiState.value.loggedReps) {
            _uiState.update { it.copy(loggedReps = filteredReps) }
        }
    }

    fun onWeightChanged(weight: String) {
        val filteredWeight = weight.filter { it.isDigit() || it == '.' }
        if (filteredWeight != _uiState.value.loggedWeight) {
            _uiState.update { it.copy(loggedWeight = filteredWeight) }
        }
    }

    // --- Timer Control --- 

    // Function to start timer for exercise
    fun startTimer(durationMillis: Long, type: String) {
        timerJob?.cancel()
        
        val durationSeconds = (durationMillis / 1000).toInt()
        
        _uiState.update { 
            it.copy(
                timerState = if (type == "rest") TimerState.RunningRest else TimerState.RunningExercise,
                remainingTimeSeconds = durationSeconds,
                initialDurationSeconds = durationSeconds
            ) 
        }
        
        timerJob = viewModelScope.launch {
            for (i in durationSeconds downTo 1) {
                _uiState.update { it.copy(remainingTimeSeconds = i) }
                delay(1000) // 1 second between counts
            }
            
            if (isActive) {
                _uiState.update { 
                    it.copy(
                        timerState = TimerState.Idle,
                        remainingTimeSeconds = 0
                    )
                }
                
                // Show notification when timer completes
                appContext?.let { ctx ->
                    NotificationHelper.showTimerCompletedNotification(
                        ctx,
                        "Timer Complete",
                        "Your $type timer is complete!"
                    )
                }
            }
        }
    }

    fun pauseTimer() {
        if (uiState.value.timerState != TimerState.RunningExercise && 
           uiState.value.timerState != TimerState.RunningRest) return
           
        timerJob?.cancel()
        timerJob = null
        _uiState.update { it.copy(timerState = TimerState.Paused, pausedState = it.timerState) }
    }

    fun resumeTimer() {
        if (uiState.value.timerState != TimerState.Paused) return
        
        val resumeState = uiState.value.pausedState ?: TimerState.Idle
        val remainingTime = uiState.value.remainingTimeSeconds
        
        if (resumeState != TimerState.Idle && remainingTime > 0) {
             startTimer(remainingTime.toLong(), "Exercise")
        }
    }

    // Function to start a rest timer between sets
    fun startRestTimer() {
        val currentExerciseInstance = _uiState.value.currentExerciseInstance ?: return
        
        // Parse the rest time from the sets description
        val restTimeSeconds = parseRestTime(currentExerciseInstance.setsDescription)
        
        timerJob?.cancel()
        
        _uiState.update { 
            it.copy(
                timerState = TimerState.RunningRest,
                remainingTimeSeconds = restTimeSeconds,
                initialDurationSeconds = restTimeSeconds
            ) 
        }
        
        timerJob = viewModelScope.launch {
            for (i in restTimeSeconds downTo 1) {
                _uiState.update { it.copy(remainingTimeSeconds = i) }
                delay(1000) // 1 second between counts
            }
            
            if (isActive) {
                _uiState.update { 
                    it.copy(
                        timerState = TimerState.Idle,
                        remainingTimeSeconds = 0
                    )
                }
                
                // Show notification and vibrate when rest timer completes
                appContext?.let { ctx ->
                    NotificationHelper.notifyRestComplete(ctx)
                }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update { it.copy(
            timerState = TimerState.Idle, 
            pausedState = null, 
            remainingTimeSeconds = 0, 
            initialDurationSeconds = null
        ) }
    }

    // --- Workout Progression --- 

    fun finishSet() {
        viewModelScope.launch {
            try {
                // Stop the timer
                stopTimer()
                
                val currentState = _uiState.value
                val currentExerciseInstance = currentState.currentExerciseInstance ?: return@launch
                val reps = currentState.loggedReps.toIntOrNull() ?: 0
                val weight = currentState.loggedWeight.toDoubleOrNull() ?: 0.0
                
                if (reps <= 0) {
                    // Show error for invalid reps and auto-clear after 10 seconds
                    _uiState.update { it.copy(error = "Please enter valid repetitions") }
                    clearErrorAfterDelay(10000) // Clear error after 10 seconds
                    return@launch
                }
                
                // Create new map with this set's data
                val updatedPreviousSetData = currentState.previousSetData.toMutableMap()
                updatedPreviousSetData[currentState.currentSet] = Pair(reps, weight)
                
                // Log the set entry
                try {
                    val setLog = SetLog(
                        id = "", // Use an empty string for auto-generation
                        exerciseId = currentExerciseInstance.exerciseId,
                        timestamp = Date(),
                        reps = reps,
                        weight = weight
                    )
                    
                    setLogRepository.insertSetLog(setLog)
                    
                    // Update workout summary stats
                    _uiState.update { 
                        it.copy(
                            totalSetsCompleted = it.totalSetsCompleted + 1,
                            totalReps = it.totalReps + reps,
                            totalWeightLifted = it.totalWeightLifted + weight,
                            previousSetData = updatedPreviousSetData, // Update previous set data
                            error = null // Clear any previous errors
                        ) 
                    }
                    
                    // Determine if we need to go to the next exercise or stay on current one
                    val targetSets = currentState.targetSets ?: 3 // Default to 3 if not specified
                    
                    if (currentState.currentSet >= targetSets) {
                        // Move to next exercise if available
                        nextExercise()
                    } else {
                        // Stay on current exercise but increment set number
                        _uiState.update { 
                            it.copy(
                                currentSet = it.currentSet + 1,
                                loggedReps = "",
                                loggedWeight = ""
                            ) 
                        }
                        
                        // If rest time is specified, start rest timer
                        val restTime = parseRestTime(currentExerciseInstance.setsDescription)
                        if (restTime > 0) {
                            startRestTimer()
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = "Failed to save set: ${e.message}") }
                    clearErrorAfterDelay(10000) // Clear error after 10 seconds
                    Log.e("WorkoutExecution", "Error saving set log: ${e.message}")
                }
            } catch (e: Exception) {
                Log.e("WorkoutExecution", "Error in finishSet: ${e.message}")
                _uiState.update { it.copy(error = "An error occurred. Please try again.") }
                clearErrorAfterDelay(10000) // Clear error after 10 seconds
            }
        }
    }

    // Add function to clear errors after a delay
    private fun clearErrorAfterDelay(delayMillis: Long) {
        viewModelScope.launch {
            delay(delayMillis)
            _uiState.update { it.copy(error = null) }
        }
    }

    // Public function to clear errors immediately
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Navigate to the previous exercise in the workout
     */
    fun previousExercise() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.currentExerciseIndex <= 0 || currentState.workoutDay == null) {
                return@launch
            }

            // Save current exercise data
            val currentIndex = currentState.currentExerciseIndex
            exerciseDataMap[currentIndex] = ExerciseData(
                loggedReps = currentState.loggedReps,
                loggedWeight = currentState.loggedWeight,
                currentSet = currentState.currentSet,
                previousSetData = currentState.previousSetData.mapValues { it.value.second }
            )

            // Get previous exercise index
            val previousIndex = currentIndex - 1
            val previousExercise = currentState.workoutDay.exercises[previousIndex]
            
            // Get the exercise definition for the previous exercise
            val exerciseId = previousExercise.exerciseId
            val previousExerciseDefinition = getExerciseDefinition(exerciseId)
            
            // Retrieve saved data for the previous exercise if it exists
            val previousData = exerciseDataMap[previousIndex] ?: ExerciseData()

            // Update state with previous exercise and its data
            _uiState.update { state ->
                state.copy(
                    currentExerciseIndex = previousIndex,
                    currentExerciseDefinition = previousExerciseDefinition,
                    currentExerciseInstance = previousExercise,
                    timerState = TimerState.Idle,
                    loggedReps = previousData.loggedReps,
                    loggedWeight = previousData.loggedWeight,
                    currentSet = previousData.currentSet,
                    previousSetData = previousData.previousSetData.mapValues { (key, value) -> Pair(key, value) },
                    error = null
                )
            }

            // Stop any running timers
            stopTimer()
        }
    }

    /**
     * Navigate to the next exercise in the workout
     */
    fun nextExercise() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val workoutDay = currentState.workoutDay ?: return@launch

            // Check if we're already at the last exercise
            if (currentState.currentExerciseIndex >= workoutDay.exercises.size - 1) {
                return@launch
            }

            // Save current exercise data
            val currentIndex = currentState.currentExerciseIndex
            exerciseDataMap[currentIndex] = ExerciseData(
                loggedReps = currentState.loggedReps,
                loggedWeight = currentState.loggedWeight,
                currentSet = currentState.currentSet,
                previousSetData = currentState.previousSetData.mapValues { it.value.second }
            )

            // Get next exercise index
            val nextIndex = currentIndex + 1
            val nextExercise = workoutDay.exercises[nextIndex]

            // Get the exercise definition for the next exercise
            val exerciseId = nextExercise.exerciseId
            val nextExerciseDefinition = getExerciseDefinition(exerciseId)

            // Retrieve saved data for the next exercise if it exists
            val nextData = exerciseDataMap[nextIndex] ?: ExerciseData()

            // Update state with next exercise and its data
            _uiState.update { state ->
                state.copy(
                    currentExerciseIndex = nextIndex,
                    currentExerciseDefinition = nextExerciseDefinition,
                    currentExerciseInstance = nextExercise,
                    timerState = TimerState.Idle,
                    loggedReps = nextData.loggedReps,
                    loggedWeight = nextData.loggedWeight,
                    currentSet = nextData.currentSet,
                    previousSetData = nextData.previousSetData.mapValues { (key, value) -> Pair(key, value) },
                    error = null
                )
            }

            // Stop any running timers
            stopTimer()
        }
    }

    fun finishWorkout() {
        stopTimer()
        workoutTimerJob?.cancel()
        _uiState.update { it.copy(
            workoutComplete = true, 
            timerState = TimerState.Idle,
            shouldNavigateBack = false // Explicitly set to false to prevent auto-navigation
        ) }
    }
    
    fun skipRest() {
        if (uiState.value.timerState == TimerState.RunningRest || 
            uiState.value.timerState == TimerState.Paused) {
            stopTimer()
            
            // Simply end the rest period without advancing the set
            _uiState.update {
                it.copy(timerState = TimerState.Idle)
            }
        }
    }

    // --- Helper --- 
    private fun parseTargetSets(description: String): Int? {
        // Use cached regex pattern for better performance
        val pattern = Pattern.compile("\\d+")
        val matcher = pattern.matcher(description)
        return if (matcher.find()) {
            val matchResult = matcher.group(0)
            matchResult?.toIntOrNull() ?: 1
        } else {
            1 // Default to 1 set if no number found
        }
    }

    // Override onCleared to ensure timer is cancelled
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        workoutTimerJob?.cancel()
    }

    // Function to start countdown timer before an exercise
    fun startExerciseCountdown() {
        timerJob?.cancel()
        
        _uiState.update { 
            it.copy(
                timerState = TimerState.Countdown,
                remainingTimeSeconds = COUNTDOWN_SECONDS,
                initialDurationSeconds = COUNTDOWN_SECONDS
            ) 
        }
        
        timerJob = viewModelScope.launch {
            for (i in COUNTDOWN_SECONDS downTo 1) {
                _uiState.update { it.copy(remainingTimeSeconds = i) }
                delay(1000) // 1 second between counts
            }
            
            if (isActive) {
                _uiState.update { 
                    it.copy(
                        timerState = TimerState.Idle,
                        remainingTimeSeconds = 0
                    )
                }
                
                // Vibrate when countdown completes
                appContext?.let { ctx ->
                    NotificationHelper.vibrate(ctx, 300)
                }
            }
        }
    }

    // Add helper method to parse rest time
    private fun parseRestTime(setsDescription: String?): Int {
        if (setsDescription == null) return 60 // Default 60 seconds
        
        try {
            // Look for patterns like "90s" or "60 seconds"
            val restTimePattern = Pattern.compile("(\\d+)\\s*(?:s|sec|seconds)")
            val matcher = restTimePattern.matcher(setsDescription)
            
            if (matcher.find()) {
                return matcher.group(1)?.toIntOrNull() ?: 60
            }
        } catch (e: Exception) {
            Log.e("WorkoutExecution", "Error parsing rest time: ${e.message}")
        }
        
        return 60 // Default rest time
    }

    // Function to add a new set
    fun addSet() {
        val currentTargetSets = _uiState.value.targetSets ?: 3
        _uiState.update { 
            it.copy(targetSets = currentTargetSets + 1)
        }
    }

    // Function to toggle sound
    fun toggleSound() {
        _uiState.update { it.copy(isSoundEnabled = !it.isSoundEnabled) }
        
        // Update the notification helper to respect this setting
        appContext?.let { ctx ->
            NotificationHelper.setSoundEnabled(ctx, !uiState.value.isSoundEnabled)
        }
    }
    
    // Function to control navigation after workout completion
    fun navigateFromWorkoutSummary() {
        _uiState.update { it.copy(shouldNavigateBack = true) }
    }

    // Start a timer to track individual set duration
    private fun startSetTimer() {
        // Cancel any existing timer
        setTimerJob?.cancel()
        
        // Set the start time
        val startTime = System.currentTimeMillis()
        _uiState.update { it.copy(currentSetStartTime = startTime, currentSetDurationSeconds = 0) }
        
        // Start a new timer
        setTimerJob = viewModelScope.launch {
            while (currentCoroutineContext().isActive) {
                delay(1000) // Update every second
                val currentState = _uiState.value
                val setStartTime = currentState.currentSetStartTime ?: continue
                val currentTime = System.currentTimeMillis()
                val durationSeconds = ((currentTime - setStartTime) / 1000).toInt()
                
                _uiState.update { it.copy(currentSetDurationSeconds = durationSeconds) }
            }
        }
    }
    
    // Stop the set timer
    private fun stopSetTimer(): Int {
        setTimerJob?.cancel()
        setTimerJob = null
        return _uiState.value.currentSetDurationSeconds
    }
    
    // Method to log a completed set with calorie calculation
    fun logCompletedSet() {
        viewModelScope.launch {
            try {
                // Get current state
                val currentState = _uiState.value
                val currentExerciseInstance = currentState.currentExerciseInstance 
                    ?: throw IllegalStateException("No current exercise")
                    
                val exerciseDefinition = currentState.currentExerciseDefinition
                    ?: throw IllegalStateException("No exercise definition")
                
                // Stop the set timer and get duration
                val durationSeconds = stopSetTimer()
                
                // Parse user input
                val reps = currentState.loggedReps.toIntOrNull() ?: 0
                val weight = currentState.loggedWeight.toDoubleOrNull() ?: 0.0
                
                // Calculate calories burned
                val userWeight = userPreferencesRepository.getWeight()
                val bodyTypeMultiplier = userPreferencesRepository.getBodyTypeMultiplier()
                
                val caloriesBurned = com.H_Oussama.gymplanner.util.CalorieCalculator.calculateCaloriesForSet(
                    metValue = exerciseDefinition.met,
                    userWeightKg = userWeight,
                    durationSeconds = durationSeconds,
                    bodyTypeMultiplier = bodyTypeMultiplier
                )
                
                // Update previous set data for UI
                val updatedPreviousSetData = currentState.previousSetData.toMutableMap()
                updatedPreviousSetData[currentState.currentSet] = Pair(reps, weight)
                
                // Create and save set log
                try {
                    val setLog = SetLog(
                        id = "", // Use an empty string for auto-generation
                        exerciseId = currentExerciseInstance.exerciseId,
                        timestamp = Date(),
                        reps = reps,
                        weight = weight,
                        durationSeconds = durationSeconds,
                        caloriesBurned = caloriesBurned
                    )
                    
                    setLogRepository.insertSetLog(setLog)
                    
                    // Update workout summary stats
                    _uiState.update { 
                        it.copy(
                            totalSetsCompleted = it.totalSetsCompleted + 1,
                            totalReps = it.totalReps + reps,
                            totalWeightLifted = it.totalWeightLifted + weight,
                            previousSetData = updatedPreviousSetData, // Update previous set data
                            totalCaloriesBurned = it.totalCaloriesBurned + caloriesBurned,
                            error = null // Clear any previous errors
                        ) 
                    }
                    
                    // Determine if we need to go to the next exercise or stay on current one
                    val targetSets = currentState.targetSets ?: 3 // Default to 3 if not specified
                    
                    if (currentState.currentSet >= targetSets) {
                        // Move to next exercise if available
                        nextExercise()
                    } else {
                        // Move to next set of current exercise
                        _uiState.update {
                            it.copy(
                                currentSet = it.currentSet + 1,
                                loggedReps = "",
                                loggedWeight = ""
                            )
                        }
                        
                        // Start rest timer if this isn't the last set
                        startRestTimer()
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = "Failed to log set: ${e.message}") }
                    clearErrorAfterDelay()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error logging set: ${e.message}") }
                clearErrorAfterDelay()
            }
        }
    }
    
    // Helper method to clear errors after a delay
    private fun clearErrorAfterDelay() {
        viewModelScope.launch {
            delay(3000) // Clear error after 3 seconds
            _uiState.update { it.copy(error = null) }
        }
    }

    // Call startSetTimer when beginning an exercise set
    fun startExerciseSet() {
        // Stop any active timers
        timerJob?.cancel()
        
        // Update UI state
        _uiState.update { 
            it.copy(
                timerState = TimerState.RunningExercise,
                remainingTimeSeconds = 0
            )
        }
        
        // Start set timer
        startSetTimer()
    }
    
    // Function to save workout and navigate back
    fun finishAndSaveWorkout() {
        viewModelScope.launch {
            // Send analytics or save additional workout data if needed
            try {
                val currentState = _uiState.value
                val workoutDay = currentState.workoutDay
                
                if (workoutDay != null) {
                    // Create and save CompletedWorkout record
                    val completedWorkout = CompletedWorkout(
                        workoutDayId = workoutDay.id,
                        workoutDayName = workoutDay.dayName,
                        completionDate = Date(), // Current date and time
                        durationMinutes = currentState.workoutDurationSeconds / 60,
                        totalSetsCompleted = currentState.totalSetsCompleted,
                        totalReps = currentState.totalReps,
                        totalWeightLifted = currentState.totalWeightLifted,
                        caloriesBurned = currentState.totalCaloriesBurned
                    )
                    
                    // Save to the repository
                    completedWorkoutRepository.insertCompletedWorkout(completedWorkout)
                }
                
                // Update UI state to navigate back
                _uiState.update { it.copy(shouldNavigateBack = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to save workout: ${e.message}") }
            }
        }
    }
    
    // Function to skip the rest timer
    fun skipRestTimer() {
        timerJob?.cancel()
        timerJob = null
        
        // Update UI state
        _uiState.update { 
            it.copy(
                timerState = TimerState.Idle,
                remainingTimeSeconds = 0
            )
        }
    }
    
    // Function to parse target sets from description
    private fun parseTargetSets(setsDescription: String?): Int {
        if (setsDescription == null) return 3 // Default to 3 sets
        
        // Look for patterns like "3 sets" or "3x12"
        val setsPattern = Pattern.compile("(\\d+)\\s*(?:sets|x)")
        val matcher = setsPattern.matcher(setsDescription)
        
        if (matcher.find()) {
            return matcher.group(1)?.toIntOrNull() ?: 3
        }
        
        return 3 // Default
    }
} 