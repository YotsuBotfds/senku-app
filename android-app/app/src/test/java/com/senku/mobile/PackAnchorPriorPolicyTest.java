package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Map;

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
}
