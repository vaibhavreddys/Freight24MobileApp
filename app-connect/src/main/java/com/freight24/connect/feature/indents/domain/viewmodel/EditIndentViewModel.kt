package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.compose.runtime.Immutable
import com.freight24.rest.response.IndentResponse
import kotlinx.coroutines.flow.StateFlow

interface EditIndentViewModel {
    val uiState: StateFlow<EditIndentUiState>
    fun onEvent(event: EditIndentEvent)
}

@Immutable
data class EditIndentUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val indent: IndentResponse.Indent? = null,
    val hasChanges: Boolean = false,

    // Editable fields
    val clientCost: String = "",
    val negotiatedCost: String = "",
    val businessProposedCost: String = "",
    val vehicleHeight: String = "",
    val flooringType: String = "",
    val tarpaulinRequired: Boolean = false,
    val hideCost: Boolean = false,
    val tatRequired: Boolean = false,
    val tatHours: String = "",
    val remarks: String = ""
)

sealed class EditIndentEvent {
    data class LoadIndent(val indentId: String) : EditIndentEvent()
    data class UpdateField(
        val clientCost: String? = null,
        val negotiatedCost: String? = null,
        val businessProposedCost: String? = null,
        val vehicleHeight: String? = null,
        val flooringType: String? = null,
        val tarpaulinRequired: Boolean? = null,
        val hideCost: Boolean? = null,
        val tatRequired: Boolean? = null,
        val tatHours: String? = null,
        val remarks: String? = null
    ) : EditIndentEvent()
    object SubmitUpdate : EditIndentEvent()
    object ClearError : EditIndentEvent()
    object ClearSuccess : EditIndentEvent()
}
