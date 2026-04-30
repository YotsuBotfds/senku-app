package com.senku.mobile;

import java.util.Locale;

final class PackSupportScoringPolicy {
    private static final Locale QUERY_LOCALE = Locale.US;

    private PackSupportScoringPolicy() {
    }

    static SupportBreakdown supportBreakdown(PackRepository.QueryTerms queryTerms, SearchResult result) {
        String retrievalMode = PackRepository.emptySafe(result.retrievalMode).toLowerCase(QUERY_LOCALE);
        int lexicalSupport = "vector".equals(retrievalMode)
            ? 0
            : PackRepository.lexicalKeywordScore(
                queryTerms,
                result.title,
                result.sectionHeading,
                result.category,
                result.topicTags,
                result.snippet,
                result.body
            );
        int metadataBonus = PackRepository.metadataBonus(
            queryTerms,
            result.category,
            result.contentRole,
            result.timeHorizon,
            result.structureType,
            result.topicTags
        );
        int specializedTopicBonus = specializedExplicitTopicBonus(queryTerms, result);
        int sectionBonus = queryTerms.metadataProfile.sectionHeadingBonus(result.sectionHeading);
        int structurePenalty = supportStructurePenalty(
            queryTerms.metadataProfile.prefersDiversifiedContext(),
            retrievalMode,
            result.sectionHeading
        );
        return new SupportBreakdown(
            lexicalSupport,
            metadataBonus,
            specializedTopicBonus,
            sectionBonus,
            structurePenalty
        );
    }

    static int specializedExplicitTopicBonus(PackRepository.QueryTerms queryTerms, SearchResult result) {
        if (queryTerms == null || result == null || queryTerms.metadataProfile == null) {
            return 0;
        }
        QueryMetadataProfile metadataProfile = queryTerms.metadataProfile;
        String preferredStructureType = metadataProfile.preferredStructureType();
        if (!metadataProfile.hasExplicitTopicFocus()
            || !PackRepository.requiresSpecializedRouteAnchorSignal(preferredStructureType)) {
            return 0;
        }
        boolean structureMatch = preferredStructureType.equals(
            PackRepository.emptySafe(result.structureType).trim().toLowerCase(QUERY_LOCALE)
        );
        boolean topicMatch = metadataProfile.hasExplicitTopicOverlap(result.topicTags);
        if (structureMatch && topicMatch) {
            return 18 + PackRepository.specializedAnchorFocusBonus(queryTerms, result);
        }
        if (structureMatch) {
            return 12 + PackRepository.specializedAnchorFocusBonus(queryTerms, result);
        }
        if (topicMatch) {
            return 8 + PackRepository.specializedAnchorFocusBonus(queryTerms, result);
        }
        return -10;
    }

    static int anchorAlignmentBonus(PackRepository.QueryTerms queryTerms, SearchResult result) {
        if (queryTerms == null || result == null || queryTerms.metadataProfile == null) {
            return 0;
        }
        QueryMetadataProfile metadataProfile = queryTerms.metadataProfile;
        String preferredStructureType = metadataProfile.preferredStructureType();
        if (!metadataProfile.hasExplicitTopicFocus()
            || !PackRepository.requiresSpecializedRouteAnchorSignal(preferredStructureType)) {
            return 0;
        }
        boolean structureMatch = preferredStructureType.equals(
            PackRepository.emptySafe(result.structureType).trim().toLowerCase(QUERY_LOCALE)
        );
        boolean topicMatch = metadataProfile.hasExplicitTopicOverlap(result.topicTags);
        if (structureMatch && topicMatch) {
            return 24 + PackRepository.specializedAnchorFocusBonus(queryTerms, result);
        }
        if (structureMatch) {
            return 16 + PackRepository.specializedAnchorFocusBonus(queryTerms, result);
        }
        if (topicMatch) {
            return 12 + PackRepository.specializedAnchorFocusBonus(queryTerms, result);
        }
        return -24;
    }

    static int supportRetrievalRank(String retrievalMode) {
        String mode = PackRepository.emptySafe(retrievalMode).trim().toLowerCase(QUERY_LOCALE);
        return switch (mode) {
            case "route-focus" -> 4;
            case "guide-focus" -> 3;
            case "hybrid" -> 2;
            case "lexical" -> 1;
            default -> 0;
        };
    }

    static int supportStructurePenalty(boolean diversifyContext, String retrievalMode, String sectionHeading) {
        return RetrievalRoutePolicy.supportStructurePenalty(diversifyContext, retrievalMode, sectionHeading);
    }

    static String guideGroupKey(SearchResult result) {
        String guideId = PackRepository.emptySafe(result.guideId).trim();
        if (!guideId.isEmpty()) {
            return guideId.toLowerCase(QUERY_LOCALE);
        }
        return PackRepository.emptySafe(result.title).replaceAll("\\s+", " ").trim().toLowerCase(QUERY_LOCALE);
    }

    static class SupportBreakdown {
        final int lexicalSupport;
        final int metadataBonus;
        final int specializedTopicBonus;
        final int sectionBonus;
        final int structurePenalty;

        SupportBreakdown(
            int lexicalSupport,
            int metadataBonus,
            int specializedTopicBonus,
            int sectionBonus,
            int structurePenalty
        ) {
            this.lexicalSupport = lexicalSupport;
            this.metadataBonus = metadataBonus;
            this.specializedTopicBonus = specializedTopicBonus;
            this.sectionBonus = sectionBonus;
            this.structurePenalty = structurePenalty;
        }

        int baseSupport() {
            return lexicalSupport + specializedTopicBonus + sectionBonus + structurePenalty;
        }

        int supportWithMetadata() {
            return baseSupport() + metadataBonus;
        }
    }
}
