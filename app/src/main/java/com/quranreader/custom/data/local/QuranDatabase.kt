package com.quranreader.custom.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.quranreader.custom.data.model.Bookmark
import com.quranreader.custom.data.model.AyahCoordinate
import com.quranreader.custom.data.model.QuranText

@Database(
    entities = [Bookmark::class, AyahCoordinate::class, QuranText::class],
    version = 3,
    exportSchema = false
)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun ayahCoordinateDao(): AyahCoordinateDao
    abstract fun quranTextDao(): QuranTextDao
}
