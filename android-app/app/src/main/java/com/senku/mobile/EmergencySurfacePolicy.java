package com.senku.mobile;

import java.util.Locale;

final class EmergencySurfacePolicy {
    static final String REASON_ELIGIBLE = "eligible";
    static final String REASON_NOT_DETERMINISTIC = "not_deterministic";
    static final String REASON_UNREVIEWED = "unreviewed";
    static final String REASON_GENERATED_ONLY = "generated_only";
    static final String REASON_LOW_COVERAGE = "low_coverage";
    static final String REASON_UNCERTAIN_FIT = "uncertain_fit";
    static final String REASON_GUIDE_READING = "guide_reading";
    static final String REASON_NOT_HIGH_RISK_EMERGENCY = "not_high_risk_emergency";

    private EmergencySurfacePolicy() {
    }

    static Decision evaluate(Input input) {
        Input normalized = Input.normalize(input);
        if (!normalized.deterministic) {
            return Decision.ineligible(REASON_NOT_DETERMINISTIC);
        }
        if (!isReviewed(normalized.reviewStatus, normalized.provenance)) {
            return Decision.ineligible(REASON_UNREVIEWED);
        }
        if (normalized.citedReviewedSourceCount <= 0) {
            return Decision.ineligible(REASON_GENERATED_ONLY);
        }
        if (isLowCoverage(normalized.coverageLabel, normalized.confidenceLabel)) {
            return Decision.ineligible(REASON_LOW_COVERAGE);
        }
        if (isGuideReading(normalized.answerMode)) {
            return Decision.ineligible(REASON_GUIDE_READING);
        }
        if (isUncertainFit(normalized.answerMode)) {
            return Decision.ineligible(REASON_UNCERTAIN_FIT);
        }
        if (!isHighRiskEmergency(normalized.ruleId, normalized.category)) {
            return Decision.ineligible(REASON_NOT_HIGH_RISK_EMERGENCY);
        }
        return Decision.eligible();
    }

    static boolean isHighRiskEmergency(String ruleId, String category) {
        String normalizedRuleId = normalize(ruleId);
        String normalizedCategory = normalize(category);
        return normalizedRuleId.startsWith("answer_card:")
            && hasHighRiskEmergencyRule(normalizedRuleId)
            && hasEmergencyCategory(normalizedCategory);
    }

    private static boolean hasHighRiskEmergencyRule(String normalizedRuleId) {
        return isPoisoningEmergency(normalizedRuleId)
            || isFoundryBurnHazardEmergency(normalizedRuleId)
            || normalizedRuleId.contains("choking")
            || normalizedRuleId.contains("sepsis")
            || normalizedRuleId.contains("meningitis")
            || normalizedRuleId.contains("internal_bleeding")
            || normalizedRuleId.contains("spreading_infection");
    }

    private static boolean isPoisoningEmergency(String normalizedRuleId) {
        return normalizedRuleId.contains("poisoning")
            && containsAny(
                normalizedRuleId,
                "ingestion",
                "swallowed",
                "overdose",
                "exposure",
                "inhalation"
            );
    }

    private static boolean isFoundryBurnHazardEmergency(String normalizedRuleId) {
        return "answer_card:foundry_casting_area_readiness_boundary".equals(normalizedRuleId);
    }

    private static boolean hasEmergencyCategory(String normalizedCategory) {
        return containsAny(
            normalizedCategory,
            "emergency",
            "danger",
            "burn hazard",
            "burn response",
            "hot work",
            "first aid",
            "red flag"
        );
    }

    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isReviewed(String reviewStatus, String provenance) {
        String normalizedStatus = normalize(reviewStatus);
        String normalizedProvenance = normalize(provenance);
        return ("reviewed".equals(normalizedStatus) || "pilot_reviewed".equals(normalizedStatus))
            && ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME.equals(normalizedProvenance);
    }

    private static boolean isLowCoverage(String coverageLabel, String confidenceLabel) {
        String normalizedCoverage = normalize(coverageLabel);
        String normalizedConfidence = normalize(confidenceLabel);
        return "low".equals(normalizedCoverage)
            || normalizedCoverage.contains("low coverage")
            || "low".equals(normalizedConfidence);
    }

    private static boolean isUncertainFit(String answerMode) {
        String normalized = normalize(answerMode);
        return "uncertain_fit".equals(normalized)
            || "abstain".equals(normalized)
            || normalized.contains("uncertain");
    }

    private static boolean isGuideReading(String answerMode) {
        return "guide_reading".equals(normalize(answerMode));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    static final class Input {
        final boolean deterministic;
        final String ruleId;
        final String category;
        final String reviewStatus;
        final String provenance;
        final String coverageLabel;
        final String confidenceLabel;
        final String answerMode;
        final int citedReviewedSourceCount;

        Input(
            boolean deterministic,
            String ruleId,
            String category,
            String reviewStatus,
            String provenance,
            String coverageLabel,
            String confidenceLabel,
            String answerMode,
            int citedReviewedSourceCount
        ) {
            this.deterministic = deterministic;
            this.ruleId = safe(ruleId).trim();
            this.category = safe(category).trim();
            this.reviewStatus = safe(reviewStatus).trim();
            this.provenance = safe(provenance).trim();
            this.coverageLabel = safe(coverageLabel).trim();
            this.confidenceLabel = safe(confidenceLabel).trim();
            this.answerMode = safe(answerMode).trim();
            this.citedReviewedSourceCount = Math.max(0, citedReviewedSourceCount);
        }

        private static Input normalize(Input input) {
            if (input == null) {
                return new Input(false, "", "", "", "", "", "", "", 0);
            }
            return input;
        }

        private static String safe(String value) {
            return value == null ? "" : value;
        }
    }

    static final class Decision {
        final boolean eligible;
        final String reason;

        private Decision(boolean eligible, String reason) {
            this.eligible = eligible;
            this.reason = reason;
        }

        private static Decision eligible() {
            return new Decision(true, REASON_ELIGIBLE);
        }

        private static Decision ineligible(String reason) {
            return new Decision(false, reason);
        }
    }
}
