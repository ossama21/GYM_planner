package com.H_Oussama.gymplanner.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.H_Oussama.gymplanner.data.model.WeightEntry
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface WeightEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weightEntry: WeightEntry): Long

    @Update
    suspend fun update(weightEntry: WeightEntry)

    @Delete
    suspend fun delete(weightEntry: WeightEntry)

    @Query("SELECT * FROM weight_entries WHERE id = :id")
    fun getWeightEntryById(id: String): Flow<WeightEntry?>

    @Query("SELECT * FROM weight_entries ORDER BY date DESC")
    fun getAllWeightEntries(): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries ORDER BY date DESC LIMIT 1")
    fun getLatestWeightEntry(): Flow<WeightEntry?>

    @Query("SELECT * FROM weight_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getWeightEntriesBetweenDates(startDate: Date, endDate: Date): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries ORDER BY date ASC LIMIT :limit")
    fun getWeightEntriesWithLimit(limit: Int): Flow<List<WeightEntry>>
} 
 
 