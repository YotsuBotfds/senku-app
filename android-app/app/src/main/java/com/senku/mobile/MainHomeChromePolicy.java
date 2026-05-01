package com.senku.mobile;

final class MainHomeChromePolicy {
    static final String HOME_MODE = "HOME SENKU";
    static final String SEARCH_MODE = "SEARCH";
    static final String HOME_TITLE = "Field manual \u2022 ed.2";
    static final String DEFAULT_SEARCH_TITLE = "Senku";
    static final String DEFAULT_SEARCH_QUERY_LABEL = "guides";

    private MainHomeChromePolicy() {
    }

    static ChromeState resolve(
        boolean browseMode,
        String query,
        MainRouteDecisionHelper.RouteState routeState
    ) {
        boolean backAvailable = MainRouteDecisionHelper.shouldShowHomeChromeBack(routeState);
        if (browseMode) {
            return new ChromeState(backAvailable, true, false, HOME_MODE, HOME_TITLE, true);
        }
        SearchChromeState searchChrome = resolveSearch(query, 0);
        return new ChromeState(backAvailable, false, false, SEARCH_MODE, searchChrome.title, false);
    }

    static SearchChromeState resolveSearch(String query, int resultCount) {
        String cleanQuery = safe(query).trim();
        String queryLabel = cleanQuery.isEmpty() || DEFAULT_SEARCH_QUERY_LABEL.equalsIgnoreCase(cleanQuery)
            ? DEFAULT_SEARCH_QUERY_LABEL
            : cleanQuery;
        String title = DEFAULT_SEARCH_QUERY_LABEL.equals(queryLabel) ? DEFAULT_SEARCH_TITLE : queryLabel;
        String countLabel = resultCount + (resultCount == 1 ? " RESULT" : " RESULTS");
        String countTitle = resultCount + (resultCount == 1 ? " result" : " results");
        return new SearchChromeState(
            queryLabel,
            title,
            queryLabel + " \u2022 " + countTitle,
            countLabel,
            "\u2039  |  " + SEARCH_MODE + " " + queryLabel
        );
    }

    static String visibleTitle(String mode, String title, boolean hasSeparateModeText, boolean landscapePhone) {
        if (hasSeparateModeText || !landscapePhone) {
            return title;
        }
        String cleanMode = safe(mode).trim();
        String cleanTitle = safe(title).trim();
        if (cleanMode.isEmpty()) {
            return title;
        }
        if (cleanTitle.isEmpty()) {
            return cleanMode;
        }
        return cleanMode + " \u2022 " + cleanTitle;
    }

    static boolean shouldShowTabletSearchTopbar(boolean tabletSearchLayout, boolean browseMode) {
        return tabletSearchLayout && !browseMode;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    static final class ChromeState {
        final boolean backAvailable;
        final boolean searchActionVisible;
        final boolean overflowActionVisible;
        final String mode;
        final String title;
        final boolean usesStyledHomeTitle;

        ChromeState(
            boolean backAvailable,
            boolean searchActionVisible,
            boolean overflowActionVisible,
            String mode,
            String title,
            boolean usesStyledHomeTitle
        ) {
            this.backAvailable = backAvailable;
            this.searchActionVisible = searchActionVisible;
            this.overflowActionVisible = overflowActionVisible;
            this.mode = mode;
            this.title = title;
            this.usesStyledHomeTitle = usesStyledHomeTitle;
        }
    }

    static final class SearchChromeState {
        final String queryLabel;
        final String title;
        final String titleWithCount;
        final String countLabel;
        final String landscapePhoneHeader;

        SearchChromeState(
            String queryLabel,
            String title,
            String titleWithCount,
            String countLabel,
            String landscapePhoneHeader
        ) {
            this.queryLabel = queryLabel;
            this.title = title;
            this.titleWithCount = titleWithCount;
            this.countLabel = countLabel;
            this.landscapePhoneHeader = landscapePhoneHeader;
        }
    }
}
