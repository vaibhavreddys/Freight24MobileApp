package com.freight24.connect.feature.findTrucks.domain.viewmodel

import com.freight24.rest.response.LocationDto
import com.freight24.rest.response.SearchTruckerResponse.TruckerData
import com.freight24.rest.response.TruckCategoryDto
import com.freight24.rest.response.TruckTypeDto
import kotlinx.coroutines.flow.StateFlow

data class FindTrucksUiState(
    val loadingLocation: LocationDto? = null,
    val unloadingLocation: LocationDto? = null,
    val truckTypes: TruckCategoryDto? = null,
    val selectedTruckTypes: Set<TruckTypeDto> = emptySet(),
    val isLoadingTruckTypes: Boolean = false,
    val isSearching: Boolean = false,
    val searchResults: TruckerData? = null,
    val error: String? = null,
    val showResults: Boolean = false,
)

sealed class FindTrucksEvent {
    data class SelectLoadingLocation(val location: LocationDto) : FindTrucksEvent()

    data class SelectUnloadingLocation(val location: LocationDto) : FindTrucksEvent()

    data object SwapLocations : FindTrucksEvent()

    data class ToggleTruckType(val truckType: TruckTypeDto) : FindTrucksEvent()

    data object ClearTruckTypes : FindTrucksEvent()

    data object SearchTrucks : FindTrucksEvent()

    data object DismissResults : FindTrucksEvent()

    data object ClearError : FindTrucksEvent()
}

interface FindTrucksViewModel {
    val uiState: StateFlow<FindTrucksUiState>

    fun onEvent(event: FindTrucksEvent)
}
