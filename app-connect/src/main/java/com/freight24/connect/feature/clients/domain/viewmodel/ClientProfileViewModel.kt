package com.freight24.connect.feature.clients.domain.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.domain.model.ClientDetails
import com.freight24.connect.feature.clients.domain.usecases.GetClientDetailsUseCase
import com.freight24.connect.repository.ClientRepository
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientProfileViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val getClientDetailsUseCase: GetClientDetailsUseCase,
        private val repository: ClientRepository,
    ) : ViewModel() {
        private val clientId: String = checkNotNull(savedStateHandle["clientId"])

        private val _uiState = MutableStateFlow<UiState<ClientDetails>>(UiState.Loading)
        val uiState: StateFlow<UiState<ClientDetails>> = _uiState.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        private val _errorEvent = MutableSharedFlow<String>()
        val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

        init {
            fetchClient()
        }

        fun onRefresh() = fetchClient(refresh = true)

        fun retry() = fetchClient()

        fun getDocumentSignedUrl(
            documentId: String,
            branchId: String? = null,
            onReady: (String) -> Unit,
        ) {
            viewModelScope.launch {
                val result =
                    if (branchId == null) {
                        repository.getClientDocumentSignedUrl(clientId, documentId)
                    } else {
                        repository.getBranchDocumentSignedUrl(clientId, branchId, documentId)
                    }
                result.onSuccess(onReady).onFailure {
                    _errorEvent.emit(it.message ?: "Failed to open document")
                }
            }
        }

        private fun fetchClient(refresh: Boolean = false) {
            viewModelScope.launch {
                if (refresh) _isRefreshing.value = true else _uiState.value = UiState.Loading
                getClientDetailsUseCase(clientId)
                    .onSuccess { _uiState.value = UiState.Success(it) }
                    .onFailure { _uiState.value = UiState.Error(it.message ?: "Failed to load client profile") }
                _isRefreshing.value = false
            }
        }
    }
