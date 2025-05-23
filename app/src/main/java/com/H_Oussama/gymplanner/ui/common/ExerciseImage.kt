package com.H_Oussama.gymplanner.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.H_Oussama.gymplanner.R
import com.H_Oussama.gymplanner.utils.ExerciseImageDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

// Cache for findGifAsset results
private val gifAssetCache = mutableMapOf<Pair<String, String>, String?>()

/**
 * Try to determine the exercise type (muscle group) from exercise name
 */
private fun getExerciseType(exerciseName: String): String {
    val name = exerciseName.lowercase().trim()
    
    return when {
        // Chest exercises
        name.contains("chest") || 
        name.contains("bench press") || 
        name.contains("push up") || 
        name.contains("pushup") || 
        name.contains("fly") ||
        name.contains("flye") -> "chest"
        
        // Back exercises
        name.contains("back") || 
        name.contains("lat") || 
        name.contains("row") || 
        name.contains("pull up") || 
        name.contains("pullup") || 
        name.contains("pull-up") || 
        name.contains("pulldown") || 
        name.contains("pull down") -> "back__wing"
        
        // Shoulder exercises
        name.contains("shoulder") || 
        name.contains("delt") || 
        name.contains("overhead press") || 
        name.contains("military press") || 
        name.contains("lateral raise") || 
        name.contains("front raise") || 
        name.contains("rear delt") || 
        name.contains("arnold press") -> "shoulder"
        
        // Biceps exercises
        name.contains("bicep") || 
        name.contains("curl") && !name.contains("leg curl") -> "biceps"
        
        // Triceps exercises
        name.contains("tricep") || 
        name.contains("extension") && !name.contains("leg extension") || 
        name.contains("pushdown") || 
        name.contains("skull crusher") || 
        name.contains("close grip") -> "triceps"
        
        // Leg exercises
        name.contains("leg") || 
        name.contains("squat") || 
        name.contains("lunge") || 
        name.contains("deadlift") || 
        name.contains("rdl") || 
        name.contains("hamstring") || 
        name.contains("glute") -> "leg"
        
        // Calf exercises
        name.contains("calf") || 
        name.contains("calves") -> "calf"
        
        // Abdominal/core exercises
        name.contains("ab") || 
        name.contains("abs") || 
        name.contains("core") || 
        name.contains("crunch") || 
        name.contains("sit up") || 
        name.contains("situp") || 
        name.contains("plank") || 
        name.contains("russian twist") -> "abs__core"
        
        // Forearm exercises
        name.contains("forearm") || 
        name.contains("wrist") || 
        name.contains("grip") -> "forearm"
        
        // Traps exercises
        name.contains("trap") || 
        name.contains("shrug") -> "Trapezius"
        
        // Neck exercises
        name.contains("neck") -> "neck"
        
        // Default to chest if unknown
        else -> "chest"
    }
}

/**
 * Direct mapping for problematic exercises that are hard to match automatically
 */
private val EXERCISE_NAME_TO_GIF_MAP = mapOf(
    // Back exercises
    "barbell_bent_over_row" to "back__wing/barbell_row",
    "bent_over_row" to "back__wing/barbell_row",
    "barbell_row" to "back__wing/barbell_row",
    "one_arm_dumbbell_row" to "back__wing/one_arm_dumbbell_row",
    "seated_cable_row" to "back__wing/cable_seated_row",
    "cable_seated_row" to "back__wing/cable_seated_row",
    "lat_pulldown" to "back__wing/wide_grip_lat_pulldown",
    "wide_grip_lat_pulldown" to "back__wing/wide_grip_lat_pulldown",
    "close_grip_lat_pulldown" to "back__wing/close_grip_lat_pulldown",
    "pull_up" to "back__wing/pull_up",
    "chin_up" to "back__wing/pull_up",
    "pullup" to "back__wing/pull_up",
    "chinup" to "back__wing/pull_up",
    "t_bar_row" to "back__wing/t_bar_row",
    
    // Chest exercises
    "barbell_bench_press" to "chest/barbell_bench_press",
    "bench_press" to "chest/barbell_bench_press", 
    "flat_bench_press" to "chest/barbell_bench_press",
    "incline_bench_press" to "chest/incline_bench_press",
    "incline_barbell_bench_press" to "chest/incline_bench_press",
    "decline_bench_press" to "chest/decline_bench_press",
    "decline_barbell_bench_press" to "chest/decline_bench_press",
    "dumbbell_bench_press" to "chest/dumbbell_bench_press",
    "flat_dumbbell_bench_press" to "chest/dumbbell_bench_press",
    "incline_dumbbell_bench_press" to "chest/incline_dumbbell_bench_press",
    "decline_dumbbell_bench_press" to "chest/decline_dumbbell_bench_press",
    "chest_dip" to "chest/chest_dip",
    "dips_chest_version" to "chest/chest_dip",
    "cable_crossover" to "chest/cable_crossover",
    "dumbbell_fly" to "chest/dumbbell_fly",
    "dumbbell_flye" to "chest/dumbbell_fly",
    
    // Shoulder exercises
    "overhead_press" to "shoulder/overhead_press",
    "military_press" to "shoulder/overhead_press",
    "barbell_overhead_press" to "shoulder/overhead_press",
    "barbell_military_press" to "shoulder/overhead_press",
    "dumbbell_overhead_press" to "shoulder/dumbbell_shoulder_press",
    "dumbbell_shoulder_press" to "shoulder/dumbbell_shoulder_press",
    "seated_dumbbell_shoulder_press" to "shoulder/seated_dumbbell_shoulder_press",
    "seated_dumbbell_press" to "shoulder/seated_dumbbell_shoulder_press",
    "lateral_raise" to "shoulder/dumbbell_lateral_raise",
    "dumbbell_lateral_raise" to "shoulder/dumbbell_lateral_raise",
    "front_raise" to "shoulder/dumbbell_front_raise",
    "dumbbell_front_raise" to "shoulder/dumbbell_front_raise",
    "rear_delt_fly" to "shoulder/reverse_machine_fly",
    "reverse_fly" to "shoulder/reverse_machine_fly",
    "face_pull" to "shoulder/face_pull",
    
    // Biceps exercises
    "barbell_curl" to "biceps/barbell_curl",
    "standing_barbell_curl" to "biceps/barbell_curl",
    "dumbbell_curl" to "biceps/dumbbell_curl",
    "standing_dumbbell_curl" to "biceps/dumbbell_curl",
    "hammer_curl" to "biceps/hammer_curl", 
    "dumbbell_hammer_curl" to "biceps/hammer_curl",
    "preacher_curl" to "biceps/preacher_curl",
    "barbell_preacher_curl" to "biceps/preacher_curl",
    "dumbbell_preacher_curl" to "biceps/dumbbell_preacher_curl",
    "concentration_curl" to "biceps/concentration_curl",
    "cable_curl" to "biceps/cable_curl",
    "ez_bar_curl" to "biceps/ez_bar_curl",
    
    // Triceps exercises
    "tricep_pushdown" to "triceps/pushdown",
    "cable_pushdown" to "triceps/pushdown",
    "rope_pushdown" to "triceps/rope_pushdown",
    "tricep_extension" to "triceps/lying_triceps_extension",
    "lying_tricep_extension" to "triceps/lying_triceps_extension", 
    "skull_crusher" to "triceps/lying_triceps_extension",
    "overhead_tricep_extension" to "triceps/overhead_triceps_extension",
    "dumbbell_overhead_tricep_extension" to "triceps/dumbbell_overhead_tricep_extension",
    "close_grip_bench_press" to "triceps/close_grip_bench_press",
    "tricep_dip" to "triceps/bench_dip",
    "bench_dip" to "triceps/bench_dip",
    "dips_triceps_version" to "triceps/bench_dip",
    
    // Leg exercises
    "barbell_squat" to "leg/barbell_squat",
    "back_squat" to "leg/barbell_squat",
    "front_squat" to "leg/front_squat",
    "leg_press" to "leg/leg_press",
    "leg_extension" to "leg/leg_extension",
    "leg_curl" to "leg/leg_curl", 
    "hamstring_curl" to "leg/leg_curl",
    "lying_leg_curl" to "leg/lying_leg_curl",
    "seated_leg_curl" to "leg/seated_leg_curl",
    "romanian_deadlift" to "leg/romanian_deadlift", 
    "rdl" to "leg/romanian_deadlift",
    "stiff_leg_deadlift" to "leg/romanian_deadlift",
    "deadlift" to "leg/deadlift",
    "barbell_deadlift" to "leg/deadlift",
    "conventional_deadlift" to "leg/deadlift",
    "lunge" to "leg/lunge",
    "walking_lunge" to "leg/walking_lunge",
    "bulgarian_split_squat" to "leg/bulgarian_split_squat",
    "calf_raise" to "calf/standing_calf_raise", 
    "standing_calf_raise" to "calf/standing_calf_raise",
    "seated_calf_raise" to "calf/seated_calf_raise",
    
    // Ab/Core exercises
    "crunch" to "abs__core/crunch",
    "sit_up" to "abs__core/sit_up",
    "leg_raise" to "abs__core/lying_leg_raise",
    "lying_leg_raise" to "abs__core/lying_leg_raise",
    "hanging_leg_raise" to "abs__core/hanging_leg_raise",
    "plank" to "abs__core/plank",
    "side_plank" to "abs__core/side_plank",
    "russian_twist" to "abs__core/russian_twist",
    "ab_wheel" to "abs__core/ab_wheel_rollout",
    "ab_rollout" to "abs__core/ab_wheel_rollout"
)

/**
 * Try to find a matching GIF file by trying multiple filename variations
 */
private suspend fun findGifAsset(context: Context, exerciseName: String, exerciseType: String): String? {
    val originalName = exerciseName.lowercase().trim()
    val normalizedExerciseType = exerciseType.lowercase().trim()
    val cacheKey = Pair(originalName, normalizedExerciseType)

    // Check cache first
    if (gifAssetCache.containsKey(cacheKey)) {
        val cachedPath = gifAssetCache[cacheKey]
        if (cachedPath != null) {
            Log.d("ExerciseImage", "Found GIF in cache: $cachedPath for '$exerciseName'")
        } else {
            Log.d("ExerciseImage", "Cache hit: No GIF found for '$exerciseName'")
        }
        return cachedPath
    }

    return withContext(Dispatchers.IO) {
        val normalizedName = originalName.replace(" ", "_").replace("-", "_")
        
        Log.d("ExerciseImage", "Looking for GIF for exercise: '$exerciseName', normalized: '$normalizedName', type: '$normalizedExerciseType'")
        
        // Special handling for specific known problematic exercises
        val specialCases = mapOf(
            "barbell_bent_over_row" to "exercices-gifs/back__wing_gifs/barbell_row.gif",
            "bent_over_row" to "exercices-gifs/back__wing_gifs/barbell_row.gif",
            "barbell row" to "exercices-gifs/back__wing_gifs/barbell_row.gif"
        )
        
        // Check special cases first
        for ((pattern, path) in specialCases) {
            if (normalizedName.contains(pattern) || pattern.contains(normalizedName)) {
                try {
                    context.assets.open(path).use {
                        Log.d("ExerciseImage", "Found match using special case: $pattern -> $path")
                        it.close()
                        gifAssetCache[cacheKey] = path // Cache result
                        return@withContext path
                    }
                } catch (e: IOException) {
                    Log.d("ExerciseImage", "Special case path not found: $path")
                }
            }
        }
        
        // Check direct mapping next - this is our best chance for a correct match
        val directMapping = EXERCISE_NAME_TO_GIF_MAP.entries.firstOrNull { (key, _) ->
            normalizedName == key || 
            normalizedName.contains(key) || 
            key.contains(normalizedName) ||
            normalizedName.replace("_", "") == key.replace("_", "") ||
            normalizedName.replace("_", "") == key ||
            key.replace("_", "") == normalizedName
        }?.value
        
        if (directMapping != null) {
            val directPath = "exercices-gifs/${directMapping}.gif"
            try {
                context.assets.open(directPath).use {
                    Log.d("ExerciseImage", "Found direct mapped GIF: $directPath for $exerciseName")
                    it.close()
                    gifAssetCache[cacheKey] = directPath // Cache result
                    return@withContext directPath
                }
            } catch (e: IOException) {
                Log.d("ExerciseImage", "Direct mapping found but file not found: $directPath")
            }
        }
        
        // Generate more name variations to try
        val nameVariations = generateNameVariations(normalizedName)
        
        // Directories to search in priority order
        val directories = listOf(
            // First try the detected muscle group directory
            "exercices-gifs/${normalizedExerciseType}_gifs",
            // Then try all other muscle group directories
            "exercices-gifs/chest_gifs",
            "exercices-gifs/back__wing_gifs",
            "exercices-gifs/shoulder_gifs",
            "exercices-gifs/biceps_gifs",
            "exercices-gifs/triceps_gifs",
            "exercices-gifs/leg_gifs",
            "exercices-gifs/calf_gifs",
            "exercices-gifs/abs__core_gifs",
            "exercices-gifs/forearm_gifs",
            "exercices-gifs/Trapezius_gifs",
            "exercices-gifs/erector_spinae_gifs",
            "exercices-gifs/neck_gifs",
            // Finally try the root directory
            "exercices-gifs"
        )
        
        // First, list all GIF files in the asset directories for better matching
        val allAvailableGifs = mutableListOf<Pair<String, String>>() // path, filename
        
        for (dir in directories) {
            try {
                val files = context.assets.list(dir) ?: emptyArray()
                for (file in files) {
                    if (file.endsWith(".gif")) {
                        allAvailableGifs.add(Pair("$dir/$file", file.removeSuffix(".gif")))
                    }
                }
            } catch (e: IOException) {
                // Directory might not exist, just continue
            }
        }
        
        // Log all available gifs for debugging
        // Log.d("ExerciseImage", "Available GIFs: ${allAvailableGifs.size}") // Consider removing for prod
        
        // Try to find best match based on name similarities
        for (variation in nameVariations) {
            // Try exact match first
            for ((path, filename) in allAvailableGifs) {
                if (variation == filename || 
                    variation.replace("_", "") == filename.replace("_", "")) {
                    try {
                        context.assets.open(path).use {
                            Log.d("ExerciseImage", "Found exact match: $path for variation: $variation")
                            it.close()
                            gifAssetCache[cacheKey] = path // Cache result
                            return@withContext path
                        }
                    } catch (e: IOException) { /* Continue to next */ }
                }
            }
            
            // Then try partial matches
            for ((path, filename) in allAvailableGifs) {
                if (variation.contains(filename) || 
                    filename.contains(variation) ||
                    variation.replace("_", "").contains(filename.replace("_", "")) ||
                    filename.replace("_", "").contains(variation.replace("_", ""))) {
                    try {
                        context.assets.open(path).use {
                            Log.d("ExerciseImage", "Found partial match: $path for variation: $variation")
                            it.close()
                            gifAssetCache[cacheKey] = path // Cache result
                            return@withContext path
                        }
                    } catch (e: IOException) { /* Continue to next */ }
                }
            }
        }
        
        // If all else fails, try systematic search in all directories
        for (dir in directories) {
            for (variation in nameVariations) {
                val path = "$dir/$variation.gif"
                try {
                    context.assets.open(path).use {
                        Log.d("ExerciseImage", "Found GIF using systematic search: $path")
                        it.close()
                        gifAssetCache[cacheKey] = path // Cache result
                        return@withContext path
                    }
                } catch (e: IOException) { /* Continue to next */ }
            }
        }
        
        // Systematic search failed, try partial matches by listing directories
        for (dir in directories) {
            try {
                val files = context.assets.list(dir) ?: emptyArray()
                for (file in files) {
                    if (!file.endsWith(".gif")) continue
                    
                    // Remove .gif from filename for matching
                    val fileName = file.removeSuffix(".gif")
                    
                    // Check if any name variation matches part of the filename
                    for (variation in nameVariations) {
                        if (fileName.contains(variation) || 
                            variation.contains(fileName) ||
                            // Try without underscores too
                            fileName.replace("_", "").contains(variation.replace("_", "")) ||
                            variation.replace("_", "").contains(fileName.replace("_", ""))) {
                            
                            val path = "$dir/$file"
                            try {
                                context.assets.open(path).use {
                                    Log.d("ExerciseImage", "Found fuzzy match: $path for variation: $variation")
                                    it.close()
                                    gifAssetCache[cacheKey] = path // Cache result
                                    return@withContext path
                                }
                            } catch (e: IOException) { /* Continue to next */ }
                        }
                    }
                }
            } catch (e: IOException) {
                // Directory might not exist, just continue
            }
        }
        
        Log.e("ExerciseImage", "Failed to find GIF after trying all variations for: $exerciseName")
        gifAssetCache[cacheKey] = null // Cache null result
        null
    }
}

/**
 * Generate multiple name variations for better matching
 */
private fun generateNameVariations(name: String): List<String> {
    val variations = mutableSetOf<String>()
    
    // Add the original name
    variations.add(name)
    
    // Add with underscores replaced by spaces and vice versa
    variations.add(name.replace("_", " "))
    variations.add(name.replace(" ", "_"))
    
    // Add without special characters
    variations.add(name.replace(Regex("[^a-z0-9_]"), ""))
    
    // Common word replacements to try
    val replacements = mapOf(
        "barbell" to "bb",
        "dumbbell" to "db",
        "_press" to "_bench_press",
        "bent_over" to "bentover",
        "pull_up" to "pullup",
        "pull-up" to "pullup",
        "extension" to "ext",
        "raise" to "raises",
        "curl" to "curls",
        "press" to "presses",
        "squat" to "squats",
        "lunge" to "lunges",
        "deadlift" to "deadlifts",
        "dip" to "dips"
    )
    
    // Add variations with common replacements
    val parts = name.split("_")
    for ((old, new) in replacements) {
        if (name.contains(old)) {
            variations.add(name.replace(old, new))
        }
    }
    
    // Add variations with singular/plural forms
    if (name.endsWith("s")) {
        variations.add(name.dropLast(1))
    } else {
        variations.add("${name}s")
    }
    
    // Add without prefixes like "barbell_" or "dumbbell_"
    for (prefix in listOf("barbell_", "dumbbell_", "cable_", "machine_")) {
        if (name.startsWith(prefix)) {
            variations.add(name.removePrefix(prefix))
        }
    }
    
    // Try common abbreviations
    if (name.contains("barbell")) {
        variations.add(name.replace("barbell", "bb"))
    }
    if (name.contains("dumbbell")) {
        variations.add(name.replace("dumbbell", "db"))
    }
    
    // For exercises with multiple words, try each word individually
    val words = name.split("_")
    if (words.size > 1) {
        variations.add(words.last()) // Main movement (e.g., "curl" from "dumbbell_curl")
        if (words.size > 2) {
            variations.add("${words[words.size-2]}_${words.last()}") // Last two words
        }
    }
    
    return variations.toList()
}

/**
 * Displays an exercise image with ability to search for images if none is available
 */
@Composable
fun ExerciseImage(
    exerciseName: String,
    modifier: Modifier = Modifier,
    imageIdentifier: String? = null,
    exerciseType: String? = null,
    onSearchClick: () -> Unit = {},
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit
) {
    val context = LocalContext.current
    var gifPath by remember { mutableStateOf<String?>(null) }
    var showNotFoundFallback by remember { mutableStateOf(false) }
    
    // Determine exercise type if not provided
    val effectiveExerciseType = remember(exerciseType, exerciseName, imageIdentifier) {
        exerciseType ?: getExerciseType(imageIdentifier ?: exerciseName)
    }
    
    // Find GIF path on composition
    LaunchedEffect(exerciseName, imageIdentifier, effectiveExerciseType) {
        val nameToUse = imageIdentifier ?: exerciseName
        Log.d("ExerciseImage", "Looking up GIF for: '$nameToUse' (type: $effectiveExerciseType)")
        
        val path = findGifAsset(context, nameToUse, effectiveExerciseType)
        if (path == null) {
            Log.e("ExerciseImage", "⚠️ NO MATCH FOUND for: '$nameToUse' (type: $effectiveExerciseType)")
            showNotFoundFallback = true
        } else {
            Log.d("ExerciseImage", "✅ Found GIF: $path for '$nameToUse'")
            showNotFoundFallback = false
        }
        gifPath = path
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (showNotFoundFallback) {
            // Show fallback when no GIF is found
            FallbackExerciseImage(
                exerciseName = imageIdentifier ?: exerciseName,
                exerciseType = effectiveExerciseType,
                modifier = modifier
            )
            
            // Show search button if handler provided
            if (onSearchClick != {}) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onSearchClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search for image",
                        tint = androidx.compose.ui.graphics.Color.White
                    )
                }
            }
        } else {
            // Use GifImage when we have a valid path
            gifPath?.let { path ->
                GifImage(
                    context = context,
                    assetPath = path,
                    modifier = modifier,
                    contentDescription = contentDescription ?: exerciseName,
                    contentScale = contentScale
                )
            } ?: CircularProgressIndicator()  // Show loading while path is being determined
        }
    }
}

/**
 * Fallback display when no GIF can be found for an exercise
 */
@Composable
private fun FallbackExerciseImage(
    exerciseName: String,
    exerciseType: String,
    modifier: Modifier = Modifier
) {
    val muscleGroupIcon = when (exerciseType.lowercase()) {
        "chest" -> Icons.Default.FitnessCenter
        "back", "back__wing" -> Icons.Default.FitnessCenter
        "shoulder" -> Icons.Default.FitnessCenter
        "biceps" -> Icons.Default.FitnessCenter
        "triceps" -> Icons.Default.FitnessCenter
        "leg" -> Icons.Default.FitnessCenter
        "calf" -> Icons.Default.FitnessCenter
        "abs__core", "abs", "core" -> Icons.Default.FitnessCenter
        "forearm" -> Icons.Default.FitnessCenter
        "trapezius" -> Icons.Default.FitnessCenter
        "neck" -> Icons.Default.FitnessCenter
        else -> Icons.Default.FitnessCenter
    }
    
    Column(
        modifier = modifier
            .padding(16.dp)
            .background(androidx.compose.ui.graphics.Color.LightGray, RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = muscleGroupIcon,
            contentDescription = exerciseName,
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 8.dp),
            tint = androidx.compose.ui.graphics.Color.DarkGray
        )
        Text(
            text = exerciseName.replace("_", " ")
                .split(" ")
                .joinToString(" ") { word ->
                    word.replaceFirstChar { 
                        if (it.isLowerCase()) it.uppercaseChar() else it 
                    }
                },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = androidx.compose.ui.graphics.Color.DarkGray
        )
    }
}

// String extension function to capitalize first letter of each word
fun String.capitalize(locale: Locale): String {
    return this.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }
}

/**
 * Displays a GIF image from an asset path
 */
@Composable
fun GifImage(
    context: Context,
    assetPath: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit
) {
    val gifUri = remember(assetPath) {
        val fullPath = "file:///android_asset/$assetPath"
        Uri.parse(fullPath)
    }
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(gifUri)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
        onError = { 
            Log.e("GifImage", "Error loading GIF: $assetPath", it.result.throwable)
        }
    )
}