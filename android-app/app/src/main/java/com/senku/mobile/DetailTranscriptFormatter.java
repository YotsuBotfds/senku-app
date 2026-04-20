package com.senku.mobile;

import java.util.ArrayList;
import java.util.List;

final class DetailTranscriptFormatter {
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
        for (SessionMemory.TurnSnapshot turn : transcriptTurns) {
            appendTranscriptTurn(builder, turn, previousAnchorGuideId, turnAnchorResolver);
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
        TurnAnchorResolver turnAnchorResolver
    ) {
        if (builder.length() > 0) {
            builder.append("\n\n");
        }

        builder.append("You: ").append(safe(turn == null ? null : turn.question).trim());

        String answerText = safe(turn == null ? null : turn.answerSummary).trim();
        if (!answerText.isEmpty()) {
            builder.append("\nSenku: ").append(answerText);
        }

        String anchorGuideId = resolveAnchorGuideId(turn, turnAnchorResolver);
        if (!anchorGuideId.isEmpty()) {
            if (!previousAnchorGuideId.isEmpty() && !previousAnchorGuideId.equals(anchorGuideId)) {
                builder.append("\nAnchor shift: ").append(previousAnchorGuideId).append(" -> ").append(anchorGuideId);
            } else {
                builder.append("\nAnchor: ").append(anchorGuideId);
            }
        }

        if (turn != null && turn.sources != null && !turn.sources.isEmpty()) {
            builder.append("\nSources: ").append(String.join(", ", turn.sources));
        }
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

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
