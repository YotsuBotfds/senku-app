package com.senku.mobile;

import com.senku.ui.primitives.BottomTabDestination;

final class MainResultPublicationPolicy {
    static final class ResultItemsPresentation {
        private final String highlightQuery;
        private final MainRouteDecisionHelper.RouteState routeState;

        private ResultItemsPresentation(
            String highlightQuery,
            MainRouteDecisionHelper.RouteState routeState
        ) {
            this.highlightQuery = safe(highlightQuery);
            this.routeState = routeState == null ? MainRouteDecisionHelper.browseHome() : routeState;
        }

        String highlightQuery() {
            return highlightQuery;
        }

        MainRouteDecisionHelper.RouteState routeState() {
            return routeState;
        }
    }

    static final class SearchQueryChromePresentation {
        private final boolean shouldPublish;
        private final String queryLabel;
        private final int resultCount;

        private SearchQueryChromePresentation(
            boolean shouldPublish,
            String queryLabel,
            int resultCount
        ) {
            this.shouldPublish = shouldPublish;
            this.queryLabel = safe(queryLabel);
            this.resultCount = Math.max(0, resultCount);
        }

        boolean shouldPublish() {
            return shouldPublish;
        }

        String queryLabel() {
            return queryLabel;
        }

        int resultCount() {
            return resultCount;
        }
    }

    private final String highlightQuery;
    private final MainRouteDecisionHelper.RouteState routeState;
    private final String searchQueryLabel;
    private final int resultCount;
    private final boolean updateSearchQueryChrome;

    private MainResultPublicationPolicy(
        String highlightQuery,
        MainRouteDecisionHelper.RouteState routeState,
        String searchQueryLabel,
        int resultCount,
        boolean updateSearchQueryChrome
    ) {
        this.highlightQuery = safe(highlightQuery);
        this.routeState = routeState == null ? MainRouteDecisionHelper.browseHome() : routeState;
        this.searchQueryLabel = safe(searchQueryLabel);
        this.resultCount = Math.max(0, resultCount);
        this.updateSearchQueryChrome = updateSearchQueryChrome;
    }

    static MainResultPublicationPolicy browseSurface() {
        return new MainResultPublicationPolicy("", MainRouteDecisionHelper.browseHome(), "", 0, false);
    }

    static MainResultPublicationPolicy browseSurface(MainRouteDecisionHelper.RouteState routeState) {
        MainRouteDecisionHelper.RouteState safeRouteState = routeState == null
            ? MainRouteDecisionHelper.browseHome()
            : routeState;
        return new MainResultPublicationPolicy(
            "",
            MainRouteDecisionHelper.isBrowseSurface(safeRouteState.surface)
                ? safeRouteState
                : MainRouteDecisionHelper.browseHome(),
            "",
            0,
            false
        );
    }

    static MainResultPublicationPolicy searchResultSurface(String highlightQuery) {
        return new MainResultPublicationPolicy(highlightQuery, searchResultsRouteState(), "", 0, false);
    }

    static MainResultPublicationPolicy askResultSurface(String highlightQuery) {
        return new MainResultPublicationPolicy(highlightQuery, askResultsRouteState(), "", 0, false);
    }

    static MainResultPublicationPolicy searchResultSurfaceWithSearchChrome(
        String highlightQuery,
        String searchQueryLabel,
        int resultCount
    ) {
        return new MainResultPublicationPolicy(
            highlightQuery,
            searchResultsRouteState(),
            searchQueryLabel,
            resultCount,
            true
        );
    }

    static MainResultPublicationPolicy searchResultSurfaceWithBrowseFallback(
        String highlightQuery,
        boolean browseChromeVisible
    ) {
        return new MainResultPublicationPolicy(
            highlightQuery,
            browseChromeVisible ? MainRouteDecisionHelper.browseHome() : searchResultsRouteState(),
            "",
            0,
            false
        );
    }

    static MainResultPublicationPolicy askResultSurfaceWithBrowseFallback(
        String highlightQuery,
        boolean browseChromeVisible
    ) {
        return new MainResultPublicationPolicy(
            highlightQuery,
            browseChromeVisible ? recentThreadsRouteState() : askResultsRouteState(),
            "",
            0,
            false
        );
    }

    String highlightQuery() {
        return highlightQuery;
    }

    MainRouteDecisionHelper.RouteState routeState() {
        return routeState;
    }

    boolean browseChromeVisible() {
        return MainRouteDecisionHelper.isBrowseSurface(routeState.surface);
    }

    boolean updateSearchQueryChrome() {
        return updateSearchQueryChrome;
    }

    ResultItemsPresentation resultItemsPresentation() {
        return new ResultItemsPresentation(highlightQuery, routeState);
    }

    SearchQueryChromePresentation searchQueryChromePresentation() {
        return new SearchQueryChromePresentation(updateSearchQueryChrome, searchQueryLabel, resultCount);
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

    private static MainRouteDecisionHelper.RouteState searchResultsRouteState() {
        return new MainRouteDecisionHelper.RouteState(
            MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
            BottomTabDestination.HOME,
            false
        );
    }

    private static MainRouteDecisionHelper.RouteState askResultsRouteState() {
        return new MainRouteDecisionHelper.RouteState(
            MainRouteDecisionHelper.Surface.ASK_RESULTS,
            BottomTabDestination.ASK,
            true
        );
    }

    private static MainRouteDecisionHelper.RouteState recentThreadsRouteState() {
        return new MainRouteDecisionHelper.RouteState(
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
            BottomTabDestination.ASK,
            false
        );
    }
}
