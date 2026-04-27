package com.senku.ui.sources

import com.senku.ui.evidence.EvidenceSourceModel
import org.junit.Assert.assertEquals
import org.junit.Test

class SourceRowModelTest {
    @Test
    fun buildSourceRowMetaUsesReadableSeparatorAndNormalizesCategory() {
        assertEquals("water \u00B7 anchor guide", buildSourceRowMeta(category = " Water ", isAnchor = true))
        assertEquals(
            "resource management",
            buildSourceRowMeta(category = "resource-management", isAnchor = false),
        )
        assertEquals("anchor guide", buildSourceRowMeta(category = "", isAnchor = true))
    }

    @Test
    fun displayFieldsUseFallbacksForBlankValues() {
        val source = SourceRowModel(guideId = " ", title = " ")

        assertEquals("GD-?", source.displayGuideId())
        assertEquals("Source guide", source.displayTitle())
    }

    @Test
    fun evidenceSourceMapsToSourceRowWithSharedVocabulary() {
        val source = EvidenceSourceModel(
            guideId = " GD-214 ",
            title = " Water   storage ",
            label = "Reviewed_Evidence",
            isAnchor = true,
        ).toSourceRowModel()

        assertEquals("GD-214", source.guideId)
        assertEquals("Water storage", source.title)
        assertEquals("reviewed evidence", source.category)
        assertEquals(
            "reviewed evidence \u00B7 anchor guide",
            buildSourceRowMeta(source.category, source.isAnchor),
        )
    }
}
