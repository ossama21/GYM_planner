package com.H_Oussama.gymplanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * Represents a food item with its nutritional information per standard serving
 */
@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val servingSize: Float,
    val servingUnit: String,
    val calories: Int,       // calories per serving
    val carbs: Double,       // grams per serving
    val protein: Double,     // grams per serving
    val fat: Double,         // grams per serving
    val isCustom: Boolean = true, // Whether this was added by the user or from Gemini API
    val imageUrl: String? = null,
    val createdAt: Date = Date()  // When the food item was created
) 
 
 