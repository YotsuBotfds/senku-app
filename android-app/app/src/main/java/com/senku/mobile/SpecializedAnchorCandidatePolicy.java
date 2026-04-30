package com.senku.mobile;

import java.util.Locale;

final class SpecializedAnchorCandidatePolicy {
    private static final Locale LOCALE = Locale.US;

    private SpecializedAnchorCandidatePolicy() {
    }

    static boolean matchesExplicitTopicRow(
        QueryMetadataProfile metadataProfile,
        String structureType,
        String topicTags
    ) {
        if (!requiresNarrowExplicitAnchor(metadataProfile)) {
            return true;
        }
        if (hasPreferredStructure(metadataProfile, structureType)) {
            return true;
        }
        return metadataProfile.hasExplicitTopicOverlap(topicTags);
    }

    static boolean matchesRouteMetadata(
        QueryMetadataProfile metadataProfile,
        String sectionHeading,
        String structureType,
        String topicTags
    ) {
        if (metadataProfile == null) {
            return true;
        }
        String preferredStructureType = metadataProfile.preferredStructureType();
        if (!requiresSpecializedRouteAnchorSignal(preferredStructureType)) {
            if ("water_storage".equals(preferredStructureType)
                && !metadataProfile.hasExplicitTopic("water_distribution")) {
                if (hasDirectSectionHeadingSignal(metadataProfile, sectionHeading)) {
                    return true;
                }
                return hasTopicTag(topicTags, "container_sanitation")
                    || hasTopicTag(topicTags, "water_rotation")
                    || hasTopicTag(topicTags, "disinfection");
            }
            return true;
        }
        if (hasDirectSectionHeadingSignal(metadataProfile, sectionHeading)) {
            return true;
        }
        if (metadataProfile.hasExplicitTopicOverlap(topicTags)) {
            return true;
        }
        return hasPreferredStructure(metadataProfile, structureType);
    }

    static boolean isSpecializedExplicitAnchorCandidate(
        QueryMetadataProfile metadataProfile,
        SearchResult result,
        boolean strongSoapmakingSignal
    ) {
        if (!requiresNarrowExplicitAnchor(metadataProfile)) {
            return true;
        }
        if (hasDirectSectionHeadingSignal(metadataProfile, result == null ? "" : result.sectionHeading)) {
            return true;
        }
        if ("soapmaking".equals(metadataProfile.preferredStructureType())) {
            return strongSoapmakingSignal;
        }
        return matchesExplicitTopicRow(
            metadataProfile,
            result == null ? "" : result.structureType,
            result == null ? "" : result.topicTags
        );
    }

    static boolean requiresNarrowExplicitAnchor(QueryMetadataProfile metadataProfile) {
        return metadataProfile != null
            && requiresSpecializedRouteAnchorSignal(metadataProfile.preferredStructureType())
            && metadataProfile.hasExplicitTopicFocus();
    }

    static boolean hasDirectSectionHeadingSignal(QueryMetadataProfile metadataProfile, SearchResult result) {
        return hasDirectSectionHeadingSignal(metadataProfile, result == null ? "" : result.sectionHeading);
    }

    static boolean hasDirectSectionHeadingSignal(QueryMetadataProfile metadataProfile, String sectionHeading) {
        return metadataProfile != null && metadataProfile.sectionHeadingBonus(sectionHeading) > 0;
    }

    static boolean hasDirectExplicitTopicOverlap(QueryMetadataProfile metadataProfile, SearchResult result) {
        return metadataProfile != null && result != null && metadataProfile.hasExplicitTopicOverlap(result.topicTags);
    }

    static boolean isBlankGuideFocusOutsidePreferredStructure(
        QueryMetadataProfile metadataProfile,
        SearchResult result
    ) {
        if (metadataProfile == null || result == null) {
            return false;
        }
        String retrievalMode = normalize(result.retrievalMode);
        return "guide-focus".equals(retrievalMode)
            && emptySafe(result.sectionHeading).trim().isEmpty()
            && !hasPreferredStructure(metadataProfile, result.structureType);
    }

    static boolean hasPreferredStructure(QueryMetadataProfile metadataProfile, String structureType) {
        return metadataProfile != null
            && metadataProfile.preferredStructureType().equals(normalize(structureType));
    }

    static boolean hasTopicTag(SearchResult result, String term) {
        return result != null && hasTopicTag(result.topicTags, term);
    }

    static boolean hasTopicTag(String topicTags, String term) {
        String normalizedTerm = normalize(term);
        if (normalizedTerm.isEmpty()) {
            return false;
        }
        for (String part : emptySafe(topicTags).split(",")) {
            if (normalizedTerm.equals(normalize(part))) {
                return true;
            }
        }
        return false;
    }

    private static boolean requiresSpecializedRouteAnchorSignal(String preferredStructureType) {
        return RetrievalRoutePolicy.requiresSpecializedRouteAnchorSignal(preferredStructureType);
    }

    private static String normalize(String value) {
        return emptySafe(value).trim().toLowerCase(LOCALE);
    }

    private static String emptySafe(String value) {
        return value == null ? "" : value;
    }
}
