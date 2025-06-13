package com.H_Oussama.gymplanner.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.hilt.work.HiltWorkerFactory
import com.H_Oussama.gymplanner.data.repositories.UpdateRepository
import com.H_Oussama.gymplanner.data.repositories.UserPreferencesRepository
import javax.inject.Inject

class HiltWorkerFactory @Inject constructor(
    private val workerFactory: androidx.hilt.work.HiltWorkerFactory
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return workerFactory.createWorker(appContext, workerClassName, workerParameters)
    }
} 