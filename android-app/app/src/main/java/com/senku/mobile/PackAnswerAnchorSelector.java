package com.senku.mobile;

import java.util.Locale;

final class PackAnswerAnchorSelector {
    private static final Locale QUERY_LOCALE = Locale.US;

    private PackAnswerAnchorSelector() {
    }

    static boolean shouldPreferRouteAnchorOverRankedGuide(
        PackRepository.QueryTerms queryTerms,
        SearchResult rankedAnchor
    ) {
        return AnswerAnchorPolicy.shouldPreferRouteAnchorOverRankedGuide(
            queryTerms.metadataProfile.preferredStructureType(),
            rankedAnchor
        );
    }

    static SearchResult chooseRankedOrRoutedAnchor(
        PackRepository.QueryTerms queryTerms,
        SearchResult rankedAnchor,
        SearchResult routedAnchor
    ) {
        if (!queryTerms.routeProfile.isRouteFocused()) {
            return rankedAnchor;
        }
        boolean rankedGuideFocusWaterDistributionFallback =
            queryTerms.metadataProfile.hasExplicitTopic("water_distribution")
                && "guide-focus".equals(
                    PackRepository.emptySafe(rankedAnchor.retrievalMode).trim().toLowerCase(QUERY_LOCALE)
                )
                && PackRepository.emptySafe(rankedAnchor.sectionHeading).trim().isEmpty()
                && !PackRepository.hasWaterDistributionTitleSignal(rankedAnchor);
        return AnswerAnchorPolicy.chooseRankedOrRoutedAnchor(
            AnswerAnchorPolicy.anchorChoice(rankedAnchor, routedAnchor)
                .routeFocused(queryTerms.routeProfile.isRouteFocused())
                .preferRouteAnchorOverRankedGuide(shouldPreferRouteAnchorOverRankedGuide(queryTerms, rankedAnchor))
                .preferCabinSiteSelectionRouteAnchor(PackRepository.prefersCabinSiteSelectionRouteAnchor(queryTerms))
                .routedHasCabinSiteSelectionSignal(PackRepository.hasCabinSiteSelectionAnchorSignal(routedAnchor))
                .rankedHasCabinSiteSelectionSignal(PackRepository.hasCabinSiteSelectionAnchorSignal(rankedAnchor))
                .preferRoofWeatherproofRouteAnchor(PackRepository.prefersRoofWeatherproofRouteAnchor(queryTerms))
                .routedHasRoofWeatherproofSignal(PackRepository.hasRoofWeatherproofAnchorSignal(routedAnchor))
                .rankedHasRoofWeatherproofDistractorSignal(
                    PackRepository.hasRoofWeatherproofDistractorSignal(rankedAnchor)
                )
                .rankedHasRoofWeatherproofSignal(PackRepository.hasRoofWeatherproofAnchorSignal(rankedAnchor))
                .rankedGuideFocusWaterDistributionFallback(rankedGuideFocusWaterDistributionFallback)
                .sameGuideGroup(
                    PackSupportScoringPolicy.guideGroupKey(routedAnchor)
                        .equals(PackSupportScoringPolicy.guideGroupKey(rankedAnchor))
                )
                .rankedSupportWithMetadata(
                    PackSupportScoringPolicy.supportBreakdown(queryTerms, rankedAnchor).supportWithMetadata()
                )
                .routedSupportWithMetadata(
                    PackSupportScoringPolicy.supportBreakdown(queryTerms, routedAnchor).supportWithMetadata()
                )
                .build()
        );
    }
}
