package com.freight24.connect.feature.trips.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.trips.domain.usecase.TripDetailsUseCase
import com.freight24.connect.feature.trips.domain.usecase.TripDetailsUseCaseDataParams
import com.freight24.connect.feature.trips.domain.usecase.UpdateTripUseCase
import com.freight24.connect.feature.trips.domain.usecase.UpdateTripUseCaseDataParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class EditTripViewModelImpl
    @Inject
    constructor(
        private val tripDetailsUseCase: TripDetailsUseCase,
        private val updateTripUseCase: UpdateTripUseCase,
    ) : ViewModel(), EditTripViewModel {
        private val _uiState = MutableStateFlow(EditTripUiState())
        override val uiState: StateFlow<EditTripUiState> = _uiState.asStateFlow()

        override fun onEvent(event: EditTripEvent) {
            when (event) {
                is EditTripEvent.LoadTripDetails -> loadTripDetails(event.tripId)
                is EditTripEvent.UpdateDriverName -> _uiState.update { it.copy(driverName = event.name).updateChanges() }
                is EditTripEvent.UpdateDriverPhone -> _uiState.update { it.copy(driverPhone = event.phone).updateChanges() }
                is EditTripEvent.UpdateClientCost -> _uiState.update { it.copy(clientTripCost = event.cost).updateChanges() }
                is EditTripEvent.UpdateTruckerCost -> _uiState.update { it.copy(truckerTripCost = event.cost).updateChanges() }
                is EditTripEvent.ToggleLuIncluded -> _uiState.update { it.copy(luIncluded = !it.luIncluded).updateChanges() }
                is EditTripEvent.OnTruckSelected ->
                    _uiState.update {
                        it.copy(truckId = event.truckId, truckNumber = event.truckNumber).updateChanges()
                    }
                is EditTripEvent.UpdateNotes -> _uiState.update { it.copy(notes = event.notes).updateChanges() }
                is EditTripEvent.SaveChanges -> saveChanges()
                is EditTripEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
            }
        }

        private fun loadTripDetails(tripId: String) {
            if (_uiState.value.internalTripId == tripId && _uiState.value.truckId.isNotEmpty()) return

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                tripDetailsUseCase(TripDetailsUseCaseDataParams(tripId))
                    .onSuccess { response ->
                        _uiState.update {
                            it.copy(
                                internalTripId = response.id,
                                tripCode = response.tripCode,
                                truckNumber = response.truck.registrationNumber,
                                truckId = response.truck.id,
                                driverName = response.driverName ?: "",
                                driverPhone = response.driverPhone ?: "",
                                clientTripCost = response.clientTripCost?.toPlainString().orEmpty(),
                                truckerTripCost = response.truckerTripCost ?: "",
                                luIncluded = response.financials?.luCharges?.loading?.includedInTripCost ?: false,
                                truckerStatus = response.truckerStatus,
                                // Store initial values for comparison
                                initialTruckId = response.truck.id,
                                initialTruckNumber = response.truck.registrationNumber,
                                initialDriverName = response.driverName ?: "",
                                initialDriverPhone = response.driverPhone ?: "",
                                initialClientTripCost = response.clientTripCost?.toPlainString().orEmpty(),
                                initialTruckerTripCost = response.truckerTripCost ?: "",
                                initialLuIncluded = response.financials?.luCharges?.loading?.includedInTripCost ?: false,
                                isLoading = false,
                                hasChanges = false,
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                    }
            }
        }

        private fun saveChanges() {
            val state = _uiState.value
            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true, errorMessage = null) }

                // Only send fields that have changed
                val params =
                    UpdateTripUseCaseDataParams(
                        tripId = state.internalTripId,
                        driverName = if (state.driverName != state.initialDriverName) state.driverName else null,
                        driverPhone = if (state.driverPhone != state.initialDriverPhone) state.driverPhone else null,
                        clientTripCost = if (state.clientTripCost != state.initialClientTripCost) state.clientTripCost else null,
                        truckerTripCost = if (state.truckerTripCost != state.initialTruckerTripCost) state.truckerTripCost else null,
                        luIncluded = if (state.luIncluded != state.initialLuIncluded) state.luIncluded else null,
                        truckId = if (state.truckId != state.initialTruckId) state.truckId else null,
                        reason = state.notes.ifBlank { "Updated via app" },
                    )
                updateTripUseCase(params)
                    .onSuccess {
                        _uiState.update { it.copy(isSaving = false, isSuccess = true) }
                    }
                    .onFailure { error ->
                        _uiState.update { it.copy(isSaving = false, errorMessage = error.message) }
                    }
            }
        }

        private fun EditTripUiState.updateChanges(): EditTripUiState {
            val hasChanges =
                truckId != initialTruckId ||
                    driverName != initialDriverName ||
                    driverPhone != initialDriverPhone ||
                    clientTripCost != initialClientTripCost ||
                    truckerTripCost != initialTruckerTripCost ||
                    luIncluded != initialLuIncluded ||
                    notes.isNotBlank()
            return copy(hasChanges = hasChanges)
        }
    }
