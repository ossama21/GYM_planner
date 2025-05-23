package com.H_Oussama.gymplanner.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.H_Oussama.gymplanner.data.model.FoodItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodItem: FoodItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foodItems: List<FoodItem>)

    @Update
    suspend fun update(foodItem: FoodItem)

    @Delete
    suspend fun delete(foodItem: FoodItem)

    @Query("SELECT * FROM food_items WHERE id = :id")
    fun getFoodItemById(id: String): Flow<FoodItem?>

    @Query("SELECT * FROM food_items ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentFoodItems(limit: Int): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC LIMIT 20")
    fun searchFoodItems(query: String): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items ORDER BY name ASC")
    fun getAllFoodItems(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE isCustom = :isCustom ORDER BY name ASC")
    fun getFoodItemsByCustomFlag(isCustom: Boolean): Flow<List<FoodItem>>
} 
 
 