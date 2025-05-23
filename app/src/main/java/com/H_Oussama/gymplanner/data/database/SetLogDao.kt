package com.H_Oussama.gymplanner.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.H_Oussama.gymplanner.data.model.SetLog
import kotlinx.coroutines.flow.Flow

@Dao
interface SetLogDao {

    // Insert a single log or replace if ID conflicts (shouldn't happen with UUID)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetLog(log: SetLog)

    // Insert multiple logs (e.g., at end of session)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<SetLog>)

    // Example Query: Get all logs for a specific exercise, ordered by time
    @Query("SELECT * FROM set_logs WHERE exerciseId = :exerciseId ORDER BY timestamp DESC")
    fun getLogsForExercise(exerciseId: String): Flow<List<SetLog>>

    // Example Query: Get all logs (potentially large! use with caution)
    @Query("SELECT * FROM set_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<SetLog>>

    // Query to get distinct exercise IDs that have at least one log entry
    @Query("SELECT DISTINCT exerciseId FROM set_logs ORDER BY exerciseId ASC")
    fun getExerciseIdsWithLogs(): Flow<List<String>>

    // Add other queries as needed (e.g., delete, get logs in date range)
}