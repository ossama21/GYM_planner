package com.H_Oussama.gymplanner.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.H_Oussama.gymplanner.ui.theme.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Theme mode enum
enum class ThemeMode {
    LIGHT,
    DARK
}

/**
 * ViewModel for the theme settings screen.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    // Add any repositories or use cases needed for theme functionality
) : ViewModel() {
    
    // UI state for the theme screen
    private val _uiState = MutableStateFlow(ThemeUiState())
    val uiState: StateFlow<ThemeUiState> = _uiState.asStateFlow()
    
    init {
        // Initialize with current theme preferences
        _uiState.update { 
            it.copy(
                themeMode = ThemePreferences.themeMode,
                useSystemTheme = ThemePreferences.useSystemTheme
            )
        }
        
        // Load theme settings
        loadThemeSettings()
    }
    
    /**
     * Load theme settings from preferences.
     */
    private fun loadThemeSettings() {
        viewModelScope.launch {
            // In a real app, you would load theme settings from preferences
            // For now, we're using the values from ThemePreferences
        }
    }
    
    /**
     * Set the theme mode.
     */
    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            // Update the theme mode
            _uiState.update { it.copy(themeMode = themeMode) }
            
            // When manually selecting a theme, disable "use system theme"
            if (_uiState.value.useSystemTheme) {
                _uiState.update { it.copy(useSystemTheme = false) }
                ThemePreferences.useSystemTheme = false
            }
            
            // In a real app, you would save the theme settings to preferences
        }
    }
    
    /**
     * Toggle the "use system theme" setting.
     */
    fun toggleUseSystemTheme(enabled: Boolean) {
        viewModelScope.launch {
            // Update the "use system theme" setting
            _uiState.update { it.copy(useSystemTheme = enabled) }
            
            // In a real app, you would save the theme settings to preferences
        }
    }
    
    /**
     * Apply the selected theme.
     */
    fun applyTheme() {
        viewModelScope.launch {
            // Apply the theme to the app by updating ThemePreferences
            ThemePreferences.themeMode = _uiState.value.themeMode
            ThemePreferences.useSystemTheme = _uiState.value.useSystemTheme
            
            // Log or display a message indicating the theme was applied
            val themeName = if (_uiState.value.useSystemTheme) {
                "System Default"
            } else {
                if (_uiState.value.themeMode == ThemeMode.DARK) "Dark" else "Light"
            }
            
            // This could trigger a SnackBar or some other UI feedback
            // For now, just update our state to indicate the theme was applied
            _uiState.update { it.copy(themeApplied = true) }
            
            // After a delay, reset the "themeApplied" flag
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(themeApplied = false) }
        }
    }
}

/**
 * Represents the UI state for the theme screen.
 */
data class ThemeUiState(
    // Theme settings
    val themeMode: ThemeMode = ThemeMode.DARK,
    val useSystemTheme: Boolean = false,
    
    // UI state
    val themeApplied: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) 
 
 