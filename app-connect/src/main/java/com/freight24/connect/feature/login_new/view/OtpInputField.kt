package com.freight24.connect.feature.login_new.view

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.ui.theme.FreightThemeTokens

@Composable
fun OtpInputField(
    otp: String,
    otpLength: Int,
    shake: Boolean,
    enabled: Boolean,
    onOtpChange: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val extendedColors = FreightThemeTokens.colors
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    LaunchedEffect(otp) {
        if (otp.length == otpLength) {
            keyboardController?.hide()
        }
    }

    LaunchedEffect(shake) {
        if (shake) {
            shakeOffset.snapTo(-12f)
            shakeOffset.animateTo(12f)
            shakeOffset.animateTo(-8f)
            shakeOffset.animateTo(8f)
            shakeOffset.animateTo(0f)
        }
    }

    // Hidden TextField for input capture - positioned at the OTP circles location
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = shakeOffset.value.dp)
            .alpha(0.01f) // Make it nearly invisible but still functional
    ) {
        TextField(
            value = otp,
            onValueChange = { value ->
                if (enabled) {
                    val digitsOnly = value.filter(Char::isDigit).take(otpLength)
                    onOtpChange(digitsOnly)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.surface.copy(alpha = 0f),
                unfocusedContainerColor = colors.surface.copy(alpha = 0f),
                focusedIndicatorColor = colors.surface.copy(alpha = 0f),
                unfocusedIndicatorColor = colors.surface.copy(alpha = 0f),
                disabledContainerColor = colors.surface.copy(alpha = 0f),
                cursorColor = colors.primary,
            ),
        )
    }

    // Visual OTP circles - clickable to focus the hidden TextField
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = shakeOffset.value.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) {
                if (enabled) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(otpLength) { index ->
                val char = otp.getOrNull(index)?.toString().orEmpty()
                val isActive = otp.length == index && otp.length < otpLength
                val isFilled = char.isNotEmpty()

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            when {
                                isFilled -> colors.surface
                                isActive -> extendedColors.successColor.copy(alpha = 0.12f)
                                else -> colors.surfaceVariant
                            },
                            CircleShape
                        )
                        .border(
                            width = if (isActive) 2.dp else if (isFilled) 1.dp else 0.dp,
                            color = when {
                                isActive -> extendedColors.successColor
                                isFilled -> colors.outlineVariant
                                else -> colors.surface.copy(alpha = 0f)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFilled) {
                        Text(
                            text = char,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(if (isActive) 8.dp else 6.dp)
                                .background(
                                    if (isActive) extendedColors.successColor else colors.onSurfaceVariant,
                                    CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}
