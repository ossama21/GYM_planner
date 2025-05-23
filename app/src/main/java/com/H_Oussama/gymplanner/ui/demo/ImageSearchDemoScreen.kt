package com.H_Oussama.gymplanner.ui.demo

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.H_Oussama.gymplanner.data.model.ExerciseDefinition
import com.H_Oussama.gymplanner.ui.common.ExerciseImage
import com.H_Oussama.gymplanner.util.ImageSearchResult
import com.H_Oussama.gymplanner.util.downloadAndSaveExerciseImage
import com.H_Oussama.gymplanner.util.searchExerciseImages
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImageSearchDemoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ImageSearchDemoUiState())
    val uiState: StateFlow<ImageSearchDemoUiState> = _uiState.asStateFlow()
    
    // Demo exercises
    private val demoExercises = listOf(
        ExerciseDefinition(
            id = "1",
            name = "Bench Press",
            description = "Chest exercise",
            imageIdentifier = "", // Empty to trigger search
            met = 3.5
        ),
        ExerciseDefinition(
            id = "2",
            name = "Squat",
            description = "Leg exercise",
            imageIdentifier = "", // Empty to trigger search
            met = 4.0
        ),
        ExerciseDefinition(
            id = "3",
            name = "Deadlift",
            description = "Back exercise",
            imageIdentifier = "", // Empty to trigger search
            met = 4.5
        )
    )
    
    init {
        _uiState.value = _uiState.value.copy(
            exercises = demoExercises
        )
    }
    
    fun searchForExerciseImage(exerciseId: String) {
        val exercise = demoExercises.find { it.id == exerciseId } ?: return
        
        _uiState.value = _uiState.value.copy(
            selectedExerciseId = exerciseId,
            isSearching = true,
            searchResults = emptyList()
        )
        
        viewModelScope.launch {
            try {
                val results = searchExerciseImages(exercise.name)
                _uiState.value = _uiState.value.copy(
                    searchResults = results,
                    isSearching = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to search for images: ${e.message}",
                    isSearching = false
                )
            }
        }
    }
    
    fun saveSelectedImage(context: android.content.Context, imageUrl: String) {
        val exerciseId = _uiState.value.selectedExerciseId ?: return
        val exercise = demoExercises.find { it.id == exerciseId } ?: return
        
        viewModelScope.launch {
            try {
                val savedImageName = downloadAndSaveExerciseImage(context, imageUrl, exercise.name)
                
                if (savedImageName != null) {
                    // Create a new exercise with the updated image identifier
                    val updatedExercise = exercise.copy(imageIdentifier = savedImageName)
                    
                    // Update the exercise in our local list
                    val updatedExercises = _uiState.value.exercises.map { 
                        if (it.id == exercise.id) updatedExercise else it 
                    }
                    
                    // Update the UI state
                    _uiState.value = _uiState.value.copy(
                        exercises = updatedExercises,
                        searchResults = emptyList(),
                        selectedExerciseId = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to save image",
                        selectedExerciseId = null,
                        searchResults = emptyList()
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save image: ${e.message}",
                    selectedExerciseId = null,
                    searchResults = emptyList()
                )
            }
        }
    }
    
    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            selectedExerciseId = null,
            searchResults = emptyList(),
            isSearching = false
        )
    }
}

data class ImageSearchDemoUiState(
    val exercises: List<ExerciseDefinition> = emptyList(),
    val searchResults: List<ImageSearchResult> = emptyList(),
    val selectedExerciseId: String? = null,
    val isSearching: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSearchDemoScreen() {
    val viewModel: ImageSearchDemoViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Image Search Demo") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Show exercises
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.exercises, key = { it.id }) { exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        onSearchClicked = {
                            viewModel.searchForExerciseImage(exercise.id)
                        }
                    )
                }
            }
            
            // Show error
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(error)
                }
            }
            
            // Show search results dialog
            if (uiState.selectedExerciseId != null) {
                ImageSearchDialog(
                    searchResults = uiState.searchResults,
                    isSearching = uiState.isSearching,
                    onImageSelected = { imageUrl ->
                        viewModel.saveSelectedImage(context, imageUrl)
                    },
                    onSearchAgain = {
                        viewModel.searchForExerciseImage(uiState.selectedExerciseId!!)
                    },
                    onDismiss = {
                        viewModel.clearSearch()
                    }
                )
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: ExerciseDefinition,
    onSearchClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = exercise.description ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                ExerciseImage(
                    exerciseName = exercise.name,
                    imageIdentifier = exercise.imageIdentifier,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    onSearchClick = onSearchClicked
                )
            }
        }
    }
}

@Composable
fun ImageSearchDialog(
    searchResults: List<ImageSearchResult>,
    isSearching: Boolean,
    onImageSelected: (String) -> Unit,
    onSearchAgain: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Search Results") },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (isSearching) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (searchResults.isEmpty()) {
                    Text("No images found. Try searching again.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults) { result ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onImageSelected(result.originalUrl) }
                            ) {
                                Column {
                                    AsyncImage(
                                        model = result.thumbnailUrl,
                                        contentDescription = result.title,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    Text(
                                        text = result.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSearchAgain,
                enabled = !isSearching
            ) {
                Text("Search Again")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 