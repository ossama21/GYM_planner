# Image Support Documentation

This document explains how to use the image support features in the GYM Planner app, including adding exercise images, using thumbnails, and properly formatting workout plans to incorporate images.

## Image Structure

The app supports two main types of images:

1. **Exercise Images** - Images showing how to perform specific exercises
2. **Muscle Group Images** - Images representing different muscle groups

### Directory Structure

Images are stored in the app's assets directory:

```
app/src/main/assets/
├── exercise_images/   # Exercise demonstration images
│   ├── bench-press.avif
│   ├── barbell_squats.avif
│   ├── deadlift-howto.avif
│   └── ...
├── body_images/       # Muscle group images
│   ├── chest.jpg
│   ├── back.jpg
│   └── ...
└── gym_planner_icon.jpg  # App icon
```

## Adding New Exercise Images

### Supported Formats

The app supports multiple image formats with the following priority:
1. `.avif` (preferred for smaller file size)
2. `.jpg`
3. `.png`

### Naming Convention

Exercise images should follow these naming guidelines:

- Use kebab-case (words separated by hyphens): e.g., `barbell-bench-press.avif`
- Names should be descriptive and match the exercise they represent
- Avoid spaces or special characters

### Steps to Add a New Exercise Image

1. Optimize your image to reduce file size (aim for < 50KB if possible)
2. Name the file according to the conventions above
3. Place the file in the `app/src/main/assets/exercise_images/` directory
4. Update the `ExerciseImageMatcher` class if necessary (see below)

## Linking Exercises to Images

### In ExerciseDefinition

When creating an `ExerciseDefinition`, set the `imageIdentifier` field to match the filename (without extension):

```kotlin
ExerciseDefinition(
    id = "bench_press",
    name = "Bench Press",
    description = "Lie on a flat bench...",
    imageIdentifier = "bench-press"  // matches bench-press.avif
)
```

### In Workout Plan Text Format

The workout plan parser will automatically attempt to match exercises to images. No special syntax is needed:

```
Day 1: [Mon] Chest Day {CHEST, TRICEPS}
- Bench Press | 4x8-10 | 120
- Incline Dumbbell Press | 3x10 | 90
```

The parser will:
1. Create exercise definitions
2. Set the `imageIdentifier` based on the exercise name
3. The `ExerciseImageMatcher` utility will find the best matching image

## Image Matching System

The app uses a sophisticated image matching system to find the most appropriate image for an exercise:

1. **Direct Match**: Looks for exact match after normalization
2. **Common Name Mapping**: Uses predefined mappings for common exercise variations
3. **Keyword Matching**: Matches parts of the exercise name
4. **Levenshtein Distance**: Uses string similarity for fuzzy matching
5. **Default Fallback**: Uses muscle group icons if no match is found

### Extending the Image Matcher

To add new mappings for exercise name variations, edit the `commonNameMappings` in `ExerciseImageMatcher.kt`:

```kotlin
private val commonNameMappings = mapOf(
    "bench" to "bench-press",
    "bp" to "bench-press",
    // Add your new mappings here
    "overhead press" to "dumbbell-shoulder-press",
)
```

## Using the UI Components

### ExerciseThumbnail

```kotlin
ExerciseThumbnail(
    exerciseDefinition = myExercise,
    modifier = Modifier,
    size = 80,            // Size in dp
    isCompleted = false,  // Shows a checkmark if completed
    isSquare = false      // Square or rounded corners
)
```

### MuscleGroupThumbnail

```kotlin
MuscleGroupThumbnail(
    muscleGroup = MuscleGroup.CHEST,
    modifier = Modifier,
    size = 60,
    isSquare = false
)
```

## Example Usage

```kotlin
@Composable
fun ExerciseListItem(
    exercise: ExerciseDefinition,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Use the ExerciseThumbnail composable
        ExerciseThumbnail(
            exerciseDefinition = exercise,
            isCompleted = isCompleted,
            size = 60
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = exercise.name,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
```

## Generating Example Workouts

The app includes the `ExampleWorkoutGenerator` utility that can create example workout plans using the available exercise images:

```kotlin
val generator = ExampleWorkoutGenerator(context)
val workoutPlanText = generator.generateExampleWorkoutPlanText()
```

## Troubleshooting

If images are not displaying correctly:

1. **Check Image Filenames**: Ensure they match the `imageIdentifier` in the `ExerciseDefinition`
2. **Verify Asset Path**: Confirm images are in the correct asset directory
3. **Image Format**: Try converting to a supported format (.avif, .jpg, .png)
4. **Log Messages**: Check logcat for errors related to image loading
5. **Image Matcher**: Add an explicit mapping in `ExerciseImageMatcher` if needed

## Best Practices

1. **Optimize Images**: Keep file sizes small for better performance
2. **Consistent Naming**: Follow the naming conventions for predictable matching
3. **High Quality Images**: Use clear, instructive images that show proper form
4. **Add Descriptions**: Always include detailed descriptions with exercise definitions
5. **Test on Multiple Devices**: Verify images display correctly on various screen sizes 