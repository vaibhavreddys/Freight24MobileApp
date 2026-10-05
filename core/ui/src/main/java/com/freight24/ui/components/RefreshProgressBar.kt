package com.freight24.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlin.math.sin

@Composable
fun RefreshProgressBar(
    isRefreshing: Boolean,
    pullFraction: Float,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    // FIX: Use rememberUpdatedState to avoid the coroutine closure capturing
    // the initial frozen value of pullFraction.
    val currentPullFraction by rememberUpdatedState(pullFraction)

    val barWidthPx = remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val wavelengthPx = with(density) { 40.dp.toPx() }
    val slowSpeedPxPerSec = 2f * wavelengthPx
    val fastSpeedPxPerSec = 5f * slowSpeedPxPerSec
    val halfWidth = remember { Animatable(0f) }
    var wasRefreshing by remember { mutableStateOf(false) }
    var lastIsRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing && !lastIsRefreshing) {
            wasRefreshing = true
        }
        lastIsRefreshing = isRefreshing
    }

    val phase =
        when {
            isRefreshing -> Phase.Loading
            pullFraction > 0f -> Phase.Pulling
            wasRefreshing -> Phase.Closing
            else -> Phase.Idle
        }

    LaunchedEffect(phase, barWidthPx.value) {
        when (phase) {
            Phase.Pulling -> {
                while (isActive) {
                    val target = currentPullFraction.coerceIn(0f, 1f) * barWidthPx.value / 2f
                    halfWidth.snapTo(target)
                    withFrameNanos { }
                }
            }
            Phase.Loading -> {
                val maxHalfWidth = barWidthPx.value / 2f
                if (maxHalfWidth <= 0f) return@LaunchedEffect

                // 1. Initial shrink to center when loading starts
                if (halfWidth.value > 0f) {
                    val durationMs = ((halfWidth.value / slowSpeedPxPerSec) * 1000f).toInt().coerceAtLeast(1)
                    halfWidth.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = durationMs, easing = LinearEasing),
                    )
                }

                // 2. Indeterminate loop: continuously grow and shrink
                while (isActive) {
                    val growDuration = ((maxHalfWidth / slowSpeedPxPerSec) * 1000f).toInt().coerceAtLeast(1)
                    halfWidth.animateTo(
                        targetValue = maxHalfWidth,
                        animationSpec = tween(durationMillis = growDuration, easing = LinearEasing),
                    )

                    val shrinkDuration = ((maxHalfWidth / slowSpeedPxPerSec) * 1000f).toInt().coerceAtLeast(1)
                    halfWidth.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = shrinkDuration, easing = LinearEasing),
                    )
                }
            }
            Phase.Closing -> {
                val remaining = halfWidth.value
                if (remaining <= 0f) {
                    wasRefreshing = false
                    return@LaunchedEffect
                }
                val durationMs = ((remaining / fastSpeedPxPerSec) * 1000f).toInt().coerceAtLeast(1)
                halfWidth.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = durationMs, easing = LinearEasing),
                )
                wasRefreshing = false
            }
            Phase.Idle -> {
                halfWidth.animateTo(0f, tween(durationMillis = 200))
            }
        }
    }

    val alpha = (halfWidth.value * 2f).coerceIn(0f, 1f)
    val leftX = (barWidthPx.value / 2f - halfWidth.value).coerceAtLeast(0f)
    val rightX = (barWidthPx.value / 2f + halfWidth.value).coerceAtMost(barWidthPx.value)

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clipToBounds()
                .alpha(alpha), // The component handles its own fade-out!
    ) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxSize()
                    .onSizeChanged { barWidthPx.value = it.width.toFloat() },
        ) {
            if (barWidthPx.value <= 0f || rightX <= leftX) return@Canvas
            val midY = size.height / 2f
            val strokeWidthPx = 4.dp.toPx()
            val wavelengthPxLocal = 40.dp.toPx()
            val amplitudePx = 1.dp.toPx()
            val drawStart = leftX.coerceAtLeast(0f)
            val drawEnd = rightX.coerceAtMost(barWidthPx.value)
            if (drawEnd <= drawStart) return@Canvas

            val path =
                Path().apply {
                    moveTo(drawStart, midY + amplitudePx * sin((2.0 * Math.PI * drawStart / wavelengthPxLocal).toFloat()))
                    val step = 1f
                    var x = drawStart + step
                    while (x <= drawEnd) {
                        val theta = (2.0 * Math.PI * (x / wavelengthPxLocal)).toFloat()
                        val y = midY + amplitudePx * sin(theta)
                        lineTo(x, y)
                        x += step
                    }
                }
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
            )
        }
    }
}

private enum class Phase { Idle, Pulling, Loading, Closing }
