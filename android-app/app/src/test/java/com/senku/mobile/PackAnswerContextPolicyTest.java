package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class PackAnswerContextPolicyTest {
    @Test
    public void shouldKeepGuideSectionForContextAlwaysKeepsAnchorSection() {
        boolean keep = PackAnswerContextPolicy.shouldKeepGuideSectionForContext(
            "water_distribution",
            true,
            -12,
            false,
            false,
            false,
            false,
            false
        );

        assertEquals(true, keep);
    }

    @Test
    public void shouldKeepGuideSectionForContextRejectsNegativeWaterSection() {
        boolean keep = PackAnswerContextPolicy.shouldKeepGuideSectionForContext(
            "water_distribution",
            false,
            -1,
            false,
            false,
            false,
            false,
            false
        );

        assertEquals(false, keep);
    }

    @Test
    public void shouldKeepGuideSectionForContextRejectsRoofDistractorWhenRoofContextIsPreferred() {
        boolean keep = PackAnswerContextPolicy.shouldKeepGuideSectionForContext(
            "cabin_house",
            false,
            0,
            true,
            true,
            false,
            false,
            false
        );

        assertEquals(false, keep);
    }

    @Test
    public void shouldKeepGuideSectionForContextAllowsGovernanceDistractorWithTrustRepairSignal() {
        boolean keep = PackAnswerContextPolicy.shouldKeepGuideSectionForContext(
            "community_governance",
            false,
            0,
            false,
            false,
            true,
            true,
            true
        );

        assertEquals(true, keep);
    }
}
