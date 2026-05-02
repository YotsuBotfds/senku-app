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
    public void sanitizeAnswerTextRejectsTokenAndScriptNoise() {
        assertEquals("", PromptAnswerTextPolicy.sanitizeAnswerText("<pad><eos>"));
        assertEquals("", PromptAnswerTextPolicy.sanitizeAnswerText("<unk>"));
        assertEquals("", PromptAnswerTextPolicy.sanitizeAnswerText("qzxv"));
        assertEquals("", PromptAnswerTextPolicy.sanitizeAnswerText("\u6E2C\u8A66"));
        assertEquals("No.", PromptAnswerTextPolicy.sanitizeAnswerText("No."));
    }

    @Test
    public void cleanAnswerRemovesMarkdownAndLatexLikeNoise() {
        String cleaned = PromptAnswerTextPolicy.cleanAnswer(
            "Answer: **Use dry tinder**. \\\\text{Keep airflow open}. ```"
        );

        assertEquals("Use dry tinder. Keep airflow open.", cleaned);
    }

    @Test
    public void cleanAnswerRendersSectionsFromCodeBlockOutput() {
        String cleaned = PromptAnswerTextPolicy.cleanAnswer(
            "Answer: ```Short answer: Keep tinder dry before lighting. " +
                "Steps: 1. Cover the tinder. 2. Cover the tinder. 3. Lift it off wet ground. " +
                "Limits or safety: Do not light under low brush.```"
        );

        assertEquals(
            "Short answer: Keep tinder dry before lighting.\n\n" +
                "Steps:\n" +
                "1. Cover the tinder.\n" +
                "2. Lift it off wet ground.\n\n" +
                "Limits or safety:\n" +
                "Do not light under low brush.",
            cleaned
        );
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

    @Test
    public void lowCoverageDetectionCatchesCommonModelFallbackPhrases() {
        assertTrue(PromptAnswerTextPolicy.isLowCoverageAnswer("That is not covered in the retrieved notes."));
        assertTrue(PromptAnswerTextPolicy.isLowCoverageAnswer("I am unable to provide that from the notes."));
        assertTrue(PromptAnswerTextPolicy.isLowCoverageAnswer("There is no direct information about violin setup."));
        assertTrue(PromptAnswerTextPolicy.isLowCoverageAnswer("Nothing in the notes explains tax filing."));
    }
}
