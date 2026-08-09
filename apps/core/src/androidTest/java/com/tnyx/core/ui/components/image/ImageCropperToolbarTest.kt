package com.tnyx.core.ui.components.image

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tnyx.core.theme.TnyxThemeConfig
import com.tnyx.core.theme.TnyxThemeMode
import com.tnyx.core.theme.TnyxThemeProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageCropperToolbarTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun toolbarActionsRemainVisibleAndDispatchCallbacks() {
        var flipCount = 0
        var rotateCount = 0
        var resetCount = 0
        var cancelCount = 0
        var applyCount = 0

        composeRule.setContent {
            TnyxThemeProvider(
                config = TnyxThemeConfig(
                    mode = TnyxThemeMode.Dark,
                    useDynamicColor = false,
                ),
            ) {
                ImageCropperToolbar(
                    fineAngle = 0f,
                    isFlippedHorizontal = false,
                    selectedAspectRatio = CropAspectRatio.SQUARE,
                    isProcessing = false,
                    canApply = true,
                    onFineAngleChange = {},
                    onFlip = { flipCount++ },
                    onRotateQuarterTurn = { rotateCount++ },
                    onReset = { resetCount++ },
                    onAspectRatioSelected = {},
                    onCancel = { cancelCount++ },
                    onApply = { applyCount++ },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Horizontal Flip / Mirror")
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithContentDescription("Rotate 90 degrees")
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithContentDescription("Reset")
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithContentDescription("Cancel")
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithContentDescription("Apply")
            .assertIsDisplayed()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, flipCount)
            assertEquals(1, rotateCount)
            assertEquals(1, resetCount)
            assertEquals(1, cancelCount)
            assertEquals(1, applyCount)
        }
    }

    @Test
    fun rulerDragDispatchesFineAngleChange() {
        var selectedAngle = 0f

        composeRule.setContent {
            TnyxThemeProvider(
                config = TnyxThemeConfig(
                    mode = TnyxThemeMode.Dark,
                    useDynamicColor = false,
                ),
            ) {
                ImageCropperToolbar(
                    fineAngle = 0f,
                    isFlippedHorizontal = false,
                    selectedAspectRatio = CropAspectRatio.SQUARE,
                    isProcessing = false,
                    canApply = true,
                    onFineAngleChange = { selectedAngle = it },
                    onFlip = {},
                    onRotateQuarterTurn = {},
                    onReset = {},
                    onAspectRatioSelected = {},
                    onCancel = {},
                    onApply = {},
                )
            }
        }

        composeRule.onNodeWithTag(ImageCropperRulerTestTag)
            .assertIsDisplayed()
            .performTouchInput { swipeLeft() }

        composeRule.runOnIdle {
            assertNotEquals(0f, selectedAngle)
        }
    }

    @Test
    fun discardDialogDispatchesBothActions() {
        var keepEditingCount = 0
        var discardCount = 0

        composeRule.setContent {
            TnyxThemeProvider(
                config = TnyxThemeConfig(
                    mode = TnyxThemeMode.Dark,
                    useDynamicColor = false,
                ),
            ) {
                ImageCropperDiscardDialog(
                    onKeepEditing = { keepEditingCount++ },
                    onDiscard = { discardCount++ },
                )
            }
        }

        composeRule.onNodeWithText("Keep editing")
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithText("Discard")
            .assertIsDisplayed()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, keepEditingCount)
            assertEquals(1, discardCount)
        }
    }
}
