package com.H_Oussama.gymplanner.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.H_Oussama.gymplanner.data.exercise.ExerciseImage
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseImageDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseImage(exerciseImage: ExerciseImage)
    
    @Query("SELECT * FROM exercise_images WHERE exerciseId = :exerciseId LIMIT 1")
    fun getExerciseImageByExerciseId(exerciseId: String): Flow<ExerciseImage?>
    
    @Query("SELECT * FROM exercise_images WHERE exerciseId = :exerciseId LIMIT 1")
    suspend fun getExerciseImageByExerciseIdOnce(exerciseId: String): ExerciseImage?
    
    @Query("DELETE FROM exercise_images WHERE exerciseId = :exerciseId")
    suspend fun deleteExerciseImagesByExerciseId(exerciseId: String)
    
    @Query("DELETE FROM exercise_images WHERE id = :id")
    suspend fun deleteExerciseImageById(id: String)
} 