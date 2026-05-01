package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
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
        assertRoute(
            previousAsk.routeState,
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
            BottomTabDestination.ASK,
            false
        );
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
    public void homeChromeBackMatchesSystemBackForVisibleResultRoutes() {
        MainRouteDecisionHelper.RouteState search =
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
                BottomTabDestination.HOME,
                false
            );
        MainRouteDecisionHelper.RouteState ask =
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.ASK_RESULTS,
                BottomTabDestination.ASK,
                true
            );

        assertSameRouteTransition(
            MainRouteDecisionHelper.systemBack(search, BottomTabDestination.PINS),
            MainRouteDecisionHelper.homeChromeBack(search)
        );
        assertSameRouteTransition(
            MainRouteDecisionHelper.systemBack(ask, BottomTabDestination.PINS),
            MainRouteDecisionHelper.homeChromeBack(ask)
        );
    }

    @Test
    public void homeChromeBackIsOnlyVisibleForNonBrowseRoutes() {
        assertFalse(MainRouteDecisionHelper.shouldShowHomeChromeBack(MainRouteDecisionHelper.browseHome()));
        assertFalse(MainRouteDecisionHelper.shouldShowHomeChromeBack(
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.SAVED_GUIDES,
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
    public void browseSurfaceClassifierCoversBrowseRecentAndSavedOnly() {
        assertTrue(MainRouteDecisionHelper.isBrowseSurface(MainRouteDecisionHelper.Surface.BROWSE));
        assertTrue(MainRouteDecisionHelper.isBrowseSurface(MainRouteDecisionHelper.Surface.RECENT_THREADS));
        assertTrue(MainRouteDecisionHelper.isBrowseSurface(MainRouteDecisionHelper.Surface.SAVED_GUIDES));

        assertFalse(MainRouteDecisionHelper.isBrowseSurface(MainRouteDecisionHelper.Surface.SEARCH_RESULTS));
        assertFalse(MainRouteDecisionHelper.isBrowseSurface(MainRouteDecisionHelper.Surface.ASK_RESULTS));
    }

    @Test
    public void phoneTabSelectionOwnerMapsVirtualDestinationsToVisibleOwners() {
        assertEquals(BottomTabDestination.HOME, MainRouteDecisionHelper.phoneTabSelectionOwner(null));
        assertEquals(BottomTabDestination.HOME, MainRouteDecisionHelper.phoneTabSelectionOwner(BottomTabDestination.HOME));
        assertEquals(BottomTabDestination.HOME, MainRouteDecisionHelper.phoneTabSelectionOwner(BottomTabDestination.SEARCH));
        assertEquals(BottomTabDestination.ASK, MainRouteDecisionHelper.phoneTabSelectionOwner(BottomTabDestination.ASK));
        assertEquals(BottomTabDestination.ASK, MainRouteDecisionHelper.phoneTabSelectionOwner(BottomTabDestination.THREADS));
        assertEquals(BottomTabDestination.PINS, MainRouteDecisionHelper.phoneTabSelectionOwner(BottomTabDestination.PINS));
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
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
            BottomTabDestination.ASK,
            false,
            MainRouteDecisionHelper.Effect.SHOW_RECENT_THREADS
        );
        assertTransition(
            MainRouteDecisionHelper.openPhoneTab(searchResults, BottomTabDestination.PINS),
            MainRouteDecisionHelper.Surface.SAVED_GUIDES,
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
    public void askUnavailableOrNoSourceFailureReturnsToAskBrowseOwnerWithoutAskLane() {
        MainRouteDecisionHelper.RouteState routeState =
            MainRouteDecisionHelper.askUnavailableOrNoSourceFailure();

        assertRoute(
            routeState,
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
            BottomTabDestination.ASK,
            false
        );
        assertTrue(MainRouteDecisionHelper.isBrowseSurface(routeState.surface));
    }

    @Test
    public void askUnavailableOrNoSourceFailureMatchesLegacyNormalizedRoute() {
        MainRouteDecisionHelper.RouteState legacyRoute =
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.BROWSE,
                BottomTabDestination.ASK,
                false
            );

        assertSameRoute(
            legacyRoute,
            MainRouteDecisionHelper.askUnavailableOrNoSourceFailure()
        );
    }

    @Test
    public void askUnavailableOrNoSourceFailureBackReturnsHomeBeforeExit() {
        MainRouteDecisionHelper.RouteState failureRoute =
            MainRouteDecisionHelper.askUnavailableOrNoSourceFailure();

        MainRouteDecisionHelper.Transition firstBack =
            MainRouteDecisionHelper.systemBack(failureRoute, null);
        MainRouteDecisionHelper.Transition secondBack =
            MainRouteDecisionHelper.systemBack(firstBack.routeState, null);

        assertEquals(MainRouteDecisionHelper.Effect.SHOW_PREVIOUS_TAB, firstBack.effect);
        assertRoute(firstBack.routeState, MainRouteDecisionHelper.Surface.BROWSE, BottomTabDestination.HOME, false);
        assertEquals(MainRouteDecisionHelper.Effect.EXIT_ACTIVITY, secondBack.effect);
        assertRoute(secondBack.routeState, MainRouteDecisionHelper.Surface.BROWSE, BottomTabDestination.HOME, false);
    }

    @Test
    public void askUnavailableOrNoSourceFailureDoesNotShowHomeChromeBack() {
        assertFalse(MainRouteDecisionHelper.shouldShowHomeChromeBack(
            MainRouteDecisionHelper.askUnavailableOrNoSourceFailure()
        ));
    }

    @Test
    public void openEmptyAskLaneSelectsAskAndActivatesLaneWithinCurrentSurfaceMode() {
        assertRoute(
            MainRouteDecisionHelper.openEmptyAskLane(new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
                BottomTabDestination.HOME,
                false
            )),
            MainRouteDecisionHelper.Surface.ASK_RESULTS,
            BottomTabDestination.ASK,
            true
        );
        assertRoute(
            MainRouteDecisionHelper.openEmptyAskLane(new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.SAVED_GUIDES,
                BottomTabDestination.PINS,
                false
            )),
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
            BottomTabDestination.ASK,
            true
        );
    }

    @Test
    public void browseGuideFlowsClearAskLaneWithoutChangingCurrentTabOwner() {
        assertRoute(
            MainRouteDecisionHelper.browseGuides(new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.ASK_RESULTS,
                BottomTabDestination.ASK,
                true
            )),
            MainRouteDecisionHelper.Surface.ASK_RESULTS,
            BottomTabDestination.ASK,
            false
        );
        assertRoute(
            MainRouteDecisionHelper.filterGuidesByCategory(new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.SAVED_GUIDES,
                BottomTabDestination.PINS,
                true
            )),
            MainRouteDecisionHelper.Surface.SAVED_GUIDES,
            BottomTabDestination.PINS,
            false
        );
    }

    @Test
    public void clearingChatSessionReturnsToHomeBrowseRoute() {
        assertRoute(
            MainRouteDecisionHelper.clearChatSession(),
            MainRouteDecisionHelper.Surface.BROWSE,
            BottomTabDestination.HOME,
            false
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
    public void openHomeIntentTrueClearsStaleSearchAndAskRoutes() {
        assertTransition(
            MainRouteDecisionHelper.openHomeIntent(
                true,
                new MainRouteDecisionHelper.RouteState(
                    MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
                    BottomTabDestination.HOME,
                    false
                )
            ),
            MainRouteDecisionHelper.Surface.BROWSE,
            BottomTabDestination.HOME,
            false,
            MainRouteDecisionHelper.Effect.SHOW_BROWSE_HOME
        );
        assertTransition(
            MainRouteDecisionHelper.openHomeIntent(
                true,
                new MainRouteDecisionHelper.RouteState(
                    MainRouteDecisionHelper.Surface.ASK_RESULTS,
                    BottomTabDestination.ASK,
                    true
                )
            ),
            MainRouteDecisionHelper.Surface.BROWSE,
            BottomTabDestination.HOME,
            false,
            MainRouteDecisionHelper.Effect.SHOW_BROWSE_HOME
        );
    }

    @Test
    public void openHomeIntentFalseKeepsCurrentRouteState() {
        assertTransition(
            MainRouteDecisionHelper.openHomeIntent(
                false,
                new MainRouteDecisionHelper.RouteState(
                    MainRouteDecisionHelper.Surface.ASK_RESULTS,
                    BottomTabDestination.ASK,
                    true
                )
            ),
            MainRouteDecisionHelper.Surface.ASK_RESULTS,
            BottomTabDestination.ASK,
            true,
            MainRouteDecisionHelper.Effect.NONE
        );
    }

    @Test
    public void openSavedIntentTrueRoutesBrowseSearchAndAskToSavedPins() {
        assertTransition(
            MainRouteDecisionHelper.openSavedIntent(
                true,
                new MainRouteDecisionHelper.RouteState(
                    MainRouteDecisionHelper.Surface.BROWSE,
                    BottomTabDestination.HOME,
                    false
                )
            ),
            MainRouteDecisionHelper.Surface.SAVED_GUIDES,
            BottomTabDestination.PINS,
            false,
            MainRouteDecisionHelper.Effect.SHOW_SAVED_GUIDES
        );
        assertTransition(
            MainRouteDecisionHelper.openSavedIntent(
                true,
                new MainRouteDecisionHelper.RouteState(
                    MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
                    BottomTabDestination.HOME,
                    false
                )
            ),
            MainRouteDecisionHelper.Surface.SAVED_GUIDES,
            BottomTabDestination.PINS,
            false,
            MainRouteDecisionHelper.Effect.SHOW_SAVED_GUIDES
        );
        assertTransition(
            MainRouteDecisionHelper.openSavedIntent(
                true,
                new MainRouteDecisionHelper.RouteState(
                    MainRouteDecisionHelper.Surface.ASK_RESULTS,
                    BottomTabDestination.ASK,
                    true
                )
            ),
            MainRouteDecisionHelper.Surface.SAVED_GUIDES,
            BottomTabDestination.PINS,
            false,
            MainRouteDecisionHelper.Effect.SHOW_SAVED_GUIDES
        );
    }

    @Test
    public void openSavedIntentFalseKeepsCurrentRouteState() {
        assertTransition(
            MainRouteDecisionHelper.openSavedIntent(
                false,
                new MainRouteDecisionHelper.RouteState(
                    MainRouteDecisionHelper.Surface.ASK_RESULTS,
                    BottomTabDestination.ASK,
                    true
                )
            ),
            MainRouteDecisionHelper.Surface.ASK_RESULTS,
            BottomTabDestination.ASK,
            true,
            MainRouteDecisionHelper.Effect.NONE
        );
    }

    @Test
    public void installedPackPublishesBrowseGuidesOnlyForBrowseRoutesWithoutAutoQuery() {
        assertTrue(MainRouteDecisionHelper.shouldPublishInstalledBrowseGuides(
            false,
            MainRouteDecisionHelper.browseHome()
        ));
        assertTrue(MainRouteDecisionHelper.shouldPublishInstalledBrowseGuides(
            false,
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.SAVED_GUIDES,
                BottomTabDestination.PINS,
                false
            )
        ));

        assertFalse(MainRouteDecisionHelper.shouldPublishInstalledBrowseGuides(
            true,
            MainRouteDecisionHelper.browseHome()
        ));
        assertFalse(MainRouteDecisionHelper.shouldPublishInstalledBrowseGuides(
            false,
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
                BottomTabDestination.HOME,
                false
            )
        ));
        assertFalse(MainRouteDecisionHelper.shouldPublishInstalledBrowseGuides(
            false,
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.ASK_RESULTS,
                BottomTabDestination.ASK,
                true
            )
        ));
    }

    @Test
    public void installCompletionPolicyPublishesBrowseGuidesForBrowseRecentAndSavedRoutes() {
        assertInstallCompletionAction(
            false,
            MainRouteDecisionHelper.browseHome(),
            MainInstallCompletionPolicy.Action.PUBLISH_BROWSE_GUIDES
        );
        assertInstallCompletionAction(
            false,
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.RECENT_THREADS,
                BottomTabDestination.ASK,
                false
            ),
            MainInstallCompletionPolicy.Action.PUBLISH_BROWSE_GUIDES
        );
        assertInstallCompletionAction(
            false,
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.SAVED_GUIDES,
                BottomTabDestination.PINS,
                false
            ),
            MainInstallCompletionPolicy.Action.PUBLISH_BROWSE_GUIDES
        );
    }

    @Test
    public void installCompletionPolicyPreservesCurrentResultsForSearchAskAndAutoQueryRoutes() {
        assertInstallCompletionAction(
            false,
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
                BottomTabDestination.HOME,
                false
            ),
            MainInstallCompletionPolicy.Action.PRESERVE_CURRENT_RESULTS
        );
        assertInstallCompletionAction(
            false,
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.ASK_RESULTS,
                BottomTabDestination.ASK,
                true
            ),
            MainInstallCompletionPolicy.Action.PRESERVE_CURRENT_RESULTS
        );
        assertInstallCompletionAction(
            true,
            MainRouteDecisionHelper.browseHome(),
            MainInstallCompletionPolicy.Action.PRESERVE_CURRENT_RESULTS
        );
        assertInstallCompletionAction(
            true,
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.RECENT_THREADS,
                BottomTabDestination.ASK,
                false
            ),
            MainInstallCompletionPolicy.Action.PRESERVE_CURRENT_RESULTS
        );
        assertInstallCompletionAction(
            true,
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.SAVED_GUIDES,
                BottomTabDestination.PINS,
                false
            ),
            MainInstallCompletionPolicy.Action.PRESERVE_CURRENT_RESULTS
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
        MainRouteDecisionHelper.RouteState saved =
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.BROWSE,
                BottomTabDestination.PINS,
                false
            );

        assertEquals(MainRouteDecisionHelper.Surface.BROWSE, search.surface);
        assertEquals(BottomTabDestination.HOME, search.activePhoneTab);
        assertFalse(search.askLaneActive);
        assertEquals(MainRouteDecisionHelper.Surface.RECENT_THREADS, threads.surface);
        assertEquals(BottomTabDestination.ASK, threads.activePhoneTab);
        assertFalse(threads.askLaneActive);
        assertEquals(MainRouteDecisionHelper.Surface.SAVED_GUIDES, saved.surface);
        assertEquals(BottomTabDestination.PINS, saved.activePhoneTab);
        assertFalse(saved.askLaneActive);
    }

    @Test
    public void routeStateForModeResolvesBrowseSearchAndAskSurfaces() {
        assertRoute(
            MainRouteDecisionHelper.routeStateForMode(true, BottomTabDestination.ASK, true),
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
            BottomTabDestination.ASK,
            true
        );
        assertRoute(
            MainRouteDecisionHelper.routeStateForMode(true, BottomTabDestination.PINS, false),
            MainRouteDecisionHelper.Surface.SAVED_GUIDES,
            BottomTabDestination.PINS,
            false
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

    @Test
    public void explicitFlowDestinationRoutesBrowseSurfacesThroughVisibleOwners() {
        MainRouteDecisionHelper.RouteState current =
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.RECENT_THREADS,
                BottomTabDestination.ASK,
                true
            );

        assertRoute(
            MainRouteDecisionHelper.routeStateForExplicitFlowDestination(current, BottomTabDestination.HOME),
            MainRouteDecisionHelper.Surface.BROWSE,
            BottomTabDestination.HOME,
            false
        );
        assertRoute(
            MainRouteDecisionHelper.routeStateForExplicitFlowDestination(current, BottomTabDestination.SEARCH),
            MainRouteDecisionHelper.Surface.BROWSE,
            BottomTabDestination.HOME,
            false
        );
        assertRoute(
            MainRouteDecisionHelper.routeStateForExplicitFlowDestination(current, BottomTabDestination.ASK),
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
            BottomTabDestination.ASK,
            true
        );
        assertRoute(
            MainRouteDecisionHelper.routeStateForExplicitFlowDestination(current, BottomTabDestination.THREADS),
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
            BottomTabDestination.ASK,
            false
        );
        assertRoute(
            MainRouteDecisionHelper.routeStateForExplicitFlowDestination(current, BottomTabDestination.PINS),
            MainRouteDecisionHelper.Surface.SAVED_GUIDES,
            BottomTabDestination.PINS,
            false
        );
    }

    @Test
    public void explicitFlowDestinationPreservesResultSurfaceMode() {
        MainRouteDecisionHelper.RouteState current =
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.ASK_RESULTS,
                BottomTabDestination.ASK,
                true
            );

        assertRoute(
            MainRouteDecisionHelper.routeStateForExplicitFlowDestination(current, BottomTabDestination.HOME),
            MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
            BottomTabDestination.HOME,
            false
        );
        assertRoute(
            MainRouteDecisionHelper.routeStateForExplicitFlowDestination(current, BottomTabDestination.SEARCH),
            MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
            BottomTabDestination.HOME,
            false
        );
        assertRoute(
            MainRouteDecisionHelper.routeStateForExplicitFlowDestination(current, BottomTabDestination.ASK),
            MainRouteDecisionHelper.Surface.ASK_RESULTS,
            BottomTabDestination.ASK,
            true
        );
        assertRoute(
            MainRouteDecisionHelper.routeStateForExplicitFlowDestination(current, BottomTabDestination.THREADS),
            MainRouteDecisionHelper.Surface.ASK_RESULTS,
            BottomTabDestination.ASK,
            false
        );
        assertRoute(
            MainRouteDecisionHelper.routeStateForExplicitFlowDestination(current, BottomTabDestination.PINS),
            MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
            BottomTabDestination.PINS,
            false
        );
    }

    @Test
    public void explicitFlowDestinationPreservesCurrentAskLaneForMissingDestination() {
        assertRoute(
            MainRouteDecisionHelper.routeStateForExplicitFlowDestination(
                new MainRouteDecisionHelper.RouteState(
                    MainRouteDecisionHelper.Surface.RECENT_THREADS,
                    BottomTabDestination.ASK,
                    true
                ),
                null
            ),
            MainRouteDecisionHelper.Surface.BROWSE,
            BottomTabDestination.HOME,
            true
        );
        assertRoute(
            MainRouteDecisionHelper.routeStateForExplicitFlowDestination(
                new MainRouteDecisionHelper.RouteState(
                    MainRouteDecisionHelper.Surface.ASK_RESULTS,
                    BottomTabDestination.ASK,
                    true
                ),
                null
            ),
            MainRouteDecisionHelper.Surface.ASK_RESULTS,
            BottomTabDestination.HOME,
            true
        );
    }

    @Test
    public void routeStateCodecEncodesNormalizedValues() {
        MainRouteDecisionHelper.EncodedRouteState encoded =
            MainRouteDecisionHelper.encodeRouteState(new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.BROWSE,
                BottomTabDestination.SEARCH,
                true
            ));

        assertEquals(MainRouteDecisionHelper.Surface.BROWSE.name(), encoded.surface);
        assertEquals(BottomTabDestination.HOME.name(), encoded.activePhoneTab);
        assertTrue(encoded.askLaneActive);
    }

    @Test
    public void routeStateCodecParsesKnownSurfaceAndPhoneTabNamesOnly() {
        assertEquals(
            MainRouteDecisionHelper.Surface.SAVED_GUIDES,
            MainRouteDecisionHelper.parseRouteSurface(MainRouteDecisionHelper.Surface.SAVED_GUIDES.name())
        );
        assertEquals(
            BottomTabDestination.PINS,
            MainRouteDecisionHelper.parsePhoneTab(BottomTabDestination.PINS.name())
        );
        assertNull(MainRouteDecisionHelper.parseRouteSurface(null));
        assertNull(MainRouteDecisionHelper.parseRouteSurface("   "));
        assertNull(MainRouteDecisionHelper.parseRouteSurface("DETAIL"));
        assertNull(MainRouteDecisionHelper.parsePhoneTab(null));
        assertNull(MainRouteDecisionHelper.parsePhoneTab("   "));
        assertNull(MainRouteDecisionHelper.parsePhoneTab("DETAIL"));
    }

    @Test
    public void routeStateCodecRestoresFirstClassRouteState() {
        assertRoute(
            MainRouteDecisionHelper.resolveRestoredMainRouteState(
                MainRouteDecisionHelper.Surface.SEARCH_RESULTS.name(),
                BottomTabDestination.HOME.name(),
                false,
                true,
                BottomTabDestination.PINS.name()
            ),
            MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
            BottomTabDestination.HOME,
            false
        );
        assertRoute(
            MainRouteDecisionHelper.resolveRestoredMainRouteState(
                MainRouteDecisionHelper.Surface.RECENT_THREADS.name(),
                BottomTabDestination.THREADS.name(),
                false,
                true,
                BottomTabDestination.HOME.name()
            ),
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
            BottomTabDestination.ASK,
            false
        );
    }

    @Test
    public void routeStateCodecFallsBackToLegacyPhoneTabForMissingOrInvalidFirstClassState() {
        assertRoute(
            MainRouteDecisionHelper.resolveRestoredMainRouteState(
                null,
                null,
                false,
                false,
                BottomTabDestination.ASK.name()
            ),
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
            BottomTabDestination.ASK,
            true
        );
        assertRoute(
            MainRouteDecisionHelper.resolveRestoredMainRouteState(
                "DETAIL",
                BottomTabDestination.HOME.name(),
                false,
                true,
                BottomTabDestination.PINS.name()
            ),
            MainRouteDecisionHelper.Surface.SAVED_GUIDES,
            BottomTabDestination.PINS,
            false
        );
    }

    @Test
    public void routeStateCodecResolvesInitialBrowseChromeVisibility() {
        assertFalse(MainRouteDecisionHelper.resolveInitialBrowseChromeVisible(
            true,
            MainRouteDecisionHelper.Surface.SAVED_GUIDES.name(),
            BottomTabDestination.PINS.name(),
            false,
            true,
            BottomTabDestination.PINS.name()
        ));
        assertFalse(MainRouteDecisionHelper.resolveInitialBrowseChromeVisible(
            false,
            MainRouteDecisionHelper.Surface.ASK_RESULTS.name(),
            BottomTabDestination.ASK.name(),
            true,
            true,
            BottomTabDestination.HOME.name()
        ));
        assertTrue(MainRouteDecisionHelper.resolveInitialBrowseChromeVisible(
            false,
            MainRouteDecisionHelper.Surface.SAVED_GUIDES.name(),
            BottomTabDestination.PINS.name(),
            false,
            true,
            BottomTabDestination.HOME.name()
        ));
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

    private static void assertSameRouteTransition(
        MainRouteDecisionHelper.Transition expected,
        MainRouteDecisionHelper.Transition actual
    ) {
        assertEquals(expected.effect, actual.effect);
        assertRoute(
            actual.routeState,
            expected.routeState.surface,
            expected.routeState.activePhoneTab,
            expected.routeState.askLaneActive
        );
    }

    private static void assertSameRoute(
        MainRouteDecisionHelper.RouteState expected,
        MainRouteDecisionHelper.RouteState actual
    ) {
        assertRoute(
            actual,
            expected.surface,
            expected.activePhoneTab,
            expected.askLaneActive
        );
    }

    private static void assertInstallCompletionAction(
        boolean autoQueryPending,
        MainRouteDecisionHelper.RouteState routeState,
        MainInstallCompletionPolicy.Action expected
    ) {
        assertEquals(expected, MainInstallCompletionPolicy.resolve(autoQueryPending, routeState));
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
