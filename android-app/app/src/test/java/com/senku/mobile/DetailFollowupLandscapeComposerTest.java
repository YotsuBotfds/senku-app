package com.senku.mobile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import com.senku.ui.primitives.MetaItem;
import com.senku.ui.tablet.TabletDetailMode;

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
    public void followUpSuggestionsHideForCompactPortraitThreadClearance() {
        assertTrue(DetailActivity.shouldHideFollowUpSuggestionsForComposerClearance(false, true, true, true));
        assertTrue(DetailActivity.shouldHideFollowUpSuggestionsForComposerClearance(true, false, false, true));
        assertTrue(DetailActivity.shouldHideFollowUpSuggestionsForComposerClearance(false, true, false, true));
        assertFalse(DetailActivity.shouldHideFollowUpSuggestionsForComposerClearance(false, true, false, false));
        assertFalse(DetailActivity.shouldHideFollowUpSuggestionsForComposerClearance(false, false, true, true));
    }

    @Test
    public void landscapePhoneFollowUpPanelUsesTightVerticalBudget() {
        assertEquals(6, DetailActivity.resolveFollowUpPanelVerticalPaddingDp(true, true));
    }

    @Test
    public void followUpPanelKeepsExistingVerticalPaddingOutsideLandscapePhone() {
        assertEquals(8, DetailActivity.resolveFollowUpPanelVerticalPaddingDp(false, true));
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

        assertTrue(DetailActivity.shouldShowCollapsedPhonePortraitSourceTrigger(true, false, false, false, 3));
        assertFalse(DetailActivity.shouldShowCollapsedPhonePortraitSourceTrigger(true, true, false, false, 3));
        assertFalse(DetailActivity.shouldShowCollapsedPhonePortraitSourceTrigger(true, false, true, false, 3));
        assertFalse(DetailActivity.shouldShowCollapsedPhonePortraitSourceTrigger(true, false, false, true, 3));
        assertFalse(DetailActivity.shouldShowCollapsedPhonePortraitSourceTrigger(true, false, false, false, 0));
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
        assertEquals(Integer.MAX_VALUE, DetailActivity.resolveWhyTextMaxLines(true, true, true));
        assertEquals(Integer.MAX_VALUE, DetailActivity.resolveWhyTextMaxLines(false, false, true));
    }

    @Test
    public void phonePortraitProofRowUsesQuietCompactTitle() {
        assertEquals(
            "WHY THIS ANSWER | Limited evidence | 2 src | Show",
            DetailActivity.buildCompactPhoneProofRowTitle("Limited evidence", 2, false)
        );
        assertEquals(
            "WHY THIS ANSWER | Hide",
            DetailActivity.buildCompactPhoneProofRowTitle("", 0, true)
        );
    }

    @Test
    public void phonePortraitSourcesUseQuietCollapsedTriggerTitle() {
        assertEquals(
            "Source guides | 3 src | Show",
            DetailActivity.buildCompactPhoneSourcesTriggerTitle("Source guides", 3, false)
        );
        assertEquals(
            "SOURCES",
            DetailActivity.buildCompactPhoneSourcesTriggerTitle("", 0, true)
        );
        assertEquals(
            "SOURCES - 3",
            DetailActivity.buildCompactPhoneSourcesTriggerTitle("Source guides", 3, true)
        );
    }

    @Test
    public void phonePortraitProofPanelDefersBelowSourceTriggerExceptEmergency() {
        assertTrue(DetailActivity.shouldDeferPhonePortraitProofPanelBelowSources(true, true, false));
        assertFalse(DetailActivity.shouldDeferPhonePortraitProofPanelBelowSources(true, true, true));
        assertFalse(DetailActivity.shouldDeferPhonePortraitProofPanelBelowSources(true, false, false));
        assertFalse(DetailActivity.shouldDeferPhonePortraitProofPanelBelowSources(false, true, false));
    }

    @Test
    public void phonePortraitSourceSelectionDoesNotJumpToProvenance() {
        assertFalse(DetailActivity.shouldScrollToProvenanceOnCompactPreview(true, true));
        assertTrue(DetailActivity.shouldScrollToProvenanceOnCompactPreview(true, false));
        assertFalse(DetailActivity.shouldScrollToProvenanceOnCompactPreview(false, false));
    }

    @Test
    public void emergencyPortraitAnswerDoesNotRestoreGenericQuestionChrome() {
        assertFalse(DetailActivity.shouldRestoreAnswerSemanticPresentation(true, true));
        assertTrue(DetailActivity.shouldRestoreAnswerSemanticPresentation(true, false));
        assertFalse(DetailActivity.shouldRestoreAnswerSemanticPresentation(false, true));
    }

    @Test
    public void emergencyCompactSurfacesHideProofRailBehindSourceWhyCard() {
        assertTrue(DetailActivity.shouldHideSourcesPanelForEmergencySurface(true, true));
        assertTrue(DetailActivity.shouldHideSourcesPanelForEmergencySurface(true, false));
        assertFalse(DetailActivity.shouldHideSourcesPanelForEmergencySurface(false, false));

        assertTrue(DetailActivity.shouldHideProofChromeForEmergencySurface(true, true));
        assertFalse(DetailActivity.shouldHideProofChromeForEmergencySurface(true, false));
        assertFalse(DetailActivity.shouldHideProofChromeForEmergencySurface(false, true));

        assertTrue(DetailActivity.shouldHideRelatedGuideChromeForEmergencySurface(true, true));
        assertFalse(DetailActivity.shouldHideRelatedGuideChromeForEmergencySurface(true, false));
        assertFalse(DetailActivity.shouldHideRelatedGuideChromeForEmergencySurface(false, true));
    }

    @Test
    public void tabletEmergencyOverlayOwnsAnyTabletDetailShell() {
        assertTrue(DetailActivity.shouldUseTabletEmergencyFullHeightPage(true, true, true));
        assertFalse(DetailActivity.shouldUseTabletEmergencyFullHeightPage(true, false, true));
        assertFalse(DetailActivity.shouldUseTabletEmergencyFullHeightPage(true, true, false));
        assertFalse(DetailActivity.shouldUseTabletEmergencyFullHeightPage(false, true, true));
    }

    @Test
    public void phoneGuidePaperKeepsExtraBottomBreathingRoomForRequiredReadingRows() {
        assertEquals(20, DetailActivity.resolvePhoneGuideBodyShellBottomPaddingDp(true, false));
        assertEquals(12, DetailActivity.resolvePhoneGuideBodyShellBottomPaddingDp(true, true));
        assertEquals(16, DetailActivity.resolvePhoneGuideBodyShellBottomPaddingDp(false, false));
        assertEquals(14, DetailActivity.resolvePhoneGuideBodyShellBottomPaddingDp(false, true));
        assertEquals(2, DetailActivity.resolvePhoneGuideBodyLineSpacingExtraDp(false));
        assertEquals(2, DetailActivity.resolvePhoneGuideBodyLineSpacingExtraDp(true));
    }

    @Test
    public void phonePortraitHeaderUsesShortAnswerLabel() {
        assertEquals(
            "ANSWER GD-345 \u2022 Rain shelter",
            DetailActivity.buildPhonePortraitAnswerHeaderTitle("GD-345", "Rain shelter")
        );
        assertEquals("ANSWER GD-345", DetailActivity.buildPhonePortraitAnswerHeaderTitle("GD-345", ""));
        assertEquals("ANSWER", DetailActivity.buildPhonePortraitAnswerHeaderTitle("", ""));
        assertEquals("ANSWER GD-132 \u2022 Burn hazard", DetailActivity.buildPhoneEmergencyHeaderTitle("GD-132"));
        assertEquals("ANSWER \u2022 Burn hazard", DetailActivity.buildPhoneEmergencyHeaderTitle(""));
        assertFalse(DetailActivity.buildPhoneEmergencyHeaderTitle("GD-132").contains("\u00C3"));
        assertFalse(DetailActivity.buildPhoneEmergencyHeaderTitle("GD-132").contains("\u00E2"));
        assertTrue(DetailActivity.shouldUsePhoneAnswerHeaderTitle(true, true));
        assertFalse(DetailActivity.shouldUsePhoneAnswerHeaderTitle(true, false));
        assertFalse(DetailActivity.shouldUsePhoneAnswerHeaderTitle(false, true));
    }

    @Test
    public void phoneThreadHeaderUsesTranscriptLabel() {
        assertEquals(
            "THREAD GD-220 \u2022 Rain shelter \u2022 2 turns",
            DetailActivity.buildPhonePortraitThreadHeaderTitle("GD-220", "Rain shelter", 2)
        );
        assertEquals(
            "THREAD \u2022 2 turns",
            DetailActivity.buildPhonePortraitThreadHeaderTitle("", "", 1)
        );
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
    public void landscapePhoneThreadKeepsPrimaryAnswerAndCompactSourceRail() {
        assertTrue(DetailActivity.shouldKeepPhoneLandscapeThreadAtTop(true, 2, true));
        assertFalse(DetailActivity.shouldKeepPhoneLandscapeThreadAtTop(true, 1, true));
        assertFalse(DetailActivity.shouldKeepPhoneLandscapeThreadAtTop(true, 2, false));

        assertFalse(DetailActivity.shouldAutoOpenProvenanceForAnswerRail(true, 2, true));
        assertTrue(DetailActivity.shouldAutoOpenProvenanceForAnswerRail(true, 1, true));
        assertEquals("Sources - 2", DetailActivity.buildLandscapePhoneSourceRailTitle("Sources", 2));
        assertTrue(DetailActivity.shouldHideGenericAnswerScaffoldForThread(true, 2, true));
    }

    @Test
    public void landscapePhoneThreadPreservesTopAfterComposerSetup() {
        assertTrue(DetailActivity.shouldPreservePhoneLandscapeThreadTopAfterComposerSetup(true, 2, true));
        assertFalse(DetailActivity.shouldPreservePhoneLandscapeThreadTopAfterComposerSetup(true, 1, true));
        assertFalse(DetailActivity.shouldPreservePhoneLandscapeThreadTopAfterComposerSetup(true, 2, false));
        assertFalse(DetailActivity.shouldPreservePhoneLandscapeThreadTopAfterComposerSetup(false, 2, true));
        assertTrue(DetailActivity.shouldPreservePhoneLandscapeThreadTopAfterComposerFocus(true, 2, true));
        assertFalse(DetailActivity.shouldPreservePhoneLandscapeThreadTopAfterComposerFocus(true, 1, true));
        assertFalse(DetailActivity.shouldPreservePhoneLandscapeThreadTopAfterComposerFocus(true, 2, false));
        assertFalse(DetailActivity.shouldPreservePhoneLandscapeThreadTopAfterComposerFocus(false, 2, true));
        assertArrayEquals(
            new long[] {0L, 80L, 240L, 480L},
            DetailActivity.phoneLandscapeThreadTopPreservationDelaysMs()
        );
    }

    @Test
    public void phoneLandscapeAnswerResetsToHeaderAfterRender() {
        assertTrue(DetailActivity.shouldResetPhoneLandscapeAnswerScroll(true, true));
        assertFalse(DetailActivity.shouldResetPhoneLandscapeAnswerScroll(false, true));
        assertFalse(DetailActivity.shouldResetPhoneLandscapeAnswerScroll(true, false));
    }

    @Test
    public void restoredAnswerSemanticShellKeepsPhoneLandscapeQuestionPadding() {
        assertEquals(10, DetailActivity.resolveAnswerSemanticQuestionPaddingDp(true, false));
        assertEquals(10, DetailActivity.resolveAnswerSemanticQuestionPaddingDp(false, true));
        assertEquals(0, DetailActivity.resolveAnswerSemanticQuestionPaddingDp(false, false));
    }

    @Test
    public void answerModeSuppressesLegacyBodyMirror() {
        assertTrue(DetailActivity.shouldHideBodyMirrorForAnswerMode(true));
        assertFalse(DetailActivity.shouldHideBodyMirrorForAnswerMode(false));
    }

    @Test
    public void sharedDetailModeClassifiesAnswerThreadAndGuideShells() {
        assertEquals(TabletDetailMode.Guide, DetailActivity.resolveTabletDetailModeForState(false, 2));
        assertEquals(TabletDetailMode.Answer, DetailActivity.resolveTabletDetailModeForState(true, 1));
        assertEquals(TabletDetailMode.Answer, DetailActivity.resolveTabletDetailModeForState(true, 0));
        assertEquals(TabletDetailMode.Thread, DetailActivity.resolveTabletDetailModeForState(true, 2));

        assertFalse(DetailActivity.isThreadDetailRoute(false, 3));
        assertFalse(DetailActivity.isThreadDetailRoute(true, 1));
        assertTrue(DetailActivity.isThreadDetailRoute(true, 2));

        assertTrue(DetailActivity.shouldHideGenericAnswerScaffoldForThread(true, 2, true));
        assertFalse(DetailActivity.shouldHideGenericAnswerScaffoldForThread(true, 2, false));
        assertFalse(DetailActivity.shouldHideGenericAnswerScaffoldForThread(true, 1, true));
    }

    @Test
    public void tabletAnswerTitleUsesQuestionTopicInsteadOfSelectedSourceTitle() {
        assertEquals(
            "Rain shelter",
            DetailActivity.buildTabletAnswerTopicTitleForQuestions(
                Arrays.asList("How do I build a simple rain shelter from tarp and cord?"),
                1
            )
        );
        assertEquals(
            "Rain shelter - 2 turns",
            DetailActivity.buildTabletAnswerTopicTitleForQuestions(
                Arrays.asList(
                    "How do I build a simple rain shelter from tarp and cord?",
                    "What should I do next after the ridge line is up?"
                ),
                2
            )
        );
    }

    @Test
    public void tabletAnswerShellHonorsSubtitleSourceCountFloor() {
        assertEquals(3, DetailActivity.resolveAnswerShellSourceCount(2, "GD-345 - 3 sources - rev 04-27"));
        assertEquals(2, DetailActivity.resolveAnswerShellSourceCount(2, "GD-345 - rev 04-27"));
        assertTrue(DetailActivity.shouldInferUncertainTabletShellFromSourceSummary(
            true,
            false,
            false,
            false,
            2,
            "GD-345 - 3 sources - rev 04-27"
        ));
        assertFalse(DetailActivity.shouldInferUncertainTabletShellFromSourceSummary(
            true,
            false,
            false,
            true,
            2,
            "GD-345 - 3 sources - rev 04-27"
        ));
    }

    @Test
    public void phoneThreadDetailHidesProofRailBelowTranscript() {
        assertTrue(DetailActivity.shouldHideProofRailForThreadDetail(true, 2, true));
        assertFalse(DetailActivity.shouldHideProofRailForThreadDetail(true, 2, false));
        assertFalse(DetailActivity.shouldHideProofRailForThreadDetail(true, 1, true));
        assertFalse(DetailActivity.shouldHideProofRailForThreadDetail(false, 2, true));
    }

    @Test
    public void phoneLandscapeThreadSuppressesInlineCrossReferencesBelowTranscript() {
        assertFalse(DetailActivity.shouldShowLandscapePhoneInlineCrossReferences(
            true,
            true,
            true,
            false,
            true,
            2
        ));
        assertTrue(DetailActivity.shouldShowLandscapePhoneInlineCrossReferences(
            true,
            true,
            false,
            false,
            true,
            2
        ));
        assertFalse(DetailActivity.shouldShowLandscapePhoneInlineCrossReferences(
            true,
            true,
            false,
            true,
            true,
            2
        ));
        assertFalse(DetailActivity.shouldShowLandscapePhoneInlineCrossReferences(
            true,
            true,
            false,
            false,
            false,
            2
        ));
        assertFalse(DetailActivity.shouldShowLandscapePhoneInlineCrossReferences(
            true,
            true,
            false,
            false,
            true,
            0
        ));
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
            "GD-345 \u2022 ANCHOR \u2022 93%\nTarp & Cord Shelters\n\"A simple ridgeline shelter requires only tarp, cord, and two anchor points.\"",
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
