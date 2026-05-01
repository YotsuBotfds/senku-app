package com.senku.mobile;

import com.senku.ui.primitives.BottomTabDestination;

import java.util.ArrayList;

final class MainRouteCoordinator {
    interface Host {
        boolean shouldHandleMainSurfaceNavigation();

        void onRouteStateApplied(
            MainRouteDecisionHelper.RouteState routeState,
            boolean browseChromeVisible
        );

        void returnToBrowse();

        void updateActionLabels();

        void dismissSearchKeyboard();

        void ensureBrowseHomeVisible();

        void scrollBrowseToTop();

        void focusSharedInput();

        void scrollRecentThreadsIntoView();

        void prepareSavedGuidesDestination();
    }

    private final Host host;
    private final ArrayList<BottomTabDestination> phoneTabBackStack = new ArrayList<>();
    private BottomTabDestination activePhoneTab = BottomTabDestination.HOME;
    private boolean askLaneActive;
    private boolean browseChromeActive = true;

    MainRouteCoordinator(Host host) {
        this.host = host;
    }

    BottomTabDestination activePhoneTab() {
        return activePhoneTab;
    }

    boolean askLaneActive() {
        return askLaneActive;
    }

    boolean isBrowseModeActive() {
        return browseChromeActive;
    }

    MainRouteDecisionHelper.RouteState currentRouteState() {
        return MainRouteDecisionHelper.routeStateForMode(
            browseChromeActive,
            activePhoneTab,
            askLaneActive
        );
    }

    void applyRouteState(MainRouteDecisionHelper.RouteState routeState) {
        MainRouteDecisionHelper.RouteState safeRouteState = routeState == null
            ? MainRouteDecisionHelper.browseHome()
            : routeState;
        activePhoneTab = safeRouteState.activePhoneTab;
        askLaneActive = safeRouteState.askLaneActive;
        browseChromeActive = MainRouteDecisionHelper.isBrowseSurface(safeRouteState.surface);
        if (host != null) {
            host.onRouteStateApplied(safeRouteState, browseChromeActive);
        }
    }

    void showBrowseChrome(boolean show) {
        applyRouteState(MainRouteDecisionHelper.routeStateForMode(
            show,
            activePhoneTab,
            askLaneActive
        ));
    }

    boolean applySystemBackTransition() {
        if (host == null || !host.shouldHandleMainSurfaceNavigation()) {
            return false;
        }
        return MainRouteEffectController.applySystemBackTransition(currentRouteState(), routeEffects());
    }

    boolean applyBackTransition(MainRouteDecisionHelper.Transition transition) {
        return MainRouteEffectController.applyBackTransition(transition, routeEffects());
    }

    boolean applyHomeChromeBackTransition() {
        return MainRouteEffectController.applyHomeChromeBackTransition(currentRouteState(), routeEffects());
    }

    void openPhoneTab(BottomTabDestination destination, boolean pushHistory) {
        if (host == null || !host.shouldHandleMainSurfaceNavigation()) {
            return;
        }
        applyPhoneTabTransition(
            MainRouteDecisionHelper.openPhoneTab(currentRouteState(), destination),
            pushHistory
        );
    }

    void applyPhoneTabTransition(MainRouteDecisionHelper.Transition transition, boolean pushHistory) {
        MainRouteEffectController.applyPhoneTabTransition(transition, pushHistory, routeEffects());
    }

    void setPhoneTabFromFlow(BottomTabDestination destination) {
        if (host == null || !host.shouldHandleMainSurfaceNavigation()) {
            return;
        }
        MainRouteEffectController.applyExplicitFlowDestination(
            currentRouteState(),
            destination,
            routeEffects()
        );
    }

    void applyResumeKeyboardPolicy(boolean hasVisibleResults) {
        if (host == null) {
            return;
        }
        MainRouteEffectController.applyResumeKeyboardPolicy(
            hasVisibleResults,
            currentRouteState(),
            routeEffects()
        );
    }

    void enterSearchResultsRoute() {
        applyRouteState(MainRouteDecisionHelper.enterSearch(currentRouteState()).routeState);
    }

    void enterAskResultsRoute() {
        applyRouteState(MainRouteDecisionHelper.enterAsk(currentRouteState()).routeState);
    }

    void openEmptyAskLane() {
        applyRouteState(MainRouteDecisionHelper.openEmptyAskLane(currentRouteState()));
    }

    void browseGuidesRoute() {
        applyRouteState(MainRouteDecisionHelper.browseGuides(currentRouteState()));
    }

    void clearChatSessionRoute() {
        applyRouteState(MainRouteDecisionHelper.clearChatSession());
    }

    private MainRouteEffectController.BackEffects routeEffects() {
        return MainRouteEffectController.backEffects(
            this::activePhoneTab,
            this::pushPhoneTab,
            this::applyRouteState,
            this::popPreviousPhoneTab,
            host::returnToBrowse,
            this::isBrowseModeActive,
            host::updateActionLabels,
            host::dismissSearchKeyboard,
            host::ensureBrowseHomeVisible,
            host::scrollBrowseToTop,
            host::focusSharedInput,
            host::scrollRecentThreadsIntoView,
            host::prepareSavedGuidesDestination
        );
    }

    private void pushPhoneTab(BottomTabDestination destination) {
        MainPhoneTabHistoryPolicy.StackState stackState =
            MainPhoneTabHistoryPolicy.push(phoneTabBackStack, destination);
        phoneTabBackStack.clear();
        phoneTabBackStack.addAll(stackState.stack);
    }

    private BottomTabDestination popPreviousPhoneTab() {
        MainPhoneTabHistoryPolicy.PopResult popResult =
            MainPhoneTabHistoryPolicy.popPrevious(phoneTabBackStack, activePhoneTab);
        phoneTabBackStack.clear();
        phoneTabBackStack.addAll(popResult.stack);
        return popResult.destination;
    }
}
