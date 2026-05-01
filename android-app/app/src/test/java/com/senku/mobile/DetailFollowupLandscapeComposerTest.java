package com.senku.mobile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import android.view.View;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import com.senku.ui.composer.DockedComposerModel;
import com.senku.ui.primitives.MetaItem;
import com.senku.ui.tablet.TabletDetailMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class DetailFollowupLandscapeComposerTest {
    @Test
    public void followUpSendButtonSubmitsThroughPhoneFollowUpPath() {
        FollowUpInteractionHarness harness = new FollowUpInteractionHarness();

        harness.clickSend("  what should I check next?  ");

        assertEquals(1, harness.submitCount);
        assertEquals(FollowUpComposerController.SubmitRoute.PHONE_FOLLOWUP, harness.lastRoute);
        assertEquals("what should I check next?", harness.lastQuery);
    }

    @Test
    public void followUpImeSubmitUsesSamePhoneFollowUpPathAsSendButton() {
        FollowUpInteractionHarness harness = new FollowUpInteractionHarness();

        assertTrue(harness.editorAction(EditorInfo.IME_ACTION_SEND, null, "  what should I do next?  "));

        assertEquals(1, harness.submitCount);
        assertEquals(FollowUpComposerController.SubmitRoute.PHONE_FOLLOWUP, harness.lastRoute);
        assertEquals("what should I do next?", harness.lastQuery);
    }

    @Test
    public void followUpEmptySendAndImeSubmitStayOnEmptyInputPath() {
        FollowUpInteractionHarness sendHarness = new FollowUpInteractionHarness();
        FollowUpInteractionHarness imeHarness = new FollowUpInteractionHarness();

        sendHarness.clickSend("   \n\t  ");
        assertTrue(imeHarness.editorAction(EditorInfo.IME_ACTION_SEND, null, ""));

        assertEquals(1, sendHarness.submitCount);
        assertEquals(FollowUpComposerController.SubmitRoute.EMPTY_INPUT, sendHarness.lastRoute);
        assertEquals("", sendHarness.lastQuery);
        assertEquals(1, imeHarness.submitCount);
        assertEquals(FollowUpComposerController.SubmitRoute.EMPTY_INPUT, imeHarness.lastRoute);
        assertEquals("", imeHarness.lastQuery);
    }

    @Test
    public void visibleBusyRetryButtonClickDoesNotStartDuplicateFollowUp() {
        FollowUpRetryButtonHarness harness = new FollowUpRetryButtonHarness(
            new FollowUpComposerState(
                "",
                true,
                true,
                null,
                FollowUpComposerState.Surface.PHONE,
                null,
                "  active stalled query  "
            )
        );

        FollowUpComposerController.RetryPresentation presentation =
            FollowUpComposerController.resolveRetryPresentation(harness.phoneState, true);
        harness.clickRetryButton();

        assertTrue(presentation.visible);
        assertTrue(presentation.actionEnabled);
        assertEquals("active stalled query", presentation.query);
        assertEquals(0, harness.startedGenerationCount);
        assertEquals(1, harness.blockedCount);
        assertEquals("active stalled query", harness.visibleDraft);
    }

    @Test
    public void followUpNonSubmitImeActionDoesNotDispatchFollowUp() {
        FollowUpInteractionHarness harness = new FollowUpInteractionHarness();

        assertFalse(harness.editorAction(EditorInfo.IME_ACTION_NONE, null, "what next?"));

        assertEquals(0, harness.submitCount);
    }

    @Test
    public void followUpComposerSubmitAcceptsSendDoneAndHardwareEnterUp() {
        assertTrue(DetailActivity.isFollowUpSubmitAction(EditorInfo.IME_ACTION_SEND, null));
        assertTrue(DetailActivity.isFollowUpSubmitAction(EditorInfo.IME_ACTION_DONE, null));
        assertTrue(DetailActivity.isFollowUpHardwareEnterSubmitAction(
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.ACTION_UP
        ));
    }

    @Test
    public void followUpComposerSubmitRejectsNonSubmitActions() {
        assertFalse(DetailActivity.isFollowUpSubmitAction(EditorInfo.IME_ACTION_NONE, null));
        assertFalse(DetailActivity.isFollowUpHardwareEnterSubmitAction(
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.ACTION_DOWN
        ));
        assertFalse(DetailActivity.isFollowUpHardwareEnterSubmitAction(
            KeyEvent.KEYCODE_TAB,
            KeyEvent.ACTION_UP
        ));
    }

    @Test
    public void dockedComposerHidesRetryChromeOnLandscapePhone() {
        assertFalse(FollowUpComposerController.shouldShowDockedComposerRetry(true, true));
    }

    @Test
    public void dockedComposerKeepsRetryChromeOutsideLandscapePhone() {
        assertTrue(FollowUpComposerController.shouldShowDockedComposerRetry(true, false));
        assertFalse(FollowUpComposerController.shouldShowDockedComposerRetry(false, false));
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
        assertEquals(8, DetailActivity.resolveFollowUpPanelHorizontalPaddingDp(true, true));
        assertEquals(3, DetailActivity.resolveFollowUpPanelVerticalPaddingDp(true, true));
        assertEquals(8, DetailActivity.resolveFollowUpPanelBottomPaddingDp(true, true));
    }

    @Test
    public void followUpPanelKeepsExistingVerticalPaddingOutsideLandscapePhone() {
        assertEquals(12, DetailActivity.resolveFollowUpPanelHorizontalPaddingDp(false, true));
        assertEquals(14, DetailActivity.resolveFollowUpPanelHorizontalPaddingDp(false, false));
        assertEquals(8, DetailActivity.resolveFollowUpPanelVerticalPaddingDp(false, true));
        assertEquals(14, DetailActivity.resolveFollowUpPanelVerticalPaddingDp(false, false));
        assertEquals(12, DetailActivity.resolveFollowUpPanelBottomPaddingDp(false, true));
        assertEquals(18, DetailActivity.resolveFollowUpPanelBottomPaddingDp(false, false));
    }

    @Test
    public void phonePortraitFollowUpKeepsCompactContextTitleVisible() {
        assertTrue(DetailActivity.shouldShowCompactFollowUpContextTitle(false, true, false, false, false));
        assertTrue(DetailActivity.shouldShowCompactFollowUpContextTitle(false, false, false, false, true));
        assertFalse(DetailActivity.shouldShowCompactFollowUpContextTitle(true, true, false, false, false));
        assertFalse(DetailActivity.shouldShowCompactFollowUpContextTitle(false, true, true, false, false));
        assertFalse(DetailActivity.shouldShowCompactFollowUpContextTitle(false, true, false, true, false));
    }

    @Test
    public void landscapeComposerDoesNotStealFocusFromInitialRenderLegacyFocus() {
        assertFalse(DetailActivity.shouldRequestLandscapeDockedComposerFocus(true, true, true, false, false));
    }

    @Test
    public void landscapeComposerPreservesUserLegacyInputFocus() {
        assertTrue(DetailActivity.shouldRequestLandscapeDockedComposerFocus(true, true, true, true, false));
        assertFalse(DetailActivity.shouldRequestLandscapeDockedComposerFocus(true, true, true, true, true));
        assertFalse(DetailActivity.shouldRequestLandscapeDockedComposerFocus(false, true, true, true, false));
        assertFalse(DetailActivity.shouldRequestLandscapeDockedComposerFocus(true, false, true, true, false));
        assertFalse(DetailActivity.shouldRequestLandscapeDockedComposerFocus(true, true, false, true, false));
    }

    @Test
    public void compactDockedComposerUsesCompactHint() {
        assertEquals(
            "Ask follow-up",
            DetailDockedComposerPresentationPolicy.resolveHint(
                "Ask another question without leaving this thread",
                "Ask follow-up",
                true
            )
        );
        assertEquals(
            "Ask follow-up",
            DetailDockedComposerPresentationPolicy.resolveHint(
                "Ask follow-up",
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
            DetailDockedComposerPresentationPolicy.resolveHint(
                "Ask another question without leaving this thread",
                "Ask follow-up",
                false
            )
        );
    }

    @Test
    public void dockedComposerPolicyAssemblesCompactModel() {
        FollowUpComposerState state = FollowUpComposerState.idle(
            "  check the ridge line  ",
            FollowUpComposerState.Surface.PHONE
        ).withFailure("offline failure", "  retry ridge line  ");
        FollowUpComposerController.RetryPresentation retryPresentation =
            FollowUpComposerController.resolveRetryPresentation(state, true);

        DockedComposerModel model = DetailDockedComposerPresentationPolicy.resolveModel(
            state,
            "Ask another question without leaving this thread",
            "Ask follow-up",
            true,
            false,
            true,
            true,
            retryPresentation,
            false,
            "  Retry  ",
            "  GD-345 \u2022 CONTEXT KEPT  "
        );

        assertEquals("check the ridge line", model.getText());
        assertEquals("Ask follow-up", model.getHint());
        assertTrue(model.getEnabled());
        assertTrue(model.getShowRetry());
        assertEquals("Retry", model.getRetryLabel());
        assertTrue(model.getCompact());
        assertEquals("GD-345 \u2022 CONTEXT KEPT", model.getContextHint());
    }

    @Test
    public void dockedComposerPolicySuppressesRetryOnLandscapePhone() {
        FollowUpComposerState state = FollowUpComposerState.idle(
            "",
            FollowUpComposerState.Surface.PHONE
        ).withFailure("offline failure", "retry ridge line");
        FollowUpComposerController.RetryPresentation retryPresentation =
            FollowUpComposerController.resolveRetryPresentation(state, true);

        DockedComposerModel model = DetailDockedComposerPresentationPolicy.resolveModel(
            state,
            "Ask another question without leaving this thread",
            "Ask follow-up",
            true,
            false,
            true,
            true,
            retryPresentation,
            true,
            "Retry",
            ""
        );

        assertFalse(model.getShowRetry());
    }

    @Test
    public void dockedComposerPolicyDisablesInputWhenStateOrActionsAreUnavailable() {
        FollowUpComposerState busyState = FollowUpComposerState.idle(
            "draft",
            FollowUpComposerState.Surface.PHONE
        ).asSubmitting();

        assertFalse(DetailDockedComposerPresentationPolicy.resolveModel(
            busyState,
            "Ask another question without leaving this thread",
            "Ask follow-up",
            false,
            false,
            true,
            true,
            null,
            false,
            "Retry",
            ""
        ).getEnabled());
        assertFalse(DetailDockedComposerPresentationPolicy.resolveModel(
            FollowUpComposerState.idle("draft", FollowUpComposerState.Surface.PHONE),
            "Ask another question without leaving this thread",
            "Ask follow-up",
            false,
            false,
            false,
            true,
            null,
            false,
            "Retry",
            ""
        ).getEnabled());
        assertFalse(DetailDockedComposerPresentationPolicy.resolveModel(
            FollowUpComposerState.idle("draft", FollowUpComposerState.Surface.PHONE),
            "Ask another question without leaving this thread",
            "Ask follow-up",
            false,
            false,
            true,
            false,
            null,
            false,
            "Retry",
            ""
        ).getEnabled());
    }

    @Test
    public void phoneAnswerDockedComposerCarriesContextHint() {
        assertEquals(
            "GD-345 \u2022 THIS DEVICE \u2022 CONTEXT KEPT",
            DetailActivity.buildAnswerDockedComposerContextHint("GD-345")
        );
        assertEquals(
            "THIS DEVICE \u2022 CONTEXT KEPT",
            DetailActivity.buildAnswerDockedComposerContextHint("")
        );
    }

    @Test
    public void phoneAnswerDockedComposerSuppressesRedundantContextWhenTopChromeOwnsIt() {
        assertEquals(
            "",
            DetailActivity.buildAnswerDockedComposerContextHint("GD-345", true)
        );
        assertTrue(DetailActivity.shouldSuppressDockedComposerContextHintForTopChrome(true, true, true));
        assertFalse(DetailActivity.shouldSuppressDockedComposerContextHintForTopChrome(true, true, false));
        assertFalse(DetailActivity.shouldSuppressDockedComposerContextHintForTopChrome(true, false, false));
        assertFalse(DetailActivity.shouldSuppressDockedComposerContextHintForTopChrome(true, false, true));
        assertFalse(DetailActivity.shouldSuppressDockedComposerContextHintForTopChrome(false, true, false));
    }

    @Test
    public void topDetailChromeCarriesAnswerContextOnlyWithMetadata() {
        assertTrue(DetailActivity.topDetailChromeCarriesContextMetadata(
            false,
            "GD-345",
            1,
            "",
            "",
            false
        ));
        assertTrue(DetailActivity.topDetailChromeCarriesContextMetadata(
            false,
            "",
            1,
            "Tarp & Cord Shelters",
            "",
            false
        ));
        assertFalse(DetailActivity.topDetailChromeCarriesContextMetadata(
            false,
            "",
            1,
            "",
            "",
            false
        ));
    }

    @Test
    public void topDetailChromeCarriesThreadAndEmergencyContextMetadata() {
        assertTrue(DetailActivity.topDetailChromeCarriesContextMetadata(
            true,
            "",
            2,
            "",
            "",
            false
        ));
        assertTrue(DetailActivity.topDetailChromeCarriesContextMetadata(
            true,
            "",
            1,
            "",
            "Rain shelter",
            false
        ));
        assertTrue(DetailActivity.topDetailChromeCarriesContextMetadata(
            false,
            "",
            1,
            "",
            "",
            true
        ));
    }

    @Test
    public void phoneThreadDockedComposerCarriesThreadContextHint() {
        assertEquals(
            "THREAD CONTEXT \u2022 2 TURNS \u2022 GD-220 ANCHOR",
            DetailActivity.buildThreadDockedComposerContextHint("GD-220", 2)
        );
        assertEquals(
            "THREAD CONTEXT \u2022 1 TURN",
            DetailActivity.buildThreadDockedComposerContextHint("", 0)
        );
    }

    @Test
    public void phoneThreadDockedComposerSuppressesRedundantContextWhenTopChromeOwnsIt() {
        assertEquals(
            "",
            DetailActivity.buildThreadDockedComposerContextHint("GD-220", 2, true)
        );
    }

    @Test
    public void emergencyFollowUpContextTitleSuppressesOnlyWhenTopChromeOwnsContext() {
        assertTrue(DetailActivity.shouldSuppressFollowUpContextTitleForTopChrome(true, true));
        assertFalse(DetailActivity.shouldSuppressFollowUpContextTitleForTopChrome(true, false));
        assertFalse(DetailActivity.shouldSuppressFollowUpContextTitleForTopChrome(false, true));
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
        assertTrue(DetailActivity.shouldShowDetailFollowUpPanel(
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
        assertTrue(DetailActivity.shouldUseLandscapePhoneSourceRail(true, true));
    }

    @Test
    public void phoneLandscapeAnswerDoesNotInstallSideNavRail() {
        assertFalse(DetailActivity.shouldInstallPhoneLandscapeAnswerNavRail(true, true));
        assertFalse(DetailActivity.shouldInstallPhoneLandscapeAnswerNavRail(true, false));
        assertFalse(DetailActivity.shouldInstallPhoneLandscapeAnswerNavRail(false, true));
        assertEquals(64, DetailActivity.resolvePhoneLandscapeAnswerNavRailWidthDp());
    }

    @Test
    public void phoneLandscapeAnswerAllocatesRoomForSourceRail() {
        assertEquals(1.20f, DetailActivity.resolveLandscapeDetailPrimaryColumnWeight(false, true), 0.0f);
        assertEquals(1.0f, DetailActivity.resolveLandscapeDetailSideColumnWeight(false, true), 0.0f);
        assertEquals(1.65f, DetailActivity.resolveLandscapeDetailPrimaryColumnWeight(false, false), 0.0f);
        assertEquals(0.75f, DetailActivity.resolveLandscapeDetailSideColumnWeight(false, false), 0.0f);
        assertEquals(2.25f, DetailActivity.resolveLandscapeDetailPrimaryColumnWeight(true, true), 0.0f);
        assertEquals(0.71f, DetailActivity.resolveLandscapeDetailSideColumnWeight(true, true), 0.0f);
        assertEquals(4, DetailActivity.resolvePhoneLandscapeSourceRailCardVerticalPaddingDp());
        assertEquals(4, DetailActivity.resolvePhoneLandscapeSourceRailCardTopMarginDp());
        assertEquals(9.5f, DetailActivity.resolvePhoneLandscapeSourceRailCardTextSizeSp(), 0.0f);
        assertEquals(8, DetailActivity.resolvePhoneLandscapeRelatedRailTopMarginDp());
        assertEquals(13.0f, DetailActivity.resolvePhoneLandscapeRelatedRailTitleTextSizeSp(), 0.0f);
        assertEquals(5, DetailActivity.resolvePhoneLandscapeRelatedRailButtonVerticalPaddingDp());
        assertEquals(12.5f, DetailActivity.resolvePhoneLandscapeRelatedRailButtonTextSizeSp(), 0.0f);
    }

    @Test
    public void phoneLandscapeAnswerRestoresQuestionMetaShell() {
        assertTrue(DetailActivity.shouldShowQuestionHeaderLabel(true, true));
        assertFalse(DetailActivity.shouldShowQuestionHeaderLabel(true, false));
        assertFalse(DetailActivity.shouldShowQuestionHeaderLabel(false, true));
        assertTrue(DetailActivity.shouldShowQuestionSubtitleForLayout(true, true, false));
        assertTrue(DetailActivity.shouldShowQuestionSubtitleForLayout(true, false, true));
        assertFalse(DetailActivity.shouldShowQuestionSubtitleForLayout(true, false, false));
        assertEquals(14.5f, DetailActivity.resolveAnswerQuestionTitleTextSizeSp(true, true, false), 0.0f);
        assertEquals(16.0f, DetailActivity.resolveAnswerQuestionTitleTextSizeSp(true, false, true), 0.0f);
        assertEquals(18.0f, DetailActivity.resolveAnswerQuestionTitleTextSizeSp(false, true, false), 0.0f);
        assertEquals(12.5f, DetailActivity.resolvePhoneLandscapeAnswerBodyTextSizeSp(), 0.0f);
        assertEquals(1.03f, DetailActivity.resolvePhoneLandscapeAnswerBodyLineSpacingMultiplier(), 0.0f);
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
            "\u2014 SOURCES",
            DetailActivity.buildCompactPhoneSourcesTriggerTitle("", 0, true)
        );
        assertEquals(
            "\u2014 SOURCES \u2022 3",
            DetailActivity.buildCompactPhoneSourcesTriggerTitle("Source guides", 3, true)
        );
        assertEquals(10, DetailActivity.resolvePhonePortraitSourceCardHorizontalPaddingDp());
        assertEquals(5, DetailActivity.resolvePhonePortraitSourceCardVerticalPaddingDp());
        assertEquals(5, DetailActivity.resolvePhonePortraitSourceCardTopMarginDp());
        assertEquals(10.0f, DetailActivity.resolvePhonePortraitSourceCardTextSizeSp(), 0.0f);
    }

    @Test
    public void answerSourcesStampUsesSourceLanguageInsteadOfProofRail() {
        String label = DetailActivity.buildDetailSourcesProofStampLabel();

        assertEquals("SOURCES", label);
        assertFalse(label.contains("PROOF"));
        assertFalse(label.contains("GUIDES"));
    }

    @Test
    public void phonePortraitProofPanelDefersBelowSourceTriggerExceptEmergency() {
        assertTrue(DetailActivity.shouldDeferPhonePortraitProofPanelBelowSources(true, true, false));
        assertFalse(DetailActivity.shouldDeferPhonePortraitProofPanelBelowSources(true, true, true));
        assertFalse(DetailActivity.shouldDeferPhonePortraitProofPanelBelowSources(true, false, false));
        assertFalse(DetailActivity.shouldDeferPhonePortraitProofPanelBelowSources(false, true, false));
        assertTrue(DetailActivity.shouldHidePhonePortraitAnswerWhyPanel(true, true, false));
        assertFalse(DetailActivity.shouldHidePhonePortraitAnswerWhyPanel(true, true, true));
        assertFalse(DetailActivity.shouldHidePhonePortraitAnswerWhyPanel(true, false, false));
        assertTrue(DetailActivity.shouldForceExpandedPhonePortraitAnswerSections(true, true, false));
        assertFalse(DetailActivity.shouldForceExpandedPhonePortraitAnswerSections(true, true, true));
    }

    @Test
    public void phonePortraitSourceSelectionDoesNotJumpToProvenance() {
        assertFalse(DetailActivity.shouldScrollToProvenanceOnCompactPreview(true, true));
        assertTrue(DetailActivity.shouldScrollToProvenanceOnCompactPreview(true, false));
        assertFalse(DetailActivity.shouldScrollToProvenanceOnCompactPreview(false, false));
    }

    @Test
    public void phonePortraitSourceSelectionRevealsCrossReferenceLaneOnlyAfterGuideAnchor() {
        assertTrue(DetailActivity.shouldRevealAnswerSourceGraphAfterSelection(true, true, "gd-345|tarp", 3));
        assertFalse(DetailActivity.shouldRevealAnswerSourceGraphAfterSelection(true, true, "", 3));
        assertFalse(DetailActivity.shouldRevealAnswerSourceGraphAfterSelection(true, true, "gd-345|tarp", 0));
        assertFalse(DetailActivity.shouldRevealAnswerSourceGraphAfterSelection(true, false, "gd-345|tarp", 3));
        assertFalse(DetailActivity.shouldRevealAnswerSourceGraphAfterSelection(false, true, "gd-345|tarp", 3));

        assertTrue(DetailActivity.shouldExpandAnswerSourceGraphAfterSourceSelection(true, true, "gd-345|tarp"));
        assertFalse(DetailActivity.shouldExpandAnswerSourceGraphAfterSourceSelection(true, true, ""));
        assertFalse(DetailActivity.shouldExpandAnswerSourceGraphAfterSourceSelection(true, false, "gd-345|tarp"));
        assertFalse(DetailActivity.shouldExpandAnswerSourceGraphAfterSourceSelection(false, true, "gd-345|tarp"));
    }

    @Test
    public void phonePortraitSourcePreviewOpenCtaGetsComposerClearance() {
        assertTrue(DetailActivity.shouldApplyRelatedGuidePreviewCtaClearance(true, true, true, true, true));
        assertFalse(DetailActivity.shouldApplyRelatedGuidePreviewCtaClearance(false, true, true, true, true));
        assertFalse(DetailActivity.shouldApplyRelatedGuidePreviewCtaClearance(true, false, true, true, true));
        assertFalse(DetailActivity.shouldApplyRelatedGuidePreviewCtaClearance(true, true, false, true, true));
        assertFalse(DetailActivity.shouldApplyRelatedGuidePreviewCtaClearance(true, true, true, false, true));
        assertFalse(DetailActivity.shouldApplyRelatedGuidePreviewCtaClearance(true, true, true, true, false));

        assertEquals(112, DetailActivity.resolveRelatedGuidePreviewCtaBottomClearancePx(14, 96, 16));
        assertEquals(120, DetailActivity.resolveRelatedGuidePreviewCtaBottomClearancePx(120, 80, 16));
        assertEquals(228, DetailActivity.computeScrollYToKeepViewBottomVisible(680, 48, 560, 60, 0));
        assertEquals(300, DetailActivity.computeScrollYToKeepViewBottomVisible(680, 48, 560, 60, 300));
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
        assertEquals(24, DetailActivity.resolvePhoneGuideBodyShellBottomPaddingDp(true, false));
        assertEquals(14, DetailActivity.resolvePhoneGuideBodyShellBottomPaddingDp(true, true));
        assertEquals(18, DetailActivity.resolvePhoneGuideBodyShellBottomPaddingDp(false, false));
        assertEquals(16, DetailActivity.resolvePhoneGuideBodyShellBottomPaddingDp(false, true));
        assertEquals(18, DetailActivity.resolvePhoneGuideBodyShellHorizontalPaddingDp(true, false));
        assertEquals(16, DetailActivity.resolvePhoneGuideBodyShellHorizontalPaddingDp(true, true));
        assertEquals(10, DetailActivity.resolvePhoneGuideBodyShellTopPaddingDp(true, false));
        assertEquals(6, DetailActivity.resolvePhoneGuideBodyShellTopPaddingDp(true, true));
        assertEquals(3, DetailActivity.resolvePhoneGuideBodyLineSpacingExtraDp(false));
        assertEquals(2, DetailActivity.resolvePhoneGuideBodyLineSpacingExtraDp(true));
        assertEquals(8, DetailActivity.resolvePhoneGuideViewportHorizontalPaddingDp(false));
        assertEquals(5, DetailActivity.resolvePhoneGuideViewportHorizontalPaddingDp(true));
        assertEquals(8, DetailActivity.resolvePhoneGuideViewportTopPaddingDp(false));
        assertEquals(3, DetailActivity.resolvePhoneGuideViewportTopPaddingDp(true));
        assertEquals(8, DetailActivity.resolvePhoneGuidePaperBottomViewportInsetDp(false));
        assertEquals(6, DetailActivity.resolvePhoneGuidePaperBottomViewportInsetDp(true));
    }

    @Test
    public void phonePortraitHeaderUsesShortAnswerLabel() {
        assertEquals(
            "ANSWER GD-345 \u2022 Rain shelter",
            DetailActivity.buildPhonePortraitAnswerHeaderTitle("GD-345", "Rain shelter")
        );
        assertEquals("ANSWER GD-345", DetailActivity.buildPhonePortraitAnswerHeaderTitle("GD-345", ""));
        assertEquals("ANSWER", DetailActivity.buildPhonePortraitAnswerHeaderTitle("", ""));
        assertEquals("EMERGENCY GD-132 \u2022 Burn hazard", DetailActivity.buildPhoneEmergencyHeaderTitle("GD-132"));
        assertEquals("EMERGENCY \u2022 Burn hazard", DetailActivity.buildPhoneEmergencyHeaderTitle(""));
        assertFalse(DetailActivity.buildPhoneEmergencyHeaderTitle("GD-132").contains("\u00C3"));
        assertFalse(DetailActivity.buildPhoneEmergencyHeaderTitle("GD-132").contains("\u00E2"));
        assertTrue(DetailActivity.shouldUsePhoneAnswerHeaderTitle(true, true));
        assertFalse(DetailActivity.shouldUsePhoneAnswerHeaderTitle(true, false));
        assertFalse(DetailActivity.shouldUsePhoneAnswerHeaderTitle(false, true));
        assertEquals(13.0f, DetailActivity.resolvePhonePortraitAppHeaderTitleTextSizeSp(), 0.0f);
        assertEquals(9.5f, DetailActivity.resolvePhonePortraitAppHeaderMetaTextSizeSp(), 0.0f);
        assertEquals(16.0f, DetailActivity.resolvePhonePortraitQuestionTitleTextSizeSp(), 0.0f);
        assertEquals(10.5f, DetailActivity.resolvePhonePortraitQuestionMetaTextSizeSp(), 0.0f);
        assertEquals(15.0f, DetailActivity.resolvePhonePortraitAnswerBodyTextSizeSp(), 0.0f);
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
        assertEquals("SOURCES - 2", DetailActivity.buildLandscapePhoneSourceRailTitle("Sources", 2));
        assertEquals("SOURCES - 2", DetailActivity.buildLandscapePhoneSourceRailTitle("Source guides", 2));
        assertTrue(DetailActivity.shouldHideGenericAnswerScaffoldForThread(true, 2, true));
        assertFalse(DetailActivity.shouldHideProofRailForThreadDetail(true, 2, true, true));
        assertTrue(DetailActivity.shouldUseCompactPhoneLandscapeThreadTranscript(true, 2, true));
    }

    @Test
    public void phoneAnswerChromeFlattensSourceAndRelatedWrappersOutsideEmergency() {
        assertTrue(DetailActivity.shouldUseFlatAnswerDetailChrome(true, true, false));
        assertFalse(DetailActivity.shouldUseFlatAnswerDetailChrome(true, true, true));
        assertFalse(DetailActivity.shouldUseFlatAnswerDetailChrome(true, false, false));
        assertFalse(DetailActivity.shouldUseFlatAnswerDetailChrome(false, true, false));
    }

    @Test
    public void phoneLandscapeGuideKeepsRelatedGuideRailMounted() {
        assertTrue(DetailActivity.shouldKeepGuideRelatedRailInPlace(false, false));
        assertFalse(DetailActivity.shouldKeepGuideRelatedRailInPlace(false, true));
        assertFalse(DetailActivity.shouldKeepGuideRelatedRailInPlace(true, false));
    }

    @Test
    public void phoneLandscapeGuideUsesSectionRailInsteadOfCrossReferenceDrawer() {
        assertTrue(DetailActivity.shouldUsePhoneLandscapeGuideSectionRail(false, true));
        assertFalse(DetailActivity.shouldUsePhoneLandscapeGuideSectionRail(true, true));
        assertFalse(DetailActivity.shouldUsePhoneLandscapeGuideSectionRail(false, false));

        assertEquals(2.25f, DetailActivity.resolveLandscapeDetailPrimaryColumnWeight(true), 0.001f);
        assertEquals(0.71f, DetailActivity.resolveLandscapeDetailSideColumnWeight(true), 0.001f);
        assertEquals(1.65f, DetailActivity.resolveLandscapeDetailPrimaryColumnWeight(false), 0.001f);
        assertEquals(0.75f, DetailActivity.resolveLandscapeDetailSideColumnWeight(false), 0.001f);
    }

    @Test
    public void phoneLandscapeGuideSectionRailUsesGuideSectionsOutsideReviewDemo() {
        SearchResult guide = new SearchResult(
            "Water Collection",
            "",
            "",
            "## Site selection\nKeep the catchment uphill from waste.\n"
                + "## First flush\nDiscard the first runoff.\n"
                + "## Storage\nCover clean containers.",
            "GD-501",
            "",
            "",
            ""
        );

        assertEquals(
            Arrays.asList(
                "\u00A71  Site selection",
                "\u00A72  First flush",
                "\u00A73  Storage"
            ),
            DetailActivity.phoneLandscapeGuideSectionRailLabels(guide, false)
        );
        assertEquals(3, DetailActivity.phoneLandscapeGuideSectionRailCount(guide, false));
    }

    @Test
    public void phoneLandscapeGuideSectionRailKeepsTargetLabelsInReviewDemoOnly() {
        SearchResult foundryGuide = new SearchResult(
            "Foundry & Metal Casting",
            "",
            "",
            "## Live section\nUse real guide text outside demo mode.",
            "GD-132",
            "",
            "",
            ""
        );

        assertEquals(
            Arrays.asList("\u00A71  Live section"),
            DetailActivity.phoneLandscapeGuideSectionRailLabels(foundryGuide, false)
        );
        assertEquals(
            Arrays.asList(
                "\u00A71  Area readiness",
                "\u00A72  Required reading",
                "\u00A73  Hazard screen",
                "\u00A74  Material labeling",
                "\u00A75  No-go triggers",
                "\u00A76  Access control",
                "\u00A77  Owner handoff"
            ),
            DetailActivity.phoneLandscapeGuideSectionRailLabels(foundryGuide, true)
        );
        assertEquals(17, DetailActivity.phoneLandscapeGuideSectionRailCount(foundryGuide, true));
    }

    @Test
    public void answerSourceRailKeepsReviewedRainShelterStackOrder() {
        List<SearchResult> railSources = DetailActivity.resolveVisibleSourceRailSourcesForState(
            true,
            false,
            true,
            Arrays.asList(
                source("GD-220", "Abrasives Manufacturing", "abrasives manufacturing"),
                source("GD-132", "Foundry & Metal Casting", "foundry metal casting"),
                source("GD-345", "Tarp & Cord Shelters", "rain shelter tarp cord")
            ),
            null
        );

        assertEquals(3, railSources.size());
        assertEquals("GD-220", railSources.get(0).guideId);
        assertEquals("GD-132", railSources.get(1).guideId);
        assertEquals("GD-345", railSources.get(2).guideId);
    }

    @Test
    public void primaryGuideKeepsRainShelterTextPromotionBehindReviewDemoPolicy() {
        List<SearchResult> sources = Arrays.asList(
            source("GD-220", "Abrasives Manufacturing", "abrasives manufacturing"),
            source("GD-345", "Tarp & Cord Shelters", "rain shelter tarp cord")
        );

        assertEquals("GD-220", DetailActivity.primaryGuideIdForSources(sources, false));
        assertEquals("GD-345", DetailActivity.primaryGuideIdForSources(sources, true));
    }

    @Test
    public void phoneLandscapeThreadSourceRailUsesOneRepresentativePerTurn() {
        SearchResult initialAnchor = source("GD-220", "Abrasives Manufacturing", "abrasives manufacturing");
        SearchResult relatedSafety = source("GD-132", "Foundry & Metal Casting", "foundry metal casting");
        SearchResult currentAnchor = source("GD-345", "Tarp & Cord Shelters", "rain shelter tarp cord");

        List<SearchResult> railSources = DetailActivity.resolveVisibleSourceRailSourcesForState(
            true,
            true,
            true,
            Arrays.asList(currentAnchor),
            Arrays.asList(new SessionMemory.TurnSnapshot(
                "How do I build a simple rain shelter from tarp and cord?",
                "Build a ridgeline first.",
                "Build a ridgeline first.",
                Arrays.asList("GD-220", "GD-132"),
                Arrays.asList(initialAnchor, relatedSafety),
                null,
                0L
            ))
        );

        assertEquals(2, railSources.size());
        assertEquals("GD-220", railSources.get(0).guideId);
        assertEquals("GD-345", railSources.get(1).guideId);
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
            new long[] {0L, 80L, 240L, 480L, 900L, 1400L},
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
    public void compactPortraitAnswerFlowUsesFullColumnWidth() {
        assertTrue(DetailActivity.shouldUseFullWidthCompactPortraitAnswerFlow(true, true));
        assertFalse(DetailActivity.shouldUseFullWidthCompactPortraitAnswerFlow(true, false));
        assertFalse(DetailActivity.shouldUseFullWidthCompactPortraitAnswerFlow(false, true));
    }

    @Test
    public void answerHeaderConfidenceDotUsesMockLabels() {
        assertEquals(
            "\u2022 CONFIDENT",
            DetailActivity.buildAnswerHeaderConfidenceDotLabel(OfflineAnswerEngine.ConfidenceLabel.HIGH, false, false)
        );
        assertEquals(
            "\u2022 UNSURE",
            DetailActivity.buildAnswerHeaderConfidenceDotLabel(OfflineAnswerEngine.ConfidenceLabel.MEDIUM, false, false)
        );
        assertEquals(
            "\u2022 UNSURE",
            DetailActivity.buildAnswerHeaderConfidenceDotLabel(OfflineAnswerEngine.ConfidenceLabel.LOW, false, false)
        );
        assertEquals(
            "\u2022 UNSURE",
            DetailActivity.buildAnswerHeaderConfidenceDotLabel(OfflineAnswerEngine.ConfidenceLabel.HIGH, true, false)
        );
        assertEquals(
            "",
            DetailActivity.buildAnswerHeaderConfidenceDotLabel(OfflineAnswerEngine.ConfidenceLabel.HIGH, false, true)
        );
    }

    @Test
    public void answerModeSuppressesLegacyBodyMirror() {
        assertTrue(DetailActivity.shouldHideBodyMirrorForAnswerMode(true));
        assertFalse(DetailActivity.shouldHideBodyMirrorForAnswerMode(false));
        assertEquals(View.GONE, DetailActivity.resolveBodyMirrorShellVisibility(true, false));
        assertEquals(View.GONE, DetailActivity.resolveBodyMirrorShellVisibility(false, true));
        assertEquals(View.VISIBLE, DetailActivity.resolveBodyMirrorShellVisibility(false, false));
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
    public void tabletPortraitThreadKeepsPortraitStateForComposeRails() {
        assertFalse(DetailActivity.shouldUseTabletPortraitCompactStructuralShell(
            true,
            true,
            TabletDetailMode.Thread,
            false
        ));
        assertFalse(DetailActivity.resolveTabletStateLandscapeFlag(
            false,
            true,
            true,
            TabletDetailMode.Thread,
            false
        ));
        assertFalse(DetailActivity.resolveTabletStateLandscapeFlag(
            false,
            true,
            true,
            TabletDetailMode.Answer,
            false
        ));
        assertFalse(DetailActivity.resolveTabletStateLandscapeFlag(
            false,
            false,
            true,
            TabletDetailMode.Thread,
            false
        ));
    }

    @Test
    public void tabletPortraitEmergencyUsesCompactStructuralShellBehindOverlay() {
        assertTrue(DetailActivity.shouldUseTabletPortraitCompactStructuralShell(
            true,
            true,
            TabletDetailMode.Answer,
            true
        ));
        assertTrue(DetailActivity.resolveTabletStateLandscapeFlag(
            false,
            true,
            true,
            TabletDetailMode.Answer,
            true
        ));
        assertTrue(DetailActivity.resolveTabletStateLandscapeFlag(
            true,
            false,
            true,
            TabletDetailMode.Answer,
            false
        ));
        assertFalse(DetailActivity.resolveTabletStateLandscapeFlag(
            false,
            true,
            false,
            TabletDetailMode.Guide,
            true
        ));
    }

    @Test
    public void tabletAnswerGuideModeUsesSourceLanguageInsteadOfProofRail() {
        String summary = DetailActivity.buildTabletAnswerGuideModeSummary("GD-345", "");
        String anchorLabel = DetailActivity.buildTabletAnswerGuideModeAnchorLabel(false);

        assertEquals("GD-345", summary);
        assertEquals("Sources", anchorLabel);
        assertFalse((anchorLabel + " - " + summary).contains("Proof rail"));
        assertFalse(summary.contains("Answer source"));
        assertFalse(summary.contains("selected"));
    }

    @Test
    public void tabletAnswerGuideModeFallsBackToQuietContextText() {
        assertEquals(
            "Rain shelter",
            DetailActivity.buildTabletAnswerGuideModeSummary("", "Rain shelter")
        );
        assertEquals(
            "Answer context",
            DetailActivity.buildTabletAnswerGuideModeSummary("", "")
        );
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
        assertTrue(DetailActivity.shouldHideProofRailForThreadDetail(true, 2, true, false));
        assertFalse(DetailActivity.shouldHideProofRailForThreadDetail(true, 2, true, true));
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
    public void compactPhoneAnswerSourcePreviewDropsRedundantQuoteLine() {
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

        assertTrue(DetailActivity.shouldUseCompactPhoneAnswerSourcePreviewCard(true, true, false));
        assertFalse(DetailActivity.shouldUseCompactPhoneAnswerSourcePreviewCard(true, true, true));
        assertEquals(2, DetailActivity.resolvePhonePortraitSourceCardMaxLines(true, false));
        assertEquals(3, DetailActivity.resolvePhonePortraitSourceCardMaxLines(true, true));
        assertEquals(3, DetailActivity.resolvePhonePortraitSourceCardVerticalPaddingDp(true));
        assertEquals(4, DetailActivity.resolvePhonePortraitSourceCardTopMarginDp(true));
        assertEquals(9.5f, DetailActivity.resolvePhonePortraitSourceCardTextSizeSp(true), 0.0f);
        assertEquals(
            "GD-345\nTarp & Cord Shelters",
            DetailActivity.buildPhonePortraitSourceCardLabel(card, false, true)
        );
    }

    @Test
    public void compactPhoneAnswerRelatedPreviewKeepsCtaCloserToViewport() {
        assertEquals(3, DetailActivity.resolveRelatedGuidePreviewCollapsedMaxLines(true, true));
        assertEquals(5, DetailActivity.resolveRelatedGuidePreviewCollapsedMaxLines(true, false));
        assertEquals(5, DetailActivity.resolveRelatedGuidePreviewCollapsedMaxLines(false, true));
        assertTrue(DetailActivity.shouldApplyRelatedGuidePreviewCtaClearance(true, true, true, true, true));
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

    private static SearchResult source(String guideId, String title, String topicTags) {
        return new SearchResult(title, "", "", "", guideId, "", "", "", "", "", "", topicTags);
    }

    private static final class FollowUpInteractionHarness {
        int submitCount;
        FollowUpComposerController.SubmitRoute lastRoute;
        String lastQuery;

        void clickSend(String draft) {
            dispatchSubmit(draft);
        }

        boolean editorAction(int actionId, KeyEvent event, String draft) {
            if (!DetailActivity.isFollowUpSubmitAction(actionId, event)) {
                return false;
            }
            dispatchSubmit(draft);
            return true;
        }

        private void dispatchSubmit(String draft) {
            submitCount += 1;
            lastQuery = FollowUpComposerState.normalizeDraft(draft);
            lastRoute = FollowUpComposerController.resolvePhoneSubmitRoute(draft);
        }
    }

    private static final class FollowUpRetryButtonHarness {
        FollowUpComposerState phoneState;
        String visibleDraft;
        int blockedCount;
        int startedGenerationCount;
        final View.OnClickListener retryClickListener = v -> retryLastFailedQuery();

        FollowUpRetryButtonHarness(FollowUpComposerState phoneState) {
            this.phoneState = phoneState;
            this.visibleDraft = phoneState == null ? "" : phoneState.draftText;
        }

        void clickRetryButton() {
            retryClickListener.onClick(null);
        }

        private void retryLastFailedQuery() {
            DetailFollowUpActionController.Decision decision =
                DetailFollowUpActionController.resolveRetry(
                    false,
                    phoneState,
                    visibleDraft,
                    null,
                    null
                );
            applyRetryDraft(decision);
            applyFollowUpActionDecision(decision);
        }

        private void applyRetryDraft(DetailFollowUpActionController.Decision decision) {
            if (decision == null || decision.action == DetailFollowUpActionController.Action.EMPTY) {
                return;
            }
            visibleDraft = decision.query;
            phoneState = phoneState == null
                ? FollowUpComposerState.idle(decision.query, FollowUpComposerState.Surface.PHONE)
                : phoneState.withDraft(decision.query);
        }

        private void applyFollowUpActionDecision(DetailFollowUpActionController.Decision decision) {
            if (decision == null || decision.action == DetailFollowUpActionController.Action.EMPTY) {
                return;
            }
            if (decision.action == DetailFollowUpActionController.Action.BLOCKED) {
                blockedCount += 1;
                return;
            }
            startedGenerationCount += 1;
        }
    }
}
