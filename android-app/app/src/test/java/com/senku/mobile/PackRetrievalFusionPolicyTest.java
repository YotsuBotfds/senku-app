package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public final class PackRetrievalFusionPolicyTest {
    @Test
    public void lexicalMergeDedupesByChunkAndReindexesByFusedScore() {
        PackRepository.RankedChunk sharedFts = chunk("c-shared", "GD-100", "Shared", "fts body", 0, 0.0f);
        PackRepository.RankedChunk ftsOnly = chunk("c-fts", "GD-101", "Fts", "short", 1, 0.0f);
        PackRepository.RankedChunk sharedKeyword = chunk("c-shared", "GD-100", "Shared", "keyword body", 2, 0.0f);

        List<PackRepository.RankedChunk> merged = PackRetrievalFusionPolicy.mergeLexicalHits(
            List.of(sharedFts, ftsOnly),
            List.of(sharedKeyword)
        );

        assertEquals(2, merged.size());
        assertEquals("c-shared", merged.get(0).chunkId);
        assertEquals(0, merged.get(0).lexicalRank);
        assertTrue(merged.get(0).lexicalScore > merged.get(1).lexicalScore);
    }

    @Test
    public void hybridMergePrefersDualLaneHitOnTieBreak() {
        PackRepository.RankedChunk lexicalOnly = chunk("c-lexical", "GD-100", "Lexical", "body", 0, 0.0f);
        PackRepository.RankedChunk dualLexical = chunk("c-dual", "GD-200", "Dual", "body", 1, 0.0f);
        PackRepository.RankedChunk dualVector = chunk("c-dual", "GD-200", "Dual", "body", 1, 0.9f);

        List<PackRepository.CombinedHit> merged = PackRetrievalFusionPolicy.mergeHybrid(
            List.of(lexicalOnly, dualLexical),
            List.of(dualVector),
            null,
            Collections.emptyMap(),
            message -> {
            }
        );

        assertEquals("c-dual", merged.get(0).chunk.chunkId);
        assertEquals(1, merged.get(0).lexicalRank);
        assertEquals(0, merged.get(0).vectorRank);
    }

    @Test
    public void anchorPriorPromotesRelatedGuideDuringFusion() {
        PackRepository.RankedChunk lexicalTop = chunk("c-top", "GD-100", "Top", "body", 0, 0.0f);
        PackRepository.RankedChunk related = chunk("c-related", "GD-200", "Related", "body", 1, 0.0f);

        List<PackRepository.CombinedHit> merged = PackRetrievalFusionPolicy.mergeHybrid(
            List.of(lexicalTop, related),
            List.of(),
            new PackRepository.AnchorPriorDirective("GD-999", 0, 2),
            Map.of("GD-999", Map.of("GD-200", 0.5)),
            message -> {
            }
        );

        assertEquals("GD-200", merged.get(0).chunk.guideId);
    }

    @Test
    public void searchResultProjectionDedupesGuideSectionAndKeepsLimit() {
        PackRepository.CombinedHit first = combined(chunk("c-1", "GD-100", "Same", "first body", 0, 0.0f), 0);
        PackRepository.CombinedHit duplicate = combined(chunk("c-2", "GD-100", "Same", "duplicate body", 1, 0.0f), 1);
        PackRepository.CombinedHit next = combined(chunk("c-3", "GD-200", "Next", "next body", 2, 0.0f), 2);

        List<SearchResult> results = PackRetrievalFusionPolicy.toSearchResults(List.of(first, duplicate, next), 2);

        assertEquals(2, results.size());
        assertEquals("GD-100", results.get(0).guideId);
        assertEquals("GD-200", results.get(1).guideId);
    }

    private static PackRepository.CombinedHit combined(PackRepository.RankedChunk chunk, int lexicalRank) {
        PackRepository.CombinedHit hit = new PackRepository.CombinedHit(chunk);
        hit.lexicalRank = lexicalRank;
        hit.rrfScore = PackRetrievalFusionPolicy.reciprocalRankForTest(lexicalRank);
        return hit;
    }

    private static PackRepository.RankedChunk chunk(
        String chunkId,
        String guideId,
        String sectionHeading,
        String document,
        int rank,
        float vectorScore
    ) {
        return new PackRepository.RankedChunk(
            chunkId,
            rank,
            guideId + " title",
            guideId,
            sectionHeading,
            "test",
            document,
            "",
            "",
            "",
            "",
            0.0,
            rank,
            vectorScore > 0.0f ? rank : Integer.MAX_VALUE,
            vectorScore > 0.0f ? vectorScore : Float.NEGATIVE_INFINITY
        );
    }
}
