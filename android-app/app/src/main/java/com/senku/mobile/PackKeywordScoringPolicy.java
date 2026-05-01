package com.senku.mobile;

import java.util.Locale;

final class PackKeywordScoringPolicy {
    private static final Locale QUERY_LOCALE = Locale.US;

    private PackKeywordScoringPolicy() {
    }

    static int scoreCandidate(
        PackRepository.QueryTerms queryTerms,
        String title,
        String section,
        String category,
        String tags,
        String description,
        String document,
        String contentRole,
        String timeHorizon,
        String structureType,
        String topicTags
    ) {
        return keywordScore(queryTerms, title, section, category, tags, description, document)
            + metadataBonus(queryTerms, category, contentRole, timeHorizon, structureType, topicTags);
    }

    static int keywordScore(
        PackRepository.QueryTerms queryTerms,
        String title,
        String section,
        String category,
        String tags,
        String description,
        String document
    ) {
        String titleLower = safe(title).toLowerCase(QUERY_LOCALE);
        String sectionLower = safe(section).toLowerCase(QUERY_LOCALE);
        String categoryLower = safe(category).toLowerCase(QUERY_LOCALE);
        String tagsLower = safe(tags).toLowerCase(QUERY_LOCALE);
        String descriptionLower = safe(description).toLowerCase(QUERY_LOCALE);
        String documentLower = safe(document).toLowerCase(QUERY_LOCALE);
        String queryLower = queryTerms.queryLower;

        int score = 0;
        if (PackTextMatchPolicy.containsTerm(titleLower, queryLower)) {
            score += 18;
        }
        if (PackTextMatchPolicy.containsTerm(sectionLower, queryLower)) {
            score += 14;
        }
        if (PackTextMatchPolicy.containsTerm(descriptionLower, queryLower)) {
            score += 8;
        }

        int strongMatches = 0;
        for (String token : queryTerms.primaryKeywordTokens()) {
            if (PackTextMatchPolicy.containsTerm(titleLower, token)) {
                score += 12;
                strongMatches += 1;
            }
            if (PackTextMatchPolicy.containsTerm(sectionLower, token)) {
                score += 10;
                strongMatches += 1;
            }
            if (PackTextMatchPolicy.containsTerm(tagsLower, token)) {
                score += 8;
                strongMatches += 1;
            }
            if (PackTextMatchPolicy.containsTerm(categoryLower, token)) {
                score += 5;
            }
            if (PackTextMatchPolicy.containsTerm(descriptionLower, token)) {
                score += 6;
                strongMatches += 1;
            }
            if (PackTextMatchPolicy.containsTerm(documentLower, token)) {
                score += 3;
                strongMatches += 1;
            }
        }

        int expansionMatches = 0;
        for (String token : queryTerms.expansionTokens) {
            if (PackTextMatchPolicy.containsTerm(titleLower, token)) {
                score += 6;
                expansionMatches += 1;
            }
            if (PackTextMatchPolicy.containsTerm(sectionLower, token)) {
                score += 5;
                expansionMatches += 1;
            }
            if (PackTextMatchPolicy.containsTerm(tagsLower, token)) {
                score += 4;
                expansionMatches += 1;
            }
            if (PackTextMatchPolicy.containsTerm(categoryLower, token)) {
                score += 3;
            }
            if (PackTextMatchPolicy.containsTerm(descriptionLower, token)) {
                score += 3;
                expansionMatches += 1;
            }
            if (PackTextMatchPolicy.containsTerm(documentLower, token)) {
                score += 2;
                expansionMatches += 1;
            }
        }

        score += queryTerms.routeProfile.metadataBonus(
            titleLower,
            sectionLower,
            categoryLower,
            tagsLower,
            descriptionLower,
            documentLower
        );

        if (strongMatches >= 2) {
            score += 10;
        }
        if (!queryTerms.primaryKeywordTokens().isEmpty() && strongMatches >= queryTerms.primaryKeywordTokens().size()) {
            score += 8;
        }
        if (expansionMatches >= 2) {
            score += 4;
        }
        return score;
    }

    static int metadataBonus(
        PackRepository.QueryTerms queryTerms,
        String category,
        String contentRole,
        String timeHorizon,
        String structureType,
        String topicTags
    ) {
        return queryTerms.metadataProfile.metadataBonus(
            category,
            contentRole,
            timeHorizon,
            structureType,
            topicTags
        );
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
