package com.senku.mobile;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class GuideBodySanitizer {
    private static final Locale QUERY_LOCALE = Locale.US;
    private static final Pattern GUIDE_ADMONITION_FENCE_PATTERN = Pattern.compile("^:::\\s*([a-zA-Z]+)?\\s*$");
    private static final Pattern GUIDE_ADMONITION_LABEL_PATTERN = Pattern.compile("^(DANGER|WARNING|CAUTION|IMPORTANT|NOTE):?$");
    private static final Pattern GUIDE_ADMONITION_INLINE_PREFIX_PATTERN =
        Pattern.compile("^(DANGER|WARNING|CAUTION|IMPORTANT|NOTE|DANGEROUS)(?:\\s*[:.-]\\s*|\\s+)(.+)$");
    private static final Pattern GUIDE_MARKDOWN_HEADING_PATTERN = Pattern.compile("^#{1,6}\\s+");
    private static final Pattern GUIDE_SECTION_LINE_PATTERN = Pattern.compile("^Source section:\\s*(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern GUIDE_REQUIRED_READING_PATTERN = Pattern.compile("^Required Reading\\s*:\\s*(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern GUIDE_SECTION_DISPLAY_PATTERN = Pattern.compile("^Section\\s+\\d+\\s*[:\\-\\u00b7]?\\s+.+$", Pattern.CASE_INSENSITIVE);
    private static final Pattern GUIDE_LAYOUT_TAG_PATTERN = Pattern.compile("(?i)</?(section|div|span|p)\\b[^>]*>");
    private static final Pattern GUIDE_INLINE_STYLE_TAG_PATTERN = Pattern.compile("(?i)</?(strong|b|em|i|mark|small)\\b[^>]*>");
    private static final Pattern GUIDE_MARKDOWN_LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]+)\\)");
    private static final Pattern GUIDE_HTML_BREAK_PATTERN = Pattern.compile("(?i)<br\\s*/?>");
    private static final Pattern GUIDE_MARKDOWN_ESCAPE_PATTERN = Pattern.compile("\\\\([\\[\\]\\(\\)#*_`])");
    private static final String GUIDE_SECTION_ANCHOR_MARKER = "[[SECTION]] ";
    private static final String GUIDE_REQUIRED_READING_LABEL = "Required reading";

    private GuideBodySanitizer() {
    }

    static final class ParsedGuideBody {
        final String displayText;
        final GuideBodyLine[] lines;

        ParsedGuideBody(String displayText, GuideBodyLine[] lines) {
            this.displayText = safe(displayText);
            this.lines = lines == null ? new GuideBodyLine[0] : lines;
        }
    }

    static final class GuideBodyLine {
        enum Kind {
            TEXT,
            SECTION,
            ADMONITION_LABEL,
            ADMONITION_TEXT,
            REQUIRED_READING
        }

        final Kind kind;
        final String text;
        final String label;

        GuideBodyLine(Kind kind, String text, String label) {
            this.kind = kind == null ? Kind.TEXT : kind;
            this.text = safe(text);
            this.label = safe(label);
        }
    }

    static String sanitizeGuideBodyForDisplay(String body) {
        return parseGuideBodyForDisplay(body).displayText;
    }

    static ParsedGuideBody parseGuideBodyForDisplay(String body) {
        String cleaned = normalizeGuideDisplayText(safe(body)).trim();
        if (cleaned.isEmpty()) {
            return new ParsedGuideBody("", new GuideBodyLine[0]);
        }
        String[] lines = cleaned.split("\\n", -1);
        java.util.ArrayList<GuideBodyLine> parsedLines = new java.util.ArrayList<>(lines.length);
        StringBuilder builder = new StringBuilder(cleaned.length() + 32);
        boolean insideAdmonitionBlock = false;
        String activeAdmonitionLabel = "";
        boolean firstAdmonitionContentLine = false;
        int sectionOrdinal = 0;
        for (String rawLine : lines) {
            String trimmed = safe(rawLine).trim();
            boolean markdownHeading = GUIDE_MARKDOWN_HEADING_PATTERN.matcher(trimmed).find();
            Matcher fenceMatcher = GUIDE_ADMONITION_FENCE_PATTERN.matcher(trimmed);
            if (fenceMatcher.matches()) {
                String marker = safe(fenceMatcher.group(1)).trim();
                insideAdmonitionBlock = !marker.isEmpty();
                if (!marker.isEmpty()) {
                    activeAdmonitionLabel = normalizeGuideAdmonitionLabel(marker);
                    firstAdmonitionContentLine = true;
                    if (builder.length() > 0 && builder.charAt(builder.length() - 1) != '\n') {
                        builder.append('\n');
                    }
                    appendParsedLine(
                        builder,
                        parsedLines,
                        new GuideBodyLine(GuideBodyLine.Kind.ADMONITION_LABEL, activeAdmonitionLabel, activeAdmonitionLabel)
                    );
                } else {
                    activeAdmonitionLabel = "";
                    firstAdmonitionContentLine = false;
                }
                continue;
            }
            String displayLine = sanitizeGuideDisplayLine(rawLine, insideAdmonitionBlock);
            if (insideAdmonitionBlock && !displayLine.isEmpty()) {
                displayLine = stripDuplicateAdmonitionLabel(displayLine, activeAdmonitionLabel, firstAdmonitionContentLine);
            }
            displayLine = formatGuideAdmonitionLine(displayLine);
            boolean explicitSectionMarkerLine = isGuideSectionMarkerLine(displayLine);
            boolean sectionLine = !insideAdmonitionBlock
                && (markdownHeading || explicitSectionMarkerLine);
            String sectionValue = sectionLine ? extractGuideSectionValue(displayLine) : "";
            boolean requiredReadingLine = isRequiredReadingLine(displayLine);
            if (displayLine.isEmpty() && trimmed.isEmpty()) {
                appendBlankLine(builder, parsedLines);
                continue;
            }
            if (!displayLine.isEmpty()) {
                if (sectionLine && !sectionValue.isEmpty()) {
                    sectionOrdinal++;
                    appendParsedLine(
                        builder,
                        parsedLines,
                        new GuideBodyLine(
                            GuideBodyLine.Kind.SECTION,
                            buildGuideSectionDisplayLabel(sectionOrdinal, sectionValue),
                            buildGuideSectionPrefix(sectionOrdinal)
                        )
                    );
                } else if (requiredReadingLine) {
                    appendParsedLine(
                        builder,
                        parsedLines,
                        new GuideBodyLine(
                            GuideBodyLine.Kind.REQUIRED_READING,
                            formatRequiredReadingLine(displayLine),
                            GUIDE_REQUIRED_READING_LABEL
                        )
                    );
                } else {
                    appendParsedLine(
                        builder,
                        parsedLines,
                        new GuideBodyLine(
                            canonicalGuideAdmonitionLabel(displayLine).isEmpty()
                                ? (insideAdmonitionBlock ? GuideBodyLine.Kind.ADMONITION_TEXT : GuideBodyLine.Kind.TEXT)
                                : GuideBodyLine.Kind.ADMONITION_LABEL,
                            displayLine,
                            canonicalGuideAdmonitionLabel(displayLine)
                        )
                    );
                }
                if (insideAdmonitionBlock) {
                    firstAdmonitionContentLine = false;
                }
            }
        }
        while (!parsedLines.isEmpty() && parsedLines.get(parsedLines.size() - 1).text.isEmpty()) {
            parsedLines.remove(parsedLines.size() - 1);
        }
        collapseRepeatedBlankLines(parsedLines);
        String displayText = joinParsedLines(parsedLines);
        return new ParsedGuideBody(displayText, parsedLines.toArray(new GuideBodyLine[0]));
    }

    private static void collapseRepeatedBlankLines(java.util.ArrayList<GuideBodyLine> parsedLines) {
        boolean previousBlank = false;
        for (int i = 0; i < parsedLines.size(); i++) {
            boolean blank = parsedLines.get(i).text.isEmpty();
            if (blank && previousBlank) {
                parsedLines.remove(i);
                i--;
                continue;
            }
            previousBlank = blank;
        }
    }

    private static String joinParsedLines(java.util.ArrayList<GuideBodyLine> parsedLines) {
        StringBuilder builder = new StringBuilder();
        for (GuideBodyLine line : parsedLines) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(line.text);
        }
        return builder.toString().trim();
    }

    private static void appendParsedLine(
        StringBuilder builder,
        java.util.ArrayList<GuideBodyLine> parsedLines,
        GuideBodyLine line
    ) {
        if (builder.length() > 0 && builder.charAt(builder.length() - 1) != '\n') {
            builder.append('\n');
        }
        builder.append(line.text).append('\n');
        parsedLines.add(line);
    }

    private static void appendBlankLine(StringBuilder builder, java.util.ArrayList<GuideBodyLine> parsedLines) {
        builder.append('\n');
        parsedLines.add(new GuideBodyLine(GuideBodyLine.Kind.TEXT, "", ""));
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

    private static String formatGuideAdmonitionLine(String line) {
        String cleaned = safe(line).trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        String canonical = canonicalGuideAdmonitionLabel(cleaned);
        if (!canonical.isEmpty()) {
            return canonical;
        }
        Matcher inlinePrefixMatcher = GUIDE_ADMONITION_INLINE_PREFIX_PATTERN.matcher(cleaned);
        if (!inlinePrefixMatcher.matches()) {
            return cleaned;
        }
        String label = canonicalGuideAdmonitionLabel(inlinePrefixMatcher.group(1));
        if (label.isEmpty()) {
            return cleaned;
        }
        String detail = safe(inlinePrefixMatcher.group(2)).trim();
        if (detail.isEmpty()) {
            return label;
        }
        return label + " \u00b7 " + detail;
    }

    private static boolean isGuideSectionMarkerLine(String line) {
        String cleaned = safe(line).trim();
        return cleaned.startsWith(GUIDE_SECTION_ANCHOR_MARKER)
            || GUIDE_SECTION_LINE_PATTERN.matcher(cleaned).matches();
    }

    private static String extractGuideSectionValue(String line) {
        String cleaned = safe(line).trim();
        if (cleaned.startsWith(GUIDE_SECTION_ANCHOR_MARKER)) {
            return cleaned.substring(GUIDE_SECTION_ANCHOR_MARKER.length()).trim();
        }
        Matcher sourceSectionMatcher = GUIDE_SECTION_LINE_PATTERN.matcher(cleaned);
        if (sourceSectionMatcher.matches()) {
            return safe(sourceSectionMatcher.group(1)).trim();
        }
        if (isGuideSectionDisplayLine(cleaned)) {
            return cleaned.replaceFirst("(?i)^Section\\s+\\d+\\s+", "").trim();
        }
        return cleaned;
    }

    private static boolean isRequiredReadingLine(String line) {
        return GUIDE_REQUIRED_READING_PATTERN.matcher(safe(line).trim()).matches();
    }

    private static String formatRequiredReadingLine(String line) {
        Matcher matcher = GUIDE_REQUIRED_READING_PATTERN.matcher(safe(line).trim());
        if (!matcher.matches()) {
            return safe(line).trim();
        }
        String detail = safe(matcher.group(1)).trim();
        return detail.isEmpty() ? GUIDE_REQUIRED_READING_LABEL : GUIDE_REQUIRED_READING_LABEL + " \u00b7 " + detail;
    }

    static boolean isGuideSectionDisplayLine(String line) {
        return GUIDE_SECTION_DISPLAY_PATTERN.matcher(safe(line).trim()).matches();
    }

    private static String buildGuideSectionDisplayLabel(int sectionOrdinal, String value) {
        return buildGuideSectionPrefix(sectionOrdinal) + " " + safe(value).trim();
    }

    private static String buildGuideSectionPrefix(int sectionOrdinal) {
        return "Section " + Math.max(sectionOrdinal, 1);
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

    static String normalizeGuideDisplayText(String text) {
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
        cleaned = cleaned.replace("ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â", " - ");
        cleaned = cleaned.replace("ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“", "-");
        cleaned = cleaned.replace("ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢", "->");
        cleaned = cleaned.replace("ÃƒÂ¢Ã…Â¡Ã‚Â ÃƒÂ¯Ã‚Â¸Ã‚Â", "!");
        return DetailWarningCopySanitizer.sanitizeWarningResidualCopy(cleaned);
    }

    static String normalizeGuideAdmonitionLabel(String marker) {
        String normalized = safe(marker).trim().toLowerCase(QUERY_LOCALE);
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

    static String canonicalGuideAdmonitionLabel(String label) {
        String cleaned = safe(label).trim().toUpperCase(QUERY_LOCALE);
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

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
