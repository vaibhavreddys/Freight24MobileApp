package com.freight24.connect.feature.trips.domain.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.trips.domain.model.LuChargeRequest
import com.freight24.connect.feature.trips.domain.usecase.SlipUrlSignedDocumentUseCase
import com.freight24.connect.feature.trips.domain.usecase.SlipUrlSignedDocumentUseCaseDataParams
import com.freight24.connect.feature.trips.domain.usecase.UploadLUChargesUseCase
import com.freight24.connect.feature.trips.domain.usecase.UploadLUChargesUseCaseParams
import com.freight24.connect.navigation.ConnectDestination
import com.freight24.connect.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class LuChargesViewModelImpl @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val uploadLUChargesUseCase: UploadLUChargesUseCase,
    private val slipUrlSignedDocumentUseCase: SlipUrlSignedDocumentUseCase,
    @param:ApplicationContext private val context: Context
) : ViewModel(), LuChargesViewModel {

    private val _uiState = MutableStateFlow(LuChargesUiState())
    override val uiState: StateFlow<LuChargesUiState> = _uiState.asStateFlow()

    init {
        val tripId = savedStateHandle.get<String>(ConnectDestination.LuCharges.TRIP_ID_ARG) ?: ""
        val loadingAmount = savedStateHandle.get<String>(ConnectDestination.LuCharges.LOADING_AMOUNT_ARG)?.takeIf { it != "null" && it != "0.0" } ?: ""
        val unloadingAmount = savedStateHandle.get<String>(ConnectDestination.LuCharges.UNLOADING_AMOUNT_ARG)?.takeIf { it != "null" && it != "0.0" } ?: ""
        val loadingSlipUrl = savedStateHandle.get<String>(ConnectDestination.LuCharges.LOADING_SLIP_URL_ARG)?.takeIf { it != "null" && it.isNotBlank() }
        val unloadingSlipUrl = savedStateHandle.get<String>(ConnectDestination.LuCharges.UNLOADING_SLIP_URL_ARG)?.takeIf { it != "null" && it.isNotBlank() }
        val loadingId = savedStateHandle.get<String>(ConnectDestination.LuCharges.LOADING_ID)?.takeIf { it != "null" && it.isNotBlank() }
        val unloadingId = savedStateHandle.get<String>(ConnectDestination.LuCharges.UNLOADING_ID)?.takeIf { it != "null" && it.isNotBlank() }
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
            ) 
        }
    }

    override fun onEvent(event: LuChargesEvent) {
        when (event) {
            is LuChargesEvent.OnLoadingAmountChange -> _uiState.update { it.copy(loadingAmount = event.value) }
            is LuChargesEvent.OnUnloadingAmountChange -> _uiState.update { it.copy(unloadingAmount = event.value) }
            is LuChargesEvent.OnNotesChange -> _uiState.update { it.copy(notes = event.value) }
            LuChargesEvent.OnUploadClick -> _uiState.update { it.copy(showDocTypeSheet = true) }
            is LuChargesEvent.OnDocTypeSelected -> _uiState.update { 
                it.copy(selectedDocType = event.type, showDocTypeSheet = false, showFilePicker = true) 
            }
            is LuChargesEvent.OnFilePicked -> handleFilePicked(event.uris, event.isPdf)
            is LuChargesEvent.OnRemoteDocClick -> handleRemoteDocClick(event.docType)
            is LuChargesEvent.OnDeleteDoc -> {
                when (event.type) {
                    LuDocType.LOADING -> _uiState.update { it.copy(loadingDocument = null, loadingSlipUrl = null, loadingId = null) }
                    LuDocType.UNLOADING -> _uiState.update { it.copy(unloadingDocument = null, unloadingSlipUrl = null, unloadingId = null) }
                }
            }
            LuChargesEvent.OnSubmit -> submitCharges()
            LuChargesEvent.OnDismissSheet -> _uiState.update { it.copy(showDocTypeSheet = false, showFilePicker = false) }
            LuChargesEvent.OnSignedUrlConsumed -> _uiState.update { it.copy(navigateToSignedUrl = null) }
        }
    }

    private fun handleFilePicked(uris: List<Uri>, isPdf: Boolean) {
        viewModelScope.launch {
            val file = if (isPdf) {
                FileUtils.getFileFromUri(context, uris.first())
            } else {
                FileUtils.convertImageToPdf(context, uris)
            }
            
            _uiState.update { currentState ->
                currentState.copy(
                    showFilePicker = false,
                    loadingDocument = if (currentState.selectedDocType == LuDocType.LOADING) file else currentState.loadingDocument,
                    unloadingDocument = if (currentState.selectedDocType == LuDocType.UNLOADING) file else currentState.unloadingDocument
                )
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
            val params = SlipUrlSignedDocumentUseCaseDataParams(
                tripId = state.tripId,
                chargeId = chargeId
            )
            
            slipUrlSignedDocumentUseCase(params).onSuccess { signedDoc ->
                _uiState.update { 
                    it.copy(
                        isSubmitting = false, 
                        navigateToSignedUrl = signedDoc.data.signedUrl 
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
            val request = LuChargeRequest(
                loadingAmount = state.loadingAmount.toDoubleOrNull(),
                loadingPaymentMethod = "trucker_pays",
                loadingReimbursable = true,
                unloadingAmount = state.unloadingAmount.toDoubleOrNull(),
                unloadingPaymentMethod = "trucker_pays",
                unloadingReimbursable = true,
                notes = state.notes
            )
            
            val params = UploadLUChargesUseCaseParams(
                tripId = state.tripId,
                loadingFile = state.loadingDocument,
                unloadingFile = state.unloadingDocument,
                request = request
            )
            
            uploadLUChargesUseCase(params).onSuccess {
                _uiState.update { it.copy(isSubmitting = false, shouldNavigateBack = true) }
            }.onFailure { error ->
                _uiState.update { it.copy(isSubmitting = false, error = error.message ?: "Update failed") }
            }
        }
    }
}
