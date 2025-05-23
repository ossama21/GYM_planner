package com.H_Oussama.gymplanner.ui.calculators.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.H_Oussama.gymplanner.ui.calculators.bodyfat.BodyFatUiState
import com.H_Oussama.gymplanner.ui.calculators.bodyfat.BodyFatViewModel
import com.H_Oussama.gymplanner.utils.calculators.BodyFatCategory
import com.H_Oussama.gymplanner.utils.calculators.Gender

@Composable
fun ModernBodyFatResultCard(
    bodyFatUiState: BodyFatUiState,
    viewModel: BodyFatViewModel
) {
    val category = bodyFatUiState.bodyFatCategory
    val formattedBodyFat = bodyFatUiState.bodyFatResult?.let { "%.1f%%".format(it) }

    if (formattedBodyFat != null && category != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E1E)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your Body Fat Result",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = formattedBodyFat,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = getBodyFatCategoryColor(category)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = category.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = getBodyFatCategoryColor(category),
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = getBodyFatCategoryDescription(category, bodyFatUiState.gender == Gender.MALE),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun BodyFatInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Body Fat Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Text(
                text = "Body fat percentage is the amount of fat mass in your body compared to your total body weight. It is an important indicator of health and fitness levels.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Body Fat Categories for Men:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "• Essential Fat: 2-5%\n" +
                        "• Athletes: 6-13%\n" +
                        "• Fitness: 14-17%\n" +
                        "• Average: 18-24%\n" +
                        "• Obese: 25%+",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Body Fat Categories for Women:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "• Essential Fat: 10-13%\n" +
                        "• Athletes: 14-20%\n" +
                        "• Fitness: 21-24%\n" +
                        "• Average: 25-31%\n" +
                        "• Obese: 32%+",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

// Helper functions
private fun getBodyFatCategoryColor(category: BodyFatCategory): Color {
    return when (category) {
        BodyFatCategory.ESSENTIAL_FAT -> Color(0xFFE91E63) // Pink
        BodyFatCategory.ATHLETES -> Color(0xFF4CAF50) // Green
        BodyFatCategory.FITNESS -> Color(0xFF8BC34A) // Light Green
        BodyFatCategory.AVERAGE -> Color(0xFFFFC107) // Amber
        BodyFatCategory.OBESE -> Color(0xFFF44336) // Red
    }
}

private fun getBodyFatCategoryDescription(category: BodyFatCategory, isMale: Boolean): String {
    return when (category) {
        BodyFatCategory.ESSENTIAL_FAT -> if (isMale) 
            "This is the minimum level of fat a male body needs for basic physiological functions. Not recommended to maintain for long periods." 
        else 
            "This is the minimum level of fat a female body needs for basic physiological and reproductive functions."
        BodyFatCategory.ATHLETES -> "Athletic body fat levels are typical for competitive athletes and those who consistently engage in intensive training."
        BodyFatCategory.FITNESS -> "Fitness level body fat is visibly lean and indicates good exercise habits and healthy nutrition."
        BodyFatCategory.AVERAGE -> "This is the average range for most people. It's considered healthy but offers room for improvement through fitness and nutrition."
        BodyFatCategory.OBESE -> "This level of body fat is associated with increased health risks. Consider consulting with healthcare professionals about healthy weight management strategies."
    }
} 
 
 