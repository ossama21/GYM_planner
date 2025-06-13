package com.H_Oussama.gymplanner.di

import android.content.Context
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.H_Oussama.gymplanner.data.repositories.UpdateRepository
import com.H_Oussama.gymplanner.data.repositories.UserPreferencesRepository
import com.H_Oussama.gymplanner.workers.UpdateWorker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    
    /**
     * Factory provider for UpdateWorker to solve Hilt-WorkManager integration issues
     */
    @Singleton
    @Provides
    fun provideCustomWorkerFactory(
        updateRepository: UpdateRepository,
        userPreferencesRepository: UserPreferencesRepository
    ): WorkerFactory {
        return UpdateWorkerFactory(updateRepository, userPreferencesRepository)
    }
}

/**
 * Custom WorkerFactory for UpdateWorker to handle dependency injection manually
 * This solves an issue where Hilt's WorkerFactory doesn't properly create workers with constructor injection
 */
class UpdateWorkerFactory @Inject constructor(
    private val updateRepository: UpdateRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : WorkerFactory() {
    
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): UpdateWorker? {
        return if (workerClassName == UpdateWorker::class.java.name) {
            UpdateWorker(
                appContext,
                workerParameters,
                updateRepository,
                userPreferencesRepository
            )
        } else {
            null
        }
    }
} 