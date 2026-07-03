package com.felix.themovieshow.viewmodel

import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.api.model.PopularMovieResponse
import com.felix.themovieshow.data.api.model.TopRatedMovieResponse
import com.felix.themovieshow.data.repository.HomeRepository
import com.felix.themovieshow.data.resource.Resource
import com.felix.themovieshow.ui.home.HomeViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: HomeRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun sampleMovie(id: Int) = Movie(
        id = id,
        title = "Movie $id",
        posterPath = null,
        backdropPath = null,
        overview = "",
        releaseDate = "2026-01-01",
        voteAverage = 7.0,
        genreIds = listOf(1)
    )

    @Test
    fun `init loads popular and top rated movies on success`() = runTest(testDispatcher) {
        val popularResponse = PopularMovieResponse(page = 1, results = listOf(sampleMovie(1)), totalPages = 5, totalResults = 100)
        val topRatedResponse = TopRatedMovieResponse(page = 1, results = listOf(sampleMovie(2)), totalPages = 5, totalResults = 100)

        coEvery { repository.getPopularMovie(1) } returns Resource.Success(popularResponse)
        coEvery { repository.getTopRated(1) } returns Resource.Success(topRatedResponse)

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.popularMovies.size)
        assertEquals(1, state.topRatedMovies.size)
        assertEquals(1, state.popularMovies[0].id)
        assertEquals(2, state.topRatedMovies[0].id)
        assertFalse(state.isLoadingPopular)
        assertFalse(state.isLoadingTopRated)
        assertNull(state.errorMessage)
    }

    @Test
    fun `init sets errorMessage when api call fails`() = runTest(testDispatcher) {
        coEvery { repository.getPopularMovie(1) } returns Resource.Error("Network error")
        coEvery { repository.getTopRated(1) } returns Resource.Success(TopRatedMovieResponse(1, emptyList(), 1, 0))

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Network error", state.errorMessage)
        assertFalse(state.isLoadingPopular)
    }

    @Test
    fun `loadPopularMovies appends movies and increments page`() = runTest(testDispatcher) {
        val page1 = PopularMovieResponse(1, listOf(sampleMovie(1)), 5, 100)
        val page2 = PopularMovieResponse(2, listOf(sampleMovie(2)), 5, 100)

        coEvery { repository.getPopularMovie(1) } returns Resource.Success(page1)
        coEvery { repository.getPopularMovie(2) } returns Resource.Success(page2)
        coEvery { repository.getTopRated(any()) } returns Resource.Success(TopRatedMovieResponse(1, emptyList(), 1, 0))

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        viewModel.loadPopularMovies()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.popularMovies.size)
        assertEquals(2, state.popularPage)
        assertEquals(listOf(1, 2), state.popularMovies.map { it.id })
    }

    @Test
    fun `loadPopularMovies with reset true clears existing movies`() = runTest(testDispatcher) {
        val page1 = PopularMovieResponse(1, listOf(sampleMovie(1)), 5, 100)
        val resetPage = PopularMovieResponse(1, listOf(sampleMovie(3)), 5, 100)

        coEvery { repository.getPopularMovie(1) } returns Resource.Success(page1)
        coEvery { repository.getTopRated(any()) } returns Resource.Success(TopRatedMovieResponse(1, emptyList(), 1, 0))

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        coEvery { repository.getPopularMovie(1) } returns Resource.Success(resetPage)
        viewModel.loadPopularMovies(reset = true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.popularMovies.size)
        assertEquals(3, state.popularMovies[0].id)
    }

    @Test
    fun `loadPopularMovies does nothing when already loading`() = runTest(testDispatcher) {
        coEvery { repository.getPopularMovie(1) } returns Resource.Success(PopularMovieResponse(1, listOf(sampleMovie(1)), 5, 100))
        coEvery { repository.getTopRated(any()) } returns Resource.Success(TopRatedMovieResponse(1, emptyList(), 1, 0))

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        coEvery { repository.getPopularMovie(2) } coAnswers {
            Resource.Success(PopularMovieResponse(2, listOf(sampleMovie(2)), 5, 100))
        }

        viewModel.loadPopularMovies()

        advanceUntilIdle()

        coVerify(exactly = 1) { repository.getPopularMovie(2) }
    }

    @Test
    fun `retryLoad reloads both popular and top rated movies`() = runTest(testDispatcher) {
        coEvery { repository.getPopularMovie(1) } returns Resource.Error("Error")
        coEvery { repository.getTopRated(1) } returns Resource.Error("Error")

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        coEvery { repository.getPopularMovie(1) } returns Resource.Success(PopularMovieResponse(1, listOf(sampleMovie(1)), 5, 100))
        coEvery { repository.getTopRated(1) } returns Resource.Success(TopRatedMovieResponse(1, listOf(sampleMovie(2)), 5, 100))

        viewModel.retryLoad()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.popularMovies.size)
        assertEquals(1, state.topRatedMovies.size)
        assertNull(state.errorMessage)
    }
}
