package com.quranreader.custom.data.page

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.StatFs
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Quran Page Provider - Manages page images with local-first approach
 * Based on quran_android implementation
 */
@Singleton
class QuranPageProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val TAG = "QuranPageProvider"
        
        // Multiple CDN sources for reliability
        private val IMAGE_SOURCES = listOf(
            ImageSource("https://android.quran.com/data/width_800", "page%03d.png"),
            ImageSource("https://cdn.qurancdn.com/images/w800", "page_%03d.png"),
            ImageSource("https://quran-pages.herokuapp.com/api/pages", "%d") // Fallback API
        )
        
        private const val WIDTH_PARAM = "width_800"
        private const val TOTAL_PAGES = 604
        private const val MIN_FILE_SIZE = 1024 // 1KB minimum for valid image
        private const val REQUIRED_FREE_SPACE_MB = 50 // Require 50MB free space
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 1000L
    }
    
    // File access synchronization to prevent corruption
    private val fileLocks = ConcurrentHashMap<Int, Mutex>()
    
    data class ImageSource(val baseUrl: String, val pathFormat: String)
    
    /**
     * Get page image - checks local first, downloads if needed
     */
    suspend fun getPageImage(page: Int): Result<Bitmap> = withContext(Dispatchers.IO) {
        val mutex = fileLocks.getOrPut(page) { Mutex() }
        mutex.withLock {
            try {
                // 1. Check if valid page exists locally
                val localFile = getLocalPageFile(page)
                if (isValidImageFile(localFile)) {
                    Log.d(TAG, "Loading page $page from local storage")
                    val bitmap = decodeBitmapSafely(localFile.absolutePath)
                    if (bitmap != null) {
                        return@withContext Result.success(bitmap)
                    } else {
                        // File exists but corrupted, delete it
                        Log.w(TAG, "Corrupted file detected for page $page, deleting")
                        localFile.delete()
                    }
                }
                
                // 2. Download from server with retry logic
                Log.d(TAG, "Downloading page $page from server")
                val downloadResult = downloadPageWithRetry(page)
                if (downloadResult.isSuccess) {
                    val bitmap = downloadResult.getOrNull()
                    if (bitmap != null) {
                        return@withContext Result.success(bitmap)
                    }
                }
                
                Result.failure(downloadResult.exceptionOrNull() ?: Exception("Failed to load page $page"))
            } catch (e: Exception) {
                Log.e(TAG, "Error loading page $page", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Download page with retry logic and multiple sources
     */
    private suspend fun downloadPageWithRetry(page: Int): Result<Bitmap> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null
        
        // Try each image source
        for (source in IMAGE_SOURCES) {
            for (attempt in 1..MAX_RETRY_ATTEMPTS) {
                try {
                    val result = downloadFromSource(page, source)
                    if (result.isSuccess) {
                        val bitmap = result.getOrNull()
                        if (bitmap != null) {
                            // Save to local storage
                            savePageToLocal(page, bitmap)
                            return@withContext Result.success(bitmap)
                        }
                    }
                    lastException = result.exceptionOrNull() as? Exception
                } catch (e: Exception) {
                    lastException = e
                    Log.w(TAG, "Attempt $attempt failed for page $page from ${source.baseUrl}: ${e.message}")
                    
                    // Wait before retry (exponential backoff)
                    if (attempt < MAX_RETRY_ATTEMPTS) {
                        delay(RETRY_DELAY_MS * attempt)
                    }
                }
            }
        }
        
        Result.failure(lastException ?: Exception("All download sources failed for page $page"))
    }
    
    /**
     * Download from specific source
     */
    private suspend fun downloadFromSource(page: Int, source: ImageSource): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            val url = when {
                source.pathFormat.contains("%03d") -> "${source.baseUrl}/${source.pathFormat.format(page)}"
                source.pathFormat.contains("%d") -> "${source.baseUrl}/${source.pathFormat.format(page)}"
                else -> "${source.baseUrl}/${source.pathFormat}"
            }
            
            Log.d(TAG, "Trying to download page $page from: $url")
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                response.body?.byteStream()?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        return@withContext Result.success(bitmap)
                    }
                }
            }
            
            val errorMsg = when (response.code) {
                404 -> "Page not found"
                500, 502, 503 -> "Server error (${response.code})"
                else -> "HTTP ${response.code}: ${response.message}"
            }
            Result.failure(Exception(errorMsg))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Network timeout"))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("No internet connection"))
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading from ${source.baseUrl}", e)
            Result.failure(e)
        }
    }

    /**
     * Download page and save directly as raw bytes with validation
     * Used by DownloadWorker for bulk background downloads.
     */
    suspend fun downloadAndSavePage(page: Int): Result<Unit> = withContext(Dispatchers.IO) {
        val mutex = fileLocks.getOrPut(page) { Mutex() }
        mutex.withLock {
            try {
                val file = getLocalPageFile(page)
                if (isValidImageFile(file)) {
                    return@withContext Result.success(Unit)
                }
                
                // Check available disk space
                if (!hasEnoughDiskSpace()) {
                    return@withContext Result.failure(Exception("Insufficient disk space"))
                }
                
                var lastException: Exception? = null
                
                // Try each source with retry logic
                for (source in IMAGE_SOURCES) {
                    for (attempt in 1..MAX_RETRY_ATTEMPTS) {
                        try {
                            val result = downloadAndSaveFromSource(page, source, file)
                            if (result.isSuccess) {
                                return@withContext Result.success(Unit)
                            }
                            lastException = result.exceptionOrNull() as? Exception
                        } catch (e: Exception) {
                            lastException = e
                            if (attempt < MAX_RETRY_ATTEMPTS) {
                                delay(RETRY_DELAY_MS * attempt)
                            }
                        }
                    }
                }
                
                Result.failure(lastException ?: Exception("All sources failed for page $page"))
            } catch (e: Exception) {
                Log.e(TAG, "Error saving page $page", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Download and save from specific source with validation
     */
    private suspend fun downloadAndSaveFromSource(page: Int, source: ImageSource, file: File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = when {
                source.pathFormat.contains("%03d") -> "${source.baseUrl}/${source.pathFormat.format(page)}"
                source.pathFormat.contains("%d") -> "${source.baseUrl}/${source.pathFormat.format(page)}"
                else -> "${source.baseUrl}/${source.pathFormat}"
            }
            
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                response.body?.byteStream()?.use { input ->
                    file.parentFile?.mkdirs()
                    val tempFile = File(file.parentFile, "${file.name}.tmp")
                    
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output, bufferSize = 8 * 1024)
                    }
                    
                    // Validate downloaded file
                    if (isValidImageFile(tempFile)) {
                        tempFile.renameTo(file)
                        Log.d(TAG, "Successfully downloaded page $page from ${source.baseUrl}")
                        return@withContext Result.success(Unit)
                    } else {
                        tempFile.delete()
                        return@withContext Result.failure(Exception("Downloaded file is corrupted"))
                    }
                }
            }
            
            Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Save page to local storage
     */
    private fun savePageToLocal(page: Int, bitmap: Bitmap) {
        try {
            val file = getLocalPageFile(page)
            file.parentFile?.mkdirs()
            
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            Log.d(TAG, "Saved page $page to local storage")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving page $page", e)
        }
    }
    
    /**
     * Get local file for page
     */
    private fun getLocalPageFile(page: Int): File {
        val pageStr = String.format("%03d", page)
        val pagesDir = File(context.filesDir, "pages/$WIDTH_PARAM")
        return File(pagesDir, "page$pageStr.png")
    }
    
    /**
     * Check if page is downloaded and valid
     */
    fun isPageDownloaded(page: Int): Boolean {
        return isValidImageFile(getLocalPageFile(page))
    }
    
    /**
     * Validate image file integrity
     */
    private fun isValidImageFile(file: File): Boolean {
        if (!file.exists() || file.length() < MIN_FILE_SIZE) {
            return false
        }
        
        // Quick validation by trying to decode bounds
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(file.absolutePath, options)
        return options.outWidth > 0 && options.outHeight > 0
    }
    
    /**
     * Safely decode bitmap with memory optimization
     */
    private fun decodeBitmapSafely(filePath: String): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.RGB_565 // Use less memory
                inSampleSize = 1 // Can be adjusted based on device capabilities
            }
            BitmapFactory.decodeFile(filePath, options)
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "OutOfMemoryError decoding bitmap: $filePath", e)
            // Try with higher sample size
            try {
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.RGB_565
                    inSampleSize = 2
                }
                BitmapFactory.decodeFile(filePath, options)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to decode bitmap even with sampling: $filePath", e2)
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding bitmap: $filePath", e)
            null
        }
    }
    
    /**
     * Check if device has enough disk space
     */
    private fun hasEnoughDiskSpace(): Boolean {
        return try {
            val stat = StatFs(context.filesDir.path)
            val availableBytes = stat.availableBytes
            val requiredBytes = REQUIRED_FREE_SPACE_MB * 1024 * 1024L
            availableBytes > requiredBytes
        } catch (e: Exception) {
            Log.w(TAG, "Could not check disk space", e)
            true // Assume we have space if we can't check
        }
    }
    
    /**
     * Get download statistics
     */
    fun getDownloadStats(): DownloadStats {
        val pagesDir = File(context.filesDir, "pages/$WIDTH_PARAM")
        val downloadedCount = if (pagesDir.exists()) {
            pagesDir.listFiles()?.size ?: 0
        } else {
            0
        }
        
        return DownloadStats(
            pagesDownloaded = downloadedCount,
            totalPages = TOTAL_PAGES
        )
    }
    
    /**
     * Download pages, skipping already-downloaded ones (resume support).
     * Used by DownloadViewModel for foreground progress tracking.
     */
    suspend fun downloadPagesParallel(
        startPage: Int,
        endPage: Int,
        onProgress: (Int, Int) -> Unit
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val totalPages = endPage - startPage + 1
            var successCount = 0

            for (page in startPage..endPage) {
                // Skip already downloaded pages — this is the resume fix
                if (isPageDownloaded(page)) {
                    successCount++
                    onProgress(successCount, totalPages)
                    continue
                }
                val result = downloadAndSavePage(page)
                if (result.isSuccess) successCount++
                onProgress(successCount, totalPages)
            }
            Result.success(successCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading pages", e)
            Result.failure(e)
        }
    }
    
    /**
     * Download multiple pages (sequential) - legacy method for compatibility
     */
    suspend fun downloadPages(
        startPage: Int,
        endPage: Int,
        onProgress: (Int, Int) -> Unit
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var successCount = 0
            val totalPages = endPage - startPage + 1
            
            for (page in startPage..endPage) {
                if (isPageDownloaded(page)) {
                    successCount++
                    onProgress(successCount, totalPages)
                    continue
                }
                
                val result = downloadAndSavePage(page)
                if (result.isSuccess) {
                    successCount++
                    onProgress(successCount, totalPages)
                }
            }
            
            Result.success(successCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading pages", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete all downloaded pages (for clearing cache)
     */
    fun clearCache() {
        try {
            val pagesDir = File(context.filesDir, "pages")
            if (pagesDir.exists()) {
                pagesDir.deleteRecursively()
            }
            Log.d(TAG, "Cache cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache", e)
        }
    }
}

/**
 * Download statistics
 */
data class DownloadStats(
    val pagesDownloaded: Int,
    val totalPages: Int
) {
    val progress: Float get() = pagesDownloaded.toFloat() / totalPages
    val progressPercent: Int get() = (progress * 100).toInt()
}
