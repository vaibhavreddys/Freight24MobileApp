package com.freight24.trucker.feature.login.view.verify

import OtpInputField
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.freight24.ui.UiState
import com.freight24.ui.theme.AppTypography
import com.freight24.ui.theme.Freight24Theme

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
    val uiState by viewModel.uiState.collectAsState()
    val navigateToDashboard by viewModel.navigateToDashboard.collectAsState()

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

    // Loading
    if (uiState is UiState.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    // Error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            snackbarHostState.showSnackbar((uiState as UiState.Error).message)
            viewModel.resetUiState()
        }
    }

    Freight24Theme {
        val colors = MaterialTheme.colorScheme
        Surface(modifier = Modifier.fillMaxSize(), color = colors.background) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Back button
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
                ) {
                    Text("←", fontSize = 32.sp, color = colors.surface)
                }

                // Main scrollable content with windowInsets padding
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.systemBars))
                            .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center, // Perfect centering
                ) {
                    Text(
                        "Verify your number",
                        style = AppTypography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onBackground,
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "We sent a 6-digit code to $phoneNumber",
                        style = AppTypography.bodyLarge,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(48.dp))

                    OtpInputField(
                        otp = otp,
                        otpLength = 6,
                        shake = shake,
                        onOtpChange = viewModel::onOtpChange,
                    )

                    Spacer(Modifier.height(40.dp))

                    Text(
                        text =
                            if (isResendEnabled) {
                                "Resend code"
                            } else {
                                "Resend code in ${String.format(
                                    "%02d:%02d",
                                    remainingSeconds / 60,
                                    remainingSeconds % 60,
                                )}"
                            },
                        color = colors.onSurfaceVariant,
                        style = AppTypography.bodyMedium,
                        modifier =
                            Modifier.clickable(enabled = isResendEnabled) {
                                viewModel.onResendClick()
                            },
                    )

                    Spacer(Modifier.height(80.dp)) // Push button down naturally

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "I didn't receive a code",
                            color = colors.onSurfaceVariant,
                            style = AppTypography.bodyMedium,
                            textDecoration = TextDecoration.Underline,
                            modifier =
                                Modifier
                                    .padding(bottom = 16.dp)
                                    .clickable { /* TODO */ },
                        )

                        Button(
                            onClick = { viewModel.onVerifyClick(phoneNumber) },
                            enabled = isVerifyEnabled,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = colors.primary,
                                    contentColor = colors.onPrimary,
                                    disabledContainerColor = colors.surfaceDim,
                                    disabledContentColor = colors.onSurfaceVariant,
                                ),
                            shape = RoundedCornerShape(28.dp),
                        ) {
                            Text("Verify", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(40.dp))
                    }
                }

                // Snackbar
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                ) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = Color.Black,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }
        }
    }
}
