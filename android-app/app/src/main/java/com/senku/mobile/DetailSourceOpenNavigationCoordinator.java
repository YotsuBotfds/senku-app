package com.senku.mobile;

final class DetailSourceOpenNavigationCoordinator {
    private DetailSourceOpenNavigationCoordinator() {
    }

    static Decision decide(SearchResult source) {
        SearchResult fallback = source == null ? new SearchResult("", "", "", "") : source;
        String guideId = safe(fallback.guideId).trim();
        return new Decision(fallback, guideId, !guideId.isEmpty());
    }

    static final class Decision {
        final SearchResult source;
        final String guideId;
        final boolean shouldLoadGuideBeforeOpen;

        private Decision(SearchResult source, String guideId, boolean shouldLoadGuideBeforeOpen) {
            this.source = source;
            this.guideId = guideId;
            this.shouldLoadGuideBeforeOpen = shouldLoadGuideBeforeOpen;
        }
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
