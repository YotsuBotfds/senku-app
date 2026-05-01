package com.senku.mobile;

import com.senku.ui.primitives.BottomTabDestination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class MainNavigationTabPolicy {
    private MainNavigationTabPolicy() {
    }

    static BottomTabDestination displayedActiveDestination(MainRouteDecisionHelper.RouteState routeState) {
        return MainRouteDecisionHelper.displayedPhoneTab(routeState);
    }

    static TabDisplayModel modelForSlot(
        BottomTabDestination slotDestination,
        MainRouteDecisionHelper.RouteState routeState
    ) {
        BottomTabDestination slot = slotDestination == null ? BottomTabDestination.HOME : slotDestination;
        BottomTabDestination displayedDestination =
            MainRouteDecisionHelper.displayedPhoneTabSlot(slot, routeState);
        BottomTabDestination displayedActiveDestination = displayedActiveDestination(routeState);
        return new TabDisplayModel(
            slot,
            displayedDestination,
            displayedDestination == displayedActiveDestination,
            labelFor(displayedDestination),
            contentDescriptionFor(displayedDestination)
        );
    }

    static List<TabDisplayModel> modelsForSlots(
        List<BottomTabDestination> slotDestinations,
        MainRouteDecisionHelper.RouteState routeState
    ) {
        if (slotDestinations == null || slotDestinations.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<TabDisplayModel> models = new ArrayList<>(slotDestinations.size());
        for (BottomTabDestination destination : slotDestinations) {
            if (destination != null) {
                models.add(modelForSlot(destination, routeState));
            }
        }
        return models;
    }

    static String labelFor(BottomTabDestination destination) {
        switch (safeDestination(destination)) {
            case SEARCH:
                return "Search";
            case ASK:
                return "Ask";
            case THREADS:
                return "Threads";
            case PINS:
                return "Saved";
            case HOME:
            default:
                return "Library";
        }
    }

    static String contentDescriptionFor(BottomTabDestination destination) {
        switch (safeDestination(destination)) {
            case SEARCH:
                return "Search";
            case ASK:
                return "Ask the manual";
            case THREADS:
                return "Open recent threads";
            case PINS:
                return "Open saved guides";
            case HOME:
            default:
                return "Open Library";
        }
    }

    private static BottomTabDestination safeDestination(BottomTabDestination destination) {
        return destination == null ? BottomTabDestination.HOME : destination;
    }

    static final class TabDisplayModel {
        final BottomTabDestination slotDestination;
        final BottomTabDestination displayedDestination;
        final boolean selected;
        final String label;
        final String contentDescription;

        TabDisplayModel(
            BottomTabDestination slotDestination,
            BottomTabDestination displayedDestination,
            boolean selected,
            String label,
            String contentDescription
        ) {
            this.slotDestination = slotDestination == null ? BottomTabDestination.HOME : slotDestination;
            this.displayedDestination = displayedDestination == null ? BottomTabDestination.HOME : displayedDestination;
            this.selected = selected;
            this.label = label == null ? "" : label;
            this.contentDescription = contentDescription == null ? this.label : contentDescription;
        }
    }
}
