package com.freight24.trucker.feature.login.view.signin

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.freight24.trucker.feature.login.domain.AuthErrorMessage
import com.freight24.trucker.feature.login.view.signin.LoginViewModel.Companion.MAX_PHONE_LENGTH
import com.freight24.trucker.navigation.TruckerDestination
import com.freight24.ui.UiState
import com.freight24.ui.trucker.TRUCKER_HELPLINE_URI

private const val SEND_OTP_AUTO_CLICK_MS = 2000

/**
 * Wiring layer for the login screen.
 *
 * Owns the ViewModel, the auto-send countdown, navigation, the snackbar and
 * the notification permission — and nothing else. It maps ViewModel flows
 * into [LoginUiState] and hands that to [LoginContent].
 *
 * It deliberately contains no layout, colour, or typography: a visual
 * redesign should not require touching this file.
 */
@Composable
internal fun LoginScreen(
    navController: NavHostController,
    sessionExpiredMessage: String? = null,
    onSessionExpiredMessageShown: () -> Unit = {},
) {
    val viewModel: LoginViewModel = hiltViewModel()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val navigateToVerifyOtp by viewModel.navigateToVerifyOtp.collectAsState()

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val fillProgress = remember { Animatable(0f) }

    // Failure copy is held here rather than in the ViewModel: it is
    // presentation state that must survive until the driver edits the
    // number, instead of vanishing with a system Toast.
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isSending = uiState is UiState.Loading
    val isPhoneComplete = phoneNumber.length == MAX_PHONE_LENGTH

    LaunchedEffect(isPhoneComplete) {
        if (isPhoneComplete) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    // Two-second wipe on the CTA once the number is complete, then fire the
    // request. Lets a confident driver keep their hands on the device while
    // leaving an obvious manual fallback.
    var wasPreviouslyComplete = remember { mutableStateOf(isPhoneComplete) }
    LaunchedEffect(isPhoneComplete) {
        if (isPhoneComplete && !wasPreviouslyComplete.value && !isSending) {
            fillProgress.animateTo(1f, tween(durationMillis = SEND_OTP_AUTO_CLICK_MS))
            if (viewModel.uiState.value !is UiState.Loading) {
                viewModel.onSendOtpClick()
            }
        } else if (!isPhoneComplete) {
            fillProgress.snapTo(0f)
        }
        wasPreviouslyComplete.value = isPhoneComplete
    }

    LaunchedEffect(navigateToVerifyOtp) {
        navigateToVerifyOtp?.let { phone ->
            navController.navigate(TruckerDestination.VerifyOtp.createRoute(phone))
            viewModel.onNavigationHandled()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            // Last line of defence: the ViewModel already maps failures to
            // driver-safe copy, so a blank here means something upstream
            // produced nothing. Never render an empty error block.
            errorMessage = (uiState as UiState.Error).message
                .ifBlank { AuthErrorMessage.UNREACHABLE }
            viewModel.resetUiState()
        }
    }

    LaunchedEffect(phoneNumber) {
        if (phoneNumber.isNotEmpty()) errorMessage = null
    }

    LaunchedEffect(sessionExpiredMessage) {
        sessionExpiredMessage?.let {
            android.widget.Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            onSessionExpiredMessageShown()
        }
    }

    // Notification permission — fire-and-forget. We deliberately don't
    // surface a denial here: the auth screen is the wrong place to nag.
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* ignored — the driver can still use the app */ }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val actions = remember(viewModel, context) {
        object : LoginActions {
            override fun onPhoneChange(value: String) = viewModel.onPhoneNumberChange(value)
            override fun onSendCode() = viewModel.onSendOtpClick()
            override fun onCallHelpline() = dialSupport(context)
        }
    }

    LoginContent(
        state = LoginUiState(
            phoneNumber = phoneNumber,
            isSending = isSending,
            errorMessage = errorMessage,
            isPhoneComplete = isPhoneComplete,
            autoSendProgress = fillProgress.value,
        ),
        actions = actions,
    )
}

private fun dialSupport(context: Context) {
    val intent = Intent(Intent.ACTION_DIAL, android.net.Uri.parse(TRUCKER_HELPLINE_URI))
    runCatching { context.startActivity(intent) }
        .onFailure { Toast.makeText(context, "Unable to open dialer", Toast.LENGTH_SHORT).show() }
}
