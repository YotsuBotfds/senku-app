package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class DetailSourcePreviewPolicyTokenTest {
    @Test
    public void phoneSourceEntryPreviewUsesCompactReadablePolicyDensity() {
        assertEquals(10, DetailActivity.resolvePhonePortraitSourceCardHorizontalPaddingDp());
        assertEquals(5, DetailActivity.resolvePhonePortraitSourceCardVerticalPaddingDp(false));
        assertEquals(3, DetailActivity.resolvePhonePortraitSourceCardVerticalPaddingDp(true));
        assertEquals(5, DetailActivity.resolvePhonePortraitSourceCardTopMarginDp());
        assertEquals(4, DetailActivity.resolvePhonePortraitSourceCardTopMarginDp(true));
        assertEquals(10.0f, DetailActivity.resolvePhonePortraitSourceCardTextSizeSp(), 0.0f);
        assertEquals(9.5f, DetailActivity.resolvePhonePortraitSourceCardTextSizeSp(true), 0.0f);

        assertEquals(3, DetailActivity.resolvePhonePortraitSourceCardMaxLines(false, false));
        assertEquals(3, DetailActivity.resolvePhonePortraitSourceCardMaxLines(false, true));
        assertEquals(2, DetailActivity.resolvePhonePortraitSourceCardMaxLines(true, false));
        assertEquals(3, DetailActivity.resolvePhonePortraitSourceCardMaxLines(true, true));
    }

    @Test
    public void phoneRelatedGuidePreviewKeepsOpenCtaClearOfComposerByPolicy() {
        assertEquals(3, DetailActivity.resolveRelatedGuidePreviewCollapsedMaxLines(true, true));
        assertEquals(5, DetailActivity.resolveRelatedGuidePreviewCollapsedMaxLines(true, false));
        assertEquals(5, DetailActivity.resolveRelatedGuidePreviewCollapsedMaxLines(false, true));

        assertTrue(DetailActivity.shouldApplyRelatedGuidePreviewCtaClearance(
            true,
            true,
            true,
            true,
            true
        ));
        assertFalse(DetailActivity.shouldApplyRelatedGuidePreviewCtaClearance(
            true,
            true,
            true,
            false,
            true
        ));
        assertFalse(DetailActivity.shouldApplyRelatedGuidePreviewCtaClearance(
            false,
            true,
            true,
            true,
            true
        ));
        assertEquals(
            28,
            DetailActivity.resolveRelatedGuidePreviewCtaBottomClearancePx(8, 12, 16)
        );
    }
}
