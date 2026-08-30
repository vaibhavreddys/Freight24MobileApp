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

    enum class OwnerFilter(val label: String) {
        ALL("All"),
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        PENDING("Pending")
    }

    private val _uiState = MutableStateFlow<UiState<List<TruckOwnerDto>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<TruckOwnerDto>>> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(OwnerFilter.ALL)
    val selectedFilter: StateFlow<OwnerFilter> = _selectedFilter.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val filteredOwners: StateFlow<List<TruckOwnerDto>> = combine(
        _uiState,
        _searchQuery,
        _selectedFilter
    ) { state, query, filter ->
        if (state is UiState.Success) {
            state.data.filter { owner ->
                val matchesQuery =
                    query.isBlank() ||
                        owner.full_name.contains(query, ignoreCase = true) ||
                        owner.tag_code.orEmpty().contains(query, ignoreCase = true)
                val matchesFilter =
                    when (filter) {
                        OwnerFilter.ALL -> true
                        OwnerFilter.ACTIVE -> owner.statusKey == OwnerFilter.ACTIVE
                        OwnerFilter.INACTIVE -> owner.statusKey == OwnerFilter.INACTIVE
                        OwnerFilter.PENDING -> owner.statusKey == OwnerFilter.PENDING
                    }
                matchesQuery && matchesFilter
            }
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        loadTruckOwners()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChanged(filter: OwnerFilter) {
        _selectedFilter.value = filter
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
                    if (!isRefresh) {
                        _uiState.value = UiState.Error(error.message ?: "Failed to load truck owners")
                    }
                    _isRefreshing.value = false
                }
        }
    }
}

private val TruckOwnerDto.statusKey: ManageTruckOwnersViewModel.OwnerFilter
    get() =
        when (status?.trim()?.lowercase()) {
            "active" -> ManageTruckOwnersViewModel.OwnerFilter.ACTIVE
            "inactive" -> ManageTruckOwnersViewModel.OwnerFilter.INACTIVE
            else -> ManageTruckOwnersViewModel.OwnerFilter.PENDING
        }
