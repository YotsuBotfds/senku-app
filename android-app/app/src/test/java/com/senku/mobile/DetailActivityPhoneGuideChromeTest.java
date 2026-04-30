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
        assertEquals(3, DetailActivity.resolvePhoneGuideViewportTopPaddingDp(true));
        assertEquals(5, DetailActivity.resolvePhoneGuideViewportHorizontalPaddingDp(true));
        assertEquals(8, DetailActivity.resolvePhoneGuideViewportTopPaddingDp(false));
        assertEquals(8, DetailActivity.resolvePhoneGuideViewportHorizontalPaddingDp(false));
        assertEquals(6, DetailActivity.resolvePhoneGuidePaperBottomViewportInsetDp(true));
        assertEquals(8, DetailActivity.resolvePhoneGuidePaperBottomViewportInsetDp(false));
    }

    @Test
    public void singlePaperPhoneGuideShellGetsFullWidthPaperDensity() {
        assertEquals(16, DetailActivity.resolvePhoneGuideBodyShellHorizontalPaddingDp(true, true));
        assertEquals(18, DetailActivity.resolvePhoneGuideBodyShellHorizontalPaddingDp(true, false));
        assertEquals(6, DetailActivity.resolvePhoneGuideBodyShellTopPaddingDp(true, true));
        assertEquals(10, DetailActivity.resolvePhoneGuideBodyShellTopPaddingDp(true, false));
        assertEquals(14, DetailActivity.resolvePhoneGuideBodyShellBottomPaddingDp(true, true));
        assertEquals(24, DetailActivity.resolvePhoneGuideBodyShellBottomPaddingDp(true, false));
        assertEquals(2, DetailActivity.resolvePhoneGuideBodyLineSpacingExtraDp(true));
        assertEquals(3, DetailActivity.resolvePhoneGuideBodyLineSpacingExtraDp(false));
        assertEquals(0, DetailActivity.resolvePhoneGuideAnswerBubbleTopMarginDp(true));
        assertEquals(4, DetailActivity.resolvePhoneGuideAnswerBubbleTopMarginDp(false));
        assertEquals(15.0f, DetailActivity.resolvePhoneGuideBodyTextSizeSp(true), 0.001f);
        assertEquals(15.5f, DetailActivity.resolvePhoneGuideBodyTextSizeSp(false), 0.001f);
    }

    @Test
    public void phonePortraitGuideOverflowStaysHiddenBecauseActionsAreExplicit() {
        assertFalse(DetailActivity.shouldShowDetailOverflowAction(false, true, true, "GD-132"));
        assertFalse(DetailActivity.shouldShowDetailOverflowAction(false, false, true, "GD-132"));
        assertFalse(DetailActivity.shouldShowDetailOverflowAction(false, true, false, "GD-132"));
        assertFalse(DetailActivity.shouldShowDetailOverflowAction(false, true, true, ""));
    }

    @Test
    public void phoneAnswerOverflowProvidesSavePathWhenDirectPinIsSuppressed() {
        assertTrue(DetailActivity.shouldShowDetailOverflowAction(true, true, true, "GD-132"));
        assertTrue(DetailActivity.shouldShowDetailOverflowAction(true, true, false, "GD-132"));
        assertFalse(DetailActivity.shouldShowDetailOverflowAction(true, true, true, ""));
        assertFalse(DetailActivity.shouldShowDetailOverflowAction(true, true, true, "   "));
        assertFalse(DetailActivity.shouldShowDetailOverflowAction(true, false, true, "GD-132"));
    }

    @Test
    public void productReviewModeRequiresAuthorizedDetailIntentExtra() {
        assertFalse(DetailActivity.resolveProductReviewModeForTest(false, false));
        assertFalse(DetailActivity.resolveProductReviewModeForTest(false, true));
        assertFalse(DetailActivity.resolveProductReviewModeForTest(true, false));
        assertFalse(DetailActivity.resolveProductReviewModeForTest(true, true));
        assertTrue(DetailActivity.resolveProductReviewModeForTest(true, true, true, true));
        assertFalse(DetailActivity.resolveProductReviewModeForTest(true, true, true, false));
    }

    @Test
    public void portraitGuidePaperKeepsOuterInsetWiderThanLandscape() {
        assertTrue(
            DetailActivity.resolvePhoneGuideViewportHorizontalPaddingDp(false) >
                DetailActivity.resolvePhoneGuideViewportHorizontalPaddingDp(true)
        );
        assertTrue(
            DetailActivity.resolvePhoneGuideBodyShellHorizontalPaddingDp(true, false) >
                DetailActivity.resolvePhoneGuideBodyShellHorizontalPaddingDp(true, true)
        );
    }
}
