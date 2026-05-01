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

    @Test
    public void foodTheftGovernanceContextRejectsKitchenOpsSupportAndKeepsCommonsSupport() {
        SearchResult anchor = governanceSupport(
            "Commons Management & Sustainable Resource Governance",
            "Monitoring and Graduated Sanctions",
            "Resource rules, monitoring, restitution, and graduated sanctions for shared food stores.",
            "GD-626"
        );
        SearchResult kitchenOps = new SearchResult(
            "Community Kitchen Operations",
            "",
            "Food prep, serving lines, kitchen operations, and cooking rotation logistics.",
            "Food prep, serving lines, kitchen operations, and cooking rotation logistics.",
            "GD-KITCHEN",
            "Meal Service and Cooking Rotations",
            "resource-management",
            "route-focus",
            "subsystem",
            "long_term",
            "community_governance",
            "community_governance,resource_governance"
        );
        SearchResult commonsSupport = governanceSupport(
            "Resource Governance Field Notes",
            "Restitution and Proportional Consequences",
            "Verify facts, document the loss, apply restitution, and protect the commons.",
            "GD-COMMONS"
        );
        List<SearchResult> ranked = new ArrayList<>();
        ranked.add(anchor);
        ranked.add(kitchenOps);
        ranked.add(commonsSupport);

        List<SearchResult> ordered = PackAnswerContextPolicy.rankSupportCandidatesForTest(
            "someone is stealing food from the group what do we do",
            anchor,
            ranked
        );

        assertFalse(ordered.contains(kitchenOps));
        assertTrue(ordered.contains(commonsSupport));
    }

    @Test
    public void assembleGuideAnswerContextDiversifiedNonWaterOrdersSupportBeforeExtraAnchorSections() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery("how do i build a tarp shelter");
        SearchResult anchor = result("GD-A", "Tarp Shelter", "Anchor", "guide-focus");
        SearchResult anchorDetail = result("GD-A", "Tarp Shelter", "Ridgeline", "guide-focus");
        SearchResult anchorFill = result("GD-A", "Tarp Shelter", "Drainage", "guide-focus");
        SearchResult support = result("GD-S", "Weatherproofing", "Runoff", "route-focus");
        List<PackAnswerContextPolicy.SupportCandidate> supports = new ArrayList<>();
        supports.add(new PackAnswerContextPolicy.SupportCandidate(support, 1, 10));

        List<SearchResult> context = PackAnswerContextPolicy.assembleGuideAnswerContext(
            queryTerms,
            true,
            anchor,
            java.util.List.of(anchor, anchorDetail, anchorFill),
            supports,
            4
        );

        assertEquals("Anchor", context.get(0).sectionHeading);
        assertEquals("Runoff", context.get(1).sectionHeading);
        assertEquals("Ridgeline", context.get(2).sectionHeading);
        assertEquals("Drainage", context.get(3).sectionHeading);
        assertEquals("route-focus", context.get(1).retrievalMode);
    }

    @Test
    public void assembleGuideAnswerContextWaterDistributionSeedsAnchorGuideBeforeSupport() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(
            "how do i design a gravity fed water distribution system"
        );
        SearchResult anchor = result("GD-W", "Water Distribution", "Gravity-Fed Distribution", "guide-focus");
        SearchResult storage = result("GD-W", "Water Distribution", "Storage Tank", "guide-focus");
        SearchResult taps = result("GD-W", "Water Distribution", "Household Taps", "guide-focus");
        SearchResult support = result("GD-S", "Spring Box", "Source Capture", "route-focus");
        List<PackAnswerContextPolicy.SupportCandidate> supports = new ArrayList<>();
        supports.add(new PackAnswerContextPolicy.SupportCandidate(support, 1, 10));

        List<SearchResult> context = PackAnswerContextPolicy.assembleGuideAnswerContext(
            queryTerms,
            true,
            anchor,
            java.util.List.of(anchor, storage, taps),
            supports,
            4
        );

        assertEquals("Gravity-Fed Distribution", context.get(0).sectionHeading);
        assertEquals("Storage Tank", context.get(1).sectionHeading);
        assertEquals("Household Taps", context.get(2).sectionHeading);
        assertEquals("Source Capture", context.get(3).sectionHeading);
        assertEquals("guide-focus", context.get(2).retrievalMode);
        assertEquals("route-focus", context.get(3).retrievalMode);
    }

    @Test
    public void assembleGuideAnswerContextSuppressesDuplicateSectionsAndRespectsLimit() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery("how do i build a tarp shelter");
        SearchResult anchor = result("GD-A", "Tarp Shelter", "Anchor", "guide-focus");
        SearchResult anchorDetail = result("GD-A", "Tarp Shelter", "Ridgeline", "guide-focus");
        SearchResult anchorFill = result("GD-A", "Tarp Shelter", "Drainage", "guide-focus");
        SearchResult duplicateSupport = result("GD-A", "Tarp Shelter", "Ridgeline", "route-focus");
        SearchResult support = result("GD-S", "Weatherproofing", "Runoff", "route-focus");
        SearchResult extraSupport = result("GD-X", "Cordage", "Knots", "route-focus");
        List<PackAnswerContextPolicy.SupportCandidate> supports = new ArrayList<>();
        supports.add(new PackAnswerContextPolicy.SupportCandidate(duplicateSupport, 1, 12));
        supports.add(new PackAnswerContextPolicy.SupportCandidate(support, 2, 10));
        supports.add(new PackAnswerContextPolicy.SupportCandidate(extraSupport, 3, 8));

        List<SearchResult> context = PackAnswerContextPolicy.assembleGuideAnswerContext(
            queryTerms,
            false,
            anchor,
            java.util.List.of(anchor, anchorDetail, anchorFill),
            supports,
            3
        );

        assertEquals(3, context.size());
        assertEquals("Anchor", context.get(0).sectionHeading);
        assertEquals("Ridgeline", context.get(1).sectionHeading);
        assertEquals("Runoff", context.get(2).sectionHeading);
        assertEquals("route-focus", context.get(2).retrievalMode);
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

    private static SearchResult governanceSupport(
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
            "resource-management",
            "route-focus",
            "subsystem",
            "long_term",
            "community_governance",
            "community_governance,conflict_resolution"
        );
    }

    private static SearchResult result(String guideId, String title, String sectionHeading, String retrievalMode) {
        String text = title + " " + sectionHeading;
        return new SearchResult(
            title,
            "",
            text,
            text,
            guideId,
            sectionHeading,
            "building",
            retrievalMode,
            "planning",
            "long_term",
            "cabin_house",
            "shelter,weatherproofing"
        );
    }
}
