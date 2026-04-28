package com.senku.mobile;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DetailGuidePresentationFormatter {
    private static final Pattern GUIDE_ADMONITION_PREFIX_PATTERN =
        Pattern.compile("^(DANGER|WARNING|CAUTION|IMPORTANT|NOTE|DANGEROUS)(?::|\\.|\\b)");

    private final Context context;

    DetailGuidePresentationFormatter(Context context) {
        this.context = context;
    }

    static String buildGuideBody(SearchResult result) {
        if (result == null) {
            return "";
        }
        String sectionHeading = safe(result.sectionHeading).trim();
        String body = buildGuideBodyWithSection(sectionHeading, safe(result.body));
        if (!body.isEmpty()) {
            return body;
        }
        return buildGuideBodyWithSection(sectionHeading, safe(result.snippet));
    }

    static String normalizeGuideDisplayTextForLegacy(String text) {
        return GuideBodySanitizer.normalizeGuideDisplayText(text);
    }

    static String normalizeGuideAdmonitionLabelForLegacy(String marker) {
        return GuideBodySanitizer.normalizeGuideAdmonitionLabel(marker);
    }

    static String canonicalGuideAdmonitionLabelForLegacy(String label) {
        return GuideBodySanitizer.canonicalGuideAdmonitionLabel(label);
    }

    SpannableStringBuilder buildStyledGuideBody(String body) {
        GuideBodySanitizer.ParsedGuideBody parsedBody = GuideBodySanitizer.parseGuideBodyForDisplay(body);
        String cleaned = parsedBody.displayText;
        SpannableStringBuilder styled = new SpannableStringBuilder(cleaned);
        if (cleaned.isEmpty()) {
            return styled;
        }
        String displayText = styled.toString();
        boolean insideAdmonitionBlock = false;
        int admonitionAccentColorRes = guideAdmonitionWarningColorResForLegacy();
        int cursor = 0;
        for (GuideBodySanitizer.GuideBodyLine line : parsedBody.lines) {
            int lineStart = cursor;
            int lineEnd = cursor + line.text.length();
            String trimmed = line.text.trim();
            if (line.kind == GuideBodySanitizer.GuideBodyLine.Kind.SECTION) {
                styleGuideAnchorLine(styled, displayText, lineStart, lineEnd, line.label);
                insideAdmonitionBlock = false;
            } else if (line.kind == GuideBodySanitizer.GuideBodyLine.Kind.REQUIRED_READING) {
                styleRequiredReadingLine(styled, displayText, lineStart, lineEnd, line.label);
                insideAdmonitionBlock = false;
            } else {
                int prefixEnd = guideAdmonitionPrefixEnd(trimmed);
                if (prefixEnd > 0) {
                    int spanEnd = lineStart + prefixEnd;
                    styled.setSpan(new StyleSpan(Typeface.BOLD), lineStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styled.setSpan(new TypefaceSpan("monospace"), lineStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styled.setSpan(new RelativeSizeSpan(0.95f), lineStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    admonitionAccentColorRes = admonitionAccentColorRes(trimmed);
                    styled.setSpan(
                        new ForegroundColorSpan(color(admonitionAccentColorRes)),
                        lineStart,
                        spanEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                    boolean labelOnly = GuideBodySanitizer.canonicalGuideAdmonitionLabel(trimmed).equals(trimmed);
                    if (labelOnly) {
                        styled.setSpan(
                            new BackgroundColorSpan(color(guideAdmonitionBackgroundColorResForLegacy())),
                            lineStart,
                            lineEnd,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                    } else {
                        applyGuideIndentedWarningLine(styled, lineStart, lineEnd, admonitionAccentColorRes);
                    }
                    insideAdmonitionBlock = true;
                } else if (trimmed.isEmpty()) {
                    insideAdmonitionBlock = false;
                } else if (insideAdmonitionBlock) {
                    applyGuideIndentedWarningLine(styled, lineStart, lineEnd, admonitionAccentColorRes);
                }
            }
            cursor = lineEnd + 1;
        }
        return styled;
    }

    static int guideBodyTextColorResForLegacy() {
        return R.color.senku_rev03_paper_ink;
    }

    static int guideAnchorLabelColorResForLegacy() {
        return R.color.senku_rev03_paper_ok;
    }

    static int guideAnchorValueColorResForLegacy() {
        return R.color.senku_rev03_paper_ink_muted;
    }

    static int guideAdmonitionDangerColorResForLegacy() {
        return R.color.senku_rev03_paper_danger;
    }

    static int guideAdmonitionWarningColorResForLegacy() {
        return R.color.senku_rev03_paper_warn;
    }

    static int guideAdmonitionNoteColorResForLegacy() {
        return R.color.senku_rev03_paper_ok;
    }

    static int guideAdmonitionBackgroundColorResForLegacy() {
        return R.color.senku_rev03_paper;
    }

    static int admonitionAccentColorResForLegacy(String line) {
        return admonitionAccentColorRes(line);
    }

    private static int admonitionAccentColorRes(String line) {
        String normalized = safe(line).trim().toUpperCase(Locale.US);
        if (normalized.startsWith("DANGER") || normalized.startsWith("DANGEROUS")) {
            return guideAdmonitionDangerColorResForLegacy();
        }
        if (normalized.startsWith("WARNING") || normalized.startsWith("CAUTION")) {
            return guideAdmonitionWarningColorResForLegacy();
        }
        return guideAdmonitionNoteColorResForLegacy();
    }

    private void styleGuideAnchorLine(
        SpannableStringBuilder styled,
        String fullText,
        int lineStart,
        int lineEnd,
        String anchorLabel
    ) {
        int labelEnd = Math.min(lineEnd, lineStart + safe(anchorLabel).length());
        int valueStart = firstNonWhitespaceIndex(fullText, labelEnd, lineEnd);
        styled.setSpan(new StyleSpan(Typeface.BOLD), lineStart, labelEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new TypefaceSpan("monospace"), lineStart, labelEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new RelativeSizeSpan(0.9f), lineStart, labelEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(
            new ForegroundColorSpan(color(guideAnchorLabelColorResForLegacy())),
            lineStart,
            labelEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        if (valueStart < lineEnd) {
            styled.setSpan(new StyleSpan(Typeface.BOLD), valueStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            styled.setSpan(
                new ForegroundColorSpan(color(guideAnchorValueColorResForLegacy())),
                valueStart,
                lineEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }

    private void styleRequiredReadingLine(
        SpannableStringBuilder styled,
        String fullText,
        int lineStart,
        int lineEnd,
        String label
    ) {
        int labelEnd = Math.min(lineEnd, lineStart + safe(label).length());
        int valueStart = firstNonWhitespaceIndex(fullText, labelEnd, lineEnd);
        styled.setSpan(new StyleSpan(Typeface.BOLD), lineStart, labelEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new TypefaceSpan("monospace"), lineStart, labelEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new RelativeSizeSpan(0.92f), lineStart, labelEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(
            new ForegroundColorSpan(color(guideAdmonitionWarningColorResForLegacy())),
            lineStart,
            labelEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        if (valueStart < lineEnd) {
            styled.setSpan(
                new ForegroundColorSpan(color(guideBodyTextColorResForLegacy())),
                valueStart,
                lineEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }

    private static int firstNonWhitespaceIndex(String text, int start, int end) {
        int cursor = Math.max(0, start);
        while (cursor < end && Character.isWhitespace(text.charAt(cursor))) {
            cursor++;
        }
        return cursor;
    }

    private void applyGuideIndentedWarningLine(
        SpannableStringBuilder styled,
        int lineStart,
        int lineEnd,
        int accentColorRes
    ) {
        if (lineStart >= lineEnd) {
            return;
        }
        styled.setSpan(
            new LeadingMarginSpan.Standard(dp(10), dp(18)),
            lineStart,
            lineEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        styled.setSpan(
            new ForegroundColorSpan(color(guideBodyTextColorResForLegacy())),
            lineStart,
            lineEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        styled.setSpan(
            new BackgroundColorSpan(color(admonitionContinuationBackgroundColorRes(accentColorRes))),
            lineStart,
            lineEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }

    private static int admonitionContinuationBackgroundColorRes(int accentColorRes) {
        return guideAdmonitionBackgroundColorResForLegacy();
    }

    private static int guideAdmonitionPrefixEnd(String line) {
        Matcher matcher = GUIDE_ADMONITION_PREFIX_PATTERN.matcher(safe(line).trim());
        if (!matcher.find()) {
            return -1;
        }
        int prefixEnd = matcher.end();
        int colonIndex = line.indexOf(':');
        if (colonIndex >= 0 && colonIndex >= prefixEnd) {
            return colonIndex + 1;
        }
        int periodIndex = line.indexOf('.');
        if (periodIndex >= 0 && periodIndex >= prefixEnd) {
            return periodIndex + 1;
        }
        return prefixEnd;
    }

    private static String buildGuideBodyWithSection(String sectionHeading, String sourceText) {
        String source = safe(sourceText).trim();
        if (source.isEmpty()) {
            return "";
        }
        String section = safe(sectionHeading).trim();
        if (section.isEmpty()) {
            return GuideBodySanitizer.sanitizeGuideBodyForDisplay(source);
        }
        return GuideBodySanitizer.sanitizeGuideBodyForDisplay("## " + section + "\n\n" + source);
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
