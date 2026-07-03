package com.felix.themovieshow.data.api.model


import com.squareup.moshi.Json

// Model untuk endpoint GET /movie/top_rated

data class TopRatedMovieResponse(
    val page: Int,
    val results: List<Movie>,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "total_results") val totalResults: Int
)