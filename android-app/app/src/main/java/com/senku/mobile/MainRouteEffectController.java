package com.senku.mobile;

final class MainRouteEffectController {
    interface Effects {
        boolean isBrowseModeActive();

        void updateActionLabels();

        void dismissSearchKeyboard();

        void ensureBrowseHomeVisible();

        void scrollBrowseToTop();

        void focusSearchInput();

        void scrollRecentThreadsIntoView();

        void prepareSavedGuidesDestination();
    }

    private MainRouteEffectController() {
    }

    static void applyPhoneTabTransitionEffect(
        MainRouteDecisionHelper.Effect effect,
        Effects effects
    ) {
        if (effect == null || effect == MainRouteDecisionHelper.Effect.NONE || effects == null) {
            return;
        }
        effects.updateActionLabels();
        switch (effect) {
            case SHOW_BROWSE_HOME:
                effects.updateActionLabels();
                effects.dismissSearchKeyboard();
                effects.ensureBrowseHomeVisible();
                effects.scrollBrowseToTop();
                break;
            case FOCUS_SEARCH_INPUT:
            case FOCUS_ASK_INPUT:
                if (effects.isBrowseModeActive()) {
                    effects.scrollBrowseToTop();
                }
                effects.focusSearchInput();
                break;
            case SHOW_RECENT_THREADS:
                effects.dismissSearchKeyboard();
                effects.ensureBrowseHomeVisible();
                effects.scrollRecentThreadsIntoView();
                break;
            case SHOW_SAVED_GUIDES:
                effects.dismissSearchKeyboard();
                effects.prepareSavedGuidesDestination();
                break;
            default:
                break;
        }
    }
}
