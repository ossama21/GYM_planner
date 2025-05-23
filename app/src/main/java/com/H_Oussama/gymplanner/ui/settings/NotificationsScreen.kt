package com.H_Oussama.gymplanner.ui.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.H_Oussama.gymplanner.ui.common.TransparentTopBar
import com.H_Oussama.gymplanner.ui.theme.GymPlannerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showWorkoutTimeDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Transparent top bar
        TransparentTopBar(
            title = "Notifications",
            onBackClick = onBackClick
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp) // Add some space between top bar and content
        ) {
            // Notification types section
            NotificationSection(
                title = "Notification Types",
                items = listOf(
                    NotificationItem(
                        title = "Workout Reminders",
                        description = "Remind you about upcoming workouts",
                        icon = Icons.Default.FitnessCenter,
                        isEnabled = uiState.workoutReminders,
                        onToggle = viewModel::toggleWorkoutReminders
                    ),
                    NotificationItem(
                        title = "Rest Timer",
                        description = "Notify when rest time is over",
                        icon = Icons.Default.Timer,
                        isEnabled = uiState.restTimerAlerts,
                        onToggle = viewModel::toggleRestTimerAlerts
                    ),
                    NotificationItem(
                        title = "Progress Updates",
                        description = "Weekly progress summaries",
                        icon = Icons.Default.ShowChart,
                        isEnabled = uiState.progressUpdates,
                        onToggle = viewModel::toggleProgressUpdates
                    ),
                    NotificationItem(
                        title = "Water Reminders",
                        description = "Remind you to stay hydrated",
                        icon = Icons.Default.WaterDrop,
                        isEnabled = uiState.waterReminders,
                        onToggle = viewModel::toggleWaterReminders
                    ),
                    NotificationItem(
                        title = "Tips & Motivation",
                        description = "Get workout tips and motivation",
                        icon = Icons.Default.EmojiEvents,
                        isEnabled = uiState.tipsAndMotivation,
                        onToggle = viewModel::toggleTipsAndMotivation
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Reminder time settings
            ReminderTimeSection(
                workoutReminderTime = uiState.workoutReminderTime,
                onWorkoutTimeClick = { showWorkoutTimeDialog = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sound and vibration settings
            SoundAndVibrationSection(
                soundEnabled = uiState.soundEnabled,
                vibrationEnabled = uiState.vibrationEnabled,
                onToggleSound = viewModel::toggleSound,
                onToggleVibration = viewModel::toggleVibration
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Reset to defaults button
            NotificationsResetButton(onClick = viewModel::resetToDefaults)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Time picker dialog
    if (showWorkoutTimeDialog) {
        TimePickerDialog(
            title = "Set Workout Reminder Time",
            initialTime = uiState.workoutReminderTime,
            onDismiss = { showWorkoutTimeDialog = false },
            onConfirm = { hour, minute ->
                viewModel.setWorkoutReminderTime(hour, minute)
                showWorkoutTimeDialog = false
            }
        )
    }
}

@Composable
fun NotificationSection(
    title: String,
    items: List<NotificationItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Section title
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
        )
        
        // Section card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                items.forEachIndexed { index, item ->
                    NotificationToggleItem(
                        item = item,
                        isLast = index == items.size - 1
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationToggleItem(
    item: NotificationItem,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Title and description
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (item.description.isNotEmpty()) {
                Text(
                    text = item.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        
        // Toggle switch
        Switch(
            checked = item.isEnabled,
            onCheckedChange = { newValue -> item.onToggle(newValue) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
    
    if (!isLast) {
        Divider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            modifier = Modifier.padding(start = 56.dp)
        )
    }
}

@Composable
fun ReminderTimeSection(
    workoutReminderTime: String,
    onWorkoutTimeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Section title
        Text(
            text = "Reminder Times",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
        )
        
        // Section card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Workout reminder time item
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onWorkoutTimeClick)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Title and time
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Workout Reminder Time",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = workoutReminderTime,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    
                    // Chevron icon
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun SoundAndVibrationSection(
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    onToggleSound: (Boolean) -> Unit,
    onToggleVibration: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Section title
        Text(
            text = "Sound & Vibration",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
        )
        
        // Section card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Sound toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Title
                    Text(
                        text = "Sound",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Switch
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = onToggleSound,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
                
                Divider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    modifier = Modifier.padding(start = 56.dp)
                )
                
                // Vibration toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Title
                    Text(
                        text = "Vibration",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Switch
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = onToggleVibration,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationsResetButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Restore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiaryContainer
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Reset to Default Settings")
    }
}

@Composable
fun TimePickerDialog(
    title: String,
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val initialHour = initialTime.substringBefore(":").toIntOrNull() ?: 8
    val initialMinute = initialTime.substringAfter(":").toIntOrNull() ?: 0
    
    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Simple time picker with hour and minute pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hours picker
                    NumberPicker(
                        value = selectedHour,
                        onValueChange = { selectedHour = it },
                        range = 0..23
                    )
                    
                    Text(
                        text = ":",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    // Minutes picker
                    NumberPicker(
                        value = selectedMinute,
                        onValueChange = { selectedMinute = it },
                        range = 0..59
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // AM/PM indicator (24-hour format)
                Text(
                    text = if (selectedHour < 12) "AM" else "PM",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedHour, selectedMinute) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Set")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = {
                val newValue = if (value + 1 > range.last) range.first else value + 1
                onValueChange(newValue)
            }
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Increase",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Text(
            text = String.format("%02d", value),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        IconButton(
            onClick = {
                val newValue = if (value - 1 < range.first) range.last else value - 1
                onValueChange(newValue)
            }
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Decrease",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Data class for notification item
data class NotificationItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isEnabled: Boolean,
    val onToggle: (Boolean) -> Unit
)

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    // Using an anonymous class to create a preview UI state
    val mockViewModel = androidx.lifecycle.viewmodel.compose.viewModel<NotificationsViewModel>()
    
    // Update the UI state to use default values manually
    mockViewModel.updateUiStateForPreview(
        NotificationsUiState(
            workoutReminders = true,
            restTimerAlerts = true,
            progressUpdates = false,
            waterReminders = false,
            tipsAndMotivation = true,
            workoutReminderTime = "08:00",
            soundEnabled = true,
            vibrationEnabled = true
        )
    )

    GymPlannerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NotificationsScreen(
                viewModel = mockViewModel
            )
        }
    }
} 
 
 