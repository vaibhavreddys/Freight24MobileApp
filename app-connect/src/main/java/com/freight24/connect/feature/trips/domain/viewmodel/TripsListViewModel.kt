package com.freight24.connect.feature.trips.domain.viewmodel

import com.freight24.connect.feature.trips.domain.usecase.TripTypes
import com.freight24.rest.response.MyTripsDataResponse
import com.freight24.rest.response.TripDataResponse
import kotlinx.coroutines.flow.StateFlow

data class TripsListUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val trips: List<TripDataResponse> = emptyList(),
    val selectedFilter: TripTypes = TripTypes.ONGOING
)

sealed class TripsListEvent {
    data class SelectFilter(val filter: TripTypes) : TripsListEvent()
    data object Refresh : TripsListEvent()
}

interface TripsListViewModel {
    val uiState: StateFlow<TripsListUiState>
    fun onEvent(event: TripsListEvent)
}
