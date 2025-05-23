package com.H_Oussama.gymplanner.ui.calculators

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.H_Oussama.gymplanner.ui.navigation.Routes
import com.H_Oussama.gymplanner.ui.theme.*

// Data class to hold calculator info with added icon and description
data class CalculatorInfo(
    val route: String, 
    val title: String, 
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun CalculatorsHubScreen(
    onNavigateToCalculator: (String) -> Unit
) {
    val calculators = listOf(
        CalculatorInfo(
            route = Routes.BMI_CALCULATOR,
            title = "BMI Calculator",
            description = "Calculate your Body Mass Index based on height and weight",
            icon = Icons.Default.MonitorWeight,
            color = Color(0xFF4FC3F7) // Light blue
        ),
        CalculatorInfo(
            route = Routes.TDEE_CALCULATOR,
            title = "TDEE Calculator",
            description = "Find your daily calorie needs based on activity level",
            icon = Icons.Default.LocalFireDepartment,
            color = Color(0xFFFF8A65) // Orange
        ),
        CalculatorInfo(
            route = Routes.BODY_FAT_CALCULATOR,
            title = "Body Fat Calculator",
            description = "Estimate your body fat percentage using measurements",
            icon = Icons.Default.Straighten,
            color = Color(0xFF81C784) // Green
        )
    )

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
            // Hero section
            item {
                HeroSection()
            }
            
            // Section title
            item {
                SectionTitle(
                    title = "Fitness Calculators",
                    subtitle = "Tools to track and optimize your fitness journey"
                )
            }
            
            // Calculator cards
            items(calculators) { calculator ->
                CalculatorCard(
                    title = calculator.title,
                    description = calculator.description,
                    icon = calculator.icon,
                    iconColor = calculator.color,
                    onClick = { onNavigateToCalculator(calculator.route) }
                )
            }
            
            // Informational section
            item {
                InformationCard()
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
    val gradientStartColor = if (isSystemInDarkTheme()) Color(0xFF2C2C2E) else Color(0xFF4FC3F7).copy(alpha = 0.7f)
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
                text = "Fitness Calculators",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Track your metrics and optimize your fitness goals",
                style = MaterialTheme.typography.bodyLarge,
                color = secondaryTextColor
            )
        }
        
        // Decorative elements
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
private fun SectionTitle(title: String, subtitle: String) {
    val textColor = getTextColor()
    val secondaryTextColor = getSecondaryTextColor()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = secondaryTextColor
        )
    }
}

@Composable
private fun CalculatorCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    val cardColor = getCardColor()
    val textColor = getTextColor()
    val secondaryTextColor = getSecondaryTextColor()
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Card icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Card content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryTextColor
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Navigate to calculator",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun InformationCard() {
    val cardColor = getDarkerCardColor()
    val textColor = getTextColor()
    val secondaryTextColor = getSecondaryTextColor()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Why Use Our Calculators?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Our calculators provide accurate estimates to help you set realistic goals and track progress. Remember that results are estimates and may vary based on individual factors.",
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryTextColor
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Regular measurements help track your progress over time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor
                )
            }
        }
    }
}