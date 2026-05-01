package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class MainAutomationIntentPolicyTest {
    @Test
    public void decodeQueryPreservesPlusAndDecodesPercentEscapes() {
        assertEquals(
            "boil water + filter",
            MainAutomationIntentPolicy.decodeQuery("boil%20water%20+%20filter")
        );
        assertEquals("caf\u00E9 stove", MainAutomationIntentPolicy.decodeQuery("caf%C3%A9%20stove"));
        assertNull(MainAutomationIntentPolicy.decodeQuery(null));
    }

    @Test
    public void applyDecisionOpensEmptyAskLaneForAutoAskWithoutQuery() {
        MainAutomationIntentPolicy.ApplyDecision nullQuery = MainAutomationIntentPolicy.resolveApply(
            MainAutomationIntentPolicy.IntentState.fromEncoded(null, true, null)
        );
        MainAutomationIntentPolicy.ApplyDecision blankQuery = MainAutomationIntentPolicy.resolveApply(
            MainAutomationIntentPolicy.IntentState.fromEncoded("%20%20", true, null)
        );

        assertEquals(MainAutomationIntentPolicy.ApplyEffect.OPEN_EMPTY_ASK_LANE, nullQuery.effect);
        assertEquals(MainAutomationIntentPolicy.Route.ASK, nullQuery.route);
        assertEquals(MainAutomationIntentPolicy.ApplyEffect.OPEN_EMPTY_ASK_LANE, blankQuery.effect);
        assertTrue(MainAutomationIntentPolicy.shouldOpenEmptyAutoAskLane(null, true));
        assertTrue(MainAutomationIntentPolicy.shouldOpenEmptyAutoAskLane("   ", true));
    }

    @Test
    public void applyDecisionSetsTrimmedQueryAndRoute() {
        MainAutomationIntentPolicy.ApplyDecision askDecision = MainAutomationIntentPolicy.resolveApply(
            MainAutomationIntentPolicy.IntentState.fromEncoded("%20water%20storage%20", true, null)
        );
        MainAutomationIntentPolicy.ApplyDecision searchDecision = MainAutomationIntentPolicy.resolveApply(
            MainAutomationIntentPolicy.IntentState.fromEncoded("winter%20shelter", false, null)
        );

        assertEquals(MainAutomationIntentPolicy.ApplyEffect.SET_QUERY, askDecision.effect);
        assertEquals("water storage", askDecision.query);
        assertEquals(MainAutomationIntentPolicy.Route.ASK, askDecision.route);
        assertEquals(MainAutomationIntentPolicy.ApplyEffect.SET_QUERY, searchDecision.effect);
        assertEquals("winter shelter", searchDecision.query);
        assertEquals(MainAutomationIntentPolicy.Route.SEARCH, searchDecision.route);
    }

    @Test
    public void automationNoOpsWhenAlreadyHandledOrNoRunnableQuery() {
        assertEquals(
            MainAutomationIntentPolicy.AutomationEffect.NONE,
            MainAutomationIntentPolicy.resolveAutomation(
                MainAutomationIntentPolicy.IntentState.fromEncoded("water", false, null),
                true,
                true
            ).effect
        );
        assertEquals(
            MainAutomationIntentPolicy.AutomationEffect.NONE,
            MainAutomationIntentPolicy.resolveAutomation(
                MainAutomationIntentPolicy.IntentState.fromEncoded(null, false, null),
                false,
                true
            ).effect
        );
        assertEquals(
            MainAutomationIntentPolicy.AutomationEffect.NONE,
            MainAutomationIntentPolicy.resolveAutomation(
                MainAutomationIntentPolicy.IntentState.fromEncoded("%20%20", false, null),
                false,
                true
            ).effect
        );
    }

    @Test
    public void automationEmptyAutoAskMarksHandledAndSuppressesFocus() {
        MainAutomationIntentPolicy.AutomationDecision decision = MainAutomationIntentPolicy.resolveAutomation(
            MainAutomationIntentPolicy.IntentState.fromEncoded(null, true, "ignored%20followup"),
            false,
            false
        );

        assertEquals(MainAutomationIntentPolicy.AutomationEffect.OPEN_EMPTY_ASK_LANE, decision.effect);
        assertEquals("", decision.query);
        assertNull(decision.followUpQuery);
        assertEquals(MainAutomationIntentPolicy.Route.ASK, decision.route);
        assertTrue(decision.markHandled);
        assertTrue(decision.suppressSearchFocus);
    }

    @Test
    public void automationBeforeRepositoryReadyPreparesQueryWithoutMarkingHandled() {
        MainAutomationIntentPolicy.AutomationDecision decision = MainAutomationIntentPolicy.resolveAutomation(
            MainAutomationIntentPolicy.IntentState.fromEncoded(
                "%20water%20storage%20",
                true,
                "what%20comes%20next"
            ),
            false,
            false
        );

        assertEquals(
            MainAutomationIntentPolicy.AutomationEffect.PREPARE_QUERY_WAITING_FOR_REPOSITORY,
            decision.effect
        );
        assertEquals("water storage", decision.query);
        assertEquals("what comes next", decision.followUpQuery);
        assertEquals(MainAutomationIntentPolicy.Route.ASK, decision.route);
        assertFalse(decision.markHandled);
        assertFalse(decision.suppressSearchFocus);
    }

    @Test
    public void delayedAutoAskRunsOnlyAfterRepositoryReady() {
        MainAutomationIntentPolicy.IntentState intentState =
            MainAutomationIntentPolicy.IntentState.fromEncoded("boil%20water", true, "then%20cool");

        MainAutomationIntentPolicy.AutomationDecision waitingDecision =
            MainAutomationIntentPolicy.resolveAutomation(intentState, false, false);
        MainAutomationIntentPolicy.AutomationDecision readyDecision =
            MainAutomationIntentPolicy.resolveAutomation(intentState, false, true);

        assertEquals(
            MainAutomationIntentPolicy.AutomationEffect.PREPARE_QUERY_WAITING_FOR_REPOSITORY,
            waitingDecision.effect
        );
        assertEquals("boil water", waitingDecision.query);
        assertEquals("then cool", waitingDecision.followUpQuery);
        assertEquals(MainAutomationIntentPolicy.Route.ASK, waitingDecision.route);
        assertFalse(waitingDecision.markHandled);
        assertFalse(waitingDecision.suppressSearchFocus);

        assertRunDecision(readyDecision, "boil water", "then cool", MainAutomationIntentPolicy.Route.ASK);
    }

    @Test
    public void automationRunsSearchOrAskWhenRepositoryReady() {
        MainAutomationIntentPolicy.AutomationDecision askDecision = MainAutomationIntentPolicy.resolveAutomation(
            MainAutomationIntentPolicy.IntentState.fromEncoded("boil%20water", true, "continue"),
            false,
            true
        );
        MainAutomationIntentPolicy.AutomationDecision searchDecision = MainAutomationIntentPolicy.resolveAutomation(
            MainAutomationIntentPolicy.IntentState.fromEncoded("shelter", false, null),
            false,
            true
        );

        assertRunDecision(askDecision, "boil water", "continue", MainAutomationIntentPolicy.Route.ASK);
        assertRunDecision(searchDecision, "shelter", null, MainAutomationIntentPolicy.Route.SEARCH);
    }

    @Test
    public void hasAutoQueryRequiresNonBlankDecodedQuery() {
        assertTrue(MainAutomationIntentPolicy.hasAutoQuery(
            MainAutomationIntentPolicy.IntentState.fromEncoded("water", false, null)
        ));
        assertFalse(MainAutomationIntentPolicy.hasAutoQuery(
            MainAutomationIntentPolicy.IntentState.fromEncoded("%20%20", true, null)
        ));
        assertFalse(MainAutomationIntentPolicy.hasAutoQuery(null));
    }

    private static void assertRunDecision(
        MainAutomationIntentPolicy.AutomationDecision decision,
        String query,
        String followUpQuery,
        MainAutomationIntentPolicy.Route route
    ) {
        assertEquals(MainAutomationIntentPolicy.AutomationEffect.RUN_QUERY, decision.effect);
        assertEquals(query, decision.query);
        assertEquals(followUpQuery, decision.followUpQuery);
        assertEquals(route, decision.route);
        assertTrue(decision.markHandled);
        assertTrue(decision.suppressSearchFocus);
    }
}
