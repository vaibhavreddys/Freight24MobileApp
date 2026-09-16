package com.freight24.connect.feature.indents.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.connect.feature.indents.domain.viewmodel.IndentContactRole
import com.freight24.rest.response.IndentResponse
import com.freight24.ui.components.PerforationRule
import com.freight24.ui.components.TagShape
import com.freight24.ui.theme.FreightThemeTokens

// ===========================================================================
// Read-only docket readouts.
//
// Create / Review / Edit are the *form* side of the paperwork — ruled lines to
// write on, a completeness spine, a tally of what is still owed. These are the
// *record* side: the same visual language with nothing to fill in.
//
//   • the SEAL        — the stamp that says a docket has been lodged.
//   • the LEDGER LINE — label, dotted leader, value, on one line. The densest
//                       way to print a fact, and the reason the details page
//                       can carry every field without becoming a scroll.
//   • the LANE STRIP  — the route read back vertically, markers and run intact.
//   • the CALL SHEET  — who the driver rings and why, read off the indent
//                       rather than chosen on it.
//
// Every colour resolves through MaterialTheme.colorScheme or
// FreightThemeTokens.colors — no raw hex — so light and dark are both authored.
// ===========================================================================

private val ReadoutEyebrowSpacing = 1.1.sp

/**
 * Tighter than a form clause: read-only content needs no room to write in.
 *
 * The end padding is wider than the rest so right-aligned header content — the
 * Profile chip, the cost-visibility marker — clears the 16dp dog-ear instead of
 * butting into the fold.
 */
internal val ReadoutClausePadding = PaddingValues(start = 14.dp, end = 20.dp, top = 12.dp, bottom = 12.dp)

/**
 * Spine colour for a docket card, keyed to the indent's status.
 *
 * Mirrors [IndentStatusChip] so the spine and the chip on the same record never
 * disagree about what colour "open" is.
 */
@Composable
fun docketStatusAccent(status: String): Color {
    val scheme = MaterialTheme.colorScheme
    val ext = FreightThemeTokens.colors
    return when (status.uppercase()) {
        "OPEN", "COMPLETED" -> ext.success
        "READY_FOR_ASSIGNMENT" -> scheme.primary
        "CANCELLED" -> scheme.error
        else -> scheme.outline
    }
}

// ---------------------------------------------------------------------------
// Ledger line
// ---------------------------------------------------------------------------

/** Dotted leader between a label and its value, the way a printed index runs. */
@Composable
private fun LeaderRule(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp),
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = size.height,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(1.5f, 4f), 0f),
        )
    }
}

/**
 * One printed fact: label, dotted leader, value — a single line.
 *
 * This is the density device. The previous details page spent an icon badge, a
 * label and a value on three stacked lines per fact (~48dp); a ledger line says
 * the same thing in one (~26dp), and the leader does the work the icon was
 * doing by holding the eye to the right edge.
 *
 * The label and value are both measured at their intrinsic width and the leader
 * takes what is left, so short values simply get a longer leader rather than
 * leaving a gap.
 */
@Composable
fun LedgerLine(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
    emphasis: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val blank = value.isNullOrBlank()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = ReadoutEyebrowSpacing),
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurfaceVariant,
            maxLines = 1,
        )
        Spacer(Modifier.width(8.dp))
        LeaderRule(Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (blank) "—" else value!!,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasis) FontWeight.Bold else FontWeight.SemiBold,
            color = if (blank) scheme.outline else scheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ---------------------------------------------------------------------------
// Seal
// ---------------------------------------------------------------------------

/**
 * The stamp on a lodged docket: a dashed ring — the edge of a rubber stamp —
 * around a solid disc carrying the tick.
 *
 * Deliberately not a Material "success circle": the dashed ring is what makes it
 * read as *stamped* rather than *celebrated*, which is the register this app
 * writes in.
 */
@Composable
fun DocketSeal(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
) {
    val ext = FreightThemeTokens.colors

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = this.size.minDimension / 2f - 1.5f
            drawCircle(
                color = ext.success.copy(alpha = 0.5f),
                radius = radius,
                style = Stroke(
                    width = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f),
                ),
            )
        }
        Box(
            modifier = Modifier
                .size(size * 0.68f)
                .background(ext.success, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Indent created",
                tint = ext.onSuccess,
                modifier = Modifier.size(size * 0.36f),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Lane strip
// ---------------------------------------------------------------------------

/** One stop on a read-only lane: marker, tracked label, value. */
@Composable
private fun LaneStripStop(
    label: String,
    value: String,
    markerColor: Color,
    isOrigin: Boolean,
    testTag: String?,
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (testTag != null) it.docketTag(testTag) else it },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(modifier = Modifier.size(11.dp)) {
            val radius = size.minDimension / 2f
            if (isOrigin) {
                drawCircle(color = markerColor, radius = radius, style = Stroke(width = 2.5f))
                drawCircle(color = markerColor, radius = radius * 0.34f)
            } else {
                drawCircle(color = markerColor, radius = radius)
                drawCircle(color = scheme.surface, radius = radius * 0.3f)
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = ReadoutEyebrowSpacing),
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * The route read back as a vertical lane.
 *
 * Not [LaneReadout]: that one closes with "Route is fixed once the indent is
 * raised", which is true on Review and Edit but wrong on a lodged docket and on
 * a list card. This is the same lane without the form-side footnote, and at
 * 11dp markers and a 14dp run it is the compact form.
 */
@Composable
fun LaneStrip(
    originLabel: String,
    originValue: String,
    destinationLabel: String,
    destinationValue: String,
    modifier: Modifier = Modifier,
    originTestTag: String? = null,
    destinationTestTag: String? = null,
) {
    val ext = FreightThemeTokens.colors

    Column(modifier = modifier.fillMaxWidth()) {
        LaneStripStop(
            label = originLabel,
            value = originValue,
            markerColor = ext.routeOrigin,
            isOrigin = true,
            testTag = originTestTag,
        )

        Canvas(
            modifier = Modifier
                .padding(start = 5.dp)
                .width(1.dp)
                .height(14.dp),
        ) {
            drawLine(
                color = ext.routeDestination.copy(alpha = 0.5f),
                start = Offset(size.width / 2f, 0f),
                end = Offset(size.width / 2f, size.height),
                strokeWidth = size.width,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 4f), 0f),
            )
        }

        LaneStripStop(
            label = destinationLabel,
            value = destinationValue,
            markerColor = ext.routeDestination,
            isOrigin = false,
            testTag = destinationTestTag,
        )
    }
}

// ---------------------------------------------------------------------------
// Call sheet — read back from the indent
// ---------------------------------------------------------------------------

/**
 * One line on a docket's call sheet.
 *
 * Deliberately not `IndentResponse.IndentContact`: a lodged indent carries the
 * recorded rows, but the create flow still holds `ClientContactV2Dto`s and the
 * success page has to print both. Normalising to this keeps one renderer.
 */
data class DocketCallLine(
    val roleLabel: String,
    val name: String,
    val phone: String?,
)

/** Recorded contact rows, in role order. */
fun List<IndentResponse.IndentContact>.toDocketCallLines(): List<DocketCallLine> {
    val byRole = associateBy { it.contactType?.uppercase().orEmpty() }
    return IndentContactRole.entries
        .mapNotNull { role -> byRole[role.name] }
        .map { contact ->
            DocketCallLine(
                roleLabel = contact.contactType?.let { type ->
                    IndentContactRole.entries.firstOrNull { it.name == type.uppercase() }?.label
                        ?: type
                } ?: "Contact",
                name = contact.contactName?.takeIf { it.isNotBlank() } ?: "—",
                phone = contact.contactPhone?.takeIf { it.isNotBlank() },
            )
        }
}

/**
 * The call sheet as recorded on the indent.
 *
 * Unlike the create-side roster this prints **only the roles that were
 * nominated**. On a form, showing all four slots is the point — it is how the
 * operator learns the optional ones exist. On a record, printing three empty
 * slots is noise; what matters is who to ring.
 *
 * The one exception is a missing `PLACEMENT`: an indent cannot be raised without
 * one, so its absence means this record predates contact roles. That is worth
 * saying out loud rather than silently omitting.
 */
@Composable
fun IndentContactReadout(
    lines: List<DocketCallLine>,
    modifier: Modifier = Modifier,
    onCall: ((String) -> Unit)? = null,
    testTagPrefix: String = "indent_details_contact",
) {
    val scheme = MaterialTheme.colorScheme
    val ext = FreightThemeTokens.colors

    // Roles print in whatever order the indent recorded them, so the gutter is
    // measured from the lines actually on this record.
    val stampGutter = rememberRoleStampGutter(lines.map { it.roleLabel })

    Column(modifier = modifier.fillMaxWidth()) {
        if (lines.isEmpty()) {
            Text(
                text = "No contacts recorded on this indent.",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.outline,
                modifier = Modifier.padding(vertical = 6.dp),
            )
            return@Column
        }

        lines.forEachIndexed { index, line ->
            val phone = line.phone

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .docketTag("${testTagPrefix}_${line.roleLabel.lowercase()}")
                    .padding(vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.width(stampGutter)) {
                    RoleStamp(
                        text = line.roleLabel,
                        container = ext.surfaceSuccess,
                        content = ext.onSurfaceSuccess,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = line.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (phone != null) {
                        Text(
                            text = formatReadoutPhone(phone),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
                if (phone != null && onCall != null) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = scheme.surfaceContainerHighest,
                        modifier = Modifier
                            .size(30.dp)
                            .docketTag("${testTagPrefix}_call_${line.roleLabel.lowercase()}")
                            .clickable { onCall(phone) },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call ${line.name}",
                                tint = ext.success,
                                modifier = Modifier.size(15.dp),
                            )
                        }
                    }
                }
            }

            if (index != lines.lastIndex) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = scheme.outlineVariant.copy(alpha = 0.45f),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Consignor clause
// ---------------------------------------------------------------------------

/**
 * Clause 01 of a lodged docket: who the consignment is for.
 *
 * This is the section that had to change. The client block used to print a
 * company name and a phone number, because a phone number was all an indent
 * carried. An indent now carries a **branch** and up to four **named contact
 * roles**, so the block prints the branch on its ledger and the call sheet
 * underneath — the two things an operator actually rings about.
 *
 * The branch prints "Not specified" rather than being hidden when absent: an
 * indent raised without one is a fact about the record, not a gap in the UI.
 */
@Composable
fun ConsignorClause(
    number: Int,
    clientName: String,
    tagCode: String?,
    branchName: String?,
    branchCode: String?,
    clientPhone: String?,
    callLines: List<DocketCallLine>,
    modifier: Modifier = Modifier,
    onViewProfileClick: (() -> Unit)? = null,
    onCall: ((String) -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val phone = clientPhone?.takeIf { it.isNotBlank() }
    val branchLabel = buildString {
        append(branchName?.takeIf { it.isNotBlank() } ?: "Not specified")
        branchCode?.takeIf { it.isNotBlank() }?.let { append("  ·  $it") }
    }

    ClauseDocket(
        number = number,
        title = "Consignor",
        state = ClauseState.EMPTY,
        testTag = "indent_details_consignor",
        modifier = modifier,
        contentPadding = ReadoutClausePadding,
        accent = scheme.outline,
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onViewProfileClick != null) {
                    Surface(
                        shape = RoundedChipShape,
                        color = scheme.surfaceContainerHighest,
                        modifier = Modifier
                            .docketTag("indent_details_view_profile")
                            .clickable { onViewProfileClick() },
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Profile",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = scheme.onSurfaceVariant,
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = scheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = clientName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            tagCode?.takeIf { it.isNotBlank() }?.let { tag ->
                Spacer(Modifier.width(8.dp))
                Surface(shape = TagShape, color = scheme.surfaceContainerHighest) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        LedgerLine(label = "Branch", value = branchLabel)
        LedgerLine(label = "Phone", value = phone?.let { formatReadoutPhone(it) })

        Spacer(Modifier.height(10.dp))
        PerforationRule()
        Spacer(Modifier.height(10.dp))

        Text(
            text = "CALL SHEET",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.3.sp),
            fontWeight = FontWeight.Bold,
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(2.dp))

        IndentContactReadout(
            lines = callLines,
            onCall = onCall,
            testTagPrefix = "indent_details_contact",
        )
    }
}

private val RoundedChipShape = RoundedCornerShape(50)

/** `+919880300304` → `+91 98803 00304`. Leaves anything unexpected alone. */
private fun formatReadoutPhone(phone: String): String {
    val digits = phone.filter { it.isDigit() }
    return when {
        digits.length == 10 -> "${digits.substring(0, 5)} ${digits.substring(5)}"
        digits.length == 12 && phone.startsWith("+") ->
            "+${digits.substring(0, 2)} ${digits.substring(2, 7)} ${digits.substring(7)}"
        else -> phone
    }
}
