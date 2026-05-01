package com.senku.mobile;

final class MainInstallCompletionPolicy {
    private MainInstallCompletionPolicy() {
    }

    static Action resolve(boolean autoQueryPending, MainRouteDecisionHelper.RouteState routeState) {
        return plan(autoQueryPending, routeState).action();
    }

    static CompletionPlan plan(boolean autoQueryPending, MainRouteDecisionHelper.RouteState routeState) {
        boolean shouldPublishBrowseGuides =
            MainRouteDecisionHelper.shouldPublishInstalledBrowseGuides(autoQueryPending, routeState);
        if (!shouldPublishBrowseGuides) {
            return new CompletionPlan(Action.PRESERVE_CURRENT_RESULTS, null);
        }
        return new CompletionPlan(
            Action.PUBLISH_BROWSE_GUIDES,
            MainResultPublicationPolicy.browseSurface(routeState)
        );
    }

    enum Action {
        PUBLISH_BROWSE_GUIDES,
        PRESERVE_CURRENT_RESULTS
    }

    static final class CompletionPlan {
        private final Action action;
        private final MainResultPublicationPolicy browsePublication;

        private CompletionPlan(Action action, MainResultPublicationPolicy browsePublication) {
            this.action = action == null ? Action.PRESERVE_CURRENT_RESULTS : action;
            this.browsePublication = browsePublication;
        }

        Action action() {
            return action;
        }

        boolean shouldPublishBrowseGuides() {
            return action == Action.PUBLISH_BROWSE_GUIDES && browsePublication != null;
        }

        MainResultPublicationPolicy browsePublication() {
            return browsePublication;
        }
    }
}
