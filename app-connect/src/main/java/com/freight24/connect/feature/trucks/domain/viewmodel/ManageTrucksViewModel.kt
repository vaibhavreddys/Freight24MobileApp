package com.freight24.connect.feature.trucks.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.domain.model.TruckManagementItem
import com.freight24.connect.feature.trucks.domain.usecases.GetTrucksParams
import com.freight24.connect.feature.trucks.domain.usecases.GetTrucksUseCase
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
class ManageTrucksViewModel
    @Inject
    constructor(
        private val getTrucksUseCase: GetTrucksUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<UiState<List<TruckManagementItem>>>(UiState.Loading)
        val uiState: StateFlow<UiState<List<TruckManagementItem>>> = _uiState.asStateFlow()

        private val _searchQuery = MutableStateFlow("")
        val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

        private val _trucks = MutableStateFlow<List<TruckManagementItem>>(emptyList())
        val filteredTrucks: StateFlow<List<TruckManagementItem>> = _trucks.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        private val _isLoadingMore = MutableStateFlow(false)
        val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

        private var page = 1
        private var endOfList = false
        private var status: String? = null
        private var documentStatus: String? = null
        private var searchJob: Job? = null
        private var requestJob: Job? = null

        init {
            loadFirstPage()
        }

        fun onSearchQueryChanged(query: String) {
            _searchQuery.value = query
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                delay(350)
                loadFirstPage()
            }
        }

        fun setFilters(status: String?, documentStatus: String?) {
            if (this.status == status && this.documentStatus == documentStatus) return
            this.status = status
            this.documentStatus = documentStatus
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
            if (refresh) {
                _isRefreshing.value = true
            } else {
                _uiState.value = UiState.Loading
            }
            loadPage(1, append = false, refresh = refresh)
        }

        private fun loadPage(targetPage: Int, append: Boolean, refresh: Boolean = false) {
            requestJob = viewModelScope.launch {
                if (append) _isLoadingMore.value = true
                getTrucksUseCase(
                    GetTrucksParams(
                        query = _searchQuery.value.trim().ifBlank { null },
                        status = status,
                        documentStatus = documentStatus,
                        page = targetPage,
                    ),
                ).onSuccess { result ->
                    page = result.page
                    endOfList = result.endOfList
                    _trucks.value = if (append) (_trucks.value + result.items).distinctBy { it.truckId } else result.items
                    _uiState.value = UiState.Success(_trucks.value)
                }.onFailure { error ->
                    if (!append && !refresh) {
                        _uiState.value = UiState.Error(error.message ?: "Failed to load trucks")
                    }
                }
                _isLoadingMore.value = false
                _isRefreshing.value = false
            }
        }
    }
