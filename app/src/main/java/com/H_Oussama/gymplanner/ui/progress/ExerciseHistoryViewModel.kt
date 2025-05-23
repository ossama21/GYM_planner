package com.H_Oussama.gymplanner.ui.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.H_Oussama.gymplanner.data.database.AppDatabase
import com.H_Oussama.gymplanner.data.model.SetLog
import com.H_Oussama.gymplanner.data.repositories.SetLogRepository
import com.H_Oussama.gymplanner.ui.navigation.Routes
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ExerciseHistoryUiState(
    val logs: List<SetLog> = emptyList(),
    val exerciseName: String? = null, // Get from NavArgs
    val isLoading: Boolean = true,
    val error: String? = null
)

class ExerciseHistoryViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {

    private val setLogRepository: SetLogRepository
    private val exerciseId: String? = savedStateHandle.get<String>(Routes.EXERCISE_ID_ARG)
    private val exerciseName: String? = savedStateHandle.get<String>(Routes.EXERCISE_NAME_ARG)

    private val _uiState = MutableStateFlow(ExerciseHistoryUiState(exerciseName = exerciseName ?: exerciseId))
    val uiState: StateFlow<ExerciseHistoryUiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        setLogRepository = SetLogRepository(database.setLogDao())

        loadHistory()
    }

    private fun loadHistory() {
        if (exerciseId == null) {
            _uiState.update { it.copy(isLoading = false, error = "Exercise ID not provided.") }
            return
        }

        viewModelScope.launch {
            setLogRepository.getLogsForExercise(exerciseId)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Error loading history: ${e.message}") }
                }
                .collect { logs ->
                    _uiState.update { it.copy(logs = logs, isLoading = false, error = null) }
                }
        }
    }
}