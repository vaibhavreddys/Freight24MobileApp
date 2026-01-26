package com.freight24.trucker.feature.trips.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.model.PaginatedTrips
import com.freight24.common.model.Trip
import com.freight24.trucker.feature.trips.domain.usecase.FetchTripsUseCase
import com.freight24.trucker.feature.trips.domain.usecase.FetchTripsUseCaseParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TripFilter {
    ACTIVE,
    POD,
    COMPLETED,
    ALL,
}

@HiltViewModel
internal class TripsViewModel
    @Inject
    constructor(
        private val useCase: FetchTripsUseCase,
    ) : ViewModel() {
        private val _allTrips = MutableStateFlow<List<Trip>>(emptyList())

        private val _filteredTrips = MutableStateFlow<List<Trip>>(emptyList())
        val filteredTrips: StateFlow<List<Trip>> = _filteredTrips.asStateFlow()

        private val _selectedFilter = MutableStateFlow(TripFilter.ACTIVE)
        val selectedFilter: StateFlow<TripFilter> = _selectedFilter.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        private val _filterCounts = MutableStateFlow<Map<TripFilter, Int>>(emptyMap())
        val filterCounts: StateFlow<Map<TripFilter, Int>> = _filterCounts.asStateFlow()

        private var currentPage = 1

        init {
            fetchTrips()
        }

        fun fetchTrips(forceRefresh: Boolean = false) {
            viewModelScope.launch {
                if (forceRefresh) currentPage = 1

                useCase(FetchTripsUseCaseParams(currentPage))
                    .onSuccess { paginatedTrips: PaginatedTrips ->
                        if (currentPage == 1) {
                            _allTrips.value = paginatedTrips.trips
                        } else {
                            _allTrips.value += paginatedTrips.trips
                        }
                        applyFilter()
                        calculateCounts()
                    }.onFailure { error: Throwable ->
                        // Handle error
                    }
            }
        }
        
        private fun calculateCounts() {
            val trips = _allTrips.value
            val counts = TripFilter.entries.associateWith { filter ->
                when (filter) {
                    TripFilter.ACTIVE ->
                        trips.count {
                            it.status.uppercase() in listOf("ASSIGNED", "LOADING", "IN_TRANSIT", "IN TRANSIT")
                        }
                    TripFilter.POD ->
                        trips.count {
                            it.status.uppercase() in listOf("UNLOADED", "INVOICED")
                        }
                    TripFilter.COMPLETED ->
                        trips.count {
                            it.status.uppercase() == "PAID"
                        }
                    TripFilter.ALL -> trips.size
                }
            }
            _filterCounts.value = counts
        }

        fun onFilterSelected(filter: TripFilter) {
            _selectedFilter.value = filter
            applyFilter()
        }

        private fun applyFilter() {
            val trips = _allTrips.value
            _filteredTrips.value =
                when (_selectedFilter.value) {
                    TripFilter.ACTIVE ->
                        trips.filter {
                            it.status.uppercase() in listOf("ASSIGNED", "LOADING", "IN_TRANSIT", "IN TRANSIT")
                        }
                    TripFilter.POD ->
                        trips.filter {
                            it.status.uppercase() in listOf("UNLOADED", "INVOICED")
                        }
                    TripFilter.COMPLETED ->
                        trips.filter {
                            it.status.uppercase() == "PAID"
                        }
                    TripFilter.ALL -> trips
                }
        }

        fun onRefresh() {
            viewModelScope.launch {
                _isRefreshing.value = true
                try {
                    fetchTrips(forceRefresh = true)
                } finally {
                    _isRefreshing.value = false
                }
            }
        }

        fun loadMore() {
            // Implementation for pagination if needed
        }
    }
