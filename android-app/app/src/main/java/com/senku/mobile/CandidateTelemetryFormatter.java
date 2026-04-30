package com.senku.mobile;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class CandidateTelemetryFormatter {
    private static final Locale TELEMETRY_LOCALE = Locale.US;
    private static final int TOP_ROW_LIMIT = 20;
    private static final int FALLBACK_ROW_LIMIT = 10;
    private static final int QUERY_LIMIT = 120;
    private static final int SECTION_LIMIT = 40;
    private static final int TOPIC_LIMIT = 60;
    private static final int MAX_LINE_LENGTH = 4096;

    private CandidateTelemetryFormatter() {
    }

    static int limitedRowCount(int available) {
        return Math.min(TOP_ROW_LIMIT, Math.max(available, 0));
    }

    static int limitedRowCount(int available, int requestedLimit) {
        if (requestedLimit <= 0) {
            return limitedRowCount(available);
        }
        return Math.min(Math.min(requestedLimit, TOP_ROW_LIMIT), Math.max(available, 0));
    }

    static String buildLine(String stage, String query, List<String> rows) {
        int available = rows == null ? 0 : rows.size();
        if (available == 0) {
            return composeLine(stage, query, Collections.emptyList(), false);
        }

        int rowCount = Math.min(TOP_ROW_LIMIT, available);
        String line = composeLine(stage, query, rows.subList(0, rowCount), false);
        if (line.length() <= MAX_LINE_LENGTH) {
            return line;
        }

        rowCount = Math.min(FALLBACK_ROW_LIMIT, available);
        while (rowCount > 0) {
            line = composeLine(stage, query, rows.subList(0, rowCount), true);
            if (line.length() <= MAX_LINE_LENGTH) {
                return line;
            }
            rowCount -= 1;
        }
        return composeLine(stage, query, Collections.emptyList(), true);
    }

    static String formatRow(int rank, SearchResult result, double score) {
        return formatRow(
            rank,
            result == null ? "" : result.guideId,
            result == null ? "" : result.sectionHeading,
            score,
            result == null ? "" : result.structureType,
            result == null ? "" : result.category,
            result == null ? "" : result.topicTags
        );
    }

    static String formatRerankedRow(
        int rank,
        SearchResult result,
        double finalScore,
        double baseScore,
        int metadataBonus
    ) {
        return formatRow(rank, result, finalScore) +
            "|" + formatNumber(baseScore) +
            "|" + metadataBonus +
            "|" + formatNumber(finalScore);
    }

    static String formatRow(
        int rank,
        String guideId,
        String sectionHeading,
        double score,
        String structureType,
        String category,
        String topicTags
    ) {
        return rank +
            "|" + escapeField(guideId, 24) +
            "|" + escapeField(sectionHeading, SECTION_LIMIT) +
            "|" + formatNumber(score) +
            "|" + escapeField(structureType, 24) +
            "|" + escapeField(category, 24) +
            "|" + escapeField(topicTags, TOPIC_LIMIT);
    }

    static String formatNumber(double value) {
        if (!Double.isFinite(value)) {
            return "";
        }
        return String.format(TELEMETRY_LOCALE, "%.3f", value);
    }

    private static String composeLine(String stage, String query, List<String> rows, boolean truncated) {
        StringBuilder builder = new StringBuilder();
        builder.append("search.candidates.")
            .append(emptySafe(stage).trim())
            .append(" query=\"")
            .append(escapeQuery(query))
            .append("\" n=")
            .append(rows == null ? 0 : rows.size());
        if (truncated) {
            builder.append(" truncated=true");
        }
        builder.append(" rows=[");
        if (rows != null) {
            for (int index = 0; index < rows.size(); index++) {
                if (index > 0) {
                    builder.append(" || ");
                }
                builder.append(rows.get(index));
            }
        }
        builder.append("]");
        return builder.toString();
    }

    private static String escapeQuery(String query) {
        return escapeValue(query, QUERY_LIMIT, true);
    }

    private static String escapeField(String value, int limit) {
        return escapeValue(value, limit, false);
    }

    private static String escapeValue(String value, int limit, boolean escapeQuotes) {
        String safe = clip(value, limit)
            .replace("\\", "\\\\")
            .replace("|", "\\|")
            .replace("]", "\\]");
        if (escapeQuotes) {
            safe = safe.replace("\"", "\\\"");
        }
        return safe;
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
}
