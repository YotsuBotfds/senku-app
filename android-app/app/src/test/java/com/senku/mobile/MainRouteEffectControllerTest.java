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

        RecordingBackEffects(boolean browseModeActive) {
            this.browseModeActive = browseModeActive;
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
