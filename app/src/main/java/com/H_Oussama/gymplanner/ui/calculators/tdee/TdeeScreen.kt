package com.H_Oussama.gymplanner.ui.calculators.tdee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.H_Oussama.gymplanner.ui.calculators.components.*
import com.H_Oussama.gymplanner.ui.theme.GymPlannerTheme
import com.H_Oussama.gymplanner.utils.calculators.ActivityLevel
import com.H_Oussama.gymplanner.utils.calculators.Gender
import com.H_Oussama.gymplanner.utils.calculators.TdeeFormula
import com.H_Oussama.gymplanner.utils.calculators.UnitSystem
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TdeeScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: TdeeViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("TDEE Calculator", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF2C2C2E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                modifier = Modifier.height(56.dp)
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp), // Add padding for bottom nav bar
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero section
            HeroSection(
                title = "TDEE Calculator",
                subtitle = "Calculate your daily calorie needs"
            )
            
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Gender Selection Card
                SelectionCard(
                    title = "Select Gender",
                    content = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Gender.values().forEach { gender ->
                                FilterChip(
                                    selected = uiState.gender == gender,
                                    onClick = { viewModel.onGenderChange(gender) },
                                    label = { Text(if (gender == Gender.MALE) "Male" else "Female") },
                                    leadingIcon = if (uiState.gender == gender) {
                                        {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = "Selected",
                                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                                            )
                                        }
                                    } else {
                                        null
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                                        containerColor = Color(0xFF2C2C2E)
                                    )
                                )
                            }
                        }
                    }
                )
                
                // Units Selection Card
                SelectionCard(
                    title = "Select Units",
                    content = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            UnitSystem.values().forEach { system ->
                                FilterChip(
                                    selected = uiState.selectedUnitSystem == system,
                                    onClick = { viewModel.onUnitSystemChange(system) },
                                    label = { Text(if (system == UnitSystem.METRIC) "Metric (kg, cm)" else "Imperial (lbs, in)") },
                                    leadingIcon = if (uiState.selectedUnitSystem == system) {
                                        {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = "Selected",
                                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                                            )
                                        }
                                    } else {
                                        null
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                                        containerColor = Color(0xFF2C2C2E)
                                    )
                                )
                            }
                        }
                    }
                )
                
                // Input Fields
                InputFieldsCard(
                    content = {
                        StyledTextField(
                            value = uiState.ageInput,
                            onValueChange = viewModel::onAgeChange,
                            label = "Age (years)",
                            keyboardType = KeyboardType.Number,
                            isError = uiState.error != null
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        StyledTextField(
                            value = uiState.weightInput,
                            onValueChange = viewModel::onWeightChange,
                            label = viewModel.getWeightLabel(),
                            keyboardType = KeyboardType.Decimal,
                            isError = uiState.error != null
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        StyledTextField(
                            value = uiState.heightInput,
                            onValueChange = viewModel::onHeightChange,
                            label = viewModel.getHeightLabel(),
                            keyboardType = KeyboardType.Decimal,
                            isError = uiState.error != null,
                            errorText = uiState.error
                        )
                    }
                )
                
                // Activity Level Selection
                SelectionCard(
                    title = "Activity Level",
                    content = {
                        var expanded by remember { mutableStateOf(false) }
                        
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = formatActivityLevel(uiState.activityLevel),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    disabledTextColor = Color.White.copy(alpha = 0.6f),
                                    errorTextColor = MaterialTheme.colorScheme.error,
                                    focusedContainerColor = Color(0xFF2C2C2E),
                                    unfocusedContainerColor = Color(0xFF2C2C2E),
                                    disabledContainerColor = Color(0xFF1C1C1E),
                                    errorContainerColor = Color(0xFF2C2C2E),
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                    errorCursorColor = MaterialTheme.colorScheme.error,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.Gray,
                                    disabledBorderColor = Color.Gray.copy(alpha = 0.6f),
                                    errorBorderColor = MaterialTheme.colorScheme.error,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedLabelColor = Color.Gray,
                                    disabledLabelColor = Color.Gray.copy(alpha = 0.6f),
                                    errorLabelColor = MaterialTheme.colorScheme.error
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color(0xFF2C2C2E))
                            ) {
                                ActivityLevel.values().forEach { level ->
                                    DropdownMenuItem(
                                        text = { Text(formatActivityLevel(level), color = Color.White) },
                                        onClick = {
                                            viewModel.onActivityLevelChange(level)
                                            expanded = false
                                        },
                                        colors = MenuDefaults.itemColors(
                                            textColor = Color.White,
                                            leadingIconColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                )
                
                // Formula Selection Card (Optional, simplifies UI if hidden)
                SelectionCard(
                    title = "TDEE Formula",
                    content = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            TdeeFormula.values().forEach { formula ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = uiState.formula == formula,
                                        onClick = { viewModel.onFormulaChange(formula) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary,
                                            unselectedColor = Color.Gray
                                        )
                                    )
                                    
                                    Text(
                                        text = formatFormulaName(formula),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                )
                
                // Calculate Button
                Button(
                    onClick = { viewModel.calculateTdee() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "Calculate TDEE", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Result section
                if (uiState.tdeeResult != null && uiState.bmrResult != null && uiState.error == null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    ModernTdeeResultCard(
                        tdeeUiState = uiState,
                        viewModel = viewModel
                    )
                }
                
                // Info Card
                Spacer(modifier = Modifier.height(16.dp))
                TdeeInfoCard()
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SelectionGroupTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.fillMaxWidth()
    )
}

// Helper to format ActivityLevel enum for display
private fun formatActivityLevel(level: ActivityLevel): String {
    return level.name.lowercase(Locale.ROOT).replaceFirstChar { it.titlecase(Locale.ROOT) }.replace("_", " ")
}

// Helper to format TdeeFormula enum for display
private fun formatFormulaName(formula: TdeeFormula): String {
    return when (formula) {
        TdeeFormula.MIFFLIN_ST_JEOR -> "Mifflin-St Jeor"
        TdeeFormula.HARRIS_BENEDICT -> "Harris-Benedict"
        TdeeFormula.KATCH_MCARDLE -> "Katch-McArdle"
    }
}

@Composable
private fun TdeeResultCard(uiState: TdeeUiState) {
     ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // BMR Section
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Basal Metabolic Rate (BMR)", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${uiState.bmrResult} kcal / day",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "(Calories burned at rest)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            // TDEE Section
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                 Text("Total Daily Energy Expenditure (TDEE)", style = MaterialTheme.typography.titleMedium)
                 Text(
                    text = "${uiState.tdeeResult} kcal / day",
                    style = MaterialTheme.typography.headlineMedium, // Make TDEE more prominent
                    color = MaterialTheme.colorScheme.primary // Highlight TDEE
                 )
                 Text(
                     text = "(Est. calories burned including activity)",
                     style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant,
                     textAlign = TextAlign.Center
                 )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TdeeScreenPreview() {
    GymPlannerTheme {
        TdeeScreen()
    }
}