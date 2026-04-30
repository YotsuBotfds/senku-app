package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PackQueryPipelineHelperTest {
    @Test
    public void mapGuideRowBuildsGuideSearchResultWithClippedDescription() {
        SearchResult result = PackQueryPipelineHelper.mapGuideRow(
            "Water Storage",
            "GD-252",
            "resource-management",
            "easy",
            "Use food-safe containers. ".repeat(12),
            "Full guide body",
            "planning",
            "long_term",
            "water_storage",
            "container_sanitation"
        );

        assertEquals("Water Storage", result.title);
        assertEquals("GD-252 | resource-management | easy", result.subtitle);
        assertEquals("GD-252", result.guideId);
        assertEquals("guide", result.retrievalMode);
        assertEquals("planning", result.contentRole);
        assertTrue(result.snippet.length() <= 180);
        assertTrue(result.snippet.endsWith("..."));
    }

    @Test
    public void mapChunkResultBuildsPipelineSubtitleAndClipsDocument() {
        SearchResult result = PackQueryPipelineHelper.mapChunkResult(
            "Foundations and Footings",
            "GD-383",
            "Frost Line",
            "building",
            "Footing choices and drainage. ".repeat(12),
            "hybrid",
            "starter",
            "long_term",
            "general",
            "foundation,drainage"
        );

        assertEquals("GD-383 | building | Frost Line | hybrid", result.subtitle);
        assertEquals("Frost Line", result.sectionHeading);
        assertEquals("hybrid", result.retrievalMode);
        assertTrue(result.snippet.length() <= 220);
        assertTrue(result.snippet.endsWith("..."));
    }

    @Test
    public void retrievalModeForRanksMatchesPipelineModes() {
        assertEquals("hybrid", PackQueryPipelineHelper.retrievalModeForRanks(0, 2));
        assertEquals("vector", PackQueryPipelineHelper.retrievalModeForRanks(Integer.MAX_VALUE, 2));
        assertEquals("lexical", PackQueryPipelineHelper.retrievalModeForRanks(0, Integer.MAX_VALUE));
    }

    @Test
    public void guideSectionKeyUsesGuideIdThenTitleFallback() {
        assertEquals("gd-383::frost line", PackQueryPipelineHelper.guideSectionKey("GD-383", "", "Frost Line"));
        assertEquals("water storage", PackQueryPipelineHelper.guideSectionKey("", "Water Storage", ""));
    }

    @Test
    public void searchSummaryUsesFallbackBreakdownWhenNull() {
        String line = PackQueryPipelineHelper.buildSearchSummaryLine(
            "query",
            false,
            0,
            1,
            2,
            3,
            null
        );

        assertTrue(line.contains("fallback=unknown"));
        assertTrue(line.contains("rerankMs=0"));
    }

    @Test
    public void slowTripwirePrefersFirstSlowStageAndClampsNegativeDurations() {
        PackQueryPipelineHelper.SearchLatencyBreakdown breakdown =
            new PackQueryPipelineHelper.SearchLatencyBreakdown(-1L, 130L, 999L, 0L, 0L, 0L, "fts_path");

        String line = PackQueryPipelineHelper.buildSlowQueryTripwireDebugLine("rain shelter", breakdown);

        assertTrue(breakdown.hasSlowStage());
        assertEquals("fts", breakdown.firstSlowStage());
        assertTrue(line.contains("stage=fts"));
        assertTrue(line.contains("routeMs=0"));
    }

    @Test
    public void rerankTimingLineClampsNegativeInputs() {
        String line = PackQueryPipelineHelper.buildRerankTimingDebugLine("query", -1, -4, -2, -10L);

        assertTrue(line.contains("topK=0"));
        assertTrue(line.contains("chunks=0"));
        assertTrue(line.contains("selected=0"));
        assertTrue(line.contains("totalRerankMs=0.000"));
    }

}
