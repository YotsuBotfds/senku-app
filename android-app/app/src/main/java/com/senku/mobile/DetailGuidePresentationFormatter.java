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
    private static final Pattern GUIDE_MANUAL_KICKER_PATTERN =
        Pattern.compile("^FIELD MANUAL\\s+\\u00b7\\s+REV\\s+\\d{2}-\\d{2}\\s+\\u00b7\\s+PK\\s+\\d+$");
    private static final Pattern GUIDE_MANUAL_META_PATTERN =
        Pattern.compile("^GD-\\d+\\s+\\u00b7\\s+\\d+\\s+SECTIONS?(?:\\s+\\u00b7\\s+.+)?$");

    private final Context context;

    DetailGuidePresentationFormatter(Context context) {
        this.context = context;
    }

    static String buildGuideBody(SearchResult result) {
        if (result == null) {
            return "";
        }
        String sectionHeading = safe(result.sectionHeading).trim();
        String sourceBody = safe(result.body);
        String body = buildGuideBodyWithSection(sectionHeading, sourceBody);
        if (!body.isEmpty()) {
            return prependGuidePaperHeader(result, body, inferGuideSectionCount(sourceBody, body));
        }
        String sourceSnippet = safe(result.snippet);
        String snippetBody = buildGuideBodyWithSection(sectionHeading, sourceSnippet);
        return prependGuidePaperHeader(result, snippetBody, inferGuideSectionCount(sourceSnippet, snippetBody));
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
            } else if (isGuideManualKickerLine(trimmed)) {
                styleGuideManualKickerLine(styled, lineStart, lineEnd);
                insideAdmonitionBlock = false;
            } else if (isGuideManualMetaLine(trimmed)) {
                styleGuideManualMetaLine(styled, lineStart, lineEnd);
                insideAdmonitionBlock = false;
            } else if (isGuideManualTitleLine(parsedBody.lines, line)) {
                styleGuideManualTitleLine(styled, lineStart, lineEnd);
                insideAdmonitionBlock = false;
            } else if (line.kind == GuideBodySanitizer.GuideBodyLine.Kind.REQUIRED_READING) {
                styleRequiredReadingLine(styled, displayText, lineStart, lineEnd, line.label);
                insideAdmonitionBlock = false;
            } else if (line.kind == GuideBodySanitizer.GuideBodyLine.Kind.HEADING) {
                styleGuideHeadingLine(styled, lineStart, lineEnd);
                insideAdmonitionBlock = false;
            } else if (line.kind == GuideBodySanitizer.GuideBodyLine.Kind.ADMONITION_LABEL) {
                admonitionAccentColorRes = admonitionAccentColorRes(trimmed);
                styleGuideAdmonitionLabelLine(styled, lineStart, lineEnd, line.label, admonitionAccentColorRes);
                insideAdmonitionBlock = true;
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
                } else {
                    styleGuideTextLine(styled, lineStart, lineEnd);
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
        styled.setSpan(new RelativeSizeSpan(0.66f), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(
            new ForegroundColorSpan(color(guideAnchorLabelColorResForLegacy())),
            lineStart,
            labelEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        if (valueStart < lineEnd) {
            styled.setSpan(
                new ForegroundColorSpan(color(guideAnchorValueColorResForLegacy())),
                valueStart,
                lineEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }

    private void styleGuideHeadingLine(SpannableStringBuilder styled, int lineStart, int lineEnd) {
        if (lineStart >= lineEnd) {
            return;
        }
        styled.setSpan(new StyleSpan(Typeface.BOLD), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new TypefaceSpan("sans"), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new RelativeSizeSpan(0.82f), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(
            new ForegroundColorSpan(color(guideBodyTextColorResForLegacy())),
            lineStart,
            lineEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }

    private void styleGuideManualKickerLine(SpannableStringBuilder styled, int lineStart, int lineEnd) {
        styled.setSpan(new StyleSpan(Typeface.BOLD), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new TypefaceSpan("monospace"), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new RelativeSizeSpan(0.62f), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(
            new ForegroundColorSpan(color(guideAdmonitionWarningColorResForLegacy())),
            lineStart,
            lineEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }

    private void styleGuideManualTitleLine(SpannableStringBuilder styled, int lineStart, int lineEnd) {
        styled.setSpan(new StyleSpan(Typeface.BOLD), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new TypefaceSpan("sans"), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new RelativeSizeSpan(0.88f), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(
            new ForegroundColorSpan(color(guideBodyTextColorResForLegacy())),
            lineStart,
            lineEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }

    private void styleGuideManualMetaLine(SpannableStringBuilder styled, int lineStart, int lineEnd) {
        styled.setSpan(new StyleSpan(Typeface.BOLD), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new TypefaceSpan("monospace"), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new RelativeSizeSpan(0.62f), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(
            new ForegroundColorSpan(color(guideAnchorValueColorResForLegacy())),
            lineStart,
            lineEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
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
        styled.setSpan(new RelativeSizeSpan(0.70f), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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

    private void styleGuideAdmonitionLabelLine(
        SpannableStringBuilder styled,
        int lineStart,
        int lineEnd,
        String label,
        int accentColorRes
    ) {
        int labelEnd = Math.min(lineEnd, lineStart + safe(label).length());
        styled.setSpan(
            new BackgroundColorSpan(color(guideAdmonitionBackgroundColorResForLegacy())),
            lineStart,
            lineEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        styled.setSpan(new StyleSpan(Typeface.BOLD), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new RelativeSizeSpan(0.70f), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (labelEnd > lineStart) {
            styled.setSpan(new TypefaceSpan("monospace"), lineStart, labelEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            styled.setSpan(new ForegroundColorSpan(color(accentColorRes)), lineStart, labelEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void styleGuideTextLine(SpannableStringBuilder styled, int lineStart, int lineEnd) {
        if (lineStart >= lineEnd) {
            return;
        }
        styled.setSpan(new RelativeSizeSpan(0.74f), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(
            new ForegroundColorSpan(color(guideBodyTextColorResForLegacy())),
            lineStart,
            lineEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
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
        styled.setSpan(new RelativeSizeSpan(0.74f), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
        if (sourceAlreadyStartsWithSection(source, section)) {
            return GuideBodySanitizer.sanitizeGuideBodyForDisplay(source);
        }
        return GuideBodySanitizer.sanitizeGuideBodyForDisplay("## " + section + "\n\n" + source);
    }

    private static boolean sourceAlreadyStartsWithSection(String source, String section) {
        String firstLine = "";
        String[] lines = safe(source).split("\\n", -1);
        for (String line : lines) {
            firstLine = safe(line).trim();
            if (!firstLine.isEmpty()) {
                break;
            }
        }
        if (firstLine.isEmpty()) {
            return false;
        }
        String normalizedFirstLine = normalizeSectionComparisonLine(firstLine);
        String normalizedSection = normalizeSectionComparisonLine(section);
        return !normalizedSection.isEmpty() && normalizedFirstLine.equals(normalizedSection);
    }

    private static String prependGuidePaperHeader(SearchResult result, String body, int inferredSectionCount) {
        String cleanedBody = safe(body).trim();
        if (cleanedBody.isEmpty()) {
            return "";
        }
        String title = GuideBodySanitizer.normalizeGuideDisplayText(safe(result == null ? null : result.title)).trim();
        String guideId = safe(result == null ? null : result.guideId).trim();
        if (title.isEmpty() && guideId.isEmpty()) {
            return cleanedBody;
        }
        StringBuilder builder = new StringBuilder(cleanedBody.length() + title.length() + 96);
        builder.append("FIELD MANUAL \u00b7 REV 04-27 \u00b7 PK 2\n");
        if (!title.isEmpty()) {
            builder.append(title).append('\n');
        }
        if (!guideId.isEmpty()) {
            builder.append(guideId).append(" \u00b7 ")
                .append(formatGuideSectionCount(Math.max(countGuideSections(cleanedBody), inferredSectionCount)))
                .append('\n');
        }
        builder.append('\n').append(cleanedBody);
        return builder.toString().trim();
    }

    private static int inferGuideSectionCount(String sourceText, String displayBody) {
        int displaySections = countGuideSections(displayBody);
        int sourceSections = countRawGuideSections(sourceText);
        int frontMatterRelatedCount = countFrontMatterListEntries(sourceText, "related");
        return Math.max(displaySections, Math.max(sourceSections, frontMatterRelatedCount));
    }

    private static int countGuideSections(String body) {
        GuideBodySanitizer.ParsedGuideBody parsedBody = GuideBodySanitizer.parseGuideBodyForDisplay(body);
        int sections = 0;
        for (GuideBodySanitizer.GuideBodyLine line : parsedBody.lines) {
            if (line.kind == GuideBodySanitizer.GuideBodyLine.Kind.SECTION) {
                sections++;
            }
        }
        return Math.max(1, sections);
    }

    private static int countRawGuideSections(String body) {
        String cleaned = safe(body);
        if (cleaned.trim().isEmpty()) {
            return 0;
        }
        int sections = 0;
        String[] lines = cleaned.split("\\n", -1);
        for (String line : lines) {
            String trimmed = safe(line).trim();
            if (trimmed.matches("(?i)^<section\\b.*")) {
                sections++;
            } else if (trimmed.matches("^##\\s+.+")) {
                sections++;
            } else if (trimmed.startsWith("[[SECTION]] ")
                || trimmed.matches("(?i)^Source section:\\s*.+$")
                || GuideBodySanitizer.isGuideSectionDisplayLine(trimmed)) {
                sections++;
            }
        }
        return sections;
    }

    private static int countFrontMatterListEntries(String body, String key) {
        String cleaned = safe(body).trim();
        String safeKey = safe(key).trim();
        if (cleaned.isEmpty() || safeKey.isEmpty() || !cleaned.startsWith("---\n")) {
            return 0;
        }
        String[] lines = cleaned.split("\\n", -1);
        boolean insideFrontMatter = false;
        boolean insideTargetList = false;
        int count = 0;
        for (String line : lines) {
            String trimmed = safe(line).trim();
            if ("---".equals(trimmed)) {
                if (!insideFrontMatter) {
                    insideFrontMatter = true;
                    continue;
                }
                break;
            }
            if (!insideFrontMatter) {
                continue;
            }
            if (trimmed.equals(safeKey + ":")) {
                insideTargetList = true;
                continue;
            }
            if (insideTargetList && trimmed.startsWith("- ")) {
                count++;
                continue;
            }
            if (insideTargetList && !trimmed.isEmpty() && !line.startsWith(" ") && trimmed.endsWith(":")) {
                insideTargetList = false;
            }
        }
        return count;
    }

    private static String formatGuideSectionCount(int sections) {
        int safeSections = Math.max(1, sections);
        return safeSections + (safeSections == 1 ? " SECTION" : " SECTIONS");
    }

    private static boolean isGuideManualKickerLine(String line) {
        return GUIDE_MANUAL_KICKER_PATTERN.matcher(safe(line).trim()).matches();
    }

    private static boolean isGuideManualMetaLine(String line) {
        return GUIDE_MANUAL_META_PATTERN.matcher(safe(line).trim()).matches();
    }

    private static boolean isGuideManualTitleLine(
        GuideBodySanitizer.GuideBodyLine[] lines,
        GuideBodySanitizer.GuideBodyLine line
    ) {
        if (lines == null || lines.length < 2 || line == null || line.kind != GuideBodySanitizer.GuideBodyLine.Kind.TEXT) {
            return false;
        }
        return line == lines[1] && isGuideManualKickerLine(lines[0].text);
    }

    private static String normalizeSectionComparisonLine(String line) {
        return safe(line)
            .trim()
            .replaceFirst("^#{1,6}\\s+", "")
            .replaceFirst("(?i)^(?:Section\\s+|\\u00a7\\s*)\\d+\\s*[:\\-\\u00b7]?\\s*", "")
            .replaceAll("\\s+", " ")
            .trim()
            .toLowerCase(Locale.US);
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
