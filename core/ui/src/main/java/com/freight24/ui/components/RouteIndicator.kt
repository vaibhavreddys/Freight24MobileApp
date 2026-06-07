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
import androidx.compose.ui.unit.dp
import com.freight24.ui.theme.FreightThemeTokens

@Composable
fun RouteIndicatorWithCities(
    fromCity: String,
    toCity: String,
) {
    val extended = FreightThemeTokens.colors
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
            Text(
                text = fromCity,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(18.dp))

            Text(
                text = toCity,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
