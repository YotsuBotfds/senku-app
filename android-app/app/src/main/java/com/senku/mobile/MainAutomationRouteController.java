package com.senku.mobile;

import com.senku.ui.primitives.BottomTabDestination;

final class MainAutomationRouteController {
    interface Host {
        void setSharedQuery(String query);

        void setPhoneTabFromFlow(BottomTabDestination destination);

        void showBrowseChrome(boolean show);

        void enterAskResultsRoute();

        void enterSearchResultsRoute();

        void runAsk(String query);

        void runSearch(String query);

        void setPendingAutoFollowUpQuery(String query);

        void setSuppressSearchFocusForAutomation(boolean suppressSearchFocus);

        void markAutoIntentHandled(boolean handled);

        void openEmptyAskLane(boolean focusInput);
    }

    private final Host host;

    MainAutomationRouteController(Host host) {
        this.host = host;
    }

    void applyIntentQuery(MainAutomationIntentPolicy.IntentState intentState) {
        MainAutomationIntentPolicy.ApplyDecision decision =
            MainAutomationIntentPolicy.resolveApply(intentState);
        if (decision.effect == MainAutomationIntentPolicy.ApplyEffect.OPEN_EMPTY_ASK_LANE) {
            host.openEmptyAskLane(false);
            return;
        }
        if (decision.effect == MainAutomationIntentPolicy.ApplyEffect.SET_QUERY) {
            host.setSharedQuery(decision.query);
            host.setPhoneTabFromFlow(phoneTabForAutomationRoute(decision.route));
            host.showBrowseChrome(false);
        }
    }

    void maybeHandleAutomation(
        MainAutomationIntentPolicy.IntentState intentState,
        boolean autoIntentHandled,
        boolean repositoryReady
    ) {
        MainAutomationIntentPolicy.AutomationDecision decision =
            MainAutomationIntentPolicy.resolveAutomation(intentState, autoIntentHandled, repositoryReady);
        if (decision.effect == MainAutomationIntentPolicy.AutomationEffect.NONE) {
            return;
        }
        if (decision.effect == MainAutomationIntentPolicy.AutomationEffect.OPEN_EMPTY_ASK_LANE) {
            host.markAutoIntentHandled(decision.markHandled);
            host.setSuppressSearchFocusForAutomation(decision.suppressSearchFocus);
            host.showBrowseChrome(true);
            host.openEmptyAskLane(true);
            return;
        }

        host.setSharedQuery(decision.query);
        host.setPendingAutoFollowUpQuery(decision.followUpQuery);
        host.showBrowseChrome(false);
        if (decision.effect == MainAutomationIntentPolicy.AutomationEffect.PREPARE_QUERY_WAITING_FOR_REPOSITORY) {
            return;
        }

        host.markAutoIntentHandled(decision.markHandled);
        host.setSuppressSearchFocusForAutomation(decision.suppressSearchFocus);
        if (decision.route == MainAutomationIntentPolicy.Route.ASK) {
            host.enterAskResultsRoute();
            host.runAsk(decision.query);
        } else {
            host.enterSearchResultsRoute();
            host.runSearch(decision.query);
        }
    }

    static BottomTabDestination phoneTabForAutomationRoute(MainAutomationIntentPolicy.Route route) {
        return route == MainAutomationIntentPolicy.Route.ASK
            ? BottomTabDestination.ASK
            : BottomTabDestination.SEARCH;
    }
}
