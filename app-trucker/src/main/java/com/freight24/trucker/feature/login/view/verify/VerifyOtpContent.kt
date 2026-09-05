package com.freight24.trucker.feature.login.view.verify

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.freight24.ui.trucker.TRUCKER_HELPLINE
import com.freight24.ui.trucker.TruckerBackButton
import com.freight24.ui.trucker.TruckerDivider
import com.freight24.ui.trucker.TruckerErrorBlock
import com.freight24.ui.trucker.TruckerHelplineRow
import com.freight24.ui.trucker.TruckerOtpField
import com.freight24.ui.trucker.TruckerPrimaryButton
import com.freight24.ui.trucker.TruckerSecondaryButton
import com.freight24.ui.trucker.TruckerStepChip
import com.freight24.ui.trucker.TruckerSystemBars
import com.freight24.ui.trucker.TruckerTextButton
import com.freight24.ui.trucker.TruckerType
import com.freight24.ui.trucker.rememberTruckerColors

/**
 * Stateless OTP UI.
 *
 * Takes a [VerifyOtpUiState] and a set of [VerifyOtpActions]; holds no
 * ViewModel, fires no side effects, owns no animation. All labels are
 * precomputed in the state so this file is purely layout.
 *
 * Structure (top to bottom):
 *   back + step chip -> headline + "sent to" line (with Change) -> OTP
 *   cells -> error -> expiry line -> primary action -> resend -> (spacer)
 *   -> divider -> helpline
 */
@Composable
internal fun VerifyOtpContent(
    state: VerifyOtpUiState,
    actions: VerifyOtpActions,
    modifier: Modifier = Modifier,
) {
    val colors = rememberTruckerColors()
    TruckerSystemBars(colors.canvas)

    // Same restrained entry as login: header -> code -> actions -> footer.
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { revealed = true }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .imePadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp, bottom = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TruckerBackButton(colors = colors, onClick = actions::onBack)
            Spacer(modifier = Modifier.weight(1f))
            TruckerStepChip(text = "Step 2 of 2", colors = colors)
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(
            visible = revealed,
            enter = fadeIn(tween(220)) + expandVertically(tween(260)),
        ) {
            Column {
                Text(
                    text = "Enter the 6-digit code",
                    style = TruckerType.headline,
                    color = colors.ink,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "We sent it to this number.",
                    style = TruckerType.body,
                    color = colors.inkMuted,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Number + change affordance on one line, so the driver can check
                // the destination and fix it without hunting for a separate control.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = state.formattedPhone,
                        style = TruckerType.bodyStrong,
                        color = colors.ink,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TruckerTextButton(
                        text = "Change",
                        enabled = !state.isVerifying,
                        colors = colors,
                        onClick = actions::onChangeNumber,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(
            visible = revealed,
            enter = fadeIn(tween(220, delayMillis = 70)) +
                expandVertically(tween(280, delayMillis = 70)),
        ) {
            Column {
                TruckerOtpField(
                    otp = state.otp,
                    otpLength = state.otpLength,
                    shake = state.shake,
                    enabled = !state.isVerifying && !state.isResending && !state.isExpired,
                    isError = state.errorMessage != null,
                    colors = colors,
                    onOtpChange = actions::onOtpChange,
                )

                Spacer(modifier = Modifier.height(16.dp))

                TruckerErrorBlock(
                    message = state.errorMessage.orEmpty(),
                    colors = colors,
                    visible = state.errorMessage != null,
                    detail = state.attemptsLabel,
                )

                if (state.errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Expiry line. Switches to the warning colour in the last 30
                // seconds so it is not missed.
                Text(
                    text = state.expiryLabel,
                    style = TruckerType.caption,
                    color = if (state.isExpired || state.isExpiringSoon) colors.error else colors.inkMuted,
                    modifier = Modifier.widthIn(max = 330.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(
            visible = revealed,
            enter = fadeIn(tween(220, delayMillis = 140)) +
                expandVertically(tween(280, delayMillis = 140)),
        ) {
            Column {
                TruckerPrimaryButton(
                    text = state.primaryLabel,
                    enabled = if (state.isExpired) state.canResend else state.canVerify,
                    loading = state.isVerifying,
                    colors = colors,
                    onClick = if (state.isExpired) actions::onResend else actions::onVerify,
                    testTag = "verify_btn",
                )

                // When the code is expired the primary action already IS the
                // resend — stacking a second identical "Resend code" row
                // below it is clutter, so the secondary row is live-only.
                if (!state.isExpired) {
                    Spacer(modifier = Modifier.height(12.dp))

                    TruckerSecondaryButton(
                        text = state.resendLabel,
                        enabled = state.canResend,
                        loading = state.isResending,
                        icon = Icons.Filled.Refresh,
                        colors = colors,
                        onClick = actions::onResend,
                        testTag = "resend_btn",
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Spacer(modifier = Modifier.weight(1f))

        AnimatedVisibility(
            visible = revealed,
            enter = fadeIn(tween(220, delayMillis = 210)) +
                expandVertically(tween(280, delayMillis = 210)),
        ) {
            Column {
                TruckerDivider(colors = colors)

                Spacer(modifier = Modifier.height(16.dp))

                TruckerHelplineRow(
                    helpline = TRUCKER_HELPLINE,
                    colors = colors,
                    onClick = actions::onCallHelpline,
                )
            }
        }
    }
}
