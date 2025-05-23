package com.H_Oussama.gymplanner.ui.nutrition

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.min
import java.util.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun NutritionTrackerScreen(
    onNavigateToFoodEntry: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHowTo: () -> Unit = {},
    viewModel: NutritionTrackerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header with date navigation
            NutritionHeader(
                dateFormatted = viewModel.getFormattedDate(),
                onPreviousDay = { 
                    // Get yesterday's date
                    val calendar = Calendar.getInstance()
                    calendar.time = uiState.date
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                    viewModel.setDate(calendar.time)
                },
                onNextDay = {
                    // Get tomorrow's date
                    val calendar = Calendar.getInstance()
                    calendar.time = uiState.date
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    viewModel.setDate(calendar.time)
                },
                onBackClick = onNavigateBack,
                onHelpClick = onNavigateToHowTo
            )
            
            // Main content area - scrollable with weight of 1f
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // Today's Goal card (using our new component)
                TodaysGoalCard(
                    currentCalories = uiState.summary.calories,
                    goalCalories = uiState.goals.calories,
                    currentBurnedCalories = uiState.caloriesBurned,
                    currentProtein = uiState.summary.protein,
                    goalProtein = uiState.goals.protein,
                    currentCarbs = uiState.summary.carbs,
                    goalCarbs = uiState.goals.carbs,
                    onEditGoal = { 
                        // Open the Edit Goal dialog 
                        viewModel.openEditGoalDialog()
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Water tracking card
                WaterTrackingCard(
                    currentWater = uiState.waterIntake.toInt(),
                    goalWater = uiState.goals.water.toInt(),
                    onAddWater = { viewModel.incrementWaterIntake() }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Meal sections
                uiState.meals.forEach { meal ->
                    MealSection(
                        mealType = meal.meal.name,
                        entries = meal.entries,
                        onAddFood = { 
                            onNavigateToFoodEntry()
                        },
                        onDeleteEntry = { entry ->
                            viewModel.deleteEntry(entry.entry)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Quote at bottom
                Text(
                    text = "Healthy habits, healthy you.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Debug meal info button
                DebugMealInfoButton(viewModel)
                
                Spacer(modifier = Modifier.height(80.dp)) // Add extra space at the bottom
            }
        }
        
        // FAB for adding food
        FloatingActionButton(
            onClick = { onNavigateToFoodEntry() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Food"
            )
        }
        
        // Loading indicator if needed
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        
        // Error message if any
        uiState.errorMessage?.let { errorMessage ->
            Toast(message = errorMessage)
        }
    }

    // Edit Goal Dialog
    if (uiState.showEditGoalDialog) {
        EditGoalDialog(
            currentCalorieGoal = uiState.goals.calories,
            onDismiss = { viewModel.closeEditGoalDialog() },
            onSave = { calories -> 
                viewModel.updateNutritionGoals(
                    calories = calories,
                    carbs = uiState.goals.carbs,
                    protein = uiState.goals.protein, 
                    fat = uiState.goals.fat
                )
                viewModel.closeEditGoalDialog()
            }
        )
    }
}

@Composable
fun NutritionHeader(
    dateFormatted: String,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onBackClick: () -> Unit,
    onHelpClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        
        // Date navigator
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousDay) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous Day",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Text(
                text = dateFormatted, 
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            IconButton(onClick = onNextDay) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next Day",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Help button
        IconButton(onClick = onHelpClick) {
            Icon(
                imageVector = Icons.Default.HelpOutline,
                contentDescription = "Help",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        // Empty space to balance layout
        Box(modifier = Modifier.width(48.dp))
    }
}

@Composable
fun WaterTrackingCard(
    currentWater: Int,
    goalWater: Int,
    onAddWater: () -> Unit
) {
    val progress = if (goalWater > 0) min(currentWater.toFloat() / goalWater.toFloat(), 1f) else 0f
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE3F2FD).copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Water icon in circle
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2196F3).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💧",
                            fontSize = 20.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Water title
                    Text(
                        text = "Water Intake",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Water amount
                    Text(
                        text = "$currentWater/$goalWater",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF2196F3),
                                        Color(0xFF03A9F4)
                                    )
                                )
                            )
                    )
                    
                    // Glasses indicators
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(goalWater) { index ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index < currentWater) Color.White.copy(alpha = 0.7f)
                                        else Color.Transparent
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (index < currentWater) {
                                    Text(
                                        text = "💧",
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Next reminder
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⏰ ",
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Next in 1h 9m",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Add water button
                    Button(
                        onClick = onAddWater,
                        modifier = Modifier
                            .height(40.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Text(
                            text = "Add 💧",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealSection(
    mealType: String,
    entries: List<FoodWithEntry>,
    onAddFood: () -> Unit,
    onDeleteEntry: (FoodWithEntry) -> Unit
) {
    // Get appropriate emoji based on meal type
    val mealEmoji = when (mealType) {
        "Breakfast" -> "🍳"
        "Lunch" -> "🍲"
        "Dinner" -> "🍽️"
        "Snacks" -> "🍪"
        else -> "🥗"
    }
    
    // Get appropriate color based on meal type
    val mealColor = when (mealType) {
        "Breakfast" -> Color(0xFFFFC107) // Amber
        "Lunch" -> Color(0xFFFF9800) // Orange
        "Dinner" -> Color(0xFFE91E63) // Pink
        "Snacks" -> Color(0xFF8BC34A) // Light Green
        else -> Color(0xFF9C27B0) // Purple
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            mealColor.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Meal header with add button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Meal type icon and name
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Meal emoji in circle
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(mealColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mealEmoji,
                                fontSize = 20.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = mealType,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Add button
                    Button(
                        onClick = onAddFood,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = mealColor
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Food",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text(
                                text = "Add",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Food entries list
                if (entries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No entries yet",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "Tap + to add food",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    entries.forEach { entry ->
                        FoodEntryItem(
                            foodName = entry.food.name,
                            calories = entry.entry.calories,
                            servingSize = "${entry.entry.servingSize} ${entry.food.servingUnit}",
                            onDelete = { onDeleteEntry(entry) },
                            mealColor = mealColor
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Total calories for this meal
                    val totalCalories = entries.sumOf { it.entry.calories }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Total calories section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(mealColor.copy(alpha = 0.1f))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Calories",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Text(
                                text = "$totalCalories Kcal",
                                fontWeight = FontWeight.Bold,
                                color = mealColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoodEntryItem(
    foodName: String,
    calories: Int,
    servingSize: String,
    onDelete: () -> Unit,
    mealColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Food emoji placeholder based on first letter
        val foodEmoji = when (foodName.firstOrNull()?.lowercaseChar()) {
            'a' -> "🍎" // Apple
            'b' -> "🍌" // Banana
            'c' -> "🍗" // Chicken
            'd' -> "🥮" // Dessert
            'e' -> "🥚" // Egg
            'f' -> "🍟" // Fries
            'g' -> "🍇" // Grapes
            'h' -> "🍯" // Honey
            'i' -> "🍦" // Ice cream
            'j' -> "🥓" // Jam (using bacon emoji)
            'k' -> "🥝" // Kiwi
            'l' -> "🍋" // Lemon
            'm' -> "🥩" // Meat
            'n' -> "🥜" // Nuts
            'o' -> "🍊" // Orange
            'p' -> "🍕" // Pizza
            'q' -> "🥮" // Quiche (using moon cake emoji)
            'r' -> "🍚" // Rice
            's' -> "🍣" // Sushi
            't' -> "🌮" // Taco
            'u' -> "🍇" // Uvas (grapes in Spanish)
            'v' -> "🥗" // Vegetables
            'w' -> "🥐" // Waffle
            'x' -> "🌽" // Xocolatl (corn, used in chocolate)
            'y' -> "🍠" // Yam
            'z' -> "🥒" // Zucchini
            else -> "🍽️" // Default food icon
        }
        
        // Food emoji in circle
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(mealColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = foodEmoji,
                fontSize = 16.sp
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Food details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = foodName,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = servingSize,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Calories
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$calories Kcal",
                fontWeight = FontWeight.SemiBold,
                color = mealColor
            )
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Delete Food Entry",
                    tint = Color(0xFFFF5252)
                )
            }
        }
    }
}

@Composable
fun Toast(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(400)),
            exit = fadeOut(animationSpec = tween(400))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(bottom = 80.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = message,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

// Debug button
@Composable
fun DebugMealInfoButton(viewModel: NutritionTrackerViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    
    Button(
        onClick = { showDialog = true },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text("Debug: Show Meal Info")
    }
    
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Debug Meal Info",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    viewModel.testGeminiApi()
                    Text("Testing Gemini API. Check logcat for results.")
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun EditGoalDialog(
    currentCalorieGoal: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var calorieGoal by remember { mutableStateOf(currentCalorieGoal.toString()) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Edit Daily Calorie Goal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Calorie goal input
                OutlinedTextField(
                    value = calorieGoal,
                    onValueChange = { newValue -> 
                        // Only allow numeric input
                        if (newValue.isEmpty() || newValue.all { char -> char.isDigit() }) {
                            calorieGoal = newValue
                        }
                    },
                    label = { Text("Daily Calorie Goal") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            val calories = calorieGoal.toIntOrNull() ?: currentCalorieGoal
                            onSave(calories)
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
} 