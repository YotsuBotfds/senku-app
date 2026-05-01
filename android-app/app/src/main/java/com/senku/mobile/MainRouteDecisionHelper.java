package com.senku.mobile;

import com.senku.ui.primitives.BottomTabDestination;

public final class MainRouteDecisionHelper {
    static final String STATE_PHONE_TAB = "phone_tab";
    static final String STATE_MAIN_ROUTE_SURFACE = "main_route_surface";
    static final String STATE_MAIN_ROUTE_ACTIVE_TAB = "main_route_active_tab";
    static final String STATE_MAIN_ROUTE_ASK_LANE_ACTIVE = "main_route_ask_lane_active";

    enum Surface {
        BROWSE,
        RECENT_THREADS,
        SAVED_GUIDES,
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
        if (isBrowseSurface(route.surface) && route.activePhoneTab != BottomTabDestination.HOME) {
            BottomTabDestination previous = phoneTabSelectionOwner(previousPhoneTab);
            return new Transition(
                routeForBrowseDestination(previous == route.activePhoneTab ? BottomTabDestination.HOME : previous),
                Effect.SHOW_PREVIOUS_TAB
            );
        }
        if (!isBrowseSurface(route.surface)) {
            return new Transition(browseHome(), Effect.RETURN_TO_BROWSE);
        }
        return new Transition(route, Effect.EXIT_ACTIVITY);
    }

    static Transition homeChromeBack(RouteState state) {
        RouteState route = normalize(state);
        if (isBrowseSurface(route.surface)) {
            return new Transition(route, Effect.NONE);
        }
        return systemBack(route, null);
    }

    static boolean shouldShowHomeChromeBack(RouteState state) {
        return !isBrowseSurface(normalize(state).surface);
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
                    routeForBrowseDestination(BottomTabDestination.THREADS),
                    Effect.SHOW_RECENT_THREADS
                );
            case PINS:
                return new Transition(
                    routeForBrowseDestination(BottomTabDestination.PINS),
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

    static RouteState askUnavailableOrNoSourceFailure() {
        return new RouteState(Surface.RECENT_THREADS, BottomTabDestination.ASK, false);
    }

    static Transition returnHome(RouteState state) {
        normalize(state);
        return new Transition(browseHome(), Effect.SHOW_BROWSE_HOME);
    }

    static Transition openHomeIntent(boolean openHomeExtra, RouteState state) {
        RouteState route = normalize(state);
        if (!openHomeExtra) {
            return new Transition(route, Effect.NONE);
        }
        return returnHome(route);
    }

    static Transition openSavedIntent(boolean openSavedExtra, RouteState state) {
        RouteState route = normalize(state);
        if (!openSavedExtra) {
            return new Transition(route, Effect.NONE);
        }
        return openPhoneTab(route, BottomTabDestination.PINS);
    }

    static boolean shouldPublishInstalledBrowseGuides(boolean autoQueryPending, RouteState state) {
        return !autoQueryPending && isBrowseSurface(normalize(state).surface);
    }

    static RouteState routeStateForMode(
        boolean browseMode,
        BottomTabDestination activePhoneTab,
        boolean askLaneActive
    ) {
        Surface surface = browseMode
            ? browseSurfaceForPhoneTab(activePhoneTab)
            : (askLaneActive || activePhoneTab == BottomTabDestination.ASK
                ? Surface.ASK_RESULTS
                : Surface.SEARCH_RESULTS);
        return new RouteState(surface, activePhoneTab, askLaneActive);
    }

    static RouteState routeStateForExplicitFlowDestination(
        RouteState currentState,
        BottomTabDestination destination
    ) {
        RouteState route = normalize(currentState);
        BottomTabDestination activeTab = phoneTabSelectionOwner(destination);
        boolean explicitAskLaneActive = AskSearchCoordinator.resolveAskLaneActiveForExplicitPhoneFlow(
            destination,
            route.askLaneActive
        );
        return routeStateForMode(isBrowseSurface(route.surface), activeTab, explicitAskLaneActive);
    }

    static EncodedRouteState encodeRouteState(RouteState routeState) {
        RouteState safeRouteState = normalize(routeState);
        return new EncodedRouteState(
            safeRouteState.surface.name(),
            safeRouteState.activePhoneTab.name(),
            safeRouteState.askLaneActive
        );
    }

    static RouteState resolveRestoredMainRouteState(
        String rawSurface,
        String rawActivePhoneTab,
        boolean askLaneActive,
        boolean hasFirstClassRouteState,
        String legacyPhoneTab
    ) {
        if (hasFirstClassRouteState) {
            Surface surface = parseRouteSurface(rawSurface);
            BottomTabDestination activeTab = parsePhoneTab(rawActivePhoneTab);
            if (surface != null && activeTab != null) {
                return new RouteState(surface, activeTab, askLaneActive);
            }
        }
        BottomTabDestination legacyTab = resolveRestoredPhoneTab(legacyPhoneTab);
        return routeStateForMode(true, legacyTab, legacyTab == BottomTabDestination.ASK);
    }

    static boolean resolveInitialBrowseChromeVisible(
        boolean autoQueryPending,
        String rawSurface,
        String rawActivePhoneTab,
        boolean askLaneActive,
        boolean hasFirstClassRouteState,
        String legacyPhoneTab
    ) {
        return !autoQueryPending && isBrowseSurface(resolveRestoredMainRouteState(
            rawSurface,
            rawActivePhoneTab,
            askLaneActive,
            hasFirstClassRouteState,
            legacyPhoneTab
        ).surface);
    }

    static BottomTabDestination resolveRestoredPhoneTab(String rawValue) {
        BottomTabDestination destination = parsePhoneTab(rawValue);
        return destination == null ? BottomTabDestination.HOME : phoneTabSelectionOwner(destination);
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

    private static RouteState routeForBrowseDestination(BottomTabDestination destination) {
        return new RouteState(browseSurfaceForPhoneTab(destination), destination, false);
    }

    private static Surface browseSurfaceForPhoneTab(BottomTabDestination destination) {
        BottomTabDestination owner = phoneTabSelectionOwner(destination);
        switch (owner) {
            case ASK:
                return Surface.RECENT_THREADS;
            case PINS:
                return Surface.SAVED_GUIDES;
            case HOME:
            default:
                return Surface.BROWSE;
        }
    }

    static boolean isBrowseSurface(Surface surface) {
        return surface == Surface.BROWSE
            || surface == Surface.RECENT_THREADS
            || surface == Surface.SAVED_GUIDES;
    }

    static Surface parseRouteSurface(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return null;
        }
        try {
            return Surface.valueOf(rawValue);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    static BottomTabDestination parsePhoneTab(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return null;
        }
        try {
            return BottomTabDestination.valueOf(rawValue);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static RouteState normalize(RouteState state) {
        return state == null ? browseHome() : state.normalized();
    }

    static final class EncodedRouteState {
        final String surface;
        final String activePhoneTab;
        final boolean askLaneActive;

        EncodedRouteState(String surface, String activePhoneTab, boolean askLaneActive) {
            this.surface = surface;
            this.activePhoneTab = activePhoneTab;
            this.askLaneActive = askLaneActive;
        }
    }

    static final class RouteState {
        final Surface surface;
        final BottomTabDestination activePhoneTab;
        final boolean askLaneActive;

        RouteState(Surface surface, BottomTabDestination activePhoneTab, boolean askLaneActive) {
            Surface safeSurface = surface == null ? Surface.BROWSE : surface;
            BottomTabDestination owner = phoneTabSelectionOwner(activePhoneTab);
            this.surface = isBrowseSurface(safeSurface) ? browseSurfaceForPhoneTab(owner) : safeSurface;
            this.activePhoneTab = owner;
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
