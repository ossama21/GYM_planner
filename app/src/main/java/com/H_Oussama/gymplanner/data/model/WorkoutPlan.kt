package com.H_Oussama.gymplanner.data.model

/**
 * Represents a complete workout plan.
 *
 * @property planName The name of the workout plan
 * @property days The list of workout days in this plan
 */
data class WorkoutPlan(
    val planName: String,
    val days: List<WorkoutDay>,
    val weekdayAssociations: Map<Weekday, Int> = emptyMap() // Maps weekdays to day indices
)

/**
 * Represents a single day in a workout plan.
 *
 * @property id The unique identifier for this workout day
 * @property dayName The name of this workout day (e.g., "Push Day", "Leg Day")
 * @property dayNumber The sequential number of this workout day in the plan
 * @property exercises The list of exercises for this day
 * @property isRestDay Whether this is a rest day
 * @property primaryMuscleGroup The primary muscle group targeted on this day (for image display)
 * @property secondaryMuscleGroup Optional secondary muscle group
 * @property estimatedDuration Estimated workout duration in minutes
 */
data class WorkoutDay(
    val id: String = java.util.UUID.randomUUID().toString(),
    val dayName: String,
    val dayNumber: Int = 0,
    val exercises: List<ExerciseInstance>,
    val isRestDay: Boolean = false,
    val primaryMuscleGroup: MuscleGroup? = null,
    val secondaryMuscleGroup: MuscleGroup? = null,
    val estimatedDuration: Int = calculateEstimatedDuration(exercises)
) {
    // Getter for backwards compatibility
    val name: String get() = dayName
    
    // Getter for exercise references
    val exerciseRefs: List<String> get() = exercises.map { it.exerciseId }
    
    companion object {
        /**
         * Calculate estimated workout duration based on exercise count, sets, and rest times
         */
        fun calculateEstimatedDuration(exercises: List<ExerciseInstance>): Int {
            if (exercises.isEmpty()) return 0
            
            // Base time for each exercise (setup, etc.)
            val baseTimePerExercise = 2 // minutes
            
            // Sum up exercise-specific times
            var totalMinutes = exercises.size * baseTimePerExercise
            
            // Add time for sets and rest periods
            exercises.forEach { exercise ->
                // Estimate sets count from description
                val estimatedSets = extractSetsCount(exercise.setsDescription)
                
                // Estimate time per set (usually 30-60 seconds)
                val timePerSet = 1 // minute
                
                // Add set time
                totalMinutes += estimatedSets * timePerSet
                
                // Add rest time
                totalMinutes += (estimatedSets - 1) * (exercise.restTimeSeconds / 60)
            }
            
            return totalMinutes
        }
        
        /**
         * Extract the number of sets from a sets description
         */
        private fun extractSetsCount(setsDescription: String): Int {
            // Try to parse "X sets" pattern
            val setsPattern = "(\\d+)\\s*sets?".toRegex(RegexOption.IGNORE_CASE)
            val match = setsPattern.find(setsDescription)
            
            return match?.groupValues?.get(1)?.toIntOrNull() ?: 3 // Default to 3 if cannot parse
        }
    }
}

/**
 * Represents a single exercise instance in a workout day.
 *
 * @property exerciseId The identifier for this exercise
 * @property setsDescription Description of sets and reps (e.g., "3 sets of 8 reps")
 * @property restTimeSeconds Rest time between sets in seconds
 */
data class ExerciseInstance(
    val exerciseId: String,
    val setsDescription: String,
    val restTimeSeconds: Int
)

/**
 * Represents days of the week for workout scheduling
 */
enum class Weekday {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
    
    companion object {
        /**
         * Parse a weekday string into a Weekday enum
         */
        fun fromString(day: String): Weekday? {
            return when (day.trim().lowercase()) {
                "monday", "mon", "m" -> MONDAY
                "tuesday", "tue", "tues", "t" -> TUESDAY
                "wednesday", "wed", "w" -> WEDNESDAY
                "thursday", "thu", "thurs", "th" -> THURSDAY
                "friday", "fri", "f" -> FRIDAY
                "saturday", "sat", "s" -> SATURDAY
                "sunday", "sun", "su" -> SUNDAY
                else -> null
            }
        }
        
        /**
         * Get the current weekday
         */
        fun getCurrent(): Weekday {
            val calendar = java.util.Calendar.getInstance()
            return when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
                java.util.Calendar.MONDAY -> MONDAY
                java.util.Calendar.TUESDAY -> TUESDAY
                java.util.Calendar.WEDNESDAY -> WEDNESDAY
                java.util.Calendar.THURSDAY -> THURSDAY
                java.util.Calendar.FRIDAY -> FRIDAY
                java.util.Calendar.SATURDAY -> SATURDAY
                java.util.Calendar.SUNDAY -> SUNDAY
                else -> MONDAY // Default fallback
            }
        }
    }
    
    /**
     * Get the display name of the weekday
     */
    fun getDisplayName(): String {
        return when (this) {
            MONDAY -> "Monday"
            TUESDAY -> "Tuesday"
            WEDNESDAY -> "Wednesday"
            THURSDAY -> "Thursday"
            FRIDAY -> "Friday"
            SATURDAY -> "Saturday"
            SUNDAY -> "Sunday"
        }
    }
    
    /**
     * Get the short display name of the weekday
     */
    fun getShortName(): String {
        return when (this) {
            MONDAY -> "Mon"
            TUESDAY -> "Tue"
            WEDNESDAY -> "Wed"
            THURSDAY -> "Thu"
            FRIDAY -> "Fri"
            SATURDAY -> "Sat"
            SUNDAY -> "Sun"
        }
    }
}

/**
 * Represents major muscle groups for displaying appropriate body images
 */
enum class MuscleGroup {
    CHEST, BACK, SHOULDERS, BICEPS, TRICEPS, QUADS, CALVES, FOREARMS;
    
    companion object {
        /**
         * Parse a muscle group string into a MuscleGroup enum
         */
        fun fromString(muscleGroup: String): MuscleGroup? {
            return when (muscleGroup.trim().lowercase()) {
                "chest", "pecs", "pectorals", "pectoral", "pec" -> CHEST
                "back", "lats", "lat", "latissimus", "traps", "trapezius" -> BACK
                "shoulders", "delts", "deltoids", "shoulder", "delt", "deltoid" -> SHOULDERS
                "biceps", "bis", "guns", "arms", "bicep", "arm" -> BICEPS
                "triceps", "tris", "tricep" -> TRICEPS
                "quads", "legs", "thighs", "quadriceps", "leg", "thigh", "quad" -> QUADS
                "calves", "calf", "calve" -> CALVES
                "forearms", "forearm", "wrists", "wrist" -> FOREARMS
                else -> null
            }
        }
        
        /**
         * Detect muscle groups from a workout day name
         */
        fun detectFromDayName(dayName: String): Pair<MuscleGroup?, MuscleGroup?> {
            val normalized = dayName.lowercase()
            
            // Define patterns for each muscle group
            val patterns = mapOf(
                CHEST to listOf("chest", "pec", "push", "bench"),
                BACK to listOf("back", "pull", "lat", "row"),
                SHOULDERS to listOf("shoulder", "delt", "ohp", "press"),
                BICEPS to listOf("bicep", "curl", "arm"),
                TRICEPS to listOf("tricep", "extension", "push"),
                QUADS to listOf("quad", "leg", "squat"),
                CALVES to listOf("calf", "calve"),
                FOREARMS to listOf("forearm", "grip", "wrist")
            )
            
            // Find matching patterns
            val matches = patterns.filter { (_, terms) ->
                terms.any { normalized.contains(it) }
            }.keys.toList()
            
            return when {
                matches.isEmpty() -> Pair(null, null)
                matches.size == 1 -> Pair(matches[0], null)
                else -> Pair(matches[0], matches[1])
            }
        }
    }
    
    /**
     * Get the display name of the muscle group
     */
    fun getDisplayName(): String {
        return when (this) {
            CHEST -> "Chest"
            BACK -> "Back"
            SHOULDERS -> "Shoulders"
            BICEPS -> "Biceps"
            TRICEPS -> "Triceps"
            QUADS -> "Legs"
            CALVES -> "Calves"
            FOREARMS -> "Forearms"
        }
    }
    
    /**
     * Get the image asset path for this muscle group
     */
    fun getImagePath(): String {
        // Update filenames to match exactly what's in the assets directory
        val filename = when (this) {
            CHEST -> "chest.jpg"
            BACK -> "back.jpg"
            SHOULDERS -> "shoulders.jpg"
            BICEPS -> "biceps.jpg"
            TRICEPS -> "triceps.jpg"
            QUADS -> "quads.jpg"
            CALVES -> "calves.jpg" // Updated from "calf.jpg" to match asset name
            FOREARMS -> "forearms.jpg"
        }
        val path = "body_images/$filename"
        android.util.Log.d("MuscleGroup", "getImagePath for $name: $path")
        return path
    }
}

 
 
 