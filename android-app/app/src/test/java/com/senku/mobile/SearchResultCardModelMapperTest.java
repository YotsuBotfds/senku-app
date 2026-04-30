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
        assertEquals(
            "Tarp & Cord Shelters: Keep runoff away from the sleeping area.",
            model.getSnippet()
        );
        assertEquals("Concept match", model.getLaneLabel());
        assertEquals(0xFF112233, model.getLaneColorArgb());
        assertEquals("74", model.getRankLabel());
        assertEquals("GD-345", model.getGuideIdLabel());
        assertEquals("Role: Topic // Window: Long Term // Category: Shelter", model.getMetadataLine());
        assertTrue(model.getShowContinueThreadChip());
        assertEquals("Continue", model.getContinueThreadLabel());
        assertEquals("Continue conversation about GD-345", model.getContinueThreadContentDescription());
        assertEquals("Guide", model.getLinkedGuideLabel());
        assertEquals(
            "Open cross-reference guide: GD-214 - Water Storage",
            model.getLinkedGuideContentDescription()
        );
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
        assertEquals("92", model.getRankLabel());
        assertEquals("", model.getMetadataLine());
        assertFalse(model.getShowContinueThreadChip());
        assertEquals("Continue conversation about this result", model.getContinueThreadContentDescription());
        assertNull(model.getLinkedGuideLabel());
        assertNull(model.getLinkedGuideContentDescription());
    }

    @Test
    public void rowRankScoreAndGuideMarkersMatchAdapterSearchRowContract() {
        assertEquals("92", SearchResultCardModelMapper.buildRankLabelForTest(0));
        assertEquals("61", SearchResultCardModelMapper.buildRankLabelForTest(3));
        assertEquals("92", SearchResultCardModelMapper.buildRankLabelForTest(-1));
        assertEquals("49", SearchResultCardModelMapper.buildTabletScoreLabelForTest(5));
        assertEquals("GD-345", SearchResultCardModelMapper.buildTabletGuideMarkerForTest("GD-345", 2));
        assertEquals("#3", SearchResultCardModelMapper.buildTabletGuideMarkerForTest("", 2));
    }

    @Test
    public void rowAttributeLineMatchesAdapterPreviewRailContract() {
        assertEquals(
            "SHELTER \u00b7 TOPIC \u00b7 WINDOW IMMEDIATE",
            SearchResultCardModelMapper.buildTabletAttributeLineForTest("shelter", "role_topic", "immediate")
        );
        assertEquals(
            "SHELTER \u00b7 TOPIC \u00b7 WINDOW LONG",
            SearchResultCardModelMapper.buildTabletAttributeLineForTest("shelter", "role_topic", "long-term")
        );
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
}
