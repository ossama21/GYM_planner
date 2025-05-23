package com.H_Oussama.gymplanner.data.model

import androidx.room.Embedded
import androidx.room.Relation

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
 
 