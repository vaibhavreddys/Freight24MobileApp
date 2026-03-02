package com.freight24.connect.feature.trips.domain.viewmodel

import com.freight24.common.usecase.UseCaseParams
import kotlinx.coroutines.flow.StateFlow

data class EditTripUiState(
    val internalTripId: String = "",
    val tripCode: String = "",
    val truckNumber: String = "",
    val truckId: String = "",
    val driverName: String = "",
    val driverPhone: String = "",
    val clientTripCost: String = "",
    val truckerTripCost: String = "",
    val luIncluded: Boolean = false,
    val truckerStatus: String = "",
    val notes: String = "",
    
    // Initial values for comparison
    val initialTruckId: String = "",
    val initialTruckNumber: String = "",
    val initialDriverName: String = "",
    val initialDriverPhone: String = "",
    val initialClientTripCost: String = "",
    val initialTruckerTripCost: String = "",
    val initialLuIncluded: Boolean = false,

    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val hasChanges: Boolean = false
)

sealed class EditTripEvent {
    data class LoadTripDetails(val tripId: String) : EditTripEvent()
    data class UpdateDriverName(val name: String) : EditTripEvent()
    data class UpdateDriverPhone(val phone: String) : EditTripEvent()
    data class UpdateClientCost(val cost: String) : EditTripEvent()
    data class UpdateTruckerCost(val cost: String) : EditTripEvent()
    data object ToggleLuIncluded : EditTripEvent()
    data class OnTruckSelected(val truckId: String, val truckNumber: String) : EditTripEvent()
    data class UpdateNotes(val notes: String) : EditTripEvent()
    data object SaveChanges : EditTripEvent()
    data object ClearError : EditTripEvent()
}

interface EditTripViewModel {
    val uiState: StateFlow<EditTripUiState>
    fun onEvent(event: EditTripEvent)
}
