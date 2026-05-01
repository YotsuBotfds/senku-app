package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PackRouteRefinementPolicyTest {
    @Test
    public void currentHeadGlassmakingBonusPrefersFromScratchGuide() {
        String query = "how do i make glass from silica sand and soda ash";
        SearchResult rawMaterials = result(
            "Glass Making: Raw Materials & Furnace Construction",
            "Batch Preparation & Melting Process",
            "GD-711",
            "chemistry",
            "reference",
            "immediate",
            "glassmaking",
            "glassmaking,annealing,lye_safety",
            "Silica sand, soda ash, limestone, furnace setup, melting, forming, and annealing."
        );
        SearchResult fromScratch = result(
            "Glass, Optics & Ceramics",
            "Glassmaking from Scratch",
            "GD-123",
            "crafts",
            "subsystem",
            "mixed",
            "glassmaking",
            "glassmaking,annealing",
            "Start with soda-lime glass, furnace heat, forming, and annealing."
        );

        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(query);

        assertTrue(
            PackRouteRefinementPolicy.currentHeadRouteRefinementBonus(queryTerms, fromScratch)
                > PackRouteRefinementPolicy.currentHeadRouteRefinementBonus(queryTerms, rawMaterials)
        );
        assertEquals(
            PackRepository.currentHeadRouteRefinementBonusForTest(query, fromScratch),
            PackRouteRefinementPolicy.currentHeadRouteRefinementBonus(queryTerms, fromScratch)
        );
    }

    @Test
    public void currentHeadRoofWeatherproofingBonusPrefersDedicatedWeatherproofingGuide() {
        String query = "how do i weatherproof a cabin roof";
        SearchResult construction = result(
            "Construction & Carpentry",
            "Roofing Systems",
            "GD-094",
            "building",
            "starter",
            "long_term",
            "cabin_house",
            "foundation,wall_construction,roofing",
            "General framing and roofing systems for off-grid construction."
        );
        SearchResult cabinWeatherproofing = result(
            "Insulation & Weatherproofing",
            "Roof Insulation Techniques",
            "GD-106",
            "building",
            "subsystem",
            "long_term",
            "cabin_house",
            "weatherproofing",
            "Weatherproofing, moisture barriers, roof insulation, air sealing, and climate control."
        );

        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(query);

        assertTrue(
            PackRouteRefinementPolicy.currentHeadRouteRefinementBonus(queryTerms, cabinWeatherproofing)
                > PackRouteRefinementPolicy.currentHeadRouteRefinementBonus(queryTerms, construction)
        );
        assertEquals(
            PackRepository.currentHeadRouteRefinementBonusForTest(query, cabinWeatherproofing),
            PackRouteRefinementPolicy.currentHeadRouteRefinementBonus(queryTerms, cabinWeatherproofing)
        );
    }

    @Test
    public void currentHeadBroadHouseOrderingPriorityPrefersConstructionSequence() {
        String query = "how do i build a house";
        SearchResult siteSelection = result(
            "Shelter Site Selection & Hazard Assessment",
            "Terrain Analysis",
            "GD-446",
            "survival",
            "safety",
            "immediate",
            "emergency_shelter",
            "site_selection,drainage",
            "Terrain analysis, natural hazards, wind exposure, and water proximity."
        );
        SearchResult constructionSequence = result(
            "Construction & Carpentry",
            "Foundations",
            "GD-094",
            "building",
            "subsystem",
            "long_term",
            "cabin_house",
            "drainage,foundation,wall_construction,roofing",
            "Build sequence for a house: drainage, foundations, walls, roofing, and weatherproofing."
        );

        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(query);

        assertTrue(
            PackRouteRefinementPolicy.currentHeadRouteOrderingPriority(queryTerms, constructionSequence)
                > PackRouteRefinementPolicy.currentHeadRouteOrderingPriority(queryTerms, siteSelection)
        );
        assertEquals(
            PackRepository.currentHeadRouteOrderingPriorityForTest(query, constructionSequence),
            PackRouteRefinementPolicy.currentHeadRouteOrderingPriority(queryTerms, constructionSequence)
        );
    }

    @Test
    public void broadWaterRefinementBonusPenalizesGenericInventorySection() {
        String query = "what's the safest way to store treated water long term";
        SearchResult inventory = result(
            "Home Inventory",
            "Water Storage & Purification",
            "GD-373",
            "resource-management",
            "starter",
            "long_term",
            "water_storage",
            "water_storage",
            "Keep a broad household inventory with basic emergency supplies."
        );

        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(query);
        int bonus = PackRouteRefinementPolicy.broadWaterRouteRefinementBonus(queryTerms, inventory);

        assertTrue(bonus <= -40);
        assertEquals(
            PackRepository.broadWaterRouteRefinementBonusForTest(query, inventory),
            bonus
        );
    }

    @Test
    public void broadWaterRefinementBonusBoostsSpecializedPlanningGuide() {
        String query = "what's the safest way to store treated water long term";
        SearchResult planning = result(
            "Storage & Material Management",
            "",
            "GD-252",
            "resource-management",
            "planning",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation",
            "Use food-safe containers, sanitize them, and rotate treated water on a schedule.",
            "guide-focus"
        );

        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(query);
        int bonus = PackRouteRefinementPolicy.broadWaterRouteRefinementBonus(queryTerms, planning);

        assertTrue(bonus >= 30);
        assertEquals(
            PackRepository.broadWaterRouteRefinementBonusForTest(query, planning),
            bonus
        );
    }

    @Test
    public void broadWaterOrderingPriorityDemotesContainerCraftGuideWithoutBuildIntent() {
        String query = "what's the safest way to store treated water long term";
        SearchResult craftGuide = result(
            "Storage Container and Vessel Making",
            "",
            "GD-417",
            "building",
            "safety",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation",
            "Make storage vessels and containers from available materials.",
            "guide-focus"
        );
        SearchResult planning = result(
            "Storage & Material Management",
            "",
            "GD-252",
            "resource-management",
            "planning",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation",
            "Use food-safe containers, sanitize them, and rotate treated water on a schedule.",
            "guide-focus"
        );

        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(query);

        assertTrue(
            PackRouteRefinementPolicy.broadWaterRouteOrderingPriority(queryTerms, planning)
                > PackRouteRefinementPolicy.broadWaterRouteOrderingPriority(queryTerms, craftGuide)
        );
        assertEquals(
            PackRepository.broadWaterRouteOrderingPriorityForTest(query, planning),
            PackRouteRefinementPolicy.broadWaterRouteOrderingPriority(queryTerms, planning)
        );
    }

    private static SearchResult result(
        String title,
        String sectionHeading,
        String guideId,
        String category,
        String contentRole,
        String timeHorizon,
        String structureType,
        String topicTags,
        String snippet
    ) {
        return result(
            title,
            sectionHeading,
            guideId,
            category,
            contentRole,
            timeHorizon,
            structureType,
            topicTags,
            snippet,
            "route-focus"
        );
    }

    private static SearchResult result(
        String title,
        String sectionHeading,
        String guideId,
        String category,
        String contentRole,
        String timeHorizon,
        String structureType,
        String topicTags,
        String snippet,
        String retrievalMode
    ) {
        return new SearchResult(
            title,
            "",
            snippet,
            snippet,
            guideId,
            sectionHeading,
            category,
            retrievalMode,
            contentRole,
            timeHorizon,
            structureType,
            topicTags
        );
    }
}
