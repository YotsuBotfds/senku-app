package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;

public final class PackRouteFocusedResultRankerTest {
    @Test
    public void sharedGuideAggregationCanLiftMultipleSectionsAheadOfSingleSection() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery("how do i make soap");
        LinkedHashMap<String, PackRepository.ScoredSearchResult> candidates = new LinkedHashMap<>();
        add(candidates, "a:rendering", result("Animal Fat Soap", "GD-122", "Rendering Fat", "soapmaking"), 0, 30);
        add(candidates, "a:lye", result("Animal Fat Soap", "GD-122", "Lye Water", "soapmaking"), 1, 30);
        add(candidates, "b:safety", result("Broad Chemistry", "GD-227", "Caustic Basics", "general"), 2, 37);

        List<SearchResult> ranked = PackRouteFocusedResultRanker.rank(queryTerms, candidates, 3);

        assertEquals("GD-122", ranked.get(0).guideId);
        assertEquals("GD-122", ranked.get(1).guideId);
        assertEquals("GD-227", ranked.get(2).guideId);
    }

    @Test
    public void broadWaterStorageRankingSuppressesGuideBundlingAndUsesWaterPriority() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(
            "what is the safest way to store treated water long term"
        );
        LinkedHashMap<String, PackRepository.ScoredSearchResult> candidates = new LinkedHashMap<>();
        add(candidates, "a:basics", waterResult(
            "Storage Basics",
            "GD-252",
            "Container Storage",
            "safety",
            "water_storage,container_sanitation,water_rotation",
            "guide-focus"
        ), 0, 4);
        add(candidates, "a:rotation", waterResult(
            "Storage Basics",
            "GD-252",
            "Rotation Inspection",
            "safety",
            "water_storage,container_sanitation,water_rotation",
            "guide-focus"
        ), 1, 4);
        add(candidates, "b:inventory", waterResult(
            "Home Inventory",
            "GD-373",
            "General Supplies",
            "starter",
            "water_storage",
            "route-focus"
        ), 2, 30);

        List<SearchResult> ranked = PackRouteFocusedResultRanker.rank(queryTerms, candidates, 3);

        assertEquals("GD-252", ranked.get(0).guideId);
        assertEquals("Container Storage", ranked.get(0).sectionHeading);
        assertEquals("GD-252", ranked.get(1).guideId);
        assertEquals("Rotation Inspection", ranked.get(1).sectionHeading);
        assertEquals("GD-373", ranked.get(2).guideId);
    }

    @Test
    public void rankAppliesLimitAndFallsBackToTitleThenOriginalIndex() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery("how do i make soap");
        LinkedHashMap<String, PackRepository.ScoredSearchResult> candidates = new LinkedHashMap<>();
        add(candidates, "z:first", result("Zulu", "GD-001", "Same", "general"), 0, 10);
        add(candidates, "a:first", result("Alpha", "GD-002", "Same", "general"), 1, 10);
        add(candidates, "a:second", result("Alpha", "GD-003", "Same", "general"), 2, 10);

        List<SearchResult> ranked = PackRouteFocusedResultRanker.rank(queryTerms, candidates, 2);

        assertEquals("GD-002", ranked.get(0).guideId);
        assertEquals("GD-003", ranked.get(1).guideId);
        assertEquals(2, ranked.size());
    }

    private static void add(
        LinkedHashMap<String, PackRepository.ScoredSearchResult> candidates,
        String sectionKey,
        SearchResult result,
        int originalIndex,
        int score
    ) {
        candidates.put(sectionKey, new PackRepository.ScoredSearchResult(result, originalIndex, score));
    }

    private static SearchResult result(String title, String guideId, String sectionHeading, String structureType) {
        return new SearchResult(
            title,
            "",
            "snippet",
            "body",
            guideId,
            sectionHeading,
            "crafts",
            "route-focus",
            "reference",
            "mixed",
            structureType,
            "soapmaking"
        );
    }

    private static SearchResult waterResult(
        String title,
        String guideId,
        String sectionHeading,
        String contentRole,
        String topicTags,
        String retrievalMode
    ) {
        return new SearchResult(
            title,
            "",
            "snippet",
            "body",
            guideId,
            sectionHeading,
            "resource-management",
            retrievalMode,
            contentRole,
            "long_term",
            "water_storage",
            topicTags
        );
    }
}
