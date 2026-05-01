package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public final class PackSearchFinalizationPolicyTest {
    @Test
    public void elapsedMsBetweenFloorsToMillisecondsAndClampsNegativeIntervals() {
        assertEquals(1L, PackSearchFinalizationPolicy.elapsedMsBetween(1_000_000L, 2_999_999L));
        assertEquals(0L, PackSearchFinalizationPolicy.elapsedMsBetween(5_000_000L, 4_000_000L));
    }

    @Test
    public void toSearchFinalizationPreservesResultsReferenceAndElapsedMs() {
        List<SearchResult> results = List.of(result("GD-100", "First", "Section"));

        PackRepository.SearchFinalization finalization =
            PackSearchFinalizationPolicy.toSearchFinalization(results, 17L);

        assertSame(results, finalization.rerankedResults);
        assertEquals(17L, finalization.rerankElapsedMs);
    }

    @Test
    public void prerankCandidateTelemetryCapsToRequestedLimitAndFormatsChunkFields() {
        PackRepository.CombinedHit first = combined(chunk("c-1", "GD-100", "Water Storage", "water,sanitation"), 0.75);
        PackRepository.CombinedHit second = combined(chunk("c-2", "GD-200", "Rain Shelter", "rain,shelter"), 0.5);

        String line = PackSearchFinalizationPolicy.buildPrerankCandidateTelemetryLine(
            "rain \"shelter\"",
            List.of(first, second),
            1
        );

        assertTrue(line.startsWith("search.candidates.prerank query=\"rain \\\"shelter\\\"\" n=1 rows=["));
        assertTrue(line.contains("1|GD-100|Water Storage|0.750|emergency_shelter|survival|water,sanitation"));
        assertTrue(line.endsWith("]"));
        assertTrue(!line.contains("GD-200"));
    }

    @Test
    public void rerankedCandidateTelemetryIncludesBaseMetadataAndFinalScores() {
        SearchResult result = result("GD-345", "Rain Shelter", "Rigging");
        PackRepository.RerankedResult reranked =
            new PackRepository.RerankedResult(result, 0, 7, 42.25);

        String line = PackSearchFinalizationPolicy.buildRerankedCandidateTelemetryLine(
            "rain shelter",
            List.of(reranked)
        );

        assertEquals(
            "search.candidates.reranked query=\"rain shelter\" n=1 rows=["
                + "1|GD-345|Rigging|42.250|emergency_shelter|survival|rain,shelter"
                + "|35.250|7|42.250]",
            line
        );
    }

    @Test
    public void candidateTelemetryHandlesNullInputsAsEmptyRows() {
        assertEquals(
            "search.candidates.prerank query=\"empty\" n=0 rows=[]",
            PackSearchFinalizationPolicy.buildPrerankCandidateTelemetryLine("empty", null, 5)
        );
        assertEquals(
            "search.candidates.reranked query=\"empty\" n=0 rows=[]",
            PackSearchFinalizationPolicy.buildRerankedCandidateTelemetryLine("empty", null)
        );
    }

    private static PackRepository.CombinedHit combined(PackRepository.RankedChunk chunk, double score) {
        PackRepository.CombinedHit hit = new PackRepository.CombinedHit(chunk);
        hit.rrfScore = score;
        return hit;
    }

    private static PackRepository.RankedChunk chunk(
        String chunkId,
        String guideId,
        String sectionHeading,
        String topicTags
    ) {
        return new PackRepository.RankedChunk(
            chunkId,
            0,
            guideId + " title",
            guideId,
            sectionHeading,
            "survival",
            "body",
            "",
            "",
            "emergency_shelter",
            topicTags,
            0.0,
            0,
            Integer.MAX_VALUE,
            Float.NEGATIVE_INFINITY
        );
    }

    private static SearchResult result(String guideId, String title, String sectionHeading) {
        return new SearchResult(
            title,
            "",
            title + " " + sectionHeading,
            "",
            guideId,
            sectionHeading,
            "survival",
            "hybrid",
            "",
            "",
            "emergency_shelter",
            "rain,shelter"
        );
    }
}
