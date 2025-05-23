package com.H_Oussama.gymplanner.data.repositories

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for calculating and tracking calories burned from workouts and exercises.
 * Provides methods to record workouts, calculate calories burned, and retrieve daily summaries.
 */
@Singleton
class CalorieService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Constants for calorie burn rates based on intensity (calories per minute)
    companion object {
        private const val LOW_INTENSITY_BURN_RATE = 3.5
        private const val MEDIUM_INTENSITY_BURN_RATE = 7.0
        private const val HIGH_INTENSITY_BURN_RATE = 10.5
        
        private const val PREF_NAME = "calorie_service_prefs"
        private const val KEY_PREFIX_CALORIES = "calories_burned_"
        private const val KEY_PREFIX_WORKOUT_LOG = "workout_log_"
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    private val _caloriesBurnedToday = MutableStateFlow(0)
    val caloriesBurnedToday: StateFlow<Int> = _caloriesBurnedToday.asStateFlow()
    
    init {
        // Initialize today's calories burned
        val today = LocalDate.now()
        _caloriesBurnedToday.value = getCaloriesBurnedForDate(today)
    }
    
    /**
     * Records a workout, calculates calories burned, and updates stored data.
     * 
     * @param workoutName Name of the workout
     * @param durationMinutes Duration of the workout in minutes
     * @param intensityLevel Intensity level ("low", "medium", or "high")
     * @param date The date of the workout, defaults to today
     * @return The number of calories burned
     */
    fun recordWorkout(
        workoutName: String,
        durationMinutes: Int,
        intensityLevel: String,
        date: LocalDate = LocalDate.now()
    ): Int {
        // Calculate calories burned based on intensity and duration
        val burnRate = when (intensityLevel.lowercase()) {
            "low" -> LOW_INTENSITY_BURN_RATE
            "high" -> HIGH_INTENSITY_BURN_RATE
            else -> MEDIUM_INTENSITY_BURN_RATE
        }
        
        val caloriesBurned = (burnRate * durationMinutes).toInt()
        
        // Get previously burned calories for the day
        val previousCalories = getCaloriesBurnedForDate(date)
        val newTotalCalories = previousCalories + caloriesBurned
        
        // Save the updated calories
        saveCaloriesBurnedForDate(date, newTotalCalories)
        
        // Save the workout log
        val workoutLog = WorkoutLog(
            workoutName = workoutName,
            durationMinutes = durationMinutes,
            intensityLevel = intensityLevel,
            caloriesBurned = caloriesBurned,
            date = date
        )
        saveWorkoutLog(workoutLog)
        
        // Update today's calories burned if the workout is for today
        if (date == LocalDate.now()) {
            _caloriesBurnedToday.value = newTotalCalories
        }
        
        return caloriesBurned
    }
    
    /**
     * Gets the total calories burned for a specific date.
     * 
     * @param date The date to get calories for
     * @return The total calories burned on that date
     */
    fun getCaloriesBurnedForDate(date: LocalDate): Int {
        val key = KEY_PREFIX_CALORIES + formatDate(date)
        return sharedPreferences.getInt(key, 0)
    }
    
    /**
     * Sets the total calories burned for a specific date.
     * 
     * @param date The date to set calories for
     * @param calories The total calories burned
     */
    private fun saveCaloriesBurnedForDate(date: LocalDate, calories: Int) {
        val key = KEY_PREFIX_CALORIES + formatDate(date)
        sharedPreferences.edit().putInt(key, calories).apply()
    }
    
    /**
     * Saves a workout log to shared preferences.
     * 
     * @param workoutLog The workout log to save
     */
    private fun saveWorkoutLog(workoutLog: WorkoutLog) {
        val existingLogs = getWorkoutLogsForDate(workoutLog.date)
        val updatedLogs = existingLogs + workoutLog
        
        // Convert logs to JSON and save
        val logsJson = updatedLogs.joinToString(";") { it.toStorageString() }
        val key = KEY_PREFIX_WORKOUT_LOG + formatDate(workoutLog.date)
        
        sharedPreferences.edit().putString(key, logsJson).apply()
    }
    
    /**
     * Gets all workout logs for a specific date.
     * 
     * @param date The date to get logs for
     * @return A list of workout logs for that date
     */
    fun getWorkoutLogsForDate(date: LocalDate): List<WorkoutLog> {
        val key = KEY_PREFIX_WORKOUT_LOG + formatDate(date)
        val logsJson = sharedPreferences.getString(key, "") ?: ""
        
        if (logsJson.isEmpty()) return emptyList()
        
        return logsJson.split(";").mapNotNull { logString ->
            try {
                WorkoutLog.fromStorageString(logString)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Helper method to format a date consistently for storage keys.
     */
    private fun formatDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
    
    /**
     * Data class representing a workout log entry.
     */
    data class WorkoutLog(
        val workoutName: String,
        val durationMinutes: Int,
        val intensityLevel: String,
        val caloriesBurned: Int,
        val date: LocalDate
    ) {
        /**
         * Converts the workout log to a storage string format.
         */
        fun toStorageString(): String {
            return "$workoutName|$durationMinutes|$intensityLevel|$caloriesBurned|${formatDate(date)}"
        }
        
        companion object {
            /**
             * Creates a WorkoutLog from a storage string.
             */
            fun fromStorageString(storageString: String): WorkoutLog {
                val parts = storageString.split("|")
                if (parts.size != 5) throw IllegalArgumentException("Invalid storage string format")
                
                return WorkoutLog(
                    workoutName = parts[0],
                    durationMinutes = parts[1].toInt(),
                    intensityLevel = parts[2],
                    caloriesBurned = parts[3].toInt(),
                    date = LocalDate.parse(parts[4], DateTimeFormatter.ISO_LOCAL_DATE)
                )
            }
            
            private fun formatDate(date: LocalDate): String {
                return date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            }
        }
    }
}
