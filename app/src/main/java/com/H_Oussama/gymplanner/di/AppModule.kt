package com.H_Oussama.gymplanner.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.H_Oussama.gymplanner.data.database.AppDatabase
import com.H_Oussama.gymplanner.data.database.ExerciseDefinitionDao
import com.H_Oussama.gymplanner.data.database.ExerciseImageDao
import com.H_Oussama.gymplanner.data.database.SetLogDao
import com.H_Oussama.gymplanner.data.repositories.ConfigRepository
import com.H_Oussama.gymplanner.data.repositories.ExerciseDefinitionRepository
import com.H_Oussama.gymplanner.data.repositories.ExerciseRepository
import com.H_Oussama.gymplanner.data.repositories.SetLogRepository
import com.H_Oussama.gymplanner.data.repositories.WorkoutPlanRepository
import com.H_Oussama.gymplanner.data.repositories.UserPreferencesRepository
import com.H_Oussama.gymplanner.data.datastore.WorkoutPlanSerializer
import com.H_Oussama.gymplanner.ui.workoutplan.WorkoutPlanViewModel
import com.H_Oussama.gymplanner.util.EnhancedImageMatcher
import com.H_Oussama.gymplanner.utils.ExerciseNameNormalizer
// import com.h_oussama.gymplanner.datastore.WorkoutPlanProto
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    // Temporarily comment out DataStore provider
    /*
    @Provides
    @Singleton
    fun provideWorkoutPlanDataStore(
        @ApplicationContext context: Context
    ): DataStore<WorkoutPlanProto> {
        return DataStoreFactory.create(
            serializer = WorkoutPlanSerializer,
            produceFile = { context.filesDir.resolve("workout_plan.pb") },
            corruptionHandler = null,
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        )
    }
    */

    // Comment out the database provider since it's now in DatabaseModule
    /*
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "gym_planner_database"
        )
        // Add migrations if needed
        .fallbackToDestructiveMigration(false) // Placeholder - replace with proper migrations
        .build()
    }
    */

    @Provides
    @Singleton
    fun provideExerciseDefinitionDao(appDatabase: AppDatabase): ExerciseDefinitionDao {
        return appDatabase.exerciseDefinitionDao()
    }

    @Provides
    @Singleton
    fun provideSetLogDao(appDatabase: AppDatabase): SetLogDao {
        return appDatabase.setLogDao()
    }
    
    @Provides
    @Singleton
    fun provideExerciseImageDao(appDatabase: AppDatabase): ExerciseImageDao {
        return appDatabase.exerciseImageDao()
    }

    // Update the repository provider to not depend on DataStore
    @Provides
    @Singleton
    fun provideWorkoutPlanRepository(@ApplicationContext context: Context): WorkoutPlanRepository {
        return WorkoutPlanRepository(context)
    }

    @Provides
    @Singleton
    fun provideExerciseDefinitionRepository(dao: ExerciseDefinitionDao): ExerciseDefinitionRepository {
        return ExerciseDefinitionRepository(dao)
    }

    @Provides
    @Singleton
    fun provideSetLogRepository(dao: SetLogDao): SetLogRepository {
        return SetLogRepository(dao)
    }
    
    @Provides
    @Singleton
    fun provideExerciseRepository(
        exerciseImageDao: ExerciseImageDao,
        @ApplicationContext context: Context
    ): ExerciseRepository {
        return ExerciseRepository(exerciseImageDao, context)
    }
    
    @Provides
    @Singleton
    fun provideConfigRepository(@ApplicationContext context: Context): ConfigRepository {
        return ConfigRepository(context)
    }
    
    @Provides
    @Singleton
    fun provideExerciseNameNormalizer(
        configRepository: ConfigRepository,
        @ApplicationContext context: Context
    ): ExerciseNameNormalizer {
        return ExerciseNameNormalizer(configRepository, context)
    }

    @Provides
    @Singleton
    fun provideWorkoutPlanViewModel(
        exerciseDefinitionRepository: ExerciseDefinitionRepository,
        workoutPlanRepository: WorkoutPlanRepository,
        enhancedImageMatcher: EnhancedImageMatcher,
        exerciseNameNormalizer: ExerciseNameNormalizer,
        @ApplicationContext context: Context
    ): WorkoutPlanViewModel {
        return WorkoutPlanViewModel(
            exerciseDefinitionRepository, 
            workoutPlanRepository, 
            enhancedImageMatcher, 
            exerciseNameNormalizer,
            context
        )
    }
} 