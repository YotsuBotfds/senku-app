package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PromptAnswerTextPolicyTest {
    @Test
    public void cleanAnswerRemovesAnswerPrefixesAndModelTokens() {
        String cleaned = PromptAnswerTextPolicy.cleanAnswer(
            " Answer: <bos> Use overlapping bark courses and seal the ridge. <eos> "
        );

        assertEquals("Use overlapping bark courses and seal the ridge.", cleaned);
    }

    @Test
    public void cleanAnswerRendersClassicSectionsAndDeduplicatesSteps() {
        String cleaned = PromptAnswerTextPolicy.cleanAnswer(
            "Short answer: Keep the opening leeward so wind does not blow through camp. " +
                "Extra detail should be truncated. " +
                "Steps: 1. Face the opening away from wind. 2. Face the opening away from wind. " +
                "3. Test smoke flow. 4. Stake the cover. 5. Add a spare task. " +
                "Limits or safety: Avoid low cold spots where water collects. " +
                "Stop if the site floods. Third sentence should not remain."
        );

        assertEquals(
            "Short answer: Keep the opening leeward so wind does not blow through camp.\n\n" +
                "Steps:\n" +
                "1. Face the opening away from wind.\n" +
                "2. Test smoke flow.\n" +
                "3. Stake the cover.\n" +
                "4. Add a spare task.\n\n" +
                "Limits or safety:\n" +
                "Avoid low cold spots where water collects. Stop if the site floods.",
            cleaned
        );
    }

    @Test
    public void cleanAnswerRendersSummaryKeyPointsSections() {
        String cleaned = PromptAnswerTextPolicy.cleanAnswer(
            "Summary: Start with low-risk shared work before merging groups.\n\n" +
                "Key points:\n" +
                "1. Set a reversible joint task.\n" +
                "2. Keep transparent records.\n" +
                "Risks or limits: Do not force a full merger before trust improves."
        );

        assertEquals(
            "Summary: Start with low-risk shared work before merging groups.\n\n" +
                "Key points:\n" +
                "1. Set a reversible joint task.\n" +
                "2. Keep transparent records.\n\n" +
                "Risks or limits:\n" +
                "Do not force a full merger before trust improves.",
            cleaned
        );
    }

    @Test
    public void sanitizeAnswerTextReturnsBlankForCorruptedAnswers() {
        assertEquals("", PromptAnswerTextPolicy.sanitizeAnswerText("<pad> x"));
        assertEquals("", PromptAnswerTextPolicy.sanitizeAnswerText("xy"));
        assertTrue(PromptAnswerTextPolicy.isLikelyCorruptedAnswer("<unk> y"));
        assertFalse(PromptAnswerTextPolicy.isLikelyCorruptedAnswer("No, avoid damp tinder."));
    }

    @Test
    public void lowCoverageDetectionMatchesKnownFallbackLanguageOnly() {
        assertTrue(PromptAnswerTextPolicy.isLowCoverageAnswer(
            "The retrieved notes do not address that material."
        ));
        assertTrue(PromptAnswerTextPolicy.isLowCoverageAnswer(
            "No specific information is available in the notes."
        ));
        assertFalse(PromptAnswerTextPolicy.isLowCoverageAnswer(
            "Use dry kindling and build airflow from below."
        ));
        assertFalse(PromptAnswerTextPolicy.isLowCoverageAnswer(""));
    }
}
