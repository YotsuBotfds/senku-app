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
    public void crossReferenceSubtitlesUseCompactActionCopy() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);

        assertEquals(
            "2 linked guides for [GD-214] Water Storage. Preview here, or open the full guide.",
            formatter.buildAnswerModeRelatedGuidesSubtitle(
                new DetailRelatedGuidePresentationFormatter.State(false, false, "", "", "[GD-214] Water Storage"),
                2
            )
        );
        assertEquals(
            "1 linked guide for [GD-215] Rainwater Catchment. Preview here, or open the full guide.",
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
    public void nonRailPreviewRowDescriptionNamesExactOpenAction() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);

        assertEquals(
            "Linked guide 1 of 1. [GD-215] Rainwater Catchment. Cross-reference from GD-214. Preview this linked guide here, then use Open full guide when ready.",
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
}
