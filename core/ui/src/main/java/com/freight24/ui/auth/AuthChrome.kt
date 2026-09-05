package com.freight24.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.ui.theme.BeVietnamPro
import com.freight24.ui.theme.FreightBrandForest
import com.freight24.ui.theme.Sora

// ---------------------------------------------------------------------------
// Freight24 auth / onboarding design language.
//
// Direction: any screen in this family reads like a freight consignment
// document — a brand-inked masthead (ledger-ruled, lane-marked) over a docked
// body sheet (warm bone paper in light / green-tinted near-black in dark)
// that carries the one control that matters. Structure and material do the
// work; no gradients, no decorative blur, no neutral-grey surfaces.
//
// The components are intentionally opinionated. They assume:
//   * Sora as the display face and Be Vietnam Pro as the body face (wired in
//     core/ui's typography).
//   * Deep forest green as the primary brand mark (ConnectPrimary40 light,
//     ConnectPrimary80 dark), with a warm bronze as the secondary mark.
//   * The active scheme's `surface` is NEVER the body of the document — it
//     resolves to a flat neutral grey that punches a generic form page
//     through the premium direction. Use [AuthColors.paper] instead.
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// Shapes
// ---------------------------------------------------------------------------

/** Top-corner radius for the docked body sheet. Matches the masthead's
 *  bottom-corner radius so the two sections read as adjacent panels. */
val AuthSheetShape: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

/** Bottom-corner radius for the masthead block. Use via
 *  [Modifier.clip] on the masthead's [Box]. */
val AuthHeroShape: Shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)

/** Pill radius for the primary action and outlined rows. */
val AuthControlShape: Shape = RoundedCornerShape(16.dp)

/** Slightly tighter pill for input fields. */
val AuthFieldShape: Shape = RoundedCornerShape(14.dp)

/** Stadium radius for chips and short tags. */
val AuthChipShape: Shape = RoundedCornerShape(50)

// ---------------------------------------------------------------------------
// Color tokens
// ---------------------------------------------------------------------------

/**
 * Document-wide color tokens for a single auth/onboarding screen.
 *
 * The masthead stays a deep brand block in BOTH modes — never the mode's
 * [primary], which flips to a pale mint in dark mode and would wash the
 * block out. The body sheet uses warm bone paper in light and a green-tinted
 * near-black in dark, so the body of the document carries the same identity
 * as the masthead.
 *
 * Mode is derived from the ambient scheme's surface luminance so the user's
 * light/dark override is honoured without re-wrapping the theme.
 */
@Immutable
class AuthColors(
    /** Deep brand block fill for the masthead and primary action. */
    val field: Color,
    /** Default text/icon color when sitting on top of [field]. */
    val ink: Color,
    /** De-emphasized ink — subtitles, overlines, step counters. */
    val inkMuted: Color,
    /** 1px hairlines on the masthead (ledger ruling, column rule). */
    val hairline: Color,
    /** Bronze accent — lane markings, progress, freight-mark icon. */
    val lane: Color,
    /** Sheet paper — the body of the document. NOT the M3 surface. */
    val paper: Color,
    /** Faint ruling across the sheet paper, reads as paper grain. */
    val paperRuling: Color,
    /** Hairline edge of the sheet — top rule, row borders. */
    val paperEdge: Color,
) {
    /** Brand-ink color at the given alpha — useful for icon tints inside
     *  outlined rows that need a low-key brand accent. */
    fun fieldAt(alpha: Float): Color = field.copy(alpha = alpha)
}

/**
 * The dark document palette. A named singleton rather than inline hex in
 * the composable below, so the values are stated once and
 * [rememberAuthColors] only selects the mode.
 */
private val AuthDarkColors = AuthColors(
    field = Color(0xFF0F3527),
    ink = Color(0xFFF1F6F2),
    inkMuted = Color(0xFFF1F6F2).copy(alpha = 0.62f),
    hairline = Color(0xFFF1F6F2).copy(alpha = 0.07f),
    lane = Color(0xFFDCC38A).copy(alpha = 0.34f),
    paper = Color(0xFF0F1411),
    paperRuling = Color(0xFFF1F6F2).copy(alpha = 0.045f),
    paperEdge = Color(0xFFF1F6F2).copy(alpha = 0.085f),
)

/** The light document palette. See [AuthDarkColors]. */
private val AuthLightColors = AuthColors(
    field = FreightBrandForest,
    ink = Color(0xFFF6FAF7),
    inkMuted = Color(0xFFF6FAF7).copy(alpha = 0.66f),
    hairline = Color(0xFFF6FAF7).copy(alpha = 0.09f),
    lane = Color(0xFFDCC38A).copy(alpha = 0.40f),
    paper = Color(0xFFEFE9D9),
    paperRuling = FreightBrandForest.copy(alpha = 0.06f),
    paperEdge = FreightBrandForest.copy(alpha = 0.13f),
)

/**
 * Returns the document's color tokens for the current mode. Use this once
 * per screen and pass the result to every component.
 */
@Composable
fun rememberAuthColors(): AuthColors {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return remember(dark) { if (dark) AuthDarkColors else AuthLightColors }
}

// ---------------------------------------------------------------------------
// Auth type tokens — the three auth-specific styles, stated once here
// instead of as inline size/weight overrides at each call site.
// ---------------------------------------------------------------------------

/** Tracked overline for eyebrows, field labels and step counters. */
private val AuthOverlineStyle = TextStyle(
    fontFamily = BeVietnamPro,
    fontWeight = FontWeight.SemiBold,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 1.4.sp,
)

/** Primary-action label. */
private val AuthPrimaryLabelStyle = TextStyle(
    fontFamily = Sora,
    fontWeight = FontWeight.SemiBold,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
)

/** Centred legal microcopy. */
private val AuthLegalStyle = TextStyle(
    fontFamily = BeVietnamPro,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.4.sp,
)

// ---------------------------------------------------------------------------
// Masthead
// ---------------------------------------------------------------------------

/**
 * Faint ledger ruling behind the hero copy: horizontal hairlines on a 22dp
 * pitch and one column rule at 72% width. Reads as operations paperwork
 * rather than a decorative gradient.
 *
 * The bronze lane dashes are NOT drawn here — they live in [AuthLaneMarkings]
 * so they can sit in a known lower-right position independent of how tall
 * the masthead box ends up.
 */
@Composable
fun BoxScope.AuthLedgerBackdrop(colors: AuthColors) {
    Canvas(modifier = Modifier.matchParentSize()) {
        val pitch = 22.dp.toPx()
        val w = size.width
        val h = size.height

        var y = 0f
        while (y < h) {
            drawLine(
                color = colors.hairline,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f,
            )
            y += pitch
        }

        drawLine(
            color = colors.hairline.copy(alpha = colors.hairline.alpha * 1.6f),
            start = Offset(w * 0.72f, 0f),
            end = Offset(w * 0.72f, h),
            strokeWidth = 1f,
        )
    }
}

/**
 * Three bronze lane dashes parked in the lower-right of the masthead — the
 * "consignment stamp" mark. Anchored via [BoxScope.align] so they stay in
 * the corner regardless of how tall the masthead grows.
 */
@Composable
fun BoxScope.AuthLaneMarkings(
    colors: AuthColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .align(Alignment.BottomEnd)
            .padding(end = 8.dp, bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 2.dp)
                .clip(CircleShape)
                .background(colors.lane),
        )
        Box(
            modifier = Modifier
                .size(width = 30.dp, height = 2.dp)
                .clip(CircleShape)
                .background(colors.lane),
        )
        Box(
            modifier = Modifier
                .size(width = 18.dp, height = 2.dp)
                .clip(CircleShape)
                .background(colors.lane),
        )
    }
}

// ---------------------------------------------------------------------------
// Body sheet
// ---------------------------------------------------------------------------

/**
 * Faint ledger ruling across the body sheet. Same character as the
 * masthead backdrop but a slightly wider pitch and a touch lighter — it
 * reads as the grain of the paper rather than as decoration, and it is
 * what keeps a light screen from looking like a plain white form.
 */
@Composable
fun BoxScope.AuthPaperRuling(colors: AuthColors) {
    Canvas(modifier = Modifier.matchParentSize()) {
        val pitch = 26.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = colors.paperRuling,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
            )
            y += pitch
        }
    }
}

/**
 * The docked body sheet. Sits adjacent to the masthead — NOT overlapping
 * it. A 20.dp drop shadow here on a sheet that overlaps deep brand green
 * produces a visible darker band on the green (the shadow gradient
 * darkening the brand block), which reads as a stray horizontal shade.
 * Use the masthead's [AuthHeroShape] bottom corners + this shape's top
 * corners to keep the two sections flush and visually adjacent.
 */
@Composable
fun AuthSheet(
    colors: AuthColors,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(AuthSheetShape)
            .background(colors.paper),
    ) {
        AuthPaperRuling(colors)

        // Top hairline — the seam between masthead and body.
        Canvas(modifier = Modifier.matchParentSize()) {
            drawLine(
                color = colors.paperEdge,
                start = Offset(0f, 0.5f),
                end = Offset(size.width, 0.5f),
                strokeWidth = 1.dp.toPx(),
            )
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)) {
            content()
        }
    }
}

// ---------------------------------------------------------------------------
// Form controls
// ---------------------------------------------------------------------------

/**
 * Small tracked overline — eyebrows, field labels, step counters.
 * Uses the [color] passed in so the caller controls the ink source
 * (inkMuted on the masthead, onSurfaceVariant on the sheet).
 */
@Composable
fun AuthOverline(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = AuthOverlineStyle,
        color = color,
    )
}

/**
 * Hairline rule that separates the form from the legal/support footers.
 * Sits on the sheet's paper so it reads as a fold in the document, not a
 * divider between two cards.
 */
@Composable
fun AuthRule(colors: AuthColors, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(1.dp)) {
        drawRect(color = colors.paperEdge, size = size)
    }
}

/**
 * Bordered, low-emphasis row used for support shortcuts. Sits on the
 * sheet's paper — NOT on the M3 default surface (which is a near-white
 * neutral in light mode and would punch a grey rectangle through the
 * warm bone). The border is a brand-tinted hairline so the row still
 * reads as an interactive surface without leaving the paper.
 */
@Composable
fun AuthOutlinedRow(
    onClick: () -> Unit,
    colors: AuthColors,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(AuthControlShape)
            .background(colors.paper)
            .border(1.dp, colors.paperEdge, AuthControlShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        content()
    }
}

/**
 * Primary action button.
 *
 * Active state: full brand field with ink label.
 *
 * Disabled state: deep brand field at 12% alpha (light) / 18% (dark) with
 * dimmed ink. This is deliberately NOT the M3 default `primaryContainer`,
 * which resolves to a pale mint that washes out against the warm bone
 * paper. The low-alpha brand field keeps the control reading as a brand
 * element in its "standby" position.
 *
 * [progress] is an optional 0..1 wipe that fills the button from the
 * left, used for the auto-send countdown on the login screen. Loading
 * replaces the label with a spinner but keeps the brand field — work in
 * progress, not a dead control.
 */
@Composable
fun AuthPrimaryAction(
    label: String,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
    colors: AuthColors,
    modifier: Modifier = Modifier,
    progress: Float = 0f,
    testTag: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val wiped = progress > 0f
    val labelOnBrand = wiped && progress >= 0.5f

    val standbyAlpha = if (colors.field.luminance() < 0.5f) 0.18f else 0.12f
    val container = when {
        !enabled && !wiped -> colors.field.copy(alpha = standbyAlpha)
        else -> colors.field
    }
    val ink = colors.ink
    val standbyInkAlpha = if (colors.field.luminance() < 0.5f) 0.55f else 0.42f
    val content = when {
        loading -> ink
        labelOnBrand -> ink
        !enabled && !wiped -> ink.copy(alpha = standbyInkAlpha)
        else -> ink
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .let {
                if (testTag != null) {
                    it
                        .testTag(testTag)
                        .semantics { testTagsAsResourceId = true }
                } else {
                    it
                }
            }
            .clip(AuthControlShape)
            .background(container)
            .then(
                if (enabled) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (wiped) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(colors.field),
            )
        }

        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (loading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp,
                        color = ink,
                    )
                }
            } else {
                Text(
                    text = label,
                    style = AuthPrimaryLabelStyle,
                    color = content,
                )
                if (trailing != null) {
                    Box(Modifier.weight(1f))
                    Box(modifier = Modifier.alpha(if (enabled || wiped) 1f else standbyInkAlpha)) {
                        trailing()
                    }
                }
            }
        }
    }
}

/**
 * Route progress as freight lane-markers: one segment per step, the
 * travelled and current lanes lit in bronze, the remaining ones a
 * hairline. Reads as "how far along the lane you are" rather than a
 * generic progress bar.
 */
@Composable
fun AuthLaneProgress(
    steps: Int,
    current: Int,
    colors: AuthColors,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(steps) { i ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(if (i + 1 <= current) colors.lane else colors.hairline),
            )
        }
    }
}

/** Centre-aligned legal microcopy. */
@Composable
fun AuthLegalNote(text: String, color: Color) {
    Text(
        text = text,
        style = AuthLegalStyle,
        color = color,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}
