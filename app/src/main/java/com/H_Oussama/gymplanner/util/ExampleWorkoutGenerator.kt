package com.H_Oussama.gymplanner.util

import android.content.Context
import com.H_Oussama.gymplanner.data.model.ExerciseDefinition
import com.H_Oussama.gymplanner.data.model.MuscleGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Utility class for generating example workout plans with
 * exercises that match the available exercise images.
 */
class ExampleWorkoutGenerator(private val context: Context) {
    // Cache for available exercise images
    private var availableImageFiles: List<String>? = null
    
    /**
     * Get a list of available exercise image files (without extensions)
     */
    suspend fun getAvailableExerciseImageFiles(): List<String> = withContext(Dispatchers.IO) {
        if (availableImageFiles != null) {
            return@withContext availableImageFiles!!
        }
        
        try {
            val assetManager = context.assets
            val imageFiles = assetManager.list("exercise_images") ?: emptyArray()
            
            availableImageFiles = imageFiles.map { filename ->
                filename.substringBeforeLast(".")
            }.distinct()
            
            return@withContext availableImageFiles!!
        } catch (e: Exception) {
            return@withContext emptyList()
        }
    }
    
    /**
     * Generate example exercise definitions based on available image files
     */
    suspend fun generateExampleExercises(): List<ExerciseDefinition> = withContext(Dispatchers.IO) {
        val imageFiles = getAvailableExerciseImageFiles()
        
        val exercises = imageFiles.map { imageFile ->
            // Convert kebab-case to readable name
            val displayName = imageFile
                .replace("-", " ")
                .split(" ")
                .joinToString(" ") { word ->
                    word.replaceFirstChar { it.uppercase() }
                }
            
            // Convert kebab-case to snake_case for ID
            val id = imageFile.replace("-", "_")
            
            ExerciseDefinition(
                id = id,
                name = displayName,
                description = getExerciseDescription(displayName),
                imageIdentifier = imageFile
            )
        }
        
        return@withContext exercises
    }
    
    /**
     * Generate a complete example workout plan text based on available exercises
     */
    suspend fun generateExampleWorkoutPlanText(): String = withContext(Dispatchers.IO) {
        val exercises = generateExampleExercises()
        
        // Group exercises by potential muscle groups based on name
        val chestExercises = exercises.filter { 
            it.name.lowercase().contains("bench") || 
            it.name.lowercase().contains("press") ||
            it.name.lowercase().contains("push") ||
            it.name.lowercase().contains("fly") ||
            it.name.lowercase().contains("chest")
        }.take(4)
        
        val backExercises = exercises.filter {
            it.name.lowercase().contains("row") ||
            it.name.lowercase().contains("pull") ||
            it.name.lowercase().contains("lat") ||
            it.name.lowercase().contains("back")
        }.take(4)
        
        val legExercises = exercises.filter {
            it.name.lowercase().contains("squat") ||
            it.name.lowercase().contains("leg") ||
            it.name.lowercase().contains("deadlift") ||
            it.name.lowercase().contains("calf") ||
            it.name.lowercase().contains("lunge")
        }.take(4)
        
        val shoulderExercises = exercises.filter {
            it.name.lowercase().contains("shoulder") ||
            it.name.lowercase().contains("press") ||
            it.name.lowercase().contains("raise") ||
            it.name.lowercase().contains("upright")
        }.take(3)
        
        val armExercises = exercises.filter {
            it.name.lowercase().contains("curl") ||
            it.name.lowercase().contains("tricep") ||
            it.name.lowercase().contains("extension")
        }.take(4)
        
        // Build the workout plan text
        val planText = StringBuilder()
        planText.appendLine("Plan Name: Example Workout with Image Support")
        planText.appendLine()
        planText.appendLine("// Generated based on available exercise images")
        planText.appendLine("// Format: Day X: [Weekday] Name {Primary Muscle, Secondary Muscle}")
        planText.appendLine()
        
        // Day 1: Chest
        planText.appendLine("Day 1: [Mon] Chest Day {CHEST, TRICEPS}")
        chestExercises.forEach { exercise ->
            planText.appendLine("- ${exercise.name} | 4x8-10 | 90")
        }
        planText.appendLine()
        
        // Day 2: Back
        planText.appendLine("Day 2: [Tue] Back Day {BACK, BICEPS}")
        backExercises.forEach { exercise ->
            planText.appendLine("- ${exercise.name} | 4x10-12 | 90")
        }
        planText.appendLine()
        
        // Day 3: Rest
        planText.appendLine("Day 3: [Wed] Rest Day")
        planText.appendLine()
        
        // Day 4: Legs
        planText.appendLine("Day 4: [Thu] Leg Day {QUADS, CALVES}")
        legExercises.forEach { exercise ->
            planText.appendLine("- ${exercise.name} | 4x8-12 | 120")
        }
        planText.appendLine()
        
        // Day 5: Shoulders
        planText.appendLine("Day 5: [Fri] Shoulder Day {SHOULDERS, TRAPS}")
        shoulderExercises.forEach { exercise ->
            planText.appendLine("- ${exercise.name} | 3x10-12 | 90")
        }
        planText.appendLine()
        
        // Day 6: Arms
        planText.appendLine("Day 6: [Sat] Arm Day {BICEPS, TRICEPS}")
        armExercises.forEach { exercise ->
            planText.appendLine("- ${exercise.name} | 3x12-15 | 60")
        }
        planText.appendLine()
        
        // Day 7: Rest
        planText.appendLine("Day 7: [Sun] Rest Day")
        
        return@withContext planText.toString()
    }
    
    /**
     * Generate a sample description for an exercise
     */
    private fun getExerciseDescription(exerciseName: String): String {
        val lowercaseName = exerciseName.lowercase()
        
        return when {
            lowercaseName.contains("bench press") -> 
                "Lie on a flat bench with feet firmly on the ground. Grip the barbell with hands slightly wider than shoulder-width. Lower the bar to your chest, then press back up to starting position."
                
            lowercaseName.contains("squat") -> 
                "Stand with feet shoulder-width apart, barbell across upper back. Bend knees and lower hips until thighs are parallel to floor. Push through heels to return to standing position."
                
            lowercaseName.contains("deadlift") -> 
                "Stand with feet hip-width apart, barbell over midfoot. Bend at hips and knees to grip the bar. Keep back flat as you drive through heels and extend hips to stand up with the weight."
                
            lowercaseName.contains("pull up") -> 
                "Hang from a bar with hands slightly wider than shoulder-width. Pull your body up until chin clears the bar, then lower with control to starting position."
                
            lowercaseName.contains("row") -> 
                "Bend at hips with back flat and grip the bar/handles. Pull the weight to your lower chest/abdomen, squeezing shoulder blades together. Lower with control to starting position."
                
            lowercaseName.contains("curl") -> 
                "Stand with feet shoulder-width apart, holding weights at sides with palms facing forward. Curl the weights toward shoulders while keeping elbows close to body, then lower with control."
                
            lowercaseName.contains("extension") -> 
                "Position yourself with weights overhead or in front of body. Extend arms against resistance while keeping shoulders stable. Return to starting position with control."
                
            lowercaseName.contains("press") -> 
                "Start with weights at shoulder height. Press the weights overhead until arms are fully extended. Lower weights back to shoulders with control."
                
            lowercaseName.contains("raise") -> 
                "Stand with weights at sides. Raise weights out to sides until arms are parallel to floor, keeping elbows slightly bent. Lower with control to starting position."
                
            lowercaseName.contains("fly") -> 
                "Position with arms extended to sides. Keep a slight bend in elbows as you bring hands together in front of chest in an arcing motion. Return to starting position with control."
                
            lowercaseName.contains("lunge") -> 
                "Stand with feet hip-width apart. Step forward with one leg and lower hips until both knees are bent at 90-degree angles. Push through front heel to return to starting position."
                
            lowercaseName.contains("calf") -> 
                "Stand on edge of platform with heels hanging off. Rise up onto toes as high as possible, then lower heels below platform level. Repeat for desired repetitions."
                
            else -> "Perform the exercise with proper form, focusing on controlled movements and full range of motion."
        }
    }
} 