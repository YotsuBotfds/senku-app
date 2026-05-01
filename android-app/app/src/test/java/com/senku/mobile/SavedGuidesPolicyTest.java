package com.senku.mobile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.senku.ui.primitives.BottomTabDestination;

import org.junit.Test;

public final class SavedGuidesPolicyTest {
    @Test
    public void pinsBrowseFlowShowsEmptySavedSection() {
        assertTrue(SavedGuidesPolicy.shouldShowSection(true, BottomTabDestination.PINS, 0));
    }

    @Test
    public void homeAndAskBrowseFlowsHideEmptySavedSection() {
        assertFalse(SavedGuidesPolicy.shouldShowSection(true, BottomTabDestination.HOME, 0));
        assertFalse(SavedGuidesPolicy.shouldShowSection(true, BottomTabDestination.ASK, 0));
    }

    @Test
    public void nonEmptySavedGuidesShowAcrossBrowseFlows() {
        assertTrue(SavedGuidesPolicy.shouldShowSection(true, BottomTabDestination.HOME, 1));
        assertTrue(SavedGuidesPolicy.shouldShowSection(true, BottomTabDestination.ASK, 2));
        assertTrue(SavedGuidesPolicy.shouldShowSection(true, BottomTabDestination.PINS, 12));
    }

    @Test
    public void nonBrowseFlowsHideSavedSectionEvenWhenSavedGuidesExist() {
        assertFalse(SavedGuidesPolicy.shouldShowSection(false, BottomTabDestination.PINS, 0));
        assertFalse(SavedGuidesPolicy.shouldShowSection(false, BottomTabDestination.HOME, 1));
        assertFalse(SavedGuidesPolicy.shouldShowSection(false, BottomTabDestination.ASK, 2));
    }

    @Test
    public void savedDestinationLoadsBrowseGuidesOnlyWhenRepositoryReadyAndGuidesMissing() {
        assertTrue(SavedGuidesPolicy.shouldLoadBrowseGuidesForDestination(true, 0));

        assertFalse(SavedGuidesPolicy.shouldLoadBrowseGuidesForDestination(true, 1));
        assertFalse(SavedGuidesPolicy.shouldLoadBrowseGuidesForDestination(false, 0));
    }

    @Test
    public void savedPhoneFlowIntentFollowsRouteSelectionOwner() {
        assertTrue(SavedGuidesPolicy.isSavedPhoneFlowIntent(BottomTabDestination.PINS));

        assertFalse(SavedGuidesPolicy.isSavedPhoneFlowIntent(BottomTabDestination.HOME));
        assertFalse(SavedGuidesPolicy.isSavedPhoneFlowIntent(BottomTabDestination.SEARCH));
        assertFalse(SavedGuidesPolicy.isSavedPhoneFlowIntent(BottomTabDestination.ASK));
        assertFalse(SavedGuidesPolicy.isSavedPhoneFlowIntent(BottomTabDestination.THREADS));
        assertFalse(SavedGuidesPolicy.isSavedPhoneFlowIntent(null));
    }

    @Test
    public void openSavedExtraSelectsWholeSavedRoute() {
        MainRouteDecisionHelper.Transition transition = SavedGuidesPolicy.openSavedDestination(
            true,
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.ASK_RESULTS,
                BottomTabDestination.ASK,
                true
            )
        );

        assertEquals(MainRouteDecisionHelper.Effect.SHOW_SAVED_GUIDES, transition.effect);
        assertEquals(MainRouteDecisionHelper.Surface.SAVED_GUIDES, transition.routeState.surface);
        assertEquals(BottomTabDestination.PINS, transition.routeState.activePhoneTab);
        assertFalse(transition.routeState.askLaneActive);
        assertTrue(SavedGuidesPolicy.shouldShowSection(
            MainRouteDecisionHelper.isBrowseSurface(transition.routeState.surface),
            transition.routeState.activePhoneTab,
            0
        ));
    }

    @Test
    public void openSavedExtraPreservesSavedSectionFocusEligibility() {
        MainRouteDecisionHelper.Transition transition = SavedGuidesPolicy.openSavedDestination(
            true,
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
                BottomTabDestination.HOME,
                false
            )
        );

        boolean browseMode = MainRouteDecisionHelper.isBrowseSurface(transition.routeState.surface);
        boolean savedSectionVisible = SavedGuidesPolicy.shouldShowSection(
            browseMode,
            transition.routeState.activePhoneTab,
            0
        );

        assertEquals(MainRouteDecisionHelper.Effect.SHOW_SAVED_GUIDES, transition.effect);
        assertTrue(SavedGuidesPolicy.shouldFocusSection(true, browseMode, savedSectionVisible));
    }

    @Test
    public void missingOpenSavedExtraLeavesCurrentRouteUntouched() {
        MainRouteDecisionHelper.RouteState currentRoute = new MainRouteDecisionHelper.RouteState(
            MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
            BottomTabDestination.HOME,
            false
        );

        MainRouteDecisionHelper.Transition transition =
            SavedGuidesPolicy.openSavedDestination(false, currentRoute);

        assertEquals(MainRouteDecisionHelper.Effect.NONE, transition.effect);
        assertEquals(currentRoute.surface, transition.routeState.surface);
        assertEquals(currentRoute.activePhoneTab, transition.routeState.activePhoneTab);
        assertEquals(currentRoute.askLaneActive, transition.routeState.askLaneActive);
    }

    @Test
    public void focusRequiresPendingBrowseAndVisibleSection() {
        assertTrue(SavedGuidesPolicy.shouldFocusSection(true, true, true));

        assertFalse(SavedGuidesPolicy.shouldFocusSection(false, true, true));
        assertFalse(SavedGuidesPolicy.shouldFocusSection(true, false, true));
        assertFalse(SavedGuidesPolicy.shouldFocusSection(true, true, false));
    }
}
