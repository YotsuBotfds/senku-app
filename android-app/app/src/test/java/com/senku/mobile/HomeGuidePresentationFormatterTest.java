package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class HomeGuidePresentationFormatterTest {
    @Test
    public void guideButtonLabelUsesModelFieldsWithoutAndroidContext() {
        HomeGuidePresentationFormatter formatter = new HomeGuidePresentationFormatter(
            null,
            new MainPresentationFormatter(null)
        );

        SearchResult result = new SearchResult(
            "Rainwater Catchment And Storage Rotation",
            "GD-215 | Water storage basics | hybrid",
            "",
            "",
            "GD-215",
            "Collection checks",
            "water_sanitation",
            "hybrid"
        );

        String label = formatter.buildHomeRelatedGuideButtonLabel(result, null);
        String[] lines = label.split("\n");

        assertEquals(2, lines.length);
        assertContainsTokens(lines[0], "GD-215", "Rainwater", "Catchment");
        assertFalse(lines[0].contains("Storage Rotation"));
        assertContainsTokens(lines[1], "Collection checks", "Water Sanitation");
        assertFalse(label.contains("hybrid"));
    }

    @Test
    public void guideButtonLabelFallsBackToCleanSubtitleWhenModelContextIsBlank() {
        HomeGuidePresentationFormatter formatter = new HomeGuidePresentationFormatter(
            null,
            new MainPresentationFormatter(null)
        );

        SearchResult result = new SearchResult(
            "",
            "GD-215 | Water storage basics | hybrid",
            "",
            "",
            "GD-215",
            "",
            "",
            "hybrid"
        );

        String label = formatter.buildHomeRelatedGuideButtonLabel(result, null);
        String[] lines = label.split("\n");

        assertEquals(2, lines.length);
        assertEquals("GD-215", lines[0]);
        assertContainsTokens(lines[1], "Water storage basics");
        assertFalse(label.contains("hybrid"));
    }

    private static void assertContainsTokens(String value, String... tokens) {
        String normalizedValue = value.toLowerCase();
        for (String token : tokens) {
            assertTrue(
                "Expected <" + value + "> to contain token <" + token + ">",
                normalizedValue.contains(token.toLowerCase())
            );
        }
    }
}
