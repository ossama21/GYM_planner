package com.H_Oussama.gymplanner

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.H_Oussama.gymplanner.data.repositories.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var muteButton: ImageButton
    private lateinit var skipButton: Button
    private var isMuted = false
    private val maxWaitTime = 3000L // Max wait time before proceeding to MainActivity
    private var isNavigating = false
    
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if intro should be skipped
        if (userPreferencesRepository.getSkipIntro()) {
            navigateToMainActivity()
            return
        }
        
        // Preload MainActivity in background
        preloadMainActivity()
        
        setContentView(R.layout.activity_splash)

        // Hide the status bar for immersive experience
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        // Get shared preferences
        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        
        // Get mute state from UserPreferencesRepository
        isMuted = userPreferencesRepository.isIntroVideoMuted()

        // Initialize the VideoView
        videoView = findViewById(R.id.splashVideoView)
        muteButton = findViewById(R.id.muteButton)
        skipButton = findViewById(R.id.skipButton)

        // Update mute button icon based on current state
        updateMuteButtonIcon()
        
        // Set up mute button click listener
        muteButton.setOnClickListener {
            isMuted = !isMuted
            updateMuteButtonIcon()
            updateVideoVolume()
            
            // Save preference to UserPreferencesRepository using a coroutine
            lifecycleScope.launch {
                try {
                    userPreferencesRepository.setIntroVideoMuted(isMuted)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Fallback to shared preferences if repository fails
                    sharedPreferences.edit {
                        putBoolean("splash_muted", isMuted)
                    }
                }
            }
        }
        
        // Set up skip button
        skipButton.setOnClickListener {
            navigateToMainActivity()
        }

        // Set a timeout to ensure we move to MainActivity even if video has issues
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing && !isNavigating) {
                navigateToMainActivity()
            }
        }, maxWaitTime)
        
        // Set up the video URI and prepare listeners
        setupVideoPlayback()
    }
    
    private fun preloadMainActivity() {
        // Use an empty thread to preload MainActivity classes
        Thread {
            try {
                // Preload MainActivity classes
                Class.forName("com.H_Oussama.gymplanner.MainActivity")
            } catch (e: Exception) {
                // Ignore any errors during preloading
            }
        }.start()
    }

    private fun updateMuteButtonIcon() {
        muteButton.setImageResource(
            if (isMuted) R.drawable.ic_volume_off
            else R.drawable.ic_volume_on
        )
    }

    private fun updateVideoVolume() {
        val volume = if (isMuted) 0f else 1f
        
        // Try to access the current MediaPlayer if it exists
        val mediaPlayer = getCurrentMediaPlayer()
        if (mediaPlayer != null) {
            // If MediaPlayer exists, set volume directly
            mediaPlayer.setVolume(volume, volume)
        } else {
            // We need to set the prepared listener if the MediaPlayer doesn't exist yet
            videoView.setOnPreparedListener { mp ->
                mp.setVolume(volume, volume)
                mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                mp.isLooping = false
                mp.start()
            }
        }
    }

    /**
     * Attempts to get the current MediaPlayer instance from the VideoView
     * Returns null if no MediaPlayer is found or if an error occurs
     */
    private fun getCurrentMediaPlayer(): MediaPlayer? {
        try {
            // Try different field names that might be used in various Android versions
            val fieldNames = arrayOf("mMediaPlayer", "mVideoView")
            
            for (fieldName in fieldNames) {
                try {
                    val field = VideoView::class.java.getDeclaredField(fieldName)
                    field.isAccessible = true
                    val obj = field.get(videoView)
                    
                    if (obj is MediaPlayer) {
                        return obj
                    }
                } catch (e: Exception) {
                    // Continue to the next field name
                    continue
                }
            }
            
            // If we get here, we couldn't find a MediaPlayer using reflection
            // Try a different approach - check if we can access it through the VideoView API
            if (videoView.isPlaying) {
                // If it's playing, we know there's a MediaPlayer, but we can't access it directly
                // Instead, we'll use our prepared listener in setupVideoPlayback 
                return null
            }
            
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun loadSplashVideo() {
        try {
            // Get the custom video path from preferences if available
            val defaultVideoPath = "android.resource://" + packageName + "/raw/splash_video"
            val customVideoPath = sharedPreferences.getString("custom_splash_video", defaultVideoPath)
            
            val uri = Uri.parse(customVideoPath)
            videoView.setVideoURI(uri)
        } catch (e: Exception) {
            // If any error occurs loading the video, proceed to MainActivity
            navigateToMainActivity()
        }
    }

    private fun showChangeVideoOptions() {
        Toast.makeText(this, 
            "Long press detected! Here you would show video selection options.", 
            Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainActivity() {
        if (!isNavigating) {
            isNavigating = true
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close the splash activity
        }
    }

    override fun onBackPressed() {
        // Skip to MainActivity on back press
        navigateToMainActivity()
        super.onBackPressed()
    }

    private fun setupVideoPlayback() {
        // Set up the video URI
        loadSplashVideo()

        // When video is done playing, navigate to MainActivity
        videoView.setOnCompletionListener { mediaPlayer: MediaPlayer? ->
            navigateToMainActivity()
        }

        // If there's an error loading the video, skip to MainActivity
        videoView.setOnErrorListener { mediaPlayer, what, extra ->
            Toast.makeText(this, "Error loading video", Toast.LENGTH_SHORT).show()
            navigateToMainActivity()
            true
        }

        // Long press on video view to show change video options
        videoView.setOnLongClickListener {
            showChangeVideoOptions()
            true
        }
        
        // Set up prepared listener to handle video setup and volume
        videoView.setOnPreparedListener { mp ->
            // Get the current mute status from repository
            isMuted = userPreferencesRepository.isIntroVideoMuted()
            
            // Set volume based on current mute state
            val volume = if (isMuted) 0f else 1f
            mp.setVolume(volume, volume)
            
            // Set video to low quality and optimize for performance
            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
            mp.isLooping = false
            mp.start()
            
            // Make sure the mute button shows the correct state
            updateMuteButtonIcon()
        }
    }
} 