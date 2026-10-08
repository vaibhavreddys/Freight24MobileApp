package com.freight24.connect.feature.indents.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.connect.R
import com.freight24.ui.components.DocketCard
import com.freight24.ui.components.MetaTag
import com.freight24.ui.components.PerforationRule
import com.freight24.ui.theme.FreightThemeTokens

// ===========================================================================
// The indent card, as a docket in a tray.
//
// The list is the one screen an operator scrolls all day, so the card is
// measured in how many indents fit on a screen. The previous card spent
// ~156dp on an icon-free header, a 52dp route indicator, three 32dp Material
// AssistChips and 32dp of padding. This one spends ~119dp on the same
// information:
//
//   • padding 16 → 10
//   • the route indicator's 12dp dots and 28dp run → 8dp markers and a 10dp
//     dashed run, so the lane still reads as a lane at half the height
//   • three AssistChips (32dp) → three MetaTags (20dp), which is also what the
//     rest of the docket language uses for stamps
//   • the status spine carries status instead of a chip taking a row
//
// Nothing was dropped: company, code, age, both costs, both cities, truck type,
// weight and material all still print, and the admin/clone footer is unchanged.
// ===========================================================================

@Composable
fun ConnectIndentCard(
    companyName: String,
    indentCode: String?,
    postedTime: String,
    status: String,
    fromCity: String,
    toCity: String,
    clientCostFormatted: String?,
    negotiatedCostFormatted: String?,
    truckDisplay: String,
    weightDisplay: String,
    materialDisplay: String,
    interestCount: Int? = null,
    showAdmin: Boolean = false,
    adminName: String? = null,
    onClick: () -> Unit,
    onCloneClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    onCallClick: (() -> Unit)? = null,
    ownershipLabel: String? = null,
    isMine: Boolean = false,
    muted: Boolean = false,
    /**
     * Optional detachable footer, rendered below the cargo row behind its own
     * [PerforationRule]. The Interest Inbox uses this to hang its expandable
     * trucker list off the docket, so both screens share one card rather than
     * the inbox growing a second, divergent card of its own.
     */
    footer: (@Composable () -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val visibleClientCost = clientCostFormatted?.takeIf { it.hasPositiveCost() }
    val visibleNegotiatedCost = negotiatedCostFormatted?.takeIf { it.hasPositiveCost() }
    val showClientCost = visibleClientCost != null
    val showNegotiatedCost = visibleNegotiatedCost != null

    DocketCard(
        accent = docketStatusAccent(status),
        containerColor = if (muted) scheme.surfaceContainerLow else scheme.surface,
        testTag = "indent_card",
        onClick = onClick,
        modifier = modifier.padding(horizontal = 12.dp, vertical = 5.dp),
        // End padding is wider than the rest so right-aligned content clears the
        // 16dp dog-ear. At 12dp the age tag ran into the fold and the two greys
        // merged into one notch.
        contentPadding = PaddingValues(start = 12.dp, end = 18.dp, top = 10.dp, bottom = 10.dp),
    ) {
        // --- Header: consignor, docket number, age -------------------------
        // The code and the age tag are measured before the name, so a long
        // company name truncates and the docket number never does. The number
        // is what an operator reads out; the name they already know.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = companyName,
                style = MaterialTheme.typography.titleSmall.copy(letterSpacing = (-0.2).sp),
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (indentCode != null) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = indentCode,
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = (-0.1).sp),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.width(8.dp))
            MetaTag(
                text = postedTime,
                container = scheme.surfaceContainerHighest,
                content = scheme.onSurfaceVariant,
            )
        }

        if (ownershipLabel != null) {
            Spacer(Modifier.height(6.dp))
            MetaTag(
                text = ownershipLabel,
                container = if (isMine) scheme.primaryContainer else scheme.surfaceContainerHighest,
                content = if (isMine) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(7.dp))

        // --- Lane and money ------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            CompactLane(
                fromCity = fromCity,
                toCity = toCity,
                modifier = Modifier.weight(1f),
            )

            if (showClientCost || showNegotiatedCost) {
                Spacer(Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.End) {
                    if (showClientCost) {
                        CostLine(label = stringResource(R.string.client_cost), value = visibleClientCost)
                    }
                    if (showClientCost && showNegotiatedCost) {
                        Spacer(Modifier.height(4.dp))
                    }
                    if (showNegotiatedCost) {
                        CostLine(
                            label = stringResource(R.string.target_cost),
                            value = visibleNegotiatedCost,
                            emphasised = true,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // --- Cargo ---------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MetaTag(
                text = truckDisplay,
                container = scheme.surfaceContainerHighest,
                content = scheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(4.dp))
            MetaTag(
                text = weightDisplay,
                container = scheme.surfaceContainerHighest,
                content = scheme.onSurfaceVariant,
            )
            if (materialDisplay.isNotBlank()) {
                Spacer(Modifier.width(4.dp))
                MetaTag(
                    text = materialDisplay,
                    container = scheme.surfaceContainerHighest,
                    content = scheme.onSurfaceVariant,
                )
            }

            if (interestCount != null && interestCount > 0) {
                Spacer(Modifier.weight(1f))
                MetaTag(
                    text = if (interestCount == 1) "1 interested" else "$interestCount interested",
                    container = scheme.primaryContainer,
                    content = scheme.onPrimaryContainer,
                )
            }

            if (onCallClick != null) {
                if (interestCount == null || interestCount <= 0) {
                    Spacer(Modifier.weight(1f))
                } else {
                    Spacer(Modifier.width(4.dp))
                }
                CallPill(onClick = onCallClick)
            }
        }

        // --- Admin footer --------------------------------------------------
        // Only on indents raised by someone else, so it costs nothing on the
        // common case. The tear line keeps it reading as a detachable footer.
        if (showAdmin) {
            Spacer(Modifier.height(9.dp))
            PerforationRule()
            Spacer(Modifier.height(9.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (adminName != null) {
                    Text(
                        text = "Raised by $adminName",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = scheme.surfaceContainerHighest,
                    modifier =
                        Modifier
                            .docketTag("indent_card_clone")
                            .clickable(onClick = onCloneClick),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Clone indent",
                            modifier = Modifier.size(13.dp),
                            tint = scheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "Clone",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // --- Caller-supplied footer ----------------------------------------
        // Same tear-line treatment as the admin footer above, so a card that
        // grows a footer still reads as one detachable docket.
        footer?.let {
            Spacer(Modifier.height(9.dp))
            PerforationRule()
            Spacer(Modifier.height(7.dp))
            it()
        }
    }
}

private fun String.hasPositiveCost(): Boolean =
    filter { it.isDigit() || it == '.' || it == '-' }
        .toDoubleOrNull()
        ?.let { it > 0.0 }
        ?: false

/**
 * The route at card scale: an 8dp pickup ring, a 10dp dashed run, an 8dp
 * delivery disc, then the two cities.
 *
 * The run is drawn rather than laid out with a 4dp gradient bar so it can stay
 * short — the lane's identity is the ring/disc/dashed vocabulary, not its
 * length, and at card scale the length is what costs the row its height.
 */
@Composable
private fun CompactLane(
    fromCity: String,
    toCity: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val ext = FreightThemeTokens.colors

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.width(9.dp).height(34.dp)) {
            val cx = size.width / 2f
            val r = size.width / 2f

            drawCircle(
                color = ext.routeOrigin,
                radius = r,
                center = Offset(cx, r),
                style = Stroke(width = 2.5f),
            )
            drawCircle(
                color = ext.routeOrigin,
                radius = r * 0.34f,
                center = Offset(cx, r),
            )
            drawLine(
                color = ext.routeDestination.copy(alpha = 0.5f),
                start = Offset(cx, r * 2 + 2f),
                end = Offset(cx, size.height - r * 2 - 2f),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f), 0f),
            )
            drawCircle(
                color = ext.routeDestination,
                radius = r,
                center = Offset(cx, size.height - r),
            )
            drawCircle(
                color = scheme.surface,
                radius = r * 0.34f,
                center = Offset(cx, size.height - r),
            )
        }

        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fromCity,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = toCity,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * A cost as one line: tracked label, then the amount.
 *
 * Stacking label over value cost 36dp per cost and made the money column 78dp
 * tall — taller than the lane beside it, so it alone decided the card's height.
 * On one line it is 20dp.
 *
 * The label column is sized by the widest row rather than pinned to a fixed
 * width: the parent Column already does that, and a fixed width clipped
 * "NEGOTIATED" to "NEGOTIATE" on a 360dp screen.
 */
@Composable
private fun CostLine(
    label: String,
    value: String,
    emphasised: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.7.sp),
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurfaceVariant,
            maxLines = 1,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (emphasised) scheme.primary else scheme.onSurface,
            maxLines = 1,
        )
    }
}

/**
 * The call action, sized to sit on the cargo row rather than below it.
 *
 * Keeps the word "Call" — an icon-only disc this small reads as decoration, and
 * the operator is about to place a phone call.
 */
@Composable
private fun CallPill(onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = scheme.primary,
        modifier =
            Modifier
                .docketTag("indent_card_call")
                .clickable(onClick = onClick)
                .semantics { contentDescription = "Call client" },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = scheme.onPrimary,
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = "Call",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimary,
            )
        }
    }
}
