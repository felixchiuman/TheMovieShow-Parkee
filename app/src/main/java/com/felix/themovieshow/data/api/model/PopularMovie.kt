package com.felix.themovieshow.data.api.model


import com.squareup.moshi.Json

// Model untuk endpoint GET /movie/popular

data class PopularMovieResponse(
    val page: Int,
    val results: List<Movie>,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "total_results") val totalResults: Int
)