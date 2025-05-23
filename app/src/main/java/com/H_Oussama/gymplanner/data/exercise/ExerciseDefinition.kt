package com.H_Oussama.gymplanner.data.exercise

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents the static definition of an exercise.
 *
 * @param id Unique identifier for the exercise.
 * @param name The name of the exercise (e.g., "Bench Press").
 * @param description Optional description or instructions for the exercise.
 * @param imageIdentifier A string key to link this exercise to its corresponding image file.
 * @param met Default MET value, adjust as needed
 */
@Entity(tableName = "exercise_definitions")
data class ExerciseDefinition(
    @PrimaryKey
    val id: String, // Could be a UUID or a unique name key
    val name: String,
    val description: String? = null,
    val imageIdentifier: String, // e.g., "bench_press.jpg" or just "bench_press"
    val met: Double = 3.5 // Default MET value, adjust as needed
) 