package com.freight24.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freight24.ui.trucker.TruckerColors
import com.freight24.ui.trucker.rememberTruckerColors

/**
 * A reusable filter chip component for consistent selection UI across the app.
 * This matches the design used in the Trips tab but with reduced default sizes.
 *
 * The selected fill is [TruckerColors.brand] — the same token the trucker Trips
 * rail and the profile header select with — so one chip means one thing across
 * both modules. It used to be Material's `onPrimaryContainer`, which resolved to
 * near-black in light and mint in dark, reading as a different product from the
 * forest green used everywhere else.
 */
@Composable
fun FilterSelectionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    count: Int? = null,
) {
    val colors = rememberTruckerColors()

    val backgroundColor =
        if (isSelected) {
            colors.brand
        } else {
            MaterialTheme.colorScheme.surface
        }

    val textColor =
        if (isSelected) {
            colors.onBrand
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

    val borderColor =
        if (isSelected) {
            Color.Transparent
        } else {
            MaterialTheme.colorScheme.outlineVariant
        }

    Surface(
        modifier = modifier.minimumInteractiveComponentSize().clickable { onClick() },
        color = backgroundColor,
        shape = RoundedCornerShape(50),
        border = if (isSelected) null else BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = textColor,
                )
                Spacer(Modifier.width(8.dp))
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp),
            )

            if (count != null && count > 0) {
                Spacer(Modifier.width(8.dp))

                Surface(
                    color =
                        if (isSelected) {
                            colors.surface
                        } else {
                            colors.brandSoft
                        },
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.brand,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}
