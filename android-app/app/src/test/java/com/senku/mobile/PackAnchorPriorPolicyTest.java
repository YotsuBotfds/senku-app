package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PackAnchorPriorPolicyTest {
    @Test
    public void directAnchorReceivesFullCurrentTurnBonus() {
        PackAnchorPriorPolicy.AnchorPriorAdjustment adjustment = PackAnchorPriorPolicy.adjustment(
            "GD-444",
            0,
            "GD-444",
            Map.of()
        );

        assertTrue(adjustment.hasBonus());
        assertEquals(1.0, adjustment.decay, 0.0001);
        assertEquals(1.0, adjustment.weight, 0.0001);
        assertEquals(0.08, adjustment.bonus, 0.0001);
    }

    @Test
    public void reciprocalRelatedAnchorUsesProvidedWeightAndDecay() {
        PackAnchorPriorPolicy.AnchorPriorAdjustment adjustment = PackAnchorPriorPolicy.adjustment(
            "GD-444",
            1,
            "GD-555",
            Map.of("GD-555", 0.5)
        );

        assertTrue(adjustment.hasBonus());
        assertEquals(0.6, adjustment.decay, 0.0001);
        assertEquals(0.5, adjustment.weight, 0.0001);
        assertEquals(0.024, adjustment.bonus, 0.0001);
    }

    @Test
    public void expiredAnchorDoesNotProduceBonus() {
        PackAnchorPriorPolicy.AnchorPriorAdjustment adjustment = PackAnchorPriorPolicy.adjustment(
            "GD-444",
            3,
            "GD-444",
            Map.of()
        );

        assertFalse(adjustment.hasBonus());
        assertEquals(0.0, adjustment.decay, 0.0001);
        assertEquals(0.0, adjustment.bonus, 0.0001);
    }

    @Test
    public void unrelatedGuideDoesNotProduceBonus() {
        PackAnchorPriorPolicy.AnchorPriorAdjustment adjustment = PackAnchorPriorPolicy.adjustment(
            "GD-444",
            0,
            "GD-777",
            Map.of("GD-555", 0.5)
        );

        assertFalse(adjustment.hasBonus());
        assertEquals(1.0, adjustment.decay, 0.0001);
        assertEquals(0.0, adjustment.weight, 0.0001);
        assertEquals(0.0, adjustment.bonus, 0.0001);
    }

    @Test
    public void relatedLinkWeightsMarksReciprocalLinksStrongerThanOneWayLinks() {
        LinkedHashMap<String, Set<String>> directedLinks = new LinkedHashMap<>();
        directedLinks.put("GD-100", new LinkedHashSet<>(List.of("GD-200", "GD-300")));
        directedLinks.put("GD-200", new LinkedHashSet<>(List.of("GD-100")));
        directedLinks.put("GD-300", new LinkedHashSet<>());

        Map<String, Map<String, Double>> weights = PackAnchorPriorPolicy.relatedLinkWeights(directedLinks);

        assertEquals(0.5, weights.get("GD-100").get("GD-200"), 0.0001);
        assertEquals(0.3, weights.get("GD-100").get("GD-300"), 0.0001);
        assertEquals(0.5, weights.get("GD-200").get("GD-100"), 0.0001);
        assertTrue(weights.get("GD-300").isEmpty());
    }

    @Test
    public void relatedLinkWeightsPreservesDirectedInsertionOrder() {
        LinkedHashMap<String, Set<String>> directedLinks = new LinkedHashMap<>();
        directedLinks.put("GD-100", new LinkedHashSet<>(List.of("GD-300", "GD-200")));
        directedLinks.put("GD-200", new LinkedHashSet<>());
        directedLinks.put("GD-300", new LinkedHashSet<>());

        Map<String, Map<String, Double>> weights = PackAnchorPriorPolicy.relatedLinkWeights(directedLinks);

        assertEquals(List.of("GD-100", "GD-200", "GD-300"), new ArrayList<>(weights.keySet()));
        assertEquals(List.of("GD-300", "GD-200"), new ArrayList<>(weights.get("GD-100").keySet()));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void relatedLinkWeightsReturnsImmutableOuterMap() {
        LinkedHashMap<String, Set<String>> directedLinks = new LinkedHashMap<>();
        directedLinks.put("GD-100", new LinkedHashSet<>());

        PackAnchorPriorPolicy.relatedLinkWeights(directedLinks).put("GD-200", Map.of());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void relatedLinkWeightsReturnsImmutableInnerMaps() {
        LinkedHashMap<String, Set<String>> directedLinks = new LinkedHashMap<>();
        directedLinks.put("GD-100", new LinkedHashSet<>(List.of("GD-200")));
        directedLinks.put("GD-200", new LinkedHashSet<>());

        PackAnchorPriorPolicy.relatedLinkWeights(directedLinks).get("GD-100").put("GD-300", 0.3);
    }
}
