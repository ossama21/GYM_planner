package com.H_Oussama.gymplanner.utils.calculators

import kotlin.math.roundToInt
import kotlin.math.pow

enum class ActivityLevel(val multiplier: Double) {
    SEDENTARY(1.2), // Little or no exercise
    LIGHTLY_ACTIVE(1.375), // Light exercise/sports 1-3 days/week
    MODERATELY_ACTIVE(1.55), // Moderate exercise/sports 3-5 days/week
    VERY_ACTIVE(1.725), // Hard exercise/sports 6-7 days a week
    EXTRA_ACTIVE(1.9) // Very hard exercise/sports & physical job or 2x training
}

object CalorieCalculator {

    /**
     * Calculates BMR using the Mifflin-St Jeor Equation.
     */
    private fun calculateBmrMifflinStJeor(weightKg: Double, heightCm: Double, ageYears: Int, isMale: Boolean): Double? {
        if (weightKg <= 0 || heightCm <= 0 || ageYears <= 0) return null

        val base = (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * ageYears)
        return if (isMale) base + 5 else base - 161
    }

    /**
     * Calculates BMR using the Harris-Benedict Equation (revised).
     */
    private fun calculateBmrHarrisBenedict(weightKg: Double, heightCm: Double, ageYears: Int, isMale: Boolean): Double? {
        if (weightKg <= 0 || heightCm <= 0 || ageYears <= 0) return null

        return if (isMale) {
            88.362 + (13.397 * weightKg) + (4.799 * heightCm) - (5.677 * ageYears)
        } else {
            447.593 + (9.247 * weightKg) + (3.098 * heightCm) - (4.330 * ageYears)
        }
    }

    /**
     * Calculates BMR using the Katch-McArdle Formula.
     * Note: This formula uses lean body mass which needs to be estimated if not provided.
     */
    private fun calculateBmrKatchMcArdle(weightKg: Double, heightCm: Double, ageYears: Int, isMale: Boolean): Double? {
        if (weightKg <= 0 || heightCm <= 0 || ageYears <= 0) return null

        // Estimate body fat percentage using the BMI method
        val bmi = weightKg / (heightCm / 100.0).pow(2)
        val bodyFatPercentage = if (isMale) {
            (1.20 * bmi) + (0.23 * ageYears) - 16.2
        } else {
            (1.20 * bmi) + (0.23 * ageYears) - 5.4
        }.coerceIn(5.0, 50.0) / 100.0 // Convert to decimal and limit to reasonable range

        // Calculate lean body mass
        val leanBodyMass = weightKg * (1 - bodyFatPercentage)
        
        // Apply Katch-McArdle formula
        return 370 + (21.6 * leanBodyMass)
    }

    /**
     * Calculates Basal Metabolic Rate (BMR) and Total Daily Energy Expenditure (TDEE).
     *
     * @param weight User's weight.
     * @param height User's height.
     * @param ageYears User's age in years.
     * @param isMale Whether the user is male.
     * @param activityLevel User's estimated activity level.
     * @param formula The formula to use for BMR calculation.
     * @param units The unit system used for weight and height (METRIC: kg, cm; IMPERIAL: lbs, inches).
     * @return Pair of (BMR, TDEE) as doubles, or null if input is invalid.
     */
    fun calculateTdee(
        weight: Double, 
        height: Double, 
        ageYears: Int, 
        isMale: Boolean, 
        activityLevel: ActivityLevel, 
        formula: TdeeFormula = TdeeFormula.MIFFLIN_ST_JEOR,
        units: UnitSystem
    ): Pair<Double, Double>? {
        if (weight <= 0 || height <= 0 || ageYears <= 0) return null

        val weightInKg: Double
        val heightInCm: Double

        when (units) {
            UnitSystem.METRIC -> {
                weightInKg = weight
                heightInCm = height // Assuming height is in cm for metric
            }
            UnitSystem.IMPERIAL -> {
                weightInKg = weight / BmiCalculator.LB_PER_KG // Reuse conversion constant
                heightInCm = height * (BmiCalculator.CM_PER_METER / BmiCalculator.INCHES_PER_METER) // Convert inches to cm
            }
        }

        val bmr = when (formula) {
            TdeeFormula.MIFFLIN_ST_JEOR -> calculateBmrMifflinStJeor(weightInKg, heightInCm, ageYears, isMale)
            TdeeFormula.HARRIS_BENEDICT -> calculateBmrHarrisBenedict(weightInKg, heightInCm, ageYears, isMale)
            TdeeFormula.KATCH_MCARDLE -> calculateBmrKatchMcArdle(weightInKg, heightInCm, ageYears, isMale)
        } ?: return null
        
        val tdee = bmr * activityLevel.multiplier

        return Pair(bmr, tdee)
    }
}