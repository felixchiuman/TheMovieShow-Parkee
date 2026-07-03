package com.felix.themovieshow.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.ui.component.EmptyView
import com.felix.themovieshow.ui.component.ErrorView
import com.felix.themovieshow.ui.component.LoadingView
import com.felix.themovieshow.ui.component.MovieRowSection
import com.felix.themovieshow.ui.component.TopHeaderGreeting
import androidx.compose.ui.tooling.preview.Preview
import com.felix.themovieshow.ui.theme.TheMovieShowTheme
import com.felix.themovieshow.ui.theme.BackgroundDark

@Composable
fun HomeScreen(
    userName: String,
    onMovieClick: (Movie) -> Unit,
    onSeeAllClick: (String) -> Unit,
    onFavoriteClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        userName = userName,
        uiState = uiState,
        onMovieClick = onMovieClick,
        onSeeAllClick = onSeeAllClick,
        onFavoriteClick = onFavoriteClick,
        onLoadMorePopular = { viewModel.loadPopularMovies() },
        onLoadMoreTopRated = { viewModel.loadTopRatedMovies() },
        onLoadMoreNowPlaying = {viewModel.loadNowPlayingMovies()},
        onRetry = viewModel::retryLoad
    )
}

@Composable
fun HomeScreenContent(
    userName: String,
    uiState: HomeUiState,
    onMovieClick: (Movie) -> Unit,
    onSeeAllClick: (String) -> Unit,
    onFavoriteClick: () -> Unit,
    onLoadMorePopular: () -> Unit,
    onLoadMoreTopRated: () -> Unit,
    onLoadMoreNowPlaying: () -> Unit,
    onRetry: () -> Unit
) {
    val isInitialLoading = uiState.popularMovies.isEmpty() &&
            uiState.topRatedMovies.isEmpty() &&
            (uiState.isLoadingPopular || uiState.isLoadingTopRated || uiState.isNowPlaying)

    val hasNoData = uiState.popularMovies.isEmpty() &&
            uiState.topRatedMovies.isEmpty() &&
            uiState.nowPlayingMovies.isEmpty()

    Scaffold(containerColor = BackgroundDark) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            TopHeaderGreeting(userName = userName, onFavoriteClick = onFavoriteClick)

            when {
                isInitialLoading -> LoadingView()
                uiState.errorMessage != null && hasNoData -> {
                    ErrorView(message = uiState.errorMessage, onRetry = onRetry)
                }
                hasNoData -> EmptyView("Tidak ada film untuk ditampilkan")
                else -> {
                    Spacer(Modifier.height(8.dp))

                    if (uiState.popularMovies.isNotEmpty()) {
                        MovieRowSection(
                            title = "Popular Movies",
                            movies = uiState.popularMovies,
                            onMovieClick = onMovieClick,
                            onLoadMore = onLoadMorePopular,
                            onViewAllClick = { onSeeAllClick("popular") }
                        )
                    }

                    if (uiState.topRatedMovies.isNotEmpty()) {
                        MovieRowSection(
                            title = "Top Rated",
                            movies = uiState.topRatedMovies,
                            onMovieClick = onMovieClick,
                            onLoadMore = onLoadMoreTopRated,
                            onViewAllClick = { onSeeAllClick("top_rated") }
                        )
                    }

                    if (uiState.nowPlayingMovies.isNotEmpty()) {
                        MovieRowSection(
                            title = "Now Playing",
                            movies = uiState.nowPlayingMovies,
                            onMovieClick = onMovieClick,
                            onLoadMore = onLoadMoreNowPlaying,
                            onViewAllClick = { onSeeAllClick("now_playing") }
                        )
                    }

                    if (uiState.isLoadingPopular || uiState.isLoadingTopRated || uiState.isNowPlaying) {
                        LoadingView()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    TheMovieShowTheme {
        HomeScreenContent(
            userName = "Felix",
            uiState = HomeUiState(
                popularMovies = listOf(
                    Movie(
                        id = 1,
                        title = "Inception",
                        posterPath = "/sample_poster.jpg",
                        backdropPath = null,
                        overview = "A thief who steals corporate secrets...",
                        releaseDate = "2010-07-15",
                        voteAverage = 8.8,
                        genreIds = listOf(1, 2)
                    )
                ),
                topRatedMovies = listOf(
                    Movie(
                        id = 2,
                        title = "The Dark Knight",
                        posterPath = "/sample_poster.jpg",
                        backdropPath = null,
                        overview = "When the menace known as the Joker...",
                        releaseDate = "2008-07-18",
                        voteAverage = 9.0,
                        genreIds = listOf(1, 4)
                    )
                )
            ),
            onMovieClick = {},
            onSeeAllClick = {},
            onFavoriteClick = {},
            onLoadMorePopular = {},
            onLoadMoreTopRated = {},
            onLoadMoreNowPlaying = {},
            onRetry = {}
        )
    }
}