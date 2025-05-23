package com.H_Oussama.gymplanner.ui.calculators.bodyfat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.H_Oussama.gymplanner.ui.theme.GymPlannerTheme
import com.H_Oussama.gymplanner.utils.calculators.Gender
import com.H_Oussama.gymplanner.utils.calculators.UnitSystem
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.H_Oussama.gymplanner.ui.calculators.components.HeroSection
import com.H_Oussama.gymplanner.ui.calculators.components.SelectionCard
import com.H_Oussama.gymplanner.ui.calculators.components.InputFieldsCard
import com.H_Oussama.gymplanner.ui.calculators.components.StyledTextField
import com.H_Oussama.gymplanner.ui.calculators.components.ModernBodyFatResultCard
import com.H_Oussama.gymplanner.ui.calculators.components.BodyFatInfoCard
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyFatScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: BodyFatViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    GymPlannerTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Body Fat Calculator", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate Back",
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
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Units Selection
                SelectionGroupTitle("Units")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    UnitSystem.values().forEach { system ->
                        FilterChip(
                            selected = uiState.selectedUnitSystem == system,
                            onClick = { viewModel.onUnitSystemChange(system) },
                            label = { Text(if (system == UnitSystem.METRIC) "Metric (cm)" else "Imperial (in)") },
                            leadingIcon = if (uiState.selectedUnitSystem == system) {
                                { Icon(Icons.Filled.Check, contentDescription = "Selected", modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                            } else { null }
                        )
                    }
                }

                // --- Gender Selection ---
                SelectionGroupTitle("Gender")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Gender.values().forEach { gender ->
                        FilterChip(
                            selected = uiState.gender == gender,
                            onClick = { viewModel.onGenderChange(gender) },
                            label = { Text(gender.name.lowercase().replaceFirstChar { it.titlecase() }) },
                            leadingIcon = if (uiState.gender == gender) {
                                { Icon(Icons.Filled.Check, contentDescription = "Selected", modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                            } else { null }
                        )
                    }
                }

                // --- Input Fields ---
                OutlinedTextField(
                    value = uiState.heightInput,
                    onValueChange = viewModel::onHeightChange,
                    label = { Text(viewModel.getHeightLabel()) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.error != null
                )

                OutlinedTextField(
                    value = uiState.neckInput,
                    onValueChange = viewModel::onNeckChange,
                    label = { Text(viewModel.getNeckLabel()) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.error != null
                )

                OutlinedTextField(
                    value = uiState.waistInput,
                    onValueChange = viewModel::onWaistChange,
                    label = { Text(viewModel.getWaistLabel()) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.error != null
                )

                // Hip Circumference (only shown for females)
                AnimatedVisibility(visible = uiState.gender == Gender.FEMALE) {
                    OutlinedTextField(
                        value = uiState.hipInput,
                        onValueChange = viewModel::onHipChange,
                        label = { Text(viewModel.getHipLabel()) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.gender == Gender.FEMALE,
                        isError = uiState.error != null
                    )
                }
                
                // Show error message if there is one
                if (uiState.error != null) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.calculateBodyFat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Calculate Body Fat %")
                }

                // --- Display Result ---
                if (uiState.bodyFatResult != null && uiState.error == null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ModernBodyFatResultCard(
                        bodyFatUiState = uiState,
                        viewModel = viewModel
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    BodyFatInfoCard()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BodyFatScreenPreview() {
    GymPlannerTheme {
        BodyFatScreen()
    }
}

// Reusable composable for Title + FilterChips
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SelectionGroup(
    title: String,
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    itemLabel: @Composable (T) -> String
) {
    SelectionGroupTitle(title)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEach { item ->
            FilterChip(
                selected = selectedItem == item,
                onClick = { onItemSelected(item) },
                label = { Text(itemLabel(item)) },
                leadingIcon = if (selectedItem == item) {
                    { Icon(Icons.Filled.Check, contentDescription = "Selected", modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                } else { null }
            )
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

@Composable
private fun BodyFatResultCard(
    uiState: BodyFatUiState,
    viewModel: BodyFatViewModel
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Estimated Body Fat Percentage", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            val bodyFatPercentage = viewModel.getFormattedBodyFat() ?: "--"
            Text(
                text = "$bodyFatPercentage %",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary // Highlight result
            )
             Text(
                text = "(Based on US Navy formula - estimate only)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
} 