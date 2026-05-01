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
    public void currentHeadBroadHouseOrderingPriorityPrefersGeneralSequenceOverFoundationOnlyDetail() {
        String query = "how do i build a cabin";
        SearchResult foundationOnly = result(
            "Foundations and Footings",
            "Frost Line and Frost Heave",
            "GD-383",
            "building",
            "starter",
            "long_term",
            "general",
            "drainage,foundation",
            "Footing choices, frost line depth, and drainage for long-term houses."
        );
        SearchResult constructionSequence = result(
            "Construction & Carpentry",
            "Wall Construction and Roofing Systems",
            "GD-094",
            "building",
            "starter",
            "long_term",
            "cabin_house",
            "drainage,foundation,wall_construction,roofing,weatherproofing",
            "Build sequence for a cabin: drainage, foundations, walls, roofing, and weatherproofing."
        );

        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(query);

        assertTrue(
            PackRouteRefinementPolicy.currentHeadRouteOrderingPriority(queryTerms, constructionSequence)
                > PackRouteRefinementPolicy.currentHeadRouteOrderingPriority(queryTerms, foundationOnly)
        );
    }

    @Test
    public void currentHeadWindowWeatherproofingBonusKeepsHouseOpeningWeatherproofingAheadOfConstruction() {
        String query = "how do i weatherproof cabin windows";
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
        SearchResult openingWeatherproofing = result(
            "Insulation & Weatherproofing",
            "Window and Door Weatherstripping",
            "GD-106",
            "building",
            "subsystem",
            "long_term",
            "cabin_house",
            "weatherproofing,ventilation",
            "Weatherstripping, sill sealing, air sealing, and moisture control for windows and doors."
        );

        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(query);

        assertTrue(
            PackRouteRefinementPolicy.currentHeadRouteRefinementBonus(queryTerms, openingWeatherproofing)
                > PackRouteRefinementPolicy.currentHeadRouteRefinementBonus(queryTerms, construction)
        );
    }

    @Test
    public void currentHeadRouteMethodsReturnZeroForMissingInputs() {
        SearchResult result = result(
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
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(
            "how do i make glass from silica sand and soda ash"
        );
        PackRepository.QueryTerms nullMetadataQueryTerms = new PackRepository.QueryTerms(
            "",
            null,
            null,
            null,
            null,
            null,
            null
        );

        assertEquals(0, PackRouteRefinementPolicy.currentHeadRouteRefinementBonus(null, result));
        assertEquals(0, PackRouteRefinementPolicy.currentHeadRouteRefinementBonus(queryTerms, null));
        assertEquals(0, PackRouteRefinementPolicy.currentHeadRouteRefinementBonus(nullMetadataQueryTerms, result));
        assertEquals(0, PackRouteRefinementPolicy.currentHeadRouteOrderingPriority(null, result));
        assertEquals(0, PackRouteRefinementPolicy.currentHeadRouteOrderingPriority(queryTerms, null));
        assertEquals(0, PackRouteRefinementPolicy.currentHeadRouteOrderingPriority(nullMetadataQueryTerms, result));
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
    public void broadWaterRouteMethodsReturnZeroForMissingInputs() {
        SearchResult result = result(
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
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(
            "what's the safest way to store treated water long term"
        );
        PackRepository.QueryTerms nullMetadataQueryTerms = new PackRepository.QueryTerms(
            "",
            null,
            null,
            null,
            null,
            null,
            null
        );

        assertEquals(0, PackRouteRefinementPolicy.broadWaterRouteRefinementBonus(null, result));
        assertEquals(0, PackRouteRefinementPolicy.broadWaterRouteRefinementBonus(queryTerms, null));
        assertEquals(0, PackRouteRefinementPolicy.broadWaterRouteRefinementBonus(nullMetadataQueryTerms, result));
        assertEquals(0, PackRouteRefinementPolicy.broadWaterRouteOrderingPriority(null, result));
        assertEquals(0, PackRouteRefinementPolicy.broadWaterRouteOrderingPriority(queryTerms, null));
        assertEquals(0, PackRouteRefinementPolicy.broadWaterRouteOrderingPriority(nullMetadataQueryTerms, result));
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
