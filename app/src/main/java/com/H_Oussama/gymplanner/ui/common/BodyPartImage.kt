package com.H_Oussama.gymplanner.ui.common

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.H_Oussama.gymplanner.data.model.MuscleGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

private const val TAG = "BodyPartImageDebug"

// Cache to avoid reloading the same image multiple times
private val bitmapCache = mutableMapOf<String, android.graphics.Bitmap?>()

@Composable
fun BodyPartImage(
    primaryMuscleGroup: MuscleGroup?,
    secondaryMuscleGroup: MuscleGroup? = null,
    modifier: Modifier = Modifier,
    height: Int = 140,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var primaryBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var secondaryBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Print available assets at the start for debugging
    DisposableEffect(Unit) {
        try {
            val assets = context.assets.list("body_images") ?: emptyArray()
            Log.d(TAG, "Available assets in body_images: ${assets.joinToString()}")
        } catch (e: IOException) {
            Log.e(TAG, "Error listing assets: ${e.message}")
        }
        onDispose { }
    }
    
    // Log what we're trying to load
    Log.d(TAG, "BodyPartImage called with primary: ${primaryMuscleGroup?.name}, " +
            "secondary: ${secondaryMuscleGroup?.name}")
    
    // Direct file mapping for assets (ensure lowercase to avoid case sensitivity issues)
    val muscleToAssetFile = mapOf(
        MuscleGroup.CHEST to "chest.jpg",
        MuscleGroup.BACK to "back.jpg",
        MuscleGroup.SHOULDERS to "shoulders.jpg",
        MuscleGroup.BICEPS to "biceps.jpg",
        MuscleGroup.TRICEPS to "triceps.jpg",
        MuscleGroup.QUADS to "quads.jpg",
        MuscleGroup.CALVES to "calves.jpg",
        MuscleGroup.FOREARMS to "forearms.jpg"
    )
    
    // Load primary muscle group image
    LaunchedEffect(primaryMuscleGroup) {
        if (primaryMuscleGroup == null) {
            Log.d(TAG, "Primary muscle group is null")
            isLoading = false
            return@LaunchedEffect
        }
        
        val fileName = muscleToAssetFile[primaryMuscleGroup]
        if (fileName == null) {
            Log.e(TAG, "No mapping for muscle group: ${primaryMuscleGroup.name}")
            errorMessage = "No image mapping for ${primaryMuscleGroup.name}"
            isLoading = false
            return@LaunchedEffect
        }
        
        // Check cache first
        val cacheKey = "primary_${fileName}"
        if (bitmapCache.containsKey(cacheKey)) {
            Log.d(TAG, "Using cached image for $cacheKey")
            primaryBitmap = bitmapCache[cacheKey]
            
            if (secondaryMuscleGroup == null || secondaryBitmap != null) {
                isLoading = false
            }
            return@LaunchedEffect
        }
        
            withContext(Dispatchers.IO) {
            try {
                val assetPath = "body_images/$fileName"
                Log.d(TAG, "Loading primary from asset path: $assetPath")
                
                context.assets.open(assetPath).use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        Log.d(TAG, "Successfully loaded primary image: $fileName (${bitmap.width}x${bitmap.height})")
                        primaryBitmap = bitmap
                        bitmapCache[cacheKey] = bitmap
                    } else {
                        Log.e(TAG, "Failed to decode bitmap for $fileName")
                        errorMessage = "Failed to decode image: $fileName"
                    }
                    }
                } catch (e: Exception) {
                Log.e(TAG, "Error loading primary image: ${e.javaClass.simpleName}: ${e.message}")
                e.printStackTrace()
                errorMessage = "Error loading image: ${e.message}"
            } finally {
                if (secondaryMuscleGroup == null || secondaryBitmap != null) {
                    isLoading = false
                }
            }
        }
    }
    
    // Load secondary muscle group image
    LaunchedEffect(secondaryMuscleGroup) {
        if (secondaryMuscleGroup == null) {
            Log.d(TAG, "Secondary muscle group is null")
            if (primaryMuscleGroup == null || primaryBitmap != null) {
                isLoading = false
            }
            return@LaunchedEffect
        }
        
        val fileName = muscleToAssetFile[secondaryMuscleGroup]
        if (fileName == null) {
            Log.e(TAG, "No mapping for muscle group: ${secondaryMuscleGroup.name}")
            errorMessage = "No image mapping for ${secondaryMuscleGroup.name}"
            isLoading = false
            return@LaunchedEffect
        }
        
        // Check cache first
        val cacheKey = "secondary_${fileName}"
        if (bitmapCache.containsKey(cacheKey)) {
            Log.d(TAG, "Using cached image for $cacheKey")
            secondaryBitmap = bitmapCache[cacheKey]
            
            if (primaryMuscleGroup == null || primaryBitmap != null) {
                isLoading = false
            }
            return@LaunchedEffect
        }
        
                withContext(Dispatchers.IO) {
            try {
                val assetPath = "body_images/$fileName"
                Log.d(TAG, "Loading secondary from asset path: $assetPath")
                
                context.assets.open(assetPath).use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        Log.d(TAG, "Successfully loaded secondary image: $fileName (${bitmap.width}x${bitmap.height})")
                        secondaryBitmap = bitmap
                        bitmapCache[cacheKey] = bitmap
                    } else {
                        Log.e(TAG, "Failed to decode bitmap for $fileName")
                        errorMessage = "Failed to decode image: $fileName"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading secondary image: ${e.javaClass.simpleName}: ${e.message}")
                e.printStackTrace()
                errorMessage = "Error loading image: ${e.message}"
            } finally {
                if (primaryMuscleGroup == null || primaryBitmap != null) {
                    isLoading = false
                }
            }
        }
    }

    // Display Logic
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
            when {
                isLoading -> {
                // Loading state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Loading...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Error state
            errorMessage != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "Error",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage ?: "Unknown error occurred",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 14.sp
                    )
                }
            }
            // Both primary and secondary available
            primaryBitmap != null && secondaryBitmap != null -> {
                Log.d(TAG, "Displaying split view")
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left half (primary)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    ) {
                        Image(
                            bitmap = primaryBitmap!!.asImageBitmap(),
                            contentDescription = primaryMuscleGroup?.getDisplayName(),
                            contentScale = contentScale,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Primary label
                        Text(
                            text = primaryMuscleGroup?.getDisplayName() ?: "Primary",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(4.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    
                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.7f))
                    )
                    
                    // Right half (secondary)
        Box(
            modifier = Modifier
                            .weight(1f)
                .fillMaxSize()
                    ) {
                        Image(
                            bitmap = secondaryBitmap!!.asImageBitmap(),
                            contentDescription = secondaryMuscleGroup?.getDisplayName(),
                            contentScale = contentScale,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Secondary label
            Text(
                            text = secondaryMuscleGroup?.getDisplayName() ?: "Secondary",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            // Only primary available
            primaryBitmap != null -> {
                Log.d(TAG, "Displaying single view")
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        bitmap = primaryBitmap!!.asImageBitmap(),
                        contentDescription = primaryMuscleGroup?.getDisplayName(),
                        contentScale = contentScale,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Primary label
            Text(
                        text = primaryMuscleGroup?.getDisplayName() ?: "Muscle",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
                }
            }
            // Fallback if all else fails
            else -> {
                Log.w(TAG, "Showing fallback UI")
                MuscleGroupFallback(muscleGroup = primaryMuscleGroup)
            }
        }
    }
}

@Composable
private fun MuscleGroupFallback(
    muscleGroup: MuscleGroup?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter, 
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = muscleGroup?.getDisplayName() ?: "Workout",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Shows an image for a rest day.
 */
@Composable
fun RestDayImage(
    modifier: Modifier = Modifier,
    height: Int = 180
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Rest Day",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 