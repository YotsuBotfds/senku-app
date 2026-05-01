package com.senku.mobile;

import static com.senku.mobile.PromptHarnessText.safe;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PromptHarnessParsing {
    private static final Pattern GUIDE_ID_PATTERN =
        Pattern.compile("\\bGD-\\d+\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern TURN_COUNT_PATTERN =
        Pattern.compile("(\\d+)\\s+turn", Pattern.CASE_INSENSITIVE);
    private static final Pattern LEADING_INTEGER_PATTERN = Pattern.compile("(\\d+)");

    private PromptHarnessParsing() {
    }

    static String extractGuideId(String rawText) {
        Matcher matcher = GUIDE_ID_PATTERN.matcher(safe(rawText));
        if (matcher.find()) {
            return safe(matcher.group()).toUpperCase(Locale.US);
        }
        return "";
    }

    static String extractGuideTitleFragment(String rawText) {
        String label = safe(rawText).trim();
        int colonIndex = label.indexOf(':');
        if (colonIndex >= 0 && colonIndex + 1 < label.length()) {
            label = label.substring(colonIndex + 1).trim();
        }
        label = label.replaceFirst("(?i)^GD-\\d+\\s*-\\s*", "").trim();
        if (label.isEmpty()) {
            return "";
        }
        return label.length() > 24 ? label.substring(0, 24).trim() : label;
    }

    static int extractTurnCount(String rawText) {
        Matcher matcher = TURN_COUNT_PATTERN.matcher(safe(rawText));
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    static int extractLeadingInteger(String rawText) {
        Matcher matcher = LEADING_INTEGER_PATTERN.matcher(safe(rawText));
        if (!matcher.find()) {
            return 0;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    static String clipExpectedSummary(String expectedSummaryLower) {
        String cleaned = safe(expectedSummaryLower).trim();
        if (cleaned.length() <= 24) {
            return cleaned;
        }
        return cleaned.substring(0, 24).trim();
    }
}
