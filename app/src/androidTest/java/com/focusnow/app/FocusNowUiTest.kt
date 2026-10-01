package com.focusnow.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.focusnow.app.data.model.UserProfile
import com.focusnow.app.ui.screens.home.HomeScreen
import com.focusnow.app.ui.theme.FocusNowTheme
import org.junit.Rule
import org.junit.Test

class FocusNowUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testHomeScreen_displaysKeyElements() {
        composeTestRule.setContent {
            FocusNowTheme {
                HomeScreen(
                    onNavigateTo = {}
                )
            }
        }

        // Verify key headers and quick action texts
        composeTestRule.onNodeWithText("Quick Actions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start Study").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Plan Day").assertIsDisplayed()
    }
}
