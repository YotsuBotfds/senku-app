package com.senku.mobile;

import java.util.ArrayList;
import java.util.List;

final class PackSearchTelemetryPolicy {
    static final String OUTCOME_PLAIN_LIKE_NO_TERMS = "plainLike_no_terms";
    static final String OUTCOME_ROUTE_ONLY = "route_only";
    static final String OUTCOME_PLAIN_LIKE_EMPTY_LEXICAL = "plainLike_empty_lexical";
    static final String OUTCOME_VECTOR_DISABLED = "vector_disabled";
    static final String OUTCOME_CENTROID_MISSING = "centroid_missing";
    static final String OUTCOME_HYBRID = "hybrid";

    private PackSearchTelemetryPolicy() {
    }

    static SearchLatencyBreakdown buildBreakdown(
        long routeMs,
        long ftsMs,
        long keywordMs,
        long vectorMs,
        long rerankMs,
        long totalMs,
        String outcome
    ) {
        return new SearchLatencyBreakdown(
            routeMs,
            ftsMs,
            keywordMs,
            vectorMs,
            rerankMs,
            totalMs,
            outcome
        );
    }

    static OutcomeTelemetry buildOutcomeTelemetry(
        String query,
        boolean routeFocused,
        int routeSpecCount,
        int lexicalHits,
        int vectorHits,
        int routeResults,
        long routeMs,
        long ftsMs,
        long keywordMs,
        long vectorMs,
        long rerankMs,
        long totalMs,
        String outcome
    ) {
        SearchLatencyBreakdown breakdown = buildBreakdown(
            routeMs,
            ftsMs,
            keywordMs,
            vectorMs,
            rerankMs,
            totalMs,
            outcome
        );
        String summaryLine = PackQueryPipelineHelper.buildSearchSummaryLine(
            query,
            routeFocused,
            routeSpecCount,
            lexicalHits,
            vectorHits,
            routeResults,
            breakdown
        );
        String slowTripwireLine = breakdown.hasSlowStage()
            ? PackQueryPipelineHelper.buildSlowQueryTripwireDebugLine(query, breakdown)
            : "";
        return new OutcomeTelemetry(breakdown, summaryLine, slowTripwireLine);
    }

    static String buildLexicalCandidateTelemetryLine(String query, List<PackRepository.RankedChunk> hits) {
        ArrayList<String> rows = new ArrayList<>();
        if (hits != null) {
            int capped = CandidateTelemetryFormatter.limitedRowCount(hits.size());
            for (int index = 0; index < capped; index++) {
                PackRepository.RankedChunk hit = hits.get(index);
                rows.add(
                    CandidateTelemetryFormatter.formatRow(
                        index + 1,
                        hit.guideId,
                        hit.sectionHeading,
                        hit.lexicalScore,
                        hit.structureType,
                        hit.category,
                        hit.topicTags
                    )
                );
            }
        }
        return CandidateTelemetryFormatter.buildLine("lexical", query, rows);
    }

    static String buildVectorCandidateTelemetryLine(String query, List<PackRepository.RankedChunk> hits) {
        ArrayList<String> rows = new ArrayList<>();
        if (hits != null) {
            int capped = CandidateTelemetryFormatter.limitedRowCount(hits.size());
            for (int index = 0; index < capped; index++) {
                PackRepository.RankedChunk hit = hits.get(index);
                rows.add(
                    CandidateTelemetryFormatter.formatRow(
                        index + 1,
                        hit.guideId,
                        hit.sectionHeading,
                        hit.vectorScore,
                        hit.structureType,
                        hit.category,
                        hit.topicTags
                    )
                );
            }
        }
        return CandidateTelemetryFormatter.buildLine("vector", query, rows);
    }

    static final class OutcomeTelemetry {
        final SearchLatencyBreakdown breakdown;
        final String summaryLine;
        final String slowTripwireLine;

        OutcomeTelemetry(SearchLatencyBreakdown breakdown, String summaryLine, String slowTripwireLine) {
            this.breakdown = breakdown;
            this.summaryLine = summaryLine == null ? "" : summaryLine;
            this.slowTripwireLine = slowTripwireLine == null ? "" : slowTripwireLine;
        }
    }
}
