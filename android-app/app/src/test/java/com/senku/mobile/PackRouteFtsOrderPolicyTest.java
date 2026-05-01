package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public final class PackRouteFtsOrderPolicyTest {
    @Test
    public void noBm25OrderLabelsStayInRoutePrecedenceOrder() {
        assertEquals(
            List.of(
                "soapmaking_priority",
                "water_distribution_priority",
                "clay_oven_priority",
                "community_governance_priority",
                "site_selection_microclimate_priority",
                "site_selection_priority",
                "roofing_priority",
                "wall_construction_priority",
                "foundation_priority",
                "rowid"
            ),
            PackRouteFtsOrderPolicy.orderedLabelsForTest()
        );
    }

    @Test
    public void noBm25OrderArgCountsStayAlignedWithCaseTerms() {
        assertEquals(12, PackRouteFtsOrderPolicy.argCountForLabelForTest("soapmaking_priority"));
        assertEquals(10, PackRouteFtsOrderPolicy.argCountForLabelForTest("water_distribution_priority"));
        assertEquals(12, PackRouteFtsOrderPolicy.argCountForLabelForTest("clay_oven_priority"));
        assertEquals(12, PackRouteFtsOrderPolicy.argCountForLabelForTest("community_governance_priority"));
        assertEquals(10, PackRouteFtsOrderPolicy.argCountForLabelForTest("site_selection_microclimate_priority"));
        assertEquals(9, PackRouteFtsOrderPolicy.argCountForLabelForTest("site_selection_priority"));
        assertEquals(8, PackRouteFtsOrderPolicy.argCountForLabelForTest("roofing_priority"));
        assertEquals(6, PackRouteFtsOrderPolicy.argCountForLabelForTest("wall_construction_priority"));
        assertEquals(6, PackRouteFtsOrderPolicy.argCountForLabelForTest("foundation_priority"));
        assertEquals(0, PackRouteFtsOrderPolicy.argCountForLabelForTest("rowid"));
    }

    @Test
    public void noBm25OrderSelectionPreservesSpecializedLabels() {
        assertOrderLabel("how do i make soap from animal fat and ash", "soapmaking_priority");
        assertOrderLabel("how do i design a gravity-fed water distribution system", "water_distribution_priority");
        assertOrderLabel("how do i build a clay oven", "clay_oven_priority");
        assertOrderLabel(
            "How do we merge with another group if we don't trust each other yet?",
            "community_governance_priority"
        );
        assertOrderLabel(
            "winter sun summer shade site selection cabin house",
            "site_selection_microclimate_priority"
        );
        assertOrderLabel(
            "How do I choose a building site if drainage, wind, sun, and access all matter?",
            "site_selection_priority"
        );
        assertOrderLabel("what about sealing the roof", "roofing_priority");
        assertOrderLabel("how do i frame a cabin wall with rough lumber", "wall_construction_priority");
        assertOrderLabel("how do i pour a cabin foundation with limited cement", "foundation_priority");
        assertOrderLabel("how do i build a house", "rowid");
    }

    private static void assertOrderLabel(String query, String expectedLabel) {
        PackRepository.QueryTerms queryTerms = PackRepository.QueryTerms.fromQuery(query);
        PackRouteFtsOrderPolicy.RouteFtsOrderSpec orderSpec =
            PackRouteFtsOrderPolicy.noBm25RouteFtsOrder(queryTerms.queryLower, queryTerms.metadataProfile);

        assertEquals(query, expectedLabel, orderSpec.label);
        assertEquals(
            query,
            PackRouteFtsOrderPolicy.argCountForLabelForTest(expectedLabel),
            orderSpec.args.size()
        );
    }
}
