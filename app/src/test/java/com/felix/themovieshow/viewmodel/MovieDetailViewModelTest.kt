package com.felix.themovieshow.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.felix.themovieshow.data.api.model.AuthorDetails
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.api.model.MoviePagedResponse
import com.felix.themovieshow.data.api.model.Review
import com.felix.themovieshow.data.api.model.ReviewPagedResponse
import com.felix.themovieshow.data.api.model.Video
import com.felix.themovieshow.data.repository.FavoriteRepository
import com.felix.themovieshow.data.repository.HomeRepository
import com.felix.themovieshow.data.repository.MovieDetailRepository
import com.felix.themovieshow.data.repository.ReviewRepository
import com.felix.themovieshow.data.resource.Resource
import com.felix.themovieshow.ui.detail.MovieDetailViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var detailRepository: MovieDetailRepository
    private lateinit var reviewRepository: ReviewRepository
    private lateinit var homeRepository: HomeRepository
    private lateinit var favoriteRepository: FavoriteRepository

    private val movieId = 931285

    private val sampleMovie = Movie(
        id = movieId,
        title = "Mortal Kombat II",
        posterPath = "/poster.jpg",
        backdropPath = "/backdrop.jpg",
        overview = "Sample overview",
        releaseDate = "2026-05-07",
        voteAverage = 8.0,
        genreIds = listOf(28)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        detailRepository = mockk()
        reviewRepository = mockk()
        homeRepository = mockk()
        favoriteRepository = mockk()
        
        // Default mock for favorite status
        coEvery { favoriteRepository.isFavorite(movieId) } returns flowOf(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MovieDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("movieId" to movieId))
        return MovieDetailViewModel(
            detailRepository,
            reviewRepository,
            homeRepository,
            favoriteRepository,
            savedStateHandle
        )
    }

    private fun stubDefaultsSuccess() {
        coEvery { detailRepository.getMovieTrailer(movieId) } returns Resource.Success(null)
        coEvery { reviewRepository.getMovieReviews(movieId, 1) } returns
            Resource.Success(ReviewPagedResponse(1, emptyList(), 1))
        coEvery { homeRepository.discoverMoviesByGenre(any(), 1) } returns
            Resource.Success(MoviePagedResponse(1, emptyList(), 1))
    }

    @Test
    fun `loadMovieDetail sets movie and triggers trailer, review, related fetch on success`() =
        runTest(testDispatcher) {
            coEvery { detailRepository.getMovieDetail(movieId) } returns Resource.Success(sampleMovie)
            coEvery { detailRepository.getMovieTrailer(movieId) } returns
                Resource.Success(Video(id = "1", key = "abc123", site = "YouTube", type = "Trailer"))
            coEvery { reviewRepository.getMovieReviews(movieId, 1) } returns Resource.Success(
                ReviewPagedResponse(
                    1,
                    listOf(Review("1", "Felix", "Bagus!", AuthorDetails(9.0))),
                    1
                )
            )
            coEvery { homeRepository.discoverMoviesByGenre(28, 1) } returns Resource.Success(
                MoviePagedResponse(1, listOf(sampleMovie.copy(id = 2, title = "Related Movie")), 1)
            )

            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(sampleMovie, state.movie)
            assertEquals("abc123", state.trailerKey)
            assertEquals(1, state.reviewPreview.size)
            assertEquals(1, state.relatedMovies.size)
            assertFalse(state.isLoading)
        }

    @Test
    fun `loadMovieDetail sets errorMessage when api call fails`() = runTest(testDispatcher) {
        coEvery { detailRepository.getMovieDetail(movieId) } returns Resource.Error("Movie not found")

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Movie not found", state.errorMessage)
        assertNull(state.movie)
        assertFalse(state.isLoading)
    }

    @Test
    fun `observeFavoriteStatus updates isSaved state`() = runTest(testDispatcher) {
        coEvery { detailRepository.getMovieDetail(movieId) } returns Resource.Success(sampleMovie)
        stubDefaultsSuccess()
        coEvery { favoriteRepository.isFavorite(movieId) } returns flowOf(true)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `toggleSave adds favorite when currently not saved`() = runTest(testDispatcher) {
        coEvery { detailRepository.getMovieDetail(movieId) } returns Resource.Success(sampleMovie)
        stubDefaultsSuccess()
        coEvery { favoriteRepository.isFavorite(movieId) } returns flowOf(false)
        coEvery { favoriteRepository.addFavorite(sampleMovie) } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleSave()
        advanceUntilIdle()

        coVerify(exactly = 1) { favoriteRepository.addFavorite(sampleMovie) }
    }

    @Test
    fun `toggleSave removes favorite when currently saved`() = runTest(testDispatcher) {
        coEvery { detailRepository.getMovieDetail(movieId) } returns Resource.Success(sampleMovie)
        stubDefaultsSuccess()
        coEvery { favoriteRepository.isFavorite(movieId) } returns flowOf(true)
        coEvery { favoriteRepository.removeFavorite(movieId) } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)

        viewModel.toggleSave()
        advanceUntilIdle()

        coVerify(exactly = 1) { favoriteRepository.removeFavorite(movieId) }
    }
}
