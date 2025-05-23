package com.H_Oussama.gymplanner.data.repositories

import android.content.Context
import com.H_Oussama.gymplanner.data.database.ExerciseImageDao
import com.H_Oussama.gymplanner.data.exercise.ExerciseDefinition
import com.H_Oussama.gymplanner.data.exercise.ExerciseImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepository @Inject constructor(
    private val exerciseImageDao: ExerciseImageDao,
    private val context: Context
) {
    
    // Insert a new exercise image
    suspend fun insertExerciseImage(exerciseImage: ExerciseImage) {
        exerciseImageDao.insertExerciseImage(exerciseImage)
    }
    
    // Get an exercise image by exercise ID
    fun getExerciseImageByExerciseId(exerciseId: String): Flow<ExerciseImage?> {
        return exerciseImageDao.getExerciseImageByExerciseId(exerciseId)
    }
    
    // Get an exercise image by exercise ID (one-time check)
    suspend fun getExerciseImageByExerciseIdOnce(exerciseId: String): ExerciseImage? {
        return exerciseImageDao.getExerciseImageByExerciseIdOnce(exerciseId)
    }
    
    // Delete exercise images for a specific exercise
    suspend fun deleteExerciseImages(exerciseId: String) {
        exerciseImageDao.deleteExerciseImagesByExerciseId(exerciseId)
    }
    
    // Delete a specific exercise image
    suspend fun deleteExerciseImage(imageId: String) {
        exerciseImageDao.deleteExerciseImageById(imageId)
    }
} 