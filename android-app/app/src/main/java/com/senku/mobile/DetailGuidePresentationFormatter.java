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

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DetailGuidePresentationFormatter {
    private static final Pattern GUIDE_ADMONITION_FENCE_PATTERN = Pattern.compile("^:::\\s*([a-zA-Z]+)?\\s*$");
    private static final Pattern GUIDE_ADMONITION_LABEL_PATTERN = Pattern.compile("^(DANGER|WARNING|CAUTION|IMPORTANT|NOTE):?$");
    private static final Pattern GUIDE_ADMONITION_PREFIX_PATTERN = Pattern.compile("^(DANGER|WARNING|CAUTION|IMPORTANT|NOTE|DANGEROUS)(?::|\\.|\\b)");
    private static final Pattern GUIDE_ADMONITION_INLINE_PREFIX_PATTERN =
        Pattern.compile("^(DANGER|WARNING|CAUTION|IMPORTANT|NOTE|DANGEROUS)(?:\\s*[:.-]\\s*|\\s+)(.+)$");
    private static final Pattern GUIDE_MARKDOWN_HEADING_PATTERN = Pattern.compile("^#{1,6}\\s+");
    private static final Pattern GUIDE_LAYOUT_TAG_PATTERN = Pattern.compile("(?i)</?(section|div|span|p)\\b[^>]*>");
    private static final Pattern GUIDE_INLINE_STYLE_TAG_PATTERN = Pattern.compile("(?i)</?(strong|b|em|i|mark|small)\\b[^>]*>");
    private static final Pattern GUIDE_MARKDOWN_LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]+)\\)");
    private static final Pattern GUIDE_HTML_BREAK_PATTERN = Pattern.compile("(?i)<br\\s*/?>");
    private static final Pattern GUIDE_MARKDOWN_ESCAPE_PATTERN = Pattern.compile("\\\\([\\[\\]\\(\\)#*_`])");
    private static final Pattern GUIDE_SECTION_LINE_PATTERN = Pattern.compile("^Source section:\\s*(.+)$", Pattern.CASE_INSENSITIVE);
    private static final String GUIDE_SECTION_ANCHOR_MARKER = "[[SECTION]] ";
    private static final String GUIDE_SECTION_DISPLAY_PREFIX = "\u2014 \u00a7 ";
    private static final String GUIDE_SECTION_DISPLAY_SEPARATOR = " \u00b7 ";

    private static final class GuideBodyLine {
        final String text;
        final String anchorLabel;

        GuideBodyLine(String text, String anchorLabel) {
            this.text = safe(text);
            this.anchorLabel = safe(anchorLabel);
        }

        boolean isAnchor() {
            return !anchorLabel.isEmpty();
        }
    }

    private final Context context;

    DetailGuidePresentationFormatter(Context context) {
        this.context = context;
    }

    static String buildGuideBody(SearchResult result) {
        if (result == null) {
            return "";
        }
        String sectionHeading = safe(result.sectionHeading).trim();
        String body = sanitizeGuideBodyForDisplay(safe(result.body));
        if (!body.isEmpty()) {
            return sectionHeading.isEmpty() ? body : "Source section: " + sectionHeading + "\n\n" + body;
        }
        String snippet = sanitizeGuideBodyForDisplay(safe(result.snippet));
        return sectionHeading.isEmpty() ? snippet : "Source section: " + sectionHeading + "\n\n" + snippet;
    }

    static String normalizeGuideDisplayTextForLegacy(String text) {
        return normalizeGuideDisplayText(text);
    }

    static String normalizeGuideAdmonitionLabelForLegacy(String marker) {
        return normalizeGuideAdmonitionLabel(marker);
    }

    static String canonicalGuideAdmonitionLabelForLegacy(String label) {
        return canonicalGuideAdmonitionLabel(label);
    }

    SpannableStringBuilder buildStyledGuideBody(String body) {
        String cleaned = sanitizeGuideBodyForDisplay(body);
        SpannableStringBuilder styled = new SpannableStringBuilder(cleaned);
        if (cleaned.isEmpty()) {
            return styled;
        }
        String[] rawLines = cleaned.split("\\R", -1);
        ArrayList<GuideBodyLine> lines = new ArrayList<>(rawLines.length);
        StringBuilder displayBuilder = new StringBuilder(cleaned.length() + 32);
        int sectionOrdinal = 0;
        for (int i = 0; i < rawLines.length; i++) {
            GuideBodyLine line = toGuideBodyLine(rawLines[i], sectionOrdinal + 1);
            if (line.isAnchor()) {
                sectionOrdinal++;
            }
            lines.add(line);
            displayBuilder.append(line.text);
            if (i < rawLines.length - 1) {
                displayBuilder.append('\n');
            }
        }
        styled = new SpannableStringBuilder(displayBuilder.toString());
        String displayText = styled.toString();
        boolean insideAdmonitionBlock = false;
        int admonitionAccentColorRes = guideAdmonitionWarningColorResForLegacy();
        int cursor = 0;
        for (GuideBodyLine line : lines) {
            int lineStart = cursor;
            int lineEnd = cursor + line.text.length();
            String trimmed = line.text.trim();
            if (line.isAnchor()) {
                styleGuideAnchorLine(styled, displayText, lineStart, lineEnd, line.anchorLabel);
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
                    boolean labelOnly = canonicalGuideAdmonitionLabel(trimmed).equals(trimmed);
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

    private GuideBodyLine toGuideBodyLine(String rawLine, int sectionOrdinal) {
        String line = safe(rawLine);
        Matcher sectionMatcher = GUIDE_SECTION_LINE_PATTERN.matcher(line.trim());
        if (sectionMatcher.matches()) {
            String value = safe(sectionMatcher.group(1)).trim();
            return new GuideBodyLine(
                buildGuideSectionLabel(sectionOrdinal, value),
                buildGuideSectionPrefix(sectionOrdinal)
            );
        }
        if (line.startsWith(GUIDE_SECTION_ANCHOR_MARKER)) {
            String value = line.substring(GUIDE_SECTION_ANCHOR_MARKER.length()).trim();
            return new GuideBodyLine(
                buildGuideSectionLabel(sectionOrdinal, value),
                buildGuideSectionPrefix(sectionOrdinal)
            );
        }
        return new GuideBodyLine(line, "");
    }

    private static String buildGuideSectionLabel(int sectionOrdinal, String value) {
        return buildGuideSectionPrefix(sectionOrdinal) + GUIDE_SECTION_DISPLAY_SEPARATOR + safe(value).trim().toUpperCase(Locale.US);
    }

    private static String buildGuideSectionPrefix(int sectionOrdinal) {
        return GUIDE_SECTION_DISPLAY_PREFIX + Math.max(sectionOrdinal, 1);
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

    private static String sanitizeGuideBodyForDisplay(String body) {
        return GuideBodySanitizer.sanitizeGuideBodyForDisplay(body);
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

    private static String sanitizeGuideDisplayLine(String rawLine, boolean insideAdmonitionBlock) {
        String cleaned = safe(rawLine);
        cleaned = GUIDE_MARKDOWN_ESCAPE_PATTERN.matcher(cleaned).replaceAll("$1");
        cleaned = GUIDE_LAYOUT_TAG_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = GUIDE_INLINE_STYLE_TAG_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = GUIDE_MARKDOWN_HEADING_PATTERN.matcher(cleaned).replaceFirst("");
        cleaned = GUIDE_MARKDOWN_LINK_PATTERN.matcher(cleaned).replaceAll("$1");
        cleaned = cleaned.replace("**", "");
        cleaned = cleaned.replace("__", "");
        cleaned = cleaned.replace("`", "");
        if (insideAdmonitionBlock) {
            cleaned = cleaned.replaceFirst("^[^\\p{Alnum}\\[]+\\s*", "");
        }
        return cleaned.trim();
    }

    private static String stripDuplicateAdmonitionLabel(String line, String admonitionLabel, boolean firstAdmonitionContentLine) {
        String cleaned = safe(line).trim();
        if (!firstAdmonitionContentLine || cleaned.isEmpty()) {
            return cleaned;
        }
        String normalizedLabel = canonicalGuideAdmonitionLabel(admonitionLabel);
        if (normalizedLabel.isEmpty()) {
            return cleaned;
        }
        if (normalizedLabel.equals(canonicalGuideAdmonitionLabel(cleaned))) {
            return "";
        }
        Matcher inlinePrefixMatcher = GUIDE_ADMONITION_INLINE_PREFIX_PATTERN.matcher(cleaned);
        if (!inlinePrefixMatcher.matches()) {
            return cleaned;
        }
        if (!normalizedLabel.equals(canonicalGuideAdmonitionLabel(inlinePrefixMatcher.group(1)))) {
            return cleaned;
        }
        return safe(inlinePrefixMatcher.group(2)).trim();
    }

    private static String normalizeGuideDisplayText(String text) {
        String cleaned = safe(text).replace("\r\n", "\n");
        cleaned = GUIDE_HTML_BREAK_PATTERN.matcher(cleaned).replaceAll("\n");
        cleaned = cleaned.replace("&nbsp;", " ");
        cleaned = cleaned.replace("&amp;", "&");
        cleaned = cleaned.replace("&lt;", "<");
        cleaned = cleaned.replace("&gt;", ">");
        cleaned = cleaned.replace("\u00e2\u20ac\u201d", " - ");
        cleaned = cleaned.replace("\u00e2\u20ac\u201c", "-");
        cleaned = cleaned.replace("\u00e2\u2020\u2019", "->");
        cleaned = cleaned.replace("\u00c2", "");
        cleaned = cleaned.replace("Ã¢â‚¬â€", " - ");
        cleaned = cleaned.replace("Ã¢â‚¬â€œ", "-");
        cleaned = cleaned.replace("Ã¢â€ â€™", "->");
        cleaned = cleaned.replace("Ã¢Å¡Â Ã¯Â¸Â", "!");
        return DetailActivity.sanitizeWarningResidualCopy(cleaned);
    }

    private static String normalizeGuideAdmonitionLabel(String marker) {
        String normalized = safe(marker).trim().toLowerCase(Locale.US);
        if ("danger".equals(normalized)) {
            return "DANGER";
        }
        if ("warning".equals(normalized)) {
            return "WARNING";
        }
        if ("caution".equals(normalized)) {
            return "CAUTION";
        }
        if ("important".equals(normalized)) {
            return "IMPORTANT";
        }
        return "NOTE";
    }

    private static String canonicalGuideAdmonitionLabel(String label) {
        String cleaned = safe(label).trim().toUpperCase(Locale.US);
        if (cleaned.endsWith(":")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
        }
        if ("DANGEROUS".equals(cleaned)) {
            return "DANGER";
        }
        if (GUIDE_ADMONITION_LABEL_PATTERN.matcher(cleaned).matches()) {
            return cleaned;
        }
        return "";
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
