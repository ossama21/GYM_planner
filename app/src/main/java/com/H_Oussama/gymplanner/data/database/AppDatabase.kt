package com.H_Oussama.gymplanner.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.H_Oussama.gymplanner.data.model.CompletedWorkout
import com.H_Oussama.gymplanner.data.model.ExerciseDefinition
import com.H_Oussama.gymplanner.data.model.FoodItem
import com.H_Oussama.gymplanner.data.model.Meal
import com.H_Oussama.gymplanner.data.model.NutritionEntry
import com.H_Oussama.gymplanner.data.model.SetLog
import com.H_Oussama.gymplanner.data.model.WeightEntry
import com.H_Oussama.gymplanner.data.exercise.ExerciseImage

@Database(
    entities = [
        SetLog::class,
        ExerciseDefinition::class,
        NutritionEntry::class,
        FoodItem::class,
        WeightEntry::class,
        Meal::class,
        CompletedWorkout::class,
        ExerciseImage::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun setLogDao(): SetLogDao
    abstract fun exerciseDefinitionDao(): ExerciseDefinitionDao
    abstract fun nutritionEntryDao(): NutritionEntryDao
    abstract fun foodItemDao(): FoodItemDao
    abstract fun weightEntryDao(): WeightEntryDao
    abstract fun mealDao(): MealDao
    abstract fun completedWorkoutDao(): CompletedWorkoutDao
    abstract fun exerciseImageDao(): ExerciseImageDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gym_genius_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}