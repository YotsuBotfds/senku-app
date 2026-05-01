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
        assertEquals(explicitStacked.routeIntent, stacked.routeIntent);
        assertEquals(explicitTaskRoot.effect, taskRoot.effect);
        assertEquals(explicitTaskRoot.finishBehavior, taskRoot.finishBehavior);
        assertEquals(explicitTaskRoot.routeIntent, taskRoot.routeIntent);
    }

    @Test
    public void sourceRouteIsClassifiedButDoesNotChangeBackEffect() {
        assertRouteEffect(false, DetailBackPolicy.SourceRoute.ANSWER, DetailBackPolicy.Effect.FINISH_ACTIVITY);
        assertRouteEffect(false, DetailBackPolicy.SourceRoute.EMERGENCY_ANSWER, DetailBackPolicy.Effect.FINISH_ACTIVITY);
        assertRouteEffect(false, DetailBackPolicy.SourceRoute.GUIDE, DetailBackPolicy.Effect.FINISH_ACTIVITY);
        assertRouteEffect(false, DetailBackPolicy.SourceRoute.HOME_GUIDE, DetailBackPolicy.Effect.FINISH_ACTIVITY);
        assertRouteEffect(false, DetailBackPolicy.SourceRoute.CROSS_REFERENCE_GUIDE, DetailBackPolicy.Effect.FINISH_ACTIVITY);
        assertRouteEffect(true, DetailBackPolicy.SourceRoute.ANSWER, DetailBackPolicy.Effect.NAVIGATE_HOME);
        assertRouteEffect(true, DetailBackPolicy.SourceRoute.EMERGENCY_ANSWER, DetailBackPolicy.Effect.NAVIGATE_HOME);
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
    public void routeSpecificBackIntentDocumentsCurrentAnswerGuideCrossReferenceAndEmergencyUx() {
        assertBackIntent(
            false,
            DetailBackPolicy.SourceRoute.ANSWER,
            DetailBackPolicy.BackTrigger.SYSTEM_BACK,
            DetailBackPolicy.RouteBackIntent.FINISH_OPENED_DETAIL
        );
        assertBackIntent(
            true,
            DetailBackPolicy.SourceRoute.ANSWER,
            DetailBackPolicy.BackTrigger.SYSTEM_BACK,
            DetailBackPolicy.RouteBackIntent.NAVIGATE_HOME
        );
        assertBackIntent(
            false,
            DetailBackPolicy.SourceRoute.GUIDE,
            DetailBackPolicy.BackTrigger.GUIDE_RETURN,
            DetailBackPolicy.RouteBackIntent.FINISH_OPENED_DETAIL
        );
        assertBackIntent(
            true,
            DetailBackPolicy.SourceRoute.GUIDE,
            DetailBackPolicy.BackTrigger.GUIDE_RETURN,
            DetailBackPolicy.RouteBackIntent.NAVIGATE_HOME
        );
        assertBackIntent(
            false,
            DetailBackPolicy.SourceRoute.CROSS_REFERENCE_GUIDE,
            DetailBackPolicy.BackTrigger.GUIDE_RETURN,
            DetailBackPolicy.RouteBackIntent.FINISH_OPENED_DETAIL
        );
        assertBackIntent(
            true,
            DetailBackPolicy.SourceRoute.CROSS_REFERENCE_GUIDE,
            DetailBackPolicy.BackTrigger.GUIDE_RETURN,
            DetailBackPolicy.RouteBackIntent.NAVIGATE_HOME
        );
        assertBackIntent(
            false,
            DetailBackPolicy.SourceRoute.EMERGENCY_ANSWER,
            DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON,
            DetailBackPolicy.RouteBackIntent.FINISH_OPENED_DETAIL
        );
        assertBackIntent(
            true,
            DetailBackPolicy.SourceRoute.EMERGENCY_ANSWER,
            DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON,
            DetailBackPolicy.RouteBackIntent.NAVIGATE_EMERGENCY_MANUAL_HOME
        );
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
    public void emergencyAnswerSurfacesKeepExplicitBackAffordanceAndTaskRootManualFallback() {
        DetailBackPolicy.Inputs stackedInputs = new DetailBackPolicy.Inputs(
            false,
            DetailBackPolicy.SourceRoute.EMERGENCY_ANSWER,
            DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON
        );
        DetailBackPolicy.Inputs taskRootInputs = new DetailBackPolicy.Inputs(
            true,
            DetailBackPolicy.SourceRoute.EMERGENCY_ANSWER,
            DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON
        );
        DetailBackPolicy.VisibleBackAffordance stacked = DetailBackPolicy.visibleBackAffordance(stackedInputs);
        DetailBackPolicy.VisibleBackAffordance taskRoot = DetailBackPolicy.visibleBackAffordance(taskRootInputs);

        assertEquals(R.string.detail_back, stacked.labelResource);
        assertEquals(R.string.detail_back_content_description, stacked.contentDescriptionResource);
        assertFalse(stacked.longPressHomeShortcutEnabled);

        assertEquals(R.string.detail_emergency_app_rail_manual_label, taskRoot.labelResource);
        assertEquals(R.string.detail_emergency_app_rail_manual_content_description, taskRoot.contentDescriptionResource);
        assertFalse(taskRoot.longPressHomeShortcutEnabled);

        assertCurrentClassifiedBackDecision(
            false,
            DetailBackPolicy.SourceRoute.EMERGENCY_ANSWER,
            DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON,
            DetailBackPolicy.Effect.FINISH_ACTIVITY
        );
        assertCurrentClassifiedBackDecision(
            true,
            DetailBackPolicy.SourceRoute.EMERGENCY_ANSWER,
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

                assertTaskRootAffordanceResources(sourceRoute, taskRoot);
                assertFalse(taskRoot.longPressHomeShortcutEnabled);
            }
        }
    }

    @Test
    public void visibleBackAffordanceCoversDetailAndEmergencyChromeResources() {
        DetailBackPolicy.VisibleBackAffordance stackedGuide = DetailBackPolicy.visibleBackAffordance(
            new DetailBackPolicy.Inputs(
                false,
                DetailBackPolicy.SourceRoute.GUIDE,
                DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON
            )
        );
        DetailBackPolicy.VisibleBackAffordance taskRootHomeGuide = DetailBackPolicy.visibleBackAffordance(
            new DetailBackPolicy.Inputs(
                true,
                DetailBackPolicy.SourceRoute.HOME_GUIDE,
                DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON
            )
        );

        assertEquals(R.string.detail_back, stackedGuide.labelResource);
        assertEquals(R.string.detail_back_content_description, stackedGuide.contentDescriptionResource);
        assertFalse(stackedGuide.longPressHomeShortcutEnabled);

        assertEquals(R.string.home_button, taskRootHomeGuide.labelResource);
        assertEquals(R.string.detail_home_content_description, taskRootHomeGuide.contentDescriptionResource);
        assertFalse(taskRootHomeGuide.longPressHomeShortcutEnabled);
    }

    @Test
    public void answerSourceVisibleBackUsesBackChromeAndFinishesStackedActivity() {
        DetailBackPolicy.Inputs answerStack = new DetailBackPolicy.Inputs(
            false,
            DetailBackPolicy.SourceRoute.ANSWER,
            DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON
        );

        DetailBackPolicy.Decision decision = DetailBackPolicy.decide(answerStack);
        DetailBackPolicy.VisibleBackAffordance affordance = DetailBackPolicy.visibleBackAffordance(answerStack);

        assertEquals(DetailBackPolicy.Effect.FINISH_ACTIVITY, decision.effect);
        assertEquals(DetailBackPolicy.FinishBehavior.FINISH, decision.finishBehavior);
        assertEquals(R.string.detail_back, affordance.labelResource);
        assertEquals(R.string.detail_back_content_description, affordance.contentDescriptionResource);
        assertFalse(affordance.longPressHomeShortcutEnabled);
    }

    @Test
    public void sourceProvenanceReturnFromAnswerStackFinishesOnlyOpenedDetail() {
        DetailBackPolicy.Decision openedSourceReturn = DetailBackPolicy.decide(
            new DetailBackPolicy.Inputs(
                false,
                DetailBackPolicy.SourceRoute.GUIDE,
                DetailBackPolicy.BackTrigger.GUIDE_RETURN
            )
        );
        DetailBackPolicy.VisibleBackAffordance openedSourceBack = DetailBackPolicy.visibleBackAffordance(
            new DetailBackPolicy.Inputs(
                false,
                DetailBackPolicy.SourceRoute.GUIDE,
                DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON
            )
        );
        DetailBackPolicy.Decision preservedAnswerContext = DetailBackPolicy.decide(
            new DetailBackPolicy.Inputs(
                false,
                DetailBackPolicy.SourceRoute.ANSWER,
                DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON
            )
        );

        assertEquals(DetailBackPolicy.Effect.FINISH_ACTIVITY, openedSourceReturn.effect);
        assertEquals(DetailBackPolicy.FinishBehavior.FINISH, openedSourceReturn.finishBehavior);
        assertEquals(R.string.detail_back, openedSourceBack.labelResource);
        assertEquals(R.string.detail_back_content_description, openedSourceBack.contentDescriptionResource);
        assertFalse(openedSourceBack.longPressHomeShortcutEnabled);
        assertEquals(DetailBackPolicy.Effect.FINISH_ACTIVITY, preservedAnswerContext.effect);
        assertEquals(DetailBackPolicy.FinishBehavior.FINISH, preservedAnswerContext.finishBehavior);
    }

    @Test
    public void crossReferenceProvenanceReturnFromAnswerStackFinishesOnlyOpenedDetail() {
        DetailBackPolicy.Decision openedCrossReferenceReturn = DetailBackPolicy.decide(
            new DetailBackPolicy.Inputs(
                false,
                DetailBackPolicy.SourceRoute.CROSS_REFERENCE_GUIDE,
                DetailBackPolicy.BackTrigger.GUIDE_RETURN
            )
        );
        DetailBackPolicy.VisibleBackAffordance openedCrossReferenceBack = DetailBackPolicy.visibleBackAffordance(
            new DetailBackPolicy.Inputs(
                false,
                DetailBackPolicy.SourceRoute.CROSS_REFERENCE_GUIDE,
                DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON
            )
        );
        DetailBackPolicy.Decision preservedAnswerContext = DetailBackPolicy.decide(
            new DetailBackPolicy.Inputs(
                false,
                DetailBackPolicy.SourceRoute.ANSWER,
                DetailBackPolicy.BackTrigger.SYSTEM_BACK
            )
        );

        assertEquals(DetailBackPolicy.Effect.FINISH_ACTIVITY, openedCrossReferenceReturn.effect);
        assertEquals(DetailBackPolicy.FinishBehavior.FINISH, openedCrossReferenceReturn.finishBehavior);
        assertEquals(R.string.detail_back, openedCrossReferenceBack.labelResource);
        assertEquals(R.string.detail_back_content_description, openedCrossReferenceBack.contentDescriptionResource);
        assertFalse(openedCrossReferenceBack.longPressHomeShortcutEnabled);
        assertEquals(DetailBackPolicy.Effect.FINISH_ACTIVITY, preservedAnswerContext.effect);
        assertEquals(DetailBackPolicy.FinishBehavior.FINISH, preservedAnswerContext.finishBehavior);
    }

    @Test
    public void taskRootVisibleBackReturnsHomeWhenNoAnswerContextIsUnderOpenedDetail() {
        for (DetailBackPolicy.SourceRoute sourceRoute : DetailBackPolicy.SourceRoute.values()) {
            DetailBackPolicy.Inputs taskRootVisibleBack = new DetailBackPolicy.Inputs(
                true,
                sourceRoute,
                DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON
            );

            DetailBackPolicy.Decision decision = DetailBackPolicy.decide(taskRootVisibleBack);
            DetailBackPolicy.VisibleBackAffordance affordance =
                DetailBackPolicy.visibleBackAffordance(taskRootVisibleBack);

            assertEquals(DetailBackPolicy.Effect.NAVIGATE_HOME, decision.effect);
            assertEquals(DetailBackPolicy.FinishBehavior.FINISH, decision.finishBehavior);
            assertEquals(expectedCurrentRouteBackIntent(true, sourceRoute), decision.routeIntent);
            assertTaskRootAffordanceResources(sourceRoute, affordance);
            assertFalse(affordance.longPressHomeShortcutEnabled);
        }
    }

    @Test
    public void stackedVisibleBackFinishesForEveryNonSavedDetailRoute() {
        for (DetailBackPolicy.SourceRoute sourceRoute : DetailBackPolicy.SourceRoute.values()) {
            DetailBackPolicy.Inputs stackedVisibleBack = new DetailBackPolicy.Inputs(
                false,
                sourceRoute,
                DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON
            );

            DetailBackPolicy.Decision decision = DetailBackPolicy.decide(stackedVisibleBack);
            DetailBackPolicy.VisibleBackAffordance affordance =
                DetailBackPolicy.visibleBackAffordance(stackedVisibleBack);

            assertEquals(DetailBackPolicy.Effect.FINISH_ACTIVITY, decision.effect);
            assertEquals(DetailBackPolicy.FinishBehavior.FINISH, decision.finishBehavior);
            assertEquals(DetailBackPolicy.RouteBackIntent.FINISH_OPENED_DETAIL, decision.routeIntent);
            assertEquals(R.string.detail_back, affordance.labelResource);
            assertEquals(R.string.detail_back_content_description, affordance.contentDescriptionResource);
            assertFalse(affordance.longPressHomeShortcutEnabled);
        }
    }

    @Test
    public void answerSourceVisibleBackUsesHomeChromeAndNavigatesHomeAtTaskRoot() {
        DetailBackPolicy.Inputs answerTaskRoot = new DetailBackPolicy.Inputs(
            true,
            DetailBackPolicy.SourceRoute.ANSWER,
            DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON
        );

        DetailBackPolicy.Decision decision = DetailBackPolicy.decide(answerTaskRoot);
        DetailBackPolicy.VisibleBackAffordance affordance = DetailBackPolicy.visibleBackAffordance(answerTaskRoot);

        assertEquals(DetailBackPolicy.Effect.NAVIGATE_HOME, decision.effect);
        assertEquals(DetailBackPolicy.FinishBehavior.FINISH, decision.finishBehavior);
        assertEquals(DetailBackPolicy.RouteBackIntent.NAVIGATE_HOME, decision.routeIntent);
        assertEquals(R.string.home_button, affordance.labelResource);
        assertEquals(R.string.detail_home_content_description, affordance.contentDescriptionResource);
        assertFalse(affordance.longPressHomeShortcutEnabled);
    }

    @Test
    public void emergencyAnswerSourceVisibleBackUsesManualChromeAndNavigatesHomeAtTaskRoot() {
        DetailBackPolicy.Inputs emergencyAnswerTaskRoot = new DetailBackPolicy.Inputs(
            true,
            DetailBackPolicy.SourceRoute.EMERGENCY_ANSWER,
            DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON
        );

        DetailBackPolicy.Decision decision = DetailBackPolicy.decide(emergencyAnswerTaskRoot);
        DetailBackPolicy.VisibleBackAffordance affordance =
            DetailBackPolicy.visibleBackAffordance(emergencyAnswerTaskRoot);

        assertEquals(DetailBackPolicy.Effect.NAVIGATE_HOME, decision.effect);
        assertEquals(DetailBackPolicy.FinishBehavior.FINISH, decision.finishBehavior);
        assertEquals(DetailBackPolicy.RouteBackIntent.NAVIGATE_EMERGENCY_MANUAL_HOME, decision.routeIntent);
        assertEquals(R.string.detail_emergency_app_rail_manual_label, affordance.labelResource);
        assertEquals(R.string.detail_emergency_app_rail_manual_content_description, affordance.contentDescriptionResource);
        assertFalse(affordance.longPressHomeShortcutEnabled);
    }

    @Test
    public void guideSourceVisibleBackUsesBackChromeAndFinishesStackedActivity() {
        DetailBackPolicy.Inputs guideStack = new DetailBackPolicy.Inputs(
            false,
            DetailBackPolicy.SourceRoute.GUIDE,
            DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON
        );

        DetailBackPolicy.Decision decision = DetailBackPolicy.decide(guideStack);
        DetailBackPolicy.VisibleBackAffordance affordance = DetailBackPolicy.visibleBackAffordance(guideStack);

        assertEquals(DetailBackPolicy.Effect.FINISH_ACTIVITY, decision.effect);
        assertEquals(DetailBackPolicy.FinishBehavior.FINISH, decision.finishBehavior);
        assertEquals(DetailBackPolicy.RouteBackIntent.FINISH_OPENED_DETAIL, decision.routeIntent);
        assertEquals(R.string.detail_back, affordance.labelResource);
        assertEquals(R.string.detail_back_content_description, affordance.contentDescriptionResource);
        assertFalse(affordance.longPressHomeShortcutEnabled);
    }

    @Test
    public void guideSourceVisibleBackUsesHomeChromeAndNavigatesHomeAtTaskRoot() {
        DetailBackPolicy.Inputs guideTaskRoot = new DetailBackPolicy.Inputs(
            true,
            DetailBackPolicy.SourceRoute.GUIDE,
            DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON
        );

        DetailBackPolicy.Decision decision = DetailBackPolicy.decide(guideTaskRoot);
        DetailBackPolicy.VisibleBackAffordance affordance = DetailBackPolicy.visibleBackAffordance(guideTaskRoot);

        assertEquals(DetailBackPolicy.Effect.NAVIGATE_HOME, decision.effect);
        assertEquals(DetailBackPolicy.FinishBehavior.FINISH, decision.finishBehavior);
        assertEquals(DetailBackPolicy.RouteBackIntent.NAVIGATE_HOME, decision.routeIntent);
        assertEquals(R.string.home_button, affordance.labelResource);
        assertEquals(R.string.detail_home_content_description, affordance.contentDescriptionResource);
        assertFalse(affordance.longPressHomeShortcutEnabled);
    }

    @Test
    public void unknownSourceVisibleBackUsesDefaultBackChromeAndFinishesStackedActivity() {
        DetailBackPolicy.Inputs unknownStack = new DetailBackPolicy.Inputs(
            false,
            DetailBackPolicy.SourceRoute.UNKNOWN,
            DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON
        );

        DetailBackPolicy.Decision decision = DetailBackPolicy.decide(unknownStack);
        DetailBackPolicy.VisibleBackAffordance affordance = DetailBackPolicy.visibleBackAffordance(unknownStack);

        assertEquals(DetailBackPolicy.Effect.FINISH_ACTIVITY, decision.effect);
        assertEquals(DetailBackPolicy.FinishBehavior.FINISH, decision.finishBehavior);
        assertEquals(DetailBackPolicy.RouteBackIntent.FINISH_OPENED_DETAIL, decision.routeIntent);
        assertEquals(R.string.detail_back, affordance.labelResource);
        assertEquals(R.string.detail_back_content_description, affordance.contentDescriptionResource);
        assertFalse(affordance.longPressHomeShortcutEnabled);
    }

    @Test
    public void unknownSourceVisibleBackUsesHomeChromeAndNavigatesHomeAtTaskRoot() {
        DetailBackPolicy.Inputs unknownTaskRoot = new DetailBackPolicy.Inputs(
            true,
            DetailBackPolicy.SourceRoute.UNKNOWN,
            DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON
        );

        DetailBackPolicy.Decision decision = DetailBackPolicy.decide(unknownTaskRoot);
        DetailBackPolicy.VisibleBackAffordance affordance = DetailBackPolicy.visibleBackAffordance(unknownTaskRoot);

        assertEquals(DetailBackPolicy.Effect.NAVIGATE_HOME, decision.effect);
        assertEquals(DetailBackPolicy.FinishBehavior.FINISH, decision.finishBehavior);
        assertEquals(DetailBackPolicy.RouteBackIntent.NAVIGATE_HOME, decision.routeIntent);
        assertEquals(R.string.home_button, affordance.labelResource);
        assertEquals(R.string.detail_home_content_description, affordance.contentDescriptionResource);
        assertFalse(affordance.longPressHomeShortcutEnabled);
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
        assertEquals(explicit.routeIntent, decision.routeIntent);
    }

    @Test
    public void nullRouteAndTriggerNormalizeToUnknownSystemBack() {
        DetailBackPolicy.Decision decision = DetailBackPolicy.decide(
            new DetailBackPolicy.Inputs(false, null, null)
        );

        assertEquals(DetailBackPolicy.Effect.FINISH_ACTIVITY, decision.effect);
        assertEquals(DetailBackPolicy.FinishBehavior.FINISH, decision.finishBehavior);
        assertEquals(DetailBackPolicy.RouteBackIntent.FINISH_OPENED_DETAIL, decision.routeIntent);
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
        assertEquals(expectedCurrentRouteBackIntent(taskRoot, sourceRoute), decision.routeIntent);
    }

    private static void assertBackIntent(
        boolean taskRoot,
        DetailBackPolicy.SourceRoute sourceRoute,
        DetailBackPolicy.BackTrigger trigger,
        DetailBackPolicy.RouteBackIntent expectedIntent
    ) {
        DetailBackPolicy.Decision decision = DetailBackPolicy.decide(
            new DetailBackPolicy.Inputs(taskRoot, sourceRoute, trigger)
        );

        assertEquals(expectedIntent, decision.routeIntent);
    }

    private static DetailBackPolicy.RouteBackIntent expectedCurrentRouteBackIntent(
        boolean taskRoot,
        DetailBackPolicy.SourceRoute sourceRoute
    ) {
        if (!taskRoot) {
            return DetailBackPolicy.RouteBackIntent.FINISH_OPENED_DETAIL;
        }
        if (sourceRoute == DetailBackPolicy.SourceRoute.EMERGENCY_ANSWER) {
            return DetailBackPolicy.RouteBackIntent.NAVIGATE_EMERGENCY_MANUAL_HOME;
        }
        return DetailBackPolicy.RouteBackIntent.NAVIGATE_HOME;
    }

    private static void assertTaskRootAffordanceResources(
        DetailBackPolicy.SourceRoute sourceRoute,
        DetailBackPolicy.VisibleBackAffordance affordance
    ) {
        if (sourceRoute == DetailBackPolicy.SourceRoute.EMERGENCY_ANSWER) {
            assertEquals(R.string.detail_emergency_app_rail_manual_label, affordance.labelResource);
            assertEquals(
                R.string.detail_emergency_app_rail_manual_content_description,
                affordance.contentDescriptionResource
            );
            return;
        }
        assertEquals(R.string.home_button, affordance.labelResource);
        assertEquals(R.string.detail_home_content_description, affordance.contentDescriptionResource);
    }
}
