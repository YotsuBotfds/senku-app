package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public final class SearchLinkedGuideCuePolicyTest {
    @Test
    public void fixedLabelsKeepPreviewLineHiddenByDefault() {
        assertFalse(SearchLinkedGuideCuePolicy.shouldShowPreviewLine());
        assertEquals("Guide", SearchLinkedGuideCuePolicy.chipLabel());
        assertEquals("Guide", SearchLinkedGuideCuePolicy.previewLineLabel());
    }

    @Test
    public void previewLineRequiresGuideIdAndPrefersCleanTitle() {
        assertEquals(
            "",
            SearchLinkedGuideCuePolicy.previewLine("Display", " ", "Title", false)
        );
        assertEquals(
            "Guide: Tarp shelter setup",
            SearchLinkedGuideCuePolicy.previewLine("GD-345 - fallback", "GD-345", "  Tarp shelter setup  ", false)
        );
    }

    @Test
    public void previewLabelFallsBackFromDisplayLabelToGuideAndTitleParts() {
        assertEquals(
            "Display Label",
            SearchLinkedGuideCuePolicy.previewLabel(" Display Label ", "GD-345", "Rain Shelter")
        );
        assertEquals(
            "GD-345 - Rain Shelter",
            SearchLinkedGuideCuePolicy.previewLabel("", "GD-345", "Rain Shelter")
        );
        assertEquals("Rain Shelter", SearchLinkedGuideCuePolicy.previewLabel("", "", "Rain Shelter"));
        assertEquals("GD-345", SearchLinkedGuideCuePolicy.previewLabel("", "GD-345", ""));
    }

    @Test
    public void compactCueLabelUsesStableCopyForCompactModeAndMissingGuideIds() {
        assertEquals(
            "Guide connection",
            SearchLinkedGuideCuePolicy.compactCueLabel("Display", "GD-345", "Rain Shelter", true)
        );
        assertEquals(
            "Linked guide GD-345",
            SearchLinkedGuideCuePolicy.compactCueLabel("Display", "GD-345", "Rain Shelter", false)
        );
        assertEquals(
            "Guide connection",
            SearchLinkedGuideCuePolicy.compactCueLabel("Display", "", "", false)
        );
        assertEquals(
            "Guide connection",
            SearchLinkedGuideCuePolicy.compactCueLabel("", "", "", false)
        );
    }

    @Test
    public void descriptionsAppendActionLabelOnlyWhenPresent() {
        assertEquals(
            "Guide connection available",
            SearchLinkedGuideCuePolicy.availableDescription(" ")
        );
        assertEquals(
            "Guide connection available: Open guide",
            SearchLinkedGuideCuePolicy.availableDescription(" Open guide ")
        );
        assertEquals(
            "Open cross-reference guide",
            SearchLinkedGuideCuePolicy.openDescription("")
        );
        assertEquals(
            "Open cross-reference guide: Open guide",
            SearchLinkedGuideCuePolicy.openDescription("Open guide")
        );
    }
}
