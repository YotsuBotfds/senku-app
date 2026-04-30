package com.senku.mobile;

final class MainResultPublicationPolicy {
    private final String highlightQuery;
    private final boolean browseChromeVisible;
    private final String searchQueryLabel;
    private final int resultCount;
    private final boolean updateSearchQueryChrome;

    private MainResultPublicationPolicy(
        String highlightQuery,
        boolean browseChromeVisible,
        String searchQueryLabel,
        int resultCount,
        boolean updateSearchQueryChrome
    ) {
        this.highlightQuery = safe(highlightQuery);
        this.browseChromeVisible = browseChromeVisible;
        this.searchQueryLabel = safe(searchQueryLabel);
        this.resultCount = Math.max(0, resultCount);
        this.updateSearchQueryChrome = updateSearchQueryChrome;
    }

    static MainResultPublicationPolicy browseSurface() {
        return new MainResultPublicationPolicy("", true, "", 0, false);
    }

    static MainResultPublicationPolicy resultSurface(String highlightQuery) {
        return new MainResultPublicationPolicy(highlightQuery, false, "", 0, false);
    }

    static MainResultPublicationPolicy resultSurfaceWithSearchChrome(
        String highlightQuery,
        String searchQueryLabel,
        int resultCount
    ) {
        return new MainResultPublicationPolicy(highlightQuery, false, searchQueryLabel, resultCount, true);
    }

    static MainResultPublicationPolicy resultSurfaceWithBrowseFallback(String highlightQuery, boolean browseChromeVisible) {
        return new MainResultPublicationPolicy(highlightQuery, browseChromeVisible, "", 0, false);
    }

    String highlightQuery() {
        return highlightQuery;
    }

    boolean browseChromeVisible() {
        return browseChromeVisible;
    }

    boolean updateSearchQueryChrome() {
        return updateSearchQueryChrome;
    }

    String searchQueryLabel() {
        return searchQueryLabel;
    }

    int resultCount() {
        return resultCount;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
