package com.felix.themovieshow.ui.home

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

data class HomeUiState(
    val popularMovies: List<Movie> = emptyList(),
    val isLoadingPopular: Boolean = false,
    val popularPage: Int = 1,
    val topRatedMovies: List<Movie> = emptyList(),
    val isLoadingTopRated: Boolean = false,
    val topRatedPage: Int = 1,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPopularMovies(reset = true)
        loadTopRatedMovies(reset = true)
    }

    fun loadPopularMovies(reset: Boolean = false) {
        if (_uiState.value.isLoadingPopular) return
        viewModelScope.launch {
            val page = if (reset) 1 else _uiState.value.popularPage + 1
            _uiState.update { it.copy(isLoadingPopular = true) }

            when (val result = repository.getPopularMovie(page)) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        val newMovies = if (reset) {
                            result.data.results
                        } else {
                            (state.popularMovies + result.data.results).distinctBy { it.id }
                        }
                        state.copy(
                            isLoadingPopular = false,
                            popularMovies = newMovies,
                            popularPage = page,
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoadingPopular = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun loadTopRatedMovies(reset: Boolean = false) {
        if (_uiState.value.isLoadingTopRated) return
        viewModelScope.launch {
            val page = if (reset) 1 else _uiState.value.topRatedPage + 1
            _uiState.update { it.copy(isLoadingTopRated = true) }

            when (val result = repository.getTopRated(page)) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        val newMovies = if (reset) {
                            result.data.results
                        } else {
                            (state.topRatedMovies + result.data.results).distinctBy { it.id }
                        }
                        state.copy(
                            isLoadingTopRated = false,
                            topRatedMovies = newMovies,
                            topRatedPage = page,
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoadingTopRated = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun retryLoad() {
        loadPopularMovies(reset = true)
        loadTopRatedMovies(reset = true)
    }
}