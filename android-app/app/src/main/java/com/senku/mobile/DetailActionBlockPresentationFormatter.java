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
    static final String ACTION_LABEL_DO_FIRST = "Do first";
    static final String ACTION_LABEL_AVOID = "Avoid";
    static final String ACTION_LABEL_ESCALATE = "Escalate if";

    enum ActionBlockKind {
        DO_FIRST,
        AVOID,
        ESCALATE
    }

    static final class ActionBlockSpec {
        final ActionBlockKind kind;
        final String label;
        final String body;

        ActionBlockSpec(ActionBlockKind kind, String label, String body) {
            this.kind = kind;
            this.label = label;
            this.body = body;
        }
    }

    static final class EmergencyActionSpec {
        final String title;
        final String detail;

        EmergencyActionSpec(String title, String detail) {
            this.title = title;
            this.detail = detail;
        }
    }

    interface ActionBlockTextSanitizer {
        String sanitize(String text);
    }

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

    void renderEmergencyPortraitActions(LinearLayout panel, String currentBody, int severityAccentColor) {
        if (panel == null) {
            return;
        }
        panel.removeAllViews();
        List<EmergencyActionSpec> actions = extractEmergencyActionSpecs(
            answerBodyFormatter.formatAnswerBody(currentBody),
            this::sanitizeActionBlockText
        );
        if (actions.isEmpty()) {
            panel.setVisibility(View.GONE);
            return;
        }
        panel.setVisibility(View.VISIBLE);
        panel.setBackgroundColor(context.getColor(android.R.color.transparent));
        TextView heading = new TextView(context);
        heading.setText("IMMEDIATE ACTIONS - " + actions.size());
        heading.setTextAppearance(context, android.R.style.TextAppearance_Small);
        heading.setTextColor(context.getColor(R.color.senku_text_muted_light));
        heading.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        heading.setLetterSpacing(0.08f);
        heading.setPadding(0, 0, 0, dp(8));
        panel.addView(heading);
        for (int i = 0; i < actions.size(); i++) {
            panel.addView(buildEmergencyPortraitActionView(i + 1, actions.get(i), severityAccentColor, i > 0));
        }
    }

    private List<ActionBlock> buildHighRiskActionBlocks(String currentBody, int severityAccentColor) {
        ArrayList<ActionBlock> blocks = new ArrayList<>();
        List<ActionBlockSpec> specs = extractHighRiskActionBlockSpecs(
            answerBodyFormatter.formatAnswerBody(currentBody),
            this::sanitizeActionBlockText
        );
        for (ActionBlockSpec spec : specs) {
            blocks.add(new ActionBlock(actionBlockLabel(spec.kind), spec.body, actionBlockAccentColor(spec.kind, severityAccentColor)));
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

    private View buildEmergencyPortraitActionView(
        int number,
        EmergencyActionSpec action,
        int severityAccentColor,
        boolean addTopDivider
    ) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, addTopDivider ? dp(12) : dp(8), 0, dp(12));
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        row.setLayoutParams(rowParams);

        TextView badge = new TextView(context);
        badge.setText(String.valueOf(number));
        badge.setGravity(android.view.Gravity.CENTER);
        badge.setTextAppearance(context, android.R.style.TextAppearance_Small);
        badge.setTextColor(severityAccentColor);
        badge.setBackgroundResource(R.drawable.bg_emergency_action_badge);
        row.addView(badge, new LinearLayout.LayoutParams(dp(34), dp(34)));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(14), 0, 0, 0);

        TextView title = new TextView(context);
        title.setText(action.title);
        title.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        title.setTextColor(context.getColor(R.color.senku_text_light));
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setLineSpacing(0f, 1.05f);

        TextView detail = new TextView(context);
        detail.setText(action.detail);
        detail.setTextAppearance(context, android.R.style.TextAppearance_Small);
        detail.setTextColor(context.getColor(R.color.senku_text_muted_light));
        detail.setLineSpacing(0f, 1.08f);
        detail.setPadding(0, dp(3), 0, 0);
        detail.setVisibility(action.detail.isEmpty() ? View.GONE : View.VISIBLE);

        content.addView(title);
        content.addView(detail);
        row.addView(content, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return row;
    }

    static List<EmergencyActionSpec> extractEmergencyActionSpecs(
        String formattedAnswerText,
        ActionBlockTextSanitizer sanitizer
    ) {
        ArrayList<EmergencyActionSpec> actions = new ArrayList<>();
        for (String step : extractStepLines(formattedAnswerText)) {
            String cleaned = sanitizeActionBlockText(step, sanitizer);
            if (cleaned.isEmpty()) {
                continue;
            }
            actions.add(splitEmergencyAction(cleaned));
        }
        return actions;
    }

    private static EmergencyActionSpec splitEmergencyAction(String cleaned) {
        int splitIndex = firstSentenceBoundary(cleaned);
        if (splitIndex < 0) {
            return new EmergencyActionSpec(cleaned, "");
        }
        String title = cleaned.substring(0, splitIndex).trim();
        String detail = cleaned.substring(splitIndex).replaceFirst("^[.!?]+\\s*", "").trim();
        if (title.isEmpty()) {
            return new EmergencyActionSpec(cleaned, "");
        }
        return new EmergencyActionSpec(title, detail);
    }

    private static int firstSentenceBoundary(String text) {
        String value = safe(text).trim();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if ((c == '.' || c == '!' || c == '?') && i + 1 < value.length()) {
                char next = value.charAt(i + 1);
                if (Character.isWhitespace(next)) {
                    return i;
                }
            }
        }
        return -1;
    }

    static List<ActionBlockSpec> extractHighRiskActionBlockSpecs(
        String formattedAnswerText,
        ActionBlockTextSanitizer sanitizer
    ) {
        ArrayList<ActionBlockSpec> blocks = new ArrayList<>();
        List<String> steps = extractStepLines(formattedAnswerText);
        if (steps.isEmpty()) {
            return blocks;
        }
        String doFirst = sanitizeActionBlockText(steps.get(0), sanitizer);
        String avoid = "";
        String escalate = "";
        for (String step : steps) {
            String normalized = sanitizeActionBlockText(step, sanitizer);
            String lower = normalized.toLowerCase(Locale.US);
            if (avoid.isEmpty() && containsAnyPhrase(lower, "do not", "avoid", "never")) {
                avoid = extractActionMarkerClause(normalized, sanitizer, "Do not", "Avoid", "Never");
            }
            if (escalate.isEmpty() && containsAnyPhrase(lower, "escalate", "get medical", "seek", "worsening", "fever", "red streaking")) {
                escalate = extractActionMarkerClause(
                    normalized,
                    sanitizer,
                    "Escalate",
                    "Get medical",
                    "Seek",
                    "Worsening",
                    "Fever",
                    "Red streaking"
                );
            }
        }
        if (!doFirst.isEmpty()) {
            blocks.add(new ActionBlockSpec(ActionBlockKind.DO_FIRST, ACTION_LABEL_DO_FIRST, doFirst));
        }
        if (!avoid.isEmpty()) {
            blocks.add(new ActionBlockSpec(ActionBlockKind.AVOID, ACTION_LABEL_AVOID, avoid));
        }
        if (!escalate.isEmpty()) {
            blocks.add(new ActionBlockSpec(ActionBlockKind.ESCALATE, ACTION_LABEL_ESCALATE, escalate));
        }
        return blocks;
    }

    private static List<String> extractStepLines(String answerText) {
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
        return sanitizeActionBlockText(
            text,
            textToSanitize -> citationPresentationFormatter.stripInlineCitationText(safe(textToSanitize).trim())
        );
    }

    private static String sanitizeActionBlockText(String text, ActionBlockTextSanitizer sanitizer) {
        String cleaned = safe(text).trim();
        if (sanitizer != null) {
            cleaned = safe(sanitizer.sanitize(cleaned)).trim();
        }
        cleaned = cleaned.replace(":::", "");
        cleaned = cleaned.replaceAll("\\s{2,}", " ").trim();
        return DetailWarningCopySanitizer.sanitizeWarningResidualCopy(cleaned);
    }

    private static String extractActionMarkerClause(String text, ActionBlockTextSanitizer sanitizer, String... markers) {
        String cleaned = sanitizeActionBlockText(text, sanitizer);
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

    private String actionBlockLabel(ActionBlockKind kind) {
        if (kind == ActionBlockKind.DO_FIRST) {
            return context.getString(R.string.detail_action_do_first);
        }
        if (kind == ActionBlockKind.AVOID) {
            return context.getString(R.string.detail_action_avoid);
        }
        return context.getString(R.string.detail_action_escalate);
    }

    private int actionBlockAccentColor(ActionBlockKind kind, int severityAccentColor) {
        if (kind == ActionBlockKind.DO_FIRST) {
            return severityAccentColor;
        }
        if (kind == ActionBlockKind.AVOID) {
            return context.getColor(R.color.senku_accent_warning);
        }
        return context.getColor(R.color.senku_emergency_banner_bg);
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
