package com.senku.mobile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class QueryTextTokenPolicy {
    private static final Locale QUERY_LOCALE = Locale.US;

    private QueryTextTokenPolicy() {
    }

    static List<String> normalizedParts(String token) {
        ArrayList<String> parts = new ArrayList<>();
        String[] split = emptySafe(token).toLowerCase(QUERY_LOCALE).split("[^a-z0-9]+");
        for (String part : split) {
            String normalized = emptySafe(part).trim().toLowerCase(QUERY_LOCALE);
            if (normalized.isEmpty()) {
                continue;
            }
            parts.add(normalized);
        }
        return parts;
    }

    private static String emptySafe(String value) {
        return value == null ? "" : value;
    }
}
