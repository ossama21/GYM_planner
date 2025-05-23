package com.H_Oussama.gymplanner.utils.calculators

import kotlin.math.pow

enum class UnitSystem {
    METRIC, IMPERIAL
}

object BmiCalculator {

    internal const val LB_PER_KG = 2.20462
    internal const val INCHES_PER_METER = 39.3701
    internal const val CM_PER_METER = 100.0
    internal const val FEET_PER_METER = 3.28084
    internal const val INCHES_PER_FOOT = 12.0

    /**
     * Calculates Body Mass Index (BMI).
     *
     * @param weight User's weight.
     * @param height User's height.
     * @param units The unit system used for weight and height (METRIC: kg, cm; IMPERIAL: lbs, inches).
     * @return Calculated BMI value, or null if input is invalid (e.g., non-positive values).
     */
    fun calculateBmi(weight: Double, height: Double, units: UnitSystem): Double? {
        if (weight <= 0 || height <= 0) return null

        val weightInKg: Double
        val heightInMeters: Double

        when (units) {
            UnitSystem.METRIC -> {
                weightInKg = weight
                heightInMeters = height / CM_PER_METER // Assuming height is in cm for metric
            }
            UnitSystem.IMPERIAL -> {
                weightInKg = weight / LB_PER_KG
                heightInMeters = height / INCHES_PER_METER // Assuming height is in inches for imperial
            }
        }

        if (heightInMeters <= 0) return null // Avoid division by zero

        return weightInKg / heightInMeters.pow(2)
    }

    /**
     * Provides a general category based on BMI value.
     * Note: BMI categories can vary slightly and have limitations.
     */
    fun getBmiCategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25 -> "Normal weight"
            bmi < 30 -> "Overweight"
            else -> "Obese"
        }
    }
} 