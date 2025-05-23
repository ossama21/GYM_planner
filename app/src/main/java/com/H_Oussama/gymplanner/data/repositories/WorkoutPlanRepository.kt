package com.H_Oussama.gymplanner.data.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.H_Oussama.gymplanner.data.model.ExerciseInstance
import com.H_Oussama.gymplanner.data.model.WorkoutDay
import com.H_Oussama.gymplanner.data.model.WorkoutPlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Singleton
import java.util.concurrent.atomic.AtomicBoolean

@Singleton
class WorkoutPlanRepository(
    private val context: Context? = null // Make nullable for testing
) {
    companion object {
        private const val PREFS_NAME = "workout_plan_prefs"
        private const val KEY_WORKOUT_PLAN = "workout_plan"
    }
    
    // In-memory cached plan
    private val _workoutPlanFlow = MutableStateFlow<WorkoutPlan?>(null)
    val workoutPlanFlow: StateFlow<WorkoutPlan?> = _workoutPlanFlow
    
    // Flag to track if we've attempted to load the plan
    private val planLoadAttempted = AtomicBoolean(false)
    
    // Shared preferences instance - lazy initialized
    private val sharedPrefs: SharedPreferences? by lazy {
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // Lazy load plan - only when first requested
    suspend fun ensurePlanLoaded() {
        if (!planLoadAttempted.getAndSet(true) && _workoutPlanFlow.value == null) {
            loadSavedPlan()
        }
    }
    
    private suspend fun loadSavedPlan() = withContext(Dispatchers.IO) {
        if (context == null) return@withContext
        
        try {
            val planJson = sharedPrefs?.getString(KEY_WORKOUT_PLAN, null)
            
            if (planJson != null) {
                val workoutPlan = jsonToWorkoutPlan(planJson)
                _workoutPlanFlow.value = workoutPlan
            }
        } catch (e: Exception) {
            // Handle loading errors (just don't set a plan)
            e.printStackTrace()
        }
    }
    
    suspend fun saveWorkoutPlan(plan: WorkoutPlan) = withContext(Dispatchers.IO) {
        // Update in-memory state
        _workoutPlanFlow.value = plan
        
        // Save to SharedPreferences if context available
        if (context != null) {
            try {
                val planJson = workoutPlanToJson(plan)
                sharedPrefs?.edit(commit = true) {
                    putString(KEY_WORKOUT_PLAN, planJson)
                }
            } catch (e: Exception) {
                // Handle saving errors
                e.printStackTrace()
            }
        }
    }
    
    suspend fun clearWorkoutPlan() = withContext(Dispatchers.IO) {
        // Clear in-memory state
        _workoutPlanFlow.value = null
        
        // Clear from SharedPreferences if context available
        if (context != null) {
            sharedPrefs?.edit(commit = true) {
                remove(KEY_WORKOUT_PLAN)
            }
        }
    }
    
    // --- JSON Serialization --- 
    
    private fun workoutPlanToJson(plan: WorkoutPlan): String {
        val json = JSONObject()
        json.put("planName", plan.planName)
        
        val daysArray = JSONArray()
        plan.days.forEach { day ->
            val dayJson = JSONObject()
            dayJson.put("dayName", day.dayName)
            
            // Save muscle group information
            day.primaryMuscleGroup?.let { 
                dayJson.put("primaryMuscleGroup", it.name)
            }
            
            day.secondaryMuscleGroup?.let {
                dayJson.put("secondaryMuscleGroup", it.name)
            }
            
            // Save rest day flag
            dayJson.put("isRestDay", day.isRestDay)
            
            // Save estimated duration
            dayJson.put("estimatedDuration", day.estimatedDuration)
            
            val exercisesArray = JSONArray()
            day.exercises.forEach { exercise ->
                val exerciseJson = JSONObject()
                exerciseJson.put("exerciseId", exercise.exerciseId)
                exerciseJson.put("setsDescription", exercise.setsDescription)
                exerciseJson.put("restTimeSeconds", exercise.restTimeSeconds)
                exercisesArray.put(exerciseJson)
            }
            
            dayJson.put("exercises", exercisesArray)
            daysArray.put(dayJson)
        }
        
        json.put("days", daysArray)
        return json.toString()
    }
    
    private fun jsonToWorkoutPlan(jsonString: String): WorkoutPlan {
        val json = JSONObject(jsonString)
        val planName = json.getString("planName")
        
        // Pre-allocate collections with appropriate size
        val daysArray = json.getJSONArray("days")
        val daysSize = daysArray.length()
        val days = ArrayList<WorkoutDay>(daysSize)
        
        for (i in 0 until daysSize) {
            val dayJson = daysArray.getJSONObject(i)
            val dayName = dayJson.getString("dayName")
            
            // Restore muscle group information
            val primaryMuscleGroupStr = if (dayJson.has("primaryMuscleGroup")) 
                dayJson.getString("primaryMuscleGroup") else null
            val secondaryMuscleGroupStr = if (dayJson.has("secondaryMuscleGroup")) 
                dayJson.getString("secondaryMuscleGroup") else null
                
            // Convert strings to MuscleGroup enum values
            val primaryMuscleGroup = primaryMuscleGroupStr?.let { 
                com.H_Oussama.gymplanner.data.model.MuscleGroup.valueOf(it) 
            }
            val secondaryMuscleGroup = secondaryMuscleGroupStr?.let { 
                com.H_Oussama.gymplanner.data.model.MuscleGroup.valueOf(it) 
            }
            
            // Restore rest day flag (default to false if not present)
            val isRestDay = if (dayJson.has("isRestDay")) 
                dayJson.getBoolean("isRestDay") else false
                
            // Restore estimated duration (default to 0 if not present)
            val estimatedDuration = if (dayJson.has("estimatedDuration")) 
                dayJson.getInt("estimatedDuration") else 0
            
            val exercisesArray = dayJson.getJSONArray("exercises")
            val exercisesSize = exercisesArray.length()
            val exercises = ArrayList<ExerciseInstance>(exercisesSize)
            
            for (j in 0 until exercisesSize) {
                val exerciseJson = exercisesArray.getJSONObject(j)
                val exerciseId = exerciseJson.getString("exerciseId")
                val setsDescription = exerciseJson.getString("setsDescription")
                val restTimeSeconds = exerciseJson.getInt("restTimeSeconds")
                
                exercises.add(ExerciseInstance(
                    exerciseId = exerciseId,
                    setsDescription = setsDescription,
                    restTimeSeconds = restTimeSeconds
                ))
            }
            
            days.add(WorkoutDay(
                dayName = dayName,
                dayNumber = days.size + 1,
                exercises = exercises,
                primaryMuscleGroup = primaryMuscleGroup,
                secondaryMuscleGroup = secondaryMuscleGroup,
                isRestDay = isRestDay,
                estimatedDuration = estimatedDuration
            ))
        }
        
        return WorkoutPlan(planName, days)
    }
} 