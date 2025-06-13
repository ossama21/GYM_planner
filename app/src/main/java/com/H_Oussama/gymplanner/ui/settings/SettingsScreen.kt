package com.H_Oussama.gymplanner.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.H_Oussama.gymplanner.R
import com.H_Oussama.gymplanner.ui.theme.GymPlannerTheme
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.H_Oussama.gymplanner.ui.navigation.Routes

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToUnits: () -> Unit = {},
    onNavigateToTheme: () -> Unit = {},
    onNavigateToWorkoutGoals: () -> Unit = {},
    onNavigateToManageImages: () -> Unit = {},
    navController: NavController
) {
    // Refresh user data whenever the screen is shown
    LaunchedEffect(true) {
        viewModel.refreshUserData()
    }
    
    val uiState = viewModel.uiState.collectAsState().value
    val context = LocalContext.current
    
    // Dialog states
    var showAboutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }
    var showGeminiApiDialog by remember { mutableStateOf(false) }
    var apiKeyText by remember { mutableStateOf(uiState.geminiApiKey) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Profile Card
        ProfileCard(
            username = uiState.username,
            age = uiState.age,
            isSignedIn = uiState.isSignedIn,
            weight = uiState.weight,
            goal = uiState.goal,
            workoutsPerWeek = uiState.workoutsPerWeek,
            onSignInClick = viewModel::onSignInClick
        )
        
        // Settings Categories
        SettingsSection(
            title = stringResource(id = R.string.account),
            items = listOf(
                SettingsItemData(
                    title = stringResource(id = R.string.profile),
                    icon = Icons.Default.Person,
                    onClick = onNavigateToProfile
                ),
                SettingsItemData(
                    title = stringResource(id = R.string.notifications),
                    icon = Icons.Default.Notifications,
                    onClick = onNavigateToNotifications
                )
            )
        )
        
        SettingsSection(
            title = stringResource(id = R.string.preferences),
            items = listOf(
                SettingsItemData(
                    title = stringResource(id = R.string.units),
                    subtitle = "kg, cm",
                    icon = Icons.Default.SquareFoot,
                    onClick = onNavigateToUnits
                ),
                SettingsItemData(
                    title = stringResource(id = R.string.theme),
                    subtitle = "Dark",
                    icon = Icons.Default.DarkMode,
                    onClick = onNavigateToTheme
                ),
                SettingsItemData(
                    title = stringResource(id = R.string.language),
                    subtitle = when (uiState.language) {
                        "fr" -> stringResource(id = R.string.language_french)
                        "ar" -> stringResource(id = R.string.language_arabic)
                        else -> stringResource(id = R.string.language_english)
                    },
                    icon = Icons.Default.Language,
                    onClick = viewModel::onLanguageClick
                )
            )
        )
        
        SettingsSection(
            title = stringResource(id = R.string.training),
            items = listOf(
                SettingsItemData(
                    title = stringResource(id = R.string.workout_goals),
                    icon = Icons.Default.FitnessCenter,
                    onClick = onNavigateToWorkoutGoals
                )
            )
        )
        
        SettingsSection(
            title = stringResource(id = R.string.app),
            items = listOf(
                SettingsItemData(
                    title = stringResource(id = R.string.changelog),
                    icon = Icons.Default.History,
                    onClick = { showChangelogDialog = true }
                ),
                SettingsItemData(
                    title = stringResource(id = R.string.about),
                    icon = Icons.Default.Info,
                    onClick = { showAboutDialog = true }
                ),
                SettingsItemData(
                    title = "Skip Intro",
                    subtitle = if (uiState.skipIntro) "Intro disabled" else "Show intro on startup",
                    icon = Icons.Default.SkipNext,
                    hasToggle = true,
                    isToggleChecked = uiState.skipIntro,
                    onToggleChange = { viewModel.toggleSkipIntro() },
                    onClick = { viewModel.toggleSkipIntro() }
                )
            )
        )
        
        // Developer Mode Section - only shown when enabled
        if (uiState.developerMode) {
            SettingsSection(
                title = "Developer Options",
                items = listOf(
                    SettingsItemData(
                        title = "Test Update Check",
                        subtitle = "Manually trigger update check",
                        icon = Icons.Default.Update,
                        onClick = { viewModel.triggerManualUpdateCheck(context) }
                    ),
                    SettingsItemData(
                        title = stringResource(id = R.string.gemini_api_settings),
                        subtitle = if (uiState.geminiApiKey.isNotEmpty()) 
                            stringResource(id = R.string.api_key_configured) 
                        else 
                            stringResource(id = R.string.configure_api_key),
                        icon = Icons.Default.Api,
                        onClick = { showGeminiApiDialog = true },
                        hasEndContent = uiState.geminiApiKey.isNotEmpty(),
                        endContentType = 1
                    ),
                    SettingsItemData(
                        title = stringResource(id = R.string.manage_exercise_images),
                        icon = Icons.Default.PhotoLibrary,
                        onClick = onNavigateToManageImages
                    )
                ),
                geminiApiTestResult = when (uiState.geminiApiTestResult) {
                    GeminiApiTestResult.SUCCESS -> 1
                    GeminiApiTestResult.FAILED -> 2
                    GeminiApiTestResult.ERROR -> 3
                    GeminiApiTestResult.TESTING -> 4
                    else -> 0
                }
            )
        }
        
        // Logout button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = viewModel::onLogoutClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(id = R.string.logout))
            }
        }
        
        // Version info
        Text(
            text = stringResource(id = R.string.version),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .clickable { viewModel.onVersionClicked() },
            textAlign = TextAlign.Center
        )
    }
    
    // About Dialog
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
    
    // Help & Support Dialog
    if (showHelpDialog) {
        HelpAndSupportDialog(onDismiss = { showHelpDialog = false })
    }
    
    // Changelog Dialog
    if (showChangelogDialog) {
        ChangelogDialog(onDismiss = { showChangelogDialog = false })
    }
    
    // Gemini API Dialog
    if (showGeminiApiDialog) {
        AlertDialog(
            onDismissRequest = { showGeminiApiDialog = false },
            title = { Text(stringResource(id = R.string.gemini_api_settings)) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    OutlinedTextField(
                        value = apiKeyText,
                        onValueChange = { apiKeyText = it },
                        label = { Text(stringResource(id = R.string.api_key)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = apiKeyText.isBlank() && uiState.geminiApiTestResult == GeminiApiTestResult.FAILED
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Test result display
                    if (uiState.geminiApiTestResult != GeminiApiTestResult.NOT_TESTED) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        color = when (uiState.geminiApiTestResult) {
                                            GeminiApiTestResult.SUCCESS -> Color.Green
                                            GeminiApiTestResult.FAILED, GeminiApiTestResult.ERROR -> Color.Red
                                            GeminiApiTestResult.TESTING -> Color.Yellow
                                            else -> Color.Gray
                                        },
                                        shape = CircleShape
                                    )
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = when (uiState.geminiApiTestResult) {
                                    GeminiApiTestResult.SUCCESS -> stringResource(id = R.string.api_working)
                                    GeminiApiTestResult.FAILED -> stringResource(id = R.string.api_failed)
                                    GeminiApiTestResult.ERROR -> stringResource(id = R.string.api_error, uiState.geminiApiTestError)
                                    GeminiApiTestResult.TESTING -> stringResource(id = R.string.api_testing)
                                    else -> stringResource(id = R.string.api_not_tested)
                                },
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    // Testing indicator
                    if (uiState.isTestingGeminiApi) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Row {
                    TextButton(
                        onClick = {
                            if (apiKeyText.isNotBlank()) {
                            viewModel.updateGeminiApiKey(apiKeyText)
                            viewModel.testGeminiApiKey()
                        }
                        },
                        enabled = apiKeyText.isNotBlank() && !uiState.isTestingGeminiApi
                    ) {
                        Text(stringResource(id = R.string.test_save))
                    }
                    
                    TextButton(
                        onClick = { 
                            // Always save the key if it's not blank before closing
                            if (apiKeyText.isNotBlank()) {
                                viewModel.updateGeminiApiKey(apiKeyText)
                            }
                            showGeminiApiDialog = false 
                        }
                    ) {
                        Text(stringResource(id = R.string.close))
                    }
                }
            }
        )
    }
    
    // Language Selection Dialog
    if (uiState.showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLanguageDialog() },
            title = { Text(stringResource(id = R.string.language)) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    // English option
                    LanguageOption(
                        name = stringResource(id = R.string.language_english),
                        code = "en",
                        isSelected = uiState.language == "en",
                        onSelect = { viewModel.setLanguage(context, "en") }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // French option
                    LanguageOption(
                        name = stringResource(id = R.string.language_french),
                        code = "fr",
                        isSelected = uiState.language == "fr",
                        onSelect = { viewModel.setLanguage(context, "fr") }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Arabic option
                    LanguageOption(
                        name = stringResource(id = R.string.language_arabic),
                        code = "ar",
                        isSelected = uiState.language == "ar",
                        onSelect = { viewModel.setLanguage(context, "ar") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.dismissLanguageDialog() }
                ) {
                    Text(stringResource(id = R.string.close))
                }
            }
        )
    }
}

@Composable
fun LanguageOption(
    name: String,
    code: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onSelect() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun AppHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Logo
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF181C2A), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = R.string.app_name),
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProfileCard(
    username: String,
    age: Int,
    isSignedIn: Boolean,
    weight: Float,
    goal: String,
    workoutsPerWeek: Int,
    onSignInClick: () -> Unit
) {
    // Implementation of profile card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // User info
            Text(
                text = username,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Cake,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$age ${stringResource(id = R.string.years)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Icon(
                    imageVector = Icons.Default.Balance,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$weight kg",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User goals and stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.goal),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = goal,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text(
                        text = stringResource(id = R.string.workouts_per_week),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = workoutsPerWeek.toString(),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.about_title)) },
        text = {
            Column {
                Text(stringResource(id = R.string.about_description))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(stringResource(id = R.string.version_text))
                Text(stringResource(id = R.string.developer))
                Text(stringResource(id = R.string.developer_email))
                Text(stringResource(id = R.string.developer_github))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(stringResource(id = R.string.features), fontWeight = FontWeight.Bold)
                Text(stringResource(id = R.string.feature_1))
                Text(stringResource(id = R.string.feature_2))
                Text(stringResource(id = R.string.feature_3))
                Text(stringResource(id = R.string.feature_4))
                Text(stringResource(id = R.string.feature_5))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(stringResource(id = R.string.thank_you))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.close))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun HelpAndSupportDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.help_title)) },
        text = {
            Column {
                Text(stringResource(id = R.string.help_description))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(stringResource(id = R.string.faq), fontWeight = FontWeight.Bold)
                Text(stringResource(id = R.string.faq_1))
                Text(stringResource(id = R.string.faq_2))
                Text(stringResource(id = R.string.faq_3))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(stringResource(id = R.string.contact_support), fontWeight = FontWeight.Bold)
                Text(stringResource(id = R.string.support_email))
                Text(stringResource(id = R.string.response_time))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(stringResource(id = R.string.user_guide), fontWeight = FontWeight.Bold)
                Text(stringResource(id = R.string.user_guide_text))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(stringResource(id = R.string.community), fontWeight = FontWeight.Bold)
                Text(stringResource(id = R.string.community_text))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.close))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun ChangelogDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.changelog_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(stringResource(id = R.string.changelog_v4_5))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(stringResource(id = R.string.changelog_v4_0))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(stringResource(id = R.string.changelog_v3_5))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(stringResource(id = R.string.changelog_v3_0))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.close))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    val previewViewModel = object {
        val uiState = SettingsUiState(
            username = "Testing",
            age = 23,
            isSignedIn = false,
            weight = 66.2f,
            goal = "Build muscle",
            workoutsPerWeek = 5
        )
    }
    
    GymPlannerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF14161F)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // App header
                    AppHeader()
                    
                    // Profile Card
                    ProfileCard(
                        username = previewViewModel.uiState.username,
                        age = previewViewModel.uiState.age,
                        isSignedIn = previewViewModel.uiState.isSignedIn,
                        weight = previewViewModel.uiState.weight,
                        goal = previewViewModel.uiState.goal,
                        workoutsPerWeek = previewViewModel.uiState.workoutsPerWeek,
                        onSignInClick = { }
                    )
                    
                    // Settings Categories (just showing one for the preview)
                    SettingsSection(
                        title = "Account",
                        items = listOf(
                            SettingsItemData(
                                title = "Profile",
                                icon = Icons.Default.Person,
                                onClick = { }
                            ),
                            SettingsItemData(
                                title = "Notifications",
                                subtitle = "Daily reminders",
                                icon = Icons.Default.Notifications,
                                onClick = { }
                            )
                        ),
                        geminiApiTestResult = 0
                    )
                }
            }
        }
    }
} 