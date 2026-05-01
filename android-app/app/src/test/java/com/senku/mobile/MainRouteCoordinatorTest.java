package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.senku.ui.primitives.BottomTabDestination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class MainRouteCoordinatorTest {
    @Test
    public void applyRouteStateOwnsRouteFieldsAndNotifiesHostViewBoundary() {
        RecordingHost host = new RecordingHost();
        MainRouteCoordinator coordinator = new MainRouteCoordinator(host);

        coordinator.applyRouteState(new MainRouteDecisionHelper.RouteState(
            MainRouteDecisionHelper.Surface.ASK_RESULTS,
            BottomTabDestination.ASK,
            true
        ));

        assertRoute(
            coordinator.currentRouteState(),
            MainRouteDecisionHelper.Surface.ASK_RESULTS,
            BottomTabDestination.ASK,
            true
        );
        assertEquals(BottomTabDestination.ASK, coordinator.activePhoneTab());
        assertTrue(coordinator.askLaneActive());
        assertFalse(coordinator.isBrowseModeActive());
        assertEquals(
            Arrays.asList("route:ASK_RESULTS:ASK:true:browse=false"),
            host.calls
        );
    }

    @Test
    public void phoneTabTransitionPushesHistoryAndAppliesSameSectionEffects() {
        RecordingHost host = new RecordingHost();
        MainRouteCoordinator coordinator = new MainRouteCoordinator(host);

        coordinator.openPhoneTab(BottomTabDestination.ASK, true);
        coordinator.openPhoneTab(BottomTabDestination.PINS, true);

        assertRoute(
            coordinator.currentRouteState(),
            MainRouteDecisionHelper.Surface.SAVED_GUIDES,
            BottomTabDestination.PINS,
            false
        );
        assertEquals(
            Arrays.asList(
                "route:RECENT_THREADS:ASK:true:browse=true",
                "updateActionLabels",
                "scrollBrowseToTop",
                "focusSharedInput",
                "route:SAVED_GUIDES:PINS:false:browse=true",
                "updateActionLabels",
                "dismissSearchKeyboard",
                "prepareSavedGuidesDestination"
            ),
            host.calls
        );
    }

    @Test
    public void systemBackUsesInternalTabHistoryBeforeBrowseHomeFallback() {
        RecordingHost host = new RecordingHost();
        MainRouteCoordinator coordinator = new MainRouteCoordinator(host);
        coordinator.openPhoneTab(BottomTabDestination.ASK, true);
        coordinator.openPhoneTab(BottomTabDestination.PINS, true);
        host.calls.clear();

        assertTrue(coordinator.applySystemBackTransition());

        assertRoute(
            coordinator.currentRouteState(),
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
            BottomTabDestination.ASK,
            false
        );
        assertEquals(
            Arrays.asList(
                "route:RECENT_THREADS:ASK:false:browse=true",
                "updateActionLabels",
                "dismissSearchKeyboard",
                "ensureBrowseHomeVisible",
                "scrollRecentThreadsIntoView"
            ),
            host.calls
        );
    }

    @Test
    public void systemBackFromResultsReturnsThroughHostBrowseEffect() {
        RecordingHost host = new RecordingHost();
        MainRouteCoordinator coordinator = new MainRouteCoordinator(host);
        coordinator.enterSearchResultsRoute();
        host.calls.clear();

        assertTrue(coordinator.applySystemBackTransition());

        assertRoute(
            coordinator.currentRouteState(),
            MainRouteDecisionHelper.Surface.BROWSE,
            BottomTabDestination.HOME,
            false
        );
        assertEquals(
            Arrays.asList(
                "route:BROWSE:HOME:false:browse=true",
                "returnToBrowse"
            ),
            host.calls
        );
    }

    @Test
    public void homeChromeBackFromResultsReturnsThroughSameBrowseEffect() {
        RecordingHost host = new RecordingHost();
        MainRouteCoordinator coordinator = new MainRouteCoordinator(host);
        coordinator.enterAskResultsRoute();
        host.calls.clear();

        assertTrue(coordinator.applyHomeChromeBackTransition());

        assertRoute(
            coordinator.currentRouteState(),
            MainRouteDecisionHelper.Surface.BROWSE,
            BottomTabDestination.HOME,
            false
        );
        assertEquals(
            Arrays.asList(
                "route:BROWSE:HOME:false:browse=true",
                "returnToBrowse"
            ),
            host.calls
        );
    }

    @Test
    public void resultRoutesSystemAndHomeChromeBackApplySameBrowseContract() {
        CoordinatorBackCase[] cases = new CoordinatorBackCase[] {
            new CoordinatorBackCase("search system", false, false),
            new CoordinatorBackCase("search chrome", false, true),
            new CoordinatorBackCase("ask system", true, false),
            new CoordinatorBackCase("ask chrome", true, true)
        };

        for (CoordinatorBackCase testCase : cases) {
            RecordingHost host = new RecordingHost();
            MainRouteCoordinator coordinator = new MainRouteCoordinator(host);
            if (testCase.askResults) {
                coordinator.enterAskResultsRoute();
            } else {
                coordinator.enterSearchResultsRoute();
            }
            host.calls.clear();

            boolean handled = testCase.homeChromeBack
                ? coordinator.applyHomeChromeBackTransition()
                : coordinator.applySystemBackTransition();

            assertTrue(testCase.label, handled);
            assertRoute(
                coordinator.currentRouteState(),
                MainRouteDecisionHelper.Surface.BROWSE,
                BottomTabDestination.HOME,
                false
            );
            assertEquals(
                testCase.label,
                Arrays.asList(
                    "route:BROWSE:HOME:false:browse=true",
                    "returnToBrowse"
                ),
                host.calls
            );
        }
    }

    @Test
    public void savedSystemBackUsesHistoryBeforeHomeFallback() {
        SavedCoordinatorBackCase[] cases = new SavedCoordinatorBackCase[] {
            new SavedCoordinatorBackCase(
                "saved after ask",
                new BottomTabDestination[] {BottomTabDestination.ASK, BottomTabDestination.PINS},
                MainRouteDecisionHelper.Surface.RECENT_THREADS,
                BottomTabDestination.ASK,
                Arrays.asList(
                    "route:RECENT_THREADS:ASK:false:browse=true",
                    "updateActionLabels",
                    "dismissSearchKeyboard",
                    "ensureBrowseHomeVisible",
                    "scrollRecentThreadsIntoView"
                )
            ),
            new SavedCoordinatorBackCase(
                "saved without history",
                new BottomTabDestination[] {BottomTabDestination.PINS},
                MainRouteDecisionHelper.Surface.BROWSE,
                BottomTabDestination.HOME,
                Arrays.asList(
                    "route:BROWSE:HOME:false:browse=true",
                    "updateActionLabels",
                    "dismissSearchKeyboard",
                    "ensureBrowseHomeVisible",
                    "scrollBrowseToTop"
                )
            )
        };

        for (SavedCoordinatorBackCase testCase : cases) {
            RecordingHost host = new RecordingHost();
            MainRouteCoordinator coordinator = new MainRouteCoordinator(host);
            for (BottomTabDestination destination : testCase.openTabs) {
                coordinator.openPhoneTab(destination, true);
            }
            host.calls.clear();

            assertTrue(testCase.label, coordinator.applySystemBackTransition());
            assertRoute(
                coordinator.currentRouteState(),
                testCase.expectedSurface,
                testCase.expectedPhoneTab,
                false
            );
            assertEquals(testCase.label, testCase.expectedCalls, host.calls);
        }
    }

    @Test
    public void previousTabHistoryReturnsToMostRecentDifferentOwner() {
        PreviousTabCase[] cases = new PreviousTabCase[] {
            new PreviousTabCase(
                "saved returns to ask",
                new BottomTabDestination[] {BottomTabDestination.ASK, BottomTabDestination.PINS},
                MainRouteDecisionHelper.Surface.RECENT_THREADS,
                BottomTabDestination.ASK
            ),
            new PreviousTabCase(
                "ask returns to saved",
                new BottomTabDestination[] {BottomTabDestination.PINS, BottomTabDestination.ASK},
                MainRouteDecisionHelper.Surface.SAVED_GUIDES,
                BottomTabDestination.PINS
            )
        };

        for (PreviousTabCase testCase : cases) {
            RecordingHost host = new RecordingHost();
            MainRouteCoordinator coordinator = new MainRouteCoordinator(host);
            for (BottomTabDestination destination : testCase.openTabs) {
                coordinator.openPhoneTab(destination, true);
            }
            host.calls.clear();

            assertTrue(testCase.label, coordinator.applySystemBackTransition());
            assertRoute(
                coordinator.currentRouteState(),
                testCase.expectedSurface,
                testCase.expectedPhoneTab,
                false
            );
            assertTrue(testCase.label, host.calls.contains("updateActionLabels"));
        }
    }

    @Test
    public void askUnavailableOrNoSourceFailureClearsAskResultsBeforeBack() {
        RecordingHost host = new RecordingHost();
        MainRouteCoordinator coordinator = new MainRouteCoordinator(host);
        coordinator.enterAskResultsRoute();
        host.calls.clear();

        coordinator.applyRouteState(MainRouteDecisionHelper.askUnavailableOrNoSourceFailure());

        assertRoute(
            coordinator.currentRouteState(),
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
            BottomTabDestination.ASK,
            false
        );
        assertTrue(coordinator.isBrowseModeActive());
        assertFalse(coordinator.askLaneActive());
        assertEquals(
            Arrays.asList("route:RECENT_THREADS:ASK:false:browse=true"),
            host.calls
        );

        host.calls.clear();
        assertFalse(coordinator.applyHomeChromeBackTransition());
        assertTrue(host.calls.isEmpty());

        assertTrue(coordinator.applySystemBackTransition());
        assertRoute(
            coordinator.currentRouteState(),
            MainRouteDecisionHelper.Surface.BROWSE,
            BottomTabDestination.HOME,
            false
        );
        assertCallsIncludeExactly(
            Arrays.asList(
                "route:BROWSE:HOME:false:browse=true",
                "updateActionLabels",
                "dismissSearchKeyboard",
                "ensureBrowseHomeVisible",
                "scrollBrowseToTop"
            ),
            host.calls
        );
    }

    @Test
    public void disabledNavigationHostLeavesTabTapUnhandled() {
        RecordingHost host = new RecordingHost();
        host.handleNavigation = false;
        MainRouteCoordinator coordinator = new MainRouteCoordinator(host);

        coordinator.openPhoneTab(BottomTabDestination.PINS, true);

        assertRoute(
            coordinator.currentRouteState(),
            MainRouteDecisionHelper.Surface.BROWSE,
            BottomTabDestination.HOME,
            false
        );
        assertTrue(host.calls.isEmpty());
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

    private static void assertCallsIncludeExactly(
        List<String> expectedCalls,
        List<String> actualCalls
    ) {
        assertEquals(expectedCalls.size(), actualCalls.size());
        assertTrue(actualCalls.containsAll(expectedCalls));
    }

    private static final class CoordinatorBackCase {
        final String label;
        final boolean askResults;
        final boolean homeChromeBack;

        CoordinatorBackCase(String label, boolean askResults, boolean homeChromeBack) {
            this.label = label;
            this.askResults = askResults;
            this.homeChromeBack = homeChromeBack;
        }
    }

    private static final class SavedCoordinatorBackCase {
        final String label;
        final BottomTabDestination[] openTabs;
        final MainRouteDecisionHelper.Surface expectedSurface;
        final BottomTabDestination expectedPhoneTab;
        final List<String> expectedCalls;

        SavedCoordinatorBackCase(
            String label,
            BottomTabDestination[] openTabs,
            MainRouteDecisionHelper.Surface expectedSurface,
            BottomTabDestination expectedPhoneTab,
            List<String> expectedCalls
        ) {
            this.label = label;
            this.openTabs = openTabs;
            this.expectedSurface = expectedSurface;
            this.expectedPhoneTab = expectedPhoneTab;
            this.expectedCalls = expectedCalls;
        }
    }

    private static final class PreviousTabCase {
        final String label;
        final BottomTabDestination[] openTabs;
        final MainRouteDecisionHelper.Surface expectedSurface;
        final BottomTabDestination expectedPhoneTab;

        PreviousTabCase(
            String label,
            BottomTabDestination[] openTabs,
            MainRouteDecisionHelper.Surface expectedSurface,
            BottomTabDestination expectedPhoneTab
        ) {
            this.label = label;
            this.openTabs = openTabs;
            this.expectedSurface = expectedSurface;
            this.expectedPhoneTab = expectedPhoneTab;
        }
    }

    private static final class RecordingHost implements MainRouteCoordinator.Host {
        final List<String> calls = new ArrayList<>();
        boolean handleNavigation = true;

        @Override
        public boolean shouldHandleMainSurfaceNavigation() {
            return handleNavigation;
        }

        @Override
        public void onRouteStateApplied(
            MainRouteDecisionHelper.RouteState routeState,
            boolean browseChromeVisible
        ) {
            calls.add("route:"
                + routeState.surface
                + ":"
                + routeState.activePhoneTab
                + ":"
                + routeState.askLaneActive
                + ":browse="
                + browseChromeVisible);
        }

        @Override
        public void returnToBrowse() {
            calls.add("returnToBrowse");
        }

        @Override
        public void updateActionLabels() {
            calls.add("updateActionLabels");
        }

        @Override
        public void dismissSearchKeyboard() {
            calls.add("dismissSearchKeyboard");
        }

        @Override
        public void ensureBrowseHomeVisible() {
            calls.add("ensureBrowseHomeVisible");
        }

        @Override
        public void scrollBrowseToTop() {
            calls.add("scrollBrowseToTop");
        }

        @Override
        public void focusSharedInput() {
            calls.add("focusSharedInput");
        }

        @Override
        public void scrollRecentThreadsIntoView() {
            calls.add("scrollRecentThreadsIntoView");
        }

        @Override
        public void prepareSavedGuidesDestination() {
            calls.add("prepareSavedGuidesDestination");
        }
    }
}
