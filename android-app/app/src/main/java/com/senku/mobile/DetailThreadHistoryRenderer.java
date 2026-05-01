package com.senku.mobile;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

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
    private static final String FOOTER_METADATA_SEPARATOR = " \u2022 ";
    private static final Pattern SIMPLE_GUIDE_ID_PATTERN = Pattern.compile("GD-\\d{3}");
    private static final int QUESTION_MAX_LINES = 2;
    private static final int ANSWER_MAX_LINES = 4;
    private static final int PHONE_LANDSCAPE_ANSWER_MAX_LINES = 1;
    private static final int RAIL_ANSWER_MAX_LINES = 1;
    private static final int GUIDE_CHIP_LIMIT = 2;
    private static final int THREAD_ANSWER_CHAR_LIMIT = 240;
    private static final int UTILITY_RAIL_ANSWER_CHAR_LIMIT = 96;

    static final class State {
        final boolean utilityRail;
        final boolean wideLayout;
        final boolean compactPortraitSections;
        final int bubbleWidthPx;
        final boolean phoneLandscapeNoRail;

        State(boolean utilityRail, boolean wideLayout, boolean compactPortraitSections, int bubbleWidthPx) {
            this(utilityRail, wideLayout, compactPortraitSections, bubbleWidthPx, false);
        }

        State(
            boolean utilityRail,
            boolean wideLayout,
            boolean compactPortraitSections,
            int bubbleWidthPx,
            boolean phoneLandscapeNoRail
        ) {
            this.utilityRail = utilityRail;
            this.wideLayout = wideLayout;
            this.compactPortraitSections = compactPortraitSections;
            this.bubbleWidthPx = bubbleWidthPx;
            this.phoneLandscapeNoRail = phoneLandscapeNoRail;
        }
    }

    private final Context context;
    private final DetailSessionPresentationFormatter sessionFormatter;
    private final boolean reviewDemoSourcePolicy;

    DetailThreadHistoryRenderer(
        Context context,
        DetailSessionPresentationFormatter sessionFormatter,
        DetailSourcePresentationFormatter sourceFormatter
    ) {
        this(context, sessionFormatter, sourceFormatter, false);
    }

    DetailThreadHistoryRenderer(
        Context context,
        DetailSessionPresentationFormatter sessionFormatter,
        DetailSourcePresentationFormatter sourceFormatter,
        boolean reviewDemoSourcePolicy
    ) {
        this.context = context;
        this.sessionFormatter = sessionFormatter;
        this.reviewDemoSourcePolicy = reviewDemoSourcePolicy;
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
        UnaryOperator<String> answerFormatter,
        boolean includeCurrentTurnInHistory
    ) {
        if (container == null) {
            return;
        }
        container.removeAllViews();
        List<SessionMemory.TurnSnapshot> transcriptTurns = transcriptTurns(earlierTurns, currentTurn);
        List<SessionMemory.TurnSnapshot> visibleTranscriptTurns = visibleHistoryTurns(
            earlierTurns,
            currentTurn,
            includeCurrentTurnInHistory
        );
        if (visibleTranscriptTurns.isEmpty()) {
            container.setVisibility(View.GONE);
            return;
        }
        List<SessionMemory.TurnSnapshot> visibleTurns = trimPriorTurnsForInlineHistory(visibleTranscriptTurns, state);
        container.setVisibility(View.VISIBLE);
        String previousAnchorGuideId = anchorGuideIdBeforeVisibleTurns(visibleTranscriptTurns, visibleTurns);
        int firstTurnNumber = firstVisibleTurnNumber(visibleTranscriptTurns, visibleTurns);
        List<NumberedTurn> displayTurns = numberedDisplayTurnsForInlineHistory(visibleTurns, firstTurnNumber, state);
        for (NumberedTurn displayTurn : displayTurns) {
            addTurn(container, displayTurn.turn, previousAnchorGuideId, state, answerFormatter, true, displayTurn.turnNumber);
            previousAnchorGuideId = nextAnchorGuideId(previousAnchorGuideId, displayTurn.turn);
        }
        addThreadContextFooter(container, transcriptTurns, state);
    }

    void renderPriorTurnsHistory(
        LinearLayout container,
        List<SessionMemory.TurnSnapshot> earlierTurns,
        SessionMemory.TurnSnapshot currentTurn,
        State state,
        UnaryOperator<String> answerFormatter,
        boolean includeCurrentTurnInHistory
    ) {
        if (container == null) {
            return;
        }
        container.removeAllViews();
        List<SessionMemory.TurnSnapshot> transcriptTurns = transcriptTurns(earlierTurns, currentTurn);
        List<SessionMemory.TurnSnapshot> visibleTranscriptTurns = visibleHistoryTurns(
            earlierTurns,
            currentTurn,
            includeCurrentTurnInHistory
        );
        List<SessionMemory.TurnSnapshot> visibleTurns = trimPriorTurnsForInlineHistory(visibleTranscriptTurns, state);
        if (visibleTurns.isEmpty()) {
            container.setVisibility(View.GONE);
            return;
        }
        container.setVisibility(View.VISIBLE);
        String previousAnchorGuideId = anchorGuideIdBeforeVisibleTurns(visibleTranscriptTurns, visibleTurns);
        int firstTurnNumber = firstVisibleTurnNumber(visibleTranscriptTurns, visibleTurns);
        for (int index = 0; index < visibleTurns.size(); index++) {
            SessionMemory.TurnSnapshot turn = visibleTurns.get(index);
            addTurn(container, turn, previousAnchorGuideId, state, answerFormatter, false, firstTurnNumber + index);
            previousAnchorGuideId = nextAnchorGuideId(previousAnchorGuideId, turn);
        }
        addThreadContextFooter(container, transcriptTurns, state);
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

    static List<SessionMemory.TurnSnapshot> visibleHistoryTurns(
        List<SessionMemory.TurnSnapshot> earlierTurns,
        SessionMemory.TurnSnapshot currentTurn,
        boolean includeCurrentTurn
    ) {
        return includeCurrentTurn
            ? transcriptTurns(earlierTurns, currentTurn)
            : priorTranscriptTurns(earlierTurns);
    }

    static List<SessionMemory.TurnSnapshot> priorTranscriptTurns(List<SessionMemory.TurnSnapshot> earlierTurns) {
        ArrayList<SessionMemory.TurnSnapshot> turns = new ArrayList<>();
        if (earlierTurns == null) {
            return turns;
        }
        for (SessionMemory.TurnSnapshot turn : earlierTurns) {
            if (isRenderableTurn(turn)) {
                turns.add(turn);
            }
        }
        return turns;
    }

    static int firstVisibleTurnNumber(
        List<SessionMemory.TurnSnapshot> priorTranscriptTurns,
        List<SessionMemory.TurnSnapshot> visibleTurns
    ) {
        int priorCount = priorTranscriptTurns == null ? 0 : priorTranscriptTurns.size();
        int visibleCount = visibleTurns == null ? 0 : visibleTurns.size();
        return Math.max(1, priorCount - visibleCount + 1);
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
        if (isPhoneLandscapeNoRailTranscript(state, true)) {
            return 1;
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
        return false;
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
        turnRow.addView(buildBodyLine(turn.question, true, inlineTranscriptBubble, state.utilityRail && !inlineTranscriptBubble, state));

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
            state.utilityRail && !inlineTranscriptBubble,
            state
        ));

        List<String> guideLabels = visibleGuideChipLabelsForTurn(
            turn,
            state,
            inlineTranscriptBubble,
            reviewDemoSourcePolicy
        );
        if (!guideLabels.isEmpty() && shouldShowGuideChips(state, inlineTranscriptBubble, container)) {
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
        boolean densePhoneLandscape = isPhoneLandscapeNoRailTranscript(state, inlineTranscriptBubble);
        int padH = railRow ? dp(6) : (state.wideLayout ? dp(8) : dp(densePhoneLandscape ? 6 : 10));
        int padTop = container != null && container.getChildCount() > 0
            ? (railRow ? dp(6) : (state.wideLayout ? dp(8) : dp(densePhoneLandscape ? 3 : 10)))
            : dp(densePhoneLandscape ? 2 : 5);
        int padBottom = railRow ? dp(6) : (state.wideLayout ? dp(9) : dp(densePhoneLandscape ? 3 : 12));
        row.setPadding(padH, padTop, padH, padBottom);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            state.wideLayout ? state.bubbleWidthPx : ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        if (state.wideLayout) {
            params.gravity = Gravity.CENTER_HORIZONTAL;
        }
        row.setLayoutParams(params);
        if (container != null && container.getChildCount() > 0) {
            row.addView(buildDivider(densePhoneLandscape));
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

    private TextView buildBodyLine(
        String text,
        boolean userTurn,
        boolean inlineTranscriptBubble,
        boolean railBubble,
        State state
    ) {
        TextView body = new TextView(context);
        body.setText(text);
        body.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        body.setTextColor(context.getColor(bodyTextColorRes(userTurn)));
        body.setTypeface(bodyTypeface(userTurn));
        body.setTextSize(bodyTextSizeSp(userTurn, inlineTranscriptBubble, railBubble, state));
        body.setLineSpacing(0f, bodyLineSpacing(userTurn, inlineTranscriptBubble, railBubble, state));
        body.setPadding(
            0,
            0,
            0,
            userTurn
                ? dp(questionBottomPaddingDp(inlineTranscriptBubble, railBubble, state))
                : dp(answerBottomPaddingDp(inlineTranscriptBubble, railBubble, state))
        );
        if (userTurn) {
            body.setMaxLines(questionMaxLines(state, inlineTranscriptBubble, railBubble));
            body.setEllipsize(TextUtils.TruncateAt.END);
        } else {
            body.setMaxLines(answerMaxLines(state, inlineTranscriptBubble));
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
            statusLabel.setText(confidenceDotLabel(resolvedStatus));
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

    private void addThreadContextFooter(
        LinearLayout container,
        List<SessionMemory.TurnSnapshot> transcriptTurns,
        State state
    ) {
        if (container == null || state == null || state.utilityRail) {
            return;
        }
        String labelText = threadContextFooterLabel(
            transcriptTurns,
            firstAnchorGuideIdForTranscript(transcriptTurns)
        );
        if (labelText.isEmpty()) {
            return;
        }
        TextView label = buildMetaLine(labelText, true);
        label.setTextColor(context.getColor(R.color.senku_rev03_ink_2));
        label.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        boolean densePhoneLandscape = isPhoneLandscapeNoRailTranscript(state, true);
        label.setPadding(
            dp(densePhoneLandscape ? 8 : 10),
            dp(densePhoneLandscape ? 2 : 4),
            dp(densePhoneLandscape ? 8 : 10),
            dp(densePhoneLandscape ? 0 : 2)
        );
        container.addView(label);
    }

    private View buildDivider(boolean densePhoneLandscape) {
        View divider = new View(context);
        divider.setBackgroundColor(context.getColor(R.color.senku_rev03_hairline));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            Math.max(1, dp(1))
        );
        params.setMargins(0, 0, 0, dp(densePhoneLandscape ? 3 : 8));
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
        StringBuilder builder = new StringBuilder("A")
            .append(safeTurnNumber)
            .append(time);
        String anchorGuideId = answerAnchorGuideIdForTurn(turn, previousAnchorGuideId, reviewDemoSourcePolicy);
        if (!anchorGuideId.isEmpty()) {
            builder.append(" \u00B7 ANCHOR ").append(anchorGuideId);
        } else {
            builder.append(" \u00B7 ANSWER");
        }
        return builder.toString();
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

    private String nextAnchorGuideId(String previousAnchorGuideId, SessionMemory.TurnSnapshot turn) {
        String anchorGuideId = sessionFormatter.primaryGuideIdForTurn(turn);
        return anchorGuideId.isEmpty() ? safe(previousAnchorGuideId).trim() : anchorGuideId;
    }

    private String firstAnchorGuideIdForTranscript(List<SessionMemory.TurnSnapshot> transcriptTurns) {
        if (transcriptTurns == null) {
            return "";
        }
        for (SessionMemory.TurnSnapshot turn : transcriptTurns) {
            String anchorGuideId = sessionFormatter.primaryGuideIdForTurn(turn);
            if (!anchorGuideId.isEmpty()) {
                return anchorGuideId;
            }
        }
        return "";
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
        State state,
        boolean inlineTranscriptBubble
    ) {
        return visibleGuideChipLabelsForTurn(turn, state, inlineTranscriptBubble, false);
    }

    static List<String> visibleGuideChipLabelsForTurn(
        SessionMemory.TurnSnapshot turn,
        State state,
        boolean inlineTranscriptBubble,
        boolean reviewDemoSourcePolicy
    ) {
        List<String> guideIds = guideChipLabelsForTurn(turn, reviewDemoSourcePolicy);
        if (guideIds.size() <= 1 || !isPhoneLandscapeNoRailTranscript(state, inlineTranscriptBubble)) {
            return guideIds;
        }
        String contextualGuideId = contextualPrimaryGuideIdForTurn(turn, reviewDemoSourcePolicy);
        if (!contextualGuideId.isEmpty()) {
            return Collections.singletonList(contextualGuideId);
        }
        return new ArrayList<>(guideIds.subList(0, 1));
    }

    static boolean shouldShowGuideChips(State state, boolean inlineTranscriptBubble, LinearLayout container) {
        return shouldShowGuideChips(
            state,
            inlineTranscriptBubble,
            container == null ? 0 : container.getChildCount()
        );
    }

    static boolean shouldShowGuideChips(State state, boolean inlineTranscriptBubble, int existingTurnCount) {
        if (isPhoneLandscapeNoRailTranscript(state, inlineTranscriptBubble)) {
            return true;
        }
        return state == null || !state.utilityRail;
    }

    static boolean isPhoneLandscapeNoRailTranscript(State state, boolean inlineTranscriptBubble) {
        return inlineTranscriptBubble
            && state != null
            && !state.utilityRail
            && !state.compactPortraitSections
            && state.phoneLandscapeNoRail;
    }

    static int answerMaxLines(State state, boolean inlineTranscriptBubble) {
        if (state != null && state.utilityRail && !inlineTranscriptBubble) {
            return RAIL_ANSWER_MAX_LINES;
        }
        if (isPhoneLandscapeNoRailTranscript(state, inlineTranscriptBubble)) {
            return PHONE_LANDSCAPE_ANSWER_MAX_LINES;
        }
        return ANSWER_MAX_LINES;
    }

    static int questionMaxLines(State state, boolean inlineTranscriptBubble, boolean railBubble) {
        if (isPhoneLandscapeNoRailTranscript(state, inlineTranscriptBubble)) {
            return 1;
        }
        return QUESTION_MAX_LINES;
    }

    static int bodyTextSizeSp(
        boolean userTurn,
        boolean inlineTranscriptBubble,
        boolean railBubble,
        State state
    ) {
        if (railBubble) {
            return 13;
        }
        if (isPhoneLandscapeNoRailTranscript(state, inlineTranscriptBubble)) {
            return 13;
        }
        return userTurn || inlineTranscriptBubble ? 15 : 13;
    }

    static int bodyTextColorRes(boolean userTurn) {
        return userTurn ? R.color.senku_rev03_ink_0 : R.color.senku_rev03_ink_0;
    }

    static float bodyLineSpacing(
        boolean userTurn,
        boolean inlineTranscriptBubble,
        boolean railBubble,
        State state
    ) {
        if (userTurn || railBubble || isPhoneLandscapeNoRailTranscript(state, inlineTranscriptBubble)) {
            return 1.02f;
        }
        return 1.08f;
    }

    static int questionBottomPaddingDp(
        boolean inlineTranscriptBubble,
        boolean railBubble,
        State state
    ) {
        if (isPhoneLandscapeNoRailTranscript(state, inlineTranscriptBubble)) {
            return 3;
        }
        return 8;
    }

    static int answerBottomPaddingDp(
        boolean inlineTranscriptBubble,
        boolean railBubble,
        State state
    ) {
        if (isPhoneLandscapeNoRailTranscript(state, inlineTranscriptBubble)) {
            return 1;
        }
        return 4;
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

    private Typeface bodyTypeface(boolean userTurn) {
        if (userTurn) {
            return rev03Typeface(R.font.inter_tight, Typeface.BOLD, Typeface.DEFAULT_BOLD);
        }
        return rev03Typeface(R.font.source_serif_4, Typeface.NORMAL, Typeface.SERIF);
    }

    private Typeface rev03Typeface(int fontResId, int style, Typeface fallback) {
        Typeface typeface = ResourcesCompat.getFont(context, fontResId);
        return Typeface.create(typeface != null ? typeface : fallback, style);
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
