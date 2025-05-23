package com.H_Oussama.gymplanner.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Enhanced image matcher that uses a keyword-based approach for matching 
 * exercise names to available image files.
 */
class EnhancedImageMatcher(private val context: Context) {
    companion object {
        private const val TAG = "EnhancedImageMatcher"
        private const val DEBUG = true // Set to true for detailed logging
        private const val PREFS_NAME = "exercise_image_cache"
        private const val KEY_LAST_CACHE_UPDATE = "last_cache_update"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val cacheDir: File = File(context.cacheDir, "exercise_images")
    
    // Cache of available image files to avoid repeated file system access
    private var exerciseImageCache: List<String>? = null
    private var muscleImageCache: Map<String, Bitmap>? = null
    private var cachedBodyImageFiles: List<String>? = null
    
    // Direct image mappings - exact matches take priority
    private val directMappings = mapOf(
        "bench press" to "bench-press",
        "barbell squat" to "barbell_squats",
        "deadlift" to "deadlift-howto",
        "pull ups" to "pull-ups",
        "pull-ups" to "pull-ups",
        "barbell rows" to "barbbell_rows",
        "incline dumbbell press" to "incline-dumbbell-bench-press"
        // Add more direct mappings as needed
    )
    
    // Keyword to image mappings - for partial matching
    private val keywordMappings = mapOf(
        "bench" to "bench-press",
        "squat" to "barbell_squats",
        "deadlift" to "deadlift-howto",
        "pull" to "pull-ups",
        "row" to "barbbell_rows",
        "cable fly" to "cable-fly",
        "fly" to "cable-fly",
        "incline" to "incline-dumbbell-bench-press",
        "shoulder press" to "dumbbell-shoulder-press",
        "dumbbell press" to "dumbbell-shoulder-press",
        "lateral raise" to "cable-lateral-raise",
        "curl" to "barbell-curl",
        "hammer" to "hammer-curl",
        "tricep" to "lying-tricep-extension",
        "extension" to "lying-tricep-extension",
        "lat pulldown" to "lat-pulldown",
        "seated row" to "seated-cable-row",
        "calf raise" to "standing-calf-raise",
        "push up" to "push-ups",
        "upright row" to "upright-row",
        "leg press" to "leg-press",
        "romanian" to "romanian-deadlift"
        // Add more keyword mappings as needed
    )
    
    // Muscle group image mappings
    private val muscleGroupMappings = mapOf(
        "CHEST" to "chest.jpg",
        "BACK" to "back.jpg",
        "SHOULDERS" to "shoulders.jpg",
        "BICEPS" to "biceps.jpg",
        "TRICEPS" to "triceps.jpg",
        "QUADS" to "quads.jpg",
        "CALVES" to "calf.jpg",
        "FOREARMS" to "forearms.jpg",
        "ARMS" to "biceps.jpg" // Default "ARMS" to show biceps as requested
    )
    
    init {
        // Create cache directory if it doesn't exist
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }
    
    /**
     * Clears all internal image caches.
     */
    fun clearCaches() {
        logDebug("Clearing all image caches")
        exerciseImageCache = null
        muscleImageCache = null
        cachedBodyImageFiles = null
        // Clear the SharedPreferences cache timestamp to ensure fresh load next time
        prefs.edit {
            remove(KEY_LAST_CACHE_UPDATE)
        }
        // Optionally, clear the disk cache as well if it's causing issues,
        // but be mindful of performance implications.
        // try {
        //     cacheDir.deleteRecursively()
        //     cacheDir.mkdirs()
        //     logDebug("Cleared disk cache directory")
        // } catch (e: Exception) {
        //     Log.e(TAG, "Error clearing disk cache: ${e.message}")
        // }
    }
    
    /**
     * Initialize the image caches. Call this early to preload images.
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (exerciseImageCache == null) {
            logDebug("Initializing exercise image cache")
            exerciseImageCache = getAvailableExerciseImages()
            logDebug("Found ${exerciseImageCache?.size ?: 0} exercise images")
        }
        
        if (muscleImageCache == null) {
            logDebug("Initializing muscle group image cache")
            muscleImageCache = loadMuscleGroupImages()
            logDebug("Loaded ${muscleImageCache?.size ?: 0} muscle group images")
        }
        
        if (cachedBodyImageFiles == null) {
            logDebug("Caching available body image files")
            try {
                val files = context.assets.list("body_images") ?: emptyArray()
                cachedBodyImageFiles = files.toList()
                logDebug("Cached ${cachedBodyImageFiles?.size ?: 0} body image files: ${cachedBodyImageFiles?.joinToString()}")
            } catch (e: Exception) {
                Log.e(TAG, "Error listing body images: ${e.message}")
                cachedBodyImageFiles = emptyList()
            }
        }
    }
    
    /**
     * Find the best matching image identifier for an exercise name.
     * Uses a multi-step approach:
     * 1. Direct mapping lookup
     * 2. Keyword mapping lookup
     * 3. File existence check
     * 4. Fallback to default
     */
    suspend fun findBestExerciseImage(exerciseName: String): String? = withContext(Dispatchers.IO) {
        if (exerciseName.isBlank()) {
            logDebug("Exercise name is blank, cannot find matching image")
            return@withContext null
        }
        
        val normalizedName = normalizeString(exerciseName)
        logDebug("Finding best image for: '$normalizedName'")
        
        // Step 1: Check direct mappings
        directMappings[normalizedName]?.let { imageId ->
            logDebug("Direct mapping found: $imageId")
            return@withContext imageId
        }
        
        // Step 2: Check keyword mappings
        for ((keyword, imageId) in keywordMappings) {
            if (normalizedName.contains(keyword)) {
                logDebug("Keyword match found: '$keyword' → '$imageId'")
                return@withContext imageId
            }
        }
        
        // Step 3: Check if a file exists with a name similar to the exercise
        val availableImages = exerciseImageCache ?: getAvailableExerciseImages()
        val kebabName = normalizedName.replace(" ", "-")
        val similarImage = availableImages.firstOrNull { 
            normalizeString(it).contains(kebabName) || 
            kebabName.contains(normalizeString(it))
        }
        
        if (similarImage != null) {
            logDebug("Similar image found: $similarImage")
            return@withContext similarImage
        }
        
        // Step 4: Fallback to a default based on word detection
        val fallbackImage = when {
            normalizedName.contains("bench") -> "bench-press"
            normalizedName.contains("chest") -> "bench-press"
            normalizedName.contains("squat") -> "barbell_squats"
            normalizedName.contains("leg") -> "leg-press"
            normalizedName.contains("dead") -> "deadlift-howto"
            normalizedName.contains("pull") -> "pull-ups"
            normalizedName.contains("shoulder") -> "dumbbell-shoulder-press"
            normalizedName.contains("curl") -> "barbell-curl"
            normalizedName.contains("tricep") -> "lying-tricep-extension"
            normalizedName.contains("row") -> "barbbell_rows"
            else -> availableImages.firstOrNull() ?: ""
        }
        
        logDebug("Using fallback image: $fallbackImage")
        return@withContext fallbackImage
    }
    
    /**
     * Load an exercise image from assets directory with the given identifier.
     * Tries different file extensions if needed.
     */
    suspend fun loadExerciseImage(imageIdentifier: String?): Bitmap? = withContext(Dispatchers.IO) {
        if (imageIdentifier.isNullOrBlank()) {
            logDebug("Null or blank image identifier provided")
            return@withContext null
        }
        
        logDebug("Loading exercise image: $imageIdentifier")
        
        // First check if we have a cached version
        val cachedFile = File(cacheDir, "$imageIdentifier.jpg")
        if (cachedFile.exists()) {
            try {
                val bitmap = BitmapFactory.decodeFile(cachedFile.absolutePath)
                if (bitmap != null) {
                    logDebug("Loaded image from cache: ${cachedFile.absolutePath}")
                    return@withContext bitmap
                }
            } catch (e: Exception) {
                logDebug("Failed to load cached image: ${e.message}")
            }
        }
        
        // If not in cache, try loading from assets
        val extensions = listOf("avif", "jpg", "png")
        
        for (ext in extensions) {
            try {
                val imagePath = "exercise_images/$imageIdentifier.$ext"
                logDebug("Trying path: $imagePath")
                
                context.assets.open(imagePath).use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        // Cache the bitmap for future use
                        try {
                            FileOutputStream(cachedFile).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                            }
                            logDebug("Cached image to: ${cachedFile.absolutePath}")
                        } catch (e: Exception) {
                            logDebug("Failed to cache image: ${e.message}")
                        }
                        
                        logDebug("Successfully loaded image: $imagePath")
                        return@withContext bitmap
                    }
                }
            } catch (e: Exception) {
                logDebug("Failed to load with extension $ext: ${e.message}")
                // Continue to next extension
            }
        }
        
        // Try without extension as last resort
        try {
            val imagePath = "exercise_images/$imageIdentifier"
            logDebug("Trying path without extension: $imagePath")
            
            context.assets.open(imagePath).use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                if (bitmap != null) {
                    // Cache the bitmap for future use
                    try {
                        FileOutputStream(cachedFile).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                        }
                        logDebug("Cached image to: ${cachedFile.absolutePath}")
                    } catch (e: Exception) {
                        logDebug("Failed to cache image: ${e.message}")
                    }
                    
                    logDebug("Successfully loaded image without extension")
                    return@withContext bitmap
                }
            }
        } catch (e: Exception) {
            logDebug("Failed to load without extension: ${e.message}")
        }
        
        logDebug("No image found for identifier: $imageIdentifier")
        return@withContext null
    }
    
    /**
     * Load a muscle group image from assets.
     */
    suspend fun loadMuscleImage(muscleGroup: String): Bitmap? = withContext(Dispatchers.IO) {
        if (muscleImageCache?.containsKey(muscleGroup) == true) {
            logDebug("Using cached muscle image for: $muscleGroup")
            return@withContext muscleImageCache?.get(muscleGroup)
        }

        val upperCaseMuscle = muscleGroup.uppercase()
        logDebug("Loading muscle image for: $upperCaseMuscle")

        var filename: String? = null
        var bitmap: Bitmap? = null

        // Try mapped filename first
        filename = muscleGroupMappings[upperCaseMuscle]
        if (filename != null) {
            val imagePath = "body_images/$filename"
            try {
                val inputStream = context.assets.open(imagePath)
                bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    logDebug("Successfully loaded muscle image: $imagePath")
                    // Cache it
                    muscleImageCache = (muscleImageCache ?: emptyMap()) + (upperCaseMuscle to bitmap)
                    return@withContext bitmap
                }
            } catch (e: Exception) {
                logDebug("Failed to load mapped muscle image $filename: ${e.message}")
            }
        }

        // Fallback to simple name if mapping not found or failed
        if (bitmap == null) {
            filename = upperCaseMuscle.lowercase() + ".jpg" // Use uppercase muscle for consistency
            val imagePath = "body_images/$filename"
            try {
                val inputStream = context.assets.open(imagePath)
                bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    logDebug("Successfully loaded muscle image: $imagePath")
                    // Cache it
                    muscleImageCache = (muscleImageCache ?: emptyMap()) + (upperCaseMuscle to bitmap)
                    return@withContext bitmap
                }
            } catch (e: Exception) {
                logDebug("Failed to load muscle image $filename: ${e.message}")
            }
        }

        logDebug("No muscle image found for: $upperCaseMuscle")
        return@withContext null
    }
    
    /**
     * Get a list of available exercise images from assets.
     */
    private suspend fun getAvailableExerciseImages(): List<String> = withContext(Dispatchers.IO) {
        try {
            val files = context.assets.list("exercise_images") ?: emptyArray()
            return@withContext files.toList()
        } catch (e: Exception) {
            Log.e(TAG, "Error listing exercise images: ${e.message}")
            return@withContext emptyList()
        }
    }
    
    /**
     * Load all muscle group images from assets and cache them.
     */
    private suspend fun loadMuscleGroupImages(): Map<String, Bitmap> = withContext(Dispatchers.IO) {
        val imageMap = mutableMapOf<String, Bitmap>()
        for ((muscleGroup, filename) in muscleGroupMappings) {
            try {
                val imagePath = "body_images/$filename"
                context.assets.open(imagePath).use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        imageMap[muscleGroup] = bitmap
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading muscle group image for $muscleGroup: ${e.message}")
            }
        }
        
        return@withContext imageMap
    }
    
    /**
     * Normalize a string for comparison by converting to lowercase and removing special characters.
     */
    private fun normalizeString(input: String): String {
        return input.lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .trim()
    }
    
    /**
     * Log debug messages if DEBUG is enabled.
     */
    private fun logDebug(message: String) {
        if (DEBUG) {
            Log.d(TAG, message)
        }
    }
}