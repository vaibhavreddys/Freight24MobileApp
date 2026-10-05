package com.freight24.connect.feature.trucks.views.details

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.model.TruckDetail
import com.freight24.common.model.TruckDocument
import com.freight24.connect.feature.trucks.domain.usecases.GetTruckByIdUseCase
import com.freight24.connect.feature.trucks.domain.usecases.GetTruckByIdUseCaseParams
import com.freight24.connect.feature.trucks.domain.usecases.GetTruckDocumentSignedUrlParams
import com.freight24.connect.feature.trucks.domain.usecases.GetTruckDocumentSignedUrlUseCase
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TruckDetailsViewModel
    @Inject
    constructor(
        private val getTruckByIdUseCase: GetTruckByIdUseCase,
        private val getTruckDocumentSignedUrlUseCase: GetTruckDocumentSignedUrlUseCase,
        @ApplicationContext private val context: Context,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val truckId: String = checkNotNull(savedStateHandle["truckId"])

        private val _uiState = MutableStateFlow<UiState<TruckDetail>>(UiState.Loading)
        val uiState: StateFlow<UiState<TruckDetail>> = _uiState.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        private val _isUploading = MutableStateFlow(false)
        val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

        private val _selectedUris = MutableStateFlow<List<Uri>>(emptyList())
        val selectedUris: StateFlow<List<Uri>> = _selectedUris.asStateFlow()

        private val _isPdf = MutableStateFlow(false)
        val isPdf: StateFlow<Boolean> = _isPdf.asStateFlow()

        private val _currentDocumentType = MutableStateFlow<String?>(null)
        val currentDocumentType: StateFlow<String?> = _currentDocumentType.asStateFlow()

        private val _uploadSuccess = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()
        val uploadSuccess = _uploadSuccess.asSharedFlow()

        private val _errorEvent = kotlinx.coroutines.flow.MutableSharedFlow<String>()
        val errorEvent = _errorEvent.asSharedFlow()

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

                getTruckByIdUseCase(GetTruckByIdUseCaseParams(truckId))
                    .onSuccess { response ->
                        response.data?.let { dto ->
                            _uiState.value = UiState.Success(dto.toDomain())
                        } ?: run {
                            _uiState.value = UiState.Error("Empty response data")
                        }
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

        fun onFilesPicked(
            uris: List<Uri>,
            pdf: Boolean,
            documentType: String,
        ) {
            _errorEvent.tryEmit("Uploading documents not supported here")
        }

        fun clearSelection() {
            _selectedUris.value = emptyList()
            _isPdf.value = false
            _currentDocumentType.value = null
        }

        fun uploadDocuments() {
            // Not supported
        }

        fun fetchSignedUrl(
            documentType: String,
            onUrlReady: (String) -> Unit,
        ) {
            viewModelScope.launch {
                getTruckDocumentSignedUrlUseCase(
                    GetTruckDocumentSignedUrlParams(truckId, documentType),
                ).onSuccess(onUrlReady)
                    .onFailure { error ->
                        _errorEvent.emit(error.message ?: "Failed to open document")
                    }
            }
        }
    }

private fun com.freight24.rest.response.TruckDetailDto.toDomain() =
    TruckDetail(
        truckId = this.truckId,
        registrationNumber = this.registrationNumber,
        isVerified = this.isVerified,
        truckType = this.truckType,
        bodyType = this.bodyType,
        displayName = this.displayName,
        make = this.make,
        model = this.model,
        year = this.year,
        rcCard = this.rcCard?.toDomain(),
        fcCertificate = this.fcCertificate?.toDomain(),
        insurance = this.insurance?.toDomain(),
        isBlacklisted = this.isBlacklisted,
    )

private fun com.freight24.rest.response.DocumentDto.toDomain() =
    TruckDocument(
        url = this.url,
        status = this.status,
    )
