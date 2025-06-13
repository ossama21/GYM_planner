package com.H_Oussama.gymplanner.data.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Date
import java.time.LocalDate
import java.time.ZoneId
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.H_Oussama.gymplanner.workers.UpdateWorker

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "user_preferences"
        
        // Profile Keys
        private const val KEY_USERNAME = "username"
        private const val KEY_AGE = "age"
        private const val KEY_WEIGHT = "weight"
        private const val KEY_HEIGHT = "height"
        private const val KEY_GOAL = "goal"
        private const val KEY_WORKOUTS_PER_WEEK = "workouts_per_week"
        private const val KEY_EMAIL = "email"
        private const val KEY_BODY_TYPE = "body_type"
        
        // Notification Keys
        private const val KEY_WORKOUT_REMINDERS = "workout_reminders"
        private const val KEY_REST_TIMER_ALERTS = "rest_timer_alerts"
        private const val KEY_PROGRESS_UPDATES = "progress_updates"
        private const val KEY_WATER_REMINDERS = "water_reminders"
        private const val KEY_TIPS_AND_MOTIVATION = "tips_and_motivation"
        private const val KEY_WORKOUT_REMINDER_TIME = "workout_reminder_time"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        
        // Theme Keys
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_USE_SYSTEM_THEME = "use_system_theme"
        
        // Units Keys
        private const val KEY_UNIT_SYSTEM = "unit_system"
        private const val KEY_WEIGHT_UNIT = "weight_unit"
        private const val KEY_HEIGHT_UNIT = "height_unit"
        private const val KEY_DISTANCE_UNIT = "distance_unit"
        
        // App Intro Keys
        private const val KEY_INTRO_VIDEO_MUTED = "intro_video_muted"
        private const val KEY_SKIP_INTRO = "skip_intro"
        private const val KEY_FIRST_RUN = "first_run"
        
        // Language Keys
        private const val KEY_LANGUAGE = "language"
        
        // Update check keys
        private const val KEY_LAST_UPDATE_CHECK = "last_update_check"
        private const val KEY_SKIPPED_UPDATE_COUNT = "skipped_update_count"
        
        // Developer mode key
        private const val KEY_DEVELOPER_MODE = "developer_mode"
        
        // Activity level multipliers
        const val ACTIVITY_LEVEL_SEDENTARY = "sedentary"
        const val ACTIVITY_LEVEL_LIGHT = "light"
        const val ACTIVITY_LEVEL_MODERATE = "moderate"
        const val ACTIVITY_LEVEL_ACTIVE = "active"
        const val ACTIVITY_LEVEL_VERY_ACTIVE = "very_active"
        
        // Goal types
        const val GOAL_LOSE_WEIGHT = "lose_weight"
        const val GOAL_MAINTAIN = "maintain"
        const val GOAL_GAIN_MUSCLE = "gain_muscle"
        
        // Body type constants
        const val BODY_TYPE_ECTOMORPH = "ectomorph"
        const val BODY_TYPE_MESOMORPH = "mesomorph"
        const val BODY_TYPE_ENDOMORPH = "endomorph"
        
        // Keys for activity level and gender
        private const val KEY_ACTIVITY_LEVEL = "activity_level"
        private const val KEY_GENDER = "gender"
        
        // Gemini API key
        const val KEY_GEMINI_API_KEY = "gemini_api_key"
    }
    
    // Shared preferences instance
    private val sharedPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Check if this is the first run of the app
     * This is used to determine if we should check for updates on startup
     */
    fun isFirstRun(): Boolean {
        return !sharedPrefs.contains(KEY_FIRST_RUN)
    }
    
    /**
     * Set the first run flag
     */
    fun setFirstRun(isFirstRun: Boolean) {
        sharedPrefs.edit {
            putBoolean(KEY_FIRST_RUN, !isFirstRun)
        }
    }
    
    // Developer mode methods
    
    /**
     * Set developer mode status
     */
    fun setDeveloperMode(enabled: Boolean) {
        sharedPrefs.edit {
            putBoolean(KEY_DEVELOPER_MODE, enabled)
        }
    }
    
    /**
     * Check if developer mode is enabled
     */
    fun isDeveloperModeEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_DEVELOPER_MODE, false)
    }
    
    // Profile Methods
    
    suspend fun saveUsername(username: String) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putString(KEY_USERNAME, username)
        }
    }
    
    fun getUsername(): String {
        return sharedPrefs.getString(KEY_USERNAME, "User") ?: "User"
    }
    
    suspend fun saveAge(age: Int) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putInt(KEY_AGE, age)
        }
    }
    
    fun getAge(): Int {
        return sharedPrefs.getInt(KEY_AGE, 25)
    }
    
    suspend fun saveWeight(weight: Float) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putFloat(KEY_WEIGHT, weight)
        }
    }
    
    fun getWeight(): Float {
        return sharedPrefs.getFloat(KEY_WEIGHT, 70.0f)
    }
    
    // New methods for body type
    suspend fun saveBodyType(bodyType: String) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putString(KEY_BODY_TYPE, bodyType)
        }
    }
    
    fun getBodyType(): String {
        return sharedPrefs.getString(KEY_BODY_TYPE, BODY_TYPE_MESOMORPH) ?: BODY_TYPE_MESOMORPH
    }
    
    // Get body type calorie multiplier based on type
    fun getBodyTypeMultiplier(): Double {
        return when (getBodyType()) {
            BODY_TYPE_ECTOMORPH -> 0.95
            BODY_TYPE_MESOMORPH -> 1.0
            BODY_TYPE_ENDOMORPH -> 1.05
            else -> 1.0 // Default to mesomorph multiplier
        }
    }
    
    suspend fun saveHeight(height: Int) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putInt(KEY_HEIGHT, height)
        }
    }
    
    fun getHeight(): Int {
        return sharedPrefs.getInt(KEY_HEIGHT, 175)
    }
    
    suspend fun saveGoal(goal: String) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putString(KEY_GOAL, goal)
        }
    }
    
    fun getGoal(): String {
        return sharedPrefs.getString(KEY_GOAL, "Build muscle") ?: "Build muscle"
    }
    
    suspend fun saveWorkoutsPerWeek(count: Int) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putInt(KEY_WORKOUTS_PER_WEEK, count)
        }
    }
    
    fun getWorkoutsPerWeek(): Int {
        return sharedPrefs.getInt(KEY_WORKOUTS_PER_WEEK, 3)
    }
    
    suspend fun saveEmail(email: String) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putString(KEY_EMAIL, email)
        }
    }
    
    fun getEmail(): String {
        return sharedPrefs.getString(KEY_EMAIL, "user@example.com") ?: "user@example.com"
    }
    
    // Notification Methods
    
    suspend fun saveWorkoutReminders(enabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putBoolean(KEY_WORKOUT_REMINDERS, enabled)
        }
    }
    
    fun getWorkoutReminders(): Boolean {
        return sharedPrefs.getBoolean(KEY_WORKOUT_REMINDERS, true)
    }
    
    suspend fun saveRestTimerAlerts(enabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putBoolean(KEY_REST_TIMER_ALERTS, enabled)
        }
    }
    
    fun getRestTimerAlerts(): Boolean {
        return sharedPrefs.getBoolean(KEY_REST_TIMER_ALERTS, true)
    }
    
    suspend fun saveProgressUpdates(enabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putBoolean(KEY_PROGRESS_UPDATES, enabled)
        }
    }
    
    fun getProgressUpdates(): Boolean {
        return sharedPrefs.getBoolean(KEY_PROGRESS_UPDATES, false)
    }
    
    suspend fun saveWaterReminders(enabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putBoolean(KEY_WATER_REMINDERS, enabled)
        }
    }
    
    fun getWaterReminders(): Boolean {
        return sharedPrefs.getBoolean(KEY_WATER_REMINDERS, false)
    }
    
    suspend fun saveTipsAndMotivation(enabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putBoolean(KEY_TIPS_AND_MOTIVATION, enabled)
        }
    }
    
    fun getTipsAndMotivation(): Boolean {
        return sharedPrefs.getBoolean(KEY_TIPS_AND_MOTIVATION, true)
    }
    
    suspend fun saveWorkoutReminderTime(time: String) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putString(KEY_WORKOUT_REMINDER_TIME, time)
        }
    }
    
    fun getWorkoutReminderTime(): String {
        return sharedPrefs.getString(KEY_WORKOUT_REMINDER_TIME, "08:00") ?: "08:00"
    }
    
    suspend fun saveSoundEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putBoolean(KEY_SOUND_ENABLED, enabled)
        }
    }
    
    fun getSoundEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_SOUND_ENABLED, true)
    }
    
    suspend fun saveVibrationEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putBoolean(KEY_VIBRATION_ENABLED, enabled)
        }
    }
    
    fun getVibrationEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_VIBRATION_ENABLED, true)
    }
    
    // Theme Methods
    
    suspend fun saveThemeMode(themeMode: String) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putString(KEY_THEME_MODE, themeMode)
        }
    }
    
    fun getThemeMode(): String {
        return sharedPrefs.getString(KEY_THEME_MODE, "DARK") ?: "DARK"
    }
    
    suspend fun saveUseSystemTheme(useSystemTheme: Boolean) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putBoolean(KEY_USE_SYSTEM_THEME, useSystemTheme)
        }
    }
    
    fun getUseSystemTheme(): Boolean {
        return sharedPrefs.getBoolean(KEY_USE_SYSTEM_THEME, true)
    }
    
    // Units Methods
    
    suspend fun saveUnitSystem(unitSystem: String) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putString(KEY_UNIT_SYSTEM, unitSystem)
        }
    }
    
    fun getUnitSystem(): String {
        return sharedPrefs.getString(KEY_UNIT_SYSTEM, "METRIC") ?: "METRIC"
    }
    
    suspend fun saveWeightUnit(weightUnit: String) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putString(KEY_WEIGHT_UNIT, weightUnit)
        }
    }
    
    fun getWeightUnit(): String {
        return sharedPrefs.getString(KEY_WEIGHT_UNIT, "KILOGRAMS") ?: "KILOGRAMS"
    }
    
    suspend fun saveHeightUnit(heightUnit: String) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putString(KEY_HEIGHT_UNIT, heightUnit)
        }
    }
    
    fun getHeightUnit(): String {
        return sharedPrefs.getString(KEY_HEIGHT_UNIT, "CENTIMETERS") ?: "CENTIMETERS"
    }
    
    suspend fun saveDistanceUnit(distanceUnit: String) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putString(KEY_DISTANCE_UNIT, distanceUnit)
        }
    }
    
    fun getDistanceUnit(): String {
        return sharedPrefs.getString(KEY_DISTANCE_UNIT, "KILOMETERS") ?: "KILOMETERS"
    }
    
    // Reset methods
    
    suspend fun resetNotificationSettings() = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putBoolean(KEY_WORKOUT_REMINDERS, true)
            putBoolean(KEY_REST_TIMER_ALERTS, true)
            putBoolean(KEY_PROGRESS_UPDATES, false)
            putBoolean(KEY_WATER_REMINDERS, false)
            putBoolean(KEY_TIPS_AND_MOTIVATION, true)
            putString(KEY_WORKOUT_REMINDER_TIME, "08:00")
            putBoolean(KEY_SOUND_ENABLED, true)
            putBoolean(KEY_VIBRATION_ENABLED, true)
        }
    }
    
    suspend fun resetUnitSettings() = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putString(KEY_UNIT_SYSTEM, "METRIC")
            putString(KEY_WEIGHT_UNIT, "KILOGRAMS")
            putString(KEY_HEIGHT_UNIT, "CENTIMETERS")
            putString(KEY_DISTANCE_UNIT, "KILOMETERS")
        }
    }
    
    // Nutrition Methods
    
    private val KEY_GEMINI_API_KEY = "gemini_api_key"
    private val KEY_CALORIE_GOAL = "calorie_goal"
    private val KEY_PROTEIN_GOAL = "protein_goal"
    private val KEY_CARB_GOAL = "carb_goal"
    private val KEY_FAT_GOAL = "fat_goal"
    private val KEY_WATER_INTAKE_GOAL = "water_intake_goal"
    private val KEY_WATER_INTAKE_PREFIX = "water_intake_"
    
    fun getGeminiApiKey(): String {
        return sharedPrefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
    }
    
    // Add a non-suspend function for setting API key when used from places like ConfigRepository
    fun setGeminiApiKey(key: String) {
        sharedPrefs.edit {
            putString(KEY_GEMINI_API_KEY, key)
        }
    }
    
    suspend fun setCalorieGoal(goal: Int) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putInt(KEY_CALORIE_GOAL, goal)
        }
    }
    
    fun getCalorieGoal(): Int? {
        if (!sharedPrefs.contains(KEY_CALORIE_GOAL)) return null
        return sharedPrefs.getInt(KEY_CALORIE_GOAL, 2000)
    }
    
    suspend fun setProteinGoal(goal: Float) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putFloat(KEY_PROTEIN_GOAL, goal)
        }
    }
    
    fun getProteinGoal(): Float? {
        if (!sharedPrefs.contains(KEY_PROTEIN_GOAL)) return null
        return sharedPrefs.getFloat(KEY_PROTEIN_GOAL, 125f)
    }
    
    suspend fun setCarbGoal(goal: Float) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putFloat(KEY_CARB_GOAL, goal)
        }
    }
    
    fun getCarbGoal(): Float? {
        if (!sharedPrefs.contains(KEY_CARB_GOAL)) return null
        return sharedPrefs.getFloat(KEY_CARB_GOAL, 250f)
    }
    
    suspend fun setFatGoal(goal: Float) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putFloat(KEY_FAT_GOAL, goal)
        }
    }
    
    fun getFatGoal(): Float? {
        if (!sharedPrefs.contains(KEY_FAT_GOAL)) return null
        return sharedPrefs.getFloat(KEY_FAT_GOAL, 70f)
    }
    
    suspend fun setWaterIntakeGoal(goal: Int) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putInt(KEY_WATER_INTAKE_GOAL, goal)
        }
    }
    
    fun getWaterIntakeGoal(): Int? {
        if (!sharedPrefs.contains(KEY_WATER_INTAKE_GOAL)) return null
        return sharedPrefs.getInt(KEY_WATER_INTAKE_GOAL, 8)
    }
    
    // Store water intake for a specific date, using the date's time in millis as part of the key
    suspend fun setWaterIntake(date: Date, amount: Int) = withContext(Dispatchers.IO) {
        val dateKey = "${KEY_WATER_INTAKE_PREFIX}${date.time}"
        sharedPrefs.edit(commit = true) {
            putInt(dateKey, amount)
        }
    }
    
    // Store water intake for a specific LocalDate
    suspend fun setWaterIntake(date: LocalDate, amount: Int) = withContext(Dispatchers.IO) {
        val dateInMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val dateKey = "${KEY_WATER_INTAKE_PREFIX}${dateInMillis}"
        sharedPrefs.edit(commit = true) {
            putInt(dateKey, amount)
        }
    }
    
    // Get water intake for a specific date
    fun getWaterIntake(date: Date): Int? {
        val dateKey = "${KEY_WATER_INTAKE_PREFIX}${date.time}"
        if (!sharedPrefs.contains(dateKey)) return null
        return sharedPrefs.getInt(dateKey, 0)
    }
    
    // Get water intake for a specific LocalDate
    fun getWaterIntake(date: LocalDate): Int? {
        val dateInMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val dateKey = "${KEY_WATER_INTAKE_PREFIX}${dateInMillis}"
        if (!sharedPrefs.contains(dateKey)) return null
        return sharedPrefs.getInt(dateKey, 0)
    }
    
    // Save activity level
    suspend fun saveActivityLevel(level: String) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putString(KEY_ACTIVITY_LEVEL, level)
        }
    }
    
    // Get activity level
    fun getActivityLevel(): String {
        return sharedPrefs.getString(KEY_ACTIVITY_LEVEL, ACTIVITY_LEVEL_MODERATE) ?: ACTIVITY_LEVEL_MODERATE
    }
    
    // Save gender
    suspend fun saveGender(gender: String) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putString(KEY_GENDER, gender)
        }
    }
    
    // Get gender
    fun getGender(): String {
        return sharedPrefs.getString(KEY_GENDER, "male") ?: "male"
    }
    
    /**
     * Calculate daily calorie goal based on user's profile data
     * Uses the Harris-Benedict equation with activity level modifier
     */
    fun calculateDailyCalorieGoal(): Int {
        val weight = getWeight() // in kg
        val height = getHeight() // in cm
        val age = getAge()
        val gender = getGender()
        val activityLevel = getActivityLevel()
        val goal = getGoal()
        
        // Calculate Basal Metabolic Rate (BMR) using Harris-Benedict equation
        val bmr = if (gender.equals("female", ignoreCase = true)) {
            // BMR for women = 655.1 + (9.563 × weight in kg) + (1.850 × height in cm) - (4.676 × age in years)
            655.1 + (9.563 * weight) + (1.850 * height) - (4.676 * age)
        } else {
            // BMR for men = 66.47 + (13.75 × weight in kg) + (5.003 × height in cm) - (6.755 × age in years)
            66.47 + (13.75 * weight) + (5.003 * height) - (6.755 * age)
        }
        
        // Apply activity level multiplier
        val activityMultiplier = when (activityLevel) {
            ACTIVITY_LEVEL_SEDENTARY -> 1.2 // Little to no exercise
            ACTIVITY_LEVEL_LIGHT -> 1.375 // Light exercise 1-3 days/week
            ACTIVITY_LEVEL_MODERATE -> 1.55 // Moderate exercise 3-5 days/week
            ACTIVITY_LEVEL_ACTIVE -> 1.725 // Active exercise 6-7 days/week
            ACTIVITY_LEVEL_VERY_ACTIVE -> 1.9 // Very active, hard exercise daily
            else -> 1.55 // Default to moderate
        }
        
        // Calculate maintenance calories
        val maintenanceCalories = bmr * activityMultiplier
        
        // Adjust based on goal
        val goalCalories = when (goal) {
            GOAL_LOSE_WEIGHT -> maintenanceCalories * 0.8 // 20% caloric deficit
            GOAL_GAIN_MUSCLE -> maintenanceCalories * 1.1 // 10% caloric surplus
            else -> maintenanceCalories // Maintain weight
        }
        
        return goalCalories.toInt()
    }
    
    // App Intro Methods
    
    suspend fun setIntroVideoMuted(muted: Boolean) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putBoolean(KEY_INTRO_VIDEO_MUTED, muted)
        }
    }
    
    fun isIntroVideoMuted(): Boolean {
        return sharedPrefs.getBoolean(KEY_INTRO_VIDEO_MUTED, false)
    }
    
    suspend fun setSkipIntro(skip: Boolean) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putBoolean(KEY_SKIP_INTRO, skip)
        }
    }
    
    fun getSkipIntro(): Boolean {
        return sharedPrefs.getBoolean(KEY_SKIP_INTRO, false)
    }
    
    // Language Methods
    
    suspend fun setLanguage(languageCode: String) = withContext(Dispatchers.IO) {
        sharedPrefs.edit(commit = true) {
            putString(KEY_LANGUAGE, languageCode)
        }
    }
    
    fun getLanguage(): String {
        return sharedPrefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }

    // Update Check Methods
    fun getLastUpdateCheckTimestamp(): Long {
        return sharedPrefs.getLong(KEY_LAST_UPDATE_CHECK, 0L)
    }

    fun setLastUpdateCheckTimestamp(timestamp: Long) {
        sharedPrefs.edit {
            putLong(KEY_LAST_UPDATE_CHECK, timestamp)
        }
    }

    fun getSkippedUpdateCount(): Int {
        return sharedPrefs.getInt(KEY_SKIPPED_UPDATE_COUNT, 0)
    }

    fun incrementSkippedUpdateCount() {
        val currentCount = getSkippedUpdateCount()
        sharedPrefs.edit {
            putInt(KEY_SKIPPED_UPDATE_COUNT, currentCount + 1)
        }
    }

    fun resetSkippedUpdateCount() {
        sharedPrefs.edit {
            putInt(KEY_SKIPPED_UPDATE_COUNT, 0)
        }
    }
} 