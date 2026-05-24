package com.freight24.connect.feature.trips.domain.viewmodel

import android.net.Uri
import kotlinx.coroutines.flow.StateFlow
import java.io.File

data class LuChargesUiState(
    val tripId: String = "",
    val loadingAmount: String = "",
    val unloadingAmount: String = "",
    val notes: String = "",
    val loadingDocument: File? = null,
    val unloadingDocument: File? = null,
    val loadingSlipUrl: String? = null,
    val unloadingSlipUrl: String? = null,
    val loadingId: String? = null,
    val unloadingId: String? = null,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val shouldNavigateBack: Boolean = false,
    val navigateToSignedUrl: String? = null,
    val showSourcePicker: Boolean = false,
    val showFileConfirmation: Boolean = false,
    val selectedUris: List<Uri> = emptyList(),
    val isPdf: Boolean = false,
    val selectedDocType: LuDocType? = null,
    val loadingProcessing: Boolean = false,
    val unloadingProcessing: Boolean = false,
    val loadingShowReupload: Boolean = false,
    val unloadingShowReupload: Boolean = false,

    // Initial values for comparison
    val initialLoadingAmount: String = "",
    val initialUnloadingAmount: String = "",
    val initialNotes: String = "",
    val hasChanges: Boolean = false
)

sealed class LuChargesEvent {
    data class OnLoadingAmountChange(val value: String) : LuChargesEvent()
    data class OnUnloadingAmountChange(val value: String) : LuChargesEvent()
    data class OnNotesChange(val value: String) : LuChargesEvent()
    data class OnShowSourcePicker(val docType: LuDocType) : LuChargesEvent()
    data class OnFilePicked(val uris: List<Uri>, val isPdf: Boolean) : LuChargesEvent()
    data object OnConfirmUpload : LuChargesEvent()
    data object OnClearSelection : LuChargesEvent()
    data class OnDeleteDoc(val type: LuDocType) : LuChargesEvent()
    data class OnRemoteDocClick(val docType: String) : LuChargesEvent()
    data object OnSubmit : LuChargesEvent()
    data object OnDismissSourcePicker : LuChargesEvent()
    data object OnSignedUrlConsumed : LuChargesEvent()
}

enum class LuDocType {
    LOADING, UNLOADING
}

interface LuChargesViewModel {
    val uiState: StateFlow<LuChargesUiState>
    fun onEvent(event: LuChargesEvent)
}
