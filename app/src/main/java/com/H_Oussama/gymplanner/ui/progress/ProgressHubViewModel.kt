package com.H_Oussama.gymplanner.ui.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.H_Oussama.gymplanner.data.database.AppDatabase
import com.H_Oussama.gymplanner.data.repositories.ExerciseDefinitionRepository
import com.H_Oussama.gymplanner.data.repositories.SetLogRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Represents an exercise with historical logs
data class LoggedExerciseInfo(
    val id: String,
    val name: String // Now fetched from DB
)

data class ProgressHubUiState(
    val loggedExercises: List<LoggedExerciseInfo> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class) // For flatMapLatest
class ProgressHubViewModel(application: Application) : AndroidViewModel(application) {

    private val setLogRepository: SetLogRepository
    private val exerciseDefinitionRepository: ExerciseDefinitionRepository

    val uiState: StateFlow<ProgressHubUiState>

    init {
        val database = AppDatabase.getDatabase(application)
        setLogRepository = SetLogRepository(database.setLogDao())
        exerciseDefinitionRepository = ExerciseDefinitionRepository(database.exerciseDefinitionDao())

        // Define the uiState flow declaratively
        uiState = setLogRepository.getExerciseIdsWithLogs()
            .flatMapLatest { loggedIds -> // When IDs change, refetch names
                if (loggedIds.isEmpty()) {
                    // If no IDs, emit empty state immediately
                    flowOf(ProgressHubUiState(isLoading = false))
                } else {
                    // Fetch all definitions (could be optimized if becomes slow)
                    exerciseDefinitionRepository.getAllDefinitions()
                        .map { allDefinitions ->
                            val definitionsMap = allDefinitions.associateBy { it.id }
                            val exerciseInfos = loggedIds.mapNotNull { id ->
                                definitionsMap[id]?.let { definition ->
                                    LoggedExerciseInfo(id = definition.id, name = definition.name)
                                } // If definition not found for a logged ID, skip it (shouldn't happen ideally)
                            }
                            ProgressHubUiState(loggedExercises = exerciseInfos, isLoading = false)
                        }
                }
            }
            .catch { e -> emit(ProgressHubUiState(isLoading = false, error = "Error loading progress: ${e.message}")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ProgressHubUiState(isLoading = true)
            )
    }

    // loadLoggedExercises and mapIdsToNames are no longer needed as the flow handles it
}