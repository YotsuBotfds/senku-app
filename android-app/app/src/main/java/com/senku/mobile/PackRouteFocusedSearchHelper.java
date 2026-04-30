package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class PackRouteFocusedSearchHelper {
    private PackRouteFocusedSearchHelper() {
    }

    static List<QueryRouteProfile.RouteSearchSpec> routeSearchSpecs(PackRepository.QueryTerms queryTerms) {
        if (queryTerms == null || queryTerms.routeProfile == null) {
            return Collections.emptyList();
        }
        return queryTerms.routeProfile.routeSearchSpecs(queryTerms.queryLower);
    }

    static List<RouteSearchStep> routeSearchSteps(PackRepository.QueryTerms queryTerms, int maxTokens) {
        ArrayList<RouteSearchStep> steps = new ArrayList<>();
        for (QueryRouteProfile.RouteSearchSpec routeSpec : routeSearchSpecs(queryTerms)) {
            RouteSearchStep step = routeSearchStep(queryTerms, routeSpec, maxTokens);
            if (step != null) {
                steps.add(step);
            }
        }
        return steps;
    }

    static RouteSearchStep routeSearchStep(
        PackRepository.QueryTerms queryTerms,
        QueryRouteProfile.RouteSearchSpec routeSpec,
        int maxTokens
    ) {
        if (queryTerms == null || queryTerms.routeProfile == null || routeSpec == null) {
            return null;
        }
        PackRepository.QueryTerms specTerms = PackRepository.QueryTerms.fromText(
            routeSpec.text(),
            queryTerms.routeProfile
        );
        List<String> tokens = limitTokens(specTerms.primaryKeywordTokens(), maxTokens);
        List<String> categories = routeSpec.categories().isEmpty()
            ? new ArrayList<>(queryTerms.routeProfile.preferredCategories())
            : new ArrayList<>(routeSpec.categories());
        if (tokens == null || tokens.isEmpty() || categories.isEmpty()) {
            return null;
        }
        return new RouteSearchStep(routeSpec, specTerms, tokens, categories);
    }

    private static List<String> limitTokens(List<String> tokens, int max) {
        if (tokens == null || tokens.isEmpty() || max <= 0 || tokens.size() <= max) {
            return tokens;
        }
        return new ArrayList<>(tokens.subList(0, max));
    }

    static final class RouteSearchStep {
        final QueryRouteProfile.RouteSearchSpec routeSpec;
        final PackRepository.QueryTerms specTerms;
        final List<String> tokens;
        final List<String> categories;

        RouteSearchStep(
            QueryRouteProfile.RouteSearchSpec routeSpec,
            PackRepository.QueryTerms specTerms,
            List<String> tokens,
            List<String> categories
        ) {
            this.routeSpec = routeSpec;
            this.specTerms = specTerms;
            this.tokens = tokens;
            this.categories = categories;
        }
    }
}
