package com.H_Oussama.gymplanner.data.repositories

import android.content.Context
import com.H_Oussama.gymplanner.data.model.GitHubRelease
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepository @Inject constructor(
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getLatestRelease(): Result<GitHubRelease> {
        return try {
            val request = Request.Builder()
                .url("https://api.github.com/repos/ossama21/GYM_planner/releases/latest")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val release = json.decodeFromString<GitHubRelease>(responseBody)
                    Result.success(release)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Failed to fetch release: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 