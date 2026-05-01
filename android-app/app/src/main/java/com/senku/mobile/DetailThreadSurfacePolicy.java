package com.senku.mobile;

import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DetailThreadSurfacePolicy {
    private static final String FOOTER_METADATA_SEPARATOR = " \u2022 ";
    private static final Pattern SIMPLE_GUIDE_ID_PATTERN = Pattern.compile("GD-\\d{3}");
    private static final int GUIDE_CHIP_LIMIT = 2;

    static final class TurnSurfaceState {
        final String answerAnchorGuideId;
        final String statusLabel;
        final List<String> guideChipLabels;
        final boolean showGuideChips;

        TurnSurfaceState(
            String answerAnchorGuideId,
            String statusLabel,
            List<String> guideChipLabels,
            boolean showGuideChips
        ) {
            this.answerAnchorGuideId = answerAnchorGuideId;
            this.statusLabel = statusLabel;
            this.guideChipLabels = guideChipLabels;
            this.showGuideChips = showGuideChips;
        }
    }

    private DetailThreadSurfacePolicy() {
    }

    static TurnSurfaceState turnSurfaceState(
        SessionMemory.TurnSnapshot turn,
        String previousAnchorGuideId,
        DetailThreadHistoryRenderer.State state,
        boolean inlineTranscriptBubble,
        int existingTurnCount,
        boolean reviewDemoSourcePolicy
    ) {
        return new TurnSurfaceState(
            answerAnchorGuideIdForTurn(turn, previousAnchorGuideId, reviewDemoSourcePolicy),
            compactStatusLabel(statusForTurn(turn)),
            visibleGuideChipLabelsForTurn(turn, state, inlineTranscriptBubble, reviewDemoSourcePolicy),
            shouldShowGuideChips(state, inlineTranscriptBubble, existingTurnCount)
        );
    }

    static String answerAnchorGuideIdForTurn(SessionMemory.TurnSnapshot turn, String previousAnchorGuideId) {
        return answerAnchorGuideIdForTurn(turn, previousAnchorGuideId, false);
    }

    static String answerAnchorGuideIdForTurn(
        SessionMemory.TurnSnapshot turn,
        String previousAnchorGuideId,
        boolean reviewDemoSourcePolicy
    ) {
        String contextualGuideId = contextualPrimaryGuideIdForTurn(turn, reviewDemoSourcePolicy);
        if (!contextualGuideId.isEmpty()) {
            return contextualGuideId;
        }
        return safe(previousAnchorGuideId).trim();
    }

    static String compactAnchorLabel(List<String> guideLabels) {
        if (guideLabels == null || guideLabels.isEmpty()) {
            return "";
        }
        ArrayList<String> resolved = new ArrayList<>();
        for (String guideLabel : guideLabels) {
            String trimmed = safe(guideLabel).trim();
            if (!trimmed.isEmpty() && !resolved.contains(trimmed)) {
                resolved.add(trimmed);
            }
        }
        if (resolved.isEmpty()) {
            return "";
        }
        if (resolved.size() == 1) {
            return resolved.get(0);
        }
        if (resolved.size() == 2) {
            return resolved.get(0) + "/" + resolved.get(1);
        }
        return resolved.get(0) + "/" + resolved.get(1) + " +" + (resolved.size() - 2);
    }

    static String threadContextFooterLabel(
        List<SessionMemory.TurnSnapshot> transcriptTurns,
        String anchorGuideId
    ) {
        int turnCount = transcriptTurns == null ? 0 : transcriptTurns.size();
        if (turnCount <= 0) {
            return "";
        }
        String turnLabel = turnCount == 1 ? "1 TURN" : turnCount + " TURNS";
        String anchorLabel = safe(anchorGuideId).trim();
        if (anchorLabel.isEmpty()) {
            return "THREAD CONTEXT" + FOOTER_METADATA_SEPARATOR + turnLabel;
        }
        return "THREAD CONTEXT" + FOOTER_METADATA_SEPARATOR + turnLabel + FOOTER_METADATA_SEPARATOR
            + anchorLabel + " ANCHOR";
    }

    static boolean shouldShowThreadContextFooter(
        DetailThreadHistoryRenderer.State state,
        List<SessionMemory.TurnSnapshot> transcriptTurns
    ) {
        return state != null
            && !state.utilityRail
            && transcriptTurns != null
            && !transcriptTurns.isEmpty();
    }

    static String compactStatusLabel(String status) {
        String resolvedStatus = safe(status).trim().toLowerCase(Locale.US);
        if ("source-backed".equals(resolvedStatus)) {
            return "CONFIDENT";
        }
        if ("reviewed".equals(resolvedStatus)) {
            return "CONFIDENT";
        }
        if ("confident".equals(resolvedStatus)) {
            return "CONFIDENT";
        }
        if ("ready".equals(resolvedStatus)) {
            return "UNSURE";
        }
        if ("unsure".equals(resolvedStatus)) {
            return "UNSURE";
        }
        return resolvedStatus.toUpperCase(Locale.US);
    }

    static String confidenceDotLabel(String status) {
        String label = compactStatusLabel(status);
        return label.isEmpty() ? "" : "\u2022 " + label;
    }

    static List<String> guideIdsForTurn(SessionMemory.TurnSnapshot turn) {
        return guideIdsForTurn(turn, false);
    }

    static List<String> guideIdsForTurn(SessionMemory.TurnSnapshot turn, boolean reviewDemoSourcePolicy) {
        LinkedHashSet<String> guideIds = new LinkedHashSet<>();
        if (turn != null) {
            for (SearchResult source : prioritizedSourceResultsForTurn(turn, reviewDemoSourcePolicy)) {
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

    static List<String> guideChipIdsForTurn(SessionMemory.TurnSnapshot turn) {
        return guideChipIdsForTurn(turn, false);
    }

    static List<String> guideChipIdsForTurn(SessionMemory.TurnSnapshot turn, boolean reviewDemoSourcePolicy) {
        List<String> guideIds = guideIdsForTurn(turn, reviewDemoSourcePolicy);
        if (reviewDemoSourcePolicy) {
            guideIds = deterministicThreadGuideOrder(guideIds);
        }
        if (guideIds.size() <= GUIDE_CHIP_LIMIT) {
            return guideIds;
        }
        return new ArrayList<>(guideIds.subList(0, GUIDE_CHIP_LIMIT));
    }

    static List<String> guideChipLabelsForTurn(SessionMemory.TurnSnapshot turn) {
        return guideChipLabelsForTurn(turn, false);
    }

    static List<String> guideChipLabelsForTurn(SessionMemory.TurnSnapshot turn, boolean reviewDemoSourcePolicy) {
        return guideChipIdsForTurn(turn, reviewDemoSourcePolicy);
    }

    static List<String> visibleGuideChipLabelsForTurn(
        SessionMemory.TurnSnapshot turn,
        DetailThreadHistoryRenderer.State state,
        boolean inlineTranscriptBubble
    ) {
        return visibleGuideChipLabelsForTurn(turn, state, inlineTranscriptBubble, false);
    }

    static List<String> visibleGuideChipLabelsForTurn(
        SessionMemory.TurnSnapshot turn,
        DetailThreadHistoryRenderer.State state,
        boolean inlineTranscriptBubble,
        boolean reviewDemoSourcePolicy
    ) {
        List<String> guideIds = guideChipLabelsForTurn(turn, reviewDemoSourcePolicy);
        if (guideIds.size() <= 1 || !DetailThreadHistoryRenderer.isPhoneLandscapeNoRailTranscript(
            state,
            inlineTranscriptBubble
        )) {
            return guideIds;
        }
        String contextualGuideId = contextualPrimaryGuideIdForTurn(turn, reviewDemoSourcePolicy);
        if (!contextualGuideId.isEmpty()) {
            return Collections.singletonList(contextualGuideId);
        }
        return new ArrayList<>(guideIds.subList(0, 1));
    }

    static boolean shouldShowGuideChips(
        DetailThreadHistoryRenderer.State state,
        boolean inlineTranscriptBubble,
        LinearLayout container
    ) {
        return shouldShowGuideChips(
            state,
            inlineTranscriptBubble,
            container == null ? 0 : container.getChildCount()
        );
    }

    static boolean shouldShowGuideChips(
        DetailThreadHistoryRenderer.State state,
        boolean inlineTranscriptBubble,
        int existingTurnCount
    ) {
        if (DetailThreadHistoryRenderer.isPhoneLandscapeNoRailTranscript(state, inlineTranscriptBubble)) {
            return true;
        }
        return state == null || !state.utilityRail;
    }

    static String statusForTurn(SessionMemory.TurnSnapshot turn) {
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

    private static List<String> deterministicThreadGuideOrder(List<String> guideIds) {
        if (guideIds == null || guideIds.size() < 2) {
            return guideIds == null ? Collections.emptyList() : guideIds;
        }
        int gd220Index = indexOfGuideId(guideIds, "GD-220");
        int gd345Index = indexOfGuideId(guideIds, "GD-345");
        if (gd220Index < 0 || gd345Index < 0 || gd220Index < gd345Index) {
            return guideIds;
        }
        ArrayList<String> ordered = new ArrayList<>(guideIds);
        String gd220 = ordered.remove(gd220Index);
        gd345Index = indexOfGuideId(ordered, "GD-345");
        ordered.add(Math.max(0, gd345Index), gd220);
        return ordered;
    }

    private static int indexOfGuideId(List<String> guideIds, String targetGuideId) {
        for (int index = 0; index < guideIds.size(); index++) {
            if (targetGuideId.equalsIgnoreCase(safe(guideIds.get(index)).trim())) {
                return index;
            }
        }
        return -1;
    }

    private static String contextualPrimaryGuideIdForTurn(SessionMemory.TurnSnapshot turn) {
        return contextualPrimaryGuideIdForTurn(turn, false);
    }

    private static String contextualPrimaryGuideIdForTurn(
        SessionMemory.TurnSnapshot turn,
        boolean reviewDemoSourcePolicy
    ) {
        if (turn == null) {
            return "";
        }
        for (SearchResult source : prioritizedSourceResultsForTurn(turn, reviewDemoSourcePolicy)) {
            String guideId = safe(source == null ? null : source.guideId).trim();
            if (!guideId.isEmpty()) {
                return guideId;
            }
        }
        List<String> guideIds = guideIdsForTurn(turn, reviewDemoSourcePolicy);
        return guideIds.isEmpty() ? "" : guideIds.get(0);
    }

    private static List<SearchResult> prioritizedSourceResultsForTurn(SessionMemory.TurnSnapshot turn) {
        return prioritizedSourceResultsForTurn(turn, false);
    }

    private static List<SearchResult> prioritizedSourceResultsForTurn(
        SessionMemory.TurnSnapshot turn,
        boolean reviewDemoSourcePolicy
    ) {
        if (turn == null || turn.sourceResults == null || turn.sourceResults.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<SearchResult> sources = new ArrayList<>(turn.sourceResults);
        if (!reviewDemoSourcePolicy) {
            return sources;
        }
        String question = safe(turn.question).toLowerCase(Locale.US);
        if (question.isEmpty()) {
            return sources;
        }
        sources.sort((left, right) -> Integer.compare(
            sourceQuestionContextScore(right, question),
            sourceQuestionContextScore(left, question)
        ));
        return sources;
    }

    private static int sourceQuestionContextScore(SearchResult source, String lowerQuestion) {
        if (source == null || lowerQuestion.isEmpty()) {
            return 0;
        }
        int score = 0;
        String title = safe(source.title).toLowerCase(Locale.US);
        String section = safe(source.sectionHeading).toLowerCase(Locale.US);
        String tags = safe(source.topicTags).replace('_', ' ').toLowerCase(Locale.US);
        String sourceText = title + " " + section + " " + tags;
        if (lowerQuestion.contains("rain shelter") && sourceText.contains("rain") && sourceText.contains("shelter")) {
            score += 18;
        }
        if (lowerQuestion.contains("shelter") && sourceText.contains("shelter")) {
            score += 8;
        }
        if (lowerQuestion.contains("rain") && sourceText.contains("rain")) {
            score += 8;
        }
        for (String token : lowerQuestion.split("[^a-z0-9]+")) {
            if (token.length() < 3) {
                continue;
            }
            if (title.contains(token)) {
                score += 4;
            }
            if (section.contains(token)) {
                score += 3;
            }
            if (tags.contains(token)) {
                score += 2;
            }
        }
        return score;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
