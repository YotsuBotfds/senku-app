package com.senku.mobile;

final class MainInstallCompletionPolicy {
    private MainInstallCompletionPolicy() {
    }

    static Action resolve(boolean autoQueryPending, MainRouteDecisionHelper.RouteState routeState) {
        return MainRouteDecisionHelper.shouldPublishInstalledBrowseGuides(autoQueryPending, routeState)
            ? Action.PUBLISH_BROWSE_GUIDES
            : Action.PRESERVE_CURRENT_RESULTS;
    }

    enum Action {
        PUBLISH_BROWSE_GUIDES,
        PRESERVE_CURRENT_RESULTS
    }
}
