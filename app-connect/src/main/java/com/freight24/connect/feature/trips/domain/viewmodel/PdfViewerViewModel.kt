package com.freight24.connect.feature.trips.domain.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.trips.domain.usecase.LoadTripDocumentFile
import com.freight24.connect.feature.trips.domain.usecase.TripDetailsUseCase
import com.freight24.connect.feature.trips.domain.usecase.TripDetailsUseCaseDataParams
import com.freight24.connect.feature.trips.domain.usecase.VerifyLrUseCase
import com.freight24.connect.feature.trips.domain.usecase.VerifyLrUseCaseDataParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class PdfViewerUiState(
    val isLoading: Boolean = false,
    val pdfFile: File? = null,
    val errorMessage: String? = null,
    val documentStatus: String? = null,
    val isApproving: Boolean = false,
    val isApproved: Boolean = false,
    val remarks: String? = null,
    val showApproveDialog: Boolean = false,
    val showRejectDialog: Boolean = false,
    val remarksInput: String = "",
    val shouldNavigateBack: Boolean = false,
    val clientName: String? = null,
    val isLoadingClient: Boolean = true,
    val documentType: String = "lr",
)

sealed class PdfViewerEvent {
    data object LoadPdf : PdfViewerEvent()

    data object ShowApproveDialog : PdfViewerEvent()

    data object ShowRejectDialog : PdfViewerEvent()

    data object DismissDialog : PdfViewerEvent()

    data class UpdateRemarks(val remarks: String) : PdfViewerEvent()

    data object ApproveLr : PdfViewerEvent()

    data object RejectLr : PdfViewerEvent()
}

@HiltViewModel
internal class PdfViewerViewModel
    @Inject
    constructor(
        private val loadTripDocumentFile: LoadTripDocumentFile,
        private val verifyLrUseCase: VerifyLrUseCase,
        private val tripDetailsUseCase: TripDetailsUseCase,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val tripId: String = checkNotNull(savedStateHandle["tripId"])
        private val documentId: String = checkNotNull(savedStateHandle["documentId"])
        private val initialStatus: String? = savedStateHandle["status"]
        private val localPath: String? = savedStateHandle["localPath"]

        private val _uiState =
            MutableStateFlow(
                PdfViewerUiState(
                    documentStatus = initialStatus,
                    documentType = savedStateHandle.get<String>("documentType") ?: "lr",
                ),
            )
        val uiState: StateFlow<PdfViewerUiState> = _uiState.asStateFlow()

        init {
            loadClient()
            if (!localPath.isNullOrBlank() && localPath != "null") {
                val file = File(localPath)
                if (file.exists()) {
                    _uiState.update { it.copy(pdfFile = file, isLoading = false) }
                } else {
                    loadPdf()
                }
            } else {
                loadPdf()
            }
        }

        fun onEvent(event: PdfViewerEvent) {
            when (event) {
                PdfViewerEvent.LoadPdf -> {
                    loadClient()
                    loadPdf()
                }
                PdfViewerEvent.ShowApproveDialog ->
                    if (isLrDocument) {
                        _uiState.update {
                            it.copy(
                                showApproveDialog = true,
                                remarksInput = "",
                            )
                        }
                    }
                PdfViewerEvent.ShowRejectDialog -> if (isLrDocument) _uiState.update { it.copy(showRejectDialog = true, remarksInput = "") }
                PdfViewerEvent.DismissDialog ->
                    _uiState.update {
                        it.copy(
                            showApproveDialog = false,
                            showRejectDialog = false,
                            remarksInput = "",
                        )
                    }
                is PdfViewerEvent.UpdateRemarks -> _uiState.update { it.copy(remarksInput = event.remarks) }
                PdfViewerEvent.ApproveLr -> if (isLrDocument) approveLr()
                PdfViewerEvent.RejectLr -> if (isLrDocument) rejectLr()
            }
        }

        private val isLrDocument: Boolean
            get() = _uiState.value.documentType.equals("lr", ignoreCase = true)

        private fun loadClient() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoadingClient = true) }
                val result = tripDetailsUseCase(TripDetailsUseCaseDataParams(tripId))
                val client = result.getOrNull()?.client
                val clientName =
                    sequenceOf(client?.displayName, client?.companyName, client?.name)
                        .mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) }
                        .firstOrNull()
                _uiState.update { it.copy(clientName = clientName, isLoadingClient = false) }
            }
        }

        private fun loadPdf() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                loadTripDocumentFile(tripId, documentId).fold(
                    onSuccess = { file ->
                        _uiState.update { it.copy(isLoading = false, pdfFile = file) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Failed to load document") }
                    },
                )
            }
        }

        private fun approveLr() {
            viewModelScope.launch {
                _uiState.update { it.copy(isApproving = true, errorMessage = null, showApproveDialog = false) }
                val result =
                    verifyLrUseCase(
                        VerifyLrUseCaseDataParams(
                            tripId = tripId,
                            status = "verified",
                            remarks = _uiState.value.remarksInput,
                        ),
                    )
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isApproving = false,
                                isApproved = true,
                                documentStatus = "approved",
                                remarksInput = "",
                                shouldNavigateBack = true,
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isApproving = false, errorMessage = error.message ?: "Approval failed") }
                    },
                )
            }
        }

        private fun rejectLr() {
            viewModelScope.launch {
                _uiState.update { it.copy(isApproving = true, errorMessage = null, showRejectDialog = false) }
                val result =
                    verifyLrUseCase(
                        VerifyLrUseCaseDataParams(
                            tripId = tripId,
                            status = "rejected",
                            remarks = _uiState.value.remarksInput,
                        ),
                    )
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isApproving = false,
                                isApproved = false,
                                documentStatus = "rejected",
                                remarksInput = "",
                                shouldNavigateBack = true,
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isApproving = false, errorMessage = error.message ?: "Approval failed") }
                    },
                )
            }
        }
    }
