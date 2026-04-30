package com.senku.ui.primitives

import org.junit.Assert.assertEquals
import org.junit.Test

class BottomTabBarTest {
    @Test
    fun composeVerticalRailTokensMatchRev03XmlRailScale() {
        assertEquals(22, Rev03ComposeNavRailIconSizeDp)
        assertEquals(10, Rev03ComposeNavRailLabelFontSizeSp)
        assertEquals(13, Rev03ComposeNavRailLabelLineHeightSp)
    }
}
