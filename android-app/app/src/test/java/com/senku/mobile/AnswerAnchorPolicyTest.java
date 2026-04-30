package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

    @Test
    public void anchorChoiceBuilderMapsNamedInputsToChoiceFields() {
        SearchResult rankedAnchor = guideFocusAnchor("general_reference");
        SearchResult routedAnchor = guideFocusAnchor("water_distribution");

        AnswerAnchorPolicy.AnchorChoice choice = AnswerAnchorPolicy.anchorChoice(rankedAnchor, routedAnchor)
            .routeFocused(true)
            .preferRouteAnchorOverRankedGuide(true)
            .preferCabinSiteSelectionRouteAnchor(true)
            .routedHasCabinSiteSelectionSignal(true)
            .rankedHasCabinSiteSelectionSignal(false)
            .preferRoofWeatherproofRouteAnchor(true)
            .routedHasRoofWeatherproofSignal(true)
            .rankedHasRoofWeatherproofDistractorSignal(true)
            .rankedHasRoofWeatherproofSignal(false)
            .rankedGuideFocusWaterDistributionFallback(true)
            .sameGuideGroup(false)
            .rankedSupportWithMetadata(31)
            .routedSupportWithMetadata(43)
            .build();

        assertTrue(choice.rankedAnchor == rankedAnchor);
        assertTrue(choice.routedAnchor == routedAnchor);
        assertTrue(choice.routeFocused);
        assertTrue(choice.preferRouteAnchorOverRankedGuide);
        assertTrue(choice.preferCabinSiteSelectionRouteAnchor);
        assertTrue(choice.routedHasCabinSiteSelectionSignal);
        assertFalse(choice.rankedHasCabinSiteSelectionSignal);
        assertTrue(choice.preferRoofWeatherproofRouteAnchor);
        assertTrue(choice.routedHasRoofWeatherproofSignal);
        assertTrue(choice.rankedHasRoofWeatherproofDistractorSignal);
        assertFalse(choice.rankedHasRoofWeatherproofSignal);
        assertTrue(choice.rankedGuideFocusWaterDistributionFallback);
        assertFalse(choice.sameGuideGroup);
        assertEquals(31, choice.rankedSupportWithMetadata);
        assertEquals(43, choice.routedSupportWithMetadata);
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
