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
            "Ask follow-up",
            DetailActivity.resolveDockedComposerHint(
                "Ask another question without leaving this thread",
                "Ask follow-up",
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
                "Ask follow-up",
                false
            )
        );
    }

    @Test
    public void followUpPanelUsesContractEligibilityForAnswerPostures() {
        assertTrue(DetailActivity.shouldShowDetailFollowUpPanel(
            DetailSurfaceContract.answer(DetailSurfaceContract.AnswerKind.GENERATED)
        ));
        assertTrue(DetailActivity.shouldShowDetailFollowUpPanel(
            DetailSurfaceContract.answer(DetailSurfaceContract.AnswerKind.DETERMINISTIC)
        ));
        assertFalse(DetailActivity.shouldShowDetailFollowUpPanel(
            DetailSurfaceContract.answer(DetailSurfaceContract.AnswerKind.ABSTAIN)
        ));
        assertFalse(DetailActivity.shouldShowDetailFollowUpPanel(
            DetailSurfaceContract.answer(DetailSurfaceContract.AnswerKind.UNCERTAIN_FIT)
        ));
    }

    @Test
    public void followUpPanelStaysHiddenForGuidePosture() {
        assertFalse(DetailActivity.shouldShowDetailFollowUpPanel(DetailSurfaceContract.guide()));
    }

    @Test
    public void phonePortraitAnswerSourcesExpandByDefaultAfterStableAnswer() {
        assertTrue(DetailActivity.shouldExpandPhonePortraitSourcesByDefault(true, true, false, 3));
        assertFalse(DetailActivity.shouldExpandPhonePortraitSourcesByDefault(true, true, true, 3));
        assertFalse(DetailActivity.shouldExpandPhonePortraitSourcesByDefault(true, true, false, 0));
        assertFalse(DetailActivity.shouldExpandPhonePortraitSourcesByDefault(false, true, false, 3));
        assertFalse(DetailActivity.shouldExpandPhonePortraitSourcesByDefault(true, false, false, 3));
    }

    @Test
    public void phonePortraitHidesDuplicateInlineSourcePreviewWhenSourcesAreExpanded() {
        assertTrue(DetailActivity.shouldHideInlineSourcePreviewForPhonePortrait(true, true, true));
        assertFalse(DetailActivity.shouldHideInlineSourcePreviewForPhonePortrait(true, true, false));
        assertFalse(DetailActivity.shouldHideInlineSourcePreviewForPhonePortrait(false, true, true));
        assertFalse(DetailActivity.shouldHideInlineSourcePreviewForPhonePortrait(true, false, true));
    }

    @Test
    public void phoneLandscapeHidesInlineSourcesWhenSideRailOwnsSources() {
        assertTrue(DetailActivity.shouldHideInlineSourcesForAnswerLayout(true, false, false, true));
        assertTrue(DetailActivity.shouldHideInlineSourcesForAnswerLayout(true, true, false, false));
        assertTrue(DetailActivity.shouldHideInlineSourcesForAnswerLayout(true, false, true, false));
        assertTrue(DetailActivity.shouldHideInlineSourcesForAnswerLayout(false, false, false, false));
        assertFalse(DetailActivity.shouldHideInlineSourcesForAnswerLayout(true, false, false, false));
    }

    @Test
    public void phonePortraitProofSummaryUsesTighterCollapsedBodyBudget() {
        assertEquals(2, DetailActivity.resolveWhyTextMaxLines(false, true, true));
        assertEquals(4, DetailActivity.resolveWhyTextMaxLines(false, true, false));
        assertEquals(4, DetailActivity.resolveWhyTextMaxLines(true, true, true));
        assertEquals(Integer.MAX_VALUE, DetailActivity.resolveWhyTextMaxLines(false, false, true));
    }

    @Test
    public void phonePortraitHeaderUsesShortAnswerLabel() {
        assertEquals(
            "Answer GD-345 - Rain shelter",
            DetailActivity.buildPhonePortraitAnswerHeaderTitle("GD-345", "Rain shelter")
        );
        assertEquals("Answer GD-345", DetailActivity.buildPhonePortraitAnswerHeaderTitle("GD-345", ""));
        assertEquals("Answer", DetailActivity.buildPhonePortraitAnswerHeaderTitle("", ""));
    }

    @Test
    public void deterministicPhonePortraitKeepsAnchorChipVisible() {
        assertTrue(DetailActivity.shouldShowAnswerAnchorChip(true, true, true));
        assertFalse(DetailActivity.shouldShowAnswerAnchorChip(true, true, false));
        assertTrue(DetailActivity.shouldShowAnswerAnchorChip(true, false, false));
        assertFalse(DetailActivity.shouldShowAnswerAnchorChip(false, false, true));
    }

    @Test
    public void landscapePhoneUsesSideRegionsForSourcesAndThread() {
        assertTrue(DetailActivity.shouldUseLandscapePhoneSourceRail(true, true));
        assertFalse(DetailActivity.shouldUseLandscapePhoneSourceRail(false, true));
        assertFalse(DetailActivity.shouldUseLandscapePhoneSourceRail(true, false));

        assertTrue(DetailActivity.shouldUseSideThreadPanel(true, false, false));
        assertTrue(DetailActivity.shouldUseSideThreadPanel(false, true, false));
        assertTrue(DetailActivity.shouldUseSideThreadPanel(false, false, true));
        assertFalse(DetailActivity.shouldUseSideThreadPanel(false, false, false));
    }

    @Test
    public void phoneLandscapeAnswerResetsToHeaderAfterRender() {
        assertTrue(DetailActivity.shouldResetPhoneLandscapeAnswerScroll(true, true));
        assertFalse(DetailActivity.shouldResetPhoneLandscapeAnswerScroll(false, true));
        assertFalse(DetailActivity.shouldResetPhoneLandscapeAnswerScroll(true, false));
    }

    @Test
    public void answerModeSuppressesLegacyBodyMirror() {
        assertTrue(DetailActivity.shouldHideBodyMirrorForAnswerMode(true));
        assertFalse(DetailActivity.shouldHideBodyMirrorForAnswerMode(false));
    }

    @Test
    public void phonePortraitSourceCardLabelCarriesMetaTitleAndQuote() {
        DetailSourcePresentationFormatter.EvidenceCard card =
            new DetailSourcePresentationFormatter.EvidenceCard(
                "GD-345",
                "ANCHOR",
                "93%",
                "Tarp & Cord Shelters",
                "A simple ridgeline shelter requires only tarp, cord, and two anchor points.",
                "",
                true
            );

        assertEquals(
            "GD-345 - ANCHOR - 93%\nTarp & Cord Shelters\n\"A simple ridgeline shelter requires only tarp, cord, and two anchor points.\"",
            DetailActivity.buildPhonePortraitSourceCardLabel(card)
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
