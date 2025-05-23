package com.H_Oussama.gymplanner.util

import com.H_Oussama.gymplanner.data.model.ExerciseDefinition
import com.H_Oussama.gymplanner.data.model.SetLog

/**
 * Utility class for calculating calories burned during workouts.
 */
object CalorieCalculator {
    
    /**
     * Calculates calories burned for a single set using the formula:
     * Calories = 0.0175 × MET × user_weight_kg × (duration_seconds / 60) × body_type_multiplier
     *
     * @param metValue The MET (Metabolic Equivalent of Task) value for the exercise
     * @param userWeightKg The user's weight in kilograms
     * @param durationSeconds The duration of the set in seconds
     * @param bodyTypeMultiplier The multiplier based on user's body type (default = 1.0)
     * @return The estimated calories burned
     */
    fun calculateCaloriesForSet(
        metValue: Double,
        userWeightKg: Float,
        durationSeconds: Int,
        bodyTypeMultiplier: Double = 1.0
    ): Double {
        return 0.0175 * metValue * userWeightKg * (durationSeconds / 60.0) * bodyTypeMultiplier
    }

    /**
     * Calculates calories burned for a single set given the exercise definition.
     *
     * @param exercise The exercise definition containing the MET value
     * @param userWeightKg The user's weight in kilograms
     * @param durationSeconds The duration of the set in seconds
     * @param bodyTypeMultiplier The multiplier based on user's body type
     * @return The estimated calories burned
     */
    fun calculateCaloriesForSet(
        exercise: ExerciseDefinition,
        userWeightKg: Float,
        durationSeconds: Int,
        bodyTypeMultiplier: Double
    ): Double {
        return calculateCaloriesForSet(
            metValue = exercise.met,
            userWeightKg = userWeightKg,
            durationSeconds = durationSeconds,
            bodyTypeMultiplier = bodyTypeMultiplier
        )
    }

    /**
     * Calculates total calories burned from a list of set logs.
     *
     * @param setLogs List of set logs
     * @return The sum of calories burned across all sets
     */
    fun calculateTotalCalories(setLogs: List<SetLog>): Double {
        return setLogs.sumOf { it.caloriesBurned }
    }
} 