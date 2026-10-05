package com.freight24.connect.feature.login_new.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.connect.feature.login_new.viewmodel.NewLoginViewModel
import com.freight24.ui.UiState
import com.freight24.ui.auth.AuthFieldShape
import com.freight24.ui.auth.AuthHeroShape
import com.freight24.ui.auth.AuthLaneMarkings
import com.freight24.ui.auth.AuthLedgerBackdrop
import com.freight24.ui.auth.AuthLegalNote
import com.freight24.ui.auth.AuthOverline
import com.freight24.ui.auth.AuthPrimaryAction
import com.freight24.ui.auth.AuthRule
import com.freight24.ui.auth.AuthSheet
import com.freight24.ui.auth.rememberAuthColors
import com.freight24.ui.components.InlineErrorBanner
import com.freight24.ui.connect.ConnectTruckMark
import com.freight24.ui.trucker.IndianPhoneVisualTransformation

private const val sendOtpButtonAutoClickAfter = 2000

@Composable
fun NewLoginScreen(
    sessionExpiredMessage: String? = null,
    onSessionExpiredMessageShown: () -> Unit = {},
    onOtpRequested: (String) -> Unit,
) {
    val viewModel: NewLoginViewModel = hiltViewModel()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val isPhoneValid by viewModel.isPhoneValid.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val fillProgress = remember { Animatable(0f) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Inline, themed presentation for send-OTP failures — stays until the
    // user edits the number, instead of vanishing with a system Toast.
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isPhoneValid) {
        if (isPhoneValid) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    // Initialize to current state to prevent triggering on screen recreation/back navigation
    var wasPreviouslyValid = remember { mutableStateOf(isPhoneValid) }

    LaunchedEffect(isPhoneValid) {
        // Only trigger on the transition from invalid -> valid
        if (isPhoneValid && !wasPreviouslyValid.value && uiState !is UiState.Loading) {
            fillProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = sendOtpButtonAutoClickAfter),
            )
            // Guard against race conditions if user manually clicks during animation
            if (uiState !is UiState.Loading) {
                viewModel.onSendOtpClick()
            }
        } else if (!isPhoneValid) {
            fillProgress.snapTo(0f)
        }
        // Update state to track the edge transition
        wasPreviouslyValid.value = isPhoneValid
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UiState.Success -> {
                onOtpRequested(state.data)
                viewModel.resetUiState()
            }
            is UiState.Error -> {
                errorMessage = state.message.ifBlank { "Couldn't send OTP. Please try again." }
                viewModel.resetUiState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(phoneNumber) {
        if (phoneNumber.isNotEmpty()) errorMessage = null
    }

    LaunchedEffect(sessionExpiredMessage) {
        sessionExpiredMessage?.let {
            snackbarHostState.showSnackbar(it)
            onSessionExpiredMessageShown()
        }
    }

    // Renders under the activity-level Freight24Theme — Connect identity,
    // user dark-mode override honoured. No nested theme re-wrap.
    val colorScheme = MaterialTheme.colorScheme
    val hero = rememberAuthColors()
    val isLoading = uiState is UiState.Loading
    val buttonEnabled = isPhoneValid && !isLoading
    val fillFraction = fillProgress.value

    Scaffold(
        modifier =
            Modifier
                .fillMaxSize()
                .imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .background(hero.paper)
                    .navigationBarsPadding(),
        ) {
            // ---------------------------------------------------------------
            // Masthead — brand-inked block, ledger-ruled, lane-marked.
            // ---------------------------------------------------------------
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(AuthHeroShape)
                        .background(hero.field)
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp)
                        .padding(top = 28.dp, bottom = 44.dp),
            ) {
                AuthLedgerBackdrop(hero)
                AuthLaneMarkings(hero)

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = ConnectTruckMark,
                            contentDescription = null,
                            tint = hero.lane,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        AuthOverline(text = "FREIGHT24 · CONNECT", color = hero.inkMuted)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Bronze seam rule — the crease of the document.
                    Box(
                        modifier =
                            Modifier
                                .size(width = 44.dp, height = 2.dp)
                                .background(hero.lane),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Sign in to\nFreight24 Connect",
                        style = MaterialTheme.typography.headlineLarge,
                        color = hero.ink,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Trips, fleet and settlements — the whole day's freight in one console.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = hero.inkMuted,
                        modifier = Modifier.widthIn(max = 292.dp),
                        lineHeight = 20.sp,
                    )
                }
            }

            // ---------------------------------------------------------------
            // Sheet — the one control that matters, then the legal footer.
            // ---------------------------------------------------------------
            AuthSheet(colors = hero) {
                Column {
                    AuthOverline(
                        text = "MOBILE NUMBER",
                        color = colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = viewModel::onPhoneNumberChange,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .testTag("phone_input")
                                .semantics { testTagsAsResourceId = true },
                        singleLine = true,
                        shape = AuthFieldShape,
                        textStyle =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp,
                                letterSpacing = 0.6.sp,
                                fontFeatureSettings = "tnum",
                            ),
                        placeholder = {
                            Text(
                                text = "98765 43210",
                                style =
                                    MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 17.sp,
                                        letterSpacing = 0.6.sp,
                                    ),
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                            )
                        },
                        prefix = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "+91",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colorScheme.onSurface,
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                // Column rule — the field reads like a form
                                // cell, not a rounded slab.
                                Box(
                                    modifier =
                                        Modifier
                                            .size(width = 1.dp, height = 20.dp)
                                            .background(colorScheme.outlineVariant),
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                        },
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done,
                                autoCorrectEnabled = false,
                            ),
                        visualTransformation = IndianPhoneVisualTransformation,
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = hero.paperEdge,
                                disabledBorderColor = hero.paperEdge,
                                // The field sits inside a sheet of warm bone paper;
                                // using scheme's `surfaceContainerLowest` here would
                                // paint a stark #FFFFFF box through the paper.
                                focusedContainerColor = hero.paper,
                                unfocusedContainerColor = hero.paper,
                                disabledContainerColor = hero.paper,
                                focusedTextColor = colorScheme.onSurface,
                                unfocusedTextColor = colorScheme.onSurface,
                                cursorColor = colorScheme.primary,
                            ),
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    AuthPrimaryAction(
                        label = "Send OTP",
                        enabled = buttonEnabled,
                        loading = isLoading,
                        progress = fillFraction,
                        onClick = viewModel::onSendOtpClick,
                        testTag = "send_otp_btn",
                        colors = hero,
                        trailing = {
                            Text(
                                text = "→",
                                style = MaterialTheme.typography.titleSmall,
                                color =
                                    if (buttonEnabled) {
                                        colorScheme.onPrimary
                                    } else {
                                        colorScheme.onPrimaryContainer
                                    },
                            )
                        },
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InlineErrorBanner(
                        message = errorMessage.orEmpty(),
                        visible = errorMessage != null,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AuthRule(colors = hero)

                    Spacer(modifier = Modifier.height(16.dp))

                    AuthLegalNote(
                        text = "By continuing you agree to the Freight24 Terms of Service and Privacy Policy.",
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
