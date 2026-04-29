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
}
