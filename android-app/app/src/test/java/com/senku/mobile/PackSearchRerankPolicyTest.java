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
        assertEquals(expectedSingleGuideFinalScore(first.metadataBonus), first.finalScore, 0.0);
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

    private static double expectedSingleGuideFinalScore(int score) {
        return score + Math.min(24, Math.max(1, score) / 3);
    }
}
