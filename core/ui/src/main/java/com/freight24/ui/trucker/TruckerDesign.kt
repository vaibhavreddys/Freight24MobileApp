package com.freight24.ui.trucker

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.freight24.ui.theme.BeVietnamPro
import com.freight24.ui.theme.FreightBrandForest
import com.freight24.ui.theme.Sora

// ---------------------------------------------------------------------------
// Freight24 Trucker design system.
//
// Audience: Indian truck owners and fleet operators, roughly 30-55 years
// old, frequently using the app one-handed, outdoors, in bright sunlight,
// often with a system font scale turned up. The system is built for them,
// not for a design audience:
//
//   * LARGE TYPE. Body text never drops below 16sp. Field labels are 15sp
//     semibold. The CTA label is 17sp bold. Digits are 24sp. The legal
//     line is 14sp, not 10sp. Nothing on screen is smaller than 14sp.
//
//   * HIGH CONTRAST, ALWAYS. Muted text is a SOLID mid-tone
//     (inkMuted = ~4.6:1 on canvas), never a low-alpha white/black. Low
//     alpha greys disappear in sunlight and are the single worst thing
//     you can do to a 50-year-old reading a phone at a dhaba.
//
//   * BIG TARGETS. Primary action 60dp, fields 64dp, OTP cells 60dp,
//     secondary rows 56dp. Comfortable for a thumb on a moving vehicle.
//
//   * BRAND FORWARD. Freight24 deep forest green (#14422C) is the primary
//     and the CTA fill. The brand is a trust signal for this audience —
//     a nameless white form reads as a scam. The brand mark sits at the
//     top of every auth screen and the helpline is always visible.
//
//   * OBVIOUS OVER CLEVER. Labels say what they mean ("Mobile number",
//     "Enter the 6-digit code"). No eyebrow tokens, no dispatch
//     abbreviations, no monospaced designer chrome, no decorative
//     hairlines. One job per screen, one obvious primary action.
//
// This file is the shared design system for app-trucker — every component
// here is meant to be reused by the post-login screens, so the language
// established on auth carries straight through into the rest of the app.
// ---------------------------------------------------------------------------

/** The truck mark — filled rather than outlined, so it stays legible at
 *  small sizes and in bright light for the target age group. */
val TruckerBrandMark: ImageVector = Icons.Filled.LocalShipping

/** Support number shown on every trucker auth screen. Defined once so the
 *  login and OTP screens can never drift apart. */
const val TRUCKER_HELPLINE = "96201 11667"

/** `tel:` URI for [TRUCKER_HELPLINE], ready for an ACTION_DIAL intent. */
const val TRUCKER_HELPLINE_URI = "tel:+919620111667"

// ---------------------------------------------------------------------------
// Shape tokens — generous radii, nothing sharp, nothing decorative
// ---------------------------------------------------------------------------

val TruckerButtonShape = RoundedCornerShape(14.dp)
val TruckerFieldShape = RoundedCornerShape(12.dp)
val TruckerCardShape = RoundedCornerShape(16.dp)
private val TruckerCellShape = RoundedCornerShape(10.dp)

// ---------------------------------------------------------------------------
// Sizing tokens — the touch-target contract
// ---------------------------------------------------------------------------

object TruckerSizes {
    /** Primary action. Big enough to hit without looking. */
    val primaryAction = 60.dp
    /** Text fields and the OTP row. */
    val field = 64.dp
    /** Secondary / outlined rows (resend, change number). */
    val secondary = 56.dp
    /** Minimum tap target for icon buttons. */
    val iconButton = 56.dp
    /** Icon size inside button labels. */
    val labelIcon = 22.dp
}

// ---------------------------------------------------------------------------
// Color tokens
// ---------------------------------------------------------------------------

@Immutable
data class TruckerColors(
    /** Page canvas. Warm off-white in light — less glare than pure white
     *  when the driver is standing in the sun. */
    val canvas: Color,
    /** Raised surface: fields, cells, cards sitting on [canvas]. */
    val surface: Color,
    /** The Freight24 brand green. Primary action fill, brand mark. */
    val brand: Color,
    /** Pressed state of [brand]. */
    val brandPressed: Color,
    /** Pale brand wash — icon bubbles, soft fills, the step chip. */
    val brandSoft: Color,
    /** Primary text. Near-black with a green undertone. */
    val ink: Color,
    /** Secondary text. A SOLID mid-tone, never a low alpha. */
    val inkMuted: Color,
    /** Borders on [surface] and dividers on [canvas]. Clearly visible. */
    val line: Color,
    /** Warm saffron. Active focus, the expiring timer, the active OTP
     *  caret. Used sparingly — it is the only warm note on the page. */
    val accent: Color,
    /** Error text and error fill. Unambiguous red. */
    val error: Color,
    /** Text sitting on [error]. */
    val onError: Color,
    /** Text/icon sitting on [brand]. */
    val onBrand: Color,
)

/** The dark trucker palette. A named singleton rather than inline hex in
 *  the composable below, so the values are stated once and
 *  [rememberTruckerColors] only selects the mode. */
private val TruckerDarkColors = TruckerColors(
    canvas = Color(0xFF0F1A14),
    surface = Color(0xFF1B2C22),
    brand = Color(0xFF4C9B70),
    brandPressed = Color(0xFF3E8460),
    brandSoft = Color(0xFF23402F),
    ink = Color(0xFFE9F1EB),
    inkMuted = Color(0xFFA2B8AA),
    line = Color(0xFF33513E),
    accent = Color(0xFFE5A63C),
    error = Color(0xFFF08A7E),
    onError = Color(0xFF2A0B08),
    onBrand = Color(0xFF08150E),
)

/** The light trucker palette. See [TruckerDarkColors]. */
private val TruckerLightColors = TruckerColors(
    canvas = Color(0xFFF7F6F2),
    surface = Color(0xFFF6FAF6),
    brand = FreightBrandForest,
    brandPressed = Color(0xFF0D3121),
    brandSoft = Color(0xFFCDE5D5),
    ink = Color(0xFF12211A),
    inkMuted = Color(0xFF4C6055),
    line = Color(0xFFB7CDBE),
    accent = Color(0xFFB8760E),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    onBrand = Color(0xFFFFFFFF),
)

/** Returns the trucker palette for the current mode. Mode is derived from
 *  the ambient scheme's surface luminance, so the driver's light/dark
 *  override is honoured without re-wrapping the theme. */
@Composable
fun rememberTruckerColors(): TruckerColors {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return remember(dark) { if (dark) TruckerDarkColors else TruckerLightColors }
}

// ---------------------------------------------------------------------------
// Type scale — named tokens, nothing inlined at the call site
//
// Sora carries the display voice (headlines, digits); Be Vietnam Pro
// carries everything else. No monospace anywhere: a monospaced,
// token-heavy treatment reads as designer chrome to this audience and
// costs legibility at small optical sizes.
// ---------------------------------------------------------------------------

/**
 * Named type styles. A plain object rather than a `@Composable remember`
 * because none of it depends on the active theme, so every call site reads
 * the same instances without allocating per composition.
 */
object TruckerType {
    private val bodyFont = BeVietnamPro
    private val displayFont = Sora

    val headline = TextStyle(
        fontFamily = displayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 37.sp,
        letterSpacing = (-0.4).sp,
    )
    val title = TextStyle(
        fontFamily = displayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 27.sp,
    )
    val body = TextStyle(
        fontFamily = bodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 23.sp,
    )
    val bodyStrong = TextStyle(
        fontFamily = bodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 23.sp,
    )
    val fieldLabel = TextStyle(
        fontFamily = bodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
    )
    val fieldValue = TextStyle(
        fontFamily = bodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.6.sp,
    )
    /** Digits get the display face at a large optical size — the number is
     *  the thing the driver checks twice before tapping. */
    val digit = TextStyle(
        fontFamily = displayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 1.5.sp,
        fontFeatureSettings = "tnum",
    )
    val button = TextStyle(
        fontFamily = bodyFont,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.3.sp,
    )
    val caption = TextStyle(
        fontFamily = bodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )
    val captionStrong = TextStyle(
        fontFamily = bodyFont,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
    )
}

// ---------------------------------------------------------------------------
// System bars
// ---------------------------------------------------------------------------

/** Paints the status bar to [canvas] with matching icon contrast. */
@Composable
fun TruckerSystemBars(canvas: Color) {
    val view = LocalView.current
    DisposableEffect(view, canvas) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        val previousColor = window.statusBarColor
        val previousLightIcons = controller.isAppearanceLightStatusBars

        window.statusBarColor = canvas.toArgb()
        controller.isAppearanceLightStatusBars = canvas.luminance() > 0.5f

        onDispose {
            window.statusBarColor = previousColor
            controller.isAppearanceLightStatusBars = previousLightIcons
        }
    }
}

// ---------------------------------------------------------------------------
// Brand header — the trust anchor at the top of every auth screen
// ---------------------------------------------------------------------------

/**
 * The Freight24 brand mark: a filled truck glyph in a pale brand bubble,
 * the wordmark, and an optional role chip ("TRUCKER").
 *
 * This sits at the top of the login and OTP screens so the driver always
 * knows whose app they are in. For this audience an unbranded form is a
 * phishing risk, not a clean aesthetic.
 */
@Composable
fun TruckerBrandHeader(
    colors: TruckerColors,
    modifier: Modifier = Modifier,
    role: String? = "Trucker",
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(colors.brandSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = TruckerBrandMark,
                contentDescription = null,
                tint = colors.brand,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Freight24",
                style = TruckerType.title,
                color = colors.ink,
            )
            if (role != null) {
                Text(
                    text = role,
                    style = TruckerType.caption,
                    color = colors.inkMuted,
                )
            }
        }
    }
}

/** Back affordance with a 56dp target. */
@Composable
fun TruckerBackButton(
    colors: TruckerColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(TruckerSizes.iconButton)
            .offset(x = (-12).dp),
    ) {
        Icon(
            imageVector = Icons.Filled.ArrowBackIosNew,
            contentDescription = "Back",
            tint = colors.ink,
            modifier = Modifier.size(20.dp),
        )
    }
}

// ---------------------------------------------------------------------------
// Primary action
// ---------------------------------------------------------------------------

/**
 * The one obvious action on the screen: a 60dp solid brand-green pill with
 * a 17sp bold label.
 *
 * Disabled state is a flat brand wash with muted ink — still clearly a
 * button, just not yet available, rather than a greyed-out ghost. Loading
 * keeps the brand fill and swaps the label for a spinner so the control
 * never looks dead mid-request.
 */
@Composable
fun TruckerPrimaryButton(
    text: String,
    enabled: Boolean,
    colors: TruckerColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    testTag: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val container by animateColorAsState(
        targetValue = when {
            !enabled -> colors.brandSoft
            pressed -> colors.brandPressed
            else -> colors.brand
        },
        animationSpec = tween(durationMillis = 110),
        label = "trucker-button",
    )
    val contentColor = if (enabled) colors.onBrand else colors.inkMuted

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(TruckerSizes.primaryAction)
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
            .clip(TruckerButtonShape)
            .background(container)
            .then(
                if (enabled && !loading) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .semantics { contentDescription = "Loading" },
                strokeWidth = 2.5.dp,
                color = contentColor,
            )
        } else {
            Text(text = text, style = TruckerType.button, color = contentColor)
        }
    }
}

/**
 * Secondary action: a 56dp outlined row with a leading icon. Used for
 * resend and other non-primary paths so they never compete with
 * [TruckerPrimaryButton].
 */
@Composable
fun TruckerSecondaryButton(
    text: String,
    enabled: Boolean,
    colors: TruckerColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    icon: ImageVector? = null,
    testTag: String? = null,
) {
    val contentColor = if (enabled) colors.ink else colors.inkMuted
    val borderColor = if (enabled) colors.line else colors.line.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(TruckerSizes.secondary)
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
            .clip(TruckerButtonShape)
            .background(colors.surface)
            .border(width = 1.5.dp, color = borderColor, shape = TruckerButtonShape)
            .then(
                if (enabled && !loading) {
                    Modifier.clickable(onClick = onClick, role = Role.Button)
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = contentColor,
                )
                Spacer(modifier = Modifier.width(10.dp))
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(TruckerSizes.labelIcon),
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                style = TruckerType.bodyStrong,
                color = contentColor,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Labelled field
// ---------------------------------------------------------------------------

/**
 * A labelled text field: 15sp semibold label above a 64dp surface with a
 * visible border that turns brand green on focus.
 *
 * [prefix] renders a static leading block (used for "+91") inside the
 * field's own border, so the country code reads as part of the number
 * rather than as a separate chip beside it.
 */
@Composable
fun TruckerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    enabled: Boolean = true,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Phone,
        imeAction = ImeAction.Done,
    ),
    testTag: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val borderColor by animateColorAsState(
        targetValue = when {
            focused -> colors.brand
            else -> colors.line
        },
        animationSpec = tween(durationMillis = 140),
        label = "trucker-field-border",
    )
    val borderWidth = if (focused) 2.dp else 1.5.dp

    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = TruckerType.fieldLabel, color = colors.ink)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(TruckerSizes.field)
                .clip(TruckerFieldShape)
                .background(colors.surface)
                .border(width = borderWidth, color = borderColor, shape = TruckerFieldShape)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (prefix != null) {
                // Same 30sp line box as the input text below, so the prefix
                // and the digits share one optical centre instead of two
                // independently-centred boxes drifting apart.
                Text(
                    text = prefix,
                    style = TruckerType.fieldValue.copy(lineHeight = 30.sp),
                    color = colors.inkMuted,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(26.dp)
                        .background(colors.line),
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    // The tag belongs on the editable node itself —
                    // `testTagsAsResourceId` only exposes the id on the node
                    // it is applied to, so tagging the wrapper leaves the
                    // EditText's resource-id empty and breaks the
                    // `phone_input` contract the Maestro flows type into.
                    modifier = Modifier
                        .fillMaxWidth()
                        .let { base ->
                            if (testTag != null) {
                                base
                                    .testTag(testTag)
                                    .semantics { testTagsAsResourceId = true }
                            } else {
                                base
                            }
                        },
                    interactionSource = interactionSource,
                    enabled = enabled,
                    singleLine = true,
                    keyboardOptions = keyboardOptions,
                    visualTransformation = visualTransformation
                        ?: androidx.compose.ui.text.input.VisualTransformation.None,
                    textStyle = TruckerType.digit.copy(color = colors.ink),
                    cursorBrush = SolidColor(colors.brand),
                    decorationBox = { inner ->
                        // Centre the placeholder in the input's 30sp line box.
                        // Without this it top-aligns inside the decoration box
                        // and sits ~3sp above the "+91" prefix.
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = TruckerType.fieldValue.copy(color = colors.inkMuted),
                                )
                            }
                            inner()
                        }
                    },
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// OTP cells
// ---------------------------------------------------------------------------

/**
 * Six-cell code entry.
 *
 * Cells are 60dp tall with clearly visible borders and 24sp display-face
 * digits. The active cell carries a 2dp brand border and a saffron caret.
 * A filled cell gets a pale brand wash so the driver can see at a glance
 * how far through the code they are.
 *
 * The real BasicTextField sits invisibly behind the cells. It MUST stay
 * full-size and in the hierarchy: a 1dp/hidden field drops out of the
 * accessibility tree and breaks `tapOn: id: otp_input` + `inputText` in
 * the Maestro flows. Hence alpha(0.01f) rather than size(0.dp).
 */
@Composable
fun TruckerOtpField(
    otp: String,
    otpLength: Int,
    shake: Boolean,
    enabled: Boolean,
    colors: TruckerColors,
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
        modifier = modifier
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
            modifier = Modifier
                .fillMaxWidth()
                .height(TruckerSizes.field)
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

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TruckerSizes.field),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(otpLength) { index ->
                    val char = otp.getOrNull(index)?.toString().orEmpty()
                    val isFilled = char.isNotEmpty()
                    val isActive = otp.length == index && !isFilled

                    val container = when {
                        isError && isFilled -> colors.error.copy(alpha = 0.12f)
                        isFilled -> colors.brandSoft
                        else -> colors.surface
                    }
                    val border = when {
                        isError && isFilled -> colors.error
                        isActive -> colors.brand
                        isFilled -> colors.brand.copy(alpha = 0.45f)
                        else -> colors.line
                    }
                    val borderWidth = if (isActive || (isError && isFilled)) 2.dp else 1.5.dp

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(TruckerCellShape)
                            .background(container)
                            .border(width = borderWidth, color = border, shape = TruckerCellShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        when {
                            isFilled -> Text(
                                text = char,
                                style = TruckerType.digit,
                                color = if (isError) colors.error else colors.ink,
                            )
                            isActive -> Box(
                                modifier = Modifier
                                    .size(width = 2.dp, height = 26.dp)
                                    .background(colors.accent),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Error block
// ---------------------------------------------------------------------------

/**
 * Inline failure message: a warning icon plus the message at 16sp on a
 * tinted error surface. Big enough to actually read, and it states what
 * to do next rather than just what went wrong.
 */
@Composable
fun TruckerErrorBlock(
    message: String,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    detail: String? = null,
) {
    AnimatedVisibility(
        visible = visible && message.isNotBlank(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(TruckerCardShape)
                .background(colors.error.copy(alpha = 0.12f))
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = colors.error,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message,
                    style = TruckerType.bodyStrong,
                    color = colors.error,
                )
                if (detail != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = detail,
                        style = TruckerType.caption,
                        color = colors.inkMuted,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Helpline row — the trust/escape hatch on every auth screen
// ---------------------------------------------------------------------------

/**
 * Always-on helpline. 64dp tall, a phone icon in a pale brand bubble, the
 * number at 17sp bold so it can be read at arm's length.
 *
 * For this audience a phone number is the primary trust signal and the
 * real fallback when the flow goes wrong — it must never be hidden behind
 * a "contact us" link.
 */
@Composable
fun TruckerHelplineRow(
    helpline: String,
    colors: TruckerColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    caption: String = "Need help? Call our helpline",
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(TruckerSizes.field)
            .clip(TruckerCardShape)
            .background(colors.surface)
            .border(width = 1.5.dp, color = colors.line, shape = TruckerCardShape)
            .clickable(onClick = onClick, role = Role.Button)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.brandSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Call,
                contentDescription = null,
                tint = colors.brand,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = caption,
                style = TruckerType.caption,
                color = colors.inkMuted,
            )
            Text(
                text = helpline,
                style = TruckerType.captionStrong,
                color = colors.ink,
            )
        }
    }
}

/** Standard 1.5dp divider on the canvas. */
@Composable
fun TruckerDivider(colors: TruckerColors, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.5.dp)
            .background(colors.line),
    )
}

/** Legal microcopy at 14sp — never smaller, so it stays readable. */
@Composable
fun TruckerLegalNote(
    text: String,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = TruckerType.caption,
        color = colors.inkMuted,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}

/** "Step 2 of 2" chip — a pale brand pill, plain language. */
@Composable
fun TruckerStepChip(
    text: String,
    colors: TruckerColors,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(colors.brandSoft)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = TruckerType.captionStrong,
            color = colors.brand,
        )
    }
}

/** A plain-language inline text button ("Change", "Resend code").
 *
 *  Padded out to a 48dp minimum target so it stays tappable for a thumb
 *  on a moving vehicle. */
@Composable
fun TruckerTextButton(
    text: String,
    enabled: Boolean,
    colors: TruckerColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .semantics {
                role = Role.Button
                if (!enabled) disabled()
            }
            .clip(TruckerButtonShape)
            .then(
                if (enabled) Modifier.clickable(onClick = onClick, role = Role.Button) else Modifier,
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = TruckerType.bodyStrong,
            color = if (enabled) colors.brand else colors.inkMuted,
        )
    }
}
