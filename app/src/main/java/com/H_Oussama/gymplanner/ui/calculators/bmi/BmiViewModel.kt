package com.H_Oussama.gymplanner.ui.calculators.bmi

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.H_Oussama.gymplanner.utils.calculators.BmiCalculator
import com.H_Oussama.gymplanner.utils.calculators.BmiCategory
import com.H_Oussama.gymplanner.utils.calculators.UnitSystem
import java.text.DecimalFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class BmiUiState(
    val weightInput: String = "",
    val heightInput: String = "",
    val selectedUnitSystem: UnitSystem = UnitSystem.METRIC,
    val bmiResult: Double? = null,
    val bmiCategory: BmiCategory? = null,
    val error: String? = null
)

@HiltViewModel
class BmiViewModel @Inject constructor() : ViewModel() {

    var uiState by mutableStateOf(BmiUiState())
        private set

    private val decimalFormat = DecimalFormat("#.0") // Format BMI to one decimal place

    fun onWeightChange(value: String) {
        uiState = uiState.copy(weightInput = value, bmiResult = null, bmiCategory = null, error = null)
    }

    fun onHeightChange(value: String) {
        uiState = uiState.copy(heightInput = value, bmiResult = null, bmiCategory = null, error = null)
    }

    fun onUnitSystemChange(system: UnitSystem) {
        // Clear inputs when changing system to avoid unit confusion
        uiState = uiState.copy(selectedUnitSystem = system, weightInput = "", heightInput = "", bmiResult = null, bmiCategory = null, error = null)
    }

    fun calculateBmi() {
        val weight = uiState.weightInput.toDoubleOrNull()
        val height = uiState.heightInput.toDoubleOrNull()

        if (weight == null || height == null || weight <= 0 || height <= 0) {
            uiState = uiState.copy(error = "Please enter valid positive numbers for weight and height.", bmiResult = null, bmiCategory = null)
            return
        }

        val bmi = BmiCalculator.calculateBmi(weight, height, uiState.selectedUnitSystem)

        if (bmi == null) {
            uiState = uiState.copy(error = "Calculation error. Check inputs.", bmiResult = null, bmiCategory = null)
        } else {
            val category = BmiCategory.fromBmi(bmi)
            uiState = uiState.copy(
                bmiResult = bmi,
                bmiCategory = category,
                error = null
            )
        }
    }

    fun getFormattedBmi(): String? {
        return uiState.bmiResult?.let { decimalFormat.format(it) }
    }

    fun getWeightLabel(): String {
        return if (uiState.selectedUnitSystem == UnitSystem.METRIC) "Weight (kg)" else "Weight (lbs)"
    }

    fun getHeightLabel(): String {
        return if (uiState.selectedUnitSystem == UnitSystem.METRIC) "Height (cm)" else "Height (inches)"
    }
}