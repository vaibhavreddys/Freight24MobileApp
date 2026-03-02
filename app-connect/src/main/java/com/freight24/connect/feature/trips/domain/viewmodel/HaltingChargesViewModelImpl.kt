package com.freight24.connect.feature.trips.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.trips.domain.usecase.GetHaltingRateUseCase
import com.freight24.connect.feature.trips.domain.usecase.GetHaltingRateUseCaseDataParams
import com.freight24.connect.feature.trips.domain.usecase.UpdateHaltingRateUseCase
import com.freight24.connect.feature.trips.domain.usecase.UpdateHaltingRateUseCaseDataParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class HaltingChargesViewModelImpl @Inject constructor(
    private val getHaltingRateUseCase: GetHaltingRateUseCase,
    private val updateHaltingRateUseCase: UpdateHaltingRateUseCase
) : ViewModel(), HaltingChargesViewModel {

    private val _uiState = MutableStateFlow(HaltingChargesUiState())
    override val uiState = _uiState.asStateFlow()

    override fun onEvent(event: HaltingChargesEvent) {
        when (event) {
            is HaltingChargesEvent.LoadHaltingRate -> loadHaltingRate(event.truckTypeId, event.initialLoadingDays, event.initialUnloadingDays, event.notes)
            is HaltingChargesEvent.UpdateLoadingDays -> {
                _uiState.update { it.copy(loadingDays = event.days.coerceAtLeast(0)).updateChanges() }
            }
            is HaltingChargesEvent.UpdateUnloadingDays -> {
                _uiState.update { it.copy(unloadingDays = event.days.coerceAtLeast(0)).updateChanges() }
            }
            is HaltingChargesEvent.UpdateNotes -> {
                _uiState.update { it.copy(notes = event.notes).updateChanges() }
            }
            is HaltingChargesEvent.SubmitHaltingCharges -> submitHaltingCharges(event.tripId)
        }
    }

    private fun loadHaltingRate(truckTypeId: String, loadingDays: Int, unloadingDays: Int, notes: String) {
        _uiState.update { 
            it.copy(
                isLoading = true, 
                errorMessage = null, 
                loadingDays = loadingDays, 
                unloadingDays = unloadingDays, 
                notes = notes,
                initialLoadingDays = loadingDays,
                initialUnloadingDays = unloadingDays,
                initialNotes = notes,
                hasChanges = false
            ) 
        }
        viewModelScope.launch {
            getHaltingRateUseCase(GetHaltingRateUseCaseDataParams(truckTypeId)).fold(
                onSuccess = { rate ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            dailyRate = rate.haltingRatePerDay.toDoubleOrNull() ?: 0.0
                        ) 
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Failed to load halting rate") }
                }
            )
        }
    }

    private fun submitHaltingCharges(tripId: String) {
        val state = _uiState.value
        _uiState.update { it.copy(isUpdating = true, errorMessage = null) }
        viewModelScope.launch {
            val params = UpdateHaltingRateUseCaseDataParams(
                tripId = tripId,
                loadingDays = state.loadingDays,
                unloadingDays = state.unloadingDays,
                notes = state.notes
            )
            updateHaltingRateUseCase(params).fold(
                onSuccess = {
                    _uiState.update { it.copy(isUpdating = false, updateSuccess = true) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isUpdating = false, errorMessage = error.message ?: "Update failed") }
                }
            )
        }
    }

    private fun HaltingChargesUiState.updateChanges(): HaltingChargesUiState {
        val hasChanges = loadingDays != initialLoadingDays ||
                unloadingDays != initialUnloadingDays ||
                notes != initialNotes
        return copy(hasChanges = hasChanges)
    }
}
