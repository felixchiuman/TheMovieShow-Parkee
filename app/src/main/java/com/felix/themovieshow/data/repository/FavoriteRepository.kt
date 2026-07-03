package com.felix.themovieshow.data.repository

import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.local.dao.FavoriteMovieDao
import com.felix.themovieshow.data.local.entity.toFavoriteEntity
import com.felix.themovieshow.data.local.entity.toMovie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface FavoriteRepository {
    fun getFavorites(): Flow<List<Movie>>
    fun isFavorite(movieId: Int): Flow<Boolean>
    suspend fun addFavorite(movie: Movie)
    suspend fun removeFavorite(movieId: Int)
}

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val dao: FavoriteMovieDao
) : FavoriteRepository {

    override fun getFavorites(): Flow<List<Movie>> =
        dao.getAllFavorites().map { list -> list.map { it.toMovie() } }

    override fun isFavorite(movieId: Int): Flow<Boolean> =
        dao.isFavorite(movieId)

    override suspend fun addFavorite(movie: Movie) =
        dao.insert(movie.toFavoriteEntity())

    override suspend fun removeFavorite(movieId: Int) =
        dao.deleteById(movieId)
}
