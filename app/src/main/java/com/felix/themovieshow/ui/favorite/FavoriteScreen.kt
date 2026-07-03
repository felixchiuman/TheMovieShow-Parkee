package com.felix.themovieshow.ui.favorite

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.ui.component.EmptyView
import com.felix.themovieshow.ui.component.LoadingView
import com.felix.themovieshow.ui.theme.AccentRed
import com.felix.themovieshow.ui.theme.BackgroundDark
import com.felix.themovieshow.ui.theme.TextSecondary
import com.felix.themovieshow.ui.theme.TheMovieShowTheme

@Composable
fun FavoriteScreen(
    onBackClick: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    viewModel: FavoriteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    FavoriteContent(
        favorites = uiState.favorites,
        isLoading = uiState.isLoading,
        onBackClick = onBackClick,
        onMovieClick = onMovieClick,
        onRemoveClick = viewModel::removeFavorite
    )
}

@Composable
fun FavoriteContent(
    favorites: List<Movie>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    onRemoveClick: (Int) -> Unit
) {
    Scaffold(containerColor = BackgroundDark) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    "Favorite Movie",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            when {
                isLoading -> LoadingView()
                favorites.isEmpty() -> EmptyView(
                    message = "Belum ada film favorit.\nTap ikon ❤ di halaman detail untuk menyimpan."
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(favorites, key = { it.id }) { movie ->
                        FavoriteMovieRow(
                            movie = movie,
                            onClick = { onMovieClick(movie) },
                            onRemoveClick = { onRemoveClick(movie.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteMovieRow(
    movie: Movie,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = movie.posterUrl,
            contentDescription = movie.title,
            modifier = Modifier
                .width(90.dp)
                .height(130.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                movie.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            movie.releaseDate?.take(4)?.let { year ->
                Spacer(Modifier.height(2.dp))
                Text(year, color = TextSecondary, fontSize = 12.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                movie.overview,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onRemoveClick) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = "Remove from favorites",
                tint = AccentRed,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ============ PREVIEWS ============

private val sampleFavorites = listOf(
    Movie(
        id = 1,
        title = "Mortal Kombat II",
        posterPath = "/sample.jpg",
        backdropPath = null,
        overview = "The fan favorite champions are pitted against one another in the ultimate battle.",
        releaseDate = "2026-05-07",
        voteAverage = 8.0,
        genreIds = listOf(28)
    ),
    Movie(
        id = 2,
        title = "Dune: Part Three",
        posterPath = "/sample2.jpg",
        backdropPath = null,
        overview = "Paul Atreides continues his journey across Arrakis.",
        releaseDate = "2026-12-18",
        voteAverage = 8.5,
        genreIds = listOf(878)
    )
)

@Preview(showBackground = true, name = "Favorite - Filled")
@Composable
private fun FavoriteContentPreview() {
    TheMovieShowTheme {
        FavoriteContent(
            favorites = sampleFavorites,
            isLoading = false,
            onBackClick = {},
            onMovieClick = {},
            onRemoveClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Favorite - Empty")
@Composable
private fun FavoriteEmptyPreview() {
    TheMovieShowTheme {
        FavoriteContent(
            favorites = emptyList(),
            isLoading = false,
            onBackClick = {},
            onMovieClick = {},
            onRemoveClick = {}
        )
    }
}
