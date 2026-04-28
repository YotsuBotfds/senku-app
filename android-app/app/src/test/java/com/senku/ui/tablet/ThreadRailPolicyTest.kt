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
            "Q2 \u00B7 FIELD QUESTION",
            threadRailTurnMetaLabel(
                index = 2,
                guideMode = false,
                status = Status.Done,
                active = true,
                sourceCount = 3,
            ),
        )
        assertEquals("Q2 \u00B7 ACTIVE", threadRailTurnMetaLabel(2, guideMode = false, status = Status.Done, active = true))
        assertEquals("A2 \u00B7 3 SOURCES", threadRailAnswerMetaLabel(2, guideMode = false, sourceCount = 3))
        assertEquals("REF 2 \u00B7 NO SOURCES", threadRailAnswerMetaLabel(2, guideMode = true, sourceCount = 0))
        assertEquals("NO SOURCES", threadRailTurnSourceLabel(sourceCount = 0))
        assertEquals("1 SOURCE", threadRailTurnSourceLabel(sourceCount = 1))
        assertEquals("3 SOURCES", threadRailTurnSourceLabel(sourceCount = 3))
    }

    @Test
    fun threadRailQuestionAnswerLabelsStayTranscriptShapedWithSourceContext() {
        assertEquals(
            "Q1 \u00B7 FIELD QUESTION",
            threadRailTurnMetaLabel(
                index = 1,
                guideMode = false,
                status = Status.Active,
                active = true,
                sourceCount = 2,
            ),
        )
        assertEquals("A1 \u00B7 2 SOURCES", threadRailAnswerMetaLabel(1, guideMode = false, sourceCount = 2))
    }

    @Test
    fun threadRailSourceLabelsPrioritizeRainShelterContextOverAbrasivesAnchorRole() {
        val rainShelter = SourceState("rain", "GD-345", "Rain shelter in wet weather", isAnchor = false, isSelected = false)
        val abrasivesAnchor = SourceState("abrasives", "GD-220", "Abrasives Manufacturing", isAnchor = true, isSelected = true)

        assertEquals(3, threadRailSourceContextPriority(rainShelter))
        assertEquals(0, threadRailSourceContextPriority(abrasivesAnchor))
        assertEquals("GD-345 - RAIN SHELTER", threadRailSourceDisplayLabel(rainShelter, guideMode = true))
        assertEquals("GD-220 - ANCHOR", threadRailSourceDisplayLabel(abrasivesAnchor, guideMode = true))
        assertEquals(
            listOf(rainShelter, abrasivesAnchor),
            listOf(abrasivesAnchor, rainShelter).sortedByDescending { threadRailSourceContextPriority(it) },
        )
    }

    @Test
    fun threadRailOmitsClearedSourceFallbackRows() {
        val clearedSource = SourceState(
            key = "field-note",
            id = "",
            title = "Field note summary",
            isAnchor = true,
            isSelected = true,
        )

        assertEquals("", threadRailSourceDisplayLabel(clearedSource, guideMode = false))
        assertEquals("", threadRailSourceDisplayLabel(clearedSource, guideMode = true))
        assertEquals("", threadRailSourceTitleLabel(clearedSource, guideMode = true))
        assertEquals(false, threadRailShouldShowSource(clearedSource, guideMode = true))
    }

    @Test
    fun threadRailUsesRealGuideIdsOrMeaningfulSourceLabels() {
        val sourceWithoutId = SourceState(
            key = "support",
            id = "",
            title = "Supporting note",
            isAnchor = true,
            isSelected = true,
        )
        val sourceWithId = SourceState(
            key = "guide",
            id = "GD-132",
            title = "Foundry & Metal Casting",
            isAnchor = true,
            isSelected = false,
        )

        assertEquals("Supporting note", threadRailSourceDisplayLabel(sourceWithoutId, guideMode = true))
        assertEquals("GD-132 - ANCHOR", threadRailSourceDisplayLabel(sourceWithId, guideMode = true))
        assertEquals(true, threadRailShouldShowSource(sourceWithoutId, guideMode = true))
    }

    @Test
    fun threadRailRowsReserveTranscriptScanningSpace() {
        assertEquals(60, threadRailTurnRowMinHeightDp(active = false))
        assertEquals(68, threadRailTurnRowMinHeightDp(active = true))
        assertEquals(58, threadRailSourceRowMinHeightDp())
    }
}
