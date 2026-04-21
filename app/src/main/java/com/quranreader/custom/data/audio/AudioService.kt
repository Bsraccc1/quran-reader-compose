package com.quranreader.custom.data.audio

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * F-03, F-04: Audio Service for playing Quran recitation
 * Supports range playback and repeat functionality
 */
class AudioService : Service() {

    private val binder = AudioBinder()
    private var mediaPlayer: MediaPlayer? = null

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentAyah = MutableStateFlow<AyahInfo?>(null)
    val currentAyah: StateFlow<AyahInfo?> = _currentAyah.asStateFlow()

    private var playlist: List<AyahInfo> = emptyList()
    private var currentIndex = 0
    private var repeatCount = 1
    private var currentRepeat = 0

    inner class AudioBinder : Binder() {
        fun getService(): AudioService = this@AudioService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    /**
     * F-03: Play ayat range from X to Y
     */
    fun playRange(surah: Int, fromAyah: Int, toAyah: Int, repeat: Int = 1) {
        playlist = (fromAyah..toAyah).map { ayah ->
            AyahInfo(surah, ayah)
        }
        repeatCount = repeat
        currentIndex = 0
        currentRepeat = 0
        playCurrentAyah()
    }

    /**
     * F-04: Set repeat count (0 = infinite)
     */
    fun setRepeatCount(count: Int) {
        repeatCount = count
    }

    private fun playCurrentAyah() {
        if (currentIndex >= playlist.size) {
            // End of playlist
            if (repeatCount == 0 || currentRepeat < repeatCount - 1) {
                // Repeat playlist
                currentRepeat++
                currentIndex = 0
                playCurrentAyah()
            } else {
                // Stop
                stop()
            }
            return
        }

        val ayah = playlist[currentIndex]
        _currentAyah.value = ayah
        _playbackState.value = PlaybackState.Playing

        // In production, load actual audio file from assets
        // For now, simulate playback
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            // setDataSource("path/to/audio/${ayah.surah}_${ayah.ayah}.mp3")
            // prepare()
            // start()
            setOnCompletionListener {
                onAyahCompleted()
            }
        }
    }

    private fun onAyahCompleted() {
        currentIndex++
        playCurrentAyah()
    }

    fun play() {
        mediaPlayer?.start()
        _playbackState.value = PlaybackState.Playing
    }

    fun pause() {
        mediaPlayer?.pause()
        _playbackState.value = PlaybackState.Paused
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _playbackState.value = PlaybackState.Idle
        _currentAyah.value = null
        currentIndex = 0
        currentRepeat = 0
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
    }
}

sealed class PlaybackState {
    object Idle : PlaybackState()
    object Playing : PlaybackState()
    object Paused : PlaybackState()
}

data class AyahInfo(
    val surah: Int,
    val ayah: Int
)
