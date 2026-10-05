package com.freight24.connect.feature.login_new.view

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.ui.auth.AuthFieldShape
import com.freight24.ui.auth.rememberAuthColors

/**
 * Six-cell code entry.
 *
 * Cells carry state through material, not decoration: an empty cell is a
 * blank well, the cell awaiting input gets a brand caret, and a filled cell
 * lights to the brand container so the code visibly builds as it is typed.
 * Errors are carried by the border alone.
 */
@Composable
fun OtpInputField(
    otp: String,
    otpLength: Int,
    shake: Boolean,
    enabled: Boolean,
    isError: Boolean = false,
    onOtpChange: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val hero = rememberAuthColors()
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

    // Hidden TextField for input capture, overlaid on the visual cells so the
    // strip is exactly one cell tall — no phantom gap above the boxes.
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("otp_input")
                .semantics { testTagsAsResourceId = true }
                .offset(x = shakeOffset.value.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    if (enabled) {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
                },
        contentAlignment = Alignment.Center,
    ) {
        TextField(
            value = otp,
            onValueChange = { value ->
                if (enabled) {
                    val digitsOnly = value.filter(Char::isDigit).take(otpLength)
                    onOtpChange(digitsOnly)
                }
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .alpha(0.01f)
                    .focusRequester(focusRequester),
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = colors.surface.copy(alpha = 0f),
                    unfocusedContainerColor = colors.surface.copy(alpha = 0f),
                    focusedIndicatorColor = colors.surface.copy(alpha = 0f),
                    unfocusedIndicatorColor = colors.surface.copy(alpha = 0f),
                    disabledContainerColor = colors.surface.copy(alpha = 0f),
                    cursorColor = colors.primary,
                ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            repeat(otpLength) { index ->
                val char = otp.getOrNull(index)?.toString().orEmpty()
                val isActive = otp.length == index && otp.length < otpLength
                val isFilled = char.isNotEmpty()

                Box(
                    modifier =
                        Modifier
                            .size(width = 46.dp, height = 56.dp)
                            .clip(AuthFieldShape)
                            .background(
                                if (isFilled) colors.primaryContainer else hero.paper,
                            )
                            .border(
                                width =
                                    when {
                                        isActive -> 1.5.dp
                                        isFilled && isError -> 1.5.dp
                                        else -> 1.dp
                                    },
                                color =
                                    when {
                                        isActive -> colors.primary
                                        isFilled && isError -> colors.error
                                        isFilled -> colors.primaryContainer
                                        else -> colors.outlineVariant
                                    },
                                shape = AuthFieldShape,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        isFilled -> {
                            Text(
                                text = char,
                                style =
                                    MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 22.sp,
                                        fontFeatureSettings = "tnum",
                                    ),
                                color =
                                    if (isError) {
                                        colors.onErrorContainer
                                    } else {
                                        colors.onPrimaryContainer
                                    },
                            )
                        }
                        isActive -> {
                            // Caret, not a dot — the cell reads as a live
                            // input rather than a status indicator.
                            Box(
                                modifier =
                                    Modifier
                                        .size(width = 2.dp, height = 22.dp)
                                        .background(colors.primary),
                            )
                        }
                    }
                }
            }
        }
    }
}
