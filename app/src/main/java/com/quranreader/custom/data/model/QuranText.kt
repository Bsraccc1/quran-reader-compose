package com.quranreader.custom.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Quran text entity for storing Arabic text
 */
@Entity(tableName = "quran_text")
data class QuranText(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val surah: Int,
    val ayah: Int,
    val page: Int,
    val text: String  // Arabic text in Uthmani script
)
