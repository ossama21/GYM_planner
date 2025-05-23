package com.H_Oussama.gymplanner.utils.calculators

import kotlin.math.log10
import kotlin.math.pow

object BodyFatCalculator {

    /**
     * Estimates body fat percentage using the U.S. Navy method.
     * Requires circumference measurements.
     *
     * @param isMale User's biological sex (true for male, false for female).
     * @param height User's height.
     * @param neckCircumference Circumference of the neck.
     * @param waistCircumference Circumference of the waist (at navel level).
     * @param hipCircumference Circumference of the hips (only required for females).
     * @param units Unit system for height and circumferences (METRIC: cm; IMPERIAL: inches).
     * @return Estimated body fat percentage, or null if input is invalid.
     */
    fun calculateNavyBodyFat(
        isMale: Boolean,
        height: Double,
        neckCircumference: Double,
        waistCircumference: Double,
        hipCircumference: Double? = null, // Required only for females
        units: UnitSystem
    ): Double? {

        if (height <= 0 || neckCircumference <= 0 || waistCircumference <= 0) return null
        if (!isMale && (hipCircumference == null || hipCircumference <= 0)) return null

        val heightVal: Double
        val neckVal: Double
        val waistVal: Double
        val hipVal: Double? // Null for males

        // Convert all measurements to inches for the formula
        when (units) {
            UnitSystem.METRIC -> {
                val cmToInch = 1.0 / 2.54
                heightVal = height * cmToInch
                neckVal = neckCircumference * cmToInch
                waistVal = waistCircumference * cmToInch
                hipVal = hipCircumference?.times(cmToInch)
            }
            UnitSystem.IMPERIAL -> {
                heightVal = height
                neckVal = neckCircumference
                waistVal = waistCircumference
                hipVal = hipCircumference
            }
        }

        return try {
            if (isMale) {
                val factor = waistVal - neckVal
                if (factor <= 0 || heightVal <= 0) return null // Avoid log of non-positive
                val percentage = 86.010 * log10(factor) - 70.041 * log10(heightVal) + 36.76
                percentage.coerceIn(0.0, 100.0) // Ensure result is within bounds
            } else {
                if (hipVal == null || hipVal <= 0) return null
                val factor = waistVal + hipVal - neckVal
                if (factor <= 0 || heightVal <= 0) return null // Avoid log of non-positive
                val percentage = 163.205 * log10(factor) - 97.684 * log10(heightVal) - 78.387
                percentage.coerceIn(0.0, 100.0) // Ensure result is within bounds
            }
        } catch (e: Exception) {
            // Catch potential log10 domain errors or other math issues
            null
        }
    }

    /**
     * Estimates body fat percentage based on BMI, age, and gender.
     * This is a simplified method with limited accuracy.
     *
     * @param isMale User's gender (true for male, false for female).
     * @param height User's height.
     * @param weight User's weight.
     * @param age User's age.
     * @param units Unit system (METRIC: kg, cm; IMPERIAL: lbs, inches).
     * @return Estimated body fat percentage, or null if input is invalid.
     */
    fun estimateBodyFatFromBmi(
        isMale: Boolean,
        height: Double,
        weight: Double,
        age: Int,
        units: UnitSystem
    ): Double? {
        // Calculate BMI first
        val bmi = BmiCalculator.calculateBmi(weight, height, units) ?: return null
        
        // Apply the Deurenberg equation for body fat percentage
        val bodyFatPercentage = if (isMale) {
            (1.20 * bmi) + (0.23 * age) - 16.2
        } else {
            (1.20 * bmi) + (0.23 * age) - 5.4
        }
        
        return bodyFatPercentage.coerceIn(0.0, 100.0) // Ensure result is within bounds
    }
} 