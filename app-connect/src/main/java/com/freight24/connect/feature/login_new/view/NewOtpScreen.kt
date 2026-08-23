package com.freight24.connect.feature.login_new.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalPhone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.connect.feature.login_new.viewmodel.NewOtpViewModel
import com.freight24.ui.UiState
import com.freight24.ui.theme.Freight24Theme

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

    BackHandler(onBack = onBackClick)

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UiState.Success -> {
                onLoginSuccess()
                viewModel.markNavigationHandled()
            }
            is UiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetUiState()
            }
            else -> Unit
        }
    }

    // Show keyboard when screen loads
    LaunchedEffect(Unit) {
        keyboardController?.show()
    }

    // Inherits the activity-level Freight24Theme (Connect identity, correct dark mode).
    Freight24Theme {
        val colorScheme = MaterialTheme.colorScheme

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .background(colorScheme.background)
                    .statusBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
            ) {
                // Back affordance — same destination as "Wrong number?".
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .offset(x = (-12).dp)
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Verify Phone Number",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Enter the 6-digit code sent to",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "+91 ${formatPhoneDisplay(phoneNumber)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                    )

                    TextButton(onClick = onWrongNumberClick, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Wrong number?",
                                style = MaterialTheme.typography.labelLarge,
                                color = colorScheme.primary,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OtpInputField(
                    otp = otp,
                    otpLength = 6,
                    shake = shake,
                    enabled = !isResending && uiState !is UiState.Loading,
                    onOtpChange = viewModel::onOtpChange
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Neutral countdown chip (was error-red; countdown ≠ failure).
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = null,
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = verificationSecondsRemaining.toMmSs(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = viewModel::onVerifyClick,
                    enabled = isVerifyEnabled && uiState !is UiState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("verify_btn")
                        .semantics { testTagsAsResourceId = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary,
                        disabledContainerColor = colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                        disabledContentColor = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = if (uiState is UiState.Loading) "VERIFYING..." else "VERIFY & PROCEED",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = viewModel::onResendClick,
                    enabled = isResendEnabled && !isResending,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = if (isResendEnabled) colorScheme.primary else colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isResendEnabled) "Resend OTP" else "Resend OTP in ${resendSecondsRemaining}s",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isResendEnabled) colorScheme.primary else colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Footer support block
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(top = 32.dp, bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Having trouble with verification?",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colorScheme.surfaceContainerLow)
                            .clickable { dialSupport(context) }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(colorScheme.primary.copy(alpha = 0.10f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalPhone,
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "CALL SUPPORT",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onSurface,
                                )
                                Text(
                                    text = "Instant help",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatPhoneDisplay(phoneNumber: String): String {
    return if (phoneNumber.length > 5) "${phoneNumber.take(5)} ${phoneNumber.drop(5)}" else phoneNumber
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
