package com.senku.mobile;

import com.senku.ui.primitives.BottomTabDestination;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class MainRouteEffectController {
    interface Effects {
        boolean isBrowseModeActive();

        void updateActionLabels();

        void dismissSearchKeyboard();

        void ensureBrowseHomeVisible();

        void scrollBrowseToTop();

        void focusSharedInput();

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

    static BackEffects backEffects(
        Supplier<BottomTabDestination> activePhoneTab,
        Consumer<BottomTabDestination> pushPhoneTab,
        Consumer<MainRouteDecisionHelper.RouteState> applyRouteState,
        Supplier<BottomTabDestination> popPreviousPhoneTab,
        Runnable returnToBrowse,
        BooleanSupplier browseModeActive,
        Runnable updateActionLabels,
        Runnable dismissSearchKeyboard,
        Runnable ensureBrowseHomeVisible,
        Runnable scrollBrowseToTop,
        Runnable focusSharedInput,
        Runnable scrollRecentThreadsIntoView,
        Runnable prepareSavedGuidesDestination
    ) {
        return new CallbackBackEffects(
            activePhoneTab,
            pushPhoneTab,
            applyRouteState,
            popPreviousPhoneTab,
            returnToBrowse,
            browseModeActive,
            updateActionLabels,
            dismissSearchKeyboard,
            ensureBrowseHomeVisible,
            scrollBrowseToTop,
            focusSharedInput,
            scrollRecentThreadsIntoView,
            prepareSavedGuidesDestination
        );
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

    static boolean applyHomeChromeBackTransition(
        MainRouteDecisionHelper.RouteState currentRouteState,
        BackEffects effects
    ) {
        if (effects == null) {
            return false;
        }
        MainRouteDecisionHelper.Transition transition =
            MainRouteDecisionHelper.homeChromeBack(currentRouteState);
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

    static void applyExplicitFlowDestination(
        MainRouteDecisionHelper.RouteState currentRouteState,
        BottomTabDestination destination,
        RouteEffects effects
    ) {
        if (effects == null) {
            return;
        }
        effects.applyRouteState(MainRouteDecisionHelper.routeStateForExplicitFlowDestination(
            currentRouteState,
            destination
        ));
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
                effects.focusSharedInput();
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

    private static final class CallbackBackEffects implements BackEffects {
        private final Supplier<BottomTabDestination> activePhoneTab;
        private final Consumer<BottomTabDestination> pushPhoneTab;
        private final Consumer<MainRouteDecisionHelper.RouteState> applyRouteState;
        private final Supplier<BottomTabDestination> popPreviousPhoneTab;
        private final Runnable returnToBrowse;
        private final BooleanSupplier browseModeActive;
        private final Runnable updateActionLabels;
        private final Runnable dismissSearchKeyboard;
        private final Runnable ensureBrowseHomeVisible;
        private final Runnable scrollBrowseToTop;
        private final Runnable focusSharedInput;
        private final Runnable scrollRecentThreadsIntoView;
        private final Runnable prepareSavedGuidesDestination;

        private CallbackBackEffects(
            Supplier<BottomTabDestination> activePhoneTab,
            Consumer<BottomTabDestination> pushPhoneTab,
            Consumer<MainRouteDecisionHelper.RouteState> applyRouteState,
            Supplier<BottomTabDestination> popPreviousPhoneTab,
            Runnable returnToBrowse,
            BooleanSupplier browseModeActive,
            Runnable updateActionLabels,
            Runnable dismissSearchKeyboard,
            Runnable ensureBrowseHomeVisible,
            Runnable scrollBrowseToTop,
            Runnable focusSharedInput,
            Runnable scrollRecentThreadsIntoView,
            Runnable prepareSavedGuidesDestination
        ) {
            this.activePhoneTab = activePhoneTab;
            this.pushPhoneTab = pushPhoneTab;
            this.applyRouteState = applyRouteState;
            this.popPreviousPhoneTab = popPreviousPhoneTab;
            this.returnToBrowse = returnToBrowse;
            this.browseModeActive = browseModeActive;
            this.updateActionLabels = updateActionLabels;
            this.dismissSearchKeyboard = dismissSearchKeyboard;
            this.ensureBrowseHomeVisible = ensureBrowseHomeVisible;
            this.scrollBrowseToTop = scrollBrowseToTop;
            this.focusSharedInput = focusSharedInput;
            this.scrollRecentThreadsIntoView = scrollRecentThreadsIntoView;
            this.prepareSavedGuidesDestination = prepareSavedGuidesDestination;
        }

        @Override
        public BottomTabDestination activePhoneTab() {
            return activePhoneTab.get();
        }

        @Override
        public void pushPhoneTab(BottomTabDestination destination) {
            pushPhoneTab.accept(destination);
        }

        @Override
        public void applyRouteState(MainRouteDecisionHelper.RouteState routeState) {
            applyRouteState.accept(routeState);
        }

        @Override
        public BottomTabDestination popPreviousPhoneTab() {
            return popPreviousPhoneTab.get();
        }

        @Override
        public void returnToBrowse() {
            returnToBrowse.run();
        }

        @Override
        public boolean isBrowseModeActive() {
            return browseModeActive.getAsBoolean();
        }

        @Override
        public void updateActionLabels() {
            updateActionLabels.run();
        }

        @Override
        public void dismissSearchKeyboard() {
            dismissSearchKeyboard.run();
        }

        @Override
        public void ensureBrowseHomeVisible() {
            ensureBrowseHomeVisible.run();
        }

        @Override
        public void scrollBrowseToTop() {
            scrollBrowseToTop.run();
        }

        @Override
        public void focusSharedInput() {
            focusSharedInput.run();
        }

        @Override
        public void scrollRecentThreadsIntoView() {
            scrollRecentThreadsIntoView.run();
        }

        @Override
        public void prepareSavedGuidesDestination() {
            prepareSavedGuidesDestination.run();
        }
    }
}
