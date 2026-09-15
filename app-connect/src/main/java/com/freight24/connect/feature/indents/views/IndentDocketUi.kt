package com.freight24.connect.feature.indents.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.connect.feature.indents.domain.viewmodel.IndentContactRole
import com.freight24.rest.response.ClientContactV2Dto
import com.freight24.ui.components.DocketCard
import com.freight24.ui.components.PerforationRule
import com.freight24.ui.components.TagShape
import com.freight24.ui.theme.FreightThemeTokens

// ===========================================================================
// The indent docket.
//
// An indent is the consignment note that starts a shipment, so the three
// screens are three states of one document rather than three forms:
//
//   Create  — a blank docket being filled in
//   Review  — the completed docket, read back before it is raised
//   Edit    — a correction slip against a docket already on the desk
//
// It reuses the shared "clearing desk" primitives (DocketCard's lane spine and
// dog-ear, PerforationRule, MetaTag) and extends them with four devices that
// only make sense on paperwork:
//
//   • the CLAUSE STAMP   — "01 · CONSIGNOR" — a numbered, ruled clause heading,
//                          so the form reads as clauses on a note instead of a
//                          column of cards.
//   • the RULED LINE     — text is entered on a rule, the way it is written on
//                          a paper form, not inside a rounded box.
//   • the CALL SHEET     — the four contact roles as one roster, because that
//                          is what they are: who the driver rings, and why.
//   • the COMPLETENESS SPINE — the card spine is keyed to clause state, so a
//                          glance down the left edge shows what is still owed.
//
// Every colour resolves through MaterialTheme.colorScheme or
// FreightThemeTokens.colors — no raw hex — so light and dark are both authored.
// ===========================================================================

/**
 * How far along a clause is. Drives the spine colour and the tally rail.
 *
 * The distinction between [COMPLETE] and [FILLED] is deliberate: a required
 * clause that has everything it needs is *complete*, while an optional clause
 * that the operator chose to fill in is merely *filled*. Colouring both green
 * would claim an optional clause was required all along.
 */
enum class ClauseState { EMPTY, PARTIAL, FILLED, COMPLETE, ERROR }

@Composable
fun clauseAccent(state: ClauseState): Color {
    val ext = FreightThemeTokens.colors
    val scheme = MaterialTheme.colorScheme
    return when (state) {
        ClauseState.ERROR -> scheme.error
        ClauseState.COMPLETE -> ext.success
        ClauseState.FILLED -> scheme.primary
        ClauseState.PARTIAL -> ext.warning
        ClauseState.EMPTY -> scheme.outlineVariant
    }
}

/** Exposes a stable resource-id so `scripts/android_agent.py` can drive the node. */
fun Modifier.docketTag(tag: String): Modifier =
    this.testTag(tag).semantics { testTagsAsResourceId = true }

private val EyebrowSpacing = 1.1.sp

// ---------------------------------------------------------------------------
// Clause heading
// ---------------------------------------------------------------------------

/**
 * A numbered clause heading: a stamped numeral, the clause name in tracked
 * caps, then a hairline rule running to the edge of the docket.
 *
 * The rule is the point — it ties the heading to the margin the way a printed
 * form does, and it gives [trailing] a place to sit without a second row.
 */
@Composable
fun IndentClauseHeader(
    number: Int,
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(shape = TagShape, color = scheme.primaryContainer) {
            Text(
                text = number.toString().padStart(2, '0'),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            )
        }
        Spacer(Modifier.width(9.dp))
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.4.sp),
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(10.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = scheme.outlineVariant.copy(alpha = 0.7f),
        )
        if (trailing != null) {
            Spacer(Modifier.width(10.dp))
            trailing()
        }
    }
}

/**
 * One clause of the docket: a [DocketCard] whose spine is keyed to
 * [state], headed by [IndentClauseHeader], with an optional tear-off [footer].
 */
@Composable
fun ClauseDocket(
    number: Int,
    title: String,
    state: ClauseState,
    testTag: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
    footer: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    DocketCard(
        accent = clauseAccent(state),
        testTag = testTag,
        onClick = null,
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 15.dp, bottom = 15.dp),
    ) {
        IndentClauseHeader(number = number, title = title, trailing = trailing)
        Spacer(Modifier.height(16.dp))
        content()
        if (footer != null) {
            Spacer(Modifier.height(16.dp))
            PerforationRule()
            Spacer(Modifier.height(12.dp))
            footer()
        }
    }
}

/** Slim tick rail: one segment per clause, coloured by state. */
@Composable
fun ClauseTally(
    states: List<ClauseState>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        states.forEach { state ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(clauseAccent(state), RoundedCornerShape(2.dp)),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Ruled input
// ---------------------------------------------------------------------------

/**
 * A text field written on a ruled line.
 *
 * The rule thickens and takes the accent colour on focus, and goes to the error
 * colour when [isError] — the same signal a boxed field gives, without boxing
 * every input on a page that is supposed to read as a form.
 */
@Composable
fun RuledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    trailingLabel: String? = null,
    minLines: Int = 1,
) {
    RuledFieldChrome(
        label = label,
        testTag = testTag,
        modifier = modifier,
        isError = isError,
        supportingText = supportingText,
        trailingLabel = trailingLabel,
    ) { focused ->
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = minLines == 1,
            minLines = minLines,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .fillMaxWidth()
                .docketTag(testTag)
                .onFocusChanged { focused.value = it.isFocused },
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

/**
 * [RuledField] for money, backed by a [TextFieldValue] so the caller can keep
 * the caret correct while thousands separators are inserted into the text.
 * The plain-`String` overload cannot do this — Compose reconstructs the
 * selection on every recomposition and the caret jumps to the end.
 */
@Composable
fun RuledAmountField(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    trailingLabel: String? = null,
) {
    RuledFieldChrome(
        label = label,
        testTag = testTag,
        modifier = modifier,
        isError = isError,
        supportingText = supportingText,
        trailingLabel = trailingLabel,
    ) { focused ->
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .docketTag(testTag)
                .onFocusChanged { focused.value = it.isFocused },
            decorationBox = { innerTextField ->
                Box {
                    if (value.text.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

/**
 * The rule, eyebrow label and supporting text shared by both ruled inputs.
 * [content] receives the focus flag so each variant can colour its own caret.
 */
@Composable
private fun RuledFieldChrome(
    label: String,
    testTag: String,
    modifier: Modifier,
    isError: Boolean,
    supportingText: String?,
    trailingLabel: String?,
    content: @Composable (MutableState<Boolean>) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val focused = remember { mutableStateOf(false) }
    val focusedNow by focused

    val ruleColor = when {
        isError -> scheme.error
        focusedNow -> scheme.primary
        else -> scheme.outlineVariant
    }
    val ruleThickness = if (focusedNow || isError) 2.dp else 1.dp

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = EyebrowSpacing),
                fontWeight = FontWeight.SemiBold,
                color = if (isError) scheme.error else scheme.onSurfaceVariant,
            )
            if (trailingLabel != null) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = trailingLabel,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = EyebrowSpacing),
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.outline,
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        content(focused)
        Spacer(Modifier.height(5.dp))
        HorizontalDivider(thickness = ruleThickness, color = ruleColor)
        if (supportingText != null) {
            Spacer(Modifier.height(5.dp))
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) scheme.error else scheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * A picker on a ruled line — tap to open a search/selection screen. The
 * counterpart of [RuledField] for values the operator chooses rather than types.
 *
 * Deliberately *not* a disabled text field: a disabled field greys its own
 * content out and reads as "you may not touch this", when the whole row is
 * actually the tap target.
 */
@Composable
fun DocketPicker(
    label: String,
    value: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    placeholder: String = "Tap to select",
    isError: Boolean = false,
    supportingText: String? = null,
    enabled: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    val hasValue = value.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .docketTag(testTag)
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = EyebrowSpacing),
            fontWeight = FontWeight.SemiBold,
            color = if (isError) scheme.error else scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (hasValue) value else placeholder,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (hasValue) FontWeight.SemiBold else FontWeight.Normal,
                color = when {
                    !enabled -> scheme.outline
                    hasValue -> scheme.onSurface
                    else -> scheme.outline
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isError) scheme.error else scheme.primary,
            )
        }
        Spacer(Modifier.height(5.dp))
        HorizontalDivider(
            thickness = if (isError) 2.dp else 1.dp,
            color = when {
                isError -> scheme.error
                hasValue -> scheme.primary.copy(alpha = 0.45f)
                else -> scheme.outlineVariant
            },
        )
        if (supportingText != null) {
            Spacer(Modifier.height(5.dp))
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) scheme.error else scheme.onSurfaceVariant,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Ledger (read-only) rows
// ---------------------------------------------------------------------------

/** One fact: label above, value below, values sharing a left edge. */
@Composable
fun LedgerRow(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val blank = value.isNullOrBlank()

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = EyebrowSpacing),
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = if (blank) "—" else value!!,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (blank) scheme.outline else scheme.onSurface,
        )
    }
}

@Composable
fun LedgerDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(vertical = 12.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
    )
}

// ---------------------------------------------------------------------------
// Call sheet — the four contact roles
// ---------------------------------------------------------------------------

/**
 * One contact role on the call sheet: the role as a stamp, then either the
 * chosen contact or an invitation to choose one.
 *
 * The role tag is always rendered — including for empty optional roles — so the
 * roster keeps a fixed shape and the operator can see all four slots at once
 * rather than discovering the optional ones by scrolling.
 */
@Composable
fun ContactRoleSlot(
    role: IndentContactRole,
    contact: ClientContactV2Dto?,
    onClick: () -> Unit,
    onClear: (() -> Unit)?,
    testTag: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    placeholder: String = "Choose contact",
) {
    val scheme = MaterialTheme.colorScheme
    val ext = FreightThemeTokens.colors
    val filled = contact != null

    val tagContainer = when {
        isError -> scheme.errorContainer
        filled -> ext.surfaceSuccess
        else -> scheme.surfaceContainerHighest
    }
    val tagContent = when {
        isError -> scheme.onErrorContainer
        filled -> ext.onSurfaceSuccess
        else -> scheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .docketTag(testTag)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(shape = TagShape, color = tagContainer) {
            Text(
                text = role.label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.9.sp),
                fontWeight = FontWeight.Bold,
                color = tagContent,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            )
        }

        // The one role the backend will not accept an indent without. Flagged on
        // the roster itself rather than only on a failed submit, so the operator
        // knows before they reach the bottom of the docket.
        if (role.required && !filled) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = "REQUIRED",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                fontWeight = FontWeight.Bold,
                color = if (isError) scheme.error else scheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact?.fullName?.takeIf { it.isNotBlank() } ?: placeholder,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (filled) FontWeight.SemiBold else FontWeight.Normal,
                color = if (filled) scheme.onSurface else scheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val detail = contact?.let { c ->
                listOfNotNull(
                    c.designation?.takeIf { it.isNotBlank() },
                    c.phone?.takeIf { it.isNotBlank() },
                ).joinToString(" · ").takeIf { it.isNotBlank() }
            }
            if (detail != null) {
                Spacer(Modifier.height(1.dp))
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (filled && onClear != null) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear ${role.label} contact",
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = onClear),
                tint = scheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(10.dp))
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (isError) scheme.error else scheme.outline,
        )
    }
}

/**
 * The call sheet read back. Same four roles in the same order as the picker,
 * with the chevrons and the invitation to choose removed — a role nobody was
 * nominated for prints as an em dash rather than a prompt.
 */
@Composable
fun CallSheetReadout(
    contactFor: (IndentContactRole) -> ClientContactV2Dto?,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val ext = FreightThemeTokens.colors

    Column(modifier = modifier.fillMaxWidth()) {
        IndentContactRole.entries.forEachIndexed { index, role ->
            val contact = contactFor(role)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .docketTag("indent_contact_readout_${role.wireKey}")
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = TagShape,
                    color = if (contact != null) ext.surfaceSuccess else scheme.surfaceContainerHighest,
                ) {
                    Text(
                        text = role.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.9.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (contact != null) ext.onSurfaceSuccess else scheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact?.fullName?.takeIf { it.isNotBlank() } ?: "—",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (contact != null) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (contact != null) scheme.onSurface else scheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val detail = contact?.let { c ->
                        listOfNotNull(
                            c.designation?.takeIf { it.isNotBlank() },
                            c.phone?.takeIf { it.isNotBlank() },
                        ).joinToString(" · ").takeIf { it.isNotBlank() }
                    }
                    if (detail != null) {
                        Spacer(Modifier.height(1.dp))
                        Text(
                            text = detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            if (index != IndentContactRole.entries.lastIndex) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = scheme.outlineVariant.copy(alpha = 0.45f),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Lane
// ---------------------------------------------------------------------------

/**
 * The route as a vertical lane: pickup ring, dashed run, delivery disc. Each
 * stop is its own tap target, so the lane *is* the two pickers rather than a
 * decorative header above two form fields.
 */
@Composable
fun LanePicker(
    originLabel: String,
    originValue: String,
    onOriginClick: () -> Unit,
    originTestTag: String,
    destinationLabel: String,
    destinationValue: String,
    onDestinationClick: () -> Unit,
    destinationTestTag: String,
    modifier: Modifier = Modifier,
    originError: Boolean = false,
    destinationError: Boolean = false,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        LaneStop(
            label = originLabel,
            value = originValue,
            onClick = onOriginClick,
            testTag = originTestTag,
            markerColor = FreightThemeTokens.colors.routeOrigin,
            isOrigin = true,
            isError = originError,
        )
        LaneRun()
        LaneStop(
            label = destinationLabel,
            value = destinationValue,
            onClick = onDestinationClick,
            testTag = destinationTestTag,
            markerColor = FreightThemeTokens.colors.routeDestination,
            isOrigin = false,
            isError = destinationError,
        )
    }
}

/**
 * The same lane, read back. Review and Edit show the route as a fact rather
 * than a control, so the stops lose their chevrons and their rules but keep the
 * markers and the run between them — the shape is what identifies the lane.
 */
@Composable
fun LaneReadout(
    originLabel: String,
    originValue: String,
    destinationLabel: String,
    destinationValue: String,
    modifier: Modifier = Modifier,
    originTestTag: String? = null,
    destinationTestTag: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val ext = FreightThemeTokens.colors

    Column(modifier = modifier.fillMaxWidth()) {
        LaneReadoutStop(
            label = originLabel,
            value = originValue,
            markerColor = ext.routeOrigin,
            isOrigin = true,
            testTag = originTestTag,
        )
        LaneRun()
        LaneReadoutStop(
            label = destinationLabel,
            value = destinationValue,
            markerColor = ext.routeDestination,
            isOrigin = false,
            testTag = destinationTestTag,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "Route is fixed once the indent is raised.",
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )
    }
}

/** The dashed run between two lane stops. */
@Composable
private fun LaneRun() {
    // Read the token outside the draw lambda — FreightThemeTokens.colors is a
    // @Composable getter and cannot be invoked from a DrawScope.
    val runColor = FreightThemeTokens.colors.routeDestination.copy(alpha = 0.55f)

    Canvas(
        modifier = Modifier
            .padding(start = 6.dp)
            .width(2.dp)
            .height(22.dp),
    ) {
        drawLine(
            color = runColor,
            start = Offset(size.width / 2f, 0f),
            end = Offset(size.width / 2f, size.height),
            strokeWidth = size.width,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 5f), 0f),
        )
    }
}

@Composable
private fun LaneReadoutStop(
    label: String,
    value: String,
    markerColor: Color,
    isOrigin: Boolean,
    testTag: String?,
) {
    val scheme = MaterialTheme.colorScheme
    val hasValue = value.isNotBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.docketTag(testTag) else Modifier)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LaneMarker(markerColor = markerColor, isOrigin = isOrigin)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = EyebrowSpacing),
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = if (hasValue) value else "—",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (hasValue) scheme.onSurface else scheme.outline,
            )
        }
    }
}

@Composable
private fun LaneMarker(markerColor: Color, isOrigin: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Canvas(modifier = Modifier.size(14.dp)) {
        val radius = size.minDimension / 2f
        if (isOrigin) {
            // Pickup reads as a ring with a pin through it.
            drawCircle(color = markerColor, radius = radius, style = Stroke(width = 2.5f))
            drawCircle(color = markerColor, radius = radius * 0.32f)
        } else {
            drawCircle(color = markerColor, radius = radius)
            drawCircle(color = scheme.surface, radius = radius * 0.3f)
        }
    }
}

@Composable
private fun LaneStop(
    label: String,
    value: String,
    onClick: () -> Unit,
    testTag: String,
    markerColor: Color,
    isOrigin: Boolean,
    isError: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    val hasValue = value.isNotBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .docketTag(testTag)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LaneMarker(markerColor = markerColor, isOrigin = isOrigin)

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = EyebrowSpacing),
                fontWeight = FontWeight.SemiBold,
                color = if (isError) scheme.error else scheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = if (hasValue) value else "Choose location",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (hasValue) FontWeight.SemiBold else FontWeight.Normal,
                color = if (hasValue) scheme.onSurface else scheme.outline,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(5.dp))
            HorizontalDivider(
                thickness = if (isError) 2.dp else 1.dp,
                color = when {
                    isError -> scheme.error
                    hasValue -> markerColor.copy(alpha = 0.5f)
                    else -> scheme.outlineVariant
                },
            )
        }

        Spacer(Modifier.width(10.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (isError) scheme.error else scheme.outline,
        )
    }
}

// ---------------------------------------------------------------------------
// Switches and chips
// ---------------------------------------------------------------------------

/** A labelled switch row: title, one-line explanation, control on the right. */
@Composable
fun DocketSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .docketTag(testTag)
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) scheme.onSurface else scheme.outline,
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        androidx.compose.material3.Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

/** Selectable chip used for vehicle types. Carries a state stripe when chosen. */
@Composable
fun VehicleTypeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val ext = FreightThemeTokens.colors

    Surface(
        modifier = modifier.docketTag(testTag).clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (isSelected) scheme.primaryContainer else Color.Transparent,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) scheme.primary else scheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(ext.success, RoundedCornerShape(50)),
                )
                Spacer(Modifier.width(7.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
            )
        }
    }
}
