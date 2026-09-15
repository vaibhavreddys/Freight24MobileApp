package com.freight24.connect.feature.indents.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.connect.feature.indents.domain.viewmodel.IndentContactRole
import com.freight24.rest.response.ClientBranchV2Dto
import com.freight24.rest.response.ClientContactV2Dto
import com.freight24.ui.components.FreightEmptyState
import com.freight24.ui.components.MetaTag
import com.freight24.ui.components.PerforationRule
import com.freight24.ui.theme.FreightThemeTokens

// ===========================================================================
// Picker sheets for the indent docket.
//
// Branches and contacts are both loaded the moment a client is picked, so
// neither needs a search screen and a round-trip through savedStateHandle —
// the options are already in ViewModel state by the time the operator taps.
// A sheet keeps the docket on screen behind it, which matters when the choice
// depends on what is already filled in above.
// ===========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickerSheetScaffold(
    eyebrow: String,
    title: String,
    onDismiss: () -> Unit,
    closeTestTag: String,
    content: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = scheme.surface,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp, bottom = 14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = eyebrow.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.docketTag(closeTestTag),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = scheme.onSurfaceVariant,
                    )
                }
            }
            PerforationRule()
            Spacer(Modifier.height(4.dp))
            content()
            Spacer(Modifier.height(12.dp))
        }
    }
}

/**
 * One selectable option: name, supporting detail, and a check when chosen.
 * Inactive branches render struck-through and are not selectable — the backend
 * rejects an inactive branch outright, so offering it would only manufacture a
 * 400 further down the flow.
 */
@Composable
private fun PickerOptionRow(
    title: String,
    detail: String?,
    selected: Boolean,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val ext = FreightThemeTokens.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .docketTag(testTag)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = when {
                    !enabled -> scheme.outline
                    selected -> scheme.primary
                    else -> scheme.onSurface
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!detail.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        if (selected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(ext.surfaceSuccess, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = ext.onSurfaceSuccess,
                )
            }
        }
    }
}

/** Branch picker. Selecting the current branch again clears it — the field is optional. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchPickerSheet(
    branches: List<ClientBranchV2Dto>,
    selectedId: String?,
    onSelect: (ClientBranchV2Dto?) -> Unit,
    onDismiss: () -> Unit,
) {
    PickerSheetScaffold(
        eyebrow = "Clause 01 · Consignor",
        title = "Client branch",
        onDismiss = onDismiss,
        closeTestTag = "create_indent_branch_sheet_close",
    ) {
        if (branches.isEmpty()) {
            FreightEmptyState(
                icon = Icons.Default.Domain,
                title = "No branches on file",
                subtitle = "This client has no branches yet. The indent can still be raised without one.",
            )
            return@PickerSheetScaffold
        }

        LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
            items(branches, key = { it.id }) { branch ->
                val active = branch.status.equals("active", true)
                val detail = listOfNotNull(
                    branch.branchCode?.takeIf { it.isNotBlank() },
                    branch.branchType?.name?.takeIf { it.isNotBlank() },
                    branch.address?.city?.name?.takeIf { it.isNotBlank() },
                    if (active) null else "INACTIVE",
                ).joinToString(" · ").takeIf { it.isNotBlank() }

                PickerOptionRow(
                    title = branch.branchName?.takeIf { it.isNotBlank() } ?: "Unnamed branch",
                    detail = detail,
                    selected = branch.id == selectedId,
                    enabled = active,
                    testTag = "create_indent_branch_option_${branch.id}",
                    onClick = {
                        onSelect(if (branch.id == selectedId) null else branch)
                        onDismiss()
                    },
                )
            }
        }
    }
}

/** Contact picker for one role. Selecting the current contact again clears the role. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactPickerSheet(
    role: IndentContactRole,
    contacts: List<ClientContactV2Dto>,
    selectedId: String?,
    onSelect: (ClientContactV2Dto?) -> Unit,
    onDismiss: () -> Unit,
) {
    PickerSheetScaffold(
        eyebrow = "Clause 01 · Call sheet",
        title = "${role.label} contact",
        onDismiss = onDismiss,
        closeTestTag = "create_indent_contact_sheet_close",
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MetaTag(
                text = if (role.required) "REQUIRED" else "OPTIONAL",
                container = if (role.required) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
                content = if (role.required) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (role.required) {
                    "The driver needs someone to ring on arrival."
                } else {
                    "Leave empty if the client has not nominated one."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(8.dp))

        if (contacts.isEmpty()) {
            FreightEmptyState(
                icon = Icons.Default.Person,
                title = "No contacts on file",
                subtitle = "This client has no active contacts, so no role can be filled. " +
                    "Add a contact to the client before raising an indent.",
            )
            return@PickerSheetScaffold
        }

        LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
            items(contacts, key = { it.id }) { contact ->
                val detail = listOfNotNull(
                    contact.designation?.takeIf { it.isNotBlank() },
                    contact.phone?.takeIf { it.isNotBlank() },
                    contact.email?.takeIf { it.isNotBlank() },
                ).joinToString(" · ").takeIf { it.isNotBlank() }

                PickerOptionRow(
                    title = contact.fullName?.takeIf { it.isNotBlank() } ?: "Unnamed contact",
                    detail = detail,
                    selected = contact.id == selectedId,
                    enabled = true,
                    testTag = "create_indent_contact_option_${contact.id}",
                    onClick = {
                        onSelect(if (contact.id == selectedId) null else contact)
                        onDismiss()
                    },
                )
            }
        }
    }
}
