package com.H_Oussama.gymplanner.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.H_Oussama.gymplanner.data.model.ExerciseDefinition
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDefinitionDao {

    // Insert a definition, ignore if ID already exists (parser might try to insert duplicates initially)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExerciseDefinition(definition: ExerciseDefinition)

    // Get a specific definition by its ID
    @Query("SELECT * FROM exercise_definitions WHERE id = :id LIMIT 1")
    fun getExerciseDefinitionById(id: String): Flow<ExerciseDefinition?> // Flow allows observing changes

     // Get a specific definition by its ID (non-Flow version for parser?)
     @Query("SELECT * FROM exercise_definitions WHERE id = :id LIMIT 1")
     suspend fun getExerciseDefinitionByIdOnce(id: String): ExerciseDefinition? // Suspend fun for one-time check

    // Get all definitions
    @Query("SELECT * FROM exercise_definitions ORDER BY name ASC")
    fun getAllExerciseDefinitions(): Flow<List<ExerciseDefinition>>

    // Find definition by name (case-insensitive)
    @Query("SELECT * FROM exercise_definitions WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun findExerciseDefinitionByName(name: String): ExerciseDefinition?
}