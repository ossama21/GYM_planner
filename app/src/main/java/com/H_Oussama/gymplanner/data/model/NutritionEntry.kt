package com.H_Oussama.gymplanner.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID
import java.util.Calendar

/**
 * Utility function to normalize a date by setting time to midnight
 * This ensures consistent date comparison without time components
 */
fun Date.normalizeDate(): Date {
    return Calendar.getInstance().apply {
        time = this@normalizeDate
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time
}

/**
 * Represents a food item consumed by the user on a specific date
 */
@Entity(
    tableName = "nutrition_entries",
    foreignKeys = [
        ForeignKey(
            entity = FoodItem::class,
            parentColumns = ["id"],
            childColumns = ["foodItemId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Meal::class,
            parentColumns = ["id"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("foodItemId"),
        Index("mealId"),
        Index("date")
    ]
)
data class NutritionEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val foodItemId: String,
    val mealId: String,
    val date: Date,
    val servingSize: Float,
    val numberOfServings: Float,
    val calories: Int,       // Calculated based on serving size and food item
    val carbs: Double,       // Calculated based on serving size and food item
    val protein: Double,     // Calculated based on serving size and food item
    val fat: Double,         // Calculated based on serving size and food item
    val notes: String? = null,
    val createdAt: Date = Date()
) {
    companion object {
        fun createFromFoodItem(
            foodItem: FoodItem,
            mealId: String,
            servingSize: Float,
            date: Date = Date()
        ): NutritionEntry {
            val servingRatio = servingSize / foodItem.servingSize
            return NutritionEntry(
                foodItemId = foodItem.id,
                mealId = mealId,
                date = date.normalizeDate(),
                servingSize = servingSize,
                numberOfServings = servingRatio,
                calories = (foodItem.calories * servingRatio).toInt(),
                carbs = foodItem.carbs * servingRatio,
                protein = foodItem.protein * servingRatio,
                fat = foodItem.fat * servingRatio,
                notes = "Added manually"
            )
        }
    }
} 