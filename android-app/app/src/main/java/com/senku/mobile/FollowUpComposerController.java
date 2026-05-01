package com.senku.mobile;

final class FollowUpComposerController {
    enum SubmitAction {
        EMPTY,
        BLOCKED,
        SUBMIT
    }

    enum SubmitRoute {
        EMPTY_INPUT,
        PHONE_FOLLOWUP
    }

    enum RetryAction {
        EMPTY,
        RETRY
    }

    static final class RetryPresentation {
        final boolean visible;
        final boolean actionEnabled;
        final String query;

        private RetryPresentation(boolean visible, boolean actionEnabled, String query) {
            this.visible = visible;
            this.actionEnabled = actionEnabled;
            this.query = FollowUpComposerState.normalizeDraft(query);
        }
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

    static SubmitDecision resolveSubmit(
        String rawQuery,
        FollowUpComposerState.Surface surface,
        boolean busy
    ) {
        return resolveSubmit(new FollowUpComposerState(rawQuery, busy, busy, "", surface, null, null));
    }

    static SubmitRoute resolvePhoneSubmitRoute(String rawQuery) {
        SubmitDecision decision = resolveSubmit(rawQuery, FollowUpComposerState.Surface.PHONE, false);
        return decision.action == SubmitAction.EMPTY
            ? SubmitRoute.EMPTY_INPUT
            : SubmitRoute.PHONE_FOLLOWUP;
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

    static RetryPresentation resolveRetryPresentation(FollowUpComposerState state, boolean surfaceAllowsRetry) {
        FollowUpComposerState safeState = safeState(state);
        boolean visible = surfaceAllowsRetry && safeState.retryVisible();
        return new RetryPresentation(
            visible,
            visible && safeState.retryActionEnabled(),
            visible ? safeState.retryQuery() : ""
        );
    }

    static FollowUpComposerState resolveGenerationSuccess(FollowUpComposerState state) {
        FollowUpComposerState safeState = safeState(state);
        return FollowUpComposerState.idle("", safeState.surface);
    }

    static FollowUpComposerState resolveGenerationFailure(
        FollowUpComposerState state,
        String failureMessage,
        String submittedQuery
    ) {
        FollowUpComposerState safeState = safeState(state);
        String retryQuery = FollowUpComposerState.normalizeDraft(submittedQuery);
        if (retryQuery.isEmpty()) {
            retryQuery = safeState.submitQuery();
        }
        return FollowUpComposerState.idle(retryQuery, safeState.surface)
            .withFailure(failureMessage, retryQuery);
    }

    private static FollowUpComposerState safeState(FollowUpComposerState state) {
        return state == null ? FollowUpComposerState.idle("", FollowUpComposerState.Surface.PHONE) : state;
    }
}
