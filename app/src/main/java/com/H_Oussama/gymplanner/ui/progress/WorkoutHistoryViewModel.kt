package com.H_Oussama.gymplanner.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.H_Oussama.gymplanner.data.model.CompletedWorkout
import com.H_Oussama.gymplanner.data.repositories.CompletedWorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class WorkoutHistoryUiState(
    val completedWorkouts: List<CompletedWorkoutItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class CompletedWorkoutItem(
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

@HiltViewModel
class WorkoutHistoryViewModel @Inject constructor(
    private val completedWorkoutRepository: CompletedWorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutHistoryUiState())
    val uiState: StateFlow<WorkoutHistoryUiState> = _uiState.asStateFlow()
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
    
    init {
        loadCompletedWorkouts()
    }
    
    private fun loadCompletedWorkouts() {
        viewModelScope.launch {
            completedWorkoutRepository.getAllCompletedWorkouts()
                .catch { e ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Failed to load workout history: ${e.message}"
                    )}
                }
                .collectLatest { workouts ->
                    val workoutItems = workouts.map { workout ->
                        CompletedWorkoutItem(
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
                    
                    _uiState.update { it.copy(
                        completedWorkouts = workoutItems,
                        isLoading = false
                    )}
                }
        }
    }
} 