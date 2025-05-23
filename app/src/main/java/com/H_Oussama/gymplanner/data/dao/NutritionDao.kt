package com.H_Oussama.gymplanner.data.dao

import androidx.room.*
import com.H_Oussama.gymplanner.data.model.FoodItem
import com.H_Oussama.gymplanner.data.model.Meal
import com.H_Oussama.gymplanner.data.model.NutritionEntry
import com.H_Oussama.gymplanner.data.model.MealWithEntries
import com.H_Oussama.gymplanner.data.model.NutritionEntryWithFood
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Object for nutrition related tables (food_items, meals, nutrition_entries)
 */
@Dao
interface NutritionDao {
    
    // Food Items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodItem: FoodItem): Long
    
    @Update
    suspend fun update(foodItem: FoodItem)
    
    @Delete
    suspend fun delete(foodItem: FoodItem)
    
    @Query("SELECT * FROM food_items WHERE id = :id")
    fun getFoodItemById(id: String): Flow<FoodItem?>
    
    @Query("SELECT * FROM food_items WHERE name LIKE '%' || :query || '%' ORDER BY name ASC LIMIT 20")
    fun searchFoodItems(query: String): Flow<List<FoodItem>>
    
    @Query("SELECT * FROM food_items ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentFoodItems(limit: Int): Flow<List<FoodItem>>
    
    // Meals
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: Meal): Long
    
    @Query("SELECT * FROM meals WHERE id = :id")
    suspend fun getMealById(id: String): Meal?
    
    @Query("SELECT * FROM meals ORDER BY displayOrder ASC")
    fun getAllMeals(): Flow<List<Meal>>
    
    // Nutrition Entries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: NutritionEntry): Long
    
    @Update
    suspend fun update(entry: NutritionEntry)
    
    @Delete
    suspend fun delete(entry: NutritionEntry)
    
    @Query("SELECT * FROM nutrition_entries WHERE id = :id")
    fun getEntryById(id: String): Flow<NutritionEntry?>
    
    @Query("SELECT * FROM nutrition_entries WHERE date = :date")
    fun getEntriesForDate(date: Date): Flow<List<NutritionEntry>>
    
    @Query("SELECT SUM(calories) FROM nutrition_entries WHERE date = :date")
    fun getTotalCaloriesForDate(date: Date): Flow<Int?>
    
    @Query("SELECT SUM(carbs) FROM nutrition_entries WHERE date = :date")
    fun getTotalCarbsForDate(date: Date): Flow<Double?>
    
    @Query("SELECT SUM(protein) FROM nutrition_entries WHERE date = :date")
    fun getTotalProteinForDate(date: Date): Flow<Double?>
    
    @Query("SELECT SUM(fat) FROM nutrition_entries WHERE date = :date")
    fun getTotalFatForDate(date: Date): Flow<Double?>
    
    @Transaction
    @Query("SELECT * FROM nutrition_entries WHERE date = :date")
    suspend fun getNutritionEntriesForDate(date: Date): List<NutritionEntryWithFood>
    
    @Transaction
    @Query("""
        SELECT 
            m.*, 
            COUNT(ne.id) as entryCount
        FROM meals m
        LEFT JOIN nutrition_entries ne ON m.id = ne.mealId AND ne.date = :date
        GROUP BY m.id
        ORDER BY m.displayOrder
    """)
    suspend fun getMealsWithEntriesForDate(date: Date): List<MealWithEntries>
    
    @Query("""
        SELECT 
            SUM(fi.calories * ne.numberOfServings) as totalCalories,
            SUM(fi.protein * ne.numberOfServings) as totalProtein,
            SUM(fi.carbs * ne.numberOfServings) as totalCarbs,
            SUM(fi.fat * ne.numberOfServings) as totalFat
        FROM nutrition_entries ne
        JOIN food_items fi ON ne.foodItemId = fi.id
        WHERE ne.date = :date
    """)
    suspend fun getTotalNutritionForDate(date: Date): NutritionTotals?
    
    // Initialize default meals if none exist
    @Transaction
    suspend fun initializeDefaultMealsIfNeeded() {
        val mealCount = getMealCount()
        if (mealCount == 0) {
            val defaultMeals = listOf(
                Meal(name = "Breakfast", displayOrder = 0),
                Meal(name = "Lunch", displayOrder = 1),
                Meal(name = "Dinner", displayOrder = 2),
                Meal(name = "Snacks", displayOrder = 3)
            )
            defaultMeals.forEach { insert(it) }
        }
    }
    
    @Query("SELECT COUNT(*) FROM meals")
    suspend fun getMealCount(): Int
}

/**
 * Simple data class to hold nutrition totals for a day
 */
data class NutritionTotals(
    val totalCalories: Int = 0,
    val totalProtein: Int = 0,
    val totalCarbs: Int = 0,
    val totalFat: Int = 0
) 
 
 