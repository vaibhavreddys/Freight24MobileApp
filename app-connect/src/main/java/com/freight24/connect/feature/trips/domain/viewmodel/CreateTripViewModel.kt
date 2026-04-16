package com.freight24.connect.feature.trips.domain.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.indents.domain.usecases.GetIndentByIdUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetIndentByIdUseCaseParams
import com.freight24.connect.feature.trips.domain.usecase.CreateTripUseCase
import com.freight24.connect.feature.trips.domain.usecase.CreateTripUseCaseDataParams
import com.freight24.connect.feature.trucks.domain.usecases.GetTruckByIdUseCase
import com.freight24.connect.feature.trucks.domain.usecases.GetTruckByIdUseCaseParams
import com.freight24.rest.response.CreatedTripResponse
import com.freight24.rest.response.IndentResponse
import com.freight24.rest.response.TruckDetailDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateTripUiState(
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val errorMessage: String? = null,
    val indent: IndentResponse.Indent? = null,
    val truck: TruckDetailDto? = null,
    val driverName: String = "",
    val driverPhone: String = "",
    val expectedLoadingDate: String = "", // UI only for now as API doesn't support it
    val createdTrip: CreatedTripResponse.TripData? = null,
    val snackbarMessage: String? = null
)

sealed class CreateTripEvent {
    data class LoadData(val indentId: String, val truckId: String) : CreateTripEvent()
    data class UpdateDriverName(val name: String) : CreateTripEvent()
    data class UpdateDriverPhone(val phone: String) : CreateTripEvent()
    data class UpdateExpectedLoadingDate(val date: String) : CreateTripEvent()
    object CreateTrip : CreateTripEvent()
    object ClearError : CreateTripEvent()
    object ClearSnackbar : CreateTripEvent()
}

@HiltViewModel
internal class CreateTripViewModel @Inject constructor(
    private val getIndentByIdUseCase: GetIndentByIdUseCase,
    private val getTruckByIdUseCase: GetTruckByIdUseCase,
    private val createTripUseCase: CreateTripUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTripUiState())
    val uiState: StateFlow<CreateTripUiState> = _uiState.asStateFlow()

    private var indentId: String? = null
    private var truckId: String? = null

    init {
        // Initialize if arguments are passed via savedStateHandle directly (optional, but good practice)
        // However, we are passing checks in LoadData event from Screen composable usually.
    }

    fun onEvent(event: CreateTripEvent) {
        when (event) {
            is CreateTripEvent.LoadData -> {
                indentId = event.indentId
                truckId = event.truckId
                loadData(event.indentId, event.truckId)
            }
            is CreateTripEvent.UpdateDriverName -> {
                _uiState.update { it.copy(driverName = event.name) }
            }
            is CreateTripEvent.UpdateDriverPhone -> {
                _uiState.update { it.copy(driverPhone = event.phone) }
            }
            is CreateTripEvent.UpdateExpectedLoadingDate -> {
                _uiState.update { it.copy(expectedLoadingDate = event.date) }
            }
            is CreateTripEvent.CreateTrip -> {
                createTrip()
            }
            is CreateTripEvent.ClearError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
            is CreateTripEvent.ClearSnackbar -> {
                _uiState.update { it.copy(snackbarMessage = null) }
            }
        }
    }

    private fun loadData(indentId: String, truckId: String) {
        if (indentId.isEmpty() || indentId == "{indentId}" || truckId.isEmpty() || truckId == "{truckId}") {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Invalid indent or truck selected"
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Fetch Indent
            val indentResult = getIndentByIdUseCase(GetIndentByIdUseCaseParams(indentId))
            val truckResult = getTruckByIdUseCase(GetTruckByIdUseCaseParams(truckId))

            _uiState.update {
                it.copy(
                    isLoading = false,
                    indent = indentResult.getOrNull()?.data,
                    truck = truckResult.getOrNull()?.data,
                    errorMessage = if (indentResult.isFailure || truckResult.isFailure) {
                        "Failed to load details: ${indentResult.exceptionOrNull()?.message ?: ""} ${truckResult.exceptionOrNull()?.message ?: ""}"
                    } else null
                )
            }
        }
    }

    private fun createTrip() {
        val iId = indentId ?: return
        val tId = truckId ?: return
        val state = _uiState.value

        if (state.driverPhone.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "Please update driver phone number") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, errorMessage = null) }

            val result = createTripUseCase(
                CreateTripUseCaseDataParams(
                    indentId = iId,
                    truckId = tId,
                    driverName = state.driverName,
                    driverPhone = state.driverPhone
                )
            )

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        createdTrip = result.getOrNull()
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to create trip"
                    )
                }
            }
        }
    }
}
