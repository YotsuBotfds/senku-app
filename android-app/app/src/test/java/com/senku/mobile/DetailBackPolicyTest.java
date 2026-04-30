package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class DetailBackPolicyTest {
    @Test
    public void nonTaskRootBackFinishesActivityForEveryCurrentRouteAndTrigger() {
        assertCurrentMatrixDecision(false, DetailBackPolicy.Effect.FINISH_ACTIVITY);
    }

    @Test
    public void taskRootBackFallsBackToHomeForEveryCurrentRouteAndTrigger() {
        assertCurrentMatrixDecision(true, DetailBackPolicy.Effect.NAVIGATE_HOME);
    }

    @Test
    public void legacyTaskRootBooleanOverloadMatchesDefaultSystemBackRoute() {
        DetailBackPolicy.Decision stacked = DetailBackPolicy.decide(false);
        DetailBackPolicy.Decision explicitStacked = DetailBackPolicy.decide(
            new DetailBackPolicy.Inputs(
                false,
                DetailBackPolicy.SourceRoute.UNKNOWN,
                DetailBackPolicy.BackTrigger.SYSTEM_BACK
            )
        );
        DetailBackPolicy.Decision taskRoot = DetailBackPolicy.decide(true);
        DetailBackPolicy.Decision explicitTaskRoot = DetailBackPolicy.decide(
            new DetailBackPolicy.Inputs(
                true,
                DetailBackPolicy.SourceRoute.UNKNOWN,
                DetailBackPolicy.BackTrigger.SYSTEM_BACK
            )
        );

        assertEquals(explicitStacked.effect, stacked.effect);
        assertEquals(explicitStacked.finishBehavior, stacked.finishBehavior);
        assertEquals(explicitTaskRoot.effect, taskRoot.effect);
        assertEquals(explicitTaskRoot.finishBehavior, taskRoot.finishBehavior);
    }

    @Test
    public void sourceRouteIsClassifiedButDoesNotChangeBackEffect() {
        assertRouteEffect(false, DetailBackPolicy.SourceRoute.ANSWER, DetailBackPolicy.Effect.FINISH_ACTIVITY);
        assertRouteEffect(false, DetailBackPolicy.SourceRoute.GUIDE, DetailBackPolicy.Effect.FINISH_ACTIVITY);
        assertRouteEffect(false, DetailBackPolicy.SourceRoute.HOME_GUIDE, DetailBackPolicy.Effect.FINISH_ACTIVITY);
        assertRouteEffect(false, DetailBackPolicy.SourceRoute.CROSS_REFERENCE_GUIDE, DetailBackPolicy.Effect.FINISH_ACTIVITY);
        assertRouteEffect(true, DetailBackPolicy.SourceRoute.ANSWER, DetailBackPolicy.Effect.NAVIGATE_HOME);
        assertRouteEffect(true, DetailBackPolicy.SourceRoute.GUIDE, DetailBackPolicy.Effect.NAVIGATE_HOME);
        assertRouteEffect(true, DetailBackPolicy.SourceRoute.HOME_GUIDE, DetailBackPolicy.Effect.NAVIGATE_HOME);
        assertRouteEffect(true, DetailBackPolicy.SourceRoute.CROSS_REFERENCE_GUIDE, DetailBackPolicy.Effect.NAVIGATE_HOME);
    }

    @Test
    public void backTriggerIsClassifiedButDoesNotChangeBackEffect() {
        assertTriggerEffect(false, DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON, DetailBackPolicy.Effect.FINISH_ACTIVITY);
        assertTriggerEffect(false, DetailBackPolicy.BackTrigger.SYSTEM_BACK, DetailBackPolicy.Effect.FINISH_ACTIVITY);
        assertTriggerEffect(false, DetailBackPolicy.BackTrigger.SUPPORT_NAVIGATE_UP, DetailBackPolicy.Effect.FINISH_ACTIVITY);
        assertTriggerEffect(false, DetailBackPolicy.BackTrigger.GUIDE_RETURN, DetailBackPolicy.Effect.FINISH_ACTIVITY);
        assertTriggerEffect(true, DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON, DetailBackPolicy.Effect.NAVIGATE_HOME);
        assertTriggerEffect(true, DetailBackPolicy.BackTrigger.SYSTEM_BACK, DetailBackPolicy.Effect.NAVIGATE_HOME);
        assertTriggerEffect(true, DetailBackPolicy.BackTrigger.SUPPORT_NAVIGATE_UP, DetailBackPolicy.Effect.NAVIGATE_HOME);
        assertTriggerEffect(true, DetailBackPolicy.BackTrigger.GUIDE_RETURN, DetailBackPolicy.Effect.NAVIGATE_HOME);
    }

    @Test
    public void answerRouteVisibleBackAndSystemBackShareTaskRootPolicy() {
        assertCurrentClassifiedBackDecision(
            false,
            DetailBackPolicy.SourceRoute.ANSWER,
            DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON,
            DetailBackPolicy.Effect.FINISH_ACTIVITY
        );
        assertCurrentClassifiedBackDecision(
            false,
            DetailBackPolicy.SourceRoute.ANSWER,
            DetailBackPolicy.BackTrigger.SYSTEM_BACK,
            DetailBackPolicy.Effect.FINISH_ACTIVITY
        );
        assertCurrentClassifiedBackDecision(
            true,
            DetailBackPolicy.SourceRoute.ANSWER,
            DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON,
            DetailBackPolicy.Effect.NAVIGATE_HOME
        );
        assertCurrentClassifiedBackDecision(
            true,
            DetailBackPolicy.SourceRoute.ANSWER,
            DetailBackPolicy.BackTrigger.SYSTEM_BACK,
            DetailBackPolicy.Effect.NAVIGATE_HOME
        );
    }

    @Test
    public void emergencyAnswerSurfacesKeepExplicitBackAffordanceAndTaskRootFallback() {
        DetailBackPolicy.VisibleBackAffordance stacked = DetailBackPolicy.visibleBackAffordance(false);
        DetailBackPolicy.VisibleBackAffordance taskRoot = DetailBackPolicy.visibleBackAffordance(true);

        assertEquals(R.string.detail_back, stacked.labelResource);
        assertEquals(R.string.detail_back_content_description, stacked.contentDescriptionResource);
        assertFalse(stacked.longPressHomeShortcutEnabled);

        assertEquals(R.string.home_button, taskRoot.labelResource);
        assertEquals(R.string.detail_home_content_description, taskRoot.contentDescriptionResource);
        assertFalse(taskRoot.longPressHomeShortcutEnabled);

        assertCurrentClassifiedBackDecision(
            false,
            DetailBackPolicy.SourceRoute.ANSWER,
            DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON,
            DetailBackPolicy.Effect.FINISH_ACTIVITY
        );
        assertCurrentClassifiedBackDecision(
            true,
            DetailBackPolicy.SourceRoute.ANSWER,
            DetailBackPolicy.BackTrigger.SYSTEM_BACK,
            DetailBackPolicy.Effect.NAVIGATE_HOME
        );
    }

    @Test
    public void visibleBackAffordanceMatchesCurrentTaskRootLabels() {
        DetailBackPolicy.VisibleBackAffordance stacked = DetailBackPolicy.visibleBackAffordance(false);
        DetailBackPolicy.VisibleBackAffordance taskRoot = DetailBackPolicy.visibleBackAffordance(true);

        assertEquals(R.string.detail_back, stacked.labelResource);
        assertEquals(R.string.detail_back_content_description, stacked.contentDescriptionResource);
        assertFalse(stacked.longPressHomeShortcutEnabled);

        assertEquals(R.string.home_button, taskRoot.labelResource);
        assertEquals(R.string.detail_home_content_description, taskRoot.contentDescriptionResource);
        assertFalse(taskRoot.longPressHomeShortcutEnabled);
    }

    @Test
    public void routeAwareVisibleBackAffordanceNamesHomeWhenBackNavigatesHome() {
        for (DetailBackPolicy.SourceRoute sourceRoute : DetailBackPolicy.SourceRoute.values()) {
            for (DetailBackPolicy.BackTrigger trigger : DetailBackPolicy.BackTrigger.values()) {
                DetailBackPolicy.VisibleBackAffordance stacked = DetailBackPolicy.visibleBackAffordance(
                    new DetailBackPolicy.Inputs(false, sourceRoute, trigger)
                );
                DetailBackPolicy.VisibleBackAffordance taskRoot = DetailBackPolicy.visibleBackAffordance(
                    new DetailBackPolicy.Inputs(true, sourceRoute, trigger)
                );

                assertEquals(R.string.detail_back, stacked.labelResource);
                assertEquals(R.string.detail_back_content_description, stacked.contentDescriptionResource);
                assertFalse(stacked.longPressHomeShortcutEnabled);

                assertEquals(R.string.home_button, taskRoot.labelResource);
                assertEquals(R.string.detail_home_content_description, taskRoot.contentDescriptionResource);
                assertFalse(taskRoot.longPressHomeShortcutEnabled);
            }
        }
    }

    @Test
    public void nullAffordanceInputsNormalizeToStackedSystemBack() {
        DetailBackPolicy.VisibleBackAffordance affordance = DetailBackPolicy.visibleBackAffordance(
            (DetailBackPolicy.Inputs) null
        );

        assertEquals(R.string.detail_back, affordance.labelResource);
        assertEquals(R.string.detail_back_content_description, affordance.contentDescriptionResource);
        assertFalse(affordance.longPressHomeShortcutEnabled);
    }

    @Test
    public void fallbackPredicateIsTaskRootOnly() {
        assertTrue(DetailBackPolicy.shouldFallbackToHome(true));
        assertFalse(DetailBackPolicy.shouldFallbackToHome(false));
    }

    @Test
    public void nullInputsNormalizeToStackedSystemBack() {
        DetailBackPolicy.Decision decision = DetailBackPolicy.decide(null);
        DetailBackPolicy.Decision explicit = DetailBackPolicy.decide(
            new DetailBackPolicy.Inputs(
                false,
                DetailBackPolicy.SourceRoute.UNKNOWN,
                DetailBackPolicy.BackTrigger.SYSTEM_BACK
            )
        );

        assertEquals(explicit.effect, decision.effect);
        assertEquals(explicit.finishBehavior, decision.finishBehavior);
    }

    @Test
    public void nullRouteAndTriggerNormalizeToUnknownSystemBack() {
        DetailBackPolicy.Decision decision = DetailBackPolicy.decide(
            new DetailBackPolicy.Inputs(false, null, null)
        );

        assertEquals(DetailBackPolicy.Effect.FINISH_ACTIVITY, decision.effect);
        assertEquals(DetailBackPolicy.FinishBehavior.FINISH, decision.finishBehavior);
    }

    private static void assertCurrentMatrixDecision(
        boolean taskRoot,
        DetailBackPolicy.Effect expectedEffect
    ) {
        for (DetailBackPolicy.SourceRoute sourceRoute : DetailBackPolicy.SourceRoute.values()) {
            for (DetailBackPolicy.BackTrigger trigger : DetailBackPolicy.BackTrigger.values()) {
                assertCurrentClassifiedBackDecision(taskRoot, sourceRoute, trigger, expectedEffect);
            }
        }
    }

    private static void assertRouteEffect(
        boolean taskRoot,
        DetailBackPolicy.SourceRoute sourceRoute,
        DetailBackPolicy.Effect expectedEffect
    ) {
        for (DetailBackPolicy.BackTrigger trigger : DetailBackPolicy.BackTrigger.values()) {
            assertCurrentClassifiedBackDecision(taskRoot, sourceRoute, trigger, expectedEffect);
        }
    }

    private static void assertTriggerEffect(
        boolean taskRoot,
        DetailBackPolicy.BackTrigger trigger,
        DetailBackPolicy.Effect expectedEffect
    ) {
        for (DetailBackPolicy.SourceRoute sourceRoute : DetailBackPolicy.SourceRoute.values()) {
            assertCurrentClassifiedBackDecision(taskRoot, sourceRoute, trigger, expectedEffect);
        }
    }

    private static void assertCurrentClassifiedBackDecision(
        boolean taskRoot,
        DetailBackPolicy.SourceRoute sourceRoute,
        DetailBackPolicy.BackTrigger trigger,
        DetailBackPolicy.Effect expectedEffect
    ) {
        DetailBackPolicy.Decision decision = DetailBackPolicy.decide(
            new DetailBackPolicy.Inputs(taskRoot, sourceRoute, trigger)
        );

        assertEquals(expectedEffect, decision.effect);
        assertEquals(DetailBackPolicy.FinishBehavior.FINISH, decision.finishBehavior);
    }
}
