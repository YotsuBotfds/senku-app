package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import com.senku.mobile.AskSearchCoordinator.SubmitTarget;
import com.senku.ui.primitives.BottomTabDestination;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class MainActivityPhoneNavigationTest {
    @Test
    public void visiblePhoneTabsUseFirstNavigationSliceOrder() {
        assertEquals(
            Arrays.asList(
                BottomTabDestination.HOME,
                BottomTabDestination.ASK,
                BottomTabDestination.PINS
            ),
            MainActivity.buildVisiblePhoneTabDestinations()
        );
    }

    @Test
    public void visiblePhoneTabsDelegateToPrimaryProductDestinations() {
        assertEquals(
            MainActivity.buildPhonePrimaryDestinations(),
            MainActivity.buildVisiblePhoneTabDestinations()
        );
    }

    @Test
    public void visiblePhoneTabsDoNotReintroduceSearchOrThreads() {
        List<BottomTabDestination> visibleTabs = MainActivity.buildVisiblePhoneTabDestinations();

        assertFalse(visibleTabs.contains(BottomTabDestination.SEARCH));
        assertFalse(visibleTabs.contains(BottomTabDestination.THREADS));
    }

    @Test
    public void runtimeBottomTabOnlyInstallsWhereThereIsNoPostureNavRail() {
        assertTrue(MainActivity.shouldInstallRuntimePhoneBottomTabBar(true, false));
        assertFalse(MainActivity.shouldInstallRuntimePhoneBottomTabBar(true, true));
        assertFalse(MainActivity.shouldInstallRuntimePhoneBottomTabBar(false, false));
    }

    @Test
    public void eachPostureUsesOnlyOnePrimaryNavigationSurface() {
        assertFalse(MainActivity.shouldInstallRuntimePhoneBottomTabBar(true, false)
            && MainActivity.shouldBindStaticNavigationRail(false, false));
        assertFalse(MainActivity.shouldInstallRuntimePhoneBottomTabBar(true, true)
            && MainActivity.shouldBindStaticNavigationRail(true, false));
        assertFalse(MainActivity.shouldInstallRuntimePhoneBottomTabBar(false, false)
            && MainActivity.shouldBindStaticNavigationRail(false, true));
    }

    @Test
    public void staticNavigationRailBindsOnLandscapePhoneAndTabletShells() {
        assertTrue(MainActivity.shouldBindStaticNavigationRail(true, false));
        assertTrue(MainActivity.shouldBindStaticNavigationRail(false, true));

        assertFalse(MainActivity.shouldBindStaticNavigationRail(false, false));
    }

    @Test
    public void mainSurfaceNavigationHandlesPhoneAndTabletShells() {
        assertTrue(MainActivity.shouldHandleMainSurfaceNavigationTabs(true, false));
        assertTrue(MainActivity.shouldHandleMainSurfaceNavigationTabs(false, true));

        assertFalse(MainActivity.shouldHandleMainSurfaceNavigationTabs(false, false));
    }

    @Test
    public void openSavedExtraRequestsSavedDestination() {
        assertTrue(MainActivity.shouldOpenSavedDestination(true));
    }

    @Test
    public void openSavedExtraSelectsSavedPinsRouteAndSavedGuideSection() {
        MainRouteDecisionHelper.Transition transition = MainRouteDecisionHelper.openSavedIntent(
            true,
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.ASK_RESULTS,
                BottomTabDestination.ASK,
                true
            )
        );

        assertEquals(MainRouteDecisionHelper.Effect.SHOW_SAVED_GUIDES, transition.effect);
        assertRouteState(
            transition.routeState,
            MainRouteDecisionHelper.Surface.SAVED_GUIDES,
            BottomTabDestination.PINS,
            false
        );
        assertTrue(MainRouteDecisionHelper.isBrowseSurface(transition.routeState.surface));
        assertTrue(MainActivity.shouldShowSavedGuideSection(
            MainRouteDecisionHelper.isBrowseSurface(transition.routeState.surface),
            transition.routeState.activePhoneTab,
            0
        ));
    }

    @Test
    public void missingOpenSavedExtraDoesNotRequestSavedDestination() {
        assertFalse(MainActivity.shouldOpenSavedDestination(false));
    }

    @Test
    public void openHomeExtraRequestsManualHomeDestination() {
        assertTrue(MainActivity.shouldOpenHomeDestination(true));
    }

    @Test
    public void missingOpenHomeExtraDoesNotRequestManualHomeDestination() {
        assertFalse(MainActivity.shouldOpenHomeDestination(false));
    }

    @Test
    public void savedGuideSectionShowsEmptyStateOnlyForSavedFlow() {
        assertTrue(MainActivity.shouldShowSavedGuideSection(true, BottomTabDestination.PINS, 0));

        assertFalse(MainActivity.shouldShowSavedGuideSection(true, BottomTabDestination.HOME, 0));
        assertFalse(MainActivity.shouldShowSavedGuideSection(true, BottomTabDestination.ASK, 0));
        assertFalse(MainActivity.shouldShowSavedGuideSection(false, BottomTabDestination.PINS, 0));
    }

    @Test
    public void savedGuideSectionShowsNonEmptySavedGuidesAcrossBrowseFlows() {
        assertTrue(MainActivity.shouldShowSavedGuideSection(true, BottomTabDestination.HOME, 1));
        assertTrue(MainActivity.shouldShowSavedGuideSection(true, BottomTabDestination.PINS, 12));

        assertFalse(MainActivity.shouldShowSavedGuideSection(false, BottomTabDestination.HOME, 1));
    }

    @Test
    public void savedDestinationDoesNotWaitForRepositoryToShowEmptyState() {
        assertTrue(MainActivity.shouldShowSavedGuideSection(true, BottomTabDestination.PINS, 0));
        assertFalse(MainActivity.shouldLoadBrowseGuidesForSavedDestination(false, 0));
    }

    @Test
    public void savedDestinationRefreshesBrowseGuidesOnlyWhenRepositoryIsReadyButNotLoaded() {
        assertTrue(MainActivity.shouldLoadBrowseGuidesForSavedDestination(true, 0));

        assertFalse(MainActivity.shouldLoadBrowseGuidesForSavedDestination(true, 1));
        assertFalse(MainActivity.shouldLoadBrowseGuidesForSavedDestination(false, 0));
    }

    @Test
    public void phoneFlowIntentPredicatesNameLibraryAskAndSavedOwnership() {
        assertTrue(MainActivity.isLibraryPhoneFlowIntent(BottomTabDestination.HOME));
        assertTrue(MainActivity.isLibraryPhoneFlowIntent(BottomTabDestination.SEARCH));
        assertTrue(MainActivity.isAskPhoneFlowIntent(BottomTabDestination.ASK));
        assertTrue(MainActivity.isAskPhoneFlowIntent(BottomTabDestination.THREADS));
        assertTrue(MainActivity.isSavedPhoneFlowIntent(BottomTabDestination.PINS));

        assertFalse(MainActivity.isLibraryPhoneFlowIntent(BottomTabDestination.ASK));
        assertFalse(MainActivity.isAskPhoneFlowIntent(BottomTabDestination.SEARCH));
        assertFalse(MainActivity.isSavedPhoneFlowIntent(BottomTabDestination.HOME));
    }

    @Test
    public void phoneFlowIntentPredicatesAreMutuallyExclusiveForEveryDestination() {
        for (BottomTabDestination destination : BottomTabDestination.values()) {
            int matchingPredicateCount = 0;
            if (MainActivity.isLibraryPhoneFlowIntent(destination)) {
                matchingPredicateCount++;
            }
            if (MainActivity.isAskPhoneFlowIntent(destination)) {
                matchingPredicateCount++;
            }
            if (MainActivity.isSavedPhoneFlowIntent(destination)) {
                matchingPredicateCount++;
            }

            assertEquals(1, matchingPredicateCount);
        }
    }

    @Test
    public void phoneFlowIntentPredicatesFollowRouteSelectionOwner() {
        for (BottomTabDestination destination : BottomTabDestination.values()) {
            BottomTabDestination owner = MainRouteDecisionHelper.phoneTabSelectionOwner(destination);

            assertEquals(owner == BottomTabDestination.HOME, MainActivity.isLibraryPhoneFlowIntent(destination));
            assertEquals(owner == BottomTabDestination.ASK, MainActivity.isAskPhoneFlowIntent(destination));
            assertEquals(owner == BottomTabDestination.PINS, MainActivity.isSavedPhoneFlowIntent(destination));
        }

        assertFalse(MainActivity.isLibraryPhoneFlowIntent(null));
        assertFalse(MainActivity.isAskPhoneFlowIntent(null));
        assertFalse(MainActivity.isSavedPhoneFlowIntent(null));
    }

    @Test
    public void restoredPhoneTabUsesVisibleSelectionOwner() {
        assertEquals(
            BottomTabDestination.ASK,
            MainActivity.resolveRestoredPhoneTab(BottomTabDestination.THREADS.name())
        );
        assertEquals(
            BottomTabDestination.ASK,
            MainActivity.resolveRestoredPhoneTab(BottomTabDestination.ASK.name())
        );
        assertEquals(
            BottomTabDestination.HOME,
            MainActivity.resolveRestoredPhoneTab(BottomTabDestination.SEARCH.name())
        );
        assertEquals(
            BottomTabDestination.PINS,
            MainActivity.resolveRestoredPhoneTab(BottomTabDestination.PINS.name())
        );
    }

    @Test
    public void restoredPhoneTabFallsBackToHomeForMissingOrUnknownState() {
        assertEquals(BottomTabDestination.HOME, MainActivity.resolveRestoredPhoneTab(null));
        assertEquals(BottomTabDestination.HOME, MainActivity.resolveRestoredPhoneTab("   "));
        assertEquals(BottomTabDestination.HOME, MainActivity.resolveRestoredPhoneTab("DETAIL"));
    }

    @Test
    public void restoredMainRouteStateKeepsFirstClassResultSurfaces() {
        assertRouteState(
            MainActivity.resolveRestoredMainRouteState(
                MainRouteDecisionHelper.Surface.SEARCH_RESULTS.name(),
                BottomTabDestination.HOME.name(),
                false,
                true,
                BottomTabDestination.HOME.name()
            ),
            MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
            BottomTabDestination.HOME,
            false
        );
        assertRouteState(
            MainActivity.resolveRestoredMainRouteState(
                MainRouteDecisionHelper.Surface.ASK_RESULTS.name(),
                BottomTabDestination.ASK.name(),
                true,
                true,
                BottomTabDestination.HOME.name()
            ),
            MainRouteDecisionHelper.Surface.ASK_RESULTS,
            BottomTabDestination.ASK,
            true
        );
    }

    @Test
    public void restoredMainRouteStateKeepsSavedAndRecentBrowseSurfaces() {
        assertRouteState(
            MainActivity.resolveRestoredMainRouteState(
                MainRouteDecisionHelper.Surface.SAVED_GUIDES.name(),
                BottomTabDestination.PINS.name(),
                false,
                true,
                BottomTabDestination.HOME.name()
            ),
            MainRouteDecisionHelper.Surface.SAVED_GUIDES,
            BottomTabDestination.PINS,
            false
        );
        assertRouteState(
            MainActivity.resolveRestoredMainRouteState(
                MainRouteDecisionHelper.Surface.RECENT_THREADS.name(),
                BottomTabDestination.ASK.name(),
                false,
                true,
                BottomTabDestination.HOME.name()
            ),
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
            BottomTabDestination.ASK,
            false
        );
    }

    @Test
    public void restoredMainRouteStateFallsBackToLegacyPhoneTabWhenFirstClassStateIsMissingOrInvalid() {
        assertRouteState(
            MainActivity.resolveRestoredMainRouteState(
                null,
                null,
                false,
                false,
                BottomTabDestination.ASK.name()
            ),
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
            BottomTabDestination.ASK,
            true
        );
        assertRouteState(
            MainActivity.resolveRestoredMainRouteState(
                "DETAIL",
                BottomTabDestination.HOME.name(),
                false,
                true,
                BottomTabDestination.PINS.name()
            ),
            MainRouteDecisionHelper.Surface.SAVED_GUIDES,
            BottomTabDestination.PINS,
            false
        );
    }

    @Test
    public void restoredMainRouteStateControlsInitialBrowseChromeForResultSurfaces() {
        assertFalse(MainActivity.resolveInitialBrowseChromeVisibleForTest(
            false,
            MainRouteDecisionHelper.Surface.SEARCH_RESULTS.name(),
            BottomTabDestination.HOME.name(),
            false,
            true,
            BottomTabDestination.HOME.name()
        ));
        assertFalse(MainActivity.resolveInitialBrowseChromeVisibleForTest(
            false,
            MainRouteDecisionHelper.Surface.ASK_RESULTS.name(),
            BottomTabDestination.ASK.name(),
            true,
            true,
            BottomTabDestination.HOME.name()
        ));
        assertTrue(MainActivity.resolveInitialBrowseChromeVisibleForTest(
            false,
            MainRouteDecisionHelper.Surface.SAVED_GUIDES.name(),
            BottomTabDestination.PINS.name(),
            false,
            true,
            BottomTabDestination.HOME.name()
        ));
        assertFalse(MainActivity.resolveInitialBrowseChromeVisibleForTest(
            true,
            MainRouteDecisionHelper.Surface.SAVED_GUIDES.name(),
            BottomTabDestination.PINS.name(),
            false,
            true,
            BottomTabDestination.PINS.name()
        ));
    }

    @Test
    public void installCompletionDoesNotPublishBrowseGuidesOverRestoredResultSurfaces() {
        assertRestoredResultRouteSuppressesInstallCompletionBrowsePublish(
            MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
            BottomTabDestination.HOME,
            false
        );
        assertRestoredResultRouteSuppressesInstallCompletionBrowsePublish(
            MainRouteDecisionHelper.Surface.ASK_RESULTS,
            BottomTabDestination.ASK,
            true
        );
    }

    @Test
    public void installCompletionPreservesRestoredResultPayloadWhenBrowsePublishIsSkipped() {
        assertInstallCompletionKeepsRestoredResultPayload(
            MainRouteDecisionHelper.Surface.SEARCH_RESULTS,
            BottomTabDestination.HOME,
            false,
            "rain shelter"
        );
        assertInstallCompletionKeepsRestoredResultPayload(
            MainRouteDecisionHelper.Surface.ASK_RESULTS,
            BottomTabDestination.ASK,
            true,
            "child swallowed cleaner"
        );
    }

    @Test
    public void sharedInputSubmitRoutesToAskWhenAskOwnsTheVisibleFlow() {
        assertEquals(
            SubmitTarget.ASK,
            MainActivity.resolveSharedSubmitTarget(BottomTabDestination.ASK, false)
        );
        assertEquals(
            SubmitTarget.ASK,
            MainActivity.resolveSharedSubmitTarget(BottomTabDestination.HOME, true)
        );
        assertTrue(MainActivity.shouldSubmitSharedInputAsAsk(BottomTabDestination.ASK, false));
        assertTrue(MainActivity.shouldSubmitSharedInputAsAsk(BottomTabDestination.HOME, true));
    }

    @Test
    public void searchButtonSubmitUsesAskTargetWhenAskOwnsSharedInput() {
        assertEquals(
            SubmitTarget.ASK,
            MainActivity.resolveSearchButtonSubmitTarget(BottomTabDestination.ASK, false)
        );
        assertEquals(
            SubmitTarget.ASK,
            MainActivity.resolveSearchButtonSubmitTarget(BottomTabDestination.HOME, true)
        );
        assertEquals(
            SubmitTarget.SEARCH,
            MainActivity.resolveSearchButtonSubmitTarget(BottomTabDestination.HOME, false)
        );
        assertEquals(
            SubmitTarget.SEARCH,
            MainActivity.resolveSearchButtonSubmitTarget(BottomTabDestination.PINS, false)
        );
    }

    @Test
    public void searchButtonSubmitActionUsesSameTargetAndVisibleCopy() {
        MainActivity.SharedSubmitAction askAction =
            MainActivity.resolveSearchButtonSubmitActionForTest(BottomTabDestination.ASK, false, true);
        MainActivity.SharedSubmitAction searchAction =
            MainActivity.resolveSearchButtonSubmitActionForTest(BottomTabDestination.HOME, false, true);

        assertEquals(SubmitTarget.ASK, askAction.target);
        assertEquals(R.string.ask_button_ready, askAction.buttonTextResource);
        assertEquals(R.string.ask_button_description, askAction.buttonDescriptionResource);
        assertEquals(SubmitTarget.SEARCH, searchAction.target);
        assertEquals(R.string.external_review_home_search_button, searchAction.buttonTextResource);
        assertEquals(R.string.search_button_description, searchAction.buttonDescriptionResource);
    }

    @Test
    public void explicitPhoneFlowEntryClaimsOrClearsAskLaneOwnership() {
        assertTrue(MainActivity.resolveAskLaneActiveForExplicitPhoneFlow(BottomTabDestination.ASK, false));

        assertFalse(MainActivity.resolveAskLaneActiveForExplicitPhoneFlow(BottomTabDestination.SEARCH, true));
        assertFalse(MainActivity.resolveAskLaneActiveForExplicitPhoneFlow(BottomTabDestination.HOME, true));
        assertFalse(MainActivity.resolveAskLaneActiveForExplicitPhoneFlow(BottomTabDestination.PINS, true));
        assertFalse(MainActivity.resolveAskLaneActiveForExplicitPhoneFlow(BottomTabDestination.THREADS, true));

        assertTrue(MainActivity.resolveAskLaneActiveForExplicitPhoneFlow(null, true));
        assertFalse(MainActivity.resolveAskLaneActiveForExplicitPhoneFlow(null, false));
    }

    @Test
    public void explicitSearchEntryPreventsStaleAskLaneSubmitTarget() {
        boolean askLaneActive = MainActivity.resolveAskLaneActiveForExplicitPhoneFlow(
            BottomTabDestination.SEARCH,
            true
        );

        assertFalse(askLaneActive);
        assertEquals(
            SubmitTarget.SEARCH,
            MainActivity.resolveSharedSubmitTarget(BottomTabDestination.HOME, askLaneActive)
        );
        assertEquals(
            SubmitTarget.SEARCH,
            MainActivity.resolveSearchButtonSubmitTarget(BottomTabDestination.HOME, askLaneActive)
        );
    }

    @Test
    public void mainActivitySubmitHelpersDelegateToAskSearchCoordinator() {
        for (BottomTabDestination destination : BottomTabDestination.values()) {
            for (boolean askLaneActive : Arrays.asList(false, true)) {
                SubmitTarget coordinatorTarget = AskSearchCoordinator.resolveSubmitTarget(destination, askLaneActive);

                assertEquals(
                    coordinatorTarget,
                    MainActivity.resolveSharedSubmitTarget(destination, askLaneActive)
                );
                assertEquals(
                    coordinatorTarget,
                    MainActivity.resolveSearchButtonSubmitTarget(destination, askLaneActive)
                );
                assertEquals(
                    AskSearchCoordinator.shouldSubmitAsAsk(destination, askLaneActive),
                    MainActivity.shouldSubmitSharedInputAsAsk(destination, askLaneActive)
                );
            }
        }
    }

    @Test
    public void autoAskWithoutQueryOpensAskLaneInsteadOfSearchAutomation() {
        assertTrue(MainActivity.shouldOpenEmptyAutoAskLaneForTest(null, true));
        assertTrue(MainActivity.shouldOpenEmptyAutoAskLaneForTest("   ", true));

        assertFalse(MainActivity.shouldOpenEmptyAutoAskLaneForTest("how do I boil water", true));
        assertFalse(MainActivity.shouldOpenEmptyAutoAskLaneForTest(null, false));
    }

    @Test
    public void sharedInputSubmitStaysSearchForLibraryAndSavedFlows() {
        assertEquals(
            SubmitTarget.SEARCH,
            MainActivity.resolveSharedSubmitTarget(BottomTabDestination.HOME, false)
        );
        assertEquals(
            SubmitTarget.SEARCH,
            MainActivity.resolveSharedSubmitTarget(BottomTabDestination.SEARCH, false)
        );
        assertEquals(
            SubmitTarget.SEARCH,
            MainActivity.resolveSharedSubmitTarget(BottomTabDestination.PINS, false)
        );
        assertFalse(MainActivity.shouldSubmitSharedInputAsAsk(BottomTabDestination.HOME, false));
        assertFalse(MainActivity.shouldSubmitSharedInputAsAsk(BottomTabDestination.SEARCH, false));
        assertFalse(MainActivity.shouldSubmitSharedInputAsAsk(BottomTabDestination.PINS, false));
    }

    @Test
    public void sharedInputSubmitAcceptsSearchDoneAndHardwareEnter() {
        assertTrue(MainActivity.isImeSubmitAction(EditorInfo.IME_ACTION_SEARCH));
        assertTrue(MainActivity.isImeSubmitAction(EditorInfo.IME_ACTION_DONE));
        assertFalse(MainActivity.isImeSubmitAction(EditorInfo.IME_ACTION_NONE));

        assertTrue(MainActivity.isHardwareEnterSubmitAction(KeyEvent.KEYCODE_ENTER, KeyEvent.ACTION_UP));
        assertFalse(MainActivity.isHardwareEnterSubmitAction(KeyEvent.KEYCODE_ENTER, KeyEvent.ACTION_DOWN));
        assertFalse(MainActivity.isHardwareEnterSubmitAction(KeyEvent.KEYCODE_TAB, KeyEvent.ACTION_UP));

        assertTrue(MainActivity.isSharedInputSubmitAction(EditorInfo.IME_ACTION_SEARCH, null));
    }

    @Test
    public void routeEffectControllerAppliesHomeTabEffectsInRouteOrder() {
        RecordingRouteEffects effects = new RecordingRouteEffects(true);

        MainRouteEffectController.applyPhoneTabTransitionEffect(
            MainRouteDecisionHelper.Effect.SHOW_BROWSE_HOME,
            effects
        );

        assertEquals(
            Arrays.asList(
                "updateActionLabels",
                "dismissSearchKeyboard",
                "ensureBrowseHomeVisible",
                "scrollBrowseToTop"
            ),
            effects.calls
        );
    }

    @Test
    public void routeEffectControllerFocusesSharedInputWithoutBrowseScrollOnResultSurfaces() {
        RecordingRouteEffects effects = new RecordingRouteEffects(false);

        MainRouteEffectController.applyPhoneTabTransitionEffect(
            MainRouteDecisionHelper.Effect.FOCUS_ASK_INPUT,
            effects
        );

        assertEquals(
            Arrays.asList("updateActionLabels", "focusSearchInput"),
            effects.calls
        );
    }

    @Test
    public void routeEffectControllerAppliesSavedAndRecentSectionEffects() {
        RecordingRouteEffects savedEffects = new RecordingRouteEffects(true);
        RecordingRouteEffects recentEffects = new RecordingRouteEffects(true);

        MainRouteEffectController.applyPhoneTabTransitionEffect(
            MainRouteDecisionHelper.Effect.SHOW_SAVED_GUIDES,
            savedEffects
        );
        MainRouteEffectController.applyPhoneTabTransitionEffect(
            MainRouteDecisionHelper.Effect.SHOW_RECENT_THREADS,
            recentEffects
        );

        assertEquals(
            Arrays.asList("updateActionLabels", "dismissSearchKeyboard", "prepareSavedGuidesDestination"),
            savedEffects.calls
        );
        assertEquals(
            Arrays.asList(
                "updateActionLabels",
                "dismissSearchKeyboard",
                "ensureBrowseHomeVisible",
                "scrollRecentThreadsIntoView"
            ),
            recentEffects.calls
        );
    }

    @Test
    public void routeEffectControllerAppliesPreviousTabTransitionRouteWithoutReopeningAskLane() {
        MainRouteDecisionHelper.Transition transition = MainRouteDecisionHelper.systemBack(
            new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.SAVED_GUIDES,
                BottomTabDestination.PINS,
                false
            ),
            BottomTabDestination.ASK
        );
        RecordingRouteEffects effects = new RecordingRouteEffects(true);

        MainRouteEffectController.applyPhoneTabTransitionEffect(transition, effects);

        assertEquals(MainRouteDecisionHelper.Effect.SHOW_PREVIOUS_TAB, transition.effect);
        assertRouteState(
            transition.routeState,
            MainRouteDecisionHelper.Surface.RECENT_THREADS,
            BottomTabDestination.ASK,
            false
        );
        assertEquals(
            Arrays.asList(
                "updateActionLabels",
                "dismissSearchKeyboard",
                "ensureBrowseHomeVisible",
                "scrollRecentThreadsIntoView"
            ),
            effects.calls
        );
    }

    @Test
    public void routeEffectControllerReturnsPreviousSavedAndHomeRoutesByTransitionSurface() {
        RecordingRouteEffects savedEffects = new RecordingRouteEffects(true);
        RecordingRouteEffects homeEffects = new RecordingRouteEffects(true);

        MainRouteEffectController.applyPhoneTabTransitionEffect(
            new MainRouteDecisionHelper.Transition(
                new MainRouteDecisionHelper.RouteState(
                    MainRouteDecisionHelper.Surface.SAVED_GUIDES,
                    BottomTabDestination.PINS,
                    false
                ),
                MainRouteDecisionHelper.Effect.SHOW_PREVIOUS_TAB
            ),
            savedEffects
        );
        MainRouteEffectController.applyPhoneTabTransitionEffect(
            new MainRouteDecisionHelper.Transition(
                MainRouteDecisionHelper.browseHome(),
                MainRouteDecisionHelper.Effect.SHOW_PREVIOUS_TAB
            ),
            homeEffects
        );

        assertEquals(
            Arrays.asList("updateActionLabels", "dismissSearchKeyboard", "prepareSavedGuidesDestination"),
            savedEffects.calls
        );
        assertEquals(
            Arrays.asList(
                "updateActionLabels",
                "dismissSearchKeyboard",
                "ensureBrowseHomeVisible",
                "scrollBrowseToTop"
            ),
            homeEffects.calls
        );
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

    private static void assertRestoredResultRouteSuppressesInstallCompletionBrowsePublish(
        MainRouteDecisionHelper.Surface surface,
        BottomTabDestination activePhoneTab,
        boolean askLaneActive
    ) {
        MainRouteDecisionHelper.RouteState restoredRouteState =
            MainActivity.resolveRestoredMainRouteState(
                surface.name(),
                activePhoneTab.name(),
                askLaneActive,
                true,
                BottomTabDestination.HOME.name()
            );

        assertRouteState(restoredRouteState, surface, activePhoneTab, askLaneActive);
        assertFalse(MainRouteDecisionHelper.isBrowseSurface(restoredRouteState.surface));
        assertFalse(MainRouteDecisionHelper.shouldPublishInstalledBrowseGuides(false, restoredRouteState));
    }

    private static void assertInstallCompletionKeepsRestoredResultPayload(
        MainRouteDecisionHelper.Surface surface,
        BottomTabDestination activePhoneTab,
        boolean askLaneActive,
        String restoredQuery
    ) {
        MainRouteDecisionHelper.RouteState restoredRouteState =
            MainActivity.resolveRestoredMainRouteState(
                surface.name(),
                activePhoneTab.name(),
                askLaneActive,
                true,
                BottomTabDestination.HOME.name()
            );
        List<String> restoredResults = Arrays.asList("restored:" + surface.name());
        List<String> installedBrowseGuides = Arrays.asList("browse:GD-001", "browse:GD-002");
        List<String> visibleResults = new java.util.ArrayList<>(restoredResults);
        String activeHighlightQuery = restoredQuery;

        MainInstallCompletionPolicy.Action action =
            MainInstallCompletionPolicy.resolve(false, restoredRouteState);
        if (action == MainInstallCompletionPolicy.Action.PUBLISH_BROWSE_GUIDES) {
            visibleResults = new java.util.ArrayList<>(installedBrowseGuides);
            activeHighlightQuery = "";
        }

        assertEquals(MainInstallCompletionPolicy.Action.PRESERVE_CURRENT_RESULTS, action);
        assertEquals(restoredResults, visibleResults);
        assertFalse("restored results should remain non-empty after skipped browse publication", visibleResults.isEmpty());
        assertEquals(restoredQuery, activeHighlightQuery);
    }

    private static final class RecordingRouteEffects implements MainRouteEffectController.Effects {
        final List<String> calls = new java.util.ArrayList<>();
        private final boolean browseModeActive;

        RecordingRouteEffects(boolean browseModeActive) {
            this.browseModeActive = browseModeActive;
        }

        @Override
        public boolean isBrowseModeActive() {
            return browseModeActive;
        }

        @Override
        public void updateActionLabels() {
            calls.add("updateActionLabels");
        }

        @Override
        public void dismissSearchKeyboard() {
            calls.add("dismissSearchKeyboard");
        }

        @Override
        public void ensureBrowseHomeVisible() {
            calls.add("ensureBrowseHomeVisible");
        }

        @Override
        public void scrollBrowseToTop() {
            calls.add("scrollBrowseToTop");
        }

        @Override
        public void focusSearchInput() {
            calls.add("focusSearchInput");
        }

        @Override
        public void scrollRecentThreadsIntoView() {
            calls.add("scrollRecentThreadsIntoView");
        }

        @Override
        public void prepareSavedGuidesDestination() {
            calls.add("prepareSavedGuidesDestination");
        }
    }
}
