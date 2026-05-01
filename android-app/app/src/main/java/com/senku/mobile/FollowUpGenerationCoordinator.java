package com.senku.mobile;

final class FollowUpGenerationCoordinator {
    static final class GenerationStartDecision {
        final String draftText;
        final String lastFailedQuery;

        private GenerationStartDecision(String draftText, String lastFailedQuery) {
            this.draftText = FollowUpComposerState.normalizeDraft(draftText);
            this.lastFailedQuery = FollowUpComposerState.normalizeDraft(lastFailedQuery);
        }
    }

    private FollowUpGenerationCoordinator() {
    }

    static GenerationStartDecision resolveGenerationStart(FollowUpComposerState state) {
        FollowUpComposerState started = FollowUpComposerController.resolveGenerationStart(state);
        return new GenerationStartDecision(
            started.draftText,
            ""
        );
    }
}
