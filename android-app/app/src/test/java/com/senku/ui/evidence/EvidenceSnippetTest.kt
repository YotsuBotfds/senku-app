package com.senku.ui.evidence

import org.junit.Assert.assertEquals
import org.junit.Test

class EvidenceSnippetTest {
    @Test
    fun anchorLineUsesCompactSourceLanguageWithoutPipeSeparators() {
        assertEquals(
            "GD-214 anchor",
            EvidenceSnippetModel(
                guideId = " GD-214 ",
                title = "Water storage",
            ).anchorLine(),
        )
    }

    @Test
    fun anchorLineFallsBackToAnchorWhenGuideIdIsMissing() {
        assertEquals(
            "anchor",
            EvidenceSnippetModel(
                guideId = " ",
                title = " ",
            ).anchorLine(),
        )
    }

    @Test
    fun evidenceSnippetDensityMatchesPhoneSourceCardPolish() {
        assertEquals(10f, EvidenceSnippetPaddingHorizontal.value)
        assertEquals(9f, EvidenceSnippetPaddingVertical.value)
        assertEquals(6f, EvidenceSnippetSectionSpacing.value)
        assertEquals(12f, EvidenceSnippetTitleSize.value)
        assertEquals(16f, EvidenceSnippetTitleLineHeight.value)
        assertEquals(11f, EvidenceSnippetSnippetSize.value)
        assertEquals(16f, EvidenceSnippetSnippetLineHeight.value)
    }

    @Test
    fun quotedSnippetKeepsExistingEmptyAndQuotedSemantics() {
        assertEquals(
            "\"Keep sealed.\"",
            EvidenceSnippetModel(
                guideId = "GD-214",
                title = "Water storage",
                snippet = " Keep sealed. ",
            ).quotedSnippet(),
        )
        assertEquals(
            "No snippet yet.",
            EvidenceSnippetModel(
                guideId = "GD-214",
                title = "Water storage",
                snippet = " ",
            ).quotedSnippet(),
        )
    }
}
