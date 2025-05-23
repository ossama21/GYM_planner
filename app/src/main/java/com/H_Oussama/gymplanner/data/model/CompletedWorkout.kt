package com.H_Oussama.gymplanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.H_Oussama.gymplanner.data.database.DateConverter
import java.util.Date

/**
 * Represents a completed workout session.
 * This entity links a workout day with the date it was completed and stores summary data.
 */
@Entity(tableName = "completed_workouts")
@TypeConverters(DateConverter::class)
data class CompletedWorkout(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val workoutDayId: String,
    val workoutDayName: String,
    val completionDate: Date,
    val durationMinutes: Int,
    val totalSetsCompleted: Int,
    val totalReps: Int,
    val totalWeightLifted: Double,
    val caloriesBurned: Double
) 