package com.freight24.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
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
import com.freight24.ui.theme.Freight24Theme
import com.freight24.ui.theme.FreightAppTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoadCardLayoutTest {
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
                        LoadCard(
                            sourceCity = location,
                            destinationCity = location,
                            postedTime = "1h ago",
                            price = "35000",
                            loadWeight = "16 Tons",
                            truckInfo = "32 ft MXL",
                            commodity = "Carton Box",
                            f24AgentName = "Admin",
                            onCallClick = {},
                            onBookmarkClick = {},
                            modifier = Modifier.testTag("test_load_card"),
                        )
                    }
                }
            }
        }
        val timeLayouts = mutableListOf<TextLayoutResult>()
        val priceLayouts = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText("1h ago", useUnmergedTree = true).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(timeLayouts) }
        compose.onNodeWithText("₹35,000", useUnmergedTree = true).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(priceLayouts) }
        assertEquals(1, timeLayouts.single().lineCount)
        assertEquals(1, priceLayouts.single().lineCount)
        assertTrue("Price must remain fully visible", !priceLayouts.single().isLineEllipsized(0))
        assertTrue("Posted time must remain fully visible", !timeLayouts.single().isLineEllipsized(0))

        val origin = compose.onAllNodesWithText(location, useUnmergedTree = true)[0]
        val destination = compose.onAllNodesWithText(location, useUnmergedTree = true)[1]
        for (node in listOf(origin, destination)) {
            val layouts = mutableListOf<TextLayoutResult>()
            node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertTrue(layouts.single().lineCount <= 2)
        }
        assertTrue(origin.fetchSemanticsNode().boundsInRoot.top >= compose.onNodeWithText("₹35,000", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.bottom)
        assertTrue(destination.fetchSemanticsNode().boundsInRoot.top >= origin.fetchSemanticsNode().boundsInRoot.bottom)

        val output = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        if (output != null) {
            File(output).mkdirs()
            File(output, "load-card-${width}-${fontScale}.png").outputStream().use {
                compose.onNodeWithTag("test_load_card").captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }
}
