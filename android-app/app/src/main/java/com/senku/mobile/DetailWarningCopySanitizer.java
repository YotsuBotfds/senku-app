package com.senku.mobile;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DetailWarningCopySanitizer {
    private static final Pattern WARNING_RESIDUAL_BRACKET_PATTERN =
        Pattern.compile("\\[([^\\[\\]\\n]{1,80})\\]");
    private static final Pattern WARNING_RESIDUAL_CITATION_PATTERN =
        Pattern.compile("(?i)^(?:GD[-/]\\d{1,3})(?:\\s*,\\s*GD[-/]\\d{1,3})*$");
    private static final String[] WARNING_RESIDUAL_PREFIXES = new String[] {
        "instructional mandate",
        "instructional constraint",
        "instructional warning",
        "instructional advisory",
        "instructional note",
        "system instruction",
        "system warning",
        "system advisory",
        "control instruction",
        "control warning",
        "safety instruction",
        "safety mandate",
        "safety advisory",
        "safety note",
        "safety warning",
        "safety constraint",
        "guide proof",
        "guide source",
        "proof route",
        "proof metadata",
        "route proof"
    };
    private static final String[] WARNING_RESIDUAL_TRAIL_MARKERS = new String[] {
        "implied",
        "label",
        "labels",
        "residue",
        "hazard",
        "hazards",
        "risk",
        "risks",
        "process",
        "processes",
        "clutter",
        "chrome",
        "metadata"
    };

    private DetailWarningCopySanitizer() {}

    static String sanitizeWarningResidualCopy(String text) {
        String cleaned = safe(text);
        if (cleaned.isEmpty()) {
            return "";
        }
        Matcher matcher = WARNING_RESIDUAL_BRACKET_PATTERN.matcher(cleaned);
        StringBuffer buffer = new StringBuffer(cleaned.length());
        while (matcher.find()) {
            String label = safe(matcher.group(1));
            if (isWarningResidualBracket(label)) {
                matcher.appendReplacement(buffer, "");
                continue;
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(0)));
        }
        matcher.appendTail(buffer);
        cleaned = buffer.toString();
        cleaned = cleaned.replaceAll("\\[\\s*\\]", "");
        cleaned = cleaned.replaceAll("\\(\\s*\\)", "");
        cleaned = cleaned.replaceAll("[ \\t]+([,.;:!?])", "$1");
        cleaned = cleaned.replaceAll("[ \\t]+\\n", "\n");
        cleaned = cleaned.replaceAll(" {2,}", " ");
        return cleaned.trim();
    }

    private static boolean isWarningResidualBracket(String label) {
        String normalized = safe(label).trim().replaceAll("\\s+", " ").toLowerCase(Locale.US);
        if (normalized.isEmpty()) {
            return false;
        }
        if (WARNING_RESIDUAL_CITATION_PATTERN.matcher(normalized).matches()) {
            return false;
        }
        if (normalized.contains("gd-") || normalized.contains("gd/")) {
            return false;
        }
        if ("warning".equals(normalized)
            || "caution".equals(normalized)
            || "advisory".equals(normalized)
            || "instruction".equals(normalized)) {
            return true;
        }
        for (String prefix : WARNING_RESIDUAL_PREFIXES) {
            if (normalized.startsWith(prefix)) {
                return true;
            }
        }
        if (!normalized.startsWith("warning ")
            && !normalized.startsWith("advisory ")
            && !normalized.startsWith("caution ")) {
            return false;
        }
        for (String marker : WARNING_RESIDUAL_TRAIL_MARKERS) {
            if (normalized.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
