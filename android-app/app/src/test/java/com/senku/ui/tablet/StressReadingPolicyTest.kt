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
    fun tabletGuidePaperPolicyContainsPortraitReaderWithoutChangingLandscapeSheet() {
        assertEquals(560, tabletGuidePaperMaxWidthDp(isLandscape = true))
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
            xrefs = emptyList(),
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

    private fun threadTurn(id: String): ThreadTurnState =
        ThreadTurnState(
            id = id,
            question = "Question",
            answer = com.senku.ui.answer.AnswerContent(
                short = "Answer",
                sourceCount = 1,
                host = "Host",
                elapsedSeconds = 1.0,
            ),
            status = Status.Done,
            isActive = false,
        )
}
