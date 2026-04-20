package com.senku.mobile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public final class SearchResultTest {
    @Test
    public void equalResultsHaveMatchingHashCodes() {
        SearchResult first = new SearchResult(
            "Guide",
            "GD-001 | water | guide-focus",
            "Snippet",
            "Body",
            "GD-001",
            "Section",
            "water",
            "guide-focus"
        );
        SearchResult second = new SearchResult(
            "Guide",
            "GD-001 | water | guide-focus",
            "Snippet",
            "Body",
            "GD-001",
            "Section",
            "water",
            "guide-focus"
        );

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void differentResultsDoNotCompareEqual() {
        SearchResult first = new SearchResult("Guide", "", "Snippet", "Body");
        SearchResult second = new SearchResult("Guide", "", "Different snippet", "Body");

        assertNotEquals(first, second);
    }

    @Test
    public void toStringIncludesUsefulSummaryFields() {
        SearchResult result = new SearchResult(
            "Guide",
            "GD-001 | water | guide-focus",
            "Snippet",
            "Body",
            "GD-001",
            "Section",
            "water",
            "guide-focus"
        );

        String text = result.toString();
        assertTrue(text.contains("Guide"));
        assertTrue(text.contains("GD-001"));
        assertTrue(text.contains("guide-focus"));
    }
}
