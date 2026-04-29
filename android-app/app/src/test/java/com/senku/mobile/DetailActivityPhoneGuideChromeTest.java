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
        assertEquals(0.71f, DetailActivity.resolveLandscapeDetailSideColumnWeight(true), 0.001f);
        assertEquals(10, DetailActivity.resolvePhoneLandscapeGuideSectionRailTopPaddingDp());
    }

    @Test
    public void guidePhoneViewportUsesTargetPaperInsets() {
        assertEquals(4, DetailActivity.resolvePhoneGuideViewportTopPaddingDp(true));
        assertEquals(16, DetailActivity.resolvePhoneGuideViewportHorizontalPaddingDp(true));
        assertEquals(8, DetailActivity.resolvePhoneGuideViewportTopPaddingDp(false));
        assertEquals(6, DetailActivity.resolvePhoneGuideViewportHorizontalPaddingDp(false));
    }

    @Test
    public void singlePaperPhoneGuideShellGetsFullWidthPaperDensity() {
        assertEquals(18, DetailActivity.resolvePhoneGuideBodyShellHorizontalPaddingDp(true, true));
        assertEquals(18, DetailActivity.resolvePhoneGuideBodyShellHorizontalPaddingDp(true, false));
        assertEquals(8, DetailActivity.resolvePhoneGuideBodyShellTopPaddingDp(true, true));
        assertEquals(10, DetailActivity.resolvePhoneGuideBodyShellTopPaddingDp(true, false));
        assertEquals(18, DetailActivity.resolvePhoneGuideBodyShellBottomPaddingDp(true, true));
        assertEquals(26, DetailActivity.resolvePhoneGuideBodyShellBottomPaddingDp(true, false));
        assertEquals(3, DetailActivity.resolvePhoneGuideBodyLineSpacingExtraDp(true));
        assertEquals(4, DetailActivity.resolvePhoneGuideBodyLineSpacingExtraDp(false));
        assertEquals(0, DetailActivity.resolvePhoneGuideAnswerBubbleTopMarginDp(true));
        assertEquals(4, DetailActivity.resolvePhoneGuideAnswerBubbleTopMarginDp(false));
        assertEquals(16.0f, DetailActivity.resolvePhoneGuideBodyTextSizeSp(true), 0.001f);
        assertEquals(17.0f, DetailActivity.resolvePhoneGuideBodyTextSizeSp(false), 0.001f);
    }
}
