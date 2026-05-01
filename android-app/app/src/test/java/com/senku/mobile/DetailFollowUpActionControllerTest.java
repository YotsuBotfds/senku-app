package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class DetailFollowUpActionControllerTest {
    @Test
    public void phoneSubmitStartsGenerationWithNormalizedQuery() {
        FollowUpComposerState phoneState = FollowUpComposerState.idle(
            "  what changed overnight?\r\n",
            FollowUpComposerState.Surface.PHONE
        );

        DetailFollowUpActionController.Decision decision =
            DetailFollowUpActionController.resolvePhoneSubmit(phoneState);

        assertEquals(DetailFollowUpActionController.Action.START_GENERATION, decision.action);
        assertEquals(DetailFollowUpActionController.Target.PHONE_FOLLOWUP, decision.target);
        assertEquals("what changed overnight?", decision.query);
    }

    @Test
    public void tabletSubmitUsesRawQueryOverRestoredDraft() {
        FollowUpComposerState tabletState = FollowUpComposerState.idle(
            "  stale restored draft  ",
            FollowUpComposerState.Surface.TABLET
        );

        DetailFollowUpActionController.Decision decision =
            DetailFollowUpActionController.resolveTabletSubmit(tabletState, "  raw tablet follow up  ");

        assertEquals(DetailFollowUpActionController.Action.START_GENERATION, decision.action);
        assertEquals(DetailFollowUpActionController.Target.TABLET_FOLLOWUP, decision.target);
        assertEquals("raw tablet follow up", decision.query);
    }

    @Test
    public void submitReportsEmptyBeforeBlocked() {
        FollowUpComposerState blockedBlank = new FollowUpComposerState(
            " \n\t ",
            true,
            true,
            null,
            FollowUpComposerState.Surface.PHONE,
            null,
            null
        );

        DetailFollowUpActionController.Decision decision =
            DetailFollowUpActionController.resolvePhoneSubmit(blockedBlank);

        assertEquals(DetailFollowUpActionController.Action.EMPTY, decision.action);
        assertEquals(DetailFollowUpActionController.Target.PHONE_FOLLOWUP, decision.target);
        assertEquals("", decision.query);
    }

    @Test
    public void submitBlocksDuplicateWithoutDroppingQuery() {
        FollowUpComposerState duplicateSubmit = FollowUpComposerState.idle(
            "  inspect the ridge line  ",
            FollowUpComposerState.Surface.TABLET
        ).asSubmitting();

        DetailFollowUpActionController.Decision decision =
            DetailFollowUpActionController.resolveTabletSubmit(duplicateSubmit, duplicateSubmit.draftText);

        assertEquals(DetailFollowUpActionController.Action.BLOCKED, decision.action);
        assertEquals(DetailFollowUpActionController.Target.TABLET_FOLLOWUP, decision.target);
        assertEquals("inspect the ridge line", decision.query);
    }

    @Test
    public void nullPhoneSubmitResolvesToEmptyPhoneTarget() {
        DetailFollowUpActionController.Decision decision =
            DetailFollowUpActionController.resolvePhoneSubmit(null);

        assertEquals(DetailFollowUpActionController.Action.EMPTY, decision.action);
        assertEquals(DetailFollowUpActionController.Target.PHONE_FOLLOWUP, decision.target);
        assertEquals("", decision.query);
    }

    @Test
    public void nullTabletSubmitResolvesToEmptyTabletTarget() {
        DetailFollowUpActionController.Decision decision =
            DetailFollowUpActionController.resolveTabletSubmit(null, null);

        assertEquals(DetailFollowUpActionController.Action.EMPTY, decision.action);
        assertEquals(DetailFollowUpActionController.Target.TABLET_FOLLOWUP, decision.target);
        assertEquals("", decision.query);
    }

    @Test
    public void phoneRetryStartsGenerationFromFailedQueryBeforeVisibleDraftFallback() {
        FollowUpComposerState phoneState = FollowUpComposerState.idle(
            "visible edited draft",
            FollowUpComposerState.Surface.PHONE
        ).withFailure("offline failure", "  failed query  ");

        DetailFollowUpActionController.Decision decision =
            DetailFollowUpActionController.resolveRetry(
                false,
                phoneState,
                "visible fallback",
                null,
                null
            );

        assertEquals(DetailFollowUpActionController.Action.START_GENERATION, decision.action);
        assertEquals(DetailFollowUpActionController.Target.PHONE_FOLLOWUP, decision.target);
        assertEquals("failed query", decision.query);
    }

    @Test
    public void tabletRetryFallsBackToVisibleDraftAndTargetsTabletGeneration() {
        FollowUpComposerState tabletState = FollowUpComposerState.idle(
            "",
            FollowUpComposerState.Surface.TABLET
        );

        DetailFollowUpActionController.Decision decision =
            DetailFollowUpActionController.resolveRetry(
                true,
                null,
                null,
                tabletState,
                "  retry visible draft\r "
            );

        assertEquals(DetailFollowUpActionController.Action.START_GENERATION, decision.action);
        assertEquals(DetailFollowUpActionController.Target.TABLET_FOLLOWUP, decision.target);
        assertEquals("retry visible draft", decision.query);
    }

    @Test
    public void retryReportsEmptyWhenRetryAndVisibleDraftAreBlank() {
        FollowUpComposerState phoneState = FollowUpComposerState.idle(
            "",
            FollowUpComposerState.Surface.PHONE
        );

        DetailFollowUpActionController.Decision decision =
            DetailFollowUpActionController.resolveRetry(
                false,
                phoneState,
                " \n\t ",
                null,
                null
            );

        assertEquals(DetailFollowUpActionController.Action.EMPTY, decision.action);
        assertEquals(DetailFollowUpActionController.Target.PHONE_FOLLOWUP, decision.target);
        assertEquals("", decision.query);
    }

    @Test
    public void retryPassesResolvedQueryThroughSubmitDedupeGate() {
        FollowUpComposerState stalledPhoneState = new FollowUpComposerState(
            "",
            true,
            true,
            null,
            FollowUpComposerState.Surface.PHONE,
            null,
            "  active stalled query  "
        );

        DetailFollowUpActionController.Decision decision =
            DetailFollowUpActionController.resolveRetry(
                false,
                stalledPhoneState,
                null,
                null,
                null
            );

        assertEquals(DetailFollowUpActionController.Action.BLOCKED, decision.action);
        assertEquals(DetailFollowUpActionController.Target.PHONE_FOLLOWUP, decision.target);
        assertEquals("active stalled query", decision.query);
    }

    @Test
    public void busyFailedQueryRetryBlocksDuplicateGeneration() {
        FollowUpComposerState busyFailedPhoneState = new FollowUpComposerState(
            "visible draft",
            true,
            true,
            "offline failure",
            FollowUpComposerState.Surface.PHONE,
            "  failed query  ",
            null
        );

        DetailFollowUpActionController.Decision decision =
            DetailFollowUpActionController.resolveRetry(
                false,
                busyFailedPhoneState,
                "visible fallback",
                null,
                null
            );

        assertEquals(DetailFollowUpActionController.Action.BLOCKED, decision.action);
        assertEquals(DetailFollowUpActionController.Target.PHONE_FOLLOWUP, decision.target);
        assertEquals("failed query", decision.query);
    }

    @Test
    public void visibleStallRetryClickStillBlocksDuplicateGeneration() {
        FollowUpComposerState stalledPhoneState = new FollowUpComposerState(
            "",
            true,
            true,
            null,
            FollowUpComposerState.Surface.PHONE,
            null,
            "  active stalled query  "
        );

        FollowUpComposerController.RetryPresentation presentation =
            FollowUpComposerController.resolveRetryPresentation(stalledPhoneState, true);
        DetailFollowUpActionController.Decision decision =
            DetailFollowUpActionController.resolveRetry(
                false,
                stalledPhoneState,
                null,
                null,
                null
            );

        assertEquals(true, presentation.visible);
        assertEquals(true, presentation.actionEnabled);
        assertEquals("active stalled query", presentation.query);
        assertEquals(DetailFollowUpActionController.Action.BLOCKED, decision.action);
        assertEquals(DetailFollowUpActionController.Target.PHONE_FOLLOWUP, decision.target);
        assertEquals("active stalled query", decision.query);
    }

    @Test
    public void visibleTabletStallRetryClickStillBlocksDuplicateGeneration() {
        FollowUpComposerState stalledTabletState = new FollowUpComposerState(
            "",
            true,
            true,
            null,
            FollowUpComposerState.Surface.TABLET,
            null,
            "  active tablet stalled query  "
        );

        FollowUpComposerController.RetryPresentation presentation =
            FollowUpComposerController.resolveRetryPresentation(stalledTabletState, true);
        DetailFollowUpActionController.Decision decision =
            DetailFollowUpActionController.resolveRetry(
                true,
                null,
                null,
                stalledTabletState,
                null
            );

        assertEquals(true, presentation.visible);
        assertEquals(true, presentation.actionEnabled);
        assertEquals("active tablet stalled query", presentation.query);
        assertEquals(DetailFollowUpActionController.Action.BLOCKED, decision.action);
        assertEquals(DetailFollowUpActionController.Target.TABLET_FOLLOWUP, decision.target);
        assertEquals("active tablet stalled query", decision.query);
    }

    @Test
    public void nullInputsResolveToEmptyCurrentSurfaceRetry() {
        DetailFollowUpActionController.Decision phoneDecision =
            DetailFollowUpActionController.resolveRetry(false, null, null, null, null);
        DetailFollowUpActionController.Decision tabletDecision =
            DetailFollowUpActionController.resolveRetry(true, null, null, null, null);

        assertEquals(DetailFollowUpActionController.Action.EMPTY, phoneDecision.action);
        assertEquals(DetailFollowUpActionController.Target.PHONE_FOLLOWUP, phoneDecision.target);
        assertEquals("", phoneDecision.query);
        assertEquals(DetailFollowUpActionController.Action.EMPTY, tabletDecision.action);
        assertEquals(DetailFollowUpActionController.Target.TABLET_FOLLOWUP, tabletDecision.target);
        assertEquals("", tabletDecision.query);
    }
}
