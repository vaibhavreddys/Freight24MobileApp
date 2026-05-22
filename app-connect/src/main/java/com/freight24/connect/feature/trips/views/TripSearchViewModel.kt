package com.freight24.connect.feature.trips.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.rest.api.TripApi
import com.freight24.rest.response.TripSearchResultDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TripSearchUiState(
    val query: String = "",
    val results: List<TripSearchResultDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val scope: String = "my",
)

@HiltViewModel
internal class TripSearchViewModel
    @Inject
    constructor(
        private val tripApi: TripApi,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(TripSearchUiState())
        val uiState: StateFlow<TripSearchUiState> = _uiState.asStateFlow()

        private var searchJob: Job? = null

        fun onQueryChanged(query: String) {
            if (query == _uiState.value.query) return
            _uiState.value = _uiState.value.copy(query = query)
            searchJob?.cancel()
            if (query.isBlank() || query.count { it.isDigit() } < 4) {
                _uiState.value = _uiState.value.copy(results = emptyList(), isLoading = false, error = null)
                return
            }
            searchJob =
                viewModelScope.launch {
                    delay(300)
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    try {
                        val response = tripApi.searchTrips(query = query, scope = _uiState.value.scope)
                        val data = response.body()?.data ?: emptyList()
                        _uiState.value =
                            _uiState.value.copy(results = data, isLoading = false)
                    } catch (e: Exception) {
                        _uiState.value =
                            _uiState.value.copy(
                                results = emptyList(),
                                isLoading = false,
                                error = e.message ?: "Search failed",
                            )
                    }
                }
        }

        fun onScopeChanged(scope: String) {
            _uiState.value = _uiState.value.copy(scope = scope)
            if (_uiState.value.query.isNotBlank()) {
                onQueryChanged(_uiState.value.query)
            }
        }
    }
