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
                "focusSearchInput"
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
        public void focusSearchInput() {
            calls.add("focusSearchInput");
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
