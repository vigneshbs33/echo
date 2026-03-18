package com.echo.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EchoAppUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun onboarding_displaysTitleAndApiKeyFields() {
        // Assert that the Echo title is present on the Onboarding screen
        composeTestRule.onNodeWithText("Echo").assertExists()

        // Assert that the API key card title exists
        composeTestRule.onNodeWithText("1. Your OpenAI Key \ud83d\udd11").assertExists()
        
        // Assert that the launch card exists
        composeTestRule.onNodeWithText("3. Launch Echo \ud83d\ude80").assertExists()
    }
}
