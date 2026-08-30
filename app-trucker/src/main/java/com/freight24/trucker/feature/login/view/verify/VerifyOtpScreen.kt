package com.freight24.trucker.feature.login.view.verify

import OtpInputField
import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.freight24.trucker.R
import com.freight24.ui.theme.AppTypography

@SuppressLint("DefaultLocale")
@Composable
internal fun VerifyOtpScreen(
    navController: NavController,
    phoneNumber: String,
    onLoginSuccess: () -> Unit,
) {
    val viewModel: VerifyOtpViewModel = hiltViewModel()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val isResendEnabled by viewModel.isResendEnabled.collectAsState()
    val isVerifyEnabled by viewModel.isVerifyEnabled.collectAsState()
    val otp by viewModel.otp.collectAsState()
    val shake by viewModel.shake.collectAsState()
    val buttonState by viewModel.buttonState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val attemptsRemaining by viewModel.attemptsRemaining.collectAsState()
    val navigateToDashboard by viewModel.navigateToDashboard.collectAsState()
    val isResending by viewModel.isResending.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        keyboardController?.show()
    }

    LaunchedEffect(navigateToDashboard) {
        if (navigateToDashboard) {
            onLoginSuccess()
            viewModel.onDashboardNavigated()
        }
    }

    val colors = MaterialTheme.colorScheme
    Surface(modifier = Modifier.fillMaxSize(), color = colors.background) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Back button
//                IconButton(
//                    onClick = { navController.popBackStack() },
//                    modifier = Modifier
//                        .align(Alignment.TopStart)
//                        .padding(16.dp),
//                ) {
//                    Icon(
//                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                        contentDescription = "Back",
//                        tint = colors.onSurface,
//                    )
//                }

                // Main scrollable content with windowInsets padding
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.systemBars))
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(60.dp))

                    // App name header
//                    Text(
//                        text = "FREIGHT 24",
//                        style = AppTypography.titleLarge,
//                        fontWeight = FontWeight.Bold,
//                        color = colors.primary,
//                        letterSpacing = 2.sp,
//                    )

//                    Spacer(Modifier.height(16.dp))

                    // Verification title
                    Text(
                        text = "Verifying your number",
                        style = AppTypography.headlineSmall,
                        color = colors.onBackground,
                    )

                    Spacer(Modifier.height(12.dp))

                    // Subtitle
                    Text(
                        text = "Enter the 6-digit code sent to your phone",
                        style = AppTypography.bodyLarge,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(32.dp))

                    // Truck illustration card
                    val isDark = colors.background.luminance() < 0.5f
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        shape = MaterialTheme.shapes.large,
                        color = colors.surfaceContainerLow,
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (isDark) R.drawable.otp_truck_dark else R.drawable.otp_truck_light
                            ),
                            contentDescription = "Truck illustration",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }

                    Spacer(Modifier.height(40.dp))

                    // OTP Input Field
                    OtpInputField(
                        otp = otp,
                        otpLength = 6,
                        shake = shake,
                        onOtpChange = viewModel::onOtpChange,
                    )

                    // Persistent error display
                    errorMessage?.let { message ->
                        Spacer(Modifier.height(16.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = colors.errorContainer,
                            tonalElevation = 0.dp,
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = message,
                                    color = colors.onErrorContainer,
                                    style = AppTypography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                )
                                attemptsRemaining?.let { attempts ->
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "$attempts attempt${if (attempts != 1) "s" else ""} remaining",
                                        color = colors.onErrorContainer.copy(alpha = 0.7f),
                                        style = AppTypography.bodySmall,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    } ?: Spacer(Modifier.height(40.dp))

                    // Verify button
                    ExpressiveVerifyButton(
                        state = buttonState,
                        enabled = isVerifyEnabled,
                        onClick = { viewModel.onVerifyClick(phoneNumber) },
                        modifier = Modifier
                            .testTag("verify_btn")
                            .semantics { testTagsAsResourceId = true },
                    )

                    Spacer(Modifier.height(24.dp))

                    // Didn't receive code text
                    Text(
                        text = "Didn't receive the code?",
                        style = AppTypography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )

                    Spacer(Modifier.height(8.dp))

                    // Resend Code link
                    Text(
                        text = when {
                            isResending -> "Resending..."
                            isResendEnabled -> "Resend Code"
                            else -> "Resend Code in ${String.format(
                                "%02d:%02d",
                                remainingSeconds / 60,
                                remainingSeconds % 60,
                            )}"
                        },
                        style = AppTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isResendEnabled) colors.primary else colors.onSurfaceVariant,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clickable(enabled = isResendEnabled && !isResending) {
                                viewModel.onResendClick(phoneNumber)
                            },
                    )

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
}