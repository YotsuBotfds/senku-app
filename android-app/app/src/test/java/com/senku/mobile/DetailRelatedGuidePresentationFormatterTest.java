package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class DetailRelatedGuidePresentationFormatterTest {
    @Test
    public void answerModeAnchorLabelPrefersGuideIdAndTrimmedTitle() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);

        assertEquals(
            "GD-214 · Water Storage",
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
            "GD-214",
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
    public void relatedGuideContextLabelUsesCompactAnchorAndRequiredMetadata() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);

        assertEquals(
            "GD-132 \u00b7 Foundry\nAnchor",
            formatter.buildRelatedGuideButtonLabel(
                new SearchResult("Foundry", "", "", "", "GD-132", "", "", "", "source_anchor", "", "", "")
            )
        );
        assertEquals(
            "GD-499 \u00b7 Bellows\nRequired",
            formatter.buildRelatedGuideButtonLabel(
                new SearchResult("Bellows", "", "", "", "GD-499", "", "", "", "required_reading", "", "", "")
            )
        );
    }

    @Test
    public void guideConnectionSubtitlesUseCompactActionCopy() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);

        assertEquals(
            "RELATED GUIDES - 4",
            formatter.buildAnswerModeRelatedGuidesTitle(4)
        );
        assertEquals(
            "2 guides",
            formatter.buildAnswerModeRelatedGuidesSubtitle(
                new DetailRelatedGuidePresentationFormatter.State(false, false, "", "", "[GD-214] Water Storage"),
                2
            )
        );
        assertEquals(
            "1 linked guide \u00b7 required reading \u00b7 GD-215 \u00b7 Rainwater Catchment.",
            formatter.buildNonRailRelatedGuidesSubtitle(
                new DetailRelatedGuidePresentationFormatter.State(
                    false,
                    true,
                    "GD-215",
                    "GD-215 \u00b7 Rainwater Catchment",
                    ""
                ),
                1
            )
        );
    }

    @Test
    public void stationRailSubtitleUsesCrossReferenceOpenedFromCopy() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);

        assertEquals(
            "6 linked guides \u00b7 opened from GD-220.",
            formatter.buildStationRelatedGuidesSubtitle(
                new DetailRelatedGuidePresentationFormatter.State(true, false, "GD-220", "", ""),
                6
            )
        );
    }

    @Test
    public void nonRailPreviewRowDescriptionUsesGuideConnectionLanguage() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);

        assertEquals("Cross-reference", formatter.buildNonRailRelatedGuidesTitle());
        assertEquals(
            "Cross-reference linked guide 1 of 1. GD-215 \u00b7 Rainwater Catchment. Anchored to GD-214. Preview here. Open full guide switches pages.",
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
            "Related guides. 2 guides",
            formatter.buildAnswerModeRelatedGuidesPanelContentDescription(state, 2)
        );
        assertEquals(
            "Related guide 1 of 2. GD-215 \u00b7 Rainwater Catchment. Supports [GD-214] Water Storage. Previews here. Open full guide switches pages.",
            formatter.buildAnswerModeRelatedGuideButtonContentDescription(state, guide, 0, 2, true)
        );
    }

    @Test
    public void nonRailOpenDescriptionUsesLinkedGuideCopy() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);

        assertEquals(
            "Open cross-reference linked guide 1 of 1. GD-215 \u00b7 Rainwater Catchment. Anchored to GD-214. Opens the linked guide page in the installed pack.",
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
                false
            )
        );
    }

    @Test
    public void relatedGuideContextLabelCompactsCrossReferenceMetadata() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);

        assertEquals(
            "GD-215 \u00b7 Rainwater Catchment\nCross-ref",
            formatter.buildRelatedGuideButtonLabel(
                new SearchResult("Rainwater Catchment", "", "", "", "GD-215", "", "", "", "cross_reference", "", "", "")
            )
        );
    }

    @Test
    public void foundryAbrasivesCrossReferencePresentsAsAnchor() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);

        assertEquals(
            "GD-220 \u00b7 Abrasives Manufacturing\nAnchor",
            formatter.buildRelatedGuideButtonLabel(
                new SearchResult(
                    "Abrasives Manufacturing",
                    "",
                    "",
                    "",
                    "GD-220",
                    "",
                    "",
                    "",
                    "cross_reference",
                    "",
                    "",
                    ""
                )
            )
        );
    }

    @Test
    public void answerModeRelatedGuideLabelMatchesMockRowCopy() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);

        assertEquals(
            "GD-294 Cave Shelter Systems & Cold-Weather",
            formatter.buildAnswerModeRelatedGuideButtonLabel(
                new SearchResult("Cave Shelter Systems & Cold-Weather", "", "", "", "GD-294", "", "", "")
            )
        );
        assertEquals(
            "GD-695 Hurricane & Severe Storm Sheltering",
            formatter.buildAnswerModeRelatedGuideButtonLabel(
                new SearchResult("Hurricane & Severe Storm Sheltering", "", "", "", "GD-695", "", "", "")
            )
        );
    }
}
