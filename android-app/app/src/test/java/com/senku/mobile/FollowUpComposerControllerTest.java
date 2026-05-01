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
    public void successfulGenerationClearsSubmittedDraft() {
        FollowUpComposerState submitted = FollowUpComposerState.idle(
            "  inspect the lower seam  ",
            FollowUpComposerState.Surface.PHONE
        ).asSubmitting();

        FollowUpComposerState completed =
            FollowUpComposerController.resolveGenerationSuccess(submitted);

        assertEquals("", completed.draftText);
        assertEquals("", completed.failureMessage);
        assertEquals("", completed.retryQuery());
        assertEquals(FollowUpComposerState.Surface.PHONE, completed.surface);
    }

    @Test
    public void successfulGenerationClearsOnlyMatchingSubmittedDraft() {
        FollowUpComposerState sameDraft = FollowUpComposerState.idle(
            "  inspect the lower seam  ",
            FollowUpComposerState.Surface.PHONE
        );
        FollowUpComposerState emptyDraftAfterStart = FollowUpComposerState.idle(
            "",
            FollowUpComposerState.Surface.TABLET
        );

        FollowUpComposerState phoneCompleted =
            FollowUpComposerController.resolveGenerationSuccess(
                sameDraft,
                "inspect the lower seam"
            );
        FollowUpComposerState tabletCompleted =
            FollowUpComposerController.resolveGenerationSuccess(
                emptyDraftAfterStart,
                "inspect the lower seam"
            );

        assertEquals("", phoneCompleted.draftText);
        assertEquals("", phoneCompleted.retryQuery());
        assertEquals(FollowUpComposerState.Surface.PHONE, phoneCompleted.surface);
        assertEquals("", tabletCompleted.draftText);
        assertEquals(FollowUpComposerState.Surface.TABLET, tabletCompleted.surface);
    }

    @Test
    public void successfulGenerationPreservesDifferentInProgressDraft() {
        FollowUpComposerState editedWhileRunning = FollowUpComposerState.idle(
            "  a newer follow-up draft  ",
            FollowUpComposerState.Surface.TABLET
        ).withFailure("old failure", "old failed query");

        FollowUpComposerState completed =
            FollowUpComposerController.resolveGenerationSuccess(
                editedWhileRunning,
                "submitted query"
            );

        assertEquals("a newer follow-up draft", completed.draftText);
        assertEquals("", completed.failureMessage);
        assertEquals("", completed.retryQuery());
        assertEquals(FollowUpComposerState.Surface.TABLET, completed.surface);
    }

    @Test
    public void successfulGenerationPreservesVisibleDraftWhenSubmittedQueryMissing() {
        FollowUpComposerState editedWhileRunning = FollowUpComposerState.idle(
            "  keep this visible draft  ",
            FollowUpComposerState.Surface.PHONE
        );

        FollowUpComposerState completed =
            FollowUpComposerController.resolveGenerationSuccess(
                editedWhileRunning,
                " \n\t "
            );

        assertEquals("keep this visible draft", completed.draftText);
        assertEquals("", completed.retryQuery());
        assertEquals(FollowUpComposerState.Surface.PHONE, completed.surface);
    }

    @Test
    public void generationStartClearsSubmittedDraftBeforeAnswerCompletes() {
        FollowUpComposerState submitted = FollowUpComposerState.idle(
            "  inspect the lower seam  ",
            FollowUpComposerState.Surface.TABLET
        ).asSubmitting();

        FollowUpComposerState started =
            FollowUpComposerController.resolveGenerationStart(submitted);

        assertEquals("", started.draftText);
        assertEquals(true, started.busy);
        assertEquals(true, started.submitting);
        assertEquals(FollowUpComposerState.Surface.TABLET, started.surface);
    }

    @Test
    public void failedGenerationRestoresSubmittedDraftForRetry() {
        FollowUpComposerState submitted = FollowUpComposerState.idle(
            "  inspect the lower seam  ",
            FollowUpComposerState.Surface.TABLET
        ).asSubmitting();

        FollowUpComposerState failed =
            FollowUpComposerController.resolveGenerationFailure(
                submitted,
                "  offline answer failed  ",
                "  inspect the lower seam  "
            );
        FollowUpComposerController.RetryDecision retry =
            FollowUpComposerController.resolveRetry(failed, "");

        assertEquals("inspect the lower seam", failed.draftText);
        assertEquals("offline answer failed", failed.failureMessage);
        assertEquals("inspect the lower seam", failed.retryQuery());
        assertEquals(FollowUpComposerController.RetryAction.RETRY, retry.action);
        assertEquals("inspect the lower seam", retry.query);
        assertEquals(FollowUpComposerState.Surface.TABLET, failed.surface);
    }

    @Test
    public void failedGenerationFallsBackToVisibleDraftWhenSubmittedQueryMissing() {
        FollowUpComposerState visibleDraft = FollowUpComposerState.idle(
            "  retry visible wording  ",
            FollowUpComposerState.Surface.PHONE
        );

        FollowUpComposerState failed =
            FollowUpComposerController.resolveGenerationFailure(
                visibleDraft,
                "failed",
                " \n\t "
            );

        assertEquals("retry visible wording", failed.draftText);
        assertEquals("retry visible wording", failed.retryQuery());
    }

    @Test
    public void failedGenerationPreservesDifferentInProgressDraftButRetriesSubmittedQuery() {
        FollowUpComposerState editedWhileRunning = FollowUpComposerState.idle(
            "  newer draft to keep  ",
            FollowUpComposerState.Surface.PHONE
        );

        FollowUpComposerState failed =
            FollowUpComposerController.resolveGenerationFailure(
                editedWhileRunning,
                "offline answer failed",
                "  submitted query  "
            );
        FollowUpComposerController.RetryDecision retry =
            FollowUpComposerController.resolveRetry(failed, "");
        FollowUpComposerController.SubmitDecision submit =
            FollowUpComposerController.resolveSubmit(failed);

        assertEquals("newer draft to keep", failed.draftText);
        assertEquals("submitted query", failed.retryQuery());
        assertEquals(FollowUpComposerController.RetryAction.RETRY, retry.action);
        assertEquals("submitted query", retry.query);
        assertEquals(FollowUpComposerController.SubmitAction.SUBMIT, submit.action);
        assertEquals("newer draft to keep", submit.query);
    }

    @Test
    public void failureRetryPreservesSubmittedQueryAfterGenerationStartClearsDraft() {
        FollowUpComposerState submitted = FollowUpComposerState.idle(
            "  inspect the ridge line  ",
            FollowUpComposerState.Surface.PHONE
        ).asSubmitting();

        FollowUpComposerState started =
            FollowUpComposerController.resolveGenerationStart(submitted);
        FollowUpComposerState failed =
            FollowUpComposerController.resolveGenerationFailure(
                started,
                "offline answer failed",
                "  inspect the ridge line  "
            );
        FollowUpComposerState completed =
            FollowUpComposerController.resolveGenerationSuccess(failed);

        assertEquals("", started.draftText);
        assertEquals("inspect the ridge line", failed.draftText);
        assertEquals("inspect the ridge line", failed.retryQuery());
        assertEquals("", completed.draftText);
        assertEquals("", completed.retryQuery());
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
    public void phoneSendAndImeSubmitShareRawDecisionPolicy() {
        String sendDraft = "  what changed overnight?\r\n";
        String imeDraft = "  what changed overnight?\r\n";

        FollowUpComposerController.SubmitDecision sendDecision =
            FollowUpComposerController.resolveSubmit(
                sendDraft,
                FollowUpComposerState.Surface.PHONE,
                false
            );
        FollowUpComposerController.SubmitDecision imeDecision =
            FollowUpComposerController.resolveSubmit(
                imeDraft,
                FollowUpComposerState.Surface.PHONE,
                false
            );

        assertEquals(FollowUpComposerController.SubmitAction.SUBMIT, sendDecision.action);
        assertEquals(sendDecision.action, imeDecision.action);
        assertEquals(sendDecision.query, imeDecision.query);
        assertEquals(
            FollowUpComposerController.resolvePhoneSubmitRoute(sendDraft),
            FollowUpComposerController.resolvePhoneSubmitRoute(imeDraft)
        );
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

    @Test
    public void controlPresentationDisablesSubmitForEmptyDraftWhileInputRemainsAvailable() {
        FollowUpComposerController.ControlPresentation presentation =
            FollowUpComposerController.resolveControlPresentation(
                FollowUpComposerState.idle(" \n\t ", FollowUpComposerState.Surface.PHONE)
            );

        assertEquals(true, presentation.inputEnabled);
        assertEquals(false, presentation.submitEnabled);
    }

    @Test
    public void controlPresentationEnablesSubmitOnlyForSendableIdleDraft() {
        FollowUpComposerController.ControlPresentation presentation =
            FollowUpComposerController.resolveControlPresentation(
                FollowUpComposerState.idle("  what changed?  ", FollowUpComposerState.Surface.PHONE)
            );

        assertEquals(true, presentation.inputEnabled);
        assertEquals(true, presentation.submitEnabled);
    }

    @Test
    public void controlPresentationBlocksInputAndSubmitWhileBusy() {
        FollowUpComposerController.ControlPresentation presentation =
            FollowUpComposerController.resolveControlPresentation(
                FollowUpComposerState.idle("retry this", FollowUpComposerState.Surface.PHONE)
                    .asSubmitting()
            );

        assertEquals(false, presentation.inputEnabled);
        assertEquals(false, presentation.submitEnabled);
    }

    @Test
    public void dockedRetryVisibilitySuppressesLandscapePhoneChrome() {
        assertEquals(false, FollowUpComposerController.shouldShowDockedComposerRetry(true, true));
        assertEquals(true, FollowUpComposerController.shouldShowDockedComposerRetry(true, false));
        assertEquals(false, FollowUpComposerController.shouldShowDockedComposerRetry(false, false));
    }

    @Test
    public void nullStateResolvesToEmptyPhoneComposerDecisions() {
        FollowUpComposerController.SubmitDecision submit =
            FollowUpComposerController.resolveSubmit(null);
        FollowUpComposerController.RetryDecision retry =
            FollowUpComposerController.resolveRetry(null, " \t\n ");
        FollowUpComposerController.RetryPresentation presentation =
            FollowUpComposerController.resolveRetryPresentation(null, true);
        FollowUpComposerState completed =
            FollowUpComposerController.resolveGenerationSuccess(null);
        FollowUpComposerController.ControlPresentation controls =
            FollowUpComposerController.resolveControlPresentation(null);

        assertEquals(FollowUpComposerController.SubmitAction.EMPTY, submit.action);
        assertEquals("", submit.query);
        assertEquals(FollowUpComposerController.RetryAction.EMPTY, retry.action);
        assertEquals("", retry.query);
        assertEquals(false, presentation.visible);
        assertEquals(false, presentation.actionEnabled);
        assertEquals("", presentation.query);
        assertEquals("", completed.draftText);
        assertEquals(FollowUpComposerState.Surface.PHONE, completed.surface);
        assertEquals(true, controls.inputEnabled);
        assertEquals(false, controls.submitEnabled);
    }
}
