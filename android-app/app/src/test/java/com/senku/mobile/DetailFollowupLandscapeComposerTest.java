package com.senku.mobile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
}
