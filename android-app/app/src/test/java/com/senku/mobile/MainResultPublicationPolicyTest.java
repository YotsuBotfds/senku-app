package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.senku.ui.primitives.BottomTabDestination;

import org.junit.Test;

public final class MainResultPublicationPolicyTest {
    @Test
    public void browseSurfaceClearsHighlightAndKeepsHomeChrome() {
        MainResultPublicationPolicy policy = MainResultPublicationPolicy.browseSurface();

        assertEquals("", policy.highlightQuery());
        assertRouteState(
            policy.routeState(),
            MainRouteDecisionHelper.Surface.BROWSE,
            BottomTabDestination.HOME,
            false
        );
        assertTrue(policy.browseChromeVisible());
        assertFalse(policy.updateSearchQueryChrome());
    }

    @Test
    public void browseSurfaceCanCarryExplicitBrowseSubSurface() {
        MainResultPublicationPolicy policy = MainResultPublicationPolicy.browseSurface(
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.SAVED_GUIDES,
                BottomTabDestination.PINS,
                false
            )
        );

        assertEquals("", policy.highlightQuery());
        assertRouteState(
            policy.routeState(),
            MainRouteDecisionHelper.Surface.SAVED_GUIDES,
            BottomTabDestination.PINS,
            false
        );
        assertTrue(policy.browseChromeVisible());
        assertFalse(policy.updateSearchQueryChrome());
    }

    @Test
    public void askResultSurfaceCarriesExplicitAskRouteWithoutUpdatingQueryWells() {
        MainResultPublicationPolicy policy = MainResultPublicationPolicy.resultSurface("rain shelter");

        assertEquals("rain shelter", policy.highlightQuery());
        assertRouteState(
            policy.routeState(),
            MainRouteDecisionHelper.Surface.ASK_RESULTS,
            BottomTabDestination.ASK,
            true
        );
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
        assertRouteState(
            policy.routeState(),
            MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
            BottomTabDestination.HOME,
            false
        );
        assertTrue(policy.updateSearchQueryChrome());
        assertFalse(policy.browseChromeVisible());
    }

    @Test
    public void failedSearchResultSurfaceCanReturnToBrowseChrome() {
        MainResultPublicationPolicy policy =
            MainResultPublicationPolicy.searchResultSurfaceWithBrowseFallback(null, true);

        assertEquals("", policy.highlightQuery());
        assertRouteState(
            policy.routeState(),
            MainRouteDecisionHelper.Surface.BROWSE,
            BottomTabDestination.HOME,
            false
        );
        assertTrue(policy.browseChromeVisible());
        assertFalse(policy.updateSearchQueryChrome());
    }

    @Test
    public void failedAskResultSurfaceCanReturnToRecentThreadsChrome() {
        MainResultPublicationPolicy policy =
            MainResultPublicationPolicy.askResultSurfaceWithBrowseFallback("cleaner", true);

        assertEquals("cleaner", policy.highlightQuery());
        assertRouteState(
            policy.routeState(),
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
            BottomTabDestination.ASK,
            false
        );
        assertTrue(policy.browseChromeVisible());
        assertFalse(policy.updateSearchQueryChrome());
    }

    private static void assertRouteState(
        MainRouteDecisionHelper.RouteState routeState,
        MainRouteDecisionHelper.Surface surface,
        BottomTabDestination activePhoneTab,
        boolean askLaneActive
    ) {
        assertEquals(surface, routeState.surface);
        assertEquals(activePhoneTab, routeState.activePhoneTab);
        assertEquals(askLaneActive, routeState.askLaneActive);
    }
}
