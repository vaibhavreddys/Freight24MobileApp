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
import com.freight24.common.util.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
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
                    FileUtils.getFileFromUri(context, uris.first())
                } else {
                    FileUtils.convertImageToPdf(context, uris)
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
