package com.H_Oussama.gymplanner.data.repositories

import com.H_Oussama.gymplanner.data.database.ExerciseDefinitionDao
import com.H_Oussama.gymplanner.data.model.ExerciseDefinition
import com.H_Oussama.gymplanner.data.model.primaryMuscleGroup
import com.H_Oussama.gymplanner.data.model.secondaryMuscleGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map

class ExerciseDefinitionRepository(private val exerciseDefinitionDao: ExerciseDefinitionDao?) {

    // Get all definitions as a Flow
    fun getAllDefinitions(): Flow<List<ExerciseDefinition>> {
        return exerciseDefinitionDao?.getAllExerciseDefinitions() ?: emptyFlow()
    }

    // Get a specific definition by ID as a Flow
    fun getDefinitionById(id: String): Flow<ExerciseDefinition?> {
        return exerciseDefinitionDao?.getExerciseDefinitionById(id) ?: emptyFlow()
    }

    // Get a specific definition by ID (one-time check)
    suspend fun getDefinitionByIdOnce(id: String): ExerciseDefinition? {
        return exerciseDefinitionDao?.getExerciseDefinitionByIdOnce(id)
    }

    // Find definition by name (one-time check)
    suspend fun findDefinitionByName(name: String): ExerciseDefinition? {
        return exerciseDefinitionDao?.findExerciseDefinitionByName(name)
    }

    // Insert a definition
    suspend fun insertDefinition(definition: ExerciseDefinition) {
        exerciseDefinitionDao?.insertExerciseDefinition(definition)
    }

    // Get definitions by muscle group
    fun getDefinitionsByMuscleGroup(muscleGroupName: String): Flow<List<ExerciseDefinition>> {
        // Assuming muscleGroupName is the string representation of MuscleGroup enum name (e.g., "CHEST")
        // Or it could be the display name like "Chest". The comparison needs to be consistent.
        // For now, let's assume it's the enum name or a case-insensitive match to the display name.
        return exerciseDefinitionDao?.getAllExerciseDefinitions()?.map { definitions ->
            definitions.filter { exercise ->
                val primaryMatch = exercise.primaryMuscleGroup?.name?.equals(muscleGroupName, ignoreCase = true) == true || 
                                 exercise.primaryMuscleGroup?.getDisplayName()?.equals(muscleGroupName, ignoreCase = true) == true
                val secondaryMatch = exercise.secondaryMuscleGroup?.name?.equals(muscleGroupName, ignoreCase = true) == true ||
                                   exercise.secondaryMuscleGroup?.getDisplayName()?.equals(muscleGroupName, ignoreCase = true) == true
                primaryMatch || secondaryMatch
            }
        } ?: emptyFlow()
    }

    // Potential future methods: updateDefinition, deleteDefinition
}