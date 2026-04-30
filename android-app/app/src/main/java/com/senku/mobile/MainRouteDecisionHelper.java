package com.senku.mobile;

import com.senku.ui.primitives.BottomTabDestination;

public final class MainRouteDecisionHelper {
    enum Surface {
        BROWSE,
        SEARCH_RESULTS,
        ASK_RESULTS
    }

    enum Effect {
        NONE,
        EXIT_ACTIVITY,
        FOCUS_SEARCH_INPUT,
        FOCUS_ASK_INPUT,
        SHOW_BROWSE_HOME,
        SHOW_PREVIOUS_TAB,
        SHOW_RECENT_THREADS,
        SHOW_SAVED_GUIDES,
        RETURN_TO_BROWSE
    }

    private MainRouteDecisionHelper() {
    }

    static RouteState browseHome() {
        return new RouteState(Surface.BROWSE, BottomTabDestination.HOME, false);
    }

    static Transition systemBack(RouteState state, BottomTabDestination previousPhoneTab) {
        RouteState route = normalize(state);
        if (route.surface == Surface.BROWSE && route.activePhoneTab != BottomTabDestination.HOME) {
            BottomTabDestination previous = phoneTabSelectionOwner(previousPhoneTab);
            return new Transition(
                new RouteState(Surface.BROWSE, previous == route.activePhoneTab ? BottomTabDestination.HOME : previous, false),
                Effect.SHOW_PREVIOUS_TAB
            );
        }
        if (route.surface != Surface.BROWSE) {
            return new Transition(browseHome(), Effect.RETURN_TO_BROWSE);
        }
        return new Transition(route, Effect.EXIT_ACTIVITY);
    }

    static Transition homeChromeBack(RouteState state) {
        RouteState route = normalize(state);
        if (route.surface == Surface.BROWSE) {
            return new Transition(route, Effect.NONE);
        }
        return new Transition(browseHome(), Effect.RETURN_TO_BROWSE);
    }

    static boolean shouldShowHomeChromeBack(RouteState state) {
        return normalize(state).surface != Surface.BROWSE;
    }

    static Transition openPhoneTab(RouteState state, BottomTabDestination destination) {
        RouteState route = normalize(state);
        switch (destination == null ? BottomTabDestination.HOME : destination) {
            case SEARCH:
                return new Transition(
                    new RouteState(route.surface, BottomTabDestination.HOME, false),
                    Effect.FOCUS_SEARCH_INPUT
                );
            case ASK:
                return new Transition(
                    new RouteState(route.surface, BottomTabDestination.ASK, true),
                    Effect.FOCUS_ASK_INPUT
                );
            case THREADS:
                return new Transition(
                    new RouteState(Surface.BROWSE, BottomTabDestination.ASK, false),
                    Effect.SHOW_RECENT_THREADS
                );
            case PINS:
                return new Transition(
                    new RouteState(Surface.BROWSE, BottomTabDestination.PINS, false),
                    Effect.SHOW_SAVED_GUIDES
                );
            case HOME:
            default:
                return returnHome(route);
        }
    }

    static Transition enterSearch(RouteState state) {
        normalize(state);
        return new Transition(
            new RouteState(Surface.SEARCH_RESULTS, BottomTabDestination.HOME, false),
            Effect.FOCUS_SEARCH_INPUT
        );
    }

    static Transition enterAsk(RouteState state) {
        normalize(state);
        return new Transition(
            new RouteState(Surface.ASK_RESULTS, BottomTabDestination.ASK, true),
            Effect.FOCUS_ASK_INPUT
        );
    }

    static Transition returnHome(RouteState state) {
        normalize(state);
        return new Transition(browseHome(), Effect.SHOW_BROWSE_HOME);
    }

    static RouteState routeStateForMode(
        boolean browseMode,
        BottomTabDestination activePhoneTab,
        boolean askLaneActive
    ) {
        Surface surface = browseMode
            ? Surface.BROWSE
            : (askLaneActive || activePhoneTab == BottomTabDestination.ASK
                ? Surface.ASK_RESULTS
                : Surface.SEARCH_RESULTS);
        return new RouteState(surface, activePhoneTab, askLaneActive);
    }

    static BottomTabDestination phoneTabSelectionOwner(BottomTabDestination destination) {
        if (destination == null) {
            return BottomTabDestination.HOME;
        }
        switch (destination) {
            case SEARCH:
                return BottomTabDestination.HOME;
            case THREADS:
                return BottomTabDestination.ASK;
            case PINS:
                return BottomTabDestination.PINS;
            case ASK:
                return BottomTabDestination.ASK;
            case HOME:
            default:
                return BottomTabDestination.HOME;
        }
    }

    private static RouteState normalize(RouteState state) {
        return state == null ? browseHome() : state.normalized();
    }

    static final class RouteState {
        final Surface surface;
        final BottomTabDestination activePhoneTab;
        final boolean askLaneActive;

        RouteState(Surface surface, BottomTabDestination activePhoneTab, boolean askLaneActive) {
            this.surface = surface == null ? Surface.BROWSE : surface;
            this.activePhoneTab = phoneTabSelectionOwner(activePhoneTab);
            this.askLaneActive = askLaneActive;
        }

        private RouteState normalized() {
            return new RouteState(surface, activePhoneTab, askLaneActive);
        }
    }

    static final class Transition {
        final RouteState routeState;
        final Effect effect;

        Transition(RouteState routeState, Effect effect) {
            this.routeState = normalize(routeState);
            this.effect = effect == null ? Effect.NONE : effect;
        }
    }
}
