package com.senku.mobile;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DetailThreadHistoryRenderer {
    private static final Pattern SIMPLE_GUIDE_ID_PATTERN = Pattern.compile("GD-\\d{3}");
    private static final int QUESTION_MAX_LINES = 2;
    private static final int ANSWER_MAX_LINES = 3;
    private static final int RAIL_ANSWER_MAX_LINES = 1;
    private static final int GUIDE_CHIP_LIMIT = 2;
    private static final int THREAD_ANSWER_CHAR_LIMIT = 240;
    private static final int UTILITY_RAIL_ANSWER_CHAR_LIMIT = 96;

    static final class State {
        final boolean utilityRail;
        final boolean wideLayout;
        final boolean compactPortraitSections;
        final int bubbleWidthPx;

        State(boolean utilityRail, boolean wideLayout, boolean compactPortraitSections, int bubbleWidthPx) {
            this.utilityRail = utilityRail;
            this.wideLayout = wideLayout;
            this.compactPortraitSections = compactPortraitSections;
            this.bubbleWidthPx = bubbleWidthPx;
        }
    }

    private final Context context;
    private final DetailSessionPresentationFormatter sessionFormatter;

    DetailThreadHistoryRenderer(
        Context context,
        DetailSessionPresentationFormatter sessionFormatter,
        DetailSourcePresentationFormatter sourceFormatter
    ) {
        this.context = context;
        this.sessionFormatter = sessionFormatter;
    }

    void clearHistory(LinearLayout container) {
        if (container == null) {
            return;
        }
        container.removeAllViews();
        container.setVisibility(View.GONE);
    }

    void renderInlineThreadHistory(
        LinearLayout container,
        List<SessionMemory.TurnSnapshot> earlierTurns,
        SessionMemory.TurnSnapshot currentTurn,
        State state,
        UnaryOperator<String> answerFormatter
    ) {
        if (container == null) {
            return;
        }
        container.removeAllViews();
        if (earlierTurns == null || earlierTurns.isEmpty()) {
            container.setVisibility(View.GONE);
            return;
        }
        List<SessionMemory.TurnSnapshot> transcriptTurns = transcriptTurns(earlierTurns, currentTurn);
        List<SessionMemory.TurnSnapshot> visibleTurns = trimPriorTurnsForInlineHistory(transcriptTurns, state);
        container.setVisibility(View.VISIBLE);
        String previousAnchorGuideId = anchorGuideIdBeforeVisibleTurns(transcriptTurns, visibleTurns);
        int firstTurnNumber = Math.max(1, transcriptTurns.size() - visibleTurns.size() + 1);
        List<NumberedTurn> displayTurns = numberedDisplayTurnsForInlineHistory(visibleTurns, firstTurnNumber, state);
        for (NumberedTurn displayTurn : displayTurns) {
            addTurn(container, displayTurn.turn, previousAnchorGuideId, state, answerFormatter, true, displayTurn.turnNumber);
            previousAnchorGuideId = nextAnchorGuideId(previousAnchorGuideId, displayTurn.turn);
        }
    }

    void renderPriorTurnsHistory(
        LinearLayout container,
        List<SessionMemory.TurnSnapshot> earlierTurns,
        SessionMemory.TurnSnapshot currentTurn,
        State state,
        UnaryOperator<String> answerFormatter
    ) {
        if (container == null) {
            return;
        }
        container.removeAllViews();
        if (earlierTurns == null || earlierTurns.isEmpty()) {
            container.setVisibility(View.GONE);
            return;
        }
        List<SessionMemory.TurnSnapshot> transcriptTurns = transcriptTurns(earlierTurns, currentTurn);
        List<SessionMemory.TurnSnapshot> visibleTurns = trimPriorTurnsForInlineHistory(transcriptTurns, state);
        if (visibleTurns.isEmpty()) {
            container.setVisibility(View.GONE);
            return;
        }
        container.setVisibility(View.VISIBLE);
        String previousAnchorGuideId = anchorGuideIdBeforeVisibleTurns(transcriptTurns, visibleTurns);
        int firstTurnNumber = Math.max(1, transcriptTurns.size() - visibleTurns.size() + 1);
        for (int index = 0; index < visibleTurns.size(); index++) {
            SessionMemory.TurnSnapshot turn = visibleTurns.get(index);
            addTurn(container, turn, previousAnchorGuideId, state, answerFormatter, false, firstTurnNumber + index);
            previousAnchorGuideId = nextAnchorGuideId(previousAnchorGuideId, turn);
        }
    }

    static List<SessionMemory.TurnSnapshot> transcriptTurns(
        List<SessionMemory.TurnSnapshot> earlierTurns,
        SessionMemory.TurnSnapshot currentTurn
    ) {
        ArrayList<SessionMemory.TurnSnapshot> turns = new ArrayList<>();
        if (earlierTurns != null) {
            for (SessionMemory.TurnSnapshot turn : earlierTurns) {
                if (isRenderableTurn(turn)) {
                    turns.add(turn);
                }
            }
        }
        if (isRenderableTurn(currentTurn) && !hasMatchingTrailingTurn(turns, currentTurn)) {
            turns.add(currentTurn);
        }
        return turns;
    }

    private List<SessionMemory.TurnSnapshot> trimPriorTurnsForInlineHistory(
        List<SessionMemory.TurnSnapshot> earlierTurns,
        State state
    ) {
        if (earlierTurns == null || earlierTurns.isEmpty()) {
            return Collections.emptyList();
        }
        int limit = visiblePriorTurnLimit(state);
        if (earlierTurns.size() <= limit) {
            return new ArrayList<>(earlierTurns);
        }
        return new ArrayList<>(earlierTurns.subList(earlierTurns.size() - limit, earlierTurns.size()));
    }

    static int visiblePriorTurnLimit(State state) {
        if (state == null) {
            return 2;
        }
        if (state.compactPortraitSections) {
            return 2;
        }
        if (state.wideLayout || state.utilityRail) {
            return 2;
        }
        return 2;
    }

    static boolean shouldShowRecentTurnFirst(State state) {
        return state != null && state.wideLayout && !state.compactPortraitSections;
    }

    private static List<NumberedTurn> numberedDisplayTurnsForInlineHistory(
        List<SessionMemory.TurnSnapshot> visibleTurns,
        int firstTurnNumber,
        State state
    ) {
        ArrayList<NumberedTurn> numberedTurns = new ArrayList<>();
        if (visibleTurns == null || visibleTurns.isEmpty()) {
            return numberedTurns;
        }
        for (int index = 0; index < visibleTurns.size(); index++) {
            numberedTurns.add(new NumberedTurn(visibleTurns.get(index), firstTurnNumber + index));
        }
        if (shouldShowRecentTurnFirst(state)) {
            Collections.reverse(numberedTurns);
        }
        return numberedTurns;
    }

    private String anchorGuideIdBeforeVisibleTurns(
        List<SessionMemory.TurnSnapshot> earlierTurns,
        List<SessionMemory.TurnSnapshot> visibleTurns
    ) {
        if (earlierTurns == null || earlierTurns.isEmpty() || visibleTurns == null || visibleTurns.isEmpty()) {
            return "";
        }
        int hiddenCount = Math.max(0, earlierTurns.size() - visibleTurns.size());
        String previousAnchorGuideId = "";
        for (int index = 0; index < hiddenCount; index++) {
            previousAnchorGuideId = nextAnchorGuideId(previousAnchorGuideId, earlierTurns.get(index));
        }
        return previousAnchorGuideId;
    }

    private void addTurn(
        LinearLayout container,
        SessionMemory.TurnSnapshot turn,
        String previousAnchorGuideId,
        State state,
        UnaryOperator<String> answerFormatter,
        boolean inlineTranscriptBubble,
        int turnNumber
    ) {
        if (turn == null || container == null) {
            return;
        }
        LinearLayout turnRow = buildTurnRow(container, state, inlineTranscriptBubble);
        turnRow.addView(buildMetaLine(buildTurnLabel(turnNumber, true, turn, previousAnchorGuideId), true));
        turnRow.addView(buildBodyLine(turn.question, true, inlineTranscriptBubble, state.utilityRail && !inlineTranscriptBubble));

        String answerSummary = safe(turn.answerSummary).trim();
        if (answerSummary.isEmpty()) {
            container.addView(turnRow);
            return;
        }
        turnRow.addView(buildAnswerMetaRow(
            buildTurnLabel(turnNumber, false, turn, previousAnchorGuideId),
            compactStatusLabel(statusForTurn(turn))
        ));
        turnRow.addView(buildBodyLine(
            compactThreadAnswer(answerSummary, state.utilityRail, answerFormatter),
            false,
            inlineTranscriptBubble,
            state.utilityRail && !inlineTranscriptBubble
        ));

        List<String> guideLabels = guideChipLabelsForTurn(turn);
        if (!guideLabels.isEmpty() && shouldShowGuideChips(state, inlineTranscriptBubble)) {
            turnRow.addView(buildGuideChipRow(guideLabels));
        }
        container.addView(turnRow);
    }

    private LinearLayout buildTurnRow(
        LinearLayout container,
        State state,
        boolean inlineTranscriptBubble
    ) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.VERTICAL);
        boolean railRow = state.utilityRail && !inlineTranscriptBubble;
        int padH = railRow ? dp(8) : dp(10);
        int padTop = container != null && container.getChildCount() > 0 ? (railRow ? dp(8) : dp(10)) : dp(6);
        int padBottom = railRow ? dp(8) : dp(12);
        row.setPadding(padH, padTop, padH, padBottom);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            state.wideLayout ? state.bubbleWidthPx : ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        row.setLayoutParams(params);
        if (container != null && container.getChildCount() > 0) {
            row.addView(buildDivider());
        }
        return row;
    }

    private TextView buildMetaLine(String labelText, boolean userTurn) {
        TextView label = new TextView(context);
        label.setText(labelText);
        label.setTextAppearance(context, android.R.style.TextAppearance_Small);
        label.setTextColor(context.getColor(userTurn ? R.color.senku_rev03_ink_2 : R.color.senku_rev03_accent));
        label.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        label.setTextSize(11);
        label.setSingleLine(true);
        label.setEllipsize(TextUtils.TruncateAt.END);
        label.setPadding(0, 0, 0, dp(4));
        return label;
    }

    private TextView buildBodyLine(String text, boolean userTurn, boolean inlineTranscriptBubble, boolean railBubble) {
        TextView body = new TextView(context);
        body.setText(text);
        body.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        body.setTextColor(context.getColor(userTurn ? R.color.senku_rev03_ink_0 : R.color.senku_text_light));
        body.setTypeface(userTurn ? Typeface.DEFAULT_BOLD : Typeface.SERIF, userTurn ? Typeface.BOLD : Typeface.NORMAL);
        body.setTextSize(userTurn ? (railBubble ? 14 : 15) : (inlineTranscriptBubble ? 15 : 14));
        body.setLineSpacing(0f, userTurn ? 1.02f : 1.08f);
        body.setPadding(0, 0, 0, userTurn ? dp(8) : dp(4));
        if (userTurn) {
            body.setMaxLines(QUESTION_MAX_LINES);
            body.setEllipsize(TextUtils.TruncateAt.END);
        } else {
            body.setMaxLines(railBubble ? RAIL_ANSWER_MAX_LINES : ANSWER_MAX_LINES);
            body.setEllipsize(TextUtils.TruncateAt.END);
        }
        return body;
    }

    private LinearLayout buildAnswerMetaRow(String labelText, String status) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(1), 0, dp(3));

        TextView label = buildMetaLine(labelText, false);
        label.setPadding(0, 0, dp(8), 0);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(label, labelParams);

        String resolvedStatus = safe(status).trim();
        if (!resolvedStatus.isEmpty()) {
            TextView statusLabel = new TextView(context);
            statusLabel.setText("\u2022 " + compactStatusLabel(resolvedStatus));
            statusLabel.setTextAppearance(context, android.R.style.TextAppearance_Small);
            statusLabel.setTextColor(context.getColor(statusColorRes(resolvedStatus)));
            statusLabel.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
            statusLabel.setTextSize(11);
            statusLabel.setSingleLine(true);
            row.addView(statusLabel);
        }
        return row;
    }

    private LinearLayout buildGuideChipRow(List<String> guideIds) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(2), 0, 0);
        for (String guideId : guideIds) {
            String resolvedGuideId = safe(guideId).trim();
            if (resolvedGuideId.isEmpty()) {
                continue;
            }
            TextView chip = new TextView(context);
            chip.setText(resolvedGuideId);
            chip.setTextAppearance(context, android.R.style.TextAppearance_Small);
            chip.setTextColor(context.getColor(R.color.senku_rev03_ink_1));
            chip.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
            chip.setTextSize(11);
            chip.setSingleLine(true);
            chip.setBackground(buildChipBackground());
            chip.setPadding(dp(6), dp(1), dp(6), dp(1));
            LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            chipParams.setMarginEnd(dp(6));
            row.addView(chip, chipParams);
        }
        return row;
    }

    private View buildDivider() {
        View divider = new View(context);
        divider.setBackgroundColor(context.getColor(R.color.senku_rev03_hairline));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            Math.max(1, dp(1))
        );
        params.setMargins(0, 0, 0, dp(8));
        divider.setLayoutParams(params);
        return divider;
    }

    private GradientDrawable buildChipBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(context.getColor(android.R.color.transparent));
        drawable.setStroke(Math.max(1, dp(1)), context.getColor(R.color.senku_rev03_hairline_strong));
        drawable.setCornerRadius(dp(14));
        return drawable;
    }

    String buildTurnLabel(
        int turnNumber,
        boolean userTurn,
        SessionMemory.TurnSnapshot turn,
        String previousAnchorGuideId
    ) {
        int safeTurnNumber = Math.max(1, turnNumber);
        String time = timeForTurn(turn);
        if (userTurn) {
            return "Q" + safeTurnNumber + time + " \u00B7 FIELD QUESTION";
        }
        String anchorGuideId = sessionFormatter.primaryGuideIdForTurn(turn);
        StringBuilder builder = new StringBuilder("A")
            .append(safeTurnNumber)
            .append(time);
        String anchorLabel = anchorGuideId;
        if (!anchorLabel.isEmpty()) {
            builder.append(" \u00B7 ");
            builder.append("ANCHOR ");
            builder.append(anchorLabel);
        }
        return builder.toString();
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

    private String nextAnchorGuideId(String previousAnchorGuideId, SessionMemory.TurnSnapshot turn) {
        String anchorGuideId = sessionFormatter.primaryGuideIdForTurn(turn);
        return anchorGuideId.isEmpty() ? safe(previousAnchorGuideId).trim() : anchorGuideId;
    }

    static String compactThreadAnswer(String answerSummary, boolean utilityRail, UnaryOperator<String> answerFormatter) {
        String compact = answerFormatter == null ? safe(answerSummary) : safe(answerFormatter.apply(answerSummary));
        compact = leadTranscriptAnswer(compact);
        int limit = utilityRail ? UTILITY_RAIL_ANSWER_CHAR_LIMIT : THREAD_ANSWER_CHAR_LIMIT;
        if (compact.length() > limit) {
            compact = compact.substring(0, limit - 3).trim() + "...";
        }
        return compact;
    }

    static String leadTranscriptAnswer(String answerText) {
        String compact = safe(answerText).trim();
        if (compact.isEmpty()) {
            return "";
        }
        String firstCandidate = "";
        boolean preferNextBodyLine = false;
        boolean skipNextBodyLine = false;
        String[] lines = compact.split("\\r?\\n");
        for (String line : lines) {
            String candidate = safe(line).trim();
            if (candidate.isEmpty()) {
                continue;
            }
            if (isTranscriptAnswerHeading(candidate)) {
                if (isTranscriptQuestionHeading(candidate)) {
                    skipNextBodyLine = true;
                    preferNextBodyLine = false;
                }
                if (isTranscriptAnswerLeadHeading(candidate)) {
                    preferNextBodyLine = true;
                }
                if (!firstCandidate.isEmpty() && isTranscriptAnswerTrailingHeading(candidate)) {
                    break;
                }
                continue;
            }
            if (skipNextBodyLine) {
                skipNextBodyLine = false;
                continue;
            }
            if (preferNextBodyLine) {
                return candidate;
            }
            if (firstCandidate.isEmpty()) {
                firstCandidate = candidate;
            }
        }
        return firstCandidate;
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

    static List<String> guideIdsForTurn(SessionMemory.TurnSnapshot turn) {
        LinkedHashSet<String> guideIds = new LinkedHashSet<>();
        if (turn != null) {
            for (SearchResult source : prioritizedSourceResultsForTurn(turn)) {
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
        List<String> guideIds = guideIdsForTurn(turn);
        if (guideIds.size() <= GUIDE_CHIP_LIMIT) {
            return guideIds;
        }
        return new ArrayList<>(guideIds.subList(0, GUIDE_CHIP_LIMIT));
    }

    static List<String> guideChipLabelsForTurn(SessionMemory.TurnSnapshot turn) {
        return guideChipIdsForTurn(turn);
    }

    static boolean shouldShowGuideChips(State state, boolean inlineTranscriptBubble) {
        return state == null || !state.utilityRail;
    }

    private static boolean isTranscriptAnswerHeading(String line) {
        String normalized = normalizeTranscriptHeading(line);
        return normalized.equals("answer")
            || normalized.equals("short answer")
            || normalized.equals("source match")
            || normalized.equals("steps")
            || normalized.equals("field steps")
            || normalized.equals("limits safety")
            || normalized.equals("limits and safety")
            || normalized.equals("watch")
            || normalized.equals("question")
            || normalized.equals("user")
            || normalized.equals("field question")
            || normalized.startsWith("field entry")
            || normalized.startsWith("uncertain fit")
            || normalized.startsWith("boundary")
            || normalized.matches("\\d+\\..*");
    }

    private static boolean isTranscriptAnswerLeadHeading(String line) {
        String normalized = normalizeTranscriptHeading(line);
        return normalized.equals("answer")
            || normalized.equals("short answer")
            || normalized.equals("source match");
    }

    private static boolean isTranscriptQuestionHeading(String line) {
        return normalizeTranscriptHeading(line).equals("field question");
    }

    private static boolean isTranscriptAnswerTrailingHeading(String line) {
        String normalized = normalizeTranscriptHeading(line);
        return normalized.equals("steps")
            || normalized.equals("field steps")
            || normalized.equals("limits safety")
            || normalized.equals("limits and safety")
            || normalized.equals("watch")
            || normalized.startsWith("uncertain fit")
            || normalized.startsWith("boundary");
    }

    private static String normalizeTranscriptHeading(String line) {
        return safe(line)
            .trim()
            .replace('&', ' ')
            .replace(':', ' ')
            .replace('-', ' ')
            .replaceAll("\\s+", " ")
            .toLowerCase(Locale.US);
    }

    private static boolean hasMatchingTrailingTurn(
        List<SessionMemory.TurnSnapshot> turns,
        SessionMemory.TurnSnapshot candidate
    ) {
        if (turns == null || turns.isEmpty() || candidate == null) {
            return false;
        }
        SessionMemory.TurnSnapshot last = turns.get(turns.size() - 1);
        return safe(last == null ? null : last.question).trim().equals(safe(candidate.question).trim())
            && safe(last == null ? null : last.answerSummary).trim().equals(safe(candidate.answerSummary).trim());
    }

    private static boolean isRenderableTurn(SessionMemory.TurnSnapshot turn) {
        return turn != null
            && (!safe(turn.question).trim().isEmpty()
                || !safe(turn.answerSummary).trim().isEmpty()
                || (turn.sources != null && !turn.sources.isEmpty())
                || (turn.sourceResults != null && !turn.sourceResults.isEmpty()));
    }

    private static final class NumberedTurn {
        final SessionMemory.TurnSnapshot turn;
        final int turnNumber;

        NumberedTurn(SessionMemory.TurnSnapshot turn, int turnNumber) {
            this.turn = turn;
            this.turnNumber = turnNumber;
        }
    }

    private static List<SearchResult> prioritizedSourceResultsForTurn(SessionMemory.TurnSnapshot turn) {
        if (turn == null || turn.sourceResults == null || turn.sourceResults.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<SearchResult> sources = new ArrayList<>(turn.sourceResults);
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

    private String timeForTurn(SessionMemory.TurnSnapshot turn) {
        if (turn == null || turn.recordedAtEpochMs <= 0L) {
            return "";
        }
        return " \u00B7 " + new SimpleDateFormat("HH:mm", Locale.US).format(new Date(turn.recordedAtEpochMs));
    }

    private static int statusColorRes(String status) {
        String resolvedStatus = safe(status).trim().toLowerCase(Locale.US);
        if ("reviewed".equals(resolvedStatus)
            || "source-backed".equals(resolvedStatus)
            || "confident".equals(resolvedStatus)) {
            return R.color.senku_rev03_ok;
        }
        if ("ready".equals(resolvedStatus) || "unsure".equals(resolvedStatus)) {
            return R.color.senku_rev03_accent;
        }
        return R.color.senku_rev03_warn;
    }

    private int dp(int value) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
