package com.freight24.connect.feature.trucks.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.trucks.domain.usecases.GetTrucksUseCase
import com.freight24.rest.response.TruckDto
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageTrucksViewModel
    @Inject
    constructor(
        private val getTrucksUseCase: GetTrucksUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<UiState<List<TruckDto>>>(UiState.Loading)
        val uiState: StateFlow<UiState<List<TruckDto>>> = _uiState.asStateFlow()

        private val _searchQuery = MutableStateFlow("")
        val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        val filteredTrucks: StateFlow<List<TruckDto>> =
            combine(
                _uiState,
                _searchQuery,
            ) { state, query ->
                if (state is UiState.Success) {
                    if (query.isBlank()) {
                        state.data
                    } else {
                        state.data.filter {
                            it.registrationNumber.contains(query, ignoreCase = true)
                        }
                    }
                } else {
                    emptyList()
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        init {
            loadTrucks()
        }

        fun onSearchQueryChanged(query: String) {
            _searchQuery.value = query
        }

        fun onRefresh() {
            loadTrucks(isRefresh = true)
        }

        fun retry() {
            loadTrucks()
        }

        private fun loadTrucks(isRefresh: Boolean = false) {
            viewModelScope.launch {
                if (isRefresh) {
                    _isRefreshing.value = true
                } else {
                    _uiState.value = UiState.Loading
                }

                getTrucksUseCase()
                    .onSuccess { trucks ->
                        _uiState.value = UiState.Success(trucks)
                        _isRefreshing.value = false
                    }
                    .onFailure { error ->
                        // A refresh failure must not wipe a visible list into a
                        // full-screen error; only the initial load shows Error.
                        if (!isRefresh) {
                            _uiState.value = UiState.Error(error.message ?: "Failed to load trucks")
                        }
                        _isRefreshing.value = false
                    }
            }
        }
    }
