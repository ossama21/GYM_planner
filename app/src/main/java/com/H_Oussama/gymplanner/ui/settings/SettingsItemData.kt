package com.H_Oussama.gymplanner.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Switch

/**
 * Data class representing an item in the settings screen
 */
data class SettingsItemData(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val iconTint: Color = Color.Blue,
    val onClick: () -> Unit,
    val hasEndContent: Boolean = false,
    val endContentType: Int = 0,
    val hasToggle: Boolean = false,
    val isToggleChecked: Boolean = false,
    val onToggleChange: (() -> Unit)? = null
)

/**
 * Renders a settings item with optional end content
 */
@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color = Color.Blue,
    onClick: () -> Unit,
    endContent: @Composable (() -> Unit)? = null,
    hasToggle: Boolean = false,
    isToggleChecked: Boolean = false,
    onToggleChange: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Text content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
        
        // Toggle switch, end content, or chevron
        if (hasToggle && onToggleChange != null) {
            androidx.compose.material3.Switch(
                checked = isToggleChecked,
                onCheckedChange = { onToggleChange() }
            )
        } else if (endContent != null) {
            endContent()
        } else {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Renders a section of settings items
 */
@Composable
fun SettingsSection(
    title: String,
    items: List<SettingsItemData>,
    geminiApiTestResult: Int = 0 // 0: NOT_TESTED, 1: SUCCESS, 2: FAILED, 3: ERROR, 4: TESTING
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp)
    ) {
        // Section title
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Section items
        items.forEach { item ->
            SettingsItem(
                title = item.title,
                subtitle = item.subtitle,
                icon = item.icon,
                iconTint = item.iconTint,
                onClick = item.onClick,
                hasToggle = item.hasToggle,
                isToggleChecked = item.isToggleChecked,
                onToggleChange = item.onToggleChange,
                endContent = if (item.hasEndContent) {
                    when (item.endContentType) {
                        1 -> { // Gemini API status indicator
                            @Composable {
                                // Show colored indicator for API status
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            color = when (geminiApiTestResult) {
                                                1 -> Color.Green // SUCCESS
                                                2, 3 -> Color.Red // FAILED or ERROR
                                                4 -> Color.Yellow // TESTING
                                                else -> Color.Gray // NOT_TESTED or unknown
                                            },
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                        else -> {
                            @Composable {
                                // Default empty end content
                            }
                        }
                    }
                } else {
                    null
                }
            )
        }
    }
} 