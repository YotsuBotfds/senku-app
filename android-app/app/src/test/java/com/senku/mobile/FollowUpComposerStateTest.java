package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class FollowUpComposerStateTest {
    @Test
    public void idleStateNormalizesDraftAndDefaultsToPhoneSurface() {
        FollowUpComposerState state = FollowUpComposerState.idle("  what next?\r\n", null);

        assertEquals("what next?", state.draftText);
        assertEquals("what next?", state.submitQuery());
        assertEquals(FollowUpComposerState.Surface.PHONE, state.surface);
        assertTrue(state.isPhone());
        assertFalse(state.isTablet());
        assertTrue(state.hasDraft());
        assertTrue(state.inputEnabled());
        assertTrue(state.submitEnabled());
    }

    @Test
    public void blankDraftDisablesSubmitWithoutBlockingInput() {
        FollowUpComposerState state = FollowUpComposerState.idle(" \n\t ", FollowUpComposerState.Surface.TABLET);

        assertEquals("", state.draftText);
        assertEquals(FollowUpComposerState.Surface.TABLET, state.surface);
        assertTrue(state.isTablet());
        assertFalse(state.hasDraft());
        assertTrue(state.inputEnabled());
        assertFalse(state.submitEnabled());
    }

    @Test
    public void busyOrSubmittingBlocksInputSubmitAndRetry() {
        FollowUpComposerState busy = new FollowUpComposerState(
            "draft",
            true,
            false,
            null,
            FollowUpComposerState.Surface.PHONE,
            "retry me",
            null
        );
        FollowUpComposerState submitting = FollowUpComposerState.idle(
            "draft",
            FollowUpComposerState.Surface.PHONE
        ).asSubmitting();

        assertTrue(busy.isBlocked());
        assertFalse(busy.inputEnabled());
        assertFalse(busy.submitEnabled());
        assertFalse(busy.retryEnabled());

        assertTrue(submitting.isBlocked());
        assertFalse(submitting.inputEnabled());
        assertFalse(submitting.submitEnabled());
        assertFalse(submitting.retryEnabled());
    }

    @Test
    public void failureMessageIsNormalizedAndDoesNotEnableRetryByItself() {
        FollowUpComposerState state = FollowUpComposerState.idle(
            "try again",
            FollowUpComposerState.Surface.PHONE
        ).withFailure("  Network unavailable.  ", " ");

        assertTrue(state.hasFailure());
        assertEquals("Network unavailable.", state.failureMessage);
        assertFalse(state.retryEnabled());
    }

    @Test
    public void retryUsesLastFailedQueryBeforeActiveRetryQuery() {
        FollowUpComposerState state = new FollowUpComposerState(
            "",
            false,
            false,
            "failed",
            FollowUpComposerState.Surface.TABLET,
            "  failed follow-up  ",
            "current answer title"
        );

        assertEquals("failed follow-up", state.retryQuery());
        assertTrue(state.retryEnabled());
    }

    @Test
    public void retryFallsBackToActiveRetryQueryWhenLastFailureIsMissing() {
        FollowUpComposerState state = new FollowUpComposerState(
            "",
            false,
            false,
            null,
            FollowUpComposerState.Surface.PHONE,
            null,
            "  current answer title  "
        );

        assertEquals("current answer title", state.retryQuery());
        assertTrue(state.retryEnabled());
    }

    @Test
    public void retryQuerySelectionNormalizesLineEndingsAndEmptyInputs() {
        assertEquals("first\nsecond", FollowUpComposerState.selectRetryQuery(" first\r\nsecond ", "fallback"));
        assertEquals("fallback", FollowUpComposerState.selectRetryQuery(" \r ", " fallback "));
        assertEquals("", FollowUpComposerState.selectRetryQuery(null, null));
    }

    @Test
    public void copyHelpersPreserveExistingStateDecisions() {
        FollowUpComposerState state = FollowUpComposerState.idle(
            "draft",
            FollowUpComposerState.Surface.TABLET
        ).withFailure("failed", "retry");

        FollowUpComposerState changedDraft = state.withDraft(" next ");
        FollowUpComposerState busy = changedDraft.withBusy(true);

        assertEquals("next", changedDraft.draftText);
        assertEquals("failed", changedDraft.failureMessage);
        assertEquals("retry", changedDraft.retryQuery());
        assertEquals(FollowUpComposerState.Surface.TABLET, changedDraft.surface);
        assertTrue(changedDraft.submitEnabled());

        assertFalse(busy.submitEnabled());
        assertFalse(busy.retryEnabled());
    }
}
