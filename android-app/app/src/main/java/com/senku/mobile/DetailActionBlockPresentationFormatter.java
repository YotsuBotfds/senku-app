package com.senku.mobile;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class DetailActionBlockPresentationFormatter {
    private static final class ActionBlock {
        final String label;
        final String body;
        final int accentColor;

        ActionBlock(String label, String body, int accentColor) {
            this.label = label;
            this.body = body;
            this.accentColor = accentColor;
        }
    }

    private final Context context;
    private final DetailAnswerBodyFormatter answerBodyFormatter;
    private final DetailCitationPresentationFormatter citationPresentationFormatter;

    DetailActionBlockPresentationFormatter(
        Context context,
        DetailAnswerBodyFormatter answerBodyFormatter,
        DetailCitationPresentationFormatter citationPresentationFormatter
    ) {
        this.context = context;
        this.answerBodyFormatter = answerBodyFormatter;
        this.citationPresentationFormatter = citationPresentationFormatter;
    }

    void renderHighRiskActionBlocks(LinearLayout panel, String currentBody, int severityAccentColor) {
        if (panel == null) {
            return;
        }
        panel.removeAllViews();
        List<ActionBlock> blocks = buildHighRiskActionBlocks(currentBody, severityAccentColor);
        if (blocks.isEmpty()) {
            panel.setVisibility(View.GONE);
            return;
        }
        panel.setVisibility(View.VISIBLE);
        for (int i = 0; i < blocks.size(); i++) {
            panel.addView(buildActionBlockView(blocks.get(i), i > 0));
        }
    }

    private List<ActionBlock> buildHighRiskActionBlocks(String currentBody, int severityAccentColor) {
        ArrayList<ActionBlock> blocks = new ArrayList<>();
        List<String> steps = extractStepLines(answerBodyFormatter.formatAnswerBody(currentBody));
        if (steps.isEmpty()) {
            return blocks;
        }
        String doFirst = sanitizeActionBlockText(steps.get(0));
        String avoid = "";
        String escalate = "";
        for (String step : steps) {
            String normalized = sanitizeActionBlockText(step);
            String lower = normalized.toLowerCase(Locale.US);
            if (avoid.isEmpty() && containsAnyPhrase(lower, "do not", "avoid", "never")) {
                avoid = extractActionMarkerClause(normalized, "Do not", "Avoid", "Never");
            }
            if (escalate.isEmpty() && containsAnyPhrase(lower, "escalate", "get medical", "seek", "worsening", "fever", "red streaking")) {
                escalate = extractActionMarkerClause(normalized, "Escalate", "Get medical", "Seek", "Worsening", "Fever", "Red streaking");
            }
        }
        if (!doFirst.isEmpty()) {
            blocks.add(new ActionBlock(context.getString(R.string.detail_action_do_first), doFirst, severityAccentColor));
        }
        if (!avoid.isEmpty()) {
            blocks.add(new ActionBlock(context.getString(R.string.detail_action_avoid), avoid, context.getColor(R.color.senku_accent_warning)));
        }
        if (!escalate.isEmpty()) {
            blocks.add(new ActionBlock(context.getString(R.string.detail_action_escalate), escalate, context.getColor(R.color.senku_emergency_banner_bg)));
        }
        return blocks;
    }

    private View buildActionBlockView(ActionBlock block, boolean addTopMargin) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundResource(R.drawable.bg_surface_panel);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        if (addTopMargin) {
            cardParams.topMargin = dp(8);
        }
        card.setLayoutParams(cardParams);

        View accentBar = new View(context);
        accentBar.setBackgroundColor(block.accentColor);
        card.addView(accentBar, new LinearLayout.LayoutParams(dp(5), ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(10), dp(12), dp(10));

        TextView label = new TextView(context);
        label.setText(block.label.toUpperCase(Locale.US));
        label.setTextAppearance(context, android.R.style.TextAppearance_Small);
        label.setTextColor(block.accentColor);
        label.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        label.setLetterSpacing(0.08f);

        TextView body = new TextView(context);
        body.setText(block.body);
        body.setTextAppearance(context, android.R.style.TextAppearance_Small);
        body.setTextColor(context.getColor(R.color.senku_text_light));
        body.setTypeface(Typeface.DEFAULT_BOLD);
        body.setLineSpacing(0f, 1.12f);
        body.setPadding(0, dp(6), 0, 0);

        content.addView(label);
        content.addView(body);
        card.addView(
            content,
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        );
        return card;
    }

    private List<String> extractStepLines(String answerText) {
        ArrayList<String> steps = new ArrayList<>();
        String[] lines = safe(answerText).split("\\R");
        for (String rawLine : lines) {
            String line = safe(rawLine).trim();
            if (line.matches("^\\d+\\.\\s+.*")) {
                steps.add(line.replaceFirst("^\\d+\\.\\s*", "").trim());
            }
        }
        return steps;
    }

    private String sanitizeActionBlockText(String text) {
        String cleaned = safe(text).trim();
        cleaned = citationPresentationFormatter.stripInlineCitationText(cleaned);
        cleaned = cleaned.replace(":::", "");
        cleaned = cleaned.replaceAll("\\s{2,}", " ").trim();
        return DetailWarningCopySanitizer.sanitizeWarningResidualCopy(cleaned);
    }

    private String extractActionMarkerClause(String text, String... markers) {
        String cleaned = sanitizeActionBlockText(text);
        if (cleaned.isEmpty()) {
            return "";
        }
        String lower = cleaned.toLowerCase(Locale.US);
        int bestIndex = -1;
        for (String marker : markers) {
            String normalizedMarker = safe(marker).trim().toLowerCase(Locale.US);
            if (normalizedMarker.isEmpty()) {
                continue;
            }
            int markerIndex = lower.indexOf(normalizedMarker);
            if (markerIndex >= 0 && (bestIndex < 0 || markerIndex < bestIndex)) {
                bestIndex = markerIndex;
            }
        }
        if (bestIndex <= 0) {
            return cleaned;
        }
        return cleaned.substring(bestIndex).trim();
    }

    private static boolean containsAnyPhrase(String text, String... markers) {
        for (String marker : markers) {
            if (text.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
