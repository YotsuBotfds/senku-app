package com.senku.mobile;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class GuideBodySanitizer {
    private static final Locale QUERY_LOCALE = Locale.US;
    private static final Pattern GUIDE_ADMONITION_FENCE_PATTERN = Pattern.compile("^:::\\s*([a-zA-Z][a-zA-Z-]*)?\\s*$");
    private static final Pattern GUIDE_ADMONITION_LABEL_PATTERN = Pattern.compile("^(DANGER|WARNING|CAUTION|IMPORTANT|NOTE):?$");
    private static final Pattern GUIDE_ADMONITION_INLINE_PREFIX_PATTERN =
        Pattern.compile("^(DANGER|WARNING|CAUTION|IMPORTANT|NOTE|DANGEROUS)(?:\\s*[:.-]\\s*|\\s+)(.+)$");
    private static final Pattern GUIDE_MARKDOWN_HEADING_PATTERN = Pattern.compile("^#{1,6}\\s+");
    private static final Pattern GUIDE_MARKDOWN_SECTION_HEADING_PATTERN = Pattern.compile("^##\\s+");
    private static final Pattern GUIDE_SECTION_LINE_PATTERN = Pattern.compile("^Source section:\\s*(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern GUIDE_BRACKET_SECTION_LINE_PATTERN =
        Pattern.compile("^\\[\\[?/?SECTION\\]?\\]?\\s*(.*)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern GUIDE_REQUIRED_READING_PATTERN =
        Pattern.compile("^Required Reading\\s*[:\\-\\u00b7]\\s*(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern GUIDE_REQUIRED_READING_ROW_PATTERN =
        Pattern.compile("^GD-\\d+\\s+\\u00b7\\s+.+$");
    private static final Pattern GUIDE_SECTION_DISPLAY_PATTERN =
        Pattern.compile("^(?:[\\-\\u2013\\u2014]+\\s*)?(?:Section\\s+|\\u00a7\\s*)\\d+\\s*[:\\-\\u00b7]?\\s+.+$", Pattern.CASE_INSENSITIVE);
    private static final Pattern GUIDE_LAYOUT_TAG_PATTERN = Pattern.compile("(?i)</?(section|div|span|p)\\b[^>]*>");
    private static final Pattern GUIDE_INLINE_STYLE_TAG_PATTERN = Pattern.compile("(?i)</?(strong|b|em|i|mark|small)\\b[^>]*>");
    private static final Pattern GUIDE_HTML_COMMENT_PATTERN = Pattern.compile("(?s)<!--.*?-->");
    private static final Pattern GUIDE_MARKDOWN_IMAGE_PATTERN = Pattern.compile("!\\[([^\\]]*)\\]\\(([^\\)]+)\\)");
    private static final Pattern GUIDE_MARKDOWN_LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]+)\\)");
    private static final Pattern GUIDE_HTML_LINK_PATTERN = Pattern.compile("(?i)<a\\b[^>]*>(.*?)</a>");
    private static final Pattern GUIDE_HTML_BREAK_PATTERN = Pattern.compile("(?i)<br\\s*/?>");
    private static final Pattern GUIDE_HTML_TABLE_BOUNDARY_PATTERN =
        Pattern.compile("(?i)</?(?:tr|thead|tbody|tfoot|table)\\b[^>]*>");
    private static final Pattern GUIDE_HTML_CELL_BOUNDARY_PATTERN = Pattern.compile("(?i)</?(?:td|th)\\b[^>]*>");
    private static final Pattern GUIDE_HTML_TAG_PATTERN = Pattern.compile("(?i)</?[a-z][a-z0-9:-]*\\b[^>]*>");
    private static final Pattern GUIDE_MARKDOWN_ESCAPE_PATTERN = Pattern.compile("\\\\([\\[\\]\\(\\)#*_`])");
    private static final Pattern GUIDE_MARKDOWN_RULE_PATTERN = Pattern.compile("^\\s*(?:-{3,}|_{3,}|\\*{3,})\\s*$");
    private static final Pattern GUIDE_METADATA_LINE_PATTERN =
        Pattern.compile("(?i)^\\s*(title|slug|date|updated|source|tags|category|id)\\s*:\\s+.+$");
    private static final Pattern GUIDE_SEPARATOR_RUN_PATTERN = Pattern.compile("\\s*(?:\\u00b7\\s*){2,}");
    private static final String GUIDE_SECTION_ANCHOR_MARKER = "[[SECTION]] ";
    private static final String GUIDE_REQUIRED_READING_LABEL = "REQUIRED READING";

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
            REQUIRED_READING,
            HEADING
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
        String cleaned = stripGuideFrontMatter(normalizeGuideDisplayText(safe(body))).trim();
        if (cleaned.isEmpty()) {
            return new ParsedGuideBody("", new GuideBodyLine[0]);
        }
        String[] lines = cleaned.split("\\n", -1);
        java.util.ArrayList<GuideBodyLine> parsedLines = new java.util.ArrayList<>(lines.length);
        StringBuilder builder = new StringBuilder(cleaned.length() + 32);
        boolean insideAdmonitionBlock = false;
        String activeAdmonitionLabel = "";
        boolean firstAdmonitionContentLine = false;
        boolean pendingAdmonitionLabel = false;
        boolean compactedOpeningDanger = false;
        boolean insideReviewedBoundaryOpening = false;
        int reviewedBoundaryOpeningTextLines = 0;
        boolean truncateReviewedBoundaryOpening = false;
        int sectionOrdinal = 0;
        for (String rawLine : lines) {
            if (truncateReviewedBoundaryOpening) {
                break;
            }
            String trimmed = safe(rawLine).trim();
            if (shouldSkipGuideDisplayLine(trimmed)) {
                continue;
            }
            boolean markdownSectionHeading = GUIDE_MARKDOWN_SECTION_HEADING_PATTERN.matcher(trimmed).find();
            Matcher fenceMatcher = GUIDE_ADMONITION_FENCE_PATTERN.matcher(trimmed);
            if (fenceMatcher.matches()) {
                String marker = safe(fenceMatcher.group(1)).trim();
                insideAdmonitionBlock = !marker.isEmpty();
                if (!marker.isEmpty()) {
                    activeAdmonitionLabel = normalizeGuideAdmonitionLabel(marker);
                    firstAdmonitionContentLine = true;
                    pendingAdmonitionLabel = true;
                } else {
                    if (pendingAdmonitionLabel) {
                        appendParsedLine(
                            builder,
                            parsedLines,
                            new GuideBodyLine(
                                GuideBodyLine.Kind.ADMONITION_LABEL,
                                activeAdmonitionLabel,
                                activeAdmonitionLabel
                            )
                        );
                    }
                    activeAdmonitionLabel = "";
                    firstAdmonitionContentLine = false;
                    pendingAdmonitionLabel = false;
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
                && (markdownSectionHeading || explicitSectionMarkerLine || isGuideSectionDisplayLine(displayLine));
            String sectionValue = sectionLine ? extractGuideSectionValue(displayLine) : "";
            boolean requiredReadingLine = isRequiredReadingLine(displayLine);
            if (pendingAdmonitionLabel && !displayLine.isEmpty() && !requiredReadingLine) {
                AdmonitionTitleSplit split = splitAdmonitionTitle(displayLine);
                String labelText = activeAdmonitionLabel;
                if (!split.title.isEmpty()) {
                    labelText = activeAdmonitionLabel + " \u00b7 " + split.title;
                    displayLine = split.detail;
                }
                appendParsedLine(
                    builder,
                    parsedLines,
                    new GuideBodyLine(GuideBodyLine.Kind.ADMONITION_LABEL, labelText, activeAdmonitionLabel)
                );
                pendingAdmonitionLabel = false;
                firstAdmonitionContentLine = false;
                if (displayLine.isEmpty()) {
                    continue;
                }
            }
            if (displayLine.isEmpty() && trimmed.isEmpty()) {
                if (insideAdmonitionBlock) {
                    continue;
                }
                appendBlankLine(builder, parsedLines);
                continue;
            }
            if (!displayLine.isEmpty()) {
                if (sectionLine && !sectionValue.isEmpty()) {
                    if (insideReviewedBoundaryOpening && reviewedBoundaryOpeningTextLines > 0) {
                        truncateReviewedBoundaryOpening = true;
                        break;
                    }
                    insideReviewedBoundaryOpening = false;
                    sectionOrdinal++;
                    SectionDisplayParts sectionParts = buildSectionDisplayParts(sectionOrdinal, sectionValue);
                    appendParsedLine(
                        builder,
                        parsedLines,
                        new GuideBodyLine(
                            GuideBodyLine.Kind.SECTION,
                            sectionParts.label,
                            buildGuideSectionPrefix(sectionOrdinal)
                        )
                    );
                    if (!sectionParts.heading.isEmpty()) {
                        insideReviewedBoundaryOpening = "Reviewed Answer-Card Boundary".equals(sectionParts.heading);
                        reviewedBoundaryOpeningTextLines = 0;
                        appendParsedLine(
                            builder,
                            parsedLines,
                            new GuideBodyLine(
                                GuideBodyLine.Kind.HEADING,
                                sectionParts.heading,
                                sectionParts.heading
                            )
                        );
                    }
                } else if (requiredReadingLine) {
                    String requiredReadingDisplayLine = formatRequiredReadingLine(displayLine);
                    appendParsedLine(
                        builder,
                        parsedLines,
                        new GuideBodyLine(
                            GuideBodyLine.Kind.REQUIRED_READING,
                            requiredReadingDisplayLine,
                            requiredReadingLabelForDisplay(requiredReadingDisplayLine)
                        )
                    );
                    pendingAdmonitionLabel = false;
                } else {
                    if (insideReviewedBoundaryOpening) {
                        if (displayLine.isEmpty()) {
                            if (reviewedBoundaryOpeningTextLines > 0) {
                                truncateReviewedBoundaryOpening = true;
                            }
                            continue;
                        }
                        if (reviewedBoundaryOpeningTextLines >= 2) {
                            truncateReviewedBoundaryOpening = true;
                            break;
                        }
                        java.util.ArrayList<String> reviewedLines = splitReviewedBoundaryOpeningLines(
                            trimReviewedBoundaryOpeningBoilerplate(displayLine)
                        );
                        if (reviewedLines.isEmpty()) {
                            continue;
                        }
                        for (String reviewedLine : reviewedLines) {
                            if (reviewedBoundaryOpeningTextLines >= 2) {
                                truncateReviewedBoundaryOpening = true;
                                break;
                            }
                            appendParsedLine(
                                builder,
                                parsedLines,
                                new GuideBodyLine(GuideBodyLine.Kind.TEXT, reviewedLine, "")
                            );
                            reviewedBoundaryOpeningTextLines++;
                        }
                        continue;
                    }
                    if (insideAdmonitionBlock
                        && !compactedOpeningDanger
                        && "DANGER".equals(activeAdmonitionLabel)
                        && !displayLine.isEmpty()) {
                        displayLine = compactOpeningDangerLine(displayLine);
                        compactedOpeningDanger = true;
                    }
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
                    pendingAdmonitionLabel = false;
                }
            }
        }
        if (pendingAdmonitionLabel) {
            appendParsedLine(
                builder,
                parsedLines,
                new GuideBodyLine(GuideBodyLine.Kind.ADMONITION_LABEL, activeAdmonitionLabel, activeAdmonitionLabel)
            );
        }
        while (!parsedLines.isEmpty() && parsedLines.get(parsedLines.size() - 1).text.isEmpty()) {
            parsedLines.remove(parsedLines.size() - 1);
        }
        collapseRepeatedBlankLines(parsedLines);
        collapseRepeatedSectionLines(parsedLines);
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

    private static void collapseRepeatedSectionLines(java.util.ArrayList<GuideBodyLine> parsedLines) {
        String previousSectionValue = "";
        for (int i = 0; i < parsedLines.size(); i++) {
            GuideBodyLine line = parsedLines.get(i);
            if (line.kind != GuideBodyLine.Kind.SECTION) {
                if (!line.text.isEmpty()) {
                    previousSectionValue = "";
                }
                continue;
            }
            String sectionValue = normalizeSectionComparisonValue(line.text);
            if (!sectionValue.isEmpty() && sectionValue.equals(previousSectionValue)) {
                parsedLines.remove(i);
                i--;
                continue;
            }
            previousSectionValue = sectionValue;
        }
    }

    private static String normalizeSectionComparisonValue(String text) {
        return safe(text)
            .trim()
            .replaceFirst("^[\\-\\u2013\\u2014]+\\s*", "")
            .replaceFirst("(?i)^(?:Section\\s+|\\u00a7\\s*)\\d+\\s*[:\\-\\u00b7]?\\s*", "")
            .replaceAll("\\s+", " ")
            .trim()
            .toLowerCase(QUERY_LOCALE);
    }

    private static boolean shouldSkipGuideDisplayLine(String trimmedLine) {
        String cleaned = safe(trimmedLine).trim();
        return GUIDE_MARKDOWN_RULE_PATTERN.matcher(cleaned).matches()
            || GUIDE_METADATA_LINE_PATTERN.matcher(cleaned).matches()
            || isEmptyBracketSectionMarkerLine(cleaned);
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
        cleaned = GUIDE_HTML_COMMENT_PATTERN.matcher(cleaned).replaceAll(" ");
        cleaned = GUIDE_LAYOUT_TAG_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = GUIDE_INLINE_STYLE_TAG_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = GUIDE_MARKDOWN_HEADING_PATTERN.matcher(cleaned).replaceFirst("");
        cleaned = GUIDE_MARKDOWN_IMAGE_PATTERN.matcher(cleaned).replaceAll("$1");
        cleaned = GUIDE_MARKDOWN_LINK_PATTERN.matcher(cleaned).replaceAll("$1");
        cleaned = GUIDE_HTML_LINK_PATTERN.matcher(cleaned).replaceAll("$1");
        cleaned = GUIDE_HTML_TABLE_BOUNDARY_PATTERN.matcher(cleaned).replaceAll(" ");
        cleaned = GUIDE_HTML_CELL_BOUNDARY_PATTERN.matcher(cleaned).replaceAll(" \u00b7 ");
        cleaned = GUIDE_HTML_TAG_PATTERN.matcher(cleaned).replaceAll(" ");
        cleaned = cleaned.replace("**", "");
        cleaned = cleaned.replace("__", "");
        cleaned = cleaned.replace("`", "");
        if (cleaned.trim().startsWith("!")) {
            cleaned = cleaned.trim().substring(1).trim();
        }
        if (insideAdmonitionBlock) {
            cleaned = cleaned.replaceFirst("^[^\\p{Alnum}\\[]+\\s*", "");
        }
        return normalizeGuideSeparators(cleaned).trim();
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
        detail = trimAdmonitionInlineDetailPrefix(detail);
        detail = stripDuplicateAdmonitionDetailPrefix(detail, label);
        if (detail.isEmpty()) {
            return label;
        }
        return label + " \u00b7 " + detail;
    }

    private static String trimAdmonitionInlineDetailPrefix(String detail) {
        String cleaned = safe(detail).trim();
        while (!cleaned.isEmpty()) {
            char first = cleaned.charAt(0);
            if (first == '\u00b7' || first == '-' || first == ':' || first == '.') {
                cleaned = cleaned.substring(1).trim();
                continue;
            }
            break;
        }
        return cleaned;
    }

    private static boolean isGuideSectionMarkerLine(String line) {
        String cleaned = safe(line).trim();
        return cleaned.startsWith(GUIDE_SECTION_ANCHOR_MARKER)
            || GUIDE_SECTION_LINE_PATTERN.matcher(cleaned).matches()
            || GUIDE_BRACKET_SECTION_LINE_PATTERN.matcher(cleaned).matches();
    }

    private static String extractGuideSectionValue(String line) {
        String cleaned = safe(line).trim();
        if (cleaned.startsWith(GUIDE_SECTION_ANCHOR_MARKER)) {
            return cleaned.substring(GUIDE_SECTION_ANCHOR_MARKER.length()).trim();
        }
        Matcher bracketSectionMatcher = GUIDE_BRACKET_SECTION_LINE_PATTERN.matcher(cleaned);
        if (bracketSectionMatcher.matches()) {
            return safe(bracketSectionMatcher.group(1)).trim();
        }
        Matcher sourceSectionMatcher = GUIDE_SECTION_LINE_PATTERN.matcher(cleaned);
        if (sourceSectionMatcher.matches()) {
            return safe(sourceSectionMatcher.group(1)).trim();
        }
        if (isGuideSectionDisplayLine(cleaned)) {
            return cleaned
                .replaceFirst("^[\\-\\u2013\\u2014]+\\s*", "")
                .replaceFirst("(?i)^(?:Section\\s+|\\u00a7\\s*)\\d+\\s*[:\\-\\u00b7]?\\s+", "")
                .trim();
        }
        return cleaned;
    }

    private static boolean isEmptyBracketSectionMarkerLine(String line) {
        Matcher matcher = GUIDE_BRACKET_SECTION_LINE_PATTERN.matcher(safe(line).trim());
        return matcher.matches() && safe(matcher.group(1)).trim().isEmpty();
    }

    private static boolean isRequiredReadingLine(String line) {
        String cleaned = safe(line).trim();
        return GUIDE_REQUIRED_READING_PATTERN.matcher(cleaned).matches()
            || GUIDE_REQUIRED_READING_ROW_PATTERN.matcher(cleaned).matches();
    }

    private static String formatRequiredReadingLine(String line) {
        String cleaned = safe(line).trim();
        if (GUIDE_REQUIRED_READING_ROW_PATTERN.matcher(cleaned).matches()) {
            return cleaned;
        }
        Matcher matcher = GUIDE_REQUIRED_READING_PATTERN.matcher(cleaned);
        if (!matcher.matches()) {
            return cleaned;
        }
        String detail = safe(matcher.group(1)).trim();
        detail = detail.replaceFirst("(?i)^required\\s+reading\\s*[:\\-\\u00b7]?\\s*", "").trim();
        detail = compactRequiredReadingDetail(detail);
        return detail.isEmpty() ? GUIDE_REQUIRED_READING_LABEL : GUIDE_REQUIRED_READING_LABEL + " \u00b7 " + detail;
    }

    private static String requiredReadingLabelForDisplay(String line) {
        String cleaned = safe(line).trim();
        if (GUIDE_REQUIRED_READING_ROW_PATTERN.matcher(cleaned).matches()) {
            int separator = cleaned.indexOf('\u00b7');
            return separator > 0 ? cleaned.substring(0, separator).trim() : cleaned;
        }
        return GUIDE_REQUIRED_READING_LABEL;
    }

    private static String trimReviewedBoundaryOpeningBoilerplate(String line) {
        String cleaned = safe(line).trim();
        cleaned = cleaned.replaceFirst(
            "(?i)^This is the reviewed answer-card surface for\\s+GD-\\d+\\.\\s*",
            ""
        );
        cleaned = cleaned.replaceFirst("(?i)^Use it only for\\s+", "Use this section only for ");
        return cleaned.trim();
    }

    private static String compactRequiredReadingDetail(String detail) {
        String cleaned = safe(detail).trim();
        String normalized = cleaned.toLowerCase(QUERY_LOCALE);
        if (normalized.startsWith("before attempting any procedures in this guide, read the chemical safety guide")) {
            return "Chemical Safety Guide";
        }
        if (normalized.startsWith("before attempting this guide, read the chemical safety guide")) {
            return "Chemical Safety Guide";
        }
        return cleaned;
    }

    private static String compactOpeningDangerLine(String line) {
        String cleaned = safe(line).trim();
        String[] sentences = cleaned.split("(?<=[.!?])\\s+");
        if (cleaned.length() <= 220 && sentences.length <= 2) {
            return cleaned;
        }
        if (sentences.length < 2) {
            return cleaned;
        }
        StringBuilder builder = new StringBuilder();
        for (String sentence : sentences) {
            String candidate = safe(sentence).trim();
            if (candidate.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(sentenceCaseLeadingEvery(candidate));
            if (candidate.toUpperCase(QUERY_LOCALE).startsWith("EVERY ")) {
                break;
            }
            if (builder.length() >= 130) {
                if (nextSentenceStartsWithEvery(sentences, sentence)) {
                    continue;
                }
                break;
            }
        }
        return builder.length() == 0 ? cleaned : builder.toString();
    }

    private static String sentenceCaseLeadingEvery(String sentence) {
        String cleaned = safe(sentence).trim();
        if (cleaned.startsWith("EVERY ")) {
            return "Every " + cleaned.substring("EVERY ".length());
        }
        return cleaned;
    }

    private static java.util.ArrayList<String> splitReviewedBoundaryOpeningLines(String line) {
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        String cleaned = safe(line).trim();
        if (cleaned.isEmpty()) {
            return lines;
        }
        String splitMarker = " Start with ";
        int splitIndex = cleaned.indexOf(splitMarker);
        if (splitIndex > 0) {
            String first = cleaned.substring(0, splitIndex).trim();
            String second = cleaned.substring(splitIndex + 1).trim();
            if (!first.isEmpty()) {
                lines.add(first);
            }
            if (!second.isEmpty()) {
                lines.add(second);
            }
            return lines;
        }
        lines.add(cleaned);
        return lines;
    }

    private static boolean nextSentenceStartsWithEvery(String[] sentences, String currentSentence) {
        if (sentences == null || currentSentence == null) {
            return false;
        }
        for (int i = 0; i < sentences.length - 1; i++) {
            if (currentSentence.equals(sentences[i])) {
                return safe(sentences[i + 1]).trim().toUpperCase(QUERY_LOCALE).startsWith("EVERY ");
            }
        }
        return false;
    }

    private static AdmonitionTitleSplit splitAdmonitionTitle(String line) {
        String cleaned = safe(line).trim();
        int colonIndex = cleaned.indexOf(':');
        if (colonIndex < 0) {
            if (looksLikeStandaloneAdmonitionTitle(cleaned)) {
                return new AdmonitionTitleSplit(cleaned, "");
            }
            return new AdmonitionTitleSplit("", cleaned);
        }
        String title = cleaned.substring(0, colonIndex).trim();
        String detail = cleaned.substring(colonIndex + 1).trim();
        if (title.isEmpty() || detail.isEmpty()) {
            return new AdmonitionTitleSplit("", cleaned);
        }
        if (title.length() > 48 || title.endsWith(".")) {
            return new AdmonitionTitleSplit("", cleaned);
        }
        return new AdmonitionTitleSplit(title, detail);
    }

    private static boolean looksLikeStandaloneAdmonitionTitle(String line) {
        String cleaned = safe(line).trim();
        if (cleaned.isEmpty() || cleaned.length() > 48 || cleaned.endsWith(".")) {
            return false;
        }
        if (canonicalGuideAdmonitionLabel(cleaned).equals(cleaned)) {
            return false;
        }
        String[] words = cleaned.split("\\s+");
        if (words.length > 6) {
            return false;
        }
        int titleishWords = 0;
        for (String word : words) {
            String normalized = word.replaceAll("^[^\\p{Alnum}]+|[^\\p{Alnum}]+$", "");
            if (normalized.isEmpty()) {
                continue;
            }
            char first = normalized.charAt(0);
            if (Character.isUpperCase(first) || normalized.equals(normalized.toUpperCase(QUERY_LOCALE))) {
                titleishWords++;
            }
        }
        return titleishWords == words.length && titleishWords > 0;
    }

    static boolean isGuideSectionDisplayLine(String line) {
        return GUIDE_SECTION_DISPLAY_PATTERN.matcher(safe(line).trim()).matches();
    }

    private static SectionDisplayParts buildSectionDisplayParts(int sectionOrdinal, String value) {
        String cleaned = safe(value).trim();
        String heading = "";
        String label = cleaned;
        int colonIndex = cleaned.indexOf(':');
        if (colonIndex > 0 && colonIndex < cleaned.length() - 1) {
            String beforeColon = cleaned.substring(0, colonIndex).trim();
            String afterColon = cleaned.substring(colonIndex + 1).trim();
            if (!beforeColon.isEmpty() && !afterColon.isEmpty() && beforeColon.length() <= 56) {
                heading = beforeColon;
                label = firstGuideSectionCue(afterColon);
            }
        }
        if (label.isEmpty()) {
            label = cleaned;
        }
        return new SectionDisplayParts(
            buildGuideSectionPrefix(sectionOrdinal) + " " + label.toUpperCase(QUERY_LOCALE),
            heading
        );
    }

    private static String firstGuideSectionCue(String text) {
        String cleaned = safe(text).trim();
        int commaIndex = cleaned.indexOf(',');
        if (commaIndex > 0) {
            cleaned = cleaned.substring(0, commaIndex).trim();
        }
        return sentenceCase(cleaned);
    }

    private static String sentenceCase(String text) {
        String cleaned = safe(text).trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        if (cleaned.length() == 1) {
            return cleaned.toUpperCase(QUERY_LOCALE);
        }
        return cleaned.substring(0, 1).toUpperCase(QUERY_LOCALE)
            + cleaned.substring(1).toLowerCase(QUERY_LOCALE);
    }

    private static String buildGuideSectionPrefix(int sectionOrdinal) {
        return "\u2014 \u00a7 " + Math.max(sectionOrdinal, 1) + " \u00b7";
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
        return stripDuplicateAdmonitionDetailPrefix(safe(inlinePrefixMatcher.group(2)).trim(), normalizedLabel);
    }

    private static String stripDuplicateAdmonitionDetailPrefix(String detail, String admonitionLabel) {
        String cleaned = safe(detail).trim();
        String normalizedLabel = canonicalGuideAdmonitionLabel(admonitionLabel);
        if (cleaned.isEmpty() || normalizedLabel.isEmpty()) {
            return cleaned;
        }
        Matcher inlinePrefixMatcher = GUIDE_ADMONITION_INLINE_PREFIX_PATTERN.matcher(cleaned);
        if (inlinePrefixMatcher.matches()
            && normalizedLabel.equals(canonicalGuideAdmonitionLabel(inlinePrefixMatcher.group(1)))) {
            return safe(inlinePrefixMatcher.group(2)).trim();
        }
        return cleaned;
    }

    static String normalizeGuideDisplayText(String text) {
        String cleaned = safe(text).replace("\r\n", "\n");
        cleaned = GUIDE_HTML_BREAK_PATTERN.matcher(cleaned).replaceAll("\n");
        cleaned = cleaned.replace("&nbsp;", " ");
        cleaned = cleaned.replace("&amp;", "&");
        cleaned = cleaned.replace("&quot;", "\"");
        cleaned = cleaned.replace("&apos;", "'");
        cleaned = cleaned.replace("&lt;", "<");
        cleaned = cleaned.replace("&gt;", ">");
        cleaned = cleaned.replace("\u00f0\u0178\u201d\u00a5", "");
        cleaned = cleaned.replace("\u00f0\u0178\u203a\u00a1\u00ef\u00b8\u008f", "");
        cleaned = cleaned.replace("\u00e2\u0161\u00a0\u00ef\u00b8\u008f", "");
        cleaned = cleaned.replace("\u00e2\u0161\u2014\u00ef\u00b8\u008f", "");
        cleaned = cleaned.replace("\u00f0\u0178\u008f\u201d\u00ef\u00b8\u008f", "");
        cleaned = cleaned.replace("â€™", "'");
        cleaned = cleaned.replace("â€˜", "'");
        cleaned = cleaned.replace("â€œ", "\"");
        cleaned = cleaned.replace("\u00e2\u20ac\u009d", "\"");
        cleaned = cleaned.replace("â€“", "-");
        cleaned = cleaned.replace("â€”", " - ");
        cleaned = cleaned.replace("â€¢", "\u00b7");
        cleaned = cleaned.replace("â†’", "->");
        cleaned = cleaned.replace("Â·", "\u00b7");
        cleaned = cleaned.replace("Â§", "\u00a7");
        cleaned = cleaned.replace("Â", "");
        cleaned = cleaned.replace("\u00e2\u20ac\u201d", " - ");
        cleaned = cleaned.replace("\u00e2\u20ac\u201c", "-");
        cleaned = cleaned.replace("\u00e2\u20ac\u2122", "'");
        cleaned = cleaned.replace("\u00e2\u20ac\u0153", "\"");
        cleaned = cleaned.replace("\u00e2\u20ac\ufffd", "\"");
        cleaned = cleaned.replace("\u00e2\u2020\u2019", "->");
        cleaned = cleaned.replace("\u00e2\u20ac\u00a2", "\u00b7");
        cleaned = cleaned.replace("\u00c2\u00b7", "\u00b7");
        cleaned = cleaned.replace("\u00c2\u00a7", "\u00a7");
        cleaned = cleaned.replace("\u00c2", "");
        cleaned = cleaned.replace("ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â", " - ");
        cleaned = cleaned.replace("ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“", "-");
        cleaned = cleaned.replace("ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢", "->");
        cleaned = cleaned.replace("ÃƒÂ¢Ã…Â¡Ã‚Â ÃƒÂ¯Ã‚Â¸Ã‚Â", "!");
        return normalizeGuideSeparators(DetailWarningCopySanitizer.sanitizeWarningResidualCopy(cleaned));
    }

    private static String stripGuideFrontMatter(String text) {
        String cleaned = safe(text).trim();
        if (!cleaned.startsWith("---\n")) {
            return cleaned;
        }
        int closingMarker = cleaned.indexOf("\n---", 4);
        if (closingMarker < 0) {
            return cleaned;
        }
        int afterClosingLine = cleaned.indexOf('\n', closingMarker + 4);
        return afterClosingLine < 0 ? "" : cleaned.substring(afterClosingLine + 1);
    }

    private static String normalizeGuideSeparators(String text) {
        String cleaned = safe(text);
        cleaned = cleaned.replace("Â·", "\u00b7");
        cleaned = GUIDE_SEPARATOR_RUN_PATTERN.matcher(cleaned).replaceAll(" \u00b7 ");
        cleaned = cleaned.replaceAll("\\s+\\u00b7\\s+", " \u00b7 ");
        cleaned = cleaned.replaceAll("^\\s*\\u00b7\\s*", "");
        cleaned = cleaned.replaceAll("\\s*\\u00b7\\s*$", "");
        cleaned = cleaned.replaceAll("[ \\t]+([,.;:])", "$1");
        return cleaned;
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

    private static final class AdmonitionTitleSplit {
        final String title;
        final String detail;

        AdmonitionTitleSplit(String title, String detail) {
            this.title = safe(title);
            this.detail = safe(detail);
        }
    }

    private static final class SectionDisplayParts {
        final String label;
        final String heading;

        SectionDisplayParts(String label, String heading) {
            this.label = safe(label);
            this.heading = safe(heading);
        }
    }
}
