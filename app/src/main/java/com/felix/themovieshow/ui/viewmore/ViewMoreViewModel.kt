package com.felix.themovieshow.ui.viewmore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.repository.HomeRepository
import com.felix.themovieshow.data.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ViewMoreUiState(
    val movies: List<Movie> = emptyList(),
    val currentPage: Int = 1,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val endReached: Boolean = false
)

@HiltViewModel
class ViewMoreViewModel @Inject constructor(
    private val repository: HomeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val category: String = checkNotNull(savedStateHandle["category"])

    private val _uiState = MutableStateFlow(ViewMoreUiState())
    val uiState: StateFlow<ViewMoreUiState> = _uiState.asStateFlow()

    init {
        loadMovies(reset = true)
    }

    fun loadMore() {
        if (_uiState.value.isLoading || _uiState.value.endReached) return
        _uiState.update { it.copy(isLoading = true) }
        loadMovies(reset = false)
    }

    fun retry() {
        loadMovies(reset = true)
    }

    private fun loadMovies(reset: Boolean) {
        viewModelScope.launch {
            val page = if (reset) 1 else _uiState.value.currentPage + 1
            if (reset) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            if (category == "popular") {
                when (val result = repository.getPopularMovie(page)) {
                    is Resource.Success -> handleSuccess(result.data.results, result.data.totalPages, page, reset)
                    is Resource.Error -> handleError(result.message)
                }
            } else {
                when (val result = repository.getTopRated(page)) {
                    is Resource.Success -> handleSuccess(result.data.results, result.data.totalPages, page, reset)
                    is Resource.Error -> handleError(result.message)
                }
            }
        }
    }

    private fun handleSuccess(results: List<Movie>, totalPages: Int, page: Int, reset: Boolean) {
        _uiState.update { state ->
            val newMovies = if (reset) {
                results
            } else {
                (state.movies + results).distinctBy { it.id }
            }
            state.copy(
                isLoading = false,
                movies = newMovies,
                currentPage = page,
                endReached = page >= totalPages,
                errorMessage = null
            )
        }
    }

    private fun handleError(message: String?) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }
}