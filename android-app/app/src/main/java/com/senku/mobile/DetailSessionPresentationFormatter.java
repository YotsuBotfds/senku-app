package com.senku.mobile;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DetailSessionPresentationFormatter {
    private static final Pattern SIMPLE_GUIDE_ID_PATTERN = Pattern.compile("GD-\\d{3}");

    private final Context context;

    DetailSessionPresentationFormatter(Context context) {
        this.context = context;
    }

    SessionMemory.TurnSnapshot currentTurnSnapshot(
        String currentTitle,
        String currentBody,
        List<SearchResult> currentSources,
        String currentRuleId,
        ReviewedCardMetadata reviewedCardMetadata,
        long timestampMs
    ) {
        ArrayList<String> sourceLabels = new ArrayList<>();
        ArrayList<SearchResult> sourceResults = new ArrayList<>();
        if (currentSources != null) {
            for (SearchResult source : currentSources) {
                String guideId = safe(source == null ? null : source.guideId).trim();
                String title = safe(source == null ? null : source.title).trim();
                sourceLabels.add(guideId.isEmpty() ? title : guideId);
                if (source != null) {
                    sourceResults.add(source);
                }
            }
        }
        return new SessionMemory.TurnSnapshot(
            safe(currentTitle),
            safe(currentBody),
            safe(currentBody),
            sourceLabels,
            sourceResults,
            safe(currentRuleId),
            ReviewedCardMetadata.normalize(reviewedCardMetadata),
            null,
            timestampMs
        );
    }

    List<SessionMemory.TurnSnapshot> earlierTurns(List<SessionMemory.TurnSnapshot> snapshots, String currentTitle) {
        if (snapshots == null || snapshots.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<SessionMemory.TurnSnapshot> turns = new ArrayList<>(snapshots);
        int lastIndex = turns.size() - 1;
        SessionMemory.TurnSnapshot last = turns.get(lastIndex);
        if (safe(last == null ? null : last.question).trim().equalsIgnoreCase(safe(currentTitle).trim())) {
            turns.remove(lastIndex);
        }
        return turns;
    }

    String buildThreadSummary(int earlierCount, int sourceCount) {
        int turnCount = Math.max(1, earlierCount + 1);
        return buildThreadContextLabel(turnCount, "");
    }

    String buildSessionSummaryText(
        List<SessionMemory.TurnSnapshot> earlierTurns,
        SessionMemory.TurnSnapshot currentTurn,
        boolean stationMode,
        int sourceCount
    ) {
        int turnCount = Math.max(1, (earlierTurns == null ? 0 : earlierTurns.size()) + 1);
        return buildThreadContextLabel(turnCount, primaryAnchorGuideId(earlierTurns, currentTurn));
    }

    String buildCurrentAnchorShiftSummary(
        List<SessionMemory.TurnSnapshot> earlierTurns,
        SessionMemory.TurnSnapshot currentTurn
    ) {
        String currentAnchorGuideId = primaryGuideIdForTurn(currentTurn);
        if (currentAnchorGuideId.isEmpty() || earlierTurns == null || earlierTurns.isEmpty()) {
            return "";
        }
        String previousAnchorGuideId = primaryGuideIdForTurn(earlierTurns.get(earlierTurns.size() - 1));
        if (previousAnchorGuideId.isEmpty() || previousAnchorGuideId.equals(currentAnchorGuideId)) {
            return "";
        }
        return context.getString(R.string.detail_thread_anchor_shift, previousAnchorGuideId, currentAnchorGuideId);
    }

    String buildAnchorText(String guideId, boolean wideLayout) {
        String resolvedGuideId = safe(guideId).trim();
        return resolvedGuideId.isEmpty()
            ? context.getString(R.string.detail_anchor_unknown)
            : context.getString(
                wideLayout ? R.string.detail_anchor_guide : R.string.detail_anchor_guide_short,
                resolvedGuideId
            );
    }

    String resolvePrimaryGuideId(List<SearchResult> currentSources, String currentSubtitle) {
        if (currentSources != null && !currentSources.isEmpty()) {
            String guideId = safe(currentSources.get(0) == null ? null : currentSources.get(0).guideId).trim();
            if (!guideId.isEmpty()) {
                return guideId;
            }
        }
        String subtitle = safe(currentSubtitle);
        int markerStart = subtitle.indexOf("[");
        int markerEnd = subtitle.indexOf("]");
        if (markerStart >= 0 && markerEnd > markerStart) {
            return subtitle.substring(markerStart + 1, markerEnd).trim();
        }
        return "";
    }

    String buildCompactHeaderTitle(String guideId, String primaryLabel) {
        String resolvedGuideId = safe(guideId).trim();
        String resolvedPrimaryLabel = safe(primaryLabel).trim();
        if (!resolvedGuideId.isEmpty() && !resolvedPrimaryLabel.isEmpty()) {
            return "Entry " + resolvedGuideId + " | " + resolvedPrimaryLabel;
        }
        if (!resolvedGuideId.isEmpty()) {
            return "Entry " + resolvedGuideId;
        }
        if (!resolvedPrimaryLabel.isEmpty()) {
            return "Field lead | " + resolvedPrimaryLabel;
        }
        return context.getString(R.string.detail_thread_title);
    }

    String buildAnchorLineText(SessionMemory.TurnSnapshot turn, String previousAnchorGuideId) {
        String anchorGuideId = primaryGuideIdForTurn(turn);
        if (anchorGuideId.isEmpty()) {
            return "";
        }
        String previous = safe(previousAnchorGuideId).trim();
        return previous.isEmpty() || previous.equals(anchorGuideId)
            ? context.getString(R.string.detail_thread_anchor_line, anchorGuideId)
            : context.getString(R.string.detail_thread_anchor_shift, previous, anchorGuideId);
    }

    OfflineAnswerEngine.AnswerMode reopenedAnswerMode(SessionMemory.TurnSnapshot turn) {
        return hasReviewedCardMetadata(turn) ? OfflineAnswerEngine.AnswerMode.CONFIDENT : null;
    }

    OfflineAnswerEngine.ConfidenceLabel reopenedConfidenceLabel(SessionMemory.TurnSnapshot turn) {
        return hasReviewedCardMetadata(turn) ? OfflineAnswerEngine.ConfidenceLabel.HIGH : null;
    }

    String primaryGuideIdForTurn(SessionMemory.TurnSnapshot turn) {
        if (turn == null) {
            return "";
        }
        if (turn.sourceResults != null) {
            for (SearchResult source : turn.sourceResults) {
                String guideId = safe(source == null ? null : source.guideId).trim();
                if (!guideId.isEmpty()) {
                    return guideId;
                }
            }
        }
        if (turn.sources != null) {
            for (String sourceLabel : turn.sources) {
                Matcher matcher = SIMPLE_GUIDE_ID_PATTERN.matcher(safe(sourceLabel));
                if (matcher.find()) {
                    return matcher.group();
                }
            }
        }
        return "";
    }

    private String primaryAnchorGuideId(
        List<SessionMemory.TurnSnapshot> earlierTurns,
        SessionMemory.TurnSnapshot currentTurn
    ) {
        String currentAnchorGuideId = primaryGuideIdForTurn(currentTurn);
        if (!currentAnchorGuideId.isEmpty()) {
            return currentAnchorGuideId;
        }
        if (earlierTurns == null) {
            return "";
        }
        for (int index = earlierTurns.size() - 1; index >= 0; index--) {
            String anchorGuideId = primaryGuideIdForTurn(earlierTurns.get(index));
            if (!anchorGuideId.isEmpty()) {
                return anchorGuideId;
            }
        }
        return "";
    }

    private static String buildThreadContextLabel(int turnCount, String anchorGuideId) {
        int safeTurnCount = Math.max(1, turnCount);
        String turnLabel = safeTurnCount == 1 ? "1 TURN" : safeTurnCount + " TURNS";
        String resolvedAnchorGuideId = safe(anchorGuideId).trim();
        if (resolvedAnchorGuideId.isEmpty()) {
            return "THREAD CONTEXT - " + turnLabel;
        }
        return "THREAD CONTEXT - " + turnLabel + " - " + resolvedAnchorGuideId + " ANCHOR";
    }

    private static boolean hasReviewedCardMetadata(SessionMemory.TurnSnapshot turn) {
        return turn != null
            && ReviewedCardMetadata.normalize(turn.reviewedCardMetadata).isPresent();
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
