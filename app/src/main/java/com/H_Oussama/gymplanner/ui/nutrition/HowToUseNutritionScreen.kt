package com.H_Oussama.gymplanner.ui.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToUseNutritionScreen(
    onNavigateBack: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    // Define the prompt for ChatGPT
    val chatGptPrompt = """
Act as a nutrition calculator. Ask me for the details of a meal I ate (including ingredients and approximate quantities if possible) and the meal type (Breakfast, Lunch, Dinner, or Snack). Then, estimate the nutritional information (calories, protein, carbs, fat) for the *entire* meal described. 

VERY IMPORTANT: Provide your final answer *only* as a single JSON object in the following format, with no other text, introductions, or explanations before or after it:

{
  "meal_type": "<Breakfast|Lunch|Dinner|Snack>",
  "food_name": "<Concise name of the meal/food>",
  "calories": <Total estimated calories as integer>,
  "protein_g": <Total estimated protein in grams as float>,
  "carbs_g": <Total estimated carbs in grams as float>,
  "fat_g": <Total estimated fat in grams as float>
}

Example: If I tell you I had 'two scrambled eggs with spinach and a slice of toast' for 'Breakfast', you should output something like:
{
  "meal_type": "Breakfast",
  "food_name": "Scrambled eggs with spinach and toast",
  "calories": 350,
  "protein_g": 22.5,
  "carbs_g": 15.8,
  "fat_g": 20.1
}

Do not include the example in your actual response to me. Just provide the JSON for the meal I describe.
""".trimIndent()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("How to Log Nutrition") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Manual Nutrition Logging Guide",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                "This app uses a manual process to log nutrition data with the help of an AI like ChatGPT:",
                style = MaterialTheme.typography.bodyLarge
            )

            // Step 1: Copy Prompt
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Step 1: Copy the Prompt Below", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                         "Use this prompt with an AI assistant (like ChatGPT, Claude, Gemini, etc.) to get nutritional information in the correct format.",
                         style = MaterialTheme.typography.bodyMedium
                     )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = chatGptPrompt,
                        onValueChange = {}, // Read-only
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 150.dp, max= 300.dp),
                        label = { Text("Prompt for AI Assistant") },
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp) // Use Material typography
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(chatGptPrompt))
                            // Optionally show a snackbar confirmation
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Copy Prompt")
                    }
                }
            }

            // Step 2: Use AI
             Card(modifier = Modifier.fillMaxWidth()) {
                 Column(Modifier.padding(16.dp)) {
                     Text("Step 2: Get Nutrition Data from AI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                     Spacer(modifier = Modifier.height(8.dp))
                     Text(
                         "1. Go to your preferred AI assistant (e.g., chatgpt.com).\n" +
                         "2. Paste the copied prompt and start a new chat.\n" +
                         "3. Answer the AI's questions about your meal (what you ate, quantity, meal type).\n" +
                         "4. The AI should reply with ONLY a JSON object containing the nutrition data.\n" +
                         "5. Copy the entire JSON response provided by the AI.",
                         style = MaterialTheme.typography.bodyMedium
                     )
                 }
            }
            
             // Step 3: Paste into App
            Card(modifier = Modifier.fillMaxWidth()) {
                 Column(Modifier.padding(16.dp)) {
                    Text("Step 3: Add Data to App", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                     Spacer(modifier = Modifier.height(8.dp))
                     Text(
                         "1. Come back to the Nutrition screen in this app.\n" +
                         "2. Tap the '+' Floating Action Button.\n" +
                         "3. Paste the entire JSON response you copied from the AI into the text field.\n" +
                         "4. Tap 'Save'. The app will parse the data and add the entry.",
                         style = MaterialTheme.typography.bodyMedium
                     )
                }
            }
        }
    }
} 