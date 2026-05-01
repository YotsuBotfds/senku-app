package com.senku.mobile;

import com.senku.ui.answer.AnswerContent;
import com.senku.ui.answer.AnswerContentFactory;
import com.senku.ui.answer.AnswerSurfaceInference;
import com.senku.ui.answer.AnswerSurfaceLabel;
import com.senku.ui.answer.Evidence;
import com.senku.ui.answer.Mode;
import com.senku.ui.answer.PaperAnswerCardModel;

final class DetailAnswerCardModelFactory {
    private static final String DEFAULT_SHOW_PROOF_LABEL = "Show proof";

    private DetailAnswerCardModelFactory() {
    }

    static PaperAnswerCardModel buildPaperModel(State state) {
        State safeState = State.normalize(state);
        AnswerSurfaceInference answerSurface = inferAnswerSurface(safeState);
        AnswerContent content = AnswerContentFactory.fromRenderedAnswer(
            safeState.formattedBody,
            safeState.sourceCount,
            buildHostLabel(safeState),
            AnswerContentFactory.parseElapsedSeconds(safeState.subtitle),
            buildEvidence(safeState),
            safeState.abstainRoute,
            safeState.uncertainFitRoute,
            safeState.showStreamingCursor,
            answerSurface.getAnswerSurfaceLabel(),
            answerSurface.getAnswerProvenance(),
            answerSurface.getAnswerSurfaceLabel() == AnswerSurfaceLabel.ReviewedCardEvidence,
            safeState.reviewedCardMetadata
        );
        return new PaperAnswerCardModel(content, Mode.Paper, DEFAULT_SHOW_PROOF_LABEL);
    }

    static Evidence buildEvidence(State state) {
        State safeState = State.normalize(state);
        if (safeState.abstainRoute || safeState.lowCoverageRoute) {
            return Evidence.None;
        }
        if (safeState.uncertainFitRoute) {
            return Evidence.Moderate;
        }
        if (safeState.deterministicRoute) {
            return Evidence.Strong;
        }
        if (!safeState.evidenceStrengthLabel.isEmpty()
            && safeState.strongEvidenceLabel.equals(safeState.evidenceStrengthLabel)) {
            return Evidence.Strong;
        }
        if (!safeState.evidenceStrengthLabel.isEmpty()
            && safeState.moderateEvidenceLabel.equals(safeState.evidenceStrengthLabel)) {
            return Evidence.Moderate;
        }
        return Evidence.None;
    }

    static String buildHostLabel(State state) {
        State safeState = State.normalize(state);
        String hostLabel = AnswerContentFactory.parseHost(safeState.subtitle);
        if (!hostLabel.isEmpty()) {
            return hostLabel;
        }
        if (safeState.deterministicRoute) {
            return "Deterministic";
        }
        if (safeState.abstainRoute || safeState.uncertainFitRoute) {
            return "Instant";
        }
        return "";
    }

    static AnswerSurfaceInference inferAnswerSurface(State state) {
        State safeState = State.normalize(state);
        return AnswerContentFactory.inferAnswerSurface(
            safeState.uncertainFitRoute
                ? OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT
                : safeState.answerResponseMode,
            safeState.abstainRoute,
            safeState.deterministicRoute,
            safeState.sourceCount,
            safeState.ruleId,
            safeState.reviewedCardMetadata
        );
    }

    static final class State {
        final String formattedBody;
        final String subtitle;
        final int sourceCount;
        final String evidenceStrengthLabel;
        final String strongEvidenceLabel;
        final String moderateEvidenceLabel;
        final boolean abstainRoute;
        final boolean lowCoverageRoute;
        final boolean uncertainFitRoute;
        final boolean deterministicRoute;
        final boolean showStreamingCursor;
        final OfflineAnswerEngine.AnswerMode answerResponseMode;
        final String ruleId;
        final ReviewedCardMetadata reviewedCardMetadata;

        State(
            String formattedBody,
            String subtitle,
            int sourceCount,
            String evidenceStrengthLabel,
            String strongEvidenceLabel,
            String moderateEvidenceLabel,
            boolean abstainRoute,
            boolean lowCoverageRoute,
            boolean uncertainFitRoute,
            boolean deterministicRoute,
            boolean showStreamingCursor,
            OfflineAnswerEngine.AnswerMode answerResponseMode,
            String ruleId,
            ReviewedCardMetadata reviewedCardMetadata
        ) {
            this.formattedBody = safe(formattedBody);
            this.subtitle = safe(subtitle);
            this.sourceCount = Math.max(0, sourceCount);
            this.evidenceStrengthLabel = safe(evidenceStrengthLabel);
            this.strongEvidenceLabel = safe(strongEvidenceLabel);
            this.moderateEvidenceLabel = safe(moderateEvidenceLabel);
            this.abstainRoute = abstainRoute;
            this.lowCoverageRoute = lowCoverageRoute;
            this.uncertainFitRoute = uncertainFitRoute;
            this.deterministicRoute = deterministicRoute;
            this.showStreamingCursor = showStreamingCursor;
            this.answerResponseMode = answerResponseMode;
            this.ruleId = safe(ruleId).trim();
            this.reviewedCardMetadata = ReviewedCardMetadata.normalize(reviewedCardMetadata);
        }

        static State normalize(State state) {
            return state == null
                ? new State(
                    "",
                    "",
                    0,
                    "",
                    "",
                    "",
                    false,
                    false,
                    false,
                    false,
                    false,
                    null,
                    "",
                    null
                )
                : state;
        }
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
