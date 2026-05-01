package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class DetailSourceProvenanceCoordinatorTest {
    @Test
    public void sourceGraphRevealRequiresAnswerCompactSelectedSourceAndRelatedGuides() {
        assertTrue(DetailSourceProvenanceCoordinator.shouldRevealAnswerSourceGraphAfterSelection(
            true,
            true,
            "gd-345|tarp",
            3
        ));
        assertFalse(DetailSourceProvenanceCoordinator.shouldRevealAnswerSourceGraphAfterSelection(
            true,
            true,
            "   ",
            3
        ));
        assertFalse(DetailSourceProvenanceCoordinator.shouldRevealAnswerSourceGraphAfterSelection(
            true,
            true,
            "gd-345|tarp",
            0
        ));
        assertFalse(DetailSourceProvenanceCoordinator.shouldRevealAnswerSourceGraphAfterSelection(
            true,
            false,
            "gd-345|tarp",
            3
        ));
        assertFalse(DetailSourceProvenanceCoordinator.shouldRevealAnswerSourceGraphAfterSelection(
            false,
            true,
            "gd-345|tarp",
            3
        ));
    }

    @Test
    public void sourceGraphExpansionRequiresAnswerCompactAndSelectedSource() {
        assertTrue(DetailSourceProvenanceCoordinator.shouldExpandAnswerSourceGraphAfterSourceSelection(
            true,
            true,
            "gd-345|tarp"
        ));
        assertFalse(DetailSourceProvenanceCoordinator.shouldExpandAnswerSourceGraphAfterSourceSelection(
            true,
            true,
            null
        ));
        assertFalse(DetailSourceProvenanceCoordinator.shouldExpandAnswerSourceGraphAfterSourceSelection(
            true,
            false,
            "gd-345|tarp"
        ));
        assertFalse(DetailSourceProvenanceCoordinator.shouldExpandAnswerSourceGraphAfterSourceSelection(
            false,
            true,
            "gd-345|tarp"
        ));
    }

    @Test
    public void previewCtaClearanceRequiresPhoneAnswerComposerAndVisibleCta() {
        assertTrue(DetailSourceProvenanceCoordinator.shouldApplyRelatedGuidePreviewCtaClearance(
            true,
            true,
            true,
            true,
            true
        ));
        assertFalse(DetailSourceProvenanceCoordinator.shouldApplyRelatedGuidePreviewCtaClearance(
            false,
            true,
            true,
            true,
            true
        ));
        assertFalse(DetailSourceProvenanceCoordinator.shouldApplyRelatedGuidePreviewCtaClearance(
            true,
            false,
            true,
            true,
            true
        ));
        assertFalse(DetailSourceProvenanceCoordinator.shouldApplyRelatedGuidePreviewCtaClearance(
            true,
            true,
            false,
            true,
            true
        ));
        assertFalse(DetailSourceProvenanceCoordinator.shouldApplyRelatedGuidePreviewCtaClearance(
            true,
            true,
            true,
            false,
            true
        ));
        assertFalse(DetailSourceProvenanceCoordinator.shouldApplyRelatedGuidePreviewCtaClearance(
            true,
            true,
            true,
            true,
            false
        ));
    }

    @Test
    public void ctaBottomClearanceKeepsExistingPaddingOrComposerHeightPlusExtra() {
        assertEquals(
            112,
            DetailSourceProvenanceCoordinator.resolveRelatedGuidePreviewCtaBottomClearancePx(14, 96, 16)
        );
        assertEquals(
            120,
            DetailSourceProvenanceCoordinator.resolveRelatedGuidePreviewCtaBottomClearancePx(120, 80, 16)
        );
        assertEquals(
            0,
            DetailSourceProvenanceCoordinator.resolveRelatedGuidePreviewCtaBottomClearancePx(-12, -4, -8)
        );
    }

    @Test
    public void scrollYKeepsTargetBottomVisibleWithoutScrollingBackward() {
        assertEquals(
            228,
            DetailSourceProvenanceCoordinator.computeScrollYToKeepViewBottomVisible(680, 48, 560, 60, 0)
        );
        assertEquals(
            300,
            DetailSourceProvenanceCoordinator.computeScrollYToKeepViewBottomVisible(680, 48, 560, 60, 300)
        );
        assertEquals(
            0,
            DetailSourceProvenanceCoordinator.computeScrollYToKeepViewBottomVisible(-20, -10, 560, -30, -1)
        );
    }
}
