package com.senku.ui.tablet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StressReadingPolicyTest {
    @Test
    fun tabletLandscapeReadingPolicyKeepsAnswerFirstWidthBudget() {
        val policy = tabletLandscapeReadingLayoutPolicy()

        assertEquals(276, policy.threadRailWidthDp)
        assertEquals(486, policy.answerMaxWidthDp)
        assertEquals(344, policy.evidenceRailWidthDp)
        assertEquals(16, policy.answerHorizontalPaddingDp)
    }

    @Test
    fun tabletPortraitReadingPolicyUsesSqueezedPortraitBudget() {
        val policy = tabletPortraitReadingLayoutPolicy()

        assertEquals(148, policy.threadRailWidthDp)
        assertEquals(472, policy.answerMaxWidthDp)
        assertEquals(224, policy.evidenceRailWidthDp)
        assertEquals(10, policy.answerHorizontalPaddingDp)
    }

    @Test
    fun tabletReadingPolicySelectorUsesOrientationSpecificBudgets() {
        val portraitPolicy = tabletReadingLayoutPolicy(isLandscape = false)
        val landscapePolicy = tabletReadingLayoutPolicy(isLandscape = true)

        assertEquals(tabletPortraitReadingLayoutPolicy(), portraitPolicy)
        assertEquals(tabletLandscapeReadingLayoutPolicy(), landscapePolicy)
        assertEquals(148, portraitPolicy.threadRailWidthDp)
        assertEquals(472, portraitPolicy.answerMaxWidthDp)
        assertEquals(224, portraitPolicy.evidenceRailWidthDp)
        assertEquals(10, portraitPolicy.answerHorizontalPaddingDp)
        assertEquals(276, landscapePolicy.threadRailWidthDp)
        assertEquals(486, landscapePolicy.answerMaxWidthDp)
        assertEquals(344, landscapePolicy.evidenceRailWidthDp)
        assertEquals(16, landscapePolicy.answerHorizontalPaddingDp)
    }

    @Test
    fun tabletLandscapeTypeScaleKeepsAnswerReadableWithoutOversizingBody() {
        val policy = tabletLandscapeDetailTypeScalePolicy()

        assertEquals(20, policy.questionFontSizeSp)
        assertEquals(25, policy.questionLineHeightSp)
        assertEquals(14, policy.answerFontSizeSp)
        assertEquals(21, policy.answerLineHeightSp)
        assertEquals(12, policy.stepFontSizeSp)
        assertEquals(17, policy.stepLineHeightSp)
    }

    @Test
    fun tabletPortraitTypeScaleTightensAfterPortraitSqueeze() {
        val policy = tabletPortraitDetailTypeScalePolicy()

        assertEquals(20, policy.questionFontSizeSp)
        assertEquals(25, policy.questionLineHeightSp)
        assertEquals(14, policy.answerFontSizeSp)
        assertEquals(21, policy.answerLineHeightSp)
        assertEquals(11, policy.limitFontSizeSp)
        assertEquals(16, policy.limitLineHeightSp)
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
        )

        assertEquals(
            "THREAD CONTEXT KEPT - 2 TURNS - 2 SOURCES",
            tabletComposerContextHint(state),
        )
    }

    @Test
    fun tabletComposerContextHintFallsBackToGuideIdAndEmptySourceLabel() {
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
            evidenceExpanded = false,
            isLandscape = true,
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
