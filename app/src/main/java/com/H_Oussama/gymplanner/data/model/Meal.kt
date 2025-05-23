package com.H_Oussama.gymplanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

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
 
 