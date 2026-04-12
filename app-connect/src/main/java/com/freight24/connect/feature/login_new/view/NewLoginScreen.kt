package com.freight24.connect.feature.login_new.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.connect.feature.login_new.viewmodel.NewLoginViewModel
import com.freight24.ui.UiState
import com.freight24.ui.theme.Freight24Theme
import android.widget.Toast
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.google.common.io.Files.append

private val loginGradient =
    Brush.verticalGradient(
        colors = listOf(Color(0xFF4FD1C5), Color(0xFFA7F3D0)),
    )

private val cardShape = RoundedCornerShape(32.dp)

@Composable
fun NewLoginScreen(onOtpRequested: (String) -> Unit) {
    val viewModel: NewLoginViewModel = hiltViewModel()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val isPhoneValid by viewModel.isPhoneValid.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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

    Freight24Theme {
        Scaffold(modifier = Modifier.fillMaxSize()) { contentPadding ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                        .background(Color(0xFFF7FAF9)),
            ) {
                // Gradient Header Background
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .background(loginGradient),
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Header Content
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp, start = 24.dp, end = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Logo Container
                        Box(
                            modifier =
                                Modifier
                                    .size(92.dp)
                                    .background(Color(0xFF1D4E89), RoundedCornerShape(28.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalShipping,
                                contentDescription = "Freight24 Logo",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp),
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = buildAnnotatedString {
                                append("Welcome to FREIGHT")
                                withStyle(
                                    style = SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 36.sp
                                    )
                                ) {
                                    append("24")
                                }
                            },
                            fontSize = 28.sp,  // Base size for "Welcome to FREIGHT"
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Log in to your dashboard to manage shipments and fleet operations",
                            fontSize = 16.sp,
                            color = Color(0xFF4B5563),
                            textAlign = TextAlign.Center,
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Login Card
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            // Phone Number Input
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = viewModel::onPhoneNumberChange,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        // FIX #1: Disable autofill highlight by using transparent background
                                        .background(Color.Transparent),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                placeholder = {
                                    Text(
                                        text = "98765 43210",
                                        color = Color(0xFF9CA3AF),
                                    )
                                },
                                leadingIcon = {
                                    Text(
                                        text = "🇮🇳",
                                        fontSize = 20.sp,
                                    )
                                },
                                prefix = {
                                    Text(
                                        text = "+91",
                                        color = Color(0xFF111827), // FIX #2: Dark color for visibility
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(end = 4.dp)
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
                                        focusedBorderColor = Color(0xFF27AE60),
                                        unfocusedBorderColor = Color(0xFFD1D5DB),
                                        focusedPrefixColor = Color(0xFF111827), // FIX #2
                                        unfocusedPrefixColor = Color(0xFF111827), // FIX #2
                                        focusedTextColor = Color(0xFF111827), // FIX #2: Dark text
                                        unfocusedTextColor = Color(0xFF111827), // FIX #2: Dark text
                                        focusedPlaceholderColor = Color(0xFF9CA3AF),
                                        unfocusedPlaceholderColor = Color(0xFF9CA3AF),
                                        // FIX #1: Disable autofill yellow background
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                    ),
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Send OTP Button
                            Button(
                                onClick = viewModel::onSendOtpClick,
                                enabled = isPhoneValid && uiState !is UiState.Loading,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor =
                                            if (isPhoneValid && uiState !is UiState.Loading) {
                                                Color(0xFF27AE60)
                                            } else {
                                                Color(0xFF9CA3AF)
                                            },
                                        contentColor = Color.White,
                                        disabledContainerColor = Color(0xFFD1D5DB),
                                        disabledContentColor = Color.White,
                                    ),
                                shape = RoundedCornerShape(28.dp),
                                elevation =
                                    ButtonDefaults.buttonElevation(
                                        defaultElevation = 4.dp,
                                        pressedElevation = 8.dp,
                                    ),
                            ) {
                                if (uiState is UiState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color.White,
                                        strokeWidth = 2.5.dp,
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Sending...",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                } else {
                                    Text(
                                        text = "Send OTP",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Footer Links
                    Text(
                        text = "Terms of Service & Privacy Policy",
                        fontSize = 14.sp,
                        color = Color(0xFF9CA3AF),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 32.dp),
                        textAlign = TextAlign.Center,
                    )
                }
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