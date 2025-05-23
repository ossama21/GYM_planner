package com.H_Oussama.gymplanner.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.H_Oussama.gymplanner.R

// Define routes as constants
object Routes {
    const val WORKOUT_PLAN = "workout_plan"
    const val WORKOUT_PLAN_EDITOR = "workout_plan_editor"
    const val CALCULATORS_HUB = "calculators_hub"
    const val PROGRESS_HUB = "progress_hub"
    const val BMI_CALCULATOR = "bmi_calculator"
    const val TDEE_CALCULATOR = "tdee_calculator"
    const val BODY_FAT_CALCULATOR = "body_fat_calculator"
    const val WORKOUT_EXECUTION_ROUTE = "workout_execution" // Base route for execution screen
    const val WORKOUT_DAY_INDEX_ARG = "dayIndex"
    const val WORKOUT_EXECUTION = "$WORKOUT_EXECUTION_ROUTE/{$WORKOUT_DAY_INDEX_ARG}" // Route with argument
    const val EXERCISE_HISTORY_ROUTE = "exercise_history"
    const val EXERCISE_ID_ARG = "exerciseId"
    const val EXERCISE_NAME_ARG = "name"
    const val EXERCISE_HISTORY = "$EXERCISE_HISTORY_ROUTE/{$EXERCISE_ID_ARG}?$EXERCISE_NAME_ARG={$EXERCISE_NAME_ARG}"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    const val NOTIFICATIONS = "notifications"
    const val UNITS = "units"
    const val THEME = "theme"
    const val WORKOUT_GOALS = "workout_goals"
    const val MANAGE_EXERCISE_IMAGES_SCREEN = "manage_exercise_images"
    
    // Exercises by Body Part Screen
    const val EXERCISES_BY_BODY_PART_ROUTE = "exercises_by_body_part"
    const val CATEGORY_NAME_ARG = "categoryName"
    const val EXERCISES_BY_BODY_PART_SCREEN = "$EXERCISES_BY_BODY_PART_ROUTE/{$CATEGORY_NAME_ARG}"
    
    // Additional Settings Screens
    const val ACCOUNT_SETTINGS_SCREEN = "account_settings_screen"
    const val NOTIFICATIONS_SETTINGS_SCREEN = "notifications_settings_screen"
    const val THEME_SETTINGS_SCREEN = "theme_settings_screen"
    const val ABOUT_SCREEN = "about_screen"
    const val PRIVACY_POLICY_SCREEN = "privacy_policy_screen"
    const val TERMS_OF_SERVICE_SCREEN = "terms_of_service_screen"
    const val SUPPORT_HELP_SCREEN = "support_help_screen"
    const val DATA_BACKUP_SCREEN = "data_backup_screen"
    const val ACHIEVEMENTS_SCREEN = "achievements_screen"
    
    // Nutrition tracking
    const val NUTRITION_TRACKER = "nutrition_tracker"
    const val FOOD_ENTRY = "food_entry"
    const val FOOD_DETAIL = "food_detail/{foodId}"
    const val NUTRITION_HOW_TO = "nutrition_how_to"
    
    // Weight tracking
    const val WEIGHT_TRACKER = "weight_tracker"
    
    // Workout logging
    const val WORKOUT_LOG = "workout_log"
    
    // Workout history and comparison
    const val WORKOUT_HISTORY = "workout_history"
    const val WORKOUT_COMPARISON_ROUTE = "workout_comparison"
    const val WORKOUT_ID_ARG = "workoutId"
    const val WORKOUT_COMPARISON = "$WORKOUT_COMPARISON_ROUTE/{$WORKOUT_ID_ARG}"
    
    // Demo screens
    const val IMAGE_SEARCH_DEMO = "image_search_demo"
}

// Sealed class for Bottom Navigation items
sealed class BottomNavItem(
    val route: String,
    val titleResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    @Composable
    fun getTitle(): String = stringResource(id = titleResId)
    
    object Workout : BottomNavItem(
        route = Routes.WORKOUT_PLAN,
        titleResId = R.string.workout,
        selectedIcon = Icons.Filled.FitnessCenter, // Use Icons.Filled
        unselectedIcon = Icons.Outlined.FitnessCenter // Use Icons.Outlined
    )
    object Calculators : BottomNavItem(
        route = Routes.CALCULATORS_HUB,
        titleResId = R.string.calculators,
        selectedIcon = Icons.Filled.Calculate, // Use Icons.Filled
        unselectedIcon = Icons.Outlined.Calculate // Use Icons.Outlined
    )
    // Add other bottom nav items like "Progress" or "Settings" here
    object Progress : BottomNavItem(
        route = Routes.PROGRESS_HUB,
        titleResId = R.string.progress,
        selectedIcon = Icons.Filled.Timeline, // Use Icons.Filled
        unselectedIcon = Icons.Outlined.Timeline // Use Icons.Outlined
    )
    
    object Settings : BottomNavItem(
        route = Routes.SETTINGS,
        titleResId = R.string.settings,
        selectedIcon = Icons.Filled.Settings, // Use Icons.Filled
        unselectedIcon = Icons.Outlined.Settings // Use Icons.Outlined
    )
}