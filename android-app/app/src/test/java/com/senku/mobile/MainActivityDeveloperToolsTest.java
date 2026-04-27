package com.senku.mobile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class MainActivityDeveloperToolsTest {
    @Test
    public void productReviewModeHidesDeveloperPanelOnBrowseHome() {
        assertFalse(MainActivity.shouldShowDeveloperToolsPanel(
            true,
            true,
            true
        ));
    }

    @Test
    public void developerPanelRemainsAvailableOutsideProductReviewModeOnBrowseHome() {
        assertTrue(MainActivity.shouldShowDeveloperToolsPanel(
            false,
            true,
            true
        ));
    }

    @Test
    public void developerPanelStaysHiddenOnResultSurfaces() {
        assertFalse(MainActivity.shouldShowDeveloperToolsPanel(
            false,
            false,
            true
        ));
    }

    @Test
    public void developerPanelOptOutPreservesEmptyBrowseAccess() {
        assertTrue(MainActivity.shouldShowDeveloperToolsPanel(
            false,
            true,
            false
        ));
    }
}
