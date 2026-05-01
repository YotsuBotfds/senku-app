package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.senku.ui.primitives.BottomTabDestination;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public final class MainAutomationRouteControllerTest {
    @Test
    public void applyIntentQuerySetsQueryTabAndChromeForSearchRoute() {
        RecordingHost host = new RecordingHost();
        MainAutomationRouteController controller = new MainAutomationRouteController(host);

        controller.applyIntentQuery(
            MainAutomationIntentPolicy.IntentState.fromEncoded("%20winter%20shelter%20", false, null)
        );

        assertEquals("winter shelter", host.sharedQuery);
        assertEquals(BottomTabDestination.SEARCH, host.phoneTab);
        assertFalse(host.browseChromeVisible);
        assertEquals("setSharedQuery:winter shelter", host.events.get(0));
        assertEquals("setPhoneTab:SEARCH", host.events.get(1));
        assertEquals("showBrowseChrome:false", host.events.get(2));
    }

    @Test
    public void applyIntentQueryOpensEmptyAskLaneWithoutChangingChrome() {
        RecordingHost host = new RecordingHost();
        MainAutomationRouteController controller = new MainAutomationRouteController(host);

        controller.applyIntentQuery(
            MainAutomationIntentPolicy.IntentState.fromEncoded(null, true, null)
        );

        assertTrue(host.emptyAskLaneOpened);
        assertFalse(host.emptyAskLaneFocused);
        assertEquals(1, host.events.size());
        assertEquals("openEmptyAskLane:false", host.events.get(0));
    }

    @Test
    public void automationBeforeRepositoryReadyPreparesQueryOnly() {
        RecordingHost host = new RecordingHost();
        MainAutomationRouteController controller = new MainAutomationRouteController(host);

        controller.maybeHandleAutomation(
            MainAutomationIntentPolicy.IntentState.fromEncoded("boil%20water", true, "then%20cool"),
            false,
            false
        );

        assertEquals("boil water", host.sharedQuery);
        assertEquals("then cool", host.pendingAutoFollowUpQuery);
        assertFalse(host.browseChromeVisible);
        assertFalse(host.autoIntentHandled);
        assertFalse(host.suppressSearchFocusForAutomation);
        assertFalse(host.askRun);
        assertEquals("showBrowseChrome:false", host.events.get(2));
    }

    @Test
    public void automationRunsAskRouteWhenRepositoryReady() {
        RecordingHost host = new RecordingHost();
        MainAutomationRouteController controller = new MainAutomationRouteController(host);

        controller.maybeHandleAutomation(
            MainAutomationIntentPolicy.IntentState.fromEncoded("boil%20water", true, "then%20cool"),
            false,
            true
        );

        assertEquals("boil water", host.sharedQuery);
        assertEquals("then cool", host.pendingAutoFollowUpQuery);
        assertTrue(host.autoIntentHandled);
        assertTrue(host.suppressSearchFocusForAutomation);
        assertTrue(host.askResultsRouteEntered);
        assertTrue(host.askRun);
        assertEquals("boil water", host.runQuery);
        assertEquals("enterAskResultsRoute", host.events.get(5));
        assertEquals("runAsk:boil water", host.events.get(6));
    }

    @Test
    public void automationRunsSearchRouteWhenRepositoryReady() {
        RecordingHost host = new RecordingHost();
        MainAutomationRouteController controller = new MainAutomationRouteController(host);

        controller.maybeHandleAutomation(
            MainAutomationIntentPolicy.IntentState.fromEncoded("shelter", false, null),
            false,
            true
        );

        assertEquals("shelter", host.sharedQuery);
        assertNull(host.pendingAutoFollowUpQuery);
        assertTrue(host.autoIntentHandled);
        assertTrue(host.searchResultsRouteEntered);
        assertTrue(host.searchRun);
        assertEquals("shelter", host.runQuery);
    }

    @Test
    public void automationOpensEmptyAskLaneAndMarksHandled() {
        RecordingHost host = new RecordingHost();
        MainAutomationRouteController controller = new MainAutomationRouteController(host);

        controller.maybeHandleAutomation(
            MainAutomationIntentPolicy.IntentState.fromEncoded(null, true, "ignored"),
            false,
            false
        );

        assertTrue(host.autoIntentHandled);
        assertTrue(host.suppressSearchFocusForAutomation);
        assertTrue(host.browseChromeVisible);
        assertTrue(host.emptyAskLaneOpened);
        assertTrue(host.emptyAskLaneFocused);
        assertEquals("markAutoIntentHandled:true", host.events.get(0));
        assertEquals("setSuppressSearchFocus:true", host.events.get(1));
        assertEquals("showBrowseChrome:true", host.events.get(2));
        assertEquals("openEmptyAskLane:true", host.events.get(3));
    }

    private static final class RecordingHost implements MainAutomationRouteController.Host {
        final List<String> events = new ArrayList<>();
        String sharedQuery = "";
        BottomTabDestination phoneTab;
        boolean browseChromeVisible = true;
        String pendingAutoFollowUpQuery;
        boolean suppressSearchFocusForAutomation;
        boolean autoIntentHandled;
        boolean emptyAskLaneOpened;
        boolean emptyAskLaneFocused;
        boolean askResultsRouteEntered;
        boolean searchResultsRouteEntered;
        boolean askRun;
        boolean searchRun;
        String runQuery = "";

        @Override
        public void setSharedQuery(String query) {
            sharedQuery = query;
            events.add("setSharedQuery:" + query);
        }

        @Override
        public void setPhoneTabFromFlow(BottomTabDestination destination) {
            phoneTab = destination;
            events.add("setPhoneTab:" + destination);
        }

        @Override
        public void showBrowseChrome(boolean show) {
            browseChromeVisible = show;
            events.add("showBrowseChrome:" + show);
        }

        @Override
        public void enterAskResultsRoute() {
            askResultsRouteEntered = true;
            events.add("enterAskResultsRoute");
        }

        @Override
        public void enterSearchResultsRoute() {
            searchResultsRouteEntered = true;
            events.add("enterSearchResultsRoute");
        }

        @Override
        public void runAsk(String query) {
            askRun = true;
            runQuery = query;
            events.add("runAsk:" + query);
        }

        @Override
        public void runSearch(String query) {
            searchRun = true;
            runQuery = query;
            events.add("runSearch:" + query);
        }

        @Override
        public void setPendingAutoFollowUpQuery(String query) {
            pendingAutoFollowUpQuery = query;
            events.add("setPendingAutoFollowUpQuery:" + query);
        }

        @Override
        public void setSuppressSearchFocusForAutomation(boolean suppressSearchFocus) {
            suppressSearchFocusForAutomation = suppressSearchFocus;
            events.add("setSuppressSearchFocus:" + suppressSearchFocus);
        }

        @Override
        public void markAutoIntentHandled(boolean handled) {
            autoIntentHandled = handled;
            events.add("markAutoIntentHandled:" + handled);
        }

        @Override
        public void openEmptyAskLane(boolean focusInput) {
            emptyAskLaneOpened = true;
            emptyAskLaneFocused = focusInput;
            events.add("openEmptyAskLane:" + focusInput);
        }
    }
}
