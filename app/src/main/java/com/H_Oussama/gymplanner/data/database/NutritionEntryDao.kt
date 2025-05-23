package com.H_Oussama.gymplanner.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.H_Oussama.gymplanner.data.model.NutritionEntry
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface NutritionEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(nutritionEntry: NutritionEntry): Long

    @Update
    suspend fun update(nutritionEntry: NutritionEntry)

    @Delete
    suspend fun delete(nutritionEntry: NutritionEntry)

    @Query("SELECT * FROM nutrition_entries WHERE id = :id")
    fun getEntryById(id: String): Flow<NutritionEntry?>

    @Query("SELECT * FROM nutrition_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getEntriesBetweenDates(startDate: Date, endDate: Date): Flow<List<NutritionEntry>>

    @Query("SELECT * FROM nutrition_entries WHERE date = :date AND mealId = :mealId ORDER BY date DESC")
    fun getEntriesForMealId(date: Date, mealId: String): Flow<List<NutritionEntry>>

    @Query("SELECT SUM(calories) FROM nutrition_entries WHERE date = :date")
    fun getTotalCaloriesForDate(date: Date): Flow<Int?>

    @Query("SELECT SUM(carbs) FROM nutrition_entries WHERE date = :date")
    fun getTotalCarbsForDate(date: Date): Flow<Double?>

    @Query("SELECT SUM(protein) FROM nutrition_entries WHERE date = :date")
    fun getTotalProteinForDate(date: Date): Flow<Double?>

    @Query("SELECT SUM(fat) FROM nutrition_entries WHERE date = :date")
    fun getTotalFatForDate(date: Date): Flow<Double?>

    @Transaction
    @Query("SELECT * FROM nutrition_entries WHERE date = :date ORDER BY mealId, date DESC")
    fun getEntriesForDate(date: Date): Flow<List<NutritionEntry>>

    @Query("DELETE FROM nutrition_entries WHERE date = :date")
    suspend fun deleteEntriesForDate(date: Date)
} 
 
 