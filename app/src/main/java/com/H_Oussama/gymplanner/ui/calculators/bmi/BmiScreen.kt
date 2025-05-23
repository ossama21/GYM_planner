package com.H_Oussama.gymplanner.ui.calculators.bmi

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.H_Oussama.gymplanner.R
import com.H_Oussama.gymplanner.ui.calculators.components.*
import com.H_Oussama.gymplanner.ui.theme.GymPlannerTheme
import com.H_Oussama.gymplanner.utils.calculators.UnitSystem
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmiScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: BmiViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("BMI Calculator", color = Color.White) },
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
            // Hero section - removing this as we now have a proper title in the app bar
            // and this was causing the excessive spacing
            
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                
                // Input Fields Card
                InputFieldsCard(
                    content = {
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
                
                // Calculate Button
                Button(
                    onClick = { viewModel.calculateBmi() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "Calculate BMI", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Result section
                if (uiState.bmiResult != null && uiState.error == null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    ModernBmiResultCard(
                        bmiUiState = uiState,
                        viewModel = viewModel
                    )
                }
                
                // Info Card
                Spacer(modifier = Modifier.height(16.dp))
                BmiInfoCard()
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HeroSection(
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2C2C2E),
                        Color(0xFF121212)
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        
        // Decorative element
        Box(
            modifier = Modifier
                .size(100.dp)
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
private fun BmiResultCard(
    bmiUiState: BmiUiState,
    viewModel: BmiViewModel
) {
    val category = bmiUiState.bmiCategory
    val formattedBmi = viewModel.getFormattedBmi()

    if (formattedBmi != null && category != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.bmi_result_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formattedBmi,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Category: $category",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BmiScreenPreview() {
    GymPlannerTheme {
        BmiScreen()
    }
}