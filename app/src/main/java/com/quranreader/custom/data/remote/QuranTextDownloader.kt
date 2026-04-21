package com.quranreader.custom.data.remote

import android.content.Context
import android.util.Log
import com.quranreader.custom.data.QuranInfo
import com.quranreader.custom.data.local.QuranTextDao
import com.quranreader.custom.data.model.QuranText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Downloads and populates Quran text from Tanzil.net
 * Uses Uthmani script (similar to Medina Mushaf)
 */
class QuranTextDownloader(
    private val context: Context,
    private val quranTextDao: QuranTextDao
) {
    companion object {
        private const val TAG = "QuranTextDownloader"
        // Tanzil Quran text - Uthmani script
        private const val TANZIL_URL = "https://tanzil.net/trans/?transID=en.sahih&type=txt-2"
        private const val TANZIL_UTHMANI_URL = "https://tanzil.net/pub/download/download.php?file=quran-uthmani.txt&type=txt-2"
    }

    /**
     * Check if database is already populated
     */
    suspend fun isDataAvailable(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                quranTextDao.getCount() > 6000 // Quran has 6236 ayahs
            } catch (e: Exception) {
                Log.e(TAG, "Error checking data availability", e)
                false
            }
        }
    }

    /**
     * Download and populate Quran text
     */
    suspend fun downloadAndPopulate(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting Quran text download...")
                
                // Download from Tanzil
                val quranTexts = downloadFromTanzil()
                
                if (quranTexts.isEmpty()) {
                    return@withContext Result.failure(Exception("No data downloaded"))
                }
                
                // Insert into database
                quranTextDao.insertAll(quranTexts)
                
                Log.d(TAG, "Successfully populated ${quranTexts.size} ayahs")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading Quran text", e)
                Result.failure(e)
            }
        }
    }

    private fun downloadFromTanzil(): List<QuranText> {
        val texts = mutableListOf<QuranText>()
        
        try {
            val url = URL(TANZIL_UTHMANI_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                var line: String?
                
                while (reader.readLine().also { line = it } != null) {
                    line?.let { parseTanzilLine(it, texts) }
                }
                
                reader.close()
            } else {
                Log.e(TAG, "HTTP error: $responseCode")
            }
            
            connection.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading from Tanzil", e)
            // Fallback to embedded sample data
            return getSampleData()
        }
        
        return texts
    }

    private fun parseTanzilLine(line: String, texts: kotlin.collections.MutableList<QuranText>) {
        // Skip comments and empty lines
        if (line.startsWith("#") || line.isBlank()) return
        
        // Format: surah|ayah|text
        val parts = line.split("|")
        if (parts.size >= 3) {
            try {
                val surah = parts[0].toInt()
                val ayah = parts[1].toInt()
                val text = parts[2]
                
                // Get page number from QuranInfo
                val page = getPageForAyah(surah, ayah)
                
                texts.add(QuranText(
                    surah = surah,
                    ayah = ayah,
                    page = page,
                    text = text
                ))
            } catch (e: Exception) {
                Log.w(TAG, "Error parsing line: $line", e)
            }
        }
    }

    private fun getPageForAyah(surah: Int, ayah: Int): Int {
        // This is a simplified mapping - ideally should use proper page mapping
        // For now, use surah start page as approximation
        return QuranInfo.getStartPage(surah)
    }

    /**
     * Sample data for testing (Al-Fatihah)
     */
    private fun getSampleData(): List<QuranText> {
        return listOf(
            QuranText(surah = 1, ayah = 1, page = 1, text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ"),
            QuranText(surah = 1, ayah = 2, page = 1, text = "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَـٰلَمِينَ"),
            QuranText(surah = 1, ayah = 3, page = 1, text = "ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ"),
            QuranText(surah = 1, ayah = 4, page = 1, text = "مَـٰلِكِ يَوْمِ ٱلدِّينِ"),
            QuranText(surah = 1, ayah = 5, page = 1, text = "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ"),
            QuranText(surah = 1, ayah = 6, page = 1, text = "ٱهْدِنَا ٱلصِّرَٰطَ ٱلْمُسْتَقِيمَ"),
            QuranText(surah = 1, ayah = 7, page = 1, text = "صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ ٱلْمَغْضُوبِ عَلَيْهِمْ وَلَا ٱلضَّآلِّينَ")
        )
    }
}
