package com.freight24.connect.feature.trips.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.trips.domain.usecase.GetMyTripsUseCase
import com.freight24.connect.feature.trips.domain.usecase.GetMyTripsUseCaseDataParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TripsListViewModelImpl
    @Inject
    constructor(
        private val getMyTripsUseCase: GetMyTripsUseCase,
    ) : ViewModel(), TripsListViewModel {
        private val _uiState = MutableStateFlow(TripsListUiState())
        override val uiState: StateFlow<TripsListUiState> = _uiState.asStateFlow()

        private var fetchJob: Job? = null

        init {
            fetchTrips()
        }

        override fun onEvent(event: TripsListEvent) {
            when (event) {
                is TripsListEvent.SelectFilter -> {
                    if (_uiState.value.selectedFilter != event.filter) {
                        _uiState.update { it.copy(selectedFilter = event.filter) }
                        fetchTrips()
                    }
                }
                TripsListEvent.Refresh -> fetchTrips()
            }
        }

        private fun fetchTrips() {
            fetchJob?.cancel()
            fetchJob =
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                    val result =
                        getMyTripsUseCase(
                            GetMyTripsUseCaseDataParams(
                                tripTypes = _uiState.value.selectedFilter,
                                page = 1,
                                limit = 20,
                            ),
                        )

                    result.fold(
                        onSuccess = { response ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    trips = response.data,
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = error.message ?: "Unknown error",
                                )
                            }
                        },
                    )
                }
        }
    }
