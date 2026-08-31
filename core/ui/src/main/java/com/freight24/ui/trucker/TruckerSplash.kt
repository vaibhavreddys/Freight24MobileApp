package com.freight24.ui.trucker

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Freight24 Trucker cold-start splash — "the monogram unfurls".
 *
 * Same one-shot, no-loop choreography as the Connect splash:
 *  1. the "F24" monogram fades in and settles,
 *  2. the wordmark unfurls around it — "F" glides left, "24" glides right
 *     while "REIGHT" is revealed between them. The monogram glyphs never
 *     leave the screen, so there is no cut, only expansion,
 *  3. a road line draws out beneath the finished wordmark and rests.
 *
 * Colors read from [MaterialTheme.colorScheme] so the splash always matches
 * the user's active trucker theme (Indigo / Green / Logistics) and mode.
 * Purely visual. No navigation implications.
 */
@Composable
fun TruckerSplashScreen(
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val background = colorScheme.background
    val wordmarkColor = colorScheme.onBackground
    val accentColor = colorScheme.primary
    val roadColor = colorScheme.outlineVariant

    val markAlpha = remember { Animatable(0f) }
    val markOffsetY = remember { Animatable(8f) }
    val unfurl = remember { Animatable(0f) }
    val roadScale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { markAlpha.animateTo(1f, tween(durationMillis = 320)) }
        launch {
            markOffsetY.animateTo(
                0f,
                tween(durationMillis = 420, easing = LinearOutSlowInEasing),
            )
        }
        launch {
            delay(480)
            unfurl.animateTo(1f, tween(durationMillis = 620, easing = FastOutSlowInEasing))
        }
        launch {
            delay(980)
            roadScale.animateTo(1f, tween(durationMillis = 320, easing = LinearOutSlowInEasing))
        }
    }

    val wordmarkStyle = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.graphicsLayer {
                    alpha = markAlpha.value
                    translationY = markOffsetY.value.dp.toPx()
                },
            ) {
                Text(text = "F", style = wordmarkStyle, color = wordmarkColor)
                Text(
                    text = "REIGHT ",
                    style = wordmarkStyle,
                    color = wordmarkColor,
                    maxLines = 1,
                    softWrap = false,
                    // Reveal only the leading `unfurl` fraction of the natural
                    // text width; the Row re-centres every frame, so "F" and
                    // "24" glide outward as the middle letters stream in.
                    modifier = Modifier
                        .clipToBounds()
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(
                                constraints.copy(minWidth = 0),
                            )
                            val width = (placeable.width * unfurl.value).roundToInt()
                            layout(width, placeable.height) {
                                placeable.placeRelative(0, 0)
                            }
                        },
                )
                Text(text = "24", style = wordmarkStyle, color = accentColor)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Road line: draws out from centre once, then rests.
            Box(
                modifier = Modifier
                    .size(width = 96.dp, height = 2.dp)
                    .clip(RoundedCornerShape(1.dp)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { scaleX = roadScale.value }
                        .background(
                            color = roadColor,
                            shape = RoundedCornerShape(1.dp),
                        ),
                )
            }
        }
    }
}