package com.H_Oussama.gymplanner.data.dao

import androidx.room.*
import com.H_Oussama.gymplanner.data.model.WeightEntry
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Data Access Object for weight entries table
 */
@Dao
interface WeightDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightEntry(entry: WeightEntry): Long
    
    @Update
    suspend fun updateWeightEntry(entry: WeightEntry)
    
    @Delete
    suspend fun deleteWeightEntry(entry: WeightEntry)
    
    @Query("SELECT * FROM weight_entries WHERE id = :id")
    suspend fun getWeightEntryById(id: String): WeightEntry?
    
    @Query("SELECT * FROM weight_entries ORDER BY date DESC")
    fun getAllWeightEntries(): Flow<List<WeightEntry>>
    
    @Query("SELECT * FROM weight_entries ORDER BY date DESC LIMIT 1")
    fun getLatestWeightEntry(): Flow<WeightEntry?>
    
    @Query("SELECT * FROM weight_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getWeightEntriesInRange(startDate: Date, endDate: Date): List<WeightEntry>
    
    @Query("SELECT * FROM weight_entries WHERE date = :date LIMIT 1")
    suspend fun getWeightEntryForDate(date: Date): WeightEntry?
    
    @Query("SELECT MIN(weight) FROM weight_entries")
    suspend fun getMinWeight(): Float?
    
    @Query("SELECT MAX(weight) FROM weight_entries")
    suspend fun getMaxWeight(): Float?
    
    @Query("SELECT AVG(weight) FROM weight_entries WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAverageWeightInRange(startDate: Date, endDate: Date): Float?
} 
 
 