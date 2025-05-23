package com.H_Oussama.gymplanner.data.exercise

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity to store references to exercise images, either in assets or locally stored.
 * 
 * @param id Unique identifier for this image
 * @param exerciseId The ID of the exercise definition this image belongs to
 * @param imageUri The URI or path to the image file
 * @param isAsset Whether the image is stored in the app's assets (true) or locally (false)
 */
@Entity(
    tableName = "exercise_images",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseDefinition::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseId")]
)
data class ExerciseImage(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val exerciseId: String,
    val imageUri: String,
    val isAsset: Boolean = false
) 