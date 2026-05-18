package com.freight24.connect.feature.adhocTruck.domain.viewmodel

import com.freight24.rest.response.AdhocTruckOwnerDto
import com.freight24.rest.response.LocationDto
import com.freight24.rest.response.TruckCategoryDto
import com.freight24.rest.response.TruckTypeDto
import kotlinx.coroutines.flow.StateFlow

data class QuickAdhocTruckUiState(
    val phoneNumber: String = "",
    val matchedOwners: List<AdhocTruckOwnerDto> = emptyList(),
    val selectedOwner: AdhocTruckOwnerDto? = null,
    val ownerName: String = "",
    val truckTypes: TruckCategoryDto? = null,
    val selectedTruckType: TruckTypeDto? = null,
    val isLoadingTruckTypes: Boolean = false,
    val isSearchingPhone: Boolean = false,
    val locationQuery: String = "",
    val locationResults: List<LocationDto> = emptyList(),
    val selectedLocation: LocationDto? = null,
    val isSearchingLocation: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val error: String? = null,
)

sealed class QuickAdhocTruckEvent {
    data class PhoneChanged(val phone: String) : QuickAdhocTruckEvent()
    data class SelectOwner(val owner: AdhocTruckOwnerDto) : QuickAdhocTruckEvent()
    data class OwnerNameChanged(val name: String) : QuickAdhocTruckEvent()
    data class SelectTruckType(val truckType: TruckTypeDto) : QuickAdhocTruckEvent()
    data class LocationQueryChanged(val query: String) : QuickAdhocTruckEvent()
    data class SelectLocation(val location: LocationDto) : QuickAdhocTruckEvent()
    data object Submit : QuickAdhocTruckEvent()
    data object ClearError : QuickAdhocTruckEvent()
}

interface QuickAdhocTruckViewModel {
    val uiState: StateFlow<QuickAdhocTruckUiState>
    fun onEvent(event: QuickAdhocTruckEvent)
}
