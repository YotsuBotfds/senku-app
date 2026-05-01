package com.senku.mobile;

import java.util.List;
import java.util.Locale;

final class PackRouteFocusedAnchorPolicy {
    private static final Locale QUERY_LOCALE = Locale.US;

    private PackRouteFocusedAnchorPolicy() {
    }

    static SearchResult selectRouteFocusedAnchor(
        PackRepository.QueryTerms queryTerms,
        List<SearchResult> rankedResults,
        boolean requireDirectSignal
    ) {
        if (rankedResults == null || rankedResults.isEmpty()) {
            return null;
        }

        SearchResult best = null;
        int bestScore = Integer.MIN_VALUE;
        for (int index = 0; index < rankedResults.size(); index++) {
            SearchResult candidate = rankedResults.get(index);
            String mode = PackRepository.emptySafe(candidate.retrievalMode).trim().toLowerCase(QUERY_LOCALE);
            if (!"route-focus".equals(mode)) {
                continue;
            }
            if (!PackRepository.isSpecializedExplicitAnchorCandidate(queryTerms, candidate)) {
                continue;
            }
            if (requireDirectSignal && !PackRepository.hasDirectAnchorSignal(queryTerms, candidate)) {
                continue;
            }

            int score = AnswerAnchorPolicy.routeFocusedAnchorScore(
                PackSupportScoringPolicy.supportBreakdown(queryTerms, candidate).supportWithMetadata(),
                index,
                PackSupportScoringPolicy.anchorAlignmentBonus(queryTerms, candidate),
                PackRepository.broadRouteSectionPreferenceBonus(queryTerms, candidate),
                PackRepository.cabinSiteSelectionAnchorBias(queryTerms, candidate),
                PackRepository.roofWeatherproofAnchorBias(queryTerms, candidate)
            );
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }
}
