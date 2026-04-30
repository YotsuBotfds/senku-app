package com.senku.mobile;

import java.util.Objects;

public final class FollowUpComposerState {
    public enum Surface {
        PHONE,
        TABLET
    }

    public final String draftText;
    public final boolean busy;
    public final boolean submitting;
    public final String failureMessage;
    public final Surface surface;
    public final String lastFailedQuery;
    public final String activeRetryQuery;

    public FollowUpComposerState(
        String draftText,
        boolean busy,
        boolean submitting,
        String failureMessage,
        Surface surface,
        String lastFailedQuery,
        String activeRetryQuery
    ) {
        this.draftText = normalizeDraft(draftText);
        this.busy = busy;
        this.submitting = submitting;
        this.failureMessage = normalizeMessage(failureMessage);
        this.surface = surface == null ? Surface.PHONE : surface;
        this.lastFailedQuery = normalizeDraft(lastFailedQuery);
        this.activeRetryQuery = normalizeDraft(activeRetryQuery);
    }

    public static FollowUpComposerState idle(String draftText, Surface surface) {
        return new FollowUpComposerState(draftText, false, false, null, surface, null, null);
    }

    public FollowUpComposerState withDraft(String draftText) {
        return new FollowUpComposerState(
            draftText,
            busy,
            submitting,
            failureMessage,
            surface,
            lastFailedQuery,
            activeRetryQuery
        );
    }

    public FollowUpComposerState withBusy(boolean busy) {
        return new FollowUpComposerState(
            draftText,
            busy,
            submitting,
            failureMessage,
            surface,
            lastFailedQuery,
            activeRetryQuery
        );
    }

    public FollowUpComposerState asSubmitting() {
        return new FollowUpComposerState(draftText, true, true, null, surface, lastFailedQuery, activeRetryQuery);
    }

    public FollowUpComposerState withFailure(String failureMessage, String lastFailedQuery) {
        return new FollowUpComposerState(
            draftText,
            false,
            false,
            failureMessage,
            surface,
            lastFailedQuery,
            activeRetryQuery
        );
    }

    public boolean hasDraft() {
        return !draftText.isEmpty();
    }

    public boolean hasFailure() {
        return !failureMessage.isEmpty();
    }

    public boolean isBlocked() {
        return busy || submitting;
    }

    public boolean isPhone() {
        return surface == Surface.PHONE;
    }

    public boolean isTablet() {
        return surface == Surface.TABLET;
    }

    public boolean inputEnabled() {
        return !isBlocked();
    }

    public boolean submitEnabled() {
        return !isBlocked() && hasDraft();
    }

    public boolean retryEnabled() {
        return !isBlocked() && !retryQuery().isEmpty();
    }

    public boolean retryVisible() {
        return retryEnabled() || hasActiveRetryQuery();
    }

    public boolean retryActionEnabled() {
        return retryEnabled() || hasActiveRetryQuery();
    }

    public boolean canSubmit() {
        return submitEnabled();
    }

    public String submitQuery() {
        return draftText;
    }

    public String retryQuery() {
        return selectRetryQuery(lastFailedQuery, activeRetryQuery);
    }

    public static String selectRetryQuery(String lastFailedQuery, String activeRetryQuery) {
        String failed = normalizeDraft(lastFailedQuery);
        if (!failed.isEmpty()) {
            return failed;
        }
        return normalizeDraft(activeRetryQuery);
    }

    public static String normalizeSavedDraft(String rawDraft) {
        return normalizeDraft(rawDraft);
    }

    public static String resolveDraftForSave(String visibleDraft, String restoredDraft) {
        if (visibleDraft == null) {
            return normalizeSavedDraft(restoredDraft);
        }
        return normalizeSavedDraft(visibleDraft);
    }

    private boolean hasActiveRetryQuery() {
        return !activeRetryQuery.isEmpty();
    }

    static String normalizeDraft(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace("\r\n", "\n").replace('\r', '\n').trim();
        return normalized;
    }

    private static String normalizeMessage(String value) {
        return Objects.toString(value, "").trim();
    }
}
