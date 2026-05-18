package com.freight24.connect.feature.adhocTruck.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.adhocTruck.domain.usecase.QuickAdhocTruckUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetTruckTypesUseCase
import com.freight24.connect.repository.LocationRepository
import com.freight24.rest.request.CreateAdhocTruckRequest
import com.freight24.rest.response.AdhocTruckOwnerDto
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
internal class QuickAdhocTruckViewModelImpl @Inject constructor(
    private val useCase: QuickAdhocTruckUseCase,
    private val getTruckTypesUseCase: GetTruckTypesUseCase,
    private val locationRepository: LocationRepository,
) : ViewModel(), QuickAdhocTruckViewModel {

    private val _uiState = MutableStateFlow(QuickAdhocTruckUiState())
    override val uiState: StateFlow<QuickAdhocTruckUiState> = _uiState.asStateFlow()

    private var phoneSearchJob: Job? = null
    private var locationSearchJob: Job? = null

    init {
        loadTruckTypes()
    }

    private fun loadTruckTypes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTruckTypes = true) }
            val result = getTruckTypesUseCase.invoke()
            _uiState.update {
                it.copy(
                    truckTypes = result.getOrNull(),
                    isLoadingTruckTypes = false,
                )
            }
        }
    }

    override fun onEvent(event: QuickAdhocTruckEvent) {
        when (event) {
            is QuickAdhocTruckEvent.PhoneChanged -> onPhoneChanged(event.phone)
            is QuickAdhocTruckEvent.SelectOwner -> onSelectOwner(event.owner)
            is QuickAdhocTruckEvent.OwnerNameChanged -> {
                _uiState.update { it.copy(ownerName = event.name) }
            }
            is QuickAdhocTruckEvent.SelectTruckType -> {
                _uiState.update { it.copy(selectedTruckType = event.truckType) }
            }
            is QuickAdhocTruckEvent.LocationQueryChanged -> onLocationQueryChanged(event.query)
            is QuickAdhocTruckEvent.SelectLocation -> {
                _uiState.update { it.copy(selectedLocation = event.location, locationQuery = event.location.name) }
            }
            is QuickAdhocTruckEvent.Submit -> submit()
            is QuickAdhocTruckEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun onPhoneChanged(phone: String) {
        _uiState.update {
            it.copy(
                phoneNumber = phone,
                selectedOwner = null,
                matchedOwners = emptyList(),
            )
        }

        phoneSearchJob?.cancel()
        if (phone.length >= 2) {
            phoneSearchJob = viewModelScope.launch {
                delay(400)
                _uiState.update { it.copy(isSearchingPhone = true) }
                val result = useCase.searchByPhone(phone)
                result.onSuccess { owners ->
                    _uiState.update {
                        it.copy(
                            matchedOwners = owners,
                            isSearchingPhone = false,
                        )
                    }
                }.onFailure {
                    _uiState.update { it.copy(isSearchingPhone = false) }
                }
            }
        }
    }

    private fun onSelectOwner(owner: AdhocTruckOwnerDto) {
        _uiState.update {
            it.copy(
                selectedOwner = owner,
                phoneNumber = owner.phone,
                matchedOwners = emptyList(),
                ownerName = owner.fullName,
            )
        }
    }

    private fun onLocationQueryChanged(query: String) {
        _uiState.update { it.copy(locationQuery = query, selectedLocation = null) }

        locationSearchJob?.cancel()
        if (query.length >= 2) {
            locationSearchJob = viewModelScope.launch {
                delay(400)
                _uiState.update { it.copy(isSearchingLocation = true) }
                val result = locationRepository.searchLocations(query)
                result.onSuccess { locations ->
                    _uiState.update {
                        it.copy(
                            locationResults = locations,
                            isSearchingLocation = false,
                        )
                    }
                }.onFailure {
                    _uiState.update { it.copy(isSearchingLocation = false) }
                }
            }
        } else {
            _uiState.update { it.copy(locationResults = emptyList()) }
        }
    }

    private fun submit() {
        val state = _uiState.value

        if (state.phoneNumber.isBlank()) {
            _uiState.update { it.copy(error = "Enter phone number") }
            return
        }
        if (state.selectedTruckType == null) {
            _uiState.update { it.copy(error = "Select a truck type") }
            return
        }
        if (state.selectedLocation == null) {
            _uiState.update { it.copy(error = "Select truck location") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            val request = CreateAdhocTruckRequest(
                phoneNumber = state.phoneNumber,
                truckOwnerId = state.selectedOwner?.id,
                truckOwnerName = state.ownerName.ifBlank { null },
                truckTypeId = state.selectedTruckType.id.toInt(),
                locationId = state.selectedLocation.id.toInt(),
                locationType = state.selectedLocation.type,
            )

            val result = useCase.createEntry(request)
            result.onSuccess {
                _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        error = e.message ?: "Failed to create entry",
                    )
                }
            }
        }
    }
}
