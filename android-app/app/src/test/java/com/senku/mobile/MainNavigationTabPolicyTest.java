package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.senku.ui.primitives.BottomTabDestination;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class MainNavigationTabPolicyTest {
    @Test
    public void searchResultsDisplaySearchInHomeSlotWithoutChangingRouteOwner() {
        MainRouteDecisionHelper.RouteState routeState =
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
                BottomTabDestination.HOME,
                false
            );

        MainNavigationTabPolicy.TabDisplayModel model =
            MainNavigationTabPolicy.modelForSlot(BottomTabDestination.HOME, routeState);

        assertEquals(BottomTabDestination.HOME, routeState.activePhoneTab);
        assertEquals(BottomTabDestination.HOME, model.slotDestination);
        assertEquals(BottomTabDestination.SEARCH, model.displayedDestination);
        assertTrue(model.selected);
        assertEquals("Search", model.label);
        assertEquals("Search", model.contentDescription);
    }

    @Test
    public void searchResultsKeepOtherVisibleSlotsOnTheirDisplayedDestinations() {
        MainRouteDecisionHelper.RouteState routeState =
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
                BottomTabDestination.HOME,
                false
            );

        List<MainNavigationTabPolicy.TabDisplayModel> models =
            MainNavigationTabPolicy.modelsForSlots(
                Arrays.asList(
                    BottomTabDestination.HOME,
                    BottomTabDestination.ASK,
                    BottomTabDestination.PINS
                ),
                routeState
            );

        assertEquals(BottomTabDestination.SEARCH, models.get(0).displayedDestination);
        assertEquals(BottomTabDestination.ASK, models.get(1).displayedDestination);
        assertEquals(BottomTabDestination.PINS, models.get(2).displayedDestination);
        assertTrue(models.get(0).selected);
        assertFalse(models.get(1).selected);
        assertFalse(models.get(2).selected);
    }

    @Test
    public void browseRoutesUseConcreteLibraryAskSavedCopy() {
        List<MainNavigationTabPolicy.TabDisplayModel> models =
            MainNavigationTabPolicy.modelsForSlots(
                Arrays.asList(
                    BottomTabDestination.HOME,
                    BottomTabDestination.ASK,
                    BottomTabDestination.PINS
                ),
                MainRouteDecisionHelper.browseHome()
            );

        assertEquals(BottomTabDestination.HOME, models.get(0).displayedDestination);
        assertEquals("Library", models.get(0).label);
        assertEquals("Open Library", models.get(0).contentDescription);
        assertTrue(models.get(0).selected);

        assertEquals("Ask", models.get(1).label);
        assertEquals("Ask the manual", models.get(1).contentDescription);
        assertFalse(models.get(1).selected);

        assertEquals("Saved", models.get(2).label);
        assertEquals("Open saved guides", models.get(2).contentDescription);
        assertFalse(models.get(2).selected);
    }

    @Test
    public void askResultsDisplayAskAsActiveDestination() {
        MainRouteDecisionHelper.RouteState routeState =
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.ASK_RESULTS,
                BottomTabDestination.ASK,
                true
            );

        assertEquals(BottomTabDestination.ASK, MainNavigationTabPolicy.displayedActiveDestination(routeState));
        assertTrue(MainNavigationTabPolicy.modelForSlot(BottomTabDestination.ASK, routeState).selected);
        assertFalse(MainNavigationTabPolicy.modelForSlot(BottomTabDestination.HOME, routeState).selected);
    }
}
