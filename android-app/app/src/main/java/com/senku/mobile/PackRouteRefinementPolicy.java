package com.senku.mobile;

import java.util.Locale;

final class PackRouteRefinementPolicy {
    private static final Locale QUERY_LOCALE = Locale.US;

    private PackRouteRefinementPolicy() {
    }

    static int currentHeadRouteOrderingPriority(PackRepository.QueryTerms queryTerms, SearchResult result) {
        return currentHeadRouteRefinementBonus(queryTerms, result) * 4
            + PackSupportScoringPolicy.anchorAlignmentBonus(queryTerms, result);
    }

    static int currentHeadRouteRefinementBonus(PackRepository.QueryTerms queryTerms, SearchResult result) {
        if (queryTerms == null || result == null || queryTerms.metadataProfile == null) {
            return 0;
        }
        String structure = queryTerms.metadataProfile.preferredStructureType();
        String titleSection = normalizeMatchText(
            PackRepository.emptySafe(result.title) + " " + PackRepository.emptySafe(result.sectionHeading)
        );
        String candidateStructure = normalized(result.structureType);
        String category = normalized(result.category);
        String role = normalized(result.contentRole);
        int score = 0;

        if ("glassmaking".equals(structure)) {
            if (titleSection.contains("glass optics ceramics")
                && titleSection.contains("glassmaking from scratch")) {
                score += 72;
            } else if (titleSection.contains("glass optics ceramics")) {
                score += 34;
            }
            if (titleSection.contains("glass making raw materials")
                && (titleSection.contains("raw materials")
                    || titleSection.contains("batch preparation")
                    || titleSection.contains("furnace construction"))) {
                score -= 18;
            }
            if ("reference".equals(role) && titleSection.contains("raw materials")) {
                score -= 8;
            }
        }

        if ("cabin_house".equals(structure)
            && (queryTerms.metadataProfile.hasExplicitTopic("roofing")
                || queryTerms.metadataProfile.hasExplicitTopic("weatherproofing"))) {
            boolean weatherproofQuery = queryTerms.queryLower.contains("weatherproof")
                || queryTerms.queryLower.contains("rainproof");
            boolean waterproofQuery = queryTerms.queryLower.contains("waterproof");
            if (weatherproofQuery && titleSection.contains("insulation weatherproofing")) {
                score += 72;
            }
            if (waterproofQuery && titleSection.contains("waterproofing and sealants")) {
                score += 48;
            }
            if ("cabin_house".equals(candidateStructure)) {
                score += 18;
            } else if ("small_watercraft".equals(candidateStructure)) {
                score -= weatherproofQuery ? 18 : 4;
            }
            if (titleSection.contains("construction carpentry")) {
                score -= 34;
            }
        }

        if ("cabin_house".equals(structure)
            && !queryTerms.metadataProfile.hasExplicitTopicFocus()
            && queryTerms.routeProfile.isStarterBuildProject()) {
            if ("cabin_house".equals(candidateStructure)) {
                score += 32;
            }
            if ("building".equals(category)) {
                score += 12;
            }
            if (titleSection.contains("construction carpentry")) {
                score += 74;
            }
            if (titleSection.contains("shelter site selection")) {
                score -= 64;
            }
            if ("emergency_shelter".equals(candidateStructure) || "survival".equals(category)) {
                score -= 34;
            }
            if (titleSection.contains("foundation")
                || titleSection.contains("wall construction")
                || titleSection.contains("roofing systems")) {
                score += 10;
            }
        }
        return score;
    }

    static int broadWaterRouteOrderingPriority(PackRepository.QueryTerms queryTerms, SearchResult result) {
        if (queryTerms == null || result == null || queryTerms.metadataProfile == null) {
            return 0;
        }
        QueryMetadataProfile metadataProfile = queryTerms.metadataProfile;
        if (!"water_storage".equals(metadataProfile.preferredStructureType())
            || metadataProfile.hasExplicitTopic("water_distribution")) {
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
        if (queryTerms == null || result == null || queryTerms.metadataProfile == null) {
            return 0;
        }
        QueryMetadataProfile metadataProfile = queryTerms.metadataProfile;
        if (!"water_storage".equals(metadataProfile.preferredStructureType())
            || metadataProfile.hasExplicitTopic("water_distribution")) {
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
}
