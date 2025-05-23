package com.H_Oussama.gymplanner.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * Represents a weight measurement entry
 */
@Entity(
    tableName = "weight_entries",
    indices = [Index("date")]
)
data class WeightEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val weight: Float, // in kg or lbs based on user preference
    val date: Date,
    val bodyFatPercentage: Float? = null,
    val notes: String? = null,
    val createdAt: Date = Date()
) 
 
 