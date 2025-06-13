package com.H_Oussama.gymplanner

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.H_Oussama.gymplanner.ui.GymApp // Import the main App composable
import com.H_Oussama.gymplanner.utils.NotificationHelper // Import helper
import dagger.hilt.android.AndroidEntryPoint // Import Hilt annotation
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.H_Oussama.gymplanner.data.repositories.UserPreferencesRepository
import javax.inject.Inject
import com.H_Oussama.gymplanner.ui.theme.GymPlannerTheme
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.H_Oussama.gymplanner.data.repositories.UpdateRepository
import com.H_Oussama.gymplanner.data.model.GitHubRelease
import com.H_Oussama.gymplanner.ui.common.UpdateDialog
import java.io.File
import androidx.core.content.FileProvider
import android.app.DownloadManager
import android.net.Uri
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import android.util.Log

@AndroidEntryPoint // Add Hilt annotation
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository
    
    @Inject
    lateinit var updateRepository: UpdateRepository
    
    private var releaseToInstall by mutableStateOf<GitHubRelease?>(null)
    
    // Request permissions for notifications (required for Android 13+)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.d("MainActivity", "Notification permission denied")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize notification helper
        NotificationHelper.initialize(this)
        
        // Request notification permission for Android 13+
        requestNotificationPermission()
        
        // Apply saved language
        applyLanguage()
        
        setContent {
            GymPlannerTheme {
                GymApp()
                UpdateScreen()
            }
        }
        
        // Check for updates only if explicitly needed, otherwise rely on the scheduled worker
        // This prevents notification spam on each app start
        maybeCheckForUpdateOnFirstRun()
    }
    
    /**
     * Only check for updates on first app run to avoid notification spam
     */
    private fun maybeCheckForUpdateOnFirstRun() {
        lifecycleScope.launch {
            val firstRun = userPreferencesRepository.isFirstRun()
            if (firstRun) {
                userPreferencesRepository.setFirstRun(false)
                // Only check for updates on first run, not every time the app starts
                checkForUpdate()
            }
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    private fun applyLanguage() {
        val languageCode = userPreferencesRepository.getLanguage()
        GymPlannerApplication.setLocale(this, languageCode)
    }
    
    private fun checkForUpdate() {
        lifecycleScope.launch {
            val lastCheck = userPreferencesRepository.getLastUpdateCheckTimestamp()
            if (System.currentTimeMillis() - lastCheck > TimeUnit.DAYS.toMillis(7)) {
                updateRepository.getLatestRelease().onSuccess { release ->
                    val latestVersion = release.tag_name.removePrefix("v").split("-")[0]
                    val currentVersion = packageManager.getPackageInfo(packageName, 0).versionName?.split("-")?.get(0) ?: "0.0.0"
                    if (isNewerVersion(latestVersion, currentVersion)) {
                        releaseToInstall = release
                    }
                    userPreferencesRepository.setLastUpdateCheckTimestamp(System.currentTimeMillis())
                }
            }
        }
    }

    @Composable
    private fun UpdateScreen() {
        releaseToInstall?.let { release ->
            val skippedCount = userPreferencesRepository.getSkippedUpdateCount()
            val isForced = skippedCount >= 3
            UpdateDialog(
                release = release,
                onUpdateClick = {
                    downloadAndInstall(release)
                    releaseToInstall = null
                    userPreferencesRepository.resetSkippedUpdateCount()
                },
                onDismiss = {
                    releaseToInstall = null
                    if (!isForced) {
                        userPreferencesRepository.incrementSkippedUpdateCount()
                    }
                },
                isForceUpdate = isForced
            )
        }
    }

    private fun isNewerVersion(latestVersion: String, currentVersion: String): Boolean {
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
    }

    private fun downloadAndInstall(release: GitHubRelease) {
        val apkAsset = release.assets.find { it.name.endsWith(".apk") } ?: return
        val downloadUrl = apkAsset.browser_download_url

        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle(release.name)
            .setDescription("Downloading update")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkAsset.name)
        
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    val downloadedApk = downloadManager.getUriForDownloadedFile(downloadId)
                    installApk(downloadedApk)
                    unregisterReceiver(this)
                }
            }
        }
        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun installApk(uri: Uri) {
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(installIntent)
    }
    
    companion object {
        fun restart(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
    }
}
 