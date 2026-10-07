package com.freight24.connect.feature.trips.domain.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.util.FileUtils
import com.freight24.connect.feature.trips.domain.model.LuChargeRequest
import com.freight24.connect.feature.trips.domain.usecase.SlipUrlSignedDocumentUseCase
import com.freight24.connect.feature.trips.domain.usecase.SlipUrlSignedDocumentUseCaseDataParams
import com.freight24.connect.feature.trips.domain.usecase.UploadLUChargesUseCase
import com.freight24.connect.feature.trips.domain.usecase.UploadLUChargesUseCaseParams
import com.freight24.connect.navigation.ConnectDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
internal class LuChargesViewModelImpl
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        private val uploadLUChargesUseCase: UploadLUChargesUseCase,
        private val slipUrlSignedDocumentUseCase: SlipUrlSignedDocumentUseCase,
        @param:ApplicationContext private val context: Context,
    ) : ViewModel(), LuChargesViewModel {
        private val _uiState = MutableStateFlow(LuChargesUiState())
        override val uiState: StateFlow<LuChargesUiState> = _uiState.asStateFlow()

        init {
            val tripId = savedStateHandle.get<String>(ConnectDestination.LuCharges.TRIP_ID_ARG) ?: ""
            val loadingAmount =
                savedStateHandle.get<String>(ConnectDestination.LuCharges.LOADING_AMOUNT_ARG)?.takeIf {
                    it != "null" && it != "0.0"
                } ?: ""
            val unloadingAmount =
                savedStateHandle.get<String>(ConnectDestination.LuCharges.UNLOADING_AMOUNT_ARG)?.takeIf {
                    it != "null" && it != "0.0"
                } ?: ""
            val loadingSlipUrl =
                savedStateHandle.get<String>(ConnectDestination.LuCharges.LOADING_SLIP_URL_ARG)?.takeIf {
                    it != "null" && it.isNotBlank()
                }
            val unloadingSlipUrl =
                savedStateHandle.get<String>(ConnectDestination.LuCharges.UNLOADING_SLIP_URL_ARG)?.takeIf {
                    it != "null" && it.isNotBlank()
                }
            val loadingId =
                savedStateHandle.get<String>(
                    ConnectDestination.LuCharges.LOADING_ID,
                )?.takeIf { it != "null" && it.isNotBlank() }
            val unloadingId =
                savedStateHandle.get<String>(
                    ConnectDestination.LuCharges.UNLOADING_ID,
                )?.takeIf { it != "null" && it.isNotBlank() }
            val notes = savedStateHandle.get<String>(ConnectDestination.LuCharges.NOTES)?.takeIf { it != "null" && it.isNotBlank() } ?: ""

            _uiState.update {
                it.copy(
                    tripId = tripId,
                    loadingAmount = loadingAmount,
                    unloadingAmount = unloadingAmount,
                    loadingSlipUrl = loadingSlipUrl,
                    unloadingSlipUrl = unloadingSlipUrl,
                    loadingId = loadingId,
                    unloadingId = unloadingId,
                    notes = notes,
                    // Set initial values
                    initialLoadingAmount = loadingAmount,
                    initialUnloadingAmount = unloadingAmount,
                    initialNotes = notes,
                    hasChanges = false,
                )
            }
        }

        override fun onEvent(event: LuChargesEvent) {
            when (event) {
                is LuChargesEvent.OnLoadingAmountChange -> _uiState.update { it.copy(loadingAmount = event.value).updateChanges() }
                is LuChargesEvent.OnUnloadingAmountChange -> _uiState.update { it.copy(unloadingAmount = event.value).updateChanges() }
                is LuChargesEvent.OnNotesChange -> _uiState.update { it.copy(notes = event.value).updateChanges() }
                is LuChargesEvent.OnShowSourcePicker ->
                    _uiState.update {
                        it.copy(showSourcePicker = true, selectedDocType = event.docType)
                    }
                is LuChargesEvent.OnFilePicked ->
                    _uiState.update {
                        it.copy(showSourcePicker = false, showFileConfirmation = true, selectedUris = event.uris, isPdf = event.isPdf)
                    }
                LuChargesEvent.OnConfirmUpload -> handleFileProcessing()
                LuChargesEvent.OnClearSelection ->
                    _uiState.update {
                        it.copy(showFileConfirmation = false, selectedUris = emptyList(), selectedDocType = null)
                    }
                is LuChargesEvent.OnDeleteDoc -> {
                    when (event.type) {
                        LuDocType.LOADING ->
                            _uiState.update {
                                it.copy(
                                    loadingDocument = null,
                                    loadingSlipUrl = null,
                                    loadingId = null,
                                    loadingShowReupload = true,
                                ).updateChanges()
                            }
                        LuDocType.UNLOADING ->
                            _uiState.update {
                                it.copy(
                                    unloadingDocument = null,
                                    unloadingSlipUrl = null,
                                    unloadingId = null,
                                    unloadingShowReupload = true,
                                ).updateChanges()
                            }
                    }
                }
                is LuChargesEvent.OnRemoteDocClick -> handleRemoteDocClick(event.docType)
                LuChargesEvent.OnSubmit -> submitCharges()
                LuChargesEvent.OnDismissSourcePicker -> _uiState.update { it.copy(showSourcePicker = false) }
                LuChargesEvent.OnSignedUrlConsumed -> _uiState.update { it.copy(navigateToSignedUrl = null) }
            }
        }

        private fun handleFileProcessing() {
            val state = _uiState.value
            val docType = state.selectedDocType ?: return

            _uiState.update {
                it.copy(
                    showFileConfirmation = false,
                    loadingProcessing = docType == LuDocType.LOADING,
                    unloadingProcessing = docType == LuDocType.UNLOADING,
                )
            }

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val file: File? =
                        if (state.isPdf) {
                            val uri =
                                state.selectedUris.firstOrNull() ?: run {
                                    _uiState.update { it.copy(loadingProcessing = false, unloadingProcessing = false) }
                                    return@launch
                                }
                            FileUtils.getFileFromUri(context, uri)
                        } else {
                            if (state.selectedUris.isEmpty()) {
                                _uiState.update { it.copy(loadingProcessing = false, unloadingProcessing = false) }
                                return@launch
                            }
                            FileUtils.convertImageToPdf(context, state.selectedUris)
                        }

                    if (file != null) {
                        val sizeKb = file.length() / 1024
                        val sizeMb = sizeKb / 1024.0
                        Log.d(
                            "LuCharges",
                            "Converted ${if (docType == LuDocType.LOADING) "loading" else "unloading"} file: ${file.name}, size=${file.length()} bytes ($sizeKb KB / ${"%.1f".format(
                                sizeMb,
                            )} MB)",
                        )
                    }

                    _uiState.update { currentState ->
                        val updated =
                            when (docType) {
                                LuDocType.LOADING ->
                                    currentState.copy(
                                        loadingDocument = file,
                                        loadingShowReupload = false,
                                        loadingProcessing = false,
                                    )
                                LuDocType.UNLOADING ->
                                    currentState.copy(
                                        unloadingDocument = file,
                                        unloadingShowReupload = false,
                                        unloadingProcessing = false,
                                    )
                            }
                        updated.copy(
                            showFileConfirmation = false,
                            selectedUris = emptyList(),
                            selectedDocType = null,
                        ).updateChanges()
                    }
                } catch (error: kotlinx.coroutines.CancellationException) {
                    throw error
                } catch (error: Exception) {
                    _uiState.update { it.copy(error = error.message ?: "Failed to process document") }
                } finally {
                    _uiState.update { it.copy(loadingProcessing = false, unloadingProcessing = false) }
                }
            }
        }

        private fun handleRemoteDocClick(docType: String) {
            val state = _uiState.value
            val chargeId = if (docType == "LOADING_SLIP") state.loadingId else state.unloadingId

            if (chargeId == null) {
                _uiState.update { it.copy(error = "Charge ID not found") }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isSubmitting = true, error = null) }
                val params =
                    SlipUrlSignedDocumentUseCaseDataParams(
                        tripId = state.tripId,
                        chargeId = chargeId,
                    )

                slipUrlSignedDocumentUseCase(params).onSuccess { signedDoc ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            navigateToSignedUrl = signedDoc.data.signedUrl,
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(isSubmitting = false, error = error.message ?: "Failed to get signed URL") }
                }
            }
        }

        private fun submitCharges() {
            val state = _uiState.value
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            viewModelScope.launch {
                val request =
                    LuChargeRequest(
                        loadingAmount = state.loadingAmount.toDoubleOrNull(),
                        loadingPaymentMethod = "trucker_pays",
                        loadingReimbursable = true,
                        unloadingAmount = state.unloadingAmount.toDoubleOrNull(),
                        unloadingPaymentMethod = "trucker_pays",
                        unloadingReimbursable = true,
                        notes = state.notes,
                    )

                val params =
                    UploadLUChargesUseCaseParams(
                        tripId = state.tripId,
                        loadingFile = state.loadingDocument,
                        unloadingFile = state.unloadingDocument,
                        request = request,
                    )

                uploadLUChargesUseCase(params).onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, shouldNavigateBack = true) }
                }.onFailure { error ->
                    _uiState.update { it.copy(isSubmitting = false, error = error.message ?: "Update failed") }
                }
            }
        }

        private fun LuChargesUiState.updateChanges(): LuChargesUiState {
            val hasChanges =
                loadingAmount != initialLoadingAmount ||
                    unloadingAmount != initialUnloadingAmount ||
                    notes != initialNotes ||
                    loadingDocument != null ||
                    unloadingDocument != null
            return copy(hasChanges = hasChanges)
        }
    }
