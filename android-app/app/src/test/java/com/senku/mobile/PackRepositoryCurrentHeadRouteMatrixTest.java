package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PackRepositoryCurrentHeadRouteMatrixTest {
    @Test
    public void currentHeadRouteFocusedMatrixPreservesHighSignalOwnersAndLanes() {
        assertRouteFocusedOutput(
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
        assertRouteFocusedOutput(
            "how do i build a house with windows and a roof",
            "cabin_house",
            "roofing_priority",
            "GD-106",
            PackRepository.selectSpecializedStructuredAnchorForTest(
                "how do i build a house with windows and a roof",
                constructionAndCarpentrySequence(),
                cabinRoofWeatherproofing()
            )
        );
        assertRouteFocusedOutput(
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
        assertRouteFocusedOutput(
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
        assertRouteFocusedOutput(
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
        assertRouteFocusedOutput(
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
        assertRouteFocusedOutput(
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
    }

    private static void assertRouteFocusedOutput(
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
        assertEquals(query, expectedStructure, selected.structureType);
    }

    private static SearchResult constructionAndCarpentrySequence() {
        return result(
            "Construction & Carpentry",
            "Build sequence for a house: drainage, foundations, walls, windows, roofing, and weatherproofing.",
            "GD-094",
            "Foundations",
            "building",
            "starter",
            "long_term",
            "cabin_house",
            "drainage,foundation,wall_construction,roofing,window_assembly,weatherproofing"
        );
    }

    private static SearchResult emergencyShelterSiteSelection() {
        return result(
            "Shelter Site Selection & Hazard Assessment",
            "Terrain analysis, natural hazards, wind exposure, and water proximity.",
            "GD-446",
            "Terrain Analysis",
            "survival",
            "safety",
            "immediate",
            "emergency_shelter",
            "site_selection,drainage"
        );
    }

    private static SearchResult commonsFoodTheftGovernance() {
        return result(
            "Commons Management & Sustainable Resource Governance",
            "Verify facts, document losses, require restitution, and use graduated sanctions.",
            "GD-626",
            "Monitoring & Graduated Sanctions",
            "resource-management",
            "subsystem",
            "long_term",
            "community_governance",
            "community_governance,conflict_resolution,trust_systems"
        );
    }

    private static SearchResult mutualAidFinanceGovernance() {
        return result(
            "Insurance, Risk Pooling & Mutual Aid Funds",
            "Shared fund administration and accounting for pooled aid reserves.",
            "GD-657",
            "Fund Governance and Accounting",
            "resource-management",
            "reference",
            "long_term",
            "community_governance",
            "community_governance,trust_systems"
        );
    }

    private static SearchResult waterDistributionSystemDesign() {
        return result(
            "Community Water Distribution Systems",
            "Design gravity-fed distribution networks with storage tank planning and household taps.",
            "GD-270",
            "Gravity-Fed Distribution Systems",
            "building",
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
            "safety",
            "long_term",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );
    }

    private static SearchResult soapmakingProcess() {
        return result(
            "Homestead Chemistry",
            "Render fat, prepare lye water, mix to trace, and cure soap bars safely.",
            "GD-122",
            "Soap Making - Cold Process",
            "crafts",
            "subsystem",
            "mixed",
            "soapmaking",
            "soapmaking,lye_safety"
        );
    }

    private static SearchResult genericChemistrySafety() {
        return result(
            "Chemical Safety",
            "Generic caustic handling notes.",
            "GD-227",
            "Caustic Hazard Basics",
            "chemistry",
            "reference",
            "mixed",
            "general",
            ""
        );
    }

    private static SearchResult glassmakingFromScratch() {
        return result(
            "Glass, Optics & Ceramics",
            "Start with silica sand, soda ash, limestone, furnace heat, forming, and annealing.",
            "GD-123",
            "Glassmaking from Scratch",
            "crafts",
            "subsystem",
            "long_term",
            "glassmaking",
            "glassmaking,annealing"
        );
    }

    private static SearchResult glassRawMaterialsBoundary() {
        return result(
            "Glass Making: Raw Materials & Furnace Construction",
            "Raw-material hazard inventory and furnace-area access controls.",
            "GD-711",
            "Raw Material Hazard Inventory",
            "chemistry",
            "safety",
            "long_term",
            "glassmaking",
            "glassmaking"
        );
    }

    private static SearchResult cabinRoofWeatherproofing() {
        return result(
            "Insulation & Weatherproofing",
            "Weatherproofing, moisture barriers, roof insulation, air sealing, and climate control.",
            "GD-106",
            "Roof Insulation Techniques",
            "building",
            "subsystem",
            "long_term",
            "cabin_house",
            "roofing,weatherproofing"
        );
    }

    private static SearchResult genericConstructionRoofing() {
        return result(
            "Construction & Carpentry",
            "General framing and roofing systems for off-grid construction.",
            "GD-094",
            "Roofing Systems",
            "building",
            "starter",
            "long_term",
            "cabin_house",
            "foundation,wall_construction,roofing"
        );
    }

    private static SearchResult result(
        String title,
        String snippet,
        String guideId,
        String sectionHeading,
        String category,
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
            "route-focus",
            contentRole,
            timeHorizon,
            structureType,
            topicTags
        );
    }
}
