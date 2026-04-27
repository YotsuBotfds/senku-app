package com.senku.ui.sources

import org.junit.Assert.assertEquals
import org.junit.Test

class SourceRowModelTest {
    @Test
    fun buildSourceRowMetaUsesReadableSeparatorAndNormalizesCategory() {
        assertEquals("water · anchor", buildSourceRowMeta(category = " Water ", isAnchor = true))
        assertEquals(
            "resource management",
            buildSourceRowMeta(category = "resource-management", isAnchor = false),
        )
        assertEquals("anchor", buildSourceRowMeta(category = "", isAnchor = true))
    }

    @Test
    fun displayFieldsUseFallbacksForBlankValues() {
        val source = SourceRowModel(guideId = " ", title = " ")

        assertEquals("GD-?", source.displayGuideId())
        assertEquals("Source guide", source.displayTitle())
    }
}
