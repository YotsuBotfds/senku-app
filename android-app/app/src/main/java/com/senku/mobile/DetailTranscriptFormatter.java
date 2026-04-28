package com.senku.mobile;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DetailTranscriptFormatter {
    private static final Pattern SIMPLE_GUIDE_ID_PATTERN = Pattern.compile("GD-\\d{3}");

    interface TurnAnchorResolver {
        String resolve(SessionMemory.TurnSnapshot turn);
    }

    private DetailTranscriptFormatter() {}

    static String buildTranscriptExportText(
        String currentTitle,
        List<SessionMemory.TurnSnapshot> recentTurns,
        SessionMemory.TurnSnapshot currentTurn,
        TurnAnchorResolver turnAnchorResolver
    ) {
        ArrayList<SessionMemory.TurnSnapshot> transcriptTurns = new ArrayList<>();
        if (recentTurns != null) {
            transcriptTurns.addAll(recentTurns);
        }
        if (isExportableTurn(currentTurn) && !hasMatchingTrailingTurn(transcriptTurns, currentTurn, turnAnchorResolver)) {
            transcriptTurns.add(currentTurn);
        }
        if (transcriptTurns.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Senku transcript");
        String transcriptTitle = safe(currentTitle).trim();
        if (!transcriptTitle.isEmpty()) {
            builder.append("\nTopic: ").append(transcriptTitle);
        }

        String previousAnchorGuideId = "";
        for (int index = 0; index < transcriptTurns.size(); index++) {
            SessionMemory.TurnSnapshot turn = transcriptTurns.get(index);
            appendTranscriptTurn(builder, turn, previousAnchorGuideId, turnAnchorResolver, index + 1);
            String anchorGuideId = resolveAnchorGuideId(turn, turnAnchorResolver);
            if (!anchorGuideId.isEmpty()) {
                previousAnchorGuideId = anchorGuideId;
            }
        }

        return builder.toString().trim();
    }

    private static void appendTranscriptTurn(
        StringBuilder builder,
        SessionMemory.TurnSnapshot turn,
        String previousAnchorGuideId,
        TurnAnchorResolver turnAnchorResolver,
        int turnNumber
    ) {
        if (builder.length() > 0) {
            builder.append("\n\n");
        }

        int safeTurnNumber = Math.max(1, turnNumber);
        builder.append("Q").append(safeTurnNumber).append(": ")
            .append(safe(turn == null ? null : turn.question).trim());

        String answerText = DetailThreadHistoryRenderer.leadTranscriptAnswer(
            safe(turn == null ? null : turn.answerSummary)
        );
        if (!answerText.isEmpty()) {
            builder.append("\nA").append(safeTurnNumber);
            String answerMeta = buildAnswerMeta(turn, turnAnchorResolver);
            if (!answerMeta.isEmpty()) {
                builder.append(" [").append(answerMeta).append("]");
            }
            builder.append(": ").append(answerText);
        }
    }

    private static String buildAnswerMeta(
        SessionMemory.TurnSnapshot turn,
        TurnAnchorResolver turnAnchorResolver
    ) {
        String anchorGuideId = resolveAnchorGuideId(turn, turnAnchorResolver);
        StringBuilder builder = new StringBuilder();
        List<String> guideIds = guideIdsForTurn(turn, turnAnchorResolver);
        String anchorLabel = DetailThreadHistoryRenderer.compactAnchorLabel(guideIds);
        if (!anchorLabel.isEmpty()) {
            builder.append(anchorLabel);
        } else if (!anchorGuideId.isEmpty()) {
            builder.append(anchorGuideId);
        }
        String status = statusForTurn(turn);
        if (!status.isEmpty()) {
            if (builder.length() > 0) {
                builder.append(" \u00B7 ");
            }
            builder.append(compactStatusLabel(status));
        }
        return builder.toString();
    }

    private static boolean hasMatchingTrailingTurn(
        List<SessionMemory.TurnSnapshot> turns,
        SessionMemory.TurnSnapshot candidate,
        TurnAnchorResolver turnAnchorResolver
    ) {
        if (turns == null || turns.isEmpty() || candidate == null) {
            return false;
        }

        SessionMemory.TurnSnapshot last = turns.get(turns.size() - 1);
        if (!safe(last.question).trim().equals(safe(candidate.question).trim())) {
            return false;
        }

        String lastAnchor = resolveAnchorGuideId(last, turnAnchorResolver);
        String candidateAnchor = resolveAnchorGuideId(candidate, turnAnchorResolver);
        if (!lastAnchor.equals(candidateAnchor)) {
            return false;
        }

        return safe(last.answerSummary).trim().equals(safe(candidate.answerSummary).trim())
            || !lastAnchor.isEmpty()
            || (!last.sources.isEmpty() && !candidate.sources.isEmpty() && safe(last.sources.get(0)).equals(safe(candidate.sources.get(0))));
    }

    private static boolean isExportableTurn(SessionMemory.TurnSnapshot turn) {
        if (turn == null) {
            return false;
        }
        return !safe(turn.question).trim().isEmpty()
            || !safe(turn.answerSummary).trim().isEmpty()
            || (turn.sources != null && !turn.sources.isEmpty());
    }

    private static String resolveAnchorGuideId(
        SessionMemory.TurnSnapshot turn,
        TurnAnchorResolver turnAnchorResolver
    ) {
        return turnAnchorResolver == null ? "" : safe(turnAnchorResolver.resolve(turn)).trim();
    }

    private static List<String> guideIdsForTurn(
        SessionMemory.TurnSnapshot turn,
        TurnAnchorResolver turnAnchorResolver
    ) {
        LinkedHashSet<String> guideIds = new LinkedHashSet<>();
        String anchorGuideId = resolveAnchorGuideId(turn, turnAnchorResolver);
        if (!anchorGuideId.isEmpty()) {
            guideIds.add(anchorGuideId);
        }
        if (turn != null && turn.sourceResults != null) {
            for (SearchResult source : turn.sourceResults) {
                String guideId = safe(source == null ? null : source.guideId).trim();
                if (!guideId.isEmpty()) {
                    guideIds.add(guideId);
                }
            }
        }
        if (turn != null && turn.sources != null) {
            for (String sourceLabel : turn.sources) {
                Matcher matcher = SIMPLE_GUIDE_ID_PATTERN.matcher(safe(sourceLabel));
                while (matcher.find()) {
                    guideIds.add(matcher.group());
                }
            }
        }
        return new ArrayList<>(guideIds);
    }

    private static String statusForTurn(SessionMemory.TurnSnapshot turn) {
        if (turn == null) {
            return "";
        }
        if (ReviewedCardMetadata.normalize(turn.reviewedCardMetadata).isPresent()) {
            return "confident";
        }
        if (!safe(turn.ruleId).trim().isEmpty()) {
            return "confident";
        }
        return safe(turn.answerSummary).trim().isEmpty() ? "" : "unsure";
    }

    private static String compactStatusLabel(String status) {
        String resolvedStatus = safe(status).trim().toLowerCase(java.util.Locale.US);
        if ("source-backed".equals(resolvedStatus)
            || "reviewed".equals(resolvedStatus)
            || "confident".equals(resolvedStatus)) {
            return "CONFIDENT";
        }
        if ("ready".equals(resolvedStatus) || "unsure".equals(resolvedStatus)) {
            return "UNSURE";
        }
        return resolvedStatus.toUpperCase(java.util.Locale.US);
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
