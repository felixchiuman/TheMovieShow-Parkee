package com.felix.themovieshow.viewmodel

import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.repository.FavoriteRepository
import com.felix.themovieshow.ui.favorite.FavoriteViewModel
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoriteViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FavoriteRepository

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
    fun `uiState emits favorites from repository`() = runTest(testDispatcher) {
        val favorites = listOf(sampleMovie(1), sampleMovie(2))
        coEvery { repository.getFavorites() } returns flowOf(favorites)

        val viewModel = FavoriteViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(favorites, state.favorites)
        assertEquals(2, state.favorites.size)
    }

    @Test
    fun `removeFavorite calls repository removeFavorite`() = runTest(testDispatcher) {
        coEvery { repository.getFavorites() } returns flowOf(emptyList())
        coEvery { repository.removeFavorite(1) } returns Unit

        val viewModel = FavoriteViewModel(repository)
        viewModel.removeFavorite(1)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.removeFavorite(1) }
    }
}
