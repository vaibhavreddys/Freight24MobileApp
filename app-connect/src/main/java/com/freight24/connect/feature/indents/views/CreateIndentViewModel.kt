package com.freight24.connect.feature.indents.views

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.indents.domain.CreateIndentUseCase
import com.freight24.connect.feature.indents.domain.GetClientsUseCase
import com.freight24.connect.feature.indents.domain.GetLocationsUseCase
import com.freight24.connect.feature.indents.domain.GetTruckTypesUseCase
import com.freight24.rest.request.CreateIndentRequest
import com.freight24.rest.response.ClientDto
import com.freight24.rest.response.LocationDto
import com.freight24.rest.response.TruckCategoryDto
import com.freight24.rest.response.TruckTypeDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateIndentViewModel
    @Inject
    constructor(
        private val getLocationsUseCase: GetLocationsUseCase,
        private val getClientsUseCase: GetClientsUseCase,
        private val getTruckTypesUseCase: GetTruckTypesUseCase,
        private val createIndentUseCase: CreateIndentUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(CreateIndentUiState())
        val uiState: StateFlow<CreateIndentUiState> = _uiState.asStateFlow()

        private var searchJob: Job? = null

        init {
            loadTruckTypes()
        }

        private fun loadTruckTypes() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoadingTruckTypes = true) }
                val result = getTruckTypesUseCase.invoke()
                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            truckTypes =
                                result.getOrElse {
                                    TruckCategoryDto(
                                        emptyList(),
                                        emptyList(),
                                        emptyList(),
                                    )
                                },
                            isLoadingTruckTypes = false,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoadingTruckTypes = false,
                            errorMessage = result.exceptionOrNull()?.message,
                        )
                    }
                }
            }
        }

        fun onEvent(event: CreateIndentEvent) {
            when (event) {
                is CreateIndentEvent.SearchFromLocation -> searchLocations(event.query, true)
                is CreateIndentEvent.SearchToLocation -> searchLocations(event.query, false)
                is CreateIndentEvent.SearchClient -> searchClients(event.query)
                is CreateIndentEvent.SelectFromLocation ->
                    _uiState.update {
                        it.copy(
                            selectedFromLocation = event.location,
                            fromLocationQuery = event.location.name,
                            fromLocationSearchResults = emptyList(),
                        )
                    }

                is CreateIndentEvent.SelectToLocation ->
                    _uiState.update {
                        it.copy(
                            selectedToLocation = event.location,
                            toLocationQuery = event.location.name,
                            toLocationSearchResults = emptyList(),
                        )
                    }

                is CreateIndentEvent.SelectClient ->
                    _uiState.update {
                        it.copy(
                            selectedClient = event.client,
                            clientQuery = event.client.companyName,
                            clientSearchResults = emptyList(),
                        )
                    }

                is CreateIndentEvent.UpdateMaterialName -> {
                    _uiState.update { it.copy(materialName = event.name) }
                }
                is CreateIndentEvent.UpdateWeight -> {
                    _uiState.update { it.copy(weight = event.weight) }
                }
                is CreateIndentEvent.SelectTruckType -> {
                    _uiState.update { it.copy(selectedTruckType = event.truckType) }
                }
                is CreateIndentEvent.UpdateOptionalField -> updateOptionalField(event)
                is CreateIndentEvent.SubmitIndent -> submitIndent()
                is CreateIndentEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
                is CreateIndentEvent.ClearSuccess -> _uiState.update { it.copy(successMessage = null) }
            }
        }

        private fun searchLocations(
            query: String,
            isFrom: Boolean,
        ) {
            if (isFrom) {
                _uiState.update { it.copy(fromLocationQuery = query) }
            } else {
                _uiState.update {
                    it.copy(
                        toLocationQuery = query,
                    )
                }
            }

            searchJob?.cancel()
            searchJob =
                viewModelScope.launch {
                    delay(300) // Debounce
                    if (query.length < 3) return@launch

                    val result = getLocationsUseCase.invoke(query)
                    if (result.isSuccess) {
                        val locations = result.getOrNull() ?: emptyList()
                        if (isFrom) {
                            _uiState.update { it.copy(fromLocationSearchResults = locations) }
                        } else {
                            _uiState.update { it.copy(toLocationSearchResults = locations) }
                        }
                    }
                }
        }

        private fun searchClients(query: String) {
            _uiState.update { it.copy(clientQuery = query) }

            searchJob?.cancel()
            searchJob =
                viewModelScope.launch {
                    delay(300)
                    if (query.length < 3) return@launch

                    val result = getClientsUseCase.invoke(query)
                    if (result.isSuccess) {
                        _uiState.update { it.copy(clientSearchResults = result.getOrNull() ?: emptyList()) }
                    }
                }
        }

        private fun updateOptionalField(event: CreateIndentEvent.UpdateOptionalField) {
            _uiState.update { state ->
                state.copy(
                    clientCost = event.clientCost ?: state.clientCost,
                    negotiatedCost = event.negotiatedCost ?: state.negotiatedCost,
                    businessProposedCost = event.businessProposedCost ?: state.businessProposedCost,
                    vehicleHeight = event.vehicleHeight ?: state.vehicleHeight,
                    flooringType = event.flooringType ?: state.flooringType,
                    tatRequired = event.tatRequired ?: state.tatRequired,
                    tarpaulinRequired = event.tarpaulinRequired ?: state.tarpaulinRequired,
                    hideCost = event.hideCost ?: state.hideCost,
                    remarks = event.remarks ?: state.remarks,
                    tatHours = event.tatHours ?: state.tatHours,
                )
            }
        }

        private fun submitIndent() {
            val state = _uiState.value
            if (validate(state)) {
                viewModelScope.launch {
                    _uiState.update { it.copy(isSubmitting = true) }
                    val request =
                        CreateIndentRequest(
                            fromLocationId = state.selectedFromLocation!!.id,
                            toLocationId = state.selectedToLocation!!.id,
                            clientId = state.selectedClient!!.clientId,
                            materialName = state.materialName,
                            weight = state.weight.toDoubleOrNull() ?: 0.0,
                            truckTypeId = state.selectedTruckType!!.id,
                            clientCost = state.clientCost.toDoubleOrNull(),
                            negotiatedCost = state.negotiatedCost.toDoubleOrNull(),
                            businessProposedCost = state.businessProposedCost.toDoubleOrNull(),
                            vehicleHeight = state.vehicleHeight.toDoubleOrNull(),
                            flooringType = state.flooringType,
                            tatRequired = state.tatRequired,
                            tarpaulinRequired = state.tarpaulinRequired,
                            hideCost = state.hideCost,
                            remarks = state.remarks,
                        )
                    val result = createIndentUseCase.invoke(request)
                    if (result.isSuccess) {
                        _uiState.update { it.copy(isSubmitting = false, successMessage = "Indent Created Successfully") }
                    } else {
                        _uiState.update { it.copy(isSubmitting = false, errorMessage = result.exceptionOrNull()?.message) }
                    }
                }
            }
        }

        private fun validate(state: CreateIndentUiState): Boolean {
            if (state.selectedFromLocation == null) {
                _uiState.update { it.copy(errorMessage = "Please select From Location") }
                return false
            }
            if (state.selectedToLocation == null) {
                _uiState.update { it.copy(errorMessage = "Please select To Location") }
                return false
            }
            if (state.selectedClient == null) {
                _uiState.update { it.copy(errorMessage = "Please select Client") }
                return false
            }
            if (state.materialName.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Please enter Material Name") }
                return false
            }
            if (state.weight.isBlank() || (state.weight.toDoubleOrNull() ?: 0.0) <= 0) {
                _uiState.update { it.copy(errorMessage = "Please enter valid Weight") }
                return false
            }
            if (state.selectedTruckType == null) {
                _uiState.update { it.copy(errorMessage = "Please select Truck Type") }
                return false
            }
            return true
        }
    }

@Immutable
data class CreateIndentUiState(
    val fromLocationQuery: String = "",
    val fromLocationSearchResults: List<LocationDto> = emptyList(),
    val selectedFromLocation: LocationDto? = null,
    val toLocationQuery: String = "",
    val toLocationSearchResults: List<LocationDto> = emptyList(),
    val selectedToLocation: LocationDto? = null,
    val clientQuery: String = "",
    val clientSearchResults: List<ClientDto> = emptyList(),
    val selectedClient: ClientDto? = null,
    val materialName: String = "",
    val weight: String = "",
    val truckTypes: TruckCategoryDto =
        TruckCategoryDto(
            emptyList(),
            emptyList(),
            emptyList(),
        ),
    val isLoadingTruckTypes: Boolean = false,
    val selectedTruckType: TruckTypeDto? = null,
    // Optional
    val clientCost: String = "",
    val negotiatedCost: String = "",
    val businessProposedCost: String = "",
    val vehicleHeight: String = "",
    val flooringType: String = "Wooden", // Default
    val tatRequired: Boolean = false,
    val tatHours: String = "",
    val tarpaulinRequired: Boolean = false,
    val hideCost: Boolean = false,
    val remarks: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

sealed class CreateIndentEvent {
    data class SearchFromLocation(val query: String) : CreateIndentEvent()

    data class SearchToLocation(val query: String) : CreateIndentEvent()

    data class SearchClient(val query: String) : CreateIndentEvent()

    data class SelectFromLocation(val location: LocationDto) : CreateIndentEvent()

    data class SelectToLocation(val location: LocationDto) : CreateIndentEvent()

    data class SelectClient(val client: ClientDto) : CreateIndentEvent()

    data class UpdateMaterialName(val name: String) : CreateIndentEvent()

    data class UpdateWeight(val weight: String) : CreateIndentEvent()

    data class SelectTruckType(val truckType: TruckTypeDto) : CreateIndentEvent()

    data class UpdateOptionalField(
        val clientCost: String? = null,
        val negotiatedCost: String? = null,
        val businessProposedCost: String? = null,
        val vehicleHeight: String? = null,
        val flooringType: String? = null,
        val tatRequired: Boolean? = null,
        val tarpaulinRequired: Boolean? = null,
        val hideCost: Boolean? = null,
        val remarks: String? = null,
        val tatHours: String? = null,
    ) : CreateIndentEvent()

    object SubmitIndent : CreateIndentEvent()

    object ClearError : CreateIndentEvent()

    object ClearSuccess : CreateIndentEvent()
}
