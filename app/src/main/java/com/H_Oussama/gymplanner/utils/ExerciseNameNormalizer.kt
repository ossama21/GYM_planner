package com.H_Oussama.gymplanner.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.H_Oussama.gymplanner.data.model.WorkoutPlan
import com.H_Oussama.gymplanner.data.repositories.UserPreferencesRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class to normalize exercise names by matching them with standard names
 * in the exercise_gifs_organized.md file using Gemini AI
 */
@Singleton
class ExerciseNameNormalizer @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val context: Context
) {
    private val TAG = "ExerciseNameNormalizer"
    private var generativeModel: GenerativeModel? = null
    private var exerciseReferenceContent: String? = null
    
    // Progress tracking
    private val _normalizationProgress = MutableStateFlow(0f)
    val normalizationProgress: StateFlow<Float> = _normalizationProgress
    
    // Error outcome classes
    sealed class NormalizationResult {
        data class Success(val plan: WorkoutPlan) : NormalizationResult()
        data class Error(val errorType: ErrorType, val message: String) : NormalizationResult()
    }
    
    enum class ErrorType {
        NO_API_KEY,
        NETWORK_ERROR,
        REFERENCE_FILE_ERROR,
        AI_MODEL_ERROR,
        PARSING_ERROR,
        OTHER
    }
    
    /**
     * Reset the normalization progress
     */
    fun resetProgress() {
        _normalizationProgress.value = 0f
    }
    
    /**
     * Update normalization progress
     */
    private suspend fun updateProgress(progress: Float) {
        _normalizationProgress.value = progress
        // Add a small delay to ensure UI can render the progress
        delay(50)
    }
    
    /**
     * Check if the device has internet connection
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    /**
     * Initialize the Gemini model with API key
     * @return true if initialization was successful, false otherwise
     */
    private fun initializeGeminiModel(apiKey: String): Boolean {
        return try {
            if (apiKey.isNotEmpty() && generativeModel == null) {
                generativeModel = GenerativeModel(
                    modelName = "gemini-2.0-flash",
                    apiKey = apiKey
                )
            }
            generativeModel != null
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Gemini model: ${e.message}")
            false
        }
    }
    
    /**
     * Load the exercise reference content from assets
     */
    private suspend fun loadExerciseReference(): String? {
        if (exerciseReferenceContent != null) {
            return exerciseReferenceContent
        }
        
        return withContext(Dispatchers.IO) {
            try {
                // Update progress to 15%
                updateProgress(0.15f)
                val inputStream = context.assets.open("exercise_gifs_organized.md")
                val reader = BufferedReader(InputStreamReader(inputStream))
                val content = reader.readText()
                reader.close()
                exerciseReferenceContent = content
                
                // Update progress to 30%
                updateProgress(0.3f)
                content
            } catch (e: Exception) {
                Log.e(TAG, "Error loading exercise reference file: ${e.message}")
                null
            }
        }
    }
    
    /**
     * Process a workout plan to normalize all exercise names based on the reference list
     *
     * @param workoutPlan The workout plan to process
     * @return NormalizationResult with either the updated workout plan or an error
     */
    suspend fun normalizeWorkoutPlanExerciseNames(workoutPlan: WorkoutPlan): NormalizationResult {
        // Reset progress first
        resetProgress()
        updateProgress(0.05f)
        
        // Check for API key
        val apiKey = userPreferencesRepository.getGeminiApiKey()
        Log.d(TAG, "Using Gemini API key from UserPreferencesRepository (length: ${apiKey.length})")
        
        if (apiKey.isBlank()) {
            return NormalizationResult.Error(
                ErrorType.NO_API_KEY,
                "Gemini API key not configured. Please add your Gemini API key in Settings to enable exercise name normalization."
            )
        }
        
        // Update progress to 10%
        updateProgress(0.1f)
        
        // Check network connectivity
        if (!isNetworkAvailable()) {
            return NormalizationResult.Error(
                ErrorType.NETWORK_ERROR,
                "No internet connection available. Please connect to the internet and try again."
            )
        }
        
        // Initialize the model
        if (!initializeGeminiModel(apiKey)) {
            return NormalizationResult.Error(
                ErrorType.AI_MODEL_ERROR,
                "Failed to initialize Gemini AI model. Please check your API key and try again."
            )
        }
        
        // Load reference content (progress updated within the function)
        val exerciseReference = loadExerciseReference()
        if (exerciseReference == null) {
            return NormalizationResult.Error(
                ErrorType.REFERENCE_FILE_ERROR,
                "Failed to load exercise reference content. Please restart the app and try again."
            )
        }
        
        // Extract all exercise names from the workout plan
        updateProgress(0.4f)
        val exerciseNames = workoutPlan.days.flatMap { day ->
            day.exercises.map { it.exerciseId }
        }.distinct()
        
        if (exerciseNames.isEmpty()) {
            // No exercises to normalize, set progress to 100% and return success
            updateProgress(1.0f)
            return NormalizationResult.Success(workoutPlan)
        }
        
        // Create a prompt for Gemini AI
        updateProgress(0.5f)
        
        val prompt = buildString {
            append("I have a list of exercise names that I need to match with standard exercise names from a reference list. ")
            append("For each exercise name, find the closest match in the reference list and return the standardized name. ")
            append("If there's no close match, keep the original name. ")
            append("Return the results in a JSON format with the original name as the key and the standardized name as the value.\n\n")
            
            append("Exercise names to normalize:\n")
            exerciseNames.forEach { append("- $it\n") }
            
            append("\nReference list of standard exercise names:\n")
            append(exerciseReference)
            
            append("\nPlease return ONLY a valid JSON object with this exact format:\n")
            append("{\n")
            append("  \"exercise_name_1\": \"standardized_name_1\",\n")
            append("  \"exercise_name_2\": \"standardized_name_2\"\n")
            append("}\n")
            append("Don't include any explanations or other text outside of the JSON object.")
        }
        
        try {
            // Call Gemini API
            updateProgress(0.6f)
            val model = generativeModel ?: throw IllegalStateException("Gemini model not initialized")
            
            val response = withContext(Dispatchers.IO) {
                model.generateContent(
                    content {
                        text(prompt)
                    }
                )
            }
            
            // Process the response
            updateProgress(0.7f)
            val (success, updatedPlan, errorMessage) = processGeminiResponse(response, workoutPlan)
            
            if (!success) {
                return NormalizationResult.Error(
                    ErrorType.PARSING_ERROR,
                    "Failed to parse AI response: $errorMessage"
                )
            }
            
            // Success! Set progress to 100% and return the updated plan
            updateProgress(1.0f)
            return NormalizationResult.Success(updatedPlan)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during normalization: ${e.message}", e)
            return NormalizationResult.Error(
                ErrorType.OTHER,
                "An error occurred during normalization: ${e.message ?: "Unknown error"}"
            )
        }
    }
    
    /**
     * Process the Gemini API response and update the workout plan with standardized exercise names
     */
    private fun processGeminiResponse(
        response: GenerateContentResponse,
        workoutPlan: WorkoutPlan
    ): Triple<Boolean, WorkoutPlan, String> {
        try {
            val responseText = response.text?.trim() ?: 
                return Triple(false, workoutPlan, "Empty response from AI")
            
            // Extract JSON content (in case there's any text before or after the JSON)
            val jsonPattern = """\{[\s\S]*\}""".toRegex()
            val jsonMatch = jsonPattern.find(responseText)
            val jsonContent = jsonMatch?.value ?: 
                return Triple(false, workoutPlan, "No valid JSON found in response")
            
            // Parse the JSON mapping
            val mappings = mutableMapOf<String, String>()
            val jsonObject = org.json.JSONObject(jsonContent)
            val keys = jsonObject.keys()
            
            while (keys.hasNext()) {
                val key = keys.next()
                val value = jsonObject.getString(key)
                mappings[key] = value
            }
            
            if (mappings.isEmpty()) {
                return Triple(false, workoutPlan, "No mappings found in JSON response")
            }
            
            // Update the workout plan with standardized exercise names
            val updatedDays = workoutPlan.days.map { day ->
                val updatedExercises = day.exercises.map { exercise ->
                    val standardizedName = mappings[exercise.exerciseId] ?: exercise.exerciseId
                    exercise.copy(exerciseId = standardizedName)
                }
                day.copy(exercises = updatedExercises)
            }
            
            return Triple(true, workoutPlan.copy(days = updatedDays), "")
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini response: ${e.message}", e)
            return Triple(false, workoutPlan, e.message ?: "Unknown error parsing AI response")
        }
    }
} 