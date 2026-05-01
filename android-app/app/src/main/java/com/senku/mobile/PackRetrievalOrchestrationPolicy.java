package com.senku.mobile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

final class PackRetrievalOrchestrationPolicy {
    private PackRetrievalOrchestrationPolicy() {
    }

    static int routeResultLimit(QueryRouteProfile routeProfile, int limit) {
        if (routeProfile == null || !routeProfile.isRouteFocused()) {
            return 0;
        }
        int divisor = routeProfile.isStarterBuildProject() ? 3 : 2;
        return Math.min(Math.max(Math.max(limit, 18) / divisor, 12), 24);
    }

    static List<SearchResult> mergeResultsWhenCentroidMissing(
        List<SearchResult> routeResults,
        List<SearchResult> lexicalFallback,
        int limit
    ) {
        return mergePreferredResults(routeResults, lexicalFallback, limit);
    }

    static List<SearchResult> mergePreferredResults(
        List<SearchResult> primary,
        List<SearchResult> secondary,
        int limit
    ) {
        if (primary.isEmpty()) {
            return secondary;
        }
        LinkedHashMap<String, SearchResult> merged = new LinkedHashMap<>();
        for (SearchResult result : primary) {
            if (merged.size() >= limit) {
                break;
            }
            merged.put(buildGuideSectionKey(result), result);
        }
        for (SearchResult result : secondary) {
            if (merged.size() >= limit) {
                break;
            }
            String key = buildGuideSectionKey(result);
            if (!merged.containsKey(key)) {
                merged.put(key, result);
            }
        }
        return new ArrayList<>(merged.values());
    }

    private static String buildGuideSectionKey(SearchResult result) {
        return PackQueryPipelineHelper.guideSectionKey(result.guideId, result.title, result.sectionHeading);
    }
}
