package com.freight24.trucker.feature.login.view.verify

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.freight24.ui.auth.AuthDestinationRow
import com.freight24.ui.auth.AuthIdentity
import com.freight24.ui.auth.AuthMasthead
import com.freight24.ui.auth.AuthOtpCells
import com.freight24.ui.auth.AuthOverline
import com.freight24.ui.auth.AuthPrimaryAction
import com.freight24.ui.auth.AuthRule
import com.freight24.ui.auth.AuthSheet
import com.freight24.ui.auth.AuthSheetActionRow
import com.freight24.ui.auth.AuthStep
import com.freight24.ui.auth.AuthTimerMeter
import com.freight24.ui.auth.rememberAuthTokens
import com.freight24.ui.trucker.TRUCKER_HELPLINE
import com.freight24.ui.trucker.TruckerAuthErrorBlock
import com.freight24.ui.trucker.TruckerAuthSupportRow
import com.freight24.ui.trucker.TruckerBrandMark
import com.freight24.ui.trucker.TruckerSystemBars

/**
 * Stateless OTP UI — the driver's copy, step 2.
 *
 * Takes a [VerifyOtpUiState] and a set of [VerifyOtpActions]; holds no
 * ViewModel, fires no side effects, owns no animation. All labels are
 * precomputed in the state, including the meter's readout and fill, so this
 * file is purely layout.
 *
 * Structure (top to bottom), the same skeleton app-connect prints:
 *   masthead (back + brand row -> step meter -> headline)
 *   sheet (destination row -> code cells -> error -> expiry meter
 *          -> primary action -> resend -> rule -> helpline)
 *
 * One deliberate departure from Connect's copy: the sheet does not repeat the
 * headline. Connect prints "Confirm it's you" in the masthead and then "Verify
 * your number" again on the sheet; the second line restates the first and
 * costs a driver a line of reading to learn nothing. The masthead states the
 * task, and the sheet starts with where the code went.
 */
@Composable
internal fun VerifyOtpContent(
    state: VerifyOtpUiState,
    actions: VerifyOtpActions,
    modifier: Modifier = Modifier,
) {
    val tokens = rememberAuthTokens(AuthIdentity.TRUCKER)
    val colors = tokens.colors
    // The document is edge-to-edge, so the status bar sits on the masthead —
    // which is the deep brand block in BOTH modes. Driving the icon contrast
    // from `paper` would paint dark icons onto dark green in light mode.
    TruckerSystemBars(colors.field)

    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { revealed = true }

    Column(
        modifier = modifier
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
            headline = "Confirm it's you",
            brandMark = TruckerBrandMark,
            onBack = actions::onBack,
            step = AuthStep(current = 2, total = 2),
            topPadding = 16.dp,
            bottomPadding = 30.dp,
        )

        AnimatedVisibility(visible = revealed, enter = fadeIn(tween(200))) {
            AuthSheet(colors = colors, metrics = tokens.metrics) {
                Column {
                    AuthOverline(
                        text = "CODE SENT TO",
                        color = colors.sheetInkMuted,
                        type = tokens.type,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // The number and the way to correct it are the same
                    // control, so there is no separate "wrong number?" link to
                    // hunt for.
                    AuthDestinationRow(
                        number = state.formattedPhone,
                        colors = colors,
                        type = tokens.type,
                        onClick = actions::onChangeNumber,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AuthOtpCells(
                        otp = state.otp,
                        otpLength = state.otpLength,
                        shake = state.shake,
                        enabled = !state.isVerifying && !state.isResending && !state.isExpired,
                        isError = state.errorMessage != null,
                        colors = colors,
                        type = tokens.type,
                        metrics = tokens.metrics,
                        onOtpChange = actions::onOtpChange,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TruckerAuthErrorBlock(
                        message = state.errorMessage.orEmpty(),
                        colors = colors,
                        type = tokens.type,
                        visible = state.errorMessage != null,
                        detail = state.attemptsLabel,
                    )

                    if (state.errorMessage != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    AuthTimerMeter(
                        label = "CODE EXPIRES IN",
                        value = state.expiryClock,
                        fraction = state.expiryFraction,
                        colors = colors,
                        type = tokens.type,
                        urgent = state.isExpired || state.isExpiringSoon,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Once the code has expired the primary action IS the
                    // resend — stacking an identical "Resend code" row under
                    // it would be two controls for one job.
                    AuthPrimaryAction(
                        label = state.primaryLabel,
                        enabled = if (state.isExpired) state.canResend else state.canVerify,
                        loading = state.isVerifying,
                        onClick = if (state.isExpired) actions::onResend else actions::onVerify,
                        testTag = "verify_btn",
                        colors = colors,
                        type = tokens.type,
                        metrics = tokens.metrics,
                    )

                    if (!state.isExpired) {
                        Spacer(modifier = Modifier.height(4.dp))

                        AuthSheetActionRow(
                            text = state.resendLabel,
                            enabled = state.canResend,
                            loading = state.isResending,
                            icon = Icons.Filled.Refresh,
                            colors = colors,
                            type = tokens.type,
                            onClick = actions::onResend,
                            testTag = "resend_btn",
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    AuthRule(colors = colors)

                    Spacer(modifier = Modifier.height(16.dp))

                    TruckerAuthSupportRow(
                        caption = "Need help? Call our helpline",
                        number = TRUCKER_HELPLINE,
                        colors = colors,
                        type = tokens.type,
                        onClick = actions::onCallHelpline,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
