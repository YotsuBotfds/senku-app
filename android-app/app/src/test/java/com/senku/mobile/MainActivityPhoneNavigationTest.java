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
    public void everyVisiblePhoneTabOwnsItsSelectionRole() {
        for (BottomTabDestination visibleTab : MainActivity.buildVisiblePhoneTabDestinations()) {
            assertEquals(
                visibleTab,
                MainActivity.phoneTabSelectionOwner(visibleTab)
            );
        }
    }

    @Test
    public void askOwnsRecentThreadSelectionRole() {
        assertEquals(
            BottomTabDestination.ASK,
            MainActivity.phoneTabSelectionOwner(BottomTabDestination.THREADS)
        );
    }

    @Test
    public void askOwnsAskSelectionRole() {
        assertEquals(
            BottomTabDestination.ASK,
            MainActivity.phoneTabSelectionOwner(BottomTabDestination.ASK)
        );
    }

    @Test
    public void libraryOwnsBrowseAndSearchGuideResultRoles() {
        assertEquals(
            BottomTabDestination.HOME,
            MainActivity.phoneTabSelectionOwner(BottomTabDestination.HOME)
        );
        assertEquals(
            BottomTabDestination.HOME,
            MainActivity.phoneTabSelectionOwner(BottomTabDestination.SEARCH)
        );
    }

    @Test
    public void savedOwnsPinnedGuideRole() {
        assertEquals(
            BottomTabDestination.PINS,
            MainActivity.phoneTabSelectionOwner(BottomTabDestination.PINS)
        );
    }

    @Test
    public void openSavedExtraRequestsSavedDestination() {
        assertTrue(MainActivity.shouldOpenSavedDestination(true));
    }

    @Test
    public void missingOpenSavedExtraDoesNotRequestSavedDestination() {
        assertFalse(MainActivity.shouldOpenSavedDestination(false));
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
                assertEquals(BottomTabDestination.HOME, MainActivity.phoneTabSelectionOwner(destination));
            }
            if (MainActivity.isAskPhoneFlowIntent(destination)) {
                matchingPredicateCount++;
                assertEquals(BottomTabDestination.ASK, MainActivity.phoneTabSelectionOwner(destination));
            }
            if (MainActivity.isSavedPhoneFlowIntent(destination)) {
                matchingPredicateCount++;
                assertEquals(BottomTabDestination.PINS, MainActivity.phoneTabSelectionOwner(destination));
            }

            assertEquals(1, matchingPredicateCount);
            assertTrue(MainActivity.buildVisiblePhoneTabDestinations().contains(
                MainActivity.phoneTabSelectionOwner(destination)
            ));
        }
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
    public void sharedInputChromeUsesQuestionSemanticsInAskMode() {
        assertEquals(
            R.string.ask_hint,
            MainActivity.resolveSharedInputHintResourceForTest(SubmitTarget.ASK)
        );
        assertEquals(
            R.string.ask_input_description,
            MainActivity.resolveSharedInputDescriptionResourceForTest(SubmitTarget.ASK)
        );
        assertEquals(
            EditorInfo.IME_ACTION_DONE,
            MainActivity.resolveSharedInputImeActionForTest(SubmitTarget.ASK)
        );
        assertEquals(
            R.string.ask_button_description,
            MainActivity.resolveSubmitButtonDescriptionResourceForTest(SubmitTarget.ASK)
        );

        assertEquals(
            R.string.search_hint,
            MainActivity.resolveSharedInputHintResourceForTest(SubmitTarget.SEARCH)
        );
        assertEquals(
            R.string.search_input_description,
            MainActivity.resolveSharedInputDescriptionResourceForTest(SubmitTarget.SEARCH)
        );
        assertEquals(
            EditorInfo.IME_ACTION_SEARCH,
            MainActivity.resolveSharedInputImeActionForTest(SubmitTarget.SEARCH)
        );
        assertEquals(
            R.string.search_button_description,
            MainActivity.resolveSubmitButtonDescriptionResourceForTest(SubmitTarget.SEARCH)
        );
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
}
