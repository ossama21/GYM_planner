package com.H_Oussama.gymplanner.ui.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.H_Oussama.gymplanner.ui.common.TransparentTopBar
import com.H_Oussama.gymplanner.ui.theme.GymPlannerTheme
import com.H_Oussama.gymplanner.data.repositories.UserPreferencesRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onDataChanged: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditAgeDialog by remember { mutableStateOf(false) }
    var showEditWeightDialog by remember { mutableStateOf(false) }
    var showEditHeightDialog by remember { mutableStateOf(false) }
    var showEditGoalDialog by remember { mutableStateOf(false) }
    var showEditBodyTypeDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Transparent top bar
        TransparentTopBar(
            title = "Profile",
            onBackClick = onBackClick
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp) // Add some space between top bar and content
        ) {
            // Profile header with avatar
            ProfileHeader(
                username = uiState.username,
                age = uiState.age,
                onEditPhotoClick = { /* Open photo picker */ }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Personal Information Section
            ProfileSection(title = "Personal Information") {
                ProfileItem(
                    icon = Icons.Default.Person,
                    title = "Name",
                    value = uiState.username,
                    onClick = { showEditNameDialog = true }
                )
                
                ProfileItem(
                    icon = Icons.Default.Cake,
                    title = "Age",
                    value = "${uiState.age} years",
                    onClick = { showEditAgeDialog = true }
                )
                
                ProfileItem(
                    icon = Icons.Default.Scale,
                    title = "Weight",
                    value = "${uiState.weight} kg",
                    onClick = { showEditWeightDialog = true }
                )
                
                ProfileItem(
                    icon = Icons.Default.Height,
                    title = "Height", 
                    value = "${uiState.height} cm",
                    onClick = { showEditHeightDialog = true }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Fitness Goals Section
            ProfileSection(title = "Fitness Goals") {
                ProfileItem(
                    icon = Icons.Default.Flag,
                    title = "Goal",
                    value = uiState.goal,
                    onClick = { showEditGoalDialog = true }
                )
                
                ProfileItem(
                    icon = Icons.Default.RepeatOne,
                    title = "Workouts per week",
                    value = "${uiState.workoutsPerWeek}",
                    onClick = { /* Show workout frequency dialog */ }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Activity Stats Section
            ProfileSection(title = "Activity Statistics") {
                ProfileItem(
                    icon = Icons.Default.Fireplace,
                    title = "Total Workouts",
                    value = "${uiState.totalWorkouts}",
                    onClick = null // Read-only item
                )
                
                ProfileItem(
                    icon = Icons.Default.FitnessCenter,
                    title = "Total Weight Lifted",
                    value = "${uiState.totalWeightLifted} kg",
                    onClick = null // Read-only item
                )
                
                ProfileItem(
                    icon = Icons.Default.Timer,
                    title = "Total Time Exercising",
                    value = "${uiState.totalTimeExercising} hours",
                    onClick = null // Read-only item
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Account Section
            ProfileSection(title = "Account") {
                ProfileItem(
                    icon = Icons.Default.Email,
                    title = "Email",
                    value = uiState.email,
                    onClick = { /* Open email edit dialog */ }
                )
                
                ProfileItem(
                    icon = Icons.Default.Password,
                    title = "Password",
                    value = "••••••••",
                    onClick = { /* Open password change dialog */ }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Delete Account button
            DeleteAccountButton(onClick = { /* Show delete confirmation dialog */ })
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Edit dialogs
    if (showEditNameDialog) {
        EditTextFieldDialog(
            title = "Edit Name",
            initialValue = uiState.username,
            onDismiss = { showEditNameDialog = false },
            onConfirm = { 
                viewModel.updateUsername(it)
                showEditNameDialog = false
                onDataChanged()
            }
        )
    }
    
    if (showEditAgeDialog) {
        EditNumberDialog(
            title = "Edit Age",
            initialValue = uiState.age,
            onDismiss = { showEditAgeDialog = false },
            onConfirm = { 
                viewModel.updateAge(it)
                showEditAgeDialog = false
                onDataChanged()
            }
        )
    }
    
    if (showEditWeightDialog) {
        EditNumberDialog(
            title = "Edit Weight (kg)",
            initialValue = uiState.weight.toInt(),
            onDismiss = { showEditWeightDialog = false },
            onConfirm = { 
                viewModel.updateWeight(it.toFloat())
                showEditWeightDialog = false
                onDataChanged()
            }
        )
    }
    
    if (showEditHeightDialog) {
        EditNumberDialog(
            title = "Edit Height (cm)",
            initialValue = uiState.height,
            onDismiss = { showEditHeightDialog = false },
            onConfirm = { 
                viewModel.updateHeight(it)
                showEditHeightDialog = false
                onDataChanged()
            }
        )
    }
    
    if (showEditGoalDialog) {
        GoalSelectionDialog(
            currentGoal = uiState.goal,
            onDismiss = { showEditGoalDialog = false },
            onSelect = { 
                viewModel.updateGoal(it)
                showEditGoalDialog = false
                onDataChanged()
            }
        )
    }
    
    // Body information section
    BodyInfoSection(
        age = uiState.age,
        weight = uiState.weight,
        height = uiState.height,
        bodyType = uiState.bodyType,
        onEditAge = { showEditAgeDialog = true },
        onEditWeight = { showEditWeightDialog = true },
        onEditHeight = { showEditHeightDialog = true },
        onEditBodyType = { showEditBodyTypeDialog = true }
    )
    
    // Body type dialog
    if (showEditBodyTypeDialog) {
        BodyTypeSelectionDialog(
            currentBodyType = uiState.bodyType,
            onBodyTypeSelected = { 
                viewModel.updateBodyType(it)
                showEditBodyTypeDialog = false
                onDataChanged()
            },
            onDismiss = { showEditBodyTypeDialog = false }
        )
    }
}

@Composable
fun ProfileHeader(
    username: String,
    age: Int,
    onEditPhotoClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile avatar with edit button
        Box(modifier = Modifier.size(120.dp)) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(60.dp)
                )
            }
            
            // Edit button
            FloatingActionButton(
                onClick = onEditPhotoClick,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.BottomEnd),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit photo",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // User name
        Text(
            text = username,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        // User age
        Text(
            text = "$age years old",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ProfileSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

@Composable
fun ProfileItem(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: (() -> Unit)?
) {
    val modifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Title and value
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = value,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        
        // Only show arrow icon if the item is clickable
        if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
    
    // Add divider if this isn't the last item (would need to add isLast parameter)
    Divider(
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        modifier = Modifier.padding(start = 72.dp)
    )
}

@Composable
fun DeleteAccountButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.DeleteForever,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Delete Account")
    }
}

@Composable
fun EditTextFieldDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf(initialValue) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(value) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditNumberDialog(
    title: String,
    initialValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var value by remember { mutableStateOf(initialValue.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    // Only accept digits
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        value = newValue
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    val numberValue = value.toIntOrNull() ?: initialValue
                    onConfirm(numberValue) 
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun GoalSelectionDialog(
    currentGoal: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val goals = listOf(
        "Build muscle",
        "Lose weight",
        "Improve fitness",
        "Increase strength",
        "Maintain weight",
        "Improve endurance"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Goal") },
        containerColor = Color(0xFF1A1D2A),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        text = {
            Column {
                goals.forEach { goal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(goal) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = goal == currentGoal,
                            onClick = { onSelect(goal) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF367CFF),
                                unselectedColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = goal,
                            color = Color.White
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF367CFF)
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        enabled = enabled,
        singleLine = singleLine,
        leadingIcon = if (icon != null) {
            { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileWeightField(
    value: String,
    onValueChange: (String) -> Unit,
    weightUnit: String = "kg",
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Weight") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        leadingIcon = { 
            Icon(
                Icons.Default.Scale,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            Text(
                text = weightUnit,
                modifier = Modifier.padding(end = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun BodyInfoSection(
    age: Int,
    weight: Float,
    height: Int,
    bodyType: String,
    onEditAge: () -> Unit,
    onEditWeight: () -> Unit,
    onEditHeight: () -> Unit,
    onEditBodyType: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MonitorWeight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Body Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ProfileItem(
                icon = Icons.Default.Cake,
                title = "Age",
                value = "$age years",
                onClick = onEditAge
            )
            ProfileItem(
                icon = Icons.Default.Balance,
                title = "Weight",
                value = "$weight kg",
                onClick = onEditWeight
            )
            ProfileItem(
                icon = Icons.Default.Height,
                title = "Height",
                value = "$height cm",
                onClick = onEditHeight
            )
            ProfileItem(
                icon = Icons.Default.Person,
                title = "Body Type",
                value = bodyType.replaceFirstChar { it.uppercase() },
                onClick = onEditBodyType
            )
        }
    }
}

@Composable
private fun BodyTypeSelectionDialog(
    currentBodyType: String,
    onBodyTypeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val bodyTypes = listOf(
        UserPreferencesRepository.BODY_TYPE_ECTOMORPH,
        UserPreferencesRepository.BODY_TYPE_MESOMORPH,
        UserPreferencesRepository.BODY_TYPE_ENDOMORPH
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Body Type") },
        text = {
            Column {
                bodyTypes.forEach { bodyType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBodyTypeSelected(bodyType) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = bodyType == currentBodyType,
                            onClick = { onBodyTypeSelected(bodyType) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when(bodyType) {
                                UserPreferencesRepository.BODY_TYPE_ECTOMORPH -> "Ectomorph (Slim)"
                                UserPreferencesRepository.BODY_TYPE_MESOMORPH -> "Mesomorph (Muscular)"
                                UserPreferencesRepository.BODY_TYPE_ENDOMORPH -> "Endomorph (Heavier)"
                                else -> bodyType.capitalize()
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    // Using viewModel API to get a preview instance
    val mockViewModel = androidx.lifecycle.viewmodel.compose.viewModel<ProfileViewModel>()
    
    // Update the UI state with dummy values for preview
    mockViewModel.updateUiStateForPreview(
        ProfileUiState(
            username = "Testing User",
            age = 28,
            weight = 75.5f,
            height = 180,
            goal = "Build muscle",
            workoutsPerWeek = 4,
            totalWorkouts = 32,
            totalWeightLifted = 2450,
            totalTimeExercising = 28,
            email = "user@example.com"
        )
    )

    GymPlannerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ProfileScreen(
                viewModel = mockViewModel
            )
        }
    }
} 
 
 