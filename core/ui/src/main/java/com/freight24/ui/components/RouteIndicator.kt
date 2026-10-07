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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight24.ui.theme.FreightThemeTokens

@Composable
fun RouteIndicatorWithCities(
    fromCity: String,
    toCity: String,
    fromLabel: String? = null,
    toLabel: String? = null,
    modifier: Modifier = Modifier,
    maxCityLines: Int = Int.MAX_VALUE,
) {
    val extended = FreightThemeTokens.colors
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.6.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Column(modifier = modifier) {
        // Intrinsic height lets the connector grow with the origin's wrapped text.
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(12.dp).fillMaxHeight().padding(top = 6.dp),
            ) {
                Box(
                    modifier = Modifier.size(12.dp).background(extended.routeOrigin, CircleShape),
                )
                Box(
                    modifier = Modifier.width(4.dp).weight(1f).background(
                        Brush.verticalGradient(listOf(extended.routeOrigin, extended.routeDestination)),
                    ),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f).padding(bottom = 12.dp)) {
                if (fromLabel != null) Text(text = fromLabel.uppercase(), style = labelStyle)
                Text(
                    text = fromCity,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = maxCityLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Row(verticalAlignment = Alignment.Top) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(12.dp)) {
                Box(modifier = Modifier.width(4.dp).height(6.dp).background(extended.routeDestination))
                Box(modifier = Modifier.size(12.dp).background(extended.routeDestination, CircleShape))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (toLabel != null) Text(text = toLabel.uppercase(), style = labelStyle)
                Text(
                    text = toCity,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = maxCityLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
