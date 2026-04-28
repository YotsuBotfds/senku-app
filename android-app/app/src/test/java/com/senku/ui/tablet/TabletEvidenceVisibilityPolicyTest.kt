package com.senku.ui.tablet

import com.senku.ui.answer.AnswerContent
import com.senku.ui.answer.Evidence
import com.senku.ui.primitives.MetaItem
import com.senku.ui.primitives.Tone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TabletEvidenceVisibilityPolicyTest {
    @Test
    fun tabletLandscapeEvidencePaneKeepsUsefulSourceEvidenceWidth() {
        val policy = tabletEvidenceVisibilityPolicy()

        assertEquals(320, policy.evidencePaneWidthDp)
        assertEquals(EvidenceRailDensity.Full, policy.landscapeRailDensity)
        assertTrue(policy.activeSnippetMaxLines >= 8)
    }

    @Test
    fun tabletPortraitCollapsedEvidencePreviewKeepsContextVisible() {
        val policy = tabletEvidenceVisibilityPolicy()

        assertTrue(policy.portraitCollapsedByDefault)
        assertTrue(policy.collapsedTitleMaxLines >= 2)
        assertTrue(policy.collapsedSnippetMaxLines >= 4)
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

    @Test
    fun crossReferenceCardCountIsEmptyWithoutAnchorOrRelatedGuides() {
        val count = buildCrossReferenceCardCount(
            AnchorState(
                key = "",
                id = "",
                title = "",
                section = "",
                snippet = "",
                hasSource = false,
            ),
            emptyList(),
        )

        assertEquals(0, count)
    }

    @Test
    fun tabletAnswerModeShowsSourceRailWhenSourcesExist() {
        assertTrue(
            tabletShouldShowEvidencePane(
                state = stateWithSources(sourceCount = 1, isLandscape = false),
                guideMode = false,
            )
        )
    }

    @Test
    fun tabletAnswerModeKeepsEvidenceRailHiddenWithoutSourcesUnlessExpanded() {
        assertFalse(
            tabletShouldShowEvidencePane(
                state = stateWithSources(sourceCount = 0, isLandscape = true),
                guideMode = false,
            )
        )
        assertTrue(
            tabletShouldShowEvidencePane(
                state = stateWithSources(sourceCount = 0, evidenceExpanded = true, isLandscape = false),
                guideMode = false,
            )
        )
    }

    @Test
    fun tabletGuideModeKeepsPortraitReaderSingleColumnAndLandscapeEvidenceRail() {
        assertFalse(
            tabletShouldShowEvidencePane(
                state = stateWithSources(sourceCount = 1, isLandscape = false, composerVisible = false),
                guideMode = true,
            )
        )
        assertTrue(
            tabletShouldShowEvidencePane(
                state = stateWithSources(sourceCount = 1, isLandscape = true, composerVisible = false),
                guideMode = true,
            )
        )
    }

    @Test
    fun tabletAnswerModeWithSelectedSourceLabelsStaysAnswerArticleWithSourceRail() {
        val state = stateWithSources(
            sourceCount = 2,
            isLandscape = true,
            guideModeLabel = "SOURCE",
            guideModeSummary = "Selected source: GD-2",
            guideModeAnchorLabel = "Opened from answer source",
            selectedSourceIndex = 2,
        )

        assertFalse(state.isGuideMode())
        assertTrue(state.hasAnswerOwnedSourceSelection())
        assertTrue(tabletShouldShowEvidencePane(state, guideMode = state.isGuideMode()))
    }

    @Test
    fun tabletDirectAnswerSourceSelectionStaysAnswerOwnedWhenComposerHidden() {
        val state = stateWithSources(
            sourceCount = 2,
            isLandscape = true,
            composerVisible = false,
            guideModeLabel = "GUIDE",
            guideModeSummary = "Selected source: GD-132",
            guideModeAnchorLabel = "Opened from answer source",
            selectedSourceIndex = 2,
        )

        assertTrue(state.hasAnswerOwnedSourceSelection())
        assertFalse(state.isGuideMode())
        assertTrue(tabletShouldShowEvidencePane(state, guideMode = state.isGuideMode()))
    }

    @Test
    fun tabletGuideReaderStillUsesGuideModeWhenComposerIsNotAnswerMode() {
        val state = stateWithSources(
            sourceCount = 1,
            isLandscape = true,
            composerVisible = false,
            guideModeLabel = "GUIDE",
            guideModeSummary = "Guide reader",
            guideModeAnchorLabel = "Opened from GD-214",
            showQuestion = false,
        )

        assertFalse(state.hasAnswerOwnedSourceSelection())
        assertTrue(state.isGuideMode())
        assertTrue(tabletShouldShowEvidencePane(state, guideMode = state.isGuideMode()))
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

    private fun stateWithSources(
        sourceCount: Int,
        evidenceExpanded: Boolean = false,
        isLandscape: Boolean,
        composerVisible: Boolean = true,
        guideModeLabel: String = "",
        guideModeSummary: String = "",
        guideModeAnchorLabel: String = "",
        selectedSourceIndex: Int = 1,
        showQuestion: Boolean = true,
    ): TabletDetailState {
        val sources = (1..sourceCount).map { index ->
            SourceState(
                key = "source-$index",
                id = "GD-$index",
                title = "Source $index",
                isAnchor = index == 1,
                isSelected = index == selectedSourceIndex,
            )
        }
        return TabletDetailState(
            guideId = "GD-214",
            guideTitle = "Water purification and storage",
            meta = listOf(MetaItem("answer", Tone.Accent)),
            turns = listOf(
                ThreadTurnState(
                    id = "T1",
                    question = "How do I store water?",
                    answer = AnswerContent(
                        short = "Keep treated water sealed between uses.",
                        sourceCount = sourceCount,
                        host = "On-device",
                        elapsedSeconds = 0.8,
                        evidence = Evidence.Moderate,
                    ),
                    status = Status.Active,
                    isActive = true,
                    showQuestion = showQuestion,
                )
            ),
            sources = sources,
            anchor = if (sources.isEmpty()) {
                AnchorState("", "", "", "", "", false)
            } else {
                anchor(section = "water storage", snippet = "Keep treated water sealed between uses.")
            },
            xrefs = emptyList(),
            composerText = "",
            composerPlaceholder = "",
            composerEnabled = true,
            composerVisible = composerVisible,
            composerShowRetry = false,
            composerRetryLabel = "Retry",
            pinVisible = false,
            pinActive = false,
            evidenceExpanded = evidenceExpanded,
            isLandscape = isLandscape,
            guideModeLabel = guideModeLabel,
            guideModeSummary = guideModeSummary,
            guideModeAnchorLabel = guideModeAnchorLabel,
        )
    }
}
