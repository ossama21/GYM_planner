package com.H_Oussama.gymplanner.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutComparisonScreen(
    viewModel: WorkoutComparisonViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Comparison") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            uiState.currentWorkout == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Workout not found",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            else -> {
                val current = uiState.currentWorkout!!
                val previous = uiState.previousWorkout
                val stats = uiState.improvementStats
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Workout name and date
                    Text(
                        text = current.workoutName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = current.formattedDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (previous == null) {
                        // No previous workout for comparison
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No Previous Workout Found",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Complete this workout again to see a comparison with this session.",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        // Comparison section
                        Text(
                            text = "Comparing with: ${previous.formattedDate}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Stats comparison
                        if (stats != null) {
                            // Duration comparison
                            ComparisonCard(
                                title = "Duration",
                                current = "${current.durationMinutes} min",
                                previous = "${previous.durationMinutes} min",
                                changePercent = stats.durationChangePercent,
                                changeText = "${abs(stats.durationChange)} min ${if (stats.durationChange >= 0) "longer" else "shorter"}"
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Reps comparison
                            ComparisonCard(
                                title = "Total Reps",
                                current = "${current.totalReps}",
                                previous = "${previous.totalReps}",
                                changePercent = stats.repsChangePercent,
                                changeText = "${abs(stats.repsChange)} reps ${if (stats.repsChange >= 0) "more" else "less"}"
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Weight comparison
                            ComparisonCard(
                                title = "Weight Lifted",
                                current = "${current.totalWeightLifted.toInt()} kg",
                                previous = "${previous.totalWeightLifted.toInt()} kg",
                                changePercent = stats.weightChangePercent,
                                changeText = "${abs(stats.weightChange.toInt())} kg ${if (stats.weightChange >= 0) "more" else "less"}"
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Calories comparison
                            ComparisonCard(
                                title = "Calories Burned",
                                current = "${current.caloriesBurned.toInt()} kcal",
                                previous = "${previous.caloriesBurned.toInt()} kcal",
                                changePercent = stats.caloriesChangePercent,
                                changeText = "${abs(stats.caloriesChange.toInt())} kcal ${if (stats.caloriesChange >= 0) "more" else "less"}"
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Visual comparison chart
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Visual Comparison",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    ComparisonBarChart(
                                        currentWorkout = current,
                                        previousWorkout = previous
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
private fun ComparisonCard(
    title: String,
    current: String,
    previous: String,
    changePercent: Double,
    changeText: String
) {
    val isImprovement = changePercent >= 0
    val changeColor = if (isImprovement) 
        Color(0xFF4CAF50) // Green
    else 
        Color(0xFFF44336) // Red
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Current value
                Column {
                    Text(
                        text = "Current",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = current,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Previous value
                Column {
                    Text(
                        text = "Previous",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = previous,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                // Change indicator
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format("%.1f%%", abs(changePercent)),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = changeColor
                        )
                        
                        Icon(
                            imageVector = if (isImprovement) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = changeColor
                        )
                    }
                    
                    Text(
                        text = changeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = changeColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonBarChart(
    currentWorkout: WorkoutComparisonData,
    previousWorkout: WorkoutComparisonData
) {
    val barWidth = 40.dp
    val barSpacing = 30.dp
    val chartHeight = 200.dp
    
    // Normalize values for chart display
    val maxReps = maxOf(currentWorkout.totalReps, previousWorkout.totalReps).toFloat()
    val maxWeight = maxOf(currentWorkout.totalWeightLifted, previousWorkout.totalWeightLifted).toFloat()
    val maxCalories = maxOf(currentWorkout.caloriesBurned, previousWorkout.caloriesBurned).toFloat()
    
    val currentRepsHeight = (currentWorkout.totalReps.toFloat() / maxReps) * chartHeight.value
    val previousRepsHeight = (previousWorkout.totalReps.toFloat() / maxReps) * chartHeight.value
    
    val currentWeightHeight = (currentWorkout.totalWeightLifted.toFloat() / maxWeight) * chartHeight.value
    val previousWeightHeight = (previousWorkout.totalWeightLifted.toFloat() / maxWeight) * chartHeight.value
    
    val currentCaloriesHeight = (currentWorkout.caloriesBurned.toFloat() / maxCalories) * chartHeight.value
    val previousCaloriesHeight = (previousWorkout.caloriesBurned.toFloat() / maxCalories) * chartHeight.value
    
    val currentColor = MaterialTheme.colorScheme.primary
    val previousColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .padding(vertical = 8.dp)
        ) {
            // Grid lines
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                // Draw horizontal grid lines
                val lineCount = 5
                for (i in 0..lineCount) {
                    val y = size.height * (1 - i.toFloat() / lineCount)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                }
            }
            
            // Bars
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // Reps bars
                BarGroup(
                    label = "Reps",
                    currentHeight = currentRepsHeight.dp,
                    previousHeight = previousRepsHeight.dp,
                    currentColor = currentColor,
                    previousColor = previousColor,
                    barWidth = barWidth
                )
                
                // Weight bars
                BarGroup(
                    label = "Weight",
                    currentHeight = currentWeightHeight.dp,
                    previousHeight = previousWeightHeight.dp,
                    currentColor = currentColor,
                    previousColor = previousColor,
                    barWidth = barWidth
                )
                
                // Calories bars
                BarGroup(
                    label = "Calories",
                    currentHeight = currentCaloriesHeight.dp,
                    previousHeight = previousCaloriesHeight.dp,
                    currentColor = currentColor,
                    previousColor = previousColor,
                    barWidth = barWidth
                )
            }
        }
        
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(currentColor, RoundedCornerShape(2.dp))
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = "Current",
                style = MaterialTheme.typography.bodySmall
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(previousColor, RoundedCornerShape(2.dp))
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = "Previous",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun BarGroup(
    label: String,
    currentHeight: androidx.compose.ui.unit.Dp,
    previousHeight: androidx.compose.ui.unit.Dp,
    currentColor: Color,
    previousColor: Color,
    barWidth: androidx.compose.ui.unit.Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.width(barWidth * 2 + 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // Current bar
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(currentHeight)
                    .background(
                        color = currentColor,
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                    )
            )
            
            // Previous bar
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(previousHeight)
                    .background(
                        color = previousColor,
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp
        )
    }
} 