package com.freight24.connect.feature.trips.domain.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.trips.domain.usecase.SignedDocumentUseCase
import com.freight24.connect.feature.trips.domain.usecase.SignedDocumentUseCaseDataParams
import com.freight24.connect.feature.trips.domain.usecase.VerifyLrUseCase
import com.freight24.connect.feature.trips.domain.usecase.VerifyLrUseCaseDataParams
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
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
    val shouldNavigateBack: Boolean = false
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
internal class PdfViewerViewModel @Inject constructor(
    private val signedDocumentUseCase: SignedDocumentUseCase,
    private val verifyLrUseCase: VerifyLrUseCase,
    @param:ApplicationContext private val context: Context,
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
            PdfViewerEvent.ShowApproveDialog -> _uiState.update { it.copy(showApproveDialog = true, remarksInput = "") }
            PdfViewerEvent.ShowRejectDialog -> _uiState.update { it.copy(showRejectDialog = true, remarksInput = "") }
            PdfViewerEvent.DismissDialog -> _uiState.update { it.copy(showApproveDialog = false, showRejectDialog = false, remarksInput = "") }
            is PdfViewerEvent.UpdateRemarks -> _uiState.update { it.copy(remarksInput = event.remarks) }
            PdfViewerEvent.ApproveLr -> approveLr()
            PdfViewerEvent.RejectLr -> rejectLr()
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
                    downloadPdf(signedDoc.data.signedUrl)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Failed to load document") }
                }
            )
        }
    }
    
    private fun downloadPdf(url: String) {
        viewModelScope.launch {
            try {
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.connect()
                    
                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        throw Exception("Server returned HTTP ${connection.responseCode} ${connection.responseMessage}")
                    }
                    
                    val inputStream = connection.inputStream
                    val file = File(context.cacheDir, "temp_pdf_${System.currentTimeMillis()}.pdf")
                    val outputStream = FileOutputStream(file)
                    
                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    _uiState.update { it.copy(isLoading = false, pdfFile = file) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to download PDF: ${e.message}") }
            }
        }
    }

    private fun approveLr() {
        viewModelScope.launch {
            _uiState.update { it.copy(isApproving = true, errorMessage = null, showApproveDialog = false) }
            val result = verifyLrUseCase(
                VerifyLrUseCaseDataParams(
                    tripId = tripId,
                    status = "verified",
                    remarks = _uiState.value.remarksInput
                )
            )
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isApproving = false, isApproved = true, documentStatus = "approved", remarksInput = "", shouldNavigateBack = true) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isApproving = false, errorMessage = error.message ?: "Approval failed") }
                }
            )
        }
    }

    private fun rejectLr() {
        viewModelScope.launch {
            _uiState.update { it.copy(isApproving = true, errorMessage = null, showRejectDialog = false) }
            val result = verifyLrUseCase(
                VerifyLrUseCaseDataParams(
                    tripId = tripId,
                    status = "rejected",
                    remarks = _uiState.value.remarksInput
                )
            )
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isApproving = false, isApproved = false, documentStatus = "rejected", remarksInput = "", shouldNavigateBack = true) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isApproving = false, errorMessage = error.message ?: "Approval failed") }
                }
            )
        }
    }
}
