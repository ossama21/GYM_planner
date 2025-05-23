package com.H_Oussama.gymplanner.ui.common

import com.H_Oussama.gymplanner.data.model.ExerciseDefinition
import com.H_Oussama.gymplanner.data.model.MuscleGroup

/**
 * This file contains examples of exercise definitions formatted for compatibility with
 * the new version of the app that supports images and thumbnails.
 */

object ExerciseExamples {

    /**
     * Example of a workout plan format with proper exercise formatting
     * for the app's parser to extract exercises with image support.
     */
    const val SAMPLE_WORKOUT_PLAN_TEXT = """
        Plan Name: 5-Day Split with Image Support
        
        // Day format: "Day X: [Weekday] Name {Primary Muscle, Secondary Muscle}"
        // Exercise format: "- Exercise Name | Sets x Reps | Rest in seconds"
        
        Day 1: [Mon] Chest Day {CHEST, TRICEPS}
        - Bench Press | 4x8-10 | 120
        - Incline Dumbbell Press | 3x10 | 90
        - Cable Fly | 3x12 | 60
        - Push-Ups | 3xFailure | 60
        
        Day 2: [Tue] Back Day {BACK, BICEPS}
        - Pull-Ups | 4x8 | 120
        - Barbell Rows | 4x10 | 120
        - Lat Pulldown | 3x12 | 90
        - Seated Cable Row | 3x12 | 90
        
        Day 3: [Wed] Rest Day
        
        Day 4: [Thu] Shoulders {SHOULDERS, TRAPS}
        - Dumbbell Shoulder Press | 4x10 | 120
        - Cable Lateral Raise | 3x12 | 60
        - Upright Row | 3x12 | 60
        
        Day 5: [Fri] Legs {QUADS, CALVES}
        - Barbell Squats | 4x8 | 180
        - Leg Press | 3x10 | 120
        - Romanian Deadlift | 3x10 | 120
        - Standing Calf Raise | 4x15 | 60
        
        Day 6: [Sat] Arms {BICEPS, TRICEPS}
        - Barbell Curl | 4x10 | 90
        - Hammer Curl | 3x12 | 60
        - Lying Tricep Extension | 4x10 | 90
        
        Day 7: [Sun] Rest Day
    """
    
    /**
     * Example of how to create exercise definitions with proper image identifiers.
     * The imageIdentifier should match one of the image files in the assets/exercise_images directory.
     */
    val SAMPLE_EXERCISE_DEFINITIONS = listOf(
        ExerciseDefinition(
            id = "bench_press",
            name = "Bench Press",
            description = "Lie on a flat bench with your feet on the ground. Grip the barbell slightly wider than shoulder-width apart. Lower the bar to your chest, then press back up to the starting position.",
            imageIdentifier = "bench-press"  // Matches the bench-press.avif file in assets/exercise_images
        ),
        
        ExerciseDefinition(
            id = "barbell_squats",
            name = "Barbell Squat",
            description = "Stand with feet shoulder-width apart, barbell across your upper back. Bend your knees and lower your hips until your thighs are parallel to the floor. Push through your heels to return to standing position.",
            imageIdentifier = "barbell_squats"  // Matches the barbell_squats.avif file in assets/exercise_images
        ),
        
        ExerciseDefinition(
            id = "deadlift",
            name = "Deadlift",
            description = "Stand with feet hip-width apart, barbell over your midfoot. Bend at the hips and knees to grip the bar, then drive through your heels and extend your hips and knees to stand up with the weight.",
            imageIdentifier = "deadlift-howto"  // Matches the deadlift-howto.avif file in assets/exercise_images
        ),
        
        ExerciseDefinition(
            id = "pull_ups",
            name = "Pull-Ups",
            description = "Hang from a bar with hands slightly wider than shoulder-width apart. Pull your body up until your chin clears the bar, then lower back to the starting position with control.",
            imageIdentifier = "pull-ups"  // Matches the pull-ups.avif file in assets/exercise_images
        )
    )
    
    /**
     * Guidelines for adding new exercises with proper image support:
     * 
     * 1. Ensure the exercise ID is a unique, lowercase string with words separated by underscores
     * 2. Use a clear, descriptive name for the exercise
     * 3. Include a detailed description of how to perform the exercise
     * 4. Set imageIdentifier to match one of the filenames in the assets/exercise_images directory
     *    (without the file extension)
     * 5. Add any common variations of the exercise name to the ExerciseImageMatcher's commonNameMappings
     */
    const val ADDING_NEW_EXERCISES_GUIDE = """
        To add new exercises with proper image support:
        
        1. Add image files to the assets/exercise_images directory:
           - Use .avif, .jpg, or .png format (avif preferred for smaller size)
           - Name the file clearly using kebab-case (e.g., "seated-cable-row.avif")
           - Optimize images for smaller file size while maintaining quality
        
        2. Create an ExerciseDefinition with:
           - Unique ID (lowercase with underscores: "cable_row")
           - Clear name ("Seated Cable Row")
           - Detailed description
           - imageIdentifier matching your image filename without extension ("seated-cable-row")
        
        3. Add common name variations to ExerciseImageMatcher if needed:
           - Open app/src/main/java/com/H_Oussama/gymplanner/util/ExerciseImageMatcher.kt
           - Add entries to the commonNameMappings map for any variations:
             "cable row" to "seated-cable-row",
             "seated row" to "seated-cable-row"
        
        4. To use in a workout plan, format as:
           - Bench Press | 4x8-10 | 120
    """
    
    /**
     * Helper function to get a sample exercise definition by name
     */
    fun getSampleExerciseByName(name: String): ExerciseDefinition? {
        return SAMPLE_EXERCISE_DEFINITIONS.find { 
            it.name.equals(name, ignoreCase = true) 
        }
    }
} 