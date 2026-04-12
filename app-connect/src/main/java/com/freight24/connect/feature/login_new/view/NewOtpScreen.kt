package com.freight24.connect.feature.login_new.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalPhone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freight24.connect.feature.login_new.viewmodel.NewOtpViewModel
import com.freight24.ui.UiState
import com.freight24.ui.theme.Freight24Theme

// Gradients matching the Login Screen
private val headerGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF4FD1C5), Color(0xFFA7F3D0))
)

private val buttonGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF2FBD6C), Color(0xFF51D37C))
)

@Composable
fun NewOtpScreen(
    phoneNumber: String,
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onWrongNumberClick: () -> Unit,
) {
    val viewModel: NewOtpViewModel = hiltViewModel()
    val otp by viewModel.otp.collectAsState()
    val shake by viewModel.shake.collectAsState()
    val verificationSecondsRemaining by viewModel.verificationSecondsRemaining.collectAsStateWithLifecycle()
    val resendSecondsRemaining by viewModel.resendSecondsRemaining.collectAsStateWithLifecycle()
    val isVerifyEnabled by viewModel.isVerifyEnabled.collectAsStateWithLifecycle()
    val isResendEnabled by viewModel.isResendEnabled.collectAsStateWithLifecycle()
    val isResending by viewModel.isResending.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    BackHandler(onBack = onBackClick)

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UiState.Success -> {
                onLoginSuccess()
                viewModel.markNavigationHandled()
            }
            is UiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetUiState()
            }
            else -> Unit
        }
    }

    // Show keyboard when screen loads
    LaunchedEffect(Unit) {
        keyboardController?.show()
    }

    Freight24Theme {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .imePadding() // Add IME padding for keyboard
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .background(Color(0xFFF3F4F6)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState) // Scroll the entire screen
                ) {
                    // 1. Header Section - Plain Gradient (NO TRUCK ICON)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp) // Reduced height
                            .background(headerGradient)
                    )

                    // 2. Card Section (Overlaps Header)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-40).dp) // Pull up to overlap
                            .padding(horizontal = 24.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(32.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Title
                                Text(
                                    text = "Verify Phone Number",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Phone Number
                                Text(
                                    text = "Enter the 6-digit code sent to",
                                    fontSize = 14.sp,
                                    color = Color(0xFF6B7280)
                                )
                                Text(
                                    text = "+91 ${formatPhoneDisplay(phoneNumber)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF111827)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Wrong Number Link
                                TextButton(
                                    onClick = onWrongNumberClick,
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = null,
                                            tint = Color(0xFF0F7A42),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Wrong number?",
                                            color = Color(0xFF0F7A42),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // OTP Input Field
                                OtpInputField(
                                    otp = otp,
                                    otpLength = 6,
                                    shake = shake,
                                    enabled = !isResending && uiState !is UiState.Loading,
                                    onOtpChange = viewModel::onOtpChange
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Timer Badge
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFEF2F2), RoundedCornerShape(18.dp))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Timer,
                                            contentDescription = null,
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = verificationSecondsRemaining.toMmSs(),
                                            color = Color(0xFFDC2626),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Verify Button
                                Button(
                                    onClick = viewModel::onVerifyClick,
                                    enabled = isVerifyEnabled && uiState !is UiState.Loading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .background(buttonGradient, RoundedCornerShape(28.dp)),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = Color.White,
                                        disabledContainerColor = Color.Transparent,
                                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                                    ),
                                    shape = RoundedCornerShape(28.dp),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 4.dp,
                                        pressedElevation = 8.dp
                                    )
                                ) {
                                    Text(
                                        text = if (uiState is UiState.Loading) "VERIFYING..." else "VERIFY & PROCEED",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Resend OTP
                                TextButton(
                                    onClick = viewModel::onResendClick,
                                    enabled = isResendEnabled && !isResending
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Refresh,
                                            contentDescription = null,
                                            tint = if (isResendEnabled) Color(0xFF0F7A42) else Color(0xFF9CA3AF),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isResendEnabled) "Resend OTP" else "Resend OTP in ${resendSecondsRemaining}s",
                                            color = if (isResendEnabled) Color(0xFF0F7A42) else Color(0xFF9CA3AF),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(200.dp)) // Add space for scrolling

                    // 3. Footer Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Having trouble with verification?",
                            color = Color(0xFF4B5563),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFFFFF), RoundedCornerShape(16.dp))
                                .clickable { dialSupport(context) }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFECFDF5), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LocalPhone,
                                        contentDescription = null,
                                        tint = Color(0xFF0F7A42),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "CALL SUPPORT",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F2937),
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Instant help",
                                        color = Color(0xFF6B7280),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun extractOtp(message: String): String? {
    return Regex("\\b\\d{6}\\b").find(message)?.value
}

private fun formatPhoneDisplay(phoneNumber: String): String {
    return if (phoneNumber.length > 5) "${phoneNumber.take(5)} ${phoneNumber.drop(5)}" else phoneNumber
}

private fun Int.toMmSs(): String {
    val minutes = this / 60
    val seconds = this % 60
    return String.format("%02d:%02d", minutes, seconds)
}

private fun dialSupport(context: Context) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+911800123456"))
    runCatching { context.startActivity(intent) }
        .onFailure { Toast.makeText(context, "Unable to open dialer", Toast.LENGTH_SHORT).show() }
}