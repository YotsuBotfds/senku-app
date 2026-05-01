package com.senku.mobile;

final class DetailDockedComposerPresentationPolicy {
    private DetailDockedComposerPresentationPolicy() {
    }

    static String resolveHint(String fullHint, String compactHint, boolean compactFollowUpMode) {
        return resolveHint(fullHint, compactHint, compactFollowUpMode, false);
    }

    static String resolveHint(
        String fullHint,
        String compactHint,
        boolean compactFollowUpMode,
        boolean emergencySurface
    ) {
        if (emergencySurface) {
            return "Ask about safe re-entry...";
        }
        String fallback = safe(fullHint).trim();
        if (!compactFollowUpMode) {
            return fallback;
        }
        String compact = safe(compactHint).trim();
        return compact.isEmpty() ? fallback : compact;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
