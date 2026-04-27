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
        assertEquals("Open related guide", relatedGuideContentDescription())
        assertEquals(
            "Open related guide: GD-214 - Boiling water",
            relatedGuideContentDescription(" GD-214 - Boiling water "),
        )
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
