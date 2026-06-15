package com.freight24.trucker.feature.login.view.signin

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
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
import com.freight24.trucker.ui.components.rememberCallAction
import com.freight24.ui.UiState
import com.freight24.ui.theme.Freight24Theme

@Composable
internal fun LoginScreen(
    navController: NavHostController,
    sessionExpiredMessage: String? = null,
    onSessionExpiredMessageShown: () -> Unit = {},
) {
    val viewModel: LoginViewModel = hiltViewModel()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val uiState by viewModel.uiState.collectAsState()
    val navigateToVerifyOtp by viewModel.navigateToVerifyOtp.collectAsState()
    val makeCall = rememberCallAction(stringResource(R.string.supportPhoneNumber))

    LaunchedEffect(navigateToVerifyOtp) {
        navigateToVerifyOtp?.let { phone ->
            navController.navigate(
                TruckerDestination.VerifyOtp.createRoute(phone),
            )
            viewModel.onNavigationHandled()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(sessionExpiredMessage) {
        sessionExpiredMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Long)
            onSessionExpiredMessageShown()
        }
    }

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
            showPermissionDeniedSnackbar = false
        }
    }

    if (uiState is UiState.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            snackbarHostState.showSnackbar((uiState as UiState.Error).message)
            viewModel.resetUiState()
        }
    }

    LaunchedEffect(phoneNumber) {
        if (phoneNumber.length == 10) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Freight24Theme {
            val colors = MaterialTheme.colorScheme

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = colors.surface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.ime))
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // App Logo Section
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 24.dp, vertical = 16.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_logo_freight24),
//                            contentDescription = "Freight 24 Logo",
//                            tint = colors.primary,
//                            modifier = Modifier.size(32.dp),
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = stringResource(R.string.app_name),
//                            style = MaterialTheme.typography.headlineMedium,
//                            color = colors.onSurface,
//                        )
//                    }

                    // Hero Illustration
                    Image(
                        painter = painterResource(id = R.drawable.login_hero_truck),
                        contentDescription = "Freight24 Truck Illustration",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(240.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop,
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Welcome Text Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.login_welcome_back),
                            style = MaterialTheme.typography.headlineLarge,
                            color = colors.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.login_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Phone Number Input Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.login_phone_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = viewModel::onPhoneNumberChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    stringResource(R.string.login_phone_placeholder),
                                    color = colors.onSurfaceVariant.copy(alpha = 0.5f),
                                )
                            },
                            leadingIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 12.dp),
                                ) {
                                    Text(
                                        text = "\uD83C\uDDEE\uD83C\uDDF3 +91",
                                        color = colors.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(24.dp)
                                            .background(color = colors.outlineVariant),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.surfaceContainerLow,
                                unfocusedContainerColor = colors.surfaceContainerLow,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.outlineVariant,
                                focusedTextColor = colors.onSurface,
                                unfocusedTextColor = colors.onSurface,
                            ),
                            shape = RoundedCornerShape(12.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Send OTP Button
                    Button(
                        onClick = { viewModel.onSendOtpClick() },
                        enabled = phoneNumber.length == MAX_PHONE_LENGTH,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary,
                            disabledContainerColor = colors.surfaceContainerHighest,
                            disabledContentColor = colors.onSurfaceVariant,
                        ),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.login_send_otp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Send OTP arrow",
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Spacer(modifier = Modifier.weight(1f))


                    // Secure Portal Divider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 48.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = colors.outlineVariant,
                            thickness = 1.dp,
                        )
                        Text(
                            text = stringResource(R.string.login_secure_portal),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp),
                            letterSpacing = 1.sp,
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = colors.outlineVariant,
                            thickness = 1.dp,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
//                    Spacer(modifier = Modifier.weight(1f))

                    // Terms and Privacy
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = { }) {
                            Text(
                                text = stringResource(R.string.login_terms_of_service),
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = "•",
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                        TextButton(onClick = { }) {
                            Text(
                                text = stringResource(R.string.login_privacy_policy),
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }

//                    Spacer(modifier = Modifier.weight(1f))

                    // Footer
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.login_copyright),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(onClick = { makeCall() }) {
                            Text(
                                text = stringResource(R.string.login_contact_support),
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}
