package com.senku.mobile;

import java.util.ArrayList;
import java.util.List;

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

    static List<SearchResult> rankSupportCandidatesForTest(
        String query,
        SearchResult anchor,
        List<SearchResult> rankedResults
    ) {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(query);
        boolean diversifyContext = queryTerms.routeProfile.prefersDiversifiedAnswerContext()
            || queryTerms.metadataProfile.prefersDiversifiedContext();
        ArrayList<SupportCandidate> ranked = rankSupportCandidates(
            queryTerms,
            queryTerms.routeProfile,
            diversifyContext,
            anchor,
            rankedResults
        );
        ArrayList<SearchResult> ordered = new ArrayList<>(ranked.size());
        for (SupportCandidate scored : ranked) {
            ordered.add(scored.result);
        }
        return ordered;
    }

    static ArrayList<SupportCandidate> rankSupportCandidates(
        PackRepository.QueryTerms queryTerms,
        QueryRouteProfile routeProfile,
        boolean diversifyContext,
        SearchResult anchor,
        List<SearchResult> rankedResults
    ) {
        ArrayList<SupportCandidate> supportingCandidates = new ArrayList<>();
        if (queryTerms == null || anchor == null || rankedResults == null) {
            return supportingCandidates;
        }
        for (int index = 1; index < rankedResults.size(); index++) {
            SearchResult candidate = rankedResults.get(index);
            if (candidate == null) {
                continue;
            }
            if (PackRepository.emptySafe(candidate.guideId).equals(PackRepository.emptySafe(anchor.guideId))) {
                continue;
            }
            if (!PackRouteSupportPolicy.supportCandidateMatchesRoute(
                routeProfile,
                queryTerms.metadataProfile,
                diversifyContext,
                candidate
            )) {
                continue;
            }
            PackSupportScoringPolicy.SupportBreakdown support =
                PackSupportScoringPolicy.supportBreakdown(queryTerms, candidate);
            int score = support.supportWithMetadata();
            if (score <= 0) {
                continue;
            }
            supportingCandidates.add(new SupportCandidate(candidate, index, score));
        }
        supportingCandidates.sort((left, right) -> {
            int scoreOrder = Integer.compare(right.score, left.score);
            if (scoreOrder != 0) {
                return scoreOrder;
            }
            int modeOrder = Integer.compare(
                PackSupportScoringPolicy.supportRetrievalRank(right.result.retrievalMode),
                PackSupportScoringPolicy.supportRetrievalRank(left.result.retrievalMode)
            );
            if (modeOrder != 0) {
                return modeOrder;
            }
            return Integer.compare(left.originalIndex, right.originalIndex);
        });
        return supportingCandidates;
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

    static final class SupportCandidate {
        final SearchResult result;
        final int originalIndex;
        final int score;

        SupportCandidate(SearchResult result, int originalIndex, int score) {
            this.result = result;
            this.originalIndex = originalIndex;
            this.score = score;
        }
    }
}
