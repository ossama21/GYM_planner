package com.H_Oussama.gymplanner.ui.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.H_Oussama.gymplanner.data.model.FoodItem
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun FoodEntryScreen(
    viewModel: NutritionTrackerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableStateOf("Breakfast") }
    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    var showServingSizeDialog by remember { mutableStateOf(false) }
    var selectedFood by remember { mutableStateOf<FoodItem?>(null) }
    var servingSize by remember { mutableFloatStateOf(1f) }
    var showAIInputDialog by remember { mutableStateOf(false) }
    var aiInputText by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    
    val uiState by viewModel.uiState.collectAsState()
    var searchResults by remember { mutableStateOf(emptyList<FoodItem>()) }
    
    // Get all available meals to find the correct meal ID
    val meals = uiState.meals.map { it.meal }
    
    // Debug meal data
    LaunchedEffect(meals) {
        println("DEBUG: Available meals: ${meals.map { "${it.name}:${it.id}" }}")
    }
    
    // Function to get the meal ID from name
    fun getMealIdFromName(name: String): String {
        val meal = meals.find { it.name.equals(name, ignoreCase = true) }
        val id = meal?.id ?: ""
        println("DEBUG: Looking for meal '$name', found ID: $id")
        return id
    }
    
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(searchQuery, isSearching) {
        if (searchQuery.isNotEmpty() && !isSearching) {
            viewModel.searchFoodItems(searchQuery)
                .collect { results ->
                    searchResults = results
                }
        } else {
            searchResults = emptyList()
        }
    }
    
    // Make sure we have meals loaded
    LaunchedEffect(Unit) {
        println("DEBUG: Initial meal loading")
        viewModel.loadMeals()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
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
                    text = "Add Food",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    isSearching = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .focusRequester(focusRequester),
                placeholder = { Text("Search your food") },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        isSearching = true
                        keyboard?.hide()
                    }
                )
            )
            
            // Meal type selection
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Meal Type:",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    mealTypes.forEach { mealType ->
                        val isSelected = mealType == selectedMealType
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { selectedMealType = mealType }
                                .padding(4.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedMealType = mealType }
                            )
                            Text(
                                text = mealType,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            
            // AI food input button
            Button(
                onClick = { showAIInputDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudCircle,
                    contentDescription = "AI",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Describe your food to AI")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Search results or recent foods
            Box(modifier = Modifier.weight(1f)) {
                when {
                    searchQuery.isNotEmpty() && !isSearching -> {
                        if (searchResults.isNotEmpty()) {
                            FoodItemsList(
                                title = "Search Results",
                                foodItems = searchResults,
                                onItemClick = { 
                                    selectedFood = it
                                    showServingSizeDialog = true
                                }
                            )
                        } else {
                            EmptyFoodList(message = "No matching foods found")
                        }
                    }
                    else -> {
                        FoodItemsList(
                            title = "Recent Foods",
                            foodItems = uiState.recentFoods,
                            onItemClick = { 
                                selectedFood = it
                                showServingSizeDialog = true
                            }
                        )
                    }
                }
            }
        }
        
        // Serving size dialog
        if (showServingSizeDialog && selectedFood != null) {
            ServingSizeDialog(
                foodItem = selectedFood!!,
                initialServingSize = servingSize,
                onDismiss = { 
                    showServingSizeDialog = false
                    selectedFood = null
                },
                onConfirm = { size ->
                    // Check if meals list is empty and try to reload if needed
                    if (meals.isEmpty()) {
                        println("DEBUG: No meals available when trying to add food! Forcing initialization.")
                        viewModel.initializeDefaultMeals()
                        // Show error message
                        viewModel.setErrorMessage("Please try again - had to initialize meal data")
                        return@ServingSizeDialog
                    }
                    
                    val mealId = getMealIdFromName(selectedMealType)
                    println("DEBUG: Selected meal: $selectedMealType, mapped to ID: $mealId")
                    
                    if (mealId.isNotEmpty()) {
                        println("DEBUG: Proceeding to add food entry for ${selectedFood?.name}")
                        viewModel.addFoodEntry(selectedFood!!, mealId, size.toFloat())
                        showServingSizeDialog = false
                        selectedFood = null
                        onNavigateBack()
                    } else {
                        println("ERROR: Could not find meal ID for $selectedMealType")
                        println("DEBUG: Available meals: ${meals.map { "${it.name}:${it.id}" }}")
                        // Try to create default meals if none exist
                        viewModel.initializeDefaultMeals()
                        // Show error message
                        viewModel.setErrorMessage("Could not find meal type. Please try again")
                    }
                }
            )
        }
        
        // AI food description input dialog
        if (showAIInputDialog) {
            AIFoodInputDialog(
                onDismiss = { showAIInputDialog = false },
                onProcess = { foodDescription ->
                    // Get the meal ID for the selected meal type
                    val mealId = getMealIdFromName(selectedMealType)
                    if (mealId.isBlank()) {
                        viewModel.setErrorMessage("Could not find the selected meal. Please try again.")
                    } else {
                        // Call the Gemini API with the food description and meal ID
                        viewModel.getFoodInfoFromGemini(foodDescription, mealId)
                        showAIInputDialog = false
                        onNavigateBack()  // Return to the nutrition tracker after processing
                    }
                }
            )
        }
    }
    
    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }
}

@Composable
fun FoodItemsList(
    title: String,
    foodItems: List<FoodItem>,
    onItemClick: (FoodItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
        ) {
            items(foodItems) { foodItem ->
                FoodItemRow(
                    foodItem = foodItem,
                    onClick = { onItemClick(foodItem) }
                )
                
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun EmptyFoodList(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = message,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FoodItemRow(
    foodItem: FoodItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Food info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = foodItem.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "${foodItem.servingSize} ${foodItem.servingUnit}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        
        // Calories
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = Color(0xFFFF6B6B),
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = "${foodItem.calories} Kcal",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServingSizeDialog(
    foodItem: FoodItem,
    initialServingSize: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var servingSize by remember { mutableFloatStateOf(initialServingSize) }
    
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
                    text = "Serving Size",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = foodItem.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "Standard serving: ${foodItem.servingSize}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Nutrition info for current serving size
                val calories = (foodItem.calories * servingSize).toInt()
                val protein = (foodItem.protein * servingSize).toInt()
                val carbs = (foodItem.carbs * servingSize).toInt()
                val fat = (foodItem.fat * servingSize).toInt()
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = "$calories Kcal",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Protein: ${protein}g",
                            fontSize = 14.sp
                        )
                        
                        Text(
                            text = "Carbs: ${carbs}g",
                            fontSize = 14.sp
                        )
                        
                        Text(
                            text = "Fat: ${fat}g",
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Servings: ${String.format("%.1f", servingSize)}",
                    fontSize = 16.sp
                )
                
                Slider(
                    value = servingSize,
                    onValueChange = { servingSize = it },
                    valueRange = 0.5f..5f,
                    steps = 9, // (5 - 0.5) / 0.5 = 9 steps
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Button(onClick = { onConfirm(servingSize) }) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun AIFoodInputDialog(
    onDismiss: () -> Unit,
    onProcess: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
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
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Describe Your Food",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Enter a description of what you ate, and\nthe AI will calculate its nutritional value.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                OutlinedTextField(
                    value = text,
                    onValueChange = { 
                        text = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(vertical = 8.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    placeholder = { Text("e.g., Grilled chicken breast with rice") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                
                Text(
                    text = "Examples:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                
                // Example food items that users can click
                ExampleFoodItem("Medium Greek yogurt with honey and almonds") { text = it }
                ExampleFoodItem("Two slices of pepperoni pizza with thin crust") { text = it }
                ExampleFoodItem("Grilled salmon with asparagus and rice") { text = it }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onDismiss() },
                        enabled = !isProcessing
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (text.isNotBlank()) {
                                isProcessing = true
                                onProcess(text.trim())
                            }
                        },
                        enabled = text.isNotBlank() && !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing...")
                        } else {
                            Text("Process")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExampleFoodItem(text: String, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick(text) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(12.dp)
        )
    }
}

// Add dummy implementation of missing methods
private fun searchFoodItems(query: String) {
    // This method will be implemented later
    // For now, just provide a stub implementation
}

private fun loadMeals() {
    // This method will be implemented later
    // For now, just provide a stub implementation
}

private fun initializeDefaultMeals() {
    // This method will be implemented later
    // For now, just provide a stub implementation
}

private fun setErrorMessage(message: String) {
    // This method will be implemented later
    // For now, just provide a stub implementation
}

private fun getFoodInfoFromGemini(foodDescription: String) {
    // This method will be implemented later
    // For now, just provide a stub implementation
} 