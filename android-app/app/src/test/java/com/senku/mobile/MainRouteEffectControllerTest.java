package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.senku.ui.primitives.BottomTabDestination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class MainRouteEffectControllerTest {
    @Test
    public void phoneTabTransitionPushesHistoryBeforeRouteAndSavedEffects() {
        MainRouteDecisionHelper.Transition transition =
            new MainRouteDecisionHelper.Transition(
                new MainRouteDecisionHelper.RouteState(
                    MainRouteDecisionHelper.Surface.SAVED_GUIDES,
                    BottomTabDestination.PINS,
                    false
                ),
                MainRouteDecisionHelper.Effect.SHOW_SAVED_GUIDES
            );
        RecordingBackEffects effects = new RecordingBackEffects(true, BottomTabDestination.ASK);

        MainRouteEffectController.applyPhoneTabTransition(transition, true, effects);

        assertEquals(
            Arrays.asList(
                "pushPhoneTab:ASK",
                "applyRouteState:SAVED_GUIDES:PINS:false",
                "updateActionLabels",
                "dismissSearchKeyboard",
                "prepareSavedGuidesDestination"
            ),
            effects.calls
        );
    }

    @Test
    public void phoneTabTransitionDoesNotPushHistoryWhenSelectionOwnerIsUnchanged() {
        MainRouteDecisionHelper.Transition transition =
            new MainRouteDecisionHelper.Transition(
                new MainRouteDecisionHelper.RouteState(
                    MainRouteDecisionHelper.Surface.RECENT_THREADS,
                    BottomTabDestination.ASK,
                    true
                ),
                MainRouteDecisionHelper.Effect.FOCUS_ASK_INPUT
            );
        RecordingBackEffects effects = new RecordingBackEffects(false, BottomTabDestination.ASK);

        MainRouteEffectController.applyPhoneTabTransition(transition, true, effects);

        assertEquals(
            Arrays.asList(
                "applyRouteState:RECENT_THREADS:ASK:true",
                "updateActionLabels",
                "focusSharedInput"
            ),
            effects.calls
        );
    }

    @Test
    public void explicitFlowDestinationAppliesRouteWithoutTransitionEffects() {
        RecordingBackEffects effects = new RecordingBackEffects(false, BottomTabDestination.ASK);

        MainRouteEffectController.applyExplicitFlowDestination(
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.ASK_RESULTS,
                BottomTabDestination.ASK,
                true
            ),
            BottomTabDestination.SEARCH,
            effects
        );

        assertEquals(
            Arrays.asList(
                "applyRouteState:SEARCH_RESULTS:HOME:false"
            ),
            effects.calls
        );
    }

    @Test
    public void backTransitionToPreviousTabAppliesRouteBeforeSectionEffects() {
        MainRouteDecisionHelper.Transition transition =
            new MainRouteDecisionHelper.Transition(
                new MainRouteDecisionHelper.RouteState(
                    MainRouteDecisionHelper.Surface.RECENT_THREADS,
                    BottomTabDestination.ASK,
                    false
                ),
                MainRouteDecisionHelper.Effect.SHOW_PREVIOUS_TAB
            );
        RecordingBackEffects effects = new RecordingBackEffects(true);

        assertTrue(MainRouteEffectController.applyBackTransition(transition, effects));

        assertEquals(
            Arrays.asList(
                "applyRouteState:RECENT_THREADS:ASK:false",
                "updateActionLabels",
                "dismissSearchKeyboard",
                "ensureBrowseHomeVisible",
                "scrollRecentThreadsIntoView"
            ),
            effects.calls
        );
    }

    @Test
    public void systemBackPopsPreviousTabBeforeApplyingRouteTransition() {
        RecordingBackEffects effects = new RecordingBackEffects(true, BottomTabDestination.PINS);
        effects.previousPhoneTab = BottomTabDestination.ASK;

        assertTrue(MainRouteEffectController.applySystemBackTransition(
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.SAVED_GUIDES,
                BottomTabDestination.PINS,
                false
            ),
            effects
        ));

        assertEquals(
            Arrays.asList(
                "popPreviousPhoneTab",
                "applyRouteState:RECENT_THREADS:ASK:false",
                "updateActionLabels",
                "dismissSearchKeyboard",
                "ensureBrowseHomeVisible",
                "scrollRecentThreadsIntoView"
            ),
            effects.calls
        );
    }

    @Test
    public void restoredSearchResultsSystemBackReturnsBrowseWithoutPoppingStaleAskOwner() {
        MainRouteDecisionHelper.RouteState recreatedSearch =
            MainRouteDecisionHelper.resolveRestoredMainRouteState(
                MainRouteDecisionHelper.Surface.SEARCH_RESULTS.name(),
                BottomTabDestination.SEARCH.name(),
                true,
                true,
                BottomTabDestination.ASK.name()
            );
        RecordingBackEffects effects = new RecordingBackEffects(false, BottomTabDestination.ASK);
        effects.previousPhoneTab = BottomTabDestination.PINS;

        assertTrue(MainRouteEffectController.applySystemBackTransition(
            recreatedSearch,
            effects
        ));

        assertEquals(
            Arrays.asList(
                "applyRouteState:BROWSE:HOME:false",
                "returnToBrowse"
            ),
            effects.calls
        );
    }

    @Test
    public void zeroResultSearchSystemBackReturnsBrowseWithoutPoppingPreviousTab() {
        MainRouteDecisionHelper.RouteState zeroResultSearch =
            MainRouteDecisionHelper.enterSearch(MainRouteDecisionHelper.browseHome()).routeState;
        RecordingBackEffects effects = new RecordingBackEffects(false, BottomTabDestination.HOME);
        effects.previousPhoneTab = BottomTabDestination.PINS;

        assertTrue(MainRouteEffectController.applySystemBackTransition(
            zeroResultSearch,
            effects
        ));

        assertEquals(
            Arrays.asList(
                "applyRouteState:BROWSE:HOME:false",
                "returnToBrowse"
            ),
            effects.calls
        );
    }

    @Test
    public void savedTransitionAfterRestoredSearchPushesHomeOwnerForBack() {
        MainRouteDecisionHelper.RouteState restoredSearch =
            MainRouteDecisionHelper.resolveRestoredMainRouteState(
                MainRouteDecisionHelper.Surface.SEARCH_RESULTS.name(),
                BottomTabDestination.HOME.name(),
                false,
                true,
                BottomTabDestination.PINS.name()
            );
        MainRouteDecisionHelper.Transition transition =
            MainRouteDecisionHelper.openPhoneTab(restoredSearch, BottomTabDestination.PINS);
        RecordingBackEffects effects = new RecordingBackEffects(false, BottomTabDestination.HOME);

        MainRouteEffectController.applyPhoneTabTransition(transition, true, effects);

        assertEquals(
            Arrays.asList(
                "pushPhoneTab:HOME",
                "applyRouteState:SAVED_GUIDES:PINS:false",
                "updateActionLabels",
                "dismissSearchKeyboard",
                "prepareSavedGuidesDestination"
            ),
            effects.calls
        );
    }

    @Test
    public void savedTransitionAfterRestoredAskPushesAskOwnerForBack() {
        MainRouteDecisionHelper.RouteState restoredAsk =
            MainRouteDecisionHelper.resolveRestoredMainRouteState(
                MainRouteDecisionHelper.Surface.ASK_RESULTS.name(),
                BottomTabDestination.ASK.name(),
                true,
                true,
                BottomTabDestination.HOME.name()
            );
        MainRouteDecisionHelper.Transition transition =
            MainRouteDecisionHelper.openPhoneTab(restoredAsk, BottomTabDestination.PINS);
        RecordingBackEffects effects = new RecordingBackEffects(false, BottomTabDestination.ASK);

        MainRouteEffectController.applyPhoneTabTransition(transition, true, effects);

        assertEquals(
            Arrays.asList(
                "pushPhoneTab:ASK",
                "applyRouteState:SAVED_GUIDES:PINS:false",
                "updateActionLabels",
                "dismissSearchKeyboard",
                "prepareSavedGuidesDestination"
            ),
            effects.calls
        );
    }

    @Test
    public void threadsBackAfterRestoreCanReturnToSavedGuides() {
        RecordingBackEffects effects = new RecordingBackEffects(true, BottomTabDestination.ASK);
        effects.previousPhoneTab = BottomTabDestination.PINS;

        assertTrue(MainRouteEffectController.applySystemBackTransition(
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.RECENT_THREADS,
                BottomTabDestination.ASK,
                false
            ),
            effects
        ));

        assertEquals(
            Arrays.asList(
                "popPreviousPhoneTab",
                "applyRouteState:SAVED_GUIDES:PINS:false",
                "updateActionLabels",
                "dismissSearchKeyboard",
                "prepareSavedGuidesDestination"
            ),
            effects.calls
        );
    }

    @Test
    public void systemBackDoesNotPopHistoryWhenBrowseHomeOwnsBack() {
        RecordingBackEffects effects = new RecordingBackEffects(true, BottomTabDestination.HOME);
        effects.previousPhoneTab = BottomTabDestination.ASK;

        assertFalse(MainRouteEffectController.applySystemBackTransition(
            MainRouteDecisionHelper.browseHome(),
            effects
        ));

        assertTrue(effects.calls.isEmpty());
    }

    @Test
    public void homeChromeBackDelegatesResultSurfacesToBrowseReturn() {
        RecordingBackEffects effects = new RecordingBackEffects(false, BottomTabDestination.ASK);
        effects.previousPhoneTab = BottomTabDestination.PINS;

        assertTrue(MainRouteEffectController.applyHomeChromeBackTransition(
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.ASK_RESULTS,
                BottomTabDestination.ASK,
                true
            ),
            effects
        ));

        assertEquals(
            Arrays.asList(
                "applyRouteState:BROWSE:HOME:false",
                "returnToBrowse"
            ),
            effects.calls
        );
    }

    @Test
    public void homeChromeBackIgnoresBrowseSurfaces() {
        RecordingBackEffects effects = new RecordingBackEffects(true, BottomTabDestination.PINS);
        effects.previousPhoneTab = BottomTabDestination.ASK;

        assertFalse(MainRouteEffectController.applyHomeChromeBackTransition(
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.SAVED_GUIDES,
                BottomTabDestination.PINS,
                false
            ),
            effects
        ));

        assertTrue(effects.calls.isEmpty());
    }

    @Test
    public void backTransitionReturnToBrowseAppliesRouteBeforeBrowseLoad() {
        MainRouteDecisionHelper.Transition transition =
            new MainRouteDecisionHelper.Transition(
                MainRouteDecisionHelper.browseHome(),
                MainRouteDecisionHelper.Effect.RETURN_TO_BROWSE
            );
        RecordingBackEffects effects = new RecordingBackEffects(false);

        assertTrue(MainRouteEffectController.applyBackTransition(transition, effects));

        assertEquals(
            Arrays.asList(
                "applyRouteState:BROWSE:HOME:false",
                "returnToBrowse"
            ),
            effects.calls
        );
    }

    @Test
    public void backTransitionLeavesExitActivityForActivityBackDispatcher() {
        MainRouteDecisionHelper.Transition transition =
            new MainRouteDecisionHelper.Transition(
                MainRouteDecisionHelper.browseHome(),
                MainRouteDecisionHelper.Effect.EXIT_ACTIVITY
            );
        RecordingBackEffects effects = new RecordingBackEffects(true);

        assertFalse(MainRouteEffectController.applyBackTransition(transition, effects));
        assertTrue(effects.calls.isEmpty());
    }

    @Test
    public void callbackBackEffectsDelegateAllActivityGlueMethods() {
        List<String> calls = new ArrayList<>();
        MainRouteDecisionHelper.RouteState routeState =
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.SAVED_GUIDES,
                BottomTabDestination.PINS,
                false
            );
        MainRouteEffectController.BackEffects effects = MainRouteEffectController.backEffects(
            () -> BottomTabDestination.ASK,
            destination -> calls.add("pushPhoneTab:" + destination),
            state -> calls.add("applyRouteState:" + state.surface + ":" + state.activePhoneTab),
            () -> {
                calls.add("popPreviousPhoneTab");
                return BottomTabDestination.HOME;
            },
            () -> calls.add("returnToBrowse"),
            () -> {
                calls.add("isBrowseModeActive");
                return true;
            },
            () -> calls.add("updateActionLabels"),
            () -> calls.add("dismissSearchKeyboard"),
            () -> calls.add("ensureBrowseHomeVisible"),
            () -> calls.add("scrollBrowseToTop"),
            () -> calls.add("focusSharedInput"),
            () -> calls.add("scrollRecentThreadsIntoView"),
            () -> calls.add("prepareSavedGuidesDestination")
        );

        assertEquals(BottomTabDestination.ASK, effects.activePhoneTab());
        effects.pushPhoneTab(BottomTabDestination.PINS);
        effects.applyRouteState(routeState);
        assertEquals(BottomTabDestination.HOME, effects.popPreviousPhoneTab());
        effects.returnToBrowse();
        assertTrue(effects.isBrowseModeActive());
        effects.updateActionLabels();
        effects.dismissSearchKeyboard();
        effects.ensureBrowseHomeVisible();
        effects.scrollBrowseToTop();
        effects.focusSharedInput();
        effects.scrollRecentThreadsIntoView();
        effects.prepareSavedGuidesDestination();

        assertEquals(
            Arrays.asList(
                "pushPhoneTab:PINS",
                "applyRouteState:SAVED_GUIDES:PINS",
                "popPreviousPhoneTab",
                "returnToBrowse",
                "isBrowseModeActive",
                "updateActionLabels",
                "dismissSearchKeyboard",
                "ensureBrowseHomeVisible",
                "scrollBrowseToTop",
                "focusSharedInput",
                "scrollRecentThreadsIntoView",
                "prepareSavedGuidesDestination"
            ),
            calls
        );
    }

    private static final class RecordingBackEffects implements MainRouteEffectController.BackEffects {
        final List<String> calls = new ArrayList<>();
        private final boolean browseModeActive;
        private final BottomTabDestination activePhoneTab;
        private BottomTabDestination previousPhoneTab;

        RecordingBackEffects(boolean browseModeActive) {
            this(browseModeActive, BottomTabDestination.HOME);
        }

        RecordingBackEffects(boolean browseModeActive, BottomTabDestination activePhoneTab) {
            this.browseModeActive = browseModeActive;
            this.activePhoneTab = activePhoneTab;
        }

        @Override
        public BottomTabDestination activePhoneTab() {
            return activePhoneTab;
        }

        @Override
        public void pushPhoneTab(BottomTabDestination destination) {
            calls.add("pushPhoneTab:" + destination);
        }

        @Override
        public void applyRouteState(MainRouteDecisionHelper.RouteState routeState) {
            calls.add("applyRouteState:"
                + routeState.surface
                + ":"
                + routeState.activePhoneTab
                + ":"
                + routeState.askLaneActive);
        }

        @Override
        public BottomTabDestination popPreviousPhoneTab() {
            calls.add("popPreviousPhoneTab");
            return previousPhoneTab;
        }

        @Override
        public void returnToBrowse() {
            calls.add("returnToBrowse");
        }

        @Override
        public boolean isBrowseModeActive() {
            return browseModeActive;
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
