package com.senku.mobile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.senku.ui.primitives.BottomTabDestination;

import org.junit.Test;

import java.util.List;

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

    @Test
    public void controllerRefreshPlanClearsWhenRepositoryOrSavedGuidesAreMissing() {
        MainSavedGuidesController controller = new MainSavedGuidesController();

        assertTrue(controller.planRefresh(false, List.of("GD-001")).renderEmpty);
        assertTrue(controller.planRefresh(true, List.of()).renderEmpty);
        assertTrue(controller.planRefresh(true, null).renderEmpty);
    }

    @Test
    public void controllerRefreshPlanCapsSavedGuideIdsForLoading() {
        MainSavedGuidesController controller = new MainSavedGuidesController();

        MainSavedGuidesController.RefreshPlan refreshPlan = controller.planRefresh(
            true,
            List.of(
                "GD-001",
                "GD-002",
                "GD-003",
                "GD-004",
                "GD-005",
                "GD-006",
                "GD-007",
                "GD-008",
                "GD-009",
                "GD-010",
                "GD-011",
                "GD-012",
                "GD-013"
            )
        );

        assertFalse(refreshPlan.renderEmpty);
        assertEquals(12, refreshPlan.guideIdsToLoad.size());
        assertEquals("GD-001", refreshPlan.guideIdsToLoad.get(0));
        assertEquals("GD-012", refreshPlan.guideIdsToLoad.get(11));
    }

    @Test
    public void controllerConsumesPendingFocusOnlyWhenBrowseSectionIsVisible() {
        MainSavedGuidesController controller = new MainSavedGuidesController();

        controller.requestSectionFocus();
        assertTrue(controller.hasPendingSectionFocusForTest());
        assertTrue(controller.shouldAttemptSectionFocus(true));

        assertFalse(controller.consumeSectionFocusIfReady(true, false));
        assertTrue(controller.hasPendingSectionFocusForTest());

        assertTrue(controller.consumeSectionFocusIfReady(true, true));
        assertFalse(controller.hasPendingSectionFocusForTest());
    }

    @Test
    public void controllerSectionStateSeparatesSectionAndEmptyContentVisibility() {
        MainSavedGuidesController controller = new MainSavedGuidesController();

        MainSavedGuidesController.SectionState savedFlowEmpty =
            controller.sectionState(true, BottomTabDestination.PINS, 0);
        MainSavedGuidesController.SectionState homeFlowEmpty =
            controller.sectionState(true, BottomTabDestination.HOME, 0);
        MainSavedGuidesController.SectionState homeFlowSaved =
            controller.sectionState(true, BottomTabDestination.HOME, 1);

        assertTrue(savedFlowEmpty.sectionVisible);
        assertTrue(savedFlowEmpty.emptyTextVisible);
        assertFalse(savedFlowEmpty.savedGuidesVisible);

        assertFalse(homeFlowEmpty.sectionVisible);
        assertTrue(homeFlowEmpty.emptyTextVisible);
        assertFalse(homeFlowEmpty.savedGuidesVisible);

        assertTrue(homeFlowSaved.sectionVisible);
        assertFalse(homeFlowSaved.emptyTextVisible);
        assertTrue(homeFlowSaved.savedGuidesVisible);
    }
}
