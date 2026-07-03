package com.felix.themovieshow.data.repository

import com.felix.themovieshow.data.api.model.NowPlayingResponse
import com.felix.themovieshow.data.api.model.PopularMovieResponse
import com.felix.themovieshow.data.api.model.TopRatedMovieResponse
import com.felix.themovieshow.data.api.network.ApiService
import com.felix.themovieshow.data.resource.Resource
import javax.inject.Inject
import javax.inject.Singleton

interface HomeRepository {
    suspend fun getPopularMovie(page: Int): Resource<PopularMovieResponse>
    suspend fun getTopRated(page: Int): Resource<TopRatedMovieResponse>
    suspend fun getNowPlaying(page: Int): Resource<NowPlayingResponse>
}

@Singleton
class HomeRepositoryImpl @Inject constructor(
    private val api: ApiService
) : HomeRepository {

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

    override suspend fun getNowPlaying(page: Int): Resource<NowPlayingResponse> {
        return try {
            val response = api.getNowPlaying(page)
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal mengambil daftar film")
        }
    }
}
