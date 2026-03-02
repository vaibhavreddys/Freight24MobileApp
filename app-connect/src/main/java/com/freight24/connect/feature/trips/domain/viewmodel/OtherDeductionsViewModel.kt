package com.freight24.connect.feature.trips.domain.viewmodel

import kotlinx.coroutines.flow.StateFlow

data class OtherDeductionsUiState(
    val tripId: String = "",
    val amount: String = "0.00",
    val notes: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    
    // Initial values for comparison
    val initialAmount: String = "0.00",
    val initialNotes: String = "",
    val hasChanges: Boolean = false
)

sealed class OtherDeductionsEvent {
    data class OnAmountChange(val amount: String) : OtherDeductionsEvent()
    data class OnReasonChange(val reason: String) : OtherDeductionsEvent()
    data object OnUpdateClick : OtherDeductionsEvent()
}

interface OtherDeductionsViewModel {
    val uiState: StateFlow<OtherDeductionsUiState>
    fun onEvent(event: OtherDeductionsEvent)
}
