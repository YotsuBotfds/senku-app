package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class PackPlainSearchSqlPolicy {
    private PackPlainSearchSqlPolicy() {
    }

    static PlainSqlPlan ftsHitsPlan(
        PackRepository.QueryTerms queryTerms,
        int limit,
        String ftsTableName,
        boolean ftsSupportsBm25
    ) {
        String ftsQuery = PackRepository.buildFtsQuery(queryTerms);
        String noOpReason = ftsNoOpReason(ftsQuery, ftsTableName, limit);
        if (!noOpReason.isEmpty()) {
            return PlainSqlPlan.empty(ftsQuery, noOpReason);
        }

        String orderClause = ftsSupportsBm25 ? " ORDER BY bm25(" + ftsTableName + ") " : " ";
        return new PlainSqlPlan(
            "SELECT c.chunk_id, c.vector_row_id, c.guide_title, c.guide_id, c.section_heading, c.category, c.document, " +
                "c.content_role, c.time_horizon, c.structure_type, c.topic_tags " +
                "FROM " + ftsTableName + " f " +
                "JOIN chunks c ON c.chunk_id = f.chunk_id " +
                "WHERE " + ftsTableName + " MATCH ? " +
                orderClause +
                "LIMIT ?",
            List.of(ftsQuery, String.valueOf(limit)),
            limit,
            ftsQuery,
            ""
        );
    }

    static PlainSqlPlan keywordHitsPlan(PackRepository.QueryTerms queryTerms, int limit) {
        List<String> tokens = queryTerms == null ? Collections.emptyList() : queryTerms.keywordTokens();
        if (tokens.isEmpty()) {
            return PlainSqlPlan.empty("", "empty_tokens");
        }
        if (limit <= 0) {
            return PlainSqlPlan.empty("", "nonpositive_limit");
        }

        int sqlLimit = RetrievalRoutePolicy.keywordSqlLimit(
            queryTerms == null ? null : queryTerms.routeProfile,
            limit
        );
        ArrayList<String> clauses = new ArrayList<>();
        ArrayList<String> args = new ArrayList<>();
        for (String token : tokens) {
            String like = "%" + token + "%";
            clauses.add("(guide_title LIKE ? OR section_heading LIKE ? OR category LIKE ? OR tags LIKE ? OR description LIKE ? OR document LIKE ?)");
            for (int index = 0; index < 6; index++) {
                args.add(like);
            }
        }
        args.add(String.valueOf(sqlLimit));

        return new PlainSqlPlan(
            "SELECT chunk_id, vector_row_id, guide_title, guide_id, section_heading, category, document, tags, description, " +
                "content_role, time_horizon, structure_type, topic_tags " +
                "FROM chunks WHERE " + String.join(" OR ", clauses) + " LIMIT ?",
            args,
            sqlLimit,
            "",
            ""
        );
    }

    static PlainSqlPlan plainLikeResultsPlan(String query, int limit) {
        String safeQuery = query == null ? "" : query;
        if (safeQuery.isEmpty()) {
            return PlainSqlPlan.empty("", "empty_query");
        }
        if (limit <= 0) {
            return PlainSqlPlan.empty("", "nonpositive_limit");
        }

        String like = "%" + safeQuery + "%";
        return new PlainSqlPlan(
            "SELECT guide_title, guide_id, section_heading, category, document, " +
                "content_role, time_horizon, structure_type, topic_tags " +
                "FROM chunks " +
                "WHERE document LIKE ? OR guide_title LIKE ? OR tags LIKE ? OR category LIKE ? OR description LIKE ? " +
                "LIMIT ?",
            List.of(like, like, like, like, like, String.valueOf(limit)),
            limit,
            "",
            ""
        );
    }

    static PlainSqlPlan vectorNeighborHitsPlan(List<VectorStore.VectorNeighbor> neighbors) {
        if (neighbors == null || neighbors.isEmpty()) {
            return PlainSqlPlan.empty("", "empty_neighbors");
        }

        ArrayList<String> placeholders = new ArrayList<>();
        ArrayList<String> args = new ArrayList<>();
        for (VectorStore.VectorNeighbor neighbor : neighbors) {
            if (neighbor == null) {
                continue;
            }
            placeholders.add("?");
            args.add(String.valueOf(neighbor.rowId));
        }
        if (args.isEmpty()) {
            return PlainSqlPlan.empty("", "empty_neighbors");
        }

        return new PlainSqlPlan(
            "SELECT chunk_id, vector_row_id, guide_title, guide_id, section_heading, category, document, " +
                "content_role, time_horizon, structure_type, topic_tags " +
                "FROM chunks WHERE vector_row_id IN (" + String.join(",", placeholders) + ")",
            args,
            args.size(),
            "",
            ""
        );
    }

    private static String ftsNoOpReason(String ftsQuery, String ftsTableName, int limit) {
        if (ftsQuery.isEmpty()) {
            return "empty_fts_query";
        }
        if (ftsTableName == null || ftsTableName.trim().isEmpty()) {
            return "empty_fts_table";
        }
        if (limit <= 0) {
            return "nonpositive_limit";
        }
        return "";
    }

    static final class PlainSqlPlan {
        final String sql;
        final List<String> args;
        final int effectiveLimit;
        final String ftsQuery;
        final String noOpReason;

        private PlainSqlPlan(
            String sql,
            List<String> args,
            int effectiveLimit,
            String ftsQuery,
            String noOpReason
        ) {
            this.sql = sql;
            this.args = args;
            this.effectiveLimit = effectiveLimit;
            this.ftsQuery = ftsQuery;
            this.noOpReason = noOpReason;
        }

        private static PlainSqlPlan empty(String ftsQuery, String noOpReason) {
            return new PlainSqlPlan("", Collections.emptyList(), 0, ftsQuery, noOpReason);
        }

        boolean isEmpty() {
            return sql.isEmpty();
        }

        String[] argsArray() {
            return args.toArray(new String[0]);
        }
    }
}
