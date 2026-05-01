package com.senku.mobile;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class PackAnswerAnchorScoringPolicy {
    private static final Locale QUERY_LOCALE = Locale.US;

    private PackAnswerAnchorScoringPolicy() {
    }

    static LinkedHashMap<String, GuideScore> buildAnchorGuideScores(
        PackRepository.QueryTerms queryTerms,
        List<SearchResult> rankedResults,
        boolean requireDirectSignal
    ) {
        LinkedHashMap<String, GuideScore> guides = new LinkedHashMap<>();
        for (int index = 0; index < rankedResults.size(); index++) {
            SearchResult result = rankedResults.get(index);
            if (!PackRepository.isSpecializedExplicitAnchorCandidate(queryTerms, result)) {
                continue;
            }
            int support = PackSupportScoringPolicy.supportBreakdown(queryTerms, result).supportWithMetadata();
            if (support <= 0) {
                continue;
            }
            if (requireDirectSignal && !PackRouteRowFilterPolicy.hasDirectAnchorSignal(queryTerms, result)) {
                continue;
            }
            String key = PackSupportScoringPolicy.guideGroupKey(result);
            GuideScore guide = guides.get(key);
            if (guide == null) {
                guide = new GuideScore(result);
                guides.put(key, guide);
            }
            int score = support;
            score += Math.max(0, 12 - index);
            score += PackSupportScoringPolicy.anchorAlignmentBonus(queryTerms, result);
            String mode = PackRepository.emptySafe(result.retrievalMode).trim().toLowerCase(QUERY_LOCALE);
            if ("route-focus".equals(mode)) {
                score += 8;
            } else if ("hybrid".equals(mode)) {
                score += 4;
            } else if ("guide-focus".equals(mode)) {
                score += 3;
            } else if ("lexical".equals(mode)) {
                score += 2;
            }
            guide.totalScore += score;
            guide.bestScore = Math.max(guide.bestScore, score);
            guide.sectionKeys.add(PackRepository.buildGuideSectionKey(
                result.guideId,
                result.title,
                result.sectionHeading
            ));
            if (score > guide.anchorScore) {
                guide.anchorScore = score;
                guide.anchor = result;
            }
        }
        return guides;
    }

    static class GuideScore {
        SearchResult anchor;
        int anchorScore;
        int totalScore;
        int bestScore;
        final Set<String> sectionKeys = new LinkedHashSet<>();

        GuideScore(SearchResult anchor) {
            this.anchor = anchor;
        }
    }
}
