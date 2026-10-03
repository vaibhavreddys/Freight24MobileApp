package com.freight24.connect.feature.indents.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.common.util.formatLoadWeight
import com.freight24.common.util.negotiatedCostExceedsClientError
import com.freight24.connect.feature.indents.domain.viewmodel.EditIndentEvent
import com.freight24.connect.feature.indents.domain.viewmodel.EditIndentUiState
import com.freight24.connect.feature.indents.domain.viewmodel.EditIndentViewModel
import com.freight24.connect.feature.indents.domain.viewmodel.EditIndentViewModelImpl
import com.freight24.connect.feature.indents.domain.viewmodel.IndentContactRole
import com.freight24.ui.components.FreightTopAppBar
import com.freight24.ui.components.MetaTag
import com.freight24.ui.components.PerforationRule
import kotlinx.coroutines.launch

// ===========================================================================
// Edit Indent — a correction slip against a docket already on the desk.
//
// The split is the point, and it follows the two write paths the backend
// actually offers:
//
//   • clauses 02–04 (lane, vehicle, cargo) are facts of the raised indent.
//     Nothing accepts a change to them, so they print as a read-only ledger
//     rather than as greyed-out inputs.
//   • clause 01's client and branch are equally fixed, but its CALL SHEET is
//     not: `PUT /api/v1/indents/groups/:id` can reassign the contact roles.
//   • clauses 05–06 are the fields `PUT /api/v1/indents/:id` accepts.
//
// The group call is fired first and aborts the save if it is refused, so a
// rejection (group no longer OPEN, or caller not the creator / an admin /
// operations manager) leaves the indent untouched rather than half-corrected.
// ===========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIndentScreen(
    indentId: String,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: EditIndentViewModel = hiltViewModel<EditIndentViewModelImpl>(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(indentId) {
        viewModel.onEvent(EditIndentEvent.LoadIndent(indentId))
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            launch { snackbarHostState.showSnackbar(it) }
            onSuccess()
            viewModel.onEvent(EditIndentEvent.ClearSuccess)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(EditIndentEvent.ClearError)
        }
    }

    if (showConfirmDialog) {
        ConfirmCorrectionDialog(
            changedCount = uiState.changedFieldCount,
            contactsChanged = uiState.contactsChanged,
            onConfirm = {
                showConfirmDialog = false
                viewModel.onEvent(EditIndentEvent.SubmitUpdate)
            },
            onDismiss = { showConfirmDialog = false },
        )
    }

    EditIndentContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onEvent = viewModel::onEvent,
        onSubmitClick = {
            val err = negotiatedCostExceedsClientError(uiState.negotiatedCost, uiState.clientCost)
            if (err != null) {
                scope.launch { snackbarHostState.showSnackbar(err) }
            } else {
                showConfirmDialog = true
            }
        },
    )
}

@Composable
private fun ConfirmCorrectionDialog(
    changedCount: Int,
    contactsChanged: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val fields = when (changedCount) {
        0 -> "No fields"
        1 -> "One field"
        else -> "$changedCount fields"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("File this correction?") },
        text = {
            Text(
                buildString {
                    append(fields)
                    append(" have been corrected on this indent. ")
                    if (contactsChanged) {
                        append("The call sheet is saved first; if the server refuses it, ")
                        append("the rest is left alone. ")
                    }
                    append("Changes take effect immediately.")
                },
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.docketTag("edit_indent_confirm_dialog_confirm"),
            ) {
                Text("File correction")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.docketTag("edit_indent_confirm_dialog_cancel"),
            ) {
                Text("Keep editing")
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditIndentContent(
    uiState: EditIndentUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onEvent: (EditIndentEvent) -> Unit,
    onSubmitClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    var contactSheetRole by remember { mutableStateOf<IndentContactRole?>(null) }

    Scaffold(
        containerColor = scheme.background,
        topBar = {
            FreightTopAppBar(
                title = { Text("Edit Indent") },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.docketTag("edit_indent_back"),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp, top = 10.dp),
            ) {
                Button(
                    onClick = onSubmitClick,
                    enabled = !uiState.isSubmitting && uiState.indent != null && uiState.hasChanges,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .docketTag("edit_indent_update_button"),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = scheme.primary,
                        contentColor = scheme.onPrimary,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = scheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(
                        when {
                            uiState.isSubmitting -> "Filing…"
                            uiState.hasChanges -> "File Correction"
                            else -> "No Changes"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    if (!uiState.isSubmitting && uiState.hasChanges) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        when {
            uiState.isLoading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = scheme.primary)
            }

            uiState.indent != null -> EditSlip(
                uiState = uiState,
                onEvent = onEvent,
                onContactClick = { contactSheetRole = it },
                paddingValues = paddingValues,
            )
        }
    }

    contactSheetRole?.let { role ->
        ContactPickerSheet(
            role = role,
            contacts = uiState.contacts,
            selectedId = uiState.contactFor(role)?.id,
            onSelect = { onEvent(EditIndentEvent.SelectContact(role, it)) },
            onDismiss = { contactSheetRole = null },
        )
    }
}

@Composable
private fun EditSlip(
    uiState: EditIndentUiState,
    onEvent: (EditIndentEvent) -> Unit,
    onContactClick: (IndentContactRole) -> Unit,
    paddingValues: PaddingValues,
) {
    val indent = uiState.indent!!
    val details = indent.indentGroupDetails
    val loadingLocation = details.indentLocations.find { it.locationType == "loading" }
    val unloadingLocation = details.indentLocations.find { it.locationType == "unloading" }
    val changed = uiState.changedFieldCount
    val callSheetChanged = uiState.contactsChanged

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        EditHead(
            indentCode = indent.indentCode,
            status = indent.status,
            changedCount = changed,
        )

        ClauseDocket(
            number = 1,
            title = "Consignor",
            state = if (callSheetChanged) ClauseState.FILLED else ClauseState.COMPLETE,
            testTag = "edit_indent_clause_consignor",
        ) {
            LedgerRow(
                label = "Client",
                value = details.clientProfile?.companyName
                    ?: details.clientProfile?.fullName,
            )
            LedgerDivider()
            LedgerRow(
                label = "Client phone",
                value = details.clientProfile?.phoneNumber,
            )
            LedgerDivider()
            LedgerRow(
                label = "Branch",
                value = details.clientBranch?.let { branch ->
                    listOfNotNull(
                        branch.branchName?.takeIf { it.isNotBlank() },
                        branch.branchCode?.takeIf { it.isNotBlank() },
                    ).joinToString(" · ").takeIf { it.isNotBlank() }
                },
            )

            Spacer(Modifier.height(14.dp))
            PerforationRule()
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "CALL SHEET",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(8.dp))
                MetaTag(
                    text = "EDITABLE",
                    container = MaterialTheme.colorScheme.primaryContainer,
                    content = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Roles can be reassigned while the indent is still open.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))

            if (uiState.isLoadingContacts) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Loading contacts…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                IndentContactRole.entries.forEachIndexed { index, role ->
                    ContactRoleSlot(
                        role = role,
                        contact = uiState.contactFor(role),
                        onClick = { onContactClick(role) },
                        // Placement cannot be cleared — the backend refuses a group
                        // save without it, so offering a clear would only build a
                        // request that cannot succeed. It can still be reassigned.
                        onClear = if (role == IndentContactRole.PLACEMENT) {
                            null
                        } else {
                            { onEvent(EditIndentEvent.SelectContact(role, null)) }
                        },
                        testTag = "edit_indent_contact_${role.wireKey}",
                        enabled = !uiState.contactsUnavailable,
                        placeholder = if (uiState.contactsUnavailable) {
                            "No contacts on file"
                        } else {
                            "Choose contact"
                        },
                    )
                    if (index != IndentContactRole.entries.lastIndex) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                        )
                    }
                }
            }

            if (uiState.contactsUnavailable && !uiState.isLoadingContacts) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "This client has no active contacts, so no role can be changed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        ClauseDocket(
            number = 2,
            title = "Lane",
            state = ClauseState.COMPLETE,
            testTag = "edit_indent_clause_lane",
            trailing = { LockedStamp() },
        ) {
            LaneReadout(
                originLabel = "Loading from",
                originValue = loadingLocation?.areaName
                    ?: loadingLocation?.cityName.orEmpty(),
                destinationLabel = "Unloading at",
                destinationValue = unloadingLocation?.areaName
                    ?: unloadingLocation?.cityName.orEmpty(),
                originTestTag = "edit_indent_from",
                destinationTestTag = "edit_indent_to",
            )
        }

        ClauseDocket(
            number = 3,
            title = "Vehicle",
            state = ClauseState.COMPLETE,
            testTag = "edit_indent_clause_vehicle",
            trailing = { LockedStamp() },
        ) {
            LedgerRow(label = "Vehicle type", value = details.truckType?.displayName)
            LedgerDivider()
            LedgerRow(label = "Body type", value = details.truckType?.bodyType)
        }

        ClauseDocket(
            number = 4,
            title = "Cargo",
            state = ClauseState.COMPLETE,
            testTag = "edit_indent_clause_cargo",
            trailing = { LockedStamp() },
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LedgerRow(
                    label = "Weight",
                    value = formatLoadWeight(details.loadWeight, "MT"),
                    modifier = Modifier.weight(1f),
                )
                LedgerRow(
                    label = "Material",
                    value = details.material,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        ClauseDocket(
            number = 5,
            title = "Commercials",
            state = if (uiState.commercialsChanged) ClauseState.FILLED else ClauseState.EMPTY,
            testTag = "edit_indent_clause_commercials",
        ) {
            MoneyRuleField(
                label = "Client cost",
                raw = uiState.clientCost,
                testTag = "edit_indent_client_cost_field",
                onCommit = { onEvent(EditIndentEvent.UpdateField(clientCost = it)) },
                trailingLabel = "INR",
            )
            Spacer(Modifier.height(18.dp))
            MoneyRuleField(
                label = "Business proposed cost",
                raw = uiState.businessProposedCost,
                testTag = "edit_indent_proposed_cost_field",
                onCommit = { onEvent(EditIndentEvent.UpdateField(businessProposedCost = it)) },
                trailingLabel = "INR",
            )
            Spacer(Modifier.height(18.dp))
            MoneyRuleField(
                label = "Negotiated trip cost",
                raw = uiState.negotiatedCost,
                testTag = "edit_indent_negotiated_cost_field",
                onCommit = { onEvent(EditIndentEvent.UpdateField(negotiatedCost = it)) },
                trailingLabel = "INR",
                isError = negotiatedCostExceedsClientError(
                    uiState.negotiatedCost,
                    uiState.clientCost,
                ) != null,
                supportingText = negotiatedCostExceedsClientError(
                    uiState.negotiatedCost,
                    uiState.clientCost,
                ),
            )
            Spacer(Modifier.height(14.dp))
            PerforationRule()
            Spacer(Modifier.height(4.dp))
            DocketSwitchRow(
                title = "Hide cost from the load board",
                description = "Truck owners see the load without the rate.",
                checked = uiState.hideCost,
                onCheckedChange = { onEvent(EditIndentEvent.UpdateField(hideCost = it)) },
                testTag = "edit_indent_hide_cost_switch",
            )
        }

        ClauseDocket(
            number = 6,
            title = "Terms",
            state = if (uiState.termsChanged) ClauseState.FILLED else ClauseState.EMPTY,
            testTag = "edit_indent_clause_terms",
        ) {
            DocketSwitchRow(
                title = "Turnaround time",
                description = "Commit to a delivery window from loading.",
                checked = uiState.tatRequired,
                onCheckedChange = { onEvent(EditIndentEvent.UpdateField(tatRequired = it)) },
                testTag = "edit_indent_tat_switch",
            )
            Spacer(Modifier.height(6.dp))
            RuledField(
                label = "TAT hours",
                value = uiState.tatHours,
                onValueChange = { onEvent(EditIndentEvent.UpdateField(tatHours = it)) },
                testTag = "edit_indent_tat_hours_field",
                placeholder = "0",
                keyboardType = KeyboardType.Decimal,
                enabled = uiState.tatRequired,
                trailingLabel = "HRS",
            )
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                RuledField(
                    label = "Vehicle height",
                    value = uiState.vehicleHeight,
                    onValueChange = { onEvent(EditIndentEvent.UpdateField(vehicleHeight = it)) },
                    testTag = "edit_indent_height_field",
                    placeholder = "0",
                    keyboardType = KeyboardType.Number,
                    trailingLabel = "FT",
                    modifier = Modifier.weight(1f),
                )
                RuledField(
                    label = "Flooring",
                    value = uiState.flooringType,
                    onValueChange = { onEvent(EditIndentEvent.UpdateField(flooringType = it)) },
                    testTag = "edit_indent_flooring_field",
                    placeholder = "steel",
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(14.dp))
            PerforationRule()
            Spacer(Modifier.height(4.dp))
            DocketSwitchRow(
                title = "Tarpaulin required",
                description = "The load must be sheeted.",
                checked = uiState.tarpaulinRequired,
                onCheckedChange = { onEvent(EditIndentEvent.UpdateField(tarpaulinRequired = it)) },
                testTag = "edit_indent_tarpaulin_switch",
            )
            Spacer(Modifier.height(10.dp))
            RuledField(
                label = "Remarks",
                value = uiState.remarks,
                onValueChange = { onEvent(EditIndentEvent.UpdateField(remarks = it)) },
                testTag = "edit_indent_remarks_field",
                placeholder = "Anything the driver should know",
                minLines = 2,
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

/** Marks a clause this slip cannot touch. */
@Composable
private fun LockedStamp() {
    val scheme = MaterialTheme.colorScheme
    MetaTag(
        text = "LOCKED",
        container = scheme.surfaceContainerHighest,
        content = scheme.onSurfaceVariant,
    )
}

@Composable
private fun EditHead(
    indentCode: String?,
    status: String,
    changedCount: Int,
) {
    val scheme = MaterialTheme.colorScheme

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "CORRECTION SLIP",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                fontWeight = FontWeight.Bold,
                color = scheme.primary,
            )
            Spacer(Modifier.weight(1f))
            MetaTag(
                text = if (changedCount == 0) "NO EDITS" else "$changedCount EDITED",
                container = if (changedCount == 0) {
                    scheme.surfaceContainerHighest
                } else {
                    scheme.primaryContainer
                },
                content = if (changedCount == 0) {
                    scheme.onSurfaceVariant
                } else {
                    scheme.onPrimaryContainer
                },
            )
        }

        Text(
            text = indentCode?.takeIf { it.isNotBlank() } ?: "Indent",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            MetaTag(
                text = status.replace('_', ' '),
                container = scheme.secondaryContainer,
                content = scheme.onSecondaryContainer,
            )
        }

        Text(
            text = "The lane, vehicle and cargo are fixed once an indent is raised. " +
                "The call sheet, commercials and terms can be corrected here.",
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )
    }
}

// ---------------------------------------------------------------------------
// Change detection
//
// Deliberately not implemented here. The head stamp, the clause spines and the
// save button all read `changedFieldCount` / `contactsChanged` /
// `commercialsChanged` off the UI state, which the ViewModel fills in one place
// (`EditIndentUiState.withDiff`).
//
// This file used to carry its own copies of that logic. They drifted from the
// ViewModel's, compared raw nulls and the form's invented defaults, and made the
// page announce "1 EDITED" on open. One implementation, one answer.
// ---------------------------------------------------------------------------
