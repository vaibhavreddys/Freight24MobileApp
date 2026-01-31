package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.indents.domain.usecases.CreateIndentUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetClientsUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetLocationsUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetTruckTypesUseCase
import com.freight24.rest.request.CreateIndentRequest
import com.freight24.rest.response.LocationDto
import com.freight24.rest.response.TruckCategoryDto
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
class IndentsViewModelImpl
    @Inject
    constructor(
        private val getLocationsUseCase: GetLocationsUseCase,
        private val getClientsUseCase: GetClientsUseCase,
        private val getTruckTypesUseCase: GetTruckTypesUseCase,
        private val createIndentUseCase: CreateIndentUseCase,
    ) : ViewModel(), IndentsViewModel {

    private val _uiState = MutableStateFlow(CreateIndentUiState())
    override val uiState: StateFlow<CreateIndentUiState>
        get() = _uiState.asStateFlow()

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

    override fun onEvent(event: CreateIndentEvent) {
        when (event) {
            is CreateIndentEvent.SearchFromLocation -> {
                searchLocations(event.query, true)
            }
            is CreateIndentEvent.SearchToLocation -> {
                searchLocations(event.query, false)
            }
            is CreateIndentEvent.SearchClient -> {
                searchClients(event.query)
            }
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
            is CreateIndentEvent.UpdateOptionalField -> {
                updateOptionalField(event)
            }
            is CreateIndentEvent.SubmitIndent -> submitIndent()
            is CreateIndentEvent.ClearError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
            is CreateIndentEvent.ClearSuccess -> {
                _uiState.update { it.copy(successMessage = null) }
            }
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

    private fun updateOptionalField(
        event: CreateIndentEvent.UpdateOptionalField
    ) {
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

                val (toAreaId, toCityId) = getAreaCityLocationId(state.selectedFromLocation)

                val loadingLocation = CreateIndentRequest.Location(
                    locationType = "loading",
                    areaId = toAreaId,
                    cityId = toCityId,
                    sequenceOrder = 1
                )

                val (fromAreaId, fromCityId) = getAreaCityLocationId(state.selectedToLocation)

                val unLoadingLocation = CreateIndentRequest.Location(
                    locationType = "unloading",
                    areaId = fromAreaId,
                    cityId = fromCityId,
                    sequenceOrder = 1
                )

                val request = CreateIndentRequest(
                    clientId = state.selectedClient!!.clientId,
                    truckTypeId = state.selectedTruckType!!.id.toInt(),
                    loadWeight = state.weight.toInt(),
                    material = state.materialName,
                    clientTripCost = state.clientCost.toIntOrNull(),
                    negotiatedTripCost = state.negotiatedCost.toIntOrNull(),
                    businessProposedCost = state.businessProposedCost.toIntOrNull(),
                    vehicleHeight = state.vehicleHeight.toFloatOrNull(),
                    hideCostInfo = state.hideCost,
                    tatEnabled = state.tatRequired,
                    tatHours = state.tatHours.toIntOrNull(),
                    tarpaulingRequired = state.tarpaulinRequired,
                    remarks = state.remarks,
                    flooringType = state.flooringType,
                    locations = listOf(loadingLocation, unLoadingLocation)
                )
                val result = createIndentUseCase.invoke(request)
                if (result.isSuccess) {
                    val indentResponse = result.getOrNull()
                    _uiState.update { 
                        it.copy(
                            isSubmitting = false, 
                            successMessage = "Indent Created Successfully",
                            createdIndentId = indentResponse?.data?.id.orEmpty()
                        ) 
                    }
                } else {
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = result.exceptionOrNull()?.message) }
                }
            }
        }
    }

    private fun getAreaCityLocationId(location: LocationDto?) : Pair<Int?, Int?> {
        var areaId: Int?= null
        var cityId: Int?= null

        if(location != null){
            if(location.type.equals("area", true)) {
                areaId = location.id.toInt()
            } else {
                cityId = location.id.toInt()
            }
        }
        return Pair(areaId, cityId)
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
