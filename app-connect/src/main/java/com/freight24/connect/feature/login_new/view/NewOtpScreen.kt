package com.freight24.connect.feature.login_new.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalPhone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.common.util.groupIndianMobileDigits
import com.freight24.connect.feature.login_new.viewmodel.NewOtpViewModel
import com.freight24.ui.UiState
import com.freight24.ui.auth.AuthChipShape
import com.freight24.ui.auth.AuthControlShape
import com.freight24.ui.auth.AuthHeroShape
import com.freight24.ui.auth.AuthLaneMarkings
import com.freight24.ui.auth.AuthLaneProgress
import com.freight24.ui.auth.AuthLedgerBackdrop
import com.freight24.ui.auth.AuthOutlinedRow
import com.freight24.ui.auth.AuthOverline
import com.freight24.ui.auth.AuthPrimaryAction
import com.freight24.ui.auth.AuthRule
import com.freight24.ui.auth.AuthSheet
import com.freight24.ui.auth.rememberAuthColors
import com.freight24.ui.components.InlineErrorBanner
import com.freight24.ui.connect.ConnectTruckMark
import com.freight24.ui.theme.FreightThemeTokens

/** Matches NewOtpViewModel.VERIFY_TIMER_SECONDS — drives the expiry meter. */
private const val VERIFY_WINDOW_SECONDS = 300

@Composable
fun NewOtpScreen(
    phoneNumber: String,
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onWrongNumberClick: () -> Unit,
) {
    val viewModel: NewOtpViewModel = hiltViewModel()
    val otp by viewModel.otp.collectAsState()
    val shake by viewModel.shake.collectAsState()
    val verificationSecondsRemaining by viewModel.verificationSecondsRemaining.collectAsStateWithLifecycle()
    val resendSecondsRemaining by viewModel.resendSecondsRemaining.collectAsStateWithLifecycle()
    val isVerifyEnabled by viewModel.isVerifyEnabled.collectAsStateWithLifecycle()
    val isResendEnabled by viewModel.isResendEnabled.collectAsStateWithLifecycle()
    val isResending by viewModel.isResending.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    // Inline, themed presentation for verify/resend failures — stays until
    // the next keystroke, instead of vanishing with a system Toast.
    var errorMessage by remember { mutableStateOf<String?>(null) }

    BackHandler(onBack = onBackClick)

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UiState.Success -> {
                onLoginSuccess()
                viewModel.markNavigationHandled()
            }
            is UiState.Error -> {
                errorMessage = state.message.ifBlank { "Couldn't verify the code. Please try again." }
                viewModel.resetUiState()
            }
            else -> Unit
        }
    }

    // A new keystroke means a new code — clear the failure presentation.
    LaunchedEffect(otp) {
        errorMessage = null
    }

    // Show keyboard when screen loads
    LaunchedEffect(Unit) {
        keyboardController?.show()
    }

    val isLoading = uiState is UiState.Loading

    // Renders under the activity-level Freight24Theme — Connect identity,
    // user dark-mode override honoured. No nested theme re-wrap.
    val colorScheme = MaterialTheme.colorScheme
    val hero = rememberAuthColors()
    val extendedColors = FreightThemeTokens.colors
    val expiringSoon = verificationSecondsRemaining in 1..30
    val expiryFraction =
        (verificationSecondsRemaining.toFloat() / VERIFY_WINDOW_SECONDS)
            .coerceIn(0f, 1f)
    val expiryColor = if (expiringSoon) extendedColors.warning else colorScheme.primary

    Scaffold(
        modifier =
            Modifier
                .fillMaxSize()
                .imePadding(),
    ) { contentPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .background(hero.paper)
                    .verticalScroll(scrollState)
                    .navigationBarsPadding(),
        ) {
            // -----------------------------------------------------------
            // Masthead — the same brand document as the login screen, now
            // carrying the route indicator so step 2 reads as continuation
            // rather than a second, unrelated screen.
            // -----------------------------------------------------------
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(AuthHeroShape)
                        .background(hero.field)
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp, bottom = 30.dp),
            ) {
                AuthLedgerBackdrop(hero)
                AuthLaneMarkings(hero)

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier =
                                Modifier
                                    .offset(x = (-8).dp)
                                    .size(48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBackIosNew,
                                contentDescription = "Back",
                                tint = hero.ink,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = ConnectTruckMark,
                            contentDescription = null,
                            tint = hero.lane,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        AuthOverline(text = "FREIGHT24 · CONNECT", color = hero.inkMuted)
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AuthLaneProgress(
                            steps = 2,
                            current = 2,
                            colors = hero,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        AuthOverline(text = "STEP 2 OF 2", color = hero.inkMuted)
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Text(
                        text = "Confirm it's you",
                        style = MaterialTheme.typography.headlineSmall,
                        color = hero.ink,
                    )
                }
            }

            // -----------------------------------------------------------
            // Sheet — the verification controls.
            // -----------------------------------------------------------
            AuthSheet(colors = hero) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Verify your number",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "We've sent a 6-digit code to",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Destination block — tap to correct the number. Reads as a
                    // consignment "deliver code to" row, not a generic chip.
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(AuthChipShape)
                                .background(hero.paper)
                                .border(1.dp, hero.paperEdge, AuthChipShape)
                                .clickable(onClick = onWrongNumberClick)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "+91 ${groupIndianMobileDigits(phoneNumber)}",
                            style =
                                MaterialTheme.typography.titleSmall.copy(
                                    fontFeatureSettings = "tnum",
                                ),
                            color = colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Change number",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(15.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    OtpInputField(
                        otp = otp,
                        otpLength = 6,
                        shake = shake,
                        enabled = !isResending && !isLoading,
                        isError = errorMessage != null,
                        onOtpChange = viewModel::onOtpChange,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    InlineErrorBanner(
                        message = errorMessage.orEmpty(),
                        visible = errorMessage != null,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Expiry — a labelled meter, not a floating grey blob.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AuthOverline(
                            text = "CODE EXPIRES IN",
                            color =
                                if (expiringSoon) {
                                    extendedColors.onSurfaceWarning
                                } else {
                                    colorScheme.onSurfaceVariant
                                },
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = verificationSecondsRemaining.toMmSs(),
                            style =
                                MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontFeatureSettings = "tnum",
                                    fontSize = 13.sp,
                                ),
                            color =
                                if (expiringSoon) {
                                    extendedColors.onSurfaceWarning
                                } else {
                                    colorScheme.onSurface
                                },
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(CircleShape)
                                .background(hero.paperEdge),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth(expiryFraction)
                                    .height(3.dp)
                                    .clip(CircleShape)
                                    .background(expiryColor),
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AuthPrimaryAction(
                        label = "Verify & Proceed",
                        enabled = isVerifyEnabled && !isLoading,
                        loading = isLoading,
                        progress = 0f,
                        onClick = viewModel::onVerifyClick,
                        testTag = "verify_btn",
                        colors = hero,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(AuthControlShape)
                                .clickable(enabled = isResendEnabled && !isResending) {
                                    viewModel.onResendClick()
                                }
                                .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (isResending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = null,
                                tint =
                                    if (isResendEnabled) {
                                        colorScheme.primary
                                    } else {
                                        colorScheme.onSurfaceVariant
                                    },
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text =
                                if (isResending) {
                                    "Sending a new code…"
                                } else if (isResendEnabled) {
                                    "Resend OTP"
                                } else {
                                    "Resend OTP in ${resendSecondsRemaining}s"
                                },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color =
                                if (isResendEnabled || isResending) {
                                    colorScheme.primary
                                } else {
                                    colorScheme.onSurfaceVariant
                                },
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    AuthRule(colors = hero)

                    Spacer(modifier = Modifier.height(16.dp))

                    AuthOutlinedRow(onClick = { dialSupport(context) }, colors = hero) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(36.dp)
                                        .background(hero.field.copy(alpha = 0.10f), CircleShape)
                                        .border(1.dp, hero.field.copy(alpha = 0.18f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalPhone,
                                    contentDescription = null,
                                    tint = hero.field,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Call support",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colorScheme.onSurface,
                                )
                                Text(
                                    text = "Instant help, 24×7",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

private fun Int.toMmSs(): String {
    val minutes = this / 60
    val seconds = this % 60
    return String.format("%02d:%02d", minutes, seconds)
}

private fun dialSupport(context: Context) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+911800123456"))
    runCatching { context.startActivity(intent) }
        .onFailure { Toast.makeText(context, "Unable to open dialer", Toast.LENGTH_SHORT).show() }
}
