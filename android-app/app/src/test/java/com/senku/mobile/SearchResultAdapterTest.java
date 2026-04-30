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
        assertEquals("74", SearchResultAdapter.buildTabletScoreLabelForTest(2));
        assertEquals("61", SearchResultAdapter.buildTabletScoreLabelForTest(3));
        assertEquals("49", SearchResultAdapter.buildTabletScoreLabelForTest(5));
    }

    @Test
    public void tabletRowsUseCompactScoreBarWidths() {
        assertEquals(22, SearchResultAdapter.scoreBarWidthDpForPositionForTest(0));
        assertEquals(22, SearchResultAdapter.scoreBarWidthDpForPositionForTest(1));
        assertEquals(22, SearchResultAdapter.scoreBarWidthDpForPositionForTest(2));
        assertEquals(22, SearchResultAdapter.scoreBarWidthDpForPositionForTest(3));
        assertEquals(22, SearchResultAdapter.scoreBarWidthDpForPositionForTest(7));
    }

    @Test
    public void tabletRowsUseScoreSensitiveTickFill() {
        assertEquals(0.94f, SearchResultAdapter.scoreBarFillFractionForPositionForTest(0), 0.001f);
        assertEquals(0.82f, SearchResultAdapter.scoreBarFillFractionForPositionForTest(1), 0.001f);
        assertEquals(0.74f, SearchResultAdapter.scoreBarFillFractionForPositionForTest(2), 0.001f);
        assertEquals(0.62f, SearchResultAdapter.scoreBarFillFractionForPositionForTest(3), 0.001f);
        assertEquals(0.52f, SearchResultAdapter.scoreBarFillFractionForPositionForTest(7), 0.001f);
    }

    @Test
    public void tabletRowsPreferGuideIdAsRankMarker() {
        assertEquals("GD-345", SearchResultAdapter.buildTabletGuideMarkerForTest("GD-345", 2));
        assertEquals("#3", SearchResultAdapter.buildTabletGuideMarkerForTest("", 2));
    }

    @Test
    public void targetRainShelterRowsKeepGuideIdOrderMarkers() {
        String[] guideIds = {"GD-023", "GD-027", "GD-345", "GD-294"};
        String[] scoreLabels = {"92", "78", "74", "61"};

        for (int i = 0; i < guideIds.length; i++) {
            assertEquals(guideIds[i], SearchResultAdapter.buildTabletGuideMarkerForTest(guideIds[i], i));
            assertEquals(scoreLabels[i], SearchResultAdapter.buildTabletScoreLabelForTest(i));
        }
    }

    @Test
    public void adapterDisplayLimitCanHideLandscapeOverflowRowsWithoutChangingResultCountText() {
        assertEquals(3, SearchResultAdapter.boundedItemCountForTest(4, 3));
        assertEquals(4, SearchResultAdapter.boundedItemCountForTest(4, Integer.MAX_VALUE));
        assertEquals(4, SearchResultAdapter.boundedItemCountForTest(4, 0));
    }

    @Test
    public void adapterDefaultsToMockSearchRowBudget() {
        assertEquals(4, SearchResultAdapter.defaultMaxDisplayedItemsForTest());
        assertEquals(4, SearchResultAdapter.boundedItemCountForTest(75, SearchResultAdapter.defaultMaxDisplayedItemsForTest()));
    }

    @Test
    public void compactRowsUseSecondPassDenseTypographyScale() {
        assertEquals(14.0f, SearchResultAdapter.compactRowTitleTextSizeSpForTest(), 0.001f);
        assertEquals(11.0f, SearchResultAdapter.compactRowSnippetTextSizeSpForTest(), 0.001f);
    }

    @Test
    public void portraitAndTabletRowsUseSecondPassDenseTypographyScale() {
        assertEquals(13.5f, SearchResultAdapter.portraitTabletRowTitleTextSizeSpForTest(), 0.001f);
        assertEquals(11.5f, SearchResultAdapter.portraitTabletRowSnippetTextSizeSpForTest(), 0.001f);
    }

    @Test
    public void searchRowTypographyKeepsDenseRev03ScaleRelationship() {
        float landscapeTitle = SearchResultAdapter.compactRowTitleTextSizeSpForTest();
        float landscapeSnippet = SearchResultAdapter.compactRowSnippetTextSizeSpForTest();
        float portraitTitle = SearchResultAdapter.portraitTabletRowTitleTextSizeSpForTest();
        float portraitSnippet = SearchResultAdapter.portraitTabletRowSnippetTextSizeSpForTest();
        float section = SearchResultAdapter.compactRowSectionTextSizeSpForTest();
        float chip = SearchResultAdapter.compactRowChipTextSizeSpForTest();

        assertEquals(14.0f, landscapeTitle, 0.001f);
        assertEquals(13.5f, portraitTitle, 0.001f);
        assertTrue(landscapeTitle > portraitSnippet);
        assertTrue(portraitSnippet > landscapeSnippet);
        assertTrue(landscapeSnippet > section);
        assertEquals(section, chip, 0.001f);
        assertEquals(8.5f, section, 0.001f);
    }

    @Test
    public void compactRowsUseSecondPassDenseVerticalRhythm() {
        assertEquals(13, SearchResultAdapter.compactRowTopPaddingDpForTest());
        assertEquals(15, SearchResultAdapter.portraitTabletRowTopPaddingDpForTest());
        assertEquals(4, SearchResultAdapter.compactRowTitleTopMarginDpForTest());
        assertEquals(6, SearchResultAdapter.portraitTabletRowTitleTopMarginDpForTest());
        assertEquals(5, SearchResultAdapter.compactRowSnippetTopMarginDpForTest());
        assertEquals(6, SearchResultAdapter.portraitTabletRowSnippetTopMarginDpForTest());
        assertEquals(10, SearchResultAdapter.compactRowDividerTopMarginDpForTest());
        assertEquals(12, SearchResultAdapter.portraitTabletRowDividerTopMarginDpForTest());
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
        assertEquals(
            "SHELTER \u00b7 TOPIC \u00b7 WINDOW LONG",
            SearchResultAdapter.buildTabletAttributeLineForTest("shelter", "role_topic", "long-term")
        );
        assertEquals("", SearchResultAdapter.buildTabletAttributeLineForTest("general", "none", "unknown"));
    }

    @Test
    public void tabletRowsFallbackToRetrievalLaneForStableMetadataRhythm() {
        assertEquals(
            "CONCEPT MATCH",
            SearchResultAdapter.buildTabletAttributeLineForResultForTest("general", "none", "unknown", "vector")
        );
        assertEquals(
            "BEST MATCH",
            SearchResultAdapter.buildTabletAttributeLineForResultForTest("", "", "", "")
        );
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
    public void compactRowSnippetUsesShorterPreviewBudgetForDenseRows() {
        assertEquals(
            "Use a ridgeline, pitch one side low into the wind, keep runoff from pooling, and pin\u2026",
            SearchResultAdapter.buildCompactRowSnippetForTest(
                "Use a ridgeline, pitch one side low into the wind, keep runoff from pooling, and pin corners before loading gear.",
                "Tarp & Cord Shelters",
                86
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
    public void defaultProductionVisualStateDoesNotSuppressMockLinkedGuideCue() {
        SearchResult reviewResult = resultWithSubtitle("GD-023 | Survival | review");

        assertFalse(SearchResultAdapter.shouldSuppressLinkedGuideCueForResultForTest(false, " rain shelter ", reviewResult));
    }

    @Test
    public void reviewRainShelterVisualStateSuppressesLinkedGuideCueOnlyWhenEnabledForMockQuery() {
        SearchResult reviewResult = resultWithSubtitle("GD-023 | Survival | review");
        SearchResult normalResult = resultWithSubtitle("");

        assertTrue(SearchResultAdapter.shouldSuppressLinkedGuideCueForResultForTest(true, " rain shelter ", reviewResult));
        assertFalse(SearchResultAdapter.shouldSuppressLinkedGuideCueForResultForTest(true, " rain shelter ", normalResult));
        assertFalse(SearchResultAdapter.shouldSuppressLinkedGuideCueForResultForTest(true, "water", reviewResult));
        assertFalse(SearchResultAdapter.shouldSuppressLinkedGuideCueForResultForTest(true, "", reviewResult));
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
