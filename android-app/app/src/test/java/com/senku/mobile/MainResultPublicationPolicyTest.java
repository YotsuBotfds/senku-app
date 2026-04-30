package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class MainResultPublicationPolicyTest {
    @Test
    public void browseSurfaceClearsHighlightAndKeepsHomeChrome() {
        MainResultPublicationPolicy policy = MainResultPublicationPolicy.browseSurface();

        assertEquals("", policy.highlightQuery());
        assertTrue(policy.browseChromeVisible());
        assertFalse(policy.updateSearchQueryChrome());
    }

    @Test
    public void resultSurfaceHidesBrowseChromeWithoutUpdatingQueryWells() {
        MainResultPublicationPolicy policy = MainResultPublicationPolicy.resultSurface("rain shelter");

        assertEquals("rain shelter", policy.highlightQuery());
        assertFalse(policy.browseChromeVisible());
        assertFalse(policy.updateSearchQueryChrome());
    }

    @Test
    public void searchChromeSurfaceCarriesQueryLabelAndClampedResultCount() {
        MainResultPublicationPolicy policy = MainResultPublicationPolicy.resultSurfaceWithSearchChrome(
            "rain",
            "Rain shelters",
            -3
        );

        assertEquals("rain", policy.highlightQuery());
        assertEquals("Rain shelters", policy.searchQueryLabel());
        assertEquals(0, policy.resultCount());
        assertTrue(policy.updateSearchQueryChrome());
        assertFalse(policy.browseChromeVisible());
    }

    @Test
    public void failedResultSurfaceCanReturnToBrowseChrome() {
        MainResultPublicationPolicy policy =
            MainResultPublicationPolicy.resultSurfaceWithBrowseFallback(null, true);

        assertEquals("", policy.highlightQuery());
        assertTrue(policy.browseChromeVisible());
        assertFalse(policy.updateSearchQueryChrome());
    }
}
