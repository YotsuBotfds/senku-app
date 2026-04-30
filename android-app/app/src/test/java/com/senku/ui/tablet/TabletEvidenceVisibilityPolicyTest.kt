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
    fun answerModeSourceRowsUseRealRainShelterGraphByDefault() {
        val anchor = AnchorState(
            key = "gd-345",
            id = "GD-345",
            title = "Tarp & Cord Shelters",
            section = "",
            snippet = "",
            hasSource = true,
        )
        val xrefs = listOf(
            XRefState(id = "GD-294", title = "Cave Shelter Systems & Cold-Weather"),
            XRefState(id = "GD-220", title = "Abrasives Manufacturing"),
            XRefState(id = "GD-132", title = "Foundry & Metal Casting"),
            XRefState(id = "GD-695", title = "Hurricane & Severe Storm Sheltering"),
        )

        val rows = tabletAnswerModeSourceRows(anchor, xrefs)

        assertEquals(
            listOf(
                TabletEvidenceCardRow(
                    guideId = "GD-345",
                    relation = "ANCHOR",
                    title = "Tarp & Cord Shelters",
                ),
                TabletEvidenceCardRow(
                    guideId = "GD-294",
                    relation = "RELATED",
                    title = "Cave Shelter Systems & Cold-Weather",
                ),
                TabletEvidenceCardRow(
                    guideId = "GD-220",
                    relation = "RELATED",
                    title = "Abrasives Manufacturing",
                ),
                TabletEvidenceCardRow(
                    guideId = "GD-132",
                    relation = "RELATED",
                    title = "Foundry & Metal Casting",
                ),
                TabletEvidenceCardRow(
                    guideId = "GD-695",
                    relation = "RELATED",
                    title = "Hurricane & Severe Storm Sheltering",
                ),
            ),
            rows,
        )
        assertEquals(7, buildAnswerModeSourceHeaderCount(anchor, xrefs, answerSourceCount = 7))
    }

    @Test
    fun answerModeSourceRowsDoNotForceCanonicalRainShelterRailFromGd345AnchorByDefault() {
        val anchor = AnchorState(
            key = "gd-345",
            id = "GD-345",
            title = "Rain shelter",
            section = "",
            snippet = "Build a ridgeline first, then drape and tension the tarp.",
            hasSource = true,
        )
        val xrefs = listOf(
            XRefState(id = "GD-294", title = "Cave Shelter Systems & Cold-Weather"),
            XRefState(id = "GD-618", title = "Seasonal Shelter Adaptation"),
            XRefState(id = "GD-446", title = "Shelter Site Selection"),
            XRefState(id = "GD-695", title = "Hurricane & Severe Storm Sheltering"),
            XRefState(id = "GD-484", title = "Insulation Materials & Thermal Design"),
            XRefState(id = "GD-109", title = "Natural Building Materials"),
        )

        val rows = tabletAnswerModeSourceRows(anchor, xrefs)

        assertEquals(listOf("GD-345", "GD-294", "GD-618", "GD-446", "GD-695", "GD-484", "GD-109"), rows.map { it.guideId })
        assertEquals(List(7) { "" }, rows.map { it.match })
        assertEquals(7, buildAnswerModeSourceHeaderCount(anchor, xrefs, answerSourceCount = 7))
    }

    @Test
    fun answerModeSourceRowsCollapseRainShelterStackOnlyWhenReviewDemoEnabled() {
        val anchor = AnchorState(
            key = "gd-345",
            id = "GD-345",
            title = "Tarp & Cord Shelters",
            section = "",
            snippet = "",
            hasSource = true,
        )
        val xrefs = listOf(
            XRefState(id = "GD-294", title = "Cave Shelter Systems & Cold-Weather"),
            XRefState(id = "GD-220", title = "Abrasives Manufacturing"),
            XRefState(id = "GD-132", title = "Foundry & Metal Casting"),
            XRefState(id = "GD-695", title = "Hurricane & Severe Storm Sheltering"),
        )

        val rows = tabletAnswerModeSourceRows(
            anchor = anchor,
            xrefs = xrefs,
            reviewDemoEvidenceStackEnabled = true,
        )

        assertEquals(listOf("GD-220", "GD-132", "GD-345"), rows.map { it.guideId })
        assertEquals(listOf("74%", "68%", "61%"), rows.map { it.match })
        assertEquals(
            3,
            buildAnswerModeSourceHeaderCount(
                anchor = anchor,
                xrefs = xrefs,
                answerSourceCount = 7,
                reviewDemoEvidenceStackEnabled = true,
            ),
        )
    }

    @Test
    fun guideModeReferenceRowsPromoteAbrasivesAnchorForFoundryGuideContext() {
        val rows = tabletGuideModeReferenceRows(
            anchor = AnchorState("", "", "", "", "", false),
            xrefs = listOf(
                XRefState(id = "GD-132", title = "Foundry & Metal Casting", relation = "ANCHOR"),
                XRefState(id = "GD-220", title = "Abrasives Manufacturing"),
                XRefState(id = "GD-225", title = "Bloomery Furnace", relation = "REQUIRED"),
                XRefState(id = "GD-499", title = "Bellows Forge Blower Construction", relation = "REQUIRED"),
            ),
        )

        assertEquals(listOf("GD-220", "GD-132", "GD-225", "GD-499"), rows.map { it.guideId })
        assertEquals(listOf("ANCHOR", "RELATED", "REQUIRED", "REQUIRED"), rows.map { it.relation })
    }

    @Test
    fun answerModeSourceRailTitleUsesCanonicalBulletSeparator() {
        assertEquals("SOURCES \u2022 3", answerModeSourceSectionTitle(3))
        assertEquals("SOURCES \u2022 0", answerModeSourceSectionTitle(-7))
    }

    @Test
    fun answerModeSourceRailHeaderExposesExpandAffordance() {
        assertEquals("TAP TO EXPAND", answerModeSourceHeaderAffordance())
    }

    @Test
    fun answerModeSourceCardsAllowTwoLineSnippetsAndRoomierCompactPadding() {
        assertEquals(2, answerModeSourceSnippetMaxLines())
        assertTrue(compactEvidenceCardVerticalPaddingDp() > 8)
    }

    @Test
    fun answerModeSourceRowsKeepReviewDemoMatchScoresSeparateFromReferenceRelation() {
        val rows = tabletAnswerModeSourceRows(
            anchor = AnchorState(
                key = "gd-345",
                id = "GD-345",
                title = "Rain shelter",
                section = "",
                snippet = "Build a ridgeline first, then drape and tension the tarp.",
                hasSource = true,
            ),
            xrefs = emptyList(),
            reviewDemoEvidenceStackEnabled = true,
        )

        assertEquals(listOf("74%", "68%", "61%"), rows.map { it.match })
        assertEquals(listOf("ANCHOR", "RELATED", "TOPIC"), rows.map { it.relation })
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
        assertEquals("SECTIONS \u2022 17", threadRailSectionTitle("SECTIONS", state.resolvedGuideSectionCount()))
    }

    @Test
    fun tabletGuideModeDoesNotInferFoundrySectionCountOutsideReviewDemo() {
        val state = stateWithSources(
            sourceCount = 1,
            isLandscape = true,
            detailMode = TabletDetailMode.Guide,
            guideId = "GD-132",
            guideTitle = "Foundry & Metal Casting",
            guideSectionCount = 0,
        )

        assertEquals(1, state.resolvedGuideSectionCount())
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

        assertEquals(1, state.resolvedGuideSectionCount())
        assertEquals(6, buildCrossReferenceCardCount(tabletSourceGraphAnchor(state.anchor), state.xrefs))
        assertEquals(6, railSources.size)
        assertEquals("CROSS-REFERENCE \u2022 6", threadRailSectionTitle("CROSS-REFERENCE", railSources.size))
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
    fun tabletAnswerModeShowsPortraitSourceRailWhenSourcesExist() {
        val state = stateWithSources(sourceCount = 3, isLandscape = false)

        assertTrue(
            tabletShouldShowEvidencePane(
                state = state,
                guideMode = false,
            )
        )
        assertEquals(3, state.resolvedAnswerSourceCount())
        assertEquals("SOURCES \u2022 3", answerModeSourceSectionTitle(state.resolvedAnswerSourceCount()))
    }

    @Test
    fun tabletAnswerModeKeepsEvidenceRailHiddenWithoutSourcesUntilExpanded() {
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
            evidenceExpanded = true,
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
            evidenceExpanded = true,
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
            guideId = "GD-220",
            guideTitle = "Rain shelter",
            sourceIds = listOf("GD-220", "GD-345"),
            detailMode = TabletDetailMode.Thread,
        )

        assertTrue(state.isThreadMode())
        assertFalse(state.isGuideMode())
        assertTrue(state.isAnswerOrThreadMode())
        assertTrue(tabletShouldShowEvidencePane(state, guideMode = state.isGuideMode()))
    }

    @Test
    fun tabletPortraitThreadModeShowsSourcePaneWhenThreadSourcesExist() {
        val state = stateWithSources(
            sourceCount = 2,
            isLandscape = false,
            guideId = "GD-220",
            guideTitle = "Rain shelter",
            sourceIds = listOf("GD-220", "GD-345"),
            detailMode = TabletDetailMode.Thread,
        )

        assertTrue(tabletShouldShowEvidencePane(state, guideMode = state.isGuideMode()))
        assertEquals(listOf("GD-220", "GD-345"), state.resolvedVisibleThreadSourceRows().map { it.id })
        assertEquals("SOURCES \u2022 2", tabletThreadSourcePaneTitle(2, isLandscape = false))
        assertEquals(240, tabletThreadEvidenceRailWidthDp(isLandscape = false))
        assertEquals(360, tabletThreadEvidenceRailWidthDp(isLandscape = true))
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
        sourceIds: List<String>? = null,
        showQuestion: Boolean = true,
        detailMode: TabletDetailMode = TabletDetailMode.Answer,
        guideSectionCount: Int = 0,
        anchor: AnchorState? = null,
        xrefs: List<XRefState> = emptyList(),
    ): TabletDetailState {
        val resolvedSourceIds = sourceIds ?: (1..sourceCount).map { index -> "GD-$index" }
        val sources = resolvedSourceIds.take(sourceCount).mapIndexed { sourceIndex, sourceId ->
            val index = sourceIndex + 1
            SourceState(
                key = "source-$index",
                id = sourceId,
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
