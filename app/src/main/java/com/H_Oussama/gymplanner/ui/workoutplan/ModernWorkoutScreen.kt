package com.H_Oussama.gymplanner.ui.workoutplan

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.H_Oussama.gymplanner.data.model.ExerciseInstance
import com.H_Oussama.gymplanner.data.model.MuscleGroup
import com.H_Oussama.gymplanner.data.model.Weekday
import com.H_Oussama.gymplanner.data.model.WorkoutDay
import com.H_Oussama.gymplanner.ui.common.BodyPartImage
import com.H_Oussama.gymplanner.ui.common.ExerciseImage
import com.H_Oussama.gymplanner.ui.common.RestDayImage
import com.H_Oussama.gymplanner.ui.navigation.Routes
import com.H_Oussama.gymplanner.data.model.ExerciseDefinition
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.runtime.mutableFloatStateOf
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernWorkoutScreen(
    navController: NavController,
    viewModel: WorkoutPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val workoutDays = uiState.workoutDays
    val currentDayIndex = uiState.currentDayIndex
    val isLoading = uiState.isLoading
    val isParsing = uiState.isParsing
    val isNormalizingNames = uiState.isNormalizingNames
    val parseError = uiState.parseError
    val normalizationError = uiState.normalizationError
    
    // Show error snackbar if there's a normalization error
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(normalizationError) {
        if (normalizationError != null) {
            snackbarHostState.showSnackbar(
                message = normalizationError,
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Long
            )
        }
    }
    
    // Get plan name
    val planName = uiState.planName ?: "My Workouts"
    
    // Get current weekday
    val calendar = Calendar.getInstance()
    val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val currentWeekday = when (currentDayOfWeek) {
        Calendar.MONDAY -> Weekday.MONDAY
        Calendar.TUESDAY -> Weekday.TUESDAY
        Calendar.WEDNESDAY -> Weekday.WEDNESDAY
        Calendar.THURSDAY -> Weekday.THURSDAY
        Calendar.FRIDAY -> Weekday.FRIDAY
        Calendar.SATURDAY -> Weekday.SATURDAY
        else -> Weekday.SUNDAY
    }
    
    // State to track the selected weekday (default to today)
    var selectedWeekday by remember { mutableStateOf(currentWeekday) }
    
    // Find workout for the selected weekday
    val selectedWorkout = remember(selectedWeekday, workoutDays) {
        // First try to find by exact regex pattern match
        val weekdayPattern = "\\[${selectedWeekday.getShortName()}\\]".toRegex(RegexOption.IGNORE_CASE)
        val matchByPattern = workoutDays.find {
            it.dayName.contains(weekdayPattern) ||
            it.dayName.contains("\\[${selectedWeekday.name}\\]".toRegex(RegexOption.IGNORE_CASE))
        }
        
        // If pattern match found, return it
        if (matchByPattern != null) {
            return@remember matchByPattern
        }
        
        // Otherwise, use index-based fallback
        val dayIndex = when(selectedWeekday) {
            Weekday.MONDAY -> 0
            Weekday.TUESDAY -> 1
            Weekday.WEDNESDAY -> 2
            Weekday.THURSDAY -> 3
            Weekday.FRIDAY -> 4
            Weekday.SATURDAY -> 5
            Weekday.SUNDAY -> 6
        }
        
        // Return the workout at the calculated index if available
        if (dayIndex < workoutDays.size) {
            workoutDays[dayIndex]
        } else {
            workoutDays.firstOrNull()
        }
    }
    
    // Current date for display
    val currentDate = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date())
    
    // State for managing delete confirmation dialog
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    // State for managing rest day details dialog
    var showRestDayDetails by remember { mutableStateOf(false) }
    var currentRestDay by remember { mutableStateOf<WorkoutDay?>(null) }
    
    // Track import text state
    var importText by remember { mutableStateOf("") }
    
    // Background animation
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val backgroundOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "background"
    )
    
    // Handle the isEditing state from the ViewModel
    if (uiState.isEditing) {
        LaunchedEffect(key1 = uiState.isEditing) {
            // Navigate to a simple text editor to edit the workout plan directly
            navController.navigate(Routes.WORKOUT_PLAN)
            // Reset the editing state after navigation
            viewModel.clearEditingState()
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            containerColor = Color(0xFF1C1C1E),
            textContentColor = Color.White,
            titleContentColor = Color.White,
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Workout Plan") },
            text = { Text("Are you sure you want to delete your workout plan? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteWorkoutPlan()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935) // Red for delete
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmation = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Rest Day Details Dialog
    if (showRestDayDetails && currentRestDay != null) {
        RestDayDetailsDialog(
            restDay = currentRestDay!!,
            weekday = selectedWeekday,
            onDismiss = { 
                showRestDayDetails = false 
                currentRestDay = null
            }
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            // App bar - only show options when there's a workout plan
            ModernAppBar(
                title = planName,
                date = currentDate, 
                onEditWorkoutPlan = {
                    viewModel.editWorkoutPlan()
                }, 
                onDeleteWorkoutPlan = {
                    // Show confirmation dialog instead of direct deletion
                    showDeleteConfirmation = true
                },
                onRefreshImages = {
                    viewModel.refreshImages()
                },
                showOptions = workoutDays.isNotEmpty()
            )
            
            // Conditional content display based on loading state and data presence
            if (isLoading && workoutDays.isEmpty()) {
                // Initial loading state (full screen loader)
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading Workout Plan...", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else if (workoutDays.isEmpty()) {
                // No plan, and not in the initial loading phase -> show import options
                EmptyWorkoutState(
                    navController = navController,
                    onImportClicked = { viewModel.importWorkoutPlan(importText) },
                    onCreateNewClicked = { /* TODO: Navigate to create workout plan */ },
                    onImportTextChanged = { importText = it },
                    importText = importText,
                    onShowFormatHelp = { /* TODO: Show help */ },
                    onInsertExample = { 
                        val example = if (importText.trim().startsWith("{")) getSampleJsonWorkoutPlan() else getSampleWorkoutPlan()
                        importText = example
                     },
                    isParsing = isParsing,
                    isNormalizingNames = isNormalizingNames,
                    parseError = parseError,
                    normalizationError = normalizationError
                )
            } else {
                // Main content area
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Display normalization error banner if present
                    normalizationError?.let { error ->
                        ErrorBanner(
                            message = error,
                            onDismiss = {
                                // Clear normalization error
                                viewModel.clearNormalizationError()
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Weekday selector (keep outside the scrollable area)
                    WeekdaySelector(
                        selectedWeekday = selectedWeekday,
                        currentWeekday = currentWeekday,
                        onWeekdaySelected = { selectedWeekday = it },
                        workoutDays = workoutDays
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Scrollable content area
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                    // Show selected day's workout
                    if (selectedWorkout != null) {
                        // Featured workout section for the selected day
                        FeaturedWorkoutSection(
                            workoutDay = selectedWorkout,
                            weekday = selectedWeekday,
                            onWorkoutClick = { workoutDay ->
                                if (workoutDay.isRestDay) {
                                    // For rest days, show the rest day details dialog
                                    currentRestDay = workoutDay
                                    showRestDayDetails = true
                                } else {
                                    // For workout days, navigate to the execution screen
                                    val dayIndex = workoutDays.indexOf(workoutDay)
                                    if (dayIndex >= 0) {
                                        navController.navigate("${Routes.WORKOUT_EXECUTION_ROUTE}/$dayIndex")
                                    }
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Selected Day's Exercises - replacing the All Workouts section
                        DayExercisesSection(
                            workoutDay = selectedWorkout,
                            exerciseDefinitions = uiState.exerciseDefinitions,
                            onExerciseClick = { /* TODO: Handle exercise click */ }
                        )
                    } else {
                        // No workout for the selected day
                        EmptyDayPlaceholder(weekday = selectedWeekday)
                        }
                        
                        // Add bottom padding for better scrolling experience
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Overlay for "Refreshing Images..."
        // This appears when isLoading is true AND workoutDays are already being shown
        if (isLoading && workoutDays.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)) // Darker overlay for better contrast
                    .clickable(enabled = false) {}, // Consume clicks to prevent interaction with content below
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Refreshing Images...",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Add snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { data ->
            Snackbar(
                action = {
                    TextButton(onClick = { snackbarHostState.currentSnackbarData?.dismiss() }) {
                        Text(data.visuals.actionLabel ?: "Dismiss", color = Color.White)
                    }
                },
                containerColor = Color(0xFF2C2C2E),
                contentColor = Color.White
            ) {
                Text(data.visuals.message)
            }
        }
    }
}

@Composable
fun ModernAppBar(
    title: String,
    date: String,
    onEditWorkoutPlan: () -> Unit,
    onDeleteWorkoutPlan: () -> Unit,
    onRefreshImages: () -> Unit,
    showOptions: Boolean = true
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 28.sp
                ),
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        
        // Only show the menu button if there's a workout plan
        if (showOptions) {
            Box {
                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2C2C2E)
                    ),
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { showMenu = true }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFF2C2C2E))
                ) {
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = "Refresh Images",
                                color = Color.White
                            )
                        },
                        onClick = {
                            onRefreshImages()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Images"
                            )
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = "Edit Workout Plan",
                                color = Color.White
                            )
                        },
                        onClick = {
                            onEditWorkoutPlan()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = "Delete Workout Plan",
                                color = Color.White
                            )
                        },
                        onClick = {
                            onDeleteWorkoutPlan()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyWorkoutState(
    navController: NavController,
    onImportClicked: () -> Unit,
    onCreateNewClicked: () -> Unit,
    onImportTextChanged: (String) -> Unit,
    importText: String,
    onShowFormatHelp: () -> Unit,
    onInsertExample: () -> Unit,
    isParsing: Boolean = false,
    isNormalizingNames: Boolean = false,
    parseError: String? = null,
    normalizationError: String? = null
) {
    // State for managing help dialog
    var showFormatHelpDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val loadFromFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val jsonText = inputStream?.bufferedReader().use { reader -> reader?.readText() } ?: ""
                onImportTextChanged(jsonText)
                inputStream?.close()
                Toast.makeText(context, "File loaded successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // Normalization progress
    var normalizationProgress by remember { mutableFloatStateOf(0f) }
    val normalizationAnimatedProgress by animateFloatAsState(
        targetValue = if (isNormalizingNames) normalizationProgress else 0f,
        animationSpec = tween(300),
        label = "progress"
    )
    
    // Update progress animation
    LaunchedEffect(isNormalizingNames) {
        if (isNormalizingNames) {
            // Simulate progress since we don't have real-time progress data
            normalizationProgress = 0f
            while (isNormalizingNames && normalizationProgress < 0.95f) {
                delay(300)
                normalizationProgress += 0.1f
                if (normalizationProgress > 0.95f) normalizationProgress = 0.95f
            }
        } else if (normalizationProgress > 0) {
            // Reset when done
            normalizationProgress = 0f
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No Workout Plan",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Import a workout plan or create a new one",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { navController.navigate(Routes.WORKOUT_PLAN_EDITOR) },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Create New Workout Plan")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = { loadFromFileLauncher.launch("application/json") },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
                Icon(
                imageVector = Icons.Default.FileOpen,
                    contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Import JSON from Storage")
        }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
            "Or paste an existing plan:",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
        OutlinedTextField(
            value = importText,
            onValueChange = onImportTextChanged,
            label = { Text("Paste workout plan here") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5,
            maxLines = 10
        )
        
                Row(
                    modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { showFormatHelpDialog = true }) {
                Text("Format Help")
            }
            
            TextButton(onClick = onInsertExample) {
                Text("Insert Example")
            }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
        // Display parse errors
        if (parseError != null) {
            ErrorMessage(errorText = parseError)
        }
        
        // Display normalization errors (only display if parse was successful)
        if (parseError == null && normalizationError != null) {
            ErrorMessage(
                errorText = normalizationError,
                icon = Icons.Default.Warning
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = onImportClicked,
            // Only disable the button when actively processing or the field is empty
            enabled = (!isParsing && !isNormalizingNames) && importText.isNotBlank(),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            if (isParsing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                Text("Parsing...")
            } else if (isNormalizingNames) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { normalizationAnimatedProgress },
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    
                                Text(
                        text = "${(normalizationAnimatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 32.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Normalizing Names...")
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Done")
            }
        }

        // Add a help text about internet connection for normalization
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Note: Internet connection and Gemini API key are required for exercise name normalization.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
    
    // Format Help Dialog
    if (showFormatHelpDialog) {
        AlertDialog(
            onDismissRequest = { showFormatHelpDialog = false },
            title = {
                Text("Workout Plan Format Help")
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Your workout plan can be in text or JSON format.\n\n" +
                              "Text Format Example:\n" +
                              "Plan Name: My Workout Plan\n\n" +
                              "Day 1: [Mon] Push Day {chest, triceps}\n" +
                              "- Bench Press | 3 sets of 10 reps | 90s\n" +
                              "- Overhead Press | 3 sets of 8 reps | 90s\n\n" +
                              "Day 2: [Wed] Pull Day {back, biceps}\n" +
                              "- Pull-ups | 3 sets of 8 reps | 90s\n" +
                              "- Rows | 3 sets of 10 reps | 60s\n\n" +
                              "JSON format is also supported. Click 'Insert Example' to see format examples.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showFormatHelpDialog = false }
                ) {
                    Text("Got It")
                }
            }
        )
    }
}

@Composable
fun ErrorMessage(
    errorText: String,
    icon: ImageVector = Icons.Default.Error
) {
    val context = LocalContext.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = errorText,
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        IconButton(
                        onClick = { 
                val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("Error Text", errorText)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(context, "Error copied to clipboard", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy error text",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
                    modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            
                        Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                        Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun WeekdaySelector(
    selectedWeekday: Weekday,
    currentWeekday: Weekday,
    onWeekdaySelected: (Weekday) -> Unit,
    workoutDays: List<WorkoutDay>
) {
    // All weekdays - defined outside of the composition for better performance
    val weekdays = remember { listOf(
        Weekday.MONDAY,
        Weekday.TUESDAY,
        Weekday.WEDNESDAY,
        Weekday.THURSDAY,
        Weekday.FRIDAY,
        Weekday.SATURDAY,
        Weekday.SUNDAY
    ) }
    
    // Map weekdays to workout days - using derivedStateOf for better performance
    val workoutDaysByWeekday by remember(workoutDays) {
        derivedStateOf {
            val mapping = mutableMapOf<Weekday, WorkoutDay>()
            
            weekdays.forEachIndexed { index, weekday ->
                // Find workout day by weekday reference in name
                val weekdayPattern = "\\[${weekday.getShortName()}\\]".toRegex(RegexOption.IGNORE_CASE)
                val matchingDay = workoutDays.find { day ->
                    day.dayName.contains(weekdayPattern) || 
                    day.dayName.contains("\\[${weekday.name}\\]".toRegex(RegexOption.IGNORE_CASE))
                }
                
                // If found, add to mapping
                if (matchingDay != null) {
                    mapping[weekday] = matchingDay
                } else if (index < workoutDays.size) {
                    // Fallback: assign by index
                    mapping[weekday] = workoutDays[index]
                }
            }
            
            mapping
        }
    }
    
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = weekdays,
            key = { it.name }  // Use stable key for each item
        ) { weekday ->
            val isSelected = weekday == selectedWeekday
            val isToday = weekday == currentWeekday
            val hasWorkout = workoutDaysByWeekday[weekday] != null
            
            WeekdayCard(
                weekday = weekday,
                isSelected = isSelected,
                isToday = isToday,
                hasWorkout = hasWorkout,
                onSelected = { onWeekdaySelected(weekday) }
            )
        }
    }
}

// Extracted weekday card to its own composable for better recomposition
@Composable
private fun WeekdayCard(
    weekday: Weekday,
    isSelected: Boolean,
    isToday: Boolean,
    hasWorkout: Boolean,
    onSelected: () -> Unit
) {
    // Date calculation is moved outside of composition for better performance
    val dayOfMonth = remember(weekday) {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        val targetDayOfWeek = when (weekday) {
            Weekday.MONDAY -> Calendar.MONDAY
            Weekday.TUESDAY -> Calendar.TUESDAY
            Weekday.WEDNESDAY -> Calendar.WEDNESDAY
            Weekday.THURSDAY -> Calendar.THURSDAY
            Weekday.FRIDAY -> Calendar.FRIDAY
            Weekday.SATURDAY -> Calendar.SATURDAY
            Weekday.SUNDAY -> Calendar.SUNDAY
        }
        
        // Calculate days to add
        var daysToAdd = targetDayOfWeek - today
        if (daysToAdd < 0) daysToAdd += 7
        
        // Clone calendar to avoid modifying the original
        val targetCalendar = calendar.clone() as Calendar
        targetCalendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
        
        targetCalendar.get(Calendar.DAY_OF_MONTH)
    }
    
    // Animation for selection
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    Card(
        modifier = Modifier
            .width(60.dp)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clickable(onClick = onSelected),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primary
                isToday -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Weekday
            Text(
                text = weekday.getShortName(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Day of month
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            else -> Color.Transparent
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isToday || isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Workout indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = when {
                            hasWorkout -> {
                                if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.primary
                            }
                            else -> Color.Transparent
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun FeaturedWorkoutSection(
    workoutDay: WorkoutDay,
    weekday: Weekday,
    onWorkoutClick: (WorkoutDay) -> Unit
) {
    // Extract day name without metadata
    val cleanName = workoutDay.dayName.let {
        it.replace(Regex("\\[\\w+\\]"), "")
          .replace(Regex("^Day\\s+\\d+:\\s*"), "")
          .replace(Regex("\\{.*\\}"), "")
          .trim()
    } ?: "Workout ${workoutDay.dayNumber}"
    
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onWorkoutClick(workoutDay) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xFF1C1C1E)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            // Muscle group image as background with better styling
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color(0xFF2C2C2E))
            ) {
                if (workoutDay.isRestDay) {
                    // Custom rest day image or icon
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hotel,
                            contentDescription = null,
                            tint = Color(0xFF4E76FF),
                            modifier = Modifier.size(80.dp)
                        )
                    }
                } else {
                    // Show muscle group icon with better styling
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Custom icon based on muscle group
                        val icon = when(workoutDay.primaryMuscleGroup) {
                            MuscleGroup.CHEST -> Icons.Default.AccessibilityNew
                            MuscleGroup.BACK -> Icons.Default.AccessibilityNew
                            MuscleGroup.SHOULDERS -> Icons.Default.AccessibilityNew
                            MuscleGroup.BICEPS -> Icons.Default.FitnessCenter
                            MuscleGroup.TRICEPS -> Icons.Default.FitnessCenter
                            MuscleGroup.QUADS -> Icons.AutoMirrored.Filled.DirectionsRun
                            MuscleGroup.CALVES -> Icons.AutoMirrored.Filled.DirectionsRun
                            MuscleGroup.FOREARMS -> Icons.Default.FitnessCenter
                            else -> Icons.Default.AccessibilityNew
                        }
                        
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color(0xFF4E76FF),
                            modifier = Modifier.size(80.dp)
                        )
                    }
                    
                    // Still try to load the actual image
                    BodyPartImage(
                        primaryMuscleGroup = workoutDay.primaryMuscleGroup,
                        secondaryMuscleGroup = workoutDay.secondaryMuscleGroup,
                        height = 180,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Dark gradient overlay for better text visibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.1f),
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            }
            
            // Content overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                // Weekday and category badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Weekday badge
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF4E76FF),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = weekday.getDisplayName(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    
                    // Rest/activity badge
                    if (workoutDay.isRestDay) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF8E8E93),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "Rest Day",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF38B6FF),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "${workoutDay.exercises.size} Exercises",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                
                // Workout name
                Text(
                    text = cleanName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Muscle groups or rest day text
                if (!workoutDay.isRestDay) {
                    val muscleGroups = listOfNotNull(
                        workoutDay.primaryMuscleGroup?.name,
                        workoutDay.secondaryMuscleGroup?.name
                    ).joinToString(", ")
                    
                    if (muscleGroups.isNotEmpty()) {
                        Text(
                            text = muscleGroups,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                } else {
                    Text(
                        text = "Take a day to recover and recharge",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        // Bottom section with stats and button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stats
            if (!workoutDay.isRestDay) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Duration
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color(0xFF4E76FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${workoutDay.estimatedDuration} min",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    
                    // Exercises count
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = Color(0xFF4E76FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${workoutDay.exercises.size} exercises",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            } else {
                Text(
                    text = "Recovery",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            
            // Button - different for workout vs rest day
            Button(
                onClick = { onWorkoutClick(workoutDay) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4E76FF)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (workoutDay.isRestDay)
                        Icons.Default.Info
                    else
                        Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (workoutDay.isRestDay) "DETAILS" else "START",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DayExercisesSection(
    workoutDay: WorkoutDay,
    exerciseDefinitions: Map<String, ExerciseDefinition>,
    onExerciseClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Section header
        Text(
            text = "Today's Exercises",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        if (workoutDay.isRestDay) {
            // Rest day message
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2C2C2E)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Hotel,
                        contentDescription = null,
                        tint = Color(0xFF4E76FF),
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Rest Day",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Take time to recover. Your muscles grow during rest!",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            // Exercise list - changed from LazyColumn to regular Column for better nested scrolling
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                workoutDay.exercises.forEachIndexed { index, exercise ->
                    val definition = exerciseDefinitions[exercise.exerciseId]
                    
                    DetailedExerciseItem(
                        exercise = exercise,
                        definition = definition,
                        index = index,
                        onClick = { onExerciseClick(exercise.exerciseId) }
                    )
                }
                
                // Add bottom padding
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DetailedExerciseItem(
    exercise: ExerciseInstance,
    definition: ExerciseDefinition?,
    index: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C2E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exercise number
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFF4E76FF).copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (index + 1).toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4E76FF)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Exercise details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = definition?.name ?: exercise.exerciseId,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    text = exercise.setsDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                
                Text(
                    text = "Rest: ${exercise.restTimeSeconds}s",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4E76FF)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Exercise image
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1C1C1E))
            ) {
                if (definition != null) {
                    // Try to load exercise image
                    ExerciseImage(
                        exerciseName = definition.name,
                        imageIdentifier = definition.imageIdentifier,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback icon
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = Color(0xFF4E76FF),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyDayPlaceholder(weekday: Weekday) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2C2C2E)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color(0xFF4E76FF),
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "No Workout for ${weekday.getDisplayName()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Select a different day or add a workout for this day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Extension function to get icon for muscle group
@Composable
fun MuscleGroup?.getIcon(): ImageVector {
    return when (this) {
        MuscleGroup.CHEST -> Icons.Default.AccessibilityNew
        MuscleGroup.BACK -> Icons.Default.AccessibilityNew
        MuscleGroup.SHOULDERS -> Icons.Default.AccessibilityNew
        MuscleGroup.BICEPS -> Icons.Default.FitnessCenter
        MuscleGroup.TRICEPS -> Icons.Default.FitnessCenter
        MuscleGroup.QUADS -> Icons.AutoMirrored.Filled.DirectionsRun
        MuscleGroup.CALVES -> Icons.AutoMirrored.Filled.DirectionsRun
        MuscleGroup.FOREARMS -> Icons.Default.FitnessCenter
        else -> Icons.Default.FitnessCenter
    }
}

// Sample workout plan text for the Example button
private fun getSampleWorkoutPlan(): String {
    return """
    Plan Name: Weekly Strength & Conditioning Plan
    
    Day 1: [Mon] Upper Body Strength {chest, shoulders}
    - Barbell Bench Press | 4 sets of 8 reps | 90s
    - Overhead Press | 3 sets of 10 reps | 90s
    - Dumbbell Rows | 3 sets of 12 reps | 60s
    - Cable Tricep Pushdowns | 3 sets of 15 reps | 60s
    - Dumbbell Curls | 3 sets of 12 reps | 60s
    
    Day 2: [Tue] Lower Body Power {quads, calves}
    - Barbell Squats | 5 sets of 5 reps | 120s
    - Romanian Deadlifts | 4 sets of 8 reps | 90s
    - Leg Press | 3 sets of 10 reps | 90s
    - Seated Calf Raises | 4 sets of 15 reps | 60s
    - Leg Curls | 3 sets of 12 reps | 60s
    
    Day 3: [Wed] Active Recovery
    - Light Cardio | 20-30 minutes | 0s
    - Stretching Routine | Full body | 0s
    - Foam Rolling | Problem areas | 0s
    
    Day 4: [Thu] Upper Body Volume {back, biceps}
    - Pull-ups | 4 sets of 8-10 reps | 90s
    - Incline Dumbbell Press | 3 sets of 12 reps | 60s
    - Lateral Raises | 3 sets of 15 reps | 45s
    - Barbell Curls | 3 sets of 10 reps | 60s
    - Skull Crushers | 3 sets of 12 reps | 60s
    
    Day 5: [Fri] Lower Body Volume {quads, calves}
    - Front Squats | 4 sets of 10 reps | 90s
    - Dumbbell Lunges | 3 sets of 12 reps | 60s
    - Leg Extensions | 3 sets of 15 reps | 60s
    - Standing Calf Raises | 4 sets of 20 reps | 45s
    - Glute Bridges | 3 sets of 15 reps | 60s
    
    Day 6: [Sat] Conditioning
    - Circuit Training | 4 rounds | 60s
    - Kettlebell Swings | 3 sets of 20 reps | 45s
    - Battle Ropes | 3 sets of 30 seconds | 30s
    - Box Jumps | 4 sets of 10 reps | 60s
    
    Day 7: [Sun] Rest Day
    - Complete Rest | Recovery focus | 0s
    """.trimIndent()
}

// Sample JSON workout plan
private fun getSampleJsonWorkoutPlan(): String {
    return """
    {
        "Plan Name": "Mass Machine Beginner Program (5 Days)",
        "Days": [
            {
                "Day": "Monday",
                "Label": "Push Strength",
                "Target": [
                    "chest",
                    "shoulders",
                    "triceps"
                ],
                "Exercises": [
                    {
                        "name": "Barbell Bench Press",
                        "sets": 4,
                        "reps": "6-8",
                        "rest": "90s"
                    },
                    {
                        "name": "Overhead Barbell Press",
                        "sets": 4,
                        "reps": "8",
                        "rest": "90s"
                    },
                    {
                        "name": "Incline Dumbbell Press",
                        "sets": 3,
                        "reps": "10",
                        "rest": "60s"
                    },
                    {
                        "name": "Lateral Raises",
                        "sets": 3,
                        "reps": "15",
                        "rest": "45s"
                    },
                    {
                        "name": "Tricep Pushdowns (Cable)",
                        "sets": 3,
                        "reps": "12-15",
                        "rest": "60s"
                    }
                ]
            },
            {
                "Day": "Tuesday",
                "Label": "Rest Day",
                "Target": [
                    "recovery"
                ]
            },
            {
                "Day": "Wednesday",
                "Label": "Pull Strength",
                "Target": [
                    "back",
                    "biceps"
                ],
                "Exercises": [
                    {
                        "name": "Deadlifts",
                        "sets": 4,
                        "reps": "5",
                        "rest": "120s"
                    },
                    {
                        "name": "Pull-ups",
                        "sets": 3,
                        "reps": "max",
                        "rest": "90s"
                    },
                    {
                        "name": "Barbell Rows",
                        "sets": 3,
                        "reps": "8-10",
                        "rest": "90s"
                    },
                    {
                        "name": "Dumbbell Hammer Curls",
                        "sets": 3,
                        "reps": "12",
                        "rest": "60s"
                    },
                    {
                        "name": "Barbell Curls",
                        "sets": 2,
                        "reps": "15",
                        "rest": "60s"
                    }
                ]
            },
            {
                "Day": "Thursday",
                "Label": "Rest Day",
                "Target": [
                    "recovery"
                ]
            },
            {
                "Day": "Friday",
                "Label": "Legs Strength",
                "Target": [
                    "quads",
                    "hamstrings",
                    "calves"
                ],
                "Exercises": [
                    {
                        "name": "Barbell Back Squats",
                        "sets": 4,
                        "reps": "6-8",
                        "rest": "120s"
                    },
                    {
                        "name": "Romanian Deadlifts (Dumbbell)",
                        "sets": 3,
                        "reps": "10",
                        "rest": "90s"
                    },
                    {
                        "name": "Leg Press",
                        "sets": 3,
                        "reps": "12",
                        "rest": "90s"
                    },
                    {
                        "name": "Standing Calf Raises",
                        "sets": 4,
                        "reps": "20",
                        "rest": "45s"
                    },
                    {
                        "name": "Seated Calf Raises",
                        "sets": 3,
                        "reps": "25",
                        "rest": "45s"
                    }
                ]
            },
            {
                "Day": "Saturday",
                "Label": "Upper Volume",
                "Target": [
                    "chest",
                    "back",
                    "arms"
                ],
                "Exercises": [
                    {
                        "name": "Incline Dumbbell Press",
                        "sets": 3,
                        "reps": "12",
                        "rest": "60s"
                    },
                    {
                        "name": "Pull-ups or Lat Pulldowns",
                        "sets": 3,
                        "reps": "10-12",
                        "rest": "90s"
                    },
                    {
                        "name": "Cable Flys",
                        "sets": 3,
                        "reps": "15",
                        "rest": "45s"
                    },
                    {
                        "name": "EZ Bar Curls",
                        "sets": 3,
                        "reps": "12",
                        "rest": "60s"
                    },
                    {
                        "name": "Skull Crushers",
                        "sets": 3,
                        "reps": "12",
                        "rest": "60s"
                    }
                ]
            },
            {
                "Day": "Sunday",
                "Label": "Conditioning & Core",
                "Target": [
                    "cardio",
                    "core",
                    "recovery"
                ],
                "Exercises": [
                    {
                        "name": "HIIT Cardio (Treadmill or Bike)",
                        "sets": 1,
                        "reps": "15-20 min",
                        "rest": "0s"
                    },
                    {
                        "name": "Ab Circuit (Planks, Crunches, Leg Raises)",
                        "sets": 3,
                        "reps": "rounds",
                        "rest": "0s"
                    },
                    {
                        "name": "Foam Rolling & Stretching",
                        "sets": 1,
                        "reps": "full body",
                        "rest": "0s"
                    }
                ]
            }
        ]
    }
    """.trimIndent()
}

@Composable
fun RestDayDetailsDialog(
    restDay: WorkoutDay,
    weekday: Weekday,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1C1C1E)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 16.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header icon
                Icon(
                    imageVector = Icons.Default.Hotel,
                    contentDescription = null,
                    tint = Color(0xFF4E76FF),
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 16.dp)
                )
                
                // Title
                Text(
                    text = "${weekday.getDisplayName()} Rest Day",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description with better styling
                RestDayInfoSection(
                    title = "Why Rest Is Important",
                    content = "Rest days are crucial for muscle recovery and growth. During intense workouts, your muscles develop micro-tears that need time to heal. This repair process is what makes muscles stronger and larger."
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                RestDayInfoSection(
                    title = "Benefits of Rest Days",
                    content = "• Allows muscle tissue to repair and grow\n• Prevents overtraining and injury\n• Replenishes glycogen stores for energy\n• Reduces mental fatigue\n• Improves overall performance"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                RestDayInfoSection(
                    title = "What To Do On Rest Days",
                    content = "• Light stretching\n• Foam rolling\n• Gentle walking\n• Proper nutrition and hydration\n• Quality sleep (7-9 hours)\n• Mental relaxation"
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4E76FF)
                    ),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text(
                        text = "CLOSE",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RestDayInfoSection(
    title: String,
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF2C2C2E),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4E76FF)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f),
            lineHeight = 24.sp
        )
    }
} 