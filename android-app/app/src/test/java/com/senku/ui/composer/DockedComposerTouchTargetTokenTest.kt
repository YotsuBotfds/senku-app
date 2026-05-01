package com.senku.ui.composer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DockedComposerTouchTargetTokenTest {
    @Test
    fun addActionKeepsFortyEightDpTouchTargetAroundThirtyTwoDpVisualCircle() {
        assertEquals(48, DockedComposerTouchTargetTokens.FIELD_USE_TOUCH_TARGET_MIN_DP)
        assertTrue(
            DockedComposerTouchTargetTokens.ADD_ACTION_TOUCH_TARGET_DP >=
                DockedComposerTouchTargetTokens.FIELD_USE_TOUCH_TARGET_MIN_DP
        )
        assertEquals(32, DockedComposerTouchTargetTokens.ADD_ACTION_VISUAL_SIZE_DP)
        assertEquals(
            (DockedComposerTouchTargetTokens.ADD_ACTION_TOUCH_TARGET_DP -
                DockedComposerTouchTargetTokens.ADD_ACTION_VISUAL_SIZE_DP) / 2,
            DockedComposerTouchTargetTokens.ADD_ACTION_PADDING_DP,
        )
    }

    @Test
    fun addActionPaddingPreservesVisualCircleInsideTouchTarget() {
        assertEquals(
            DockedComposerTouchTargetTokens.ADD_ACTION_TOUCH_TARGET_DP,
            DockedComposerTouchTargetTokens.ADD_ACTION_VISUAL_SIZE_DP +
                DockedComposerTouchTargetTokens.ADD_ACTION_PADDING_DP * 2,
        )
        assertTrue(
            DockedComposerTouchTargetTokens.ADD_ACTION_VISUAL_SIZE_DP <
                DockedComposerTouchTargetTokens.ADD_ACTION_TOUCH_TARGET_DP
        )
    }

    @Test
    fun addActionIsExplicitlyDisabledWhenNoProductionAddActionExists() {
        val state = DockedComposerAddActionPolicy.resolve(addActionAvailable = false)

        assertFalse(state.enabled)
        assertEquals(
            "Add action unavailable",
            state.contentDescription,
        )
        assertEquals(DockedComposerTouchTargetTokens.ADD_ACTION_DISABLED_ALPHA, state.contentAlpha, 0.0f)
        assertTrue(state.contentAlpha >= 0.6f)
    }

    @Test
    fun addActionPolicyNamesEnabledStateWhenActionExists() {
        val state = DockedComposerAddActionPolicy.resolve(addActionAvailable = true)

        assertTrue(state.enabled)
        assertEquals("Add action", state.contentDescription)
        assertEquals(DockedComposerTouchTargetTokens.ADD_ACTION_ENABLED_ALPHA, state.contentAlpha, 0.0f)
    }
}
