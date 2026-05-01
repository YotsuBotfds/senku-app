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
    public void lifecycleKeepsBusySubmitAndFailedQueryRetryDecisionsSeparate() {
        FollowUpComposerState idle = FollowUpComposerState.idle(
            "  inspect the ridge line  ",
            FollowUpComposerState.Surface.PHONE
        );

        FollowUpComposerController.SubmitDecision initialSubmit =
            FollowUpComposerController.resolveSubmit(idle);
        FollowUpComposerController.SubmitDecision duplicateSubmit =
            FollowUpComposerController.resolveSubmit(idle.asSubmitting());

        assertEquals(FollowUpComposerController.SubmitAction.SUBMIT, initialSubmit.action);
        assertEquals("inspect the ridge line", initialSubmit.query);
        assertEquals(FollowUpComposerController.SubmitAction.BLOCKED, duplicateSubmit.action);
        assertEquals("inspect the ridge line", duplicateSubmit.query);

        FollowUpComposerState failedAfterRestore = FollowUpComposerState.idle(
            initialSubmit.query,
            FollowUpComposerState.Surface.PHONE
        ).withFailure("offline answer failed", initialSubmit.query);
        FollowUpComposerController.RetryDecision retryRestoredFailure =
            FollowUpComposerController.resolveRetry(failedAfterRestore.withDraft("new draft"), "visible fallback");
        FollowUpComposerController.SubmitDecision submitEditedDraft =
            FollowUpComposerController.resolveSubmit(failedAfterRestore.withDraft("  new draft  "));

        assertEquals(FollowUpComposerController.RetryAction.RETRY, retryRestoredFailure.action);
        assertEquals("inspect the ridge line", retryRestoredFailure.query);
        assertEquals(FollowUpComposerController.SubmitAction.SUBMIT, submitEditedDraft.action);
        assertEquals("new draft", submitEditedDraft.query);
    }

    @Test
    public void rawSubmitDecisionNormalizesEmptyPhoneDraft() {
        FollowUpComposerController.SubmitDecision decision =
            FollowUpComposerController.resolveSubmit(
                " \r\n\t ",
                FollowUpComposerState.Surface.PHONE,
                false
            );

        assertEquals(FollowUpComposerController.SubmitAction.EMPTY, decision.action);
        assertEquals("", decision.query);
    }

    @Test
    public void rawSubmitDecisionBlocksBusyTabletDraft() {
        FollowUpComposerController.SubmitDecision decision =
            FollowUpComposerController.resolveSubmit(
                "  inspect the guyline tension  ",
                FollowUpComposerState.Surface.TABLET,
                true
            );

        assertEquals(FollowUpComposerController.SubmitAction.BLOCKED, decision.action);
        assertEquals("inspect the guyline tension", decision.query);
    }

    @Test
    public void rawSubmitDecisionAllowsSendablePhoneDraft() {
        FollowUpComposerController.SubmitDecision decision =
            FollowUpComposerController.resolveSubmit(
                "  what should I check next?\r ",
                FollowUpComposerState.Surface.PHONE,
                false
            );

        assertEquals(FollowUpComposerController.SubmitAction.SUBMIT, decision.action);
        assertEquals("what should I check next?", decision.query);
    }

    @Test
    public void phoneSubmitRouteKeepsEmptyInputOnEmptyPath() {
        assertEquals(
            FollowUpComposerController.SubmitRoute.EMPTY_INPUT,
            FollowUpComposerController.resolvePhoneSubmitRoute("")
        );
        assertEquals(
            FollowUpComposerController.SubmitRoute.EMPTY_INPUT,
            FollowUpComposerController.resolvePhoneSubmitRoute("   \n\t  ")
        );
    }

    @Test
    public void phoneSubmitRouteSendsNonBlankDraftThroughFollowupPath() {
        assertEquals(
            FollowUpComposerController.SubmitRoute.PHONE_FOLLOWUP,
            FollowUpComposerController.resolvePhoneSubmitRoute("  what should I do next?  ")
        );
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

    @Test
    public void retryPresentationExposesFailureRetryWhenSurfaceAllowsIt() {
        FollowUpComposerState state = FollowUpComposerState.idle(
            "new draft",
            FollowUpComposerState.Surface.PHONE
        ).withFailure("offline failure", "  retry this query  ");

        FollowUpComposerController.RetryPresentation presentation =
            FollowUpComposerController.resolveRetryPresentation(state, true);

        assertEquals(true, presentation.visible);
        assertEquals(true, presentation.actionEnabled);
        assertEquals("retry this query", presentation.query);
    }

    @Test
    public void retryPresentationKeepsActiveStallRetryAvailableWhileBlocked() {
        FollowUpComposerState state = new FollowUpComposerState(
            "draft",
            true,
            true,
            null,
            FollowUpComposerState.Surface.TABLET,
            null,
            "  stalled answer query  "
        );

        FollowUpComposerController.RetryPresentation presentation =
            FollowUpComposerController.resolveRetryPresentation(state, true);

        assertEquals(true, presentation.visible);
        assertEquals(true, presentation.actionEnabled);
        assertEquals("stalled answer query", presentation.query);
    }

    @Test
    public void retryPresentationSuppressesStaleRetryWhenBlockedWithoutActiveRetry() {
        FollowUpComposerState state = new FollowUpComposerState(
            "draft",
            true,
            true,
            null,
            FollowUpComposerState.Surface.PHONE,
            "old failed query",
            null
        );

        FollowUpComposerController.RetryPresentation presentation =
            FollowUpComposerController.resolveRetryPresentation(state, true);

        assertEquals(false, presentation.visible);
        assertEquals(false, presentation.actionEnabled);
        assertEquals("", presentation.query);
    }

    @Test
    public void retryPresentationObeysSurfaceSuppression() {
        FollowUpComposerState state = FollowUpComposerState.idle(
            "",
            FollowUpComposerState.Surface.PHONE
        ).withFailure("offline failure", "retry this query");

        FollowUpComposerController.RetryPresentation presentation =
            FollowUpComposerController.resolveRetryPresentation(state, false);

        assertEquals(false, presentation.visible);
        assertEquals(false, presentation.actionEnabled);
        assertEquals("", presentation.query);
    }
}
