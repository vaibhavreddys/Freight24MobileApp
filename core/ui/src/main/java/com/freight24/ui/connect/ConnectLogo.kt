package com.freight24.ui.connect

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Freight24 Connect mark — geometric side-view truck.
 * Drawn on a 24dp grid, single-color fill so it can be tinted freely
 * (forest on light, mint on dark). Minimal by design: cargo box, cab,
 * two wheels. No gradients, no skeuomorphism.
 */
public val ConnectTruckMark: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    ImageVector.Builder(
        name = "ConnectTruckMark",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
        ) {
            // Cargo box
            moveTo(4f, 6.5f)
            horizontalLineTo(13.75f)
            arcToRelative(1.25f, 1.25f, 0f, false, true, 1.25f, 1.25f)
            verticalLineTo(15.5f)
            horizontalLineTo(2.75f)
            verticalLineTo(7.75f)
            arcToRelative(1.25f, 1.25f, 0f, false, true, 1.25f, -1.25f)
            close()
            // Cab with slanted windshield
            moveTo(16.25f, 9.25f)
            horizontalLineTo(18.1f)
            curveTo(18.47f, 9.25f, 18.82f, 9.41f, 19.06f, 9.68f)
            lineTo(21.12f, 12.03f)
            curveTo(21.32f, 12.26f, 21.43f, 12.55f, 21.43f, 12.86f)
            verticalLineTo(14.5f)
            arcToRelative(1f, 1f, 0f, false, true, -1f, 1f)
            horizontalLineTo(16.25f)
            close()
            // Rear wheel
            moveTo(6.75f, 16.25f)
            arcToRelative(1.9f, 1.9f, 0f, true, false, 3.8f, 0f)
            arcToRelative(1.9f, 1.9f, 0f, true, false, -3.8f, 0f)
            close()
            // Front wheel
            moveTo(16.85f, 16.25f)
            arcToRelative(1.9f, 1.9f, 0f, true, false, 3.8f, 0f)
            arcToRelative(1.9f, 1.9f, 0f, true, false, -3.8f, 0f)
            close()
        }
    }.build()
}
