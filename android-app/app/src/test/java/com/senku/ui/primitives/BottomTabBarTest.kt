package com.senku.ui.primitives

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomTabBarTest {
    @Test
    fun composeVerticalRailTokensKeepCompactProportions() {
        assertTrue(NavRailMetrics.IconSizeDp in 20..28)
        assertTrue(NavRailMetrics.LabelFontSizeSp in 9..12)
        assertTrue(NavRailMetrics.LabelLineHeightSp in NavRailMetrics.LabelFontSizeSp..16)
        assertTrue(NavRailMetrics.IconLabelGapDp in 2..5)
        assertTrue(NavRailMetrics.IconSizeDp > NavRailMetrics.LabelLineHeightSp)
        assertTrue(NavRailMetrics.IconLabelGapDp < NavRailMetrics.LabelFontSizeSp)
        assertEquals(
            NavRailMetrics.LabelLineHeightSp - NavRailMetrics.LabelFontSizeSp,
            NavRailMetrics.IconLabelGapDp,
        )
    }

    @Test
    fun composeVerticalRailMetricsPreserveCurrentDimensions() {
        assertEquals(52, NavRailMetrics.WidthDp)
        assertEquals(1, NavRailMetrics.DividerWidthDp)
        assertEquals(4, NavRailMetrics.HorizontalPaddingDp)
        assertEquals(7, NavRailMetrics.VerticalPaddingDp)
        assertEquals(1, NavRailMetrics.ItemSpacingDp)
        assertEquals(48, NavRailMetrics.ItemHeightDp)
        assertEquals(2, NavRailMetrics.ItemHorizontalPaddingDp)
        assertEquals(2, NavRailMetrics.ItemVerticalPaddingDp)
        assertEquals(22, NavRailMetrics.IconSizeDp)
        assertEquals(10, NavRailMetrics.LabelFontSizeSp)
        assertEquals(13, NavRailMetrics.LabelLineHeightSp)
        assertEquals(3, NavRailMetrics.IconLabelGapDp)
        assertEquals(2, NavRailMetrics.LabelMaxLines)
        assertEquals(22, NavRailMetrics.SelectedIndicatorWidthDp)
        assertEquals(2, NavRailMetrics.SelectedIndicatorHeightDp)
        assertEquals(1, NavRailMetrics.UnselectedIndicatorHeightDp)
    }

    @Test
    fun legacyComposeNavRailTokenNamesPointAtCentralMetrics() {
        assertEquals(NavRailMetrics.IconSizeDp, Rev03ComposeNavRailIconSizeDp)
        assertEquals(NavRailMetrics.LabelFontSizeSp, Rev03ComposeNavRailLabelFontSizeSp)
        assertEquals(NavRailMetrics.LabelLineHeightSp, Rev03ComposeNavRailLabelLineHeightSp)
        assertEquals(NavRailMetrics.IconLabelGapDp, Rev03ComposeNavRailIconLabelGapDp)
    }
}
