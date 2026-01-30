package com.freight24.trucker.feature.trucks.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.model.Truck
import com.freight24.common.usecase.EmptyUseCaseParams
import com.freight24.trucker.feature.trucks.domain.usecase.FetchTrucksUseCase
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TruckFilter {
    ALL,
    AVAILABLE,
    ON_TRIP,
    BLACKLISTED,
}

@HiltViewModel
internal class TrucksViewModel
    @Inject
    constructor(
        private val fetchTrucksUseCase: FetchTrucksUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<UiState<List<Truck>>>(UiState.Loading)
        val uiState: StateFlow<UiState<List<Truck>>> = _uiState.asStateFlow()

        private val _selectedFilter = MutableStateFlow(TruckFilter.ALL)
        val selectedFilter: StateFlow<TruckFilter> = _selectedFilter.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        private var allTrucks: List<Truck> = emptyList()

        init {
            fetchTrucks()
        }

        fun fetchTrucks(forceRefresh: Boolean = false) {
            viewModelScope.launch {
                if (forceRefresh) {
                    _isRefreshing.value = true
                } else if (allTrucks.isEmpty()) {
                    _uiState.value = UiState.Loading
                }

                fetchTrucksUseCase(EmptyUseCaseParams)
                    .onSuccess { trucks ->
                        allTrucks = trucks
                        applyFilter()
                    }
                    .onFailure { error ->
                        if (allTrucks.isEmpty()) {
                            _uiState.value = UiState.Error(error.message ?: "Failed to fetch trucks")
                        }
                    }

                _isRefreshing.value = false
            }
        }

        fun onFilterSelected(filter: TruckFilter) {
            _selectedFilter.value = filter
            applyFilter()
        }

        private fun applyFilter() {
            val filtered =
                when (_selectedFilter.value) {
                    TruckFilter.ALL -> allTrucks
                    TruckFilter.AVAILABLE -> allTrucks.filter { it.availability.uppercase() == "AVAILABLE" || it.availability.uppercase() == "ACTIVE" }
                    TruckFilter.ON_TRIP -> allTrucks.filter { it.availability.uppercase() == "ON_TRIP" }
                    TruckFilter.BLACKLISTED -> allTrucks.filter { it.isBlacklisted }
                }
            _uiState.value = UiState.Success(filtered)
        }

        fun onRefresh() {
            fetchTrucks(forceRefresh = true)
        }

        fun retry() {
            fetchTrucks()
        }
    }
