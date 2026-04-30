package com.senku.mobile;

import java.util.Locale;

final class PackQueryPipelineHelper {
    private static final Locale QUERY_LOCALE = Locale.US;
    private static final long SEARCH_ROUTE_BUDGET_MS = 80L;
    private static final long SEARCH_FTS_BUDGET_MS = 120L;
    private static final long SEARCH_KEYWORD_BUDGET_MS = 120L;
    private static final long SEARCH_VECTOR_BUDGET_MS = 180L;
    private static final long SEARCH_RERANK_BUDGET_MS = 60L;
    private static final long SEARCH_TOTAL_BUDGET_MS = 280L;

    private PackQueryPipelineHelper() {
    }

    static SearchResult mapGuideRow(
        String title,
        String guideId,
        String category,
        String difficulty,
        String description,
        String body,
        String contentRole,
        String timeHorizon,
        String structureType,
        String topicTags
    ) {
        String safeGuideId = emptySafe(guideId);
        String safeCategory = emptySafe(category);
        return new SearchResult(
            emptySafe(title),
            safeGuideId + " | " + safeCategory + " | " + emptySafe(difficulty),
            clip(description, 180),
            emptySafe(body),
            safeGuideId,
            "",
            safeCategory,
            "guide",
            emptySafe(contentRole),
            emptySafe(timeHorizon),
            emptySafe(structureType),
            emptySafe(topicTags)
        );
    }

    static SearchResult mapChunkResult(
        String guideTitle,
        String guideId,
        String sectionHeading,
        String category,
        String document,
        String retrievalMode,
        String contentRole,
        String timeHorizon,
        String structureType,
        String topicTags
    ) {
        String safeGuideId = emptySafe(guideId);
        String safeCategory = emptySafe(category);
        String safeSection = emptySafe(sectionHeading);
        String safeRetrievalMode = emptySafe(retrievalMode);
        String safeDocument = emptySafe(document);
        return new SearchResult(
            emptySafe(guideTitle),
            safeGuideId + " | " + safeCategory + " | " + safeSection + " | " + safeRetrievalMode,
            clip(safeDocument, 220),
            safeDocument,
            safeGuideId,
            safeSection,
            safeCategory,
            safeRetrievalMode,
            emptySafe(contentRole),
            emptySafe(timeHorizon),
            emptySafe(structureType),
            emptySafe(topicTags)
        );
    }

    static String guideSectionKey(String guideId, String guideTitle, String sectionHeading) {
        String base = emptySafe(guideId).trim();
        if (base.isEmpty()) {
            base = emptySafe(guideTitle).trim();
        }
        String section = emptySafe(sectionHeading).trim();
        if (section.isEmpty()) {
            return base.toLowerCase(QUERY_LOCALE);
        }
        return (base + "::" + section).toLowerCase(QUERY_LOCALE);
    }

    static String retrievalModeForRanks(int lexicalRank, int vectorRank) {
        if (lexicalRank != Integer.MAX_VALUE && vectorRank != Integer.MAX_VALUE) {
            return "hybrid";
        }
        if (vectorRank != Integer.MAX_VALUE) {
            return "vector";
        }
        return "lexical";
    }

    static String buildSearchSummaryLine(
        String query,
        boolean routeFocused,
        int routeSpecCount,
        int lexicalHits,
        int vectorHits,
        int routeResults,
        SearchLatencyBreakdown breakdown
    ) {
        SearchLatencyBreakdown safeBreakdown = breakdown == null
            ? new SearchLatencyBreakdown(0L, 0L, 0L, 0L, 0L, 0L, "unknown")
            : breakdown;
        return "search query=\"" + query + "\" routeFocused=" + routeFocused +
            " routeSpecs=" + routeSpecCount +
            " routeMs=" + safeBreakdown.routeMs +
            " ftsMs=" + safeBreakdown.ftsMs +
            " keywordMs=" + safeBreakdown.keywordMs +
            " vectorMs=" + safeBreakdown.vectorMs +
            " rerankMs=" + safeBreakdown.rerankMs +
            " lexicalHits=" + lexicalHits +
            " vectorHits=" + vectorHits +
            " routeResults=" + routeResults +
            " fallback=" + safeBreakdown.fallbackMode +
            " totalMs=" + safeBreakdown.totalMs;
    }

    static String buildSlowQueryTripwireDebugLine(String query, SearchLatencyBreakdown breakdown) {
        if (breakdown == null) {
            return "search.slow query=\"" + query + "\" stage=unknown";
        }
        return "search.slow query=\"" + query + "\" stage=" + breakdown.firstSlowStage() +
            " fallback=" + breakdown.fallbackMode +
            " routeMs=" + breakdown.routeMs +
            " ftsMs=" + breakdown.ftsMs +
            " keywordMs=" + breakdown.keywordMs +
            " vectorMs=" + breakdown.vectorMs +
            " rerankMs=" + breakdown.rerankMs +
            " totalMs=" + breakdown.totalMs;
    }

    static String buildRerankTimingDebugLine(
        String query,
        int topK,
        int chunkCount,
        int selectedCount,
        long elapsedNanos
    ) {
        double totalMs = Math.max(0L, elapsedNanos) / 1_000_000.0;
        double avgMsPerChunk = chunkCount <= 0 ? 0.0 : totalMs / chunkCount;
        return String.format(
            QUERY_LOCALE,
            "search.rerank query=\"%s\" topK=%d chunks=%d selected=%d totalRerankMs=%.3f avgRerankMsPerChunk=%.3f",
            emptySafe(query),
            Math.max(topK, 0),
            Math.max(chunkCount, 0),
            Math.max(selectedCount, 0),
            totalMs,
            avgMsPerChunk
        );
    }

    private static String clip(String text, int limit) {
        String safe = emptySafe(text).replaceAll("\\s+", " ").trim();
        if (safe.length() <= limit) {
            return safe;
        }
        return safe.substring(0, Math.max(0, limit - 3)).trim() + "...";
    }

    private static String emptySafe(String text) {
        return text == null ? "" : text;
    }

    static final class SearchLatencyBreakdown {
        final long routeMs;
        final long ftsMs;
        final long keywordMs;
        final long vectorMs;
        final long rerankMs;
        final long totalMs;
        final String fallbackMode;

        SearchLatencyBreakdown(
            long routeMs,
            long ftsMs,
            long keywordMs,
            long vectorMs,
            long rerankMs,
            long totalMs,
            String fallbackMode
        ) {
            this.routeMs = Math.max(0L, routeMs);
            this.ftsMs = Math.max(0L, ftsMs);
            this.keywordMs = Math.max(0L, keywordMs);
            this.vectorMs = Math.max(0L, vectorMs);
            this.rerankMs = Math.max(0L, rerankMs);
            this.totalMs = Math.max(0L, totalMs);
            this.fallbackMode = emptySafe(fallbackMode).trim();
        }

        boolean hasSlowStage() {
            return !firstSlowStage().isEmpty();
        }

        String firstSlowStage() {
            if (routeMs > SEARCH_ROUTE_BUDGET_MS) {
                return "route";
            }
            if (ftsMs > SEARCH_FTS_BUDGET_MS) {
                return "fts";
            }
            if (keywordMs > SEARCH_KEYWORD_BUDGET_MS) {
                return "keyword";
            }
            if (vectorMs > SEARCH_VECTOR_BUDGET_MS) {
                return "vector";
            }
            if (rerankMs > SEARCH_RERANK_BUDGET_MS) {
                return "rerank";
            }
            if (totalMs > SEARCH_TOTAL_BUDGET_MS) {
                return "total";
            }
            return "";
        }
    }
}
