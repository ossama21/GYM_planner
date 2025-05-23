package com.H_Oussama.gymplanner.ui.calculators.tdee

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.H_Oussama.gymplanner.utils.calculators.ActivityLevel
import com.H_Oussama.gymplanner.utils.calculators.CalorieCalculator
import com.H_Oussama.gymplanner.utils.calculators.Gender
import com.H_Oussama.gymplanner.utils.calculators.TdeeFormula
import com.H_Oussama.gymplanner.utils.calculators.UnitSystem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class TdeeUiState(
    val weightInput: String = "",
    val heightInput: String = "",
    val ageInput: String = "",
    val selectedUnitSystem: UnitSystem = UnitSystem.METRIC,
    val gender: Gender = Gender.MALE,
    val activityLevel: ActivityLevel = ActivityLevel.SEDENTARY,
    val formula: TdeeFormula = TdeeFormula.MIFFLIN_ST_JEOR,
    val bmrResult: Double? = null,
    val tdeeResult: Double? = null,
    val error: String? = null
)

@HiltViewModel
class TdeeViewModel @Inject constructor() : ViewModel() {

    var uiState by mutableStateOf(TdeeUiState())
        private set

    fun onWeightChange(value: String) {
        uiState = uiState.copy(weightInput = value, bmrResult = null, tdeeResult = null, error = null)
    }

    fun onHeightChange(value: String) {
        uiState = uiState.copy(heightInput = value, bmrResult = null, tdeeResult = null, error = null)
    }

    fun onAgeChange(value: String) {
        uiState = uiState.copy(ageInput = value.filter { it.isDigit() }, bmrResult = null, tdeeResult = null, error = null)
    }

    fun onUnitSystemChange(system: UnitSystem) {
        uiState = uiState.copy(
            selectedUnitSystem = system,
            weightInput = "", // Clear inputs on unit change
            heightInput = "",
            bmrResult = null,
            tdeeResult = null,
            error = null
        )
    }

    fun onGenderChange(gender: Gender) {
        uiState = uiState.copy(gender = gender, bmrResult = null, tdeeResult = null, error = null)
    }

    fun onActivityLevelChange(level: ActivityLevel) {
        uiState = uiState.copy(activityLevel = level, bmrResult = null, tdeeResult = null, error = null)
    }

    fun onFormulaChange(formula: TdeeFormula) {
        uiState = uiState.copy(formula = formula, bmrResult = null, tdeeResult = null, error = null)
    }

    fun calculateTdee() {
        val weight = uiState.weightInput.toDoubleOrNull()
        val height = uiState.heightInput.toDoubleOrNull()
        val age = uiState.ageInput.toIntOrNull()

        if (weight == null || height == null || age == null || weight <= 0 || height <= 0 || age <= 0) {
            uiState = uiState.copy(error = "Please enter valid positive numbers for weight, height, and age.", bmrResult = null, tdeeResult = null)
            return
        }

        val result = CalorieCalculator.calculateTdee(
            weight = weight,
            height = height,
            ageYears = age,
            isMale = uiState.gender == Gender.MALE,
            activityLevel = uiState.activityLevel,
            formula = uiState.formula,
            units = uiState.selectedUnitSystem
        )

        if (result == null) {
            uiState = uiState.copy(error = "Calculation error. Check inputs.", bmrResult = null, tdeeResult = null)
        } else {
            uiState = uiState.copy(
                bmrResult = result.first,
                tdeeResult = result.second,
                error = null
            )
        }
    }

    fun getWeightLabel(): String {
        return if (uiState.selectedUnitSystem == UnitSystem.METRIC) "Weight (kg)" else "Weight (lbs)"
    }

    fun getHeightLabel(): String {
        return if (uiState.selectedUnitSystem == UnitSystem.METRIC) "Height (cm)" else "Height (inches)"
    }
}