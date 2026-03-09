package com.freight24.connect.feature.truckowners.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.trucks.domain.usecases.GetTruckOwnersUseCase
import com.freight24.rest.response.TruckOwnerDto
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageTruckOwnersViewModel @Inject constructor(
    private val getTruckOwnersUseCase: GetTruckOwnersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<TruckOwnerDto>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<TruckOwnerDto>>> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val filteredOwners: StateFlow<List<TruckOwnerDto>> = combine(
        _uiState,
        _searchQuery
    ) { state, query ->
        if (state is UiState.Success) {
            if (query.isBlank()) {
                state.data
            } else {
                state.data.filter { owner ->
                    owner.full_name.contains(query, ignoreCase = true) ||
                        owner.business_name?.contains(query, ignoreCase = true) == true ||
                        owner.phone.contains(query)
                }
            }
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadTruckOwners()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onRefresh() {
        loadTruckOwners(isRefresh = true)
    }

    fun retry() {
        loadTruckOwners()
    }

    private fun loadTruckOwners(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _uiState.value = UiState.Loading
            }

            getTruckOwnersUseCase()
                .onSuccess { owners ->
                    _uiState.value = UiState.Success(owners)
                    _isRefreshing.value = false
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to load truck owners")
                    _isRefreshing.value = false
                }
        }
    }
}
