package com.H_Oussama.gymplanner.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.H_Oussama.gymplanner.data.repositories.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the notifications settings screen.
 */
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    // UI state for the notifications screen
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()
    
    init {
        // Load notification settings
        loadNotificationSettings()
    }
    
    /**
     * Load notification settings from preferences.
     */
    private fun loadNotificationSettings() {
        _uiState.update { 
            it.copy(
                workoutReminders = userPreferencesRepository.getWorkoutReminders(),
                restTimerAlerts = userPreferencesRepository.getRestTimerAlerts(),
                progressUpdates = userPreferencesRepository.getProgressUpdates(),
                waterReminders = userPreferencesRepository.getWaterReminders(),
                tipsAndMotivation = userPreferencesRepository.getTipsAndMotivation(),
                workoutReminderTime = userPreferencesRepository.getWorkoutReminderTime(),
                soundEnabled = userPreferencesRepository.getSoundEnabled(),
                vibrationEnabled = userPreferencesRepository.getVibrationEnabled()
            )
        }
    }
    
    /**
     * Toggle workout reminders.
     */
    fun toggleWorkoutReminders(enabled: Boolean) {
        viewModelScope.launch {
            // Save to repository
            userPreferencesRepository.saveWorkoutReminders(enabled)
            
            // Update UI state
            _uiState.update { it.copy(workoutReminders = enabled) }
        }
    }
    
    /**
     * Toggle rest timer alerts.
     */
    fun toggleRestTimerAlerts(enabled: Boolean) {
        viewModelScope.launch {
            // Save to repository
            userPreferencesRepository.saveRestTimerAlerts(enabled)
            
            // Update UI state
            _uiState.update { it.copy(restTimerAlerts = enabled) }
        }
    }
    
    /**
     * Toggle progress updates.
     */
    fun toggleProgressUpdates(enabled: Boolean) {
        viewModelScope.launch {
            // Save to repository
            userPreferencesRepository.saveProgressUpdates(enabled)
            
            // Update UI state
            _uiState.update { it.copy(progressUpdates = enabled) }
        }
    }
    
    /**
     * Toggle water reminders.
     */
    fun toggleWaterReminders(enabled: Boolean) {
        viewModelScope.launch {
            // Save to repository
            userPreferencesRepository.saveWaterReminders(enabled)
            
            // Update UI state
            _uiState.update { it.copy(waterReminders = enabled) }
        }
    }
    
    /**
     * Toggle tips and motivation.
     */
    fun toggleTipsAndMotivation(enabled: Boolean) {
        viewModelScope.launch {
            // Save to repository
            userPreferencesRepository.saveTipsAndMotivation(enabled)
            
            // Update UI state
            _uiState.update { it.copy(tipsAndMotivation = enabled) }
        }
    }
    
    /**
     * Set workout reminder time.
     */
    fun setWorkoutReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            val timeString = String.format("%02d:%02d", hour, minute)
            
            // Save to repository
            userPreferencesRepository.saveWorkoutReminderTime(timeString)
            
            // Update UI state
            _uiState.update { it.copy(workoutReminderTime = timeString) }
        }
    }
    
    /**
     * Toggle sound.
     */
    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            // Save to repository
            userPreferencesRepository.saveSoundEnabled(enabled)
            
            // Update UI state
            _uiState.update { it.copy(soundEnabled = enabled) }
        }
    }
    
    /**
     * Toggle vibration.
     */
    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch {
            // Save to repository
            userPreferencesRepository.saveVibrationEnabled(enabled)
            
            // Update UI state
            _uiState.update { it.copy(vibrationEnabled = enabled) }
        }
    }
    
    /**
     * Reset all notification settings to defaults.
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            // Reset settings in repository
            userPreferencesRepository.resetNotificationSettings()
            
            // Reload UI state
            loadNotificationSettings()
        }
    }
    
    /**
     * Update UI state for preview purposes only.
     * This should NOT be used in production code.
     */
    fun updateUiStateForPreview(previewState: NotificationsUiState) {
        _uiState.value = previewState
    }
}

/**
 * Represents the UI state for the notifications screen.
 */
data class NotificationsUiState(
    // Notification types
    val workoutReminders: Boolean = false,
    val restTimerAlerts: Boolean = false,
    val progressUpdates: Boolean = false,
    val waterReminders: Boolean = false,
    val tipsAndMotivation: Boolean = false,
    
    // Reminder times
    val workoutReminderTime: String = "08:00", // Format: "HH:mm"
    
    // Sound and vibration
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    
    // UI state
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) 
 
 