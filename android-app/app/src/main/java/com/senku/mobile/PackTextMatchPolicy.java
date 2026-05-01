package com.senku.mobile;

import java.util.Locale;
import java.util.Set;

final class PackTextMatchPolicy {
    private static final Locale QUERY_LOCALE = Locale.US;

    private PackTextMatchPolicy() {
    }

    static boolean containsAnyMarker(String text, Set<String> markers) {
        String normalized = normalize(text);
        if (normalized.isEmpty() || markers == null || markers.isEmpty()) {
            return false;
        }
        String boundedText = " " + normalized + " ";
        for (String marker : markers) {
            String normalizedMarker = normalize(marker);
            if (normalizedMarker.isEmpty()) {
                continue;
            }
            if (boundedText.contains(" " + normalizedMarker + " ")) {
                return true;
            }
        }
        return false;
    }

    static boolean containsTerm(String text, String term) {
        String normalizedText = normalize(text);
        String normalizedTerm = normalize(term);
        if (normalizedText.isEmpty() || normalizedTerm.isEmpty()) {
            return false;
        }
        if (normalizedTerm.contains(" ")) {
            return normalizedText.contains(normalizedTerm);
        }
        return (" " + normalizedText + " ").contains(" " + normalizedTerm + " ");
    }

    static String normalize(String text) {
        return safe(text)
            .toLowerCase(QUERY_LOCALE)
            .replaceAll("[^a-z0-9]+", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
