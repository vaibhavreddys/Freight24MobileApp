package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.compose.runtime.Immutable
import com.freight24.common.util.negotiatedCostExceedsClientError
import com.freight24.common.util.proposedCostExceedsClientError
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
    val createdIndentCode: String? = null,
    val createdDate: String? = null,
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
    val remarks: String = "",
) {
    /**
     * Why the negotiated cost is unusable, or null when it is fine. Flags live on
     * the field itself rather than only at submit.
     */
    val negotiatedCostError: String?
        get() = negotiatedCostExceedsClientError(negotiatedCost, clientCost)

    /** Same, for the business-proposed cost. */
    val proposedCostError: String?
        get() = proposedCostExceedsClientError(businessProposedCost, clientCost)

    /**
     * The first breach across the rate fields, or null when every one of them is
     * within the client cost. The review button is gated on this so the operator
     * cannot walk into the review screen carrying a rate the backend will refuse.
     */
    val costError: String?
        get() = negotiatedCostError ?: proposedCostError
}

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
        val remarks: String? = null,
    ) : CloneIndentEvent()

    object SubmitClone : CloneIndentEvent()

    object ClearError : CloneIndentEvent()

    object ClearSuccess : CloneIndentEvent()
}
