package com.H_Oussama.gymplanner

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.H_Oussama.gymplanner.ui.common.ExerciseImageFallbackProvider
import com.H_Oussama.gymplanner.util.EnhancedImageMatcher
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import com.H_Oussama.gymplanner.data.repositories.UserPreferencesRepository
import com.H_Oussama.gymplanner.data.repositories.NutritionRepository
import androidx.work.Configuration as WorkManagerConfiguration
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import java.util.concurrent.TimeUnit
import androidx.work.WorkerFactory

// Keep the class, but remove repository instances for now.
// We'll add Hilt annotations here soon.
@HiltAndroidApp
class GymPlannerApplication : Application(), coil.ImageLoaderFactory, WorkManagerConfiguration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var customWorkerFactory: WorkerFactory
    
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository
    
    @Inject
    lateinit var nutritionRepository: NutritionRepository

    // Application scope for coroutines
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Create an instance of our enhanced image matcher
    private lateinit var imageMatcher: EnhancedImageMatcher

    override fun onCreate() {
        super.onCreate()
        
        // Schedule the update worker after WorkManager is initialized
        // (WorkManager is initialized automatically using the Provider implementation)
        scheduleWeeklyUpdateCheck()
        
        // Initialize the language based on saved preference
        initializeLanguage()
        
        // Initialize the Gemini API if key is available
        initializeGeminiApi()
        
        // Initialize the image matcher
        imageMatcher = EnhancedImageMatcher(this)
        
        // Preload image caches in the background first
        preloadImageCaches()
    }
    
    // This is the property that the WorkManager expects from Configuration.Provider interface
    override val workManagerConfiguration: WorkManagerConfiguration
        get() = WorkManagerConfiguration.Builder()
            .setWorkerFactory(customWorkerFactory)
            .build()
    
    private fun scheduleWeeklyUpdateCheck() {
        val updateWorkRequest = PeriodicWorkRequestBuilder<com.H_Oussama.gymplanner.workers.UpdateWorker>(7, TimeUnit.DAYS)
            .build()

        // Use KEEP to ensure we don't schedule multiple workers and don't run immediately
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "weekly_update_check",
                ExistingPeriodicWorkPolicy.KEEP, 
                updateWorkRequest
            )
    }
    
    /**
     * Initialize the Gemini API with the saved key
     */
    private fun initializeGeminiApi() {
        applicationScope.launch {
            try {
                val apiKey = userPreferencesRepository.getGeminiApiKey()
                if (apiKey.isNotBlank()) {
                    Log.d("GymPlannerApp", "Initializing Gemini API on app startup (key length: ${apiKey.length})")
                    nutritionRepository.initializeGeminiModel(apiKey)
                } else {
                    Log.d("GymPlannerApp", "No Gemini API key found, skipping initialization")
                }
            } catch (e: Exception) {
                Log.e("GymPlannerApp", "Error initializing Gemini API: ${e.message}")
            }
        }
    }
    
    /**
     * Initialize language based on saved preference
     */
    private fun initializeLanguage() {
        val languageCode = userPreferencesRepository.getLanguage()
        setLocale(this, languageCode)
    }
    
    /**
     * Preloads the image caches in the background to speed up image loading later
     */
    private fun preloadImageCaches() {
        applicationScope.launch {
            try {
                Log.d("GymPlannerApp", "Starting image cache preloading")
                imageMatcher.initialize()
                
                // Now that we've tried to load the real images first, generate fallbacks only if needed
                val missingMuscleGroups = checkForMissingMuscleGroupImages()
                if (missingMuscleGroups.isNotEmpty()) {
                    Log.d("GymPlannerApp", "Missing muscle group images for: ${missingMuscleGroups.joinToString()}")
                    initializeFallbackImagesFor(missingMuscleGroups)
                } else {
                    Log.d("GymPlannerApp", "All muscle group images found, fallbacks not needed")
                }
                
                Log.d("GymPlannerApp", "Image cache preloading complete")
            } catch (e: Exception) {
                Log.e("GymPlannerApp", "Failed to preload image caches: ${e.message}")
            }
        }
    }
    
    /**
     * Checks which muscle group images are missing
     */
    private suspend fun checkForMissingMuscleGroupImages(): List<String> {
        val missingGroups = mutableListOf<String>()
        
        // List of muscle groups to check
        val muscleGroups = listOf("CHEST", "BACK", "SHOULDERS", "BICEPS", "TRICEPS", "QUADS", "CALVES", "FOREARMS")
        
        for (muscleGroup in muscleGroups) {
            try {
                // Try to open the expected file
                val filename = when (muscleGroup) {
                    "CALVES" -> "calf.jpg"
                    else -> "${muscleGroup.lowercase()}.jpg"
                }
                
                val imagePath = "body_images/$filename"
                try {
                    assets.open(imagePath).close()
                    Log.d("GymPlannerApp", "Found muscle image: $imagePath")
                } catch (e: Exception) {
                    Log.d("GymPlannerApp", "Missing muscle image: $imagePath")
                    missingGroups.add(muscleGroup)
                }
            } catch (e: Exception) {
                Log.e("GymPlannerApp", "Error checking for $muscleGroup image: ${e.message}")
                missingGroups.add(muscleGroup)
            }
        }
        
        return missingGroups
    }
    
    /**
     * Initialize fallback images only for muscle groups that are missing
     */
    private fun initializeFallbackImagesFor(missingGroups: List<String>) {
        try {
            // Check and create asset directories if needed
            val assetsDir = this.filesDir.resolve("assets")
            if (!assetsDir.exists()) {
                assetsDir.mkdirs()
            }
            
            val bodyImagesDir = assetsDir.resolve("body_images")
            if (!bodyImagesDir.exists()) {
                bodyImagesDir.mkdirs()
            }
            
            // Generate fallback images only for missing groups
            applicationScope.launch {
                try {
                    ExerciseImageFallbackProvider.generateFallbackImagesForMuscleGroups(
                        this@GymPlannerApplication, 
                        missingGroups
                    )
                    Log.d("GymPlannerApp", "Selective fallback image generation complete")
                } catch (e: Exception) {
                    Log.e("GymPlannerApp", "Error generating selective fallback images: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("GymPlannerApp", "Error in initializing selective fallback images: ${e.message}")
        }
    }

    private fun initializeFallbackImages() {
        try {
            // Check and create asset directories if needed
            val assetsDir = this.filesDir.resolve("assets")
            if (!assetsDir.exists()) {
                assetsDir.mkdirs()
            }
            
            val bodyImagesDir = assetsDir.resolve("body_images")
            if (!bodyImagesDir.exists()) {
                bodyImagesDir.mkdirs()
            }
            
            val exerciseImagesDir = assetsDir.resolve("exercise_images")
            if (!exerciseImagesDir.exists()) {
                exerciseImagesDir.mkdirs()
            }
            
            // Generate fallback images
            applicationScope.launch {
                try {
                    ExerciseImageFallbackProvider.generateAndSaveMuscleGroupImages(this@GymPlannerApplication)
                    Log.d("GymPlannerApp", "Fallback image generation complete")
                } catch (e: Exception) {
                    Log.e("GymPlannerApp", "Error generating fallback images: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("GymPlannerApp", "Error in initializing fallback images: ${e.message}")
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    companion object {
        const val UPDATE_WORKER_NAME = "weekly_update_check"
        
        /**
         * Trigger a one-time update check on demand
         */
        fun triggerManualUpdateCheck(context: Context, isDevMode: Boolean = false) {
            // Create a one-time work request that identifies itself as a manual check
            val updateWorkRequest = androidx.work.OneTimeWorkRequestBuilder<com.H_Oussama.gymplanner.workers.UpdateWorker>()
                .addTag("manual_update_check")
                .build()
                
            WorkManager.getInstance(context).enqueue(updateWorkRequest)
        }
        
        fun setLocale(context: Context, languageCode: String) {
            val locale = when (languageCode) {
                "fr" -> Locale("fr")
                "ar" -> Locale("ar")
                else -> Locale("en")
            }
            
            Locale.setDefault(locale)
            
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
    }
} 