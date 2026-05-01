package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class PackRouteFocusedSearchHelper {
    private static final int DEFAULT_ROUTE_SEARCH_TOKEN_LIMIT = 6;

    private PackRouteFocusedSearchHelper() {
    }

    static List<QueryRouteProfile.RouteSearchSpec> routeSearchSpecs(PackRepository.QueryTerms queryTerms) {
        if (queryTerms == null || queryTerms.routeProfile == null) {
            return Collections.emptyList();
        }
        return queryTerms.routeProfile.routeSearchSpecs(queryTerms.queryLower);
    }

    static RouteSearchStep routeSearchStep(
        PackRepository.QueryTerms queryTerms,
        QueryRouteProfile.RouteSearchSpec routeSpec
    ) {
        return routeSearchStep(queryTerms, routeSpec, DEFAULT_ROUTE_SEARCH_TOKEN_LIMIT);
    }

    static RouteSearchStep routeSearchStep(
        PackRepository.QueryTerms queryTerms,
        QueryRouteProfile.RouteSearchSpec routeSpec,
        int maxTokens
    ) {
        if (queryTerms == null || queryTerms.routeProfile == null || routeSpec == null) {
            return null;
        }
        if (!hasRouteSearchText(routeSpec)) {
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

    static boolean shouldScanFullRouteCursor(PackRepository.QueryTerms queryTerms) {
        if (queryTerms == null || queryTerms.metadataProfile == null) {
            return false;
        }
        String preferredStructureType = queryTerms.metadataProfile.preferredStructureType();
        if ("community_security".equals(preferredStructureType)
            || "community_governance".equals(preferredStructureType)) {
            return false;
        }
        return queryTerms.metadataProfile.hasExplicitTopicFocus()
            && RetrievalRoutePolicy.requiresSpecializedRouteAnchorSignal(preferredStructureType);
    }

    static int routeChunkCandidateLimit(PackRepository.QueryTerms queryTerms, int limit) {
        return RetrievalRoutePolicy.routeChunkCandidateLimit(
            queryTerms == null ? null : queryTerms.routeProfile,
            queryTerms == null ? null : queryTerms.metadataProfile,
            limit,
            usesCompactGuideSweep(queryTerms)
        );
    }

    static int routeChunkCandidateTarget(PackRepository.QueryTerms queryTerms, int limit) {
        return RetrievalRoutePolicy.routeChunkCandidateTarget(
            queryTerms == null ? null : queryTerms.routeProfile,
            queryTerms == null ? null : queryTerms.metadataProfile,
            limit,
            usesCompactGuideSweep(queryTerms)
        );
    }

    static boolean shouldBackfillLikeAfterFts(
        PackRepository.QueryTerms queryTerms,
        int addedWithFts,
        int totalSections,
        int targetTotal
    ) {
        return RetrievalRoutePolicy.shouldBackfillLikeAfterFts(
            queryTerms == null ? null : queryTerms.routeProfile,
            queryTerms == null ? null : queryTerms.metadataProfile,
            queryTerms == null ? 0 : queryTerms.primaryKeywordTokens().size(),
            addedWithFts,
            totalSections,
            targetTotal
        );
    }

    static RouteFtsOrderSpec noBm25RouteFtsOrder(PackRepository.QueryTerms queryTerms) {
        RetrievalRoutePolicy.RouteFtsOrderSpec orderSpec = RetrievalRoutePolicy.noBm25RouteFtsOrder(
            queryTerms == null ? "" : queryTerms.queryLower,
            queryTerms == null ? null : queryTerms.metadataProfile
        );
        return new RouteFtsOrderSpec(orderSpec.clause, orderSpec.args, orderSpec.label);
    }

    private static boolean usesCompactGuideSweep(PackRepository.QueryTerms queryTerms) {
        return queryTerms != null
            && queryTerms.routeProfile != null
            && queryTerms.routeProfile.usesCompactGuideSweep(queryTerms.queryLower);
    }

    private static boolean hasRouteSearchText(QueryRouteProfile.RouteSearchSpec routeSpec) {
        return routeSpec.text() != null && !routeSpec.text().trim().isEmpty();
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

    static final class RouteFtsOrderSpec {
        final String clause;
        final List<String> args;
        final String label;

        RouteFtsOrderSpec(String clause, List<String> args, String label) {
            this.clause = clause;
            this.args = args;
            this.label = label;
        }
    }
}
