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
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val shouldNavigateBack: Boolean = false,
    val showDocTypeSheet: Boolean = false,
    val showFilePicker: Boolean = false,
    val selectedDocType: LuDocType? = null
)

sealed class LuChargesEvent {
    data class OnLoadingAmountChange(val value: String) : LuChargesEvent()
    data class OnUnloadingAmountChange(val value: String) : LuChargesEvent()
    data class OnNotesChange(val value: String) : LuChargesEvent()
    data object OnUploadClick : LuChargesEvent()
    data class OnDocTypeSelected(val type: LuDocType) : LuChargesEvent()
    data class OnFilePicked(val uris: List<Uri>, val isPdf: Boolean) : LuChargesEvent()
    data class OnDeleteDoc(val type: LuDocType) : LuChargesEvent()
    data object OnSubmit : LuChargesEvent()
    data object OnDismissSheet : LuChargesEvent()
}

enum class LuDocType {
    LOADING, UNLOADING
}

interface LuChargesViewModel {
    val uiState: StateFlow<LuChargesUiState>
    fun onEvent(event: LuChargesEvent)
}
