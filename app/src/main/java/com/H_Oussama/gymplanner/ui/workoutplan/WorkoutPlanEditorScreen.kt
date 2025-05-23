package com.H_Oussama.gymplanner.ui.workoutplan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPlanEditorScreen(
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    viewModel: WorkoutPlanViewModel = hiltViewModel()
) {
    var planText by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(true) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Workout Plan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.importWorkoutPlan(planText)
                        onSave()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF121212))
                .padding(16.dp)
        ) {
            Text(
                text = "Create Your Workout Plan",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "Enter your workout plan below. You can use the provided format or JSON format.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = planText,
                onValueChange = { planText = it },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                label = { Text("Workout Plan") },
                placeholder = { Text("Plan Name: My Workout Plan\n\nDay 1: Push Day {chest, triceps}\n- Bench Press | 3 sets of 10 | 90s\n...") },
                colors = TextFieldDefaults.colors(
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                    focusedPlaceholderColor = Color.White.copy(alpha = 0.5f)
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    viewModel.importWorkoutPlan(planText)
                    onSave()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Save Workout Plan")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Format Example:\nPlan Name: My Workout Plan\n\nDay 1: [Mon] Push Day {chest, triceps}\n- Bench Press | 3 sets of 10 | 90s\n...",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
} 