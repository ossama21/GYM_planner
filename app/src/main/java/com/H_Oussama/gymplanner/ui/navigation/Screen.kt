package com.H_Oussama.gymplanner.ui.navigation

/**
 * Sealed class that contains all navigation routes in the app.
 * Each screen is represented as an object within this sealed class.
 */
sealed class Screen(val route: String) {
    // Main screens
    object Home : Screen("home")
    object WorkoutPlan : Screen("workout_plan")
    object ProgressTracking : Screen("progress_tracking")
    object Calculators : Screen("calculators")
    object Settings : Screen("settings")
    
    // Workout execution screen with parameter for workout day ID
    object WorkoutExecution : Screen("workout_execution/{workoutDayId}") {
        fun createRoute(workoutDayId: String): String {
            return "workout_execution/$workoutDayId"
        }
    }
    
    // TDEE calculator screen
    object TdeeCalculator : Screen("tdee_calculator")
    
    // Exercise detail screen with parameter for exercise ID
    object ExerciseDetail : Screen("exercise_detail/{exerciseId}") {
        fun createRoute(exerciseId: String): String {
            return "exercise_detail/$exerciseId"
        }
    }
} 