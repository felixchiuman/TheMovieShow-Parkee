package com.felix.themovieshow.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.api.model.PopularMovieResponse
import com.felix.themovieshow.data.repository.HomeRepository
import com.felix.themovieshow.data.resource.Resource
import com.felix.themovieshow.ui.viewmore.ViewMoreViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewMoreViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: HomeRepository
    private val category = "popular"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ViewMoreViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("category" to category))
        return ViewMoreViewModel(repository, savedStateHandle)
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

    // ============ POSITIVE CASE ============

    @Test
    fun `init loads first page of movies on success`() = runTest(testDispatcher) {
        coEvery { repository.getPopularMovie(1) } returns Resource.Success(
            PopularMovieResponse(1, listOf(sampleMovie(1), sampleMovie(2)), totalPages = 5, totalResults = 10)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.movies.size)
        assertEquals(1, state.currentPage)
        assertFalse(state.isLoading)
        assertFalse(state.endReached)
    }

    @Test
    fun `loadMore appends next page and keeps previous movies`() = runTest(testDispatcher) {
        coEvery { repository.getPopularMovie(1) } returns Resource.Success(
            PopularMovieResponse(1, listOf(sampleMovie(1)), totalPages = 5, totalResults = 5)
        )
        coEvery { repository.getPopularMovie(2) } returns Resource.Success(
            PopularMovieResponse(2, listOf(sampleMovie(2)), totalPages = 5, totalResults = 5)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(1, 2), state.movies.map { it.id })
        assertEquals(2, state.currentPage)
    }

    // ============ NEGATIVE CASE ============

    @Test
    fun `init sets errorMessage when initial load fails`() = runTest(testDispatcher) {
        coEvery { repository.getPopularMovie(1) } returns
                Resource.Error("Gagal mengambil daftar film")

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Gagal mengambil daftar film", state.errorMessage)
        assertTrue(state.movies.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadMore sets errorMessage when pagination fails, without clearing existing movies`() =
        runTest(testDispatcher) {
            coEvery { repository.getPopularMovie(1) } returns Resource.Success(
                PopularMovieResponse(1, listOf(sampleMovie(1)), totalPages = 5, totalResults = 5)
            )
            coEvery { repository.getPopularMovie(2) } returns
                    Resource.Error("Network timeout")

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.loadMore()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Network timeout", state.errorMessage)
            assertEquals(1, state.movies.size)
            assertFalse(state.isLoading)
        }

    @Test
    fun `retry after error successfully reloads movies`() = runTest(testDispatcher) {
        coEvery { repository.getPopularMovie(1) } returns
                Resource.Error("Server error")

        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.movies.isEmpty())

        coEvery { repository.getPopularMovie(1) } returns Resource.Success(
            PopularMovieResponse(1, listOf(sampleMovie(1)), totalPages = 5, totalResults = 5)
        )

        viewModel.retry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.movies.size)
        assertEquals(null, state.errorMessage)
    }

    // ============ REGRESSION TEST ============

    @Test
    fun `calling loadMore three times rapidly only triggers ONE fetch for the next page`() =
        runTest(testDispatcher) {
            coEvery { repository.getPopularMovie(1) } returns Resource.Success(
                PopularMovieResponse(1, listOf(sampleMovie(1)), totalPages = 5, totalResults = 5)
            )
            coEvery { repository.getPopularMovie(2) } returns Resource.Success(
                PopularMovieResponse(2, listOf(sampleMovie(2)), totalPages = 5, totalResults = 5)
            )

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.loadMore()
            viewModel.loadMore()
            viewModel.loadMore()
            advanceUntilIdle()

            coVerify(exactly = 1) { repository.getPopularMovie(2) }
        }

    @Test
    fun `loadMore does nothing once endReached is true`() = runTest(testDispatcher) {
        coEvery { repository.getPopularMovie(1) } returns Resource.Success(
            PopularMovieResponse(1, listOf(sampleMovie(1)), totalPages = 1, totalResults = 1)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.endReached)

        viewModel.loadMore()
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.getPopularMovie(any()) }
    }
}