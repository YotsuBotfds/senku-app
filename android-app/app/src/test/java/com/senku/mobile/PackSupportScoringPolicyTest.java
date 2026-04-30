package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PackSupportScoringPolicyTest {
    @Test
    public void supportBreakdownMatchesPackRepositoryCompatibilityWrapper() {
        String query = "how do i design a gravity-fed water distribution system";
        SearchResult result = new SearchResult(
            "Community Water Distribution Systems",
            "",
            "Gravity-fed distribution and storage tank planning for settlement-scale water delivery.",
            "Gravity-fed distribution and storage tank planning for settlement-scale water delivery.",
            "GD-270",
            "Gravity-Fed Distribution Systems",
            "building",
            "vector",
            "planning",
            "long_term",
            "water_distribution",
            "water_distribution,water_storage"
        );

        PackSupportScoringPolicy.SupportBreakdown policyBreakdown =
            PackSupportScoringPolicy.supportBreakdown(PackRepository.QueryTerms.fromQuery(query), result);
        PackRepository.SupportBreakdown repositoryBreakdown = PackRepository.supportBreakdownForTest(query, result);

        assertEquals(repositoryBreakdown.lexicalSupport, policyBreakdown.lexicalSupport);
        assertEquals(repositoryBreakdown.metadataBonus, policyBreakdown.metadataBonus);
        assertEquals(repositoryBreakdown.specializedTopicBonus, policyBreakdown.specializedTopicBonus);
        assertEquals(repositoryBreakdown.sectionBonus, policyBreakdown.sectionBonus);
        assertEquals(repositoryBreakdown.structurePenalty, policyBreakdown.structurePenalty);
        assertEquals(repositoryBreakdown.supportWithMetadata(), policyBreakdown.supportWithMetadata());
    }

    @Test
    public void guideGroupKeyPrefersGuideIdAndFallsBackToNormalizedTitle() {
        SearchResult withGuideId = new SearchResult(
            "Community Water Distribution Systems",
            "",
            "",
            "",
            "GD-270",
            "",
            "building",
            "route-focus"
        );
        SearchResult withoutGuideId = new SearchResult(
            "  Community   Water Distribution Systems  ",
            "",
            "",
            "",
            "",
            "",
            "building",
            "route-focus"
        );

        assertEquals("gd-270", PackSupportScoringPolicy.guideGroupKey(withGuideId));
        assertEquals(
            "community water distribution systems",
            PackSupportScoringPolicy.guideGroupKey(withoutGuideId)
        );
    }

    @Test
    public void supportRetrievalRankOrdersRouteFocusedSupportAboveLexicalFallback() {
        assertTrue(
            PackSupportScoringPolicy.supportRetrievalRank("route-focus")
                > PackSupportScoringPolicy.supportRetrievalRank("lexical")
        );
        assertEquals(0, PackSupportScoringPolicy.supportRetrievalRank(""));
    }
}
