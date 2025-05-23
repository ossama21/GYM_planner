package com.H_Oussama.gymplanner.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.H_Oussama.gymplanner.ui.calculators.bmi.BmiScreen
import com.H_Oussama.gymplanner.ui.calculators.CalculatorsHubScreen
import com.H_Oussama.gymplanner.ui.calculators.bodyfat.BodyFatScreen
import com.H_Oussama.gymplanner.ui.calculators.tdee.TdeeScreen
import com.H_Oussama.gymplanner.ui.navigation.BottomNavItem
import com.H_Oussama.gymplanner.ui.navigation.Routes
import com.H_Oussama.gymplanner.ui.navigation.Screen
import com.H_Oussama.gymplanner.ui.progress.ExerciseHistoryScreen
import com.H_Oussama.gymplanner.ui.progress.ProgressHubScreen
import com.H_Oussama.gymplanner.ui.settings.SettingsScreen
import com.H_Oussama.gymplanner.ui.settings.ProfileScreen
import com.H_Oussama.gymplanner.ui.settings.NotificationsScreen
import com.H_Oussama.gymplanner.ui.settings.ThemeScreen
import com.H_Oussama.gymplanner.ui.settings.UnitsScreen
import com.H_Oussama.gymplanner.ui.settings.WorkoutGoalsScreen
import com.H_Oussama.gymplanner.ui.settings.ManageExerciseImagesScreen
import com.H_Oussama.gymplanner.ui.theme.GymPlannerTheme
import com.H_Oussama.gymplanner.ui.workoutplan.WorkoutExecutionScreen
import com.H_Oussama.gymplanner.ui.workoutplan.WorkoutExecutionViewModel
import com.H_Oussama.gymplanner.ui.workoutplan.WorkoutPlanScreen
import androidx.hilt.navigation.compose.hiltViewModel
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import com.H_Oussama.gymplanner.ui.nutrition.NutritionTrackerScreen
import com.H_Oussama.gymplanner.ui.nutrition.FoodEntryScreen
import com.H_Oussama.gymplanner.ui.weight.WeightTrackerScreen
import com.H_Oussama.gymplanner.ui.workoutlog.WorkoutLogScreen
import com.H_Oussama.gymplanner.ui.settings.AssetImageDebugger
import androidx.compose.ui.res.stringResource
import com.H_Oussama.gymplanner.R
import com.H_Oussama.gymplanner.ui.nutrition.HowToUseNutritionScreen
import com.H_Oussama.gymplanner.ui.settings.SettingsViewModel
import com.H_Oussama.gymplanner.ui.progress.WorkoutHistoryScreen
import com.H_Oussama.gymplanner.ui.progress.WorkoutComparisonScreen
import com.H_Oussama.gymplanner.ui.settings.ExercisesByBodyPartScreen
import com.H_Oussama.gymplanner.ui.demo.ImageSearchDemoScreen
import com.H_Oussama.gymplanner.ui.workoutplan.WorkoutPlanEditorScreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun GymApp() {
    // Make app fullscreen by hiding status bar
    val context = LocalContext.current
    // Store the dark theme value so it can be accessed in the DisposableEffect
    val isDarkTheme = isSystemInDarkTheme()
    
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !isDarkTheme
            }
        }
        onDispose {
            // Restore system UI when the composable is disposed
            val activityWindow = (context as? Activity)?.window
            if (activityWindow != null) {
                WindowCompat.setDecorFitsSystemWindows(activityWindow, true)
            }
        }
    }

    GymPlannerTheme {
        val navController = rememberNavController()
        val bottomNavItems = listOf(BottomNavItem.Workout, BottomNavItem.Calculators, BottomNavItem.Progress, BottomNavItem.Settings)

        // Get current back stack entry to determine current route and if we can go back
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val currentRoute = currentDestination?.route

        // Check if the current route is a top-level destination from the bottom bar
        val isTopLevelDestination = bottomNavItems.any { it.route == currentRoute }

        Scaffold(
            topBar = {
                // Top bar removed as indicated in screenshot
            },
            bottomBar = {
                if (isTopLevelDestination) {
                NavigationBar {
                        val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = currentNavBackStackEntry?.destination?.route

                        bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                        imageVector = if (currentRoute == item.route) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = stringResource(id = item.titleResId)
                                )
                            },
                                label = { Text(item.getTitle()) },
                                selected = currentRoute == item.route,
                            onClick = {
                                    navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Creates a NavHost with animations between destinations
            NavHost(
                navController = navController,
                    startDestination = Routes.WORKOUT_PLAN,
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { 1000 },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300))
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -1000 },
                            animationSpec = tween(300)
                        ) + fadeOut(animationSpec = tween(300))
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -1000 },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300))
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { 1000 },
                            animationSpec = tween(300)
                        ) + fadeOut(animationSpec = tween(300))
                    }
            ) {
                // Main destinations from Bottom Nav
                composable(Routes.WORKOUT_PLAN) {
                    WorkoutPlanScreen(
                        navController = navController
                    )
                }
                composable(Routes.CALCULATORS_HUB) {
                    CalculatorsHubScreen(
                        onNavigateToCalculator = { route -> navController.navigate(route) }
                    )
                }
                composable(Routes.PROGRESS_HUB) {
                    ProgressHubScreen(
                        onNavigateToHistory = { exerciseId, exerciseName ->
                            // URL-encode the name argument
                            val encodedName = java.net.URLEncoder.encode(exerciseName, java.nio.charset.StandardCharsets.UTF_8.toString())
                            navController.navigate("${Routes.EXERCISE_HISTORY_ROUTE}/$exerciseId?name=$encodedName")
                        },
                        onNavigateToNutrition = { navController.navigate(Routes.NUTRITION_TRACKER) },
                        onNavigateToWeight = { navController.navigate(Routes.WEIGHT_TRACKER) },
                        onNavigateToWorkoutLog = { navController.navigate(Routes.WORKOUT_LOG) },
                        onNavigateToWorkoutHistory = { navController.navigate(Routes.WORKOUT_HISTORY) }
                    )
                }

                // Nested Calculator destinations
                    composable(Routes.BMI_CALCULATOR) { 
                        BmiScreen(
                            onNavigateBack = { navController.navigateUp() }
                        ) 
                    }
                    composable(Routes.TDEE_CALCULATOR) { 
                        TdeeScreen(
                            onNavigateBack = { navController.navigateUp() }
                        ) 
                    }
                    composable(Routes.BODY_FAT_CALCULATOR) { 
                        BodyFatScreen(
                            onNavigateBack = { navController.navigateUp() }
                        ) 
                    }

                    // Nutrition tracker screens
                    composable(Routes.NUTRITION_TRACKER) {
                        NutritionTrackerScreen(
                            onNavigateToFoodEntry = { navController.navigate(Routes.FOOD_ENTRY) },
                            onNavigateBack = { navController.navigateUp() },
                            onNavigateToHowTo = { navController.navigate(Routes.NUTRITION_HOW_TO) }
                        )
                    }
                    
                    composable(Routes.FOOD_ENTRY) {
                        FoodEntryScreen(
                            onNavigateBack = { navController.navigateUp() }
                        )
                    }
                    
                    // Add the How To Use Nutrition Screen
                    composable(Routes.NUTRITION_HOW_TO) {
                        HowToUseNutritionScreen(
                            onNavigateBack = { navController.navigateUp() }
                        )
                    }
                    
                    // Weight tracker screen
                    composable(Routes.WEIGHT_TRACKER) {
                        WeightTrackerScreen(
                            onNavigateBack = { navController.navigateUp() }
                        )
                    }

                    // Workout Execution Screen Destination (use the correct route defined in Routes)
                    composable(
                        route = Routes.WORKOUT_EXECUTION,
                        arguments = listOf(navArgument(Routes.WORKOUT_DAY_INDEX_ARG) { type = NavType.IntType })
                    ) { backStackEntry ->
                        val dayIndex = backStackEntry.arguments?.getInt(Routes.WORKOUT_DAY_INDEX_ARG) ?: -1
                        
                        // Create viewModel with the SavedStateHandle
                        val viewModel = hiltViewModel<WorkoutExecutionViewModel>()
                    
                    WorkoutExecutionScreen(
                        onNavigateBack = { navController.navigateUp() },
                        viewModel = viewModel
                    )
                }

                // Exercise History Screen Destination
                composable(
                    route = Routes.EXERCISE_HISTORY,
                    arguments = listOf(
                        navArgument(Routes.EXERCISE_ID_ARG) { type = NavType.StringType },
                        navArgument(Routes.EXERCISE_NAME_ARG) { type = NavType.StringType ; nullable = true }
                    )
                ) {
                    ExerciseHistoryScreen() // Assuming ExerciseHistoryScreen fetches args internally or via ViewModel
                    }
                    
                    // Settings Screen
                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                            onNavigateToTheme = { navController.navigate(Routes.THEME) },
                            onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                            onNavigateToWorkoutGoals = { navController.navigate(Routes.WORKOUT_GOALS) },
                            onNavigateToManageImages = { navController.navigate(Routes.MANAGE_EXERCISE_IMAGES_SCREEN) },
                            navController = navController
                        )
                    }
                    
                    // Profile Screen
                    composable(Routes.PROFILE) { entry ->
                        // Get a reference to the SettingsViewModel to refresh it when needed
                        val settingsBackStackEntry = remember(entry) { navController.getBackStackEntry(Routes.SETTINGS) }
                        val settingsViewModel = hiltViewModel<SettingsViewModel>(
                            // Use parent backstack entry to get the correct ViewModel instance
                            viewModelStoreOwner = settingsBackStackEntry
                        )
                        
                        ProfileScreen(
                            onBackClick = { navController.navigateUp() },
                            onDataChanged = { settingsViewModel.refreshUserData() }
                        )
                    }
                    
                    // Notifications Screen
                    composable(Routes.NOTIFICATIONS) {
                        NotificationsScreen(
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                    
                    // Units Screen
                    composable(Routes.UNITS) {
                        UnitsScreen(
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                    
                    // Theme Screen
                    composable(Routes.THEME) {
                        ThemeScreen(
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                    
                    // Workout Goals Screen
                    composable(Routes.WORKOUT_GOALS) {
                        WorkoutGoalsScreen(
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                    
                    // Account Settings Screen
                    composable(Routes.ACCOUNT_SETTINGS_SCREEN) { 
                        // TODO: Implement AccountSettingsScreen
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text("Account Settings Screen - Not implemented yet")
                        }
                    }
                    composable(Routes.NOTIFICATIONS_SETTINGS_SCREEN) { 
                        // TODO: Implement NotificationsSettingsScreen
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text("Notifications Settings Screen - Not implemented yet")
                        }
                    }
                    composable(Routes.THEME_SETTINGS_SCREEN) { 
                        // TODO: Implement ThemeSettingsScreen 
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text("Theme Settings Screen - Not implemented yet")
                        }
                    }
                    composable(Routes.ABOUT_SCREEN) { 
                        // TODO: Implement AboutScreen
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text("About Screen - Not implemented yet")
                        }
                    }
                    composable(Routes.PRIVACY_POLICY_SCREEN) { 
                        // TODO: Implement PrivacyPolicyScreen
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text("Privacy Policy Screen - Not implemented yet")
                        }
                    }
                    composable(Routes.TERMS_OF_SERVICE_SCREEN) { 
                        // TODO: Implement TermsOfServiceScreen
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text("Terms of Service Screen - Not implemented yet")
                        }
                    }
                    composable(Routes.SUPPORT_HELP_SCREEN) { 
                        // TODO: Implement SupportHelpScreen
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text("Support Help Screen - Not implemented yet")
                        }
                    }
                    composable(Routes.DATA_BACKUP_SCREEN) { 
                        // TODO: Implement DataBackupScreen
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text("Data Backup Screen - Not implemented yet")
                        }
                    }
                    composable(Routes.ACHIEVEMENTS_SCREEN) { 
                        // TODO: Implement AchievementsScreen
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text("Achievements Screen - Not implemented yet")
                        }
                    }
                    composable(Routes.MANAGE_EXERCISE_IMAGES_SCREEN) {
                        ManageExerciseImagesScreen(
                            onBackClick = { navController.popBackStack() },
                            onCategoryClick = { categoryName ->
                                // URL-encode the categoryName if it can contain special characters
                                val encodedCategoryName = java.net.URLEncoder.encode(categoryName, java.nio.charset.StandardCharsets.UTF_8.toString())
                                navController.navigate("${Routes.EXERCISES_BY_BODY_PART_ROUTE}/$encodedCategoryName")
                            }
                        )
                    }
                    
                    // Placeholder for Exercises By Body Part Screen
                    composable(
                        route = Routes.EXERCISES_BY_BODY_PART_SCREEN,
                        arguments = listOf(navArgument(Routes.CATEGORY_NAME_ARG) { type = NavType.StringType })
                    ) { backStackEntry ->
                        val categoryName = backStackEntry.arguments?.getString(Routes.CATEGORY_NAME_ARG) ?: ""
                        ExercisesByBodyPartScreen(
                            categoryNameFromNav = categoryName, 
                            onBackClick = { navController.popBackStack() },
                            onEditExerciseImageClick = { exerciseId -> 
                                // TODO: Implement navigation to image editing dialog/screen
                            }
                        )
                    }
                    
                    // Workout log screen
                    composable(Routes.WORKOUT_LOG) {
                        WorkoutLogScreen(
                            onNavigateBack = { navController.navigateUp() }
                        )
                    }
                    
                    // Workout history screen
                    composable(Routes.WORKOUT_HISTORY) {
                        WorkoutHistoryScreen(
                            onNavigateToComparison = { workoutId ->
                                navController.navigate("${Routes.WORKOUT_COMPARISON_ROUTE}/$workoutId")
                            }
                        )
                    }
                    
                    // Workout comparison screen
                    composable(
                        route = Routes.WORKOUT_COMPARISON,
                        arguments = listOf(
                            navArgument(Routes.WORKOUT_ID_ARG) { type = NavType.StringType }
                        )
                    ) {
                        WorkoutComparisonScreen(
                            onNavigateBack = { navController.navigateUp() }
                        )
                    }

                    // Add the demo screen 
                    composable(Routes.IMAGE_SEARCH_DEMO) {
                        ImageSearchDemoScreen()
                    }

                    // Add the workout plan editor screen route
                    composable(Routes.WORKOUT_PLAN_EDITOR) {
                        WorkoutPlanEditorScreen(
                            onNavigateBack = { navController.navigateUp() },
                            onSave = { navController.navigate(Routes.WORKOUT_PLAN) {
                                popUpTo(Routes.WORKOUT_PLAN) { inclusive = true }
                            }}
                        )
                    }
                }
            }
        }
    }
}

// Helper function to get the appropriate title for the current route
@Composable
fun getTitleForRoute(route: String?): String {
    return when (route) {
        Routes.WORKOUT_PLAN -> stringResource(id = R.string.workout_plan)
        Routes.CALCULATORS_HUB -> stringResource(id = R.string.calculators)
        Routes.PROGRESS_HUB -> stringResource(id = R.string.progress)
        Routes.BMI_CALCULATOR -> stringResource(id = R.string.bmi_calculator)
        Routes.TDEE_CALCULATOR -> stringResource(id = R.string.tdee_calculator)
        Routes.BODY_FAT_CALCULATOR -> stringResource(id = R.string.body_fat_calculator)
        Routes.EXERCISE_HISTORY -> stringResource(id = R.string.exercise_history)
        Routes.SETTINGS -> stringResource(id = R.string.settings)
        Routes.PROFILE -> stringResource(id = R.string.profile)
        Routes.NOTIFICATIONS -> stringResource(id = R.string.notifications)
        Routes.UNITS -> stringResource(id = R.string.units)
        Routes.THEME -> stringResource(id = R.string.theme)
        Routes.WORKOUT_GOALS -> stringResource(id = R.string.workout_goals)
        Routes.WORKOUT_EXECUTION -> stringResource(id = R.string.workout)
        Routes.ACCOUNT_SETTINGS_SCREEN -> stringResource(id = R.string.account_settings)
        Routes.NOTIFICATIONS_SETTINGS_SCREEN -> stringResource(id = R.string.notifications_settings)
        Routes.THEME_SETTINGS_SCREEN -> stringResource(id = R.string.theme_settings)
        Routes.ABOUT_SCREEN -> stringResource(id = R.string.about)
        Routes.PRIVACY_POLICY_SCREEN -> stringResource(id = R.string.privacy_policy)
        Routes.TERMS_OF_SERVICE_SCREEN -> stringResource(id = R.string.terms_of_service)
        Routes.SUPPORT_HELP_SCREEN -> stringResource(id = R.string.support_help)
        Routes.DATA_BACKUP_SCREEN -> stringResource(id = R.string.data_backup)
        Routes.ACHIEVEMENTS_SCREEN -> stringResource(id = R.string.achievements)
        Routes.MANAGE_EXERCISE_IMAGES_SCREEN -> stringResource(id = R.string.manage_exercise_images)
        Routes.EXERCISES_BY_BODY_PART_SCREEN -> {
            // For routes with arguments, we might want a generic title or one that can be dynamically set
            // For now, a generic one. The actual screen can set a more specific title.
            stringResource(id = R.string.exercises_by_category) // New string resource needed
        }
        Routes.WORKOUT_LOG -> stringResource(id = R.string.workout_log)
        Routes.IMAGE_SEARCH_DEMO -> stringResource(id = R.string.image_search_demo)
        Routes.WORKOUT_PLAN_EDITOR -> stringResource(id = R.string.workout_plan_editor)
        else -> {
            stringResource(id = R.string.app_name)
        }
    }
}