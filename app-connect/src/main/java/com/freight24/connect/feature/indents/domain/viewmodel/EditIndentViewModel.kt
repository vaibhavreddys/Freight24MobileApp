package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.compose.runtime.Immutable
import com.freight24.rest.response.ClientContactV2Dto
import com.freight24.rest.response.IndentResponse
import com.freight24.rest.response.MaterialDto
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
    /**
     * How many fields differ from the indent as loaded. Drives the header tally.
     *
     * All three of these are derived in one place — [EditIndentUiState.withDiff].
     * They used to be duplicated: a copy here driving the save button, another in
     * the screen driving the tally. The two drifted, and the screen's copy
     * reported a phantom edit on open.
     */
    val changedFieldCount: Int = 0,
    /** True when a contact role differs — the only change the group endpoint carries. */
    val contactsChanged: Boolean = false,
    /** True when the commercial fields differ. Colours clause 05's spine. */
    val commercialsChanged: Boolean = false,
    /** True when the term fields differ. Colours clause 06's spine. */
    val termsChanged: Boolean = false,
    /** True when the cargo fields differ. Colours clause 04's spine. */
    val cargoChanged: Boolean = false,
    // Editable fields — accepted by PUT /api/v1/indents/:id
    val clientCost: String = "",
    val negotiatedCost: String = "",
    val businessProposedCost: String = "",
    val vehicleHeight: String = "",
    /**
     * Prefilled with [DEFAULT_FLOORING_TYPE] — an indent that records no flooring
     * is implicitly steel (Create always writes it), so an empty field would
     * misrepresent the indent and force the operator to retype the default.
     * [EditIndentUiState.withDiff] compares against the same default, so the
     * prefill is not counted as an edit.
     */
    val flooringType: String = DEFAULT_FLOORING_TYPE,
    val tarpaulinRequired: Boolean = false,
    val hideCost: Boolean = false,
    val tatRequired: Boolean = false,
    val tatHours: String = "",
    val remarks: String = "",
    /**
     * Cargo. Both are `NOT NULL` on `indent_groups`, so unlike every field
     * above these may not be cleared — [loadWeightError] / [materialError] gate
     * the save rather than letting the server refuse the whole correction.
     */
    val loadWeight: String = "",
    val material: String = "",
    val materials: List<MaterialDto> = emptyList(),
    val isLoadingMaterials: Boolean = false,
    /** Non-null when [loadWeight] cannot be filed as-is. */
    val loadWeightError: String? = null,
    /** Non-null when [material] cannot be filed as-is. */
    val materialError: String? = null,
    // Contact roles — editable only through PUT /api/v1/indents/groups/:id
    val contacts: List<ClientContactV2Dto> = emptyList(),
    val isLoadingContacts: Boolean = false,
    /** True when the client has no active contacts, so no role can be set. */
    val contactsUnavailable: Boolean = false,
    val placementContact: ClientContactV2Dto? = null,
    val paymentContact: ClientContactV2Dto? = null,
    val operationsContact: ClientContactV2Dto? = null,
    val podContact: ClientContactV2Dto? = null,
) {
    /** Contact currently bound to [role]. */
    fun contactFor(role: IndentContactRole): ClientContactV2Dto? =
        when (role) {
            IndentContactRole.PLACEMENT -> placementContact
            IndentContactRole.PAYMENT -> paymentContact
            IndentContactRole.OPERATIONS -> operationsContact
            IndentContactRole.POD -> podContact
        }

    /**
     * The contact id recorded on the indent as loaded, for [role]. Compared
     * against [contactFor] to decide whether the group endpoint needs calling.
     *
     * Falls back to the join-row id when the backend has not populated
     * `contactId`, so a role is never treated as changed purely because of how
     * the indent endpoint serialised it.
     */
    fun originalContactId(role: IndentContactRole): String? =
        indent?.indentGroupDetails?.indentContacts
            ?.firstOrNull { it.contactType.equals(role.name, ignoreCase = true) }
            ?.let { it.contactId ?: it.id }
}

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
        val remarks: String? = null,
        val loadWeight: String? = null,
        val material: String? = null,
    ) : EditIndentEvent()

    /** `null` clears the role. Placement can be cleared then re-set, but not omitted at save. */
    data class SelectContact(
        val role: IndentContactRole,
        val contact: ClientContactV2Dto?,
    ) : EditIndentEvent()

    object SubmitUpdate : EditIndentEvent()

    object ClearError : EditIndentEvent()

    object ClearSuccess : EditIndentEvent()
}
