package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.compose.runtime.Immutable
import com.freight24.rest.response.ClientBranchV2Dto
import com.freight24.rest.response.ClientContactV2Dto
import com.freight24.connect.domain.model.ClientPickerOption
import com.freight24.rest.response.LocationDto
import com.freight24.rest.response.MaterialDto
import com.freight24.rest.response.TruckCategoryDto
import com.freight24.rest.response.TruckTypeDto
import kotlinx.coroutines.flow.StateFlow

/**
 * The four contact roles an indent carries. Mirrors the backend's
 * `indent_contacts.contact_type` enum and the `contacts` request object.
 *
 * [PLACEMENT] is **unconditionally required** by `POST /api/v1/indents/groups`:
 * the backend destructures `contacts = {}` and then rejects a missing placement
 * with a 400. The other three are optional.
 */
enum class IndentContactRole(
    val wireKey: String,
    val label: String,
    val required: Boolean,
) {
    PLACEMENT("placement", "Placement", true),
    PAYMENT("payment", "Payment", false),
    OPERATIONS("operations", "Operations", false),
    POD("pod", "POD", false),
}

/**
 * Flooring the form shows when nothing has been chosen, and the value Create
 * submits (lowercased) when the operator leaves it alone.
 *
 * Create has always written this, so an indent whose `flooring_type` is null was
 * raised before flooring was captured — it is implicitly steel, not unknown.
 * Edit prefills the same value for that reason, and compares against it rather
 * than against `null` so the prefill does not read as an edit.
 */
const val DEFAULT_FLOORING_TYPE = "STEEL"

interface IndentsViewModel {
    val uiState: StateFlow<CreateIndentUiState>

    fun onEvent(event: CreateIndentEvent)
}

@Immutable
data class CreateIndentUiState(
    val fromLocationQuery: String = "",
    val fromLocationSearchResults: List<LocationDto> = emptyList(),
    val selectedFromLocation: LocationDto? = null,
    val toLocationQuery: String = "",
    val toLocationSearchResults: List<LocationDto> = emptyList(),
    val selectedToLocation: LocationDto? = null,
    val clientQuery: String = "",
    val clientSearchResults: List<ClientPickerOption> = emptyList(),
    val selectedClient: ClientPickerOption? = null,
    // --- Client branch + contact roles (backend commits 1030f9c / aeee64a) ---
    val branches: List<ClientBranchV2Dto> = emptyList(),
    val isLoadingBranches: Boolean = false,
    val selectedBranch: ClientBranchV2Dto? = null,
    val contacts: List<ClientContactV2Dto> = emptyList(),
    val isLoadingContacts: Boolean = false,
    /**
     * True when the branch/contact lookup failed or came back empty. Drives the
     * "this client has no contacts yet" state, which is the one thing that can
     * hard-block indent creation.
     */
    val contactsUnavailable: Boolean = false,
    val placementContact: ClientContactV2Dto? = null,
    val paymentContact: ClientContactV2Dto? = null,
    val operationsContact: ClientContactV2Dto? = null,
    val podContact: ClientContactV2Dto? = null,
    val materialName: String = "",
    val weight: String = "",
    val truckTypes: TruckCategoryDto =
        TruckCategoryDto(
            emptyList(),
            emptyList(),
            emptyList(),
        ),
    val isLoadingTruckTypes: Boolean = false,
    val selectedTruckType: TruckTypeDto? = null,
    /**
     * Cargo categories for the material picker, straight from the backend.
     *
     * Empty until the load lands. The picker still accepts a typed value while
     * this is empty, so a failed materials call degrades to the old free-text
     * behaviour instead of blocking the indent.
     */
    val materials: List<MaterialDto> = emptyList(),
    val isLoadingMaterials: Boolean = false,
    // Optional
    val clientCost: String = "",
    val negotiatedCost: String = "",
    val businessProposedCost: String = "",
    val vehicleHeight: String = "",
    val flooringType: String = DEFAULT_FLOORING_TYPE,
    val tatRequired: Boolean = false,
    val tatHours: String = "",
    val tarpaulinRequired: Boolean = false,
    val hideCost: Boolean = false,
    val remarks: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val createdIndentId: String? = null,
    val createdIndentCode: String? = null,
    val createdDate: String? = null,
) {
    /** Contact currently bound to [role]. */
    fun contactFor(role: IndentContactRole): ClientContactV2Dto? =
        when (role) {
            IndentContactRole.PLACEMENT -> placementContact
            IndentContactRole.PAYMENT -> paymentContact
            IndentContactRole.OPERATIONS -> operationsContact
            IndentContactRole.POD -> podContact
        }

    /** True while the client's branch/contact directory is still loading. */
    val isLoadingClientDirectory: Boolean
        get() = isLoadingBranches || isLoadingContacts

    /**
     * The subset of this state that survives the process being killed while the
     * form sits in the background.
     *
     * Only what the operator typed or picked is carried across. Search results,
     * loading flags, the branch/contact lists and the catalog lists are all
     * deliberately left out: they are refetched on return (see
     * [IndentsViewModelImpl.loadClientDirectory]) and carrying them would bloat
     * the instance-state bundle that is written on every keystroke. Transient
     * outcomes — errors, the submitting flag, the post-submit receipt — are left
     * out for a different reason: replaying a stale error or a stale receipt on
     * return is worse than starting clean.
     */
    fun toDraft(): CreateIndentDraft =
        CreateIndentDraft(
            fromLocationQuery = fromLocationQuery,
            selectedFromLocation = selectedFromLocation,
            toLocationQuery = toLocationQuery,
            selectedToLocation = selectedToLocation,
            clientQuery = clientQuery,
            selectedClient = selectedClient,
            selectedBranch = selectedBranch,
            placementContact = placementContact,
            paymentContact = paymentContact,
            operationsContact = operationsContact,
            podContact = podContact,
            materialName = materialName,
            weight = weight,
            selectedTruckType = selectedTruckType,
            clientCost = clientCost,
            negotiatedCost = negotiatedCost,
            businessProposedCost = businessProposedCost,
            vehicleHeight = vehicleHeight,
            flooringType = flooringType,
            tatRequired = tatRequired,
            tatHours = tatHours,
            tarpaulinRequired = tarpaulinRequired,
            hideCost = hideCost,
            remarks = remarks,
        )
}

/**
 * Serialized form of [CreateIndentUiState] — see [CreateIndentUiState.toDraft].
 *
 * Every field is nullable or defaulted so that a draft written by an older
 * build still decodes after fields are added or removed.
 */
@kotlinx.serialization.Serializable
data class CreateIndentDraft(
    val fromLocationQuery: String = "",
    val selectedFromLocation: LocationDto? = null,
    val toLocationQuery: String = "",
    val selectedToLocation: LocationDto? = null,
    val clientQuery: String = "",
    val selectedClient: ClientPickerOption? = null,
    val selectedBranch: ClientBranchV2Dto? = null,
    val placementContact: ClientContactV2Dto? = null,
    val paymentContact: ClientContactV2Dto? = null,
    val operationsContact: ClientContactV2Dto? = null,
    val podContact: ClientContactV2Dto? = null,
    val materialName: String = "",
    val weight: String = "",
    val selectedTruckType: TruckTypeDto? = null,
    val clientCost: String = "",
    val negotiatedCost: String = "",
    val businessProposedCost: String = "",
    val vehicleHeight: String = "",
    val flooringType: String = DEFAULT_FLOORING_TYPE,
    val tatRequired: Boolean = false,
    val tatHours: String = "",
    val tarpaulinRequired: Boolean = false,
    val hideCost: Boolean = false,
    val remarks: String = "",
)

/** Applies a restored [draft] over a fresh [CreateIndentUiState]. */
fun CreateIndentUiState.withDraft(draft: CreateIndentDraft): CreateIndentUiState =
    copy(
        fromLocationQuery = draft.fromLocationQuery,
        selectedFromLocation = draft.selectedFromLocation,
        toLocationQuery = draft.toLocationQuery,
        selectedToLocation = draft.selectedToLocation,
        clientQuery = draft.clientQuery,
        selectedClient = draft.selectedClient,
        selectedBranch = draft.selectedBranch,
        placementContact = draft.placementContact,
        paymentContact = draft.paymentContact,
        operationsContact = draft.operationsContact,
        podContact = draft.podContact,
        materialName = draft.materialName,
        weight = draft.weight,
        selectedTruckType = draft.selectedTruckType,
        clientCost = draft.clientCost,
        negotiatedCost = draft.negotiatedCost,
        businessProposedCost = draft.businessProposedCost,
        vehicleHeight = draft.vehicleHeight,
        flooringType = draft.flooringType,
        tatRequired = draft.tatRequired,
        tatHours = draft.tatHours,
        tarpaulinRequired = draft.tarpaulinRequired,
        hideCost = draft.hideCost,
        remarks = draft.remarks,
    )

sealed class CreateIndentEvent {
    data class SearchFromLocation(val query: String) : CreateIndentEvent()

    data class SearchToLocation(val query: String) : CreateIndentEvent()

    data class SearchClient(val query: String) : CreateIndentEvent()

    data class SelectFromLocation(val location: LocationDto) : CreateIndentEvent()

    data class SelectToLocation(val location: LocationDto) : CreateIndentEvent()

    data class SelectClient(val client: ClientPickerOption) : CreateIndentEvent()

    /** `null` clears the branch back to "not set" (the backend allows null). */
    data class SelectBranch(val branch: ClientBranchV2Dto?) : CreateIndentEvent()

    /** `null` clears the role. Placement can be cleared then re-set, but not omitted at submit. */
    data class SelectContact(
        val role: IndentContactRole,
        val contact: ClientContactV2Dto?,
    ) : CreateIndentEvent()

    data class UpdateMaterialName(val name: String) : CreateIndentEvent()

    data class UpdateWeight(val weight: String) : CreateIndentEvent()

    data class SelectTruckType(val truckType: TruckTypeDto) : CreateIndentEvent()

    data class UpdateOptionalField(
        val clientCost: String? = null,
        val negotiatedCost: String? = null,
        val businessProposedCost: String? = null,
        val vehicleHeight: String? = null,
        val flooringType: String? = null,
        val tatRequired: Boolean? = null,
        val tarpaulinRequired: Boolean? = null,
        val hideCost: Boolean? = null,
        val remarks: String? = null,
        val tatHours: String? = null,
    ) : CreateIndentEvent()

    object SubmitIndent : CreateIndentEvent()

    object ClearError : CreateIndentEvent()

    object ClearSuccess : CreateIndentEvent()
}
