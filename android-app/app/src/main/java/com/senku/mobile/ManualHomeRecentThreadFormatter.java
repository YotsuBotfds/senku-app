package com.senku.mobile;

import java.util.ArrayList;
import java.util.Locale;

final class ManualHomeRecentThreadFormatter {
    private ManualHomeRecentThreadFormatter() {
    }

    static String buildLabel(ChatSessionStore.ConversationPreview preview) {
        return buildLabel(preview, System.currentTimeMillis());
    }

    static String buildLabel(ChatSessionStore.ConversationPreview preview, long nowMillis) {
        SessionMemory.TurnSnapshot turn = preview == null ? null : preview.latestTurn;
        if (turn == null) {
            return "";
        }
        String question = clipQuestion(turn.question);
        String meta = buildMeta(preview, nowMillis);
        if (meta.isEmpty()) {
            return question;
        }
        return question + "\n" + meta;
    }

    static String buildMeta(ChatSessionStore.ConversationPreview preview) {
        return buildMeta(preview, System.currentTimeMillis());
    }

    static String buildMeta(ChatSessionStore.ConversationPreview preview, long nowMillis) {
        SessionMemory.TurnSnapshot turn = preview == null ? null : preview.latestTurn;
        if (turn == null) {
            return "";
        }
        ArrayList<String> parts = new ArrayList<>();
        String guideId = resolveGuideId(turn);
        if (!guideId.isEmpty()) {
            parts.add(guideId);
        }
        String timeLabel = buildTimeLabel(preview == null ? 0L : preview.lastActivityEpoch, nowMillis);
        if (!timeLabel.isEmpty()) {
            parts.add(timeLabel);
        }
        parts.add(buildConfidenceLabel(turn));
        return String.join(" \u2022 ", parts);
    }

    static String resolveGuideId(SessionMemory.TurnSnapshot turn) {
        if (turn == null || turn.sourceResults == null) {
            return "";
        }
        for (SearchResult source : turn.sourceResults) {
            String guideId = safe(source == null ? null : source.guideId).trim();
            if (!guideId.isEmpty()) {
                return guideId;
            }
        }
        return "";
    }

    static String buildTimeLabel(long recordedAtEpoch, long nowMillis) {
        if (recordedAtEpoch <= 0L) {
            return "";
        }
        long elapsedMillis = Math.max(0L, nowMillis - recordedAtEpoch);
        long totalMinutes = elapsedMillis / 60_000L;
        long totalHours = totalMinutes / 60L;
        long days = totalHours / 24L;
        if (days == 1L) {
            return "YESTERDAY";
        }
        if (days > 1L) {
            return days + "D";
        }
        return String.format(Locale.US, "%02d:%02d", totalHours, totalMinutes % 60L);
    }

    static String buildConfidenceLabel(SessionMemory.TurnSnapshot turn) {
        if (turn == null) {
            return "UNSURE";
        }
        ReviewedCardMetadata metadata = ReviewedCardMetadata.normalize(turn.reviewedCardMetadata);
        return !safe(turn.ruleId).trim().isEmpty() || metadata.isPresent() ? "CONFIDENT" : "UNSURE";
    }

    private static String clipQuestion(String question) {
        String value = safe(question).trim();
        if (value.length() <= 34) {
            return value;
        }
        return value.substring(0, 31).trim() + "...";
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
