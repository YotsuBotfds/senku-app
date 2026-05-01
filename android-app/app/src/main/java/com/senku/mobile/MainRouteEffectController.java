package com.senku.mobile;

import com.senku.ui.primitives.BottomTabDestination;

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

    interface RouteEffects extends Effects {
        BottomTabDestination activePhoneTab();

        void pushPhoneTab(BottomTabDestination destination);

        void applyRouteState(MainRouteDecisionHelper.RouteState routeState);
    }

    interface BackEffects extends RouteEffects {
        BottomTabDestination popPreviousPhoneTab();

        void returnToBrowse();
    }

    private MainRouteEffectController() {
    }

    static boolean applySystemBackTransition(
        MainRouteDecisionHelper.RouteState currentRouteState,
        BackEffects effects
    ) {
        if (effects == null) {
            return false;
        }
        BottomTabDestination previousTab = effects.isBrowseModeActive()
            && effects.activePhoneTab() != BottomTabDestination.HOME
            ? effects.popPreviousPhoneTab()
            : null;
        MainRouteDecisionHelper.Transition transition = MainRouteDecisionHelper.systemBack(
            currentRouteState,
            previousTab
        );
        return applyBackTransition(transition, effects);
    }

    static boolean applyBackTransition(
        MainRouteDecisionHelper.Transition transition,
        BackEffects effects
    ) {
        if (transition == null || effects == null) {
            return false;
        }
        switch (transition.effect) {
            case SHOW_PREVIOUS_TAB:
                effects.applyRouteState(transition.routeState);
                applyPhoneTabTransitionEffect(transition, effects);
                return true;
            case RETURN_TO_BROWSE:
                effects.applyRouteState(transition.routeState);
                effects.returnToBrowse();
                return true;
            default:
                return false;
        }
    }

    static void applyPhoneTabTransition(
        MainRouteDecisionHelper.Transition transition,
        boolean pushHistory,
        RouteEffects effects
    ) {
        if (transition == null || transition.effect == MainRouteDecisionHelper.Effect.NONE || effects == null) {
            return;
        }
        BottomTabDestination selectionOwner = transition.routeState.activePhoneTab;
        if (pushHistory && selectionOwner != effects.activePhoneTab()) {
            effects.pushPhoneTab(effects.activePhoneTab());
        }
        effects.applyRouteState(transition.routeState);
        applyPhoneTabTransitionEffect(transition, effects);
    }

    static void applyPhoneTabTransitionEffect(
        MainRouteDecisionHelper.Transition transition,
        Effects effects
    ) {
        if (transition == null) {
            return;
        }
        applyPhoneTabTransitionEffect(transition.effect, transition.routeState, effects);
    }

    static void applyPhoneTabTransitionEffect(
        MainRouteDecisionHelper.Effect effect,
        Effects effects
    ) {
        applyPhoneTabTransitionEffect(effect, null, effects);
    }

    private static void applyPhoneTabTransitionEffect(
        MainRouteDecisionHelper.Effect effect,
        MainRouteDecisionHelper.RouteState routeState,
        Effects effects
    ) {
        if (effect == null || effect == MainRouteDecisionHelper.Effect.NONE || effects == null) {
            return;
        }
        effects.updateActionLabels();
        switch (effect) {
            case SHOW_BROWSE_HOME:
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
            case SHOW_PREVIOUS_TAB:
                applyPreviousTabEffect(routeState, effects);
                break;
            default:
                break;
        }
    }

    private static void applyPreviousTabEffect(
        MainRouteDecisionHelper.RouteState routeState,
        Effects effects
    ) {
        if (routeState == null) {
            effects.dismissSearchKeyboard();
            effects.ensureBrowseHomeVisible();
            effects.scrollBrowseToTop();
            return;
        }
        switch (routeState.surface) {
            case RECENT_THREADS:
                effects.dismissSearchKeyboard();
                effects.ensureBrowseHomeVisible();
                effects.scrollRecentThreadsIntoView();
                break;
            case SAVED_GUIDES:
                effects.dismissSearchKeyboard();
                effects.prepareSavedGuidesDestination();
                break;
            case BROWSE:
            default:
                effects.dismissSearchKeyboard();
                effects.ensureBrowseHomeVisible();
                effects.scrollBrowseToTop();
                break;
        }
    }
}
