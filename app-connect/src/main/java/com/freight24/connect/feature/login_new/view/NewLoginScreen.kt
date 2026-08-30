package com.freight24.connect.feature.login_new.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.connect.feature.login_new.viewmodel.NewLoginViewModel
import com.freight24.ui.UiState
import com.freight24.ui.connect.ConnectTruckMark
import com.freight24.ui.theme.Freight24Theme
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf

private val fieldShape = RoundedCornerShape(14.dp)
private val buttonShape = RoundedCornerShape(28.dp)

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
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val fillProgress = remember { Animatable(0f) }
    val snackbarHostState = remember { SnackbarHostState() }

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
                animationSpec = tween(durationMillis = sendOtpButtonAutoClickAfter)
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
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetUiState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(sessionExpiredMessage) {
        sessionExpiredMessage?.let {
            snackbarHostState.showSnackbar(it)
            onSessionExpiredMessageShown()
        }
    }

    // Inherits the activity-level Freight24Theme (Connect identity, correct dark mode).
    Freight24Theme {
        val colorScheme = MaterialTheme.colorScheme
        val isLoading = uiState is UiState.Loading
        val buttonEnabled = isPhoneValid && !isLoading
        val fillFraction = fillProgress.value
        val buttonLabelColor = when {
            isLoading -> colorScheme.onSurfaceVariant
            fillFraction > 0.5f -> colorScheme.onPrimary
            buttonEnabled -> colorScheme.primary
            else -> colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .background(colorScheme.background)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                // Brand mark — geometric truck in a forest squircle.
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = ConnectTruckMark,
                        contentDescription = "Freight24 Logo",
                        tint = colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp),
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = buildAnnotatedString {
                        append("Welcome to FREIGHT")
                        withStyle(
                            style = SpanStyle(
                                color = colorScheme.primary,
                            )
                        ) {
                            append("24")
                        }
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Log in to your dashboard to manage shipments and fleet operations",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 300.dp),
                )

                Spacer(modifier = Modifier.height(44.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = viewModel::onPhoneNumberChange,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag("phone_input")
                            .semantics { testTagsAsResourceId = true },
                    singleLine = true,
                    shape = fieldShape,
                    placeholder = {
                        Text(
                            text = "98765 43210",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        )
                    },
                    prefix = {
                        Text(
                            text = "+91",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done,
                            autoCorrectEnabled = false,
                        ),
                    visualTransformation = IndianPhoneVisualTransformation(),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outlineVariant,
                            focusedContainerColor = colorScheme.surfaceContainerLowest,
                            unfocusedContainerColor = colorScheme.surfaceContainerLowest,
                            disabledContainerColor = colorScheme.surfaceContainerLowest,
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface,
                            cursorColor = colorScheme.primary,
                        ),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Send OTP — outline fills toward auto-send; label flips contrast at midpoint.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(buttonShape)
                        .background(
                            when {
                                buttonEnabled -> Color.Transparent
                                isLoading -> colorScheme.surfaceContainerHighest.copy(alpha = 0.4f)
                                else -> Color.Transparent
                            }
                        )
                        .border(
                            width = if (buttonEnabled || fillFraction > 0f) 1.5.dp else 1.dp,
                            color = when {
                                buttonEnabled || fillFraction > 0f -> colorScheme.primary
                                else -> colorScheme.outlineVariant
                            },
                            shape = buttonShape
                        )
                        .clickable(
                            enabled = buttonEnabled,
                            onClick = { viewModel.onSendOtpClick() }
                        ),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fillFraction)
                            .fillMaxHeight()
                            .background(colorScheme.primary)
                    )

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp,
                                color = colorScheme.primary,
                            )
                        } else {
                            Text(
                                text = "Send OTP",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = buttonLabelColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Footer Links
                Text(
                    text = "Terms of Service & Privacy Policy",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 20.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private class IndianPhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.filter(Char::isDigit).take(10)
        val transformed = buildPhoneDisplay(raw)

        val offsetMapping =
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int =
                    when {
                        offset <= 5 -> offset
                        offset <= 10 -> offset + 1
                        else -> offset + 1
                    }

                override fun transformedToOriginal(offset: Int): Int =
                    when {
                        offset <= 5 -> offset
                        offset <= 11 -> offset - 1
                        else -> offset - 1
                    }
            }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}

private fun buildPhoneDisplay(raw: String): String {
    return if (raw.length <= 5) {
        raw
    } else {
        "${raw.take(5)} ${raw.drop(5)}"
    }
}
