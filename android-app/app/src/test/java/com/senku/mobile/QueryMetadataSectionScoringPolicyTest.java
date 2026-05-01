package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class QueryMetadataSectionScoringPolicyTest {
    @Test
    public void policyPreservesExactWaterDistributionScores() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );
        QueryMetadataSectionScoringPolicy policy = new QueryMetadataSectionScoringPolicy(profile);

        assertEquals(24, policy.sectionHeadingBonus("Distribution Network Layout"));
        assertEquals(-10, policy.sectionHeadingBonus("Troubleshooting and Emergency Repairs"));
        assertEquals(
            74,
            policy.metadataBonus(
                "building",
                "planning",
                "long_term",
                "water_distribution",
                "water_distribution,water_storage"
            )
        );
    }

    @Test
    public void profileDelegatesToSectionScoringPolicy() {
        QueryMetadataProfile profile = QueryMetadataProfile.fromQuery(
            "what's the safest way to store treated water long term"
        );
        QueryMetadataSectionScoringPolicy policy = new QueryMetadataSectionScoringPolicy(profile);

        assertEquals(
            policy.sectionHeadingBonus("Food-Safe Containers and Sanitation"),
            profile.sectionHeadingBonus("Food-Safe Containers and Sanitation")
        );
        assertEquals(
            policy.sectionHeadingBonus("Water Tower Construction & Sizing"),
            profile.sectionHeadingBonus("Water Tower Construction & Sizing")
        );
        assertEquals(
            policy.metadataBonus(
                "resource-management",
                "planning",
                "long_term",
                "water_storage",
                "water_storage,container_sanitation,water_rotation,disinfection"
            ),
            profile.metadataBonus(
                "resource-management",
                "planning",
                "long_term",
                "water_storage",
                "water_storage,container_sanitation,water_rotation,disinfection"
            )
        );
    }
}
