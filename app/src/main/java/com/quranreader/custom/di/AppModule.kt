package com.quranreader.custom.di

import android.content.Context
import androidx.room.Room
import com.quranreader.custom.data.local.AyahCoordinateDao
import com.quranreader.custom.data.local.BookmarkDao
import com.quranreader.custom.data.local.QuranDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideQuranDatabase(
        @ApplicationContext context: Context
    ): QuranDatabase {
        return Room.databaseBuilder(
            context,
            QuranDatabase::class.java,
            "quran_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideBookmarkDao(database: QuranDatabase): BookmarkDao {
        return database.bookmarkDao()
    }

    @Provides
    @Singleton
    fun provideAyahCoordinateDao(database: QuranDatabase): AyahCoordinateDao {
        return database.ayahCoordinateDao()
    }

    @Provides
    @Singleton
    fun provideQuranTextDao(database: QuranDatabase): com.quranreader.custom.data.local.QuranTextDao {
        return database.quranTextDao()
    }

    @Provides
    @Singleton
    fun provideQuranTextDownloader(
        @ApplicationContext context: Context,
        quranTextDao: com.quranreader.custom.data.local.QuranTextDao
    ): com.quranreader.custom.data.remote.QuranTextDownloader {
        return com.quranreader.custom.data.remote.QuranTextDownloader(context, quranTextDao)
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // Increased for slow connections
            .readTimeout(120, TimeUnit.SECONDS)   // Increased for large image downloads
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)       // Enable automatic retry
            .build()
    }
}
