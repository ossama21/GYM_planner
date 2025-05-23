package com.H_Oussama.gymplanner.ui.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.H_Oussama.gymplanner.data.model.WeightEntry
import com.H_Oussama.gymplanner.data.repositories.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class WeightEntryUi(
    val id: String,
    val weight: Float,
    val date: Date,
    val formattedDate: String,
    val note: String = ""
)

data class WeightTrackerUiState(
    val weightEntries: List<WeightEntryUi> = emptyList(),
    val latestWeight: Float? = null,
    val showAddEntryDialog: Boolean = false,
    val selectedEntry: WeightEntryUi? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class WeightTrackerViewModel @Inject constructor(
    private val weightRepository: WeightRepository
) : ViewModel() {
    
    // UI state for the weight tracker
    private val _uiState = MutableStateFlow(WeightTrackerUiState(isLoading = true))
    val uiState: StateFlow<WeightTrackerUiState> = _uiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WeightTrackerUiState(isLoading = true)
    )
    
    init {
        loadWeightEntries()
    }
    
    private fun loadWeightEntries() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Collect weight entries
                weightRepository.getAllWeightEntries().collect { entries ->
                    // Format entries
                    val formattedEntries = entries.map { entry ->
                        WeightEntryUi(
                            id = entry.id,
                            weight = entry.weight,
                            date = entry.date,
                            formattedDate = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(entry.date),
                            note = entry.note
                        )
                    }
                    
                    // Get latest weight
                    val latestEntry = weightRepository.getLatestWeightEntry().stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(1000),
                        initialValue = null
                    ).value
                    
                    // Update UI state
                    _uiState.update { state ->
                        state.copy(
                            weightEntries = formattedEntries,
                            latestWeight = latestEntry?.weight,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Failed to load weight entries: ${e.message}"
                ) }
            }
        }
    }
    
    // Functions to manage the add/edit entry dialog
    fun showAddEntryDialog() {
        _uiState.update { it.copy(
            showAddEntryDialog = true,
            selectedEntry = null
        ) }
    }
    
    fun showEditEntryDialog(entry: WeightEntryUi) {
        _uiState.update { it.copy(
            showAddEntryDialog = true,
            selectedEntry = entry
        ) }
    }
    
    fun dismissEntryDialog() {
        _uiState.update { it.copy(
            showAddEntryDialog = false,
            selectedEntry = null
        ) }
    }
    
    // Function to add a new weight entry
    fun addWeightEntry(weight: Float, date: Date, note: String = "") {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val entry = WeightEntry(
                    weight = weight,
                    date = date,
                    note = note
                )
                weightRepository.addWeightEntry(entry)
                _uiState.update { it.copy(errorMessage = null) }
                dismissEntryDialog()
                loadWeightEntries() // Refresh list
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Failed to add weight entry: ${e.message}"
                ) }
            }
        }
    }
    
    // Function to update an existing weight entry
    fun updateWeightEntry(entryId: String, weight: Float, date: Date, note: String = "") {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val entry = WeightEntry(
                    id = entryId,
                    weight = weight,
                    date = date,
                    note = note
                )
                weightRepository.updateWeightEntry(entry)
                _uiState.update { it.copy(errorMessage = null) }
                dismissEntryDialog()
                loadWeightEntries() // Refresh list
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Failed to update weight entry: ${e.message}"
                ) }
            }
        }
    }
    
    // Function to delete a weight entry
    fun deleteWeightEntry(entryId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val entry = weightRepository.getWeightEntryById(entryId).stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = null
                ).value
                
                if (entry != null) {
                    weightRepository.deleteWeightEntry(entry)
                    _uiState.update { it.copy(errorMessage = null) }
                    loadWeightEntries() // Refresh list
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Failed to delete weight entry: ${e.message}"
                ) }
            }
        }
    }
    
    // Function to handle weight entry from the UI
    fun handleWeightEntry(weight: Float, date: Date, note: String = "") {
        val selectedEntry = _uiState.value.selectedEntry
        
        if (selectedEntry != null) {
            // Update existing entry
            updateWeightEntry(selectedEntry.id, weight, date, note)
        } else {
            // Add new entry
            addWeightEntry(weight, date, note)
        }
    }
} 
 
 