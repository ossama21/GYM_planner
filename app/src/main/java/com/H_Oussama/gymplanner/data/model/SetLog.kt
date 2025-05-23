package com.H_Oussama.gymplanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.H_Oussama.gymplanner.data.database.DateConverter
import java.util.Date

/**
 * Represents a log entry for a single set performed by the user.
 * Annotated as a Room entity.
 *
 * @param id Unique identifier for this log entry.
 * @param exerciseId Links to the [ExerciseDefinition] performed.
 * @param timestamp The date and time when the set was completed.
 * @param reps The number of repetitions performed.
 * @param weight The weight used for the set (optional, units implied by user context).
 * @param durationSeconds The duration of the set in seconds.
 * @param caloriesBurned The estimated calories burned during this set.
 */
@Entity(tableName = "set_logs")
@TypeConverters(DateConverter::class)
data class SetLog(
    @PrimaryKey val id: String, // Use id as primary key
    val exerciseId: String, // Consider adding an index if queried often
    val timestamp: Date,
    val reps: Int,
    val weight: Double? = null, // Can be null if bodyweight or not tracked
    val durationSeconds: Int = 0,
    val caloriesBurned: Double = 0.0
)