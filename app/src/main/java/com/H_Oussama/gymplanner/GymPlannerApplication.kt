package com.H_Oussama.gymplanner

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.lifecycle.lifecycleScope
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

// Keep the class, but remove repository instances for now.
// We'll add Hilt annotations here soon.
@HiltAndroidApp
class GymPlannerApplication : Application(), coil.ImageLoaderFactory {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    // Application scope for coroutines
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Create an instance of our enhanced image matcher
    private lateinit var imageMatcher: EnhancedImageMatcher

    override fun onCreate() {
        super.onCreate()
        // Hilt setup happens automatically
        
        // Initialize the language based on saved preference
        initializeLanguage()
        
        // Initialize the image matcher
        imageMatcher = EnhancedImageMatcher(this)
        
        // Preload image caches in the background first
        preloadImageCaches()
        
        // Initialize fallback images only if needed (after image caches are loaded)
        // initializeFallbackImages()  // Removing this direct call
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