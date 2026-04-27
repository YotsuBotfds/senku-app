package com.senku.mobile;

import static org.junit.Assert.assertEquals;

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

        assertEquals(
            "GD-215 | Rainwater Catchment And" + "\u2026" + "\nCollection checks - Water Sanitation",
            formatter.buildHomeRelatedGuideButtonLabel(result, null)
        );
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

        assertEquals(
            "GD-215\nWater storage basics",
            formatter.buildHomeRelatedGuideButtonLabel(result, null)
        );
    }
}
