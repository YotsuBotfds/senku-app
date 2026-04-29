package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

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
    public void sharedInputSubmitRoutesToAskWhenAskOwnsTheVisibleFlow() {
        assertEquals(
            MainActivity.SubmitTarget.ASK,
            MainActivity.resolveSharedSubmitTarget(BottomTabDestination.ASK, false)
        );
        assertEquals(
            MainActivity.SubmitTarget.ASK,
            MainActivity.resolveSharedSubmitTarget(BottomTabDestination.HOME, true)
        );
        assertTrue(MainActivity.shouldSubmitSharedInputAsAsk(BottomTabDestination.ASK, false));
        assertTrue(MainActivity.shouldSubmitSharedInputAsAsk(BottomTabDestination.HOME, true));
    }

    @Test
    public void sharedInputSubmitStaysSearchForLibraryAndSavedFlows() {
        assertEquals(
            MainActivity.SubmitTarget.SEARCH,
            MainActivity.resolveSharedSubmitTarget(BottomTabDestination.HOME, false)
        );
        assertEquals(
            MainActivity.SubmitTarget.SEARCH,
            MainActivity.resolveSharedSubmitTarget(BottomTabDestination.SEARCH, false)
        );
        assertEquals(
            MainActivity.SubmitTarget.SEARCH,
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
