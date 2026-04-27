package com.senku.ui.tablet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TabletEvidenceVisibilityPolicyTest {
    @Test
    fun tabletLandscapeEvidencePaneKeepsUsefulProofWidth() {
        val policy = tabletEvidenceVisibilityPolicy()

        assertEquals(360, policy.evidencePaneWidthDp)
        assertTrue(policy.activeSnippetMaxLines >= 8)
    }

    @Test
    fun tabletPortraitCollapsedEvidencePreviewKeepsContextVisible() {
        val policy = tabletEvidenceVisibilityPolicy()

        assertTrue(policy.collapsedTitleMaxLines >= 2)
        assertTrue(policy.collapsedSnippetMaxLines >= 4)
    }
}
