package com.senku.mobile;

final class SearchLatencyBreakdown {
    private static final long SEARCH_ROUTE_BUDGET_MS = 80L;
    private static final long SEARCH_FTS_BUDGET_MS = 120L;
    private static final long SEARCH_KEYWORD_BUDGET_MS = 120L;
    private static final long SEARCH_VECTOR_BUDGET_MS = 180L;
    private static final long SEARCH_RERANK_BUDGET_MS = 60L;
    private static final long SEARCH_TOTAL_BUDGET_MS = 280L;

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

    private static String emptySafe(String text) {
        return text == null ? "" : text;
    }
}
