package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class PackRouteSearchSqlPolicy {
    private PackRouteSearchSqlPolicy() {
    }

    static RouteFtsSqlPlan chunkFtsPlan(
        PackRepository.QueryTerms queryTerms,
        PackRepository.QueryTerms specTerms,
        List<String> categories,
        int candidateLimit,
        String ftsTableName,
        boolean ftsSupportsBm25
    ) {
        String ftsQuery = PackRepository.buildFtsQuery(specTerms, ftsSupportsBm25 ? 8 : 4, ftsSupportsBm25);
        if (ftsQuery.isEmpty() || categories.isEmpty()) {
            return RouteFtsSqlPlan.empty(ftsQuery);
        }

        ArrayList<String> categoryPlaceholders = new ArrayList<>();
        ArrayList<String> args = new ArrayList<>();
        args.add(ftsQuery);
        for (String category : categories) {
            categoryPlaceholders.add("?");
            args.add(category);
        }
        int effectiveCandidateLimit = ftsSupportsBm25
            ? candidateLimit
            : Math.min(candidateLimit, queryTerms.routeProfile.isStarterBuildProject() ? 120 : 84);
        RouteFtsOrderSpec orderSpec = routeFtsOrder(queryTerms, ftsTableName, ftsSupportsBm25);
        args.addAll(orderSpec.args);
        args.add(String.valueOf(effectiveCandidateLimit));

        return new RouteFtsSqlPlan(
            "SELECT c.guide_title, c.guide_id, c.section_heading, c.category, c.document, c.tags, c.description, " +
                "c.content_role, c.time_horizon, c.structure_type, c.topic_tags " +
                "FROM " + ftsTableName + " f " +
                "JOIN chunks c ON c.chunk_id = f.chunk_id " +
                "WHERE " + ftsTableName + " MATCH ? " +
                "AND c.category IN (" + String.join(",", categoryPlaceholders) + ") " +
                orderSpec.clause + " LIMIT ?",
            args,
            effectiveCandidateLimit,
            orderSpec.label,
            ftsQuery
        );
    }

    static RouteLikeSqlPlan chunkLikePlan(
        List<String> tokens,
        List<String> categories,
        int candidateLimit
    ) {
        if (tokens.isEmpty() || categories.isEmpty()) {
            return RouteLikeSqlPlan.empty();
        }

        ArrayList<String> categoryPlaceholders = new ArrayList<>();
        ArrayList<String> args = new ArrayList<>();
        for (String category : categories) {
            categoryPlaceholders.add("?");
            args.add(category);
        }

        ArrayList<String> clauses = new ArrayList<>();
        for (String token : tokens) {
            String like = "%" + token + "%";
            clauses.add("(guide_title LIKE ? OR section_heading LIKE ? OR tags LIKE ? OR description LIKE ? OR document LIKE ?)");
            for (int index = 0; index < 5; index++) {
                args.add(like);
            }
        }
        args.add(String.valueOf(candidateLimit));

        return new RouteLikeSqlPlan(
            "SELECT guide_title, guide_id, section_heading, category, document, tags, description, " +
                "content_role, time_horizon, structure_type, topic_tags " +
                "FROM chunks WHERE category IN (" + String.join(",", categoryPlaceholders) + ") " +
                "AND (" + String.join(" OR ", clauses) + ") LIMIT ?",
            args
        );
    }

    static RouteFtsSqlPlan guideFtsPlan(
        PackRepository.QueryTerms queryTerms,
        PackRepository.QueryTerms specTerms,
        List<String> categories,
        int candidateLimit,
        String ftsTableName,
        boolean ftsSupportsBm25
    ) {
        String ftsQuery = PackRepository.buildFtsQuery(specTerms, ftsSupportsBm25 ? 8 : 4, ftsSupportsBm25);
        if (ftsQuery.isEmpty() || categories.isEmpty()) {
            return RouteFtsSqlPlan.empty(ftsQuery);
        }

        ArrayList<String> categoryPlaceholders = new ArrayList<>();
        ArrayList<String> args = new ArrayList<>();
        args.add(ftsQuery);
        for (String category : categories) {
            categoryPlaceholders.add("?");
            args.add(category);
        }
        int effectiveCandidateLimit = ftsSupportsBm25
            ? candidateLimit
            : Math.min(candidateLimit, queryTerms.routeProfile.isStarterBuildProject() ? 48 : 24);
        RouteFtsOrderSpec orderSpec = routeFtsOrder(queryTerms, ftsTableName, ftsSupportsBm25);
        args.addAll(orderSpec.args);
        args.add(String.valueOf(effectiveCandidateLimit));

        return new RouteFtsSqlPlan(
            "SELECT c.guide_id, c.guide_title, c.section_heading, c.category, c.description, c.document, " +
                "c.content_role, c.time_horizon, c.structure_type, c.topic_tags, c.tags " +
                "FROM " + ftsTableName + " f " +
                "JOIN chunks c ON c.chunk_id = f.chunk_id " +
                "WHERE " + ftsTableName + " MATCH ? " +
                "AND c.category IN (" + String.join(",", categoryPlaceholders) + ") " +
                orderSpec.clause + " LIMIT ?",
            args,
            effectiveCandidateLimit,
            orderSpec.label,
            ftsQuery
        );
    }

    static RouteLikeSqlPlan guideLikePlan(
        List<String> tokens,
        List<String> categories,
        int candidateLimit
    ) {
        if (tokens.isEmpty() || categories.isEmpty()) {
            return RouteLikeSqlPlan.empty();
        }

        ArrayList<String> categoryPlaceholders = new ArrayList<>();
        ArrayList<String> args = new ArrayList<>();
        for (String category : categories) {
            categoryPlaceholders.add("?");
            args.add(category);
        }

        ArrayList<String> clauses = new ArrayList<>();
        for (String token : tokens) {
            String like = "%" + token + "%";
            clauses.add("(title LIKE ? OR description LIKE ? OR topic_tags LIKE ?)");
            args.add(like);
            args.add(like);
            args.add(like);
        }
        args.add(String.valueOf(candidateLimit));

        return new RouteLikeSqlPlan(
            "SELECT guide_id, title, category, description, body_markdown, " +
                "content_role, time_horizon, structure_type, topic_tags " +
                "FROM guides WHERE category IN (" + String.join(",", categoryPlaceholders) + ") " +
                "AND (" + String.join(" OR ", clauses) + ") LIMIT ?",
            args
        );
    }

    static RouteFtsOrderSpec noBm25RouteFtsOrder(PackRepository.QueryTerms queryTerms) {
        PackRouteFtsOrderPolicy.RouteFtsOrderSpec orderSpec = PackRouteFtsOrderPolicy.noBm25RouteFtsOrder(
            queryTerms == null ? "" : queryTerms.queryLower,
            queryTerms == null ? null : queryTerms.metadataProfile
        );
        return new RouteFtsOrderSpec(orderSpec.clause, orderSpec.args, orderSpec.label);
    }

    private static RouteFtsOrderSpec routeFtsOrder(
        PackRepository.QueryTerms queryTerms,
        String ftsTableName,
        boolean ftsSupportsBm25
    ) {
        return ftsSupportsBm25
            ? new RouteFtsOrderSpec(
                " ORDER BY bm25(" + ftsTableName + ") ",
                Collections.emptyList(),
                "bm25"
            )
            : noBm25RouteFtsOrder(queryTerms);
    }

    static final class RouteFtsSqlPlan {
        final String sql;
        final List<String> args;
        final int effectiveCandidateLimit;
        final String orderLabel;
        final String ftsQuery;

        private RouteFtsSqlPlan(
            String sql,
            List<String> args,
            int effectiveCandidateLimit,
            String orderLabel,
            String ftsQuery
        ) {
            this.sql = sql;
            this.args = args;
            this.effectiveCandidateLimit = effectiveCandidateLimit;
            this.orderLabel = orderLabel;
            this.ftsQuery = ftsQuery;
        }

        private static RouteFtsSqlPlan empty(String ftsQuery) {
            return new RouteFtsSqlPlan("", Collections.emptyList(), 0, "", ftsQuery);
        }

        boolean isEmpty() {
            return sql.isEmpty();
        }

        String[] argsArray() {
            return args.toArray(new String[0]);
        }
    }

    static final class RouteLikeSqlPlan {
        final String sql;
        final List<String> args;

        private RouteLikeSqlPlan(String sql, List<String> args) {
            this.sql = sql;
            this.args = args;
        }

        private static RouteLikeSqlPlan empty() {
            return new RouteLikeSqlPlan("", Collections.emptyList());
        }

        boolean isEmpty() {
            return sql.isEmpty();
        }

        String[] argsArray() {
            return args.toArray(new String[0]);
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
