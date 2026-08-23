package com.freight24.ui.connect

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Freight24 Connect cold-start splash.
 *
 * Calm, brand-true, quiet: forest mark on a deep charcoal-green field,
 * wordmark fading in. Purely visual — shown while session bootstrap runs,
 * replacing the old indeterminate spinner. No navigation implications.
 */
@Composable
fun ConnectSplashScreen(
    logo: ImageVector,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val background = connectSplashBackground(isDark)
    val markTint = if (isDark) ConnectPrimary80 else ConnectBrandForest
    val wordmarkColor = if (isDark) ConnectOnSurfaceDark90 else ConnectPrimary10

    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 450),
        label = "splashAlpha",
    )
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        color = Color.White.copy(alpha = if (isDark) 0.06f else 0.10f),
                        shape = RoundedCornerShape(28.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = logo,
                    contentDescription = null,
                    tint = markTint,
                    modifier = Modifier.size(44.dp),
                )
            }
            Text(
                text = "FREIGHT24",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                ),
                color = wordmarkColor,
                modifier = Modifier.alpha(alpha),
            )
        }
    }
}
