package com.freight24.trucker.feature.trips.view.details

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.model.TripDetail
import com.freight24.common.util.FileUtils
import com.freight24.trucker.feature.trips.domain.usecase.FetchTripDetailParams
import com.freight24.trucker.feature.trips.domain.usecase.FetchTripDetailUseCase
import com.freight24.trucker.feature.trips.domain.usecase.GetSignedDocumentParams
import com.freight24.trucker.feature.trips.domain.usecase.GetSignedDocumentUseCase
import com.freight24.trucker.feature.trips.domain.usecase.UploadLrUseCase
import com.freight24.trucker.feature.trips.domain.usecase.UploadLrUseCaseDataParams
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal enum class UploadStatus {
    IDLE,
    UPLOADING,
    ERROR,
}

@HiltViewModel
internal class TripDetailsViewModel
    @Inject
    constructor(
        private val fetchTripDetailUseCase: FetchTripDetailUseCase,
        private val updateTripStatusUseCase: com.freight24.trucker.feature.trips.domain.usecase.UpdateTripStatusUseCase,
        private val uploadLrUseCase: UploadLrUseCase,
        private val getSignedDocumentUseCase: GetSignedDocumentUseCase,
        @param:ApplicationContext private val context: Context,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val tripId: String = checkNotNull(savedStateHandle["tripId"]) {
            "Trip ID is required"
        }

        private val _uiState = MutableStateFlow<UiState<TripDetail>>(UiState.Loading)
        val uiState: StateFlow<UiState<TripDetail>> = _uiState.asStateFlow()

        private val _selectedUris = MutableStateFlow<List<Uri>>(emptyList())
        val selectedUris: StateFlow<List<Uri>> = _selectedUris.asStateFlow()

        private val _isPdf = MutableStateFlow(false)
        val isPdf: StateFlow<Boolean> = _isPdf.asStateFlow()

        private val _isUploading = MutableStateFlow(false)
        val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

        private val _uploadStatus = MutableStateFlow(UploadStatus.IDLE)
        val uploadStatus: StateFlow<UploadStatus> = _uploadStatus.asStateFlow()

        private val _errorEvent = MutableSharedFlow<String>()
        val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

        private var cachedTripDetail: TripDetail? = null

        init {
            fetchTripDetail()
        }

        private fun fetchTripDetail(forceRefresh: Boolean = false) {
            viewModelScope.launch(Dispatchers.IO) {
                if (cachedTripDetail == null) {
                    _uiState.value = UiState.Loading
                }

                fetchTripDetailUseCase(FetchTripDetailParams(tripId, forceRefresh))
                    .onSuccess { tripDetail: TripDetail ->
                        cachedTripDetail = tripDetail
                        _uiState.value = UiState.Success(tripDetail)
                    }
                    .onFailure { error: Throwable ->
                        val message = toDisplayMessage(error, "Failed to load trip details")
                        _errorEvent.emit(message)
                        cachedTripDetail?.let { _uiState.value = UiState.Success(it) }
                            ?: run { _uiState.value = UiState.Error("Unable to load trip details") }
                    }
            }
        }

        fun retry() {
            fetchTripDetail(forceRefresh = true)
        }

        fun updateTripStatus(status: String) {
            viewModelScope.launch(Dispatchers.IO) {
                updateTripStatusUseCase(
                    com.freight24.trucker.feature.trips.domain.usecase.UpdateTripStatusParams(
                        tripId,
                        status,
                    ),
                )
                    .onSuccess { tripDetail ->
                        cachedTripDetail = tripDetail
                        _uiState.value = UiState.Success(tripDetail)
                    }
                    .onFailure { error ->
                        _errorEvent.emit(toDisplayMessage(error, "Failed to update status"))
                    }
            }
        }

        fun onFilesPicked(uris: List<Uri>, pdf: Boolean) {
            _selectedUris.value = uris
            _isPdf.value = pdf
        }

        fun clearSelection() {
            _selectedUris.value = emptyList()
            _isPdf.value = false
        }

        fun uploadLR() {
            val uris = _selectedUris.value
            if (uris.isEmpty()) return
            val isPdfSelection = _isPdf.value

            viewModelScope.launch(Dispatchers.IO) {
                _isUploading.value = true
                _uploadStatus.value = UploadStatus.UPLOADING

                try {
                    val file = if (isPdfSelection) {
                        FileUtils.getFileFromUri(context, uris.first())
                    } else {
                        FileUtils.convertImageToPdf(context, uris)
                    }

                    if (file == null || !file.exists() || file.length() == 0L) {
                        _uploadStatus.value = UploadStatus.ERROR
                        _errorEvent.emit("Failed to process file. Please try again.")
                        return@launch
                    }

                    val result = uploadLrUseCase(
                        UploadLrUseCaseDataParams(
                            tripId = tripId,
                            file = file,
                            notes = "LR uploaded via Trucker App",
                        ),
                    )

                    result.onSuccess { tripDetail ->
                        cachedTripDetail = tripDetail
                        _uiState.value = UiState.Success(tripDetail)
                        clearSelection()
                        _uploadStatus.value = UploadStatus.IDLE
                    }.onFailure { error ->
                        _uploadStatus.value = UploadStatus.ERROR
                        _errorEvent.emit(toDisplayMessage(error, "Upload failed"))
                    }
                } catch (e: Exception) {
                    _uploadStatus.value = UploadStatus.ERROR
                    _errorEvent.emit(toDisplayMessage(e, "An error occurred during upload"))
                } finally {
                    _isUploading.value = false
                }
            }
        }

        fun fetchSignedUrl(documentId: String, onUrlReady: (String) -> Unit) {
            viewModelScope.launch(Dispatchers.IO) {
                getSignedDocumentUseCase(GetSignedDocumentParams(tripId, documentId))
                    .onSuccess { url ->
                        onUrlReady(url)
                    }
                    .onFailure { error ->
                        _errorEvent.emit(toDisplayMessage(error, "Failed to get document URL"))
                    }
            }
        }

        fun resetUploadError() {
            if (_uploadStatus.value == UploadStatus.ERROR) {
                _uploadStatus.value = UploadStatus.IDLE
            }
        }

        private fun toDisplayMessage(error: Throwable, fallback: String): String {
            val raw = error.message.orEmpty()
            val regex = Regex("\\\"message\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"")
            val match = regex.find(raw)?.groupValues?.getOrNull(1)
            if (!match.isNullOrBlank()) return match

            val withoutHttpPrefix = raw.substringAfter("error ", raw).trim()
            if (withoutHttpPrefix.startsWith("{")) return fallback
            return withoutHttpPrefix.ifBlank { fallback }
        }
    }
