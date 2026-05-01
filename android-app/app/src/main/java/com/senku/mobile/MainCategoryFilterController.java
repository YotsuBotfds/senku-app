package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class MainCategoryFilterController {
    private MainCategoryFilterController() {
    }

    static FilterRoute route(
        List<SearchResult> loadedGuides,
        String bucketKey,
        String label
    ) {
        if (loadedGuides == null || loadedGuides.isEmpty()) {
            return FilterRoute.browseFallback();
        }
        ArrayList<SearchResult> filtered = new ArrayList<>();
        for (SearchResult result : loadedGuides) {
            if (HomeCategoryPolicy.matchesBucket(result, bucketKey)) {
                filtered.add(result);
            }
        }
        String filterLabel = HomeCategoryPolicy.filterLabel(label, filtered.size());
        return FilterRoute.filtered(
            filtered,
            MainResultPublicationPolicy.searchResultSurfaceWithSearchChrome("", filterLabel, filtered.size()),
            filterLabel
        );
    }

    static final class FilterRoute {
        private final boolean browseFallback;
        private final List<SearchResult> filteredResults;
        private final MainResultPublicationPolicy publication;
        private final String searchQueryLabel;
        private final int resultCount;

        private FilterRoute(
            boolean browseFallback,
            List<SearchResult> filteredResults,
            MainResultPublicationPolicy publication,
            String searchQueryLabel,
            int resultCount
        ) {
            this.browseFallback = browseFallback;
            this.filteredResults = filteredResults == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(filteredResults));
            this.publication = publication;
            this.searchQueryLabel = safe(searchQueryLabel);
            this.resultCount = Math.max(0, resultCount);
        }

        private static FilterRoute browseFallback() {
            return new FilterRoute(true, Collections.emptyList(), null, "", 0);
        }

        private static FilterRoute filtered(
            List<SearchResult> filteredResults,
            MainResultPublicationPolicy publication,
            String searchQueryLabel
        ) {
            int resultCount = filteredResults == null ? 0 : filteredResults.size();
            return new FilterRoute(
                false,
                filteredResults,
                publication,
                searchQueryLabel,
                resultCount
            );
        }

        boolean shouldBrowseGuides() {
            return browseFallback;
        }

        List<SearchResult> filteredResults() {
            return filteredResults;
        }

        MainResultPublicationPolicy publication() {
            return publication;
        }

        String searchQueryLabel() {
            return searchQueryLabel;
        }

        int resultCount() {
            return resultCount;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
