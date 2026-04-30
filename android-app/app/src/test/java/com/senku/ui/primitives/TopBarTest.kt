package com.senku.ui.primitives

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TopBarTest {
    @Test
    fun actionLayoutKeepsBackLeadingAndMockChromeTrailing() {
        val layout = topBarActionLayout(
            listOf(
                TopBarActionSpec.share("Share"),
                TopBarActionSpec.back("Back"),
                TopBarActionSpec.pin("More"),
                TopBarActionSpec.home("Home"),
            )
        )

        assertEquals(listOf(TopBarActionKind.Back), layout.leading.map { it.kind })
        assertEquals(
            listOf(TopBarActionKind.Home, TopBarActionKind.Share, TopBarActionKind.Pin),
            layout.trailing.map { it.kind },
        )
    }

    @Test
    fun actionLayoutDropsInvisibleAndDuplicateActions() {
        val layout = topBarActionLayout(
            listOf(
                TopBarActionSpec.back("Back"),
                TopBarActionSpec.home("Home", isVisible = false),
                TopBarActionSpec.share("Share"),
                TopBarActionSpec.share("Duplicate share"),
            )
        )

        assertEquals(listOf(TopBarActionKind.Back), layout.leading.map { it.kind })
        assertEquals(listOf(TopBarActionKind.Share), layout.trailing.map { it.kind })
    }

    @Test
    fun actionLayoutPlacesGuideOverflowAfterHomeAndPin() {
        val layout = topBarActionLayout(
            listOf(
                TopBarActionSpec.back("Back"),
                TopBarActionSpec.overflow("More options"),
                TopBarActionSpec.pin("Save guide"),
                TopBarActionSpec.home("Library"),
            )
        )

        assertEquals(listOf(TopBarActionKind.Back), layout.leading.map { it.kind })
        assertEquals(
            listOf(TopBarActionKind.Home, TopBarActionKind.Pin, TopBarActionKind.Overflow),
            layout.trailing.map { it.kind },
        )
    }

    @Test
    fun backActionDefaultsToCompactIconAndSharedSizing() {
        val backActionWidth = topBarActionWidthDpForTest(TopBarActionKind.Back)
        val sharedActionWidths = listOf(
            backActionWidth,
            topBarActionWidthDpForTest(TopBarActionKind.Home),
            topBarActionWidthDpForTest(TopBarActionKind.Share),
        )
        val backIconSize = topBarBackIconSizeDpForTest()
        val leadingDividerHeight = topBarLeadingDividerHeightDpForTest()

        assertEquals("Back", topBarBackActionLabelForTest())
        assertEquals(false, TopBarActionSpec.back("Back").showsBackLabel)
        assertEquals(1, sharedActionWidths.distinct().size)
        assertTrue(backActionWidth in 24..36)
        assertTrue(backIconSize < backActionWidth)
        assertTrue(backIconSize in 16..24)
        assertTrue(leadingDividerHeight < backActionWidth)
        assertTrue(leadingDividerHeight in 20..32)
    }

    @Test
    fun topChromeTypographyKeepsCompactTitleAndActionLabelScale() {
        val titleFontSize = topBarTitleFontSizeSpForTest()
        val chromeLabelFontSize = topBarChromeLabelFontSizeSpForTest()

        assertTrue(titleFontSize in 12.0f..15.0f)
        assertTrue(chromeLabelFontSize in 8.0f..11.0f)
        assertTrue(titleFontSize > chromeLabelFontSize)
    }
}
