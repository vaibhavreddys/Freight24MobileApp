package com.freight24.connect.feature.trips.domain.viewmodel

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.trips.domain.usecase.TripDetailsUseCase
import com.freight24.connect.feature.trips.domain.usecase.TripDetailsUseCaseDataParams
import com.freight24.connect.feature.trips.domain.usecase.UpdateTruckerUseCase
import com.freight24.connect.feature.trips.domain.usecase.UpdateTruckerUseCaseDataParams
import com.freight24.connect.feature.trips.domain.usecase.UploadLrUseCase
import com.freight24.connect.feature.trips.domain.usecase.UploadLrUseCaseDataParams
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
internal class TripDetailsViewModelImpl @Inject constructor(
    private val tripDetailsUseCase: TripDetailsUseCase,
    private val updateTruckerUseCase: UpdateTruckerUseCase,
    private val uploadLrUseCase: UploadLrUseCase,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel(), TripDetailsViewModel {

    private val tripId: String = checkNotNull(savedStateHandle["tripId"])

    private val _uiState = MutableStateFlow(TripDetailsUiState())
    override val uiState: StateFlow<TripDetailsUiState> = _uiState.asStateFlow()

    init {
        loadTrip()
    }

    override fun onEvent(event: TripDetailsEvent) {
        when (event) {
            is TripDetailsEvent.LoadTrip -> {
                // Usually tripId is fixed from navigation, but supporting re-load if needed
                loadTrip(event.tripId)
            }
            is TripDetailsEvent.UpdateStatus -> {
                updateStatus(event.newStatus)
            }
            is TripDetailsEvent.OnFilesPicked -> {
                _uiState.update { it.copy(selectedUris = event.uris, isPdf = event.isPdf) }
            }
            TripDetailsEvent.OnClearSelection -> {
                _uiState.update { it.copy(selectedUris = emptyList(), isPdf = false) }
            }
            TripDetailsEvent.UploadLR -> {
                uploadLR()
            }
            TripDetailsEvent.Refresh -> loadTrip()
        }
    }

    private fun updateStatus(newStatus: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = updateTruckerUseCase(
                UpdateTruckerUseCaseDataParams(
                    tripId = tripId,
                    truckerStatus = newStatus,
                    remarks = "Status updated via Mobile App"
                )
            )

            result.fold(
                onSuccess = { response ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            trip = response
                        ) 
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = error.message ?: "Update failed"
                        ) 
                    }
                }
            )
        }
    }

    private fun uploadLR() {
        val uris = _uiState.value.selectedUris
        if (uris.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, errorMessage = null) }
            
            try {
                val file = if (_uiState.value.isPdf) {
                    getFileFromUri(uris.first())
                } else {
                    convertImagesToPdf(uris)
                }

                if (file != null) {
                    val result = uploadLrUseCase(
                        UploadLrUseCaseDataParams(
                            tripId = tripId,
                            file = file,
                            notes = "LR uploaded via Mobile App"
                        )
                    )

                    result.fold(
                        onSuccess = { tripResponse ->
                            _uiState.update { 
                                it.copy(
                                    isUploading = false, 
                                    selectedUris = emptyList(),
                                    isPdf = false,
                                    trip = tripResponse
                                ) 
                            }
                        },
                        onFailure = { error ->
                            _uiState.update { 
                                it.copy(
                                    isUploading = false, 
                                    errorMessage = error.message ?: "Upload failed"
                                ) 
                            }
                        }
                    )
                } else {
                    _uiState.update { it.copy(isUploading = false, errorMessage = "Failed to process file") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploading = false, errorMessage = e.message ?: "An error occurred") }
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

    private fun loadTrip(id: String = tripId) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = tripDetailsUseCase(TripDetailsUseCaseDataParams(tripId = id))

            result.fold(
                onSuccess = { response ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            trip = response
                        ) 
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = error.message ?: "Unknown error"
                        ) 
                    }
                }
            )
        }
    }
}
