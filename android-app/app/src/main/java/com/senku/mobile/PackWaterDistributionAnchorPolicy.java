package com.senku.mobile;

import java.util.List;
import java.util.Locale;

final class PackWaterDistributionAnchorPolicy {
    private static final Locale QUERY_LOCALE = Locale.US;

    private PackWaterDistributionAnchorPolicy() {
    }

    static SearchResult selectExplicitWaterDistributionAnchor(
        PackRepository.QueryTerms queryTerms,
        List<SearchResult> rankedResults
    ) {
        if (queryTerms == null
            || queryTerms.metadataProfile == null
            || !queryTerms.metadataProfile.hasExplicitTopic("water_distribution")
            || rankedResults == null
            || rankedResults.isEmpty()) {
            return null;
        }

        SearchResult best = null;
        int bestScore = Integer.MIN_VALUE;
        for (int index = 0; index < rankedResults.size(); index++) {
            SearchResult candidate = rankedResults.get(index);
            if (!PackRepository.hasDirectAnchorSignal(queryTerms, candidate)) {
                continue;
            }

            int score = Math.max(1, PackSupportScoringPolicy.supportBreakdown(queryTerms, candidate).supportWithMetadata())
                + Math.max(0, 12 - index);
            String category = normalized(candidate.category);
            if ("building".equals(category)) {
                score += 10;
            } else if ("utility".equals(category)) {
                score += 8;
            }
            if (PackRouteSignalPolicy.hasWaterDistributionTitleSignal(candidate)) {
                score += 12;
            }
            score += waterDistributionAnchorFocusBonus(candidate);

            String retrievalMode = normalized(candidate.retrievalMode);
            if ("route-focus".equals(retrievalMode)) {
                score += 10;
            } else if ("guide-focus".equals(retrievalMode)) {
                score += PackRepository.emptySafe(candidate.sectionHeading).trim().isEmpty() ? -4 : 12;
            }

            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    static int waterDistributionAnchorFocusBonus(SearchResult result) {
        String normalizedTitle = normalizeMatchText(PackRepository.emptySafe(result.title));
        String normalizedSection = normalizeMatchText(PackRepository.emptySafe(result.sectionHeading));
        String normalized = normalizeMatchText(
            PackRepository.emptySafe(result.title) + " " + PackRepository.emptySafe(result.sectionHeading)
        );
        int bonus = 0;
        String contentRole = normalized(result.contentRole);
        String structureType = normalized(result.structureType);
        if (PackRouteSignalPolicy.hasStrongWaterDistributionGuideSignal(result)) {
            bonus += 10;
        }
        if ("planning".equals(contentRole) || "subsystem".equals(contentRole)) {
            bonus += 10;
        } else if ("reference".equals(contentRole)) {
            bonus -= 8;
        }
        if ("water_distribution".equals(structureType)) {
            bonus += 6;
        }
        if (normalized.contains("distribution")) {
            bonus += 6;
        }
        if (normalized.contains("storage tank") || normalized.contains("cistern")) {
            bonus += 4;
        }
        if (normalizedTitle.contains("lifecycle")) {
            bonus -= 18;
        }
        if (normalizedSection.startsWith("phase ")) {
            bonus -= 12;
        }
        if (normalized.contains("see also") || normalized.contains("checklist")) {
            bonus -= 16;
        }
        if (normalized.contains("preventive maintenance") || normalized.contains("system care")) {
            bonus -= 14;
        }
        if (normalizedTitle.contains("drilling") || normalizedTitle.contains("troubleshooting")) {
            bonus -= 12;
        }
        if (normalized.contains("plumbing") && !normalized.contains("distribution")) {
            bonus -= 6;
        }
        if (normalized.contains("water system") && !normalized.contains("distribution")) {
            bonus -= 4;
        }
        return bonus;
    }

    private static String normalizeMatchText(String text) {
        return PackRepository.emptySafe(text)
            .toLowerCase(QUERY_LOCALE)
            .replaceAll("[^a-z0-9]+", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    private static String normalized(String text) {
        return PackRepository.emptySafe(text).trim().toLowerCase(QUERY_LOCALE);
    }
}
