package com.H_Oussama.gymplanner.data.repositories

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import javax.inject.Inject

/**
 * Repository for managing app configuration settings including nutrition goals
 * Note: Gemini API key handling has been centralized in UserPreferencesRepository
 */
class ConfigRepository @Inject constructor(
    applicationContext: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val sharedPreferences: SharedPreferences = applicationContext.getSharedPreferences(
        PREFERENCES_NAME, Context.MODE_PRIVATE
    )

    /**
     * Retrieves the Gemini API key from UserPreferencesRepository
     * This is a bridge method to maintain compatibility with existing code
     */
    fun getGeminiApiKey(): String {
        val apiKey = userPreferencesRepository.getGeminiApiKey()
        Log.d("ConfigRepository", "Getting Gemini API key from UserPreferencesRepository (length: ${apiKey.length})")
        return apiKey
    }

    /**
     * Saves the Gemini API key to UserPreferencesRepository
     * This is a bridge method to maintain compatibility with existing code
     */
    fun setGeminiApiKey(apiKey: String) {
        Log.d("ConfigRepository", "Setting Gemini API key in UserPreferencesRepository (length: ${apiKey.length})")
        // Launch a coroutine in ApplicationScope to set the API key
        userPreferencesRepository.setGeminiApiKey(apiKey)
    }

    /**
     * Get daily calorie goal
     */
    fun getDailyCalorieGoal(): Int {
        return sharedPreferences.getInt(KEY_CALORIE_GOAL, DEFAULT_CALORIE_GOAL)
    }

    /**
     * Set daily calorie goal
     */
    fun setDailyCalorieGoal(calories: Int) {
        sharedPreferences.edit().putInt(KEY_CALORIE_GOAL, calories).apply()
    }

    /**
     * Get daily protein goal (in grams)
     */
    fun getDailyProteinGoal(): Int {
        return sharedPreferences.getInt(KEY_PROTEIN_GOAL, DEFAULT_PROTEIN_GOAL)
    }

    /**
     * Set daily protein goal (in grams)
     */
    fun setDailyProteinGoal(grams: Int) {
        sharedPreferences.edit().putInt(KEY_PROTEIN_GOAL, grams).apply()
    }

    /**
     * Get daily carb goal (in grams)
     */
    fun getDailyCarbGoal(): Int {
        return sharedPreferences.getInt(KEY_CARB_GOAL, DEFAULT_CARB_GOAL)
    }

    /**
     * Set daily carb goal (in grams)
     */
    fun setDailyCarbGoal(grams: Int) {
        sharedPreferences.edit().putInt(KEY_CARB_GOAL, grams).apply()
    }

    /**
     * Get daily fat goal (in grams)
     */
    fun getDailyFatGoal(): Int {
        return sharedPreferences.getInt(KEY_FAT_GOAL, DEFAULT_FAT_GOAL)
    }

    /**
     * Set daily fat goal (in grams)
     */
    fun setDailyFatGoal(grams: Int) {
        sharedPreferences.edit().putInt(KEY_FAT_GOAL, grams).apply()
    }

    /**
     * Get daily water intake goal (in ml)
     */
    fun getDailyWaterGoal(): Int {
        return sharedPreferences.getInt(KEY_WATER_GOAL, DEFAULT_WATER_GOAL)
    }

    /**
     * Set daily water intake goal (in ml)
     */
    fun setDailyWaterGoal(ml: Int) {
        sharedPreferences.edit().putInt(KEY_WATER_GOAL, ml).apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "gym_planner_config"
        
        // Nutrition Goals
        private const val KEY_CALORIE_GOAL = "calorie_goal"
        private const val KEY_PROTEIN_GOAL = "protein_goal"
        private const val KEY_CARB_GOAL = "carb_goal"
        private const val KEY_FAT_GOAL = "fat_goal"
        private const val KEY_WATER_GOAL = "water_goal"
        
        // Default values
        private const val DEFAULT_CALORIE_GOAL = 2000
        private const val DEFAULT_PROTEIN_GOAL = 150
        private const val DEFAULT_CARB_GOAL = 200
        private const val DEFAULT_FAT_GOAL = 65
        private const val DEFAULT_WATER_GOAL = 2500 // 2.5 liters
    }
} 
 