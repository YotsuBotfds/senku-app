package com.senku.mobile;

final class DetailFollowUpActionController {
    enum Action {
        EMPTY,
        BLOCKED,
        START_GENERATION
    }

    enum Target {
        PHONE_FOLLOWUP,
        TABLET_FOLLOWUP
    }

    static final class Decision {
        final Action action;
        final Target target;
        final String query;

        private Decision(Action action, Target target, String query) {
            this.action = action;
            this.target = target;
            this.query = FollowUpComposerState.normalizeDraft(query);
        }
    }

    private DetailFollowUpActionController() {
    }

    static Decision resolvePhoneSubmit(FollowUpComposerState phoneState) {
        return submit(phoneState, Target.PHONE_FOLLOWUP);
    }

    static Decision resolveTabletSubmit(FollowUpComposerState tabletState, String rawQuery) {
        FollowUpComposerState safeState = safeState(tabletState, FollowUpComposerState.Surface.TABLET)
            .withDraft(rawQuery);
        return submit(safeState, Target.TABLET_FOLLOWUP);
    }

    static Decision resolveRetry(
        boolean tabletComposeMode,
        FollowUpComposerState phoneState,
        String phoneVisibleDraftFallback,
        FollowUpComposerState tabletState,
        String tabletVisibleDraftFallback
    ) {
        if (tabletComposeMode) {
            return retry(tabletState, tabletVisibleDraftFallback, Target.TABLET_FOLLOWUP);
        }
        return retry(phoneState, phoneVisibleDraftFallback, Target.PHONE_FOLLOWUP);
    }

    private static Decision submit(FollowUpComposerState state, Target target) {
        FollowUpComposerController.SubmitDecision submit =
            FollowUpComposerController.resolveSubmit(state);
        return fromSubmitDecision(submit, target);
    }

    private static Decision retry(
        FollowUpComposerState state,
        String visibleDraftFallback,
        Target target
    ) {
        FollowUpComposerController.RetryDecision retry =
            FollowUpComposerController.resolveRetry(state, visibleDraftFallback);
        if (retry.action == FollowUpComposerController.RetryAction.EMPTY) {
            return new Decision(Action.EMPTY, target, "");
        }

        FollowUpComposerState retryState = safeState(
            state,
            surfaceForTarget(target)
        ).withDraft(retry.query);
        return submit(retryState, target);
    }

    private static Decision fromSubmitDecision(
        FollowUpComposerController.SubmitDecision submit,
        Target target
    ) {
        if (submit.action == FollowUpComposerController.SubmitAction.EMPTY) {
            return new Decision(Action.EMPTY, target, "");
        }
        if (submit.action == FollowUpComposerController.SubmitAction.BLOCKED) {
            return new Decision(Action.BLOCKED, target, submit.query);
        }
        return new Decision(Action.START_GENERATION, target, submit.query);
    }

    private static FollowUpComposerState.Surface surfaceForTarget(Target target) {
        return target == Target.TABLET_FOLLOWUP
            ? FollowUpComposerState.Surface.TABLET
            : FollowUpComposerState.Surface.PHONE;
    }

    private static FollowUpComposerState safeState(
        FollowUpComposerState state,
        FollowUpComposerState.Surface fallbackSurface
    ) {
        return state == null ? FollowUpComposerState.idle("", fallbackSurface) : state;
    }
}
