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
            "Preview this linked guide here, then use Open full guide when ready.",
            formatter.buildRelatedGuidePreviewRowBehaviorText(state)
        );
        assertEquals(
            "Selected linked guide preview. Guide cross-reference stays anchored to [GD-214] Water Storage while this preview is open. Use Open full guide to navigate to the selected guide page.",
            formatter.buildRelatedGuidePreviewPanelDescriptionText(state)
        );
        assertEquals("[GD-214] Water Storage", formatter.buildActiveGuideContextPrimaryLabel(state));
        assertEquals(
            "Store water away from heat.\n\nGuide cross-reference stays anchored to this selected source while the preview is open.",
            formatter.buildActiveGuideContextBody(state)
        );
        assertEquals("Selected source guide", formatter.buildActiveGuideContextTitleText(state));
        assertEquals(
            "Selected source guide context. Guide cross-reference stays anchored to [GD-214] Water Storage.",
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
            "Preview this linked guide here, then use Open full guide when ready.",
            formatter.buildRelatedGuidePreviewRowBehaviorText(state)
        );
        assertEquals(
            "Preview the selected linked guide here, or open the full guide.",
            formatter.buildRelatedGuidePreviewCaptionText(state)
        );
        assertEquals(
            "Selected linked guide preview. Inspect it on this page, then use Open full guide to navigate to the selected guide page.",
            formatter.buildRelatedGuidePreviewPanelDescriptionText(state)
        );
        assertEquals("Loading linked guide preview...", formatter.buildRelatedGuidePreviewLoadingText(state));
        assertEquals(
            "No linked guide preview text is available in this pack.",
            formatter.buildRelatedGuidePreviewEmptyText(state)
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
            "[GD-999] 1234567890123456789012345678901234...",
            formatter.buildActiveGuideContextPrimaryLabel(state)
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
