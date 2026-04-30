package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

public final class PackRouteFocusedSearchHelperTest {
    @Test
    public void routeSearchStepsPreserveSpecOrderAndTerms() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery("how do i build a house");
        List<QueryRouteProfile.RouteSearchSpec> routeSpecs =
            queryTerms.routeProfile.routeSearchSpecs(queryTerms.queryLower);
        List<PackRouteFocusedSearchHelper.RouteSearchStep> routeSteps =
            PackRouteFocusedSearchHelper.routeSearchSteps(queryTerms, 6);

        assertEquals(routeSpecs.size(), routeSteps.size());
        for (int index = 0; index < routeSpecs.size(); index++) {
            QueryRouteProfile.RouteSearchSpec routeSpec = routeSpecs.get(index);
            PackRouteFocusedSearchHelper.RouteSearchStep routeStep = routeSteps.get(index);

            assertEquals(routeSpec.text(), routeStep.routeSpec.text());
            assertEquals(
                PackRepository.QueryTerms.fromText(routeSpec.text(), queryTerms.routeProfile).primaryKeywordTokens(),
                routeStep.specTerms.primaryKeywordTokens()
            );
            assertTrue(routeStep.tokens.size() <= 6);
        }
    }

    @Test
    public void routeSearchStepKeepsCategoryFallbackAndTokenCap() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery("how do i build a house");
        QueryRouteProfile.RouteSearchSpec routeSpec = new QueryRouteProfile.RouteSearchSpec(
            "alpha beta gamma delta epsilon zeta eta",
            Collections.emptySet(),
            0
        );

        PackRouteFocusedSearchHelper.RouteSearchStep routeStep =
            PackRouteFocusedSearchHelper.routeSearchStep(queryTerms, routeSpec, 3);

        assertEquals(List.of("alpha", "beta", "gamma"), routeStep.tokens);
        assertEquals(new java.util.ArrayList<>(queryTerms.routeProfile.preferredCategories()), routeStep.categories);
    }

    @Test
    public void fullRouteCursorScanRequiresSpecializedExplicitTopicFocus() {
        assertTrue(PackRouteFocusedSearchHelper.shouldScanFullRouteCursor(
            PackRepository.QueryTerms.fromQuery("how do i make soap from animal fat and ash")
        ));
        assertFalse(PackRouteFocusedSearchHelper.shouldScanFullRouteCursor(
            PackRepository.QueryTerms.fromQuery("how do i build a house")
        ));
    }

    @Test
    public void fullRouteCursorScanSkipsGovernanceAndSecurityRoutes() {
        assertFalse(PackRouteFocusedSearchHelper.shouldScanFullRouteCursor(
            PackRepository.QueryTerms.fromQuery(
                "how do i protect a vulnerable work site, field, or water point without spreading people too thin?"
            )
        ));
        assertFalse(PackRouteFocusedSearchHelper.shouldScanFullRouteCursor(
            PackRepository.QueryTerms.fromQuery("someone is stealing food from the group what do we do")
        ));
    }

    @Test
    public void fullRouteCursorScanHandlesMissingTerms() {
        assertFalse(PackRouteFocusedSearchHelper.shouldScanFullRouteCursor(null));
    }
}
