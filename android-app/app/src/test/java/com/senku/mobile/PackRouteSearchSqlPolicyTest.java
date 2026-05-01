package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    public void chunkFtsPlanKeepsEveryNoBm25OrderArgsBeforeLimit() {
        assertNoBm25OrderArgsBeforeLimit("how do i make soap from animal fat and ash", "soapmaking_priority");
        assertNoBm25OrderArgsBeforeLimit(
            "how do i design a gravity-fed water distribution system",
            "water_distribution_priority"
        );
        assertNoBm25OrderArgsBeforeLimit("how do i build a clay oven", "clay_oven_priority");
        assertNoBm25OrderArgsBeforeLimit(
            "How do we merge with another group if we don't trust each other yet?",
            "community_governance_priority"
        );
        assertNoBm25OrderArgsBeforeLimit(
            "winter sun summer shade site selection cabin house",
            "site_selection_microclimate_priority"
        );
        assertNoBm25OrderArgsBeforeLimit(
            "How do I choose a building site if drainage, wind, sun, and access all matter?",
            "site_selection_priority"
        );
        assertNoBm25OrderArgsBeforeLimit("what about sealing the roof", "roofing_priority");
        assertNoBm25OrderArgsBeforeLimit(
            "how do i frame a cabin wall with rough lumber",
            "wall_construction_priority"
        );
        assertNoBm25OrderArgsBeforeLimit(
            "how do i pour a cabin foundation with limited cement",
            "foundation_priority"
        );
        assertNoBm25OrderArgsBeforeLimit("how do i build a house", "rowid");
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
    public void ftsPlansNoOpWhenCategoriesAreEmpty() {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery("how do i build a house");
        PackRepository.QueryTerms specTerms = PackRepository.QueryTerms.fromText("site foundation", queryTerms.routeProfile);

        PackRouteSearchSqlPolicy.RouteFtsSqlPlan chunkPlan = PackRouteSearchSqlPolicy.chunkFtsPlan(
            queryTerms,
            specTerms,
            List.of(),
            72,
            "chunks_fts",
            true
        );
        PackRouteSearchSqlPolicy.RouteFtsSqlPlan guidePlan = PackRouteSearchSqlPolicy.guideFtsPlan(
            queryTerms,
            specTerms,
            List.of(),
            72,
            "chunks_fts",
            true
        );

        assertTrue(chunkPlan.isEmpty());
        assertEquals(PackRepository.buildFtsQuery(specTerms, 8, true), chunkPlan.ftsQuery);
        assertTrue(chunkPlan.args.isEmpty());
        assertTrue(guidePlan.isEmpty());
        assertEquals(PackRepository.buildFtsQuery(specTerms, 8, true), guidePlan.ftsQuery);
        assertTrue(guidePlan.args.isEmpty());
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
    public void chunkLikePlanNoOpsWhenTokensOrCategoriesAreEmpty() {
        PackRouteSearchSqlPolicy.RouteLikeSqlPlan noTokens = PackRouteSearchSqlPolicy.chunkLikePlan(
            List.of(),
            List.of("building"),
            42
        );
        PackRouteSearchSqlPolicy.RouteLikeSqlPlan noCategories = PackRouteSearchSqlPolicy.chunkLikePlan(
            List.of("alpha"),
            List.of(),
            42
        );

        assertTrue(noTokens.isEmpty());
        assertTrue(noTokens.args.isEmpty());
        assertTrue(noCategories.isEmpty());
        assertTrue(noCategories.args.isEmpty());
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

    @Test
    public void guideLikePlanNoOpsWhenTokensOrCategoriesAreEmpty() {
        PackRouteSearchSqlPolicy.RouteLikeSqlPlan noTokens = PackRouteSearchSqlPolicy.guideLikePlan(
            List.of(),
            List.of("building"),
            24
        );
        PackRouteSearchSqlPolicy.RouteLikeSqlPlan noCategories = PackRouteSearchSqlPolicy.guideLikePlan(
            List.of("alpha"),
            List.of(),
            24
        );

        assertTrue(noTokens.isEmpty());
        assertTrue(noTokens.args.isEmpty());
        assertTrue(noCategories.isEmpty());
        assertTrue(noCategories.args.isEmpty());
    }

    private static void assertNoBm25OrderArgsBeforeLimit(String query, String expectedLabel) {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(query);
        PackRepository.QueryTerms specTerms = PackRepository.QueryTerms.fromText("route focus", queryTerms.routeProfile);
        PackRouteFtsOrderPolicy.RouteFtsOrderSpec orderSpec =
            PackRouteFtsOrderPolicy.noBm25RouteFtsOrder(queryTerms.queryLower, queryTerms.metadataProfile);

        PackRouteSearchSqlPolicy.RouteFtsSqlPlan plan = PackRouteSearchSqlPolicy.chunkFtsPlan(
            queryTerms,
            specTerms,
            List.of("route-category"),
            600,
            "chunks_fts",
            false
        );

        int orderArgStart = 2;
        int orderArgEnd = orderArgStart + PackRouteFtsOrderPolicy.argCountForLabelForTest(expectedLabel);
        assertEquals(query, expectedLabel, plan.orderLabel);
        assertEquals(query, expectedLabel, orderSpec.label);
        assertEquals(query, orderSpec.args, plan.args.subList(orderArgStart, orderArgEnd));
        assertEquals(query, String.valueOf(plan.effectiveCandidateLimit), plan.args.get(orderArgEnd));
        assertEquals(query, orderArgEnd + 1, plan.args.size());
    }
}
