package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class RetrievalRoutePolicyTest {
    @Test
    public void starterHouseProjectKeepsWideRouteChunkSweep() {
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery("how do i build a house");
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery("how do i build a house");

        assertEquals(
            600,
            RetrievalRoutePolicy.routeChunkCandidateLimit(routeProfile, metadataProfile, 12, false)
        );
        assertEquals(
            42,
            RetrievalRoutePolicy.routeChunkCandidateTarget(routeProfile, metadataProfile, 12, false)
        );
    }

    @Test
    public void siteSelectionCompactSweepUsesNarrowThresholds() {
        String query = "how do i choose a safe site and foundation for a small cabin";
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(query);
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(query);

        assertEquals(
            128,
            RetrievalRoutePolicy.routeChunkCandidateLimit(routeProfile, metadataProfile, 12, true)
        );
        assertEquals(
            16,
            RetrievalRoutePolicy.routeChunkCandidateTarget(routeProfile, metadataProfile, 12, true)
        );
        assertEquals(
            12,
            RetrievalRoutePolicy.routeGuideSearchThreshold(routeProfile, metadataProfile, true, 12)
        );
    }

    @Test
    public void noBm25OrderDetectsSpecializedRoutePriority() {
        RetrievalRoutePolicy.RouteFtsOrderSpec siteOrder = RetrievalRoutePolicy.noBm25RouteFtsOrder(
            "winter sun shade microclimate building site foundation",
            QueryMetadataProfile.fromQuery("winter sun shade microclimate building site foundation")
        );
        RetrievalRoutePolicy.RouteFtsOrderSpec waterOrder = RetrievalRoutePolicy.noBm25RouteFtsOrder(
            "design a gravity-fed water distribution system",
            QueryMetadataProfile.fromQuery("design a gravity-fed water distribution system")
        );

        assertEquals("site_selection_microclimate_priority", siteOrder.label);
        assertEquals(10, siteOrder.args.size());
        assertEquals("water_distribution_priority", waterOrder.label);
        assertEquals(10, waterOrder.args.size());
    }

    @Test
    public void routeSupportStructurePenaltyStaysModeSpecific() {
        assertEquals(-10, RetrievalRoutePolicy.supportStructurePenalty(false, "guide-focus", ""));
        assertEquals(-40, RetrievalRoutePolicy.supportStructurePenalty(true, "guide-focus", ""));
        assertEquals(-18, RetrievalRoutePolicy.supportStructurePenalty(true, "route-focus", ""));
        assertEquals(0, RetrievalRoutePolicy.supportStructurePenalty(true, "route-focus", "Water Storage"));
    }
}
