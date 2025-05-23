package com.H_Oussama.gymplanner.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.H_Oussama.gymplanner.data.repositories.NutritionRepository
import com.H_Oussama.gymplanner.data.repositories.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import java.util.Locale
import com.H_Oussama.gymplanner.GymPlannerApplication
import com.H_Oussama.gymplanner.MainActivity
import android.util.Log

/**
 * ViewModel for the settings screen.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val nutritionRepository: NutritionRepository
) : ViewModel() {

    // UI state for the settings screen
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Load user profile data from the repository
        refreshUserData()
        
        // Initialize Gemini API with the stored key
        initializeGeminiAPI()
        
        // Load the stored Gemini API key into UI state
        val apiKey = userPreferencesRepository.getGeminiApiKey()
        _uiState.update { it.copy(geminiApiKey = apiKey) }
        
        // Load mute state
        val isMuted = userPreferencesRepository.isIntroVideoMuted()
        _uiState.update { it.copy(isIntroMuted = isMuted) }
        
        // Load skip intro preference
        val skipIntro = userPreferencesRepository.getSkipIntro()
        _uiState.update { it.copy(skipIntro = skipIntro) }
        
        // Load language setting
        val selectedLanguage = userPreferencesRepository.getLanguage()
        _uiState.update { it.copy(language = selectedLanguage) }
    }
    
    /**
     * Refresh user profile data from the repository.
     * Call this method when returning to the Settings screen.
     */
    fun refreshUserData() {
        loadUserProfile()
    }
    
    /**
     * Initialize the Gemini API with the saved API key
     */
    private fun initializeGeminiAPI() {
        val apiKey = userPreferencesRepository.getGeminiApiKey()
        if (apiKey.isNotEmpty()) {
            nutritionRepository.initializeGeminiModel(apiKey)
        }
    }
    
    /**
     * Load user profile data from data source.
     */
    private fun loadUserProfile() {
        _uiState.update { 
            it.copy(
                username = userPreferencesRepository.getUsername(),
                age = userPreferencesRepository.getAge(),
                isSignedIn = true, // We'll assume the user is always signed in now
                weight = userPreferencesRepository.getWeight(),
                goal = userPreferencesRepository.getGoal(),
                workoutsPerWeek = userPreferencesRepository.getWorkoutsPerWeek(),
                useMetricSystem = userPreferencesRepository.getUnitSystem() == "METRIC",
                isDarkTheme = userPreferencesRepository.getThemeMode() == "DARK",
                language = userPreferencesRepository.getLanguage(),
                appVersion = "2.0.0", // Update the app version as requested
                geminiApiKey = userPreferencesRepository.getGeminiApiKey(),
                isTestingGeminiApi = false,
                geminiApiTestResult = GeminiApiTestResult.NOT_TESTED
            )
        }
    }

    // Account settings
    fun onProfileClick() {
        // Navigate to profile screen or open profile dialog
    }

    fun onNotificationsClick() {
        // Navigate to notifications settings
    }

    fun onPrivacyClick() {
        // Navigate to privacy settings
    }

    // Preferences
    fun onUnitsClick() {
        // Open unit selection dialog
    }

    fun onThemeClick() {
        // Open theme selection dialog
    }

    fun onLanguageClick() {
        // Open language selection dialog - this will be handled by the UI with showLanguageDialog
        _uiState.update { it.copy(showLanguageDialog = true) }
    }
    
    /**
     * Change the app language
     */
    fun setLanguage(context: Context, languageCode: String) {
        _uiState.update { it.copy(language = languageCode) }
        
        // Save to preferences
        viewModelScope.launch {
            userPreferencesRepository.setLanguage(languageCode)
        }
        
        // Apply language change
        applyLanguage(context, languageCode)
        
        // Restart the app to apply language changes immediately
        MainActivity.restart(context)
    }
    
    /**
     * Apply language change to the app
     */
    private fun applyLanguage(context: Context, languageCode: String) {
        // Use the GymPlannerApplication utility method to set the locale
        GymPlannerApplication.setLocale(context, languageCode)
        
        // Close the language dialog
        _uiState.update { it.copy(showLanguageDialog = false) }
    }
    
    /**
     * Dismiss the language dialog
     */
    fun dismissLanguageDialog() {
        _uiState.update { it.copy(showLanguageDialog = false) }
    }

    // Training
    fun onWorkoutGoalsClick() {
        // Show workout goals information dialog
        // This would be implemented by the navigation controller
    }

    fun onExerciseLibraryClick() {
        // Navigate to add more exercise images screen
        // This would be implemented by the navigation controller
    }

    // App
    fun onAboutClick() {
        // Show about dialog
        // This would be implemented by the navigation controller
    }

    fun onHelpClick() {
        // Show help and support dialog
        // This would be implemented by the navigation controller
    }

    // Profile actions
    fun onSignInClick() {
        // Handle sign in
        _uiState.update { it.copy(isSignedIn = true) }
    }

    fun onSettingsClick() {
        // This is already in the settings screen, could trigger a specific section to focus
    }

    fun onLogoutClick() {
        // Handle logout
        _uiState.update { it.copy(isSignedIn = false) }
    }
    
    /**
     * Update the Gemini API key
     */
    fun updateGeminiApiKey(apiKey: String) {
        _uiState.update { it.copy(geminiApiKey = apiKey) }
        
        // Save to UserPreferences
        viewModelScope.launch {
            userPreferencesRepository.setGeminiApiKey(apiKey)
            
            // Re-initialize the model
            println("DEBUG: Initializing Gemini model with new API key (length: ${apiKey.length})")
            nutritionRepository.initializeGeminiModel(apiKey)
        }
    }
    
    /**
     * Test if the Gemini API key is valid by making a simple query
     */
    fun testGeminiApiKey() {
        viewModelScope.launch {
            println("DEBUG: Starting Gemini API test")
            
            _uiState.update { it.copy(
                isTestingGeminiApi = true,
                geminiApiTestResult = GeminiApiTestResult.TESTING
            ) }
            
            // Make sure the API key is initialized in the repository
            val apiKey = _uiState.value.geminiApiKey
            println("DEBUG: Using API key (length: ${apiKey.length})")
            nutritionRepository.initializeGeminiModel(apiKey)
            
            try {
                val isValid = nutritionRepository.testGeminiApi()
                println("DEBUG: Gemini API test completed. Result: ${if (isValid) "SUCCESS" else "FAILED"}")
                
                _uiState.update { it.copy(
                    isTestingGeminiApi = false,
                    geminiApiTestResult = if (isValid) GeminiApiTestResult.SUCCESS else GeminiApiTestResult.FAILED
                ) }
            } catch (e: Exception) {
                println("ERROR: Exception during Gemini API test: ${e.message}")
                e.printStackTrace()
                
                _uiState.update { it.copy(
                    isTestingGeminiApi = false,
                    geminiApiTestResult = GeminiApiTestResult.ERROR,
                    geminiApiTestError = e.localizedMessage ?: "Unknown error"
                ) }
            }
        }
    }

    /**
     * Check if the intro video is muted
     */
    fun isIntroMuted(): Boolean {
        return _uiState.value.isIntroMuted
    }
    
    /**
     * Toggle the mute state of the intro video
     */
    fun toggleIntroMute() {
        val newMuteState = !_uiState.value.isIntroMuted
        _uiState.update { it.copy(isIntroMuted = newMuteState) }
        
        // Save to preferences
        viewModelScope.launch {
            userPreferencesRepository.setIntroVideoMuted(newMuteState)
        }
    }
    
    /**
     * Handle click on Change Intro Video
     * This will need to be implemented with actual file picker logic
     */
    fun onChangeIntroClick() {
        // This would typically launch a file picker activity
        // For now, we'll simply toggle a state to indicate it was clicked
        _uiState.update { it.copy(showIntroVideoOptions = !it.showIntroVideoOptions) }
    }

    /**
     * Toggle whether to skip the intro when starting the app
     */
    fun toggleSkipIntro() {
        viewModelScope.launch {
            try {
                // Toggle the value
                val newValue = !_uiState.value.skipIntro
                
                // Update preferences repository
                userPreferencesRepository.setSkipIntro(newValue)
                
                // Update UI state
                _uiState.update { it.copy(skipIntro = newValue) }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling skip intro setting", e)
            }
        }
    }

    companion object {
        private const val TAG = "SettingsViewModel"
    }
}

/**
 * Represents the UI state for the settings screen.
 */
data class SettingsUiState(
    // User info
    val username: String = "",
    val age: Int = 0,
    val isSignedIn: Boolean = true, // Default to true since we're removing the sign-in button
    val weight: Float = 0f,
    val goal: String = "",
    val workoutsPerWeek: Int = 0,
    
    // Settings state
    val useMetricSystem: Boolean = true,
    val isDarkTheme: Boolean = true,
    val language: String = "en", // Default to English
    val showLanguageDialog: Boolean = false,
    
    // App state
    val isPremium: Boolean = false,
    val appVersion: String = "2.0.0", // Updated version
    
    // Gemini API state
    val geminiApiKey: String = "",
    val isTestingGeminiApi: Boolean = false,
    val geminiApiTestResult: GeminiApiTestResult = GeminiApiTestResult.NOT_TESTED,
    val geminiApiTestError: String = "",
    
    // Intro video state
    val isIntroMuted: Boolean = false,
    val showIntroVideoOptions: Boolean = false,
    val skipIntro: Boolean = false
)

/**
 * Enum representing the result of testing the Gemini API
 */
enum class GeminiApiTestResult {
    NOT_TESTED,
    TESTING,
    SUCCESS,
    FAILED,
    ERROR
} 