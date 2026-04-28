package com.senku.ui.tablet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TabletEvidenceVisibilityPolicyTest {
    @Test
    fun tabletLandscapeEvidencePaneKeepsUsefulSourceEvidenceWidth() {
        val policy = tabletEvidenceVisibilityPolicy()

        assertEquals(360, policy.evidencePaneWidthDp)
        assertEquals(EvidenceRailDensity.Full, policy.landscapeRailDensity)
        assertTrue(policy.activeSnippetMaxLines >= 10)
    }

    @Test
    fun tabletPortraitCollapsedEvidencePreviewKeepsContextVisible() {
        val policy = tabletEvidenceVisibilityPolicy()

        assertTrue(policy.portraitCollapsedByDefault)
        assertTrue(policy.collapsedTitleMaxLines >= 2)
        assertTrue(policy.collapsedSnippetMaxLines >= 5)
    }

    @Test
    fun tabletPortraitCollapsedEvidencePreviewUsesSnippetWhenAvailable() {
        val previewText = buildCollapsedEvidencePreviewText(
            anchor(
                section = "water storage",
                snippet = "Keep treated water sealed between uses.",
            )
        )

        assertEquals("Keep treated water sealed between uses.", previewText)
    }

    @Test
    fun tabletPortraitCollapsedEvidencePreviewUsesPureEvidenceSnippetNormalization() {
        val previewText = buildCollapsedEvidencePreviewText(
            anchor(
                section = "water storage",
                snippet = "  Keep treated\n water\tsealed.  ",
            )
        )

        assertEquals("Keep treated water sealed.", previewText)
    }

    @Test
    fun tabletPortraitCollapsedEvidencePreviewFallsBackToGuideSectionContext() {
        val previewText = buildCollapsedEvidencePreviewText(
            anchor(
                section = "water storage",
                snippet = "",
            )
        )

        assertEquals("Guide section: water storage", previewText)
    }

    @Test
    fun tabletPortraitCollapsedEvidencePreviewUsesPureEvidenceSectionNormalization() {
        val previewText = buildCollapsedEvidencePreviewText(
            anchor(
                section = "  water\n storage  ",
                snippet = "",
            )
        )

        assertEquals("Guide section: water storage", previewText)
    }

    @Test
    fun tabletPortraitCollapsedEvidencePreviewStaysQuietWithoutEvidenceContext() {
        val previewText = buildCollapsedEvidencePreviewText(
            anchor(
                section = "",
                snippet = "",
            )
        )

        assertEquals("", previewText)
    }

    @Test
    fun crossReferenceCardCountIncludesActiveAnchorWhenPresent() {
        val count = buildCrossReferenceCardCount(
            anchor(section = "water storage", snippet = ""),
            listOf(
                XRefState(id = "GD-215", title = "Rainwater Catchment"),
                XRefState(id = "GD-216", title = "Clay Filter"),
            ),
        )

        assertEquals(3, count)
    }

    @Test
    fun crossReferenceCardCountDoesNotInventAnchorWithoutSource() {
        val count = buildCrossReferenceCardCount(
            AnchorState(
                key = "",
                id = "",
                title = "",
                section = "",
                snippet = "",
                hasSource = false,
            ),
            listOf(XRefState(id = "GD-215", title = "Rainwater Catchment")),
        )

        assertEquals(1, count)
    }

    private fun anchor(
        section: String,
        snippet: String,
    ): AnchorState {
        return AnchorState(
            key = "source-1",
            id = "GD-214",
            title = "Water purification and storage",
            section = section,
            snippet = snippet,
            hasSource = true,
        )
    }
}
