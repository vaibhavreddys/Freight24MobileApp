package com.freight24.connect.feature.clients.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.clients.domain.usecases.GetClientsUseCase
import com.freight24.rest.response.ClientDto
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
class ManageClientsViewModel
    @Inject
    constructor(
        private val getClientsUseCase: GetClientsUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<UiState<List<ClientDto>>>(UiState.Loading)
        val uiState: StateFlow<UiState<List<ClientDto>>> = _uiState.asStateFlow()

        private val _searchQuery = MutableStateFlow("")
        val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        val filteredClients: StateFlow<List<ClientDto>> =
            combine(
                _uiState,
                _searchQuery,
            ) { state, query ->
                if (state is UiState.Success) {
                    if (query.isBlank()) {
                        state.data
                    } else {
                        state.data.filter { client ->
                            client.fullName?.contains(query, ignoreCase = true) == true ||
                                client.companyName?.contains(query, ignoreCase = true) == true ||
                                client.phoneNumber?.contains(query) == true
                        }
                    }
                } else {
                    emptyList()
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        init {
            loadClients()
        }

        fun onSearchQueryChanged(query: String) {
            _searchQuery.value = query
        }

        fun onRefresh() {
            loadClients(isRefresh = true)
        }

        fun retry() {
            loadClients()
        }

        private fun loadClients(isRefresh: Boolean = false) {
            viewModelScope.launch {
                if (isRefresh) {
                    _isRefreshing.value = true
                } else {
                    _uiState.value = UiState.Loading
                }

                getClientsUseCase()
                    .onSuccess { clients ->
                        _uiState.value = UiState.Success(clients)
                        _isRefreshing.value = false
                    }
                    .onFailure { error ->
                        _uiState.value = UiState.Error(error.message ?: "Failed to load clients")
                        _isRefreshing.value = false
                    }
            }
        }
    }
