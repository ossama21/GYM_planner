package com.H_Oussama.gymplanner.ui.workoutplan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.H_Oussama.gymplanner.data.model.ExerciseDefinition
import com.H_Oussama.gymplanner.data.model.ExerciseInstance
import com.H_Oussama.gymplanner.ui.common.LoadingSpinner
import com.H_Oussama.gymplanner.ui.common.ErrorCard
import com.H_Oussama.gymplanner.ui.common.ExerciseImage
import java.util.concurrent.TimeUnit
import androidx.activity.compose.BackHandler
import androidx.hilt.navigation.compose.hiltViewModel
import com.H_Oussama.gymplanner.data.model.MuscleGroup
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.zIndex
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.H_Oussama.gymplanner.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.BorderStroke
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalUriHandler
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.ui.res.painterResource
import com.H_Oussama.gymplanner.R
import com.H_Oussama.gymplanner.utils.ExerciseImageDownloader
import android.util.Log

// Utility function to format time in mm:ss format
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutExecutionScreen(
    viewModel: WorkoutExecutionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle navigation back
    LaunchedEffect(uiState.shouldNavigateBack) {
        if (uiState.shouldNavigateBack) {
            onNavigateBack()
        }
    }
    
    // If workout is complete, show workout complete screen
    if (uiState.workoutComplete) {
        WorkoutCompleteScreen(
            totalSets = uiState.totalSetsCompleted,
            totalWeight = uiState.totalWeightLifted,
            totalReps = uiState.totalReps,
            durationMinutes = uiState.workoutDurationSeconds / 60,
            caloriesBurned = uiState.totalCaloriesBurned,
            onFinish = { viewModel.finishAndSaveWorkout() }
        )
        return
    }
    
    // Main workout execution screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Main content
            Column(
                            modifier = Modifier
                    .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp) // Space for bottom action bar
        ) {
            // Exercise details
            if (uiState.currentExerciseDefinition != null) {
                // Exercise name and image
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add a back button
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
            Text(
                    text = uiState.currentExerciseDefinition?.name ?: "Exercise",
                        style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Empty space for alignment
                    Box(modifier = Modifier.size(48.dp))
                }
                
                // Exercise info showing current exercise number / total
                val workoutDay = uiState.workoutDay
                if (workoutDay != null) {
                    Text(
                        text = "Exercise ${uiState.currentExerciseIndex + 1} of ${workoutDay.exercises.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
                
                // Exercise image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            ExerciseAnatomyImage(
                exerciseDefinition = uiState.currentExerciseDefinition,
                modifier = Modifier.fillMaxSize()
            )
        }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Workout summary
                WorkoutSummaryCard(
                    setsCompleted = uiState.totalSetsCompleted,
                    totalWeight = uiState.totalWeightLifted,
                    totalReps = uiState.totalReps,
                    durationMinutes = uiState.workoutDurationSeconds / 60,
                    caloriesBurned = uiState.totalCaloriesBurned,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Set tracking
        Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1D2A)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Title and progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sets",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "${uiState.currentSet} of ${uiState.targetSets ?: 3}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF3B82F6),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Set tracking table
                SetTrackingTable(
                    currentSet = uiState.currentSet,
                    targetSets = uiState.targetSets ?: 3,
                            currentSetDuration = uiState.currentSetDurationSeconds,
                            onStartSet = { viewModel.startExerciseSet() },
                            onLogSet = { viewModel.logCompletedSet() },
                    previousSetData = uiState.previousSetData
                )
            
                        Spacer(modifier = Modifier.height(16.dp))
            
                        // Rep and weight input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                            // Reps input
                            OutlinedTextField(
                                value = uiState.loggedReps,
                                onValueChange = { viewModel.onRepsChanged(it) },
                                label = { Text("Reps") },
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number
                                ),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color.Gray,
                                    focusedLabelColor = Color(0xFF3B82F6),
                                    unfocusedLabelColor = Color.Gray,
                                    cursorColor = Color(0xFF3B82F6),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            
                            // Weight input
                            OutlinedTextField(
                                value = uiState.loggedWeight,
                                onValueChange = { viewModel.onWeightChanged(it) },
                                label = { Text("Weight (kg)") },
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number
                                ),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color.Gray,
                                    focusedLabelColor = Color(0xFF3B82F6),
                                    unfocusedLabelColor = Color.Gray,
                                    cursorColor = Color(0xFF3B82F6),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                        )
                    }
                }
            }
        }
        
            // Timer and Action icons
            Row(
                    modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timer display
                Column {
                    Text(
                        text = "Workout Time",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
            Text(
                        text = formatTime(uiState.workoutDurationSeconds),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
                }
                
                // Sound toggle
                IconButton(onClick = { viewModel.toggleSound() }) {
                    Icon(
                        imageVector = if (uiState.isSoundEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeMute,
                        contentDescription = "Toggle sound",
                        tint = if (uiState.isSoundEnabled) Color(0xFF3B82F6) else Color.Gray
                    )
                }
            }
        }
        
        // Rest timer overlay
        if (uiState.timerState == TimerState.RunningRest) {
            RestTimerOverlay(
                remainingSeconds = uiState.remainingTimeSeconds,
                totalSeconds = uiState.initialDurationSeconds ?: 0,
                onSkip = { viewModel.stopTimer() }
            )
        }
        
        // Bottom navigation bar for exercise navigation
        if (uiState.workoutDay != null) {
            var showFinishConfirmationDialog by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0xFF1F1F1F))
                    .height(80.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
                    // Previous exercise button
                    Button(
                        onClick = { viewModel.previousExercise() },
                        enabled = uiState.currentExerciseIndex > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3D3D3D),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF252525),
                            disabledContentColor = Color.Gray
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous exercise",
                        modifier = Modifier.size(20.dp)
                    )
                            Spacer(modifier = Modifier.width(4.dp))
                Text(
                                "Previous",
                                style = TextStyle(fontSize = 13.sp)
                            )
                        }
                    }
                    
                    // Finish workout button
                    Button(
                        onClick = { showFinishConfirmationDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Finish Workout")
                    }
                    
                    // Next exercise button
                    Button(
                        onClick = { viewModel.nextExercise() },
                        enabled = uiState.workoutDay?.let { uiState.currentExerciseIndex < it.exercises.size - 1 } ?: false,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3D3D3D),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF252525),
                            disabledContentColor = Color.Gray
                        ),
            modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .padding(start = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                                "Next",
                                style = TextStyle(fontSize = 13.sp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next exercise",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            // Confirmation dialog
            if (showFinishConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showFinishConfirmationDialog = false },
                    title = {
                        Text("Finish Workout")
                    },
                    text = {
                        Text("Are you sure you want to finish this workout?")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showFinishConfirmationDialog = false
                                viewModel.finishWorkout()
                            }
                        ) {
                            Text("Yes, Finish", color = Color(0xFF1976D2))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showFinishConfirmationDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    },
                    containerColor = Color(0xFF252525),
                    textContentColor = Color.White,
                    titleContentColor = Color.White
                )
            }
        }
    }
}

@Composable
private fun ExerciseAnatomyImage(
    exerciseDefinition: ExerciseDefinition?,
    modifier: Modifier = Modifier
) {
    val imageDownloader = hiltViewModel<ExerciseImageDownloader>()
    val downloadState by imageDownloader.downloadState.collectAsState()
    var refreshTrigger by remember { mutableStateOf(0) }
    val context = LocalContext.current
    var imageLoadError by remember { mutableStateOf(false) }
    var loadAttempted by remember { mutableStateOf(false) }
    
    // Log which exercise we're trying to display
    LaunchedEffect(exerciseDefinition) {
        if (exerciseDefinition != null) {
            imageLoadError = false
            loadAttempted = false
            Log.d("ExerciseAnatomyImage", "Attempting to load exercise: ${exerciseDefinition.name}, " +
                    "id: ${exerciseDefinition.id}, imageIdentifier: ${exerciseDefinition.imageIdentifier}")
        } else {
            Log.d("ExerciseAnatomyImage", "Exercise definition is null")
        }
    }
    
    // Listen for successful downloads and trigger a refresh
    LaunchedEffect(downloadState) {
        if (downloadState is ExerciseImageDownloader.DownloadState.Success) {
            // Increment the refresh trigger to force recomposition
            refreshTrigger++
            imageLoadError = false
            // Log when a successful download is detected to help with debugging
            Log.d("ExerciseAnatomyImage", "Download success detected, refreshing image")
        }
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1C26)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (exerciseDefinition != null) {
                // Key with refreshTrigger to force recomposition when updated
                key(exerciseDefinition.id, refreshTrigger) {
                    Log.d("ExerciseAnatomyImage", "Loading exercise GIF: ${exerciseDefinition.name}")
                    
                    com.H_Oussama.gymplanner.ui.common.ExerciseImage(
                        exerciseName = exerciseDefinition.name,
                        imageIdentifier = exerciseDefinition.imageIdentifier,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentScale = ContentScale.Fit,
                        onSearchClick = {
                            // Log search click event
                            Log.d("ExerciseAnatomyImage", "Search clicked for: ${exerciseDefinition.name}")
                            imageDownloader.searchImage(
                                com.H_Oussama.gymplanner.data.exercise.ExerciseDefinition(
                                    id = exerciseDefinition.id,
                                    name = exerciseDefinition.name,
                                    description = exerciseDefinition.description,
                                    imageIdentifier = exerciseDefinition.imageIdentifier,
                                    met = exerciseDefinition.met
                                ),
                                forceWebSearch = true
                            )
                        }
                    )
                    
                    loadAttempted = true
                }
                
                // Show fallback text if loading the image takes too long
                var showFallbackText by remember { mutableStateOf(false) }
                
                LaunchedEffect(exerciseDefinition.id, refreshTrigger) {
                    showFallbackText = false
                    delay(2000) // Wait a bit longer (2 seconds) to see if image loads
                    if (loadAttempted) {
                        showFallbackText = true
                    }
                }
                
                if (showFallbackText) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .alpha(0.8f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = exerciseDefinition.name,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Loading animation or tap Search to find one",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                // Web search button - Always visible and prominent
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            // Search for an image online
                            Log.d("ExerciseAnatomyImage", "Web search button clicked for: ${exerciseDefinition.name}")
                            imageDownloader.searchImage(
                                com.H_Oussama.gymplanner.data.exercise.ExerciseDefinition(
                                    id = exerciseDefinition.id,
                                    name = exerciseDefinition.name,
                                    description = exerciseDefinition.description,
                                    imageIdentifier = exerciseDefinition.imageIdentifier,
                                    met = exerciseDefinition.met
                                ),
                                forceWebSearch = true
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search web for image",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            "Web",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Show placeholder if no exercise is selected
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            
            // Handle image download states
            when (downloadState) {
                is ExerciseImageDownloader.DownloadState.Searching -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x80000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Searching web...",
                                color = Color.White
                            )
                        }
                    }
                }
                is ExerciseImageDownloader.DownloadState.Results -> {
                    val result = downloadState as ExerciseImageDownloader.DownloadState.Results
                    Dialog(onDismissRequest = { imageDownloader.resetState() }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Preview Image",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                
                                Image(
                                    bitmap = result.previewBitmap.asImageBitmap(),
                                    contentDescription = "Preview",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    OutlinedButton(
                                        onClick = { imageDownloader.resetState() },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Cancel")
                                    }
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Button(
                                        onClick = { 
                                            imageDownloader.downloadImage()
                                            // Log when save button is clicked
                                            Log.d("ExerciseAnatomyImage", "Save button clicked for ${exerciseDefinition?.name}")
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Save")
                                    }
                                }
                            }
                        }
                    }
                }
                is ExerciseImageDownloader.DownloadState.Downloading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x80000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Downloading...",
                                color = Color.White
                            )
                        }
                    }
                }
                is ExerciseImageDownloader.DownloadState.Success -> {
                    // Show a temporary success message
                    LaunchedEffect(Unit) {
                        Log.d("ExerciseAnatomyImage", "Success state visible, image saved")
                        // Reset state after a brief delay to allow the user to see the success state
                        delay(1000)
                        imageDownloader.resetState()
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x80000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Success",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Image saved!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                else -> { /* Nothing to do for other states */ }
            }
        }
    }
}

@Composable
private fun WorkoutSummaryCard(
    setsCompleted: Int,
    totalWeight: Double,
    totalReps: Int,
    durationMinutes: Int,
    caloriesBurned: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF293244)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
            modifier = Modifier.padding(16.dp)
                            ) {
                Text(
                text = "Workout Summary",
                style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
            Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    value = "$setsCompleted",
                    label = "Sets",
                    icon = Icons.Default.FitnessCenter
                )
                SummaryItem(
                    value = "${totalWeight.toInt()}",
                    label = "Weight (kg)",
                    icon = Icons.Default.Balance
                )
                SummaryItem(
                    value = "$totalReps",
                    label = "Reps",
                    icon = Icons.Default.RepeatOne
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    value = "$durationMinutes",
                    label = "Minutes",
                    icon = Icons.Default.Timer
                )
                SummaryItem(
                    value = "${caloriesBurned.toInt()}",
                    label = "Calories",
                    icon = Icons.Default.LocalFireDepartment
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    value: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            ) {
                Icon(
            imageVector = icon,
                    contentDescription = null,
            tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(24.dp)
                )
                Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
private fun WorkoutCompleteScreen(
    totalSets: Int,
    totalWeight: Double,
    totalReps: Int,
    durationMinutes: Int,
    caloriesBurned: Double,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
        Column(
        modifier = modifier
                .fillMaxSize()
            .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Celebration animation or icon
                Icon(
            imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
            tint = Color(0xFFFFD700), // Gold color
            modifier = Modifier.size(128.dp)
                )
            
        Spacer(modifier = Modifier.height(16.dp))
            
                    Text(
            text = "Workout Complete!",
                style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
            Text(
            text = "Great job on completing your workout",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
        // Summary stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            WorkoutStatItem(
                value = "$totalSets",
                label = "Sets",
                icon = Icons.Default.FitnessCenter
            )
                            WorkoutStatItem(
                value = "${totalReps}",
                label = "Reps",
                icon = Icons.Default.RepeatOne
            )
                            WorkoutStatItem(
                value = "${totalWeight.toInt()}kg",
                label = "Weight",
                icon = Icons.Default.Balance
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
                            WorkoutStatItem(
                value = "$durationMinutes",
                label = "Minutes",
                icon = Icons.Default.Timer
            )
                            WorkoutStatItem(
                value = "${caloriesBurned.toInt()}",
                label = "Calories",
                icon = Icons.Default.LocalFireDepartment
            )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
        // Action buttons
        Button(
            onClick = onFinish,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6)
                ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Save Workout",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth()
        ) {
                Text(
                text = "Return to Home",
                    color = Color.Gray
                ) 
        }
    }
}

@Composable
private fun WorkoutStatItem(
    value: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            ) {
                Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(24.dp)
            )
                Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun SetTrackingTable(
    currentSet: Int,
    targetSets: Int,
    currentSetDuration: Int = 0,
    onStartSet: () -> Unit,
    onLogSet: () -> Unit,
    previousSetData: Map<Int, Pair<Int, Double>> = emptyMap(),
    modifier: Modifier = Modifier
        ) {
            Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1D2433))
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Set",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.weight(0.2f),
                textAlign = TextAlign.Center
            )
                Text(
                text = "Previous",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.weight(0.4f),
                textAlign = TextAlign.Center
            )
                Text(
                text = "Target",
                style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                modifier = Modifier.weight(0.4f),
                    textAlign = TextAlign.Center
                )
        }
        
        // Set rows
        for (setNumber in 1..targetSets) {
            val isCurrentSet = setNumber == currentSet
            val isPreviousSet = setNumber < currentSet
            val isCompletedSet = previousSetData.containsKey(setNumber)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isCurrentSet) Color(0xFF1A237E).copy(alpha = 0.2f) else Color.Transparent)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Set number
                Text(
                    text = "$setNumber",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCurrentSet) Color(0xFF3B82F6) else Color.White,
                    fontWeight = if (isCurrentSet) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(0.2f),
                    textAlign = TextAlign.Center
                )
                
                // Previous reps and weight
                Text(
                    text = if (previousSetData.containsKey(setNumber)) {
                        val (reps, weight) = previousSetData[setNumber]!!
                        "$reps reps × ${weight.toInt()} kg"
                    } else if (setNumber < currentSet && previousSetData.containsKey(setNumber - 1)) {
                        val (reps, weight) = previousSetData[setNumber - 1]!!
                        "$reps reps × ${weight.toInt()} kg"
                    } else {
                        "-"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.weight(0.4f),
                    textAlign = TextAlign.Center
                )
                
                // Target column / action button
                if (isCurrentSet) {
                    if (currentSetDuration > 0) {
                        // Show log button when set is in progress
             OutlinedButton(
                            onClick = onLogSet,
                            border = BorderStroke(1.dp, Color(0xFF3B82F6)),
                        colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF3B82F6)
                        ),
                        modifier = Modifier
                                .weight(0.4f)
                                .height(36.dp)
                    ) {
                            Text(
                                text = "Log set",
                                style = MaterialTheme.typography.bodySmall
                            )
                    }
                    } else {
                        // Show start button when set hasn't started
                    Button(
                            onClick = onStartSet,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6)
                        ),
                        modifier = Modifier
                                .weight(0.4f)
                                .height(36.dp)
                        ) {
                            Text(
                                text = "Start set",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
    } else {
                    // Show checkmark for completed sets
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(0.4f)
                    ) {
                        if (isCompletedSet) {
            Icon(
                                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
            )
                        } else {
        Text(
                                text = "-",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RestTimerOverlay(
    remainingSeconds: Int,
    totalSeconds: Int,
    onSkip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC14161F)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rest",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
                Text(
                text = "$remainingSeconds",
                    style = TextStyle(
                    fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onSkip,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6)
                ),
                modifier = Modifier
                    .width(130.dp)
                    .height(48.dp)
            ) {
                Text("Skip Rest")
            }
        }
    }
}