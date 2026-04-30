package com.senku.mobile;

import java.util.Locale;

final class AnswerAnchorPolicy {
    private static final Locale QUERY_LOCALE = Locale.US;
    private static final int RANKED_ANCHOR_KEEP_DELTA = 12;

    private AnswerAnchorPolicy() {
    }

    static boolean shouldPreferRouteAnchorOverRankedGuide(
        String preferredStructureType,
        SearchResult rankedAnchor
    ) {
        String normalizedStructurePreference = safe(preferredStructureType).trim().toLowerCase(QUERY_LOCALE);
        if (!requiresSpecializedRouteAnchorSignal(normalizedStructurePreference) || rankedAnchor == null) {
            return false;
        }
        if (!"guide-focus".equals(safe(rankedAnchor.retrievalMode).trim().toLowerCase(QUERY_LOCALE))) {
            return false;
        }
        if (!safe(rankedAnchor.sectionHeading).trim().isEmpty()) {
            return false;
        }
        String normalizedStructure = safe(rankedAnchor.structureType).trim().toLowerCase(QUERY_LOCALE);
        return !normalizedStructurePreference.equals(normalizedStructure);
    }

    static int routeFocusedAnchorScore(
        int supportWithMetadata,
        int originalIndex,
        int anchorAlignmentBonus,
        int broadRouteSectionPreferenceBonus,
        int cabinSiteSelectionAnchorBias,
        int roofWeatherproofAnchorBias
    ) {
        int score = Math.max(1, supportWithMetadata);
        score += Math.max(0, 12 - originalIndex);
        score += anchorAlignmentBonus;
        score += broadRouteSectionPreferenceBonus;
        score += cabinSiteSelectionAnchorBias;
        score += roofWeatherproofAnchorBias;
        return score;
    }

    static SearchResult chooseRankedOrRoutedAnchor(AnchorChoice choice) {
        if (choice == null || !choice.routeFocused) {
            return choice == null ? null : choice.rankedAnchor;
        }
        if (choice.preferRouteAnchorOverRankedGuide) {
            return choice.routedAnchor;
        }
        if (choice.preferCabinSiteSelectionRouteAnchor
            && choice.routedHasCabinSiteSelectionSignal
            && !choice.rankedHasCabinSiteSelectionSignal) {
            return choice.routedAnchor;
        }
        if (choice.preferRoofWeatherproofRouteAnchor
            && choice.routedHasRoofWeatherproofSignal
            && (choice.rankedHasRoofWeatherproofDistractorSignal
                || !choice.rankedHasRoofWeatherproofSignal)) {
            return choice.routedAnchor;
        }
        if (choice.rankedGuideFocusWaterDistributionFallback) {
            return choice.routedAnchor;
        }
        if (choice.sameGuideGroup) {
            return choice.routedAnchor;
        }

        int rankedScore = Math.max(1, choice.rankedSupportWithMetadata);
        int routedScore = Math.max(1, choice.routedSupportWithMetadata);
        return rankedScore >= routedScore + RANKED_ANCHOR_KEEP_DELTA
            ? choice.rankedAnchor
            : choice.routedAnchor;
    }

    private static boolean requiresSpecializedRouteAnchorSignal(String preferredStructureType) {
        return "water_distribution".equals(preferredStructureType)
            || "message_auth".equals(preferredStructureType)
            || "community_security".equals(preferredStructureType)
            || "community_governance".equals(preferredStructureType)
            || "soapmaking".equals(preferredStructureType)
            || "glassmaking".equals(preferredStructureType)
            || "fair_trial".equals(preferredStructureType);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    static final class AnchorChoice {
        final SearchResult rankedAnchor;
        final SearchResult routedAnchor;
        final boolean routeFocused;
        final boolean preferRouteAnchorOverRankedGuide;
        final boolean preferCabinSiteSelectionRouteAnchor;
        final boolean routedHasCabinSiteSelectionSignal;
        final boolean rankedHasCabinSiteSelectionSignal;
        final boolean preferRoofWeatherproofRouteAnchor;
        final boolean routedHasRoofWeatherproofSignal;
        final boolean rankedHasRoofWeatherproofDistractorSignal;
        final boolean rankedHasRoofWeatherproofSignal;
        final boolean rankedGuideFocusWaterDistributionFallback;
        final boolean sameGuideGroup;
        final int rankedSupportWithMetadata;
        final int routedSupportWithMetadata;

        AnchorChoice(
            SearchResult rankedAnchor,
            SearchResult routedAnchor,
            boolean routeFocused,
            boolean preferRouteAnchorOverRankedGuide,
            boolean preferCabinSiteSelectionRouteAnchor,
            boolean routedHasCabinSiteSelectionSignal,
            boolean rankedHasCabinSiteSelectionSignal,
            boolean preferRoofWeatherproofRouteAnchor,
            boolean routedHasRoofWeatherproofSignal,
            boolean rankedHasRoofWeatherproofDistractorSignal,
            boolean rankedHasRoofWeatherproofSignal,
            boolean rankedGuideFocusWaterDistributionFallback,
            boolean sameGuideGroup,
            int rankedSupportWithMetadata,
            int routedSupportWithMetadata
        ) {
            this.rankedAnchor = rankedAnchor;
            this.routedAnchor = routedAnchor;
            this.routeFocused = routeFocused;
            this.preferRouteAnchorOverRankedGuide = preferRouteAnchorOverRankedGuide;
            this.preferCabinSiteSelectionRouteAnchor = preferCabinSiteSelectionRouteAnchor;
            this.routedHasCabinSiteSelectionSignal = routedHasCabinSiteSelectionSignal;
            this.rankedHasCabinSiteSelectionSignal = rankedHasCabinSiteSelectionSignal;
            this.preferRoofWeatherproofRouteAnchor = preferRoofWeatherproofRouteAnchor;
            this.routedHasRoofWeatherproofSignal = routedHasRoofWeatherproofSignal;
            this.rankedHasRoofWeatherproofDistractorSignal = rankedHasRoofWeatherproofDistractorSignal;
            this.rankedHasRoofWeatherproofSignal = rankedHasRoofWeatherproofSignal;
            this.rankedGuideFocusWaterDistributionFallback = rankedGuideFocusWaterDistributionFallback;
            this.sameGuideGroup = sameGuideGroup;
            this.rankedSupportWithMetadata = rankedSupportWithMetadata;
            this.routedSupportWithMetadata = routedSupportWithMetadata;
        }
    }
}
