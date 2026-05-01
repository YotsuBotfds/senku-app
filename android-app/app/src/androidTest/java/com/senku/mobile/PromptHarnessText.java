package com.senku.mobile;

import android.os.Bundle;

import java.util.Locale;

final class PromptHarnessText {
    private PromptHarnessText() {
    }

    static String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            String trimmed = safe(value).trim();
            if (!trimmed.isEmpty() && !"null".equalsIgnoreCase(trimmed)) {
                return trimmed;
            }
        }
        return "";
    }

    static String quoteForDiagnostics(String value) {
        return "'" + clipForDiagnostics(value, 240).replace('\n', ' ').replace('\r', ' ') + "'";
    }

    static String clipForDiagnostics(String value, int maxChars) {
        String trimmed = safe(value).trim();
        int limit = Math.max(0, maxChars);
        if (trimmed.length() <= limit) {
            return trimmed;
        }
        if (limit <= 3) {
            return trimmed.substring(0, limit);
        }
        return trimmed.substring(0, limit - 3) + "...";
    }

    static String safe(String value) {
        return value == null ? "" : value;
    }

    static boolean parseBooleanArg(Bundle args, String key) {
        return "true".equalsIgnoreCase(safe(args.getString(key)).trim());
    }

    static boolean containsAny(String text, String... fragments) {
        String haystack = safe(text).toLowerCase(Locale.US);
        for (String fragment : fragments) {
            String needle = safe(fragment).trim().toLowerCase(Locale.US);
            if (!needle.isEmpty() && haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    static int countOccurrences(String text, String fragment) {
        String haystack = safe(text);
        String needle = safe(fragment);
        if (haystack.isEmpty() || needle.isEmpty()) {
            return 0;
        }
        int count = 0;
        int start = 0;
        while (start >= 0) {
            start = haystack.indexOf(needle, start);
            if (start < 0) {
                break;
            }
            count += 1;
            start += needle.length();
        }
        return count;
    }

    static long parseLongArg(Bundle args, String key, long fallback) {
        String raw = safe(args.getString(key)).trim();
        if (raw.isEmpty()) {
            return fallback;
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
