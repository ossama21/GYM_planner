package com.H_Oussama.gymplanner.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Unit system enum
enum class UnitSystem {
    METRIC,
    IMPERIAL,
    CUSTOM
}

// Weight unit enum
enum class WeightUnit {
    KILOGRAMS,
    POUNDS
}

// Height unit enum
enum class HeightUnit {
    CENTIMETERS,
    FEET_INCHES
}

// Distance unit enum
enum class DistanceUnit {
    KILOMETERS,
    MILES
}

/**
 * ViewModel for the units settings screen.
 */
@HiltViewModel
class UnitsViewModel @Inject constructor(
    // Add any repositories or use cases needed for units functionality
) : ViewModel() {
    
    // UI state for the units screen
    private val _uiState = MutableStateFlow(UnitsUiState())
    val uiState: StateFlow<UnitsUiState> = _uiState.asStateFlow()
    
    init {
        // Initialize with default values (metric system)
        // In a real app, you would load this data from preferences
        _uiState.update { 
            it.copy(
                unitSystem = UnitSystem.METRIC,
                weightUnit = WeightUnit.KILOGRAMS,
                heightUnit = HeightUnit.CENTIMETERS,
                distanceUnit = DistanceUnit.KILOMETERS
            )
        }
        
        // Load unit settings
        loadUnitSettings()
    }
    
    /**
     * Load unit settings from preferences.
     */
    private fun loadUnitSettings() {
        viewModelScope.launch {
            // In a real app, you would load unit settings from preferences
            // For now, we're using the default values set in init
        }
    }
    
    /**
     * Set the unit system.
     */
    fun setUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch {
            // Update the unit system
            _uiState.update { it.copy(unitSystem = unitSystem) }
            
            // If metric or imperial selected, update all units accordingly
            if (unitSystem == UnitSystem.METRIC) {
                _uiState.update { 
                    it.copy(
                        weightUnit = WeightUnit.KILOGRAMS,
                        heightUnit = HeightUnit.CENTIMETERS,
                        distanceUnit = DistanceUnit.KILOMETERS
                    )
                }
            } else if (unitSystem == UnitSystem.IMPERIAL) {
                _uiState.update { 
                    it.copy(
                        weightUnit = WeightUnit.POUNDS,
                        heightUnit = HeightUnit.FEET_INCHES,
                        distanceUnit = DistanceUnit.MILES
                    )
                }
            }
            
            // In a real app, you would save the unit settings to preferences
        }
    }
    
    /**
     * Set the weight unit.
     */
    fun setWeightUnit(weightUnit: WeightUnit) {
        viewModelScope.launch {
            // Update the weight unit
            _uiState.update { it.copy(weightUnit = weightUnit) }
            
            // If a unit was changed individually, update to CUSTOM system
            if (_uiState.value.unitSystem != UnitSystem.CUSTOM) {
                _uiState.update { it.copy(unitSystem = UnitSystem.CUSTOM) }
            }
            
            // In a real app, you would save the unit settings to preferences
        }
    }
    
    /**
     * Set the height unit.
     */
    fun setHeightUnit(heightUnit: HeightUnit) {
        viewModelScope.launch {
            // Update the height unit
            _uiState.update { it.copy(heightUnit = heightUnit) }
            
            // If a unit was changed individually, update to CUSTOM system
            if (_uiState.value.unitSystem != UnitSystem.CUSTOM) {
                _uiState.update { it.copy(unitSystem = UnitSystem.CUSTOM) }
            }
            
            // In a real app, you would save the unit settings to preferences
        }
    }
    
    /**
     * Set the distance unit.
     */
    fun setDistanceUnit(distanceUnit: DistanceUnit) {
        viewModelScope.launch {
            // Update the distance unit
            _uiState.update { it.copy(distanceUnit = distanceUnit) }
            
            // If a unit was changed individually, update to CUSTOM system
            if (_uiState.value.unitSystem != UnitSystem.CUSTOM) {
                _uiState.update { it.copy(unitSystem = UnitSystem.CUSTOM) }
            }
            
            // In a real app, you would save the unit settings to preferences
        }
    }
    
    /**
     * Reset all unit settings to defaults (metric system).
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            // Reset to metric system
            _uiState.update { 
                it.copy(
                    unitSystem = UnitSystem.METRIC,
                    weightUnit = WeightUnit.KILOGRAMS,
                    heightUnit = HeightUnit.CENTIMETERS,
                    distanceUnit = DistanceUnit.KILOMETERS
                )
            }
            
            // In a real app, you would save the unit settings to preferences
        }
    }
}

/**
 * Represents the UI state for the units screen.
 */
data class UnitsUiState(
    // Unit system
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    
    // Individual units
    val weightUnit: WeightUnit = WeightUnit.KILOGRAMS,
    val heightUnit: HeightUnit = HeightUnit.CENTIMETERS,
    val distanceUnit: DistanceUnit = DistanceUnit.KILOMETERS,
    
    // UI state
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) 
 
 