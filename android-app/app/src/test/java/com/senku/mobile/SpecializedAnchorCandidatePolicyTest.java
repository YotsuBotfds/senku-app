package com.senku.mobile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class SpecializedAnchorCandidatePolicyTest {
    @Test
    public void explicitTopicRowsAcceptPreferredStructureOrDelimitedTopicOverlap() {
        QueryMetadataProfile profile =
            QueryMetadataProfile.fromQuery("How do I make soap with lye safely?");

        assertTrue(SpecializedAnchorCandidatePolicy.requiresNarrowExplicitAnchor(profile));
        assertTrue(SpecializedAnchorCandidatePolicy.matchesExplicitTopicRow(profile, "soapmaking", ""));
        assertTrue(SpecializedAnchorCandidatePolicy.matchesExplicitTopicRow(
            profile,
            "general",
            "food_storage; lye_safety|camp"
        ));
        assertFalse(SpecializedAnchorCandidatePolicy.matchesExplicitTopicRow(
            profile,
            "general",
            "food_storage;camp"
        ));
    }

    @Test
    public void blankGuideFocusOutsidePreferredStructureIsRejectedOnlyForWrongStructure() {
        QueryMetadataProfile profile =
            QueryMetadataProfile.fromQuery("How do I make soap with lye safely?");

        assertTrue(SpecializedAnchorCandidatePolicy.isBlankGuideFocusOutsidePreferredStructure(
            profile,
            result("guide-focus", "", "general", "")
        ));
        assertFalse(SpecializedAnchorCandidatePolicy.isBlankGuideFocusOutsidePreferredStructure(
            profile,
            result("guide-focus", "", "soapmaking", "")
        ));
        assertFalse(SpecializedAnchorCandidatePolicy.isBlankGuideFocusOutsidePreferredStructure(
            profile,
            result("route-focus", "", "general", "")
        ));
    }

    @Test
    public void topicTagMatchingIsExactAcrossSupportedDelimiters() {
        assertTrue(SpecializedAnchorCandidatePolicy.hasTopicTag(
            "water_storage; water_distribution|rotation\nlye_safety",
            "water_distribution"
        ));
        assertFalse(SpecializedAnchorCandidatePolicy.hasTopicTag("water_distribution_backup", "water_distribution"));
        assertFalse(SpecializedAnchorCandidatePolicy.hasTopicTag("water storage", "water"));
    }

    private static SearchResult result(
        String retrievalMode,
        String sectionHeading,
        String structureType,
        String topicTags
    ) {
        return new SearchResult(
            "Test Guide",
            "",
            "snippet",
            "body",
            "GD-TEST",
            sectionHeading,
            "survival",
            retrievalMode,
            "",
            "",
            structureType,
            topicTags
        );
    }
}
