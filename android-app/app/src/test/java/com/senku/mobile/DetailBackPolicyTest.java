package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class DetailBackPolicyTest {
    @Test
    public void nonTaskRootBackFinishesActivityForEveryCurrentEntryPoint() {
        for (DetailBackPolicy.BackTrigger trigger : DetailBackPolicy.BackTrigger.values()) {
            DetailBackPolicy.Decision decision = DetailBackPolicy.decide(
                new DetailBackPolicy.Inputs(false, DetailBackPolicy.SourceRoute.UNKNOWN, trigger)
            );

            assertEquals(DetailBackPolicy.Effect.FINISH_ACTIVITY, decision.effect);
            assertEquals(DetailBackPolicy.FinishBehavior.FINISH, decision.finishBehavior);
        }
    }

    @Test
    public void taskRootBackFallsBackToHomeForEveryCurrentEntryPoint() {
        for (DetailBackPolicy.BackTrigger trigger : DetailBackPolicy.BackTrigger.values()) {
            DetailBackPolicy.Decision decision = DetailBackPolicy.decide(
                new DetailBackPolicy.Inputs(true, DetailBackPolicy.SourceRoute.UNKNOWN, trigger)
            );

            assertEquals(DetailBackPolicy.Effect.NAVIGATE_HOME, decision.effect);
            assertEquals(DetailBackPolicy.FinishBehavior.FINISH, decision.finishBehavior);
        }
    }

    @Test
    public void sourceRouteIsCurrentlyDocumentedButDoesNotChangeBackEffect() {
        for (DetailBackPolicy.SourceRoute sourceRoute : DetailBackPolicy.SourceRoute.values()) {
            DetailBackPolicy.Decision stacked = DetailBackPolicy.decide(
                new DetailBackPolicy.Inputs(false, sourceRoute, DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON)
            );
            DetailBackPolicy.Decision taskRoot = DetailBackPolicy.decide(
                new DetailBackPolicy.Inputs(true, sourceRoute, DetailBackPolicy.BackTrigger.SYSTEM_BACK)
            );

            assertEquals(DetailBackPolicy.Effect.FINISH_ACTIVITY, stacked.effect);
            assertEquals(DetailBackPolicy.Effect.NAVIGATE_HOME, taskRoot.effect);
        }
    }

    @Test
    public void visibleBackAffordanceMatchesCurrentTaskRootLabels() {
        DetailBackPolicy.VisibleBackAffordance stacked = DetailBackPolicy.visibleBackAffordance(false);
        DetailBackPolicy.VisibleBackAffordance taskRoot = DetailBackPolicy.visibleBackAffordance(true);

        assertEquals(R.string.detail_back, stacked.labelResource);
        assertEquals(R.string.detail_back_content_description, stacked.contentDescriptionResource);
        assertFalse(stacked.longPressHomeShortcutEnabled);

        assertEquals(R.string.detail_back, taskRoot.labelResource);
        assertEquals(R.string.detail_back_content_description, taskRoot.contentDescriptionResource);
        assertFalse(taskRoot.longPressHomeShortcutEnabled);
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
}
