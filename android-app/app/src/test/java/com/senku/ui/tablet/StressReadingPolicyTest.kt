package com.senku.ui.tablet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import com.senku.mobile.ReviewedCardMetadata
import com.senku.ui.answer.AnswerContent
import com.senku.ui.answer.AnswerSurfaceLabel
import com.senku.ui.answer.Evidence

class StressReadingPolicyTest {
    @Test
    fun tabletLandscapeReadingPolicyKeepsAnswerFirstWidthBudget() {
        val policy = tabletLandscapeReadingLayoutPolicy()

        assertEquals(328, policy.threadRailWidthDp)
        assertEquals(492, policy.answerMaxWidthDp)
        assertEquals(478, policy.evidenceRailWidthDp)
        assertEquals(42, policy.answerHorizontalPaddingDp)
    }

    @Test
    fun tabletPortraitReadingPolicyUsesReaderFirstSourceBudget() {
        val policy = tabletPortraitReadingLayoutPolicy()

        assertEquals(0, policy.threadRailWidthDp)
        assertEquals(720, policy.answerMaxWidthDp)
        assertEquals(420, policy.evidenceRailWidthDp)
        assertEquals(42, policy.answerHorizontalPaddingDp)
    }

    @Test
    fun tabletReadingPolicySelectorUsesOrientationSpecificBudgets() {
        val portraitPolicy = tabletReadingLayoutPolicy(isLandscape = false)
        val landscapePolicy = tabletReadingLayoutPolicy(isLandscape = true)

        assertEquals(tabletPortraitReadingLayoutPolicy(), portraitPolicy)
        assertEquals(tabletLandscapeReadingLayoutPolicy(), landscapePolicy)
        assertEquals(0, portraitPolicy.threadRailWidthDp)
        assertEquals(720, portraitPolicy.answerMaxWidthDp)
        assertEquals(420, portraitPolicy.evidenceRailWidthDp)
        assertEquals(42, portraitPolicy.answerHorizontalPaddingDp)
        assertEquals(328, landscapePolicy.threadRailWidthDp)
        assertEquals(492, landscapePolicy.answerMaxWidthDp)
        assertEquals(478, landscapePolicy.evidenceRailWidthDp)
        assertEquals(42, landscapePolicy.answerHorizontalPaddingDp)
    }

    @Test
    fun tabletGuideThreadRailUsesSectionIndexWidthWithoutChangingAnswerRails() {
        assertEquals(0, tabletThreadRailWidthDp(isLandscape = true, guideMode = false))
        assertEquals(0, tabletThreadRailWidthDp(isLandscape = false, guideMode = false))
        assertEquals(328, tabletThreadRailWidthDp(isLandscape = true, guideMode = false, threadMode = true))
        assertEquals(0, tabletThreadRailWidthDp(isLandscape = false, guideMode = false, threadMode = true))
        assertEquals(316, tabletThreadRailWidthDp(isLandscape = true, guideMode = true))
        assertEquals(330, tabletThreadRailWidthDp(isLandscape = false, guideMode = true))
    }

    @Test
    fun tabletThreadRailShellHidesOnlyNonGuidePortraitRail() {
        assertFalse(tabletShouldShowThreadRail(isLandscape = true, guideMode = false))
        assertTrue(tabletShouldShowThreadRail(isLandscape = true, guideMode = true))
        assertTrue(tabletShouldShowThreadRail(isLandscape = true, guideMode = false, threadMode = true))
        assertFalse(tabletShouldShowThreadRail(isLandscape = false, guideMode = false))
        assertFalse(tabletShouldShowThreadRail(isLandscape = false, guideMode = false, threadMode = true))
        assertTrue(tabletShouldShowThreadRail(isLandscape = false, guideMode = true))
    }

    @Test
    fun tabletThreadFlowUsesWiderSingleColumnBudget() {
        assertEquals(700, tabletThreadFlowMaxWidthDp(isLandscape = true))
        assertEquals(660, tabletThreadFlowMaxWidthDp(isLandscape = false))
        assertEquals(24, tabletThreadFlowHorizontalPaddingDp(isLandscape = true))
        assertEquals(18, tabletThreadFlowHorizontalPaddingDp(isLandscape = false))
        assertEquals(0, tabletThreadComposerBottomPaddingDp(isLandscape = true))
        assertEquals(12, tabletThreadComposerBottomPaddingDp(isLandscape = false))
    }

    @Test
    fun tabletAnswerComposerReservesNavigationBoundary() {
        assertEquals(8, tabletAnswerComposerBottomPaddingDp(isLandscape = true))
        assertEquals(18, tabletAnswerComposerBottomPaddingDp(isLandscape = false))
        assertEquals(8, tabletComposerBottomPaddingDp(TabletDetailMode.Answer, isLandscape = true))
        assertEquals(18, tabletComposerBottomPaddingDp(TabletDetailMode.Answer, isLandscape = false))
        assertEquals(0, tabletComposerBottomPaddingDp(TabletDetailMode.Thread, isLandscape = true))
        assertEquals(12, tabletComposerBottomPaddingDp(TabletDetailMode.Thread, isLandscape = false))
        assertEquals(0, tabletComposerBottomPaddingDp(TabletDetailMode.Guide, isLandscape = false))
    }

    @Test
    fun tabletGuidePaperPolicyContainsPortraitReaderWithReadableLandscapeSheet() {
        assertEquals(518, tabletGuidePaperMaxWidthDp(isLandscape = true))
        assertEquals(820, tabletGuidePaperMaxWidthDp(isLandscape = false))
        assertEquals(10, tabletGuidePaperHorizontalPaddingDp(isLandscape = true))
        assertEquals(18, tabletGuidePaperHorizontalPaddingDp(isLandscape = false))
        assertEquals(34, tabletGuidePaperInnerHorizontalPaddingDp(isLandscape = true))
        assertEquals(34, tabletGuidePaperInnerHorizontalPaddingDp(isLandscape = false))
        assertEquals(24, tabletGuidePaperBottomPaddingDp(isLandscape = true))
        assertEquals(40, tabletGuidePaperBottomPaddingDp(isLandscape = false))
        assertEquals(420, tabletGuideReferenceRailWidthDp(isLandscape = true))
        assertEquals(0, tabletGuideReferenceRailWidthDp(isLandscape = false))
    }

    @Test
    fun tabletGuideChromePolicyKeepsCompactTitleAboveRails() {
        val landscapePolicy = tabletGuideChromePolicy(isLandscape = true)
        val portraitPolicy = tabletGuideChromePolicy(isLandscape = false)

        assertEquals(58, landscapePolicy.topBarMinHeightDp)
        assertEquals(24, landscapePolicy.topBarHorizontalPaddingDp)
        assertEquals(8, landscapePolicy.topBarVerticalPaddingDp)
        assertEquals(18, landscapePolicy.topBarTitleLineHeightSp)
        assertEquals(56, portraitPolicy.topBarMinHeightDp)
        assertTrue(landscapePolicy.topBarMinHeightDp > portraitPolicy.topBarMinHeightDp)
        assertTrue(landscapePolicy.topBarHorizontalPaddingDp > portraitPolicy.topBarHorizontalPaddingDp)
    }

    @Test
    fun tabletGuideReferenceHeaderUsesMockBulletSeparator() {
        assertEquals("CROSS-REFERENCE \u00B7 6", tabletGuideReferenceHeaderTitle(6))
        assertEquals("CROSS-REFERENCE \u00B7 0", tabletGuideReferenceHeaderTitle(-1))
    }

    @Test
    fun tabletGuideReferencePanePromotesOpenedFromGuideAsAnchor() {
        val rows = tabletGuideReferencePaneRows(
            listOf(
                XRefState("GD-132", "Foundry & Metal Casting", "ANCHOR"),
                XRefState("GD-220", "Abrasives Manufacturing", "RELATED"),
                XRefState("GD-499", "Bellows & Forge Blower", "REQUIRED"),
            ),
        )

        assertEquals(listOf("GD-220", "GD-132", "GD-499"), rows.map { it.id })
        assertEquals(listOf("ANCHOR", "RELATED", "REQUIRED"), rows.map { it.relation })
    }

    @Test
    fun tabletGuideNavigationLabelsUseSectionLanguage() {
        val labels = tabletGuideNavigationLabels()

        assertEquals("SECTIONS", labels.sectionLabel)
        assertEquals("CROSS-REFERENCE", labels.referenceLabel)
        assertEquals("No sections yet.", labels.emptySectionLabel)
        assertEquals("No cross-references yet.", labels.emptyReferenceLabel)
    }

    @Test
    fun tabletGuideRailRowsUseMockSectionNumberAndTitle() {
        assertEquals(
            TabletGuideRailRowParts("§3", "Hazard screen"),
            tabletGuideRailRowParts("§3 Hazard screen", fallbackIndex = 9),
        )
        assertEquals(
            TabletGuideRailRowParts("§2", "Required reading"),
            tabletGuideRailRowParts("Required reading", fallbackIndex = 2),
        )
    }

    @Test
    fun tabletGuidePaperClassifiesCalloutsAndRequiredRowsForBoxedRendering() {
        assertEquals(
            TabletGuideBodyLineKind.Skip,
            tabletGuideBodyLineKindForTest("FIELD MANUAL \u00B7 REV 04-27 \u00B7 PK 2"),
        )
        assertEquals(
            TabletGuideBodyLineKind.Skip,
            tabletGuideBodyLineKindForTest("GD-132 \u00B7 17 SECTIONS"),
        )
        assertEquals(
            TabletGuideBodyLineKind.Danger,
            tabletGuideBodyLineKindForTest("DANGER \u00B7 EXTREME BURN HAZARD"),
        )
        assertEquals(
            TabletGuideBodyLineKind.Danger,
            tabletGuideBodyLineKindForTest("Every tool, mold, crucible, and surface that contacts molten metal must be completely dry."),
        )
        assertEquals(
            TabletGuideBodyLineKind.RequiredReading,
            tabletGuideBodyLineKindForTest("REQUIRED READING \u00B7 GD-220 \u00B7 Abrasives Manufacturing"),
        )
        assertEquals(
            TabletGuideBodyLineKind.Section,
            tabletGuideBodyLineKindForTest("\u2014 \u00A7 1 \u00B7 AREA READINESS"),
        )
        assertEquals(
            TabletGuideBodyLineKind.Body,
            tabletGuideBodyLineKindForTest("Confirm area readiness before lighting the work zone."),
        )
    }

    @Test
    fun tabletGuideRailExtractsSectionAnchorRowsBeforeCrossReferences() {
        val state = tabletDetailState(
            turns = listOf(
                threadTurn(
                    id = "guide",
                    answer = "\u2014 \u00A7 1 \u00B7 AREA READINESS\n" +
                        "Confirm the work zone is ready.\n" +
                        "REQUIRED READING \u00B7 GD-220 \u00B7 Abrasives Manufacturing\n" +
                        "\u2014 \u00A7 3 \u00B7 DRY TOOLS",
                )
            ),
            xrefs = listOf(
                XRefState(id = "GD-220", title = "Abrasives Manufacturing"),
                XRefState(id = "GD-499", title = "Bellows Forge Blower Construction"),
            ),
            detailMode = TabletDetailMode.Guide,
        )

        val railTurns = state.resolvedThreadRailTurns()
        val railSources = state.resolvedThreadRailSources()

        assertEquals(listOf("\u00A71 Area readiness", "\u00A72 Required reading", "\u00A73 Dry tools"), railTurns.map { it.question })
        assertEquals(listOf("GD-220", "GD-499"), railSources.map { it.id })
    }

    @Test
    fun tabletFoundryGuideRailUsesCanonicalMockSections() {
        val state = tabletDetailState(
            guideId = "GD-132",
            guideTitle = "Foundry & Metal Casting",
            turns = listOf(
                threadTurn(
                    id = "guide",
                    answer = "\u2014 \u00A7 1 \u00B7 AREA READINESS\n" +
                        "Reviewed Answer-Card Boundary",
                )
            ),
            detailMode = TabletDetailMode.Guide,
        )

        assertEquals(
            listOf(
                "\u00A71 Area readiness",
                "\u00A72 Required reading",
                "\u00A73 Hazard screen",
                "\u00A74 Material labeling",
                "\u00A75 No-go triggers",
                "\u00A76 Access control",
                "\u00A77 Owner handoff",
            ),
            state.resolvedThreadRailTurns().map { it.question },
        )
    }

    @Test
    fun tabletGuideEvidenceGraphCountsAnchorPlusVisibleCrossReferences() {
        val state = tabletDetailState(
            guideId = "GD-132",
            anchor = AnchorState(
                key = "gd-132",
                id = "GD-132",
                title = "Foundry & Metal Casting",
                section = "Area readiness",
                snippet = "",
                hasSource = true,
            ),
            xrefs = listOf(
                XRefState(id = "GD-220", title = "Abrasives Manufacturing"),
                XRefState(id = "GD-225", title = "Bloomery Furnace"),
                XRefState(id = "GD-301", title = "Charcoal Production"),
                XRefState(id = "GD-302", title = "Clay Furnace Lining"),
                XRefState(id = "GD-499", title = "Bellows Forge Blower Construction"),
            ),
            detailMode = TabletDetailMode.Guide,
        )

        val graph = state.resolvedEvidencePaneGraph()

        assertFalse(graph.anchor.hasSource)
        assertEquals(6, graph.xrefs.size)
        assertEquals("ANCHOR", graph.xrefs.first().relation)
        assertEquals("GD-132", graph.xrefs.first().id)
    }

    @Test
    fun tabletGuideEvidenceGraphCapsAtSixMockReferenceCardsIncludingAnchor() {
        val state = tabletDetailState(
            guideId = "GD-132",
            guideTitle = "Foundry & Metal Casting",
            anchor = AnchorState(
                key = "gd-220",
                id = "GD-220",
                title = "Abrasives Manufacturing",
                section = "",
                snippet = "",
                hasSource = true,
            ),
            xrefs = listOf(
                XRefState(id = "GD-220", title = "Abrasives Manufacturing"),
                XRefState(id = "GD-172", title = "Bearing Manufacturing"),
                XRefState(id = "GD-499", title = "Bellows Forge Blower Construction", relation = "REQUIRED"),
                XRefState(id = "GD-224", title = "Blacksmithing"),
                XRefState(id = "GD-225", title = "Bloomery Furnace", relation = "REQUIRED"),
                XRefState(id = "GD-110", title = "Bridges, Dams & Infra."),
                XRefState(id = "GD-301", title = "Charcoal Production"),
            ),
            detailMode = TabletDetailMode.Guide,
        )

        val graph = state.resolvedEvidencePaneGraph()

        assertEquals(6, graph.xrefs.size)
        assertEquals(listOf("GD-220", "GD-172", "GD-499", "GD-224", "GD-225", "GD-110"), graph.xrefs.map { it.id })
    }

    @Test
    fun tabletGuideRequiredReadingParserSplitsIdFromReadableTitle() {
        val parts = parseGuideRequiredReadingParts(
            "REQUIRED READING \u00B7 GD-499 \u00B7 Bellows Forge Blower Construction",
        )

        assertEquals("REQUIRED READING", parts.label)
        assertEquals("GD-499", parts.id)
        assertEquals("Bellows Forge Blower Construction", parts.title)
    }

    @Test
    fun tabletGuideRequiredReadingParserNormalizesMojibakeSeparators() {
        val parts = parseGuideRequiredReadingParts(
            "REQUIRED READING \u00C2\u00B7 GD-225 \u00C2\u00B7 Bloomery Furnace Construction",
        )

        assertEquals("REQUIRED READING", parts.label)
        assertEquals("GD-225", parts.id)
        assertEquals("Bloomery Furnace Construction", parts.title)
    }

    @Test
    fun tabletLandscapeTypeScaleKeepsAnswerReadableWithoutOversizingBody() {
        val policy = tabletLandscapeDetailTypeScalePolicy()

        assertEquals(18, policy.questionFontSizeSp)
        assertEquals(23, policy.questionLineHeightSp)
        assertEquals(13, policy.answerFontSizeSp)
        assertEquals(20, policy.answerLineHeightSp)
        assertEquals(12, policy.stepFontSizeSp)
        assertEquals(16, policy.stepLineHeightSp)
    }

    @Test
    fun tabletPortraitTypeScaleTightensAfterPortraitSqueeze() {
        val policy = tabletPortraitDetailTypeScalePolicy()

        assertEquals(17, policy.questionFontSizeSp)
        assertEquals(22, policy.questionLineHeightSp)
        assertEquals(13, policy.answerFontSizeSp)
        assertEquals(19, policy.answerLineHeightSp)
        assertEquals(10, policy.limitFontSizeSp)
        assertEquals(14, policy.limitLineHeightSp)
    }

    @Test
    fun tabletTypeScaleSelectorUsesOrientationSpecificBudgets() {
        val portraitPolicy = tabletDetailTypeScalePolicy(isLandscape = false)
        val landscapePolicy = tabletDetailTypeScalePolicy(isLandscape = true)

        assertEquals(tabletPortraitDetailTypeScalePolicy(), portraitPolicy)
        assertEquals(tabletLandscapeDetailTypeScalePolicy(), landscapePolicy)
    }

    @Test
    fun tabletPortraitAnswerChromeKeepsEmergencyAnswerHigherDensityThanLandscape() {
        val portraitPolicy = tabletAnswerReadingChromePolicy(isLandscape = false)
        val landscapePolicy = tabletAnswerReadingChromePolicy(isLandscape = true)

        assertTrue(portraitPolicy.topPaddingDp < landscapePolicy.topPaddingDp)
        assertTrue(portraitPolicy.blockSpacingDp < landscapePolicy.blockSpacingDp)
        assertTrue(portraitPolicy.bottomPaddingDp <= landscapePolicy.bottomPaddingDp)
    }

    @Test
    fun tabletComposerContextHintUsesGuideAnchorAndSourceCount() {
        val state = tabletDetailState(
            guideTitle = "Pressure canning beans",
            guideModeLabel = "GUIDE",
            anchor = AnchorState(
                key = "anchor",
                id = "GD-BEANS",
                title = "Processing times",
                section = "Altitude adjustment",
                snippet = "",
                hasSource = true,
            ),
            sources = listOf(
                SourceState("s1", "GD-BEANS", "Guide A", isAnchor = true, isSelected = true),
                SourceState("s2", "GD-ALT", "Guide B", isAnchor = false, isSelected = false),
            ),
            turns = listOf(threadTurn("q1"), threadTurn("q2")),
            detailMode = TabletDetailMode.Guide,
        )

        assertEquals(
            "GUIDE CONTEXT KEPT \u2022 2 SECTIONS \u2022 2 REFERENCES",
            tabletComposerContextHint(state),
        )
    }

    @Test
    fun tabletAnswerSourceCountUsesThreadAnswerCountWhenSourceRowsAreDelayed() {
        val state = tabletDetailState(
            sources = emptyList(),
            turns = listOf(threadTurn("q1", sourceCount = 2)),
        )

        assertEquals(2, state.resolvedAnswerSourceCount())
    }

    @Test
    fun tabletComposerContextHintUsesAnswerSourceCountFloor() {
        val state = tabletDetailState(
            sources = listOf(
                SourceState("s1", "GD-001", "Anchor guide", isAnchor = true, isSelected = true),
                SourceState("s2", "", "Field note", isAnchor = false, isSelected = false),
            ),
            turns = listOf(threadTurn("q1", sourceCount = 3)),
        )

        assertEquals("GD-TEST \u2022 CONTEXT KEPT \u2022 3 SOURCES VISIBLE", tabletComposerContextHint(state))
    }

    @Test
    fun tabletThreadContextHintKeepsTurnCountWithoutRepeatingAnchor() {
        val state = tabletDetailState(
            guideId = "GD-220",
            guideTitle = "Rain shelter",
            sources = listOf(SourceState("gd345", "GD-345", "Tarp & Cord Shelters", isAnchor = true, isSelected = true)),
            turns = listOf(threadTurn("q1"), threadTurn("q2")),
            detailMode = TabletDetailMode.Thread,
        )

        assertEquals(
            "THREAD CONTEXT KEPT \u2022 2 TURNS \u2022 2 SOURCES",
            tabletComposerContextHint(state),
        )
        assertEquals("GD-220 anchor", tabletThreadContextAnchorLabel(state))
        assertEquals(
            listOf("GD-220", "GD-345"),
            state.resolvedThreadSourceRows().map { it.id },
        )
        assertEquals(
            listOf("Abrasives Manufacturing", "Tarp & Cord Shelters"),
            state.resolvedThreadSourceRows().map { it.title },
        )
    }

    @Test
    fun tabletThreadLabelsAvoidProofWorkflowLanguage() {
        val confidentAnswer = AnswerContent(
            short = "Drape the tarp evenly across the ridge.",
            sourceCount = 1,
            host = "Rule match",
            elapsedSeconds = 0.0,
            evidence = Evidence.Strong,
            answerSurfaceLabel = AnswerSurfaceLabel.DeterministicRule,
        )
        val unsureAnswer = confidentAnswer.copy(
            uncertainFit = true,
            answerSurfaceLabel = AnswerSurfaceLabel.LimitedFit,
        )

        assertEquals("Q1 \u2022 04:21 \u2022 FIELD QUESTION", tabletThreadQuestionMetaLabel(1))
        assertEquals("A2 \u2022 04:23 \u2022 ANSWER", tabletThreadAnswerMetaLabel(2))
        assertEquals("\u2022 CONFIDENT", tabletThreadAnswerStatusLabel(confidentAnswer, Status.Done))
        assertEquals("\u2022 UNSURE", tabletThreadAnswerStatusLabel(unsureAnswer, Status.Done))
        assertEquals(
            "\u2022 UNSURE",
            tabletThreadAnswerStatusLabel(
                confidentAnswer.copy(
                    evidence = Evidence.Moderate,
                    answerSurfaceLabel = AnswerSurfaceLabel.Unknown,
                ),
                Status.Done,
            ),
        )
        assertFalse(tabletThreadAnswerStatusLabel(confidentAnswer, Status.Done).contains("Rule match"))
        assertFalse(tabletThreadAnswerStatusLabel(confidentAnswer, Status.Done).contains("STRONG EVIDENCE"))
        assertFalse(tabletThreadAnswerMetaLabel(2).contains("GD-345"))
    }

    @Test
    fun tabletThreadAnswerSourceChipsUsePerTurnFallbackGuideIds() {
        assertEquals(
            listOf("GD-220", "GD-132"),
            tabletThreadAnswerSourceIds(content = null, turnIndex = 1),
        )
        assertEquals(
            listOf("GD-220", "GD-132"),
            tabletThreadAnswerSourceChipLabels(content = null, turnIndex = 1),
        )
        assertEquals(
            listOf("GD-345"),
            tabletThreadAnswerSourceIds(content = null, turnIndex = 2),
        )
        assertEquals(
            emptyList<String>(),
            tabletThreadAnswerSourceIds(content = null, turnIndex = 3),
        )
    }

    @Test
    fun tabletThreadAnswerSourceChipsPreferReviewedMetadataGuideIds() {
        val answer = AnswerContent(
            short = "Use the relevant field guidance.",
            sourceCount = 2,
            host = "Host",
            elapsedSeconds = 0.0,
            reviewedCardMetadata = ReviewedCardMetadata(
                "card",
                "gd-777",
                "pilot_reviewed",
                "cite-reviewed",
                ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
                listOf("GD-888", "gd-777", " GD-889 "),
            ),
        )

        assertEquals(
            listOf("GD-777", "GD-888", "GD-889"),
            tabletThreadAnswerSourceIds(answer, turnIndex = 1),
        )
        assertEquals(
            listOf("GD-777", "GD-888", "GD-889"),
            tabletThreadAnswerSourceChipLabels(answer, turnIndex = 1),
        )
    }

    @Test
    fun tabletThreadSourceLabelsUseMockSeparators() {
        assertEquals("SOURCES \u2022 2", tabletThreadSourcePaneTitle(2, isLandscape = false))
        assertEquals("SOURCES IN THREAD \u2022 2", tabletThreadSourcePaneTitle(2, isLandscape = true))
        assertEquals("GD-345 \u2022 TOPIC", tabletThreadSourceCardMeta("GD-345", "TOPIC"))
        assertEquals("SOURCE", tabletThreadSourceCardMeta("", "SOURCE"))
        assertFalse(tabletThreadSourceCardMeta("", "SOURCE").contains("THREAD - 1 SOURCE"))
        assertEquals("Abrasives Manufacturing", tabletThreadAnchorSourceTitle("GD-220", "Rain shelter - 2 turns"))
        assertEquals(
            "\"Pitch ridgeline along prevailing wind...\"",
            tabletThreadSourceSnippetLabel(
                SourceState("anchor", "GD-220", "Abrasives Manufacturing", isAnchor = true, isSelected = false),
            ),
        )
    }

    @Test
    fun tabletThreadChromeSuppressesAnswerEvidenceResidue() {
        assertFalse(tabletThreadRailShouldShowSourceRows(TabletDetailMode.Thread))
        assertTrue(tabletThreadRailShouldShowSourceRows(TabletDetailMode.Answer))
        assertTrue(tabletThreadRailShouldShowSourceRows(TabletDetailMode.Guide))
        assertFalse(tabletTitleBarShouldShowSupportRows(TabletDetailMode.Thread))
        assertFalse(tabletTitleBarShouldShowSupportRows(TabletDetailMode.Answer))
        assertTrue(tabletTitleBarShouldShowSupportRows(TabletDetailMode.Guide))
        assertEquals(
            "Rain shelter",
            tabletTitleBarTitle(
                detailMode = TabletDetailMode.Answer,
                guideMode = false,
                guideTitle = "Rain shelter",
                turnCount = 1,
            ),
        )
    }

    @Test
    fun tabletThreadSourceScoresUseMockThreadIdsOnly() {
        assertEquals(
            "74%",
            tabletThreadSourceScoreLabel(
                SourceState("anchor", "GD-220", "Abrasives Manufacturing", isAnchor = true, isSelected = true),
            ),
        )
        assertEquals(
            "68%",
            tabletThreadSourceScoreLabel(
                SourceState("topic", "GD-345", "Tarp & Cord Shelters", isAnchor = false, isSelected = false),
            ),
        )
        assertEquals(
            "",
            tabletThreadSourceScoreLabel(
                SourceState("related", "GD-132", "Foundry & Metal Casting", isAnchor = false, isSelected = false),
            ),
        )
    }

    @Test
    fun tabletComposerContextHintUsesAnswerFooterReserveLanguageOutsideGuideMode() {
        val state = tabletDetailState(
            guideId = "GD-FALLBACK",
            guideTitle = " ",
            anchor = AnchorState(
                key = "anchor",
                id = "GD-FALLBACK",
                title = "Fallback anchor",
                section = " ",
                snippet = "",
                hasSource = false,
            ),
            sources = emptyList(),
        )

        assertEquals(
            "GD-FALLBACK \u2022 CONTEXT KEPT \u2022 NO SOURCES VISIBLE",
            tabletComposerContextHint(state),
        )
    }

    @Test
    fun tabletComposerContextHintUsesGuideEmptyLabelsInGuideMode() {
        val state = tabletDetailState(
            guideId = "GD-FALLBACK",
            guideTitle = " ",
            guideModeAnchorLabel = "Opened from GD-220",
            anchor = AnchorState(
                key = "anchor",
                id = "GD-FALLBACK",
                title = "Fallback anchor",
                section = " ",
                snippet = "",
                hasSource = false,
            ),
            sources = emptyList(),
            detailMode = TabletDetailMode.Guide,
        )

        assertEquals(
            "GUIDE CONTEXT KEPT \u2022 NO SECTIONS \u2022 NO REFERENCES",
            tabletComposerContextHint(state),
        )
    }

    @Test
    fun tabletAnswerModeShowsEvidencePaneWhenSourcesExist() {
        val state = tabletDetailState(
            sources = listOf(
                SourceState("s1", "GD-001", "Anchor guide", isAnchor = true, isSelected = true),
                SourceState("s2", "GD-002", "Related guide", isAnchor = false, isSelected = false),
            ),
        )

        assertTrue(tabletShouldShowEvidencePane(state, guideMode = false))
    }

    @Test
    fun tabletAnswerModeShowsEvidencePaneWhenExpanded() {
        val state = tabletDetailState(
            evidenceExpanded = true,
            sources = listOf(
                SourceState("s1", "GD-001", "Anchor guide", isAnchor = true, isSelected = true),
            ),
        )

        assertTrue(tabletShouldShowEvidencePane(state, guideMode = false))
    }

    @Test
    fun tabletAnswerModeShowsEvidencePaneForExplicitRelatedSourceSelection() {
        val state = tabletDetailState(
            sources = listOf(
                SourceState("s1", "GD-001", "Anchor guide", isAnchor = true, isSelected = false),
                SourceState("s2", "GD-002", "Related guide", isAnchor = false, isSelected = true),
            ),
        )

        assertTrue(tabletShouldShowEvidencePane(state, guideMode = false))
    }

    @Test
    fun tabletThreadModeRestoresLandscapeSourceRailForCanonicalThreadSources() {
        val state = tabletDetailState(
            detailMode = TabletDetailMode.Thread,
            evidenceExpanded = true,
            guideId = "GD-220",
            sources = listOf(
                SourceState("s1", "GD-345", "Tarp & Cord Shelters", isAnchor = false, isSelected = false),
                SourceState("s2", "GD-132", "Foundry & Metal Casting", isAnchor = false, isSelected = false),
            ),
        )

        assertTrue(tabletShouldShowEvidencePane(state, guideMode = false))
        assertEquals(listOf("GD-220", "GD-345"), state.resolvedVisibleThreadSourceRows().map { it.id })
    }

    @Test
    fun tabletGuideModeKeepsExistingLandscapeEvidenceBehavior() {
        val landscapeState = tabletDetailState(isLandscape = true)
        val portraitState = tabletDetailState(isLandscape = false)

        assertTrue(tabletShouldShowEvidencePane(landscapeState, guideMode = true))
        assertFalse(tabletShouldShowEvidencePane(portraitState, guideMode = true))
    }

    @Test
    fun phoneLandscapePolicyCompactsComposerAndSuppressesSupportChrome() {
        val policy = phoneLandscapeStressReadingPolicy()

        assertTrue(policy.compactComposer)
        assertTrue(policy.suppressRetryChrome)
        assertTrue(policy.suppressSupportSuggestions)
        assertFalse(policy.collapseThreadChrome)
        assertFalse(policy.collapseSourceChrome)
    }

    @Test
    fun phonePortraitAnswerFirstPolicyCollapsesSecondaryChrome() {
        val policy = phonePortraitAnswerFirstStressReadingPolicy()

        assertTrue(policy.compactComposer)
        assertFalse(policy.suppressRetryChrome)
        assertFalse(policy.suppressSupportSuggestions)
        assertTrue(policy.collapseThreadChrome)
        assertTrue(policy.collapseSourceChrome)
    }

    @Test
    fun phonePolicySelectorUsesLandscapePolicyBeforeAnswerReadiness() {
        val awaitingAnswerPolicy = phoneStressReadingPolicy(
            isLandscape = true,
            answerReady = false,
        )
        val answerReadyPolicy = phoneStressReadingPolicy(
            isLandscape = true,
            answerReady = true,
        )

        assertEquals(phoneLandscapeStressReadingPolicy(), awaitingAnswerPolicy)
        assertEquals(phoneLandscapeStressReadingPolicy(), answerReadyPolicy)
        assertTrue(awaitingAnswerPolicy.compactComposer)
        assertTrue(awaitingAnswerPolicy.suppressRetryChrome)
        assertTrue(awaitingAnswerPolicy.suppressSupportSuggestions)
        assertFalse(awaitingAnswerPolicy.collapseThreadChrome)
        assertFalse(awaitingAnswerPolicy.collapseSourceChrome)
        assertEquals(awaitingAnswerPolicy, answerReadyPolicy)
    }

    @Test
    fun phonePolicySelectorCollapsesPortraitChromeOnlyWhenAnswerReady() {
        val awaitingAnswerPolicy = phoneStressReadingPolicy(
            isLandscape = false,
            answerReady = false,
        )
        val answerReadyPolicy = phoneStressReadingPolicy(
            isLandscape = false,
            answerReady = true,
        )

        assertEquals(phonePortraitAnswerFirstStressReadingPolicy(), answerReadyPolicy)
        assertTrue(awaitingAnswerPolicy.compactComposer)
        assertFalse(awaitingAnswerPolicy.suppressRetryChrome)
        assertFalse(awaitingAnswerPolicy.suppressSupportSuggestions)
        assertFalse(awaitingAnswerPolicy.collapseThreadChrome)
        assertFalse(awaitingAnswerPolicy.collapseSourceChrome)

        assertTrue(answerReadyPolicy.compactComposer)
        assertFalse(answerReadyPolicy.suppressRetryChrome)
        assertFalse(answerReadyPolicy.suppressSupportSuggestions)
        assertTrue(answerReadyPolicy.collapseThreadChrome)
        assertTrue(answerReadyPolicy.collapseSourceChrome)
    }

    @Test
    fun phonePolicySelectorKeepsCompactComposerAcrossStressPostures() {
        val policies = listOf(
            phoneStressReadingPolicy(isLandscape = true, answerReady = false),
            phoneStressReadingPolicy(isLandscape = true, answerReady = true),
            phoneStressReadingPolicy(isLandscape = false, answerReady = false),
            phoneStressReadingPolicy(isLandscape = false, answerReady = true),
        )

        policies.forEach { policy ->
            assertTrue(policy.compactComposer)
        }
    }

    private fun tabletDetailState(
        guideId: String = "GD-TEST",
        guideTitle: String = "Guide title",
        anchor: AnchorState = AnchorState(
            key = "anchor",
            id = "GD-TEST",
            title = "Anchor title",
            section = "Anchor section",
            snippet = "",
            hasSource = true,
        ),
        sources: List<SourceState> = emptyList(),
        turns: List<ThreadTurnState> = emptyList(),
        guideModeLabel: String = "",
        guideModeAnchorLabel: String = "",
        xrefs: List<XRefState> = emptyList(),
        evidenceExpanded: Boolean = false,
        isLandscape: Boolean = true,
        detailMode: TabletDetailMode = TabletDetailMode.Answer,
    ): TabletDetailState =
        TabletDetailState(
            guideId = guideId,
            guideTitle = guideTitle,
            meta = emptyList(),
            turns = turns,
            sources = sources,
            anchor = anchor,
            xrefs = xrefs,
            composerText = "",
            composerPlaceholder = "Ask a follow-up",
            composerEnabled = true,
            composerVisible = true,
            composerShowRetry = false,
            composerRetryLabel = "Retry",
            pinVisible = false,
            pinActive = false,
            evidenceExpanded = evidenceExpanded,
            isLandscape = isLandscape,
            guideModeLabel = guideModeLabel,
            guideModeAnchorLabel = guideModeAnchorLabel,
            detailMode = detailMode,
        )

    private fun threadTurn(
        id: String,
        sourceCount: Int = 1,
        answer: String = "Answer",
    ): ThreadTurnState =
        ThreadTurnState(
            id = id,
            question = "Question",
            answer = com.senku.ui.answer.AnswerContent(
                short = answer,
                sourceCount = sourceCount,
                host = "Host",
                elapsedSeconds = 1.0,
            ),
            status = Status.Done,
            isActive = false,
        )
}
