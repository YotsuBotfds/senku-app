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
    public void runtimeRouteGuideSearchThresholdClampsNoBm25WaterDistributionOnly() {
        QueryMetadataProfile waterDistribution = QueryMetadataProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );
        QueryMetadataProfile waterStorage = QueryMetadataProfile.fromQuery(
            "what is the safest way to store treated water long term"
        );

        assertEquals(
            5,
            RetrievalRoutePolicy.runtimeRouteGuideSearchThreshold(waterDistribution, false, 18)
        );
        assertEquals(
            18,
            RetrievalRoutePolicy.runtimeRouteGuideSearchThreshold(waterDistribution, true, 18)
        );
        assertEquals(
            18,
            RetrievalRoutePolicy.runtimeRouteGuideSearchThreshold(waterStorage, false, 18)
        );
    }

    @Test
    public void likeBackfillRunsWhenFtsAddsNothing() {
        assertEquals(
            true,
            RetrievalRoutePolicy.shouldBackfillLikeAfterFts(
                QueryRouteProfile.fromQuery("build a cabin foundation"),
                QueryMetadataProfile.fromQuery("build a cabin foundation"),
                2,
                0,
                0,
                18
            )
        );
    }

    @Test
    public void soapmakingLikeBackfillRunsUntilHealthyFtsPoolExists() {
        String query = "how do i make soap from animal fat and ash";
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(query);
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(query);

        assertEquals(
            true,
            RetrievalRoutePolicy.shouldBackfillLikeAfterFts(routeProfile, metadataProfile, 7, 2, 2, 18)
        );
        assertEquals(
            false,
            RetrievalRoutePolicy.shouldBackfillLikeAfterFts(routeProfile, metadataProfile, 7, 3, 6, 18)
        );
    }

    @Test
    public void directAnchorRequirementMatchesPackRepositoryCompatibilityWrapper() {
        String query = "how do i make soap from animal fat and ash";
        QueryRouteProfile routeProfile = QueryRouteProfile.fromQuery(query);
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(query);

        assertEquals(
            PackRepository.shouldRequireDirectAnchorSignalForTest(query),
            RetrievalRoutePolicy.shouldRequireDirectAnchorSignal(
                routeProfile,
                metadataProfile,
                PackRepository.QueryTerms.fromQuery(query).primaryKeywordTokens().size()
            )
        );
        assertEquals(
            PackRepository.requiresSpecializedRouteAnchorSignal("soapmaking"),
            RetrievalRoutePolicy.requiresSpecializedRouteAnchorSignal("soapmaking")
        );
    }

    @Test
    public void likeBackfillSkipsWhenRouteDoesNotRequireSpecializedAnchorSignal() {
        String query = "how do i build a storage shelf";

        assertEquals(
            false,
            RetrievalRoutePolicy.shouldBackfillLikeAfterFts(
                QueryRouteProfile.fromQuery(query),
                QueryMetadataProfile.fromQuery(query),
                5,
                2,
                2,
                18
            )
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

    @Test
    public void waterDistributionSupportRequiresRouteSignal() {
        assertEquals(
            false,
            RetrievalRoutePolicy.allowsWaterDistributionSupportCandidate(
                false,
                0,
                false,
                "route-focus",
                false,
                "",
                false
            )
        );
        assertEquals(
            true,
            RetrievalRoutePolicy.allowsWaterDistributionSupportCandidate(
                true,
                0,
                false,
                "route-focus",
                false,
                "",
                false
            )
        );
    }

    @Test
    public void waterDistributionSupportFiltersGuideFocusDistractors() {
        assertEquals(
            false,
            RetrievalRoutePolicy.allowsWaterDistributionSupportCandidate(
                true,
                0,
                false,
                "guide-focus",
                true,
                "reference",
                false
            )
        );
        assertEquals(
            false,
            RetrievalRoutePolicy.allowsWaterDistributionSupportCandidate(
                true,
                0,
                true,
                "guide-focus",
                true,
                "",
                true
            )
        );
        assertEquals(
            true,
            RetrievalRoutePolicy.allowsWaterDistributionSupportCandidate(
                false,
                1,
                false,
                "route-focus",
                false,
                "",
                true
            )
        );
    }
}
