package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PackRepositoryCurrentHeadRouteFocusedTest {
    @Test
    public void glassFromSilicaAndSodaAshPrefersGlassFromScratchGuideOverRawMaterialsBoundary() {
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
            "1. Glassmaking from Scratch",
            "GD-123",
            "crafts",
            "subsystem",
            "mixed",
            "glassmaking",
            "glassmaking,annealing",
            "Start with soda-lime glass, furnace heat, forming, and annealing."
        );

        assertTrue(
            PackRepository.currentHeadRouteRefinementBonusForTest(query, fromScratch)
                > PackRepository.currentHeadRouteRefinementBonusForTest(query, rawMaterials)
        );
        assertEquals(
            "GD-123",
            PackRepository.selectSpecializedStructuredAnchorForTest(query, rawMaterials, fromScratch).guideId
        );
    }

    @Test
    public void roofWeatherproofingPrefersCabinWeatherproofingOverGeneralConstruction() {
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

        assertTrue(
            PackRepository.currentHeadRouteRefinementBonusForTest(query, cabinWeatherproofing)
                > PackRepository.currentHeadRouteRefinementBonusForTest(query, construction)
        );
        assertEquals(
            "GD-106",
            PackRepository.selectSpecializedStructuredAnchorForTest(query, construction, cabinWeatherproofing).guideId
        );
    }

    @Test
    public void broadHousePromptPrefersHouseConstructionOverEmergencyShelterSiteSelection() {
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

        assertTrue(
            PackRepository.currentHeadRouteOrderingPriorityForTest(query, constructionSequence)
                > PackRepository.currentHeadRouteOrderingPriorityForTest(query, siteSelection)
        );
        assertEquals(
            "GD-094",
            PackRepository.selectBroadHouseAnchorForTest(query, siteSelection, constructionSequence).guideId
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
