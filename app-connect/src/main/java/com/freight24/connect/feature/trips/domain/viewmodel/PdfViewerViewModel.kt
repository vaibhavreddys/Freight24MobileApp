package com.freight24.connect.feature.trips.domain.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.trips.domain.usecase.SignedDocumentUseCase
import com.freight24.connect.feature.trips.domain.usecase.SignedDocumentUseCaseDataParams
import com.freight24.connect.feature.trips.domain.usecase.VerifyLrUseCase
import com.freight24.connect.feature.trips.domain.usecase.VerifyLrUseCaseDataParams
import com.freight24.rest.response.TripDataResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PdfViewerUiState(
    val isLoading: Boolean = false,
    val pdfUrl: String? = null,
    val errorMessage: String? = null,
    val documentStatus: String? = null,
    val isApproving: Boolean = false,
    val isApproved: Boolean = false
)

sealed class PdfViewerEvent {
    data object LoadPdf : PdfViewerEvent()
    data object ApproveLr : PdfViewerEvent()
}

@HiltViewModel
internal class PdfViewerViewModel @Inject constructor(
    private val signedDocumentUseCase: SignedDocumentUseCase,
    private val verifyLrUseCase: VerifyLrUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: String = checkNotNull(savedStateHandle["tripId"])
    private val documentId: String = checkNotNull(savedStateHandle["documentId"])
    private val initialStatus: String? = savedStateHandle["status"]

    private val _uiState = MutableStateFlow(PdfViewerUiState(documentStatus = initialStatus))
    val uiState: StateFlow<PdfViewerUiState> = _uiState.asStateFlow()

    init {
        loadPdf()
    }

    fun onEvent(event: PdfViewerEvent) {
        when (event) {
            PdfViewerEvent.LoadPdf -> loadPdf()
            PdfViewerEvent.ApproveLr -> approveLr()
        }
    }

    private fun loadPdf() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = signedDocumentUseCase(
                SignedDocumentUseCaseDataParams(tripId = tripId, documentId = documentId)
            )
            result.fold(
                onSuccess = { signedDoc ->
                    _uiState.update { it.copy(isLoading = false, pdfUrl = signedDoc.data.signedUrl) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Failed to load document") }
                }
            )
        }
    }

    private fun approveLr() {
        viewModelScope.launch {
            _uiState.update { it.copy(isApproving = true, errorMessage = null) }
            val result = verifyLrUseCase(
                VerifyLrUseCaseDataParams(tripId = tripId, documentId = documentId)
            )
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isApproving = false, isApproved = true, documentStatus = "approved") }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isApproving = false, errorMessage = error.message ?: "Approval failed") }
                }
            )
        }
    }
}
