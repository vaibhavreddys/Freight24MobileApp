package com.freight24.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.freight24.common.model.Trip
import com.freight24.ui.theme.Freight24Theme
import com.freight24.ui.theme.FreightAppTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TripCardLayoutTest {
    @get:Rule val compose = createComposeRule()
    private val location = "Bommasandra Jigani Link Road Industrial Area Bengaluru"

    @Test fun longLocationsOnNarrowPhone() = checkLayout(320, 1f)
    @Test fun longLocationsOnStandardPhone() = checkLayout(360, 1f)
    @Test fun longLocationsWithLargeFont() = checkLayout(320, 1.5f)

    private fun checkLayout(width: Int, fontScale: Float) {
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                Freight24Theme(appTheme = FreightAppTheme.GREEN, darkTheme = true) {
                    Box(Modifier.width(width.dp)) {
                        TripCard(
                            trip = Trip(
                                id = "long-route",
                                registrationNumber = "KA01AB1234",
                                truckType = "32 ft MXL",
                                status = "IN_TRANSIT",
                                createdAt = "07 Oct 2026",
                                tripCost = 35000.0,
                                totalPaidAmount = 5000.0,
                                origin = location,
                                destination = location,
                            ),
                        )
                    }
                }
            }
        }
        val output = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        if (output != null) {
            File(output).mkdirs()
            File(output, "trip-card-${width}-${fontScale}.png").outputStream().use {
                compose.onNodeWithTag("trip_card").captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
        for (text in listOf("₹ 35,000", "Paid ₹ 5,000", "07 Oct 2026", "KA01AB1234")) {
            val layouts = mutableListOf<TextLayoutResult>()
            compose.onNodeWithText(text, useUnmergedTree = true)
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertEquals(1, layouts.single().lineCount)
            val layout = layouts.single()
            assertTrue("$text must not be truncated", !layout.isLineEllipsized(0))
            assertTrue("$text must fit horizontally", layout.getLineRight(0) - layout.getLineLeft(0) <= layout.size.width + 1f)
        }
        val origin = compose.onAllNodesWithText(location, useUnmergedTree = true)[0]
        val destination = compose.onAllNodesWithText(location, useUnmergedTree = true)[1]
        for (node in listOf(origin, destination)) {
            val layouts = mutableListOf<TextLayoutResult>()
            node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertTrue(layouts.single().lineCount <= 2)
        }
        assertTrue(destination.fetchSemanticsNode().boundsInRoot.top >= origin.fetchSemanticsNode().boundsInRoot.bottom)
        assertTrue(compose.onNodeWithText("₹ 35,000", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.top >= destination.fetchSemanticsNode().boundsInRoot.bottom)


    }
}
