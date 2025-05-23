package com.H_Oussama.gymplanner.data.repositories

import com.H_Oussama.gymplanner.data.database.SetLogDao
import com.H_Oussama.gymplanner.data.model.SetLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class SetLogRepository(private val setLogDao: SetLogDao?) {

    // Expose query flows directly from DAO
    fun getLogsForExercise(exerciseId: String): Flow<List<SetLog>> {
        return setLogDao?.getLogsForExercise(exerciseId) ?: emptyFlow()
    }

    fun getAllLogs(): Flow<List<SetLog>> {
        return setLogDao?.getAllLogs() ?: emptyFlow()
    }

    // Suspend function for insertion
    suspend fun insertSetLog(log: SetLog) {
        setLogDao?.insertSetLog(log)
    }

     suspend fun insertAllLogs(logs: List<SetLog>) {
        setLogDao?.insertAll(logs)
    }

    // Expose query for distinct exercise IDs with logs
    fun getExerciseIdsWithLogs(): Flow<List<String>> {
        return setLogDao?.getExerciseIdsWithLogs() ?: emptyFlow()
    }

    // Add other methods like delete if needed
}