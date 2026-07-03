package com.felix.themovieshow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.felix.themovieshow.data.local.dao.FavoriteMovieDao
import com.felix.themovieshow.data.local.entity.FavoriteMovie

@Database(
    entities = [FavoriteMovie::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteMovieDao(): FavoriteMovieDao
}
