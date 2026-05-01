package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PackRouteAnchorBiasPolicyTest {
    @Test
    public void cabinSiteSelectionBiasMatchesRepositoryCompatibilityWrapper() {
        String query = "how do i choose a safe site and foundation for a small cabin";
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(query);
        SearchResult siteSelection = result(
            "Shelter Site Selection & Hazard Assessment",
            "Terrain Analysis",
            "GD-446",
            "survival",
            "safety",
            "cabin_house",
            "site_selection,drainage"
        );
        SearchResult foundationDetail = result(
            "Construction & Carpentry",
            "Foundation Planning",
            "GD-094",
            "building",
            "starter",
            "cabin_house",
            "site_selection,foundation,wall_construction"
        );

        assertTrue(PackRouteAnchorBiasPolicy.prefersCabinSiteSelectionRouteAnchor(queryTerms));
        assertTrue(PackRouteAnchorBiasPolicy.hasCabinSiteSelectionAnchorSignal(siteSelection));
        assertTrue(
            PackRouteAnchorBiasPolicy.cabinSiteSelectionAnchorBias(queryTerms, siteSelection)
                > PackRouteAnchorBiasPolicy.cabinSiteSelectionAnchorBias(queryTerms, foundationDetail)
        );
        assertEquals(
            PackRepository.cabinSiteSelectionAnchorBias(queryTerms, siteSelection),
            PackRouteAnchorBiasPolicy.cabinSiteSelectionAnchorBias(queryTerms, siteSelection)
        );
    }

    @Test
    public void roofWeatherproofBiasMatchesRepositoryCompatibilityWrapper() {
        String query = "how do i weatherproof a cabin roof";
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(query);
        SearchResult roofWeatherproofing = result(
            "Insulation & Weatherproofing",
            "Roof Insulation Techniques",
            "GD-106",
            "building",
            "subsystem",
            "cabin_house",
            "roofing,weatherproofing"
        );
        SearchResult structuralOverview = result(
            "Construction & Carpentry",
            "Structural Engineering Basics",
            "GD-094",
            "building",
            "starter",
            "cabin_house",
            "foundation,wall_construction,roofing"
        );

        assertTrue(PackRouteAnchorBiasPolicy.prefersRoofWeatherproofRouteAnchor(queryTerms));
        assertTrue(
            PackRouteAnchorBiasPolicy.roofWeatherproofAnchorBias(queryTerms, roofWeatherproofing)
                > PackRouteAnchorBiasPolicy.roofWeatherproofAnchorBias(queryTerms, structuralOverview)
        );
        assertEquals(
            PackRepository.roofWeatherproofAnchorBias(queryTerms, roofWeatherproofing),
            PackRouteAnchorBiasPolicy.roofWeatherproofAnchorBias(queryTerms, roofWeatherproofing)
        );
    }

    @Test
    public void currentHeadBiasMatchesRefinementCompatibilityPath() {
        String query = "how do i build a house";
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(query);
        SearchResult siteSelection = result(
            "Shelter Site Selection & Hazard Assessment",
            "Terrain Analysis",
            "GD-446",
            "survival",
            "safety",
            "emergency_shelter",
            "site_selection,drainage"
        );
        SearchResult constructionSequence = result(
            "Construction & Carpentry",
            "Foundations",
            "GD-094",
            "building",
            "starter",
            "cabin_house",
            "drainage,foundation,wall_construction,roofing,weatherproofing"
        );

        assertTrue(
            PackRouteAnchorBiasPolicy.currentHeadRouteOrderingPriority(queryTerms, constructionSequence)
                > PackRouteAnchorBiasPolicy.currentHeadRouteOrderingPriority(queryTerms, siteSelection)
        );
        assertEquals(
            PackRouteRefinementPolicy.currentHeadRouteOrderingPriority(queryTerms, constructionSequence),
            PackRouteAnchorBiasPolicy.currentHeadRouteOrderingPriority(queryTerms, constructionSequence)
        );
    }

    private static SearchResult result(
        String title,
        String sectionHeading,
        String guideId,
        String category,
        String contentRole,
        String structureType,
        String topicTags
    ) {
        String text = title + " " + sectionHeading + " " + topicTags;
        return new SearchResult(
            title,
            "",
            text,
            text,
            guideId,
            sectionHeading,
            category,
            "route-focus",
            contentRole,
            "long_term",
            structureType,
            topicTags
        );
    }
}
