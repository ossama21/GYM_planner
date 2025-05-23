package com.H_Oussama.gymplanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * Represents a weight entry for tracking user weight over time
 */
@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val weight: Float,      // Weight in kg
    val date: Date,         // Date of the entry
    val note: String = ""   // Optional note about the entry
) 
 
 