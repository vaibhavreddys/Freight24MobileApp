package com.freight24.trucker.feature.login.view.signin

import androidx.compose.animation.core.Easing
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Image  // → actually:
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.freight24.trucker.R

// ─── Color Tokens ─────────────────────────────────────────────────────────────
val Green500        = Color(0xFF2A9E58)
val GreenDark       = Color(0xFF1A2A1A)
val GreenDotHalo    = Color(0x212A9E58)
val SurfaceLight    = Color(0xFFF4F6F4)
val CardWhite       = Color(0xFFFFFFFF)
val BorderLight     = Color(0xFFD6DDD6)
val InputDivider    = Color(0xFFE6EBE6)
val PlaceholderText = Color(0xFFB0BBB0)
val PrefixSub       = Color(0xFF96A896)
val FooterText      = Color(0xFFC0CAC0)
val FooterLink      = Color(0xFF96A896)
val StepDotInactive = Color(0xFFD6DDD6)
val StepSegment     = Color(0xFFE4EAE4)
// Road colors — increased contrast vs original
val RoadMain        = Color(0xCC8FA88F)   // was 0x99A8B9A8 — darker, warmer green-gray
val RoadMinor       = Color(0x3D8FA88F)   // was 0x59B2C0B2 — lighter relative to main
val RoadDash        = Color(0xCCF4F6F4)   // center dash line
val JunctionGreen   = Color(0xFF1E8C46)

// ─── Fonts ────────────────────────────────────────────────────────────────────
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs
)

val syneFamily = FontFamily(
    Font(googleFont = GoogleFont("Syne"), fontProvider = provider, weight = FontWeight.ExtraBold)
)

val spaceGroteskFamily = FontFamily(
    Font(googleFont = GoogleFont("Space Grotesk"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Space Grotesk"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Space Grotesk"), fontProvider = provider, weight = FontWeight.Bold)
)

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
internal fun NewLoginScreen(
    viewModel: NewLoginViewModel = hiltViewModel(),
    onNavigateToOtpVerify: (String) -> Unit
) {
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val navigateToVerifyOtp by viewModel.navigateToVerifyOtp.collectAsState()

    LaunchedEffect(navigateToVerifyOtp) {
        navigateToVerifyOtp?.let { phone ->
            onNavigateToOtpVerify(phone)
            viewModel.onNavigationHandled()
        }
    }

    // ── Animation state: triggered once on first composition ──────────────────
    var animationStarted by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val rawImeHeight by remember {
        derivedStateOf {
            with(density) { imeInsets.getBottom(density).toDp() }
        }
    }
    val animatedImeHeight by animateDpAsState(
        targetValue    = rawImeHeight,
        animationSpec  = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label          = "imeHeight"
    )
    LaunchedEffect(Unit) { animationStarted = true }

    // Map fades in over 700ms
    val mapAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "mapAlpha"
    )

    // Form slides up from 40px below and fades in, delayed 300ms after map starts
    // REPLACE these two:
    val formAlpha by animateFloatAsState(
        targetValue    = if (animationStarted) 1f else 0f,
        animationSpec  = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness    = Spring.StiffnessLow   // ~500ms, very gentle
        ),
        label = "formAlpha"
    )
    val formOffsetY by animateFloatAsState(
        targetValue    = if (animationStarted) 0f else 40f,
        animationSpec  = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "formOffsetY"
    )

    // Logo slides in from left, delayed 400ms
    val logoAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 400, easing = FastOutSlowInEasing),
        label = "logoAlpha"
    )
    val logoOffsetX by animateFloatAsState(
        targetValue = if (animationStarted) 0f else -24f,
        animationSpec = tween(durationMillis = 400, delayMillis = 400, easing = FastOutSlowInEasing),
        label = "logoOffsetX"
    )

    // Input field fades in last, delayed 550ms
    val inputAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 550, easing = FastOutSlowInEasing),
        label = "inputAlpha"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        val screenHeight = maxHeight

        // ── Background layers (non-layout, purely visual) ──────────────────────
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.60f)
                .alpha(mapAlpha)
        ) {
            val cw = size.width; val ch = size.height
            drawMainRoads(cw, ch)
            drawMinorRoads(cw, ch)
            drawDiagonalRoads(cw, ch)
            drawJunctions(cw, ch)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.28f)
                .offset(y = screenHeight * 0.34f)
                .background(Brush.verticalGradient(listOf(Color.Transparent, SurfaceLight)))
        )

        // ── Form layer — imePadding here, weight spacer above form ─────────────
        // imePadding() shrinks this Column's height when keyboard opens.
        // Spacer(weight(1f)) absorbs the shrink → form rides up above keyboard.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Form content pinned to bottom of available space
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 26.dp, end = 26.dp, bottom = 36.dp)
                    .graphicsLayer {
                        alpha        = formAlpha
                        translationY = formOffsetY.dp.toPx()
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo row
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha        = logoAlpha
                            translationX = logoOffsetX.dp.toPx()
                        }
                ) {
                    Image(
                        painter            = painterResource(id = R.drawable.ic_f24_logo),
                        contentDescription = "Freight 24 Logo",
                        modifier           = Modifier.size(38.dp),
                        contentScale       = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = GreenDark)) { append("Freight") }
                            withStyle(SpanStyle(color = Green500))  { append("24") }
                        },
                        fontFamily    = syneFamily,
                        fontWeight    = FontWeight.ExtraBold,
                        fontSize      = 36.sp,
                        letterSpacing = (-0.5).sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                StepIndicator()
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(inputAlpha)
                ) {
                    PhoneInputField(
                        phoneNumber         = phoneNumber,
                        onPhoneNumberChange = { viewModel.onPhoneNumberChange(it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CtaButton(
                        enabled = phoneNumber.length == 10,
                        onClick = { viewModel.onSendOtpClick() }
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Footer()
                }
            }
        }
    }
}

// ─── Road Map Drawing ─────────────────────────────────────────────────────────

private fun DrawScope.drawMainRoads(cw: Float, ch: Float) {
    val roads = listOf(
        floatArrayOf(0f, 0.08f, 1f, 0.08f),
        floatArrayOf(0f, 0.32f, 1f, 0.32f),
        floatArrayOf(0f, 0.56f, 1f, 0.56f),
        floatArrayOf(0.35f, 0f, 0.35f, 1f),
        floatArrayOf(0.82f, 0f, 0.82f, 1f),
    )
    roads.forEach { r ->
        val s = Offset(r[0] * cw, r[1] * ch)
        val e = Offset(r[2] * cw, r[3] * ch)
        // Road surface
        drawLine(color = RoadMain, start = s, end = e, strokeWidth = 4.dp.toPx())
        // Center dash
        drawLine(
            color = RoadDash, start = s, end = e,
            strokeWidth = 0.9.dp.toPx(),
            pathEffect  = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 10.dp.toPx()), 0f)
        )
    }
}

private fun DrawScope.drawMinorRoads(cw: Float, ch: Float) {
    val roads = listOf(
        floatArrayOf(0f,    0.20f, 0.62f, 0.20f),
        floatArrayOf(0.38f, 0.20f, 1f,    0.20f),
        floatArrayOf(0f,    0.44f, 0.45f, 0.44f),
        floatArrayOf(0.55f, 0.44f, 1f,    0.44f),
        floatArrayOf(0.15f, 0f,    0.15f, 1f   ),
        floatArrayOf(0.62f, 0f,    0.62f, 1f   ),
    )
    roads.forEach { r ->
        drawLine(
            color       = RoadMinor,
            start       = Offset(r[0] * cw, r[1] * ch),
            end         = Offset(r[2] * cw, r[3] * ch),
            strokeWidth = 1.2.dp.toPx()              // was 2dp — now clearly thinner than main
        )
    }
}

private fun DrawScope.drawDiagonalRoads(cw: Float, ch: Float) {
    val roads = listOf(
        floatArrayOf(0f,    0.26f, 0.35f, 0f   ),
        floatArrayOf(0.62f, 0f,    1f,    0.40f),
    )
    roads.forEach { r ->
        drawLine(
            color       = RoadMinor.copy(alpha = 0.55f),  // slightly more visible than minor
            start       = Offset(r[0] * cw, r[1] * ch),
            end         = Offset(r[2] * cw, r[3] * ch),
            strokeWidth = 2.2.dp.toPx()
        )
    }
}

private fun DrawScope.drawJunctions(cw: Float, ch: Float) {
    val junctions = listOf(
        0.15f to 0.08f, 0.35f to 0.08f, 0.62f to 0.08f, 0.82f to 0.08f,
        0.15f to 0.20f, 0.35f to 0.20f, 0.62f to 0.20f,
        0.15f to 0.32f, 0.35f to 0.32f, 0.62f to 0.32f, 0.82f to 0.32f,
        0.35f to 0.44f, 0.62f to 0.44f, 0.82f to 0.44f,
        0.15f to 0.56f, 0.35f to 0.56f, 0.62f to 0.56f, 0.82f to 0.56f,
    )
    junctions.forEach { (x, y) ->
        val c = Offset(x * cw, y * ch)
        // Halo — smaller and more transparent than before
        drawCircle(color = JunctionGreen.copy(alpha = 0.10f), radius = 3.5.dp.toPx(), center = c)
        // Dot — smaller and less saturated than before
        drawCircle(color = JunctionGreen.copy(alpha = 0.50f), radius = 1.8.dp.toPx(), center = c)
    }
}

// ─── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun StepIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(Green500)
                .border(3.dp, GreenDotHalo, CircleShape)
        )
        Box(modifier = Modifier.weight(1f).height(1.dp).background(StepSegment))
        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(StepDotInactive))
        Box(modifier = Modifier.weight(1f).height(1.dp).background(StepSegment))
        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(StepDotInactive))
    }
}

@Composable
private fun PhoneInputField(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardWhite)
                .border(1.dp, BorderLight, RoundedCornerShape(14.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text("🇮🇳", fontSize = 14.sp)
                Text("+91", color = PrefixSub, fontSize = 10.sp,
                    fontFamily = spaceGroteskFamily, fontWeight = FontWeight.Normal)
            }

            Box(modifier = Modifier.width(1.dp).height(22.dp).background(InputDivider))

            BasicTextField(
                value         = phoneNumber,
                onValueChange = onPhoneNumberChange,
                modifier      = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction    = ImeAction.Done
                ),
                singleLine  = true,
                textStyle   = TextStyle(
                    color      = GreenDark,
                    fontSize   = 14.sp,
                    fontFamily = spaceGroteskFamily,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(Green500),
                decorationBox = { innerTextField ->
                    Box {
                        if (phoneNumber.isEmpty()) {
                            Text(
                                "Mobile number",
                                color      = PlaceholderText,
                                fontSize   = 14.sp,
                                fontFamily = spaceGroteskFamily,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        // Bottom accent line
        Box(
            modifier = Modifier
                .fillMaxWidth(0.64f)
                .height(1.dp)
                .align(Alignment.CenterHorizontally)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Green500.copy(alpha = 0.22f), Color.Transparent)
                    )
                )
        )
    }
}

@Composable
private fun CtaButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape    = RoundedCornerShape(14.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor         = GreenDark,
            contentColor           = SurfaceLight,
            disabledContainerColor = GreenDark.copy(alpha = 0.4f),
            disabledContentColor   = SurfaceLight.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
    ) {
        Row(
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Send OTP",
                fontFamily    = spaceGroteskFamily,
                fontWeight    = FontWeight.Bold,
                fontSize      = 14.sp,
                letterSpacing = 0.3.sp
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector     = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier        = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun Footer() {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text       = "Terms of Service",
            color      = FooterLink,
            fontSize   = 9.sp,
            fontFamily = spaceGroteskFamily,
            fontWeight = FontWeight.Medium,
            modifier   = Modifier.clickable { /* open terms URL */ }
        )
        Text("  ·  ", color = FooterText, fontSize = 9.sp, fontFamily = spaceGroteskFamily)
        Text(
            text       = "Privacy Policy",
            color      = FooterLink,
            fontSize   = 9.sp,
            fontFamily = spaceGroteskFamily,
            fontWeight = FontWeight.Medium,
            modifier   = Modifier.clickable { /* open privacy URL */ }
        )
    }
}
