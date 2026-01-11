package com.freight24.trucker.feature.trips.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.core.common.model.TripDetail
import com.freight24.core.common.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TripDetailUiState {
    object Loading : TripDetailUiState()
    data class Success(val trip: TripDetail) : TripDetailUiState()
    data class Error(val message: String) : TripDetailUiState()
}

@HiltViewModel
class TripDetailsViewModel @Inject constructor(
    private val repository: TripRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: String = checkNotNull(savedStateHandle["tripId"])

    private val _uiState = MutableStateFlow<TripDetailUiState>(TripDetailUiState.Loading)
    val uiState: StateFlow<TripDetailUiState> = _uiState.asStateFlow()

    init {
        fetchTripDetail()
    }

    fun fetchTripDetail() {
        viewModelScope.launch {
            _uiState.value = TripDetailUiState.Loading
            repository.fetchTripDetail(tripId)
                .catch { e ->
                    _uiState.value = TripDetailUiState.Error(e.message ?: "Unknown error")
                }
                .collect { trip ->
                    _uiState.value = TripDetailUiState.Success(trip)
                }
        }
    }
}
