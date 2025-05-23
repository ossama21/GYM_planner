package com.H_Oussama.gymplanner.ui.weight

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun WeightTrackerScreen(
    viewModel: WeightTrackerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                Text(
                    text = "Weight",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddEntryDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Weight Entry",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab bar for Daily/Monthly
            var selectedTabIndex by remember { mutableStateOf(0) }
            val tabs = listOf("Daily", "Monthly")
            
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Weight chart
            WeightChart(
                weightEntries = uiState.weightEntries,
                isMonthly = selectedTabIndex == 1
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Weight entries list
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "History",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (uiState.weightEntries.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No weight entries yet.\nTap + to add your first entry.",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(300.dp)
                        ) {
                            items(uiState.weightEntries) { entry ->
                                WeightEntryItem(
                                    weightEntry = entry,
                                    onEdit = { viewModel.showEditEntryDialog(entry) },
                                    onDelete = { viewModel.deleteWeightEntry(entry.id) }
                                )
                                
                                Divider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Add/Edit dialog
        if (uiState.showAddEntryDialog) {
            WeightEntryDialog(
                initialWeight = uiState.selectedEntry?.weight?.toString() ?: "",
                initialDate = uiState.selectedEntry?.date ?: Calendar.getInstance().time,
                initialNote = uiState.selectedEntry?.note ?: "",
                onDismiss = { viewModel.dismissEntryDialog() },
                onSave = { weight, date, note ->
                    viewModel.handleWeightEntry(weight, date, note)
                },
                isEditing = uiState.selectedEntry != null
            )
        }
    }
}

@Composable
fun WeightChart(
    weightEntries: List<WeightEntryUi>,
    isMonthly: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        if (weightEntries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Add weight entries to see your progress chart",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            // Here we would normally use a charting library
            // For the mockup, we'll create a basic representation
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Y-axis labels and chart area
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Y-axis labels
                    Column(
                        modifier = Modifier.width(40.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        val weights = weightEntries.map { it.weight }
                        val maxWeight = weights.maxOrNull()?.let { it + 5 } ?: 80f
                        val minWeight = weights.minOrNull()?.let { it - 5 } ?: 60f
                        
                        Text(
                            text = "${maxWeight.roundToInt()} kg",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${((maxWeight + minWeight) / 2).roundToInt()} kg",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${minWeight.roundToInt()} kg",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    // Chart area with mock data points and line
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    ) {
                        // This would be replaced with an actual chart component
                        // For now, just show a line with dots at the start, middle, and end
                        val primaryColor = MaterialTheme.colorScheme.primary
                        
                        // Horizontal grid lines
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            repeat(3) {
                                Divider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (it < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        
                        // Mock data points and line
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Simplified chart rendering based on weightEntries
                            // In a real implementation, use a proper charting library
                            val sortedEntries = weightEntries.sortedBy { it.date }
                            val displayEntries = if (isMonthly) {
                                // Group by month and take average for monthly view
                                sortedEntries
                                    .groupBy { SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(it.date) }
                                    .map { (_, entries) ->
                                        WeightEntryUi(
                                            id = entries.first().id,
                                            weight = entries.map { it.weight }.average().toFloat(),
                                            date = entries.first().date,
                                            formattedDate = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(entries.first().date)
                                        )
                                    }
                            } else {
                                sortedEntries
                            }
                            
                            // Show placeholder chart points
                            val dates = if (displayEntries.size >= 5) {
                                listOf(
                                    displayEntries.first(),
                                    displayEntries[displayEntries.size / 4],
                                    displayEntries[displayEntries.size / 2],
                                    displayEntries[3 * displayEntries.size / 4],
                                    displayEntries.last()
                                )
                            } else {
                                displayEntries
                            }
                            
                            val minWeight = weightEntries.minOf { it.weight }
                            val maxWeight = weightEntries.maxOf { it.weight }
                            val range = maxWeight - minWeight
                            
                            dates.forEach { entry ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Calculate height based on weight
                                    val heightPercentage = if (range > 0) {
                                        (entry.weight - minWeight) / range
                                    } else 0.5f
                                    
                                    // Data point
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(primaryColor)
                                            .align(Alignment.CenterHorizontally)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    // Date label
                                    Text(
                                        text = SimpleDateFormat(
                                            if (isMonthly) "MMM" else "dd MMM",
                                            Locale.getDefault()
                                        ).format(entry.date),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeightEntryItem(
    weightEntry: WeightEntryUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date and weight
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = weightEntry.formattedDate,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (weightEntry.note.isNotEmpty()) {
                Text(
                    text = weightEntry.note,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        
        // Weight value
        Text(
            text = "${weightEntry.weight} kg",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Edit button
        IconButton(
            onClick = onEdit,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        // Delete button
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun WeightEntryDialog(
    initialWeight: String,
    initialDate: Date,
    initialNote: String,
    onDismiss: () -> Unit,
    onSave: (Float, Date, String) -> Unit,
    isEditing: Boolean
) {
    var weightText by remember { mutableStateOf(initialWeight) }
    var date by remember { mutableStateOf(initialDate) }
    var note by remember { mutableStateOf(initialNote) }
    var selectedDateOption by remember { mutableStateOf(if (isEditing) "Custom" else "Today") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (isEditing) "Edit Weight Entry" else "Add Weight Entry",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Weight input
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Date options
                Column {
                    Text(
                        text = "Date",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            selectedDateOption = "Today"
                            date = Calendar.getInstance().time
                        }
                    ) {
                        RadioButton(
                            selected = selectedDateOption == "Today",
                            onClick = {
                                selectedDateOption = "Today"
                                date = Calendar.getInstance().time
                            }
                        )
                        Text(
                            text = "Today",
                            fontSize = 16.sp
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            selectedDateOption = "Yesterday"
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.DAY_OF_YEAR, -1)
                            date = calendar.time
                        }
                    ) {
                        RadioButton(
                            selected = selectedDateOption == "Yesterday",
                            onClick = {
                                selectedDateOption = "Yesterday"
                                val calendar = Calendar.getInstance()
                                calendar.add(Calendar.DAY_OF_YEAR, -1)
                                date = calendar.time
                            }
                        )
                        Text(
                            text = "Yesterday",
                            fontSize = 16.sp
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            selectedDateOption = "Custom"
                            // In a real app, show a date picker here
                        }
                    ) {
                        RadioButton(
                            selected = selectedDateOption == "Custom",
                            onClick = {
                                selectedDateOption = "Custom"
                                // In a real app, show a date picker here
                            }
                        )
                        Text(
                            text = "Custom: ${SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)}",
                            fontSize = 16.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Note input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val weight = weightText.toFloatOrNull() ?: 0f
                            if (weight > 0) {
                                onSave(weight, date, note)
                            }
                        },
                        enabled = weightText.toFloatOrNull() != null && weightText.toFloatOrNull() ?: 0f > 0
                    ) {
                        Text(if (isEditing) "Update" else "Save")
                    }
                }
            }
        }
    }
} 
 
 