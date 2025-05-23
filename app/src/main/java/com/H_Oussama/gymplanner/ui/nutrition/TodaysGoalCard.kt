package com.H_Oussama.gymplanner.ui.nutrition

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.H_Oussama.gymplanner.ui.common.PageIndicator
import kotlin.math.min

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodaysGoalCard(
    currentCalories: Int,
    goalCalories: Int,
    currentBurnedCalories: Int = 0,
    currentProtein: Float = 0f,
    goalProtein: Float = 0f,
    currentCarbs: Float = 0f,
    goalCarbs: Float = 0f,
    onEditGoal: () -> Unit
) {
    // Create a pager state to handle paging between different metrics
    val pagerState = rememberPagerState(pageCount = { 3 }) // Three pages: Calories, Protein, Carbs
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header row with title and edit button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today ✨",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            // Edit goal button
            TextButton(
                onClick = onEditGoal,
                modifier = Modifier
                    .shadow(4.dp, shape = RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFF5252), Color(0xFFFF8A80))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Text(
                    text = "✏️ Edit Goal",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Main card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .shadow(8.dp, shape = RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Today's Goal header with info icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Page title changes based on current page
                            val pageTitle = when (pagerState.currentPage) {
                                0 -> "🔥 Calories"
                                1 -> "🥩 Protein"
                                2 -> "🍚 Carbs"
                                else -> "Today's Goal"
                            }
                            
                            Text(
                                text = pageTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            IconButton(
                                onClick = { /* Show info */ },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Goal Information",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Star/favorite button
                        IconButton(
                            onClick = { /* Toggle favorite */ },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFF9C4))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Favorite Goal",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    // Metrics pager - swipe between calories, protein, and carbs
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        when (page) {
                            0 -> {
                                // Calories page
                                CaloriesGoalPage(
                                    currentCalories = currentCalories,
                                    goalCalories = goalCalories,
                                    currentBurnedCalories = currentBurnedCalories
                                )
                            }
                            1 -> {
                                // Protein page
                                NutrientGoalPage(
                                    current = currentProtein,
                                    goal = goalProtein,
                                    nutrientName = "Protein",
                                    unit = "g",
                                    progressColor = Color(0xFF66BB6A), // Green for protein
                                    emoji = "🥩"
                                )
                            }
                            2 -> {
                                // Carbs page
                                NutrientGoalPage(
                                    current = currentCarbs,
                                    goal = goalCarbs,
                                    nutrientName = "Carbs",
                                    unit = "g",
                                    progressColor = Color(0xFFFFA726), // Orange for carbs
                                    emoji = "🍚"
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Page indicator dots with animation
                    PageIndicatorDots(
                        totalDots = 3,
                        selectedIndex = pagerState.currentPage,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CaloriesGoalPage(
    currentCalories: Int,
    goalCalories: Int,
    currentBurnedCalories: Int
) {
    // Calculate progress 
    val calorieProgress = (currentCalories.toFloat() / goalCalories).coerceIn(0f, 1f)
    
    // Use our enhanced NutritionMetricCard for a clearer display
    NutritionMetricCard(
        title = "Calories",
        icon = "🔥",
        iconTint = Color(0xFFFF9800),
        current = currentCalories,
        goal = goalCalories,
        progress = calorieProgress,
        unit = "Kcal",
        progressBackgroundColor = Color(0xFFFFF3E0).copy(alpha = 0.5f),
        progressColor = Color(0xFFFF9800)
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    // Burned calories section
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon in circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Burned Calories",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = "Burned Calories",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "$currentBurnedCalories Kcal",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Net calories calculation
            val netCalories = goalCalories - currentCalories + currentBurnedCalories
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "$netCalories Kcal",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (netCalories >= 0) Color(0xFF4CAF50) else Color(0xFFE57373)
                )
            }
        }
    }
}

@Composable
fun NutrientGoalPage(
    current: Float,
    goal: Float,
    nutrientName: String,
    unit: String,
    progressColor: Color,
    emoji: String
) {
    // Calculate remaining amount and progress
    val remaining = goal - current
    val progress = if (goal > 0) {
        min(current / goal, 1f)
    } else 0f
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background circle (track)
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(200.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            strokeWidth = 16.dp,
            strokeCap = StrokeCap.Round
        )
        
        // Progress indicator with enhanced color
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(200.dp),
            color = progressColor,
            strokeWidth = 16.dp,
            strokeCap = StrokeCap.Round
        )

        // Center text with emoji
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format("%.1f", current),
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$emoji ",
                    fontSize = 16.sp
                )
                Text(
                    text = "$unit consumed",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    // Goal information with enhanced styling
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Daily Goal
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(progressColor.copy(alpha = 0.1f))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Box with emoji for goal
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(progressColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎯",
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Goal text
            Column {
                Text(
                    text = "Daily Goal",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${String.format("%.1f", goal)} $unit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    // Consumed and Remaining sections with enhanced styling
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Consumed information
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(progressColor.copy(alpha = 0.1f))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Box with emoji for consumed
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(progressColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Consumed text
            Column {
                Text(
                    text = "Consumed",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${String.format("%.1f", current)} $unit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Remaining
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Box with emoji for remaining
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⏱️",
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Remaining text
            Column {
                Text(
                    text = "Remaining",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${String.format("%.1f", remaining)} $unit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PageIndicatorDots(
    totalDots: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(totalDots) { index ->
            val size = if (index == selectedIndex) 10.dp else 8.dp
            val color = when (index) {
                0 -> if (index == selectedIndex) Color(0xFF448AFF) else Color(0xFF448AFF).copy(alpha = 0.3f)
                1 -> if (index == selectedIndex) Color(0xFF66BB6A) else Color(0xFF66BB6A).copy(alpha = 0.3f)
                2 -> if (index == selectedIndex) Color(0xFFFFA726) else Color(0xFFFFA726).copy(alpha = 0.3f)
                else -> if (index == selectedIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            }
            
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(size)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
} 