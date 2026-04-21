package com.quranreader.custom.data.remote

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Download Manager for Quran resources
 * Downloads pages, audio, and translations from online sources
 */
@Singleton
class DownloadManager @Inject constructor(
    private val context: Context
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "DownloadManager"
        
        // Quran.com CDN for page images - SMALLER SIZE (w800 instead of w1920)
        // Reduces from ~200MB to ~50MB for all 604 pages
        private const val PAGES_BASE_URL = "https://cdn.qurancdn.com/images/w800/"
        
        // EveryAyah.com for audio - LOWER BITRATE
        // Using 64kbps instead of 192kbps reduces from ~1GB to ~300MB
        private const val AUDIO_BASE_URL = "https://everyayah.com/data/"
        
        // Default reciter - Lower bitrate for smaller file size
        private const val DEFAULT_RECITER = "Abdul_Basit_Murattal_64kbps"
    }

    /**
     * Download Quran page image
     * @param page Page number (1-604)
     * @return Flow with download progress
     */
    fun downloadPage(page: Int): Flow<DownloadState> = flow {
        emit(DownloadState.Loading(0))
        
        try {
            val pageStr = String.format("%03d", page)
            val url = "${PAGES_BASE_URL}page$pageStr.png"
            val outputFile = File(context.filesDir, "pages/page_$pageStr.png")
            
            // Create directory if not exists
            outputFile.parentFile?.mkdirs()
            
            // Check if already downloaded
            if (outputFile.exists()) {
                emit(DownloadState.Success(outputFile.absolutePath))
                return@flow
            }
            
            // Download file
            val request = Request.Builder().url(url).build()
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }
            
            if (response.isSuccessful) {
                response.body?.let { body ->
                    val totalBytes = body.contentLength()
                    var downloadedBytes = 0L
                    
                    withContext(Dispatchers.IO) {
                        body.byteStream().use { input ->
                            FileOutputStream(outputFile).use { output ->
                                val buffer = ByteArray(8192)
                                var bytes = input.read(buffer)
                                
                                while (bytes >= 0) {
                                    output.write(buffer, 0, bytes)
                                    downloadedBytes += bytes
                                    
                                    val progress = if (totalBytes > 0) {
                                        (downloadedBytes * 100 / totalBytes).toInt()
                                    } else 0
                                    
                                    emit(DownloadState.Loading(progress))
                                    bytes = input.read(buffer)
                                }
                            }
                        }
                    }
                    
                    emit(DownloadState.Success(outputFile.absolutePath))
                } ?: emit(DownloadState.Error("Empty response body"))
            } else {
                emit(DownloadState.Error("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading page $page", e)
            emit(DownloadState.Error(e.message ?: "Unknown error"))
        }
    }

    /**
     * Download audio file for specific ayah
     * @param surah Surah number (1-114)
     * @param ayah Ayah number
     * @param reciter Reciter name (optional, uses default if null)
     */
    fun downloadAudio(
        surah: Int,
        ayah: Int,
        reciter: String? = null
    ): Flow<DownloadState> = flow {
        emit(DownloadState.Loading(0))
        
        try {
            val reciterName = reciter ?: DEFAULT_RECITER
            val surahStr = String.format("%03d", surah)
            val ayahStr = String.format("%03d", ayah)
            val fileName = "$surahStr$ayahStr.mp3"
            
            val url = "$AUDIO_BASE_URL$reciterName/$fileName"
            val outputFile = File(context.filesDir, "audio/$reciterName/$fileName")
            
            // Create directory if not exists
            outputFile.parentFile?.mkdirs()
            
            // Check if already downloaded
            if (outputFile.exists()) {
                emit(DownloadState.Success(outputFile.absolutePath))
                return@flow
            }
            
            // Download file
            val request = Request.Builder().url(url).build()
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }
            
            if (response.isSuccessful) {
                response.body?.let { body ->
                    val totalBytes = body.contentLength()
                    var downloadedBytes = 0L
                    
                    withContext(Dispatchers.IO) {
                        body.byteStream().use { input ->
                            FileOutputStream(outputFile).use { output ->
                                val buffer = ByteArray(8192)
                                var bytes = input.read(buffer)
                                
                                while (bytes >= 0) {
                                    output.write(buffer, 0, bytes)
                                    downloadedBytes += bytes
                                    
                                    val progress = if (totalBytes > 0) {
                                        (downloadedBytes * 100 / totalBytes).toInt()
                                    } else 0
                                    
                                    emit(DownloadState.Loading(progress))
                                    bytes = input.read(buffer)
                                }
                            }
                        }
                    }
                    
                    emit(DownloadState.Success(outputFile.absolutePath))
                } ?: emit(DownloadState.Error("Empty response body"))
            } else {
                emit(DownloadState.Error("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading audio $surah:$ayah", e)
            emit(DownloadState.Error(e.message ?: "Unknown error"))
        }
    }

    /**
     * Download all pages for a surah
     */
    fun downloadSurah(startPage: Int, endPage: Int): Flow<DownloadState> = flow {
        val totalPages = endPage - startPage + 1
        var downloadedPages = 0
        
        for (page in startPage..endPage) {
            downloadPage(page).collect { state ->
                when (state) {
                    is DownloadState.Success -> {
                        downloadedPages++
                        val progress = (downloadedPages * 100 / totalPages)
                        emit(DownloadState.Loading(progress))
                    }
                    is DownloadState.Error -> {
                        emit(state)
                        return@collect
                    }
                    else -> {}
                }
            }
        }
        
        emit(DownloadState.Success("Downloaded $downloadedPages pages"))
    }

    /**
     * Download all audio for a surah
     */
    fun downloadSurahAudio(
        surah: Int,
        ayahCount: Int,
        reciter: String? = null
    ): Flow<DownloadState> = flow {
        var downloadedAyahs = 0
        
        for (ayah in 1..ayahCount) {
            downloadAudio(surah, ayah, reciter).collect { state ->
                when (state) {
                    is DownloadState.Success -> {
                        downloadedAyahs++
                        val progress = (downloadedAyahs * 100 / ayahCount)
                        emit(DownloadState.Loading(progress))
                    }
                    is DownloadState.Error -> {
                        emit(state)
                        return@collect
                    }
                    else -> {}
                }
            }
        }
        
        emit(DownloadState.Success("Downloaded $downloadedAyahs audio files"))
    }

    /**
     * Check if page is downloaded
     */
    fun isPageDownloaded(page: Int): Boolean {
        val pageStr = String.format("%03d", page)
        val file = File(context.filesDir, "pages/page_$pageStr.png")
        return file.exists()
    }

    /**
     * Check if audio is downloaded
     */
    fun isAudioDownloaded(surah: Int, ayah: Int, reciter: String? = null): Boolean {
        val reciterName = reciter ?: DEFAULT_RECITER
        val surahStr = String.format("%03d", surah)
        val ayahStr = String.format("%03d", ayah)
        val fileName = "$surahStr$ayahStr.mp3"
        val file = File(context.filesDir, "audio/$reciterName/$fileName")
        return file.exists()
    }

    /**
     * Get local file path for page
     */
    fun getPagePath(page: Int): String? {
        val pageStr = String.format("%03d", page)
        val file = File(context.filesDir, "pages/page_$pageStr.png")
        return if (file.exists()) file.absolutePath else null
    }

    /**
     * Get local file path for audio
     */
    fun getAudioPath(surah: Int, ayah: Int, reciter: String? = null): String? {
        val reciterName = reciter ?: DEFAULT_RECITER
        val surahStr = String.format("%03d", surah)
        val ayahStr = String.format("%03d", ayah)
        val fileName = "$surahStr$ayahStr.mp3"
        val file = File(context.filesDir, "audio/$reciterName/$fileName")
        return if (file.exists()) file.absolutePath else null
    }

    /**
     * Get download statistics
     */
    fun getDownloadStats(): DownloadStats {
        val pagesDir = File(context.filesDir, "pages")
        val audioDir = File(context.filesDir, "audio")
        
        val pagesCount = pagesDir.listFiles()?.size ?: 0
        val audioCount = audioDir.walk().filter { it.isFile && it.extension == "mp3" }.count()
        
        return DownloadStats(
            pagesDownloaded = pagesCount,
            totalPages = 604,
            audioFilesDownloaded = audioCount
        )
    }
}

/**
 * Download state sealed class
 */
sealed class DownloadState {
    data class Loading(val progress: Int) : DownloadState()
    data class Success(val filePath: String) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

/**
 * Download statistics
 */
data class DownloadStats(
    val pagesDownloaded: Int,
    val totalPages: Int,
    val audioFilesDownloaded: Int
) {
    val pagesProgress: Int get() = (pagesDownloaded * 100 / totalPages)
}
