package com.H_Oussama.gymplanner.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.H_Oussama.gymplanner.data.model.ExerciseDefinition
import com.H_Oussama.gymplanner.data.model.MuscleGroup
import java.io.File
import java.io.FileOutputStream

/**
 * Utility class to generate and provide fallback images for exercises
 * when the actual image isn't available.
 */
object ExerciseImageFallbackProvider {
    
    private const val TAG = "ExerciseFallback"
    
    /**
     * Generate a simple color-coded fallback image for a specific muscle group
     * 
     * @param context Application context
     * @param muscleGroup The muscle group to create an image for
     * @return A bitmap containing a simple representation of the muscle group
     */
    fun generateMuscleGroupFallbackImage(
        context: Context,
        muscleGroup: MuscleGroup
    ): Bitmap {
        val width = 400
        val height = 400
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Set background color based on muscle group
        val backgroundColor = getMuscleGroupColor(muscleGroup)
        canvas.drawColor(backgroundColor)
        
        // Draw muscle group name
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 50f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        
        val displayName = muscleGroup.getDisplayName()
        val x = width / 2f
        val y = height / 2f
        
        canvas.drawText(displayName, x, y, paint)
        
        return bitmap
    }
    
    /**
     * Generate and save placeholder images for all muscle groups
     */
    fun generateAndSaveMuscleGroupImages(context: Context) {
        try {
            // Create directory in app's files directory
            val muscleImagesDir = File(context.filesDir, "muscle_images")
            if (!muscleImagesDir.exists()) {
                muscleImagesDir.mkdirs()
            }
            
            // Generate and save an image for each muscle group
            MuscleGroup.values().forEach { muscleGroup ->
                try {
                    val bitmap = generateMuscleGroupFallbackImage(context, muscleGroup)
                    val filename = muscleGroup.name.lowercase() + ".jpg"
                    val file = File(muscleImagesDir, filename)
                    
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    
                    Log.d(TAG, "Generated fallback image for ${muscleGroup.name}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error generating image for ${muscleGroup.name}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating muscle group images: ${e.message}")
        }
    }
    
    /**
     * Generate a placeholder image for an exercise based on its name
     */
    fun generateExerciseFallbackImage(
        context: Context,
        exerciseDefinition: ExerciseDefinition
    ): Bitmap {
        val width = 400
        val height = 400
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Try to detect the primary muscle group for this exercise
        val detectedMuscleGroup = detectMuscleGroupFromExercise(exerciseDefinition)
        
        // Use the detected muscle group to determine background color
        val backgroundColor = if (detectedMuscleGroup != null) {
            getMuscleGroupColor(detectedMuscleGroup)
        } else {
            Color.LTGRAY
        }
        
        canvas.drawColor(backgroundColor)
        
        // Draw exercise name
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        
        // Split the exercise name into multiple lines if needed
        val exerciseName = exerciseDefinition.name
        val words = exerciseName.split(" ")
        
        if (words.size <= 2) {
            // Short name, draw on a single line
            canvas.drawText(exerciseName, width / 2f, height / 2f, paint)
        } else {
            // Longer name, split into two lines
            val midpoint = words.size / 2
            val line1 = words.subList(0, midpoint).joinToString(" ")
            val line2 = words.subList(midpoint, words.size).joinToString(" ")
            
            canvas.drawText(line1, width / 2f, height / 2f - 25f, paint)
            canvas.drawText(line2, width / 2f, height / 2f + 25f, paint)
        }
        
        return bitmap
    }
    
    /**
     * Helper method to detect a muscle group from an exercise definition
     */
    private fun detectMuscleGroupFromExercise(exerciseDefinition: ExerciseDefinition): MuscleGroup? {
        val name = exerciseDefinition.name.lowercase()
        val id = exerciseDefinition.id.lowercase()
        
        // Check name for muscle indicators
        return when {
            name.contains("bench") || name.contains("chest") || name.contains("pec") || 
            name.contains("fly") || name.contains("push-up") || 
            id.contains("chest") || id.contains("bench") || id.contains("pec") -> MuscleGroup.CHEST
            
            name.contains("pull") || name.contains("row") || name.contains("lat") || 
            name.contains("pull-up") || name.contains("deadlift") ||
            id.contains("back") || id.contains("row") || id.contains("pull") -> MuscleGroup.BACK
            
            name.contains("shoulder") || name.contains("press") || name.contains("delt") || 
            name.contains("lateral") || name.contains("overhead") ||
            id.contains("shoulder") || id.contains("press") -> MuscleGroup.SHOULDERS
            
            name.contains("bicep") || name.contains("curl") ||
            id.contains("bicep") || id.contains("curl") -> MuscleGroup.BICEPS
            
            name.contains("tricep") || name.contains("extension") || 
            name.contains("pushdown") ||
            id.contains("tricep") -> MuscleGroup.TRICEPS
            
            name.contains("quad") || name.contains("leg") || name.contains("squat") || 
            name.contains("lunge") || name.contains("extension") ||
            id.contains("leg") || id.contains("quad") || id.contains("squat") -> MuscleGroup.QUADS
            
            name.contains("calf") || name.contains("raise") ||
            id.contains("calf") -> MuscleGroup.CALVES
            
            name.contains("forearm") || name.contains("wrist") || 
            name.contains("grip") ||
            id.contains("forearm") || id.contains("wrist") -> MuscleGroup.FOREARMS
            
            else -> null
        }
    }
    
    /**
     * Get a color associated with a muscle group
     */
    private fun getMuscleGroupColor(muscleGroup: MuscleGroup): Int {
        return when (muscleGroup) {
            MuscleGroup.CHEST -> Color.rgb(227, 94, 94)      // Red
            MuscleGroup.BACK -> Color.rgb(84, 139, 212)      // Blue
            MuscleGroup.SHOULDERS -> Color.rgb(250, 176, 91) // Orange
            MuscleGroup.BICEPS -> Color.rgb(108, 171, 119)   // Green
            MuscleGroup.TRICEPS -> Color.rgb(180, 124, 195)  // Purple
            MuscleGroup.QUADS -> Color.rgb(232, 188, 94)     // Gold
            MuscleGroup.CALVES -> Color.rgb(87, 173, 173)    // Teal
            MuscleGroup.FOREARMS -> Color.rgb(177, 147, 121) // Brown
        }
    }
    
    /**
     * Generate and save placeholder images only for specific muscle groups
     * 
     * @param context Application context
     * @param muscleGroupNames List of muscle group names (should match MuscleGroup enum names)
     */
    fun generateFallbackImagesForMuscleGroups(
        context: Context,
        muscleGroupNames: List<String>
    ) {
        try {
            // Create directory in app's files directory
            val muscleImagesDir = File(context.filesDir, "muscle_images")
            if (!muscleImagesDir.exists()) {
                muscleImagesDir.mkdirs()
            }
            
            // Generate and save an image for each requested muscle group
            for (muscleGroupName in muscleGroupNames) {
                try {
                    val muscleGroup = MuscleGroup.valueOf(muscleGroupName)
                    val bitmap = generateMuscleGroupFallbackImage(context, muscleGroup)
                    val filename = muscleGroup.name.lowercase() + ".jpg"
                    val file = File(muscleImagesDir, filename)
                    
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    
                    Log.d(TAG, "Generated fallback image for ${muscleGroup.name}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error generating image for $muscleGroupName: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating selective muscle group images: ${e.message}")
        }
    }
} 