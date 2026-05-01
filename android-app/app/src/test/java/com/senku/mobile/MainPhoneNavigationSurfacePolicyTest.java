package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.senku.ui.primitives.BottomTabDestination;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class MainPhoneNavigationSurfacePolicyTest {
    @Test
    public void visiblePhoneTabsUsePrimaryNavigationSlice() {
        assertEquals(
            Arrays.asList(
                BottomTabDestination.HOME,
                BottomTabDestination.ASK,
                BottomTabDestination.PINS
            ),
            MainPhoneNavigationSurfacePolicy.buildVisiblePhoneTabDestinations()
        );
    }

    @Test
    public void surfaceDestinationsDropNullsAndPreserveOrder() {
        List<BottomTabDestination> destinations =
            MainPhoneNavigationSurfacePolicy.phoneTabSurfaceDestinations(Arrays.asList(
                BottomTabDestination.HOME,
                null,
                BottomTabDestination.ASK,
                BottomTabDestination.PINS
            ));

        assertEquals(
            Arrays.asList(
                BottomTabDestination.HOME,
                BottomTabDestination.ASK,
                BottomTabDestination.PINS
            ),
            destinations
        );
    }

    @Test
    public void runtimeBottomBarInstallsOnlyForPortraitPhoneSurface() {
        assertTrue(MainPhoneNavigationSurfacePolicy.shouldInstallRuntimePhoneBottomTabBar(true, false));

        assertFalse(MainPhoneNavigationSurfacePolicy.shouldInstallRuntimePhoneBottomTabBar(true, true));
        assertFalse(MainPhoneNavigationSurfacePolicy.shouldInstallRuntimePhoneBottomTabBar(false, false));
    }

    @Test
    public void staticNavigationRailBindsForLandscapePhoneOrTabletSearchShell() {
        assertTrue(MainPhoneNavigationSurfacePolicy.shouldBindStaticNavigationRail(true, false));
        assertTrue(MainPhoneNavigationSurfacePolicy.shouldBindStaticNavigationRail(false, true));

        assertFalse(MainPhoneNavigationSurfacePolicy.shouldBindStaticNavigationRail(false, false));
    }

    @Test
    public void mainSurfaceTabsHandlePhoneOrTabletSearchShell() {
        assertTrue(MainPhoneNavigationSurfacePolicy.shouldHandleMainSurfaceNavigationTabs(true, false));
        assertTrue(MainPhoneNavigationSurfacePolicy.shouldHandleMainSurfaceNavigationTabs(false, true));

        assertFalse(MainPhoneNavigationSurfacePolicy.shouldHandleMainSurfaceNavigationTabs(false, false));
    }

    @Test
    public void phoneFlowIntentPredicatesFollowRouteSelectionOwner() {
        for (BottomTabDestination destination : BottomTabDestination.values()) {
            BottomTabDestination owner = MainRouteDecisionHelper.phoneTabSelectionOwner(destination);

            assertEquals(
                owner == BottomTabDestination.HOME,
                MainPhoneNavigationSurfacePolicy.isLibraryPhoneFlowIntent(destination)
            );
            assertEquals(
                owner == BottomTabDestination.ASK,
                MainPhoneNavigationSurfacePolicy.isAskPhoneFlowIntent(destination)
            );
            assertEquals(
                owner == BottomTabDestination.PINS,
                MainPhoneNavigationSurfacePolicy.isSavedPhoneFlowIntent(destination)
            );
        }

        assertFalse(MainPhoneNavigationSurfacePolicy.isLibraryPhoneFlowIntent(null));
        assertFalse(MainPhoneNavigationSurfacePolicy.isAskPhoneFlowIntent(null));
        assertFalse(MainPhoneNavigationSurfacePolicy.isSavedPhoneFlowIntent(null));
    }
}
