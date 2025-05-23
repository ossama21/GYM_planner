package com.H_Oussama.gymplanner.di

import android.content.Context
import com.H_Oussama.gymplanner.data.database.AppDatabase
import com.H_Oussama.gymplanner.data.database.CompletedWorkoutDao
import com.H_Oussama.gymplanner.data.database.FoodItemDao
import com.H_Oussama.gymplanner.data.database.MealDao
import com.H_Oussama.gymplanner.data.database.NutritionEntryDao
import com.H_Oussama.gymplanner.data.database.WeightEntryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    
    @Provides
    @Singleton
    fun provideCompletedWorkoutDao(appDatabase: AppDatabase): CompletedWorkoutDao {
        return appDatabase.completedWorkoutDao()
    }
    
    @Provides
    @Singleton
    fun provideNutritionEntryDao(appDatabase: AppDatabase): NutritionEntryDao {
        return appDatabase.nutritionEntryDao()
    }
    
    @Provides
    @Singleton
    fun provideFoodItemDao(appDatabase: AppDatabase): FoodItemDao {
        return appDatabase.foodItemDao()
    }
    
    @Provides
    @Singleton
    fun provideMealDao(appDatabase: AppDatabase): MealDao {
        return appDatabase.mealDao()
    }
    
    @Provides
    @Singleton
    fun provideWeightEntryDao(appDatabase: AppDatabase): WeightEntryDao {
        return appDatabase.weightEntryDao()
    }
} 
 
 