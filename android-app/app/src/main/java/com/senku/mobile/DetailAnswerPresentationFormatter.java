package com.senku.mobile;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DetailAnswerPresentationFormatter {
    private static final Pattern NUMBERED_STEP_LINE_PATTERN = Pattern.compile("^(\\d+\\.)(\\s+.+)?$");
    private static final String[] ANSWER_SECTION_LABELS = new String[] {
        "Short answer:",
        "Steps:",
        "Limits or safety:"
    };

    private final Context context;

    DetailAnswerPresentationFormatter(Context context) {
        this.context = context;
    }

    SpannableStringBuilder buildStyledAnswerBody(String formatted, boolean showStreamingCursor, int lowCoverageAccentColor) {
        SpannableStringBuilder styled = new SpannableStringBuilder(safe(formatted));
        applyAnswerHierarchySpans(styled, lowCoverageAccentColor);
        if (showStreamingCursor) {
            int cursorStart = styled.length();
            styled.append(" |");
            styled.setSpan(
                new StyleSpan(Typeface.BOLD),
                cursorStart,
                styled.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            styled.setSpan(
                new ForegroundColorSpan(color(R.color.senku_text_muted_light)),
                cursorStart,
                styled.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        return styled;
    }

    SpannableStringBuilder buildStyledAbstainBody(String body, int lowCoverageAccentColor) {
        String formatted = safe(body).trim();
        SpannableStringBuilder styled = new SpannableStringBuilder(formatted);
        if (formatted.isEmpty()) {
            return styled;
        }
        String[] lines = formatted.split("\\n", -1);
        int cursor = 0;
        for (int index = 0; index < lines.length; index++) {
            String line = lines[index];
            int lineStart = cursor;
            int lineEnd = lineStart + line.length();
            String trimmed = line.trim();
            if (index == 0 && !trimmed.isEmpty()) {
                styled.setSpan(new StyleSpan(Typeface.BOLD), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styled.setSpan(new RelativeSizeSpan(1.05f), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styled.setSpan(
                    new ForegroundColorSpan(lowCoverageAccentColor),
                    lineStart,
                    lineEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            } else if ("Closest matches in the library:".equals(trimmed) || "Try:".equals(trimmed)) {
                styled.setSpan(new StyleSpan(Typeface.BOLD), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styled.setSpan(new TypefaceSpan("monospace"), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styled.setSpan(
                    new ForegroundColorSpan(color(R.color.senku_text_muted_light)),
                    lineStart,
                    lineEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            } else if (trimmed.startsWith("- ")) {
                styled.setSpan(
                    new LeadingMarginSpan.Standard(0, dp(18)),
                    lineStart,
                    lineEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
            cursor = lineEnd + 1;
        }
        return styled;
    }

    private void applyAnswerHierarchySpans(SpannableStringBuilder styled, int lowCoverageAccentColor) {
        if (styled.length() == 0) {
            return;
        }
        String text = styled.toString();
        int lineStart = 0;
        while (lineStart <= text.length()) {
            int lineEnd = text.indexOf('\n', lineStart);
            if (lineEnd < 0) {
                lineEnd = text.length();
            }
            int contentStart = firstNonWhitespaceIndex(text, lineStart, lineEnd);
            int contentEnd = lastNonWhitespaceIndex(text, lineStart, lineEnd);
            if (contentStart < contentEnd) {
                String trimmedLine = text.substring(contentStart, contentEnd);
                String sectionLabel = answerSectionLabelForLine(trimmedLine);
                if (!sectionLabel.isEmpty()) {
                    styleAnswerSectionLabel(styled, text, contentStart, contentEnd, sectionLabel);
                } else if (trimmedLine.equals(context.getString(R.string.detail_loop2_corpus_gap_label))) {
                    styleCorpusGapLabel(styled, contentStart, contentEnd, lowCoverageAccentColor);
                } else if (isSafetyLine(trimmedLine)) {
                    styleSafetyLine(styled, contentStart, lineEnd);
                } else {
                    styleStepLineIfPresent(styled, trimmedLine, contentStart, lineEnd);
                }
            }
            if (lineEnd >= text.length()) {
                break;
            }
            lineStart = lineEnd + 1;
        }
    }

    private void styleAnswerSectionLabel(
        SpannableStringBuilder styled,
        String fullText,
        int contentStart,
        int contentEnd,
        String sectionLabel
    ) {
        int labelEnd = Math.min(contentEnd, contentStart + sectionLabel.length());
        styled.setSpan(
            new StyleSpan(Typeface.BOLD),
            contentStart,
            labelEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        styled.setSpan(
            new RelativeSizeSpan(1.06f),
            contentStart,
            labelEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        styled.setSpan(
            new ForegroundColorSpan(colorForAnswerSectionLabel(sectionLabel)),
            contentStart,
            labelEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        if ("Short answer:".equals(sectionLabel)) {
            int summaryStart = firstNonWhitespaceIndex(fullText, labelEnd, contentEnd);
            if (summaryStart < contentEnd) {
                styled.setSpan(
                    new StyleSpan(Typeface.BOLD),
                    summaryStart,
                    contentEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }
    }

    private void styleStepLineIfPresent(
        SpannableStringBuilder styled,
        String trimmedLine,
        int contentStart,
        int lineEnd
    ) {
        Matcher matcher = NUMBERED_STEP_LINE_PATTERN.matcher(trimmedLine);
        if (!matcher.matches()) {
            return;
        }
        int markerEnd = contentStart + safe(matcher.group(1)).length();
        styled.setSpan(
            new LeadingMarginSpan.Standard(0, dp(18)),
            contentStart,
            lineEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        styled.setSpan(
            new StyleSpan(Typeface.BOLD),
            contentStart,
            markerEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        styled.setSpan(
            new RelativeSizeSpan(1.08f),
            contentStart,
            markerEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        styled.setSpan(
            new ForegroundColorSpan(color(R.color.senku_accent_olive)),
            contentStart,
            markerEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }

    private void styleCorpusGapLabel(SpannableStringBuilder styled, int lineStart, int lineEnd, int lowCoverageAccentColor) {
        styled.setSpan(new StyleSpan(Typeface.BOLD), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new RelativeSizeSpan(1.04f), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(
            new ForegroundColorSpan(lowCoverageAccentColor),
            lineStart,
            lineEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }

    private void styleSafetyLine(SpannableStringBuilder styled, int lineStart, int lineEnd) {
        styled.setSpan(new StyleSpan(Typeface.BOLD), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(
            new ForegroundColorSpan(color(R.color.senku_accent_warning)),
            lineStart,
            lineEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }

    private int colorForAnswerSectionLabel(String sectionLabel) {
        if ("Limits or safety:".equals(sectionLabel)) {
            return color(R.color.senku_accent_warning);
        }
        if ("Steps:".equals(sectionLabel)) {
            return color(R.color.senku_accent_olive);
        }
        return color(R.color.senku_text_muted_light);
    }

    private static String answerSectionLabelForLine(String line) {
        for (String label : ANSWER_SECTION_LABELS) {
            if (line.startsWith(label)) {
                return label;
            }
        }
        return "";
    }

    private static boolean isSafetyLine(String line) {
        String normalized = safe(line).trim().toLowerCase(Locale.US);
        return normalized.startsWith("avoid:")
            || normalized.startsWith("- avoid:")
            || normalized.startsWith("limits:")
            || normalized.startsWith("limit:")
            || normalized.startsWith("safety:");
    }

    private static int firstNonWhitespaceIndex(String text, int start, int end) {
        int cursor = Math.max(0, start);
        while (cursor < end && Character.isWhitespace(text.charAt(cursor))) {
            cursor++;
        }
        return cursor;
    }

    private static int lastNonWhitespaceIndex(String text, int start, int end) {
        int cursor = Math.min(text.length(), end);
        while (cursor > start && Character.isWhitespace(text.charAt(cursor - 1))) {
            cursor--;
        }
        return cursor;
    }

    private int color(int resId) {
        return context.getColor(resId);
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
