package com.H_Oussama.gymplanner.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.H_Oussama.gymplanner.R
import com.H_Oussama.gymplanner.data.model.ExerciseDefinition
import com.H_Oussama.gymplanner.ui.common.ExerciseImage
import com.H_Oussama.gymplanner.ui.common.TransparentTopBar
import com.H_Oussama.gymplanner.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.H_Oussama.gymplanner.data.repositories.ExerciseDefinitionRepository
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import android.net.Uri
import com.H_Oussama.gymplanner.util.ImageManager
import com.H_Oussama.gymplanner.util.ImageSearchResult
import com.H_Oussama.gymplanner.util.searchExerciseImages
import com.H_Oussama.gymplanner.util.downloadAndSaveExerciseImage
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import coil.compose.AsyncImage

data class ExercisesByBodyPartUiState(
    val exercises: List<ExerciseDefinition> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val categoryName: String = "",
    val availableImages: List<String> = emptyList(),
    val searchResults: List<ImageSearchResult> = emptyList(),
    val isSearching: Boolean = false
)

@HiltViewModel
class ExercisesByBodyPartViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseDefinitionRepository,
    private val imageManager: ImageManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExercisesByBodyPartUiState())
    val uiState: StateFlow<ExercisesByBodyPartUiState> = _uiState.asStateFlow()
    
    // Add state for selected image URI
    private val _selectedImageUri = MutableStateFlow<String?>(null)
    val selectedImageUri: StateFlow<String?> = _selectedImageUri.asStateFlow()

    private val categoryName: String = savedStateHandle.get<String>(Routes.CATEGORY_NAME_ARG)?.replace("+", " ") ?: ""
    
    init {
        _uiState.value = _uiState.value.copy(categoryName = categoryName)
        loadExercisesForCategory(categoryName) // Load with decoded name
        loadAvailableImages() // Load available images
    }

    fun loadExercisesForCategory(categoryName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                exerciseRepository.getDefinitionsByMuscleGroup(categoryName)
                    .collect { exercises ->
                        _uiState.value = _uiState.value.copy(
                            exercises = exercises,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }
    
    fun loadAvailableImages() {
        viewModelScope.launch {
            try {
                val imageFiles = imageManager.getAllAvailableImages()
                val imageNames = imageFiles.map { it.nameWithoutExtension }
                _uiState.value = _uiState.value.copy(availableImages = imageNames)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to load images: ${e.message}")
            }
        }
    }
    
    fun setSelectedImageUri(uri: String) {
        _selectedImageUri.value = uri
    }
    
    fun saveSelectedImageWithName(exerciseId: String, imageName: String) {
        viewModelScope.launch {
            val uri = _selectedImageUri.value ?: return@launch
            try {
                // Use ImageManager to save the image and get the new name
                val savedImageName = imageManager.saveImageFromUri(Uri.parse(uri), imageName)
                
                // Update exercise with new image identifier
                updateExerciseImage(exerciseId, savedImageName)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to save image: ${e.message}")
            }
        }
    }
    
    fun updateExerciseImage(exerciseId: String, newImageIdentifier: String) {
        viewModelScope.launch {
            try {
                // Get the exercise definition
                val exerciseDefinition = exerciseRepository.getDefinitionByIdOnce(exerciseId) ?: return@launch
                
                // Create updated copy with new image identifier
                val updatedDefinition = exerciseDefinition.copy(imageIdentifier = newImageIdentifier)
                
                // Save to repository
                exerciseRepository.insertDefinition(updatedDefinition)
                
                // Refresh the list to show the updated image
                loadExercisesForCategory(categoryName)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to update image: ${e.message}")
            }
        }
    }
    
    fun createNewExercise(exerciseName: String, imageIdentifier: String?) {
        viewModelScope.launch {
            if (exerciseName.isBlank()) {
                _uiState.value = _uiState.value.copy(error = "Exercise name cannot be empty")
                return@launch
            }
            
            try {
                // Create a unique ID for the exercise
                val exerciseId = java.util.UUID.randomUUID().toString()
                
                // Create the new exercise definition
                val newExercise = ExerciseDefinition(
                    id = exerciseId,
                    name = exerciseName,
                    description = null, // No description initially
                    imageIdentifier = imageIdentifier ?: "",
                    met = 3.5 // Default MET value
                )
                
                // Insert into repository
                exerciseRepository.insertDefinition(newExercise)
                
                // Reload exercises to show the new one
                loadExercisesForCategory(categoryName)
                
                // Clear selected image
                _selectedImageUri.value = null
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to create exercise: ${e.message}")
            }
        }
    }
    
    suspend fun saveSelectedImageForNewExercise(imageName: String): String? {
        val uri = _selectedImageUri.value ?: return null
        
        return try {
            // Use ImageManager to save the image and get the new name
            imageManager.saveImageFromUri(Uri.parse(uri), imageName)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = "Failed to save image: ${e.message}")
            null
        }
    }

    fun searchExerciseImage(exerciseName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, error = null)
            try {
                val results = searchExerciseImages(exerciseName)
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

    fun saveSearchedImage(exerciseId: String, imageUrl: String) {
        viewModelScope.launch {
            try {
                val exercise = exerciseRepository.getDefinitionByIdOnce(exerciseId) ?: return@launch
                val savedImageName = downloadAndSaveExerciseImage(context, imageUrl, exercise.name)
                
                if (savedImageName != null) {
                    updateExerciseImage(exerciseId, savedImageName)
                    _uiState.value = _uiState.value.copy(searchResults = emptyList())
                } else {
                    _uiState.value = _uiState.value.copy(error = "Failed to save image")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to save image: ${e.message}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesByBodyPartScreen(
    categoryNameFromNav: String, // This is the raw argument from NavHost, potentially URL-encoded
    viewModel: ExercisesByBodyPartViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onEditExerciseImageClick: (exerciseId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Dialog state
    var showEditOptionsDialog by remember { mutableStateOf(false) }
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var showExistingImagesDialog by remember { mutableStateOf(false) }
    var selectedExerciseId by remember { mutableStateOf("") }
    var newImageName by remember { mutableStateOf("") }
    
    // New dialog state for creating exercises
    var showCreateExerciseDialog by remember { mutableStateOf(false) }
    var newExerciseName by remember { mutableStateOf("") }
    var showCreateImagePickerDialog by remember { mutableStateOf(false) }
    var showSelectExistingForNewDialog by remember { mutableStateOf(false) }
    var newExerciseImageName by remember { mutableStateOf("") }
    var newExerciseImageIdentifier by remember { mutableStateOf<String?>(null) }
    
    // Add new state for image search
    var showImageSearchDialog by remember { mutableStateOf(false) }
    var selectedExerciseForSearch by remember { mutableStateOf<ExerciseDefinition?>(null) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Show dialog to name the image after selection
            showImagePickerDialog = true
            // Store URI in ViewModel or local state for later processing
            viewModel.setSelectedImageUri(it.toString())
        }
    }
    
    // Image picker launcher for new exercise
    val newExerciseImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Show dialog to name the image for new exercise
            showCreateImagePickerDialog = true
            // Store URI in ViewModel for later processing
            viewModel.setSelectedImageUri(it.toString())
        }
    }

    Scaffold(
        topBar = {
            TransparentTopBar(
                title = stringResource(R.string.exercises_by_category) + ": ${uiState.categoryName}", // Use categoryName from UiState
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    showCreateExerciseDialog = true
                    newExerciseName = ""
                    newExerciseImageIdentifier = null
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add New Exercise"
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Loading exercises for ${uiState.categoryName}...")
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.error}")
                }
            } else if (uiState.exercises.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No exercises found for ${uiState.categoryName}. Use the + button to create one.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.exercises, key = { it.id }) { exercise ->
                        ExerciseListItem(
                            exercise = exercise,
                            onEditClick = {
                                selectedExerciseId = exercise.id
                                showEditOptionsDialog = true
                            },
                            onSearchImageClick = {
                                selectedExerciseForSearch = exercise
                                viewModel.searchExerciseImage(exercise.name)
                                showImageSearchDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Edit Options Dialog
    if (showEditOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showEditOptionsDialog = false },
            title = { Text("Edit Exercise Image") },
            text = { Text("Choose how you want to update the exercise image") },
            confirmButton = {
                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                    Button(
                        onClick = {
                            // First check if we have a valid selected exercise
                            if (selectedExerciseId.isNotEmpty()) {
                                showEditOptionsDialog = false
                                showExistingImagesDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Choose from existing app images")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            // First check if we have a valid selected exercise
                            if (selectedExerciseId.isNotEmpty()) {
                                showEditOptionsDialog = false
                                // Launch image picker
                                imagePickerLauncher.launch("image/*")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Load new image from device")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditOptionsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Image Naming Dialog (after picking a new image)
    if (showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            title = { Text("Name Your Image") },
            text = {
                Column {
                    Text("Give this image a descriptive name:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newImageName,
                        onValueChange = { newImageName = it },
                        label = { Text("Image Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Process and save the image, then update the exercise
                        if (selectedExerciseId.isNotEmpty()) {
                            viewModel.saveSelectedImageWithName(selectedExerciseId, newImageName)
                        }
                        showImagePickerDialog = false
                        newImageName = ""
                    },
                    enabled = newImageName.isNotBlank() && selectedExerciseId.isNotEmpty()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showImagePickerDialog = false
                        newImageName = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Existing Images Dialog
    if (showExistingImagesDialog) {
        AlertDialog(
            onDismissRequest = { showExistingImagesDialog = false },
            title = { Text("Choose Existing Image") },
            text = { 
                if (uiState.availableImages.isEmpty()) {
                    Text("No images available. Try adding some first.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.availableImages.chunked(2)) { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                for (imageName in row) {
                                    ExistingImageCard(
                                        imageName = imageName,
                                        onClick = {
                                            if (selectedExerciseId.isNotEmpty()) {
                                                viewModel.updateExerciseImage(selectedExerciseId, imageName)
                                            }
                                            showExistingImagesDialog = false
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                // If odd number of items, add spacer to keep layout balanced
                                if (row.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExistingImagesDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Create New Exercise Dialog
    if (showCreateExerciseDialog) {
        AlertDialog(
            onDismissRequest = { showCreateExerciseDialog = false },
            title = { Text("Create New Exercise") },
            text = {
                Column {
                    Text("Enter exercise name and select an image:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newExerciseName,
                        onValueChange = { newExerciseName = it },
                        label = { Text("Exercise Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Show selected image or placeholder
                    if (newExerciseImageIdentifier != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ){
                                ExerciseImage(
                                    exerciseName = "",
                                    imageIdentifier = newExerciseImageIdentifier!!,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Image selected",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Text(
                            text = "No image selected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { showSelectExistingForNewDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Choose Image")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = { newExerciseImagePickerLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("New Image")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createNewExercise(newExerciseName, newExerciseImageIdentifier)
                        showCreateExerciseDialog = false
                        newExerciseName = ""
                        newExerciseImageIdentifier = null
                    },
                    enabled = newExerciseName.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showCreateExerciseDialog = false
                        newExerciseName = ""
                        newExerciseImageIdentifier = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Image Naming Dialog for New Exercise
    if (showCreateImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showCreateImagePickerDialog = false },
            title = { Text("Name Your Image") },
            text = {
                Column {
                    Text("Give this image a descriptive name:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newExerciseImageName,
                        onValueChange = { newExerciseImageName = it },
                        label = { Text("Image Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Save the image and get the identifier
                        viewModel.viewModelScope.launch {
                            val savedImageName = viewModel.saveSelectedImageForNewExercise(newExerciseImageName)
                            newExerciseImageIdentifier = savedImageName
                        }
                        showCreateImagePickerDialog = false
                        newExerciseImageName = ""
                    },
                    enabled = newExerciseImageName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showCreateImagePickerDialog = false
                        newExerciseImageName = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Select existing image for new exercise dialog
    if (showSelectExistingForNewDialog) {
        AlertDialog(
            onDismissRequest = { showSelectExistingForNewDialog = false },
            title = { Text("Choose Existing Image") },
            text = { 
                if (uiState.availableImages.isEmpty()) {
                    Text("No images available. Try adding some first.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.availableImages.chunked(2)) { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                for (imageName in row) {
                                    ExistingImageCard(
                                        imageName = imageName,
                                        onClick = {
                                            newExerciseImageIdentifier = imageName
                                            showSelectExistingForNewDialog = false
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                // If odd number of items, add spacer to keep layout balanced
                                if (row.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSelectExistingForNewDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Image Search Dialog
    if (showImageSearchDialog && selectedExerciseForSearch != null) {
        ImageSearchDialog(
            searchResults = uiState.searchResults,
            isSearching = uiState.isSearching,
            onImageSelected = { imageUrl ->
                selectedExerciseForSearch?.let { exercise ->
                    viewModel.saveSearchedImage(exercise.id, imageUrl)
                }
                showImageSearchDialog = false
            },
            onSearchAgain = {
                selectedExerciseForSearch?.let { exercise ->
                    viewModel.searchExerciseImage(exercise.name)
                }
            },
            onDismiss = {
                showImageSearchDialog = false
                selectedExerciseForSearch = null
            }
        )
    }
}

@Composable
fun ExerciseListItem(
    exercise: ExerciseDefinition,
    onEditClick: () -> Unit,
    onSearchImageClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    ExerciseImage(
                        exerciseName = exercise.name,
                        imageIdentifier = exercise.imageIdentifier,
                        modifier = Modifier.fillMaxSize(),
                        onSearchClick = if (exercise.imageIdentifier.isBlank()) { onSearchImageClick } else { {} }
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Image for ${exercise.name}",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ExistingImageCard(
    imageName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f),
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 4.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ExerciseImage(
                    exerciseName = "",  // Not used for rendering
                    imageIdentifier = imageName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                text = imageName,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
                                    contentScale = ContentScale.Crop
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