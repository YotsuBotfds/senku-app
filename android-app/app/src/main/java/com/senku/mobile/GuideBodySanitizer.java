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
    private static final Pattern GUIDE_LAYOUT_TAG_PATTERN = Pattern.compile("(?i)</?(section|div|span|p)\\b[^>]*>");
    private static final Pattern GUIDE_INLINE_STYLE_TAG_PATTERN = Pattern.compile("(?i)</?(strong|b|em|i|mark|small)\\b[^>]*>");
    private static final Pattern GUIDE_MARKDOWN_LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]+)\\)");
    private static final Pattern GUIDE_HTML_BREAK_PATTERN = Pattern.compile("(?i)<br\\s*/?>");
    private static final Pattern GUIDE_MARKDOWN_ESCAPE_PATTERN = Pattern.compile("\\\\([\\[\\]\\(\\)#*_`])");
    private static final String GUIDE_SECTION_ANCHOR_MARKER = "[[SECTION]] ";

    private GuideBodySanitizer() {
    }

    static String sanitizeGuideBodyForDisplay(String body) {
        String cleaned = normalizeGuideDisplayText(safe(body)).trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        String[] lines = cleaned.split("\\n", -1);
        StringBuilder builder = new StringBuilder(cleaned.length() + 32);
        boolean insideAdmonitionBlock = false;
        String activeAdmonitionLabel = "";
        boolean firstAdmonitionContentLine = false;
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
                    builder.append(activeAdmonitionLabel).append('\n');
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
            if (markdownHeading
                && !displayLine.isEmpty()
                && !insideAdmonitionBlock
                && canonicalGuideAdmonitionLabel(displayLine).isEmpty()) {
                displayLine = GUIDE_SECTION_ANCHOR_MARKER + displayLine;
            }
            if (displayLine.isEmpty() && trimmed.isEmpty()) {
                builder.append('\n');
                continue;
            }
            if (!displayLine.isEmpty()) {
                builder.append(displayLine).append('\n');
                if (insideAdmonitionBlock) {
                    firstAdmonitionContentLine = false;
                }
            }
        }
        return builder.toString().replaceAll("\\n{3,}", "\n\n").trim();
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
        cleaned = cleaned.replace("ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â", " - ");
        cleaned = cleaned.replace("ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“", "-");
        cleaned = cleaned.replace("ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢", "->");
        cleaned = cleaned.replace("ÃƒÂ¢Ã…Â¡Ã‚Â ÃƒÂ¯Ã‚Â¸Ã‚Â", "!");
        return DetailWarningCopySanitizer.sanitizeWarningResidualCopy(cleaned);
    }

    private static String normalizeGuideAdmonitionLabel(String marker) {
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

    private static String canonicalGuideAdmonitionLabel(String label) {
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
