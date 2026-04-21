package com.quranreader.custom.data.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.quranreader.custom.data.page.QuranPageProvider
import com.quranreader.custom.data.preferences.UserPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * WorkManager Worker for background Quran page downloads.
 * Survives app going to background, shows persistent notification.
 */
@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val pageProvider: QuranPageProvider,
    private val userPreferences: UserPreferences
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "DownloadWorker"
        const val WORK_NAME = "quran_page_download"
        const val CHANNEL_ID = "download_channel"
        const val NOTIFICATION_ID = 1001

        const val KEY_START_PAGE = "start_page"
        const val KEY_END_PAGE = "end_page"
        const val KEY_PROGRESS = "progress"
        const val KEY_DOWNLOADED = "downloaded"
        const val KEY_TOTAL = "total"
        const val KEY_MB_DOWNLOADED = "mb_downloaded"

        fun buildRequest(startPage: Int = 1, endPage: Int = 604): OneTimeWorkRequest {
            val data = workDataOf(
                KEY_START_PAGE to startPage,
                KEY_END_PAGE to endPage
            )
            return OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(data)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresBatteryNotLow(false) // Allow download even on low battery
                        .setRequiresStorageNotLow(true)  // But require sufficient storage
                        .build()
                )
                .addTag(WORK_NAME)
                .build()
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val startPage = inputData.getInt(KEY_START_PAGE, 1)
            val endPage = inputData.getInt(KEY_END_PAGE, 604)
            val totalPages = endPage - startPage + 1

            createNotificationChannel()
            setForeground(buildForegroundInfo(0, 0, totalPages))

            var downloaded = 0
            var failed = 0
            var consecutiveFailures = 0
            val maxConsecutiveFailures = 10 // Stop if too many consecutive failures

            for (page in startPage..endPage) {
                // Check if we should stop due to too many failures
                if (consecutiveFailures >= maxConsecutiveFailures) {
                    Log.e(TAG, "Too many consecutive failures ($consecutiveFailures), stopping download")
                    break
                }

                // Skip already downloaded pages
                if (pageProvider.isPageDownloaded(page)) {
                    downloaded++
                    consecutiveFailures = 0 // Reset failure counter on success
                    val progress = (downloaded * 100) / totalPages
                    val mbDone = downloaded * 0.05f
                    setProgress(
                        workDataOf(
                            KEY_PROGRESS to progress,
                            KEY_DOWNLOADED to downloaded,
                            KEY_TOTAL to totalPages,
                            KEY_MB_DOWNLOADED to mbDone
                        )
                    )
                    setForeground(buildForegroundInfo(progress, downloaded, totalPages))
                    userPreferences.saveDownloadProgress(downloaded + (startPage - 1), 604)
                    continue
                }

                val result = pageProvider.downloadAndSavePage(page)
                if (result.isSuccess) {
                    downloaded++
                    consecutiveFailures = 0 // Reset failure counter on success
                    Log.d(TAG, "Successfully downloaded page $page")
                } else {
                    failed++
                    consecutiveFailures++
                    val error = result.exceptionOrNull()
                    Log.w(TAG, "Failed to download page $page: ${error?.message}")
                    
                    // For certain errors, we might want to stop immediately
                    when {
                        error?.message?.contains("Insufficient disk space") == true -> {
                            Log.e(TAG, "Insufficient disk space, stopping download")
                            break
                        }
                        error?.message?.contains("No internet connection") == true -> {
                            Log.e(TAG, "No internet connection, stopping download")
                            break
                        }
                    }
                }

                val progress = (downloaded * 100) / totalPages
                val mbDone = downloaded * 0.05f

                setProgress(
                    workDataOf(
                        KEY_PROGRESS to progress,
                        KEY_DOWNLOADED to downloaded,
                        KEY_TOTAL to totalPages,
                        KEY_MB_DOWNLOADED to mbDone
                    )
                )
                setForeground(buildForegroundInfo(progress, downloaded, totalPages))
                userPreferences.saveDownloadProgress(downloaded + (startPage - 1), 604)
            }

            return@withContext if (downloaded > 0) {
                userPreferences.setFirstLaunchComplete()
                showCompletionNotification(downloaded, failed)
                Log.i(TAG, "Download completed: $downloaded successful, $failed failed")
                Result.success(
                    workDataOf(
                        KEY_DOWNLOADED to downloaded,
                        KEY_TOTAL to totalPages
                    )
                )
            } else {
                Log.e(TAG, "Download failed: no pages downloaded successfully")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download worker crashed", e)
            Result.failure()
        }
    }

    private fun buildForegroundInfo(progress: Int, downloaded: Int, total: Int): ForegroundInfo {
        val mbDone = String.format("%.1f", downloaded * 0.05f)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Downloading Quran Pages")
            .setContentText("$downloaded / $total pages • $mbDone MB / 30.0 MB")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, progress == 0)
            .setOngoing(true)
            .setSilent(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun showCompletionNotification(downloaded: Int, failed: Int) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Download Complete")
            .setContentText("$downloaded pages downloaded successfully")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Quran Download",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows Quran page download progress"
                setShowBadge(false)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}
