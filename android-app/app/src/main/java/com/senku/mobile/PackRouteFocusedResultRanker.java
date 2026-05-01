package com.senku.mobile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

final class PackRouteFocusedResultRanker {
    private PackRouteFocusedResultRanker() {
    }

    static List<SearchResult> rank(
        PackRepository.QueryTerms queryTerms,
        LinkedHashMap<String, PackRepository.ScoredSearchResult> bestBySection,
        int limit
    ) {
        LinkedHashMap<String, Integer> guideTotals = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> guideSectionCounts = new LinkedHashMap<>();
        for (PackRepository.ScoredSearchResult sectionScore : bestBySection.values()) {
            String guideKey = PackSupportScoringPolicy.guideGroupKey(sectionScore.result);
            guideTotals.put(guideKey, guideTotals.getOrDefault(guideKey, 0) + Math.max(1, sectionScore.score));
            guideSectionCounts.put(guideKey, guideSectionCounts.getOrDefault(guideKey, 0) + 1);
        }

        ArrayList<PackRepository.ScoredSearchResult> scored = new ArrayList<>();
        boolean conservativeWaterGuideBundling = "water_storage".equals(queryTerms.metadataProfile.preferredStructureType())
            && !queryTerms.metadataProfile.hasExplicitTopic("water_distribution");
        for (PackRepository.ScoredSearchResult sectionScore : bestBySection.values()) {
            String guideKey = PackSupportScoringPolicy.guideGroupKey(sectionScore.result);
            int guideBonus = Math.min(28, guideTotals.getOrDefault(guideKey, 0) / 4);
            int diversityBonus = Math.min(10, guideSectionCounts.getOrDefault(guideKey, 0) * 2);
            if (conservativeWaterGuideBundling) {
                guideBonus = 0;
                diversityBonus = 0;
            }
            int score = sectionScore.score + guideBonus + diversityBonus;
            if (conservativeWaterGuideBundling) {
                score += PackRepository.broadWaterRouteRefinementBonus(queryTerms, sectionScore.result);
            }
            score += PackRepository.currentHeadRouteRefinementBonus(queryTerms, sectionScore.result);
            scored.add(new PackRepository.ScoredSearchResult(
                sectionScore.result,
                sectionScore.originalIndex,
                score
            ));
        }

        scored.sort((left, right) -> {
            if (conservativeWaterGuideBundling) {
                int priorityOrder = Integer.compare(
                    PackRepository.broadWaterRouteOrderingPriority(queryTerms, right.result),
                    PackRepository.broadWaterRouteOrderingPriority(queryTerms, left.result)
                );
                if (priorityOrder != 0) {
                    return priorityOrder;
                }
            }
            int currentHeadPriorityOrder = Integer.compare(
                PackRepository.currentHeadRouteOrderingPriority(queryTerms, right.result),
                PackRepository.currentHeadRouteOrderingPriority(queryTerms, left.result)
            );
            if (currentHeadPriorityOrder != 0) {
                return currentHeadPriorityOrder;
            }
            int scoreOrder = Integer.compare(right.score, left.score);
            if (scoreOrder != 0) {
                return scoreOrder;
            }
            int modeOrder = left.result.title.compareToIgnoreCase(right.result.title);
            if (modeOrder != 0) {
                return modeOrder;
            }
            return Integer.compare(left.originalIndex, right.originalIndex);
        });

        ArrayList<SearchResult> ordered = new ArrayList<>();
        for (PackRepository.ScoredSearchResult item : scored) {
            if (ordered.size() >= limit) {
                break;
            }
            ordered.add(item.result);
        }
        return ordered;
    }
}
