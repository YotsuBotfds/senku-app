package com.senku.ui.evidence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PureEvidenceModelsTest {
    @Test
    fun normalizeEvidenceLabelTrimsDelimitersAndWhitespace() {
        assertEquals("reviewed evidence", normalizeEvidenceLabel("  Reviewed_Evidence  "))
        assertEquals("generated evidence", normalizeEvidenceLabel("Generated_Evidence"))
        assertEquals("limited fit", normalizeEvidenceLabel("limited-fit"))
        assertEquals("source family", normalizeEvidenceLabel("source \n\t family"))
    }

    @Test
    fun sourceFallbackVocabularyIsShared() {
        assertEquals("GD-?", normalizeEvidenceGuideId(" "))
        assertEquals("Source guide", normalizeEvidenceSourceTitle(" "))
        assertEquals("anchor", evidenceAnchorLabel(EvidenceAnchorLabelStyle.Short))
        assertEquals("anchor guide", evidenceAnchorLabel(EvidenceAnchorLabelStyle.Guide))
    }

    @Test
    fun normalizeEvidenceSnippetKeepsSingleLinePreviewText() {
        assertEquals(
            "Boil water, then cool it covered.",
            normalizeEvidenceSnippet("  Boil water,\n then\tcool it covered.  "),
        )
    }

    @Test
    fun sourceRowDataUsesFallbacksAndEmptySnippetCopy() {
        val row = EvidenceSourceModel(
            guideId = " ",
            title = " ",
            snippet = " ",
        ).toEvidenceSourceRowData()

        assertEquals("GD-?", row.guideId)
        assertEquals("Source guide", row.title)
        assertEquals("", row.section)
        assertEquals("", row.metaLabel)
        assertEquals("", row.snippet)
        assertEquals("No snippet yet.", row.quotedSnippet)
        assertFalse(row.hasSnippet)
    }

    @Test
    fun sourceRowDataBuildsMetaLabelFromLabelSectionAndAnchor() {
        val row = EvidenceSourceModel(
            guideId = " GD-214 ",
            title = " Water   storage ",
            section = " treated   water ",
            snippet = " Keep sealed. ",
            label = "Reviewed_Evidence",
            isAnchor = true,
        ).toEvidenceSourceRowData()

        assertEquals("GD-214", row.guideId)
        assertEquals("Water storage", row.title)
        assertEquals("treated water", row.section)
        assertEquals("reviewed evidence | treated water | anchor", row.metaLabel)
        assertEquals("Keep sealed.", row.snippet)
        assertEquals("\"Keep sealed.\"", row.quotedSnippet)
        assertTrue(row.isAnchor)
        assertTrue(row.hasSnippet)
    }

    @Test
    fun surfaceViewDataNormalizesLabelCountsRowsAndPicksActiveSource() {
        val viewData = EvidenceSurfaceModel(
            label = " Strong_Evidence ",
            activeSourceIndex = 1,
            sources = listOf(
                EvidenceSourceModel(guideId = "GD-1", title = "First"),
                EvidenceSourceModel(guideId = "GD-2", title = "Second"),
            ),
        ).toEvidenceSurfaceViewData()

        assertEquals("strong evidence", viewData.label)
        assertEquals("2 sources", viewData.sourceCountLabel)
        assertEquals("GD-2", viewData.activeSource?.guideId)
        assertEquals(2, viewData.rows.size)
        assertFalse(viewData.isEmpty)
    }

    @Test
    fun surfaceViewDataHandlesEmptySourceList() {
        val viewData = EvidenceSurfaceModel(label = " ").toEvidenceSurfaceViewData()

        assertEquals("Evidence", viewData.label)
        assertEquals("No sources", viewData.sourceCountLabel)
        assertNull(viewData.activeSource)
        assertTrue(viewData.rows.isEmpty())
        assertTrue(viewData.isEmpty)
    }
}
