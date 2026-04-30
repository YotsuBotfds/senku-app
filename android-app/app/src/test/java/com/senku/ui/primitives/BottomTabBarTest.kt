package com.senku.ui.primitives

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomTabBarTest {
    @Test
    fun composeVerticalRailTokensKeepCompactProportions() {
        assertTrue(Rev03ComposeNavRailIconSizeDp in 20..28)
        assertTrue(Rev03ComposeNavRailLabelFontSizeSp in 9..12)
        assertTrue(Rev03ComposeNavRailLabelLineHeightSp in Rev03ComposeNavRailLabelFontSizeSp..16)
        assertTrue(Rev03ComposeNavRailIconLabelGapDp in 2..5)
        assertTrue(Rev03ComposeNavRailIconSizeDp > Rev03ComposeNavRailLabelLineHeightSp)
        assertTrue(Rev03ComposeNavRailIconLabelGapDp < Rev03ComposeNavRailLabelFontSizeSp)
        assertEquals(
            Rev03ComposeNavRailLabelLineHeightSp - Rev03ComposeNavRailLabelFontSizeSp,
            Rev03ComposeNavRailIconLabelGapDp,
        )
    }
}
