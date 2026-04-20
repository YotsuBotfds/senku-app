package com.senku.ui.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchResultCardHeuristicsTest {
    @Test
    fun laneLabelForRetrievalMode_mapsKnownValues() {
        assertEquals("Hybrid", laneLabelForRetrievalMode("hybrid"))
        assertEquals("Vector", laneLabelForRetrievalMode("vector"))
        assertEquals("Lexical", laneLabelForRetrievalMode("lexical"))
        assertEquals("Cross-ref", laneLabelForRetrievalMode("guide-focus"))
        assertEquals("Cross-ref", laneLabelForRetrievalMode("guide"))
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
