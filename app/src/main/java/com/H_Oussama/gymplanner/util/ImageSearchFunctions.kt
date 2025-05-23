package com.H_Oussama.gymplanner.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class ImageSearchResult(
    val thumbnailUrl: String,
    val originalUrl: String,
    val title: String
)

/**
 * Simple function to search for exercise images using SerpAPI
 */
suspend fun searchExerciseImages(exerciseName: String): List<ImageSearchResult> = withContext(Dispatchers.IO) {
    val TAG = "ImageSearch"
    val API_KEY = "207eb6ae7716b433da61f6265fa76cda5c95af649a2b2f81e23798d69ca9a79d"
    
    try {
        val encodedQuery = URLEncoder.encode("$exerciseName exercise form", "UTF-8")
        val searchUrl = URL("https://serpapi.com/search.json?q=$encodedQuery&tbm=isch&api_key=$API_KEY")
        
        val connection = searchUrl.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        
        val response = StringBuilder()
        BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
        }

        val jsonResponse = JSONObject(response.toString())
        val imagesResults = jsonResponse.getJSONArray("images_results")
        
        val results = mutableListOf<ImageSearchResult>()
        for (i in 0 until imagesResults.length()) {
            val image = imagesResults.getJSONObject(i)
            results.add(
                ImageSearchResult(
                    thumbnailUrl = image.getString("thumbnail"),
                    originalUrl = image.getString("original"),
                    title = image.getString("title")
                )
            )
        }
        
        results
    } catch (e: Exception) {
        Log.e(TAG, "Error searching for images: ${e.message}")
        emptyList()
    }
}

/**
 * Simple function to download an image from a URL and save it using ImageManager
 */
suspend fun downloadAndSaveExerciseImage(
    context: Context, 
    imageUrl: String, 
    exerciseName: String
): String? = withContext(Dispatchers.IO) {
    val TAG = "ImageDownload"
    
    try {
        val url = URL(imageUrl)
        val connection = url.openConnection()
        connection.connect()
        
        val inputStream = connection.getInputStream()
        val bitmap = BitmapFactory.decodeStream(inputStream)
        
        // Save using ImageManager
        val imageManager = ImageManager(context)
        imageManager.saveBitmap(bitmap, exerciseName)
    } catch (e: Exception) {
        Log.e(TAG, "Error downloading image: ${e.message}")
        null
    }
} 