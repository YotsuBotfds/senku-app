package com.senku.mobile;

import static org.junit.Assert.assertEquals;

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
    public void nullGenerationStartDefaultsToEmptyPhoneState() {
        FollowUpGenerationCoordinator.GenerationStartDecision decision =
            FollowUpGenerationCoordinator.resolveGenerationStart(null);

        assertEquals("", decision.draftText);
        assertEquals("", decision.lastFailedQuery);
    }
}
