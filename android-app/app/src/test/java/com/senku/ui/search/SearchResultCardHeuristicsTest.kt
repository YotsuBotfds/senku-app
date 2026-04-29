package com.senku.ui.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchResultCardHeuristicsTest {
    @Test
    fun laneLabelForRetrievalMode_mapsKnownValues() {
        assertEquals("Best match", laneLabelForRetrievalMode("hybrid"))
        assertEquals("Best match", laneLabelForRetrievalMode("route-focus"))
        assertEquals("Best match", laneLabelForRetrievalMode(""))
        assertEquals("Concept match", laneLabelForRetrievalMode("vector"))
        assertEquals("Keyword match", laneLabelForRetrievalMode("lexical"))
        assertEquals("Related guide", laneLabelForRetrievalMode("guide-focus"))
        assertEquals("Related guide", laneLabelForRetrievalMode("guide"))
        assertEquals("Best match", laneLabelForRetrievalMode(" HYBRID "))
        assertEquals("Best match", laneLabelForRetrievalMode("cross-ref"))
    }

    @Test
    fun continueConversationContentDescription_readsAsAskCapability() {
        assertEquals(
            "Continue conversation about this result",
            continueConversationContentDescription(),
        )
        assertEquals(
            "Continue conversation about GD-214",
            continueConversationContentDescription(" GD-214 "),
        )
    }

    @Test
    fun relatedGuideContentDescription_readsAsLibraryCapability() {
        assertEquals("Open cross-reference guide", relatedGuideContentDescription())
        assertEquals(
            "Open cross-reference guide: GD-214 - Boiling water",
            relatedGuideContentDescription(" GD-214 - Boiling water "),
        )
    }

    @Test
    fun honestRankLabel_neverImpersonatesMatchPercentages() {
        assertEquals("#1", honestRankLabel(""))
        assertEquals("#3", honestRankLabel(" #3 "))
        assertFalse(honestRankLabel("#2").contains("%"))
    }

    @Test
    fun metadataLineForSearchResultCard_surfacesOnlyHonestMetadata() {
        assertEquals(
            "Role: Safety // Window: Immediate // Category: Water",
            metadataLineForSearchResultCard("Safety", "Immediate", "Water"),
        )
        assertEquals(
            "Category: Fire",
            metadataLineForSearchResultCard("General", "unknown", "Fire"),
        )
        assertEquals(
            "",
            metadataLineForSearchResultCard(" general ", " none ", " unknown "),
        )
        assertEquals(
            "Window: Long Term",
            metadataLineForSearchResultCard("none", "Long Term", "general"),
        )
    }

    @Test
    fun compactSearchResultMetadataLabel_keepsDenseRowTokens() {
        assertEquals(
            "WATER \u00B7 SAFETY \u00B7 WINDOW IMMEDIATE",
            compactSearchResultMetadataLabel("Role: Safety // Window: Immediate // Category: Water"),
        )
        assertEquals(
            "FIRE",
            compactSearchResultMetadataLabel("Category: Fire"),
        )
        assertEquals(
            "LEGACY META",
            compactSearchResultMetadataLabel("legacy meta"),
        )
        assertEquals(
            "SHELTER \u00B7 TOPIC \u00B7 WINDOW LONG",
            compactSearchResultMetadataLabel("Role: Topic // Window: Long Term // Category: Shelter"),
        )
    }

    @Test
    fun compactResultPreviewText_stripsMarkdownAndSectionEchoes() {
        assertEquals(
            "Shelter Building: Protection from the Elements Day signaling.",
            compactResultPreviewText(
                "Shelter Building",
                "## Shelter Building\nShelter Building: Protection from the Elements Day signaling.",
            ),
        )
        assertEquals(
            "Fire Management: Best tinder in survival situations.",
            compactResultPreviewText(
                "Fire Management",
                "Best tinder in survival situations.",
            ),
        )
        assertEquals(
            "Safety Notes: Keep the tarp ridge clear of pooled water.",
            compactResultPreviewText(
                "Safety Notes",
                "- [ ] **Safety Notes**: Keep the `tarp` ridge clear of pooled water.",
            ),
        )
    }

    @Test
    fun compactResultPreviewText_avoidsRepeatingGuideIdsInPreviewLead() {
        assertEquals(
            "Tarp & Cord Shelters: Keep the ridgeline taut and shed runoff away from the sleeping area.",
            compactResultPreviewText(
                "Tarp & Cord Shelters",
                "Guide: GD-345 Tarp & Cord Shelters: Keep the ridgeline taut and shed runoff away from the sleeping area.",
            ),
        )
    }

    @Test
    fun compactResultPreviewText_preservesFlatRainShelterQueryText() {
        assertEquals(
            "rain shelter preview: choose overhead cover and shed runoff away.",
            compactResultPreviewText(
                "GD-023 \u00B7 Survival",
                "rain shelter preview: choose overhead cover and shed runoff away.",
            ),
        )
    }

    @Test
    fun scoreTickFillFraction_tracksRankScoreWithoutPercentCopy() {
        assertEquals(0.94f, scoreTickFillFraction("92"), 0.001f)
        assertEquals(0.82f, scoreTickFillFraction("78"), 0.001f)
        assertEquals(0.74f, scoreTickFillFraction("74"), 0.001f)
        assertEquals(0.62f, scoreTickFillFraction("61"), 0.001f)
        assertEquals(0.52f, scoreTickFillFraction("49"), 0.001f)
        assertEquals(0.72f, scoreTickFillFraction("#1"), 0.001f)
    }

    @Test
    fun scoreTickTrackWidthDp_keepsStableCompactTrack() {
        assertEquals(18, scoreTickTrackWidthDp("92"))
        assertEquals(18, scoreTickTrackWidthDp("78"))
        assertEquals(18, scoreTickTrackWidthDp("74"))
        assertEquals(18, scoreTickTrackWidthDp("61"))
        assertEquals(18, scoreTickTrackWidthDp("49"))
        assertEquals(18, scoreTickTrackWidthDp("#1"))
    }

    @Test
    fun warmThreadGuideIdsForPreview_onlyKeepsFreshThreads() {
        val now = 1_000_000L
        val freshIds = warmThreadGuideIdsForPreview(
            lastActivityEpoch = now - (10L * 60L * 1000L),
            sourceGuideIds = listOf("GD-214", " ", "gd-081"),
            nowEpochMs = now,
        )
        assertEquals(setOf("gd-214", "gd-081"), freshIds)

        val staleIds = warmThreadGuideIdsForPreview(
            lastActivityEpoch = now - (31L * 60L * 1000L),
            sourceGuideIds = listOf("GD-214"),
            nowEpochMs = now,
        )
        assertTrue(staleIds.isEmpty())
        assertFalse(isWarmConversation(now - (31L * 60L * 1000L), nowEpochMs = now))
        assertTrue(isWarmConversation(now - (15L * 60L * 1000L), nowEpochMs = now))
    }
}
