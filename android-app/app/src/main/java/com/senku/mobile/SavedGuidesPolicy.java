package com.senku.mobile;

import com.senku.ui.primitives.BottomTabDestination;

final class SavedGuidesPolicy {
    private SavedGuidesPolicy() {
    }

    static boolean shouldShowSection(
        boolean browseMode,
        BottomTabDestination activePhoneTab,
        int savedGuideCount
    ) {
        if (!browseMode) {
            return false;
        }
        return savedGuideCount > 0 || activePhoneTab == BottomTabDestination.PINS;
    }

    static boolean shouldLoadBrowseGuidesForDestination(boolean repositoryReady, int loadedGuideCount) {
        return repositoryReady && loadedGuideCount <= 0;
    }

    static boolean isSavedPhoneFlowIntent(BottomTabDestination destination) {
        return destination != null
            && MainRouteDecisionHelper.phoneTabSelectionOwner(destination) == BottomTabDestination.PINS;
    }

    static boolean shouldFocusSection(
        boolean pendingFocus,
        boolean browseMode,
        boolean sectionVisible
    ) {
        return pendingFocus && browseMode && sectionVisible;
    }
}
