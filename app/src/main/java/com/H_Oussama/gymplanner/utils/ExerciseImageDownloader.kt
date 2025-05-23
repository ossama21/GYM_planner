package com.H_Oussama.gymplanner.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.H_Oussama.gymplanner.data.exercise.ExerciseDefinition
import com.H_Oussama.gymplanner.data.exercise.ExerciseImage
import com.H_Oussama.gymplanner.data.repositories.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ExerciseImageDownloader @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    sealed class DownloadState {
        object Idle : DownloadState()
        object Searching : DownloadState()
        data class Results(val imageUrl: String, val previewBitmap: Bitmap) : DownloadState()
        object Downloading : DownloadState()
        data class Success(val imageUri: Uri) : DownloadState()
        data class Error(val message: String) : DownloadState()
    }

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private var currentExercise: ExerciseDefinition? = null
    private var currentImageUrl: String? = null

    fun searchImage(exercise: ExerciseDefinition, forceWebSearch: Boolean = false) {
        currentExercise = exercise
        _downloadState.value = DownloadState.Searching

        viewModelScope.launch {
            try {
                // Check local storage first (unless forced web search)
                if (!forceWebSearch) {
                    val existingImage = checkLocalImage(exercise.name)
                    if (existingImage != null) {
                        // We found a local image, use it
                        _downloadState.value = DownloadState.Success(existingImage)
                        return@launch
                    }
                }
                
                // No local image found or forceWebSearch is true, search the web
                val searchTerm = "${exercise.name} exercise anatomy"
                
                // Search for real image on the web
                val imageUrl = searchWebForImage(searchTerm)
                
                if (imageUrl.isNullOrEmpty()) {
                    // Fall back to mock data if web search fails
                    Log.d("ExerciseImageDownloader", "Web search failed, using backup data")
                    val backupUrl = getBackupImageUrl(exercise.name)
                    currentImageUrl = backupUrl
                    val previewBitmap = downloadImageAsBitmap(backupUrl)
                    _downloadState.value = DownloadState.Results(backupUrl, previewBitmap)
                } else {
                    // Use the real image we found on the web
                    currentImageUrl = imageUrl
                    val previewBitmap = downloadImageAsBitmap(imageUrl)
                    _downloadState.value = DownloadState.Results(imageUrl, previewBitmap)
                }
            } catch (e: Exception) {
                Log.e("ExerciseImageDownloader", "Search failed", e)
                _downloadState.value = DownloadState.Error("Failed to search for images: ${e.message}")
            }
        }
    }

    suspend fun checkLocalImage(exerciseName: String): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                // Check for downloaded image in app's private storage
                val exerciseImagesDir = context.filesDir.resolve("exercise_images")
                if (exerciseImagesDir.exists()) {
                    val normalizedName = cleanFileName(exerciseName)
                    val files = exerciseImagesDir.listFiles() ?: emptyArray()
                    
                    // Find any file that starts with the exercise name
                    val matchingFile = files.firstOrNull { 
                        it.name.startsWith(normalizedName)
                    }
                    
                    if (matchingFile != null) {
                        return@withContext FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            matchingFile
                        )
                    }
                }
                null
            } catch (e: Exception) {
                Log.e("ExerciseImageDownloader", "Error checking local images", e)
                null
            }
        }
    }

    fun downloadImage() {
        val url = currentImageUrl ?: return
        val exercise = currentExercise ?: return
        
        _downloadState.value = DownloadState.Downloading
        
        viewModelScope.launch {
            try {
                val imageFile = saveImageToFile(url, exercise.name)
                val imageUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    imageFile
                )
                
                // Save the image reference to the database
                val exerciseImage = ExerciseImage(
                    exerciseId = exercise.id,
                    imageUri = imageUri.toString(),
                    isAsset = false
                )
                exerciseRepository.insertExerciseImage(exerciseImage)
                
                // Log the successful save to help with debugging
                Log.d("ExerciseImageDownloader", "Image saved successfully to ${imageFile.absolutePath}")
                Log.d("ExerciseImageDownloader", "Image URI: $imageUri")
                
                // Add a small delay to ensure database operations complete
                withContext(Dispatchers.IO) {
                    delay(200)
                }
                
                _downloadState.value = DownloadState.Success(imageUri)
            } catch (e: Exception) {
                Log.e("ExerciseImageDownloader", "Download failed", e)
                _downloadState.value = DownloadState.Error("Failed to download image: ${e.message}")
            }
        }
    }

    fun resetState() {
        _downloadState.value = DownloadState.Idle
        currentImageUrl = null
    }

    private suspend fun searchWebForImage(searchTerm: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Using Pixabay API which provides instant access (no approval waiting)
                // 1. Register at https://pixabay.com/accounts/register/
                // 2. After signing up, get your API key immediately at: https://pixabay.com/api/docs/
                
                // Note: In a real app, this key should be stored securely, not hardcoded
                val apiKey = "50275041-e9b04d2c79bbc09aca154275f" // User's Pixabay API key
                
                val encodedQuery = URLEncoder.encode("$searchTerm anatomy", "UTF-8")
                val url = URL("https://pixabay.com/api/?key=$apiKey&q=$encodedQuery&image_type=photo&per_page=1")
                
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000 // 10 seconds
                connection.readTimeout = 10000
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(response)
                    val hits = jsonObject.getJSONArray("hits")
                    
                    if (hits.length() > 0) {
                        val firstHit = hits.getJSONObject(0)
                        return@withContext firstHit.getString("largeImageURL")
                    }
                }
                null
            } catch (e: Exception) {
                Log.e("ExerciseImageDownloader", "Error searching web for image", e)
                null
            }
        }
    }

    private suspend fun downloadImageAsBitmap(imageUrl: String): Bitmap {
        return withContext(Dispatchers.IO) {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        }
    }

    private suspend fun saveImageToFile(imageUrl: String, exerciseName: String): File {
        return withContext(Dispatchers.IO) {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            
            val input = connection.inputStream
            
            // Create the directory if it doesn't exist
            val exerciseImagesDir = context.filesDir.resolve("exercise_images")
            if (!exerciseImagesDir.exists()) {
                exerciseImagesDir.mkdirs()
            }
            
            // Create a unique filename based on exercise name
            val cleanName = cleanFileName(exerciseName)
            val uniqueId = UUID.randomUUID().toString().substring(0, 8)
            val file = File(exerciseImagesDir, "${cleanName}_${uniqueId}.jpg")
            
            // Save the image to the file
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
            
            file
        }
    }
    
    private fun cleanFileName(name: String): String {
        return name.replace("[^a-zA-Z0-9]".toRegex(), "_").lowercase()
    }
    
    private fun getBackupImageUrl(exerciseName: String): String {
        // Return a placeholder URL when web search fails
        return "https://via.placeholder.com/600x400.png?text=${URLEncoder.encode(exerciseName, "UTF-8")}"
    }
} 