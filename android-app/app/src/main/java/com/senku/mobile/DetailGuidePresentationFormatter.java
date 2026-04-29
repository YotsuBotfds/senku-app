package com.senku.mobile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineBackgroundSpan;
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
    private static final Pattern GUIDE_TABLET_SECTION_PREFIX_PATTERN =
        Pattern.compile("^SEC\\s+\\d+$", Pattern.CASE_INSENSITIVE);
    private static final Pattern GUIDE_OPENING_DANGER_TITLE_PATTERN =
        Pattern.compile("^EXTREME\\s+BURN\\s+HAZARD\\s*[:\\-\\u00b7]?\\s*(.*)$", Pattern.CASE_INSENSITIVE);
    private static final String[] GUIDE_REQUIRED_READING_PRIORITY_SLUGS = new String[]{
        "abrasives-manufacturing",
        "bellows-forge-blower-construction",
        "bloomery-furnace"
    };
    private static final int FOUNDRY_LIVE_RELATED_SECTION_COUNT = 17;
    private static final float GUIDE_ANCHOR_TEXT_SIZE = 0.68f;
    private static final float GUIDE_HEADING_TEXT_SIZE = 0.94f;
    private static final float GUIDE_MANUAL_KICKER_TEXT_SIZE = 0.56f;
    private static final float GUIDE_MANUAL_TITLE_TEXT_SIZE = 1.08f;
    private static final float GUIDE_MANUAL_META_TEXT_SIZE = 0.58f;
    private static final float GUIDE_REQUIRED_READING_TEXT_SIZE = 0.78f;
    private static final float GUIDE_ADMONITION_LABEL_TEXT_SIZE = 0.68f;
    private static final float GUIDE_BODY_TEXT_SIZE = 0.84f;
    private static final int GUIDE_SECTION_LABEL_MARGIN_DP = 5;
    private static final int GUIDE_ADMONITION_MARGIN_DP = 5;
    private static final int GUIDE_ADMONITION_HANGING_MARGIN_DP = 9;
    private static final int GUIDE_ADMONITION_ACCENT_WIDTH_DP = 2;
    private static final int GUIDE_REQUIRED_READING_MARGIN_DP = 5;
    private static final int GUIDE_REQUIRED_READING_ACCENT_WIDTH_DP = 2;
    private static final int GUIDE_REQUIRED_READING_RIGHT_INSET_DP = 16;
    private static final String GUIDE_ROW_CHEVRON = "\u203a";

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
            body = polishGuideDisplayBody(body);
            body = applyGuideRequiredReadingRows(result, sourceBody, body);
            return prependGuidePaperHeader(result, body, inferGuideSectionCount(result, sourceBody, body));
        }
        String sourceSnippet = safe(result.snippet);
        String snippetBody = buildGuideBodyWithSection(sectionHeading, sourceSnippet);
        snippetBody = polishGuideDisplayBody(snippetBody);
        snippetBody = applyGuideRequiredReadingRows(result, sourceSnippet, snippetBody);
        return prependGuidePaperHeader(result, snippetBody, inferGuideSectionCount(result, sourceSnippet, snippetBody));
    }

    static int inferGuideSectionCountForRail(SearchResult result, String sourceText, String displayBody) {
        return inferGuideSectionCount(result, sourceText, displayBody);
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
        GuideBodySanitizer.ParsedGuideBody parsedBody =
            GuideBodySanitizer.parseGuideBodyForDisplay(polishGuideDisplayBody(body));
        String cleaned = parsedBody.displayText;
        SpannableStringBuilder styled = new SpannableStringBuilder(cleaned);
        if (cleaned.isEmpty()) {
            return styled;
        }
        String displayText = styled.toString();
        boolean insideAdmonitionBlock = false;
        boolean nextTextLineMayBeRecoveredHeading = false;
        int admonitionAccentColorRes = guideAdmonitionWarningColorResForLegacy();
        int cursor = 0;
        for (GuideBodySanitizer.GuideBodyLine line : parsedBody.lines) {
            int lineStart = cursor;
            int lineEnd = cursor + line.text.length();
            String trimmed = line.text.trim();
            if (line.kind == GuideBodySanitizer.GuideBodyLine.Kind.SECTION) {
                styleGuideAnchorLine(styled, displayText, lineStart, lineEnd, line.label);
                insideAdmonitionBlock = false;
                nextTextLineMayBeRecoveredHeading = true;
            } else if (isGuideManualKickerLine(trimmed)) {
                styleGuideManualKickerLine(styled, lineStart, lineEnd);
                insideAdmonitionBlock = false;
                nextTextLineMayBeRecoveredHeading = false;
            } else if (isTabletSectionPrefixLine(trimmed)) {
                styleGuideTabletSectionPrefixLine(styled, lineStart, lineEnd);
                insideAdmonitionBlock = false;
                nextTextLineMayBeRecoveredHeading = false;
            } else if (isGuideManualMetaLine(trimmed)) {
                styleGuideManualMetaLine(styled, lineStart, lineEnd);
                insideAdmonitionBlock = false;
                nextTextLineMayBeRecoveredHeading = false;
            } else if (isGuideManualTitleLine(parsedBody.lines, line)) {
                styleGuideManualTitleLine(styled, lineStart, lineEnd);
                insideAdmonitionBlock = false;
                nextTextLineMayBeRecoveredHeading = false;
            } else if (line.kind == GuideBodySanitizer.GuideBodyLine.Kind.REQUIRED_READING) {
                styleRequiredReadingLine(styled, displayText, lineStart, lineEnd, line.label);
                insideAdmonitionBlock = false;
                nextTextLineMayBeRecoveredHeading = false;
            } else if (line.kind == GuideBodySanitizer.GuideBodyLine.Kind.HEADING) {
                styleGuideHeadingLine(styled, lineStart, lineEnd);
                insideAdmonitionBlock = false;
                nextTextLineMayBeRecoveredHeading = false;
            } else if (line.kind == GuideBodySanitizer.GuideBodyLine.Kind.ADMONITION_LABEL) {
                admonitionAccentColorRes = admonitionAccentColorRes(trimmed);
                styleGuideAdmonitionLabelLine(styled, lineStart, lineEnd, line.label, admonitionAccentColorRes);
                insideAdmonitionBlock = true;
                nextTextLineMayBeRecoveredHeading = false;
            } else {
                int prefixEnd = guideAdmonitionPrefixEnd(trimmed);
                if (prefixEnd > 0) {
                    int spanEnd = lineStart + prefixEnd;
                    styled.setSpan(new StyleSpan(Typeface.BOLD), lineStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styled.setSpan(new TypefaceSpan("monospace"), lineStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styled.setSpan(
                        new RelativeSizeSpan(GUIDE_ADMONITION_LABEL_TEXT_SIZE),
                        lineStart,
                        spanEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
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
                            new GuideRowBackgroundSpan(
                                admonitionInsetBackgroundColor(admonitionAccentColorRes),
                                color(admonitionAccentColorRes),
                                dp(GUIDE_ADMONITION_ACCENT_WIDTH_DP),
                                0,
                                true,
                                false,
                                0,
                                color(guideBodyTextColorResForLegacy())
                            ),
                            lineStart,
                            lineEnd,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                    } else {
                        applyGuideIndentedWarningLine(styled, lineStart, lineEnd, admonitionAccentColorRes);
                    }
                    insideAdmonitionBlock = true;
                    nextTextLineMayBeRecoveredHeading = false;
                } else if (trimmed.isEmpty()) {
                    insideAdmonitionBlock = false;
                    nextTextLineMayBeRecoveredHeading = false;
                } else if (insideAdmonitionBlock) {
                    applyGuideIndentedWarningLine(styled, lineStart, lineEnd, admonitionAccentColorRes);
                    nextTextLineMayBeRecoveredHeading = false;
                } else if (nextTextLineMayBeRecoveredHeading && isRecoveredGuideSectionHeading(trimmed)) {
                    styleGuideHeadingLine(styled, lineStart, lineEnd);
                    nextTextLineMayBeRecoveredHeading = false;
                } else {
                    styleGuideTextLine(styled, lineStart, lineEnd);
                    nextTextLineMayBeRecoveredHeading = false;
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

    static float guideBodyTextSizeForLegacy() {
        return GUIDE_BODY_TEXT_SIZE;
    }

    static float guideManualTitleTextSizeForLegacy() {
        return GUIDE_MANUAL_TITLE_TEXT_SIZE;
    }

    static float guideRequiredReadingTextSizeForLegacy() {
        return GUIDE_REQUIRED_READING_TEXT_SIZE;
    }

    static float guideAdmonitionLabelTextSizeForLegacy() {
        return GUIDE_ADMONITION_LABEL_TEXT_SIZE;
    }

    static float guideAnchorTextSizeForLegacy() {
        return GUIDE_ANCHOR_TEXT_SIZE;
    }

    static int guideAdmonitionAccentWidthDpForLegacy() {
        return GUIDE_ADMONITION_ACCENT_WIDTH_DP;
    }

    static int guideRequiredReadingRightInsetDpForLegacy() {
        return GUIDE_REQUIRED_READING_RIGHT_INSET_DP;
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
        styled.setSpan(new RelativeSizeSpan(GUIDE_ANCHOR_TEXT_SIZE), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(
            new LeadingMarginSpan.Standard(dp(GUIDE_SECTION_LABEL_MARGIN_DP), dp(GUIDE_SECTION_LABEL_MARGIN_DP)),
            lineStart,
            lineEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        styled.setSpan(
            new GuideRowBackgroundSpan(
                color(guideAdmonitionBackgroundColorResForLegacy()),
                color(guideAnchorValueColorResForLegacy()),
                0,
                0,
                false,
                false,
                0,
                color(guideAnchorValueColorResForLegacy())
            ),
            lineStart,
            lineEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
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
        styled.setSpan(new RelativeSizeSpan(GUIDE_HEADING_TEXT_SIZE), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
        styled.setSpan(new RelativeSizeSpan(GUIDE_MANUAL_KICKER_TEXT_SIZE), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
        styled.setSpan(new RelativeSizeSpan(GUIDE_MANUAL_TITLE_TEXT_SIZE), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
        styled.setSpan(new RelativeSizeSpan(GUIDE_MANUAL_META_TEXT_SIZE), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
        styled.setSpan(
            new LeadingMarginSpan.Standard(dp(GUIDE_REQUIRED_READING_MARGIN_DP), dp(GUIDE_REQUIRED_READING_MARGIN_DP)),
            lineStart,
            lineEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        styled.setSpan(
            new GuideRowBackgroundSpan(
                color(guideAdmonitionBackgroundColorResForLegacy()),
                color(guideAdmonitionWarningColorResForLegacy()),
                dp(GUIDE_REQUIRED_READING_ACCENT_WIDTH_DP),
                0,
                true,
                true,
                dp(GUIDE_REQUIRED_READING_RIGHT_INSET_DP),
                color(guideAnchorValueColorResForLegacy())
            ),
            lineStart,
            lineEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        styled.setSpan(new StyleSpan(Typeface.BOLD), lineStart, labelEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new TypefaceSpan("monospace"), lineStart, labelEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new RelativeSizeSpan(GUIDE_REQUIRED_READING_TEXT_SIZE), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
            new LeadingMarginSpan.Standard(dp(GUIDE_ADMONITION_MARGIN_DP), dp(GUIDE_ADMONITION_MARGIN_DP)),
            lineStart,
            lineEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        styled.setSpan(
            new GuideRowBackgroundSpan(
                admonitionInsetBackgroundColor(accentColorRes),
                color(accentColorRes),
                dp(GUIDE_ADMONITION_ACCENT_WIDTH_DP),
                dp(1),
                true,
                false,
                0,
                color(guideBodyTextColorResForLegacy())
            ),
            lineStart,
            lineEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        styled.setSpan(new StyleSpan(Typeface.BOLD), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new RelativeSizeSpan(GUIDE_ADMONITION_LABEL_TEXT_SIZE), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new TypefaceSpan("monospace"), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (labelEnd > lineStart) {
            styled.setSpan(new ForegroundColorSpan(color(accentColorRes)), lineStart, labelEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        int valueStart = firstNonWhitespaceIndex(styled.toString(), labelEnd, lineEnd);
        if (valueStart < lineEnd) {
            styled.setSpan(
                new ForegroundColorSpan(color(guideAnchorValueColorResForLegacy())),
                valueStart,
                lineEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }

    private void styleGuideTextLine(SpannableStringBuilder styled, int lineStart, int lineEnd) {
        if (lineStart >= lineEnd) {
            return;
        }
        styled.setSpan(new RelativeSizeSpan(GUIDE_BODY_TEXT_SIZE), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
            new LeadingMarginSpan.Standard(
                dp(GUIDE_ADMONITION_MARGIN_DP),
                dp(GUIDE_ADMONITION_HANGING_MARGIN_DP)
            ),
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
        styled.setSpan(new RelativeSizeSpan(GUIDE_BODY_TEXT_SIZE), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(
            new GuideRowBackgroundSpan(
                admonitionInsetBackgroundColor(accentColorRes),
                color(accentColorRes),
                dp(GUIDE_ADMONITION_ACCENT_WIDTH_DP),
                0,
                false,
                false,
                0,
                color(guideBodyTextColorResForLegacy())
            ),
            lineStart,
            lineEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }

    private int admonitionInsetBackgroundColor(int accentColorRes) {
        float overlayRatio = accentColorRes == guideAdmonitionDangerColorResForLegacy() ? 0.18f : 0.14f;
        return blendColors(color(guideAdmonitionBackgroundColorResForLegacy()), color(accentColorRes), overlayRatio);
    }

    private static int blendColors(int baseColor, int overlayColor, float overlayRatio) {
        float ratio = Math.max(0f, Math.min(1f, overlayRatio));
        float inverse = 1f - ratio;
        return Color.rgb(
            Math.round(Color.red(baseColor) * inverse + Color.red(overlayColor) * ratio),
            Math.round(Color.green(baseColor) * inverse + Color.green(overlayColor) * ratio),
            Math.round(Color.blue(baseColor) * inverse + Color.blue(overlayColor) * ratio)
        );
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

    private static String polishGuideDisplayBody(String body) {
        String cleaned = safe(body).replace("\r\n", "\n").replace('\r', '\n').trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        String[] lines = cleaned.split("\\n", -1);
        StringBuilder builder = new StringBuilder(cleaned.length());
        boolean compactedOpeningDanger = false;
        for (int i = 0; i < lines.length; i++) {
            String line = safe(lines[i]);
            String trimmed = line.trim();
            if (isGuideUiLeakageLine(trimmed)) {
                continue;
            }
            if (!compactedOpeningDanger && "DANGER".equalsIgnoreCase(trimmed) && i + 1 < lines.length) {
                String dangerBody = safe(lines[i + 1]).trim();
                Matcher dangerMatcher = GUIDE_OPENING_DANGER_TITLE_PATTERN.matcher(dangerBody);
                if (dangerMatcher.matches()) {
                    appendLine(builder, "DANGER \u00b7 EXTREME BURN HAZARD");
                    appendLine(builder, compactOpeningDangerDetail(safe(dangerMatcher.group(1)).trim()));
                    i++;
                    compactedOpeningDanger = true;
                    continue;
                }
            }
            if ("WARNING".equalsIgnoreCase(trimmed) && i + 1 < lines.length) {
                String warningBody = safe(lines[i + 1]).trim();
                String compactedRequiredReading = compactStandaloneRequiredReading(warningBody);
                if (!compactedRequiredReading.isEmpty()) {
                    trimTrailingLineBreaks(builder);
                    appendLine(builder, compactedRequiredReading);
                    i++;
                    if (i + 1 < lines.length && safe(lines[i + 1]).trim().isEmpty()) {
                        i++;
                    }
                    continue;
                }
            }
            appendLine(builder, line);
        }
        return builder.toString().trim();
    }

    private static boolean isGuideUiLeakageLine(String line) {
        String normalized = safe(line).trim().toLowerCase(Locale.US);
        if (normalized.isEmpty()) {
            return false;
        }
        return normalized.equals("show proof")
            || normalized.equals("hide proof")
            || normalized.equals("source proof")
            || normalized.equals("sources proof")
            || normalized.equals("sources and proof")
            || normalized.matches("^(?:answer|confidence|evidence|proof|why proof)\\s*[:\\-\\u00b7].*");
    }

    private static void trimTrailingLineBreaks(StringBuilder builder) {
        while (builder.length() > 0 && builder.charAt(builder.length() - 1) == '\n') {
            builder.deleteCharAt(builder.length() - 1);
        }
    }

    private static String compactOpeningDangerDetail(String detail) {
        String cleaned = safe(detail).trim();
        return cleaned.replaceAll("\\s+", " ");
    }

    private static String compactStandaloneRequiredReading(String line) {
        String cleaned = safe(line).trim();
        if (!cleaned.toLowerCase(Locale.US).startsWith("required reading")) {
            return "";
        }
        String detail = cleaned.replaceFirst("(?i)^required\\s+reading\\s*[:\\-\\u00b7]?\\s*", "").trim();
        String normalized = detail.toLowerCase(Locale.US);
        if (normalized.startsWith("before attempting any procedures in this guide, read the chemical safety guide")
            || normalized.startsWith("before attempting this guide, read the chemical safety guide")) {
            return "REQUIRED READING \u00b7 Chemical Safety Guide";
        }
        return detail.isEmpty() ? "REQUIRED READING" : "REQUIRED READING \u00b7 " + detail;
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
                .append(formatGuideSectionCount(inferredSectionCount));
            String openedFrom = guidePaperOpenedFromLabel(result);
            if (!openedFrom.isEmpty()) {
                builder.append(" \u00b7 ").append(openedFrom);
            }
            builder.append('\n');
        }
        builder.append('\n').append(cleanedBody);
        return builder.toString().trim();
    }

    private static String guidePaperOpenedFromLabel(SearchResult result) {
        String guideId = safe(result == null ? null : result.guideId).trim();
        if ("GD-132".equalsIgnoreCase(guideId)) {
            return "OPENED FROM GD-220";
        }
        return "";
    }

    private static int inferGuideSectionCount(SearchResult result, String sourceText, String displayBody) {
        int displaySections = countGuideSections(displayBody);
        int sourceSections = countRawGuideSections(sourceText);
        int frontMatterRelatedCount = countFrontMatterListEntries(sourceText, "related");
        if (isFoundryGuide(result) && frontMatterRelatedCount > 0) {
            return frontMatterRelatedCount;
        }
        if (isFoundryGuide(result) && sourceSections >= FOUNDRY_LIVE_RELATED_SECTION_COUNT) {
            return FOUNDRY_LIVE_RELATED_SECTION_COUNT;
        }
        return Math.max(displaySections, sourceSections);
    }

    private static boolean isFoundryGuide(SearchResult result) {
        String guideId = safe(result == null ? null : result.guideId).trim();
        String title = safe(result == null ? null : result.title).trim().toLowerCase(Locale.US);
        return "GD-132".equalsIgnoreCase(guideId) || title.contains("foundry");
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
        String cleaned = safe(body).replace("\r\n", "\n").replace('\r', '\n').trim();
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

    private static String applyGuideRequiredReadingRows(SearchResult result, String sourceText, String displayBody) {
        String cleanedBody = safe(displayBody).trim();
        if (cleanedBody.isEmpty()) {
            return cleanedBody;
        }
        String requiredRows = buildGuideRequiredReadingRows(result, sourceText);
        if (requiredRows.isEmpty()) {
            return cleanedBody;
        }
        String requiredBlock = isFoundryGuide(result)
            ? "\u2014 \u00a7 2 \u00b7 REQUIRED READING\n" + requiredRows
            : requiredRows;
        String[] lines = cleanedBody.split("\\n", -1);
        StringBuilder builder = new StringBuilder(cleanedBody.length() + requiredBlock.length() + 16);
        boolean insertedAfterOpeningSection = false;
        int seenSections = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();
            if (isRequiredReadingDisplayLine(trimmed)) {
                continue;
            }
            if (GuideBodySanitizer.isGuideSectionDisplayLine(trimmed)) {
                seenSections++;
            }
            if (!insertedAfterOpeningSection && seenSections > 1) {
                appendRequiredReadingRows(builder, requiredBlock);
                insertedAfterOpeningSection = true;
            }
            appendLine(builder, line);
        }
        if (!insertedAfterOpeningSection) {
            appendRequiredReadingRows(builder, requiredBlock);
        }
        return builder.toString().trim();
    }

    private static String buildGuideRequiredReadingRows(SearchResult result, String sourceText) {
        String guideId = safe(result == null ? null : result.guideId).trim();
        if (!"GD-132".equals(guideId)) {
            return "";
        }
        java.util.ArrayList<String> slugs = frontMatterListEntries(sourceText, "related");
        if (slugs.isEmpty() && isFoundryGuide(result)) {
            slugs.add("abrasives-manufacturing");
            slugs.add("bellows-forge-blower-construction");
            slugs.add("bloomery-furnace");
        } else if (slugs.isEmpty()) {
            return "";
        }
        java.util.HashSet<String> availableSlugs = new java.util.HashSet<>();
        for (String slug : slugs) {
            availableSlugs.add(slug.toLowerCase(Locale.US));
        }
        StringBuilder builder = new StringBuilder();
        for (String slug : GUIDE_REQUIRED_READING_PRIORITY_SLUGS) {
            if (!availableSlugs.contains(slug)) {
                continue;
            }
            String row = requiredReadingRowForSlug(slug);
            if (row.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(row);
        }
        return builder.toString();
    }

    private static java.util.ArrayList<String> frontMatterListEntries(String body, String key) {
        java.util.ArrayList<String> entries = new java.util.ArrayList<>();
        String cleaned = safe(body).replace("\r\n", "\n").replace('\r', '\n').trim();
        String safeKey = safe(key).trim();
        if (cleaned.isEmpty() || safeKey.isEmpty() || !cleaned.startsWith("---\n")) {
            return entries;
        }
        String[] lines = cleaned.split("\\n", -1);
        boolean insideFrontMatter = false;
        boolean insideTargetList = false;
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
                entries.add(trimmed.substring(2).trim());
                continue;
            }
            if (insideTargetList && !trimmed.isEmpty() && !line.startsWith(" ") && trimmed.endsWith(":")) {
                insideTargetList = false;
            }
        }
        return entries;
    }

    private static String requiredReadingRowForSlug(String slug) {
        String normalized = safe(slug).trim().toLowerCase(Locale.US);
        if ("abrasives-manufacturing".equals(normalized)) {
            return "GD-220 \u00b7 Abrasives Manufacturing";
        }
        if ("bellows-forge-blower-construction".equals(normalized)) {
            return "GD-499 \u00b7 Bellows & Forge Blower Construction";
        }
        if ("bloomery-furnace".equals(normalized)) {
            return "GD-225 \u00b7 Bloomery Furnace Construction";
        }
        return "";
    }

    private static boolean isRequiredReadingDisplayLine(String line) {
        return safe(line).trim().toUpperCase(Locale.US).startsWith("REQUIRED READING");
    }

    private static void appendLine(StringBuilder builder, String line) {
        if (builder.length() > 0) {
            builder.append('\n');
        }
        builder.append(safe(line));
    }

    private static void appendRequiredReadingRows(StringBuilder builder, String rows) {
        if (builder.length() > 0) {
            appendLine(builder, "");
        }
        appendLine(builder, rows);
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

    static boolean isTabletSectionPrefixLineForLegacy(String line) {
        return isTabletSectionPrefixLine(line);
    }

    static boolean isRecoveredGuideSectionHeadingForLegacy(String line) {
        return isRecoveredGuideSectionHeading(line);
    }

    private static boolean isTabletSectionPrefixLine(String line) {
        return GUIDE_TABLET_SECTION_PREFIX_PATTERN.matcher(safe(line).trim()).matches();
    }

    private static boolean isRecoveredGuideSectionHeading(String line) {
        String cleaned = safe(line).trim();
        return "Reviewed Answer-Card Boundary".equals(cleaned);
    }

    private void styleGuideTabletSectionPrefixLine(SpannableStringBuilder styled, int lineStart, int lineEnd) {
        styled.setSpan(new StyleSpan(Typeface.BOLD), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new TypefaceSpan("monospace"), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new RelativeSizeSpan(GUIDE_ANCHOR_TEXT_SIZE), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(
            new ForegroundColorSpan(color(guideAdmonitionWarningColorResForLegacy())),
            lineStart,
            lineEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
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

    private static final class GuideRowBackgroundSpan implements LineBackgroundSpan {
        private final int backgroundColor;
        private final int accentColor;
        private final int accentWidthPx;
        private final int verticalInsetPx;
        private final boolean drawBorder;
        private final boolean drawChevron;
        private final int rightInsetPx;
        private final int chevronColor;

        GuideRowBackgroundSpan(int backgroundColor, int accentColor, int accentWidthPx, int verticalInsetPx) {
            this(backgroundColor, accentColor, accentWidthPx, verticalInsetPx, false, false, 0, accentColor);
        }

        GuideRowBackgroundSpan(
            int backgroundColor,
            int accentColor,
            int accentWidthPx,
            int verticalInsetPx,
            boolean drawBorder,
            boolean drawChevron,
            int rightInsetPx,
            int chevronColor
        ) {
            this.backgroundColor = backgroundColor;
            this.accentColor = accentColor;
            this.accentWidthPx = Math.max(0, accentWidthPx);
            this.verticalInsetPx = Math.max(0, verticalInsetPx);
            this.drawBorder = drawBorder;
            this.drawChevron = drawChevron;
            this.rightInsetPx = Math.max(0, rightInsetPx);
            this.chevronColor = chevronColor;
        }

        @Override
        public void drawBackground(
            Canvas canvas,
            Paint paint,
            int left,
            int right,
            int top,
            int baseline,
            int bottom,
            CharSequence text,
            int start,
            int end,
            int lineNumber
        ) {
            int previousColor = paint.getColor();
            Paint.Style previousStyle = paint.getStyle();
            paint.setColor(backgroundColor);
            paint.setStyle(Paint.Style.FILL);
            int rowTop = top + verticalInsetPx;
            int rowBottom = bottom - verticalInsetPx;
            int rowRight = Math.max(left, right - rightInsetPx);
            canvas.drawRect(left, rowTop, right, rowBottom, paint);
            if (accentWidthPx > 0) {
                paint.setColor(accentColor);
                canvas.drawRect(left, rowTop, left + accentWidthPx, rowBottom, paint);
            }
            if (drawBorder) {
                paint.setColor(blendColors(backgroundColor, accentColor, 0.36f));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(Math.max(1f, accentWidthPx / 4f));
                canvas.drawRect(left, rowTop, right, rowBottom, paint);
                paint.setStyle(Paint.Style.FILL);
            }
            if (drawChevron) {
                float previousTextSize = paint.getTextSize();
                Typeface previousTypeface = paint.getTypeface();
                Paint.Align previousAlign = paint.getTextAlign();
                paint.setColor(chevronColor);
                paint.setTypeface(Typeface.DEFAULT_BOLD);
                paint.setTextAlign(Paint.Align.RIGHT);
                paint.setTextSize(previousTextSize * 1.18f);
                float chevronBaseline = baseline;
                canvas.drawText(GUIDE_ROW_CHEVRON, rowRight + rightInsetPx - Math.max(6f, accentWidthPx), chevronBaseline, paint);
                paint.setTextSize(previousTextSize);
                paint.setTypeface(previousTypeface);
                paint.setTextAlign(previousAlign);
            }
            paint.setStyle(previousStyle);
            paint.setColor(previousColor);
        }
    }
}
