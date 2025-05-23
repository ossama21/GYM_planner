package com.H_Oussama.gymplanner.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.H_Oussama.gymplanner.data.model.MuscleGroup
import com.H_Oussama.gymplanner.ui.theme.Spacing
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow

/**
 * A reusable card component that displays a muscle group with exercises and stats
 *
 * @param title The title of the card, typically the muscle group name
 * @param primaryMuscleGroup The primary muscle group to display an image for
 * @param secondaryMuscleGroup Optional secondary muscle group
 * @param exerciseCount Number of exercises for this muscle group
 * @param totalSets Total number of sets for all exercises
 * @param onClick Called when the card is clicked
 * @param modifier Optional modifier for the card
 * @param actions Optional actions to display at the bottom of the card
 * @param additionalContent Optional additional content to display below the main content
 */
@Composable
fun MuscleGroupCard(
    title: String,
    primaryMuscleGroup: MuscleGroup?,
    secondaryMuscleGroup: MuscleGroup? = null,
    exerciseCount: Int = 0,
    totalSets: Int = 0,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    actions: @Composable (RowScope.() -> Unit)? = null,
    additionalContent: @Composable (() -> Unit)? = null
) {
    ClickableCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium)
        ) {
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Main content with muscle group image and stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Muscle group image
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(120.dp)
                ) {
                    BodyPartImage(
                        primaryMuscleGroup = primaryMuscleGroup,
                        secondaryMuscleGroup = secondaryMuscleGroup,
                        height = 120
                    )
                }
                
                // Stats
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatItem(
                        icon = Icons.Default.FitnessCenter,
                        label = "Exercises",
                        value = exerciseCount.toString()
                    )
                    
                    StatItem(
                        icon = Icons.Default.FitnessCenter,
                        label = "Total Sets",
                        value = totalSets.toString()
                    )
                }
            }
            
            // Additional content if provided
            additionalContent?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                it()
            }
            
            // Actions if provided
            actions?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    content = it
                )
            }
        }
    }
}

/**
 * A simple stat item showing an icon, label and value
 */
@Composable
fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Preview of the MuscleGroupCard component
 */
@Composable
fun MuscleGroupCardPreview() {
    MuscleGroupCard(
        title = "Monday - Chest Day",
        primaryMuscleGroup = MuscleGroup.CHEST,
        secondaryMuscleGroup = MuscleGroup.BICEPS,
        exerciseCount = 5,
        totalSets = 15,
        actions = {
            TextButton(onClick = {}) {
                Text("START")
            }
        },
        additionalContent = {
            Text(
                text = "Bench Press, Incline Press, Dumbbell Fly, Push-ups, Cable Crossover",
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )
        }
    )
} 