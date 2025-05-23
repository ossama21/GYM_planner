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
 * ViewModel for the profile screen.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    // UI state for the profile screen
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        // Load user profile data from preferences
        loadUserProfile()
    }
    
    /**
     * Load user profile data from data source.
     */
    private fun loadUserProfile() {
        // Load data synchronously since SharedPreferences reads are fast
        _uiState.update {
            it.copy(
                username = userPreferencesRepository.getUsername(),
                age = userPreferencesRepository.getAge(),
                weight = userPreferencesRepository.getWeight(),
                height = userPreferencesRepository.getHeight(),
                goal = userPreferencesRepository.getGoal(),
                workoutsPerWeek = userPreferencesRepository.getWorkoutsPerWeek(),
                bodyType = userPreferencesRepository.getBodyType(),
                totalWorkouts = 32, // These are still hardcoded as they would come from a different repository
                totalWeightLifted = 2450,
                totalTimeExercising = 28,
                email = userPreferencesRepository.getEmail()
            )
        }
    }
    
    /**
     * Update user's display name.
     */
    fun updateUsername(username: String) {
        viewModelScope.launch {
            // Save to repository
            userPreferencesRepository.saveUsername(username)
            
            // Update UI state
            _uiState.update { it.copy(username = username) }
        }
    }
    
    /**
     * Update user's age.
     */
    fun updateAge(age: Int) {
        viewModelScope.launch {
            // Save to repository
            userPreferencesRepository.saveAge(age)
            
            // Update UI state
            _uiState.update { it.copy(age = age) }
        }
    }
    
    /**
     * Update user's weight.
     */
    fun updateWeight(weight: Float) {
        viewModelScope.launch {
            // Save to repository
            userPreferencesRepository.saveWeight(weight)
            
            // Update UI state
            _uiState.update { it.copy(weight = weight) }
        }
    }
    
    /**
     * Update user's height.
     */
    fun updateHeight(height: Int) {
        viewModelScope.launch {
            // Save to repository
            userPreferencesRepository.saveHeight(height)
            
            // Update UI state
            _uiState.update { it.copy(height = height) }
        }
    }
    
    /**
     * Update user's body type.
     */
    fun updateBodyType(bodyType: String) {
        viewModelScope.launch {
            // Save to repository
            userPreferencesRepository.saveBodyType(bodyType)
            
            // Update UI state
            _uiState.update { it.copy(bodyType = bodyType) }
        }
    }
    
    /**
     * Update user's fitness goal.
     */
    fun updateGoal(goal: String) {
        viewModelScope.launch {
            // Save to repository
            userPreferencesRepository.saveGoal(goal)
            
            // Update UI state
            _uiState.update { it.copy(goal = goal) }
        }
    }
    
    /**
     * Update number of workouts per week.
     */
    fun updateWorkoutsPerWeek(count: Int) {
        viewModelScope.launch {
            // Save to repository
            userPreferencesRepository.saveWorkoutsPerWeek(count)
            
            // Update UI state
            _uiState.update { it.copy(workoutsPerWeek = count) }
        }
    }
    
    /**
     * Update user's email address.
     */
    fun updateEmail(email: String) {
        viewModelScope.launch {
            // Save to repository
            userPreferencesRepository.saveEmail(email)
            
            // Update UI state
            _uiState.update { it.copy(email = email) }
        }
    }
    
    /**
     * Request account deletion.
     */
    fun requestAccountDeletion() {
        viewModelScope.launch {
            // In a real app, this would initiate the account deletion process
            // Might require additional confirmation, password verification, etc.
        }
    }
    
    /**
     * Update UI state for preview purposes only.
     * This should NOT be used in production code.
     */
    fun updateUiStateForPreview(previewState: ProfileUiState) {
        _uiState.value = previewState
    }
}

/**
 * Represents the UI state for the profile screen.
 */
data class ProfileUiState(
    // Personal info
    val username: String = "",
    val age: Int = 0,
    val weight: Float = 0f,
    val height: Int = 0,
    val bodyType: String = UserPreferencesRepository.BODY_TYPE_MESOMORPH,
    
    // Fitness goals
    val goal: String = "",
    val workoutsPerWeek: Int = 0,
    
    // Activity stats
    val totalWorkouts: Int = 0,
    val totalWeightLifted: Int = 0,
    val totalTimeExercising: Int = 0,
    val totalCaloriesBurned: Int = 0,
    
    // Account info
    val email: String = "",
    val isEmailVerified: Boolean = false,
    
    // UI state
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) 
 
 