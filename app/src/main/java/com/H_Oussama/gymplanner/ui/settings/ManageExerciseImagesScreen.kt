package com.H_Oussama.gymplanner.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import com.H_Oussama.gymplanner.R
import com.H_Oussama.gymplanner.ui.common.TransparentTopBar
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Manage Exercise Images screen.
 */
@HiltViewModel
class ManageExerciseImagesViewModel @Inject constructor() : ViewModel() {
    
    // State for exercise categories
    private val _exerciseCategories = MutableStateFlow(
        listOf(
            ExerciseCategory("Chest", "0 images", "chest.jpg"),
            ExerciseCategory("Back", "0 images", "back.jpg"),
            ExerciseCategory("Legs", "0 images", "quads.jpg"),
            ExerciseCategory("Shoulders", "0 images", "shoulders.jpg"),
            ExerciseCategory("Arms", "0 images", "biceps.jpg"),
            ExerciseCategory("Core", "0 images", "abs.jpg")
        )
    )
    val exerciseCategories: StateFlow<List<ExerciseCategory>> = _exerciseCategories.asStateFlow()
    
    // Save image to app's exercise_images directory
    suspend fun saveExerciseImage(context: Context, uri: Uri, muscleGroup: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Create exercise_images directory if it doesn't exist
                val exerciseImagesDir = File(context.filesDir, "assets/exercise_images")
                if (!exerciseImagesDir.exists()) {
                    exerciseImagesDir.mkdirs()
                }
                
                // Generate a unique filename based on muscle group and timestamp
                val timestamp = System.currentTimeMillis()
                val filename = "${muscleGroup.lowercase()}_exercise_$timestamp.jpg"
                val destFile = File(exerciseImagesDir, filename)
                
                // Copy the image file
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                
                // Update category count after successful save
                updateCategoryCount(muscleGroup)
                
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    
    // Update the image count for a specific category
    private fun updateCategoryCount(muscleGroup: String) {
        viewModelScope.launch {
            val updatedCategories = _exerciseCategories.value.map { category ->
                if (category.name.equals(muscleGroup, ignoreCase = true)) {
                    // Extract current count
                    val currentCountText = category.count
                    val currentCount = currentCountText.split(" ")[0].toIntOrNull() ?: 0
                    val newCount = currentCount + 1
                    
                    // Create updated category
                    category.copy(count = "$newCount images")
                } else {
                    category
                }
            }
            _exerciseCategories.value = updatedCategories
        }
    }
}

data class ExerciseCategory(
    val name: String,
    val count: String,
    val imageFileName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageExerciseImagesScreen(
    viewModel: ManageExerciseImagesViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onCategoryClick: (categoryName: String) -> Unit
) {
    val exerciseCategories by viewModel.exerciseCategories.collectAsState()
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Transparent top bar
        TransparentTopBar(
            title = "Manage Exercise Images",
            onBackClick = onBackClick
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp) // Add some space between top bar and content
        ) {
            // Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Enhance Your Exercise Library",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Manage visual references for your exercises to improve your form and workout experience. Select a muscle group below to view and edit images.",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Categories Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(exerciseCategories) { category ->
                    ExerciseCategoryCard(
                        category = category,
                        onClick = { onCategoryClick(category.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseCategoryCard(
    category: ExerciseCategory,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val imagePath = "file:///android_asset/body_images/${category.imageFileName}"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Category image
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(imagePath)
                        .build()
                ),
                contentDescription = "${category.name} exercises",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Category name and count
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)) 
                    .padding(8.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.Start 
            ) {
                Text(
                    text = category.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = category.count, // This count might need to be updated or rethought
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}


// Permission handling (if needed for new image picking)
@Composable
fun RequestStoragePermission(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            Toast.makeText(context, "Storage permission is required to select images.", Toast.LENGTH_LONG).show()
            onPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}

// This screen will be called by the settings screen.
// We need to update SettingsScreen.kt to navigate to ManageExerciseImagesScreen
// and also update GymApp.kt to include this new screen in the NavHost. 