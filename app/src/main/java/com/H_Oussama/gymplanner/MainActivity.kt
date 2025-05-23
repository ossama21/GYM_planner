package com.H_Oussama.gymplanner

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
import com.H_Oussama.gymplanner.data.repositories.UserPreferencesRepository
import javax.inject.Inject

@AndroidEntryPoint // Add Hilt annotation
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize notification helper
        NotificationHelper.initialize(this)
        
        // Apply saved language
        applyLanguage()
        
        setContent {
            GymApp() // Set the content to our main App composable
        }
    }
    
    private fun applyLanguage() {
        val languageCode = userPreferencesRepository.getLanguage()
        GymPlannerApplication.setLocale(this, languageCode)
    }
    
    companion object {
        fun restart(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
    }
}
 