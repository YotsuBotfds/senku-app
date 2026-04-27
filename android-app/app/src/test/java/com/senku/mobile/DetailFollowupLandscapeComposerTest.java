package com.senku.mobile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.senku.ui.primitives.MetaItem;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

public final class DetailFollowupLandscapeComposerTest {
    @Test
    public void dockedComposerHidesRetryChromeOnLandscapePhone() {
        assertFalse(DetailActivity.shouldShowDockedComposerRetry(true, true));
    }

    @Test
    public void dockedComposerKeepsRetryChromeOutsideLandscapePhone() {
        assertTrue(DetailActivity.shouldShowDockedComposerRetry(true, false));
        assertFalse(DetailActivity.shouldShowDockedComposerRetry(false, false));
    }

    @Test
    public void followUpSuggestionsHideOnLandscapePhone() {
        assertTrue(DetailActivity.shouldHideFollowUpSuggestionsOnPhoneLandscape(true));
        assertFalse(DetailActivity.shouldHideFollowUpSuggestionsOnPhoneLandscape(false));
    }

    @Test
    public void landscapePhoneFollowUpPanelUsesTightVerticalBudget() {
        assertEquals(6, DetailActivity.resolveFollowUpPanelVerticalPaddingDp(true, true));
    }

    @Test
    public void followUpPanelKeepsExistingVerticalPaddingOutsideLandscapePhone() {
        assertEquals(10, DetailActivity.resolveFollowUpPanelVerticalPaddingDp(false, true));
        assertEquals(14, DetailActivity.resolveFollowUpPanelVerticalPaddingDp(false, false));
    }

    @Test
    public void landscapeComposerDoesNotStealFocusFromInitialRenderLegacyFocus() {
        assertFalse(DetailActivity.shouldRequestLandscapeDockedComposerFocus(true, true, true, false));
    }

    @Test
    public void landscapeComposerPreservesUserLegacyInputFocus() {
        assertTrue(DetailActivity.shouldRequestLandscapeDockedComposerFocus(true, true, true, true));
        assertFalse(DetailActivity.shouldRequestLandscapeDockedComposerFocus(false, true, true, true));
        assertFalse(DetailActivity.shouldRequestLandscapeDockedComposerFocus(true, false, true, true));
        assertFalse(DetailActivity.shouldRequestLandscapeDockedComposerFocus(true, true, false, true));
    }

    @Test
    public void compactDockedComposerUsesCompactHint() {
        assertEquals(
            "Ask follow-up...",
            DetailActivity.resolveDockedComposerHint(
                "Ask another question without leaving this thread",
                "Ask follow-up...",
                true
            )
        );
    }

    @Test
    public void compactDockedComposerUsesCompactHintResource() {
        assertEquals(
            R.string.detail_followup_hint_compact,
            DetailActivity.resolveDockedComposerCompactHintResId(true)
        );
    }

    @Test
    public void dockedComposerKeepsFieldHintAsCompactResourceFallback() {
        assertEquals(
            R.string.detail_loop4_followup_hint_compact,
            DetailActivity.resolveDockedComposerCompactHintResId(false)
        );
    }

    @Test
    public void dockedComposerKeepsFullHintOutsideCompactMode() {
        assertEquals(
            "Ask another question without leaving this thread",
            DetailActivity.resolveDockedComposerHint(
                "Ask another question without leaving this thread",
                "Ask follow-up...",
                false
            )
        );
    }

    @Test
    public void metaStripAppendsFreshnessTokens() {
        ArrayList<MetaItem> items = new ArrayList<>();
        DetailActivity.appendMetaStripTokens(
            items,
            Arrays.asList("rev 04-27", "", "pack 12", "  hash abc123  ")
        );

        assertEquals(3, items.size());
        assertEquals("rev 04-27", items.get(0).getLabel());
        assertEquals("pack 12", items.get(1).getLabel());
        assertEquals("hash abc123", items.get(2).getLabel());
    }
}
