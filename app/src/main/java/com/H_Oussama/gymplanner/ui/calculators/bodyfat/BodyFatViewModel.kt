package com.H_Oussama.gymplanner.ui.calculators.bodyfat

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.H_Oussama.gymplanner.utils.calculators.BodyFatCalculator
import com.H_Oussama.gymplanner.utils.calculators.BodyFatCategory
import com.H_Oussama.gymplanner.utils.calculators.BodyFatMethod
import com.H_Oussama.gymplanner.utils.calculators.Gender
import com.H_Oussama.gymplanner.utils.calculators.UnitSystem
import java.text.DecimalFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class BodyFatUiState(
    val heightInput: String = "",
    val neckInput: String = "",
    val waistInput: String = "",
    val hipInput: String = "", // Only used for females
    val ageInput: String = "", // Used for BMI method
    val weightInput: String = "", // Used for BMI method
    val selectedUnitSystem: UnitSystem = UnitSystem.METRIC,
    val gender: Gender = Gender.MALE,
    val selectedMethod: BodyFatMethod = BodyFatMethod.NAVY,
    val bodyFatResult: Double? = null,
    val bodyFatCategory: BodyFatCategory? = null,
    val error: String? = null
)

@HiltViewModel
class BodyFatViewModel @Inject constructor() : ViewModel() {

    var uiState by mutableStateOf(BodyFatUiState())
        private set

    private val decimalFormat = DecimalFormat("#.0") // Format result to one decimal place

    fun onHeightChange(value: String) {
        uiState = uiState.copy(heightInput = value, bodyFatResult = null, bodyFatCategory = null, error = null)
    }

    fun onNeckChange(value: String) {
        uiState = uiState.copy(neckInput = value, bodyFatResult = null, bodyFatCategory = null, error = null)
    }

    fun onWaistChange(value: String) {
        uiState = uiState.copy(waistInput = value, bodyFatResult = null, bodyFatCategory = null, error = null)
    }

    fun onHipChange(value: String) {
        // Only relevant for females, but update anyway
        uiState = uiState.copy(hipInput = value, bodyFatResult = null, bodyFatCategory = null, error = null)
    }

    fun onAgeChange(value: String) {
        uiState = uiState.copy(ageInput = value, bodyFatResult = null, bodyFatCategory = null, error = null)
    }

    fun onWeightChange(value: String) {
        uiState = uiState.copy(weightInput = value, bodyFatResult = null, bodyFatCategory = null, error = null)
    }

    fun onUnitSystemChange(system: UnitSystem) {
        uiState = uiState.copy(
            selectedUnitSystem = system,
            heightInput = "", // Clear inputs on unit change
            neckInput = "",
            waistInput = "",
            hipInput = "",
            weightInput = "",
            bodyFatResult = null,
            bodyFatCategory = null,
            error = null
        )
    }

    fun onGenderChange(gender: Gender) {
        uiState = uiState.copy(
            gender = gender, 
            bodyFatResult = null, 
            bodyFatCategory = null, 
            error = null, 
            hipInput = if (gender == Gender.MALE) "" else uiState.hipInput
        ) // Clear hip if changing to male
    }

    fun onMethodChange(method: BodyFatMethod) {
        uiState = uiState.copy(
            selectedMethod = method,
            bodyFatResult = null,
            bodyFatCategory = null,
            error = null
        )
    }

    fun calculateBodyFat() {
        when (uiState.selectedMethod) {
            BodyFatMethod.NAVY -> calculateNavyMethod()
            BodyFatMethod.BMI -> calculateBmiMethod()
        }
    }

    private fun calculateNavyMethod() {
        val height = uiState.heightInput.toDoubleOrNull()
        val neck = uiState.neckInput.toDoubleOrNull()
        val waist = uiState.waistInput.toDoubleOrNull()
        val hip = if (uiState.gender == Gender.FEMALE) uiState.hipInput.toDoubleOrNull() else null

        if (height == null || neck == null || waist == null || height <= 0 || neck <= 0 || waist <= 0) {
            uiState = uiState.copy(error = "Please enter valid positive numbers for height, neck, and waist.", bodyFatResult = null, bodyFatCategory = null)
            return
        }
        if (uiState.gender == Gender.FEMALE && (hip == null || hip <= 0)) {
             uiState = uiState.copy(error = "Please enter a valid positive number for hip circumference for females.", bodyFatResult = null, bodyFatCategory = null)
            return
        }

        val result = BodyFatCalculator.calculateNavyBodyFat(
            isMale = uiState.gender == Gender.MALE,
            height = height,
            neckCircumference = neck,
            waistCircumference = waist,
            hipCircumference = hip, // Null if male
            units = uiState.selectedUnitSystem
        )

        if (result == null) {
            uiState = uiState.copy(error = "Calculation error. Check inputs.", bodyFatResult = null, bodyFatCategory = null)
        } else {
            val category = BodyFatCategory.fromBodyFat(result, uiState.gender == Gender.MALE)
            uiState = uiState.copy(
                bodyFatResult = result,
                bodyFatCategory = category,
                error = null
            )
        }
    }

    private fun calculateBmiMethod() {
        val height = uiState.heightInput.toDoubleOrNull()
        val weight = uiState.weightInput.toDoubleOrNull()
        val age = uiState.ageInput.toDoubleOrNull()

        if (height == null || weight == null || age == null || height <= 0 || weight <= 0 || age <= 0) {
            uiState = uiState.copy(error = "Please enter valid positive numbers for height, weight, and age.", bodyFatResult = null, bodyFatCategory = null)
            return
        }

        val result = BodyFatCalculator.estimateBodyFatFromBmi(
            isMale = uiState.gender == Gender.MALE,
            height = height,
            weight = weight,
            age = age.toInt(),
            units = uiState.selectedUnitSystem
        )

        if (result == null) {
            uiState = uiState.copy(error = "Calculation error. Check inputs.", bodyFatResult = null, bodyFatCategory = null)
        } else {
            val category = BodyFatCategory.fromBodyFat(result, uiState.gender == Gender.MALE)
            uiState = uiState.copy(
                bodyFatResult = result,
                bodyFatCategory = category,
                error = null
            )
        }
    }

    fun getFormattedBodyFat(): String? {
        return uiState.bodyFatResult?.let { decimalFormat.format(it) }
    }

    fun getHeightLabel(): String {
        return if (uiState.selectedUnitSystem == UnitSystem.METRIC) "Height (cm)" else "Height (inches)"
    }

    fun getWeightLabel(): String {
        return if (uiState.selectedUnitSystem == UnitSystem.METRIC) "Weight (kg)" else "Weight (lbs)"
    }

    fun getWaistLabel(): String {
        val unit = if (uiState.selectedUnitSystem == UnitSystem.METRIC) "cm" else "inches"
        return "Waist Circumference ($unit)"
    }

    fun getNeckLabel(): String {
        val unit = if (uiState.selectedUnitSystem == UnitSystem.METRIC) "cm" else "inches"
        return "Neck Circumference ($unit)"
    }

    fun getHipLabel(): String {
        val unit = if (uiState.selectedUnitSystem == UnitSystem.METRIC) "cm" else "inches"
        return "Hip Circumference ($unit)"
    }
}