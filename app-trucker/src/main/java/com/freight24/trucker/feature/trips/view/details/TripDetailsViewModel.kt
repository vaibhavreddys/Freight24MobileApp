package com.freight24.trucker.feature.trips.view.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.model.TripDetail
import com.freight24.trucker.feature.trips.domain.usecase.FetchTripDetailParams
import com.freight24.trucker.feature.trips.domain.usecase.FetchTripDetailUseCase
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.freight24.trucker.feature.trips.domain.usecase.GetSignedDocumentUseCase
import com.freight24.trucker.feature.trips.domain.usecase.GetSignedDocumentUseCaseImpl
import com.freight24.trucker.feature.trips.domain.usecase.UploadLrUseCase
import com.freight24.trucker.feature.trips.domain.usecase.UploadLrUseCaseDataParams
import com.freight24.trucker.feature.trips.domain.usecase.GetSignedDocumentParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
internal class TripDetailsViewModel @Inject constructor(
    private val fetchTripDetailUseCase: FetchTripDetailUseCase,
    private val updateTripStatusUseCase: com.freight24.trucker.feature.trips.domain.usecase.UpdateTripStatusUseCase,
    private val uploadLrUseCase: UploadLrUseCase,
    private val getSignedDocumentUseCase: GetSignedDocumentUseCase,
    @ApplicationContext private val context: Context,
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

    init {
        fetchTripDetail()
    }

    private fun fetchTripDetail(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            fetchTripDetailUseCase(FetchTripDetailParams(tripId, forceRefresh))
                .onSuccess { tripDetail: TripDetail ->
                    _uiState.value = UiState.Success(tripDetail)
                }
                .onFailure { error: Throwable ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to load trip details")
                }
        }
    }

    fun retry() {
        fetchTripDetail(forceRefresh = true)
    }

    fun updateTripStatus(status: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            updateTripStatusUseCase(
                com.freight24.trucker.feature.trips.domain.usecase.UpdateTripStatusParams(
                    tripId,
                    status
                )
            )
                .onSuccess { tripDetail ->
                    _uiState.value = UiState.Success(tripDetail)
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to update status")
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

        viewModelScope.launch {
            _isUploading.value = true
            
            try {
                val file = if (_isPdf.value) {
                    getFileFromUri(uris.first())
                } else {
                    convertImagesToPdf(uris)
                }

                if (file != null) {
                    val result = uploadLrUseCase(
                        UploadLrUseCaseDataParams(
                            tripId = tripId,
                            file = file,
                            notes = "LR uploaded via Trucker App"
                        )
                    )

                    result.onSuccess { tripDetail ->
                        _isUploading.value = false
                        clearSelection()
                        _uiState.value = UiState.Success(tripDetail)
                    }.onFailure { error ->
                        _isUploading.value = false
                        _uiState.value = UiState.Error(error.message ?: "Upload failed")
                    }
                } else {
                    _isUploading.value = false
                    _uiState.value = UiState.Error("Failed to process file")
                }
            } catch (e: Exception) {
                _isUploading.value = false
                _uiState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    private suspend fun getFileFromUri(uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_lr.pdf")
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun convertImagesToPdf(uris: List<Uri>): File? = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            uris.forEachIndexed { index, uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
                    val page = pdfDocument.startPage(pageInfo)
                    page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                    pdfDocument.finishPage(page)
                    bitmap.recycle()
                }
                inputStream?.close()
            }

            val file = File(context.cacheDir, "temp_lr_converted.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            null
        }
    }

    fun fetchSignedUrl(documentId: String, onUrlReady: (String) -> Unit) {
        viewModelScope.launch {
            getSignedDocumentUseCase(GetSignedDocumentParams(tripId, documentId))
                .onSuccess { url ->
                    onUrlReady(url)
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to get document URL")
                }
        }
    }
}
