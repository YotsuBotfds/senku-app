package com.senku.ui.tablet

import org.junit.Assert.assertEquals
import org.junit.Test

class ThreadRailPolicyTest {
    @Test
    fun threadRailSectionTitlesMatchTranscriptMockLanguage() {
        assertEquals("TURNS \u00B7 2", threadRailSectionTitle("TURNS", 2))
        assertEquals("SOURCES IN THREAD \u00B7 2", threadRailSectionTitle("SOURCES IN THREAD", 2))
    }

    @Test
    fun threadRailRowsReserveTranscriptScanningSpace() {
        assertEquals(52, threadRailTurnRowMinHeightDp(active = false))
        assertEquals(66, threadRailTurnRowMinHeightDp(active = true))
        assertEquals(56, threadRailSourceRowMinHeightDp())
    }
}
