package com.freight24.ui.trucker

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToLong

// ---------------------------------------------------------------------------
// Freight24 Trucker — fleet operations design language.
//
// This is the shared visual system for the post-login fleet surfaces in
// app-trucker (Trips and Trucks). It extends TruckerDesign.kt, which owns
// the palette, the type scale and the sizing contract; this file adds the
// *domain* vocabulary those screens need and nothing else.
//
// The vocabulary is borrowed from the physical paperwork and hardware of an
// Indian trucking operation, because that is the world these screens are
// actually about:
//
//   * NUMBER PLATE  — a truck is identified by its registration, so the
//     registration is set in a bordered, letter-spaced plate rather than
//     buried in a body line. It is the fastest thing on the card to read at
//     arm's length in a yard.
//
//   * ROAD SPINE    — a route is drawn as a dashed centre line with a rig
//     riding it, not as an abstract dot-to-dot. Origin is a solid pin,
//     destination is a flag.
//
//   * SIDING SPINE  — the coloured band down the left edge of every card is
//     the truck's siding: which bay it is parked in. Hue carries state so
//     the eye can sweep a list without reading.
//
//   * HAZARD RULE   — the diagonal hatch used on loading docks and dock
//     levellers, used as the section break instead of a hairline.
//
//   * STAMP SEAL    — verification states are rubber stamps (VERIFIED /
//     PENDING / REJECTED), tilted a few degrees, because that is literally
//     how an LR is cleared at a checkpost.
//
//   * FREIGHT BILL  — money is rendered as a ledger: label, dotted leader,
//     amount. Additions and deductions read as columns of an invoice.
//
//   * RIG SILHOUETTE — the cargo body is drawn, not iconed, and varies with
//     the body type (container / open / trailer), so the shape carries
//     information rather than decoration.
//
// Everything resolves through [TruckerColors] (defined once in
// TruckerDesign.kt) or MaterialTheme.colorScheme roles. No hex at call sites.
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// Shape & spacing tokens
// ---------------------------------------------------------------------------

val FleetCardShape = RoundedCornerShape(18.dp)
val FleetCellShape = RoundedCornerShape(12.dp)
private val PlateShape = RoundedCornerShape(7.dp)
private val StampShape = RoundedCornerShape(5.dp)
private val RailShape = RoundedCornerShape(4.dp)

object FleetSizes {
    val cardPaddingStart = 18.dp
    val cardPaddingEnd = 16.dp
    val cardPaddingTop = 15.dp
    val cardPaddingBottom = 15.dp
    val spine = 6.dp
    val row = 56.dp
}

// ---------------------------------------------------------------------------
// Semantic tones
// ---------------------------------------------------------------------------

/**
 * The four semantic hues the fleet surfaces are allowed to use. They collapse
 * onto the existing trucker palette — verified states take the brand forest,
 * in-flight states take saffron, failures take red, dormant states take the
 * muted mid-tone. Nothing else is introduced.
 */
enum class FleetTone {
    BRAND,
    ACCENT,
    ERROR,
    MUTED,
}

@Composable
fun FleetTone.color(colors: TruckerColors): Color =
    when (this) {
        FleetTone.BRAND -> colors.brand
        FleetTone.ACCENT -> colors.accent
        FleetTone.ERROR -> colors.error
        FleetTone.MUTED -> colors.inkMuted
    }

@Composable
fun FleetTone.wash(colors: TruckerColors): Color =
    color(colors).copy(alpha = 0.14f)

// ---------------------------------------------------------------------------
// Money — Indian digit grouping (lakh / crore), not Western thousands
// ---------------------------------------------------------------------------

/**
 * Formats a rupee amount with Indian grouping: 3240000 -> "32,40,000".
 * Drivers read freight in lakhs; Western grouping actively misleads here.
 */
fun formatInrAmount(value: Double): String {
    val rounded = value.roundToLong()
    val sign = if (rounded < 0) "-" else ""
    val digits = abs(rounded).toString()
    if (digits.length <= 3) return "$sign$digits"
    val groups = mutableListOf(digits.takeLast(3))
    var rest = digits.dropLast(3)
    while (rest.length > 2) {
        groups.add(0, rest.takeLast(2))
        rest = rest.dropLast(2)
    }
    if (rest.isNotEmpty()) groups.add(0, rest)
    return sign + groups.joinToString(",")
}

/** Amount with the rupee sign, ready to drop into a ledger row. */
fun inr(value: Double): String = "\u20B9 ${formatInrAmount(value)}"

// ---------------------------------------------------------------------------
// Press feedback — a physical sink, spring-driven
// ---------------------------------------------------------------------------

/** Sinks the surface slightly under the thumb. Spring, not a tween. */
@Composable
fun Modifier.fleetPress(interactionSource: MutableInteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.975f else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 420f),
        label = "fleet-press",
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

// ---------------------------------------------------------------------------
// Fleet card — the raised surface with an optional siding spine
// ---------------------------------------------------------------------------

/**
 * The one container used by every card on the fleet surfaces.
 *
 * Depth comes from tonal layering (canvas -> surface -> border), not shadow.
 * [spine] paints a full-height band down the left edge in the state hue —
 * the siding — so a list can be scanned by colour before it is read.
 */
@Composable
fun FleetCard(
    colors: TruckerColors,
    modifier: Modifier = Modifier,
    spine: Color? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = FleetCardShape
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.surface)
                // drawBehind sits after clip, so the spine is clipped to the
                // card's rounded corners without a second shape.
                .then(
                    if (spine != null) {
                        Modifier.drawSpine(spine)
                    } else {
                        Modifier
                    },
                )
                .border(width = 1.5.dp, color = colors.line, shape = shape),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = if (spine != null) FleetSizes.cardPaddingStart else FleetSizes.cardPaddingEnd,
                        end = FleetSizes.cardPaddingEnd,
                        top = FleetSizes.cardPaddingTop,
                        bottom = FleetSizes.cardPaddingBottom,
                    ),
            content = content,
        )
    }
}

private fun Modifier.drawSpine(color: Color): Modifier =
    this.then(
        Modifier.drawBehind {
            drawRect(
                color = color,
                topLeft = Offset.Zero,
                size = Size(FleetSizes.spine.toPx(), size.height),
            )
        },
    )

// ---------------------------------------------------------------------------
// Number plate
// ---------------------------------------------------------------------------

/**
 * Registration number set as a plate: a hard border and wide letter-spacing
 * in the display face on the bare surface. No fill — the outline alone is
 * enough to read as a plate, and a filled chip on every row turns a list of
 * twenty trucks into twenty green boxes.
 *
 * [caption] carries the body type underneath. Pass a brand [border] where
 * the truck is the subject of the screen rather than one row among many.
 */
@Composable
fun PlateBadge(
    registration: String,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
    caption: String? = null,
    border: Color = colors.line,
) {
    Column(modifier = modifier) {
        Box(
            modifier =
                Modifier
                    .clip(PlateShape)
                    .border(width = 1.5.dp, color = border, shape = PlateShape)
                    .padding(horizontal = 9.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = registration.uppercase(),
                style = TruckerType.captionStrong.copy(letterSpacing = 1.2.sp),
                color = colors.ink,
                maxLines = 1,
            )
        }
        if (!caption.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = caption,
                style = TruckerType.caption,
                color = colors.inkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Larger plate for detail headers, where the truck is the subject. */
@Composable
fun PlateBadgeLarge(
    registration: String,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .clip(PlateShape)
                .border(width = 1.5.dp, color = colors.brand, shape = PlateShape)
                .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = registration.uppercase(),
            style = TruckerType.digit.copy(letterSpacing = 1.6.sp),
            color = colors.ink,
            maxLines = 1,
        )
    }
}

// ---------------------------------------------------------------------------
// Road spine — origin pin, dashed centre line, destination flag
// ---------------------------------------------------------------------------

/**
 * A route drawn as the road it runs on: two stops with a dashed centre line
 * between them, filled in the brand hue up to the trip's current progress
 * and left in the outline hue for the rest.
 *
 * Deliberately has no vehicle glyph riding the line — the dashed fill
 * already carries "how far along", and a second element on a 2dp rule is
 * noise at card size.
 */
@Composable
fun RoadSpine(
    from: String,
    to: String,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
    progress: Float = 1f,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = from,
            style = TruckerType.bodyStrong,
            color = colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Start,
        )
        Spacer(modifier = Modifier.width(12.dp))
        DashedRoad(
            colors = colors,
            progress = progress,
            modifier =
                Modifier
                    .weight(1f)
                    .widthIn(min = 24.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = to,
            style = TruckerType.bodyStrong,
            color = colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun DashedRoad(
    colors: TruckerColors,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val dash = PathEffect.dashPathEffect(floatArrayOf(9f, 8f), 0f)
    Canvas(modifier = modifier.height(2.dp)) {
        val midY = size.height / 2f
        val split = size.width * progress.coerceIn(0f, 1f)
        drawLine(
            color = colors.brand,
            start = Offset(0f, midY),
            end = Offset(split, midY),
            strokeWidth = size.height,
            pathEffect = dash,
        )
        drawLine(
            color = colors.line,
            start = Offset(split, midY),
            end = Offset(size.width, midY),
            strokeWidth = size.height,
            pathEffect = dash,
        )
    }
}

// ---------------------------------------------------------------------------
// Rule — a plain hairline, the only divider the fleet surfaces use
// ---------------------------------------------------------------------------

/**
 * A 1dp hairline. Section breaks are carried by whitespace and the header
 * rule; an ornamental divider (a hazard hatch, a double rule) earns its
 * keep on a dock, not on a phone.
 */
@Composable
fun FleetRule(
    colors: TruckerColors,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.line),
    )
}

// ---------------------------------------------------------------------------
// Stage rail — the trip's progress along the siding
// ---------------------------------------------------------------------------

private val TRIP_STAGES = listOf("ASSIGNED", "LOADING", "IN_TRANSIT", "UNLOADED", "INVOICED", "PAID")

private val TRIP_STAGE_LABELS = listOf("Booked", "Loading", "Transit", "Unload", "Billed", "Paid")

/** Normalises the wire's status strings onto the stage list. */
fun tripStageIndex(status: String): Int {
    val normalised = status.uppercase().replace(" ", "_").replace("-", "_")
    return TRIP_STAGES.indexOf(normalised).coerceAtLeast(-1)
}

fun tripStageCount(): Int = TRIP_STAGES.size

/** Short, human stage name for the stage at [index]. */
fun tripStageLabel(index: Int): String = TRIP_STAGE_LABELS.getOrNull(index) ?: ""

/**
 * Segmented rail: one block per stage, filled up to the current one.
 * The block at the cursor is painted in the accent so "where it is" reads
 * at a glance, and the remaining blocks stay in the outline tone.
 */
@Composable
fun StageRail(
    currentIndex: Int,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
    segments: Int = TRIP_STAGES.size,
    height: Dp = 4.dp,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(segments) { index ->
            val tone =
                when {
                    index < currentIndex -> colors.brand
                    index == currentIndex -> colors.accent
                    else -> colors.line
                }
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(if (index == currentIndex) height + 2.dp else height)
                        .clip(RailShape)
                        .background(tone),
            )
        }
    }
}

/**
 * The same rail with its stage names underneath. Used once, on the trip
 * detail hero, where there is room to label every bay.
 */
@Composable
fun LabelledStageRail(
    currentIndex: Int,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(TRIP_STAGES.size) { index ->
                val tone =
                    when {
                        index < currentIndex -> colors.brand
                        index == currentIndex -> colors.accent
                        else -> colors.line
                    }
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(if (index == currentIndex) 6.dp else 4.dp)
                            .clip(RailShape)
                            .background(tone),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            TRIP_STAGE_LABELS.forEachIndexed { index, label ->
                val isDone = index <= currentIndex
                Text(
                    text = label,
                    style = TruckerType.caption,
                    color = if (isDone) colors.ink else colors.inkMuted,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Stamp seal — how an LR is actually cleared
// ---------------------------------------------------------------------------

/**
 * A rubber stamp: outlined, slightly tilted, all-caps. Used for document
 * and verification states in place of a filled status pill — an outline
 * reads as "applied to this document", a fill reads as "another button".
 */
@Composable
fun StampSeal(
    text: String,
    tone: FleetTone,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
) {
    val color = tone.color(colors)
    Box(
        modifier =
            modifier
                .clip(StampShape)
                .border(width = 1.5.dp, color = color, shape = StampShape)
                .padding(horizontal = 9.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.uppercase(),
            style = TruckerType.caption,
            color = color,
            maxLines = 1,
        )
    }
}

// ---------------------------------------------------------------------------
// Ledger row — the freight bill
// ---------------------------------------------------------------------------

private val LeaderEffect = PathEffect.dashPathEffect(floatArrayOf(1.5f, 4f), 0f)

/**
 * One line of a freight bill: label, dotted leader, amount.
 *
 * [emphasis] widens the type for subtotals; [tone] carries the sign
 * convention — brand for additions, error for deductions, ink otherwise.
 */
@Composable
fun LedgerRow(
    label: String,
    amount: String,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
    detail: String? = null,
    tone: FleetTone? = null,
    emphasis: Boolean = false,
) {
    val amountColor = tone?.color(colors) ?: colors.ink
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = label,
                style = if (emphasis) TruckerType.bodyStrong else TruckerType.body,
                color = if (emphasis) colors.ink else colors.inkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Canvas(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(16.dp),
            ) {
                drawLine(
                    color = colors.line,
                    start = Offset(0f, size.height - 4.dp.toPx()),
                    end = Offset(size.width, size.height - 4.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = LeaderEffect,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = amount,
                style = if (emphasis) TruckerType.title else TruckerType.bodyStrong,
                color = amountColor,
                maxLines = 1,
            )
        }
        if (!detail.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = detail,
                style = TruckerType.caption,
                color = colors.inkMuted,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Section header
// ---------------------------------------------------------------------------

@Composable
fun FleetSectionHeader(
    title: String,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .width(3.dp)
                    .height(16.dp)
                    .clip(RailShape)
                    .background(colors.brand),
        )
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = title,
            style = TruckerType.title,
            color = colors.ink,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}

/** Quiet support line used under headers and in empty slots. */
@Composable
fun FleetNote(
    text: String,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = TruckerType.body,
        color = colors.inkMuted,
        modifier = modifier,
    )
}

// ---------------------------------------------------------------------------
// Notice strip — the banner above the trip action bar
// ---------------------------------------------------------------------------

@Composable
fun FleetNotice(
    text: String,
    tone: FleetTone,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
) {
    val color = tone.color(colors)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(FleetCellShape)
                .background(tone.wash(colors))
                .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (tone == FleetTone.ERROR) Icons.Filled.ErrorOutline else Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = TruckerType.body,
            color = colors.ink,
            modifier = Modifier.weight(1f),
        )
    }
}

// ---------------------------------------------------------------------------
// Rig silhouette — drawn, not iconed, and shaped by the cargo body
// ---------------------------------------------------------------------------

/**
 * Cargo body variants. The silhouette changes with the body type so the
 * drawing carries data: a container reads as a closed corrugated box, an
 * open body reads as low side walls, a trailer reads as a full box.
 */
enum class RigShape {
    OPEN,
    CONTAINER,
    TRAILER,
}

/** Maps the wire's truck type / body type strings onto a silhouette. */
fun rigShapeFor(truckType: String?, bodyType: String?): RigShape {
    val haystack = "${truckType.orEmpty()} ${bodyType.orEmpty()}".lowercase()
    return when {
        haystack.contains("container") -> RigShape.CONTAINER
        haystack.contains("open") -> RigShape.OPEN
        haystack.contains("trailer") -> RigShape.TRAILER
        else -> RigShape.TRAILER
    }
}

/**
 * A rig in profile: cargo body, cab with a sloped windscreen, chassis, two
 * wheels. Drawn from primitives so it stays crisp at 15dp on a card and at
 * 120dp on a detail header.
 */
@Composable
fun RigSilhouette(
    shape: RigShape,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = (h * 0.10f).coerceAtLeast(1f)

        val groundY = h * 0.74f
        val cargoLeft = w * 0.02f
        val cargoRight = w * 0.58f

        // Cargo body
        when (shape) {
            RigShape.OPEN -> {
                // Low side walls only — an open body is a bed, not a box.
                val wallTop = h * 0.40f
                drawPath(
                    path =
                        Path().apply {
                            moveTo(cargoLeft, wallTop)
                            lineTo(cargoLeft, groundY)
                            lineTo(cargoRight, groundY)
                            lineTo(cargoRight, wallTop)
                        },
                    color = tint,
                    style = Stroke(width = strokeW),
                )
            }
            else -> {
                val cargoTop = if (shape == RigShape.CONTAINER) h * 0.10f else h * 0.14f
                drawRoundRect(
                    color = tint.copy(alpha = 0.18f),
                    topLeft = Offset(cargoLeft, cargoTop),
                    size = Size(cargoRight - cargoLeft, groundY - cargoTop),
                    cornerRadius = CornerRadius(h * 0.08f, h * 0.08f),
                )
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(cargoLeft, cargoTop),
                    size = Size(cargoRight - cargoLeft, groundY - cargoTop),
                    cornerRadius = CornerRadius(h * 0.08f, h * 0.08f),
                    style = Stroke(width = strokeW),
                )
                if (shape == RigShape.CONTAINER) {
                    // Corrugation — the giveaway that it is a box, not a tarp.
                    val inner = cargoRight - cargoLeft - strokeW * 2f
                    for (i in 1..4) {
                        val x = cargoLeft + strokeW + inner * i / 5f
                        drawLine(
                            color = tint.copy(alpha = 0.5f),
                            start = Offset(x, cargoTop + strokeW),
                            end = Offset(x, groundY - strokeW),
                            strokeWidth = strokeW * 0.5f,
                        )
                    }
                }
            }
        }

        // Cab — sloped windscreen, filled.
        val cabLeft = w * 0.64f
        val cabRight = w * 0.99f
        val cabTop = h * 0.26f
        drawPath(
            path =
                Path().apply {
                    moveTo(cabLeft, groundY)
                    lineTo(cabLeft, cabTop + h * 0.08f)
                    lineTo(cabRight - h * 0.16f, cabTop + h * 0.08f)
                    lineTo(cabRight, cabTop + h * 0.34f)
                    lineTo(cabRight, groundY)
                    close()
                },
            color = tint,
        )

        // Chassis
        drawLine(
            color = tint,
            start = Offset(w * 0.02f, groundY),
            end = Offset(w * 0.99f, groundY),
            strokeWidth = strokeW,
        )

        // Wheels
        val radius = h * 0.15f
        drawCircle(color = tint, radius = radius, center = Offset(w * 0.24f, groundY + radius * 0.45f))
        drawCircle(color = tint, radius = radius, center = Offset(w * 0.80f, groundY + radius * 0.45f))
    }
}

// ---------------------------------------------------------------------------
// Trip log — the checkpoint ledger
// ---------------------------------------------------------------------------

/**
 * One entry in the trip log. [state] drives the marker: cleared, current, or
 * not yet reached. The connector is solid behind the rig and dashed ahead of
 * it, matching the road spine.
 */
@Composable
fun TripLogRow(
    title: String,
    description: String,
    time: String,
    state: LogState,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
    isLast: Boolean = false,
) {
    val markerColor =
        when (state) {
            LogState.CLEARED -> colors.brand
            LogState.CURRENT -> colors.accent
            LogState.AHEAD -> colors.line
        }
    val bodyColor =
        when (state) {
            LogState.AHEAD -> colors.inkMuted
            else -> colors.ink
        }
    // Current checkpoint gets a soft wash so "where it stands" reads first.
    val contentModifier =
        if (state == LogState.CURRENT) {
            Modifier
                .clip(FleetCellShape)
                .background(state.let { markerColor.copy(alpha = 0.12f) })
                .padding(horizontal = 12.dp, vertical = 10.dp)
        } else {
            Modifier.padding(vertical = 2.dp)
        }

    Row(
        modifier = modifier.fillMaxWidth(),
    ) {
        // Marker column + connector. The connector stretches to the content
        // height so two-line descriptions don't break the timeline.
        Column(
            modifier =
                Modifier
                    .width(22.dp)
                    .padding(top = if (state == LogState.CURRENT) 12.dp else 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(if (state == LogState.CURRENT) 16.dp else 14.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(if (state == LogState.AHEAD) Color.Transparent else markerColor)
                        .border(
                            width = 2.dp,
                            color = markerColor,
                            shape = androidx.compose.foundation.shape.CircleShape,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                if (state == LogState.CURRENT) {
                    Box(
                        modifier =
                            Modifier
                                .size(6.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(colors.surface),
                    )
                }
            }
            if (!isLast) {
                val dashed = state != LogState.CLEARED
                Spacer(modifier = Modifier.height(6.dp))
                Canvas(
                    modifier =
                        Modifier
                            .width(2.dp)
                            .height(48.dp),
                ) {
                    drawLine(
                        color = markerColor,
                        start = Offset(size.width / 2f, 0f),
                        end = Offset(size.width / 2f, size.height),
                        strokeWidth = size.width,
                        pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f) else null,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(bottom = if (isLast) 0.dp else 12.dp),
        ) {
            Box(modifier = contentModifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = TruckerType.bodyStrong,
                            color = bodyColor,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (time.isNotBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = time,
                                style = TruckerType.caption,
                                color = colors.inkMuted,
                                maxLines = 1,
                                textAlign = TextAlign.End,
                            )
                        }
                    }
                    if (description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = description,
                            style = TruckerType.caption,
                            color = if (state == LogState.AHEAD) colors.inkMuted else colors.ink.copy(alpha = 0.72f),
                        )
                    }
                }
            }
        }
    }
}

enum class LogState {
    CLEARED,
    CURRENT,
    AHEAD,
}

/**
 * Remembers a fresh interaction source per card, so press feedback does not
 * leak between list rows.
 */
@Composable
fun rememberFleetInteraction(): MutableInteractionSource = remember { MutableInteractionSource() }

// ---------------------------------------------------------------------------
// Siding hue for a truck's availability
// ---------------------------------------------------------------------------

/**
 * The yard colours a truck by where it is parked: forest when it is free to
 * take work, saffron when it is out on a trip, red when it is blacklisted,
 * and the muted mid-tone when it is not in service.
 */
fun truckSidingTone(
    availability: String,
    isBlacklisted: Boolean,
): FleetTone {
    if (isBlacklisted) return FleetTone.ERROR
    return when (availability.uppercase()) {
        "AVAILABLE", "ACTIVE" -> FleetTone.BRAND
        "ON_TRIP" -> FleetTone.ACCENT
        else -> FleetTone.MUTED
    }
}

fun truckSidingLabel(
    availability: String,
    isBlacklisted: Boolean,
): String {
    if (isBlacklisted) return "Blacklisted"
    return when (availability.uppercase()) {
        "AVAILABLE", "ACTIVE" -> "Available"
        "ON_TRIP" -> "On trip"
        else -> "Off duty"
    }
}

// ---------------------------------------------------------------------------
// Spec cell — one stamped field on a truck's data plate
// ---------------------------------------------------------------------------

/**
 * A label over a value inside an outline. Used in pairs across a row for the
 * truck's specifications: body, make, model, class, year.
 */
@Composable
fun SpecCell(
    label: String,
    value: String,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clip(FleetCellShape)
                .border(width = 1.5.dp, color = colors.line, shape = FleetCellShape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = TruckerType.caption,
            color = colors.inkMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = value,
            style = TruckerType.bodyStrong,
            color = colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ---------------------------------------------------------------------------
// Verification line
// ---------------------------------------------------------------------------

/** One line stating where the truck's paperwork stands. */
@Composable
fun VerificationLine(
    isVerified: Boolean,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
) {
    val tone = if (isVerified) FleetTone.BRAND else FleetTone.ACCENT
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isVerified) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = tone.color(colors),
            modifier = Modifier.size(17.dp),
        )
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = if (isVerified) "Documents verified" else "Verification pending",
            style = TruckerType.caption,
            color = tone.color(colors),
            maxLines = 1,
        )
    }
}

// ---------------------------------------------------------------------------
// Top bar
// ---------------------------------------------------------------------------

/**
 * Detail top bar on the fleet canvas: back affordance, title, and an
 * optional second line (the trip code, the plate) so the driver always knows
 * which record they are inside.
 */
@Composable
fun FleetTopBar(
    title: String,
    colors: TruckerColors,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    backTestTag: String? = null,
    actions: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(colors.canvas)
                .statusBarsPadding()
                .padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TruckerBackButton(
            colors = colors,
            onClick = onBackClick,
            modifier = if (backTestTag != null) Modifier.testTag(backTestTag).semantics { testTagsAsResourceId = true } else Modifier,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TruckerType.title,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = TruckerType.caption,
                    color = colors.inkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (actions != null) {
            actions()
        }
    }
}

// ---------------------------------------------------------------------------
// Document wallet row
// ---------------------------------------------------------------------------

/**
 * One document in the wallet: a glyph tile, the document's name and state in
 * words, and either a stamp (once it is lodged) or an upload affordance.
 *
 * [onRowClick] is wired only when there is something to open, so an
 * un-uploaded document never looks tappable.
 */
@Composable
fun DocWalletRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
    sealText: String? = null,
    sealTone: FleetTone = FleetTone.MUTED,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    actionContentDescription: String? = null,
    onActionClick: (() -> Unit)? = null,
    onRowClick: (() -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(FleetCellShape)
                .background(colors.surface)
                .border(width = 1.5.dp, color = colors.line, shape = FleetCellShape)
                .then(
                    if (onRowClick != null) Modifier.clickable(onClick = onRowClick) else Modifier,
                )
                .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(42.dp)
                    .clip(FleetCellShape)
                    .background(colors.brandSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.brand,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TruckerType.bodyStrong,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                style = TruckerType.caption,
                color = colors.inkMuted,
                maxLines = 2,
            )
        }
        if (sealText != null) {
            Spacer(modifier = Modifier.width(10.dp))
            StampSeal(text = sealText, tone = sealTone, colors = colors)
        }
        if (actionIcon != null && onActionClick != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(colors.brandSoft)
                        .clickable(onClick = onActionClick)
                        .semantics { contentDescription = actionContentDescription ?: "" },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = actionContentDescription,
                    tint = if (sealTone == FleetTone.ERROR) colors.error else colors.brand,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Filter rail — a manifest tab strip, not a bag of chips
// ---------------------------------------------------------------------------

/** One bay in the filter rail. [count] is optional; POD and ACTIVE carry one. */
data class FleetFilterEntry(
    val label: String,
    val count: Int? = null,
)

/**
 * A single bordered strip of segments with the counts set as tally badges.
 * Segments scroll rather than shrink, so a long bay name never gets clipped
 * on a narrow phone.
 */
@Composable
fun FleetFilterRail(
    entries: List<FleetFilterEntry>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clip(FleetCellShape)
                .background(colors.surface)
                .border(width = 1.5.dp, color = colors.line, shape = FleetCellShape)
                .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        entries.forEachIndexed { index, entry ->
            if (index > 0) {
                Box(
                    modifier =
                        Modifier
                            .width(1.dp)
                            .height(18.dp)
                            .background(colors.line),
                )
            }
            val selected = index == selectedIndex
            Row(
                modifier =
                    Modifier
                        .clip(FleetCellShape)
                        .background(if (selected) colors.brand else Color.Transparent)
                        .clickable { onSelect(index) }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = entry.label,
                    style = TruckerType.captionStrong,
                    color = if (selected) colors.onBrand else colors.inkMuted,
                    maxLines = 1,
                )
                if (entry.count != null) {
                    Spacer(modifier = Modifier.width(7.dp))
                    Box(
                        modifier =
                            Modifier
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(if (selected) colors.surface else colors.brandSoft)
                                .padding(horizontal = 6.dp, vertical = 1.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = entry.count.toString(),
                            style = TruckerType.caption,
                            color = if (selected) colors.brand else colors.ink,
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Search field
// ---------------------------------------------------------------------------

/**
 * A single outlined field. Uses the platform BasicTextField so it inherits
 * the fleet type scale rather than Material's smaller default.
 */
@Composable
fun FleetSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(FleetCellShape)
                .background(colors.surface)
                .border(width = 1.5.dp, color = colors.line, shape = FleetCellShape)
                .padding(horizontal = 14.dp)
                .height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = colors.inkMuted,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TruckerType.body.copy(color = colors.ink),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.brand),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = TruckerType.body,
                            color = colors.inkMuted,
                            maxLines = 1,
                        )
                    }
                    inner()
                },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Empty state
// ---------------------------------------------------------------------------

/**
 * A rig parked in an empty yard: the silhouette on a soft disc, one line of
 * plain language, one line of support.
 */
@Composable
fun FleetEmptyState(
    title: String,
    subtitle: String,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
    shape: RigShape = RigShape.TRAILER,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .size(112.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(colors.brandSoft),
            contentAlignment = Alignment.Center,
        ) {
            RigSilhouette(
                shape = shape,
                tint = colors.brand,
                modifier = Modifier.size(width = 76.dp, height = 42.dp),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = title,
            style = TruckerType.title,
            color = colors.ink,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = TruckerType.body,
            color = colors.inkMuted,
            textAlign = TextAlign.Center,
        )
    }
}
