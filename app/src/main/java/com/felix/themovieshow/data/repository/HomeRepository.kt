package com.felix.themovieshow.data.repository

import com.felix.themovieshow.data.api.model.Genre
import com.felix.themovieshow.data.api.model.MoviePagedResponse
import com.felix.themovieshow.data.api.model.PopularMovieResponse
import com.felix.themovieshow.data.api.model.TopRatedMovieResponse
import com.felix.themovieshow.data.api.network.ApiService
import com.felix.themovieshow.data.resource.Resource
import javax.inject.Inject
import javax.inject.Singleton

interface HomeRepository {
    suspend fun getGenres(): Resource<List<Genre>>
    suspend fun discoverMoviesByGenre(genreId: Int, page: Int): Resource<MoviePagedResponse>
    suspend fun getPopularMovie(page: Int): Resource<PopularMovieResponse>
    suspend fun getTopRated(page: Int): Resource<TopRatedMovieResponse>
}

@Singleton
class HomeRepositoryImpl @Inject constructor(
    private val api: ApiService
) : HomeRepository {

    override suspend fun getGenres(): Resource<List<Genre>> {
        return try {
            val response = api.getGenres()
            Resource.Success(response.genres)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal mengambil daftar genre")
        }
    }

    override suspend fun discoverMoviesByGenre(genreId: Int, page: Int): Resource<MoviePagedResponse> {
        return try {
            val response = api.discoverMoviesByGenre(genreId, page)
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal mengambil daftar film untuk genre ini")
        }
    }

    override suspend fun getPopularMovie(page: Int): Resource<PopularMovieResponse> {
        return try {
            val response = api.getMoviePopular(page)
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal mengambil daftar film")
        }
    }

    override suspend fun getTopRated(page: Int): Resource<TopRatedMovieResponse> {
        return try {
            val response = api.getTopRated(page)
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal mengambil daftar film")
        }
    }
}
