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
    public void defaultRouteSearchStepPreservesExecutorTokenCap() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery("how do i build a house");
        QueryRouteProfile.RouteSearchSpec routeSpec = new QueryRouteProfile.RouteSearchSpec(
            "alpha beta gamma delta epsilon zeta eta theta",
            Collections.emptySet(),
            0
        );

        PackRouteFocusedSearchHelper.RouteSearchStep routeStep =
            PackRouteFocusedSearchHelper.routeSearchStep(queryTerms, routeSpec);

        assertEquals(List.of("alpha", "beta", "gamma", "delta", "epsilon", "zeta"), routeStep.tokens);
    }

    @Test
    public void fullRouteCursorScanEnablesSpecializedDirectTopicRoutes() {
        assertFullRouteCursorScan("how do i make soap from animal fat and ash", true);
        assertFullRouteCursorScan("how do i make glass from silica sand and soda ash", true);
        assertFullRouteCursorScan("how do i design a gravity-fed water distribution system", true);
    }

    @Test
    public void fullRouteCursorScanSkipsGovernanceAndSecurityRoutes() {
        assertFullRouteCursorScan(
            "how do i protect a vulnerable work site, field, or water point without spreading people too thin?",
            false
        );
        assertFullRouteCursorScan("someone is stealing food from the group what do we do", false);
    }

    @Test
    public void fullRouteCursorScanSkipsBroadHouseRoute() {
        assertFullRouteCursorScan("how do i build a house", false);
    }

    @Test
    public void fullRouteCursorScanHandlesMissingTerms() {
        assertFalse(PackRouteFocusedSearchHelper.shouldScanFullRouteCursor(null));
    }

    @Test
    public void routeChunkCandidatePlanningPreservesBroadHouseBudget() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery("how do i build a house");

        assertEquals(600, PackRouteFocusedSearchHelper.routeChunkCandidateLimit(queryTerms, 12));
        assertEquals(42, PackRouteFocusedSearchHelper.routeChunkCandidateTarget(queryTerms, 12));
    }

    @Test
    public void routeChunkCandidatePlanningPreservesCompactSiteBudget() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(
            "how do i choose a safe site and foundation for a small cabin"
        );

        assertEquals(128, PackRouteFocusedSearchHelper.routeChunkCandidateLimit(queryTerms, 12));
        assertEquals(16, PackRouteFocusedSearchHelper.routeChunkCandidateTarget(queryTerms, 12));
    }

    @Test
    public void routeBackfillPlanningPreservesSpecializedStarvationBehavior() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(
            "how do i make soap from animal fat and ash"
        );

        assertTrue(PackRouteFocusedSearchHelper.shouldBackfillLikeAfterFts(queryTerms, 2, 2, 18));
        assertFalse(PackRouteFocusedSearchHelper.shouldBackfillLikeAfterFts(queryTerms, 3, 6, 18));
    }

    @Test
    public void noBm25RouteFtsOrderPreservesSpecializedLabels() {
        assertNoBm25OrderLabel("how do i make soap from animal fat and ash", "soapmaking_priority");
        assertNoBm25OrderLabel(
            "how do i design a gravity-fed water distribution system",
            "water_distribution_priority"
        );
        assertNoBm25OrderLabel("how do i build a clay oven", "clay_oven_priority");
        assertNoBm25OrderLabel("how do i build a house", "rowid");
    }

    private static void assertFullRouteCursorScan(String query, boolean expected) {
        assertEquals(
            query,
            expected,
            PackRouteFocusedSearchHelper.shouldScanFullRouteCursor(PackRepository.QueryTerms.fromQuery(query))
        );
    }

    private static void assertNoBm25OrderLabel(String query, String expected) {
        assertEquals(
            query,
            expected,
            PackRouteFocusedSearchHelper.noBm25RouteFtsOrder(PackRepository.QueryTerms.fromQuery(query)).label
        );
    }
}
