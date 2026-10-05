package com.freight24.connect.feature.findTrucks.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.findTrucks.domain.usecases.FindTrucksUseCase
import com.freight24.connect.feature.findTrucks.domain.usecases.FindTrucksUseCaseDataParams
import com.freight24.connect.feature.indents.domain.usecases.GetTruckTypesUseCase
import com.freight24.rest.response.LocationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FindTrucksViewModelImpl
    @Inject
    constructor(
        private val getTruckTypesUseCase: GetTruckTypesUseCase,
        private val findTrucksUseCase: FindTrucksUseCase,
    ) : ViewModel(), FindTrucksViewModel {
        private val _uiState = MutableStateFlow(FindTrucksUiState())
        override val uiState: StateFlow<FindTrucksUiState> = _uiState.asStateFlow()

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
                            truckTypes = result.getOrNull(),
                            isLoadingTruckTypes = false,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoadingTruckTypes = false,
                            error = "Failed to load truck types",
                        )
                    }
                }
            }
        }

        override fun onEvent(event: FindTrucksEvent) {
            when (event) {
                is FindTrucksEvent.SelectLoadingLocation -> {
                    _uiState.update { it.copy(loadingLocation = event.location) }
                }
                is FindTrucksEvent.SelectUnloadingLocation -> {
                    _uiState.update { it.copy(unloadingLocation = event.location) }
                }
                FindTrucksEvent.SwapLocations -> {
                    _uiState.update { state ->
                        if (state.isSearching) {
                            state
                        } else {
                            state.copy(
                                loadingLocation = state.unloadingLocation,
                                unloadingLocation = state.loadingLocation,
                            )
                        }
                    }
                }
                is FindTrucksEvent.ToggleTruckType -> {
                    _uiState.update { state ->
                        val current = state.selectedTruckTypes
                        val updated =
                            if (current.contains(event.truckType)) {
                                current - event.truckType
                            } else {
                                current + event.truckType
                            }
                        state.copy(selectedTruckTypes = updated)
                    }
                }
                is FindTrucksEvent.ClearTruckTypes -> {
                    _uiState.update { it.copy(selectedTruckTypes = emptySet()) }
                }
                is FindTrucksEvent.SearchTrucks -> searchTrucks()
                is FindTrucksEvent.DismissResults -> {
                    _uiState.update { it.copy(showResults = false) }
                }
                is FindTrucksEvent.ClearError -> {
                    _uiState.update { it.copy(error = null) }
                }
            }
        }

        private fun searchTrucks() {
            val state = _uiState.value
            if (state.loadingLocation == null) {
                _uiState.update { it.copy(error = "Please select a loading location") }
                return
            }
            if (state.selectedTruckTypes.isEmpty()) {
                _uiState.update { it.copy(error = "Please select at least one truck type") }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isSearching = true, error = null) }

                val (areaId, cityId) = getAreaCityId(state.loadingLocation)
                val (_, destinationCityId) = getAreaCityId(state.unloadingLocation)

                val truckTypeIds = state.selectedTruckTypes.joinToString(",") { it.id }

                val params =
                    FindTrucksUseCaseDataParams(
                        areaId = areaId,
                        cityId = cityId,
                        destinationCityId = destinationCityId,
                        truckTypeIds = truckTypeIds,
                    )

                val result = findTrucksUseCase.invoke(params)
                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            searchResults = result.getOrNull(),
                            showResults = true,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            error = result.exceptionOrNull()?.message ?: "Search failed",
                        )
                    }
                }
            }
        }

        private fun getAreaCityId(location: LocationDto?): Pair<String?, String?> {
            if (location == null) return null to null
            return if (location.type.equals("area", ignoreCase = true)) {
                location.id to null
            } else {
                null to location.id
            }
        }
    }
