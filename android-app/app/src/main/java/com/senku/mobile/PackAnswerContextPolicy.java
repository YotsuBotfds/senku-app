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

    static boolean shouldKeepGuideSectionForContext(
        String preferredStructure,
        boolean isAnchorSection,
        int sectionBonus,
        boolean prefersRoofWeatherproofRouteAnchor,
        boolean hasRoofWeatherproofDistractorSignal,
        boolean prefersGovernanceTrustRepairContext,
        boolean hasGovernanceSupportMixDistractor,
        boolean hasGovernanceTrustRepairSignal
    ) {
        if (isAnchorSection) {
            return true;
        }
        if (("water_storage".equals(preferredStructure) || "water_distribution".equals(preferredStructure))
            && sectionBonus < 0) {
            return false;
        }
        if ("cabin_house".equals(preferredStructure)
            && prefersRoofWeatherproofRouteAnchor
            && sectionBonus <= 0
            && hasRoofWeatherproofDistractorSignal) {
            return false;
        }
        if ("community_governance".equals(preferredStructure)
            && prefersGovernanceTrustRepairContext
            && hasGovernanceSupportMixDistractor
            && !hasGovernanceTrustRepairSignal) {
            return false;
        }
        return true;
    }

    private static boolean hasExplicitWaterDistributionTopic(PackRepository.QueryTerms queryTerms) {
        return queryTerms != null
            && queryTerms.metadataProfile != null
            && queryTerms.metadataProfile.hasExplicitTopic("water_distribution");
    }
}
