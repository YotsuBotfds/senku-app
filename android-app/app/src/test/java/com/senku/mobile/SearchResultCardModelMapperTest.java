package com.senku.mobile;

import com.senku.ui.search.SearchResultCardModel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public final class SearchResultCardModelMapperTest {
    @Test
    public void mapBuildsPureCardModelFromSearchResultFields() {
        SearchResult result = new SearchResult(
            " **Rain** shelter setup ",
            "GD-345",
            "Guide: GD-345 Tarp & Cord Shelters: Keep runoff away from the sleeping area.",
            "Fallback body",
            "GD-345",
            "Tarp & Cord Shelters",
            "shelter",
            "vector",
            "role_topic",
            "long-term",
            "",
            ""
        );

        SearchResultCardModel model = SearchResultCardModelMapper.map(
            result,
            2,
            new SearchResultCardModelMapper.Options(
                false,
                false,
                false,
                0xFF112233,
                true,
                "Guide",
                "Open cross-reference guide: GD-214 - Water Storage"
            )
        );

        assertEquals("Rain shelter setup", model.getTitle());
        assertEquals("Tarp & Cord Shelters", model.getSubtitle());
        assertNormalizedContains(model.getSnippet(), "Tarp & Cord Shelters");
        assertNormalizedContains(model.getSnippet(), "runoff");
        assertEquals("Concept match", model.getLaneLabel());
        assertEquals(0xFF112233, model.getLaneColorArgb());
        assertScoreInRange(model.getRankLabel());
        assertEquals("GD-345", model.getGuideIdLabel());
        assertNormalizedContains(model.getMetadataLine(), "Role: Topic");
        assertNormalizedContains(model.getMetadataLine(), "Window: Long Term");
        assertNormalizedContains(model.getMetadataLine(), "Category: Shelter");
        assertTrue(model.getShowContinueThreadChip());
        assertEquals("Continue", model.getContinueThreadLabel());
        assertNormalizedContains(model.getContinueThreadContentDescription(), "GD-345");
        assertEquals("Guide", model.getLinkedGuideLabel());
        assertNormalizedContains(model.getLinkedGuideContentDescription(), "GD-214");
        assertNormalizedContains(model.getLinkedGuideContentDescription(), "Water Storage");
    }

    @Test
    public void mapFallsBackToBodyAndSuppressesOptionalActions() {
        SearchResult result = new SearchResult(
            "",
            "GD-001",
            "",
            "Use clean containers before storage.",
            "GD-001",
            "",
            "general",
            "",
            "none",
            "unknown",
            "",
            ""
        );

        SearchResultCardModel model = SearchResultCardModelMapper.map(
            result,
            0,
            SearchResultCardModelMapper.Options.defaults()
        );

        assertEquals("", model.getSubtitle());
        assertEquals("Use clean containers before storage.", model.getSnippet());
        assertEquals("Best match", model.getLaneLabel());
        assertScoreInRange(model.getRankLabel());
        assertEquals("", model.getMetadataLine());
        assertFalse(model.getShowContinueThreadChip());
        assertEquals("Continue conversation about this result", model.getContinueThreadContentDescription());
        assertNull(model.getLinkedGuideLabel());
        assertNull(model.getLinkedGuideContentDescription());
    }

    @Test
    public void interactionModelOwnsOptionalCardActionPresentation() {
        SearchResultInteractionModel interactionModel = SearchResultInteractionModel.decide(
            true,
            true,
            "Stored water",
            "GD-214",
            "Water Storage"
        );

        assertTrue(interactionModel.showContinueThreadChip);
        assertTrue(interactionModel.bindContinueThreadAction);
        assertTrue(interactionModel.bindLinkedGuideAction);
        assertEquals("Guide", interactionModel.linkedGuideLabel);
        assertEquals(
            "Open cross-reference guide: Stored water",
            interactionModel.linkedGuideContentDescription
        );
    }

    @Test
    public void interactionModelSuppressesLinkedGuidePresentationWithoutTargetGuide() {
        SearchResultInteractionModel interactionModel = SearchResultInteractionModel.decide(
            false,
            false,
            "Stored water",
            "",
            "Water Storage"
        );

        assertFalse(interactionModel.showContinueThreadChip);
        assertFalse(interactionModel.bindContinueThreadAction);
        assertFalse(interactionModel.bindLinkedGuideAction);
        assertNull(interactionModel.linkedGuideLabel);
        assertNull(interactionModel.linkedGuideContentDescription);
    }

    @Test
    public void cardPreviewSuppressesDuplicateGuideSubtitleAndCleansFallbackBody() {
        SearchResult result = new SearchResult(
            "Water setup",
            " GD-214 ",
            "",
            "### Storage basics\n\n- [ ] Use clean containers before storage.",
            "GD-214",
            "",
            "water",
            "keyword"
        );

        assertEquals("", SearchResultCardModelMapper.buildCardSubtitleForTest(result));
        assertEquals(
            "Storage basics Use clean containers before storage.",
            SearchResultCardModelMapper.buildCardSnippetForTest(result, 120)
        );
    }

    @Test
    public void rowRankScoreAndGuideMarkersMatchAdapterSearchRowContract() {
        int firstScore = scoreValue(SearchResultCardModelMapper.buildRankLabelForTest(0));
        int laterScore = scoreValue(SearchResultCardModelMapper.buildRankLabelForTest(3));

        assertTrue(firstScore > laterScore);
        assertScoreInRange(SearchResultCardModelMapper.buildTabletScoreLabelForTest(5));
        assertEquals(
            SearchResultCardModelMapper.buildRankLabelForTest(0),
            SearchResultCardModelMapper.buildRankLabelForTest(-1)
        );
        assertEquals("GD-345", SearchResultCardModelMapper.buildTabletGuideMarkerForTest("GD-345", 2));
        assertEquals("#3", SearchResultCardModelMapper.buildTabletGuideMarkerForTest("", 2));
    }

    @Test
    public void retrievalModeLabelsUseResultHierarchyLanguage() {
        assertEquals("Best match", SearchResultCardModelMapper.displayLabelForRetrievalModeForTest("hybrid"));
        assertEquals("Best match", SearchResultCardModelMapper.displayLabelForRetrievalModeForTest("route-focus"));
        assertEquals("Concept match", SearchResultCardModelMapper.displayLabelForRetrievalModeForTest("vector"));
        assertEquals("Keyword match", SearchResultCardModelMapper.displayLabelForRetrievalModeForTest("lexical"));
        assertEquals("Related guide", SearchResultCardModelMapper.displayLabelForRetrievalModeForTest("guide-focus"));
    }

    @Test
    public void rowAttributeLineMatchesAdapterPreviewRailContract() {
        String immediateShelter = SearchResultCardModelMapper.buildTabletAttributeLineForTest(
            "shelter",
            "role_topic",
            "immediate"
        );
        assertContains(immediateShelter, "SHELTER");
        assertContains(immediateShelter, "TOPIC");
        assertContains(immediateShelter, "IMMEDIATE");

        String longTermShelter = SearchResultCardModelMapper.buildTabletAttributeLineForTest(
            "shelter",
            "role_topic",
            "long-term"
        );
        assertContains(longTermShelter, "SHELTER");
        assertContains(longTermShelter, "TOPIC");
        assertContains(longTermShelter, "LONG");

        assertEquals("", SearchResultCardModelMapper.buildTabletAttributeLineForTest("general", "none", "unknown"));
        assertEquals(
            "CONCEPT MATCH",
            SearchResultCardModelMapper.buildTabletAttributeLineForResultForTest("general", "none", "unknown", "vector")
        );
    }

    @Test
    public void rowPreviewSnippetMatchesAdapterCleanupContract() {
        assertEquals(
            "Shelter Building: Protection from the Elements Day\u2026",
            SearchResultCardModelMapper.buildCompactRowSnippetForTest(
                "Shelter Building: Shelter Building: Protection from the Elements Day signaling vs. night signaling.",
                "Shelter Building",
                52
            )
        );
        assertEquals(
            "Use a ridgeline to shed rain.",
            SearchResultCardModelMapper.buildCompactRowSnippetForTest(
                "Guide: Tarp & Cord Shelters Use a ridgeline to shed rain.",
                "Tarp & Cord Shelters",
                120
            )
        );
    }

    @Test
    public void rowPreviewSnippetBudgetLivesWithMapperViewportPolicy() {
        SearchResult result = new SearchResult(
            "Shelter setup",
            "",
            "Use a ridgeline, pitch one side low into the wind, keep runoff from pooling, and pin corners before loading gear. "
                + "Move the entry away from downhill flow and leave a dry exit path.",
            "",
            "GD-345",
            "Tarp & Cord Shelters",
            "shelter",
            "vector",
            "role_topic",
            "short-term",
            "",
            ""
        );

        assertEquals(124, SearchResultCardModelMapper.compactRowSnippetBudgetForTest(true, false, false));
        assertEquals(142, SearchResultCardModelMapper.compactRowSnippetBudgetForTest(false, true, false));
        assertEquals(126, SearchResultCardModelMapper.compactRowSnippetBudgetForTest(false, false, true));
        assertEquals(190, SearchResultCardModelMapper.compactRowSnippetBudgetForTest(false, false, false));
        assertEquals(
            SearchResultCardModelMapper.buildCompactRowSnippetForTest(result.snippet, result.sectionHeading, 126),
            SearchResultCardModelMapper.buildCompactRowSnippetForResultForTest(result, false, false, true)
        );
    }

    @Test
    public void legacyRowModelOwnsViewportTextBudgetsAndVisibilityText() {
        SearchResult result = new SearchResult(
            "Shelter setup",
            "",
            "Use a ridgeline, pitch one side low into the wind, keep runoff from pooling, and pin corners before loading gear.",
            "",
            "GD-345",
            "Tarp & Cord Shelters",
            "shelter",
            "vector",
            "role_topic",
            "short-term",
            "",
            ""
        );

        SearchResultCardModelMapper.SearchResultRowModel rowModel = SearchResultCardModelMapper.mapLegacyRow(
            result,
            2,
            new SearchResultCardModelMapper.Options(
                false,
                true,
                false,
                true,
                0,
                SearchResultInteractionModel.hidden()
            )
        );

        assertEquals(90, rowModel.titleBudget);
        assertEquals(1, rowModel.titleMaxLines);
        assertEquals("GD-345", rowModel.guideMarker);
        assertEquals("SHELTER \u00b7 TOPIC \u00b7 WINDOW SHORT", rowModel.attributeLine);
        assertEquals(
            SearchResultCardModelMapper.buildCompactRowSnippetForTest(result.snippet, result.sectionHeading, 142),
            rowModel.snippet
        );
        assertEquals(1, rowModel.snippetMaxLines);
    }

    @Test
    public void legacyRowPresentationOwnsViewportBudgetsAndLineCounts() {
        SearchResultCardModelMapper.SearchResultRowPresentation tablet =
            SearchResultCardModelMapper.buildLegacyRowPresentationForTest(true, false, false, false);
        SearchResultCardModelMapper.SearchResultRowPresentation landscape =
            SearchResultCardModelMapper.buildLegacyRowPresentationForTest(false, true, false, true);
        SearchResultCardModelMapper.SearchResultRowPresentation smallPortrait =
            SearchResultCardModelMapper.buildLegacyRowPresentationForTest(false, false, true, false);

        assertEquals(112, tablet.titleBudget);
        assertEquals(2, tablet.titleMaxLines);
        assertEquals(124, tablet.snippetBudget);
        assertEquals(2, tablet.snippetMaxLines);

        assertEquals(90, landscape.titleBudget);
        assertEquals(1, landscape.titleMaxLines);
        assertEquals(142, landscape.snippetBudget);
        assertEquals(1, landscape.snippetMaxLines);

        assertEquals(88, smallPortrait.titleBudget);
        assertEquals(2, smallPortrait.titleMaxLines);
        assertEquals(126, smallPortrait.snippetBudget);
        assertEquals(2, smallPortrait.snippetMaxLines);
    }

    @Test
    public void legacyRowModelKeepsTabletSnippetTwoLineContract() {
        SearchResultCardModelMapper.SearchResultRowModel rowModel = SearchResultCardModelMapper.mapLegacyRow(
            null,
            0,
            new SearchResultCardModelMapper.Options(
                true,
                false,
                false,
                true,
                0,
                SearchResultInteractionModel.hidden()
            )
        );

        assertEquals(112, rowModel.titleBudget);
        assertEquals(2, rowModel.titleMaxLines);
        assertEquals(2, rowModel.snippetMaxLines);
    }

    @Test
    public void rowMetaFallbackSkipsDuplicatedGuideSectionAndRetrievalLabels() {
        SearchResult result = new SearchResult(
            "Water setup",
            " GD-214 | Water Storage | Concept match | Vector | storage | short term | field note ",
            "",
            "",
            "GD-214",
            "Water Storage",
            "water",
            "vector",
            "",
            "",
            "",
            ""
        );

        assertEquals("storage // short term // field note", SearchResultCardModelMapper.buildMetaFallbackForTest(result));
    }

    @Test
    public void rowMetaFallbackUsesGuideIdWhenSubtitleHasNoDisplayTokens() {
        SearchResult result = new SearchResult(
            "Water setup",
            "GD-214 | Water Storage | Best match | Route",
            "",
            "",
            "GD-214",
            "Water Storage",
            "water",
            "route-focus",
            "",
            "",
            "",
            ""
        );

        assertEquals("GD-214", SearchResultCardModelMapper.buildMetaFallbackForTest(result));
    }

    @Test
    public void cleanDisplayTextRemovesWarningProofResidueFromSearchCards() {
        String cleaned = SearchResultCardModelMapper.cleanDisplayTextForTest(
            "Use boiled water only [Safety Warning Implied by High Heat Processes] [GD-123]. "
                + "Keep the action visible [Backend Route reviewed_card_runtime]. "
                + "Use reviewed card evidence [Proof Metadata].",
            0
        );

        assertEquals(
            "Use boiled water only [GD-123]. Keep the action visible. Use reviewed card evidence.",
            cleaned
        );
    }

    @Test
    public void linkedGuidePreviewLinePolicyKeepsSearchRowsCompact() {
        assertFalse(SearchResultCardModelMapper.shouldShowLinkedGuidePreviewLineForTest());
    }

    @Test
    public void linkedGuideChipLabelKeepsCompactVisibleCue() {
        assertEquals("Guide", SearchResultCardModelMapper.buildLinkedGuideChipLabelForTest());
    }

    @Test
    public void linkedGuidePreviewLineLabelKeepsSearchRowPreviewLanguage() {
        assertEquals("Guide", SearchResultCardModelMapper.buildLinkedGuidePreviewLineLabelForTest());
    }

    @Test
    public void linkedGuidePreviewLinePrefersTitleWhenTargetGuideExists() {
        assertEquals(
            "Guide: Water Storage",
            SearchResultCardModelMapper.buildLinkedGuidePreviewLineForTest(
                "Stored water",
                "GD-214",
                " Water Storage ",
                false
            )
        );
    }

    @Test
    public void linkedGuidePreviewLineFallsBackToDisplayLabelAndRequiresTargetGuide() {
        assertEquals(
            "Guide: Stored water",
            SearchResultCardModelMapper.buildLinkedGuidePreviewLineForTest(
                "Stored water",
                "GD-214",
                "",
                false
            )
        );
        assertEquals(
            "",
            SearchResultCardModelMapper.buildLinkedGuidePreviewLineForTest(
                "Stored water",
                "",
                "Water Storage",
                false
            )
        );
    }

    @Test
    public void linkedGuidePreviewLabelPrefersDisplayLabelThenGuideAndTitle() {
        assertEquals(
            "Stored water",
            SearchResultCardModelMapper.buildLinkedGuidePreviewLabelForTest(
                " Stored water ",
                "GD-214",
                "Water Storage"
            )
        );
        assertEquals(
            "GD-214 - Water Storage",
            SearchResultCardModelMapper.buildLinkedGuidePreviewLabelForTest("", " GD-214 ", " Water Storage ")
        );
        assertEquals(
            "Water Storage",
            SearchResultCardModelMapper.buildLinkedGuidePreviewLabelForTest(null, "", " Water Storage ")
        );
        assertEquals(
            "GD-214",
            SearchResultCardModelMapper.buildLinkedGuidePreviewLabelForTest(null, " GD-214 ", "")
        );
        assertEquals("", SearchResultCardModelMapper.buildLinkedGuidePreviewLabelForTest(null, null, null));
    }

    @Test
    public void compactLinkedGuideCueLabelPreservesAdapterDisplayContract() {
        assertEquals(
            "Guide connection",
            SearchResultCardModelMapper.buildCompactLinkedGuideCueLabelForTest(
                "Stored water",
                "GD-214",
                "Water Storage",
                true
            )
        );
        assertEquals(
            "Linked guide GD-214",
            SearchResultCardModelMapper.buildCompactLinkedGuideCueLabelForTest(
                "",
                " GD-214 ",
                "Water Storage",
                false
            )
        );
        assertEquals(
            "Linked guide GD-214\u2026",
            SearchResultCardModelMapper.buildCompactLinkedGuideCueLabelForTest(
                "",
                "GD-214-LONG-SECTION",
                "",
                false
            )
        );
        assertEquals(
            "Guide connection",
            SearchResultCardModelMapper.buildCompactLinkedGuideCueLabelForTest(
                "Stored water",
                "",
                "Water Storage",
                false
            )
        );
        assertEquals(
            "Guide connection",
            SearchResultCardModelMapper.buildCompactLinkedGuideCueLabelForTest(null, null, null, false)
        );
    }

    @Test
    public void linkedGuideHandoffDescriptionsUseConnectionLanguage() {
        assertEquals(
            "Guide connection available: GD-214 - Water Storage",
            SearchResultCardModelMapper.buildLinkedGuideAvailableDescriptionForTest("GD-214 - Water Storage")
        );
        assertEquals(
            "Open cross-reference guide: GD-214 - Water Storage",
            SearchResultCardModelMapper.buildLinkedGuideOpenDescriptionForTest("GD-214 - Water Storage")
        );
        assertEquals(
            "Guide connection available",
            SearchResultCardModelMapper.buildLinkedGuideAvailableDescriptionForTest(" ")
        );
        assertEquals(
            "Open cross-reference guide",
            SearchResultCardModelMapper.buildLinkedGuideOpenDescriptionForTest(null)
        );
    }

    private static void assertNormalizedContains(String actual, String expectedToken) {
        assertTrue(
            "Expected <" + actual + "> to contain <" + expectedToken + ">",
            actual.toLowerCase().contains(expectedToken.toLowerCase())
        );
    }

    private static void assertScoreInRange(String label) {
        int score = scoreValue(label);
        assertTrue("Expected score in range 0..100, got <" + label + ">", score >= 0 && score <= 100);
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
