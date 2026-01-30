package com.freight24.trucker.feature.trucks.view.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.model.TruckDetail
import com.freight24.trucker.feature.trucks.domain.usecase.FetchTruckDetailParams
import com.freight24.trucker.feature.trucks.domain.usecase.FetchTruckDetailUseCase
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TruckDetailsViewModel
    @Inject
    constructor(
        private val fetchTruckDetailUseCase: FetchTruckDetailUseCase,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val truckId: String = checkNotNull(savedStateHandle["truckId"])

        private val _uiState = MutableStateFlow<UiState<TruckDetail>>(UiState.Loading)
        val uiState: StateFlow<UiState<TruckDetail>> = _uiState.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        init {
            fetchTruckDetail()
        }

        fun fetchTruckDetail(forceRefresh: Boolean = false) {
            viewModelScope.launch {
                if (forceRefresh) {
                    _isRefreshing.value = true
                } else {
                    _uiState.value = UiState.Loading
                }

                fetchTruckDetailUseCase(FetchTruckDetailParams(truckId))
                    .onSuccess { detail ->
                        _uiState.value = UiState.Success(detail)
                    }
                    .onFailure { error ->
                        _uiState.value = UiState.Error(error.message ?: "Failed to fetch truck details")
                    }

                _isRefreshing.value = false
            }
        }

        fun onRefresh() {
            fetchTruckDetail(forceRefresh = true)
        }
    }
