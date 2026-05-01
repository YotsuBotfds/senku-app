package com.senku.mobile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class SearchScoreMarkerPolicyTest {
    @Test
    public void markerUsesFixedTrackWidthAndScoreSensitiveFill() {
        SearchScoreMarkerPolicy.Marker first = SearchScoreMarkerPolicy.markerForPosition(0);
        SearchScoreMarkerPolicy.Marker second = SearchScoreMarkerPolicy.markerForPosition(1);
        SearchScoreMarkerPolicy.Marker fifth = SearchScoreMarkerPolicy.markerForPosition(4);

        assertEquals(22, first.trackWidthDp);
        assertEquals(first.trackWidthDp, second.trackWidthDp);
        assertEquals(first.trackWidthDp, fifth.trackWidthDp);
        assertEquals(0.94f, first.fillFraction, 0.001f);
        assertEquals(0.82f, second.fillFraction, 0.001f);
        assertEquals(0.52f, fifth.fillFraction, 0.001f);
    }

    @Test
    public void markerOwnsThresholdToneLabelAndContentDescription() {
        SearchScoreMarkerPolicy.Marker first = SearchScoreMarkerPolicy.markerForPosition(0);
        SearchScoreMarkerPolicy.Marker third = SearchScoreMarkerPolicy.markerForPosition(2);
        SearchScoreMarkerPolicy.Marker fifth = SearchScoreMarkerPolicy.markerForPosition(4);

        assertEquals(92, first.score);
        assertEquals("92", first.label);
        assertTrue(first.highEmphasisTone);
        assertTrue(third.highEmphasisTone);
        assertFalse(fifth.highEmphasisTone);
        assertEquals("Rank #1, score marker 92", first.contentDescription);
        assertEquals("Rank #5, score marker 55", fifth.contentDescription);
    }
}
