package com.senku.mobile;

final class MainHomeChromePolicy {
    static final String HOME_MODE = "HOME SENKU";
    static final String SEARCH_MODE = "SEARCH";
    static final String HOME_TITLE = "Field manual \u2022 ed.2";

    private MainHomeChromePolicy() {
    }

    static ChromeState resolve(
        boolean browseMode,
        String query,
        MainRouteDecisionHelper.RouteState routeState
    ) {
        boolean backAvailable = MainRouteDecisionHelper.shouldShowHomeChromeBack(routeState);
        if (browseMode) {
            return new ChromeState(backAvailable, true, HOME_MODE, HOME_TITLE, true);
        }
        String cleanQuery = safe(query).trim();
        String title = cleanQuery.isEmpty() || "guides".equalsIgnoreCase(cleanQuery)
            ? "Senku"
            : cleanQuery;
        return new ChromeState(backAvailable, false, SEARCH_MODE, title, false);
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

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    static final class ChromeState {
        final boolean backAvailable;
        final boolean searchActionVisible;
        final String mode;
        final String title;
        final boolean usesStyledHomeTitle;

        ChromeState(
            boolean backAvailable,
            boolean searchActionVisible,
            String mode,
            String title,
            boolean usesStyledHomeTitle
        ) {
            this.backAvailable = backAvailable;
            this.searchActionVisible = searchActionVisible;
            this.mode = mode;
            this.title = title;
            this.usesStyledHomeTitle = usesStyledHomeTitle;
        }
    }
}
