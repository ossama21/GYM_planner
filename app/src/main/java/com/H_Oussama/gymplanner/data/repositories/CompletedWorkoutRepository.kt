package com.H_Oussama.gymplanner.data.repositories

import com.H_Oussama.gymplanner.data.database.CompletedWorkoutDao
import com.H_Oussama.gymplanner.data.model.CompletedWorkout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class CompletedWorkoutRepository @Inject constructor(
    private val completedWorkoutDao: CompletedWorkoutDao
) {
    
    // Insert a completed workout
    suspend fun insertCompletedWorkout(completedWorkout: CompletedWorkout) {
        completedWorkoutDao.insertCompletedWorkout(completedWorkout)
    }
    
    // Get all completed workouts
    fun getAllCompletedWorkouts(): Flow<List<CompletedWorkout>> {
        return completedWorkoutDao.getAllCompletedWorkouts()
    }
    
    // Get completed workouts within a date range
    fun getCompletedWorkoutsInRange(startDate: Date, endDate: Date): Flow<List<CompletedWorkout>> {
        return completedWorkoutDao.getCompletedWorkoutsInRange(startDate, endDate)
    }
    
    // Get the most recent completed workout for a specific workoutDayId
    suspend fun getMostRecentCompletedWorkout(workoutDayId: String): CompletedWorkout? {
        return completedWorkoutDao.getMostRecentCompletedWorkout(workoutDayId)
    }
    
    // Find a previous completed instance of the same workout
    suspend fun getPreviousCompletedWorkout(workoutDayId: String, currentDate: Date): CompletedWorkout? {
        return completedWorkoutDao.getPreviousCompletedWorkout(workoutDayId, currentDate)
    }
    
    // Get the previous week's version of a workout (7-14 days before the given date)
    suspend fun getPreviousWeekWorkout(workoutDayId: String, date: Date): CompletedWorkout? {
        val calendar = Calendar.getInstance()
        calendar.time = date
        
        // Start date: 14 days before
        calendar.add(Calendar.DAY_OF_YEAR, -14)
        val startDate = calendar.time
        
        // End date: 7 days before
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val endDate = calendar.time
        
        // Query workouts in that range with the same workout day ID
        val workoutsFromPreviousWeek = completedWorkoutDao.getCompletedWorkoutsFromPreviousWeek(startDate, endDate)
        
        // Find the one with closest date
        return completedWorkoutDao.getPreviousCompletedWorkout(workoutDayId, date)
    }
    
    // Get completed workout by ID
    suspend fun getCompletedWorkoutById(id: String): CompletedWorkout? {
        return completedWorkoutDao.getCompletedWorkoutById(id)
    }
    
    // Get all completed workouts with the same workoutDayId
    fun getCompletedWorkoutsByWorkoutDayId(workoutDayId: String): Flow<List<CompletedWorkout>> {
        return completedWorkoutDao.getCompletedWorkoutsByWorkoutDayId(workoutDayId)
    }
} 