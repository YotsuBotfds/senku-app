package com.senku.mobile;

import java.util.Arrays;

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
        int firstScore = scoreValue(SearchResultCardModelMapper.buildRankLabelForTest(0));
        int laterScore = scoreValue(SearchResultCardModelMapper.buildRankLabelForTest(3));

        assertTrue(firstScore >= 0 && firstScore <= 100);
        assertTrue(laterScore >= 0 && laterScore <= 100);
        assertTrue(firstScore > laterScore);
        assertEquals(
            SearchResultCardModelMapper.buildRankLabelForTest(0),
            SearchResultCardModelMapper.buildRankLabelForTest(-1)
        );
    }

    @Test
    public void tabletRowsUseCompactScoreMarkers() {
        int previousScore = Integer.MAX_VALUE;

        for (int i = 0; i < 6; i++) {
            int score = scoreValue(SearchResultCardModelMapper.buildTabletScoreLabelForTest(i));
            assertTrue(score >= 0 && score <= 100);
            assertTrue(score <= previousScore);
            previousScore = score;
        }
    }

    @Test
    public void tabletRowsUseCompactScoreBarWidths() {
        int firstWidth = SearchResultAdapter.scoreBarWidthDpForPositionForTest(0);

        assertTrue(firstWidth > 0);
        assertEquals(firstWidth, SearchResultAdapter.scoreBarWidthDpForPositionForTest(1));
        assertEquals(firstWidth, SearchResultAdapter.scoreBarWidthDpForPositionForTest(2));
        assertEquals(firstWidth, SearchResultAdapter.scoreBarWidthDpForPositionForTest(3));
        assertEquals(firstWidth, SearchResultAdapter.scoreBarWidthDpForPositionForTest(7));
    }

    @Test
    public void tabletRowsUseScoreSensitiveTickFill() {
        float previousFill = Float.MAX_VALUE;

        for (int i : new int[] {0, 1, 2, 3, 7}) {
            float fill = SearchResultAdapter.scoreBarFillFractionForPositionForTest(i);
            assertTrue(fill > 0f && fill <= 1f);
            assertTrue(fill <= previousFill);
            previousFill = fill;
        }
    }

    @Test
    public void tabletRowsPreferGuideIdAsRankMarker() {
        assertEquals("GD-345", SearchResultCardModelMapper.buildTabletGuideMarkerForTest("GD-345", 2));
        assertEquals("#3", SearchResultCardModelMapper.buildTabletGuideMarkerForTest("", 2));
    }

    @Test
    public void targetRainShelterRowsKeepGuideIdOrderMarkers() {
        String[] guideIds = {"GD-023", "GD-027", "GD-345", "GD-294"};
        int previousScore = Integer.MAX_VALUE;

        for (int i = 0; i < guideIds.length; i++) {
            assertEquals(guideIds[i], SearchResultCardModelMapper.buildTabletGuideMarkerForTest(guideIds[i], i));
            int score = scoreValue(SearchResultCardModelMapper.buildTabletScoreLabelForTest(i));
            assertTrue(score <= previousScore);
            previousScore = score;
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
        assertTrue(
            SearchResultAdapter.compactRowTitleTextSizeSpForTest()
                > SearchResultAdapter.compactRowSnippetTextSizeSpForTest()
        );
    }

    @Test
    public void portraitAndTabletRowsUseSecondPassDenseTypographyScale() {
        assertTrue(
            SearchResultAdapter.portraitTabletRowTitleTextSizeSpForTest()
                > SearchResultAdapter.portraitTabletRowSnippetTextSizeSpForTest()
        );
    }

    @Test
    public void searchRowTypographyKeepsDenseRev03ScaleRelationship() {
        float landscapeTitle = SearchResultAdapter.compactRowTitleTextSizeSpForTest();
        float landscapeSnippet = SearchResultAdapter.compactRowSnippetTextSizeSpForTest();
        float portraitTitle = SearchResultAdapter.portraitTabletRowTitleTextSizeSpForTest();
        float portraitSnippet = SearchResultAdapter.portraitTabletRowSnippetTextSizeSpForTest();
        float section = SearchResultAdapter.compactRowSectionTextSizeSpForTest();
        float chip = SearchResultAdapter.compactRowChipTextSizeSpForTest();

        assertTrue(landscapeTitle >= portraitTitle);
        assertTrue(landscapeTitle > portraitSnippet);
        assertTrue(portraitSnippet > landscapeSnippet);
        assertTrue(landscapeSnippet > section);
        assertEquals(section, chip, 0.001f);
        assertTrue(section > 0f);
    }

    @Test
    public void compactRowsUseSecondPassDenseVerticalRhythm() {
        assertTrue(
            SearchResultAdapter.portraitTabletRowTopPaddingDpForTest()
                >= SearchResultAdapter.compactRowTopPaddingDpForTest()
        );
        assertTrue(
            SearchResultAdapter.portraitTabletRowTitleTopMarginDpForTest()
                >= SearchResultAdapter.compactRowTitleTopMarginDpForTest()
        );
        assertTrue(
            SearchResultAdapter.portraitTabletRowSnippetTopMarginDpForTest()
                >= SearchResultAdapter.compactRowSnippetTopMarginDpForTest()
        );
        assertTrue(
            SearchResultAdapter.portraitTabletRowDividerTopMarginDpForTest()
                >= SearchResultAdapter.compactRowDividerTopMarginDpForTest()
        );
        assertTrue(
            SearchResultAdapter.compactRowDividerTopMarginDpForTest()
                > SearchResultAdapter.compactRowSnippetTopMarginDpForTest()
        );
    }

    @Test
    public void tabletRowsFlattenMetadataIntoPreviewRailTokens() {
        String immediateShelter = SearchResultAdapter.buildTabletAttributeLineForTest("shelter", "role_topic", "immediate");
        assertContains(immediateShelter, "SHELTER");
        assertContains(immediateShelter, "TOPIC");
        assertContains(immediateShelter, "IMMEDIATE");

        String starterSurvival = SearchResultAdapter.buildTabletAttributeLineForTest("survival", "role_starter", "immediate");
        assertContains(starterSurvival, "SURVIVAL");
        assertContains(starterSurvival, "STARTER");
        assertContains(starterSurvival, "IMMEDIATE");

        String longTermShelter = SearchResultAdapter.buildTabletAttributeLineForTest("shelter", "role_topic", "long-term");
        assertContains(longTermShelter, "SHELTER");
        assertContains(longTermShelter, "TOPIC");
        assertContains(longTermShelter, "LONG");

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
    public void queryHighlightingSplitsPunctuationHyphenAndUnderscoreTerms() {
        assertEquals(
            Arrays.asList("tarp", "Tie", "rain"),
            SearchResultAdapter.highlightedSegmentsForTest(
                "Pack tarp. Tie cord; shed rain.",
                "TARP.tie_RAIN",
                0,
                4
            )
        );
    }

    @Test
    public void queryHighlightingKeepsLowercaseBoundaryBehavior() {
        assertEquals(
            Arrays.asList("Rain", "SHELTER", "cord"),
            SearchResultAdapter.highlightedSegmentsForTest(
                "Rain SHELTER cord",
                "rain-shelter_cord",
                0,
                4
            )
        );
    }

    @Test
    public void queryHighlightingReturnsNoSegmentsWhenTermsDoNotMatch() {
        assertTrue(
            SearchResultAdapter.highlightedSegmentsForTest(
                "Boil water before storage.",
                "rain-shelter_cord",
                0,
                4
            ).isEmpty()
        );
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
    public void linkedGuidePreviewLabelUsesMapperOwnedDisplayFallbacks() {
        assertEquals(
            "Stored water",
            SearchResultAdapter.buildLinkedGuidePreviewLabelForTest(
                " Stored water ",
                "GD-214",
                "Water Storage"
            )
        );
        assertEquals(
            "GD-214 - Water Storage",
            SearchResultAdapter.buildLinkedGuidePreviewLabelForTest("", " GD-214 ", " Water Storage ")
        );
        assertEquals(
            SearchResultCardModelMapper.buildLinkedGuidePreviewLabelForTest(null, "", " Water Storage "),
            SearchResultAdapter.buildLinkedGuidePreviewLabelForTest(null, "", " Water Storage ")
        );
    }

    @Test
    public void linkedGuidePreviewLineKeepsRowsCompact() {
        assertEquals(
            SearchResultCardModelMapper.buildLinkedGuidePreviewLineLabelForTest(),
            SearchResultAdapter.buildLinkedGuidePreviewLineForTest()
        );
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

    private static int scoreValue(String label) {
        return Integer.parseInt(label);
    }

    private static void assertContains(String actual, String expectedToken) {
        assertTrue(
            "Expected <" + actual + "> to contain <" + expectedToken + ">",
            actual.contains(expectedToken)
        );
    }
}
