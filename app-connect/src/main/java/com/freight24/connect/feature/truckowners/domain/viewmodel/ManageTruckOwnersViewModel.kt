package com.freight24.connect.feature.truckowners.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.domain.model.TruckOwnerManagementItem
import com.freight24.connect.feature.trucks.domain.usecases.GetTruckOwnersParams
import com.freight24.connect.feature.trucks.domain.usecases.GetTruckOwnersUseCase
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageTruckOwnersViewModel
    @Inject
    constructor(
        private val getTruckOwnersUseCase: GetTruckOwnersUseCase,
    ) : ViewModel() {
        enum class OwnerFilter(val label: String) {
            ALL("All"),
            ACTIVE("Active"),
            INACTIVE("Inactive"),
            PENDING("Pending documents"),
        }

        private val _uiState = MutableStateFlow<UiState<List<TruckOwnerManagementItem>>>(UiState.Loading)
        val uiState: StateFlow<UiState<List<TruckOwnerManagementItem>>> = _uiState.asStateFlow()

        private val _owners = MutableStateFlow<List<TruckOwnerManagementItem>>(emptyList())
        val filteredOwners: StateFlow<List<TruckOwnerManagementItem>> = _owners.asStateFlow()

        private val _searchQuery = MutableStateFlow("")
        val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

        private val _selectedFilter = MutableStateFlow(OwnerFilter.ALL)
        val selectedFilter: StateFlow<OwnerFilter> = _selectedFilter.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        private val _isLoadingMore = MutableStateFlow(false)
        val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

        private var page = 1
        private var endOfList = false
        private var searchJob: Job? = null
        private var requestJob: Job? = null

        init {
            loadFirstPage()
        }

        fun onSearchQueryChanged(query: String) {
            _searchQuery.value = query
            if (query.isNotBlank()) _selectedFilter.value = OwnerFilter.ALL
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                delay(350)
                loadFirstPage()
            }
        }

        fun onFilterChanged(filter: OwnerFilter) {
            if (_selectedFilter.value == filter) return
            _selectedFilter.value = filter
            loadFirstPage()
        }

        fun loadNextPage() {
            if (endOfList || _isLoadingMore.value || _isRefreshing.value) return
            loadPage(page + 1, append = true)
        }

        fun onRefresh() = loadFirstPage(refresh = true)

        fun retry() = loadFirstPage()

        private fun loadFirstPage(refresh: Boolean = false) {
            requestJob?.cancel()
            page = 1
            endOfList = false
            if (refresh) _isRefreshing.value = true else _uiState.value = UiState.Loading
            loadPage(targetPage = 1, append = false, refresh = refresh)
        }

        private fun loadPage(targetPage: Int, append: Boolean, refresh: Boolean = false) {
            requestJob = viewModelScope.launch {
                if (append) _isLoadingMore.value = true
                val filter = _selectedFilter.value
                getTruckOwnersUseCase(
                    GetTruckOwnersParams(
                        query = _searchQuery.value.trim().ifBlank { null },
                        status =
                            when (filter) {
                                OwnerFilter.ACTIVE -> "active"
                                OwnerFilter.INACTIVE -> "inactive"
                                else -> null
                            },
                        documentStatus = if (filter == OwnerFilter.PENDING) "pending_docs" else null,
                        page = targetPage,
                    ),
                ).onSuccess { result ->
                    page = result.page
                    endOfList = result.endOfList
                    _owners.value =
                        if (append) {
                            (_owners.value + result.items).distinctBy { it.ownerUserId }
                        } else {
                            result.items
                        }
                    _uiState.value = UiState.Success(_owners.value)
                }.onFailure { error ->
                    if (!append && !refresh) {
                        _uiState.value = UiState.Error(error.message ?: "Failed to load truck owners")
                    }
                }
                _isLoadingMore.value = false
                _isRefreshing.value = false
            }
        }
    }
