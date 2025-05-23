package com.H_Oussama.gymplanner.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.H_Oussama.gymplanner.data.model.CompletedWorkout
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface CompletedWorkoutDao {

    // Insert a completed workout
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletedWorkout(completedWorkout: CompletedWorkout)

    // Get all completed workouts, ordered by date (newest first)
    @Query("SELECT * FROM completed_workouts ORDER BY completionDate DESC")
    fun getAllCompletedWorkouts(): Flow<List<CompletedWorkout>>

    // Get completed workouts within a date range
    @Query("SELECT * FROM completed_workouts WHERE completionDate BETWEEN :startDate AND :endDate ORDER BY completionDate DESC")
    fun getCompletedWorkoutsInRange(startDate: Date, endDate: Date): Flow<List<CompletedWorkout>>

    // Get the most recent completed workout for a specific workoutDayId
    @Query("SELECT * FROM completed_workouts WHERE workoutDayId = :workoutDayId ORDER BY completionDate DESC LIMIT 1")
    suspend fun getMostRecentCompletedWorkout(workoutDayId: String): CompletedWorkout?

    // Find a previous completed instance of the same workout
    @Query("""
        SELECT * FROM completed_workouts 
        WHERE workoutDayId = :workoutDayId 
        AND completionDate < :currentDate 
        ORDER BY completionDate DESC 
        LIMIT 1
    """)
    suspend fun getPreviousCompletedWorkout(workoutDayId: String, currentDate: Date): CompletedWorkout?
    
    // Get completed workouts from previous week (7-14 days ago)
    @Query("""
        SELECT * FROM completed_workouts 
        WHERE completionDate BETWEEN :startDate AND :endDate
        ORDER BY completionDate DESC
    """)
    fun getCompletedWorkoutsFromPreviousWeek(startDate: Date, endDate: Date): Flow<List<CompletedWorkout>>
    
    // Get completed workout by ID
    @Query("SELECT * FROM completed_workouts WHERE id = :id")
    suspend fun getCompletedWorkoutById(id: String): CompletedWorkout?
    
    // Get all completed workouts with the same workoutDayId
    @Query("SELECT * FROM completed_workouts WHERE workoutDayId = :workoutDayId ORDER BY completionDate DESC")
    fun getCompletedWorkoutsByWorkoutDayId(workoutDayId: String): Flow<List<CompletedWorkout>>
} 