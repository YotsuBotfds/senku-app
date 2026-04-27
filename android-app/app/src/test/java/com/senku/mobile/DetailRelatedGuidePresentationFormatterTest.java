package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class DetailRelatedGuidePresentationFormatterTest {
    @Test
    public void answerModeAnchorLabelPrefersGuideIdAndTrimmedTitle() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);

        assertEquals(
            "[GD-214] Water Storage",
            formatter.buildAnswerModeRelatedGuidesAnchorLabel(
                new SearchResult("Water Storage", "", "", "", "GD-214", "", "", "")
            )
        );
        assertEquals(
            "Water Storage",
            formatter.buildAnswerModeRelatedGuidesAnchorLabel(
                new SearchResult("Water Storage", "", "", "", "", "", "", "")
            )
        );
        assertEquals(
            "[GD-214]",
            formatter.buildAnswerModeRelatedGuidesAnchorLabel(
                new SearchResult("", "", "", "", "GD-214", "", "", "")
            )
        );
    }

    @Test
    public void contextualFollowupQueryUsesTargetTitleAndSourceAnchor() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);
        DetailRelatedGuidePresentationFormatter.State state =
            new DetailRelatedGuidePresentationFormatter.State(false, false, "", "", "[GD-214] Water Storage");

        assertEquals(
            "What should I know next about Rainwater Catchment for [GD-214] Water Storage?",
            formatter.buildContextualFollowupQuery(
                state,
                new SearchResult("Rainwater Catchment", "", "", "", "GD-215", "", "", ""),
                "GD-214"
            )
        );
        assertEquals(
            "What should I know next about GD-215?",
            formatter.buildContextualFollowupQuery(
                new DetailRelatedGuidePresentationFormatter.State(false, false, "", "", ""),
                new SearchResult("", "", "", "", "GD-215", "", "", ""),
                ""
            )
        );
    }

    @Test
    public void guideConnectionSubtitlesUseCompactActionCopy() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);

        assertEquals(
            "2 related guides for [GD-214] Water Storage. Preview, then open.",
            formatter.buildAnswerModeRelatedGuidesSubtitle(
                new DetailRelatedGuidePresentationFormatter.State(false, false, "", "", "[GD-214] Water Storage"),
                2
            )
        );
        assertEquals(
            "1 related guide for [GD-215] Rainwater Catchment. Preview, then open.",
            formatter.buildNonRailRelatedGuidesSubtitle(
                new DetailRelatedGuidePresentationFormatter.State(
                    false,
                    true,
                    "GD-215",
                    "[GD-215] Rainwater Catchment",
                    ""
                ),
                1
            )
        );
    }

    @Test
    public void nonRailPreviewRowDescriptionUsesGuideConnectionLanguage() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);

        assertEquals(
            "Related guide 1 of 1. [GD-215] Rainwater Catchment. Guide connection from GD-214. Preview this related guide here, then use Open full guide when ready.",
            formatter.buildRelatedGuideButtonContentDescription(
                new DetailRelatedGuidePresentationFormatter.State(
                    false,
                    true,
                    "GD-214",
                    "[GD-214] Water Storage",
                    ""
                ),
                new SearchResult("Rainwater Catchment", "", "", "", "GD-215", "", "", ""),
                0,
                1,
                true
            )
        );
    }

    @Test
    public void answerModePanelAndRowsUseGuideConnectionLanguage() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);
        DetailRelatedGuidePresentationFormatter.State state =
            new DetailRelatedGuidePresentationFormatter.State(false, false, "", "", "[GD-214] Water Storage");
        SearchResult guide = new SearchResult("Rainwater Catchment", "", "", "", "GD-215", "", "", "");

        assertEquals(
            "Source-anchored guide connections. 2 related guides for [GD-214] Water Storage. Preview, then open.",
            formatter.buildAnswerModeRelatedGuidesPanelContentDescription(state, 2)
        );
        assertEquals(
            "Connected guide 1 of 2. [GD-215] Rainwater Catchment. Linked from [GD-214] Water Storage. Previews the related guide on this page while staying anchored to the selected source guide. Use Open full guide when ready to switch pages.",
            formatter.buildAnswerModeRelatedGuideButtonContentDescription(state, guide, 0, 2, true)
        );
    }
}
