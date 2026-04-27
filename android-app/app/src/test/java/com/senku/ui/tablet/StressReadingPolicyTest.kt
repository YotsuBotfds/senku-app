package com.senku.ui.tablet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StressReadingPolicyTest {
    @Test
    fun tabletLandscapeReadingPolicyKeepsAnswerFirstWidthBudget() {
        val policy = tabletLandscapeReadingLayoutPolicy()

        assertEquals(240, policy.threadRailWidthDp)
        assertEquals(680, policy.answerMaxWidthDp)
    }

    @Test
    fun tabletReadingPolicySelectorKeepsCurrentPostureBudgets() {
        val portraitPolicy = tabletReadingLayoutPolicy(isLandscape = false)
        val landscapePolicy = tabletReadingLayoutPolicy(isLandscape = true)

        assertEquals(tabletLandscapeReadingLayoutPolicy(), portraitPolicy)
        assertEquals(tabletLandscapeReadingLayoutPolicy(), landscapePolicy)
        assertEquals(240, portraitPolicy.threadRailWidthDp)
        assertEquals(680, portraitPolicy.answerMaxWidthDp)
        assertEquals(240, landscapePolicy.threadRailWidthDp)
        assertEquals(680, landscapePolicy.answerMaxWidthDp)
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
}
