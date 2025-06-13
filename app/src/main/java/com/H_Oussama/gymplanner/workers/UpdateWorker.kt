package com.H_Oussama.gymplanner.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.H_Oussama.gymplanner.MainActivity
import com.H_Oussama.gymplanner.R
import com.H_Oussama.gymplanner.data.model.GitHubRelease
import com.H_Oussama.gymplanner.data.repositories.UpdateRepository
import com.H_Oussama.gymplanner.data.repositories.UserPreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker that checks for app updates from GitHub
 * Note: This worker is instantiated through a custom WorkerFactory to solve Hilt issues
 */
class UpdateWorker : CoroutineWorker {
    private val updateRepository: UpdateRepository
    private val userPreferencesRepository: UserPreferencesRepository
    private val appContext: Context

    // Primary constructor with all dependencies for our custom factory
    constructor(
        context: Context,
        workerParams: WorkerParameters,
        updateRepository: UpdateRepository,
        userPreferencesRepository: UserPreferencesRepository
    ) : super(context, workerParams) {
        this.appContext = context
        this.updateRepository = updateRepository
        this.userPreferencesRepository = userPreferencesRepository
    }
    
    // Secondary constructor for WorkManager's default factory
    // Will attempt to get dependencies through Hilt
    constructor(context: Context, workerParams: WorkerParameters) : super(context, workerParams) {
        this.appContext = context
        
        // Get dependencies from Hilt
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            UpdateWorkerEntryPoint::class.java
        )
        this.updateRepository = entryPoint.updateRepository()
        this.userPreferencesRepository = entryPoint.userPreferencesRepository()
        Log.d(TAG, "Created UpdateWorker with default constructor")
    }
    
    companion object {
        private const val TAG = "UpdateWorker"
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface UpdateWorkerEntryPoint {
        fun updateRepository(): UpdateRepository
        fun userPreferencesRepository(): UserPreferencesRepository
    }

    override suspend fun doWork(): Result {
        val isDevMode = userPreferencesRepository.isDeveloperModeEnabled()
        Log.d(TAG, "Starting update check. Developer mode: $isDevMode")
        
        return withContext(Dispatchers.IO) {
            try {
                val latestReleaseResult = updateRepository.getLatestRelease()
                
                latestReleaseResult.fold(
                    onSuccess = { release ->
                        processRelease(release, isDevMode)
                        Result.success()
                    },
                    onFailure = { error ->
                        if (isDevMode) {
                            // In developer mode, send a notification about the error
                            Log.e(TAG, "Error checking for updates: ${error.message}", error)
                            showDeveloperNotification(
                                "Update Check Failed",
                                "Error: ${error.message ?: "Unknown error"}"
                            )
                        }
                        Result.failure()
                    }
                )
            } catch (e: Exception) {
                if (isDevMode) {
                    Log.e(TAG, "Exception during update check: ${e.message}", e)
                    showDeveloperNotification(
                        "Update Check Exception",
                        "Exception: ${e.message ?: "Unknown exception"}"
                    )
                }
                Result.failure()
            }
        }
    }
    
    private suspend fun processRelease(release: GitHubRelease, isDevMode: Boolean) {
        try {
            // Get full version name
            val fullVersionName = appContext.packageManager
                .getPackageInfo(appContext.packageName, 0)
                .versionName ?: "0.0.0"
                
            // Get current numeric version for comparison
            val currentVersion = fullVersionName.split("-")[0].trim()
                
            // Parse GitHub release info correctly
            val tagName = release.tag_name.removePrefix("v")
            val latestVersion = if (tagName.contains("-")) {
                tagName.split("-")[0].trim()
            } else {
                tagName.trim()
            }
            
            // Parse a clean release name from the GitHub release
            val gitHubReleaseName = release.name.replace("GYM-Planner", "").trim()
            
            // Log for debug
            Log.d(TAG, "Full current version: $fullVersionName")
            Log.d(TAG, "Current version (numeric): $currentVersion")
            Log.d(TAG, "Full tag name: ${release.tag_name}")
            Log.d(TAG, "Latest version (numeric): $latestVersion")
            Log.d(TAG, "GitHub release name: ${release.name}")
            
            // Record timestamp of check
            userPreferencesRepository.setLastUpdateCheckTimestamp(System.currentTimeMillis())
            
            val newerVersionAvailable = isNewerVersion(latestVersion, currentVersion)
            
            if (isDevMode) {
                // Always show detailed info in developer mode
                showDeveloperNotification(
                    "Update Check Diagnostic",
                    "Current: $fullVersionName\n" + 
                    "Latest: ${release.tag_name}\n" +
                    "Newer version available: $newerVersionAvailable\n" +
                    "Release name: ${release.name}"
                )
                
                // Also show an update notification for testing purposes if there's a newer version
                if (newerVersionAvailable) {
                    showUpdateNotification(
                        "Update Available: ${release.name}",
                        "A new version (${release.tag_name}) is available. Tap to learn more."
                    )
                }
            } else if (newerVersionAvailable) {
                // Only show update notification to users if there's actually an update
                showUpdateNotification(
                    "Update Available: ${release.name}",
                    "A new version (${release.tag_name}) is available. Tap to learn more."
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing release: ${e.message}", e)
            if (isDevMode) {
                showDeveloperNotification(
                    "Release Processing Error",
                    "Error: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
    
    private fun isNewerVersion(latestVersion: String, currentVersion: String): Boolean {
        try {
            // Simple version comparison, assumes format like X.Y.Z
            val latestParts = latestVersion.split(".").map { it.toIntOrNull() ?: 0 }
            val currentParts = currentVersion.split(".").map { it.toIntOrNull() ?: 0 }
            
            for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
                val latest = latestParts.getOrElse(i) { 0 }
                val current = currentParts.getOrElse(i) { 0 }
                if (latest > current) return true
                if (latest < current) return false
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error comparing versions: ${e.message}", e)
            return false
        }
    }

    private fun showUpdateNotification(title: String, description: String) {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("update_channel", "App Updates", NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Notifications about app updates"
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(appContext, "update_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(appContext).notify(1, builder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show notification: ${e.message}", e)
        }
    }
    
    private fun showDeveloperNotification(title: String, description: String) {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "dev_update_channel", 
                "Developer Updates", 
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Developer mode diagnostics"
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(appContext, "dev_update_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("DEV: $title")
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(appContext).notify(2, builder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show notification: ${e.message}", e)
        }
    }
} 