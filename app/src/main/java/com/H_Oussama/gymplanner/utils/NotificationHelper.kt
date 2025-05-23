package com.H_Oussama.gymplanner.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.H_Oussama.gymplanner.MainActivity // To launch app on notification click
import com.H_Oussama.gymplanner.R // Assuming ic_launcher exists in drawable/mipmap
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.content.SharedPreferences
import android.util.Log

object NotificationHelper {

    private const val CHANNEL_ID = "workout_timer_channel"
    private const val NOTIFICATION_ID = 1001
    private const val PREFS_NAME = "workout_notification_prefs"
    private const val KEY_SOUND_ENABLED = "sound_enabled"
    private const val TAG = "NotificationHelper"
    
    private var soundEnabled = true
    
    // Initialize preferences
    fun initialize(context: Context?) {
        if (context == null) {
            Log.d(TAG, "Cannot initialize with null context")
            return
        }
        
        createNotificationChannel(context)
        
        // Load sound preference
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true)
    }
    
    // Set sound enabled state and save to preferences
    fun setSoundEnabled(context: Context?, enabled: Boolean) {
        if (context == null) {
            Log.d(TAG, "Cannot set sound enabled with null context")
            return
        }
        
        soundEnabled = enabled
        
        // Save preference
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
        
        // Update notification channel if on Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                
            val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
            channel?.apply {
                setSound(if (enabled) null else null, null) // Null sound when disabled
                notificationManager.createNotificationChannel(this)
            }
        }
    }
    
    // Get sound enabled state
    fun isSoundEnabled(): Boolean {
        return soundEnabled
    }

    fun createNotificationChannel(context: Context?) {
        if (context == null) {
            Log.d(TAG, "Cannot create notification channel with null context")
            return
        }
        
        // Create the NotificationChannel, but only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Timer Alerts"
            val descriptionText = "Notifications for workout timer completion"
            val importance = NotificationManager.IMPORTANCE_HIGH // High importance for timer alerts
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                // Optionally enable lights, vibration, etc.
                 enableVibration(true)
                 setSound(if (soundEnabled) null else null, null) // Initially use system default sound when enabled
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun vibrate(context: Context?, durationMs: Long) {
        if (context == null) {
            Log.d(TAG, "Cannot vibrate with null context")
            return
        }
        
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (e: Exception) {
            // Handle exception (e.g., missing vibration permission)
            Log.e(TAG, "Error vibrating: ${e.message}")
        }
    }

    fun showTimerCompletedNotification(context: Context?, title: String, message: String) {
        if (context == null) {
            Log.d(TAG, "Cannot show notification with null context")
            return
        }
        
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                
            // Control sound at the notification level for older Android versions
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                if (!soundEnabled) {
                    builder.setSilent(true)
                }
            }

            with(NotificationManagerCompat.from(context)) {
                // The notification permission should be checked at app level
                // and requested if not granted
                try {
                    notify(NOTIFICATION_ID, builder.build())
                } catch (e: SecurityException) {
                    // Handle missing notification permission
                    Log.e(TAG, "Error showing notification: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification: ${e.message}")
        }
    }
    
    // Convenience method for rest timer completion
    fun notifyRestComplete(context: Context?) {
        if (context == null) {
            Log.d(TAG, "Cannot notify rest complete with null context")
            return
        }
        
        showTimerCompletedNotification(
            context,
            "Rest Complete",
            "Time to start your next set!"
        )
        
        // Also vibrate
        vibrate(context, 500)
    }
} 