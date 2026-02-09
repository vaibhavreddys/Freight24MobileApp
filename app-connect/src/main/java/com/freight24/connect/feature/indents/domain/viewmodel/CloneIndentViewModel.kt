package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.compose.runtime.Immutable
import com.freight24.rest.response.IndentResponse
import kotlinx.coroutines.flow.StateFlow

interface CloneIndentViewModel {
    val uiState: StateFlow<CloneIndentUiState>
    fun onEvent(event: CloneIndentEvent)
}

@Immutable
data class CloneIndentUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val createdIndentId: String? = null,
    val indent: IndentResponse.Indent? = null,
    
    // Editable fields
    val assignedTo: String = "",
    val notes: String = "",
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

sealed class CloneIndentEvent {
    data class LoadIndent(
        val indentId: String,
        val indentGroupId: String,
    ) : CloneIndentEvent()

    data class UpdateField(
        val assignedTo: String? = null,
        val notes: String? = null,
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
    ) : CloneIndentEvent()
    object SubmitClone : CloneIndentEvent()
    object ClearError : CloneIndentEvent()
    object ClearSuccess : CloneIndentEvent()
}
