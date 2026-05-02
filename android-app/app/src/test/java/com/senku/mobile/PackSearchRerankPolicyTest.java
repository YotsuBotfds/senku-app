package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class PackSearchRerankPolicyTest {
    @Test
    public void metadataRerankAppliesForRouteFocusOrStructureHintOnly() {
        assertTrue(PackSearchRerankPolicy.shouldApplyMetadataRerank(
            PackRepository.QueryTerms.fromQuery("how do i start a fire in the rain")
        ));
        assertTrue(PackSearchRerankPolicy.shouldApplyMetadataRerank(
            PackRepository.QueryTerms.fromQuery("How do I build a simple rain shelter from tarp and cord?")
        ));
        assertFalse(PackSearchRerankPolicy.shouldApplyMetadataRerank(
            PackRepository.QueryTerms.fromQuery("best way to organize spare tools")
        ));
        assertFalse(PackSearchRerankPolicy.shouldApplyMetadataRerank(null));
    }

    @Test
    public void queryWithoutMetadataRerankUsesPassthroughRows() {
        SearchResult first = result("GD-301", "Workshop Organization", "Tool Storage Basics", "lexical", "general");
        SearchResult second = result("GD-302", "Storage Containers", "Inventory Labels", "guide-focus", "general");

        List<PackRepository.RerankedResult> reranked = PackSearchRerankPolicy.maybeRerankResultsDetailed(
            PackRepository.QueryTerms.fromQuery("best way to organize spare tools"),
            Arrays.asList(first, second),
            1
        );

        assertEquals(1, reranked.size());
        assertSame(first, reranked.get(0).result);
        assertEquals(0, reranked.get(0).metadataBonus);
        assertEquals(0.0, reranked.get(0).finalScore, 0.0);
        assertEquals(0, reranked.get(0).originalIndex);
    }

    @Test
    public void structureHintRerankSortsByMetadataAndModeBonuses() {
        SearchResult unrelated = result(
            "GD-727",
            "Camp Inventory Ledger",
            "Inventory",
            "lexical",
            "utility",
            "maintenance,storage",
            "general"
        );
        SearchResult shelter = result(
            "GD-345",
            "Shelter Build Log",
            "Inventory",
            "vector",
            "survival",
            "tarp_shelter,weatherproofing",
            "emergency_shelter"
        );

        List<PackRepository.RerankedResult> reranked = PackSearchRerankPolicy.maybeRerankResultsDetailed(
            PackRepository.QueryTerms.fromQuery("How do I build a simple rain shelter from tarp and cord?"),
            Arrays.asList(unrelated, shelter),
            5
        );

        PackRepository.RerankedResult first = reranked.get(0);
        assertSame(shelter, first.result);
        assertTrue(first.metadataBonus > 0);
        assertTrue(first.finalScore > expectedSingleGuideFinalScore(first.metadataBonus));
    }

    @Test
    public void emergencyShelterVectorOwnerStaysInsideTopRouteWindow() {
        SearchResult winterVector = result(
            "GD-024",
            "Winter Survival Systems",
            "Cold Weather Shelter",
            "vector",
            "survival",
            "cold_weather",
            "general",
            "Build a simple rain shelter with tarp and cord ideas only as a cold weather aside."
        );
        SearchResult winterLexical = result(
            "GD-024",
            "Winter Survival Systems",
            "Emergency Rain Aside",
            "lexical",
            "survival",
            "cold_weather",
            "general",
            "Rain and shelter notes for winter travel."
        );
        SearchResult greywater = result(
            "GD-673",
            "Greywater Recycling & Treatment Systems",
            "Drainage",
            "lexical",
            "water",
            "greywater",
            "general",
            "Manage rain runoff and cordon treatment areas."
        );
        SearchResult earthShelter = result(
            "GD-873",
            "Underground Shelter & Bunker Construction",
            "Bunker Cover",
            "lexical",
            "building",
            "earth_shelter",
            "earth_shelter",
            "Build shelter cover with roof drainage."
        );
        SearchResult tarpShelter = result(
            "GD-345",
            "Tarp & Cord Shelters",
            "Rain shelter setup",
            "vector",
            "survival",
            "tarp_shelter,weatherproofing",
            "emergency_shelter",
            "Build a simple rain shelter by tying a tarp ridgeline with cord and tensioning runoff edges."
        );

        List<PackRepository.RerankedResult> reranked = PackSearchRerankPolicy.maybeRerankResultsDetailed(
            PackRepository.QueryTerms.fromQuery("How do I build a simple rain shelter from tarp and cord?"),
            Arrays.asList(winterVector, winterLexical, greywater, earthShelter, tarpShelter),
            4
        );

        int ownerIndex = indexOfGuide(reranked, "GD-345");
        assertTrue(ownerIndex >= 0);
        assertTrue(ownerIndex < 4);
    }

    @Test
    public void modeAndGuideAggregationBonusesMatchRepositoryCompatibilityValues() {
        assertEquals(8, PackSearchRerankPolicy.rerankModeBonus("route-focus"));
        assertEquals(4, PackSearchRerankPolicy.rerankModeBonus("hybrid"));
        assertEquals(3, PackSearchRerankPolicy.rerankModeBonus("guide-focus"));
        assertEquals(2, PackSearchRerankPolicy.rerankModeBonus("lexical"));
        assertEquals(0, PackSearchRerankPolicy.rerankModeBonus("vector"));

        PackAnswerAnchorScoringPolicy.GuideScore guide =
            new PackAnswerAnchorScoringPolicy.GuideScore(result("GD-100", "Guide", "A", "lexical", "general"));
        guide.totalScore = 90;
        guide.sectionKeys.add("GD-100|A");
        guide.sectionKeys.add("GD-100|B");

        assertEquals(30, PackSearchRerankPolicy.guideAggregationBonus(guide));
    }

    private static SearchResult result(
        String guideId,
        String title,
        String sectionHeading,
        String retrievalMode,
        String structureType
    ) {
        return result(
            guideId,
            title,
            sectionHeading,
            retrievalMode,
            "survival",
            "tarp_shelter,weatherproofing",
            structureType
        );
    }

    private static SearchResult result(
        String guideId,
        String title,
        String sectionHeading,
        String retrievalMode,
        String category,
        String topicTags,
        String structureType
    ) {
        return new SearchResult(
            title,
            "",
            title + " " + sectionHeading,
            title + " " + sectionHeading,
            guideId,
            sectionHeading,
            category,
            retrievalMode,
            "starter",
            "immediate",
            structureType,
            topicTags
        );
    }

    private static SearchResult result(
        String guideId,
        String title,
        String sectionHeading,
        String retrievalMode,
        String category,
        String topicTags,
        String structureType,
        String body
    ) {
        return new SearchResult(
            title,
            "",
            body,
            body,
            guideId,
            sectionHeading,
            category,
            retrievalMode,
            "starter",
            "immediate",
            structureType,
            topicTags
        );
    }

    private static int indexOfGuide(List<PackRepository.RerankedResult> reranked, String guideId) {
        for (int index = 0; index < reranked.size(); index++) {
            if (guideId.equals(reranked.get(index).result.guideId)) {
                return index;
            }
        }
        return -1;
    }

    private static double expectedSingleGuideFinalScore(int score) {
        return score + Math.min(24, Math.max(1, score) / 3);
    }
}
