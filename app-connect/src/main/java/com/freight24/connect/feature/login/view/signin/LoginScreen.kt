package com.freight24.connect.feature.login.view.signin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.ui.UiState
import com.freight24.ui.theme.Freight24Theme

@Composable
fun LoginScreen(
    sessionExpiredMessage: String? = null,
    onSessionExpiredMessageShown: () -> Unit = {},
    onVerify: (String) -> Unit,
) {
    val viewModel: LoginViewModel = hiltViewModel()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            onVerify(phoneNumber)
        }
    }

    LaunchedEffect(sessionExpiredMessage) {
        sessionExpiredMessage?.let {
            snackbarHostState.showSnackbar(it)
            onSessionExpiredMessageShown()
        }
    }

    Freight24Theme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { contentPadding ->
            Box(modifier = Modifier.padding(contentPadding).fillMaxSize()) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    // Logo Replacement
                    Text(
                        text = "Freight24",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        modifier = Modifier.padding(bottom = 80.dp),
                    )

                    Text(
                        text = "Let's get moving",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier =
                            Modifier
                                .padding(bottom = 24.dp)
                                .align(Alignment.Start),
                    )

                    // Mobile Number Input
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = viewModel::onPhoneNumberChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Mobile Number") },
                        leadingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "+91",
                                    modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Send OTP Button
                    Button(
                        onClick = { viewModel.onSendOtpClick() },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    ) {
                        Text("Send OTP", color = colorScheme.onPrimary, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Terms of Service
                    Text(
                        text = "By continuing, you agree to our Terms of Service and Privacy Policy.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 32.dp),
                    )
                }
                if (uiState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
