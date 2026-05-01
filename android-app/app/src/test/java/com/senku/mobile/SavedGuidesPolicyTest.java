package com.senku.mobile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.senku.ui.primitives.BottomTabDestination;

import org.junit.Test;

public final class SavedGuidesPolicyTest {
    @Test
    public void pinsBrowseFlowShowsEmptySavedSection() {
        assertTrue(SavedGuidesPolicy.shouldShowSection(true, BottomTabDestination.PINS, 0));
    }

    @Test
    public void homeAndAskBrowseFlowsHideEmptySavedSection() {
        assertFalse(SavedGuidesPolicy.shouldShowSection(true, BottomTabDestination.HOME, 0));
        assertFalse(SavedGuidesPolicy.shouldShowSection(true, BottomTabDestination.ASK, 0));
    }

    @Test
    public void nonEmptySavedGuidesShowAcrossBrowseFlows() {
        assertTrue(SavedGuidesPolicy.shouldShowSection(true, BottomTabDestination.HOME, 1));
        assertTrue(SavedGuidesPolicy.shouldShowSection(true, BottomTabDestination.ASK, 2));
        assertTrue(SavedGuidesPolicy.shouldShowSection(true, BottomTabDestination.PINS, 12));
    }

    @Test
    public void nonBrowseFlowsHideSavedSectionEvenWhenSavedGuidesExist() {
        assertFalse(SavedGuidesPolicy.shouldShowSection(false, BottomTabDestination.PINS, 0));
        assertFalse(SavedGuidesPolicy.shouldShowSection(false, BottomTabDestination.HOME, 1));
        assertFalse(SavedGuidesPolicy.shouldShowSection(false, BottomTabDestination.ASK, 2));
    }

    @Test
    public void savedDestinationLoadsBrowseGuidesOnlyWhenRepositoryReadyAndGuidesMissing() {
        assertTrue(SavedGuidesPolicy.shouldLoadBrowseGuidesForDestination(true, 0));

        assertFalse(SavedGuidesPolicy.shouldLoadBrowseGuidesForDestination(true, 1));
        assertFalse(SavedGuidesPolicy.shouldLoadBrowseGuidesForDestination(false, 0));
    }

    @Test
    public void savedPhoneFlowIntentFollowsRouteSelectionOwner() {
        assertTrue(SavedGuidesPolicy.isSavedPhoneFlowIntent(BottomTabDestination.PINS));

        assertFalse(SavedGuidesPolicy.isSavedPhoneFlowIntent(BottomTabDestination.HOME));
        assertFalse(SavedGuidesPolicy.isSavedPhoneFlowIntent(BottomTabDestination.SEARCH));
        assertFalse(SavedGuidesPolicy.isSavedPhoneFlowIntent(BottomTabDestination.ASK));
        assertFalse(SavedGuidesPolicy.isSavedPhoneFlowIntent(BottomTabDestination.THREADS));
        assertFalse(SavedGuidesPolicy.isSavedPhoneFlowIntent(null));
    }

    @Test
    public void focusRequiresPendingBrowseAndVisibleSection() {
        assertTrue(SavedGuidesPolicy.shouldFocusSection(true, true, true));

        assertFalse(SavedGuidesPolicy.shouldFocusSection(false, true, true));
        assertFalse(SavedGuidesPolicy.shouldFocusSection(true, false, true));
        assertFalse(SavedGuidesPolicy.shouldFocusSection(true, true, false));
    }
}
