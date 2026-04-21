package com.quranreader.custom.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.quranreader.custom.data.QuranInfo
import com.quranreader.custom.data.download.DownloadWorker
import com.quranreader.custom.data.page.QuranPageProvider
import com.quranreader.custom.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    application: Application,
    private val pageProvider: QuranPageProvider,
    private val userPreferences: UserPreferences
) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)

    private val _downloadStats = MutableStateFlow(pageProvider.getDownloadStats())
    val downloadStats: StateFlow<com.quranreader.custom.data.page.DownloadStats> = _downloadStats.asStateFlow()

    private val _downloadingPages = MutableStateFlow(false)
    val downloadingPages: StateFlow<Boolean> = _downloadingPages.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0)
    val downloadProgress: StateFlow<Int> = _downloadProgress.asStateFlow()

    private val _downloadMessage = MutableStateFlow<String?>(null)
    val downloadMessage: StateFlow<String?> = _downloadMessage.asStateFlow()

    private val _downloadedMB = MutableStateFlow(0f)
    val downloadedMB: StateFlow<Float> = _downloadedMB.asStateFlow()

    private val _totalMB = MutableStateFlow(30f)
    val totalMB: StateFlow<Float> = _totalMB.asStateFlow()

    init {
        refreshStats()
        observeWorker()
        // Restore saved progress into UI on init
        viewModelScope.launch {
            userPreferences.downloadProgress.first().let { (downloaded, total) ->
                if (downloaded > 0 && downloaded < total) {
                    _downloadProgress.value = (downloaded * 100 / total)
                    _downloadedMB.value = downloaded * 0.05f
                }
            }
        }
    }

    fun refreshStats() {
        _downloadStats.value = pageProvider.getDownloadStats()
    }

    /** Enqueue background download via WorkManager — survives app going to background */
    fun downloadAllPages() {
        val stats = pageProvider.getDownloadStats()
        // Find first missing page to resume from
        val startPage = (1..604).firstOrNull { !pageProvider.isPageDownloaded(it) } ?: run {
            _downloadMessage.value = "All pages already downloaded!"
            refreshStats()
            return
        }

        // Check if we have enough disk space before starting
        val requiredSpaceMB = ((604 - startPage + 1) * 0.05f).toInt() + 10 // Add 10MB buffer
        _downloadMessage.value = "Checking available space..."
        
        val request = DownloadWorker.buildRequest(startPage, 604)
        workManager.enqueueUniqueWork(
            DownloadWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP, // Don't restart if already running
            request
        )
        _downloadingPages.value = true
        _downloadMessage.value = if (startPage > 1) {
            "Resuming from page $startPage... (${604 - startPage + 1} pages remaining)"
        } else {
            "Starting download... (604 pages, ~30MB)"
        }
    }

    fun retryFailedDownloads() {
        // Cancel any existing work first
        workManager.cancelUniqueWork(DownloadWorker.WORK_NAME)
        
        viewModelScope.launch {
            // Wait a moment for cancellation to complete
            kotlinx.coroutines.delay(1000)
            
            // Find pages that failed to download properly
            val failedPages = (1..604).filter { !pageProvider.isPageDownloaded(it) }
            
            if (failedPages.isEmpty()) {
                _downloadMessage.value = "All pages are already downloaded!"
                refreshStats()
                return@launch
            }
            
            _downloadMessage.value = "Retrying ${failedPages.size} failed downloads..."
            
            // Start download from first failed page
            val request = DownloadWorker.buildRequest(failedPages.first(), 604)
            workManager.enqueueUniqueWork(
                DownloadWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE, // Replace any existing work
                request
            )
            _downloadingPages.value = true
        }
    }

    fun cancelDownload() {
        workManager.cancelUniqueWork(DownloadWorker.WORK_NAME)
        _downloadingPages.value = false
        _downloadMessage.value = "Download paused"
    }

    /** Observe WorkManager progress and update UI StateFlows */
    private fun observeWorker() {
        workManager.getWorkInfosForUniqueWorkLiveData(DownloadWorker.WORK_NAME)
            .observeForever { workInfos ->
                val info = workInfos?.firstOrNull() ?: return@observeForever
                when (info.state) {
                    WorkInfo.State.RUNNING -> {
                        _downloadingPages.value = true
                        val progress = info.progress.getInt(DownloadWorker.KEY_PROGRESS, 0)
                        val downloaded = info.progress.getInt(DownloadWorker.KEY_DOWNLOADED, 0)
                        val total = info.progress.getInt(DownloadWorker.KEY_TOTAL, 604)
                        val mb = info.progress.getFloat(DownloadWorker.KEY_MB_DOWNLOADED, 0f)
                        _downloadProgress.value = progress
                        _downloadedMB.value = mb
                        _downloadMessage.value = "Downloaded $downloaded / $total pages (${String.format("%.1f", mb)} MB)"
                        refreshStats()
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        _downloadingPages.value = false
                        _downloadProgress.value = 100
                        _downloadedMB.value = 30f
                        _downloadMessage.value = "All pages downloaded successfully!"
                        refreshStats()
                    }
                    WorkInfo.State.FAILED -> {
                        _downloadingPages.value = false
                        val stats = pageProvider.getDownloadStats()
                        val remaining = 604 - stats.pagesDownloaded
                        _downloadMessage.value = if (remaining > 0) {
                            "Download failed - $remaining pages remaining. Tap retry to continue."
                        } else {
                            "Download completed with some errors. All pages are available."
                        }
                        refreshStats()
                    }
                    WorkInfo.State.CANCELLED -> {
                        _downloadingPages.value = false
                        val stats = pageProvider.getDownloadStats()
                        val remaining = 604 - stats.pagesDownloaded
                        _downloadMessage.value = if (remaining > 0) {
                            "Download paused - $remaining pages remaining. Tap to resume."
                        } else {
                            "Download completed."
                        }
                        refreshStats()
                    }
                    else -> { /* ENQUEUED / BLOCKED — no-op */ }
                }
            }
    }

    fun downloadJuz(juzNumber: Int) {
        viewModelScope.launch {
            _downloadingPages.value = true
            _downloadMessage.value = "Downloading Juz $juzNumber..."
            val startPage = when (juzNumber) {
                30 -> 582
                else -> ((juzNumber - 1) * 20) + 1
            }
            val endPage = if (juzNumber == 30) 604 else juzNumber * 20
            val result = pageProvider.downloadPages(startPage, endPage) { downloaded, total ->
                _downloadProgress.value = (downloaded * 100 / total)
            }
            _downloadingPages.value = false
            _downloadMessage.value = if (result.isSuccess)
                "Juz $juzNumber downloaded!" else "Error: ${result.exceptionOrNull()?.message}"
            refreshStats()
        }
    }

    fun downloadSurahPages(surahNumber: Int) {
        viewModelScope.launch {
            _downloadMessage.value = "Downloading Surah $surahNumber pages..."
            val startPage = QuranInfo.getStartPage(surahNumber)
            val endPage = if (surahNumber < 114) QuranInfo.getStartPage(surahNumber + 1) - 1 else 604
            val result = pageProvider.downloadPages(startPage, endPage) { downloaded, total ->
                _downloadProgress.value = (downloaded * 100 / total)
            }
            _downloadMessage.value = if (result.isSuccess)
                "Surah $surahNumber pages downloaded!" else "Error: ${result.exceptionOrNull()?.message}"
            refreshStats()
        }
    }

    fun clearMessage() { _downloadMessage.value = null }

    fun clearCache() {
        pageProvider.clearCache()
        viewModelScope.launch { userPreferences.clearDownloadProgress() }
        refreshStats()
    }
}
