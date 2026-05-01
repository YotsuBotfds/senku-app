package com.senku.ui.composer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DockedComposerTouchTargetTokenTest {
    @Test
    fun addActionKeepsFortyEightDpTouchTargetAroundThirtyTwoDpVisualCircle() {
        assertTrue(DockedComposerTouchTargetTokens.ADD_ACTION_TOUCH_TARGET_DP >= 48)
        assertEquals(32, DockedComposerTouchTargetTokens.ADD_ACTION_VISUAL_SIZE_DP)
        assertEquals(
            (DockedComposerTouchTargetTokens.ADD_ACTION_TOUCH_TARGET_DP -
                DockedComposerTouchTargetTokens.ADD_ACTION_VISUAL_SIZE_DP) / 2,
            DockedComposerTouchTargetTokens.ADD_ACTION_PADDING_DP,
        )
    }
}
