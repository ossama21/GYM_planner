package com.H_Oussama.gymplanner.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.H_Oussama.gymplanner.ui.common.TransparentTopBar
import com.H_Oussama.gymplanner.ui.theme.GymPlannerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitsScreen(
    viewModel: UnitsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Transparent top bar
        TransparentTopBar(
            title = "Units",
            onBackClick = onBackClick
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp) // Add some space between top bar and content
        ) {
            // Units system section
            UnitSystemSection(
                selectedUnitSystem = uiState.unitSystem,
                onUnitSystemSelected = viewModel::setUnitSystem
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Weight units section
            WeightUnitsSection(
                selectedWeightUnit = uiState.weightUnit,
                onWeightUnitSelected = viewModel::setWeightUnit,
                unitSystem = uiState.unitSystem
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Height units section
            HeightUnitsSection(
                selectedHeightUnit = uiState.heightUnit,
                onHeightUnitSelected = viewModel::setHeightUnit,
                unitSystem = uiState.unitSystem
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Distance units section
            DistanceUnitsSection(
                selectedDistanceUnit = uiState.distanceUnit,
                onDistanceUnitSelected = viewModel::setDistanceUnit,
                unitSystem = uiState.unitSystem
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Reset to defaults button
            UnitsResetButton(onClick = viewModel::resetToDefaults)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun UnitSystemSection(
    selectedUnitSystem: UnitSystem,
    onUnitSystemSelected: (UnitSystem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Section title
        Text(
            text = "Measurement System",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
        )
        
        // Section card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Description text
                Text(
                    text = "Choose the measurement system you prefer to use throughout the app",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Metric option
                UnitSystemOption(
                    title = "Metric",
                    description = "Kilograms (kg), Centimeters (cm), Kilometers (km)",
                    icon = Icons.Default.PublishedWithChanges,
                    isSelected = selectedUnitSystem == UnitSystem.METRIC,
                    onClick = { onUnitSystemSelected(UnitSystem.METRIC) }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Imperial option
                UnitSystemOption(
                    title = "Imperial",
                    description = "Pounds (lbs), Feet/Inches (ft/in), Miles (mi)",
                    icon = Icons.Default.SquareFoot,
                    isSelected = selectedUnitSystem == UnitSystem.IMPERIAL,
                    onClick = { onUnitSystemSelected(UnitSystem.IMPERIAL) }
                )
            }
        }
    }
}

@Composable
fun UnitSystemOption(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Radio button
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Title and description
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun WeightUnitsSection(
    selectedWeightUnit: WeightUnit,
    onWeightUnitSelected: (WeightUnit) -> Unit,
    unitSystem: UnitSystem
) {
    // Show individual unit selection only if not auto from system
    if (unitSystem == UnitSystem.CUSTOM) {
        UnitSelectionSection(
            title = "Weight Units",
            description = "Choose the unit for weight measurements",
            options = listOf(
                UnitOption(
                    title = "Kilograms (kg)",
                    value = WeightUnit.KILOGRAMS,
                    isSelected = selectedWeightUnit == WeightUnit.KILOGRAMS,
                    onClick = { onWeightUnitSelected(WeightUnit.KILOGRAMS) }
                ),
                UnitOption(
                    title = "Pounds (lbs)",
                    value = WeightUnit.POUNDS,
                    isSelected = selectedWeightUnit == WeightUnit.POUNDS,
                    onClick = { onWeightUnitSelected(WeightUnit.POUNDS) }
                )
            )
        )
    } else {
        // Show the currently used units from the system selection
        UnitInfoCard(
            title = "Weight Units",
            value = when (unitSystem) {
                UnitSystem.METRIC -> "Kilograms (kg)"
                UnitSystem.IMPERIAL -> "Pounds (lbs)"
                else -> "Custom"
            },
            icon = Icons.Default.Scale
        )
    }
}

@Composable
fun HeightUnitsSection(
    selectedHeightUnit: HeightUnit,
    onHeightUnitSelected: (HeightUnit) -> Unit,
    unitSystem: UnitSystem
) {
    // Show individual unit selection only if not auto from system
    if (unitSystem == UnitSystem.CUSTOM) {
        UnitSelectionSection(
            title = "Height Units",
            description = "Choose the unit for height measurements",
            options = listOf(
                UnitOption(
                    title = "Centimeters (cm)",
                    value = HeightUnit.CENTIMETERS,
                    isSelected = selectedHeightUnit == HeightUnit.CENTIMETERS,
                    onClick = { onHeightUnitSelected(HeightUnit.CENTIMETERS) }
                ),
                UnitOption(
                    title = "Feet & Inches (ft/in)",
                    value = HeightUnit.FEET_INCHES,
                    isSelected = selectedHeightUnit == HeightUnit.FEET_INCHES,
                    onClick = { onHeightUnitSelected(HeightUnit.FEET_INCHES) }
                )
            )
        )
    } else {
        // Show the currently used units from the system selection
        UnitInfoCard(
            title = "Height Units",
            value = when (unitSystem) {
                UnitSystem.METRIC -> "Centimeters (cm)"
                UnitSystem.IMPERIAL -> "Feet & Inches (ft/in)"
                else -> "Custom"
            },
            icon = Icons.Default.Height
        )
    }
}

@Composable
fun DistanceUnitsSection(
    selectedDistanceUnit: DistanceUnit,
    onDistanceUnitSelected: (DistanceUnit) -> Unit,
    unitSystem: UnitSystem
) {
    // Show individual unit selection only if not auto from system
    if (unitSystem == UnitSystem.CUSTOM) {
        UnitSelectionSection(
            title = "Distance Units",
            description = "Choose the unit for distance measurements",
            options = listOf(
                UnitOption(
                    title = "Kilometers (km)",
                    value = DistanceUnit.KILOMETERS,
                    isSelected = selectedDistanceUnit == DistanceUnit.KILOMETERS,
                    onClick = { onDistanceUnitSelected(DistanceUnit.KILOMETERS) }
                ),
                UnitOption(
                    title = "Miles (mi)",
                    value = DistanceUnit.MILES,
                    isSelected = selectedDistanceUnit == DistanceUnit.MILES,
                    onClick = { onDistanceUnitSelected(DistanceUnit.MILES) }
                )
            )
        )
    } else {
        // Show the currently used units from the system selection
        UnitInfoCard(
            title = "Distance Units",
            value = when (unitSystem) {
                UnitSystem.METRIC -> "Kilometers (km)"
                UnitSystem.IMPERIAL -> "Miles (mi)"
                else -> "Custom"
            },
            icon = Icons.Default.Map
        )
    }
}

@Composable
fun UnitSelectionSection(
    title: String,
    description: String,
    options: List<UnitOption<*>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Section title
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
        )
        
        // Section card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Description text
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Options
                options.forEachIndexed { index, option ->
                    UnitOptionItem(
                        title = option.title,
                        isSelected = option.isSelected,
                        onClick = option.onClick
                    )
                    
                    if (index < options.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun UnitOptionItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = title,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun UnitInfoCard(
    title: String,
    value: String,
    icon: ImageVector
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Section title
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
        )
        
        // Section card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Title and value
                Column {
                    Text(
                        text = "Currently using",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    
                    Text(
                        text = value,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun UnitsResetButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Restore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiaryContainer
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Reset to Default Settings",
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

// Data class for unit option
data class UnitOption<T>(
    val title: String,
    val value: T,
    val isSelected: Boolean,
    val onClick: () -> Unit
)

@Preview(showBackground = true)
@Composable
fun UnitsScreenPreview() {
    GymPlannerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            UnitsScreen(
                viewModel = UnitsViewModel()
            )
        }
    }
} 
 
 