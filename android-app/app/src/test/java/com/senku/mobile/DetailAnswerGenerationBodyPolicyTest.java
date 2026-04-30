package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class DetailAnswerGenerationBodyPolicyTest {
    @Test
    public void normalizeStreamingBodyWrapsRawChunkInAnswerEnvelope() {
        DetailAnswerGenerationBodyPolicy policy = policy();

        assertEquals(
            "Answer\nKeep pressure on the wound.",
            policy.normalizeStreamingBody("Keep pressure on the wound.")
        );
    }

    @Test
    public void normalizeStreamingBodyPreservesExistingAnswerEnvelope() {
        DetailAnswerGenerationBodyPolicy policy = policy();

        assertEquals(
            "Answer\nKeep pressure on the wound.",
            policy.normalizeStreamingBody("Answer\nKeep pressure on the wound.")
        );
    }

    @Test
    public void resolveFinalAnswerBodyKeepsMismatchedTokenFinalBody() {
        DetailAnswerGenerationBodyPolicy policy = policy();

        DetailAnswerGenerationBodyPolicy.FinalBodyResolution resolution =
            policy.resolveFinalAnswerBody("No", 4, 5, richStreamBody());

        assertEquals("No", resolution.body);
        assertFalse(resolution.usedStreamingFallback);
        assertEquals(0, resolution.finalVisibleLength);
        assertEquals(0, resolution.streamVisibleLength);
    }

    @Test
    public void resolveFinalAnswerBodyFallsBackWhenFinalBodyIsShortAndStreamIsRicher() {
        DetailAnswerGenerationBodyPolicy policy = policy();

        DetailAnswerGenerationBodyPolicy.FinalBodyResolution resolution =
            policy.resolveFinalAnswerBody("No", 7, 7, richStreamBody());

        assertEquals(richStreamBody(), resolution.body);
        assertTrue(resolution.usedStreamingFallback);
        assertEquals(2, resolution.finalVisibleLength);
        assertTrue(resolution.streamVisibleLength >= 40);
    }

    @Test
    public void resolveFinalAnswerBodyKeepsCompleteFinalBodyOverStream() {
        DetailAnswerGenerationBodyPolicy policy = policy();
        String finalBody = "Answer\nKeep pressure on the wound and watch for trouble breathing.";

        DetailAnswerGenerationBodyPolicy.FinalBodyResolution resolution =
            policy.resolveFinalAnswerBody(finalBody, 9, 9, richStreamBody());

        assertEquals(finalBody, resolution.body);
        assertFalse(resolution.usedStreamingFallback);
    }

    @Test
    public void resolveFinalAnswerBodyKeepsFinalBodyWhenStreamHasNoVisibleText() {
        DetailAnswerGenerationBodyPolicy policy = policy();

        DetailAnswerGenerationBodyPolicy.FinalBodyResolution resolution =
            policy.resolveFinalAnswerBody("No", 11, 11, "");

        assertEquals("No", resolution.body);
        assertFalse(resolution.usedStreamingFallback);
        assertEquals(2, resolution.finalVisibleLength);
        assertEquals(0, resolution.streamVisibleLength);
    }

    private static DetailAnswerGenerationBodyPolicy policy() {
        return new DetailAnswerGenerationBodyPolicy(new DetailAnswerBodyFormatter(null));
    }

    private static String richStreamBody() {
        return "Answer\nKeep steady pressure on the wound, wrap it firmly, and watch for shock.";
    }
}
