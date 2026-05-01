package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public final class PackRouteSearchSqlPolicyTest {
    @Test
    public void chunkFtsPlanPreservesBm25SqlArgsLimitAndOrderLabel() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery("how do i build a house");
        PackRepository.QueryTerms specTerms = PackRepository.QueryTerms.fromText("timber frame", queryTerms.routeProfile);

        PackRouteSearchSqlPolicy.RouteFtsSqlPlan plan = PackRouteSearchSqlPolicy.chunkFtsPlan(
            queryTerms,
            specTerms,
            List.of("building", "shelter"),
            37,
            "chunks_fts",
            true
        );

        assertEquals(
            "SELECT c.guide_title, c.guide_id, c.section_heading, c.category, c.document, c.tags, c.description, " +
                "c.content_role, c.time_horizon, c.structure_type, c.topic_tags " +
                "FROM chunks_fts f " +
                "JOIN chunks c ON c.chunk_id = f.chunk_id " +
                "WHERE chunks_fts MATCH ? " +
                "AND c.category IN (?,?) " +
                " ORDER BY bm25(chunks_fts)  LIMIT ?",
            plan.sql
        );
        assertEquals(
            List.of(PackRepository.buildFtsQuery(specTerms, 8, true), "building", "shelter", "37"),
            plan.args
        );
        assertEquals(37, plan.effectiveCandidateLimit);
        assertEquals("bm25", plan.orderLabel);
    }

    @Test
    public void chunkFtsPlanPreservesNoBm25StarterLimitCapAndOrderLabel() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery("how do i build a house");
        PackRepository.QueryTerms specTerms = PackRepository.QueryTerms.fromText("site foundation", queryTerms.routeProfile);

        PackRouteSearchSqlPolicy.RouteFtsSqlPlan plan = PackRouteSearchSqlPolicy.chunkFtsPlan(
            queryTerms,
            specTerms,
            List.of("building"),
            600,
            "chunks_fts",
            false
        );

        assertEquals(120, plan.effectiveCandidateLimit);
        assertEquals("rowid", plan.orderLabel);
        assertEquals("120", plan.args.get(plan.args.size() - 1));
    }

    @Test
    public void chunkFtsPlanPreservesSpecializedNoBm25OrderArgsBeforeLimit() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(
            "how do i design a gravity-fed water distribution system"
        );
        PackRepository.QueryTerms specTerms = PackRepository.QueryTerms.fromText(
            "gravity distribution",
            queryTerms.routeProfile
        );

        PackRouteSearchSqlPolicy.RouteFtsSqlPlan plan = PackRouteSearchSqlPolicy.chunkFtsPlan(
            queryTerms,
            specTerms,
            List.of("water"),
            600,
            "chunks_fts",
            false
        );

        assertEquals("water_distribution_priority", plan.orderLabel);
        assertEquals(84, plan.effectiveCandidateLimit);
        assertEquals(13, plan.args.size());
        assertEquals(PackRepository.buildFtsQuery(specTerms, 4, false), plan.args.get(0));
        assertEquals("water", plan.args.get(1));
        assertEquals("%gravity-fed%", plan.args.get(2));
        assertEquals("%water tower%", plan.args.get(11));
        assertEquals("84", plan.args.get(12));
    }

    @Test
    public void guideFtsPlanPreservesNoBm25StarterLimitCapAndProjection() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery("how do i build a house");
        PackRepository.QueryTerms specTerms = PackRepository.QueryTerms.fromText("site foundation", queryTerms.routeProfile);

        PackRouteSearchSqlPolicy.RouteFtsSqlPlan plan = PackRouteSearchSqlPolicy.guideFtsPlan(
            queryTerms,
            specTerms,
            List.of("building"),
            72,
            "chunks_fts",
            false
        );

        assertEquals(
            "SELECT c.guide_id, c.guide_title, c.section_heading, c.category, c.description, c.document, " +
                "c.content_role, c.time_horizon, c.structure_type, c.topic_tags, c.tags " +
                "FROM chunks_fts f " +
                "JOIN chunks c ON c.chunk_id = f.chunk_id " +
                "WHERE chunks_fts MATCH ? " +
                "AND c.category IN (?) " +
                " ORDER BY c.row_id  LIMIT ?",
            plan.sql
        );
        assertEquals(48, plan.effectiveCandidateLimit);
        assertEquals("rowid", plan.orderLabel);
        assertEquals("48", plan.args.get(plan.args.size() - 1));
    }

    @Test
    public void chunkLikePlanPreservesSqlAndRepeatedTokenArgs() {
        PackRouteSearchSqlPolicy.RouteLikeSqlPlan plan = PackRouteSearchSqlPolicy.chunkLikePlan(
            List.of("alpha", "beta"),
            List.of("building", "water"),
            42
        );

        assertEquals(
            "SELECT guide_title, guide_id, section_heading, category, document, tags, description, " +
                "content_role, time_horizon, structure_type, topic_tags " +
                "FROM chunks WHERE category IN (?,?) " +
                "AND ((guide_title LIKE ? OR section_heading LIKE ? OR tags LIKE ? OR description LIKE ? OR document LIKE ?) " +
                "OR (guide_title LIKE ? OR section_heading LIKE ? OR tags LIKE ? OR description LIKE ? OR document LIKE ?)) LIMIT ?",
            plan.sql
        );
        assertEquals(
            List.of(
                "building",
                "water",
                "%alpha%",
                "%alpha%",
                "%alpha%",
                "%alpha%",
                "%alpha%",
                "%beta%",
                "%beta%",
                "%beta%",
                "%beta%",
                "%beta%",
                "42"
            ),
            plan.args
        );
    }

    @Test
    public void guideLikePlanPreservesMetadataOnlySqlAndArgs() {
        PackRouteSearchSqlPolicy.RouteLikeSqlPlan plan = PackRouteSearchSqlPolicy.guideLikePlan(
            List.of("alpha", "beta"),
            List.of("building"),
            24
        );

        assertEquals(
            "SELECT guide_id, title, category, description, body_markdown, " +
                "content_role, time_horizon, structure_type, topic_tags " +
                "FROM guides WHERE category IN (?) " +
                "AND ((title LIKE ? OR description LIKE ? OR topic_tags LIKE ?) " +
                "OR (title LIKE ? OR description LIKE ? OR topic_tags LIKE ?)) LIMIT ?",
            plan.sql
        );
        assertEquals(
            List.of("building", "%alpha%", "%alpha%", "%alpha%", "%beta%", "%beta%", "%beta%", "24"),
            plan.args
        );
    }
}
