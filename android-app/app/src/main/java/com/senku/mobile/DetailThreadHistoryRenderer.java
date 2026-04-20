package com.senku.mobile;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

final class DetailThreadHistoryRenderer {
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
    private final DetailSourcePresentationFormatter sourceFormatter;

    DetailThreadHistoryRenderer(
        Context context,
        DetailSessionPresentationFormatter sessionFormatter,
        DetailSourcePresentationFormatter sourceFormatter
    ) {
        this.context = context;
        this.sessionFormatter = sessionFormatter;
        this.sourceFormatter = sourceFormatter;
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
        container.setVisibility(View.VISIBLE);
        String previousAnchorGuideId = "";
        for (SessionMemory.TurnSnapshot turn : earlierTurns) {
            addTurn(container, turn, previousAnchorGuideId, state, answerFormatter, true, false);
            previousAnchorGuideId = sessionFormatter.primaryGuideIdForTurn(turn);
        }
    }

    void renderPriorTurnsHistory(
        LinearLayout container,
        List<SessionMemory.TurnSnapshot> earlierTurns,
        State state,
        UnaryOperator<String> answerFormatter
    ) {
        if (container == null) {
            return;
        }
        container.removeAllViews();
        List<SessionMemory.TurnSnapshot> visibleTurns = trimPriorTurnsForInlineHistory(earlierTurns, state);
        if (visibleTurns.isEmpty()) {
            container.setVisibility(View.GONE);
            return;
        }
        container.setVisibility(View.VISIBLE);
        String previousAnchorGuideId = "";
        for (SessionMemory.TurnSnapshot turn : visibleTurns) {
            addTurn(container, turn, previousAnchorGuideId, state, answerFormatter, false, true);
            previousAnchorGuideId = sessionFormatter.primaryGuideIdForTurn(turn);
        }
    }

    private List<SessionMemory.TurnSnapshot> trimPriorTurnsForInlineHistory(
        List<SessionMemory.TurnSnapshot> earlierTurns,
        State state
    ) {
        if (earlierTurns == null || earlierTurns.isEmpty()) {
            return Collections.emptyList();
        }
        int limit;
        if (state.compactPortraitSections) {
            limit = 1;
        } else if (state.wideLayout) {
            limit = 3;
        } else {
            limit = 2;
        }
        if (earlierTurns.size() <= limit) {
            return new ArrayList<>(earlierTurns);
        }
        return new ArrayList<>(earlierTurns.subList(earlierTurns.size() - limit, earlierTurns.size()));
    }

    private void addTurn(
        LinearLayout container,
        SessionMemory.TurnSnapshot turn,
        String previousAnchorGuideId,
        State state,
        UnaryOperator<String> answerFormatter,
        boolean inlineTranscriptBubble,
        boolean includeSourceSummary
    ) {
        if (turn == null || container == null) {
            return;
        }
        container.addView(buildHistoryBubble(turn.question, true, container, state, inlineTranscriptBubble));
        String answerSummary = safe(turn.answerSummary).trim();
        if (answerSummary.isEmpty()) {
            return;
        }
        LinearLayout answerBubble = buildHistoryBubble(
            compactThreadAnswer(answerSummary, state.utilityRail, answerFormatter),
            false,
            container,
            state,
            inlineTranscriptBubble
        );
        String anchorText = sessionFormatter.buildAnchorLineText(turn, previousAnchorGuideId);
        if (!anchorText.isEmpty()) {
            answerBubble.addView(buildMutedLine(anchorText));
        }
        if (includeSourceSummary) {
            String sourceSummary = sourceFormatter.buildSourceSummary(turn.sources);
            if (!sourceSummary.isEmpty()) {
                answerBubble.addView(buildMutedLine(sourceSummary));
            }
        }
        container.addView(answerBubble);
    }

    private LinearLayout buildHistoryBubble(
        String text,
        boolean userTurn,
        LinearLayout container,
        State state,
        boolean inlineTranscriptBubble
    ) {
        LinearLayout bubble = new LinearLayout(context);
        bubble.setOrientation(LinearLayout.VERTICAL);
        bubble.setBackgroundResource(userTurn ? R.drawable.bg_history_card_question : R.drawable.bg_history_card);
        boolean railBubble = state.utilityRail && !inlineTranscriptBubble;
        int bubblePadH = railBubble ? dp(10) : dp(14);
        int bubblePadV = railBubble ? dp(8) : dp(12);
        bubble.setPadding(bubblePadH, bubblePadV, bubblePadH, bubblePadV);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            state.wideLayout ? state.bubbleWidthPx : ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        if (userTurn) {
            params.setMarginStart(railBubble ? dp(56) : dp(40));
        } else {
            params.setMarginEnd(railBubble ? dp(56) : dp(40));
        }
        if (container != null && container.getChildCount() > 0) {
            params.topMargin = railBubble ? dp(4) : dp(8);
        }
        bubble.setLayoutParams(params);

        TextView label = new TextView(context);
        label.setText(userTurn ? context.getString(R.string.detail_header_question) : context.getString(R.string.detail_body_answer));
        label.setTextAppearance(context, android.R.style.TextAppearance_Small);
        label.setTextColor(context.getColor(R.color.senku_text_muted_light));
        label.setBackgroundResource(R.drawable.bg_status_pill);
        label.setPadding(dp(8), dp(4), dp(8), dp(4));
        bubble.addView(label);

        TextView body = new TextView(context);
        body.setText(text);
        body.setTextAppearance(context, userTurn ? android.R.style.TextAppearance_Medium : android.R.style.TextAppearance_Small);
        body.setTextColor(context.getColor(R.color.senku_text_light));
        body.setLineSpacing(0f, 1.1f);
        body.setPadding(0, dp(6), 0, 0);
        if (!userTurn && railBubble) {
            body.setMaxLines(5);
            body.setEllipsize(TextUtils.TruncateAt.END);
        }
        bubble.addView(body);
        return bubble;
    }

    private TextView buildMutedLine(String text) {
        TextView line = new TextView(context);
        line.setText(text);
        line.setTextAppearance(context, android.R.style.TextAppearance_Small);
        line.setTextColor(context.getColor(R.color.senku_text_muted_light));
        line.setPadding(0, dp(8), 0, 0);
        return line;
    }

    private String compactThreadAnswer(String answerSummary, boolean utilityRail, UnaryOperator<String> answerFormatter) {
        String compact = answerFormatter == null ? safe(answerSummary) : safe(answerFormatter.apply(answerSummary));
        if (!utilityRail) {
            return compact;
        }
        int firstBreak = compact.indexOf('\n');
        if (firstBreak > 0) {
            compact = compact.substring(0, firstBreak).trim();
        }
        if (compact.length() > 220) {
            compact = compact.substring(0, 217).trim() + "...";
        }
        return compact;
    }

    private int dp(int value) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
