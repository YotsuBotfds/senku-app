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

        assertEquals(tabletLandscapeReadingLayoutPolicy().evidenceRailWidthDp, policy.evidencePaneWidthDp)
        assertEquals(EvidenceRailDensity.Full, policy.landscapeRailDensity)
        assertTrue(policy.activeSnippetMaxLines >= 6)
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
    fun crossReferenceCardCountCountsLinkedGuidesBesideActiveAnchor() {
        val count = buildCrossReferenceCardCount(
            anchor(section = "water storage", snippet = ""),
            listOf(
                XRefState(id = "GD-215", title = "Rainwater Catchment"),
                XRefState(id = "GD-216", title = "Clay Filter"),
            ),
        )

        assertEquals(2, count)
    }

    @Test
    fun crossReferenceCardCountDropsDuplicateActiveAnchorXRef() {
        val count = buildCrossReferenceCardCount(
            anchor(section = "water storage", snippet = ""),
            listOf(
                XRefState(id = "GD-214", title = "Water purification and storage"),
                XRefState(id = "GD-215", title = "Rainwater Catchment"),
            ),
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
    fun answerModeSourceHeaderCountUsesThreadSourceFloorWhenGraphRowsAreEmpty() {
        val count = buildAnswerModeSourceHeaderCount(
            anchor = AnchorState(
                key = "",
                id = "",
                title = "",
                section = "",
                snippet = "",
                hasSource = false,
            ),
            xrefs = emptyList(),
            answerSourceCount = 2,
        )

        assertEquals(2, count)
    }

    @Test
    fun answerModeSourceHeaderCountStillCountsGraphRowsWhenTheyExceedThreadSources() {
        val count = buildAnswerModeSourceHeaderCount(
            anchor = anchor(section = "water storage", snippet = ""),
            xrefs = listOf(
                XRefState(id = "GD-215", title = "Rainwater Catchment"),
                XRefState(id = "GD-216", title = "Clay Filter"),
            ),
            answerSourceCount = 1,
        )

        assertEquals(3, count)
    }

    @Test
    fun tabletGuideModeUsesGuideSectionCountForRailHeader() {
        val state = stateWithSources(
            sourceCount = 1,
            isLandscape = true,
            detailMode = TabletDetailMode.Guide,
            guideSectionCount = 17,
        )

        assertEquals(17, state.resolvedGuideSectionCount())
        assertEquals("SECTIONS · 17", threadRailSectionTitle("SECTIONS", state.resolvedGuideSectionCount()))
    }

    @Test
    fun tabletXRefsCarryRequiredRailRelation() {
        val xref = XRefState(id = "GD-499", title = "Bellows", relation = "REQUIRED")

        assertEquals("REQUIRED", xref.relation)
    }

    @Test
    fun tabletGuideDestinationRailUsesLoadedCrossReferencesForLeftRailParity() {
        val state = stateWithSources(
            sourceCount = 1,
            isLandscape = true,
            detailMode = TabletDetailMode.Guide,
            guideId = "GD-132",
            guideTitle = "Foundry & Metal Casting",
            guideSectionCount = 1,
            anchor = AnchorState(
                key = "gd-220",
                id = "GD-220",
                title = "Abrasives Manufacturing",
                section = "",
                snippet = "",
                hasSource = true,
            ),
            xrefs = listOf(
                XRefState(id = "GD-220", title = "Abrasives Manufacturing"),
                XRefState(id = "GD-225", title = "Bloomery Furnace", relation = "REQUIRED"),
                XRefState(id = "GD-499", title = "Bellows Forge Blower Construction", relation = "REQUIRED"),
                XRefState(id = "GD-301", title = "Charcoal Production"),
                XRefState(id = "GD-302", title = "Clay Furnace Lining"),
                XRefState(id = "GD-303", title = "Ore Sorting"),
                XRefState(id = "GD-304", title = "Quench Water Station"),
            ),
        )

        val railSources = state.resolvedThreadRailSources()

        assertEquals(17, state.resolvedGuideSectionCount())
        assertEquals(6, buildCrossReferenceCardCount(tabletSourceGraphAnchor(state.anchor), state.xrefs))
        assertEquals(6, railSources.size)
        assertEquals("CROSS-REFERENCE \u00B7 6", threadRailSectionTitle("CROSS-REFERENCE", railSources.size))
        assertEquals(listOf("GD-225", "GD-499", "GD-301", "GD-302", "GD-303", "GD-304"), railSources.map { it.id })
        assertEquals("GD-220", state.anchor.id)
    }

    @Test
    fun tabletGuideDestinationRailKeepsCurrentGuideSourceUntilCrossReferencesLoad() {
        val state = stateWithSources(
            sourceCount = 1,
            isLandscape = true,
            detailMode = TabletDetailMode.Guide,
            guideId = "GD-132",
            guideTitle = "Foundry & Metal Casting",
            xrefs = emptyList(),
        )

        val railSources = state.resolvedThreadRailSources()

        assertEquals(1, railSources.size)
        assertEquals("GD-1", railSources.first().id)
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
            detailMode = TabletDetailMode.Guide,
        )

        assertFalse(state.hasAnswerOwnedSourceSelection())
        assertTrue(state.isGuideMode())
        assertTrue(tabletShouldShowEvidencePane(state, guideMode = state.isGuideMode()))
    }

    @Test
    fun tabletDetailModeLabelsSeparateAnswerThreadAndGuideChrome() {
        assertEquals("ANSWER", tabletTitleBarModeLabel(TabletDetailMode.Answer))
        assertEquals("THREAD", tabletTitleBarModeLabel(TabletDetailMode.Thread))
        assertEquals("GUIDE", tabletTitleBarModeLabel(TabletDetailMode.Guide))
    }

    @Test
    fun tabletThreadModeKeepsEvidenceSupportWithoutBecomingGuideReader() {
        val state = stateWithSources(
            sourceCount = 2,
            isLandscape = true,
            detailMode = TabletDetailMode.Thread,
        )

        assertTrue(state.isThreadMode())
        assertFalse(state.isGuideMode())
        assertTrue(state.isAnswerOrThreadMode())
        assertTrue(tabletShouldShowEvidencePane(state, guideMode = state.isGuideMode()))
    }

    @Test
    fun tabletSourceGraphDropsClearedSourceFallbackAnchor() {
        val anchor = AnchorState(
            key = "field-note",
            id = "",
            title = "Field note summary",
            section = "Supporting note",
            snippet = "",
            hasSource = true,
        )

        val graphAnchor = tabletSourceGraphAnchor(anchor)

        assertFalse(graphAnchor.hasSource)
        assertEquals("", graphAnchor.id)
        assertEquals("", graphAnchor.title)
        assertEquals("", graphAnchor.section)
    }

    @Test
    fun tabletSourceGraphKeepsOnlyRealGuideXRefs() {
        val xrefs = tabletSourceGraphXRefs(
            listOf(
                XRefState(id = "", title = "Field note summary"),
                XRefState(id = "GD-132", title = "Foundry & Metal Casting"),
            )
        )

        assertEquals(listOf(XRefState(id = "GD-132", title = "Foundry & Metal Casting")), xrefs)
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
        guideId: String = "GD-214",
        guideTitle: String = "Water purification and storage",
        composerVisible: Boolean = true,
        guideModeLabel: String = "",
        guideModeSummary: String = "",
        guideModeAnchorLabel: String = "",
        selectedSourceIndex: Int = 1,
        showQuestion: Boolean = true,
        detailMode: TabletDetailMode = TabletDetailMode.Answer,
        guideSectionCount: Int = 0,
        anchor: AnchorState? = null,
        xrefs: List<XRefState> = emptyList(),
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
            guideId = guideId,
            guideTitle = guideTitle,
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
            anchor = anchor ?: if (sources.isEmpty()) {
                AnchorState("", "", "", "", "", false)
            } else {
                anchor(section = "water storage", snippet = "Keep treated water sealed between uses.")
            },
            xrefs = xrefs,
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
            guideSectionCount = guideSectionCount,
            detailMode = detailMode,
        )
    }
}
