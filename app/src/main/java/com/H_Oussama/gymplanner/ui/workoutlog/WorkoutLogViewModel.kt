package com.H_Oussama.gymplanner.ui.workoutlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.H_Oussama.gymplanner.data.repositories.CalorieService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class WorkoutLogViewModel @Inject constructor(
    private val calorieService: CalorieService
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutLogUiState())
    val uiState: StateFlow<WorkoutLogUiState> = _uiState.asStateFlow()

    init {
        loadTodaysData()
    }

    private fun loadTodaysData() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val caloriesBurned = calorieService.getCaloriesBurnedForDate(today)
            val workoutLogs = calorieService.getWorkoutLogsForDate(today).map { log ->
                WorkoutLogUi(
                    workoutName = log.workoutName,
                    durationMinutes = log.durationMinutes,
                    intensityLevel = log.intensityLevel,
                    caloriesBurned = log.caloriesBurned,
                    date = log.date
                )
            }

            _uiState.update { state ->
                state.copy(
                    todayCaloriesBurned = caloriesBurned,
                    workoutLogs = workoutLogs,
                    isLoading = false
                )
            }
        }
    }

    fun recordWorkout(name: String, durationMinutes: Int, intensityLevel: String) {
        viewModelScope.launch {
            calorieService.recordWorkout(
                workoutName = name,
                durationMinutes = durationMinutes,
                intensityLevel = intensityLevel,
                date = LocalDate.now()
            )
            
            // Reload data after recording a workout
            loadTodaysData()
        }
    }

    data class WorkoutLogUiState(
        val todayCaloriesBurned: Int = 0,
        val workoutLogs: List<WorkoutLogUi> = emptyList(),
        val isLoading: Boolean = true,
        val errorMessage: String? = null
    )

    data class WorkoutLogUi(
        val workoutName: String,
        val durationMinutes: Int,
        val intensityLevel: String,
        val caloriesBurned: Int,
        val date: LocalDate
    )
} 