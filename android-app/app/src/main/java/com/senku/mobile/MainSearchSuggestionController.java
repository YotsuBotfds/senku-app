package com.senku.mobile;

import java.util.List;

final class MainSearchSuggestionController {
    private MainSearchSuggestionController() {
    }

    static SuggestionRoute route(
        MainSearchSuggestionPolicy.SearchSuggestion suggestion,
        List<SearchResult> allGuides,
        MainRouteDecisionHelper.RouteState currentRouteState
    ) {
        if (suggestion == null) {
            return SuggestionRoute.ignore();
        }
        if (suggestion.isCategory()) {
            return SuggestionRoute.categoryFilter(MainCategoryFilterController.route(
                allGuides,
                suggestion.categoryKey,
                suggestion.categoryLabel,
                currentRouteState
            ));
        }
        String query = safe(suggestion.searchQuery).trim();
        if (query.isEmpty()) {
            return SuggestionRoute.ignore();
        }
        return SuggestionRoute.search(query);
    }

    static final class SuggestionRoute {
        private final RouteType routeType;
        private final MainCategoryFilterController.FilterRoute categoryFilterRoute;
        private final String searchQuery;

        private SuggestionRoute(
            RouteType routeType,
            MainCategoryFilterController.FilterRoute categoryFilterRoute,
            String searchQuery
        ) {
            this.routeType = routeType == null ? RouteType.IGNORE : routeType;
            this.categoryFilterRoute = categoryFilterRoute;
            this.searchQuery = safe(searchQuery);
        }

        private static SuggestionRoute ignore() {
            return new SuggestionRoute(RouteType.IGNORE, null, "");
        }

        private static SuggestionRoute categoryFilter(MainCategoryFilterController.FilterRoute route) {
            return new SuggestionRoute(RouteType.CATEGORY_FILTER, route, "");
        }

        private static SuggestionRoute search(String query) {
            return new SuggestionRoute(RouteType.SEARCH, null, query);
        }

        boolean shouldIgnore() {
            return routeType == RouteType.IGNORE;
        }

        boolean isCategoryFilter() {
            return routeType == RouteType.CATEGORY_FILTER;
        }

        boolean isSearch() {
            return routeType == RouteType.SEARCH;
        }

        MainCategoryFilterController.FilterRoute categoryFilterRoute() {
            return categoryFilterRoute;
        }

        String searchQuery() {
            return searchQuery;
        }
    }

    private enum RouteType {
        IGNORE,
        CATEGORY_FILTER,
        SEARCH
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
