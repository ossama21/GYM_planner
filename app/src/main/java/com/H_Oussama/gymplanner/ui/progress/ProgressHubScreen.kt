package com.H_Oussama.gymplanner.ui.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.H_Oussama.gymplanner.ui.theme.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun ProgressHubScreen(
    viewModel: ProgressHubViewModel = viewModel(),
    onNavigateToHistory: (exerciseId: String, exerciseName: String) -> Unit,
    onNavigateToNutrition: () -> Unit,
    onNavigateToWeight: () -> Unit,
    onNavigateToWorkoutLog: () -> Unit,
    onNavigateToWorkoutHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val backgroundColor = getBackgroundColor()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp) // Add padding for bottom nav bar
        ) {
            // Hero section with title and subtitle
            item {
                HeroSection()
            }
            
            // Tracking options section with cards
            item {
                SectionTitle(
                    title = "Track Your Progress",
                    subtitle = "Monitor different aspects of your fitness journey",
                    icon = Icons.Default.Timeline
                )
            }
            
            // Main tracking options in a prominent grid
            item {
                TrackingOptionsGrid(
                    onNavigateToNutrition = onNavigateToNutrition,
                    onNavigateToWeight = onNavigateToWeight,
                    onNavigateToWorkoutLog = onNavigateToWorkoutLog,
                    onNavigateToWorkoutHistory = onNavigateToWorkoutHistory
                )
            }
            
            // Exercise history section
            item {
                SectionTitle(
                    title = "Exercise History",
                    subtitle = "Review your performance by exercise",
                    icon = Icons.Default.History
                )
            }
            
            // Loading, error states or exercise list
            when {
                uiState.isLoading -> {
                    item {
                        LoadingState()
                    }
                }
                uiState.error != null -> {
                    item {
                        ErrorState(message = uiState.error!!)
                    }
                }
                uiState.loggedExercises.isEmpty() -> {
                    item {
                        EmptyState()
                    }
                }
                else -> {
                    items(uiState.loggedExercises, key = { it.id }) { exerciseInfo ->
                        ExerciseHistoryItem(
                            exerciseInfo = exerciseInfo,
                            onClick = {
                                val encodedName = URLEncoder.encode(exerciseInfo.name, StandardCharsets.UTF_8.toString())
                                onNavigateToHistory(exerciseInfo.id, encodedName)
                            }
                        )
                    }
                }
            }
            
            // Bottom spacer
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

    @Composable
private fun HeroSection() {
    val gradientStartColor = if (isSystemInDarkTheme()) Color(0xFF2C2C2E) else Color(0xFF4E76FF).copy(alpha = 0.7f)
    val gradientEndColor = getBackgroundColor()
    val textColor = getTextColor()
    val secondaryTextColor = getSecondaryTextColor()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        gradientStartColor,
                        gradientEndColor
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Text(
                text = "Your Progress",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Track, analyze, and improve your fitness journey",
                style = MaterialTheme.typography.bodyLarge,
                color = secondaryTextColor
            )
        }
        
        // Decorative gradient circle
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
                .align(Alignment.TopEnd)
        )
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String, icon: ImageVector) {
    val textColor = getTextColor()
    val secondaryTextColor = getSecondaryTextColor()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryTextColor
            )
        }
    }
}

@Composable
private fun TrackingOptionsGrid(
    onNavigateToNutrition: () -> Unit,
    onNavigateToWeight: () -> Unit,
    onNavigateToWorkoutLog: () -> Unit,
    onNavigateToWorkoutHistory: () -> Unit
) {
    val cardColor = getCardColor()
    val textColor = getTextColor()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TrackingCard(
                title = "Nutrition",
                icon = Icons.Default.Restaurant,
                modifier = Modifier.weight(1f),
                cardColor = cardColor,
                textColor = textColor,
                onClick = onNavigateToNutrition
            )
            
            TrackingCard(
                title = "Weight",
                icon = Icons.Default.MonitorWeight,
                modifier = Modifier.weight(1f),
                cardColor = cardColor,
                textColor = textColor,
                onClick = onNavigateToWeight
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TrackingCard(
                title = "Workout Log",
                icon = Icons.Default.FitnessCenter,
                modifier = Modifier.weight(1f),
                cardColor = cardColor,
                textColor = textColor,
                onClick = onNavigateToWorkoutLog
            )
            
            TrackingCard(
                title = "Workout History",
                icon = Icons.Default.CalendarMonth,
                modifier = Modifier.weight(1f),
                cardColor = cardColor,
                textColor = textColor,
                onClick = onNavigateToWorkoutHistory
            )
        }
    }
}

@Composable
private fun TrackingCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    cardColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = title,
                color = textColor,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ExerciseHistoryItem(
    exerciseInfo: LoggedExerciseInfo,
    onClick: () -> Unit
) {
    val cardColor = getCardColor()
    val textColor = getTextColor()
    val secondaryTextColor = getSecondaryTextColor()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exercise icon
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Exercise details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = exerciseInfo.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                
                Text(
                    text = "View exercise history",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor
                )
            }
            
            // Arrow icon
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "View history",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LoadingState() {
    val textColor = getTextColor()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Loading exercise history...",
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
        }
    }
}

@Composable
private fun ErrorState(message: String) {
    val textColor = getTextColor()
    val secondaryTextColor = getSecondaryTextColor()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Oops! Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = textColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = secondaryTextColor
            )
        }
    }
}

@Composable
private fun EmptyState() {
    val textColor = getTextColor()
    val secondaryTextColor = getSecondaryTextColor()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No exercise history yet",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = textColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Start tracking your workouts to see your exercise history here.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = secondaryTextColor
            )
        }
    }
}