package com.senku.ui.tablet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StressReadingPolicyTest {
    @Test
    fun tabletLandscapeReadingPolicyKeepsAnswerFirstWidthBudget() {
        val policy = tabletLandscapeReadingLayoutPolicy()

        assertEquals(220, policy.threadRailWidthDp)
        assertEquals(620, policy.answerMaxWidthDp)
        assertEquals(260, policy.evidenceRailWidthDp)
        assertEquals(18, policy.answerHorizontalPaddingDp)
    }

    @Test
    fun tabletPortraitReadingPolicyUsesSqueezedPortraitBudget() {
        val policy = tabletPortraitReadingLayoutPolicy()

        assertEquals(132, policy.threadRailWidthDp)
        assertEquals(456, policy.answerMaxWidthDp)
        assertEquals(208, policy.evidenceRailWidthDp)
        assertEquals(12, policy.answerHorizontalPaddingDp)
    }

    @Test
    fun tabletReadingPolicySelectorUsesOrientationSpecificBudgets() {
        val portraitPolicy = tabletReadingLayoutPolicy(isLandscape = false)
        val landscapePolicy = tabletReadingLayoutPolicy(isLandscape = true)

        assertEquals(tabletPortraitReadingLayoutPolicy(), portraitPolicy)
        assertEquals(tabletLandscapeReadingLayoutPolicy(), landscapePolicy)
        assertEquals(132, portraitPolicy.threadRailWidthDp)
        assertEquals(456, portraitPolicy.answerMaxWidthDp)
        assertEquals(208, portraitPolicy.evidenceRailWidthDp)
        assertEquals(12, portraitPolicy.answerHorizontalPaddingDp)
        assertEquals(220, landscapePolicy.threadRailWidthDp)
        assertEquals(620, landscapePolicy.answerMaxWidthDp)
        assertEquals(260, landscapePolicy.evidenceRailWidthDp)
        assertEquals(18, landscapePolicy.answerHorizontalPaddingDp)
    }

    @Test
    fun tabletGuideThreadRailUsesSectionIndexWidthWithoutChangingAnswerRails() {
        assertEquals(220, tabletThreadRailWidthDp(isLandscape = true, guideMode = false))
        assertEquals(132, tabletThreadRailWidthDp(isLandscape = false, guideMode = false))
        assertEquals(216, tabletThreadRailWidthDp(isLandscape = true, guideMode = true))
        assertEquals(226, tabletThreadRailWidthDp(isLandscape = false, guideMode = true))
    }

    @Test
    fun tabletGuidePaperPolicyContainsPortraitReaderWithTrimmedLandscapeSheet() {
        assertEquals(520, tabletGuidePaperMaxWidthDp(isLandscape = true))
        assertEquals(440, tabletGuidePaperMaxWidthDp(isLandscape = false))
    }

    @Test
    fun tabletGuideNavigationLabelsUseSectionLanguage() {
        val labels = tabletGuideNavigationLabels()

        assertEquals("SECTIONS", labels.sectionLabel)
        assertEquals("CROSS-REFERENCE", labels.referenceLabel)
        assertEquals("No sections yet.", labels.emptySectionLabel)
        assertEquals("No cross-references yet.", labels.emptyReferenceLabel)
    }

    @Test
    fun tabletGuidePaperClassifiesCalloutsAndRequiredRowsForBoxedRendering() {
        assertEquals(
            TabletGuideBodyLineKind.Skip,
            tabletGuideBodyLineKindForTest("FIELD MANUAL \u00B7 REV 04-27 \u00B7 PK 2"),
        )
        assertEquals(
            TabletGuideBodyLineKind.Skip,
            tabletGuideBodyLineKindForTest("GD-132 \u00B7 17 SECTIONS"),
        )
        assertEquals(
            TabletGuideBodyLineKind.Danger,
            tabletGuideBodyLineKindForTest("DANGER \u00B7 EXTREME BURN HAZARD"),
        )
        assertEquals(
            TabletGuideBodyLineKind.Danger,
            tabletGuideBodyLineKindForTest("Every tool, mold, crucible, and surface that contacts molten metal must be completely dry."),
        )
        assertEquals(
            TabletGuideBodyLineKind.RequiredReading,
            tabletGuideBodyLineKindForTest("REQUIRED READING \u00B7 GD-220 \u00B7 Abrasives Manufacturing"),
        )
        assertEquals(
            TabletGuideBodyLineKind.Section,
            tabletGuideBodyLineKindForTest("\u2014 \u00A7 1 \u00B7 AREA READINESS"),
        )
        assertEquals(
            TabletGuideBodyLineKind.Body,
            tabletGuideBodyLineKindForTest("Confirm area readiness before lighting the work zone."),
        )
    }

    @Test
    fun tabletGuideRailExtractsSectionAnchorRowsBeforeCrossReferences() {
        val state = tabletDetailState(
            turns = listOf(
                threadTurn(
                    id = "guide",
                    answer = "\u2014 \u00A7 1 \u00B7 AREA READINESS\n" +
                        "Confirm the work zone is ready.\n" +
                        "REQUIRED READING \u00B7 GD-220 \u00B7 Abrasives Manufacturing\n" +
                        "\u2014 \u00A7 3 \u00B7 DRY TOOLS",
                )
            ),
            xrefs = listOf(
                XRefState(id = "GD-220", title = "Abrasives Manufacturing"),
                XRefState(id = "GD-499", title = "Bellows Forge Blower Construction"),
            ),
            detailMode = TabletDetailMode.Guide,
        )

        val railTurns = state.resolvedThreadRailTurns()
        val railSources = state.resolvedThreadRailSources()

        assertEquals(listOf("\u00A71 Area readiness", "\u00A72 Required reading", "\u00A73 Dry tools"), railTurns.map { it.question })
        assertEquals(listOf("GD-220", "GD-499"), railSources.map { it.id })
    }

    @Test
    fun tabletGuideEvidenceGraphCountsAnchorPlusVisibleCrossReferences() {
        val state = tabletDetailState(
            guideId = "GD-132",
            anchor = AnchorState(
                key = "gd-132",
                id = "GD-132",
                title = "Foundry & Metal Casting",
                section = "Area readiness",
                snippet = "",
                hasSource = true,
            ),
            xrefs = listOf(
                XRefState(id = "GD-220", title = "Abrasives Manufacturing"),
                XRefState(id = "GD-225", title = "Bloomery Furnace"),
                XRefState(id = "GD-301", title = "Charcoal Production"),
                XRefState(id = "GD-302", title = "Clay Furnace Lining"),
                XRefState(id = "GD-499", title = "Bellows Forge Blower Construction"),
            ),
            detailMode = TabletDetailMode.Guide,
        )

        val graph = state.resolvedEvidencePaneGraph()

        assertFalse(graph.anchor.hasSource)
        assertEquals(6, graph.xrefs.size)
        assertEquals("ANCHOR", graph.xrefs.first().relation)
        assertEquals("GD-132", graph.xrefs.first().id)
    }

    @Test
    fun tabletGuideRequiredReadingParserSplitsIdFromReadableTitle() {
        val parts = parseGuideRequiredReadingParts(
            "REQUIRED READING \u00B7 GD-499 \u00B7 Bellows Forge Blower Construction",
        )

        assertEquals("REQUIRED READING", parts.label)
        assertEquals("GD-499", parts.id)
        assertEquals("Bellows Forge Blower Construction", parts.title)
    }

    @Test
    fun tabletGuideRequiredReadingParserNormalizesMojibakeSeparators() {
        val parts = parseGuideRequiredReadingParts(
            "REQUIRED READING \u00C2\u00B7 GD-225 \u00C2\u00B7 Bloomery Furnace Construction",
        )

        assertEquals("REQUIRED READING", parts.label)
        assertEquals("GD-225", parts.id)
        assertEquals("Bloomery Furnace Construction", parts.title)
    }

    @Test
    fun tabletLandscapeTypeScaleKeepsAnswerReadableWithoutOversizingBody() {
        val policy = tabletLandscapeDetailTypeScalePolicy()

        assertEquals(18, policy.questionFontSizeSp)
        assertEquals(23, policy.questionLineHeightSp)
        assertEquals(13, policy.answerFontSizeSp)
        assertEquals(20, policy.answerLineHeightSp)
        assertEquals(12, policy.stepFontSizeSp)
        assertEquals(16, policy.stepLineHeightSp)
    }

    @Test
    fun tabletPortraitTypeScaleTightensAfterPortraitSqueeze() {
        val policy = tabletPortraitDetailTypeScalePolicy()

        assertEquals(17, policy.questionFontSizeSp)
        assertEquals(22, policy.questionLineHeightSp)
        assertEquals(13, policy.answerFontSizeSp)
        assertEquals(19, policy.answerLineHeightSp)
        assertEquals(10, policy.limitFontSizeSp)
        assertEquals(14, policy.limitLineHeightSp)
    }

    @Test
    fun tabletTypeScaleSelectorUsesOrientationSpecificBudgets() {
        val portraitPolicy = tabletDetailTypeScalePolicy(isLandscape = false)
        val landscapePolicy = tabletDetailTypeScalePolicy(isLandscape = true)

        assertEquals(tabletPortraitDetailTypeScalePolicy(), portraitPolicy)
        assertEquals(tabletLandscapeDetailTypeScalePolicy(), landscapePolicy)
    }

    @Test
    fun tabletPortraitAnswerChromeKeepsEmergencyAnswerHigherDensityThanLandscape() {
        val portraitPolicy = tabletAnswerReadingChromePolicy(isLandscape = false)
        val landscapePolicy = tabletAnswerReadingChromePolicy(isLandscape = true)

        assertTrue(portraitPolicy.topPaddingDp < landscapePolicy.topPaddingDp)
        assertTrue(portraitPolicy.blockSpacingDp < landscapePolicy.blockSpacingDp)
        assertTrue(portraitPolicy.bottomPaddingDp <= landscapePolicy.bottomPaddingDp)
    }

    @Test
    fun tabletComposerContextHintUsesGuideAnchorAndSourceCount() {
        val state = tabletDetailState(
            guideTitle = "Pressure canning beans",
            guideModeLabel = "GUIDE",
            anchor = AnchorState(
                key = "anchor",
                id = "GD-BEANS",
                title = "Processing times",
                section = "Altitude adjustment",
                snippet = "",
                hasSource = true,
            ),
            sources = listOf(
                SourceState("s1", "GD-BEANS", "Guide A", isAnchor = true, isSelected = true),
                SourceState("s2", "GD-ALT", "Guide B", isAnchor = false, isSelected = false),
            ),
            turns = listOf(threadTurn("q1"), threadTurn("q2")),
            detailMode = TabletDetailMode.Guide,
        )

        assertEquals(
            "GUIDE CONTEXT KEPT - 2 SECTIONS - 2 REFERENCES",
            tabletComposerContextHint(state),
        )
    }

    @Test
    fun tabletAnswerSourceCountUsesThreadAnswerCountWhenSourceRowsAreDelayed() {
        val state = tabletDetailState(
            sources = emptyList(),
            turns = listOf(threadTurn("q1", sourceCount = 2)),
        )

        assertEquals(2, state.resolvedAnswerSourceCount())
    }

    @Test
    fun tabletComposerContextHintUsesAnswerSourceCountFloor() {
        val state = tabletDetailState(
            sources = listOf(
                SourceState("s1", "GD-001", "Anchor guide", isAnchor = true, isSelected = true),
                SourceState("s2", "", "Field note", isAnchor = false, isSelected = false),
            ),
            turns = listOf(threadTurn("q1", sourceCount = 3)),
        )

        assertEquals(
            "THREAD CONTEXT KEPT - 1 TURN - 3 SOURCES",
            tabletComposerContextHint(state),
        )
    }

    @Test
    fun tabletComposerContextHintKeepsThreadLanguageOutsideGuideMode() {
        val state = tabletDetailState(
            guideId = "GD-FALLBACK",
            guideTitle = " ",
            anchor = AnchorState(
                key = "anchor",
                id = "GD-FALLBACK",
                title = "Fallback anchor",
                section = " ",
                snippet = "",
                hasSource = false,
            ),
            sources = emptyList(),
        )

        assertEquals(
            "THREAD CONTEXT KEPT - NO TURNS - NO SOURCES",
            tabletComposerContextHint(state),
        )
    }

    @Test
    fun tabletComposerContextHintUsesGuideEmptyLabelsInGuideMode() {
        val state = tabletDetailState(
            guideId = "GD-FALLBACK",
            guideTitle = " ",
            guideModeAnchorLabel = "Opened from GD-220",
            anchor = AnchorState(
                key = "anchor",
                id = "GD-FALLBACK",
                title = "Fallback anchor",
                section = " ",
                snippet = "",
                hasSource = false,
            ),
            sources = emptyList(),
            detailMode = TabletDetailMode.Guide,
        )

        assertEquals(
            "GUIDE CONTEXT KEPT - NO SECTIONS - NO REFERENCES",
            tabletComposerContextHint(state),
        )
    }

    @Test
    fun tabletAnswerModeShowsEvidencePaneWhenSourcesExist() {
        val state = tabletDetailState(
            sources = listOf(
                SourceState("s1", "GD-001", "Anchor guide", isAnchor = true, isSelected = true),
                SourceState("s2", "GD-002", "Related guide", isAnchor = false, isSelected = false),
            ),
        )

        assertTrue(tabletShouldShowEvidencePane(state, guideMode = false))
    }

    @Test
    fun tabletAnswerModeShowsEvidencePaneWhenExpanded() {
        val state = tabletDetailState(
            evidenceExpanded = true,
            sources = listOf(
                SourceState("s1", "GD-001", "Anchor guide", isAnchor = true, isSelected = true),
            ),
        )

        assertTrue(tabletShouldShowEvidencePane(state, guideMode = false))
    }

    @Test
    fun tabletAnswerModeShowsEvidencePaneForExplicitRelatedSourceSelection() {
        val state = tabletDetailState(
            sources = listOf(
                SourceState("s1", "GD-001", "Anchor guide", isAnchor = true, isSelected = false),
                SourceState("s2", "GD-002", "Related guide", isAnchor = false, isSelected = true),
            ),
        )

        assertTrue(tabletShouldShowEvidencePane(state, guideMode = false))
    }

    @Test
    fun tabletGuideModeKeepsExistingLandscapeEvidenceBehavior() {
        val landscapeState = tabletDetailState(isLandscape = true)
        val portraitState = tabletDetailState(isLandscape = false)

        assertTrue(tabletShouldShowEvidencePane(landscapeState, guideMode = true))
        assertFalse(tabletShouldShowEvidencePane(portraitState, guideMode = true))
    }

    @Test
    fun phoneLandscapePolicyCompactsComposerAndSuppressesSupportChrome() {
        val policy = phoneLandscapeStressReadingPolicy()

        assertTrue(policy.compactComposer)
        assertTrue(policy.suppressRetryChrome)
        assertTrue(policy.suppressSupportSuggestions)
        assertFalse(policy.collapseThreadChrome)
        assertFalse(policy.collapseSourceChrome)
    }

    @Test
    fun phonePortraitAnswerFirstPolicyCollapsesSecondaryChrome() {
        val policy = phonePortraitAnswerFirstStressReadingPolicy()

        assertTrue(policy.compactComposer)
        assertFalse(policy.suppressRetryChrome)
        assertFalse(policy.suppressSupportSuggestions)
        assertTrue(policy.collapseThreadChrome)
        assertTrue(policy.collapseSourceChrome)
    }

    @Test
    fun phonePolicySelectorUsesLandscapePolicyBeforeAnswerReadiness() {
        val awaitingAnswerPolicy = phoneStressReadingPolicy(
            isLandscape = true,
            answerReady = false,
        )
        val answerReadyPolicy = phoneStressReadingPolicy(
            isLandscape = true,
            answerReady = true,
        )

        assertEquals(phoneLandscapeStressReadingPolicy(), awaitingAnswerPolicy)
        assertEquals(phoneLandscapeStressReadingPolicy(), answerReadyPolicy)
        assertTrue(awaitingAnswerPolicy.compactComposer)
        assertTrue(awaitingAnswerPolicy.suppressRetryChrome)
        assertTrue(awaitingAnswerPolicy.suppressSupportSuggestions)
        assertFalse(awaitingAnswerPolicy.collapseThreadChrome)
        assertFalse(awaitingAnswerPolicy.collapseSourceChrome)
        assertEquals(awaitingAnswerPolicy, answerReadyPolicy)
    }

    @Test
    fun phonePolicySelectorCollapsesPortraitChromeOnlyWhenAnswerReady() {
        val awaitingAnswerPolicy = phoneStressReadingPolicy(
            isLandscape = false,
            answerReady = false,
        )
        val answerReadyPolicy = phoneStressReadingPolicy(
            isLandscape = false,
            answerReady = true,
        )

        assertEquals(phonePortraitAnswerFirstStressReadingPolicy(), answerReadyPolicy)
        assertTrue(awaitingAnswerPolicy.compactComposer)
        assertFalse(awaitingAnswerPolicy.suppressRetryChrome)
        assertFalse(awaitingAnswerPolicy.suppressSupportSuggestions)
        assertFalse(awaitingAnswerPolicy.collapseThreadChrome)
        assertFalse(awaitingAnswerPolicy.collapseSourceChrome)

        assertTrue(answerReadyPolicy.compactComposer)
        assertFalse(answerReadyPolicy.suppressRetryChrome)
        assertFalse(answerReadyPolicy.suppressSupportSuggestions)
        assertTrue(answerReadyPolicy.collapseThreadChrome)
        assertTrue(answerReadyPolicy.collapseSourceChrome)
    }

    @Test
    fun phonePolicySelectorKeepsCompactComposerAcrossStressPostures() {
        val policies = listOf(
            phoneStressReadingPolicy(isLandscape = true, answerReady = false),
            phoneStressReadingPolicy(isLandscape = true, answerReady = true),
            phoneStressReadingPolicy(isLandscape = false, answerReady = false),
            phoneStressReadingPolicy(isLandscape = false, answerReady = true),
        )

        policies.forEach { policy ->
            assertTrue(policy.compactComposer)
        }
    }

    private fun tabletDetailState(
        guideId: String = "GD-TEST",
        guideTitle: String = "Guide title",
        anchor: AnchorState = AnchorState(
            key = "anchor",
            id = "GD-TEST",
            title = "Anchor title",
            section = "Anchor section",
            snippet = "",
            hasSource = true,
        ),
        sources: List<SourceState> = emptyList(),
        turns: List<ThreadTurnState> = emptyList(),
        guideModeLabel: String = "",
        guideModeAnchorLabel: String = "",
        xrefs: List<XRefState> = emptyList(),
        evidenceExpanded: Boolean = false,
        isLandscape: Boolean = true,
        detailMode: TabletDetailMode = TabletDetailMode.Answer,
    ): TabletDetailState =
        TabletDetailState(
            guideId = guideId,
            guideTitle = guideTitle,
            meta = emptyList(),
            turns = turns,
            sources = sources,
            anchor = anchor,
            xrefs = xrefs,
            composerText = "",
            composerPlaceholder = "Ask a follow-up",
            composerEnabled = true,
            composerVisible = true,
            composerShowRetry = false,
            composerRetryLabel = "Retry",
            pinVisible = false,
            pinActive = false,
            evidenceExpanded = evidenceExpanded,
            isLandscape = isLandscape,
            guideModeLabel = guideModeLabel,
            guideModeAnchorLabel = guideModeAnchorLabel,
            detailMode = detailMode,
        )

    private fun threadTurn(
        id: String,
        sourceCount: Int = 1,
        answer: String = "Answer",
    ): ThreadTurnState =
        ThreadTurnState(
            id = id,
            question = "Question",
            answer = com.senku.ui.answer.AnswerContent(
                short = answer,
                sourceCount = sourceCount,
                host = "Host",
                elapsedSeconds = 1.0,
            ),
            status = Status.Done,
            isActive = false,
        )
}
