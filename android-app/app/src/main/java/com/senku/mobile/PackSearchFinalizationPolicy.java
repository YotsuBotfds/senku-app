package com.senku.mobile;

import java.util.ArrayList;
import java.util.List;

final class PackSearchFinalizationPolicy {
    private PackSearchFinalizationPolicy() {
    }

    static long elapsedMsBetween(long startedAtNs, long endedAtNs) {
        return Math.max(0L, endedAtNs - startedAtNs) / 1_000_000L;
    }

    static PackRepository.SearchFinalization toSearchFinalization(
        List<SearchResult> rerankedResults,
        long rerankElapsedMs
    ) {
        return new PackRepository.SearchFinalization(rerankedResults, rerankElapsedMs);
    }

    static String buildPrerankCandidateTelemetryLine(
        String query,
        List<PackRepository.CombinedHit> hits,
        int limit
    ) {
        ArrayList<String> rows = new ArrayList<>();
        if (hits != null) {
            int capped = CandidateTelemetryFormatter.limitedRowCount(hits.size(), limit);
            for (int index = 0; index < capped; index++) {
                PackRepository.CombinedHit hit = hits.get(index);
                rows.add(
                    CandidateTelemetryFormatter.formatRow(
                        index + 1,
                        hit.chunk.guideId,
                        hit.chunk.sectionHeading,
                        hit.rrfScore,
                        hit.chunk.structureType,
                        hit.chunk.category,
                        hit.chunk.topicTags
                    )
                );
            }
        }
        return CandidateTelemetryFormatter.buildLine("prerank", query, rows);
    }

    static String buildRerankedCandidateTelemetryLine(
        String query,
        List<PackRepository.RerankedResult> results
    ) {
        ArrayList<String> rows = new ArrayList<>();
        if (results != null) {
            int capped = CandidateTelemetryFormatter.limitedRowCount(results.size());
            for (int index = 0; index < capped; index++) {
                PackRepository.RerankedResult result = results.get(index);
                rows.add(
                    CandidateTelemetryFormatter.formatRerankedRow(
                        index + 1,
                        result.result,
                        result.finalScore,
                        result.baseScore,
                        result.metadataBonus
                    )
                );
            }
        }
        return CandidateTelemetryFormatter.buildLine("reranked", query, rows);
    }
}
