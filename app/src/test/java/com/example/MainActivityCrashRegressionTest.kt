package com.example

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainActivityCrashRegressionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `MainActivity launches in clean Go Alpha UI without Ramses 3D, welcome banner, suggested prompts, or seed books`() {
        Shadows.shadowOf(composeTestRule.activity.application)
            .grantPermissions(Manifest.permission.RECORD_AUDIO)

        composeTestRule.waitForIdle()

        // 0. Welcome / Start Journey Screen appears on launch with interactive Start Journey CTA
        composeTestRule.onNodeWithTag("opening_logo_splash").assertIsDisplayed()
        composeTestRule.onNodeWithTag("start_journey_button").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        // 1. Go Alpha Header, Dedicated GLB Slot, Input Bar, and 4-tab Bottom Bar
        composeTestRule.onNodeWithTag("menu_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("header_language_toggle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("header_alpha_go_logo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_glb_slot").assertIsDisplayed()
        composeTestRule.onNodeWithTag("grade_chip_1").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("alpha_go_home_watermark").assertIsDisplayed()
        assertTrue(
            "Expected welcome + 5 books banner to be removed",
            composeTestRule.onAllNodesWithTag("home_welcome_title").fetchSemanticsNodes().isEmpty()
        )
        assertTrue(
            "Expected Ramses 3D fallback card to be removed from Home",
            composeTestRule.onAllNodesWithTag("pharaonic_3d_fallback").fetchSemanticsNodes().isEmpty()
        )
        assertTrue(
            "Expected Ramses 3D avatar viewport to be removed from Home",
            composeTestRule.onAllNodesWithTag("ramses_3d_avatar_viewport").fetchSemanticsNodes().isEmpty()
        )
        assertTrue(
            "Expected suggested prompts under Ramses model to be removed",
            composeTestRule.onAllNodesWithTag("suggested_prompt_0").fetchSemanticsNodes().isEmpty()
        )
        composeTestRule.onNodeWithTag("chat_input_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_home").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_quizzes").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_videos").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_library").assertIsDisplayed()

        // 2. Navigate across Book-Only Quizzes (empty library CTA), Cloud Videos, Empty Library, and back to Home
        composeTestRule.onNodeWithTag("nav_quizzes").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("quizzes_tab_view").assertIsDisplayed()
        composeTestRule.onNodeWithTag("quizzes_go_to_library_button").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_videos").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("videos_tab_view").assertIsDisplayed()
        composeTestRule.onNodeWithTag("upload_video_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("videos_empty_guide").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_library").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("library_tab_view").assertIsDisplayed()
        composeTestRule.onNodeWithTag("library_upload_pdf_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("library_empty_guide").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_home").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_glb_slot").assertIsDisplayed()

        // 3. Test Go Alpha Live Voice overlay
        composeTestRule.onNodeWithTag("gemini_live_call_button").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("gemini_live_overlay").assertIsDisplayed()
        composeTestRule.onNodeWithTag("gemini_live_orb_container").assertIsDisplayed()
        composeTestRule.onNodeWithTag("gemini_live_close_button").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        // 4. Open Control Center Drawer and verify updated IMG_1050 menu items & removed legacy items
        composeTestRule.onNodeWithTag("menu_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("drawer_my_account_item").assertIsDisplayed()
        composeTestRule.onNodeWithTag("drawer_curriculum_item").assertIsDisplayed()
        composeTestRule.onNodeWithTag("drawer_achievements_item").assertIsDisplayed()
        composeTestRule.onNodeWithTag("drawer_contact_item").assertIsDisplayed()
        composeTestRule.onNodeWithTag("drawer_settings_item").assertIsDisplayed()
        composeTestRule.onNodeWithTag("drawer_sign_out_item").assertIsDisplayed()
        assertTrue(
            "Expected RAG drawer item to be removed from Control Center",
            composeTestRule.onAllNodesWithTag("drawer_rag_files_item").fetchSemanticsNodes().isEmpty()
        )
        assertTrue(
            "Expected 3D Artifacts drawer item to be removed from Control Center",
            composeTestRule.onAllNodesWithTag("drawer_artifacts_item").fetchSemanticsNodes().isEmpty()
        )
        assertTrue(
            "Expected Ramses 3D switcher to be removed from Control Center",
            composeTestRule.onAllNodesWithTag("drawer_gems_section").fetchSemanticsNodes().isEmpty()
        )
        composeTestRule.onNodeWithTag("drawer_curriculum_item").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("library_tab_view").assertIsDisplayed()

        // 5. Open Profile / Auth Sign-In Dialog
        composeTestRule.onNodeWithTag("profile_button").performClick()
        composeTestRule.waitForIdle()
        assertTrue(
            composeTestRule.onAllNodesWithTag("auth_dialog_scroll").fetchSemanticsNodes().isNotEmpty()
        )
        assertTrue(
            composeTestRule.onAllNodesWithTag("change_profile_photo_button").fetchSemanticsNodes().isNotEmpty()
        )
    }
}
