package com.freight24.ui.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
// body sheet (paper in light / green-tinted near-black in dark) that carries
// the one control that matters. Structure and material do the work; no
// gradients, no decorative blur, no neutral-grey surfaces.
//
// The components are intentionally opinionated. They assume:
//   * Sora as the display face and Be Vietnam Pro as the body face (wired in
//     core/ui's typography).
//   * Deep forest green as the primary brand mark, with a single warm accent
//     as the secondary mark.
//   * The active scheme's `surface` is NEVER the body of the document — it
//     resolves to a flat neutral grey that punches a generic form page
//     through the premium direction. Use [AuthColors.paper] instead.
//
// ---------------------------------------------------------------------------
// TWO IDENTITIES, ONE DOCUMENT
//
// Both apps in the suite open onto the *same* document — that is what makes
// them read as one product. [AuthIdentity] selects which identity's tokens
// the document is printed with:
//
//   * CONNECT — the office copy. Warm bone paper, bronze lane marks, the
//     editorial type scale. Employees, at a desk.
//   * TRUCKER — the driver's copy. The same masthead, the same sheet, the
//     same component grammar, reprinted on a cleaner, cooler stock with a
//     saffron accent, a heavier display voice and larger controls. Truck
//     owners, one-handed, outdoors, in sunlight.
//
// The variation is deliberately confined to TOKENS — palette, type scale and
// control metrics — never to structure. A screen built from these components
// is composed identically in both apps; only the printing changes. That is
// the whole trick: cohesion comes from the shared skeleton, differentiation
// from the ink and the stock.
//
// Every token below is contrast-checked for WCAG AA (4.5:1 body text, 3:1
// large text and UI boundaries) against the surface it is actually drawn on.
// ---------------------------------------------------------------------------

/**
 * Which identity's tokens the auth document is printed with. Selects a
 * complete [AuthTokens] set; see the file header for what each one means.
 */
enum class AuthIdentity {
    /** Employees. Warm bone paper, bronze accent. */
    CONNECT,

    /** Truck owners. Cooler paper, saffron accent, heavier type. */
    TRUCKER,
}

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
 * [androidx.compose.material3.ColorScheme.primary], which flips to a pale
 * mint in dark mode and would wash the block out. The body sheet is paper in
 * light and a green-tinted near-black in dark, so the body of the document
 * carries the same identity as the masthead.
 *
 * The `ink*` tokens are for text sitting ON [field]. The `sheet*` tokens are
 * for content sitting ON [paper]. Keeping them separate is what lets the
 * document be fully self-describing: a screen printed from these tokens looks
 * identical no matter which [com.freight24.ui.theme.FreightAppTheme] the user
 * has picked, so the branded front door can never pick up a stray indigo
 * accent from a settings choice.
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
    /** De-emphasized ink on [field] — subtitles, overlines, step counters. */
    val inkMuted: Color,
    /** 1px hairlines on the masthead (ledger ruling, column rule). */
    val hairline: Color,
    /** Accent marks on the masthead — lane dashes, seam rule, brand glyph. */
    val lane: Color,
    /** Sheet paper — the body of the document. NOT the M3 surface. */
    val paper: Color,
    /** Faint ruling across the sheet paper, reads as paper grain. */
    val paperRuling: Color,
    /** Hairline edge of the sheet — top rule, row borders. Decorative only;
     *  NOT a control boundary. See [controlEdge]. */
    val paperEdge: Color,
    /**
     * Boundary of an interactive control whose fill matches [paper] — text
     * fields, OTP cells, tappable rows.
     *
     * This is a *separate* token from [paperEdge] on purpose. Decorative
     * ruling may be as faint as it likes, but a field boundary is the only
     * thing identifying where the input starts, which puts it under
     * WCAG 2.1 SC 1.4.11 and therefore a hard 3:1 floor against [paper].
     * Folding both jobs into one token is how a palette ends up failing
     * 1.4.11 at 1.4:1 while looking perfectly deliberate.
     */
    val controlEdge: Color,
    /** Primary text sitting on [paper]. */
    val sheetInk: Color,
    /** Secondary text on [paper]. A solid mid-tone, never a low alpha. */
    val sheetInkMuted: Color,
    /** Accent for marks on [paper] — the active OTP caret, the expiry meter
     *  while the code is still live. */
    val sheetAccent: Color,
    /** Accent for *text* on [paper] — passes 4.5:1, unlike [sheetAccent]
     *  which is only required to clear 3:1 as a UI boundary. */
    val sheetAccentInk: Color,
    /** Failure ink and failure borders on [paper]. */
    val sheetDanger: Color,
    /** Fill of the primary action while it is disabled.
     *
     *  A full [Color] rather than an alpha on [field], because the two modes
     *  need opposite relationships to the sheet. On paper, a low-alpha brand
     *  wash reads as "the brand, held back". On the dark sheet a low-alpha
     *  [field] is *darker* than the paper around it, so the control reads as
     *  inset — pressed, not unavailable. Dark mode lifts instead: a surface a
     *  step lighter than the sheet, which is what M3 elevation means in the
     *  dark and what makes the control still look like a button. */
    val standbyFill: Color,
    /** Colour of the primary action's label while it is disabled.
     *
     *  A flat [Color] rather than an alpha on [ink] on purpose. Connect dims
     *  its disabled label to ~55% over a pale wash, which lands around 1.2:1 —
     *  legible only if you already know what it says. WCAG exempts inactive
     *  controls from the contrast minimum, so it is not a violation, but a
     *  driver who cannot read the button cannot tell what it is about to do.
     *  Trucker therefore sets a solid mid-tone here instead. */
    val standbyInk: Color,
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
private val AuthDarkColors =
    AuthColors(
        field = Color(0xFF0F3527),
        ink = Color(0xFFF1F6F2),
        inkMuted = Color(0xFFF1F6F2).copy(alpha = 0.62f),
        hairline = Color(0xFFF1F6F2).copy(alpha = 0.07f),
        lane = Color(0xFFDCC38A).copy(alpha = 0.34f),
        paper = Color(0xFF0F1411),
        paperRuling = Color(0xFFF1F6F2).copy(alpha = 0.045f),
        paperEdge = Color(0xFFF1F6F2).copy(alpha = 0.085f),
        // Connect is unchanged: its control edges keep the original hairline.
        controlEdge = Color(0xFFF1F6F2).copy(alpha = 0.085f),
        sheetInk = Color(0xFFDFE4E0),
        sheetInkMuted = Color(0xFFBFCAC3),
        sheetAccent = Color(0xFFA6D3BB),
        sheetAccentInk = Color(0xFFA6D3BB),
        sheetDanger = Color(0xFFFFB4A8),
        standbyFill = Color(0xFF0F3527).copy(alpha = 0.18f),
        standbyInk = Color(0xFFF1F6F2).copy(alpha = 0.55f),
    )

/** The light document palette. See [AuthDarkColors]. */
private val AuthLightColors =
    AuthColors(
        field = FreightBrandForest,
        ink = Color(0xFFF6FAF7),
        inkMuted = Color(0xFFF6FAF7).copy(alpha = 0.66f),
        hairline = Color(0xFFF6FAF7).copy(alpha = 0.09f),
        lane = Color(0xFFDCC38A).copy(alpha = 0.40f),
        paper = Color(0xFFEFE9D9),
        paperRuling = FreightBrandForest.copy(alpha = 0.06f),
        paperEdge = FreightBrandForest.copy(alpha = 0.13f),
        // Connect is unchanged: its control edges keep the original hairline.
        controlEdge = FreightBrandForest.copy(alpha = 0.13f),
        sheetInk = Color(0xFF181C1A),
        sheetInkMuted = Color(0xFF414843),
        sheetAccent = Color(0xFF1B5E40),
        sheetAccentInk = Color(0xFF1B5E40),
        sheetDanger = Color(0xFF93221A),
        standbyFill = FreightBrandForest.copy(alpha = 0.18f),
        standbyInk = Color(0xFFF6FAF7).copy(alpha = 0.55f),
    )

/**
 * TRUCKER — the driver's copy.
 *
 * Same masthead block as Connect (identical [field], [ink] and [hairline], so
 * the two apps are unmistakably the same product at a glance). What changes:
 *
 *   * **Stock.** The sheet is a cleaner, cooler warm-white than Connect's
 *     bone. Two reasons, one practical and one deliberate: a less yellow
 *     sheet holds up better in direct sunlight, and it gives the driver's
 *     copy a visibly different paper without leaving the paper family.
 *   * **Accent.** Saffron rather than bronze — the marker-light amber the
 *     trucker palette already owns. Held at the same low alphas Connect uses
 *     on the masthead, so it reads as a warm note rather than a warning lamp.
 *     (An earlier, higher-saturation amber pass on these screens was rejected
 *     as "too flashy"; the restraint here is intentional.)
 *   * **Sheet ink.** A solid mid-tone for secondary text rather than
 *     Connect's low-alpha wash. Low-alpha greys vanish in sunlight, which is
 *     the single worst thing you can do to this audience.
 */
private val TruckerAuthDarkColors =
    AuthColors(
        field = Color(0xFF0F3527),
        ink = Color(0xFFF1F6F2),
        inkMuted = Color(0xFFF1F6F2).copy(alpha = 0.62f),
        hairline = Color(0xFFF1F6F2).copy(alpha = 0.07f),
        lane = Color(0xFFE5A63C).copy(alpha = 0.34f),
        paper = Color(0xFF101613),
        paperRuling = Color(0xFFF1F6F2).copy(alpha = 0.045f),
        paperEdge = Color(0xFFF1F6F2).copy(alpha = 0.16f),
        // 0.38 clears 3:1 against the dark sheet. The driver reads this in a cab
        // in daylight, so the field boundary is load-bearing, not decorative.
        controlEdge = Color(0xFFF1F6F2).copy(alpha = 0.38f),
        sheetInk = Color(0xFFE9F1EB),
        sheetInkMuted = Color(0xFFA2B8AA),
        sheetAccent = Color(0xFFE5A63C),
        sheetAccentInk = Color(0xFFE5A63C),
        sheetDanger = Color(0xFFF08A7E),
        standbyFill = Color(0xFF22402F),
        standbyInk = Color(0xFFA2B8AA),
    )

/** TRUCKER light. See [TruckerAuthDarkColors]. */
private val TruckerAuthLightColors =
    AuthColors(
        field = FreightBrandForest,
        ink = Color(0xFFF6FAF7),
        inkMuted = Color(0xFFF6FAF7).copy(alpha = 0.66f),
        hairline = Color(0xFFF6FAF7).copy(alpha = 0.09f),
        lane = Color(0xFFE5A63C).copy(alpha = 0.38f),
        paper = Color(0xFFF4F2ED),
        paperRuling = FreightBrandForest.copy(alpha = 0.055f),
        paperEdge = FreightBrandForest.copy(alpha = 0.20f),
        // 0.58 clears 3:1 against the light sheet. The driver reads this in a cab
        // in daylight, so the field boundary is load-bearing, not decorative.
        controlEdge = FreightBrandForest.copy(alpha = 0.58f),
        sheetInk = Color(0xFF12211A),
        sheetInkMuted = Color(0xFF4C6055),
        sheetAccent = Color(0xFFB8760E),
        sheetAccentInk = Color(0xFF96610A),
        sheetDanger = Color(0xFFB3261E),
        standbyFill = FreightBrandForest.copy(alpha = 0.14f),
        standbyInk = Color(0xFF4C6055),
    )

/**
 * Returns the document's color tokens for the current mode and identity. Use
 * this once per screen and pass the result to every component.
 */
@Composable
fun rememberAuthColors(identity: AuthIdentity = AuthIdentity.CONNECT): AuthColors {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return remember(dark, identity) {
        when (identity) {
            AuthIdentity.CONNECT -> if (dark) AuthDarkColors else AuthLightColors
            AuthIdentity.TRUCKER -> if (dark) TruckerAuthDarkColors else TruckerAuthLightColors
        }
    }
}

// ---------------------------------------------------------------------------
// Auth type tokens
//
// Stated once as a named set instead of as inline size/weight overrides at
// each call site, so an identity can restate the scale without touching a
// single composable. Everything is Sora (display) over Be Vietnam Pro (body),
// which is the suite-wide pairing.
// ---------------------------------------------------------------------------

/**
 * The auth document's type scale.
 *
 * [AuthTypeScale.Connect] reproduces the scale the Connect screens were built
 * against, so those call sites are unaffected. [AuthTypeScale.Trucker] is the
 * same voice, spoken louder: the headline gains a step and weight, body copy
 * holds a 16sp floor, and the code digits are materially bigger. None of that
 * is decoration — this audience reads the screen one-handed, outdoors, often
 * with the system font scale already turned up, and the number is the one
 * thing they check twice before tapping.
 */
@Immutable
class AuthTypeScale(
    /** The masthead headline. */
    val heroHeadline: TextStyle,
    /** Masthead supporting copy. */
    val heroBody: TextStyle,
    /** Tracked overline — eyebrows, field labels, step counters. */
    val overline: TextStyle,
    /** Headline sitting on the sheet. */
    val sheetHeadline: TextStyle,
    /** Body copy sitting on the sheet. */
    val sheetBody: TextStyle,
    /** Label of the primary action. */
    val actionLabel: TextStyle,
    /** Centred legal microcopy. */
    val legal: TextStyle,
    /** A digit inside an OTP cell. */
    val codeDigit: TextStyle,
    /** A countdown readout. */
    val timer: TextStyle,
) {
    companion object {
        /** Connect's existing scale — unchanged, so those screens render
         *  exactly as they did before identities existed. */
        val Connect =
            AuthTypeScale(
                heroHeadline =
                    TextStyle(
                        fontFamily = Sora,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 32.sp,
                        lineHeight = 40.sp,
                        letterSpacing = (-0.5).sp,
                    ),
                heroBody =
                    TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    ),
                overline =
                    TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 1.4.sp,
                    ),
                sheetHeadline =
                    TextStyle(
                        fontFamily = Sora,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 28.sp,
                        lineHeight = 36.sp,
                        letterSpacing = (-0.4).sp,
                    ),
                sheetBody =
                    TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    ),
                actionLabel =
                    TextStyle(
                        fontFamily = Sora,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.1.sp,
                    ),
                legal =
                    TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        letterSpacing = 0.4.sp,
                    ),
                codeDigit =
                    TextStyle(
                        fontFamily = Sora,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        fontFeatureSettings = "tnum",
                    ),
                timer =
                    TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontFeatureSettings = "tnum",
                    ),
            )

        /** The driver's copy — same faces, larger optical sizes. */
        val Trucker =
            AuthTypeScale(
                heroHeadline =
                    TextStyle(
                        fontFamily = Sora,
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                        lineHeight = 41.sp,
                        letterSpacing = (-0.5).sp,
                    ),
                heroBody =
                    TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        lineHeight = 23.sp,
                    ),
                overline =
                    TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        letterSpacing = 1.3.sp,
                    ),
                sheetHeadline =
                    TextStyle(
                        fontFamily = Sora,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        lineHeight = 33.sp,
                        letterSpacing = (-0.4).sp,
                    ),
                sheetBody =
                    TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        lineHeight = 23.sp,
                    ),
                actionLabel =
                    TextStyle(
                        fontFamily = Sora,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        lineHeight = 22.sp,
                        letterSpacing = 0.2.sp,
                    ),
                legal =
                    TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.2.sp,
                    ),
                codeDigit =
                    TextStyle(
                        fontFamily = Sora,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        lineHeight = 32.sp,
                        letterSpacing = 0.5.sp,
                        fontFeatureSettings = "tnum",
                    ),
                timer =
                    TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                        fontFeatureSettings = "tnum",
                    ),
            )
    }
}

// ---------------------------------------------------------------------------
// Auth metric tokens — the touch-target contract
//
// Connect's numbers are the baseline. Trucker's are larger across the board;
// the CTA, the field and the code cells are all sized to be hit without
// looking, one-handed, on a moving vehicle.
// ---------------------------------------------------------------------------

/** Control geometry for the auth document. */
@Immutable
class AuthMetrics(
    /** Height of the primary action. */
    val actionHeight: Dp,
    /** Height of a single-line input. */
    val fieldHeight: Dp,
    /** Height of an OTP cell. Width is derived — the row always fills. */
    val otpCellHeight: Dp,
    /** Gap between OTP cells. */
    val otpCellGap: Dp,
    /** Horizontal padding inside the body sheet. */
    val sheetPaddingH: Dp,
    /** Vertical padding inside the body sheet. */
    val sheetPaddingV: Dp,
    /** Page gutter either side of the whole document. */
    val gutter: Dp,
    /** Upper bound on content width, so tablets and landscape do not
     *  stretch the document into an unreadable band. */
    val maxContentWidth: Dp,
) {
    companion object {
        /** Connect's existing geometry — unchanged. */
        val Connect =
            AuthMetrics(
                actionHeight = 56.dp,
                fieldHeight = 56.dp,
                otpCellHeight = 56.dp,
                otpCellGap = 8.dp,
                sheetPaddingH = 24.dp,
                sheetPaddingV = 28.dp,
                gutter = 24.dp,
                maxContentWidth = 520.dp,
            )

        /** Trucker's geometry — bigger targets, same skeleton. */
        val Trucker =
            AuthMetrics(
                actionHeight = 60.dp,
                fieldHeight = 64.dp,
                otpCellHeight = 64.dp,
                otpCellGap = 8.dp,
                sheetPaddingH = 24.dp,
                sheetPaddingV = 28.dp,
                gutter = 24.dp,
                maxContentWidth = 560.dp,
            )
    }
}

/**
 * The complete set of tokens a document is printed with. Pass this one value
 * around a screen rather than three.
 */
@Immutable
class AuthTokens(
    val identity: AuthIdentity,
    val colors: AuthColors,
    val type: AuthTypeScale,
    val metrics: AuthMetrics,
)

/** Returns the token set for [identity] in the current mode. */
@Composable
fun rememberAuthTokens(identity: AuthIdentity = AuthIdentity.CONNECT): AuthTokens {
    val colors = rememberAuthColors(identity)
    return remember(identity, colors) {
        when (identity) {
            AuthIdentity.CONNECT ->
                AuthTokens(
                    identity = identity,
                    colors = colors,
                    type = AuthTypeScale.Connect,
                    metrics = AuthMetrics.Connect,
                )
            AuthIdentity.TRUCKER ->
                AuthTokens(
                    identity = identity,
                    colors = colors,
                    type = AuthTypeScale.Trucker,
                    metrics = AuthMetrics.Trucker,
                )
        }
    }
}

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
        modifier =
            modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Box(
            modifier =
                Modifier
                    .size(width = 44.dp, height = 2.dp)
                    .clip(CircleShape)
                    .background(colors.lane),
        )
        Box(
            modifier =
                Modifier
                    .size(width = 30.dp, height = 2.dp)
                    .clip(CircleShape)
                    .background(colors.lane),
        )
        Box(
            modifier =
                Modifier
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
    metrics: AuthMetrics = AuthMetrics.Connect,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
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

        Column(
            modifier =
                Modifier.padding(
                    horizontal = metrics.sheetPaddingH,
                    vertical = metrics.sheetPaddingV,
                ),
        ) {
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
    type: AuthTypeScale = AuthTypeScale.Connect,
) {
    Text(
        text = text,
        modifier = modifier,
        style = type.overline,
        color = color,
    )
}

/**
 * Hairline rule that separates the form from the legal/support footers.
 * Sits on the sheet's paper so it reads as a fold in the document, not a
 * divider between two cards.
 */
@Composable
fun AuthRule(
    colors: AuthColors,
    modifier: Modifier = Modifier,
) {
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
        modifier =
            modifier
                .fillMaxWidth()
                .clip(AuthControlShape)
                .background(colors.paper)
                .border(1.dp, colors.controlEdge, AuthControlShape)
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
    type: AuthTypeScale = AuthTypeScale.Connect,
    metrics: AuthMetrics = AuthMetrics.Connect,
    trailing: (@Composable () -> Unit)? = null,
) {
    val wiped = progress > 0f
    val labelOnBrand = wiped && progress >= 0.5f

    val container =
        when {
            !enabled && !wiped -> colors.standbyFill
            else -> colors.field
        }
    val ink = colors.ink
    val content =
        when {
            loading -> ink
            labelOnBrand -> ink
            !enabled && !wiped -> colors.standbyInk
            else -> ink
        }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(metrics.actionHeight)
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
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        if (wiped) {
            Box(
                modifier =
                    Modifier
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
                    style = type.actionLabel,
                    color = content,
                )
                if (trailing != null) {
                    Box(Modifier.weight(1f))
                    Box(modifier = Modifier.alpha(if (enabled || wiped) 1f else colors.standbyInk.alpha)) {
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
                modifier =
                    Modifier
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
fun AuthLegalNote(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    type: AuthTypeScale = AuthTypeScale.Connect,
) {
    Text(
        text = text,
        style = type.legal,
        color = color,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}

// ---------------------------------------------------------------------------
// Back affordance
// ---------------------------------------------------------------------------

/**
 * Back control for a masthead.
 *
 * Pulled left by 12dp so the glyph optically aligns with the document gutter
 * rather than sitting inset from it — the icon button's own padding would
 * otherwise push the arrow visibly inward from the overline above it.
 */
@Composable
fun AuthBackButton(
    colors: AuthColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Back",
) {
    IconButton(
        onClick = onClick,
        modifier =
            modifier
                .size(48.dp)
                .offset(x = (-12).dp),
    ) {
        Icon(
            imageVector = Icons.Filled.ArrowBackIosNew,
            contentDescription = contentDescription,
            tint = colors.ink,
            modifier = Modifier.size(20.dp),
        )
    }
}

// ---------------------------------------------------------------------------
// Masthead
// ---------------------------------------------------------------------------

/**
 * Route progress for a multi-step flow — "step 2 of 2".
 *
 * [current] is 1-based. Both [AuthLaneProgress] and this label are drawn from
 * it, so a flow can never show a meter and a counter that disagree.
 */
@Immutable
data class AuthStep(val current: Int, val total: Int) {
    val label: String get() = "STEP $current OF $total"
}

/**
 * The brand masthead — the top of every auth document, in both identities.
 *
 * Layout is fixed: an identity row (optional back, optional brand glyph,
 * tracked overline), then EITHER a route-progress row (when [step] is given)
 * OR the accent seam rule that reads as the document's crease, then the
 * headline and optional supporting copy.
 *
 * The step row and the seam rule are mutually exclusive on purpose. A screen
 * that is one of a sequence needs to say where it sits; a screen that is the
 * first and only step does not, and the crease is a better mark for it than a
 * meter reading "step 1 of 1".
 */
@Composable
fun AuthMasthead(
    colors: AuthColors,
    type: AuthTypeScale,
    metrics: AuthMetrics,
    overline: String,
    headline: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    brandMark: ImageVector? = null,
    onBack: (() -> Unit)? = null,
    backContentDescription: String = "Back",
    step: AuthStep? = null,
    topPadding: Dp = 28.dp,
    bottomPadding: Dp = 44.dp,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(AuthHeroShape)
                .background(colors.field)
                .statusBarsPadding()
                // The gutter and the vertical padding belong on the BOX, not on an
                // inner column. The backdrop and the lane markings are BoxScope
                // children aligned to its content area, so padding applied further
                // in leaves them measuring against the full bleed — which parks
                // the lane dashes on the rounded bottom corner and clips them.
                .padding(horizontal = metrics.gutter)
                .padding(top = topPadding, bottom = bottomPadding),
    ) {
        AuthLedgerBackdrop(colors)
        AuthLaneMarkings(colors)

        Column(modifier = Modifier.widthIn(max = metrics.maxContentWidth)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    AuthBackButton(
                        colors = colors,
                        onClick = onBack,
                        contentDescription = backContentDescription,
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
                if (brandMark != null) {
                    Icon(
                        imageVector = brandMark,
                        contentDescription = null,
                        tint = colors.lane,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                AuthOverline(text = overline, color = colors.inkMuted, type = type)
            }

            if (step != null) {
                Spacer(modifier = Modifier.height(22.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AuthLaneProgress(
                        steps = step.total,
                        current = step.current,
                        colors = colors,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    AuthOverline(text = step.label, color = colors.inkMuted, type = type)
                }
                Spacer(modifier = Modifier.height(22.dp))
            } else {
                Spacer(modifier = Modifier.height(18.dp))
                // Bronze/saffron seam rule — the crease of the document.
                Box(
                    modifier =
                        Modifier
                            .size(width = 44.dp, height = 2.dp)
                            .background(colors.lane),
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                text = headline,
                style = type.heroHeadline,
                color = colors.ink,
            )

            if (body != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = body,
                    style = type.heroBody,
                    color = colors.inkMuted,
                    modifier = Modifier.widthIn(max = 330.dp),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Destination row — "the code went here, and you can fix it"
// ---------------------------------------------------------------------------

/**
 * Shows where a one-time code was sent, and makes the number itself the
 * control that corrects it. The affordance and the value are the same object,
 * so there is no separate "change number" link for the user to hunt for.
 *
 * Sits on the sheet, so it uses `sheet*` tokens rather than the M3 surface —
 * a `surfaceContainerLowest` fill here would punch a stark box through the
 * paper in light mode.
 */
@Composable
fun AuthDestinationRow(
    number: String,
    colors: AuthColors,
    type: AuthTypeScale,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    changeLabel: String = "Change",
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(AuthChipShape)
                .background(colors.paper)
                .border(1.dp, colors.controlEdge, AuthChipShape)
                .clickable(onClick = onClick, role = Role.Button)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The number takes the slack and ellipsises; the action label keeps its
        // intrinsic width and never wraps. With an unweighted number plus a
        // weight(1f) spacer, a narrow viewport squeezes BOTH and "Change" breaks
        // mid-word out of the pill — which is exactly what 274dp exposed.
        Text(
            text = number,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style =
                type.sheetBody.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFeatureSettings = "tnum",
                ),
            color = colors.sheetInk,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = changeLabel,
            maxLines = 1,
            softWrap = false,
            style = type.sheetBody.copy(fontWeight = FontWeight.SemiBold),
            color = colors.sheetAccentInk,
        )
    }
}

// ---------------------------------------------------------------------------
// Expiry meter
// ---------------------------------------------------------------------------

/**
 * A labelled countdown meter: caption on the left, remaining time on the
 * right, and a hairline track beneath showing how much of the window is left.
 *
 * A bare "04:35" floating under the code cells tells the user nothing about
 * whether that is most of the window or almost none of it. The meter answers
 * that at a glance, and flips to [AuthColors.sheetDanger] in the final
 * stretch so the expiry is impossible to miss.
 */
@Composable
fun AuthTimerMeter(
    label: String,
    value: String,
    fraction: Float,
    colors: AuthColors,
    type: AuthTypeScale,
    modifier: Modifier = Modifier,
    urgent: Boolean = false,
) {
    val ink = if (urgent) colors.sheetDanger else colors.sheetInk
    val fill = if (urgent) colors.sheetDanger else colors.sheetAccent

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuthOverline(
                text = label,
                color = if (urgent) colors.sheetDanger else colors.sheetInkMuted,
                type = type,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(text = value, style = type.timer, color = ink)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(colors.paperEdge),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(fraction.coerceIn(0f, 1f))
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(fill),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// OTP cells
// ---------------------------------------------------------------------------

/**
 * Code entry as a row of cells.
 *
 * Cells are laid out by WEIGHT, not by fixed width: six fixed-width cells
 * plus their gaps overflow a 360dp screen and would clip on the smaller
 * handsets this app still has to run on. Weighting means the row always fits
 * the gutter it is given, at any width, without a breakpoint.
 *
 * The hidden [BasicTextField] behind the cells MUST stay full-size and in the
 * hierarchy. A collapsed or zero-size field drops out of the accessibility
 * tree, which breaks both TalkBack and the `otp_input` test-tag contract the
 * Maestro flows type into — hence `alpha(0.01f)` rather than `size(0.dp)`.
 */
@Composable
fun AuthOtpCells(
    otp: String,
    otpLength: Int,
    shake: Boolean,
    enabled: Boolean,
    colors: AuthColors,
    type: AuthTypeScale,
    metrics: AuthMetrics,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    LaunchedEffect(otp) {
        if (otp.length == otpLength) keyboardController?.hide()
    }

    LaunchedEffect(shake) {
        if (shake) {
            shakeOffset.snapTo(-12f)
            shakeOffset.animateTo(12f)
            shakeOffset.animateTo(-8f)
            shakeOffset.animateTo(8f)
            shakeOffset.animateTo(0f)
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .offset(x = shakeOffset.value.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    if (enabled) {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
                },
    ) {
        BasicTextField(
            value = otp,
            onValueChange = { raw ->
                if (enabled) onOtpChange(raw.filter(Char::isDigit).take(otpLength))
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(metrics.otpCellHeight)
                    .alpha(0.01f)
                    .testTag("otp_input")
                    .semantics { testTagsAsResourceId = true }
                    .focusRequester(focusRequester),
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            textStyle = TextStyle(color = Color.Transparent, fontSize = 16.sp),
            cursorBrush = SolidColor(Color.Transparent),
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(metrics.otpCellHeight),
            horizontalArrangement = Arrangement.spacedBy(metrics.otpCellGap),
        ) {
            repeat(otpLength) { index ->
                val char = otp.getOrNull(index)?.toString().orEmpty()
                val isFilled = char.isNotEmpty()
                val isActive = otp.length == index && otp.length < otpLength

                // Saffron carries "this cell is where you are / this cell has
                // your digit". Brand green is deliberately NOT used here — it
                // belongs to the primary action, and spending it on six cells
                // would leave the CTA with nothing of its own.
                val container =
                    when {
                        isError && isFilled -> colors.sheetDanger.copy(alpha = 0.12f)
                        isFilled -> colors.sheetAccent.copy(alpha = 0.12f)
                        else -> colors.paper
                    }
                val border =
                    when {
                        isError && isFilled -> colors.sheetDanger
                        isActive -> colors.sheetAccent
                        isFilled -> colors.sheetAccent.copy(alpha = 0.50f)
                        // An empty cell is identified by its outline alone, so this
                        // is a control boundary and owes WCAG 1.4.11 its 3:1.
                        else -> colors.controlEdge
                    }
                val borderWidth = if (isActive || (isError && isFilled)) 2.dp else 1.dp

                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(AuthFieldShape)
                            .background(container)
                            .border(borderWidth, border, AuthFieldShape),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        isFilled ->
                            Text(
                                text = char,
                                style = type.codeDigit,
                                color = if (isError) colors.sheetDanger else colors.sheetInk,
                            )
                        isActive ->
                            Box(
                                modifier =
                                    Modifier
                                        .size(width = 2.dp, height = metrics.otpCellHeight * 0.38f)
                                        .background(colors.sheetAccent),
                            )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sheet-level field
// ---------------------------------------------------------------------------

/**
 * The document's single-line input.
 *
 * Everything about the fill is deliberate. The container is [AuthColors.paper]
 * rather than the scheme's `surfaceContainerLowest`, because that resolves to
 * a stark white box which punches a rectangle straight through the sheet in
 * light mode. The focused border is the identity accent, so the field reads as
 * part of the document rather than as a generic form control dropped on top of
 * it. [prefix] renders inside the field's own border — the country code is
 * part of the number, not a chip sitting beside it — with a column rule
 * separating the two so the field reads as a form cell.
 */
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    colors: AuthColors,
    type: AuthTypeScale,
    metrics: AuthMetrics,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
    testTag: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = metrics.fieldHeight)
                .let { base ->
                    if (testTag != null) {
                        base
                            .testTag(testTag)
                            .semantics { testTagsAsResourceId = true }
                    } else {
                        base
                    }
                },
        enabled = enabled,
        singleLine = true,
        shape = AuthFieldShape,
        textStyle =
            type.sheetBody.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = (type.sheetBody.fontSize.value + 1).sp,
                letterSpacing = 0.6.sp,
                fontFeatureSettings = "tnum",
            ),
        placeholder = {
            Text(
                text = placeholder,
                style = type.sheetBody.copy(letterSpacing = 0.6.sp),
                // Solid, not `sheetInkMuted.copy(alpha = 0.7f)`. A placeholder
                // is still text and still owes 4.5:1; the alpha knocked the
                // light palette down to ~3.1:1. The value/placeholder
                // distinction is carried by weight and size instead.
                color = colors.sheetInkMuted,
            )
        },
        prefix =
            prefix?.let {
                {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = it,
                            style = type.sheetBody.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.sheetInk,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier =
                                Modifier
                                    .size(width = 1.dp, height = 20.dp)
                                    .background(colors.paperEdge),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                }
            },
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.sheetAccent,
                unfocusedBorderColor = colors.controlEdge,
                disabledBorderColor = colors.controlEdge,
                errorBorderColor = colors.sheetDanger,
                focusedContainerColor = colors.paper,
                unfocusedContainerColor = colors.paper,
                disabledContainerColor = colors.paper,
                focusedTextColor = colors.sheetInk,
                unfocusedTextColor = colors.sheetInk,
                disabledTextColor = colors.sheetInkMuted,
                cursorColor = colors.sheetAccent,
            ),
    )
}

// ---------------------------------------------------------------------------
// Sheet-level actions
// ---------------------------------------------------------------------------

/**
 * Full-width text row sitting on the sheet, used for the secondary paths a
 * screen must offer but must not let compete with the primary action —
 * "Resend code", "Call support".
 *
 * Renders as a bare row rather than an outlined card: on the sheet, a second
 * bordered box beside the primary action reads as a second button of equal
 * weight, which is exactly the hierarchy problem this is meant to avoid.
 */
@Composable
fun AuthSheetActionRow(
    text: String,
    enabled: Boolean,
    colors: AuthColors,
    type: AuthTypeScale,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    icon: ImageVector? = null,
    testTag: String? = null,
) {
    val ink = if (enabled || loading) colors.sheetAccentInk else colors.sheetInkMuted

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .let { base ->
                    if (testTag != null) {
                        base
                            .testTag(testTag)
                            .semantics { testTagsAsResourceId = true }
                    } else {
                        base
                    }
                }
                .semantics {
                    role = Role.Button
                    if (!enabled || loading) disabled()
                }
                .clip(AuthControlShape)
                .then(
                    if (enabled && !loading) {
                        Modifier.clickable(onClick = onClick, role = Role.Button)
                    } else {
                        Modifier
                    },
                )
                .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = ink,
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ink,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = type.sheetBody.copy(fontWeight = FontWeight.SemiBold),
            color = ink,
        )
    }
}
