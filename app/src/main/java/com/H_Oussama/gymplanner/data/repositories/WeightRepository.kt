package com.H_Oussama.gymplanner.data.repositories

import com.H_Oussama.gymplanner.data.database.WeightEntryDao
import com.H_Oussama.gymplanner.data.model.WeightEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing weight tracking data
 */
@Singleton
class WeightRepository @Inject constructor(
    private val weightDao: WeightEntryDao
) {
    
    suspend fun addWeightEntry(entry: WeightEntry): Long {
        return weightDao.insert(entry)
    }
    
    suspend fun updateWeightEntry(entry: WeightEntry) {
        weightDao.update(entry)
    }
    
    suspend fun deleteWeightEntry(entry: WeightEntry) {
        weightDao.delete(entry)
    }
    
    fun getWeightEntryById(id: String): Flow<WeightEntry?> {
        return weightDao.getWeightEntryById(id)
    }
    
    fun getAllWeightEntries(): Flow<List<WeightEntry>> {
        return weightDao.getAllWeightEntries()
    }
    
    fun getLatestWeightEntry(): Flow<WeightEntry?> {
        return weightDao.getLatestWeightEntry()
    }
    
    fun getWeightEntriesWithLimit(limit: Int): Flow<List<WeightEntry>> {
        return weightDao.getWeightEntriesWithLimit(limit)
    }
    
    // Helper methods with date ranges
    suspend fun getWeeklyWeightEntries(): List<WeightEntry> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = calendar.time
        
        return getWeightEntriesBetweenDates(startDate, endDate)
    }
    
    suspend fun getMonthlyWeightEntries(): List<WeightEntry> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        
        calendar.add(Calendar.MONTH, -1)
        val startDate = calendar.time
        
        return getWeightEntriesBetweenDates(startDate, endDate)
    }
    
    // Get min and max weight from all entries
    fun getMinMaxWeight(): Flow<Pair<Float?, Float?>> {
        return getAllWeightEntries().map { entries ->
            if (entries.isEmpty()) {
                Pair(null, null)
            } else {
                val minWeight = entries.minByOrNull { it.weight }?.weight
                val maxWeight = entries.maxByOrNull { it.weight }?.weight
                Pair(minWeight, maxWeight)
            }
        }
    }
    
    // Calculate average weight in date range
    suspend fun getAverageWeightInRange(startDate: Date, endDate: Date): Float? {
        val entries = getWeightEntriesBetweenDates(startDate, endDate)
        if (entries.isEmpty()) return null
        
        val sum = entries.sumOf { it.weight.toDouble() }
        return (sum / entries.size).toFloat()
    }
    
    // Adapter method for synchronous APIs
    suspend fun getWeightEntriesBetweenDates(startDate: Date, endDate: Date): List<WeightEntry> {
        return weightDao.getWeightEntriesBetweenDates(startDate, endDate).first()
    }
} 
 
 