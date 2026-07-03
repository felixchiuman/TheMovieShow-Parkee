package com.felix.themovieshow.di

import android.content.Context
import androidx.room.Room
import com.felix.themovieshow.data.local.AppDatabase
import com.felix.themovieshow.data.local.dao.FavoriteMovieDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "themovieshow.db"
        ).build()

    @Provides
    @Singleton
    fun provideFavoriteMovieDao(db: AppDatabase): FavoriteMovieDao =
        db.favoriteMovieDao()
}
