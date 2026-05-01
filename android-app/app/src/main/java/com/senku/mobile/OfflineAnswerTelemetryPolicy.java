package com.senku.mobile;

import java.util.Locale;

final class OfflineAnswerTelemetryPolicy {
    private static final Locale LOG_LOCALE = Locale.US;

    private OfflineAnswerTelemetryPolicy() {
    }

    static String buildFirstTokenLine(String query, String path, long firstTokenMs) {
        return "ask.first_token_ms=" + Math.max(0L, firstTokenMs)
            + " query=\"" + safe(query) + "\""
            + " path=" + safe(path);
    }

    static String buildLatencySummaryLine(
        String query,
        OfflineAnswerEngine.LatencyBreakdown latencyBreakdown
    ) {
        OfflineAnswerEngine.LatencyBreakdown safeBreakdown = latencyBreakdown == null
            ? new OfflineAnswerEngine.LatencyBreakdown("", 0L, 0L, 0L, 0L, 0L, 0L)
            : latencyBreakdown;
        return "ask.latency"
            + " queryClass=\"" + safe(safeBreakdown.queryClass) + "\""
            + " retrievalMs=" + safeBreakdown.retrievalMs
            + " rerankMs=" + safeBreakdown.rerankMs
            + " promptBuildMs=" + safeBreakdown.promptBuildMs
            + " firstTokenMs=" + safeBreakdown.firstTokenMs
            + " decodeMs=" + safeBreakdown.decodeMs
            + " totalMs=" + safeBreakdown.totalMs
            + " query=\"" + safe(query) + "\"";
    }

    static String buildFinalModeLine(
        String query,
        OfflineAnswerEngine.AnswerMode finalMode,
        String route,
        long totalElapsedMs
    ) {
        OfflineAnswerEngine.AnswerMode safeMode = finalMode == null
            ? OfflineAnswerEngine.AnswerMode.CONFIDENT
            : finalMode;
        return "ask.generate final_mode=" + safeMode.name().toLowerCase(LOG_LOCALE)
            + " route=" + safe(route)
            + " query=\"" + safe(query) + "\""
            + " totalElapsedMs=" + Math.max(0L, totalElapsedMs);
    }

    static FinalModeRoute resolvePreparedFinalModeRoute(OfflineAnswerEngine.PreparedAnswer prepared) {
        if (prepared == null) {
            return FinalModeRoute.empty();
        }
        if (prepared.deterministic) {
            return new FinalModeRoute(OfflineAnswerEngine.AnswerMode.CONFIDENT, "deterministic");
        }
        if (prepared.abstain) {
            return new FinalModeRoute(OfflineAnswerEngine.AnswerMode.ABSTAIN, "early_abstain");
        }
        if (prepared.mode == OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT) {
            return new FinalModeRoute(OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT, "early_uncertain_fit");
        }
        return FinalModeRoute.empty();
    }

    static final class FinalModeRoute {
        final OfflineAnswerEngine.AnswerMode finalMode;
        final String route;

        private FinalModeRoute(OfflineAnswerEngine.AnswerMode finalMode, String route) {
            this.finalMode = finalMode;
            this.route = safe(route);
        }

        boolean isReady() {
            return finalMode != null && !route.isEmpty();
        }

        private static FinalModeRoute empty() {
            return new FinalModeRoute(null, "");
        }
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
