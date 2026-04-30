package com.senku.mobile;

final class PackAnswerContextPolicy {
    private PackAnswerContextPolicy() {
    }

    static int anchorGuideBudget(PackRepository.QueryTerms queryTerms, boolean diversifyContext, int limit) {
        if (!diversifyContext) {
            return Math.max(1, limit - 1);
        }
        if (hasExplicitWaterDistributionTopic(queryTerms)) {
            return Math.min(limit, 3);
        }
        return Math.min(limit, 2);
    }

    static boolean shouldSeedAnchorBeforeSupport(PackRepository.QueryTerms queryTerms, boolean diversifyContext) {
        return diversifyContext && hasExplicitWaterDistributionTopic(queryTerms);
    }

    private static boolean hasExplicitWaterDistributionTopic(PackRepository.QueryTerms queryTerms) {
        return queryTerms != null
            && queryTerms.metadataProfile != null
            && queryTerms.metadataProfile.hasExplicitTopic("water_distribution");
    }
}
