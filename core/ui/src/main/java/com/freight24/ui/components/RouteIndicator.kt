package com.freight24.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.ui.theme.FreightThemeTokens

@Composable
fun RouteIndicatorWithCities(
    fromCity: String,
    toCity: String,
    fromLabel: String? = null,
    toLabel: String? = null,
) {
    val extended = FreightThemeTokens.colors
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.6.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Row(
        verticalAlignment = Alignment.Top,
    ) {
        // Indicator column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Origin (pickup) dot
            Box(
                modifier =
                    Modifier
                        .size(12.dp)
                        .background(
                            color = extended.routeOrigin,
                            shape = CircleShape,
                        ),
            )

            // Gradient line — origin → destination
            Box(
                modifier =
                    Modifier
                        .width(4.dp)
                        .height(28.dp)
                        .background(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            extended.routeOrigin,
                                            extended.routeDestination,
                                        ),
                                ),
                        ),
            )

            // Destination (drop) dot
            Box(
                modifier =
                    Modifier
                        .size(12.dp)
                        .background(
                            color = extended.routeDestination,
                            shape = CircleShape,
                        ),
            )
        }

        Spacer(Modifier.width(12.dp))

        // City names aligned with dots
        Column {
            if (fromLabel != null) {
                Text(
                    text = fromLabel.uppercase(),
                    style = labelStyle,
                )
            }
            Text(
                text = fromCity,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(if (fromLabel != null) 12.dp else 18.dp))

            if (toLabel != null) {
                Text(
                    text = toLabel.uppercase(),
                    style = labelStyle,
                )
            }
            Text(
                text = toCity,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
