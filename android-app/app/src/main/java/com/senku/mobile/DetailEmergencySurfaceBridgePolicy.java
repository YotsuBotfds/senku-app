package com.senku.mobile;

import java.util.Locale;

final class DetailEmergencySurfaceBridgePolicy {
    private DetailEmergencySurfaceBridgePolicy() {
    }

    static EmergencySurfacePolicy.Decision evaluate(
        boolean detailAnswerMode,
        boolean deterministicRoute,
        String ruleId,
        String category,
        ReviewedCardMetadata reviewedCardMetadata,
        boolean lowCoverageRoute,
        OfflineAnswerEngine.ConfidenceLabel confidenceLabel,
        OfflineAnswerEngine.AnswerMode responseMode
    ) {
        return EmergencySurfacePolicy.evaluate(buildInput(
            detailAnswerMode,
            deterministicRoute,
            ruleId,
            category,
            reviewedCardMetadata,
            lowCoverageRoute,
            confidenceLabel,
            responseMode
        ));
    }

    static EmergencySurfacePolicy.Input buildInput(
        boolean detailAnswerMode,
        boolean deterministicRoute,
        String ruleId,
        String category,
        ReviewedCardMetadata reviewedCardMetadata,
        boolean lowCoverageRoute,
        OfflineAnswerEngine.ConfidenceLabel confidenceLabel,
        OfflineAnswerEngine.AnswerMode responseMode
    ) {
        ReviewedCardMetadata metadata = ReviewedCardMetadata.normalize(reviewedCardMetadata);
        return new EmergencySurfacePolicy.Input(
            deterministicRoute,
            ruleId,
            policyCategory(ruleId, category, metadata),
            metadata.reviewStatus,
            metadata.provenance,
            lowCoverageRoute ? "low" : "",
            confidenceLabel == null ? "" : confidenceLabel.name(),
            detailAnswerMode ? answerModeValue(responseMode) : "guide_reading",
            metadata.citedReviewedSourceGuideIds.size()
        );
    }

    static String policyCategory(
        String ruleId,
        String category,
        ReviewedCardMetadata reviewedCardMetadata
    ) {
        ReviewedCardMetadata metadata = ReviewedCardMetadata.normalize(reviewedCardMetadata);
        String normalizedRuleId = safe(ruleId).trim().toLowerCase(Locale.US);
        String normalizedCardRuleId = ReviewedCardMetadata.answerCardRuleId(
            safe(metadata.cardId).trim().toLowerCase(Locale.US)
        );
        if (metadata.isPresent()
            && !safe(metadata.cardId).trim().isEmpty()
            && normalizedRuleId.equals(normalizedCardRuleId)) {
            return (safe(category).trim() + " reviewed emergency answer card").trim();
        }
        return safe(category).trim();
    }

    private static String answerModeValue(OfflineAnswerEngine.AnswerMode mode) {
        return mode == null ? "" : mode.name();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
