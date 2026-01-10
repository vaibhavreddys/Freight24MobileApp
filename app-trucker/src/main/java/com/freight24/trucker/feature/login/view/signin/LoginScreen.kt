package com.freight24.trucker.feature.login.view.signin

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.freight24.trucker.R
import com.freight24.trucker.feature.login.view.signin.LoginViewModel.Companion.MAX_PHONE_LENGTH
import com.freight24.trucker.navigation.TruckerDestination
import com.freight24.ui.UiState
import com.freight24.ui.theme.AppTypography
import com.freight24.ui.theme.Freight24Theme

@Composable
internal fun LoginScreen(navController: NavHostController) {
    val viewModel: LoginViewModel = hiltViewModel()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // State for manual theme switching (temporary, to be removed later)
    var isDarkTheme by remember { mutableStateOf(false) }
    // TODO: Replace this with isSystemInDarkTheme() eventually.

    val uiState by viewModel.uiState.collectAsState()
    val navigateToVerifyOtp by viewModel.navigateToVerifyOtp.collectAsState()
    LaunchedEffect(navigateToVerifyOtp) {
        navigateToVerifyOtp?.let { phone ->
            navController.navigate(
                TruckerDestination.VerifyOtp.createRoute(phone),
            )
            // Important: Clear the event so it doesn't trigger again on recomposition
            viewModel.onNavigationHandled()
        }
    }

    // Request to send notifications
    val snackbarHostState = remember { SnackbarHostState() }

    var notificationPermissionGranted by remember {
        mutableStateOf(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
    }

    var showPermissionDeniedSnackbar by remember {
        mutableStateOf(false)
    }

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            notificationPermissionGranted = granted
            if (!granted) {
                showPermissionDeniedSnackbar = true
            }
        }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS,
            )
        }
    }
    LaunchedEffect(showPermissionDeniedSnackbar) {
        if (showPermissionDeniedSnackbar) {
            snackbarHostState.showSnackbar(
                message = "Enable notifications to receive alerts",
                duration = SnackbarDuration.Long,
            )
            showPermissionDeniedSnackbar = false // reset
        }
    }

    // Loading overlay
    if (uiState is UiState.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    // Error snackbar
    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            snackbarHostState.showSnackbar((uiState as UiState.Error).message)
            viewModel.resetUiState()
        }
    }

    // 1. Observe the phone number state.
    // If it hits 10 digits, hide keyboard and clear focus.
    LaunchedEffect(phoneNumber) {
        if (phoneNumber.length == 10) {
            keyboardController?.hide()
            focusManager.clearFocus() // Optional: removes the cursor from the text field
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { _ ->
        Freight24Theme(darkTheme = isDarkTheme) {
            val colors = MaterialTheme.colorScheme

            Surface(modifier = Modifier.fillMaxSize(), color = colors.background) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Main scrollable + inset-aware content
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.ime)) // KEY LINE — same as OTP screen
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        // Logo Section
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(bottom = 32.dp),
                        ) {
                            // TODO: Add logo here
//                            Box(
//                                modifier = Modifier
//                                    .size(80.dp)
//                                    .background(
//                                        color = colors.surface,
//                                        shape = RoundedCornerShape(16.dp)
//                                    ),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Icon(
//                                    painter = painterResource(id = R.drawable.file),
//                                    contentDescription = "Logo",
//                                    tint = Color.Unspecified,
//                                    modifier = Modifier.size(48.dp)
//                                )
//                            }
//                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.app_name),
                                color = colors.onBackground,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineMedium,
                                letterSpacing = 1.sp,
                            )
                        }

                        // Headline
                        Text(
                            text = stringResource(R.string.login_page_tagline),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onBackground,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(40.dp))

                        // Phone Input
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = viewModel::onPhoneNumberChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Mobile Number", color = colors.onSurfaceVariant) },
                            leadingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "\uD83C\uDDEE\uD83C\uDDF3 +91",
                                        modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                                        color = colors.onBackground,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Box(
                                        modifier =
                                            Modifier
                                                .width(1.dp)
                                                .height(24.dp)
                                                .background(
                                                    color = colors.surface,
                                                    shape = RoundedCornerShape(16.dp),
                                                ),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                            },
                            keyboardOptions =
                                KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                            singleLine = true,
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = colors.surfaceVariant,
                                    unfocusedContainerColor = colors.surfaceVariant,
                                    focusedBorderColor = colors.primary,
                                    unfocusedBorderColor = colors.outline,
                                    focusedTextColor = colors.onSurface,
                                    unfocusedTextColor = colors.onSurface,
                                ),
                            shape = RoundedCornerShape(8.dp),
                        )

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.onSendOtpClick() },
                            enabled = phoneNumber.length == MAX_PHONE_LENGTH,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = colors.primary,
                                    contentColor = colors.onPrimary,
                                    disabledContainerColor = colors.surfaceDim,
                                    disabledContentColor = colors.onSurfaceVariant,
                                ),
                            shape = RoundedCornerShape(28.dp),
                        ) {
                            Text("Send OTP", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(80.dp)) // Push footer down
                    }

                    // Footer pinned at bottom
                    Text(
                        text = "By continuing, you agree to our Terms of Service and Privacy Policy.",
                        style = AppTypography.labelSmall,
                        textAlign = TextAlign.Center,
                        color = colors.onSurfaceVariant,
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 24.dp)
                                .padding(horizontal = 32.dp),
                    )
                    // Dark mode toggle FAB — now on top and clickable
                    FloatingActionButton(
                        onClick = { isDarkTheme = !isDarkTheme },
                        modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp) // Safe from status bar
                                .statusBarsPadding(),
                        // Extra safe area on notched devices
//                            .navigationBarsPadding(end = true, bottom = true), // Avoid nav bar overlap
                        shape = CircleShape,
                        containerColor = colors.onBackground,
                        contentColor = colors.surfaceBright,
                    ) {
                        Text(
                            text = if (isDarkTheme) "Light" else "Dark",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}
