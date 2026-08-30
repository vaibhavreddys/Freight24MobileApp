package com.freight24.connect.feature.trips.domain.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.trips.domain.usecase.GetMyTripsUseCase
import com.freight24.connect.feature.trips.domain.usecase.GetMyTripsUseCaseDataParams
import com.freight24.connect.feature.trips.domain.usecase.TripTypes
import com.freight24.connect.navigation.ConnectDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilteredTripsViewModelImpl @Inject constructor(
    private val getMyTripsUseCase: GetMyTripsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel(), TripsListViewModel {

    private val filterTypeString: String = savedStateHandle[ConnectDestination.FilteredTrips.FILTER_TYPE_ARG] ?: TripTypes.ALL.name
    private val initialFilter = TripTypes.entries.find { it.name == filterTypeString } ?: TripTypes.ALL

    private val _uiState = MutableStateFlow(TripsListUiState(selectedFilter = initialFilter))
    override val uiState: StateFlow<TripsListUiState> = _uiState.asStateFlow()

    init {
        fetchTrips()
    }

    override fun onEvent(event: TripsListEvent) {
        when (event) {
            is TripsListEvent.SelectFilter -> {
                _uiState.update { it.copy(selectedFilter = event.filter) }
                fetchTrips()
            }
            TripsListEvent.Refresh -> fetchTrips()
        }
    }

    private fun fetchTrips() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = getMyTripsUseCase(
                GetMyTripsUseCaseDataParams(
                    tripTypes = _uiState.value.selectedFilter,
                    page = 1,
                    limit = 50
                )
            )
            result.fold(
                onSuccess = { response ->
                    _uiState.update { it.copy(isLoading = false, trips = response.data) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
            )
        }
    }
}
