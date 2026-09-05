package com.freight24.trucker.feature.login.view.signin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.freight24.ui.trucker.TRUCKER_HELPLINE
import com.freight24.ui.trucker.TruckerBrandHeader
import com.freight24.ui.trucker.TruckerButtonShape
import com.freight24.ui.trucker.TruckerDivider
import com.freight24.ui.trucker.TruckerErrorBlock
import com.freight24.ui.trucker.TruckerHelplineRow
import com.freight24.ui.trucker.TruckerLegalNote
import com.freight24.ui.trucker.TruckerPrimaryButton
import com.freight24.ui.trucker.TruckerSystemBars
import com.freight24.ui.trucker.TruckerTextField
import com.freight24.ui.trucker.TruckerType
import com.freight24.ui.trucker.IndianPhoneVisualTransformation
import com.freight24.ui.trucker.rememberTruckerColors

/**
 * Stateless login UI.
 *
 * Takes a [LoginUiState] and a set of [LoginActions]; holds no ViewModel,
 * fires no side effects, owns no animation. Everything it shows comes from
 * the state, everything it does goes out through the actions. This is the
 * only file that changes when the look changes.
 *
 * Structure (top to bottom):
 *   brand header -> headline + explanation -> number field -> primary
 *   action -> error -> (spacer) -> divider -> helpline -> legal
 */
@Composable
internal fun LoginContent(
    state: LoginUiState,
    actions: LoginActions,
    modifier: Modifier = Modifier,
) {
    val colors = rememberTruckerColors()
    TruckerSystemBars(colors.canvas)

    // One restrained entry choreography: header -> form -> footer, staggered
    // by ~70ms. No springs or loops — this screen is used one-handed in
    // bright sunlight, so motion stays out of the way after first paint.
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
            .padding(top = 20.dp, bottom = 24.dp),
    ) {
        TruckerBrandHeader(colors = colors)

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
            visible = revealed,
            enter = fadeIn(tween(220)) + expandVertically(tween(260)),
        ) {
            Column {
                Text(
                    text = "Enter your mobile number",
                    style = TruckerType.headline,
                    color = colors.ink,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "We'll send a 6-digit code to this number to confirm it's you. No password to remember.",
                    style = TruckerType.body,
                    color = colors.inkMuted,
                    modifier = Modifier.widthIn(max = 330.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
            visible = revealed,
            enter = fadeIn(tween(220, delayMillis = 70)) +
                expandVertically(tween(280, delayMillis = 70)),
        ) {
            Column {
                TruckerTextField(
                    value = state.phoneNumber,
                    onValueChange = actions::onPhoneChange,
                    label = "Mobile number",
                    placeholder = "98765 43210",
                    prefix = "+91",
                    enabled = !state.isSending,
                    colors = colors,
                    visualTransformation = IndianPhoneVisualTransformation,
                    testTag = "phone_input",
                )

                Spacer(modifier = Modifier.height(24.dp))

                // The auto-send countdown is drawn as a band wiping across the brand
                // fill, so it reads as "sending shortly" rather than as a colour
                // flash. The button stays fully tappable throughout. Hidden while
                // the request is in flight so the spinner stays legible.
                Box {
                    TruckerPrimaryButton(
                        text = if (state.isPhoneComplete) "Send code" else "Enter your number",
                        enabled = state.canSend,
                        loading = state.isSending,
                        colors = colors,
                        onClick = actions::onSendCode,
                        testTag = "send_otp_btn",
                    )
                    if (state.autoSendProgress > 0f && !state.isSending) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(TruckerButtonShape),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(state.autoSendProgress.coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .background(colors.onBrand.copy(alpha = 0.22f)),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TruckerErrorBlock(
                    message = state.errorMessage.orEmpty(),
                    colors = colors,
                    visible = state.errorMessage != null,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Spacer(modifier = Modifier.weight(1f))

        AnimatedVisibility(
            visible = revealed,
            enter = fadeIn(tween(220, delayMillis = 140)) +
                expandVertically(tween(280, delayMillis = 140)),
        ) {
            Column {
                TruckerDivider(colors = colors)

                Spacer(modifier = Modifier.height(16.dp))

                TruckerHelplineRow(
                    helpline = TRUCKER_HELPLINE,
                    colors = colors,
                    onClick = actions::onCallHelpline,
                )

                Spacer(modifier = Modifier.height(16.dp))

                TruckerLegalNote(
                    text = "By continuing you agree to the Freight24 Terms of Service and Privacy Policy.",
                    colors = colors,
                )
            }
        }
    }
}
