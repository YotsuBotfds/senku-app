package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class FollowUpGenerationCoordinatorTest {
    @Test
    public void generationStartClearsDraftAndResetsFailedQuery() {
        FollowUpComposerState state = new FollowUpComposerState(
            "  what changed since then?  ",
            true,
            true,
            "offline failure",
            FollowUpComposerState.Surface.TABLET,
            "  failed query to clear  ",
            null
        );

        FollowUpGenerationCoordinator.GenerationStartDecision decision =
            FollowUpGenerationCoordinator.resolveGenerationStart(state);

        assertEquals("", decision.draftText);
        assertEquals("", decision.lastFailedQuery);
    }

    @Test
    public void tabletRawQuerySubmitClearsDraftAndResetsFailedQueryOnGenerationStart() {
        FollowUpComposerState restoredTabletState = new FollowUpComposerState(
            "  stale restored draft  ",
            false,
            false,
            "old failure",
            FollowUpComposerState.Surface.TABLET,
            "  failed query to clear  ",
            null
        );

        FollowUpComposerController.SubmitDecision submit =
            FollowUpComposerController.resolveSubmit(
                restoredTabletState.withDraft("  raw tablet follow up  ")
            );
        FollowUpGenerationCoordinator.GenerationStartDecision start =
            FollowUpGenerationCoordinator.resolveGenerationStart(
                restoredTabletState.withDraft(submit.query).asSubmitting()
            );

        assertEquals(FollowUpComposerController.SubmitAction.SUBMIT, submit.action);
        assertEquals("raw tablet follow up", submit.query);
        assertEquals("", start.draftText);
        assertEquals("", start.lastFailedQuery);
    }

    @Test
    public void generationStartResetsFailureRetryEvenWhenActiveRetryWasVisible() {
        FollowUpComposerState stalledRetryState = new FollowUpComposerState(
            "  visible retry draft  ",
            true,
            true,
            null,
            FollowUpComposerState.Surface.PHONE,
            "  failed query to clear  ",
            "  active stalled query to clear  "
        );

        FollowUpGenerationCoordinator.GenerationStartDecision decision =
            FollowUpGenerationCoordinator.resolveGenerationStart(stalledRetryState);

        assertEquals("", decision.draftText);
        assertEquals("", decision.lastFailedQuery);
    }

    @Test
    public void nullGenerationStartDefaultsToEmptyPhoneState() {
        FollowUpGenerationCoordinator.GenerationStartDecision decision =
            FollowUpGenerationCoordinator.resolveGenerationStart(null);

        assertEquals("", decision.draftText);
        assertEquals("", decision.lastFailedQuery);
    }

    @Test
    public void stallStateBecomesVisibleAtThresholdBeforeFirstChunk() {
        FollowUpGenerationCoordinator.StallStateDecision decision =
            FollowUpGenerationCoordinator.resolveStallState(
                false,
                1000L,
                13000L,
                12000L,
                false
            );

        assertTrue(decision.stalled);
        assertTrue(decision.changed);
    }

    @Test
    public void stallStateRemainsHiddenBeforeThreshold() {
        FollowUpGenerationCoordinator.StallStateDecision decision =
            FollowUpGenerationCoordinator.resolveStallState(
                false,
                1000L,
                12999L,
                12000L,
                false
            );

        assertFalse(decision.stalled);
        assertFalse(decision.changed);
    }

    @Test
    public void stallStateClearsAfterFirstStreamingChunk() {
        FollowUpGenerationCoordinator.StallStateDecision decision =
            FollowUpGenerationCoordinator.resolveStallState(
                true,
                1000L,
                30000L,
                12000L,
                true
            );

        assertFalse(decision.stalled);
        assertTrue(decision.changed);
    }

    @Test
    public void stallStateUsesNowAsStartWhenGenerationStartIsMissing() {
        FollowUpGenerationCoordinator.StallStateDecision decision =
            FollowUpGenerationCoordinator.resolveStallState(
                false,
                0L,
                30000L,
                12000L,
                false
            );

        assertFalse(decision.stalled);
        assertFalse(decision.changed);
    }
}
