package com.H_Oussama.gymplanner.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Common icons used in the app
 */
object AppIcons {
    // Use Timer icon as Hourglass since Material doesn't have a direct hourglass icon
    val Hourglass = Icons.Filled.Timer
    
    // Timer control icons
    val Pause = Icons.Filled.Pause
    val ExitToApp = Icons.AutoMirrored.Filled.ExitToApp
    
    // Summary Icons
    val Duration = Icons.Filled.Timelapse
    val Exercises = Icons.Filled.FitnessCenter
    val Sets = Icons.Filled.Repeat
    val Reps = Icons.Filled.Calculate
    val Weight = Icons.Filled.LinearScale
    val Alarm = Icons.Filled.Alarm
} 