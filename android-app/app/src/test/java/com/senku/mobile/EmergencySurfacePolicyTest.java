package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public final class EmergencySurfacePolicyTest {
    @Test
    public void detailBridgeBuildsReviewedCardRuntimeInput() {
        ReviewedCardMetadata metadata = reviewedMetadata(
            "poisoning_unknown_ingestion",
            "GD-898",
            "pilot_reviewed",
            "GD-898"
        );

        EmergencySurfacePolicy.Input input = DetailEmergencySurfaceBridgePolicy.buildInput(
            true,
            true,
            "answer_card:poisoning_unknown_ingestion",
            "medical",
            metadata,
            false,
            OfflineAnswerEngine.ConfidenceLabel.HIGH,
            OfflineAnswerEngine.AnswerMode.CONFIDENT
        );

        assertTrue(input.deterministic);
        assertEquals("answer_card:poisoning_unknown_ingestion", input.ruleId);
        assertEquals("medical reviewed emergency answer card", input.category);
        assertEquals("pilot_reviewed", input.reviewStatus);
        assertEquals(ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME, input.provenance);
        assertEquals("HIGH", input.confidenceLabel);
        assertEquals("CONFIDENT", input.answerMode);
        assertEquals(1, input.citedReviewedSourceCount);
        assertTrue(EmergencySurfacePolicy.evaluate(input).eligible);
    }

    @Test
    public void detailActivityEmergencyBridgeDelegatesToPurePolicy() {
        ReviewedCardMetadata metadata = reviewedMetadata(
            "poisoning_unknown_ingestion",
            "GD-898",
            "pilot_reviewed",
            "GD-898"
        );

        EmergencySurfacePolicy.Decision activityDecision = DetailActivity.evaluateEmergencySurfacePolicy(
            true,
            true,
            "answer_card:poisoning_unknown_ingestion",
            "medical",
            metadata,
            false,
            OfflineAnswerEngine.ConfidenceLabel.HIGH,
            OfflineAnswerEngine.AnswerMode.CONFIDENT
        );
        EmergencySurfacePolicy.Decision bridgeDecision = DetailEmergencySurfaceBridgePolicy.evaluate(
            true,
            true,
            "answer_card:poisoning_unknown_ingestion",
            "medical",
            metadata,
            false,
            OfflineAnswerEngine.ConfidenceLabel.HIGH,
            OfflineAnswerEngine.AnswerMode.CONFIDENT
        );

        assertEquals(bridgeDecision.eligible, activityDecision.eligible);
        assertEquals(bridgeDecision.reason, activityDecision.reason);
    }

    @Test
    public void detailBridgeDoesNotAddEmergencyCategoryWithoutReviewedMetadata() {
        assertEquals(
            "medical",
            DetailEmergencySurfaceBridgePolicy.policyCategory(
                "answer_card:poisoning_unknown_ingestion",
                "medical",
                ReviewedCardMetadata.empty()
            )
        );
    }

    @Test
    public void detailBridgeDoesNotAddEmergencyCategoryForMismatchedReviewedCard() {
        assertEquals(
            "medical",
            DetailEmergencySurfaceBridgePolicy.policyCategory(
                "answer_card:poisoning_unknown_ingestion",
                "medical",
                reviewedMetadata(
                    "routine_wound_cleaning_bandage_change",
                    "GD-585",
                    "pilot_reviewed",
                    "GD-585"
                )
            )
        );
    }

    @Test
    public void detailBridgeAllowsEmergencyHeaderOnEligibleEmergencySurfaceLayout() {
        ReviewedCardMetadata metadata = reviewedMetadata(
            "poisoning_unknown_ingestion",
            "GD-898",
            "pilot_reviewed",
            "GD-898"
        );

        assertTrue(DetailActivity.shouldShowEmergencyHeaderForPolicy(
            true,
            true,
            true,
            "answer_card:poisoning_unknown_ingestion",
            "medical",
            metadata,
            false,
            OfflineAnswerEngine.ConfidenceLabel.HIGH,
            OfflineAnswerEngine.AnswerMode.CONFIDENT
        ));
    }

    @Test
    public void emergencyShortAnswerExtractionPrefersShortAnswerSection() {
        assertEquals(
            "Stop work immediately. Move to minimum 5 m from active work zone.",
            DetailActivity.extractEmergencyShortAnswer(
                "Short answer:\n" +
                    "Stop work immediately. Move to minimum 5 m from the active work zone.\n\n" +
                    "Steps:\n" +
                    "1. Stop all hot work. No new charges, no new pours."
            )
        );
        assertEquals(
            "Stop work immediately. Move to minimum 5 m from active work zone.",
            DetailActivity.extractEmergencyShortAnswer(
                "ANSWER\n" +
                    "Stop work immediately. Move to minimum 5 m from active work zone.\n\n" +
                    "STEPS\n" +
                    "1. Stop all hot work. No new charges, no new pours."
            )
        );
        assertEquals(
            "Stop work immediately. Move to minimum 5 m from active work zone.",
            DetailActivity.extractEmergencyShortAnswer(
                "ANSWER\n" +
                    "Stop work immediately. Move to minimum 5 m from active work zone.\n\n" +
                    "IMMEDIATE ACTIONS\n" +
                    "1. Stop all hot work. No new charges, no new pours."
            )
        );
    }

    @Test
    public void tabletPortraitEmergencyOverlayUsesPageWidthMargins() {
        DetailActivity.TabletEmergencyOverlayMargins margins =
            DetailActivity.resolveTabletEmergencyOverlayMarginsDp(true);

        assertEquals(
            DetailActivity.resolveTabletEmergencyAppRailWidthDp()
                + DetailActivity.resolveTabletEmergencyAppRailDividerWidthDp(),
            margins.left
        );
        assertEquals(24, margins.right);
        assertEquals(0, margins.top);
        assertEquals(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            DetailActivity.resolveTabletEmergencyOverlayHeight(true)
        );
    }

    @Test
    public void tabletPortraitEmergencyChromeUsesCompactIconTargets() {
        assertEquals(28, DetailActivity.resolveTabletEmergencyChromeNavIconSizeDp());
        assertEquals(1, DetailActivity.resolveTabletEmergencyChromeRuleHeightDp());
        assertEquals(-16, DetailActivity.resolveTabletEmergencyChromeRuleHorizontalInsetDp(true));
        assertEquals(-18, DetailActivity.resolveTabletEmergencyChromeRuleHorizontalInsetDp(false));
        assertEquals(16, DetailActivity.resolveTabletEmergencyDangerBandHorizontalBleedDp(true));
        assertEquals(0, DetailActivity.resolveTabletEmergencyDangerBandHorizontalBleedDp(false));
    }

    @Test
    public void tabletPortraitEmergencyChromeMetaUsesSharedBulletSeparators() {
        assertEquals(
            "GD-898 \u2022 DANGER \u2022 REVIEWED EVIDENCE",
            DetailActivity.buildTabletEmergencyOverlayChromeMeta(List.of(
                "GD-898",
                "danger",
                "",
                "reviewed evidence"
            ))
        );
    }

    @Test
    public void tabletPortraitEmergencyUsesSharedAppRailColumn() {
        assertEquals(72, DetailActivity.resolveTabletEmergencyAppRailWidthDp());
        assertEquals(1, DetailActivity.resolveTabletEmergencyAppRailDividerWidthDp());
        assertEquals(10.0f, DetailActivity.resolveTabletEmergencyAppRailLabelTextSizeSp(), 0.0f);
        assertEquals(13.0f, DetailActivity.resolveTabletEmergencyAppRailLabelLineHeightSp(), 0.0f);
        assertEquals(0.0f, DetailActivity.resolveTabletEmergencyAppRailLabelLetterSpacing(), 0.0f);
        assertTrue(DetailActivity.shouldShowTabletEmergencyAppRailOverlay(true, true, true));
        assertFalse(DetailActivity.shouldShowTabletEmergencyAppRailOverlay(true, false, true));
        assertFalse(DetailActivity.shouldShowTabletEmergencyAppRailOverlay(false, true, true));
        assertFalse(DetailActivity.shouldShowTabletEmergencyAppRailOverlay(true, true, false));
    }

    @Test
    public void tabletEmergencyChromePolicyOwnsFullHeightRailAndOverlayDecision() {
        DetailTabletEmergencyChromePolicy.Decision decision =
            DetailTabletEmergencyChromePolicy.evaluate(true, true, true);

        assertTrue(decision.fullHeightPage);
        assertTrue(decision.suppressStaleChrome);
        assertTrue(decision.hideDetailRoot);
        assertTrue(decision.suppressFloatingRail);
        assertTrue(decision.showAppRailOverlay);
        assertTrue(decision.showOverlayChrome);
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
        assertEquals(73, decision.overlayMarginsDp.left);
        assertEquals(24, decision.overlayMarginsDp.right);
        assertEquals(0, decision.overlayMarginsDp.top);
        assertEquals(android.view.ViewGroup.LayoutParams.MATCH_PARENT, decision.overlayHeight);
    }

    @Test
    public void tabletEmergencyChromePolicyKeepsLandscapeOverlayAnchoredWithoutFullHeightRail() {
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
        assertEquals(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, decision.overlayHeight);
    }

    @Test
    public void tabletPortraitEmergencyChromeDoesNotShowMisleadingHomeMenuGlyph() {
        assertFalse(DetailActivity.shouldShowTabletEmergencyHomeChromeAction());
    }

    @Test
    public void tabletPortraitEmergencyAppRailHomeActionNamesLibraryExit() {
        assertEquals(
            R.string.detail_emergency_app_rail_manual_label,
            DetailActivity.resolveTabletEmergencyAppRailHomeLabelResource()
        );
        assertEquals(
            R.string.detail_emergency_app_rail_manual_content_description,
            DetailActivity.resolveTabletEmergencyAppRailHomeContentDescriptionResource()
        );
    }

    @Test
    public void detailBackPolicyClassifiesOnlyEligibleAnswerAsEmergencyAnswer() {
        assertEquals(
            DetailBackPolicy.SourceRoute.EMERGENCY_ANSWER,
            DetailActivity.detailBackPolicySourceRouteForSurface(
                DetailBackPolicy.SourceRoute.ANSWER,
                true
            )
        );
        assertEquals(
            DetailBackPolicy.SourceRoute.ANSWER,
            DetailActivity.detailBackPolicySourceRouteForSurface(
                DetailBackPolicy.SourceRoute.ANSWER,
                false
            )
        );
        assertEquals(
            DetailBackPolicy.SourceRoute.GUIDE,
            DetailActivity.detailBackPolicySourceRouteForSurface(
                DetailBackPolicy.SourceRoute.GUIDE,
                true
            )
        );
        assertEquals(
            DetailBackPolicy.SourceRoute.HOME_GUIDE,
            DetailActivity.detailBackPolicySourceRouteForSurface(
                DetailBackPolicy.SourceRoute.HOME_GUIDE,
                true
            )
        );
        assertEquals(
            DetailBackPolicy.SourceRoute.CROSS_REFERENCE_GUIDE,
            DetailActivity.detailBackPolicySourceRouteForSurface(
                DetailBackPolicy.SourceRoute.CROSS_REFERENCE_GUIDE,
                true
            )
        );
        assertEquals(
            DetailBackPolicy.SourceRoute.UNKNOWN,
            DetailActivity.detailBackPolicySourceRouteForSurface(null, true)
        );
    }

    @Test
    public void eligibleEmergencyBridgePromotesTaskRootAnswerBackToManualHome() {
        ReviewedCardMetadata metadata = reviewedMetadata(
            "poisoning_unknown_ingestion",
            "GD-898",
            "pilot_reviewed",
            "GD-898"
        );
        EmergencySurfacePolicy.Decision emergencyDecision = DetailEmergencySurfaceBridgePolicy.evaluate(
            true,
            true,
            "answer_card:poisoning_unknown_ingestion",
            "medical",
            metadata,
            false,
            OfflineAnswerEngine.ConfidenceLabel.HIGH,
            OfflineAnswerEngine.AnswerMode.CONFIDENT
        );
        DetailBackPolicy.SourceRoute promotedRoute = DetailActivity.detailBackPolicySourceRouteForSurface(
            DetailBackPolicy.SourceRoute.ANSWER,
            emergencyDecision.eligible
        );
        DetailBackPolicy.Inputs inputs = new DetailBackPolicy.Inputs(
            true,
            promotedRoute,
            DetailBackPolicy.BackTrigger.VISIBLE_BACK_BUTTON
        );

        DetailBackPolicy.Decision backDecision = DetailBackPolicy.decide(inputs);
        DetailBackPolicy.VisibleBackAffordance affordance = DetailBackPolicy.visibleBackAffordance(inputs);

        assertTrue(emergencyDecision.eligible);
        assertEquals(DetailBackPolicy.SourceRoute.EMERGENCY_ANSWER, promotedRoute);
        assertEquals(DetailBackPolicy.RouteBackIntent.NAVIGATE_EMERGENCY_MANUAL_HOME, backDecision.routeIntent);
        assertEquals(R.string.detail_emergency_app_rail_manual_label, affordance.labelResource);
        assertEquals(R.string.detail_emergency_app_rail_manual_content_description, affordance.contentDescriptionResource);
    }

    @Test
    public void tabletPortraitEmergencyAppRailMarksAskAsActiveContext() {
        assertEquals(
            DetailActivity.TabletEmergencyAppRailDestination.ASK,
            DetailActivity.resolveTabletEmergencyAppRailActiveDestination()
        );
        assertFalse(DetailActivity.isTabletEmergencyAppRailDestinationActive(
            DetailActivity.TabletEmergencyAppRailDestination.HOME
        ));
        assertTrue(DetailActivity.isTabletEmergencyAppRailDestinationActive(
            DetailActivity.TabletEmergencyAppRailDestination.ASK
        ));
        assertFalse(DetailActivity.isTabletEmergencyAppRailDestinationActive(
            DetailActivity.TabletEmergencyAppRailDestination.SAVED
        ));
        assertFalse(DetailActivity.isTabletEmergencyAppRailDestinationActive(null));
    }

    @Test
    public void savedGuideRowsUseSavedGuideContentDescriptionResource() {
        assertEquals(
            R.string.saved_guide_button_content_description,
            MainActivity.savedGuideButtonContentDescriptionResource()
        );
    }

    @Test
    public void detailOverflowActionStaysHiddenUntilFunctional() {
        assertFalse(DetailActivity.shouldShowDetailOverflowAction());
    }

    @Test
    public void tabletPortraitEmergencySurfaceOwnsFullHeightPageChrome() {
        assertTrue(DetailActivity.shouldUseTabletEmergencyFullHeightPage(true, true, true));
        assertTrue(DetailActivity.shouldSuppressTabletEmergencyStaleChrome(true, true, true));
        assertTrue(DetailActivity.shouldHideTabletDetailRootBehindEmergencyOverlay(true, true, true));
        assertTrue(DetailActivity.shouldSuppressTabletEmergencyFloatingRail(true, true, true));
    }

    @Test
    public void emergencySurfaceCopyMatchesWave77Targets() {
        assertEquals(
            "Ask about safe re-entry...",
            DetailActivity.resolveDockedComposerHint(
                "Ask another question without leaving this thread",
                "Ask follow-up",
                true,
                true
            )
        );
        assertEquals("\u2014 WHY THIS ANSWER", DetailActivity.buildEmergencyWhyTitle());
        assertEquals("EMERGENCY GD-132 \u2022 Burn hazard", DetailActivity.buildPhoneEmergencyHeaderTitle("GD-132"));
        assertEquals(
            "\u2022 DANGER \u00b7 EXTREME BURN HAZARD",
            DetailActivity.buildEmergencyDangerHeaderTitle("answer_card:burn_hazard", "foundry", "Burn hazard response")
        );
        assertEquals(
            "EMERGENCY CONTEXT \u2022 GD-132 ANCHOR",
            DetailActivity.buildEmergencyDockedComposerContextHint("GD-132")
        );
        assertEquals(
            "\u2014 WHY THIS ANSWER\nGD-132 \u2022 ANCHOR \u2022 93%\nFoundry & Metal Casting",
            DetailActivity.buildEmergencyProofCardContentDescription(
                DetailActivity.buildEmergencyWhyTitle(),
                "GD-132 \u2022 ANCHOR \u2022 93%\nFoundry & Metal Casting"
            )
        );
        assertEquals(
            "GD-132 \u2022 ANCHOR",
            DetailActivity.buildEmergencyProofCardContentDescription("", "GD-132 \u2022 ANCHOR")
        );
    }

    @Test
    public void emergencyComposerContextUsesReviewedCardGuideIdWhenNotGd132() {
        ReviewedCardMetadata metadata = reviewedMetadata(
            "poisoning_unknown_ingestion",
            "GD-898",
            "pilot_reviewed",
            "GD-898"
        );

        assertEquals(
            "EMERGENCY CONTEXT \u2022 GD-898 ANCHOR",
            DetailActivity.buildEmergencyDockedComposerContextHint(metadata, List.of(), "")
        );
    }

    @Test
    public void emergencyComposerContextFallsBackToSourceMetadataThenGeneric() {
        List<SearchResult> sources = List.of(new SearchResult(
            "Poisoning first response",
            "Offline answer | deterministic | instant",
            "Call poison control and monitor airway.",
            "Emergency poisoning response.",
            "GD-284",
            "Poisoning First Response",
            "Pediatric Emergency Medicine",
            "reviewed"
        ));

        assertEquals(
            "EMERGENCY CONTEXT \u2022 GD-284 ANCHOR",
            DetailActivity.buildEmergencyDockedComposerContextHint(ReviewedCardMetadata.empty(), sources, "")
        );
        assertEquals(
            "EMERGENCY CONTEXT",
            DetailActivity.buildEmergencyDockedComposerContextHint(ReviewedCardMetadata.empty(), List.of(), "")
        );
    }

    @Test
    public void emergencyPortraitSpacingKeepsActionRowsAndProofCardReadable() {
        assertEquals(10, DetailActivity.resolveEmergencyActionRowGapDp(false));
        assertEquals(14, DetailActivity.resolveEmergencyProofCardHorizontalPaddingDp(false));
        assertEquals(10, DetailActivity.resolveEmergencyProofCardVerticalPaddingDp(false));
        assertEquals(10.5f, DetailActivity.resolveEmergencyProofCardTextSizeSp(), 0.0f);
    }

    @Test
    public void tabletEmergencySpacingUsesDenserRowsAndFlatterProofRow() {
        assertEquals(8, DetailActivity.resolveEmergencyActionRowGapDp(true));
        assertEquals(18, DetailActivity.resolveEmergencyProofCardHorizontalPaddingDp(true));
        assertEquals(12, DetailActivity.resolveEmergencyProofCardVerticalPaddingDp(true));
        assertEquals(16, DetailActivity.resolveTabletEmergencyProofHeaderTopMarginDp());
    }

    @Test
    public void tabletEmergencyProofCardUsesFlatSeparatedSurfaceTokens() {
        assertEquals(R.color.senku_rev03_bg_1, DetailActivity.resolveTabletEmergencyProofCardSurfaceColorRes());
        assertEquals(R.color.senku_rev03_accent_moss, DetailActivity.resolveTabletEmergencyProofCardAccentColorRes());
        assertEquals(R.color.senku_rev03_hairline_strong, DetailActivity.resolveTabletEmergencyProofCardStrokeColorRes());
        assertEquals(4, DetailActivity.resolveTabletEmergencyProofCardAccentRailWidthDp());
        assertEquals(0, DetailActivity.resolveTabletEmergencyProofCardCornerRadiusDp());
        assertEquals(0, DetailActivity.resolveTabletEmergencyProofCardStrokeWidthDp());
        assertEquals(74, DetailActivity.resolveTabletEmergencyProofCardMinHeightDp());
        assertEquals(8, DetailActivity.resolveTabletEmergencyProofCardTopMarginDp());
        assertEquals(13.0f, DetailActivity.resolveTabletEmergencyProofCardScoreTextSizeSp(), 0.0f);
    }

    @Test
    public void tabletEmergencyProofCardCanSplitTrailingScoreFromMetaLine() {
        CharSequence summary = "GD-132 \u2022 ANCHOR    93%\nFoundry & Metal Casting";

        assertEquals("93%", DetailActivity.extractEmergencyProofScoreLabel(summary));
        assertEquals(
            "GD-132 \u2022 ANCHOR\nFoundry & Metal Casting",
            DetailActivity.buildEmergencyProofSummaryWithoutScore(summary, "93%").toString()
        );
        assertEquals("", DetailActivity.extractEmergencyProofScoreLabel("GD-132 \u2022 ANCHOR\nFoundry"));
    }

    @Test
    public void emergencyActionBlocksUseRev03TextColors() {
        assertEquals(R.color.senku_rev03_ink_2, DetailActionBlockPresentationFormatter.emergencyActionHeadingTextColorRes());
        assertEquals(R.color.senku_rev03_ink_0, DetailActionBlockPresentationFormatter.emergencyActionTitleTextColorRes());
        assertEquals(R.color.senku_rev03_ink_2, DetailActionBlockPresentationFormatter.emergencyActionDetailTextColorRes());
    }

    @Test
    public void emergencyPortraitActionBlocksTakePriorityOverHighRiskBlocks() {
        assertEquals(
            DetailActionBlockRenderPolicy.Decision.EMERGENCY_PORTRAIT,
            DetailActivity.resolveActionBlockRenderDecision(
                true,
                true,
                true,
                true,
                "1. Stop the hazard."
            )
        );
    }

    @Test
    public void deterministicHighRiskAnswerWithBodyShowsHighRiskActionBlocks() {
        assertEquals(
            DetailActionBlockRenderPolicy.Decision.HIGH_RISK,
            DetailActivity.resolveActionBlockRenderDecision(
                false,
                true,
                true,
                true,
                "1. Apply pressure. 2. Escalate."
            )
        );
    }

    @Test
    public void highRiskActionBlocksStayHiddenOutsideOriginalEligibility() {
        assertEquals(
            DetailActionBlockRenderPolicy.Decision.NONE,
            DetailActivity.resolveActionBlockRenderDecision(false, false, true, true, "1. Apply pressure.")
        );
        assertEquals(
            DetailActionBlockRenderPolicy.Decision.NONE,
            DetailActivity.resolveActionBlockRenderDecision(false, true, false, true, "1. Apply pressure.")
        );
        assertEquals(
            DetailActionBlockRenderPolicy.Decision.NONE,
            DetailActivity.resolveActionBlockRenderDecision(false, true, true, false, "1. Apply pressure.")
        );
        assertEquals(
            DetailActionBlockRenderPolicy.Decision.NONE,
            DetailActivity.resolveActionBlockRenderDecision(false, true, true, true, "   ")
        );
    }

    @Test
    public void tabletEmergencyBackAffordanceUsesSharedIconOnlyChrome() {
        assertEquals(28, DetailActivity.resolveTabletEmergencyBackButtonMinWidthDp());
        assertEquals(28, DetailActivity.resolveTabletEmergencyChromeNavIconSizeDp());
        assertEquals(10.0f, DetailActivity.resolveTabletEmergencyBackLabelTextSizeSp(), 0.0f);
        assertEquals(12.0f, DetailActivity.resolveTabletEmergencyBackLabelLineHeightSp(), 0.0f);
        assertEquals(
            DetailActivity.resolveTabletEmergencyBackButtonMinWidthDp(),
            DetailActivity.resolveTabletEmergencyChromeNavIconSizeDp()
        );
    }

    @Test
    public void detailAndEmergencyChromeUseSharedTypeTokens() {
        assertEquals(13.0f, DetailActivity.resolveDetailTopChromeTitleTextSizeSp(), 0.0f);
        assertEquals(9.5f, DetailActivity.resolveDetailTopChromeLabelTextSizeSp(), 0.0f);
        assertEquals(11.0f, DetailActivity.resolveDetailTopChromeLabelLineHeightSp(), 0.0f);
        assertEquals(13.0f, DetailActivity.resolveTabletEmergencyChromeTitleTextSizeSp(), 0.0f);
        assertEquals(18.0f, DetailActivity.resolveTabletEmergencyChromeTitleLineHeightSp(), 0.0f);
        assertEquals(
            DetailActivity.resolveDetailTopChromeTitleTextSizeSp(),
            DetailActivity.resolvePhonePortraitAppHeaderTitleTextSizeSp(),
            0.0f
        );
        assertEquals(
            DetailActivity.resolveDetailTopChromeLabelTextSizeSp(),
            DetailActivity.resolvePhonePortraitAppHeaderMetaTextSizeSp(),
            0.0f
        );
    }

    @Test
    public void tabletEmergencyFullHeightDoesNotApplyToPhoneLandscapeOrNonEmergency() {
        assertFalse(DetailActivity.shouldUseTabletEmergencyFullHeightPage(true, false, true));
        assertFalse(DetailActivity.shouldUseTabletEmergencyFullHeightPage(false, true, true));
        assertFalse(DetailActivity.shouldUseTabletEmergencyFullHeightPage(true, true, false));
        assertFalse(DetailActivity.shouldSuppressTabletEmergencyStaleChrome(true, false, true));
        assertFalse(DetailActivity.shouldSuppressTabletEmergencyStaleChrome(false, true, true));
        assertFalse(DetailActivity.shouldSuppressTabletEmergencyStaleChrome(true, true, false));
        assertFalse(DetailActivity.shouldHideTabletDetailRootBehindEmergencyOverlay(true, false, true));
        assertFalse(DetailActivity.shouldHideTabletDetailRootBehindEmergencyOverlay(false, true, true));
        assertFalse(DetailActivity.shouldHideTabletDetailRootBehindEmergencyOverlay(true, true, false));
        assertFalse(DetailActivity.shouldSuppressTabletEmergencyFloatingRail(true, false, true));
        assertFalse(DetailActivity.shouldSuppressTabletEmergencyFloatingRail(false, true, true));
        assertFalse(DetailActivity.shouldSuppressTabletEmergencyFloatingRail(true, true, false));
    }

    @Test
    public void tabletAnswerAnchorClickStaysInAnswerContext() {
        assertTrue(DetailActivity.shouldKeepTabletAnchorClickInAnswerContext(true));
        assertFalse(DetailActivity.shouldKeepTabletAnchorClickInAnswerContext(false));
    }

    @Test
    public void tabletLandscapeEmergencyOverlayKeepsRailAnchoredMargins() {
        DetailActivity.TabletEmergencyOverlayMargins margins =
            DetailActivity.resolveTabletEmergencyOverlayMarginsDp(false);

        assertEquals(336, margins.left);
        assertEquals(24, margins.right);
        assertEquals(16, margins.top);
        assertEquals(
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            DetailActivity.resolveTabletEmergencyOverlayHeight(false)
        );
    }

    @Test
    public void detailBridgeTreatsGuideReaderAsGuideReading() {
        ReviewedCardMetadata metadata = reviewedMetadata(
            "poisoning_unknown_ingestion",
            "GD-898",
            "pilot_reviewed",
            "GD-898"
        );

        EmergencySurfacePolicy.Decision decision = DetailActivity.evaluateEmergencySurfacePolicy(
            false,
            true,
            "answer_card:poisoning_unknown_ingestion",
            "medical",
            metadata,
            false,
            OfflineAnswerEngine.ConfidenceLabel.HIGH,
            OfflineAnswerEngine.AnswerMode.CONFIDENT
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_GUIDE_READING, decision.reason);
    }

    @Test
    public void reviewedDeterministicHighRiskEmergencyQualifies() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:poisoning_unknown_ingestion",
                "First Aid & Emergency Response",
                "pilot_reviewed",
                "high",
                "high",
                "deterministic_rule",
                1
            )
        );

        assertTrue(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_ELIGIBLE, decision.reason);
    }

    @Test
    public void reviewedDeterministicSepsisEmergencyQualifies() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:newborn_danger_sepsis",
                "Pediatric Emergency Medicine",
                "reviewed",
                "medium",
                "high",
                "deterministic_rule",
                2
            )
        );

        assertTrue(decision.eligible);
    }

    @Test
    public void reviewedDeterministicSpreadingInfectionEmergencyQualifies() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:infected_wound_spreading_infection",
                "Emergency escalation",
                "reviewed",
                "high",
                "high",
                "deterministic_rule",
                1
            )
        );

        assertTrue(decision.eligible);
    }

    @Test
    public void reviewedDeterministicFoundryBurnHazardEmergencyQualifies() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:foundry_casting_area_readiness_boundary",
                "Workshop danger response",
                "reviewed",
                "high",
                "high",
                "deterministic_rule",
                1
            )
        );

        assertTrue(decision.eligible);
    }

    @Test
    public void reviewedDeterministicFoundryBurnHazardCategoryQualifies() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:foundry_casting_area_readiness_boundary",
                "Foundry burn hazard response",
                "reviewed",
                "high",
                "high",
                "deterministic_rule",
                1
            )
        );

        assertTrue(decision.eligible);
    }

    @Test
    public void reviewedDeterministicMeningitisEmergencyQualifies() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:meningitis_red_flags_child",
                "Pediatric Emergency Medicine",
                "reviewed",
                "high",
                "high",
                "deterministic_rule",
                2
            )
        );

        assertTrue(decision.eligible);
    }

    @Test
    public void routineGuideReadingDoesNotQualify() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "guide:minor_wound_cleaning",
                "First Aid & Emergency Response",
                "reviewed",
                "high",
                "high",
                "guide_reading",
                1
            )
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_GUIDE_READING, decision.reason);
    }

    @Test
    public void routineWoundCareDoesNotQualify() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:routine_wound_cleaning_bandage_change",
                "First Aid & Emergency Response",
                "reviewed",
                "high",
                "high",
                "deterministic_rule",
                1
            )
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_NOT_HIGH_RISK_EMERGENCY, decision.reason);
    }

    @Test
    public void genericFeverWithoutMeningitisOrSepsisDoesNotQualify() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:generic_fever_home_care",
                "Pediatric Emergency Medicine",
                "reviewed",
                "high",
                "high",
                "deterministic_rule",
                1
            )
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_NOT_HIGH_RISK_EMERGENCY, decision.reason);
    }

    @Test
    public void poisonPlanningAndCleaningSuppliesDoesNotQualify() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:poisoning_prevention_cleaning_supplies_storage",
                "First Aid & Emergency Response",
                "reviewed",
                "high",
                "high",
                "deterministic_rule",
                1
            )
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_NOT_HIGH_RISK_EMERGENCY, decision.reason);
    }

    @Test
    public void routineSunburnCareDoesNotQualifyAsBurnHazardEmergency() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:sunburn_sun_protection_boundary",
                "First Aid & Emergency Response",
                "reviewed",
                "high",
                "high",
                "deterministic_rule",
                1
            )
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_NOT_HIGH_RISK_EMERGENCY, decision.reason);
    }

    @Test
    public void genericBurnHazardCardDoesNotQualifyAsFoundryEmergency() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:burn_hazard_general_shop_safety",
                "Workshop danger response",
                "reviewed",
                "high",
                "high",
                "deterministic_rule",
                1
            )
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_NOT_HIGH_RISK_EMERGENCY, decision.reason);
    }

    @Test
    public void adjacentFoundryObservationCardDoesNotQualifyAsEmergencySurface() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:foundry_defects_observation_boundary",
                "Workshop danger response",
                "reviewed",
                "high",
                "high",
                "deterministic_rule",
                1
            )
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_NOT_HIGH_RISK_EMERGENCY, decision.reason);
    }

    @Test
    public void foundryReadinessWithoutEmergencyCategoryDoesNotQualify() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:foundry_casting_area_readiness_boundary",
                "Workshop readiness",
                "reviewed",
                "high",
                "high",
                "deterministic_rule",
                1
            )
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_NOT_HIGH_RISK_EMERGENCY, decision.reason);
    }

    @Test
    public void nonEmergencyMedicalEducationDoesNotQualify() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:hydration_basics",
                "General Medical Education",
                "reviewed",
                "high",
                "high",
                "deterministic_rule",
                1
            )
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_NOT_HIGH_RISK_EMERGENCY, decision.reason);
    }

    @Test
    public void uncertainFitDoesNotQualify() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:choking_airway_obstruction",
                "First Aid & Emergency Response",
                "pilot_reviewed",
                "high",
                "high",
                "uncertain_fit",
                1
            )
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_UNCERTAIN_FIT, decision.reason);
    }

    @Test
    public void lowCoverageDoesNotQualify() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:abdominal_internal_bleeding",
                "Emergency escalation",
                "pilot_reviewed",
                "low",
                "high",
                "deterministic_rule",
                1
            )
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_LOW_COVERAGE, decision.reason);
    }

    @Test
    public void generatedOnlyTextDoesNotQualify() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                true,
                "answer_card:meningitis_sepsis_child",
                "Pediatric Emergency Medicine",
                "reviewed",
                "high",
                "high",
                "deterministic_rule",
                0
            )
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_GENERATED_ONLY, decision.reason);
    }

    @Test
    public void generatedProvenanceDoesNotQualify() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            new EmergencySurfacePolicy.Input(
                true,
                "answer_card:infected_wound_spreading_infection",
                "Emergency escalation",
                "reviewed",
                "generated_model",
                "high",
                "high",
                "deterministic_rule",
                1
            )
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_UNREVIEWED, decision.reason);
    }

    @Test
    public void nonDeterministicReviewedEmergencyDoesNotQualify() {
        EmergencySurfacePolicy.Decision decision = EmergencySurfacePolicy.evaluate(
            input(
                false,
                "answer_card:poisoning_unknown_ingestion",
                "First Aid & Emergency Response",
                "pilot_reviewed",
                "high",
                "high",
                "generated_model",
                1
            )
        );

        assertFalse(decision.eligible);
        assertEquals(EmergencySurfacePolicy.REASON_NOT_DETERMINISTIC, decision.reason);
    }

    private static EmergencySurfacePolicy.Input input(
        boolean deterministic,
        String ruleId,
        String category,
        String reviewStatus,
        String coverageLabel,
        String confidenceLabel,
        String answerMode,
        int citedReviewedSourceCount
    ) {
        return new EmergencySurfacePolicy.Input(
            deterministic,
            ruleId,
            category,
            reviewStatus,
            ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
            coverageLabel,
            confidenceLabel,
            answerMode,
            citedReviewedSourceCount
        );
    }

    private static ReviewedCardMetadata reviewedMetadata(
        String cardId,
        String guideId,
        String reviewStatus,
        String citedReviewedSourceGuideId
    ) {
        return new ReviewedCardMetadata(
            cardId,
            guideId,
            reviewStatus,
            "strict_reviewed_sources",
            ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
            List.of(citedReviewedSourceGuideId)
        );
    }
}
