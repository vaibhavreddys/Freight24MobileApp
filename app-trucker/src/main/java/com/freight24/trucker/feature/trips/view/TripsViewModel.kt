package com.freight24.trucker.feature.trips.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.model.Trip
import com.freight24.trucker.feature.trips.domain.usecase.FetchTripsUseCase
import com.freight24.trucker.feature.trips.domain.usecase.FetchTripsUseCaseParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
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

        private val _selectedFilter = MutableStateFlow(TripFilter.ACTIVE)
        val selectedFilter: StateFlow<TripFilter> = _selectedFilter.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        /** True until we have trips to show — cached or fetched. Drives the initial spinner. */
        private val _isInitialLoading = MutableStateFlow(true)
        val isInitialLoading: StateFlow<Boolean> = _isInitialLoading.asStateFlow()

        private val _filterCounts = MutableStateFlow<Map<TripFilter, Int>>(emptyMap())
        val filterCounts: StateFlow<Map<TripFilter, Int>> = _filterCounts.asStateFlow()

        private val _filteredTrips = MutableStateFlow<List<Trip>>(emptyList())
        val filteredTrips: StateFlow<List<Trip>> = _filteredTrips.asStateFlow()

        /**
         * The cache is the single source of truth for the list: it is observed on
         * start-up (so a warm manifest paints immediately) and re-emits whenever a
         * fetch writes fresher rows. The network fetch therefore never touches
         * state directly — it only refreshes the cache.
         */
        init {
            observeCachedTrips()
            fetchTrips()
        }

        private fun observeCachedTrips() {
            viewModelScope.launch {
                useCase.observeTrips().collect { trips ->
                    _allTrips.value = trips
                    applyFilter()
                    calculateCounts()
                    if (trips.isNotEmpty()) {
                        _isInitialLoading.value = false
                    }
                }
            }
        }

        fun fetchTrips() {
            viewModelScope.launch {
                loadTrips()
            }
        }

        fun onRefresh() {
            viewModelScope.launch {
                _isRefreshing.value = true
                try {
                    loadTrips()
                } finally {
                    _isRefreshing.value = false
                }
            }
        }

        /**
         * A fetch only refreshes the cache; [observeCachedTrips] repaints from it.
         */
        private suspend fun loadTrips() {
            useCase(FetchTripsUseCaseParams(PAGE)).onFailure { it.printStackTrace() }
            _isInitialLoading.value = false
        }

        fun onFilterSelected(filter: TripFilter) {
            _selectedFilter.value = filter
            applyFilter()
        }

        private fun calculateCounts() {
            val trips = _allTrips.value
            _filterCounts.value =
                TripFilter.entries.associateWith { filter ->
                    trips.count { filter == TripFilter.ALL || filterFor(it.status) == filter }
                }
        }

        private fun applyFilter() {
            val filter = _selectedFilter.value
            _filteredTrips.value =
                if (filter == TripFilter.ALL) {
                    _allTrips.value
                } else {
                    _allTrips.value.filter { filterFor(it.status) == filter }
                }
        }

        /** `null` for any status the rail has no tab for — it shows under ALL only. */
        private fun filterFor(status: String): TripFilter? =
            when (status.uppercase()) {
                "ASSIGNED", "LOADING", "IN_TRANSIT", "IN TRANSIT" -> TripFilter.ACTIVE
                "UNLOADED", "INVOICED" -> TripFilter.POD
                "PAID" -> TripFilter.COMPLETED
                else -> null
            }

        private companion object {
            /**
             * The manifest endpoint pages, but a driver's board is a single
             * screen — the rail filters that one page client-side.
             */
            const val PAGE = 1
        }
    }
