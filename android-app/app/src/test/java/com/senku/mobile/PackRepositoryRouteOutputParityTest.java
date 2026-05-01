package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public final class PackRepositoryRouteOutputParityTest {
    @Test
    public void routeOutputMatrixCharacterizesCurrentTopGuideIdsAndRoutes() {
        assertRouteOutput(
            "how do i design a gravity-fed water distribution system",
            "water_distribution",
            "water_distribution_priority",
            "GD-270",
            PackRepository.selectExplicitWaterDistributionAnchorForTest(
                "how do i design a gravity-fed water distribution system",
                waterStorageDistributionOverview(),
                waterDistributionSystemDesign()
            )
        );
        assertRouteOutput(
            "what is the safest way to store treated water long term",
            "water_storage",
            "rowid",
            "GD-252",
            PackRepository.selectExplicitWaterStorageAnchorForTest(
                "what is the safest way to store treated water long term",
                genericStorageInventory(),
                treatedWaterStoragePlanning()
            )
        );
        assertRouteOutput(
            "how do i build a house",
            "cabin_house",
            "rowid",
            "GD-383",
            PackRepository.routeFocusedAnchorForTest(
                "how do i build a house",
                List.of(genericStructuralOverview(), foundationRouteOutput()),
                false
            )
        );
        assertRouteOutput(
            "how do i make soap from animal fat and ash",
            "soapmaking",
            "soapmaking_priority",
            "GD-122",
            PackRepository.selectSpecializedStructuredAnchorForTest(
                "how do i make soap from animal fat and ash",
                genericChemistrySafety(),
                soapmakingProcess()
            )
        );
        assertRouteOutput(
            "how do i make glass",
            "glassmaking",
            "rowid",
            "GD-123",
            PackRepository.selectSpecializedStructuredAnchorForTest(
                "how do i make glass",
                glassRawMaterialsBoundary(),
                glassmakingCoreGuide()
            )
        );
        assertRouteOutput(
            "how do i waterproof a roof with no tar or shingles",
            "cabin_house",
            "roofing_priority",
            "GD-515",
            PackRepository.selectAnswerAnchorForTest(
                "how do i waterproof a roof with no tar or shingles",
                genericStructuralOverview(),
                roofWeatherproofingRouteOutput()
            )
        );
        assertRouteOutput(
            "how do i weatherproof cabin windows",
            "cabin_house",
            "roofing_priority",
            "GD-106",
            PackRepository.selectSpecializedStructuredAnchorForTest(
                "how do i weatherproof cabin windows",
                genericConstructionRoofing(),
                cabinWindowWeatherproofing()
            )
        );
        assertRouteOutput(
            "someone is stealing food from the group what do we do",
            "community_governance",
            "community_governance_priority",
            "GD-626",
            PackRepository.selectSpecializedStructuredAnchorForTest(
                "someone is stealing food from the group what do we do",
                mutualAidFinanceGovernance(),
                commonsFoodTheftGovernance()
            )
        );
    }

    @Test
    public void externallyRecommendedQueriesCharacterizeFinalStructuredAnchorPath() {
        assertFinalAnchorPath(
            "how do i design a gravity-fed water distribution system",
            "water_distribution",
            "water_distribution_priority",
            "GD-270",
            PackRepository.selectExplicitWaterDistributionAnchorForTest(
                "how do i design a gravity-fed water distribution system",
                waterStorageDistributionOverview(),
                waterDistributionSystemDesign()
            )
        );
        assertFinalAnchorPath(
            "how do i make soap from animal fat and ash",
            "soapmaking",
            "soapmaking_priority",
            "GD-122",
            PackRepository.selectSpecializedStructuredAnchorForTest(
                "how do i make soap from animal fat and ash",
                genericChemistrySafety(),
                soapmakingProcess()
            )
        );
        assertFinalAnchorPath(
            "how do i make glass from silica sand and soda ash",
            "glassmaking",
            "rowid",
            "GD-123",
            PackRepository.selectSpecializedStructuredAnchorForTest(
                "how do i make glass from silica sand and soda ash",
                glassRawMaterialsBoundary(),
                glassmakingFromScratch()
            )
        );
        assertFinalAnchorPath(
            "how do i weatherproof a cabin roof",
            "cabin_house",
            "roofing_priority",
            "GD-106",
            PackRepository.selectSpecializedStructuredAnchorForTest(
                "how do i weatherproof a cabin roof",
                genericConstructionRoofing(),
                cabinRoofWeatherproofing()
            )
        );
        assertFinalAnchorPath(
            "how do i weatherproof cabin windows",
            "cabin_house",
            "roofing_priority",
            "GD-106",
            PackRepository.selectSpecializedStructuredAnchorForTest(
                "how do i weatherproof cabin windows",
                genericConstructionRoofing(),
                cabinWindowWeatherproofing()
            )
        );
        assertFinalAnchorPath(
            "someone is stealing food from the group what do we do",
            "community_governance",
            "community_governance_priority",
            "GD-626",
            PackRepository.selectSpecializedStructuredAnchorForTest(
                "someone is stealing food from the group what do we do",
                mutualAidFinanceGovernance(),
                commonsFoodTheftGovernance()
            )
        );
        assertFinalAnchorPath(
            "stolen food from the shared group stores what should we do",
            "community_governance",
            "community_governance_priority",
            "GD-626",
            PackRepository.selectSpecializedStructuredAnchorForTest(
                "stolen food from the shared group stores what should we do",
                mutualAidFinanceGovernance(),
                commonsFoodTheftGovernance()
            )
        );
        assertFinalAnchorPath(
            "how do i build a house",
            "cabin_house",
            "rowid",
            "GD-094",
            PackRepository.selectBroadHouseAnchorForTest(
                "how do i build a house",
                emergencyShelterSiteSelection(),
                constructionAndCarpentrySequence()
            )
        );
    }

    @Test
    public void centroidMissingFallbackPreservesMultipleRouteOutputsBeforeLexicalFallback() {
        List<SearchResult> merged = PackRepository.mergeResultsWhenCentroidMissingForTest(
            List.of(waterDistributionSystemDesign(), treatedWaterStoragePlanning()),
            List.of(genericStorageInventory(), genericChemistrySafety()),
            4
        );

        assertEquals(4, merged.size());
        assertEquals("GD-270", merged.get(0).guideId);
        assertEquals("route-focus", merged.get(0).retrievalMode);
        assertEquals("GD-252", merged.get(1).guideId);
        assertEquals("route-focus", merged.get(1).retrievalMode);
        assertEquals("GD-373", merged.get(2).guideId);
        assertEquals("lexical", merged.get(2).retrievalMode);
        assertEquals("GD-227", merged.get(3).guideId);
        assertEquals("lexical", merged.get(3).retrievalMode);
    }

    @Test
    public void routeFocusedAnchorHelperPreservesRepositoryOutputForDirectSignalFiltering() {
        String query = "how do i make soap from animal fat and ash";
        List<SearchResult> rankedResults = List.of(genericChemistrySafety(), soapmakingProcess());

        assertEquals(
            PackRepository.routeFocusedAnchorForTest(query, rankedResults, true).guideId,
            PackRouteFocusedAnchorPolicy.selectRouteFocusedAnchor(
                PackRepository.QueryTerms.fromQuery(query),
                rankedResults,
                true
            ).guideId
        );
    }

    @Test
    public void routeFocusedAnchorHelperPreservesRepositoryOutputForBroadRouteSelection() {
        String query = "how do i build a house";
        List<SearchResult> rankedResults = List.of(genericStructuralOverview(), foundationRouteOutput());

        assertEquals(
            PackRepository.routeFocusedAnchorForTest(query, rankedResults, false).guideId,
            PackRouteFocusedAnchorPolicy.selectRouteFocusedAnchor(
                PackRepository.QueryTerms.fromQuery(query),
                rankedResults,
                false
            ).guideId
        );
    }

    private static void assertRouteOutput(
        String query,
        String expectedStructure,
        String expectedOrderLabel,
        String expectedGuideId,
        SearchResult selected
    ) {
        assertEquals(query, expectedStructure, QueryMetadataProfile.fromQuery(query).preferredStructureType());
        assertTrue(query, QueryRouteProfile.fromQuery(query).isRouteFocused());
        assertEquals(query, expectedOrderLabel, PackRepository.noBm25RouteFtsOrderLabelForTest(query));
        assertEquals(query, expectedGuideId, selected.guideId);
        assertEquals(query, "route-focus", selected.retrievalMode);
    }

    private static void assertFinalAnchorPath(
        String query,
        String expectedStructure,
        String expectedOrderLabel,
        String expectedGuideId,
        SearchResult selected
    ) {
        assertRouteOutput(query, expectedStructure, expectedOrderLabel, expectedGuideId, selected);
    }

    private static SearchResult waterDistributionSystemDesign() {
        return result(
            "Community Water Distribution Systems",
            "Design gravity-fed distribution networks with storage tank planning and household taps.",
            "GD-270",
            "Gravity-Fed Distribution Systems",
            "building",
            "route-focus",
            "planning",
            "long_term",
            "water_distribution",
            "water_distribution,water_storage"
        );
    }

    private static SearchResult waterStorageDistributionOverview() {
        return result(
            "Storage & Material Management",
            "Water storage basics and food-safe containers.",
            "GD-252",
            "Water Storage: Hydration Assurance",
            "resource-management",
            "route-focus",
            "safety",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );
    }

    private static SearchResult treatedWaterStoragePlanning() {
        return result(
            "Storage & Material Management",
            "Use food-safe containers, sanitize them, seal them, and rotate treated water on a schedule.",
            "GD-252",
            "Water Storage & Rotation",
            "resource-management",
            "route-focus",
            "planning",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );
    }

    private static SearchResult genericStorageInventory() {
        return result(
            "Home Inventory",
            "Keep a broad household inventory with basic emergency supplies.",
            "GD-373",
            "Water Storage & Purification",
            "resource-management",
            "lexical",
            "starter",
            "long_term",
            "water_storage",
            "water_storage"
        );
    }

    private static SearchResult genericStructuralOverview() {
        return result(
            "Construction & Carpentry",
            "Structural overview for off-grid builders with general engineering basics.",
            "GD-094",
            "Structural Engineering Basics for Off-Grid Builders",
            "building",
            "route-focus",
            "starter",
            "long_term",
            "cabin_house",
            "site_selection,foundation,wall_construction,roofing,weatherproofing"
        );
    }

    private static SearchResult foundationRouteOutput() {
        return result(
            "Foundations and Footings",
            "Footing choices, frost line depth, and drainage for long-term houses.",
            "GD-383",
            "Frost Line and Frost Heave",
            "building",
            "route-focus",
            "starter",
            "long_term",
            "general",
            "drainage,foundation"
        );
    }

    private static SearchResult genericChemistrySafety() {
        return result(
            "Chemical Safety",
            "Generic caustic handling notes.",
            "GD-227",
            "Caustic Hazard Basics",
            "chemistry",
            "lexical",
            "reference",
            "mixed",
            "general",
            ""
        );
    }

    private static SearchResult soapmakingProcess() {
        return result(
            "Homestead Chemistry",
            "Render fat, prepare lye water, mix to trace, and cure soap bars safely.",
            "GD-122",
            "Soap Making - Cold Process",
            "crafts",
            "route-focus",
            "subsystem",
            "mixed",
            "soapmaking",
            "soapmaking,lye_safety"
        );
    }

    private static SearchResult glassRawMaterialsBoundary() {
        return result(
            "Glass Making: Raw Materials & Furnace Construction",
            "Raw-material hazard inventory and furnace-area access controls.",
            "GD-711",
            "Raw Material Hazard Inventory",
            "chemistry",
            "route-focus",
            "safety",
            "long_term",
            "glassmaking",
            "glassmaking"
        );
    }

    private static SearchResult glassmakingCoreGuide() {
        return result(
            "Glass, Optics & Ceramics",
            "Start with soda-lime glass, raw materials, furnace heat, forming, and annealing.",
            "GD-123",
            "Glassmaking and Annealing",
            "crafts",
            "route-focus",
            "subsystem",
            "long_term",
            "glassmaking",
            "glassmaking,annealing"
        );
    }

    private static SearchResult glassmakingFromScratch() {
        return result(
            "Glass, Optics & Ceramics",
            "Start with silica sand, soda ash, limestone, furnace heat, forming, and annealing.",
            "GD-123",
            "Glassmaking from Scratch",
            "crafts",
            "route-focus",
            "subsystem",
            "long_term",
            "glassmaking",
            "glassmaking,annealing"
        );
    }

    private static SearchResult genericConstructionRoofing() {
        return result(
            "Construction & Carpentry",
            "General framing and roofing systems for off-grid construction.",
            "GD-094",
            "Roofing Systems",
            "building",
            "route-focus",
            "starter",
            "long_term",
            "cabin_house",
            "foundation,wall_construction,roofing"
        );
    }

    private static SearchResult cabinRoofWeatherproofing() {
        return result(
            "Insulation & Weatherproofing",
            "Weatherproofing, moisture barriers, roof insulation, air sealing, and climate control.",
            "GD-106",
            "Roof Insulation Techniques",
            "building",
            "route-focus",
            "subsystem",
            "long_term",
            "cabin_house",
            "roofing,weatherproofing"
        );
    }

    private static SearchResult cabinWindowWeatherproofing() {
        return result(
            "Insulation & Weatherproofing",
            "Weatherstripping, sill sealing, air sealing, and moisture control for windows and doors.",
            "GD-106",
            "Window and Door Weatherstripping",
            "building",
            "route-focus",
            "subsystem",
            "long_term",
            "cabin_house",
            "weatherproofing,ventilation"
        );
    }

    private static SearchResult roofWeatherproofingRouteOutput() {
        return result(
            "Roofing & Weatherproofing",
            "Waterproofing and sealants for roof systems without industrial materials.",
            "GD-515",
            "Waterproofing and Sealants",
            "building",
            "route-focus",
            "subsystem",
            "long_term",
            "cabin_house",
            "roofing,weatherproofing"
        );
    }

    private static SearchResult mutualAidFinanceGovernance() {
        return result(
            "Insurance, Risk Pooling & Mutual Aid Funds",
            "Shared fund administration and accounting for pooled aid reserves.",
            "GD-657",
            "Fund Governance and Accounting",
            "resource-management",
            "route-focus",
            "reference",
            "long_term",
            "community_governance",
            "community_governance,trust_systems"
        );
    }

    private static SearchResult commonsFoodTheftGovernance() {
        return result(
            "Commons Management & Sustainable Resource Governance",
            "Verify facts, document losses, require restitution, and use graduated sanctions.",
            "GD-626",
            "Monitoring & Graduated Sanctions",
            "resource-management",
            "route-focus",
            "subsystem",
            "long_term",
            "community_governance",
            "community_governance,conflict_resolution,trust_systems"
        );
    }

    private static SearchResult emergencyShelterSiteSelection() {
        return result(
            "Shelter Site Selection & Hazard Assessment",
            "Terrain analysis, natural hazards, wind exposure, and water proximity.",
            "GD-446",
            "Terrain Analysis",
            "survival",
            "route-focus",
            "safety",
            "immediate",
            "emergency_shelter",
            "site_selection,drainage"
        );
    }

    private static SearchResult constructionAndCarpentrySequence() {
        return result(
            "Construction & Carpentry",
            "Build sequence for a house: drainage, foundations, walls, roofing, and weatherproofing.",
            "GD-094",
            "Foundations",
            "building",
            "route-focus",
            "starter",
            "long_term",
            "cabin_house",
            "drainage,foundation,wall_construction,roofing,weatherproofing"
        );
    }

    private static SearchResult result(
        String title,
        String snippet,
        String guideId,
        String sectionHeading,
        String category,
        String retrievalMode,
        String contentRole,
        String timeHorizon,
        String structureType,
        String topicTags
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
