package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

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

    @Test
    public void routeSupportPolicyMatchesRepositoryCompatibilityWrapper() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(
            "how do i design a gravity fed water distribution system"
        );
        SearchResult result = waterDistributionSupport(
            "Community Water Distribution Systems",
            "Gravity-Fed Distribution Systems",
            "Distribution layout, spring box, and household taps for a gravity-fed system.",
            "WD-270"
        );
        boolean diversifyContext = queryTerms.routeProfile.prefersDiversifiedAnswerContext()
            || queryTerms.metadataProfile.prefersDiversifiedContext();

        boolean extracted = PackRouteSupportPolicy.supportCandidateMatchesRoute(
            queryTerms.routeProfile,
            queryTerms.metadataProfile,
            diversifyContext,
            result
        );
        boolean repository = PackRepository.supportCandidateMatchesRoute(
            queryTerms.routeProfile,
            queryTerms.metadataProfile,
            diversifyContext,
            result
        );

        assertEquals(repository, extracted);
        assertTrue(extracted);
    }

    @Test
    public void rankSupportCandidatesRejectsRouteSupportDistractorThroughExtractedPolicy() {
        SearchResult anchor = waterDistributionSupport(
            "Community Water Distribution Systems",
            "Gravity-Fed Distribution Systems",
            "Anchor result for distribution network design.",
            "WD-ANCHOR"
        );
        SearchResult distractor = waterDistributionSupport(
            "Water Distribution Irrigation Troubleshooting",
            "Irrigation troubleshooting",
            "Irrigation troubleshooting and infrastructure failure analysis.",
            "WD-DISTRACTOR"
        );
        SearchResult support = waterDistributionSupport(
            "Spring Box Distribution Layout",
            "Spring Box and Household Taps",
            "Spring box, storage tank, and household taps for gravity-fed distribution.",
            "WD-SUPPORT"
        );
        List<SearchResult> ranked = new ArrayList<>();
        ranked.add(anchor);
        ranked.add(distractor);
        ranked.add(support);

        List<SearchResult> ordered = PackAnswerContextPolicy.rankSupportCandidatesForTest(
            "how do i design a gravity fed water distribution system",
            anchor,
            ranked
        );

        assertFalse(ordered.contains(distractor));
        assertTrue(ordered.contains(support));
    }

    private static SearchResult waterDistributionSupport(
        String title,
        String sectionHeading,
        String text,
        String guideId
    ) {
        return new SearchResult(
            title,
            "",
            text,
            text,
            guideId,
            sectionHeading,
            "building",
            "route-focus",
            "planning",
            "long_term",
            "water_distribution",
            "water_distribution,water_storage"
        );
    }
}
