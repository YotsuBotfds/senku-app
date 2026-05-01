package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public final class PackWaterDistributionAnchorPolicyTest {
    @Test
    public void explicitWaterDistributionAnchorPrefersDistributionGuideOverStorageOverview() {
        SearchResult storageOverview = result(
            "Storage & Material Management",
            "Water Storage: Hydration Assurance",
            "GD-252",
            "resource-management",
            "route-focus",
            "safety",
            "water_storage",
            "water_storage,container_sanitation,water_rotation"
        );
        SearchResult distributionDesign = result(
            "Community Water Distribution Systems",
            "Gravity-Fed Distribution Systems",
            "GD-270",
            "building",
            "route-focus",
            "planning",
            "water_distribution",
            "water_distribution,water_storage"
        );

        SearchResult selected = PackWaterDistributionAnchorPolicy.selectExplicitWaterDistributionAnchor(
            PackRepository.QueryTerms.fromQuery("how do i design a gravity-fed water distribution system"),
            Arrays.asList(storageOverview, distributionDesign)
        );

        assertEquals("GD-270", selected.guideId);
    }

    @Test
    public void explicitWaterDistributionAnchorKeepsRepositoryWrapperParity() {
        String query = "how do i design a gravity-fed water distribution system";
        SearchResult weakLexical = result(
            "Water Distribution Reference",
            "See Also Checklist",
            "GD-901",
            "building",
            "lexical",
            "reference",
            "water_distribution",
            "water_distribution"
        );
        SearchResult strongVector = result(
            "Community Water Distribution Systems",
            "Gravity-Fed Distribution Systems",
            "GD-270",
            "building",
            "route-focus",
            "planning",
            "water_distribution",
            "water_distribution,water_storage"
        );

        SearchResult policySelected = PackWaterDistributionAnchorPolicy.selectExplicitWaterDistributionAnchor(
            PackRepository.QueryTerms.fromQuery(query),
            Arrays.asList(weakLexical, strongVector)
        );
        SearchResult repositorySelected = PackRepository.selectExplicitWaterDistributionAnchorForTest(
            query,
            weakLexical,
            strongVector
        );

        assertEquals(repositorySelected.guideId, policySelected.guideId);
    }

    @Test
    public void explicitWaterDistributionAnchorRequiresExplicitDistributionQuery() {
        SearchResult distributionDesign = result(
            "Community Water Distribution Systems",
            "Gravity-Fed Distribution Systems",
            "GD-270",
            "building",
            "route-focus",
            "planning",
            "water_distribution",
            "water_distribution,water_storage"
        );

        assertNull(PackWaterDistributionAnchorPolicy.selectExplicitWaterDistributionAnchor(
            PackRepository.QueryTerms.fromQuery("what is the safest way to store treated water long term"),
            Arrays.asList(distributionDesign)
        ));
    }

    @Test
    public void waterDistributionAnchorFocusBonusRewardsPlanningAndPenalizesLifecycleChecklists() {
        SearchResult planningGuide = result(
            "Community Water Distribution Systems",
            "Gravity-Fed Distribution Systems",
            "GD-270",
            "building",
            "route-focus",
            "planning",
            "water_distribution",
            "water_distribution,water_storage"
        );
        SearchResult lifecycleChecklist = result(
            "Water Distribution Lifecycle",
            "Phase 3 See Also Checklist",
            "GD-902",
            "building",
            "guide-focus",
            "reference",
            "water_distribution",
            "water_distribution"
        );

        assertTrue(
            PackWaterDistributionAnchorPolicy.waterDistributionAnchorFocusBonus(planningGuide)
                > PackWaterDistributionAnchorPolicy.waterDistributionAnchorFocusBonus(lifecycleChecklist)
        );
    }

    private static SearchResult result(
        String title,
        String sectionHeading,
        String guideId,
        String category,
        String retrievalMode,
        String contentRole,
        String structureType,
        String topicTags
    ) {
        return new SearchResult(
            title,
            "",
            sectionHeading,
            sectionHeading,
            guideId,
            sectionHeading,
            category,
            retrievalMode,
            contentRole,
            "long_term",
            structureType,
            topicTags
        );
    }
}
