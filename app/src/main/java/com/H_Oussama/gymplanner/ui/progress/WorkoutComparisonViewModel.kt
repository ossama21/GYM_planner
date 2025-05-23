package com.H_Oussama.gymplanner.ui.progress

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.H_Oussama.gymplanner.data.model.CompletedWorkout
import com.H_Oussama.gymplanner.data.repositories.CompletedWorkoutRepository
import com.H_Oussama.gymplanner.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class WorkoutComparisonUiState(
    val currentWorkout: WorkoutComparisonData? = null,
    val previousWorkout: WorkoutComparisonData? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val improvementStats: ImprovementStats? = null
)

data class WorkoutComparisonData(
    val id: String,
    val workoutDayId: String,
    val workoutName: String,
    val formattedDate: String,
    val rawDate: Date,
    val durationMinutes: Int,
    val totalSetsCompleted: Int,
    val totalReps: Int,
    val totalWeightLifted: Double,
    val caloriesBurned: Double
)

data class ImprovementStats(
    val durationChange: Int, // minutes
    val setsChange: Int,
    val repsChange: Int,
    val weightChange: Double,
    val caloriesChange: Double,
    val durationChangePercent: Double,
    val repsChangePercent: Double,
    val weightChangePercent: Double,
    val caloriesChangePercent: Double
)

@HiltViewModel
class WorkoutComparisonViewModel @Inject constructor(
    private val completedWorkoutRepository: CompletedWorkoutRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workoutId: String = savedStateHandle.get<String>(Routes.WORKOUT_ID_ARG) ?: ""
    
    private val _uiState = MutableStateFlow(WorkoutComparisonUiState())
    val uiState: StateFlow<WorkoutComparisonUiState> = _uiState.asStateFlow()
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
    
    init {
        loadWorkoutComparison()
    }
    
    private fun loadWorkoutComparison() {
        if (workoutId.isEmpty()) {
            _uiState.update { it.copy(
                isLoading = false,
                error = "Invalid workout ID"
            )}
            return
        }
        
        viewModelScope.launch {
            try {
                // Load the current workout
                val currentWorkout = completedWorkoutRepository.getCompletedWorkoutById(workoutId)
                
                if (currentWorkout == null) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Workout not found"
                    )}
                    return@launch
                }
                
                // Find a previous workout of the same type
                val previousWorkout = completedWorkoutRepository.getPreviousCompletedWorkout(
                    currentWorkout.workoutDayId,
                    currentWorkout.completionDate
                )
                
                // Map to UI models
                val currentWorkoutData = mapToComparisonData(currentWorkout)
                val previousWorkoutData = previousWorkout?.let { mapToComparisonData(it) }
                
                // Calculate improvement stats if we have a previous workout
                val improvementStats = if (previousWorkoutData != null) {
                    calculateImprovementStats(currentWorkoutData, previousWorkoutData)
                } else null
                
                _uiState.update { it.copy(
                    currentWorkout = currentWorkoutData,
                    previousWorkout = previousWorkoutData,
                    improvementStats = improvementStats,
                    isLoading = false
                )}
                
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Failed to load workout comparison: ${e.message}"
                )}
            }
        }
    }
    
    private fun mapToComparisonData(workout: CompletedWorkout): WorkoutComparisonData {
        return WorkoutComparisonData(
            id = workout.id,
            workoutDayId = workout.workoutDayId,
            workoutName = workout.workoutDayName,
            formattedDate = dateFormat.format(workout.completionDate),
            rawDate = workout.completionDate,
            durationMinutes = workout.durationMinutes,
            totalSetsCompleted = workout.totalSetsCompleted,
            totalReps = workout.totalReps,
            totalWeightLifted = workout.totalWeightLifted,
            caloriesBurned = workout.caloriesBurned
        )
    }
    
    private fun calculateImprovementStats(
        current: WorkoutComparisonData,
        previous: WorkoutComparisonData
    ): ImprovementStats {
        // Calculate absolute changes
        val durationChange = current.durationMinutes - previous.durationMinutes
        val setsChange = current.totalSetsCompleted - previous.totalSetsCompleted
        val repsChange = current.totalReps - previous.totalReps
        val weightChange = current.totalWeightLifted - previous.totalWeightLifted
        val caloriesChange = current.caloriesBurned - previous.caloriesBurned
        
        // Calculate percentage changes
        val durationChangePercent = calculatePercentChange(
            previous.durationMinutes.toDouble(),
            current.durationMinutes.toDouble()
        )
        val repsChangePercent = calculatePercentChange(
            previous.totalReps.toDouble(),
            current.totalReps.toDouble()
        )
        val weightChangePercent = calculatePercentChange(
            previous.totalWeightLifted,
            current.totalWeightLifted
        )
        val caloriesChangePercent = calculatePercentChange(
            previous.caloriesBurned,
            current.caloriesBurned
        )
        
        return ImprovementStats(
            durationChange = durationChange,
            setsChange = setsChange,
            repsChange = repsChange,
            weightChange = weightChange,
            caloriesChange = caloriesChange,
            durationChangePercent = durationChangePercent,
            repsChangePercent = repsChangePercent,
            weightChangePercent = weightChangePercent,
            caloriesChangePercent = caloriesChangePercent
        )
    }
    
    private fun calculatePercentChange(previous: Double, current: Double): Double {
        if (previous == 0.0) return if (current > 0) 100.0 else 0.0
        return ((current - previous) / previous) * 100.0
    }
} 