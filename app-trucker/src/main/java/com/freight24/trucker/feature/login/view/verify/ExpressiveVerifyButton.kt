package com.freight24.trucker.feature.login.view.verify

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freight24.ui.theme.AppTypography

@Composable
internal fun ExpressiveVerifyButton(
    state: ButtonState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            ButtonState.Failure -> colors.error
            else -> colors.primary
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "buttonBgColor",
    )

    Button(
        onClick = onClick,
        enabled = state == ButtonState.Idle && enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = when (state) {
                ButtonState.Idle -> colors.surfaceContainerHighest
                else -> backgroundColor
            },
            contentColor = colors.onPrimary,
            disabledContentColor = when (state) {
                ButtonState.Idle -> colors.onSurfaceVariant
                else -> colors.onPrimary
            },
        ),
        shape = RoundedCornerShape(28.dp),
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                fadeIn(animationSpec = spring()) + scaleIn(animationSpec = spring()) togetherWith
                    fadeOut(animationSpec = spring()) + scaleOut(animationSpec = spring())
            },
            label = "buttonContent",
        ) { currentState ->
            when (currentState) {
                ButtonState.Idle -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "Verify",
                            style = AppTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                ButtonState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colors.onPrimary,
                        strokeWidth = 2.dp,
                    )
                }
                ButtonState.Success -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Verified",
                        modifier = Modifier.size(28.dp),
                    )
                }
                ButtonState.Failure -> {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Failed",
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}
