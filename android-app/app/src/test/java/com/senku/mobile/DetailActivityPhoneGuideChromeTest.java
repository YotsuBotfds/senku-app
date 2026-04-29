package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.senku.ui.primitives.BottomTabDestination;

import java.util.Arrays;

import org.junit.Test;

public final class DetailActivityPhoneGuideChromeTest {
    @Test
    public void guidePhoneBottomTabsUseLibraryAskSavedOrder() {
        assertEquals(
            Arrays.asList(
                BottomTabDestination.HOME,
                BottomTabDestination.ASK,
                BottomTabDestination.PINS
            ),
            DetailActivity.buildGuidePhoneBottomTabDestinations()
        );
    }

    @Test
    public void guidePhoneBottomTabsOnlyInstallForPhonePortraitGuideMode() {
        assertTrue(DetailActivity.shouldInstallGuidePhoneBottomTabBar(true, true));
        assertFalse(DetailActivity.shouldInstallGuidePhoneBottomTabBar(false, true));
        assertFalse(DetailActivity.shouldInstallGuidePhoneBottomTabBar(true, false));
    }

    @Test
    public void guidePhoneLandscapeSectionRailGetsReadableWidthAndTopBreathingRoom() {
        assertTrue(DetailActivity.shouldUsePhoneLandscapeGuideSectionRail(false, true));
        assertFalse(DetailActivity.shouldUsePhoneLandscapeGuideSectionRail(false, false));
        assertFalse(DetailActivity.shouldUsePhoneLandscapeGuideSectionRail(true, true));
        assertEquals(2.25f, DetailActivity.resolveLandscapeDetailPrimaryColumnWeight(true), 0.001f);
        assertEquals(0.62f, DetailActivity.resolveLandscapeDetailSideColumnWeight(true), 0.001f);
        assertEquals(10, DetailActivity.resolvePhoneLandscapeGuideSectionRailTopPaddingDp());
    }

    @Test
    public void guidePhoneLandscapeViewportAddsTopInsetWithoutChangingPortraitBottomTabDensity() {
        assertEquals(12, DetailActivity.resolvePhoneGuideViewportTopPaddingDp(true));
        assertEquals(16, DetailActivity.resolvePhoneGuideViewportHorizontalPaddingDp(true));
        assertEquals(8, DetailActivity.resolvePhoneGuideViewportTopPaddingDp(false));
        assertEquals(10, DetailActivity.resolvePhoneGuideViewportHorizontalPaddingDp(false));
    }
}
