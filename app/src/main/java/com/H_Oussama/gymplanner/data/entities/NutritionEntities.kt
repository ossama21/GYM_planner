package com.H_Oussama.gymplanner.data.entities

import androidx.room.*
import java.util.*

/**
 * Represents a meal type (Breakfast, Lunch, Dinner, Snack)
 */
@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val displayOrder: Int
)

/**
 * Represents a food item that can be used in nutrition entries
 */
@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val calories: Int,
    val protein: Int, // in grams
    val carbs: Int,   // in grams
    val fat: Int,     // in grams
    val servingSize: Float,
    val servingUnit: String,
    val imageUrl: String? = null,
    val isUserCreated: Boolean = true,
    val createdAt: Date = Date()
)

/**
 * Represents a nutritional entry - a specific food eaten at a specific time
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
    val notes: String? = null,
    val createdAt: Date = Date()
)

/**
 * Represents the relationship between a nutrition entry and its food item
 */
data class NutritionEntryWithFood(
    @Embedded val entry: NutritionEntry,
    @Relation(
        parentColumn = "foodItemId",
        entityColumn = "id"
    )
    val foodItem: FoodItem
)

/**
 * Represents a meal with all its nutrition entries and food items
 */
data class MealWithEntries(
    @Embedded val meal: Meal,
    @Relation(
        entity = NutritionEntry::class,
        parentColumn = "id",
        entityColumn = "mealId"
    )
    val entriesWithFood: List<NutritionEntryWithFood>
) 
 
 