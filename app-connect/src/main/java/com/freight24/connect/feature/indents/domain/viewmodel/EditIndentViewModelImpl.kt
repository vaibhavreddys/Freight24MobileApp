package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.util.formatIndianCurrency
import com.freight24.common.util.formatWeightValue
import com.freight24.connect.feature.indents.domain.usecases.GetClientContactsUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetIndentByIdUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetIndentByIdUseCaseParams
import com.freight24.connect.feature.indents.domain.usecases.GetMaterialsUseCase
import com.freight24.connect.feature.indents.domain.usecases.UpdateIndentGroupUseCase
import com.freight24.connect.feature.indents.domain.usecases.UpdateIndentGroupUseCaseParams
import com.freight24.connect.feature.indents.domain.usecases.UpdateIndentUseCase
import com.freight24.connect.feature.indents.domain.usecases.UpdateIndentUseCaseParams
import com.freight24.rest.request.IndentContactsRequest
import com.freight24.rest.request.UpdateIndentGroupRequest
import com.freight24.rest.request.UpdateIndentRequest
import com.freight24.rest.response.ClientContactV2Dto
import com.freight24.rest.response.IndentResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class EditIndentViewModelImpl
    @Inject
    constructor(
        private val getIndentByIdUseCase: GetIndentByIdUseCase,
        private val updateIndentUseCase: UpdateIndentUseCase,
        private val updateIndentGroupUseCase: UpdateIndentGroupUseCase,
        private val getClientContactsUseCase: GetClientContactsUseCase,
        private val getMaterialsUseCase: GetMaterialsUseCase,
    ) : ViewModel(), EditIndentViewModel {
        private val _uiState = MutableStateFlow(EditIndentUiState())
        override val uiState: StateFlow<EditIndentUiState> = _uiState.asStateFlow()

        private var currentIndentId: String? = null

        override fun onEvent(event: EditIndentEvent) {
            when (event) {
                is EditIndentEvent.LoadIndent -> {
                    currentIndentId = event.indentId
                    loadIndent(event.indentId)
                }
                is EditIndentEvent.UpdateField -> {
                    _uiState.update { state ->
                        val next =
                            state.copy(
                                clientCost = event.clientCost ?: state.clientCost,
                                negotiatedCost = event.negotiatedCost ?: state.negotiatedCost,
                                businessProposedCost =
                                    event.businessProposedCost
                                        ?: state.businessProposedCost,
                                vehicleHeight = event.vehicleHeight ?: state.vehicleHeight,
                                flooringType = event.flooringType ?: state.flooringType,
                                tarpaulinRequired = event.tarpaulinRequired ?: state.tarpaulinRequired,
                                hideCost = event.hideCost ?: state.hideCost,
                                tatRequired = event.tatRequired ?: state.tatRequired,
                                tatHours = event.tatHours ?: state.tatHours,
                                remarks = event.remarks ?: state.remarks,
                                loadWeight = event.loadWeight ?: state.loadWeight,
                                material = event.material ?: state.material,
                            )
                        next.withDiff()
                    }
                }
                is EditIndentEvent.SelectContact -> {
                    _uiState.update { state ->
                        val next =
                            when (event.role) {
                                IndentContactRole.PLACEMENT -> state.copy(placementContact = event.contact)
                                IndentContactRole.PAYMENT -> state.copy(paymentContact = event.contact)
                                IndentContactRole.OPERATIONS ->
                                    state.copy(operationsContact = event.contact)
                                IndentContactRole.POD -> state.copy(podContact = event.contact)
                            }
                        next.withDiff()
                    }
                }
                is EditIndentEvent.SubmitUpdate -> submitUpdate()
                is EditIndentEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
                is EditIndentEvent.ClearSuccess -> _uiState.update { it.copy(successMessage = null) }
            }
        }

        private fun loadIndent(indentId: String) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                val result = getIndentByIdUseCase.invoke(GetIndentByIdUseCaseParams(identId = indentId))
                if (result.isSuccess) {
                    val indent = result.getOrNull()?.data
                    if (indent != null) {
                        val details = indent.indentGroupDetails
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                indent = indent,
                                hasChanges = false,
                                clientCost =
                                    details.clientTripCost?.toInt()?.toString()
                                        ?.let(::formatIndianCurrency) ?: "",
                                negotiatedCost =
                                    indent.negotiatedTripCost?.toInt()?.toString()
                                        ?.let(::formatIndianCurrency) ?: "",
                                businessProposedCost =
                                    details.businessProposedCost?.toInt()?.toString()
                                        ?.let(::formatIndianCurrency) ?: "",
                                vehicleHeight = details.vehicleHeight?.toString() ?: "",
                                flooringType = details.flooringType.normalizedFlooringType(),
                                tarpaulinRequired = details.tarpaulingRequired,
                                hideCost = indent.hideCostInfo,
                                tatRequired = details.tatEnabled,
                                tatHours = details.tatHours?.toString() ?: "",
                                remarks = details.specialRequirements ?: "",
                                loadWeight = formatWeightValue(details.loadWeight),
                                material = details.material,
                            )
                        }
                        loadContacts(indent)
                        loadMaterials()
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to load indent",
                        )
                    }
                }
            }
        }

        /**
         * Loads the client's active contacts so the roles can be reassigned, then
         * pre-selects whatever the indent already records.
         *
         * The indent's `indentContacts` carry only a name and phone, so a role is
         * reconstructed from the richer client-contact record when the ids match and
         * falls back to the indent's own snapshot when they do not — a contact
         * deactivated since the indent was raised must still print, or the roster
         * would silently appear to have lost a role it has.
         */
        private fun loadContacts(indent: IndentResponse.Indent) {
            val clientId = indent.indentGroupDetails.clientId
            val recorded = indent.indentGroupDetails.indentContacts

            viewModelScope.launch {
                _uiState.update { it.copy(isLoadingContacts = true) }
                val result = getClientContactsUseCase.invoke(clientId)
                val available = result.getOrNull().orEmpty()

                _uiState.update { state ->
                    // Order matters: hasChanges must be computed from the state that
                    // already has the roles pre-selected. Computing it before the
                    // pre-selection compares `null` against the contact recorded on
                    // the indent and reports a phantom edit — which would light up the
                    // save button on open and fire a pointless group PUT on submit.
                    val next =
                        state
                            .copy(
                                contacts = available,
                                isLoadingContacts = false,
                                contactsUnavailable = result.isFailure || available.isEmpty(),
                            )
                            .withPreselectedContacts(recorded, available)
                    next.withDiff()
                }
            }
        }

        /**
         * The category list behind the material picker. Fetched with the indent,
         * not on sheet open, for the reason [GetMaterialsUseCase] documents.
         */
        private fun loadMaterials() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoadingMaterials = true) }
                val result = getMaterialsUseCase.invoke()
                _uiState.update {
                    it.copy(
                        materials = result.getOrElse { emptyList() },
                        isLoadingMaterials = false,
                    )
                }
            }
        }

        private fun submitUpdate() {
            val indentId = currentIndentId ?: return
            val originalIndent = _uiState.value.indent ?: return
            val originalDetails = originalIndent.indentGroupDetails
            val currentState = _uiState.value

            // Cargo is `NOT NULL`, so a half-typed correction would be refused by the
            // server as a 400 on the *whole* body — taking the commercials and
            // terms down with it. Refuse here instead, naming the cargo field.
            currentState.cargoValidationError()?.let { error ->
                _uiState.update { it.copy(errorMessage = error) }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

                // Contacts first. This is the call that can legitimately be refused —
                // the group must still be OPEN and the caller must be the creator or
                // an admin / operations manager — so firing it first means a refusal
                // leaves the indent untouched instead of half-corrected.
                if (currentState.contactsChanged) {
                    val groupResult =
                        updateIndentGroupUseCase.invoke(
                            UpdateIndentGroupUseCaseParams(
                                groupId = originalDetails.id,
                                request =
                                    UpdateIndentGroupRequest(
                                        contacts =
                                            IndentContactsRequest(
                                                placement = currentState.placementContact?.id,
                                                payment = currentState.paymentContact?.id,
                                                operations = currentState.operationsContact?.id,
                                                pod = currentState.podContact?.id,
                                            ),
                                    ),
                            ),
                        )
                    if (groupResult.isFailure) {
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                errorMessage =
                                    groupResult.exceptionOrNull()?.message
                                        ?: "Could not update the contact roles",
                            )
                        }
                        return@launch
                    }
                }

                val request =
                    UpdateIndentRequest(
                        clientTripCost =
                            compareFields(
                                currentState.clientCost.replace(",", "").toIntOrNull(),
                                originalDetails.clientTripCost?.toInt(),
                            ),
                        negotiatedTripCost =
                            compareFields(
                                currentState.negotiatedCost.replace(",", "").toIntOrNull(),
                                originalIndent.negotiatedTripCost?.toInt(),
                            ),
                        businessProposedCost =
                            compareFields(
                                currentState.businessProposedCost.replace(",", "").toIntOrNull(),
                                originalDetails.businessProposedCost?.toInt(),
                            ),
                        vehicleHeight =
                            compareFields(
                                currentState.vehicleHeight.toFloatOrNull(),
                                originalDetails.vehicleHeight?.toFloat(),
                            ),
                        hideCostInfo = compareFields(currentState.hideCost, originalIndent.hideCostInfo),
                        tatEnabled = compareFields(currentState.tatRequired, originalDetails.tatEnabled),
                        tatHours = compareFields(currentState.tatHours.toIntOrNull(), originalDetails.tatHours),
                        tarpaulingRequired =
                            compareFields(
                                currentState.tarpaulinRequired,
                                originalDetails.tarpaulingRequired,
                            ),
                        remarks = compareFields(currentState.remarks, originalDetails.specialRequirements),
                        flooringType =
                            compareFields(
                                currentState.flooringType.normalizedFlooringType(),
                                originalDetails.flooringType.normalizedFlooringType(),
                            ),
                        loadWeight =
                            compareFields(
                                currentState.loadWeight.trim().toDoubleOrNull(),
                                originalDetails.loadWeight,
                            ),
                        material = compareFields(currentState.material.trim(), originalDetails.material),
                    )

                // Contacts may have been the only edit; the group call already saved them.
                if (isRequestEmpty(request)) {
                    _uiState.update {
                        it.copy(isSubmitting = false, successMessage = "Indent updated successfully")
                    }
                    return@launch
                }

                val result =
                    updateIndentUseCase.invoke(
                        UpdateIndentUseCaseParams(id = indentId, request = request),
                    )
                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(isSubmitting = false, successMessage = "Indent updated successfully")
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to update indent",
                        )
                    }
                }
            }
        }

        private fun <T> compareFields(
            current: T?,
            original: T?,
        ): T? = if (current == original) null else current

        private fun isRequestEmpty(request: UpdateIndentRequest): Boolean =
            request.clientTripCost == null &&
                request.negotiatedTripCost == null &&
                request.businessProposedCost == null &&
                request.vehicleHeight == null &&
                request.hideCostInfo == null &&
                request.tatEnabled == null &&
                request.tatHours == null &&
                request.tarpaulingRequired == null &&
                request.remarks == null &&
                request.flooringType == null &&
                request.loadWeight == null &&
                request.material == null &&
                request.status == null &&
                request.notes == null &&
                request.luIncludedInTripCost == null
    }

/**
 * Binds each role to the client-contact record the indent already names, falling
 * back to a record built from the indent's own snapshot when the client contact
 * is no longer in the active list.
 */
private fun EditIndentUiState.withPreselectedContacts(
    recorded: List<IndentResponse.IndentContact>,
    available: List<ClientContactV2Dto>,
): EditIndentUiState {
    fun pick(role: IndentContactRole): ClientContactV2Dto? {
        val onIndent =
            recorded.firstOrNull {
                it.contactType.equals(role.name, ignoreCase = true)
            } ?: return null
        val id = onIndent.contactId ?: onIndent.id
        return available.firstOrNull { it.id == id }
            ?: ClientContactV2Dto(
                id = id,
                fullName = onIndent.contactName,
                phone = onIndent.contactPhone,
                email = onIndent.contactEmail,
            )
    }

    return copy(
        placementContact = pick(IndentContactRole.PLACEMENT),
        paymentContact = pick(IndentContactRole.PAYMENT),
        operationsContact = pick(IndentContactRole.OPERATIONS),
        podContact = pick(IndentContactRole.POD),
    )
}

/**
 * The first cargo field this slip cannot file, phrased for a snackbar.
 *
 * Reads the same two error strings the fields already show, rather than
 * re-deriving them: the rules live in [withDiff] and a second copy here would
 * be free to disagree with what the operator can see on the clause.
 *
 * Cargo is the one clause that cannot be left blank — both columns are
 * `NOT NULL` — so it is the one whose state can make a whole correction
 * unsavable. Checked before the PUT rather than after, because the server
 * rejects the entire body and the commercials and terms the operator *did* fill
 * in would be lost with it.
 */
private fun EditIndentUiState.cargoValidationError(): String? =
    loadWeightError?.let { "Cargo weight: $it" } ?: materialError?.let { "Material: $it" }

/** Blank and null mean the same thing to this form: absent text renders as `""`. */
private fun textChanged(
    current: String?,
    original: String?,
): Boolean = current?.trim().orEmpty() != original?.trim().orEmpty()

/**
 * One canonical spelling of "this indent's flooring", used by both the diff and
 * the submit payload so they can never disagree.
 *
 * Blank, `null` and the [DEFAULT_FLOORING_TYPE] prefill all collapse to the same
 * value: Create always writes a flooring type, so an indent recording none was
 * raised before the field existed and is implicitly steel rather than unknown.
 * Lowercased to match what Create submits, so `"WOOD"` typed here and `"wood"`
 * typed there reach the column as the same value.
 */
private fun String?.normalizedFlooringType(): String =
    this?.trim()?.takeIf { it.isNotEmpty() }?.lowercase(Locale.getDefault())
        ?: DEFAULT_FLOORING_TYPE.lowercase(Locale.getDefault())

/**
 * Recomputes everything the screen derives from "has the operator changed
 * anything" — the save button, the header tally, the call-sheet marker, clause
 * 04's and 05's spines — plus the cargo field errors, which live here so the
 * submit guard and the red fields can never disagree about what is fileable.
 *
 * All of it is computed here, once. It used to be computed twice — a copy in the
 * ViewModel and another in `EditIndentScreen` — and the two drifted apart.
 * The screen's copy compared raw values, so:
 *
 *  - every text field the indent left null counted as an edit, because the form
 *    renders absent text as `""`, and
 *  - `flooringType` counted as an edit on every indent raised without one, since
 *    the form pre-fills [DEFAULT_FLOORING_TYPE] while the indent records nothing.
 *    That is still the case — the form does pre-fill it — so the flooring
 *    comparison runs through [normalizedFlooringType] instead of comparing the
 *    raw strings.
 *
 * Between them the header read "1 EDITED" the moment Edit opened, the save
 * button went live, and a submit would have fired a pointless group PUT.
 */
private fun EditIndentUiState.withDiff(): EditIndentUiState {
    val details = indent?.indentGroupDetails ?: return this

    val clientCostChanged =
        clientCost.replace(",", "").toIntOrNull() != details.clientTripCost?.toInt()
    val businessCostChanged =
        businessProposedCost.replace(",", "").toIntOrNull() != details.businessProposedCost?.toInt()
    val negotiatedChanged =
        negotiatedCost.replace(",", "").toIntOrNull() != indent.negotiatedTripCost?.toInt()
    val hideCostChanged = hideCost != indent.hideCostInfo
    val tatRequiredChanged = tatRequired != details.tatEnabled
    val tatHoursChanged = tatHours.toIntOrNull() != details.tatHours
    val tarpaulinChanged = tarpaulinRequired != details.tarpaulingRequired
    val vehicleHeightChanged = vehicleHeight.toFloatOrNull() != details.vehicleHeight?.toFloat()
    val flooringChanged =
        flooringType.normalizedFlooringType() != details.flooringType.normalizedFlooringType()
    val remarksChanged = textChanged(remarks, details.specialRequirements)

    // The form shows the weight without trailing zeros (`2.0` prints as `2`), so
    // comparing the raw strings against `loadWeight.toString()` would report a
    // whole-tonnage weight as edited on every open. Both sides go through
    // formatWeightValue for the same reason.
    val loadWeightChanged =
        formatWeightValue(loadWeight.trim().toDoubleOrNull()) != formatWeightValue(details.loadWeight)
    val materialChanged = textChanged(material, details.material)

    val rolesChanged =
        IndentContactRole.entries.any { role ->
            contactFor(role)?.id != originalContactId(role)
        }

    val fieldCount =
        listOf(
            clientCostChanged,
            businessCostChanged,
            negotiatedChanged,
            hideCostChanged,
            tatRequiredChanged,
            tatHoursChanged,
            tarpaulinChanged,
            vehicleHeightChanged,
            flooringChanged,
            remarksChanged,
            loadWeightChanged,
            materialChanged,
        ).count { it } + if (rolesChanged) 1 else 0

    // Errors surface as the fields turn red while typing, not only on submit —
    // the save button stays live either way, so a silent 400 on submit would be
    // the first time the operator heard their cargo was unfileable.
    val weight = loadWeight.trim()
    val weightValue = weight.toDoubleOrNull()

    return copy(
        hasChanges = fieldCount > 0,
        changedFieldCount = fieldCount,
        contactsChanged = rolesChanged,
        loadWeightError =
            when {
                weight.isEmpty() -> "Required"
                weightValue == null -> "Enter a number"
                weightValue <= 0 -> "Must be above zero"
                else -> null
            },
        materialError = if (material.isBlank()) "Required" else null,
        commercialsChanged =
            clientCostChanged || businessCostChanged ||
                negotiatedChanged || hideCostChanged,
        termsChanged =
            tatRequiredChanged || tatHoursChanged || tarpaulinChanged ||
                vehicleHeightChanged || flooringChanged || remarksChanged,
        cargoChanged = loadWeightChanged || materialChanged,
    )
}
