package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class MainSearchSuggestionPolicy {
    static final int MIN_QUERY_LENGTH = 2;
    static final int MAX_SUGGESTIONS = 6;
    static final int MAX_CATEGORY_SUGGESTIONS = 2;

    private static final String[] ORDERED_BUCKETS = {
        "water",
        "shelter",
        "fire",
        "medicine",
        "food",
        "tools",
        "communications",
        "community"
    };

    private MainSearchSuggestionPolicy() {
    }

    static List<SearchSuggestion> buildSuggestions(String query, List<SearchResult> guides) {
        String normalizedQuery = normalize(query);
        if (normalizedQuery.length() < MIN_QUERY_LENGTH || guides == null || guides.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<SearchResult> matches = new ArrayList<>();
        for (SearchResult result : guides) {
            if (matchesSuggestionQuery(result, normalizedQuery)) {
                matches.add(result);
            }
        }
        if (matches.isEmpty()) {
            return Collections.emptyList();
        }
        matches.sort(Comparator
            .comparingInt((SearchResult result) -> suggestionScore(result, normalizedQuery))
            .thenComparing(result -> safe(result == null ? null : result.title), String.CASE_INSENSITIVE_ORDER));
        ArrayList<SearchSuggestion> suggestions = new ArrayList<>();
        addCategorySuggestions(suggestions, matches, normalizedQuery);
        LinkedHashSet<String> seenGuideKeys = new LinkedHashSet<>();
        for (SearchResult result : matches) {
            if (suggestions.size() >= MAX_SUGGESTIONS) {
                break;
            }
            String guideId = safe(result == null ? null : result.guideId).trim();
            String title = safe(result == null ? null : result.title).trim();
            String uniqueKey = !guideId.isEmpty() ? guideId : title.toLowerCase(Locale.US);
            if (uniqueKey.isEmpty() || !seenGuideKeys.add(uniqueKey)) {
                continue;
            }
            String label = buildGuideButtonLabel(result);
            if (label.isEmpty()) {
                continue;
            }
            String searchQuery = !guideId.isEmpty() ? guideId : title;
            suggestions.add(new SearchSuggestion(
                label,
                "Suggested guide: " + title + ". Tap to browse matching guides.",
                searchQuery,
                "",
                ""
            ));
        }
        return suggestions;
    }

    private static void addCategorySuggestions(
        List<SearchSuggestion> suggestions,
        List<SearchResult> matches,
        String normalizedQuery
    ) {
        LinkedHashMap<String, Integer> bucketCounts = new LinkedHashMap<>();
        for (SearchResult result : matches) {
            String bucket = primaryCategoryBucket(result);
            if (bucket.isEmpty()) {
                continue;
            }
            bucketCounts.put(bucket, bucketCounts.getOrDefault(bucket, 0) + 1);
        }
        if (bucketCounts.isEmpty()) {
            return;
        }
        ArrayList<Map.Entry<String, Integer>> rankedBuckets = new ArrayList<>(bucketCounts.entrySet());
        rankedBuckets.sort((left, right) -> Integer.compare(right.getValue(), left.getValue()));
        int added = 0;
        for (Map.Entry<String, Integer> entry : rankedBuckets) {
            if (added >= MAX_CATEGORY_SUGGESTIONS || suggestions.size() >= MAX_SUGGESTIONS) {
                break;
            }
            String bucketKey = entry.getKey();
            int count = entry.getValue();
            if (count < 3 && !queryMatchesBucketHint(normalizedQuery, bucketKey)) {
                continue;
            }
            String bucketLabel = HomeCategoryPolicy.labelForBucket(bucketKey);
            suggestions.add(new SearchSuggestion(
                bucketLabel + " (" + count + ")",
                "Suggested category: " + bucketLabel + ". Tap to browse matching guides.",
                "",
                bucketKey,
                bucketLabel
            ));
            added += 1;
        }
    }

    static boolean matchesSuggestionQuery(SearchResult result, String normalizedQuery) {
        if (result == null || safe(normalizedQuery).isEmpty()) {
            return false;
        }
        String searchable = normalize(
            safe(result.guideId) + " " +
                safe(result.title) + " " +
                safe(result.sectionHeading) + " " +
                safe(result.category) + " " +
                safe(result.topicTags) + " " +
                safe(result.subtitle)
        );
        return searchable.contains(normalizedQuery);
    }

    static int suggestionScore(SearchResult result, String normalizedQuery) {
        String guideId = normalize(result == null ? null : result.guideId);
        String title = normalize(result == null ? null : result.title);
        String section = normalize(result == null ? null : result.sectionHeading);
        String tags = normalize(result == null ? null : result.topicTags);
        String category = normalize(result == null ? null : result.category);
        if (guideId.equals(normalizedQuery)) {
            return 0;
        }
        if (title.startsWith(normalizedQuery)) {
            return 1;
        }
        if (title.contains(normalizedQuery)) {
            return 2;
        }
        if (section.startsWith(normalizedQuery) || section.contains(normalizedQuery)) {
            return 3;
        }
        if (tags.contains(normalizedQuery) || category.contains(normalizedQuery)) {
            return 4;
        }
        return 5;
    }

    static String primaryCategoryBucket(SearchResult result) {
        for (String bucket : ORDERED_BUCKETS) {
            if (HomeCategoryPolicy.matchesBucket(result, bucket)) {
                return bucket;
            }
        }
        return "";
    }

    static boolean queryMatchesBucketHint(String normalizedQuery, String bucketKey) {
        switch (safe(bucketKey).trim()) {
            case "water":
                return containsAnyBucketToken(normalizedQuery, "water", "rain", "purify", "filter", "sanitation");
            case "shelter":
                return containsAnyBucketToken(normalizedQuery, "shelter", "roof", "build", "cabin", "weather");
            case "fire":
                return containsAnyBucketToken(normalizedQuery, "fire", "fuel", "cook", "heat", "signal");
            case "medicine":
                return containsAnyBucketToken(normalizedQuery, "medicine", "medical", "first aid", "wound", "bite");
            case "food":
                return containsAnyBucketToken(normalizedQuery, "food", "garden", "crop", "seed", "cook");
            case "tools":
                return containsAnyBucketToken(normalizedQuery, "tool", "craft", "rope", "forge", "repair");
            case "communications":
                return containsAnyBucketToken(normalizedQuery, "radio", "signal", "message", "communications");
            case "community":
                return containsAnyBucketToken(normalizedQuery, "security", "community", "defense", "governance");
            default:
                return false;
        }
    }

    private static String buildGuideButtonLabel(SearchResult result) {
        String guideId = safe(result == null ? null : result.guideId).trim();
        String title = safe(result == null ? null : result.title).trim();
        if (!guideId.isEmpty() && !title.isEmpty()) {
            return guideId + " | " + clipLabel(title, 24);
        }
        if (!title.isEmpty()) {
            return clipLabel(title, 28);
        }
        return guideId;
    }

    private static String clipLabel(String text, int maxLength) {
        String value = safe(text).trim();
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 1)).trim() + "\u2026";
    }

    private static boolean containsAnyBucketToken(String searchable, String... tokens) {
        String cleanSearchable = safe(searchable);
        for (String token : tokens) {
            if (cleanSearchable.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value) {
        return safe(value).trim().toLowerCase(Locale.US);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    static final class SearchSuggestion {
        final String label;
        final String contentDescription;
        final String searchQuery;
        final String categoryKey;
        final String categoryLabel;

        SearchSuggestion(
            String label,
            String contentDescription,
            String searchQuery,
            String categoryKey,
            String categoryLabel
        ) {
            this.label = safe(label);
            this.contentDescription = safe(contentDescription);
            this.searchQuery = safe(searchQuery);
            this.categoryKey = safe(categoryKey);
            this.categoryLabel = safe(categoryLabel);
        }

        boolean isCategory() {
            return !categoryKey.isEmpty();
        }
    }
}
