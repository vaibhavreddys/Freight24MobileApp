package com.freight24.trucker.feature.login.view.signin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.freight24.ui.auth.AuthIdentity
import com.freight24.ui.auth.AuthLegalNote
import com.freight24.ui.auth.AuthMasthead
import com.freight24.ui.auth.AuthOverline
import com.freight24.ui.auth.AuthPrimaryAction
import com.freight24.ui.auth.AuthRule
import com.freight24.ui.auth.AuthSheet
import com.freight24.ui.auth.AuthTextField
import com.freight24.ui.auth.rememberAuthTokens
import com.freight24.ui.trucker.IndianPhoneVisualTransformation
import com.freight24.ui.trucker.TRUCKER_HELPLINE
import com.freight24.ui.trucker.TruckerAuthErrorBlock
import com.freight24.ui.trucker.TruckerAuthSupportRow
import com.freight24.ui.trucker.TruckerBrandMark
import com.freight24.ui.trucker.TruckerSystemBars

/**
 * Stateless login UI — the driver's copy of the auth document.
 *
 * Takes a [LoginUiState] and a set of [LoginActions]; holds no ViewModel,
 * fires no side effects, owns no animation. Everything it shows comes from
 * the state, everything it does goes out through the actions.
 *
 * Structure (top to bottom), which is the same skeleton app-connect prints:
 *   masthead (brand row -> seam -> headline -> supporting line)
 *   sheet (field label -> number field -> primary action -> error
 *          -> rule -> helpline -> legal)
 *
 * What makes this the driver's copy rather than a recolour of Connect's is in
 * the tokens, not here: [AuthIdentity.TRUCKER] supplies a cooler sheet, a
 * saffron accent, a heavier display scale and 60/64dp controls. This file
 * contains no colour, size or type literal at all.
 */
@Composable
internal fun LoginContent(
    state: LoginUiState,
    actions: LoginActions,
    modifier: Modifier = Modifier,
) {
    val tokens = rememberAuthTokens(AuthIdentity.TRUCKER)
    val colors = tokens.colors
    // The document is edge-to-edge, so the status bar sits on the masthead —
    // which is the deep brand block in BOTH modes. Driving the icon contrast
    // from `paper` would paint dark icons onto dark green in light mode.
    TruckerSystemBars(colors.field)

    // A single fade on first paint. No expand, so the sheet cannot jump: the
    // document simply arrives. Anything more is noise on a screen that gets
    // used one-handed, in sunlight, several times a day.
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { revealed = true }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.paper)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
    ) {
        AuthMasthead(
            colors = colors,
            type = tokens.type,
            metrics = tokens.metrics,
            overline = "FREIGHT24 · TRUCKER",
            headline = "Enter your mobile number",
            body = "We'll send a 6-digit code to this number. No password to remember.",
            brandMark = TruckerBrandMark,
        )

        AnimatedVisibility(visible = revealed, enter = fadeIn(tween(200))) {
            AuthSheet(colors = colors, metrics = tokens.metrics) {
                Column {
                    AuthOverline(
                        text = "MOBILE NUMBER",
                        color = colors.sheetInkMuted,
                        type = tokens.type,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AuthTextField(
                        value = state.phoneNumber,
                        onValueChange = actions::onPhoneChange,
                        placeholder = "98765 43210",
                        prefix = "+91",
                        enabled = !state.isSending,
                        colors = colors,
                        type = tokens.type,
                        metrics = tokens.metrics,
                        visualTransformation = IndianPhoneVisualTransformation,
                        testTag = "phone_input",
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // The auto-send countdown is drawn as a band wiping across
                    // the brand fill, so it reads as "sending shortly" rather
                    // than as a colour flash. The button stays tappable
                    // throughout.
                    AuthPrimaryAction(
                        label = if (state.isPhoneComplete) "Send code" else "Enter your number",
                        enabled = state.canSend,
                        loading = state.isSending,
                        progress = state.autoSendProgress,
                        onClick = actions::onSendCode,
                        testTag = "send_otp_btn",
                        colors = colors,
                        type = tokens.type,
                        metrics = tokens.metrics,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TruckerAuthErrorBlock(
                        message = state.errorMessage.orEmpty(),
                        colors = colors,
                        type = tokens.type,
                        visible = state.errorMessage != null,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AuthRule(colors = colors)

                    Spacer(modifier = Modifier.height(16.dp))

                    TruckerAuthSupportRow(
                        caption = "Need help? Call our helpline",
                        number = TRUCKER_HELPLINE,
                        colors = colors,
                        type = tokens.type,
                        onClick = actions::onCallHelpline,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AuthLegalNote(
                        text =
                            "By continuing you agree to the Freight24 Terms of " +
                                "Service and Privacy Policy.",
                        color = colors.sheetInkMuted,
                        type = tokens.type,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
