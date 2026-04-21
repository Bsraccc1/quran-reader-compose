package com.quranreader.custom.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Quran API interface for downloading pages, audio, and translations
 * Uses multiple sources:
 * - Quran.com API for pages and translations
 * - EveryAyah.com for audio files
 */
interface QuranApi {
    
    /**
     * Get Quran page image
     * Source: Quran.com CDN
     */
    @GET("data/images/{page}.png")
    suspend fun getPageImage(@Path("page") page: Int): Response<ByteArray>
    
    /**
     * Get audio file for specific ayah
     * Source: EveryAyah.com
     * Format: {surah}{ayah}.mp3 (e.g., 001001.mp3)
     */
    @GET("data/audio/{reciter}/{surah}{ayah}.mp3")
    suspend fun getAudioFile(
        @Path("reciter") reciter: String,
        @Path("surah") surah: String,
        @Path("ayah") ayah: String
    ): Response<ByteArray>
    
    /**
     * Get translation for specific surah
     */
    @GET("v4/quran/translations/{translation_id}")
    suspend fun getTranslation(
        @Path("translation_id") translationId: Int,
        @Query("chapter_number") surahNumber: Int
    ): Response<TranslationResponse>
    
    /**
     * Get list of available translations
     */
    @GET("v4/resources/translations")
    suspend fun getAvailableTranslations(): Response<TranslationsListResponse>
    
    /**
     * Get list of available reciters
     */
    @GET("v4/resources/recitations")
    suspend fun getAvailableReciters(): Response<RecitersListResponse>
}

// Response models
data class TranslationResponse(
    val translations: List<TranslationItem>
)

data class TranslationItem(
    val id: Int,
    val resource_id: Int,
    val text: String,
    val verse_key: String,
    val verse_number: Int
)

data class TranslationsListResponse(
    val translations: List<TranslationInfo>
)

data class TranslationInfo(
    val id: Int,
    val name: String,
    val author_name: String,
    val language_name: String,
    val slug: String
)

data class RecitersListResponse(
    val recitations: List<ReciterInfo>
)

data class ReciterInfo(
    val id: Int,
    val reciter_name: String,
    val style: String,
    val translated_name: TranslatedName?
)

data class TranslatedName(
    val name: String,
    val language_name: String
)
