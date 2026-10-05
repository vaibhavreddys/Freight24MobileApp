package com.freight24.trucker.feature.login.view.verify

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.freight24.ui.trucker.TRUCKER_HELPLINE_URI

/**
 * Wiring layer for the OTP screen.
 *
 * Owns the ViewModel, navigation and the dialer intent — and nothing else.
 * It maps ViewModel flows into [VerifyOtpUiState] and hands that to
 * [VerifyOtpContent].
 *
 * It deliberately contains no layout, colour, or typography: a visual
 * redesign should not require touching this file.
 */
@Composable
internal fun VerifyOtpScreen(
    navController: NavController,
    phoneNumber: String,
    onLoginSuccess: () -> Unit,
    onBackClick: () -> Unit = { navController.popBackStack() },
    onWrongNumberClick: () -> Unit = { navController.popBackStack() },
) {
    val viewModel: VerifyOtpViewModel = hiltViewModel()
    val otp by viewModel.otp.collectAsState()
    val shake by viewModel.shake.collectAsState()
    val expirySeconds by viewModel.verificationSecondsRemaining.collectAsState()
    val resendSeconds by viewModel.remainingSeconds.collectAsState()
    val canVerify by viewModel.isVerifyEnabled.collectAsState()
    val isResendEnabled by viewModel.isResendEnabled.collectAsState()
    val isResending by viewModel.isResending.collectAsState()
    val buttonState by viewModel.buttonState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val attemptsRemaining by viewModel.attemptsRemaining.collectAsState()
    val navigateToDashboard by viewModel.navigateToDashboard.collectAsState()

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(navigateToDashboard) {
        if (navigateToDashboard) {
            keyboardController?.hide()
            onLoginSuccess()
            viewModel.onDashboardNavigated()
        }
    }

    LaunchedEffect(Unit) {
        keyboardController?.show()
    }

    val actions =
        remember(viewModel, context, onBackClick, onWrongNumberClick) {
            object : VerifyOtpActions {
                override fun onOtpChange(value: String) = viewModel.onOtpChange(value)

                override fun onVerify() = viewModel.onVerifyClick(phoneNumber)

                override fun onResend() = viewModel.onResendClick(phoneNumber)

                override fun onChangeNumber() = onWrongNumberClick()

                override fun onBack() = onBackClick()

                override fun onCallHelpline() = dialSupport(context)
            }
        }

    VerifyOtpContent(
        state =
            VerifyOtpUiState(
                phoneNumber = phoneNumber,
                otp = otp,
                otpLength = 6,
                isVerifying = buttonState == ButtonState.Loading,
                isResending = isResending,
                errorMessage = errorMessage,
                attemptsRemaining = attemptsRemaining,
                resendSeconds = if (isResendEnabled) 0 else resendSeconds,
                expirySeconds = expirySeconds,
                shake = shake,
                // The ViewModel's verify flag only tracks length; the contract
                // also needs a live, non-expired code and nothing in flight.
                canVerify = canVerify && expirySeconds > 0 && buttonState != ButtonState.Loading,
            ),
        actions = actions,
    )
}

private fun dialSupport(context: Context) {
    val intent = Intent(Intent.ACTION_DIAL, android.net.Uri.parse(TRUCKER_HELPLINE_URI))
    runCatching { context.startActivity(intent) }
        .onFailure { Toast.makeText(context, "Unable to open dialer", Toast.LENGTH_SHORT).show() }
}
