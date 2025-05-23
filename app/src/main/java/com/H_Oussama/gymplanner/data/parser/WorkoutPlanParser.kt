package com.H_Oussama.gymplanner.data.parser

import android.util.Log
import com.H_Oussama.gymplanner.data.model.ExerciseDefinition
import com.H_Oussama.gymplanner.data.model.ExerciseInstance
import com.H_Oussama.gymplanner.data.model.MuscleGroup
import com.H_Oussama.gymplanner.data.model.Weekday
import com.H_Oussama.gymplanner.data.model.WorkoutDay
import com.H_Oussama.gymplanner.data.model.WorkoutPlan
import com.H_Oussama.gymplanner.data.repositories.ExerciseDefinitionRepository
import java.util.Locale
import java.util.UUID

object WorkoutPlanParser {

    private const val PLAN_NAME_PREFIX = "Plan Name:"
    private const val DAY_PATTERN = "Day\\s+\\d+:\\s+"
    private const val WEEKDAY_PATTERN = "\\[(Mon|Tue|Wed|Thu|Fri|Sat|Sun)(day)?\\]"
    private const val MUSCLE_GROUP_PATTERN = "\\{([^{}]+)\\}"
    private const val COMMENT_PREFIX = "//"
    private const val EXERCISE_PREFIX = "-"
    private const val REST_DAY_INDICATOR = "rest"
    private const val EXERCISE_DELIMITER = "|"

    // Converts "Bench Press" to "bench_press" (used as a potential fallback/initial ID)
    private fun exerciseNameToPotentialId(name: String): String {
        return name.trim().lowercase(Locale.ROOT).replace(" ", "_").replace(Regex("[^a-z0-9_]"), "")
    }

    // Extracts integer seconds from strings like "120", "90s", " 60 seconds "
    private fun parseRestTime(restString: String): Int {
        return restString.trim().filter { it.isDigit() }.toIntOrNull() ?: 0
    }

    /**
     * Parse weekday information from a day name
     * Format example: "Day 1: [Mon] Upper Body"
     */
    private fun parseWeekday(dayName: String): Weekday? {
        val weekdayPattern = Regex(WEEKDAY_PATTERN, RegexOption.IGNORE_CASE)
        val matchResult = weekdayPattern.find(dayName)
        
        if (matchResult != null) {
            val weekdayText = matchResult.groups[1]?.value ?: return null
            return Weekday.fromString(weekdayText)
        }
        
        return null
    }
    
    /**
     * Extract explicit muscle group information from a day name
     * Format example: "Day 1: Upper Body {chest, triceps}"
     */
    private fun parseMuscleGroups(dayName: String): Pair<MuscleGroup?, MuscleGroup?> {
        val muscleGroupPattern = Regex(MUSCLE_GROUP_PATTERN, RegexOption.IGNORE_CASE)
        val matchResult = muscleGroupPattern.find(dayName)
        var primaryGroup: MuscleGroup? = null
        var secondaryGroup: MuscleGroup? = null
        
        Log.d("WorkoutPlanParser", "Parsing muscles for day name: '$dayName'")
        
        if (matchResult != null) {
            val muscleGroupsText = matchResult.groups[1]?.value?.split(",") ?: emptyList()
            Log.d("WorkoutPlanParser", "  Found explicit groups text: '${matchResult.groups[1]?.value}'")
            
            primaryGroup = muscleGroupsText.getOrNull(0)?.trim()?.let { 
                val parsed = MuscleGroup.fromString(it)
                Log.d("WorkoutPlanParser", "    Parsed primary '$it' as: ${parsed?.name}")
                parsed
            }
            secondaryGroup = muscleGroupsText.getOrNull(1)?.trim()?.let { 
                 val parsed = MuscleGroup.fromString(it)
                 Log.d("WorkoutPlanParser", "    Parsed secondary '$it' as: ${parsed?.name}")
                 parsed
            }
            
            Log.d("WorkoutPlanParser", "  Result from explicit parse: Primary=${primaryGroup?.name}, Secondary=${secondaryGroup?.name}")
            return Pair(primaryGroup, secondaryGroup)
        } else {
             Log.d("WorkoutPlanParser", "  No explicit groups found (no match for pattern '$MUSCLE_GROUP_PATTERN'). Trying detection.")
             // If no explicit muscle groups, try to detect from the day name
             val cleanDayName = dayName.replace(Regex("\\[.*?\\]|\\{.*?\\}"), "").trim()
             val detectedPair = MuscleGroup.detectFromDayName(cleanDayName)
             Log.d("WorkoutPlanParser", "  Result from detection on '$cleanDayName': Primary=${detectedPair.first?.name}, Secondary=${detectedPair.second?.name}")
             return detectedPair
        }
    }
    
    /**
     * Check if a day is a rest day based on its name and exercises
     */
    private fun isRestDay(dayName: String, exercises: List<ExerciseInstance> = emptyList()): Boolean {
        // First check - if there are exercises, it's not a rest day regardless of name
        if (exercises.isNotEmpty()) {
            return false
        }
        
        // Second check - if "Rest Day" is explicitly in the name
        val normalizedName = dayName.lowercase().replace(Regex("\\[.*?\\]|\\{.*?\\}"), "").trim()
        return normalizedName.contains("rest day", ignoreCase = true) || 
               (normalizedName.contains("rest", ignoreCase = true) && 
                !normalizedName.contains("chest", ignoreCase = true) && 
                !normalizedName.contains("rest time", ignoreCase = true))
    }
    
    /**
     * Clean the day name by removing weekday and muscle group annotations
     */
    private fun cleanDayName(dayName: String): String {
        return dayName
            .replace(Regex(WEEKDAY_PATTERN), "")
            .replace(Regex(MUSCLE_GROUP_PATTERN), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    // Modified function to accept the repository
    suspend fun parseWorkoutPlan(
        text: String,
        exerciseRepo: ExerciseDefinitionRepository
    ): Result<WorkoutPlan> {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith(COMMENT_PREFIX) }
        if (lines.isEmpty()) {
            return Result.failure(IllegalArgumentException("Input text is empty or contains only comments."))
        }
        
        var planName = "Untitled Plan"
        val workoutDays = mutableListOf<WorkoutDay>()
        var currentDayName: String? = null
        val currentExercises = mutableListOf<ExerciseInstance>()
        val weekdayAssociations = mutableMapOf<Weekday, Int>()
        var dayIndex = 0

        // Wrap in try-catch to handle potential repository exceptions
        try {
            for ((index, line) in lines.withIndex()) {
                when {
                    line.startsWith(PLAN_NAME_PREFIX, ignoreCase = true) -> {
                        planName = line.substringAfter(PLAN_NAME_PREFIX).trim()
                    }
                    Regex("${DAY_PATTERN}.*", RegexOption.IGNORE_CASE).matches(line) -> {
                        // Finalize the previous day
                        if (currentDayName != null) {
                            val isRest = isRestDay(currentDayName, currentExercises)
                            val (primaryMuscle, secondaryMuscle) = parseMuscleGroups(currentDayName)
                            val weekday = parseWeekday(currentDayName)
                            val cleanName = cleanDayName(currentDayName)
                            
                            val workoutDay = if (isRest) {
                                // Create a rest day
                                WorkoutDay(
                                    dayName = cleanName,
                                    exercises = emptyList(),
                                    isRestDay = true,
                                    primaryMuscleGroup = null,
                                    secondaryMuscleGroup = null
                                )
                            } else {
                                // Create a regular workout day
                                WorkoutDay(
                                    dayName = cleanName,
                                    exercises = currentExercises.toList(),
                                    isRestDay = false,
                                    primaryMuscleGroup = primaryMuscle,
                                    secondaryMuscleGroup = secondaryMuscle
                                )
                            }
                            
                            workoutDays.add(workoutDay)
                            
                            // Store weekday association if present
                            if (weekday != null) {
                                weekdayAssociations[weekday] = dayIndex
                            }
                            
                            dayIndex++
                        }
                        
                        // Start a new day
                        currentDayName = line.trim()
                        currentExercises.clear()
                        if (currentDayName.isNullOrEmpty()) {
                            return Result.failure(IllegalArgumentException("Day name cannot be empty. Error at line ${index + 1}."))
                        }
                    }
                    line.startsWith(EXERCISE_PREFIX) && currentDayName != null && !isRestDay(currentDayName, currentExercises) -> {
                        // Exercise line starts with "-"
                        val exerciseLine = line.substring(EXERCISE_PREFIX.length).trim()
                        val parts = exerciseLine.split(EXERCISE_DELIMITER)
                        if (parts.size != 3) {
                            return Result.failure(IllegalArgumentException("Malformed exercise line. Expected '- [exercise] | [sets] | [rest]'. Error at line ${index + 1}: '$line'"))
                        }

                        val exerciseName = parts[0].trim()
                        val setsDescription = parts[1].trim()
                        val restTimeString = parts[2].trim()

                        if (exerciseName.isEmpty() || setsDescription.isEmpty()) {
                            return Result.failure(IllegalArgumentException("Exercise name and sets description cannot be empty. Error at line ${index + 1}."))
                        }

                        // --- Find or Create Exercise Definition --- 
                        val potentialId = exerciseNameToPotentialId(exerciseName)
                        var definition = exerciseRepo.findDefinitionByName(exerciseName) 
                                          ?: exerciseRepo.getDefinitionByIdOnce(potentialId)

                        if (definition == null) {
                            // Create new definition if not found by name or potential ID
                            definition = ExerciseDefinition(
                                id = potentialId, // Use derived ID initially
                                name = exerciseName,
                                // Set default image identifier based on ID
                                imageIdentifier = potentialId, 
                                description = null
                            )
                            exerciseRepo.insertDefinition(definition)
                            // We assume insertion worked and use the definition object
                        }
                        // --- End Find or Create --- 

                        val restTimeSeconds = parseRestTime(restTimeString)

                        currentExercises.add(
                            ExerciseInstance(
                                exerciseId = definition.id, // Use the ID from the found/created definition
                                setsDescription = setsDescription,
                                restTimeSeconds = restTimeSeconds
                            )
                        )
                    }
                    currentDayName != null && line.isBlank() -> {
                        // Skip blank lines within a day
                        continue
                    }
                    else -> {
                        // Check if this might be an exercise line without the dash
                        if (currentDayName != null && !isRestDay(currentDayName, currentExercises) && line.contains(EXERCISE_DELIMITER)) {
                            // Try to handle it as an exercise missing the dash prefix
                            val parts = line.split(EXERCISE_DELIMITER)
                            if (parts.size == 3) {
                                // It looks like an exercise line, so process it
                                val exerciseName = parts[0].trim()
                                val setsDescription = parts[1].trim()
                                val restTimeString = parts[2].trim()

                                if (exerciseName.isNotEmpty() && setsDescription.isNotEmpty()) {
                                    // --- Find or Create Exercise Definition ---
                                    val potentialId = exerciseNameToPotentialId(exerciseName)
                                    var definition = exerciseRepo.findDefinitionByName(exerciseName)
                                                      ?: exerciseRepo.getDefinitionByIdOnce(potentialId)

                                    if (definition == null) {
                                        definition = ExerciseDefinition(
                                            id = potentialId,
                                            name = exerciseName,
                                            imageIdentifier = potentialId,
                                            description = null
                                        )
                                        exerciseRepo.insertDefinition(definition)
                                    }
                                    // --- End Find or Create ---

                                    val restTimeSeconds = parseRestTime(restTimeString)

                                    currentExercises.add(
                                        ExerciseInstance(
                                            exerciseId = definition.id,
                                            setsDescription = setsDescription,
                                            restTimeSeconds = restTimeSeconds
                                        )
                                    )
                                    continue
                                }
                            }
                        }
                        
                        // Line before first DAY
                        if (index == 0 && !line.startsWith(PLAN_NAME_PREFIX, ignoreCase = true)) {
                            return Result.failure(IllegalArgumentException("Input should start with '$PLAN_NAME_PREFIX'. Error at line ${index + 1}."))
                        }
                        // Ignore other orphan lines for now
                    }
                }
            }

            // Add the last day
            if (currentDayName != null) {
                val isRest = isRestDay(currentDayName, currentExercises)
                val (primaryMuscle, secondaryMuscle) = parseMuscleGroups(currentDayName)
                val weekday = parseWeekday(currentDayName)
                val cleanName = cleanDayName(currentDayName)
                
                val workoutDay = if (isRest) {
                    // Create a rest day
                    WorkoutDay(
                        dayName = cleanName,
                        exercises = emptyList(),
                        isRestDay = true,
                        primaryMuscleGroup = null,
                        secondaryMuscleGroup = null
                    )
                } else {
                    // Create a regular workout day
                    WorkoutDay(
                        dayName = cleanName,
                        exercises = currentExercises.toList(),
                        isRestDay = false,
                        primaryMuscleGroup = primaryMuscle,
                        secondaryMuscleGroup = secondaryMuscle
                    )
                }
                
                workoutDays.add(workoutDay)
                
                // Store weekday association if present
                if (weekday != null) {
                    weekdayAssociations[weekday] = dayIndex
                }
            }

            if (workoutDays.isEmpty()) {
                return Result.failure(IllegalArgumentException("No valid workout days found in the plan."))
            }

            return Result.success(WorkoutPlan(planName, workoutDays, weekdayAssociations))

        } catch (e: Exception) {
            // Catch database or other exceptions during parsing
            return Result.failure(RuntimeException("Error processing plan: ${e.message}", e))
        }
    }
} 