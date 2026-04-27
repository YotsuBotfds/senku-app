package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public final class AnswerContextSelectorTest {
    @Test
    public void nullResultsReturnEmptySelection() {
        assertEquals(Collections.emptyList(), AnswerContextSelector.select(null, 3, "watercraft hull repair"));
    }

    @Test
    public void zeroLimitReturnsEmptySelection() {
        SearchResult anchor = result("Shelter Anchors", "GD-001", "Ridge lines", "survival", "lexical");

        List<SearchResult> selected = AnswerContextSelector.select(
            Collections.singletonList(anchor),
            0,
            "watercraft hull repair"
        );

        assertEquals(Collections.emptyList(), selected);
    }

    @Test
    public void meaningfulQueryWithLimitOneKeepsRankedAnchorOnly() {
        SearchResult anchor = result("Shelter Anchors", "GD-001", "Ridge lines", "survival", "lexical");
        SearchResult relevant = result("Hide Hull Repair", "GD-003", "Watercraft hull patching", "water", "hybrid");

        List<SearchResult> selected = AnswerContextSelector.select(
            Arrays.asList(anchor, relevant),
            1,
            "watercraft hull repair"
        );

        assertEquals(Collections.singletonList(anchor), selected);
    }

    @Test
    public void queryTermsPromoteRelevantContextWithinDiverseFill() {
        SearchResult anchor = result("Shelter Anchors", "GD-001", "Ridge lines", "survival", "lexical");
        SearchResult noise = result("Camp Inventory Ledger", "GD-002", "Inventory", "utility", "lexical");
        SearchResult hull = result("Hide Hull Repair", "GD-003", "Watercraft hull patching", "water", "hybrid");
        SearchResult vessel = result("River Vessel Basics", "GD-004", "Watercraft launch checks", "water", "lexical");

        List<SearchResult> selected = AnswerContextSelector.select(
            Arrays.asList(anchor, noise, hull, vessel),
            3,
            "watercraft hull repair"
        );

        assertEquals(Arrays.asList(anchor, noise, vessel), selected);
    }

    @Test
    public void emptyQueryFallsBackToRankedDiverseFill() {
        SearchResult anchor = result("Shelter Anchors", "GD-001", "Ridge lines", "survival", "lexical");
        SearchResult sameSection = result("Shelter Anchors", "GD-001", "Ridge lines", "survival", "hybrid");
        SearchResult secondGuide = result("Water Storage", "GD-002", "Clay jars", "water", "lexical");

        List<SearchResult> selected = AnswerContextSelector.select(
            Arrays.asList(anchor, sameSection, secondGuide),
            2,
            ""
        );

        assertEquals(Arrays.asList(anchor, secondGuide), selected);
    }

    private static SearchResult result(
        String title,
        String guideId,
        String sectionHeading,
        String category,
        String retrievalMode
    ) {
        return new SearchResult(
            title,
            "",
            title + " " + sectionHeading,
            "",
            guideId,
            sectionHeading,
            category,
            retrievalMode
        );
    }
}
