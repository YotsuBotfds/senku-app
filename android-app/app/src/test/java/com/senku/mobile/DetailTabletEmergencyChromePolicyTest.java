package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.view.ViewGroup;

import java.util.List;

import org.junit.Test;

public final class DetailTabletEmergencyChromePolicyTest {
    @Test
    public void portraitEmergencyDecisionOwnsFullHeightChromeAndRail() {
        DetailTabletEmergencyChromePolicy.Decision decision =
            DetailTabletEmergencyChromePolicy.evaluate(true, true, true);

        assertTrue(decision.fullHeightPage);
        assertTrue(decision.suppressStaleChrome);
        assertTrue(decision.hideDetailRoot);
        assertTrue(decision.suppressFloatingRail);
        assertTrue(decision.showAppRailOverlay);
        assertTrue(decision.showOverlayChrome);
        assertEquals(73, decision.overlayMarginsDp.left);
        assertEquals(24, decision.overlayMarginsDp.right);
        assertEquals(0, decision.overlayMarginsDp.top);
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, decision.overlayHeight);
    }

    @Test
    public void landscapeEmergencyKeepsOverlayAnchoredWithoutFullHeightChrome() {
        DetailTabletEmergencyChromePolicy.Decision decision =
            DetailTabletEmergencyChromePolicy.evaluate(true, false, true);

        assertFalse(decision.fullHeightPage);
        assertFalse(decision.suppressStaleChrome);
        assertFalse(decision.hideDetailRoot);
        assertFalse(decision.suppressFloatingRail);
        assertFalse(decision.showAppRailOverlay);
        assertFalse(decision.showOverlayChrome);
        assertEquals(336, decision.overlayMarginsDp.left);
        assertEquals(24, decision.overlayMarginsDp.right);
        assertEquals(16, decision.overlayMarginsDp.top);
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, decision.overlayHeight);
    }

    @Test
    public void chromeSizingMatchesExistingTabletOverlayTokens() {
        assertEquals(16, DetailTabletEmergencyChromePolicy.portraitHorizontalPaddingDp());
        assertEquals(12, DetailTabletEmergencyChromePolicy.portraitVerticalPaddingDp());
        assertEquals(12, DetailTabletEmergencyChromePolicy.chromeBottomMarginDp());
        assertEquals(28, DetailTabletEmergencyChromePolicy.chromeNavIconSizeDp());
        assertEquals(28, DetailTabletEmergencyChromePolicy.chromeBackActionMinWidthDp());
        assertEquals(18, DetailTabletEmergencyChromePolicy.chromeBackIconSizeDp());
        assertEquals(6, DetailTabletEmergencyChromePolicy.chromeBackHorizontalPaddingDp());
        assertEquals(10, DetailTabletEmergencyChromePolicy.chromeDividerGapDp());
        assertEquals(24, DetailTabletEmergencyChromePolicy.chromeDividerHeightDp());
        assertEquals(1, DetailTabletEmergencyChromePolicy.chromeRuleHeightDp());
        assertEquals(-16, DetailTabletEmergencyChromePolicy.chromeRuleHorizontalInsetDp(true));
        assertEquals(-18, DetailTabletEmergencyChromePolicy.chromeRuleHorizontalInsetDp(false));
        assertEquals(16, DetailTabletEmergencyChromePolicy.dangerBandHorizontalBleedDp(true));
        assertEquals(0, DetailTabletEmergencyChromePolicy.dangerBandHorizontalBleedDp(false));
    }

    @Test
    public void chromeTypeAndMetaFormattingStayStable() {
        assertEquals(13.0f, DetailTabletEmergencyChromePolicy.chromeTitleTextSizeSp(), 0.0f);
        assertEquals(18.0f, DetailTabletEmergencyChromePolicy.chromeTitleLineHeightSp(), 0.0f);
        assertEquals(9.5f, DetailTabletEmergencyChromePolicy.chromeMetaTextSizeSp(), 0.0f);
        assertEquals(11.0f, DetailTabletEmergencyChromePolicy.chromeMetaLineHeightSp(), 0.0f);
        assertEquals(10.0f, DetailTabletEmergencyChromePolicy.chromeBackLabelTextSizeSp(), 0.0f);
        assertEquals(12.0f, DetailTabletEmergencyChromePolicy.chromeBackLabelLineHeightSp(), 0.0f);
        assertEquals(0.09f, DetailTabletEmergencyChromePolicy.chromeLabelLetterSpacing(), 0.0f);
        assertEquals(
            "GD-898 \u2022 DANGER \u2022 REVIEWED EVIDENCE",
            DetailTabletEmergencyChromePolicy.buildChromeMeta(List.of("GD-898", "danger", "", "reviewed evidence"))
        );
    }

    @Test
    public void appRailChromeNamesManualExitAndKeepsAskActive() {
        DetailTabletEmergencyChromePolicy.Decision decision =
            DetailTabletEmergencyChromePolicy.evaluate(true, true, true);

        assertEquals(72, decision.appRailWidthDp);
        assertEquals(1, decision.appRailDividerWidthDp);
        assertEquals(10.0f, decision.appRailLabelTextSizeSp, 0.0f);
        assertEquals(13.0f, decision.appRailLabelLineHeightSp, 0.0f);
        assertEquals(0.0f, decision.appRailLabelLetterSpacing, 0.0f);
        assertEquals(R.string.detail_emergency_app_rail_manual_label, decision.appRailHomeLabelResource);
        assertEquals(
            R.string.detail_emergency_app_rail_manual_content_description,
            decision.appRailHomeContentDescriptionResource
        );
        assertFalse(decision.showHomeChromeAction);
        assertEquals(DetailTabletEmergencyChromePolicy.AppRailDestination.ASK, decision.activeDestination);
        assertTrue(DetailTabletEmergencyChromePolicy.isDestinationActive(
            DetailTabletEmergencyChromePolicy.AppRailDestination.ASK
        ));
    }
}
