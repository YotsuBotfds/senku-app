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
        if (!RetrievalRoutePolicy.requiresSpecializedRouteAnchorSignal(normalizedStructurePreference)
            || rankedAnchor == null) {
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

    static AnchorChoiceBuilder anchorChoice(SearchResult rankedAnchor, SearchResult routedAnchor) {
        return new AnchorChoiceBuilder(rankedAnchor, routedAnchor);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    static final class AnchorChoiceBuilder {
        private final SearchResult rankedAnchor;
        private final SearchResult routedAnchor;
        private boolean routeFocused;
        private boolean preferRouteAnchorOverRankedGuide;
        private boolean preferCabinSiteSelectionRouteAnchor;
        private boolean routedHasCabinSiteSelectionSignal;
        private boolean rankedHasCabinSiteSelectionSignal;
        private boolean preferRoofWeatherproofRouteAnchor;
        private boolean routedHasRoofWeatherproofSignal;
        private boolean rankedHasRoofWeatherproofDistractorSignal;
        private boolean rankedHasRoofWeatherproofSignal;
        private boolean rankedGuideFocusWaterDistributionFallback;
        private boolean sameGuideGroup;
        private int rankedSupportWithMetadata;
        private int routedSupportWithMetadata;

        private AnchorChoiceBuilder(SearchResult rankedAnchor, SearchResult routedAnchor) {
            this.rankedAnchor = rankedAnchor;
            this.routedAnchor = routedAnchor;
        }

        AnchorChoiceBuilder routeFocused(boolean routeFocused) {
            this.routeFocused = routeFocused;
            return this;
        }

        AnchorChoiceBuilder preferRouteAnchorOverRankedGuide(boolean preferRouteAnchorOverRankedGuide) {
            this.preferRouteAnchorOverRankedGuide = preferRouteAnchorOverRankedGuide;
            return this;
        }

        AnchorChoiceBuilder preferCabinSiteSelectionRouteAnchor(boolean preferCabinSiteSelectionRouteAnchor) {
            this.preferCabinSiteSelectionRouteAnchor = preferCabinSiteSelectionRouteAnchor;
            return this;
        }

        AnchorChoiceBuilder routedHasCabinSiteSelectionSignal(boolean routedHasCabinSiteSelectionSignal) {
            this.routedHasCabinSiteSelectionSignal = routedHasCabinSiteSelectionSignal;
            return this;
        }

        AnchorChoiceBuilder rankedHasCabinSiteSelectionSignal(boolean rankedHasCabinSiteSelectionSignal) {
            this.rankedHasCabinSiteSelectionSignal = rankedHasCabinSiteSelectionSignal;
            return this;
        }

        AnchorChoiceBuilder preferRoofWeatherproofRouteAnchor(boolean preferRoofWeatherproofRouteAnchor) {
            this.preferRoofWeatherproofRouteAnchor = preferRoofWeatherproofRouteAnchor;
            return this;
        }

        AnchorChoiceBuilder routedHasRoofWeatherproofSignal(boolean routedHasRoofWeatherproofSignal) {
            this.routedHasRoofWeatherproofSignal = routedHasRoofWeatherproofSignal;
            return this;
        }

        AnchorChoiceBuilder rankedHasRoofWeatherproofDistractorSignal(
            boolean rankedHasRoofWeatherproofDistractorSignal
        ) {
            this.rankedHasRoofWeatherproofDistractorSignal = rankedHasRoofWeatherproofDistractorSignal;
            return this;
        }

        AnchorChoiceBuilder rankedHasRoofWeatherproofSignal(boolean rankedHasRoofWeatherproofSignal) {
            this.rankedHasRoofWeatherproofSignal = rankedHasRoofWeatherproofSignal;
            return this;
        }

        AnchorChoiceBuilder rankedGuideFocusWaterDistributionFallback(
            boolean rankedGuideFocusWaterDistributionFallback
        ) {
            this.rankedGuideFocusWaterDistributionFallback = rankedGuideFocusWaterDistributionFallback;
            return this;
        }

        AnchorChoiceBuilder sameGuideGroup(boolean sameGuideGroup) {
            this.sameGuideGroup = sameGuideGroup;
            return this;
        }

        AnchorChoiceBuilder rankedSupportWithMetadata(int rankedSupportWithMetadata) {
            this.rankedSupportWithMetadata = rankedSupportWithMetadata;
            return this;
        }

        AnchorChoiceBuilder routedSupportWithMetadata(int routedSupportWithMetadata) {
            this.routedSupportWithMetadata = routedSupportWithMetadata;
            return this;
        }

        AnchorChoice build() {
            return new AnchorChoice(
                rankedAnchor,
                routedAnchor,
                routeFocused,
                preferRouteAnchorOverRankedGuide,
                preferCabinSiteSelectionRouteAnchor,
                routedHasCabinSiteSelectionSignal,
                rankedHasCabinSiteSelectionSignal,
                preferRoofWeatherproofRouteAnchor,
                routedHasRoofWeatherproofSignal,
                rankedHasRoofWeatherproofDistractorSignal,
                rankedHasRoofWeatherproofSignal,
                rankedGuideFocusWaterDistributionFallback,
                sameGuideGroup,
                rankedSupportWithMetadata,
                routedSupportWithMetadata
            );
        }
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
