package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public final class DetailSourceStackPolicyTest {
    @Test
    public void orderAnswerSourceStackDedupeExactRowsBeforeReviewedOrdering() {
        SearchResult duplicateAnchor = source(
            "GD-220",
            "Abrasives Manufacturing",
            "Foundry preparation",
            "Abrasives manufacturing starts with controlled foundry safety checks."
        );
        SearchResult topic = source(
            "GD-345",
            "Rain Shelter",
            "Tarp rigging",
            "A simple tarp and cord ridgeline shelter keeps rain away from camp."
        );
        SearchResult related = source(
            "GD-132",
            "Foundry & Metal Casting",
            "Pour zone",
            "Keep foundry water and bystanders away before metal casting begins."
        );

        List<SearchResult> ordered = DetailSourceStackPolicy.orderAnswerSourceStack(
            true,
            List.of(topic, related, duplicateAnchor, duplicateAnchor)
        );

        assertEquals(3, ordered.size());
        assertSame(duplicateAnchor, ordered.get(0));
        assertSame(related, ordered.get(1));
        assertSame(topic, ordered.get(2));
    }

    @Test
    public void orderAnswerSourceStackLeavesNonReviewedRowsInOriginalDedupeOrder() {
        SearchResult first = source("GD-100", "First", "A", "ordinary source body");
        SearchResult second = source("GD-200", "Second", "B", "another ordinary source body");

        List<SearchResult> ordered = DetailSourceStackPolicy.orderAnswerSourceStack(
            true,
            List.of(first, second, first)
        );

        assertEquals(2, ordered.size());
        assertSame(first, ordered.get(0));
        assertSame(second, ordered.get(1));
    }

    @Test
    public void reviewedSourceStackEntryRequiresEnabledMatchingGuideAndText() {
        DetailSourceStackPolicy.ReviewedSourceStackEntry disabled =
            DetailSourceStackPolicy.reviewedSourceStackEntry(
                false,
                source("GD-220", "Abrasives", "", "abrasives manufacturing foundry")
            );
        DetailSourceStackPolicy.ReviewedSourceStackEntry anchor =
            DetailSourceStackPolicy.reviewedSourceStackEntry(
                true,
                source("gd-220", "Abrasives", "", "abrasives manufacturing foundry")
            );

        assertFalse(disabled.isPresent());
        assertTrue(anchor.isPresent());
        assertTrue(anchor.isAnchor());
        assertEquals("GD-220", anchor.guideId);
        assertEquals("ANCHOR", anchor.roleLabel);
        assertEquals("74%", anchor.matchLabel);
        assertEquals("GD-220 \u00B7 ANCHOR\nAbrasives Manufacturing", anchor.stationLabel());
    }

    @Test
    public void rainShelterTopicUsesEvidenceTitleOnlyForSpecificShelterSignals() {
        DetailSourceStackPolicy.ReviewedSourceStackEntry primitive =
            DetailSourceStackPolicy.reviewedSourceStackEntry(
                true,
                source("GD-345", "Primitive Shelter", "", "primitive shelter field notes")
            );
        DetailSourceStackPolicy.ReviewedSourceStackEntry tarp =
            DetailSourceStackPolicy.reviewedSourceStackEntry(
                true,
                source("GD-345", "Tarp setup", "", "tarp cord ridgeline shelter")
            );

        assertTrue(primitive.isPresent());
        assertEquals("", primitive.evidenceTitle);
        assertEquals("Tarp & Cord Shelters", tarp.evidenceTitle);
        assertEquals("TOPIC", tarp.roleLabel);
        assertEquals(2, tarp.rank);
    }

    private static SearchResult source(String guideId, String title, String sectionHeading, String body) {
        return new SearchResult(
            title,
            "",
            body,
            body,
            guideId,
            sectionHeading,
            "test",
            "hybrid",
            "",
            "",
            "",
            ""
        );
    }
}
