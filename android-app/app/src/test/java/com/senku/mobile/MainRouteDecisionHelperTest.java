package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.senku.ui.primitives.BottomTabDestination;

import org.junit.Test;

public final class MainRouteDecisionHelperTest {
    @Test
    public void browseHomeBackExitsActivity() {
        MainRouteDecisionHelper.Transition transition =
            MainRouteDecisionHelper.systemBack(MainRouteDecisionHelper.browseHome(), null);

        assertEquals(MainRouteDecisionHelper.Effect.EXIT_ACTIVITY, transition.effect);
        assertRoute(
            transition.routeState,
            MainRouteDecisionHelper.Surface.BROWSE,
            BottomTabDestination.HOME,
            false
        );
    }

    @Test
    public void browseTabBackReturnsToPreviousTabOwner() {
        MainRouteDecisionHelper.RouteState saved =
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.BROWSE,
                BottomTabDestination.PINS,
                false
            );

        MainRouteDecisionHelper.Transition transition =
            MainRouteDecisionHelper.systemBack(saved, BottomTabDestination.ASK);

        assertEquals(MainRouteDecisionHelper.Effect.SHOW_PREVIOUS_TAB, transition.effect);
        assertRoute(
            transition.routeState,
            MainRouteDecisionHelper.Surface.BROWSE,
            BottomTabDestination.ASK,
            false
        );
    }

    @Test
    public void browseTabBackFallsHomeWhenPreviousTabIsMissingOrCurrent() {
        MainRouteDecisionHelper.RouteState ask =
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.BROWSE,
                BottomTabDestination.ASK,
                true
            );

        MainRouteDecisionHelper.Transition missingPrevious =
            MainRouteDecisionHelper.systemBack(ask, null);
        MainRouteDecisionHelper.Transition samePrevious =
            MainRouteDecisionHelper.systemBack(ask, BottomTabDestination.ASK);

        assertRoute(
            missingPrevious.routeState,
            MainRouteDecisionHelper.Surface.BROWSE,
            BottomTabDestination.HOME,
            false
        );
        assertRoute(
            samePrevious.routeState,
            MainRouteDecisionHelper.Surface.BROWSE,
            BottomTabDestination.HOME,
            false
        );
    }

    @Test
    public void systemBackFromSearchOrAskResultsReturnsHomeBrowse() {
        MainRouteDecisionHelper.Transition search =
            MainRouteDecisionHelper.systemBack(
                new MainRouteDecisionHelper.RouteState(
                    MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
                    BottomTabDestination.HOME,
                    false
                ),
                BottomTabDestination.PINS
            );
        MainRouteDecisionHelper.Transition ask =
            MainRouteDecisionHelper.systemBack(
                new MainRouteDecisionHelper.RouteState(
                    MainRouteDecisionHelper.Surface.ASK_RESULTS,
                    BottomTabDestination.ASK,
                    true
                ),
                BottomTabDestination.PINS
            );

        assertEquals(MainRouteDecisionHelper.Effect.RETURN_TO_BROWSE, search.effect);
        assertRoute(search.routeState, MainRouteDecisionHelper.Surface.BROWSE, BottomTabDestination.HOME, false);
        assertEquals(MainRouteDecisionHelper.Effect.RETURN_TO_BROWSE, ask.effect);
        assertRoute(ask.routeState, MainRouteDecisionHelper.Surface.BROWSE, BottomTabDestination.HOME, false);
    }

    @Test
    public void systemBackFromSavedBrowseReturnsPreviousOwnerBeforeHomeFallback() {
        MainRouteDecisionHelper.RouteState saved =
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.BROWSE,
                BottomTabDestination.PINS,
                false
            );

        MainRouteDecisionHelper.Transition previousAsk =
            MainRouteDecisionHelper.systemBack(saved, BottomTabDestination.ASK);
        MainRouteDecisionHelper.Transition missingPrevious =
            MainRouteDecisionHelper.systemBack(saved, null);

        assertEquals(MainRouteDecisionHelper.Effect.SHOW_PREVIOUS_TAB, previousAsk.effect);
        assertRoute(previousAsk.routeState, MainRouteDecisionHelper.Surface.BROWSE, BottomTabDestination.ASK, false);
        assertEquals(MainRouteDecisionHelper.Effect.SHOW_PREVIOUS_TAB, missingPrevious.effect);
        assertRoute(missingPrevious.routeState, MainRouteDecisionHelper.Surface.BROWSE, BottomTabDestination.HOME, false);
    }

    @Test
    public void homeChromeBackOnlyHandlesNonBrowseRoutes() {
        MainRouteDecisionHelper.Transition browse =
            MainRouteDecisionHelper.homeChromeBack(MainRouteDecisionHelper.browseHome());
        MainRouteDecisionHelper.Transition search =
            MainRouteDecisionHelper.homeChromeBack(
                new MainRouteDecisionHelper.RouteState(
                    MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
                    BottomTabDestination.HOME,
                    false
                )
            );

        assertEquals(MainRouteDecisionHelper.Effect.NONE, browse.effect);
        assertRoute(browse.routeState, MainRouteDecisionHelper.Surface.BROWSE, BottomTabDestination.HOME, false);
        assertEquals(MainRouteDecisionHelper.Effect.RETURN_TO_BROWSE, search.effect);
        assertRoute(search.routeState, MainRouteDecisionHelper.Surface.BROWSE, BottomTabDestination.HOME, false);
    }

    @Test
    public void homeChromeBackIsOnlyVisibleForNonBrowseRoutes() {
        assertFalse(MainRouteDecisionHelper.shouldShowHomeChromeBack(MainRouteDecisionHelper.browseHome()));
        assertFalse(MainRouteDecisionHelper.shouldShowHomeChromeBack(
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.BROWSE,
                BottomTabDestination.PINS,
                false
            )
        ));
        assertTrue(
            MainRouteDecisionHelper.shouldShowHomeChromeBack(
                new MainRouteDecisionHelper.RouteState(
                    MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
                    BottomTabDestination.HOME,
                    false
                )
            )
        );
    }

    @Test
    public void openingPhoneTabsUsesExistingDestinationOwnership() {
        MainRouteDecisionHelper.RouteState searchResults =
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
                BottomTabDestination.HOME,
                false
            );

        assertTransition(
            MainRouteDecisionHelper.openPhoneTab(searchResults, BottomTabDestination.SEARCH),
            MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
            BottomTabDestination.HOME,
            false,
            MainRouteDecisionHelper.Effect.FOCUS_SEARCH_INPUT
        );
        assertTransition(
            MainRouteDecisionHelper.openPhoneTab(searchResults, BottomTabDestination.ASK),
            MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
            BottomTabDestination.ASK,
            true,
            MainRouteDecisionHelper.Effect.FOCUS_ASK_INPUT
        );
        assertTransition(
            MainRouteDecisionHelper.openPhoneTab(searchResults, BottomTabDestination.THREADS),
            MainRouteDecisionHelper.Surface.BROWSE,
            BottomTabDestination.ASK,
            false,
            MainRouteDecisionHelper.Effect.SHOW_RECENT_THREADS
        );
        assertTransition(
            MainRouteDecisionHelper.openPhoneTab(searchResults, BottomTabDestination.PINS),
            MainRouteDecisionHelper.Surface.BROWSE,
            BottomTabDestination.PINS,
            false,
            MainRouteDecisionHelper.Effect.SHOW_SAVED_GUIDES
        );
    }

    @Test
    public void enteringSearchAndAskCreatesExplicitResultRoutes() {
        assertTransition(
            MainRouteDecisionHelper.enterSearch(MainRouteDecisionHelper.browseHome()),
            MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
            BottomTabDestination.HOME,
            false,
            MainRouteDecisionHelper.Effect.FOCUS_SEARCH_INPUT
        );
        assertTransition(
            MainRouteDecisionHelper.enterAsk(MainRouteDecisionHelper.browseHome()),
            MainRouteDecisionHelper.Surface.ASK_RESULTS,
            BottomTabDestination.ASK,
            true,
            MainRouteDecisionHelper.Effect.FOCUS_ASK_INPUT
        );
    }

    @Test
    public void returningHomeClearsAskLaneAndSearchResults() {
        MainRouteDecisionHelper.RouteState askResults =
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.ASK_RESULTS,
                BottomTabDestination.ASK,
                true
            );

        assertTransition(
            MainRouteDecisionHelper.returnHome(askResults),
            MainRouteDecisionHelper.Surface.BROWSE,
            BottomTabDestination.HOME,
            false,
            MainRouteDecisionHelper.Effect.SHOW_BROWSE_HOME
        );
    }

    @Test
    public void routeStateNormalizesVirtualDestinationsToVisibleOwners() {
        MainRouteDecisionHelper.RouteState search =
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.BROWSE,
                BottomTabDestination.SEARCH,
                false
            );
        MainRouteDecisionHelper.RouteState threads =
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.BROWSE,
                BottomTabDestination.THREADS,
                false
            );

        assertEquals(BottomTabDestination.HOME, search.activePhoneTab);
        assertFalse(search.askLaneActive);
        assertEquals(BottomTabDestination.ASK, threads.activePhoneTab);
        assertFalse(threads.askLaneActive);
    }

    @Test
    public void routeStateForModeResolvesBrowseSearchAndAskSurfaces() {
        assertRoute(
            MainRouteDecisionHelper.routeStateForMode(true, BottomTabDestination.ASK, true),
            MainRouteDecisionHelper.Surface.BROWSE,
            BottomTabDestination.ASK,
            true
        );
        assertRoute(
            MainRouteDecisionHelper.routeStateForMode(false, BottomTabDestination.HOME, false),
            MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
            BottomTabDestination.HOME,
            false
        );
        assertRoute(
            MainRouteDecisionHelper.routeStateForMode(false, BottomTabDestination.HOME, true),
            MainRouteDecisionHelper.Surface.ASK_RESULTS,
            BottomTabDestination.HOME,
            true
        );
        assertRoute(
            MainRouteDecisionHelper.routeStateForMode(false, BottomTabDestination.ASK, false),
            MainRouteDecisionHelper.Surface.ASK_RESULTS,
            BottomTabDestination.ASK,
            false
        );
    }

    private static void assertTransition(
        MainRouteDecisionHelper.Transition transition,
        MainRouteDecisionHelper.Surface surface,
        BottomTabDestination activePhoneTab,
        boolean askLaneActive,
        MainRouteDecisionHelper.Effect effect
    ) {
        assertEquals(effect, transition.effect);
        assertRoute(transition.routeState, surface, activePhoneTab, askLaneActive);
    }

    private static void assertRoute(
        MainRouteDecisionHelper.RouteState state,
        MainRouteDecisionHelper.Surface surface,
        BottomTabDestination activePhoneTab,
        boolean askLaneActive
    ) {
        assertEquals(surface, state.surface);
        assertEquals(activePhoneTab, state.activePhoneTab);
        assertEquals(askLaneActive, state.askLaneActive);
    }
}
