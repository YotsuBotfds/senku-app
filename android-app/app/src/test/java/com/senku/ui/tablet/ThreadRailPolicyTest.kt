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
    fun threadRailTurnLabelsUseTranscriptQuestionIds() {
        assertEquals("Q2", threadRailTurnLabel(index = 2, guideMode = false))
        assertEquals("SEC 2", threadRailTurnLabel(index = 2, guideMode = true))
        assertEquals("A2", threadRailAnswerLabel(index = 2, guideMode = false))
        assertEquals("REF 2", threadRailAnswerLabel(index = 2, guideMode = true))
    }

    @Test
    fun threadRailTurnMetaLabelsStayTurnFirst() {
        assertEquals(
            "Q2",
            threadRailTurnMetaLabel(
                index = 2,
                guideMode = false,
                status = Status.Done,
                active = true,
                sourceCount = 3,
            ),
        )
        assertEquals("Q2 \u00B7 ACTIVE", threadRailTurnMetaLabel(2, guideMode = false, status = Status.Done, active = true))
        assertEquals("A2 \u00B7 3 SRC", threadRailAnswerMetaLabel(2, guideMode = false, sourceCount = 3))
        assertEquals("REF 2 \u00B7 NO SRC", threadRailAnswerMetaLabel(2, guideMode = true, sourceCount = 0))
        assertEquals("NO SRC", threadRailTurnSourceLabel(sourceCount = 0))
        assertEquals("1 SRC", threadRailTurnSourceLabel(sourceCount = 1))
    }

    @Test
    fun threadRailRowsReserveTranscriptScanningSpace() {
        assertEquals(82, threadRailTurnRowMinHeightDp(active = false))
        assertEquals(92, threadRailTurnRowMinHeightDp(active = true))
        assertEquals(50, threadRailSourceRowMinHeightDp())
    }
}
