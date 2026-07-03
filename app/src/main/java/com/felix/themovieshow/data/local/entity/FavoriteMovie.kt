package com.felix.themovieshow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.felix.themovieshow.data.api.model.Movie

/**
 * Entity Room untuk menyimpan film favorit ke database lokal.
 *
 * Kita simpan seluruh field yang dibutuhkan buat render Favorite List & buka Detail lagi
 * secara offline (tanpa perlu hit API), plus [addedAt] untuk sorting (favorit terbaru di atas).
 */
@Entity(tableName = "favorite_movies")
data class FavoriteMovie(
    @PrimaryKey val id: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String,
    val releaseDate: String?,
    val voteAverage: Double,
    val genreIds: String,
    val addedAt: Long = System.currentTimeMillis()
)

fun Movie.toFavoriteEntity(): FavoriteMovie = FavoriteMovie(
    id = id,
    title = title,
    posterPath = posterPath,
    backdropPath = backdropPath,
    overview = overview,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    genreIds = genreIds.joinToString(",")
)

fun FavoriteMovie.toMovie(): Movie = Movie(
    id = id,
    title = title,
    posterPath = posterPath,
    backdropPath = backdropPath,
    overview = overview,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    genreIds = if (genreIds.isBlank()) emptyList()
    else genreIds.split(",").mapNotNull { it.toIntOrNull() }
)
