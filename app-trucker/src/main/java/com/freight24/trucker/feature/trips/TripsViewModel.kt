package com.freight24.trucker.feature.trips

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.core.common.model.Trip
import com.freight24.core.common.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TripFilter {
    ACTIVE, POD, COMPLETED, ALL
}

@HiltViewModel
class TripsViewModel @Inject constructor(
    private val repository: TripRepository
) : ViewModel() {

    private val _allTrips = MutableStateFlow<List<Trip>>(emptyList())
    
    private val _filteredTrips = MutableStateFlow<List<Trip>>(emptyList())
    val filteredTrips: StateFlow<List<Trip>> = _filteredTrips.asStateFlow()

    private val _selectedFilter = MutableStateFlow(TripFilter.ACTIVE)
    val selectedFilter: StateFlow<TripFilter> = _selectedFilter.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var currentPage = 1

    init {
        fetchTrips()
    }

    fun fetchTrips(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (forceRefresh) currentPage = 1
            
            try {
                repository.fetchTrips(currentPage, forceRefresh)
                    .catch { e ->
                        Log.e("TripsViewModel", "Error fetching trips: ${e.message}")
                    }
                    .collectLatest { paginatedTrips ->
                        if (currentPage == 1) {
                            _allTrips.value = paginatedTrips.trips
                        } else {
                            _allTrips.value += paginatedTrips.trips
                        }
                        applyFilter()
                    }
            } catch (e: Exception) {
                Log.e("TripsViewModel", "Launch error in fetchTrips", e)
            }
        }
    }

    fun onFilterSelected(filter: TripFilter) {
        _selectedFilter.value = filter
        applyFilter()
    }

    private fun applyFilter() {
        val trips = _allTrips.value
        _filteredTrips.value = when (_selectedFilter.value) {
            TripFilter.ACTIVE -> trips.filter { 
                it.status.uppercase() in listOf("ASSIGNED", "LOADING", "IN_TRANSIT", "IN-TRANSIT")
            }
            TripFilter.POD -> trips.filter { 
                it.status.uppercase() in listOf("UNLOADED", "INVOICED")
            }
            TripFilter.COMPLETED -> trips.filter { 
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
