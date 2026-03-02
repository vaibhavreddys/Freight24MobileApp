package com.freight24.connect.feature.trips.domain.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.trips.domain.usecase.UpdateOtherDeductionsUseCase
import com.freight24.connect.feature.trips.domain.usecase.UpdateOtherDeductionsUseCaseDataParams
import com.freight24.connect.navigation.ConnectDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class OtherDeductionsViewModelImpl @Inject constructor(
    private val useCase: UpdateOtherDeductionsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel(), OtherDeductionsViewModel {

    private val tripId: String = checkNotNull(savedStateHandle[ConnectDestination.OtherDeductions.TRIP_ID_ARG])
    private val initialAmount: String? = savedStateHandle[ConnectDestination.OtherDeductions.AMOUNT_ARG]
    private val initialNotes: String? = savedStateHandle[ConnectDestination.OtherDeductions.NOTES_ARG]

    private val _uiState = MutableStateFlow(
        OtherDeductionsUiState(
            tripId = tripId,
            amount = initialAmount?.takeIf { it != "null" && it.isNotBlank() } ?: "0",
            notes = initialNotes?.takeIf { it != "null" && it.isNotBlank() } ?: "",
            initialAmount = initialAmount?.takeIf { it != "null" && it.isNotBlank() } ?: "0",
            initialNotes = initialNotes?.takeIf { it != "null" && it.isNotBlank() } ?: "",
            hasChanges = false
        )
    )
    override val uiState: StateFlow<OtherDeductionsUiState> = _uiState.asStateFlow()

    override fun onEvent(event: OtherDeductionsEvent) {
        when (event) {
            is OtherDeductionsEvent.OnAmountChange -> {
                _uiState.update { it.copy(amount = event.amount).updateChanges() }
            }
            is OtherDeductionsEvent.OnReasonChange -> {
                _uiState.update { it.copy(notes = event.reason).updateChanges() }
            }
            OtherDeductionsEvent.OnUpdateClick -> {
                updateDeductions()
            }
        }
    }

    private fun updateDeductions() {
        val currentState = _uiState.value
        if (currentState.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val params = UpdateOtherDeductionsUseCaseDataParams(
                tripId = currentState.tripId,
                amount = currentState.amount,
                remarks = currentState.notes
            )
            useCase(params).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message ?: "Failed to update deductions") }
                }
            )
        }
    }

    private fun OtherDeductionsUiState.updateChanges(): OtherDeductionsUiState {
        val hasChanges = amount != initialAmount || notes != initialNotes
        return copy(hasChanges = hasChanges)
    }
}
