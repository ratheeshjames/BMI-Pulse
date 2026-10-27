package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `verify app name resource and dashboard launch`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("BMI Pulse", context.getString(R.string.app_name))

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dashboard_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bmi_value_text").assertIsDisplayed()
    }

    @Test
    fun `navigate to quick calculator and toggle unit system`() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_tab_calculator").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("calculator_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("unit_system_btn_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("unit_system_btn_1").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("bmi_value_text").assertExists()
    }
}
