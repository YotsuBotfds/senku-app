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

    static final class StallStateDecision {
        final boolean stalled;
        final boolean changed;

        private StallStateDecision(boolean stalled, boolean changed) {
            this.stalled = stalled;
            this.changed = changed;
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

    static StallStateDecision resolveStallState(
        boolean firstStreamingChunkSeen,
        long generationStartedAtMs,
        long nowMs,
        long stallNoticeMs,
        boolean currentStallNoticeVisible
    ) {
        long effectiveStartedAtMs = generationStartedAtMs > 0L ? generationStartedAtMs : nowMs;
        long elapsedMs = Math.max(0L, nowMs - effectiveStartedAtMs);
        long thresholdMs = Math.max(0L, stallNoticeMs);
        boolean stalled = !firstStreamingChunkSeen && elapsedMs >= thresholdMs;
        return new StallStateDecision(stalled, stalled != currentStallNoticeVisible);
    }
}
