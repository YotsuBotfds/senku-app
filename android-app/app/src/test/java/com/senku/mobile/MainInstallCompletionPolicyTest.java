package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.senku.ui.primitives.BottomTabDestination;

import org.junit.Test;

public final class MainInstallCompletionPolicyTest {
    @Test
    public void browseCompletionPlansGuidePublicationOnCurrentBrowseSurface() {
        MainRouteDecisionHelper.RouteState savedRoute = new MainRouteDecisionHelper.RouteState(
            MainRouteDecisionHelper.Surface.SAVED_GUIDES,
            BottomTabDestination.PINS,
            false
        );

        MainInstallCompletionPolicy.CompletionPlan plan =
            MainInstallCompletionPolicy.plan(false, savedRoute);

        assertEquals(MainInstallCompletionPolicy.Action.PUBLISH_BROWSE_GUIDES, plan.action());
        assertTrue(plan.shouldPublishBrowseGuides());
        assertEquals("", plan.browsePublication().highlightQuery());
        assertRouteState(
            plan.browsePublication().resultItemsPresentation().routeState(),
            MainRouteDecisionHelper.Surface.SAVED_GUIDES,
            BottomTabDestination.PINS,
            false
        );
    }

    @Test
    public void autoQueryPreservesCurrentBrowsePayload() {
        MainInstallCompletionPolicy.CompletionPlan plan =
            MainInstallCompletionPolicy.plan(true, MainRouteDecisionHelper.browseHome());

        assertEquals(MainInstallCompletionPolicy.Action.PRESERVE_CURRENT_RESULTS, plan.action());
        assertFalse(plan.shouldPublishBrowseGuides());
        assertNull(plan.browsePublication());
    }

    @Test
    public void resultRoutesPreserveCurrentPayloadWithoutBrowsePublication() {
        MainRouteDecisionHelper.RouteState searchRoute = new MainRouteDecisionHelper.RouteState(
            MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
            BottomTabDestination.HOME,
            false
        );

        MainInstallCompletionPolicy.CompletionPlan plan =
            MainInstallCompletionPolicy.plan(false, searchRoute);

        assertEquals(MainInstallCompletionPolicy.Action.PRESERVE_CURRENT_RESULTS, plan.action());
        assertFalse(plan.shouldPublishBrowseGuides());
        assertNull(plan.browsePublication());
    }

    private static void assertRouteState(
        MainRouteDecisionHelper.RouteState routeState,
        MainRouteDecisionHelper.Surface surface,
        BottomTabDestination activePhoneTab,
        boolean askLaneActive
    ) {
        assertEquals(surface, routeState.surface);
        assertEquals(activePhoneTab, routeState.activePhoneTab);
        assertEquals(askLaneActive, routeState.askLaneActive);
    }
}
