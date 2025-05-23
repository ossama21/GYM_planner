package com.H_Oussama.gymplanner.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.H_Oussama.gymplanner.data.model.Meal
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: Meal): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(meals: List<Meal>): List<Long>

    @Update
    suspend fun update(meal: Meal)

    @Delete
    suspend fun delete(meal: Meal)

    @Query("SELECT * FROM meals WHERE id = :id")
    suspend fun getMealById(id: String): Meal?

    @Query("SELECT * FROM meals ORDER BY displayOrder ASC")
    fun getAllMeals(): Flow<List<Meal>>

    @Query("SELECT COUNT(*) FROM meals")
    suspend fun getMealCount(): Int

    @Query("DELETE FROM meals")
    suspend fun deleteAllMeals()
} 