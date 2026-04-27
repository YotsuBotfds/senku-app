package com.senku.ui.tablet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TabletEvidenceVisibilityPolicyTest {
    @Test
    fun tabletLandscapeEvidencePaneKeepsUsefulProofWidth() {
        val policy = tabletEvidenceVisibilityPolicy()

        assertEquals(360, policy.evidencePaneWidthDp)
        assertTrue(policy.activeSnippetMaxLines >= 10)
    }

    @Test
    fun tabletPortraitCollapsedEvidencePreviewKeepsContextVisible() {
        val policy = tabletEvidenceVisibilityPolicy()

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
    fun tabletPortraitCollapsedEvidencePreviewFallsBackToSectionContext() {
        val previewText = buildCollapsedEvidencePreviewText(
            anchor(
                section = "water storage",
                snippet = "",
            )
        )

        assertEquals("Section: water storage", previewText)
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
