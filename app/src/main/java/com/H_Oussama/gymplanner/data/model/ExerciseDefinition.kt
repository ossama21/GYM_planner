package com.H_Oussama.gymplanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.H_Oussama.gymplanner.data.model.MuscleGroup

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

/**
 * Extension properties to determine muscle groups from an exercise's name or id
 * These are used for UI display and organization purposes
 */
val ExerciseDefinition.primaryMuscleGroup: MuscleGroup?
    get() {
        // First try to detect from the name
        val name = this.name.lowercase()
        
        // Check name for common muscle indicators
        return when {
            name.contains("bench") || name.contains("chest") || name.contains("pec") || 
            name.contains("fly") || name.contains("push-up") -> MuscleGroup.CHEST
            
            name.contains("pull") || name.contains("row") || name.contains("lat") || 
            name.contains("pull-up") || name.contains("deadlift") -> MuscleGroup.BACK
            
            name.contains("shoulder") || name.contains("press") || name.contains("delt") || 
            name.contains("lateral") || name.contains("overhead") -> MuscleGroup.SHOULDERS
            
            name.contains("bicep") || name.contains("curl") -> MuscleGroup.BICEPS
            
            name.contains("tricep") || name.contains("extension") || 
            name.contains("pushdown") -> MuscleGroup.TRICEPS
            
            name.contains("quad") || name.contains("leg") || name.contains("squat") || 
            name.contains("lunge") || name.contains("extension") -> MuscleGroup.QUADS
            
            name.contains("calf") || name.contains("raise") -> MuscleGroup.CALVES
            
            name.contains("forearm") || name.contains("wrist") || 
            name.contains("grip") -> MuscleGroup.FOREARMS
            
            // If we can't detect from name, try from id
            else -> detectMuscleGroupFromId()
        }
    }

val ExerciseDefinition.secondaryMuscleGroup: MuscleGroup?
    get() {
        // For compound exercises, often have secondary muscle groups
        val name = this.name.lowercase()
        
        // For certain exercises, return an appropriate secondary muscle
        return when {
            name.contains("bench") -> MuscleGroup.TRICEPS
            name.contains("row") -> MuscleGroup.BICEPS
            name.contains("deadlift") -> MuscleGroup.QUADS
            name.contains("squat") -> MuscleGroup.BACK
            name.contains("press") && name.contains("overhead") -> MuscleGroup.TRICEPS
            else -> null
        }
    }

/**
 * Helper function to detect muscle group from exercise ID if the name detection fails
 */
private fun ExerciseDefinition.detectMuscleGroupFromId(): MuscleGroup? {
    val id = this.id.lowercase()
    
    return when {
        id.contains("chest") || id.contains("bench") || id.contains("pec") -> MuscleGroup.CHEST
        id.contains("back") || id.contains("row") || id.contains("pull") -> MuscleGroup.BACK
        id.contains("shoulder") || id.contains("press") -> MuscleGroup.SHOULDERS
        id.contains("bicep") || id.contains("curl") -> MuscleGroup.BICEPS
        id.contains("tricep") -> MuscleGroup.TRICEPS
        id.contains("leg") || id.contains("quad") || id.contains("squat") -> MuscleGroup.QUADS
        id.contains("calf") -> MuscleGroup.CALVES
        id.contains("forearm") || id.contains("wrist") -> MuscleGroup.FOREARMS
        else -> null
    }
}