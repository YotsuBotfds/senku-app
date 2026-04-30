package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class FollowUpComposerControllerTest {
    @Test
    public void submitDecisionTreatsBlankDraftAsEmptyEvenWhenBlocked() {
        FollowUpComposerState state = new FollowUpComposerState(
            " \n\t ",
            true,
            true,
            null,
            FollowUpComposerState.Surface.PHONE,
            null,
            null
        );

        FollowUpComposerController.SubmitDecision decision =
            FollowUpComposerController.resolveSubmit(state);

        assertEquals(FollowUpComposerController.SubmitAction.EMPTY, decision.action);
        assertEquals("", decision.query);
    }

    @Test
    public void submitDecisionBlocksDoubleSubmitWithoutDroppingNormalizedQuery() {
        FollowUpComposerState state = FollowUpComposerState.idle(
            "  check tarp tension  ",
            FollowUpComposerState.Surface.TABLET
        ).withBusy(true);

        FollowUpComposerController.SubmitDecision decision =
            FollowUpComposerController.resolveSubmit(state);

        assertEquals(FollowUpComposerController.SubmitAction.BLOCKED, decision.action);
        assertEquals("check tarp tension", decision.query);
    }

    @Test
    public void submitDecisionAllowsSendableDraft() {
        FollowUpComposerState state = FollowUpComposerState.idle(
            "  what changed overnight?\r\n",
            FollowUpComposerState.Surface.PHONE
        );

        FollowUpComposerController.SubmitDecision decision =
            FollowUpComposerController.resolveSubmit(state);

        assertEquals(FollowUpComposerController.SubmitAction.SUBMIT, decision.action);
        assertEquals("what changed overnight?", decision.query);
    }

    @Test
    public void retryDecisionUsesComposerRetryBeforeVisibleDraftFallback() {
        FollowUpComposerState state = new FollowUpComposerState(
            "visible draft",
            false,
            false,
            "failed",
            FollowUpComposerState.Surface.PHONE,
            "  failed query  ",
            "active query"
        );

        FollowUpComposerController.RetryDecision decision =
            FollowUpComposerController.resolveRetry(state, "fallback draft");

        assertEquals(FollowUpComposerController.RetryAction.RETRY, decision.action);
        assertEquals("failed query", decision.query);
    }

    @Test
    public void retryDecisionFallsBackToVisibleDraftWhenNoRetryQueryExists() {
        FollowUpComposerState state = FollowUpComposerState.idle(
            "",
            FollowUpComposerState.Surface.TABLET
        );

        FollowUpComposerController.RetryDecision decision =
            FollowUpComposerController.resolveRetry(state, "  retry visible draft\r ");

        assertEquals(FollowUpComposerController.RetryAction.RETRY, decision.action);
        assertEquals("retry visible draft", decision.query);
    }

    @Test
    public void retryDecisionReportsEmptyWhenRetryAndVisibleDraftAreBlank() {
        FollowUpComposerState state = FollowUpComposerState.idle(
            "",
            FollowUpComposerState.Surface.PHONE
        );

        FollowUpComposerController.RetryDecision decision =
            FollowUpComposerController.resolveRetry(state, " \t\n ");

        assertEquals(FollowUpComposerController.RetryAction.EMPTY, decision.action);
        assertEquals("", decision.query);
    }
}
