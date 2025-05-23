package com.H_Oussama.gymplanner.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.H_Oussama.gymplanner.data.repositories.UserPreferencesRepository
import com.H_Oussama.gymplanner.ui.common.TransparentTopBar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the workout goals screen.
 */
@HiltViewModel
class WorkoutGoalsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // State for the selected goal
    private val _selectedGoal = MutableStateFlow(userPreferencesRepository.getGoal())
    val selectedGoal: StateFlow<String> = _selectedGoal.asStateFlow()

    /**
     * Updates the selected workout goal.
     */
    fun setWorkoutGoal(goal: String) {
        _selectedGoal.value = goal
        viewModelScope.launch {
            userPreferencesRepository.saveGoal(goal)
        }
    }
}

// Define workout goal options
data class WorkoutGoalOption(
    val title: String,
    val description: String,
    val icon: @Composable () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutGoalsScreen(
    viewModel: WorkoutGoalsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val selectedGoal by viewModel.selectedGoal.collectAsState()
    
    val workoutGoals = listOf(
        WorkoutGoalOption(
            title = "Build Muscle",
            description = "Increase muscle size and strength with resistance training",
            icon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) }
        ),
        WorkoutGoalOption(
            title = "Lose Weight",
            description = "Reduce body fat while maintaining muscle mass",
            icon = { Icon(Icons.Default.TrendingDown, contentDescription = null) }
        ),
        WorkoutGoalOption(
            title = "Improve Strength",
            description = "Focus on increasing your lifting capacity and power",
            icon = { Icon(Icons.Default.Bolt, contentDescription = null) }
        ),
        WorkoutGoalOption(
            title = "Maintain Fitness",
            description = "Preserve current fitness level and body composition",
            icon = { Icon(Icons.Default.Balance, contentDescription = null) }
        ),
        WorkoutGoalOption(
            title = "Improve Endurance",
            description = "Enhance stamina and cardiovascular fitness",
            icon = { Icon(Icons.Default.DirectionsRun, contentDescription = null) }
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Transparent top bar
        TransparentTopBar(
            title = "Workout Goals",
            onBackClick = onBackClick
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp) // Add some space between top bar and content
        ) {
            // Introduction
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Choose Your Fitness Journey",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Setting a clear goal helps tailor your workouts and track your progress more effectively.",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Goals List
            workoutGoals.forEach { goal ->
                GoalSelectionCard(
                    goal = goal,
                    isSelected = selectedGoal == goal.title,
                    onSelect = { viewModel.setWorkoutGoal(goal.title) }
                )
            }
            
            // Advice Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Tips for Success",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Be realistic with your goals and timeline\n" +
                              "• Track your progress regularly\n" +
                              "• Adjust your nutrition to support your goal\n" +
                              "• Get adequate rest and recovery\n" +
                              "• Stay consistent with your workouts",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun GoalSelectionCard(
    goal: WorkoutGoalOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Goal Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(percent = 50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides if (isSelected) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.primary
                ) {
                    goal.icon()
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Goal Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = goal.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Selected Check
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
} 
 
 