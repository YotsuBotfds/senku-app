package com.senku.mobile;

import java.util.Locale;

final class PackRouteRowFilterPolicy {
    private static final Locale QUERY_LOCALE = Locale.US;

    private PackRouteRowFilterPolicy() {
    }

    static boolean matchesSpecializedExplicitTopicRow(
        PackRepository.QueryTerms queryTerms,
        String structureType,
        String topicTags
    ) {
        return SpecializedAnchorCandidatePolicy.matchesExplicitTopicRow(
            queryTerms == null ? null : queryTerms.metadataProfile,
            structureType,
            topicTags
        );
    }

    static boolean matchesSpecializedRouteMetadata(
        PackRepository.QueryTerms queryTerms,
        String sectionHeading,
        String structureType,
        String topicTags
    ) {
        return SpecializedAnchorCandidatePolicy.matchesRouteMetadata(
            queryTerms == null ? null : queryTerms.metadataProfile,
            sectionHeading,
            structureType,
            topicTags
        );
    }

    static boolean shouldKeepSpecializedDirectSignalRouteResult(
        PackRepository.QueryTerms queryTerms,
        SearchResult result
    ) {
        if (queryTerms == null || result == null || queryTerms.metadataProfile == null) {
            return true;
        }
        String preferredStructureType = queryTerms.metadataProfile.preferredStructureType();
        if (!"soapmaking".equals(preferredStructureType) || !shouldRequireDirectAnchorSignal(queryTerms)) {
            return true;
        }
        return hasDirectAnchorSignal(queryTerms, result);
    }

    static boolean hasDirectAnchorSignal(PackRepository.QueryTerms queryTerms, SearchResult result) {
        if (queryTerms.metadataProfile.hasExplicitTopic("water_distribution")) {
            boolean distributionTagged = SpecializedAnchorCandidatePolicy.hasTopicTag(result, "water_distribution");
            if (!distributionTagged) {
                return false;
            }
            boolean titleSignal = PackRouteSignalPolicy.hasWaterDistributionTitleSignal(result);
            String retrievalMode = normalized(result.retrievalMode);
            if ("guide-focus".equals(retrievalMode) && PackRepository.emptySafe(result.sectionHeading).trim().isEmpty()) {
                return PackRouteSignalPolicy.hasStrongWaterDistributionGuideSignal(result);
            }
            if (queryTerms.metadataProfile.sectionHeadingBonus(result.sectionHeading) > 0) {
                return true;
            }
            String category = normalized(result.category);
            if (!"building".equals(category) && !"utility".equals(category)) {
                return false;
            }
            return titleSignal || PackRouteSignalPolicy.hasWaterDistributionDetailSignal(result);
        }
        if (queryTerms.metadataProfile.hasExplicitTopicFocus()) {
            if (SpecializedAnchorCandidatePolicy.hasDirectSectionHeadingSignal(queryTerms.metadataProfile, result)) {
                return true;
            }
            if (!SpecializedAnchorCandidatePolicy.hasDirectExplicitTopicOverlap(queryTerms.metadataProfile, result)) {
                return false;
            }
            String preferredStructureType = queryTerms.metadataProfile.preferredStructureType();
            if ("soapmaking".equals(preferredStructureType)) {
                return PackRouteSignalPolicy.hasStrongSoapmakingGuideSignal(result);
            }
            if (RetrievalRoutePolicy.requiresSpecializedRouteAnchorSignal(preferredStructureType)) {
                if (SpecializedAnchorCandidatePolicy.isBlankGuideFocusOutsidePreferredStructure(
                    queryTerms.metadataProfile,
                    result
                )) {
                    return false;
                }
                int lexicalScore = PackRepository.lexicalKeywordScore(
                    queryTerms,
                    result.title,
                    result.sectionHeading,
                    result.category,
                    "",
                    "",
                    ""
                );
                return lexicalScore > 0;
            }
            return true;
        }
        int lexicalScore = PackRepository.lexicalKeywordScore(
            queryTerms,
            result.title,
            result.sectionHeading,
            result.category,
            result.topicTags,
            result.snippet,
            result.body
        );
        if (lexicalScore > 0) {
            return true;
        }
        if (queryTerms.metadataProfile.sectionHeadingBonus(result.sectionHeading) > 0) {
            return true;
        }
        return SpecializedAnchorCandidatePolicy.hasDirectExplicitTopicOverlap(queryTerms.metadataProfile, result);
    }

    static boolean shouldKeepBroadWaterRouteRow(
        PackRepository.QueryTerms queryTerms,
        String sectionHeading,
        String contentRole,
        String structureType,
        String topicTags,
        int sectionHeadingScore
    ) {
        if (queryTerms == null || queryTerms.metadataProfile == null) {
            return true;
        }
        QueryMetadataProfile metadataProfile = queryTerms.metadataProfile;
        if (!"water_storage".equals(metadataProfile.preferredStructureType())
            || metadataProfile.hasExplicitTopic("water_distribution")) {
            return true;
        }
        if (sectionHeadingScore < 0) {
            return false;
        }

        boolean specializedWaterTopic = containsTerm(topicTags, "container_sanitation")
            || containsTerm(topicTags, "water_rotation")
            || containsTerm(topicTags, "disinfection");
        String normalizedRole = normalized(contentRole);
        String normalizedHeading = normalized(sectionHeading);
        if (sectionHeadingScore == 0 && !specializedWaterTopic) {
            return false;
        }
        if (!specializedWaterTopic
            && "starter".equals(normalizedRole)
            && (normalizedHeading.contains("water storage")
                || normalizedHeading.contains("purification"))) {
            return false;
        }
        return true;
    }

    static boolean shouldKeepBroadHouseRouteRow(
        PackRepository.QueryTerms queryTerms,
        String sectionHeading,
        String category,
        String structureType,
        String topicTags,
        int sectionHeadingScore
    ) {
        if (queryTerms == null || queryTerms.metadataProfile == null) {
            return true;
        }
        QueryMetadataProfile metadataProfile = queryTerms.metadataProfile;
        if (!"cabin_house".equals(metadataProfile.preferredStructureType())) {
            return true;
        }
        if (metadataProfile.hasExplicitTopicFocus()) {
            return sectionHeadingScore > 0;
        }
        if (sectionHeadingScore < 0) {
            return false;
        }

        String normalizedCategory = normalized(category);
        String normalizedStructure = normalized(structureType);
        int overlap = metadataProfile.preferredTopicOverlapCount(topicTags);
        if (sectionHeadingScore == 0
            && !"building".equals(normalizedCategory)
            && !"cabin_house".equals(normalizedStructure)
            && overlap < 2) {
            return false;
        }
        return true;
    }

    private static boolean shouldRequireDirectAnchorSignal(PackRepository.QueryTerms queryTerms) {
        return RetrievalRoutePolicy.shouldRequireDirectAnchorSignal(
            queryTerms.routeProfile,
            queryTerms.metadataProfile,
            queryTerms.primaryKeywordTokens().size()
        );
    }

    private static boolean containsTerm(String text, String term) {
        String normalizedText = normalizeMatchText(text);
        String normalizedTerm = normalizeMatchText(term);
        if (normalizedText.isEmpty() || normalizedTerm.isEmpty()) {
            return false;
        }
        if (normalizedTerm.contains(" ")) {
            return normalizedText.contains(normalizedTerm);
        }
        return (" " + normalizedText + " ").contains(" " + normalizedTerm + " ");
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
