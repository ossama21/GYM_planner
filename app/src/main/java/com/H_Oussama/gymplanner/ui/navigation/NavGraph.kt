package com.H_Oussama.gymplanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Routes.WORKOUT_PLAN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Asset Image Debugger screen removed
        
        // Other screens would be defined here
    }
} 
 
 