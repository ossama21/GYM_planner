package com.H_Oussama.gymplanner.ui.common

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.H_Oussama.gymplanner.data.model.MuscleGroup
import com.H_Oussama.gymplanner.util.EnhancedImageMatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A composable that displays a split view of two muscle groups.
 * 
 * @param primaryMuscleGroup The primary muscle group to display
 * @param secondaryMuscleGroup The secondary muscle group to display
 * @param modifier The modifier to apply to the composable
 * @param size The size in dp of the image view
 * @param isSquare Whether to use a square or rounded shape
 */
@Composable
fun SplitMuscleGroupImage(
    primaryMuscleGroup: MuscleGroup?,
    secondaryMuscleGroup: MuscleGroup?,
    modifier: Modifier = Modifier,
    size: Int = 80,
    isSquare: Boolean = false
) {
    val context = LocalContext.current
    val imageMatcher = remember { EnhancedImageMatcher(context) }
    
    var isLoading by remember { mutableStateOf(true) }
    var primaryBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var secondaryBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    // Load the images for both muscle groups
    LaunchedEffect(primaryMuscleGroup, secondaryMuscleGroup) {
        isLoading = true
        
        withContext(Dispatchers.IO) {
            // Load primary image
            primaryBitmap = primaryMuscleGroup?.let { imageMatcher.loadMuscleImage(it.name) }
            
            // Load secondary image (default to ARMS if null)
            secondaryBitmap = secondaryMuscleGroup?.let { imageMatcher.loadMuscleImage(it.name) }
        }
        
        isLoading = false
    }
    
    // Determine shape to use
    val shape = if (isSquare) 
                   RoundedCornerShape(8.dp)
                else 
                   RoundedCornerShape(percent = 12)
    
    // Create the surface to display the images
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
            when {
                isLoading -> {
                    // Show loading indicator
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
                primaryBitmap != null && secondaryBitmap != null -> {
                    // Show both images side by side with a dividing line
                    SplitImageDisplay(
                        primaryImage = primaryBitmap!!,
                        secondaryImage = secondaryBitmap!!,
                        primaryName = primaryMuscleGroup?.getDisplayName() ?: "Primary",
                        secondaryName = secondaryMuscleGroup?.getDisplayName() ?: "Secondary"
                    )
                }
                primaryBitmap != null -> {
                    // Show only primary image
                    Image(
                        bitmap = primaryBitmap!!.asImageBitmap(),
                        contentDescription = primaryMuscleGroup?.getDisplayName(),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    // Show fallback icon
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Muscle groups",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

/**
 * Displays two images side by side with a dividing line.
 */
@Composable
private fun SplitImageDisplay(
    primaryImage: Bitmap,
    secondaryImage: Bitmap,
    primaryName: String,
    secondaryName: String
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Calculate dimensions
            val width = size.width
            val height = size.height
            val halfWidth = width / 2
            
            // Draw primary image on left half
            drawContext.canvas.nativeCanvas.apply {
                val leftRect = android.graphics.Rect(0, 0, halfWidth.toInt(), height.toInt())
                drawBitmap(primaryImage, null, leftRect, null)
            }
            
            // Draw secondary image on right half
            drawContext.canvas.nativeCanvas.apply {
                val rightRect = android.graphics.Rect(halfWidth.toInt(), 0, width.toInt(), height.toInt())
                drawBitmap(secondaryImage, null, rightRect, null)
            }
            
            // Draw dividing line
            val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(halfWidth, 0f),
                end = Offset(halfWidth, height),
                strokeWidth = 2f,
                pathEffect = dashPathEffect,
                blendMode = BlendMode.SrcOver
            )
        }
    }
} 