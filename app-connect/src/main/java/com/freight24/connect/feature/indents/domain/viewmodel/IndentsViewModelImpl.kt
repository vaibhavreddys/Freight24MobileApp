package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.indents.domain.usecases.CreateIndentUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetClientBranchesUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetClientContactsUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetClientsUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetLocationsUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetTruckTypesUseCase
import com.freight24.rest.request.CreateIndentRequest
import com.freight24.rest.request.IndentContactsRequest
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
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class IndentsViewModelImpl
    @Inject
    constructor(
        private val getLocationsUseCase: GetLocationsUseCase,
        private val getClientsUseCase: GetClientsUseCase,
        private val getTruckTypesUseCase: GetTruckTypesUseCase,
        private val createIndentUseCase: CreateIndentUseCase,
        private val getClientBranchesUseCase: GetClientBranchesUseCase,
        private val getClientContactsUseCase: GetClientContactsUseCase,
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

            is CreateIndentEvent.SelectClient -> {
                _uiState.update {
                    it.copy(
                        selectedClient = event.client,
                        clientQuery = event.client.companyName,
                        clientSearchResults = emptyList(),
                        // Branch and contacts belong to the previous client. The
                        // backend validates both against the selected client, so
                        // carrying them over would guarantee a 400.
                        selectedBranch = null,
                        branches = emptyList(),
                        contacts = emptyList(),
                        placementContact = null,
                        paymentContact = null,
                        operationsContact = null,
                        podContact = null,
                        contactsUnavailable = false,
                    )
                }
                loadClientDirectory(event.client.clientId)
            }

            is CreateIndentEvent.SelectBranch ->
                _uiState.update { it.copy(selectedBranch = event.branch) }

            is CreateIndentEvent.SelectContact ->
                _uiState.update { state ->
                    when (event.role) {
                        IndentContactRole.PLACEMENT -> state.copy(placementContact = event.contact)
                        IndentContactRole.PAYMENT -> state.copy(paymentContact = event.contact)
                        IndentContactRole.OPERATIONS -> state.copy(operationsContact = event.contact)
                        IndentContactRole.POD -> state.copy(podContact = event.contact)
                    }
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
                _uiState.update { it.copy(successMessage = null, createdIndentId = null, createdIndentCode = null, createdDate = null) }
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
                if (query.length < 3) {
                    _uiState.update { it.copy(clientSearchResults = emptyList()) }
                    return@launch
                }

                val result = getClientsUseCase.invoke(query)
                if (result.isSuccess) {
                    _uiState.update { it.copy(clientSearchResults = result.getOrNull() ?: emptyList()) }
                } else {
                    _uiState.update {
                        it.copy(
                            clientSearchResults = emptyList(),
                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to search clients",
                        )
                    }
                }
            }
    }

    /**
     * Loads the selected client's branch and contact directory from the v2
     * client API. Both calls run in one coroutine but each failure is absorbed
     * into state rather than raised:
     *
     * - an unreachable directory is a *normal* state — a freshly onboarded
     *   client legitimately has no contacts yet;
     * - the backend hard-requires a `placement` contact, so an empty list is the
     *   one condition that can block indent creation outright. The form renders
     *   that inline where the picker would be, instead of letting the operator
     *   discover it as a 400 on submit.
     */
    private fun loadClientDirectory(clientId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingBranches = true, isLoadingContacts = true) }

            val branchesResult = getClientBranchesUseCase.invoke(clientId)
            val contactsResult = getClientContactsUseCase.invoke(clientId)

            _uiState.update { state ->
                val contacts = contactsResult.getOrNull().orEmpty()
                state.copy(
                    branches = branchesResult.getOrNull().orEmpty(),
                    isLoadingBranches = false,
                    contacts = contacts,
                    isLoadingContacts = false,
                    contactsUnavailable = contactsResult.isFailure || contacts.isEmpty(),
                )
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
                    truckTypeId = state.selectedTruckType!!.id.toIntOrNull() ?: 0,
                    loadWeight = state.weight.toDoubleOrNull() ?: 0.0,
                    material = state.materialName,
                    clientTripCost = state.clientCost.replace(",", "").toDoubleOrNull(),
                    negotiatedTripCost = state.negotiatedCost.replace(",", "").toDoubleOrNull(),
                    businessProposedCost = state.businessProposedCost.replace(",", "").toDoubleOrNull(),
                    vehicleHeight = state.vehicleHeight.toDoubleOrNull(),
                    hideCostInfo = state.hideCost,
                    tatEnabled = state.tatRequired,
                    tatHours = state.tatHours.toIntOrNull(),
                    tarpaulingRequired = state.tarpaulinRequired,
                    remarks = state.remarks,
                    flooringType = state.flooringType.lowercase(Locale.getDefault()),
                    locations = listOf(loadingLocation, unLoadingLocation),
                    clientBranchId = state.selectedBranch?.id,
                    contacts = IndentContactsRequest(
                        placement = state.placementContact?.id,
                        payment = state.paymentContact?.id,
                        operations = state.operationsContact?.id,
                        pod = state.podContact?.id,
                    ).takeUnless { it.isEmpty },
                )
                val result = createIndentUseCase.invoke(request)
                if (result.isSuccess) {
                    val indentResponse = result.getOrNull()
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            successMessage = "Indent Created Successfully",
                            createdIndentId = indentResponse?.data?.id,
                            createdIndentCode = indentResponse?.data?.indentCode,
                            createdDate = indentResponse?.data?.createdAt,
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
                areaId = location.id.toIntOrNull()
            } else {
                cityId = location.id.toIntOrNull()
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
        // The backend destructures `contacts = {}` and then rejects a missing
        // placement with a 400, so this is required even though the field reads
        // as optional client information.
        if (state.placementContact == null) {
            _uiState.update { it.copy(errorMessage = "Please select a Placement contact") }
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
