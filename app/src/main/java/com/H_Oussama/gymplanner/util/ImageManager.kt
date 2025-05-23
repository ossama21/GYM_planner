package com.H_Oussama.gymplanner.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Utility class to manage exercise images
 */
class ImageManager(private val context: Context) {

    companion object {
        const val EXERCISE_IMAGES_DIR = "exercise_images"
        const val TAG = "ImageManager"
    }

    /**
     * Gets the directory where exercise images are stored
     */
    fun getExerciseImagesDir(): File {
        val imagesDir = File(context.filesDir, EXERCISE_IMAGES_DIR)
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        return imagesDir
    }

    /**
     * List all available exercise images
     */
    fun getAllAvailableImages(): List<File> {
        return getExerciseImagesDir().listFiles()?.toList() ?: emptyList()
    }

    /**
     * Copy an image from a Uri to the app's internal storage with a given name
     * @param imageUri The Uri of the image to copy
     * @param imageName The name to save the image as (without extension)
     * @return The filename of the saved image (without path)
     */
    suspend fun saveImageFromUri(imageUri: Uri, imageName: String): String = withContext(Dispatchers.IO) {
        // Sanitize the image name (remove spaces, special chars)
        val sanitizedName = sanitizeFileName(imageName)
        
        // Determine file extension from Uri (default to .jpg if can't determine)
        val mimeType = context.contentResolver.getType(imageUri)
        val extension = when {
            mimeType?.contains("jpeg") == true || mimeType?.contains("jpg") == true -> "jpg"
            mimeType?.contains("png") == true -> "png"
            else -> "jpg"
        }
        
        // Create destination file
        val imageFile = File(getExerciseImagesDir(), "$sanitizedName.$extension")
        
        // Copy the image data
        context.contentResolver.openInputStream(imageUri)?.use { input ->
            FileOutputStream(imageFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Could not open input stream for URI: $imageUri")
        
        return@withContext sanitizedName
    }
    
    /**
     * Sanitize filename to remove invalid characters
     */
    private fun sanitizeFileName(fileName: String): String {
        // Replace spaces with underscores, remove special characters
        return fileName.trim()
            .replace("\\s+".toRegex(), "_")
            .replace("[^a-zA-Z0-9_]".toRegex(), "")
            .lowercase()
    }

    fun saveBitmap(bitmap: Bitmap, name: String): String {
        val fileName = "${name.lowercase().replace(" ", "_")}_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            return fileName
        } catch (e: Exception) {
            Log.e(TAG, "Error saving bitmap: ${e.message}")
            throw e
        }
    }
} 