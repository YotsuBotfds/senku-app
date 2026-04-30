package com.senku.mobile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class DeveloperToolsPolicyTest {
    @Test
    public void productionFacingModeDoesNotShowDeveloperTools() {
        assertFalse(DeveloperToolsPolicy.shouldShowDeveloperToolsPanel(
            false,
            false,
            true,
            true
        ));
    }

    @Test
    public void debugBrowseModeCanShowDeveloperTools() {
        assertTrue(DeveloperToolsPolicy.shouldShowDeveloperToolsPanel(
            true,
            false,
            true,
            true
        ));
    }

    @Test
    public void productReviewModeDoesNotShowDeveloperToolsEvenInDebug() {
        assertFalse(DeveloperToolsPolicy.shouldShowDeveloperToolsPanel(
            true,
            true,
            true,
            true
        ));
    }

    @Test
    public void resultSurfacesDoNotShowDeveloperTools() {
        assertFalse(DeveloperToolsPolicy.shouldShowDeveloperToolsPanel(
            true,
            false,
            false,
            true
        ));
    }
}
