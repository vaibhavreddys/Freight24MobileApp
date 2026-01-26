package com.freight24.trucker.feature.trips.view.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.model.TripDetail
import com.freight24.trucker.feature.trips.domain.usecase.FetchTripDetailParams
import com.freight24.trucker.feature.trips.domain.usecase.FetchTripDetailUseCase
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TripDetailsViewModel @Inject constructor(
    private val fetchTripDetailUseCase: FetchTripDetailUseCase,
    private val updateTripStatusUseCase: com.freight24.trucker.feature.trips.domain.usecase.UpdateTripStatusUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val tripId: String = checkNotNull(savedStateHandle["tripId"]) {
        "Trip ID is required"
    }

    private val _uiState = MutableStateFlow<UiState<TripDetail>>(UiState.Loading)
    val uiState: StateFlow<UiState<TripDetail>> = _uiState.asStateFlow()

    init {
        fetchTripDetail()
    }

    private fun fetchTripDetail(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            fetchTripDetailUseCase(FetchTripDetailParams(tripId, forceRefresh))
                .onSuccess { tripDetail: TripDetail ->
                    _uiState.value = UiState.Success(tripDetail)
                }
                .onFailure { error: Throwable ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to load trip details")
                }
        }
    }

    fun retry() {
        fetchTripDetail(forceRefresh = true)
    }

    fun updateTripStatus(status: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            updateTripStatusUseCase(
                com.freight24.trucker.feature.trips.domain.usecase.UpdateTripStatusParams(
                    tripId,
                    status
                )
            )
                .onSuccess { tripDetail ->
                    _uiState.value = UiState.Success(tripDetail)
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to update status")
                }
        }
    }
}
