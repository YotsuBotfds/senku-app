package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class AnswerAnchorPolicyTest {
    @Test
    public void routeAnchorPreferenceTracksSpecializedRoutePolicySet() {
        List<String> specializedStructureTypes = Arrays.asList(
            "water_distribution",
            "message_auth",
            "community_security",
            "community_governance",
            "soapmaking",
            "glassmaking",
            "fair_trial"
        );
        SearchResult genericGuideFocusAnchor = guideFocusAnchor("general_reference");

        for (String structureType : specializedStructureTypes) {
            assertTrue(RetrievalRoutePolicy.requiresSpecializedRouteAnchorSignal(structureType));
            assertEquals(
                RetrievalRoutePolicy.requiresSpecializedRouteAnchorSignal(structureType),
                AnswerAnchorPolicy.shouldPreferRouteAnchorOverRankedGuide(
                    structureType,
                    genericGuideFocusAnchor
                )
            );
        }

        assertEquals(
            RetrievalRoutePolicy.requiresSpecializedRouteAnchorSignal("water_storage"),
            AnswerAnchorPolicy.shouldPreferRouteAnchorOverRankedGuide(
                "water_storage",
                genericGuideFocusAnchor
            )
        );
    }

    private static SearchResult guideFocusAnchor(String structureType) {
        return new SearchResult(
            "Guide",
            "",
            "",
            "",
            "GD-001",
            "",
            "",
            "guide-focus",
            "",
            "",
            structureType,
            ""
        );
    }
}
