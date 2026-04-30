package com.senku.ui.primitives

import org.junit.Assert.assertEquals
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
        assertEquals("Back", topBarBackActionLabelForTest())
        assertEquals(false, TopBarActionSpec.back("Back").showsBackLabel)
        assertEquals(28, topBarActionWidthDpForTest(TopBarActionKind.Back))
        assertEquals(28, topBarActionWidthDpForTest(TopBarActionKind.Home))
        assertEquals(28, topBarActionWidthDpForTest(TopBarActionKind.Share))
        assertEquals(18, topBarBackIconSizeDpForTest())
        assertEquals(24, topBarLeadingDividerHeightDpForTest())
    }

    @Test
    fun topChromeTypographyUsesSharedDetailTokens() {
        assertEquals(13.0f, topBarTitleFontSizeSpForTest(), 0.0f)
        assertEquals(9.5f, topBarChromeLabelFontSizeSpForTest(), 0.0f)
    }
}
