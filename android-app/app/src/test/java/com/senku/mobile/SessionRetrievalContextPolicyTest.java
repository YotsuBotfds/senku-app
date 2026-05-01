package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

public final class SessionRetrievalContextPolicyTest {
    @Test
    public void plansFocusedHouseSiteFollowUpWithSiteSelectionTerms() {
        SessionRetrievalContextPolicy.QueryPlan plan = SessionRetrievalContextPolicy.plan(
            "what about drainage and runoff?",
            true,
            "how do i choose a safe site and foundation for a small cabin",
            "Short answer: Start by checking terrain, drainage, and frost-safe foundations.",
            List.of(
                source(
                    "Shelter Site Selection & Hazard Assessment",
                    "GD-446",
                    "Terrain Analysis",
                    "survival",
                    "route-focus",
                    "cabin_house",
                    "site_selection,drainage,foundation"
                )
            ),
            QueryMetadataProfile.fromQuery(
                "how do i choose a safe site and foundation for a small cabin what about drainage and runoff?"
            )
        );

        assertTrue(plan.sessionUsed);
        assertEquals(plan.searchQuery, plan.contextSelectionQuery);
        assertTrue(plan.searchQuery.startsWith("drainage runoff"));
        assertTrue(plan.searchQuery.contains("foundation"));
        assertTrue(plan.searchQuery.contains("site"));
        assertTrue(plan.searchQuery.contains("selection"));
        assertFalse(plan.searchQuery.startsWith("drainage runoff cabin house"));
    }

    @Test
    public void plansWaterDistributionFollowUpWithoutContainerRotationNoise() {
        SessionRetrievalContextPolicy.QueryPlan plan = SessionRetrievalContextPolicy.plan(
            "what about storage tanks",
            true,
            "how do i design a gravity-fed water distribution system",
            "Short answer: Start with elevation, flow path, and storage planning.",
            List.of(
                source(
                    "Water System Design and Distribution",
                    "GD-553",
                    "Gravity-Fed Distribution Systems",
                    "building",
                    "guide-focus",
                    "water_storage",
                    "water_storage,water_distribution"
                )
            ),
            QueryMetadataProfile.fromQuery(
                "how do i design a gravity-fed water distribution system what about storage tanks"
            )
        );

        assertTrue(plan.sessionUsed);
        assertEquals(plan.searchQuery, plan.contextSelectionQuery);
        assertTrue(plan.searchQuery.startsWith("storage tanks water distribution"));
        assertTrue(plan.searchQuery.contains("storage"));
        assertTrue(plan.searchQuery.contains("water"));
        assertTrue(plan.searchQuery.contains("distribution"));
        assertFalse(plan.searchQuery.contains("container sanitation"));
        assertFalse(plan.searchQuery.contains("rotation"));
    }

    @Test
    public void keepsTallowFollowUpTightToCandleContext() {
        SessionRetrievalContextPolicy.QueryPlan plan = SessionRetrievalContextPolicy.plan(
            "what about using tallow",
            true,
            "how do i make candles for light",
            "Short answer: Tallow is the fallback when beeswax is not available.",
            List.of(
                source("Lighting Production", "GD-122", "Emergency Lighting", "fire", "guide-focus", "", ""),
                source("Animal Fat Rendering and Tallow Uses", "GD-486", "Tallow Candles", "fire", "guide-focus", "", "")
            ),
            QueryMetadataProfile.fromQuery("how do i make candles for light what about using tallow")
        );

        assertTrue(plan.sessionUsed);
        assertEquals("tallow candles", plan.searchQuery);
        assertEquals("tallow candles", plan.contextSelectionQuery);
        assertFalse(plan.searchQuery.contains("fuel"));
    }

    @Test
    public void leavesStandaloneQueryUnchangedWhenContextDisabled() {
        String standalone = "how do i build a kiln for pottery from local clay and brick";

        SessionRetrievalContextPolicy.QueryPlan plan = SessionRetrievalContextPolicy.plan(
            standalone,
            false,
            "what's the safest way to store treated water long term",
            "Short answer: Use known food-safe containers and rotate them.",
            Collections.emptyList(),
            QueryMetadataProfile.fromQuery(standalone)
        );

        assertFalse(plan.sessionUsed);
        assertEquals(standalone, plan.searchQuery);
        assertEquals(standalone, plan.contextSelectionQuery);
        assertEquals(standalone, plan.deterministicFallbackQuery);
    }

    @Test
    public void leavesStandaloneQueryUnchangedWhenContextIsRequestedWithoutHistory() {
        String standalone = "what materials make the best tinder in wet weather";

        SessionRetrievalContextPolicy.QueryPlan plan = SessionRetrievalContextPolicy.plan(
            standalone,
            true,
            "",
            "",
            Collections.emptyList(),
            QueryMetadataProfile.fromQuery(standalone)
        );

        assertFalse(plan.sessionUsed);
        assertEquals(standalone, plan.searchQuery);
        assertEquals(standalone, plan.contextSelectionQuery);
        assertEquals(standalone, plan.deterministicFallbackQuery);
    }

    private static SearchResult source(
        String title,
        String guideId,
        String sectionHeading,
        String category,
        String retrievalMode,
        String structureType,
        String topicTags
    ) {
        return new SearchResult(
            title,
            "",
            "",
            "",
            guideId,
            sectionHeading,
            category,
            retrievalMode,
            "",
            "",
            structureType,
            topicTags
        );
    }
}
