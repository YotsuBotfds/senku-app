package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

final class PackSearchRerankPolicy {
    private static final Locale QUERY_LOCALE = Locale.US;

    private PackSearchRerankPolicy() {
    }

    static boolean shouldApplyMetadataRerank(PackRepository.QueryTerms queryTerms) {
        if (queryTerms == null) {
            return false;
        }
        boolean routeFocused = queryTerms.routeProfile != null && queryTerms.routeProfile.isRouteFocused();
        boolean hasStructureHint = queryTerms.metadataProfile != null
            && !PackRepository.emptySafe(queryTerms.metadataProfile.preferredStructureType()).isEmpty();
        return routeFocused || hasStructureHint;
    }

    static List<PackRepository.RerankedResult> maybeRerankResultsDetailed(
        PackRepository.QueryTerms queryTerms,
        List<SearchResult> results,
        int limit
    ) {
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        if (!shouldApplyMetadataRerank(queryTerms)) {
            return passthroughRerankedResults(results, limit);
        }

        LinkedHashMap<String, PackAnswerAnchorScoringPolicy.GuideScore> guides = new LinkedHashMap<>();
        ArrayList<PackRepository.RerankedResult> scored = new ArrayList<>();
        for (int index = 0; index < results.size(); index++) {
            SearchResult result = results.get(index);
            PackSupportScoringPolicy.SupportBreakdown support =
                PackSupportScoringPolicy.supportBreakdown(queryTerms, result);
            int metadataBonus = support.metadataBonus;
            int score = support.supportWithMetadata();
            score += rerankModeBonus(result.retrievalMode);

            String guideKey = PackSupportScoringPolicy.guideGroupKey(result);
            PackAnswerAnchorScoringPolicy.GuideScore guide = guides.get(guideKey);
            if (guide == null) {
                guide = new PackAnswerAnchorScoringPolicy.GuideScore(result);
                guides.put(guideKey, guide);
            }
            guide.totalScore += Math.max(1, score);
            guide.bestScore = Math.max(guide.bestScore, score);
            guide.sectionKeys.add(
                PackRepository.buildGuideSectionKey(result.guideId, result.title, result.sectionHeading)
            );
            scored.add(new PackRepository.RerankedResult(result, index, metadataBonus, score));
        }

        for (PackRepository.RerankedResult scoredResult : scored) {
            PackAnswerAnchorScoringPolicy.GuideScore guide =
                guides.get(PackSupportScoringPolicy.guideGroupKey(scoredResult.result));
            if (guide == null) {
                continue;
            }
            scoredResult.addGuideBonus(guideAggregationBonus(guide));
        }

        scored.sort((left, right) -> {
            int scoreOrder = Double.compare(right.finalScore, left.finalScore);
            if (scoreOrder != 0) {
                return scoreOrder;
            }
            return Integer.compare(left.originalIndex, right.originalIndex);
        });

        int capped = cappedResultCount(scored.size(), limit);
        return new ArrayList<>(scored.subList(0, capped));
    }

    static List<PackRepository.RerankedResult> passthroughRerankedResults(
        List<SearchResult> results,
        int limit
    ) {
        ArrayList<PackRepository.RerankedResult> passthrough = new ArrayList<>();
        if (results == null || results.isEmpty()) {
            return passthrough;
        }
        int capped = cappedResultCount(results.size(), limit);
        for (int index = 0; index < capped; index++) {
            passthrough.add(new PackRepository.RerankedResult(results.get(index), index, 0, 0.0));
        }
        return passthrough;
    }

    static int rerankModeBonus(String retrievalMode) {
        String mode = PackRepository.emptySafe(retrievalMode).trim().toLowerCase(QUERY_LOCALE);
        if ("route-focus".equals(mode)) {
            return 8;
        }
        if ("hybrid".equals(mode)) {
            return 4;
        }
        if ("guide-focus".equals(mode)) {
            return 3;
        }
        if ("lexical".equals(mode)) {
            return 2;
        }
        return 0;
    }

    static int guideAggregationBonus(PackAnswerAnchorScoringPolicy.GuideScore guide) {
        if (guide == null) {
            return 0;
        }
        int bonus = Math.min(24, guide.totalScore / 3);
        if (guide.sectionKeys.size() >= 2) {
            bonus += 6;
        }
        return bonus;
    }

    private static int cappedResultCount(int resultCount, int limit) {
        return limit <= 0 ? resultCount : Math.min(limit, resultCount);
    }
}
