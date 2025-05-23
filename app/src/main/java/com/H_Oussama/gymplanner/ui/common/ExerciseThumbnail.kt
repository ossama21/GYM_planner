package com.H_Oussama.gymplanner.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.H_Oussama.gymplanner.data.model.ExerciseDefinition
import com.H_Oussama.gymplanner.data.model.MuscleGroup
import com.H_Oussama.gymplanner.data.model.primaryMuscleGroup
import com.H_Oussama.gymplanner.util.EnhancedImageMatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A composable that displays a thumbnail image for an exercise.
 * It attempts to load the exercise image from assets and provides fallback options.
 * Uses the EnhancedImageMatcher for better image matching.
 *
 * @param exerciseDefinition The exercise definition containing the image identifier
 * @param modifier The modifier to apply to the thumbnail
 * @param size The size of the thumbnail (default: 80.dp)
 * @param isCompleted Whether to show a completion indicator (default: false)
 * @param isSquare Whether to use square or round shape (default: false - uses rounded corners)
 */
@Composable
fun ExerciseThumbnail(
    exerciseDefinition: ExerciseDefinition,
    modifier: Modifier = Modifier,
    size: Int = 80,
    isCompleted: Boolean = false,
    isSquare: Boolean = false
) {
    val context = LocalContext.current
    val imageMatcher = remember { EnhancedImageMatcher(context) }
    
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    
    // Get the primary muscle group for potential fallback
    val muscleGroup = exerciseDefinition.primaryMuscleGroup
    
    // Attempt to load the image with enhanced matcher
    LaunchedEffect(exerciseDefinition) {
        isLoading = true
        hasError = false
        bitmap = null
        
        try {
            // First try to find the best image based on the exercise name
            val bestImageId = imageMatcher.findBestExerciseImage(exerciseDefinition.name)
            
            // If that fails, try using the imageIdentifier as fallback
            val imageToLoad = bestImageId ?: exerciseDefinition.imageIdentifier
            
            if (!imageToLoad.isNullOrBlank()) {
                bitmap = imageMatcher.loadExerciseImage(imageToLoad)
            }
            
            hasError = bitmap == null
        } catch (e: Exception) {
            Log.e("ExerciseThumbnail", "Error loading image: ${e.message}")
            hasError = true
        } finally {
            isLoading = false
        }
    }
    
    // Determine the shape to use
    val shape = if (isSquare) 
                   RoundedCornerShape(8.dp)
                else 
                   RoundedCornerShape(percent = 12)
    
    // Create the thumbnail
    Surface(
        modifier = modifier
            .size(size.dp)
            .clip(shape),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Content based on loading state
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
                bitmap != null -> {
                    // Show loaded image
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = exerciseDefinition.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                hasError -> {
                    // Show fallback based on muscle group
                    MuscleGroupIcon(imageMatcher, muscleGroup)
                }
            }
            
            // Show the completion indicator if needed
            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * A simplified version of ExerciseThumbnail that only shows images for a muscle group.
 */
@Composable
fun MuscleGroupThumbnail(
    muscleGroup: MuscleGroup?,
    modifier: Modifier = Modifier,
    size: Int = 60,
    isSquare: Boolean = false
) {
    val context = LocalContext.current
    val imageMatcher = remember { EnhancedImageMatcher(context) }
    
    val shape = if (isSquare) 
                   RoundedCornerShape(8.dp)
                else 
                   RoundedCornerShape(percent = 12)
                   
    Surface(
        modifier = modifier
            .size(size.dp)
            .clip(shape),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            MuscleGroupIcon(imageMatcher, muscleGroup)
        }
    }
}

/**
 * Helper composable to display an icon for a muscle group.
 */
@Composable
private fun MuscleGroupIcon(
    imageMatcher: EnhancedImageMatcher,
    muscleGroup: MuscleGroup?
) {
    var bitmap by remember(muscleGroup) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Try to load the muscle group image
    LaunchedEffect(muscleGroup) {
        isLoading = true
        if (muscleGroup == null) {
            isLoading = false
            return@LaunchedEffect
        }
        
        try {
            bitmap = imageMatcher.loadMuscleImage(muscleGroup.name)
        } catch (e: Exception) {
            Log.e("MuscleGroupIcon", "Error: ${e.message}")
        } finally {
            isLoading = false
        }
    }
    
    when {
        isLoading -> {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
        bitmap != null -> {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = muscleGroup?.getDisplayName() ?: "Muscle group",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        else -> {
            // Show default exercise icon
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = "Exercise",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
} 