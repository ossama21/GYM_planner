package com.H_Oussama.gymplanner.util

import com.H_Oussama.gymplanner.data.model.ExerciseDefinition
import com.H_Oussama.gymplanner.data.model.MuscleGroup
import java.util.UUID

/**
 * Provides sample data for demonstration purposes in the app.
 */
object SampleData {
    
    /**
     * Generates a sample list of exercise definitions with MET values.
     */
    fun getExerciseDefinitionsWithMET(): List<ExerciseDefinition> {
        return listOf(
            ExerciseDefinition(
                id = "bench_press_barbell",
                name = "Barbell Bench Press",
                description = "Lie on a bench and press a barbell upward from your chest.",
                imageIdentifier = "bench_press_barbell",
                met = 6.0
            ),
            ExerciseDefinition(
                id = "squat_barbell",
                name = "Barbell Squat",
                description = "Stand with a barbell on your shoulders, lower your body by bending your knees, then rise back up.",
                imageIdentifier = "squat_barbell",
                met = 8.0
            ),
            ExerciseDefinition(
                id = "deadlift_barbell",
                name = "Barbell Deadlift",
                description = "Lift a barbell from the ground to hip level, keeping your back straight.",
                imageIdentifier = "deadlift_barbell",
                met = 9.0
            ),
            ExerciseDefinition(
                id = "pullup",
                name = "Pull-Up",
                description = "Hang from a bar and pull your body up until your chin is above the bar.",
                imageIdentifier = "pullup",
                met = 8.0
            ),
            ExerciseDefinition(
                id = "pushup",
                name = "Push-Up",
                description = "Start in a plank position and lower your body until your chest nearly touches the floor, then push back up.",
                imageIdentifier = "pushup",
                met = 4.0
            ),
            ExerciseDefinition(
                id = "bicep_curl",
                name = "Bicep Curl",
                description = "Hold dumbbells with arms extended, then bend your elbows to raise the weights to shoulder level.",
                imageIdentifier = "bicep_curl",
                met = 3.5
            ),
            ExerciseDefinition(
                id = "tricep_pushdown",
                name = "Tricep Pushdown",
                description = "Stand facing a cable machine, grasp the bar, and push down until your arms are fully extended.",
                imageIdentifier = "tricep_pushdown",
                met = 3.5
            ),
            ExerciseDefinition(
                id = "shoulder_press",
                name = "Shoulder Press",
                description = "Sit or stand with weights at shoulder level, then press them overhead.",
                imageIdentifier = "shoulder_press",
                met = 5.0
            ),
            ExerciseDefinition(
                id = "leg_press",
                name = "Leg Press",
                description = "Sit in a leg press machine and push the platform away from you by extending your legs.",
                imageIdentifier = "leg_press",
                met = 7.5
            ),
            ExerciseDefinition(
                id = "crunches",
                name = "Crunches",
                description = "Lie on your back with knees bent, hands behind your head, and lift your shoulders off the floor.",
                imageIdentifier = "crunches",
                met = 3.8
            )
        )
    }
} 