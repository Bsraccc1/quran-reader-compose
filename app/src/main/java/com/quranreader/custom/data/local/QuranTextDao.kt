package com.quranreader.custom.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quranreader.custom.data.model.QuranText

@Dao
interface QuranTextDao {
    @Query("SELECT * FROM quran_text WHERE page = :page ORDER BY surah, ayah")
    suspend fun getTextByPage(page: Int): List<QuranText>
    
    @Query("SELECT * FROM quran_text WHERE surah = :surah AND ayah = :ayah LIMIT 1")
    suspend fun getTextByAyah(surah: Int, ayah: Int): QuranText?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(texts: List<QuranText>)
    
    @Query("SELECT COUNT(*) FROM quran_text")
    suspend fun getCount(): Int
}
