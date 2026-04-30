package com.senku.mobile;

final class FollowUpComposerController {
    enum SubmitAction {
        EMPTY,
        BLOCKED,
        SUBMIT
    }

    enum RetryAction {
        EMPTY,
        RETRY
    }

    static final class SubmitDecision {
        final SubmitAction action;
        final String query;

        private SubmitDecision(SubmitAction action, String query) {
            this.action = action;
            this.query = FollowUpComposerState.normalizeDraft(query);
        }
    }

    static final class RetryDecision {
        final RetryAction action;
        final String query;

        private RetryDecision(RetryAction action, String query) {
            this.action = action;
            this.query = FollowUpComposerState.normalizeDraft(query);
        }
    }

    private FollowUpComposerController() {
    }

    static SubmitDecision resolveSubmit(FollowUpComposerState state) {
        FollowUpComposerState safeState = safeState(state);
        String query = safeState.submitQuery();
        if (query.isEmpty()) {
            return new SubmitDecision(SubmitAction.EMPTY, "");
        }
        if (!safeState.canSubmit()) {
            return new SubmitDecision(SubmitAction.BLOCKED, query);
        }
        return new SubmitDecision(SubmitAction.SUBMIT, query);
    }

    static RetryDecision resolveRetry(FollowUpComposerState state, String visibleDraftFallback) {
        FollowUpComposerState safeState = safeState(state);
        String query = safeState.retryQuery();
        if (query.isEmpty()) {
            query = FollowUpComposerState.normalizeDraft(visibleDraftFallback);
        }
        if (query.isEmpty()) {
            return new RetryDecision(RetryAction.EMPTY, "");
        }
        return new RetryDecision(RetryAction.RETRY, query);
    }

    private static FollowUpComposerState safeState(FollowUpComposerState state) {
        return state == null ? FollowUpComposerState.idle("", FollowUpComposerState.Surface.PHONE) : state;
    }
}
