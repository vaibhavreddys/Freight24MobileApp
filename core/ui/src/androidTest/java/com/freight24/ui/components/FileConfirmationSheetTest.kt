package com.freight24.ui.components

import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalMaterial3Api::class)
class FileConfirmationSheetTest {
    @get:Rule val compose = createComposeRule()
    private val selectedUris = listOf(Uri.parse("file:///upload_test.jpg"))

    @Test fun idleSheetAllowsUpload() {
        var clicks = 0
        compose.setContent {
            MaterialTheme {
                FileConfirmationSheet(
                    selectedUris = selectedUris,
                    isPdf = false,
                    onUploadClick = { clicks++ },
                    onDismiss = {},
                )
            }
        }
        compose.onNodeWithText("Upload").assertIsEnabled().performClick()
        compose.onNodeWithText("Please wait").assertDoesNotExist()
        compose.runOnIdle { assertEquals(1, clicks) }
    }

    @Test fun busySheetShowsPreparationThenUploadAndBlocksRepeatClick() {
        val progressText = mutableStateOf("Preparing document")
        compose.setContent {
            MaterialTheme {
                FileConfirmationSheet(
                    selectedUris = selectedUris,
                    isPdf = false,
                    isUploading = true,
                    progressText = progressText.value,
                    onUploadClick = {},
                    onDismiss = {},
                )
            }
        }
        compose.onNodeWithText("Preparing document").assertIsDisplayed()
        compose.onNodeWithText("Please wait").assertIsNotEnabled()
        compose.runOnIdle { progressText.value = "Uploading document" }
        compose.onNodeWithText("Uploading document").assertIsDisplayed()
        compose.onNodeWithText("Preparing document").assertDoesNotExist()
    }

    @Test fun busySheetCannotBeDismissedWithBack() {
        var dismissals = 0
        compose.setContent {
            MaterialTheme {
                FileConfirmationSheet(
                    selectedUris = selectedUris,
                    isPdf = false,
                    isUploading = true,
                    progressText = "Uploading document",
                    onUploadClick = {},
                    onDismiss = { dismissals++ },
                )
            }
        }
        compose.onNodeWithText("Uploading document").assertIsDisplayed()
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK)
        compose.onNodeWithText("Uploading document").assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, dismissals) }
    }
}
