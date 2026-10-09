package com.freight24.connect.feature.trips.domain.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.trips.domain.usecase.GetMyTripsUseCase
import com.freight24.connect.feature.trips.domain.usecase.GetMyTripsUseCaseDataParams
import com.freight24.connect.feature.trips.domain.usecase.TripTypes
import com.freight24.connect.navigation.ConnectDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilteredTripsViewModelImpl
    @Inject
    constructor(
        private val getMyTripsUseCase: GetMyTripsUseCase,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel(), TripsListViewModel {
        private val filterTypeString: String = savedStateHandle[ConnectDestination.FilteredTrips.FILTER_TYPE_ARG] ?: TripTypes.ALL.name
        private val initialFilter = TripTypes.entries.find { it.name == filterTypeString } ?: TripTypes.ALL

        private val _uiState = MutableStateFlow(TripsListUiState(selectedFilter = initialFilter))
        override val uiState: StateFlow<TripsListUiState> = _uiState.asStateFlow()

        // The foreground refresh can land on top of the first load, or on top of
        // itself when the user switches apps twice quickly. Cancelling the
        // in-flight fetch stops the older response from overwriting the newer one.
        private var fetchJob: Job? = null

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
                TripsListEvent.SilentRefresh -> fetchTrips(isRefresh = true)
            }
        }

        /**
         * [isRefresh] is the silent path taken when the screen returns to the
         * foreground: trips already on screen stay put while this reloads, and a
         * failure leaves them there instead of replacing a populated screen with
         * an error. Only the empty screen falls back to the loading and error
         * states, which is the same contract as the first load.
         *
         * Assigning an equal [TripsListUiState] is dropped by the StateFlow's
         * equality check, so a payload identical to what is already shown causes
         * no recomposition — "only update if it changed" holds without an
         * explicit diff.
         */
        private fun fetchTrips(isRefresh: Boolean = false) {
            fetchJob?.cancel()
            fetchJob = viewModelScope.launch {
                _uiState.update {
                    if (isRefresh && it.trips.isNotEmpty()) it else it.copy(isLoading = true, errorMessage = null)
                }
                val result =
                    getMyTripsUseCase(
                        GetMyTripsUseCaseDataParams(
                            tripTypes = _uiState.value.selectedFilter,
                            page = 1,
                            limit = 50,
                        ),
                    )
                result.fold(
                    onSuccess = { response ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = null, trips = response.data) }
                    },
                    onFailure = { error ->
                        // Never turn a screen that is already showing trips into
                        // an error page because a background reload failed.
                        _uiState.update { it.copy(isLoading = false, errorMessage = if (it.trips.isEmpty()) error.message else null) }
                    },
                )
            }
        }
    }
