package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public final class PackTextMatchPolicyTest {
    @Test
    public void normalizeLowercasesAndCollapsesNonAlphanumericRuns() {
        assertEquals(
            "wet weather fire starting kit 101",
            PackTextMatchPolicy.normalize(" Wet-weather FIRE_starting kit #101 ")
        );
    }

    @Test
    public void containsTermUsesWordBoundariesForSingleTokenTerms() {
        assertTrue(PackTextMatchPolicy.containsTerm("dry tinder and kindling", "tinder"));
        assertFalse(PackTextMatchPolicy.containsTerm("kindling tinderbox", "tinder"));
    }

    @Test
    public void containsTermAllowsNormalizedPhraseSubstrings() {
        assertTrue(PackTextMatchPolicy.containsTerm("Build a wet-weather fire lay", "wet weather fire"));
        assertFalse(PackTextMatchPolicy.containsTerm("Build a wet fire after weather turns", "wet weather fire"));
    }

    @Test
    public void containsAnyMarkerNormalizesMarkersAndRequiresBoundedMatch() {
        Set<String> markers = Set.of("site assessment", "wind-exposure");

        assertTrue(PackTextMatchPolicy.containsAnyMarker("Cabin wind exposure checklist", markers));
        assertFalse(PackTextMatchPolicy.containsAnyMarker("Cabin exposure checklist", markers));
        assertFalse(PackTextMatchPolicy.containsAnyMarker("Cabin windproofing exposure", markers));
    }

    @Test
    public void containsAnyMarkerReturnsFalseForMissingInputs() {
        assertFalse(PackTextMatchPolicy.containsAnyMarker(null, Set.of("fire")));
        assertFalse(PackTextMatchPolicy.containsAnyMarker("fire", null));
        assertFalse(PackTextMatchPolicy.containsAnyMarker("fire", Set.of()));
    }
}
