package com.H_Oussama.gymplanner.utils

import android.graphics.Bitmap

/**
 * Status updates for the image download process - maintained for backward compatibility
 */
sealed class ImageDownloadStatus {
    object Searching : ImageDownloadStatus()
    object Downloading : ImageDownloadStatus() 
    data class Success(val imageUrl: String, val bitmap: Bitmap) : ImageDownloadStatus()
    object NoImagesFound : ImageDownloadStatus()
    object DownloadFailed : ImageDownloadStatus()
    data class Error(val message: String) : ImageDownloadStatus()
} 