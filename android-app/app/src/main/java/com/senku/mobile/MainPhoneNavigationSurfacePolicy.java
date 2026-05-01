package com.senku.mobile;

import com.senku.ui.primitives.BottomTabDestination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class MainPhoneNavigationSurfacePolicy {
    private MainPhoneNavigationSurfacePolicy() {
    }

    static boolean shouldInstallRuntimePhoneBottomTabBar(boolean phoneFormFactor, boolean landscapePhone) {
        return phoneFormFactor && !landscapePhone;
    }

    static boolean shouldBindStaticNavigationRail(boolean landscapePhoneLayout, boolean tabletSearchLayout) {
        return landscapePhoneLayout || tabletSearchLayout;
    }

    static boolean shouldHandleMainSurfaceNavigationTabs(boolean phoneFormFactor, boolean tabletSearchLayout) {
        return phoneFormFactor || tabletSearchLayout;
    }

    static List<BottomTabDestination> buildVisiblePhoneTabDestinations() {
        return phoneTabSurfaceDestinations(buildPhonePrimaryDestinations());
    }

    static List<BottomTabDestination> buildPhonePrimaryDestinations() {
        return Arrays.asList(
            BottomTabDestination.HOME,
            BottomTabDestination.ASK,
            BottomTabDestination.PINS
        );
    }

    static List<BottomTabDestination> phoneTabSurfaceDestinations(
        List<BottomTabDestination> primaryDestinations
    ) {
        if (primaryDestinations == null || primaryDestinations.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<BottomTabDestination> destinations = new ArrayList<>(primaryDestinations.size());
        for (BottomTabDestination destination : primaryDestinations) {
            if (destination != null) {
                destinations.add(destination);
            }
        }
        return destinations;
    }

    static boolean isLibraryPhoneFlowIntent(BottomTabDestination destination) {
        return destination != null
            && MainRouteDecisionHelper.phoneTabSelectionOwner(destination) == BottomTabDestination.HOME;
    }

    static boolean isAskPhoneFlowIntent(BottomTabDestination destination) {
        return destination != null
            && MainRouteDecisionHelper.phoneTabSelectionOwner(destination) == BottomTabDestination.ASK;
    }

    static boolean isSavedPhoneFlowIntent(BottomTabDestination destination) {
        return SavedGuidesPolicy.isSavedPhoneFlowIntent(destination);
    }
}
