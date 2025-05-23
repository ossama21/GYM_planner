package com.H_Oussama.gymplanner.ui.workoutplan

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun WorkoutPlanScreen(
    navController: NavController,
    viewModel: WorkoutPlanViewModel = hiltViewModel()
) {
    // Simply call the ModernWorkoutScreen composable
    ModernWorkoutScreen(
        navController = navController,
        viewModel = viewModel
    )
} 