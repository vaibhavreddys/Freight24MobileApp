package com.freight24.connect.feature.trips.domain.viewmodel

import com.freight24.connect.feature.trips.domain.usecase.TripTypes
import com.freight24.rest.response.TripDataResponse
import kotlinx.coroutines.flow.StateFlow

data class TripsListUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val trips: List<TripDataResponse> = emptyList(),
    val selectedFilter: TripTypes = TripTypes.ALL,
)

sealed class TripsListEvent {
    data class SelectFilter(val filter: TripTypes) : TripsListEvent()

    data object Refresh : TripsListEvent()

    /**
     * Reloads when the screen comes back to the foreground, keeping the trips
     * already on screen and ignoring failures. Separate from [Refresh] because
     * the pull-to-refresh gesture drives the loading indicator off the same flag,
     * and an app switch must not put that indicator on screen.
     */
    data object SilentRefresh : TripsListEvent()
}

interface TripsListViewModel {
    val uiState: StateFlow<TripsListUiState>

    fun onEvent(event: TripsListEvent)
}
