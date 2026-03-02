package com.freight24.connect.feature.trips.domain.viewmodel

import kotlinx.coroutines.flow.StateFlow

data class HaltingChargesUiState(
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val errorMessage: String? = null,
    val loadingDays: Int = 0,
    val unloadingDays: Int = 0,
    val dailyRate: Double = 0.0,
    val notes: String = "",
    val updateSuccess: Boolean = false,
    
    // Initial values for comparison
    val initialLoadingDays: Int = 0,
    val initialUnloadingDays: Int = 0,
    val initialNotes: String = "",
    val hasChanges: Boolean = false
)

sealed class HaltingChargesEvent {
    data class LoadHaltingRate(
        val truckTypeId: String,
        val initialLoadingDays: Int = 0,
        val initialUnloadingDays: Int = 0,
        val notes: String = ""
    ) : HaltingChargesEvent()
    data class UpdateLoadingDays(val days: Int) : HaltingChargesEvent()
    data class UpdateUnloadingDays(val days: Int) : HaltingChargesEvent()
    data class UpdateNotes(val notes: String) : HaltingChargesEvent()
    data class SubmitHaltingCharges(val tripId: String) : HaltingChargesEvent()
}

interface HaltingChargesViewModel {
    val uiState: StateFlow<HaltingChargesUiState>
    fun onEvent(event: HaltingChargesEvent)
}
