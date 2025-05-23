package com.H_Oussama.gymplanner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Returns the appropriate background color based on the current theme
 * to ensure consistency across the app.
 */
@Composable
fun getBackgroundColor(): Color {
    return MaterialTheme.colorScheme.background
}

/**
 * Returns the appropriate surface color based on the current theme.
 */
@Composable
fun getSurfaceColor(): Color {
    return MaterialTheme.colorScheme.surface
}

/**
 * Returns a compatible card color based on the current theme.
 */
@Composable
fun getCardColor(): Color {
    return MaterialTheme.colorScheme.surfaceVariant
}

/**
 * Returns a darker card color suitable for containers that need to stand out more.
 */
@Composable
fun getDarkerCardColor(): Color {
    return if (isSystemInDarkTheme()) {
        Color(0xFF1E1E1E)
    } else {
        Color(0xFFE0E0E0)
    }
}

/**
 * Returns a color for the primary action button.
 */
@Composable
fun getActionButtonColor(): Color {
    return MaterialTheme.colorScheme.primary
}

/**
 * Gets the text color that contrasts well with the current theme's background.
 */
@Composable
fun getTextColor(): Color {
    return MaterialTheme.colorScheme.onBackground
}

/**
 * Gets the secondary text color (less prominent) for the current theme.
 */
@Composable
fun getSecondaryTextColor(): Color {
    return MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
} 