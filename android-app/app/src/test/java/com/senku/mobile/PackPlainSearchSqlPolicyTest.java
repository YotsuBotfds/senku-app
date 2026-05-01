package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

public final class PackPlainSearchSqlPolicyTest {
    @Test
    public void ftsHitsPlanPreservesBm25SqlArgsAndEffectiveLimit() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery("wet fire");

        PackPlainSearchSqlPolicy.PlainSqlPlan plan = PackPlainSearchSqlPolicy.ftsHitsPlan(
            queryTerms,
            37,
            "chunks_fts",
            true
        );

        assertEquals(
            "SELECT c.chunk_id, c.vector_row_id, c.guide_title, c.guide_id, c.section_heading, c.category, c.document, " +
                "c.content_role, c.time_horizon, c.structure_type, c.topic_tags " +
                "FROM chunks_fts f " +
                "JOIN chunks c ON c.chunk_id = f.chunk_id " +
                "WHERE chunks_fts MATCH ? " +
                " ORDER BY bm25(chunks_fts) " +
                "LIMIT ?",
            plan.sql
        );
        assertEquals(List.of(PackRepository.buildFtsQuery(queryTerms), "37"), plan.args);
        assertEquals(37, plan.effectiveLimit);
        assertEquals(PackRepository.buildFtsQuery(queryTerms), plan.ftsQuery);
        assertEquals("", plan.noOpReason);
    }

    @Test
    public void ftsHitsPlanPreservesNoBm25SqlShape() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery("wet fire");

        PackPlainSearchSqlPolicy.PlainSqlPlan plan = PackPlainSearchSqlPolicy.ftsHitsPlan(
            queryTerms,
            72,
            "chunks_fts",
            false
        );

        assertEquals(
            "SELECT c.chunk_id, c.vector_row_id, c.guide_title, c.guide_id, c.section_heading, c.category, c.document, " +
                "c.content_role, c.time_horizon, c.structure_type, c.topic_tags " +
                "FROM chunks_fts f " +
                "JOIN chunks c ON c.chunk_id = f.chunk_id " +
                "WHERE chunks_fts MATCH ? " +
                " LIMIT ?",
            plan.sql
        );
        assertEquals(List.of(PackRepository.buildFtsQuery(queryTerms), "72"), plan.args);
        assertEquals(72, plan.effectiveLimit);
    }

    @Test
    public void ftsHitsPlanNoOpsWhenTermsTableOrLimitAreInvalid() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery("wet fire");

        PackPlainSearchSqlPolicy.PlainSqlPlan emptyTerms = PackPlainSearchSqlPolicy.ftsHitsPlan(
            null,
            72,
            "chunks_fts",
            true
        );
        PackPlainSearchSqlPolicy.PlainSqlPlan missingTable = PackPlainSearchSqlPolicy.ftsHitsPlan(
            queryTerms,
            72,
            " ",
            true
        );
        PackPlainSearchSqlPolicy.PlainSqlPlan zeroLimit = PackPlainSearchSqlPolicy.ftsHitsPlan(
            queryTerms,
            0,
            "chunks_fts",
            true
        );

        assertTrue(emptyTerms.isEmpty());
        assertEquals("empty_fts_query", emptyTerms.noOpReason);
        assertTrue(emptyTerms.args.isEmpty());
        assertTrue(missingTable.isEmpty());
        assertEquals("empty_fts_table", missingTable.noOpReason);
        assertEquals(PackRepository.buildFtsQuery(queryTerms), missingTable.ftsQuery);
        assertTrue(zeroLimit.isEmpty());
        assertEquals("nonpositive_limit", zeroLimit.noOpReason);
    }

    @Test
    public void keywordHitsPlanPreservesSqlRepeatedArgsAndEffectiveSqlLimit() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery("wet fire");
        List<String> tokens = queryTerms.keywordTokens();

        PackPlainSearchSqlPolicy.PlainSqlPlan plan = PackPlainSearchSqlPolicy.keywordHitsPlan(queryTerms, 11);

        assertEquals(
            "SELECT chunk_id, vector_row_id, guide_title, guide_id, section_heading, category, document, tags, description, " +
                "content_role, time_horizon, structure_type, topic_tags " +
                "FROM chunks WHERE " +
                String.join(" OR ", Collections.nCopies(tokens.size(), keywordClause())) + " " +
                "LIMIT ?",
            plan.sql
        );
        assertEquals(keywordArgs(tokens, RetrievalRoutePolicy.keywordSqlLimit(queryTerms.routeProfile, 11)), plan.args);
        assertEquals(RetrievalRoutePolicy.keywordSqlLimit(queryTerms.routeProfile, 11), plan.effectiveLimit);
        assertEquals("", plan.noOpReason);
    }

    @Test
    public void keywordHitsPlanNoOpsWhenTokensOrLimitAreInvalid() {
        PackPlainSearchSqlPolicy.PlainSqlPlan noTerms = PackPlainSearchSqlPolicy.keywordHitsPlan(
            null,
            11
        );
        PackPlainSearchSqlPolicy.PlainSqlPlan zeroLimit = PackPlainSearchSqlPolicy.keywordHitsPlan(
            PackRepository.QueryTerms.fromQuery("wet fire"),
            0
        );

        assertTrue(noTerms.isEmpty());
        assertEquals("empty_tokens", noTerms.noOpReason);
        assertTrue(noTerms.args.isEmpty());
        assertTrue(zeroLimit.isEmpty());
        assertEquals("nonpositive_limit", zeroLimit.noOpReason);
    }

    @Test
    public void plainLikeResultsPlanPreservesSqlArgsAndLimit() {
        PackPlainSearchSqlPolicy.PlainSqlPlan plan = PackPlainSearchSqlPolicy.plainLikeResultsPlan("clay oven", 9);

        assertEquals(
            "SELECT guide_title, guide_id, section_heading, category, document, " +
                "content_role, time_horizon, structure_type, topic_tags " +
                "FROM chunks " +
                "WHERE document LIKE ? OR guide_title LIKE ? OR tags LIKE ? OR category LIKE ? OR description LIKE ? " +
                "LIMIT ?",
            plan.sql
        );
        assertEquals(List.of("%clay oven%", "%clay oven%", "%clay oven%", "%clay oven%", "%clay oven%", "9"), plan.args);
        assertEquals(9, plan.effectiveLimit);
        assertEquals("", plan.noOpReason);
    }

    @Test
    public void plainLikeResultsPlanNoOpsWhenLimitIsInvalid() {
        PackPlainSearchSqlPolicy.PlainSqlPlan plan = PackPlainSearchSqlPolicy.plainLikeResultsPlan("clay oven", 0);

        assertTrue(plan.isEmpty());
        assertEquals("nonpositive_limit", plan.noOpReason);
        assertTrue(plan.args.isEmpty());
    }

    @Test
    public void plainLikeResultsPlanNoOpsWhenQueryIsNullOrEmpty() {
        PackPlainSearchSqlPolicy.PlainSqlPlan nullQuery = PackPlainSearchSqlPolicy.plainLikeResultsPlan(null, 9);
        PackPlainSearchSqlPolicy.PlainSqlPlan emptyQuery = PackPlainSearchSqlPolicy.plainLikeResultsPlan("", 9);

        assertTrue(nullQuery.isEmpty());
        assertEquals("empty_query", nullQuery.noOpReason);
        assertTrue(nullQuery.args.isEmpty());
        assertTrue(emptyQuery.isEmpty());
        assertEquals("empty_query", emptyQuery.noOpReason);
        assertTrue(emptyQuery.args.isEmpty());
    }

    @Test
    public void vectorNeighborHitsPlanPreservesSqlArgsAndEffectiveLimit() {
        PackPlainSearchSqlPolicy.PlainSqlPlan plan = PackPlainSearchSqlPolicy.vectorNeighborHitsPlan(
            List.of(new VectorStore.VectorNeighbor(7, 0.9f), new VectorStore.VectorNeighbor(12, 0.7f))
        );

        assertEquals(
            "SELECT chunk_id, vector_row_id, guide_title, guide_id, section_heading, category, document, " +
                "content_role, time_horizon, structure_type, topic_tags " +
                "FROM chunks WHERE vector_row_id IN (?,?)",
            plan.sql
        );
        assertEquals(List.of("7", "12"), plan.args);
        assertEquals(2, plan.effectiveLimit);
        assertEquals("", plan.noOpReason);
    }

    @Test
    public void vectorNeighborHitsPlanNoOpsWhenNeighborsAreEmpty() {
        PackPlainSearchSqlPolicy.PlainSqlPlan plan = PackPlainSearchSqlPolicy.vectorNeighborHitsPlan(Collections.emptyList());

        assertTrue(plan.isEmpty());
        assertEquals("empty_neighbors", plan.noOpReason);
        assertTrue(plan.args.isEmpty());
    }

    @Test
    public void vectorNeighborHitsPlanSkipsNullNeighborsAndNoOpsWhenOnlyNull() {
        PackPlainSearchSqlPolicy.PlainSqlPlan mixed = PackPlainSearchSqlPolicy.vectorNeighborHitsPlan(
            java.util.Arrays.asList(null, new VectorStore.VectorNeighbor(12, 0.7f))
        );
        PackPlainSearchSqlPolicy.PlainSqlPlan onlyNull = PackPlainSearchSqlPolicy.vectorNeighborHitsPlan(
            Collections.singletonList(null)
        );

        assertEquals(
            "SELECT chunk_id, vector_row_id, guide_title, guide_id, section_heading, category, document, " +
                "content_role, time_horizon, structure_type, topic_tags " +
                "FROM chunks WHERE vector_row_id IN (?)",
            mixed.sql
        );
        assertEquals(List.of("12"), mixed.args);
        assertEquals(1, mixed.effectiveLimit);
        assertTrue(onlyNull.isEmpty());
        assertEquals("empty_neighbors", onlyNull.noOpReason);
        assertTrue(onlyNull.args.isEmpty());
    }

    private static String keywordClause() {
        return "(guide_title LIKE ? OR section_heading LIKE ? OR category LIKE ? OR tags LIKE ? OR description LIKE ? OR document LIKE ?)";
    }

    private static List<String> keywordArgs(List<String> tokens, int limit) {
        java.util.ArrayList<String> args = new java.util.ArrayList<>();
        for (String token : tokens) {
            String like = "%" + token + "%";
            for (int index = 0; index < 6; index++) {
                args.add(like);
            }
        }
        args.add(String.valueOf(limit));
        return args;
    }
}
