package com.senku.mobile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class SearchResultAdapterTest {
    @Test
    public void humanizeContentRoleStripsLegacyRolePrefixForDisplay() {
        assertEquals("Subsystem", SearchResultAdapter.humanizeContentRoleForTest("ROLE_SUBSYSTEM", 22));
        assertEquals("Planning", SearchResultAdapter.humanizeContentRoleForTest("role-planning", 22));
    }

    @Test
    public void humanizeContentRoleKeepsUnprefixedValuesReadable() {
        assertEquals("Safety", SearchResultAdapter.humanizeContentRoleForTest("safety", 22));
    }

    @Test
    public void retrievalModeLabelsUseResultHierarchyLanguage() {
        assertEquals("Best match", SearchResultAdapter.displayLabelForRetrievalModeForTest("hybrid"));
        assertEquals("Best match", SearchResultAdapter.displayLabelForRetrievalModeForTest("route-focus"));
        assertEquals("Concept match", SearchResultAdapter.displayLabelForRetrievalModeForTest("vector"));
        assertEquals("Keyword match", SearchResultAdapter.displayLabelForRetrievalModeForTest("lexical"));
        assertEquals("Related guide", SearchResultAdapter.displayLabelForRetrievalModeForTest("guide-focus"));
    }

    @Test
    public void composeBridgeUsesCompactScoreMarkers() {
        assertEquals("92", SearchResultAdapter.buildRankLabelForTest(0));
        assertEquals("61", SearchResultAdapter.buildRankLabelForTest(3));
        assertEquals("92", SearchResultAdapter.buildRankLabelForTest(-1));
    }

    @Test
    public void tabletRowsUseCompactScoreMarkers() {
        assertEquals("92", SearchResultAdapter.buildTabletScoreLabelForTest(0));
        assertEquals("78", SearchResultAdapter.buildTabletScoreLabelForTest(1));
        assertEquals("61", SearchResultAdapter.buildTabletScoreLabelForTest(3));
        assertEquals("49", SearchResultAdapter.buildTabletScoreLabelForTest(5));
    }

    @Test
    public void tabletRowsPreferGuideIdAsRankMarker() {
        assertEquals("GD-345", SearchResultAdapter.buildTabletGuideMarkerForTest("GD-345", 2));
        assertEquals("#3", SearchResultAdapter.buildTabletGuideMarkerForTest("", 2));
    }

    @Test
    public void tabletRowsFlattenMetadataIntoPreviewRailTokens() {
        assertEquals(
            "SHELTER \u00b7 TOPIC \u00b7 WINDOW IMMEDIATE",
            SearchResultAdapter.buildTabletAttributeLineForTest("shelter", "role_topic", "immediate")
        );
        assertEquals(
            "SURVIVAL \u00b7 STARTER \u00b7 WINDOW IMMEDIATE",
            SearchResultAdapter.buildTabletAttributeLineForTest("survival", "role_starter", "immediate")
        );
        assertEquals("", SearchResultAdapter.buildTabletAttributeLineForTest("general", "none", "unknown"));
    }

    @Test
    public void compactRowSnippetCollapsesRepeatedSectionLeadBeforeTruncating() {
        assertEquals(
            "Shelter Building: Protection from the Elements Day signaling vs. night signaling.",
            SearchResultAdapter.buildCompactRowSnippetForTest(
                "Shelter Building: Shelter Building: Protection from the Elements Day signaling vs. night signaling.",
                "Shelter Building",
                120
            )
        );
        assertEquals(
            "Shelter Building: Protection from the Elements Day\u2026",
            SearchResultAdapter.buildCompactRowSnippetForTest(
                "Shelter Building: Shelter Building: Protection from the Elements Day signaling vs. night signaling.",
                "Shelter Building",
                52
            )
        );
    }

    @Test
    public void compactRowSnippetRemovesGuidePrefixNoise() {
        assertEquals(
            "Shelter Building: Protection from the Elements.",
            SearchResultAdapter.buildCompactRowSnippetForTest(
                "Guide: GD-023 Shelter Building: Protection from the Elements.",
                "Shelter Building",
                120
            )
        );
        assertEquals(
            "Use a ridgeline to shed rain.",
            SearchResultAdapter.buildCompactRowSnippetForTest(
                "Guide: Tarp & Cord Shelters Use a ridgeline to shed rain.",
                "Tarp & Cord Shelters",
                120
            )
        );
    }

    @Test
    public void composeBridgeMetadataLineUsesRoleWindowAndCategory() {
        assertEquals(
            "Role: Safety // Window: Immediate // Category: Water",
            SearchResultAdapter.buildCardMetadataLineForTest("role_safety", "immediate", "water")
        );
        assertEquals(
            "Category: Shelter",
            SearchResultAdapter.buildCardMetadataLineForTest("general", "unknown", "shelter")
        );
    }

    @Test
    public void composeBridgeMetadataLineSuppressesPlaceholderTokens() {
        assertEquals("", SearchResultAdapter.buildCardMetadataLineForTest("general", "none", "unknown"));
        assertEquals(
            "Window: Long Term",
            SearchResultAdapter.buildCardMetadataLineForTest("none", "long-term", "general")
        );
    }

    @Test
    public void cleanDisplayTextStripsMarkdownBeforeTruncating() {
        assertEquals("Boil water", SearchResultAdapter.cleanDisplayTextForTest(" **Boil** `water` ", 40));
        assertEquals("Use clean\u2026", SearchResultAdapter.cleanDisplayTextForTest("Use clean containers", 10));
    }

    @Test
    public void linkedGuideBrowseHandoffDescriptionsUseConnectionLanguage() {
        assertEquals(
            "Guide connection available: GD-214 - Water Storage",
            SearchResultAdapter.buildLinkedGuideAvailableDescriptionForTest("GD-214 - Water Storage")
        );
        assertEquals(
            "Open cross-reference guide: GD-214 - Water Storage",
            SearchResultAdapter.buildLinkedGuideOpenDescriptionForTest("GD-214 - Water Storage")
        );
    }

    @Test
    public void linkedGuideChipKeepsCompactVisibleCue() {
        assertEquals("Guide", SearchResultAdapter.buildLinkedGuideChipLabelForTest());
    }

    @Test
    public void linkedGuidePreviewLineKeepsRowsCompact() {
        assertEquals("Guide", SearchResultAdapter.buildLinkedGuidePreviewLineForTest());
    }

    @Test
    public void linkedGuidePreviewLineIsSuppressedInSearchRows() {
        assertFalse(SearchResultAdapter.shouldShowLinkedGuidePreviewLineForTest());
    }

    @Test
    public void reviewRainShelterVisualStateSuppressesLinkedGuideCueOnlyForMockQuery() {
        SearchResult reviewResult = resultWithSubtitle("GD-023 | Survival | review");
        SearchResult normalResult = resultWithSubtitle("");

        assertTrue(SearchResultAdapter.shouldSuppressLinkedGuideCueForResultForTest(" rain shelter ", reviewResult));
        assertFalse(SearchResultAdapter.shouldSuppressLinkedGuideCueForResultForTest(" rain shelter ", normalResult));
        assertFalse(SearchResultAdapter.shouldSuppressLinkedGuideCueForResultForTest("water", reviewResult));
        assertFalse(SearchResultAdapter.shouldSuppressLinkedGuideCueForResultForTest("", reviewResult));
    }

    private static SearchResult resultWithSubtitle(String subtitle) {
        return new SearchResult(
            "Survival Basics & First 72 Hours",
            subtitle,
            "",
            "",
            "GD-023",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
        );
    }
}
