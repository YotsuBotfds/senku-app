package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.junit.Test;

public final class PackRetrievalOrchestrationPolicyTest {
    @Test
    public void routeResultLimitIsZeroForNonRouteQueries() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery("how do i preserve tomatoes");

        assertEquals(0, PackRetrievalOrchestrationPolicy.routeResultLimit(routeProfile, 16));
    }

    @Test
    public void starterBuildRouteUsesSmallerShareOfRequestedLimit() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery("how do i build a house");

        assertEquals(12, PackRetrievalOrchestrationPolicy.routeResultLimit(routeProfile, 16));
        assertEquals(24, PackRetrievalOrchestrationPolicy.routeResultLimit(routeProfile, 96));
    }

    @Test
    public void focusedNonStarterRouteUsesHalfShareWithinBounds() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery("how do i build a clay oven");

        assertEquals(12, PackRetrievalOrchestrationPolicy.routeResultLimit(routeProfile, 16));
        assertEquals(20, PackRetrievalOrchestrationPolicy.routeResultLimit(routeProfile, 40));
        assertEquals(24, PackRetrievalOrchestrationPolicy.routeResultLimit(routeProfile, 80));
    }

    @Test
    public void preferredMergeKeepsPrimaryResultsBeforeSecondaryResults() {
        SearchResult route = result("GD-100", "Route Guide", "Route Section", "route-focus");
        SearchResult reranked = result("GD-200", "Lexical Guide", "Lexical Section", "hybrid");

        List<SearchResult> merged = PackRetrievalOrchestrationPolicy.mergePreferredResults(
            List.of(route),
            List.of(reranked),
            4
        );

        assertEquals(List.of(route, reranked), merged);
    }

    @Test
    public void preferredMergeDedupesSecondaryByGuideSectionKey() {
        SearchResult route = result("GD-100", "Route Guide", "Same Section", "route-focus");
        SearchResult duplicate = result("GD-100", "Route Guide", "Same Section", "hybrid");
        SearchResult reranked = result("GD-200", "Lexical Guide", "Lexical Section", "hybrid");

        List<SearchResult> merged = PackRetrievalOrchestrationPolicy.mergePreferredResults(
            List.of(route),
            List.of(duplicate, reranked),
            4
        );

        assertEquals(List.of(route, reranked), merged);
    }

    @Test
    public void preferredMergeStopsAtLimit() {
        SearchResult route = result("GD-100", "Route Guide", "Route Section", "route-focus");
        SearchResult reranked = result("GD-200", "Lexical Guide", "Lexical Section", "hybrid");

        List<SearchResult> merged = PackRetrievalOrchestrationPolicy.mergePreferredResults(
            List.of(route),
            List.of(reranked),
            1
        );

        assertEquals(List.of(route), merged);
    }

    @Test
    public void preferredMergePreservesEmptyPrimaryFallbackBehavior() {
        List<SearchResult> fallback = List.of(result("GD-200", "Lexical Guide", "Lexical Section", "hybrid"));

        List<SearchResult> merged = PackRetrievalOrchestrationPolicy.mergePreferredResults(List.of(), fallback, 1);

        assertSame(fallback, merged);
    }

    private static SearchResult result(String guideId, String title, String sectionHeading, String retrievalMode) {
        return new SearchResult(
            title,
            "",
            title + " " + sectionHeading,
            title + " " + sectionHeading,
            guideId,
            sectionHeading,
            "test",
            retrievalMode
        );
    }
}
