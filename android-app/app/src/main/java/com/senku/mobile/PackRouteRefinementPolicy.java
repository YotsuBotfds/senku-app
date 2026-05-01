package com.senku.mobile;

import java.util.Locale;

final class PackRouteRefinementPolicy {
    private static final Locale QUERY_LOCALE = Locale.US;

    private PackRouteRefinementPolicy() {
    }

    static int currentHeadRouteOrderingPriority(PackRepository.QueryTerms queryTerms, SearchResult result) {
        return PackRouteAnchorBiasPolicy.currentHeadRouteOrderingPriority(queryTerms, result);
    }

    static int currentHeadRouteRefinementBonus(PackRepository.QueryTerms queryTerms, SearchResult result) {
        return PackRouteAnchorBiasPolicy.currentHeadRouteRefinementBonus(queryTerms, result);
    }

    static int broadWaterRouteOrderingPriority(PackRepository.QueryTerms queryTerms, SearchResult result) {
        QueryMetadataProfile metadataProfile = broadWaterMetadataProfile(queryTerms, result);
        if (metadataProfile == null) {
            return 0;
        }

        int overlap = metadataProfile.preferredTopicOverlapCount(result.topicTags);
        String normalizedMode = normalized(result.retrievalMode);
        int priority = broadWaterRouteRefinementBonus(queryTerms, result) * 4 + overlap;
        if ("guide-focus".equals(normalizedMode) && overlap >= 2) {
            priority += 4;
        }
        if ("route-focus".equals(normalizedMode) && overlap < 2) {
            priority -= 2;
        }
        return priority;
    }

    static int broadWaterRouteRefinementBonus(PackRepository.QueryTerms queryTerms, SearchResult result) {
        QueryMetadataProfile metadataProfile = broadWaterMetadataProfile(queryTerms, result);
        if (metadataProfile == null) {
            return 0;
        }

        int overlap = metadataProfile.preferredTopicOverlapCount(result.topicTags);
        String normalizedRole = normalized(result.contentRole);
        String normalizedMode = normalized(result.retrievalMode);
        String normalizedTitle = normalized(result.title);
        String normalizedSection = normalized(result.sectionHeading);
        boolean containerMakingIntent = PackStructuredAnchorPolicy.hasWaterStorageContainerMakingIntent(
            queryTerms.queryLower
        );

        int score = 0;
        if (("planning".equals(normalizedRole) || "subsystem".equals(normalizedRole)) && overlap >= 2) {
            score += 24;
        } else if ("safety".equals(normalizedRole) && overlap >= 2) {
            score += 12;
        }
        if ("guide-focus".equals(normalizedMode) && overlap >= 2) {
            score += 18;
        }
        if (overlap >= 2
            && (normalizedSection.contains("container")
                || normalizedSection.contains("rotation")
                || normalizedSection.contains("inspection")
                || normalizedSection.contains("storage strategy")
                || normalizedSection.contains("purification"))) {
            score += 12;
        }
        if (!containerMakingIntent
            && (normalizedTitle.contains("making")
                || normalizedTitle.contains("container")
                || normalizedTitle.contains("vessel"))) {
            score -= 22;
        }
        if (!containerMakingIntent
            && "guide-focus".equals(normalizedMode)
            && PackRepository.emptySafe(result.sectionHeading).trim().isEmpty()
            && ("building".equals(normalized(result.category))
                || "crafts".equals(normalized(result.category)))) {
            score -= 14;
        }
        if ("starter".equals(normalizedRole) && overlap < 2) {
            score -= 22;
        }
        if ("route-focus".equals(normalizedMode) && overlap < 2) {
            score -= 12;
        }
        if (normalizedTitle.contains("inventory") && overlap < 2) {
            score -= 32;
        }
        return score;
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

    private static boolean missingRouteInputs(PackRepository.QueryTerms queryTerms, SearchResult result) {
        return queryTerms == null || result == null || queryTerms.metadataProfile == null;
    }

    private static QueryMetadataProfile broadWaterMetadataProfile(
        PackRepository.QueryTerms queryTerms,
        SearchResult result
    ) {
        if (missingRouteInputs(queryTerms, result)) {
            return null;
        }
        QueryMetadataProfile metadataProfile = queryTerms.metadataProfile;
        if (!"water_storage".equals(metadataProfile.preferredStructureType())
            || metadataProfile.hasExplicitTopic("water_distribution")) {
            return null;
        }
        return metadataProfile;
    }
}
