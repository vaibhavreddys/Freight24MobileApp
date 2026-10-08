package com.freight24.connect.feature.indents.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.connect.feature.indents.domain.viewmodel.IndentContactRole
import com.freight24.rest.response.ClientBranchV2Dto
import com.freight24.rest.response.ClientContactV2Dto
import com.freight24.rest.response.MaterialDto
import com.freight24.ui.components.FreightEmptyState
import com.freight24.ui.components.MetaTag
import com.freight24.ui.components.PerforationRule

// ===========================================================================
// Picker sheets for the indent docket.
//
// Branches and contacts are both loaded the moment a client is picked, so
// neither needs a search screen and a round-trip through savedStateHandle —
// the options are already in ViewModel state by the time the operator taps.
// A sheet keeps the docket on screen behind it, which matters when the choice
// depends on what is already filled in above.
//
// The three sheets share one frame so the operator only has to learn it once:
//
//   • the SHEET HEAD  — clause stamp, title, close disc. The same clause stamp
//                       the docket uses, so the sheet reads as a page torn off
//                       the document behind it rather than a new surface.
//   • the PERFORATION  — the tear line under the head. House language, and it
//                       gives the head a floor without boxing it.
//   • the SEARCH WELL  — a filled, rounded input. The one place where a boxed
//                       field beats the ruled line: filtering is a different act
//                       from filling in a form, and a ruled line reads as
//                       "write here", which made the field look like floating
//                       text rather than somewhere to type.
//   • the CHOICE DISC  — every row ends in a ring that fills when chosen. A
//                       chevron would be a lie: a row here selects and closes
//                       rather than drilling into a sub-category.
// ===========================================================================

/** Sheet gutter. Everything inside a sheet — head, search, list — shares it. */
private val SheetGutter = 24.dp

/** Vertical padding inside one option row. Generous, because there are no rules. */
private val OptionRowPadding = 16.dp

/** Corner of the search well. */
private val SearchWellCorner = 12.dp

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
        // surfaceContainerLow rather than surface: the sheet has to separate
        // from a docket card, and those are already drawn on `surface`. The
        // token also steps *up* in dark mode, which surfaceContainerLowest
        // does not — it would sink the sheet below the page behind it.
        containerColor = scheme.surfaceContainerLow,
        dragHandle = { SheetDragHandle() },
        // The material sheet has a text field in it, so the sheet itself has to
        // move for the keyboard — the default content insets cover the status
        // and nav bars only.
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .imePadding(),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = SheetGutter, end = 14.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = eyebrow.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                    )
                }
                SheetCloseButton(onDismiss = onDismiss, testTag = closeTestTag)
            }
            Spacer(Modifier.height(16.dp))
            PerforationRule()
            Spacer(Modifier.height(20.dp))
            content()
            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * The grab affordance.
 *
 * Wider and darker than the Material default, with less dead space above it —
 * the default pill is 32×4 in a 22dp band on each side, which on a sheet this
 * tall reads as a stray grey dash rather than a handle.
 */
@Composable
private fun SheetDragHandle() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        RoundedCornerShape(2.dp),
                    )
                    .semantics { contentDescription = "Drag sheet" },
        )
    }
}

/**
 * Close as a disc rather than a bare glyph.
 *
 * `size(40.dp)` is deliberate: `IconButton` keeps its own
 * `minimumInteractiveComponentSize`, so the visual shrinks to 40 while the
 * touch target stays at the 48dp the accessibility rules require.
 */
@Composable
private fun SheetCloseButton(
    onDismiss: () -> Unit,
    testTag: String,
) {
    val scheme = MaterialTheme.colorScheme

    IconButton(
        onClick = onDismiss,
        modifier =
            Modifier
                .docketTag(testTag)
                .size(40.dp)
                .background(scheme.surfaceContainerHigh, CircleShape),
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            modifier = Modifier.size(18.dp),
            tint = scheme.onSurfaceVariant,
        )
    }
}

/**
 * A contained search input: filled well, leading magnifier, trailing clear.
 *
 * Not a [RuledField]. A rule under the text is how this app says "write here",
 * and in a sheet whose whole job is filtering it read as floating text with a
 * line under it — there was nothing to say the area was tappable. The well
 * carries that on its own, and the well takes the accent on focus.
 *
 * The field keeps its own state for the caret: the well is drawn from the
 * caller's `value`, but focus is local, so a recomposition higher up cannot
 * blink the accent off mid-typing.
 */
@Composable
private fun SheetSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    testTag: String,
    clearTestTag: String,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(SearchWellCorner)
    var focused by remember { mutableStateOf(false) }

    Row(
        modifier =
            Modifier
                .padding(horizontal = SheetGutter)
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .clip(shape)
                .background(scheme.surfaceContainerHighest)
                .border(
                    width = if (focused) 1.5.dp else 1.dp,
                    color = if (focused) scheme.primary else scheme.outlineVariant.copy(alpha = 0.5f),
                    shape = shape,
                )
                .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (focused) scheme.primary else scheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(12.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = scheme.onSurface),
            cursorBrush = SolidColor(scheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier =
                Modifier
                    .weight(1f)
                    .docketTag(testTag)
                    .onFocusChanged { focused = it.isFocused },
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = scheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    innerTextField()
                }
            },
        )
        if (value.isNotEmpty()) {
            IconButton(
                onClick = { onValueChange("") },
                modifier =
                    Modifier
                        .docketTag(clearTestTag)
                        .size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear search",
                    modifier = Modifier.size(18.dp),
                    tint = scheme.onSurfaceVariant,
                )
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }
    }
}

/**
 * One selectable option: name, supporting detail, and the choice disc.
 *
 * The disc is a ring that fills when chosen, because tapping a row here selects
 * it and closes the sheet. A chevron would promise navigation to a sub-category
 * that does not exist.
 *
 * `selectable` rather than `clickable` so the row reports itself as a selected
 * radio option to TalkBack, not just "double tap to activate".
 *
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

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .docketTag(testTag)
                .background(
                    if (selected) scheme.primaryContainer.copy(alpha = 0.55f) else Color.Transparent,
                )
                .selectable(
                    selected = selected,
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = onClick,
                )
                .padding(horizontal = SheetGutter, vertical = OptionRowPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (enabled) scheme.onSurface else scheme.outline,
                textDecoration = if (enabled) null else TextDecoration.LineThrough,
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
        Spacer(Modifier.width(16.dp))
        ChoiceDisc(selected = selected, enabled = enabled)
    }
}

/**
 * The choice affordance: an empty ring, or a filled disc with a check.
 *
 * The fill is `primary`, not the success green: a selected row is already
 * tinted `primaryContainer`, and the success tone is near-identical to it —
 * the check would have disappeared into its own row.
 */
@Composable
private fun ChoiceDisc(
    selected: Boolean,
    enabled: Boolean,
) {
    val scheme = MaterialTheme.colorScheme

    if (!selected) {
        Box(
            modifier =
                Modifier
                    .size(20.dp)
                    .border(
                        width = 1.5.dp,
                        color = if (enabled) scheme.outlineVariant else scheme.outlineVariant.copy(alpha = 0.5f),
                        shape = CircleShape,
                    ),
        )
        return
    }

    Box(
        modifier =
            Modifier
                .size(22.dp)
                .background(scheme.primary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = scheme.onPrimary,
        )
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

        SheetListHint("${branches.size} branches on file")
        Spacer(Modifier.height(4.dp))

        LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
            items(branches, key = { it.id }) { branch ->
                val active = branch.status.equals("active", true)
                val detail =
                    listOfNotNull(
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
            modifier = Modifier.padding(horizontal = SheetGutter),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MetaTag(
                text = if (role.required) "REQUIRED" else "OPTIONAL",
                container =
                    if (role.required) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                content =
                    if (role.required) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text =
                    if (role.required) {
                        "This is required for truck owner providing a vehicle."
                    } else {
                        "Leave empty if the client has not nominated one."
                    },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(12.dp))

        if (contacts.isEmpty()) {
            FreightEmptyState(
                icon = Icons.Default.Person,
                title = "No contacts on file",
                subtitle =
                    "This client has no active contacts, so no role can be filled. " +
                        "Add a contact to the client before raising an indent.",
            )
            return@PickerSheetScaffold
        }

        LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
            items(contacts, key = { it.id }) { contact ->
                val detail =
                    listOfNotNull(
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

/**
 * Material picker for the Cargo clause.
 *
 * Forty-seven categories is too many to scan and few enough to filter, so the
 * field at the top does both jobs: type to narrow the list, and when what you
 * typed is not a category, the last row offers to use it verbatim. That is the
 * free-text field this replaces, kept as the fallback because the trade carries
 * loads the list does not name yet — `Steel coils`, `Coir`, `Fly ash`.
 *
 * The list is capped at ~5 rows rather than filling the sheet. With the filter
 * doing the work there is nothing to gain from a taller list, and the scroll
 * indicator stops being a full-height bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialPickerSheet(
    materials: List<MaterialDto>,
    selectedName: String,
    isLoading: Boolean,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val typed = query.trim()

    val matches =
        remember(materials, typed) {
            if (typed.isEmpty()) {
                materials
            } else {
                materials.filter { it.name.contains(typed, ignoreCase = true) }
            }
        }

    // Typing a category's exact name should not also offer to "create" it.
    val typedIsCategory = materials.any { it.name.equals(typed, ignoreCase = true) }
    val offersTyped = typed.isNotEmpty() && !typedIsCategory
    val nothingMatched = matches.isEmpty() && typed.isNotEmpty()

    PickerSheetScaffold(
        eyebrow = "Clause 04 · Cargo",
        title = "Material",
        onDismiss = onDismiss,
        closeTestTag = "create_indent_material_sheet_close",
    ) {
        SheetSearchField(
            value = query,
            onValueChange = { query = it },
            placeholder = "Search materials (e.g., Steel)",
            testTag = "create_indent_material_search",
            clearTestTag = "create_indent_material_search_clear",
        )

        if (materials.isEmpty()) {
            Spacer(Modifier.height(4.dp))
            FreightEmptyState(
                icon = Icons.Default.Inventory2,
                title = if (isLoading) "Loading categories" else "Categories unavailable",
                subtitle =
                    if (isLoading) {
                        "Fetching the material list from the backend."
                    } else {
                        "The material list could not be loaded. Type the material above and " +
                            "it will be saved as entered."
                    },
            )
            // The copy above promises that what was typed is still usable, so the
            // row that commits it has to exist here too. It previously did not —
            // a failed category fetch left a field that could be typed in and
            // never submitted.
            if (!isLoading && offersTyped) {
                PerforationRule()
                TypedMaterialRow(
                    typed = typed,
                    hasMatches = false,
                    onClick = {
                        onSelect(typed)
                        onDismiss()
                    },
                )
            }
            return@PickerSheetScaffold
        }

        Spacer(Modifier.height(20.dp))
        // The count belongs to the list, not floating under the field: it is a
        // caption for what follows, and it disappears entirely when the empty
        // state below is already saying nothing matched.
        if (!nothingMatched) {
            SheetListHint(
                if (typed.isEmpty()) {
                    "Showing ${materials.size} categories"
                } else {
                    "${matches.size} of ${materials.size} match"
                },
            )
            Spacer(Modifier.height(4.dp))
        }

        LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
            if (nothingMatched) {
                item(key = "no_matches") {
                    FreightEmptyState(
                        icon = Icons.Default.Inventory2,
                        title = "No materials found",
                        subtitle =
                            "Nothing here matches \u201C$typed\u201D. Try a different " +
                                "keyword, or save it as typed.",
                    )
                }
            }

            items(matches, key = { it.id }) { material ->
                PickerOptionRow(
                    title = material.name,
                    detail = null,
                    selected = material.name.equals(selectedName, ignoreCase = true),
                    enabled = true,
                    testTag = "create_indent_material_option_${material.id}",
                    onClick = {
                        onSelect(material.name)
                        onDismiss()
                    },
                )
            }

            // Last, not first: while the operator is still narrowing a list this
            // row is a wrong turn, and it is only the answer once the list has
            // stopped helping. The tear line is what marks it as an escape hatch
            // rather than the next category in the list.
            if (offersTyped) {
                item(key = "typed_rule") {
                    Spacer(Modifier.height(12.dp))
                    PerforationRule()
                    Spacer(Modifier.height(4.dp))
                }
                item(key = "typed") {
                    TypedMaterialRow(
                        typed = typed,
                        hasMatches = matches.isNotEmpty(),
                        onClick = {
                            onSelect(typed)
                            onDismiss()
                        },
                    )
                }
            }
        }
    }
}

/** The one caption style above a list: secondary, and aligned to the row text. */
@Composable
private fun SheetListHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = SheetGutter),
    )
}

/**
 * The escape hatch: use the typed text as the material.
 *
 * Deliberately not a [PickerOptionRow]. That row means "one of the categories,
 * chosen or not"; this one means "none of them, and I mean it", so it says so on
 * a stamp rather than relying on the operator noticing the list has run out.
 */
@Composable
private fun TypedMaterialRow(
    typed: String,
    hasMatches: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .docketTag("create_indent_material_use_typed")
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = SheetGutter, vertical = OptionRowPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "\u201C$typed\u201D",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = scheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text =
                    if (hasMatches) {
                        "Not the one you meant — save as typed"
                    } else {
                        "Not in the category list — save as typed"
                    },
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(16.dp))
        MetaTag(
            text = "AS TYPED",
            container = scheme.surfaceContainerHighest,
            content = scheme.onSurfaceVariant,
        )
    }
}
