package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class DetailGuideContextPresentationFormatterTest {
    @Test
    public void answerModeCopyUsesSelectedSourceContextWithoutAndroidResources() {
        DetailGuideContextPresentationFormatter formatter =
            new DetailGuideContextPresentationFormatter(null);
        DetailGuideContextPresentationFormatter.State state = state(
            true,
            false,
            false,
            false,
            "",
            "",
            "",
            "",
            "",
            " [GD-214] Water Storage ",
            " Store water away from heat. "
        );

        assertEquals(
            "Preview here. Open full guide switches pages.",
            formatter.buildRelatedGuidePreviewRowBehaviorText(state)
        );
        assertEquals(
            "Cross-reference",
            formatter.buildRelatedGuidePreviewTitleText(state)
        );
        assertEquals(
            "Cross-reference \u00b7 anchored to [GD-214] Water Storage. Preview linked guides here.",
            formatter.buildRelatedGuidePreviewPanelDescriptionText(state)
        );
        assertEquals("[GD-214] Water Storage", formatter.buildActiveGuideContextPrimaryLabel(state));
        assertEquals(
            "Store water away from heat.\n\nPinned source for cross-reference.",
            formatter.buildActiveGuideContextBody(state)
        );
        assertEquals("Source guide", formatter.buildActiveGuideContextTitleText(state));
        assertEquals(
            "Source guide context. Cross-reference pinned to [GD-214] Water Storage.",
            formatter.buildActiveGuideContextContentDescription(state, "[GD-214] Water Storage")
        );
    }

    @Test
    public void nonRailCrossReferenceCopyUsesStandalonePreviewStrings() {
        DetailGuideContextPresentationFormatter formatter =
            new DetailGuideContextPresentationFormatter(null);
        DetailGuideContextPresentationFormatter.State state = state(
            false,
            false,
            true,
            false,
            "GD-215",
            "Rainwater Catchment",
            "",
            "",
            "",
            "",
            ""
        );

        assertEquals(
            "Preview here. Open full guide switches pages.",
            formatter.buildRelatedGuidePreviewRowBehaviorText(state)
        );
        assertEquals(
            "Selected linked guide",
            formatter.buildRelatedGuidePreviewTitleText(state)
        );
        assertEquals(
            "Preview selected guide before switching pages.",
            formatter.buildRelatedGuidePreviewCaptionText(state)
        );
        assertEquals(
            "Cross-reference. Preview linked guides here.",
            formatter.buildRelatedGuidePreviewPanelDescriptionText(state)
        );
        assertEquals("Loading cross-reference...", formatter.buildRelatedGuidePreviewLoadingText(state));
        assertEquals(
            "No cross-reference text is available in this pack.",
            formatter.buildRelatedGuidePreviewEmptyText(state)
        );
    }

    @Test
    public void activeGuideContextKeepsSelectedLinkedGuideTitleForPhoneGuideMode() {
        DetailGuideContextPresentationFormatter formatter =
            new DetailGuideContextPresentationFormatter(null);
        DetailGuideContextPresentationFormatter.State state = state(
            false,
            true,
            true,
            false,
            "GD-215",
            "Rainwater Catchment",
            "",
            "",
            "",
            "",
            ""
        );

        assertEquals(
            "Selected linked guide",
            formatter.buildRelatedGuidePreviewTitleText(state)
        );
    }

    @Test
    public void currentGuideLabelCombinesIdAndTrimmedTitleWithoutAndroidResources() {
        DetailGuideContextPresentationFormatter formatter =
            new DetailGuideContextPresentationFormatter(null);
        DetailGuideContextPresentationFormatter.State state = state(
            false,
            false,
            false,
            false,
            " GD-999 ",
            " 12345678901234567890123456789012345 Extra ",
            "",
            "",
            "",
            "",
            ""
        );

        assertEquals(
            "GD-999 · 1234567890123456789012345678901234...",
            formatter.buildActiveGuideContextPrimaryLabel(state)
        );
    }

    @Test
    public void utilityRailPreviewTitleIdentifiesSelectedLinkedGuideWithoutAndroidResources() {
        DetailGuideContextPresentationFormatter formatter =
            new DetailGuideContextPresentationFormatter(null);
        DetailGuideContextPresentationFormatter.State state = state(
            false,
            true,
            false,
            true,
            "GD-214",
            "Water Storage",
            "",
            "",
            "",
            "",
            ""
        );

        assertEquals(
            "Selected linked guide",
            formatter.buildRelatedGuidePreviewTitleText(state)
        );
    }

    @Test
    public void emptyHandoffLabelShortCircuitsSummaryWithoutAndroidResources() {
        DetailGuideContextPresentationFormatter formatter =
            new DetailGuideContextPresentationFormatter(null);
        DetailGuideContextPresentationFormatter.State state = state(
            true,
            false,
            false,
            false,
            "",
            "",
            "",
            "",
            "",
            "Source",
            ""
        );

        assertEquals("", formatter.buildCurrentGuideHandoffLabel(state));
        assertEquals("", formatter.buildCurrentGuideHandoffSummary(state));
        assertEquals("", DetailGuideContextPresentationFormatter.buildGuideHandoffSummaryText(null, " ", "Anchor"));
    }

    @Test
    public void nonRailGuideHandoffUsesCrossReferenceLanguage() {
        DetailGuideContextPresentationFormatter formatter =
            new DetailGuideContextPresentationFormatter(null);
        DetailGuideContextPresentationFormatter.State state = state(
            false,
            false,
            true,
            false,
            "GD-214",
            "Water Storage",
            "",
            "",
            "",
            "",
            ""
        );

        assertEquals("Cross-reference", formatter.buildCurrentGuideHandoffLabel(state));
    }

    @Test
    public void utilityRailGuideHandoffUsesCrossReferenceLanguage() {
        DetailGuideContextPresentationFormatter formatter =
            new DetailGuideContextPresentationFormatter(null);
        DetailGuideContextPresentationFormatter.State state = state(
            false,
            false,
            false,
            true,
            "GD-132",
            "Foundry & Metal Casting",
            "",
            "",
            "",
            "",
            ""
        );

        assertEquals("Cross-reference", formatter.buildCurrentGuideHandoffLabel(state));
    }

    private static DetailGuideContextPresentationFormatter.State state(
        boolean answerMode,
        boolean activeGuideContextPanel,
        boolean nonRailCrossReferenceCopy,
        boolean utilityRail,
        String currentGuideId,
        String currentTitle,
        String currentSubtitle,
        String currentGuideModeLabel,
        String currentGuideModeSummary,
        String sourceAnchorLabel,
        String sourceAnchorSubtitle
    ) {
        return new DetailGuideContextPresentationFormatter.State(
            answerMode,
            activeGuideContextPanel,
            nonRailCrossReferenceCopy,
            utilityRail,
            currentGuideId,
            currentTitle,
            currentSubtitle,
            currentGuideModeLabel,
            currentGuideModeSummary,
            sourceAnchorLabel,
            sourceAnchorSubtitle
        );
    }
}
