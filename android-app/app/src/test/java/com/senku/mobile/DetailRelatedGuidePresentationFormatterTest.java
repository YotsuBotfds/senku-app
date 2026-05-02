package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

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
            "\u2014 CROSS-REFERENCE \u00b7 4",
            formatter.buildAnswerModeRelatedGuidesTitle(4)
        );
        assertEquals(
            "2 linked guides \u00b7 from [GD-214] Water Storage.",
            formatter.buildAnswerModeRelatedGuidesSubtitle(
                new DetailRelatedGuidePresentationFormatter.State(false, false, "", "", "[GD-214] Water Storage"),
                2
            )
        );
        assertContainsInOrder(
            formatter.buildNonRailRelatedGuidesSubtitle(
                new DetailRelatedGuidePresentationFormatter.State(
                    false,
                    true,
                    "GD-215",
                    "GD-215 \u00b7 Rainwater Catchment",
                    ""
                ),
                1
            ),
            "1 linked guide",
            "required reading",
            "GD-215",
            "Rainwater Catchment"
        );
    }

    @Test
    public void stationRailSubtitleUsesCrossReferenceOpenedFromCopy() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);

        assertContainsInOrder(
            formatter.buildStationRelatedGuidesSubtitle(
                new DetailRelatedGuidePresentationFormatter.State(true, false, "GD-220", "", ""),
                6
            ),
            "6 linked guides",
            "opened from",
            "GD-220"
        );
    }

    @Test
    public void nonRailPreviewRowDescriptionUsesGuideConnectionLanguage() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);

        assertEquals("Cross-reference", formatter.buildNonRailRelatedGuidesTitle());
        String description = formatter.buildRelatedGuideButtonContentDescription(
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
            );
        assertContainsInOrder(
            description,
            "Cross-reference",
            "linked guide",
            "1 of 1",
            "GD-215",
            "Rainwater Catchment",
            "Anchored to GD-214",
            "Preview"
        );
        assertContains(description, "Open full guide");
    }

    @Test
    public void answerModePanelAndRowsUseGuideConnectionLanguage() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);
        DetailRelatedGuidePresentationFormatter.State state =
            new DetailRelatedGuidePresentationFormatter.State(false, false, "", "", "[GD-214] Water Storage");
        SearchResult guide = new SearchResult("Rainwater Catchment", "", "", "", "GD-215", "", "", "");

        assertContainsInOrder(
            formatter.buildAnswerModeRelatedGuidesPanelContentDescription(state, 2),
            "Cross-reference",
            "2 linked guides",
            "[GD-214] Water Storage"
        );
        String description = formatter.buildAnswerModeRelatedGuideButtonContentDescription(state, guide, 0, 2, true);
        assertContainsInOrder(
            description,
            "Cross-reference",
            "linked guide",
            "1 of 2",
            "GD-215",
            "Rainwater Catchment",
            "Anchored to [GD-214] Water Storage",
            "Previews here"
        );
        assertContains(description, "Open full guide");
    }

    @Test
    public void nonRailOpenDescriptionUsesLinkedGuideCopy() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);

        String description = formatter.buildRelatedGuideButtonContentDescription(
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
            );
        assertContainsInOrder(
            description,
            "Open",
            "cross-reference",
            "linked guide",
            "1 of 1",
            "GD-215",
            "Rainwater Catchment",
            "Anchored to GD-214"
        );
        assertContains(description, "Opens the linked guide page");
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
    public void foundryAbrasivesCrossReferenceUsesGenericMetadataByDefault() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);

        assertEquals(
            "GD-220 \u00b7 Abrasives Manufacturing\nCross-ref",
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
        String description = formatter.buildRelatedGuideButtonContentDescription(
                new DetailRelatedGuidePresentationFormatter.State(false, false, "", "", ""),
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
                ),
                0,
                1,
                false
            );
        assertContainsInOrder(
            description,
            "Open",
            "related guide",
            "1 of 1",
            "GD-220",
            "Abrasives Manufacturing",
            "Category Cross-ref"
        );
        assertContains(description, "Opens the linked guide page");
    }

    @Test
    public void foundryAbrasivesCrossReferencePresentsAsAnchorInReviewDemoMode() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null, true);

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
        String description = formatter.buildRelatedGuideButtonContentDescription(
                new DetailRelatedGuidePresentationFormatter.State(false, false, "", "", ""),
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
                ),
                0,
                1,
                false
            );
        assertContainsInOrder(
            description,
            "Open",
            "related guide",
            "1 of 1",
            "GD-220",
            "Abrasives Manufacturing",
            "Category Anchor"
        );
        assertContains(description, "Opens the linked guide page");
    }

    @Test
    public void answerModeRelatedGuideLabelMatchesMockRowCopy() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null, true);

        assertEquals(
            "GD-294 \u00b7 Cave Shelter Systems & Cold-Weather",
            formatter.buildAnswerModeRelatedGuideButtonLabel(
                new SearchResult("Cave Shelter Systems and Long-Term Habitation", "", "", "", "GD-294", "", "", "")
            )
        );
        assertEquals(
            "GD-695 \u00b7 Hurricane & Severe Storm Sheltering",
            formatter.buildAnswerModeRelatedGuideButtonLabel(
                new SearchResult("Hurricane & Severe Storm Preparedness", "", "", "", "GD-695", "", "", "")
            )
        );
        assertEquals(
            "GD-484 \u00b7 Insulation Materials & Cold-Soak",
            formatter.buildAnswerModeRelatedGuideButtonLabel(
                new SearchResult("Insulation Materials & Thermal Design", "", "", "", "GD-484", "", "", "")
            )
        );
        assertEquals(
            "GD-027 \u00b7 Primitive Technology & Stone Age",
            formatter.buildAnswerModeRelatedGuideButtonLabel(
                new SearchResult("Primitive Technology and Stone Age Skills", "", "", "", "GD-027", "", "", "")
            )
        );
    }

    @Test
    public void answerModeContentDescriptionUsesCompactCanonicalRowCopy() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null, true);

        String description = formatter.buildAnswerModeRelatedGuideButtonContentDescription(
                new DetailRelatedGuidePresentationFormatter.State(
                    false,
                    false,
                    "",
                    "",
                    "GD-345 \u00b7 Tarp & Cord Shelters"
                ),
                new SearchResult(
                    "Cave Shelter Systems and Long-Term Habitation",
                    "",
                    "",
                    "",
                    "GD-294",
                    "",
                    "survival",
                    ""
                ),
                0,
                4,
                false
            );
        assertContainsInOrder(
            description,
            "Cross-reference",
            "linked guide",
            "1 of 4",
            "GD-294",
            "Cave Shelter Systems & Cold-Weather",
            "Anchored to GD-345 \u00b7 Tarp & Cord Shelters"
        );
        assertContains(description, "Opens this cross-reference guide");
    }

    @Test
    public void answerModeRelatedGuideLabelsPreserveWave79VisibleOrderWithoutGd109() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null, true);
        SearchResult[] guides = new SearchResult[] {
            new SearchResult("Cave Shelter Systems and Long-Term Habitation", "", "", "", "GD-294", "", "survival", ""),
            new SearchResult("Hurricane & Severe Storm Preparedness", "", "", "", "GD-695", "", "weather", ""),
            new SearchResult("Insulation Materials & Thermal Design", "", "", "", "GD-484", "", "materials", ""),
            new SearchResult("Primitive Technology and Stone Age Skills", "", "", "", "GD-027", "", "survival", "")
        };

        assertEquals(
            "GD-294 \u00b7 Cave Shelter Systems & Cold-Weather",
            formatter.buildAnswerModeRelatedGuideButtonLabel(guides[0])
        );
        assertEquals(
            "GD-695 \u00b7 Hurricane & Severe Storm Sheltering",
            formatter.buildAnswerModeRelatedGuideButtonLabel(guides[1])
        );
        assertEquals(
            "GD-484 \u00b7 Insulation Materials & Cold-Soak",
            formatter.buildAnswerModeRelatedGuideButtonLabel(guides[2])
        );
        assertEquals(
            "GD-027 \u00b7 Primitive Technology & Stone Age",
            formatter.buildAnswerModeRelatedGuideButtonLabel(guides[3])
        );
    }

    @Test
    public void defaultAnswerModeRelatedGuideLabelsKeepLiveTitlesWithoutReviewCanonicalization() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null, false);

        assertEquals(
            "GD-294 \u00b7 Cave Shelter Systems and Long-Term Habitation",
            formatter.buildAnswerModeRelatedGuideButtonLabel(
                new SearchResult("Cave Shelter Systems and Long-Term Habitation", "", "", "", "GD-294", "", "", "")
            )
        );
        assertEquals(
            "GD-027 \u00b7 Primitive Technology and Stone Age Skills",
            formatter.buildAnswerModeRelatedGuideButtonLabel(
                new SearchResult("Primitive Technology and Stone Age Skills", "", "", "", "GD-027", "", "", "")
            )
        );
    }

    @Test
    public void relatedGuideGraphAnchorUsesSelectedSourceWhenItHasGuideId() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null, false);
        SearchResult selected = new SearchResult("Rainwater Catchment", "", "", "", "GD-215", "Storage", "", "");
        String selectedKey = DetailProvenancePresentationFormatter.buildSourceSelectionKey(selected);

        assertEquals(
            "GD-215",
            formatter.selectedSourceForRelatedGuideGraph(
                true,
                false,
                selectedKey,
                Arrays.asList(
                    new SearchResult("Water Storage", "", "", "", "GD-214", "", "", ""),
                    selected
                )
            ).guideId
        );
    }

    @Test
    public void selectedSourceIdentityFeedsProvenanceChipsEvidenceAndRelatedGraph() {
        DetailSourcePresentationFormatter sourceFormatter = new DetailSourcePresentationFormatter(null);
        DetailProvenancePresentationFormatter provenanceFormatter = new DetailProvenancePresentationFormatter(null);
        DetailRelatedGuidePresentationFormatter relatedFormatter =
            new DetailRelatedGuidePresentationFormatter(null, false);
        SearchResult anchor = new SearchResult(
            "Abrasives Manufacturing",
            "",
            "Anchor support",
            "Anchor body",
            "GD-220",
            "Anchor Source",
            "workshop",
            "guide-focus"
        );
        SearchResult selected = new SearchResult(
            "Tarp & Cord Shelters",
            "",
            "Rain shelter support",
            "Pitch the tarp ridge along the prevailing wind.",
            "GD-345",
            "Ridgeline pitch",
            "shelter",
            "route-focus"
        );
        String selectedKey = DetailProvenancePresentationFormatter.buildSourceSelectionKey(selected);

        assertEquals(
            "GD-345",
            provenanceFormatter.selectedSourceForProvenanceAction(
                Arrays.asList(anchor, selected),
                selectedKey
            ).guideId
        );
        SearchResult graphSource = relatedFormatter.selectedSourceForRelatedGuideGraph(
            true,
            false,
            selectedKey,
            Arrays.asList(anchor, selected)
        );
        assertEquals("GD-345", graphSource.guideId);
        assertEquals("Tarp & Cord Shelters", graphSource.title);
        assertEquals(
            "GD-345 \u00b7 Ridgeline pitch",
            sourceFormatter.buildInlineSourceChipLabel(selected, "GD-220", false)
        );
        assertEquals(
            "GD-345 \u00b7 Tarp & Cord Shelters",
            relatedFormatter.buildAnswerModeRelatedGuidesAnchorLabel(graphSource)
        );

        DetailSourcePresentationFormatter.EvidenceCard evidenceCard =
            sourceFormatter.buildEvidenceCard(selected, 1, "related");
        String evidenceLabel = sourceFormatter.buildEvidenceCardRowLabel(evidenceCard);
        assertContainsInOrder(evidenceLabel, "GD-345", "RELATED", "Tarp & Cord Shelters", "Ridgeline pitch");
    }

    @Test
    public void relatedGuideGraphAnchorRejectsSelectedPlaceholder() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null, false);
        SearchResult placeholder = new SearchResult("Placeholder", "", "", "", "", "", "", "");
        String selectedKey = DetailProvenancePresentationFormatter.buildSourceSelectionKey(placeholder);

        assertNull(
            formatter.selectedSourceForRelatedGuideGraph(
                true,
                false,
                selectedKey,
                Arrays.asList(
                    placeholder,
                    new SearchResult("Water Storage", "", "", "", "GD-214", "", "", "")
                )
            )
        );
    }

    @Test
    public void relatedGuideGraphAnchorPromotesRainShelterOnlyInReviewMode() {
        SearchResult anchor = new SearchResult("Abrasives Manufacturing", "", "", "", "GD-220", "", "", "");
        SearchResult rainShelter = new SearchResult(
            "Tarp & Cord Shelters",
            "",
            "",
            "",
            "GD-345",
            "",
            "",
            "",
            "",
            "",
            "",
            "rain shelter tarp cord"
        );

        assertEquals(
            "GD-220",
            new DetailRelatedGuidePresentationFormatter(null, false)
                .selectedSourceForRelatedGuideGraph(true, false, "", Arrays.asList(anchor, rainShelter))
                .guideId
        );
        assertEquals(
            "GD-345",
            new DetailRelatedGuidePresentationFormatter(null, true)
                .selectedSourceForRelatedGuideGraph(true, true, "", Arrays.asList(anchor, rainShelter))
                .guideId
        );
    }

    @Test
    public void relatedGuidePreviewMergeKeepsListMetadataWhenLoadedGuideOmitsFields() {
        DetailRelatedGuidePresentationFormatter formatter = new DetailRelatedGuidePresentationFormatter(null);

        SearchResult merged = formatter.mergeRelatedGuideForPreview(
            new SearchResult(
                "List title",
                "List subtitle",
                "List snippet",
                "List body",
                "GD-215",
                "List section",
                "water",
                "related",
                "cross_reference",
                "",
                "",
                "rainwater"
            ),
            new SearchResult(
                "Loaded title",
                "",
                "",
                "Loaded body",
                "GD-215",
                "",
                "",
                "guide-focus",
                "",
                "long",
                "howto",
                ""
            )
        );

        assertEquals("Loaded title", merged.title);
        assertEquals("List subtitle", merged.subtitle);
        assertEquals("List snippet", merged.snippet);
        assertEquals("Loaded body", merged.body);
        assertEquals("List section", merged.sectionHeading);
        assertEquals("water", merged.category);
        assertEquals("guide-focus", merged.retrievalMode);
        assertEquals("cross_reference", merged.contentRole);
        assertEquals("long", merged.timeHorizon);
        assertEquals("howto", merged.structureType);
        assertEquals("rainwater", merged.topicTags);
    }

    private static void assertContains(String actual, String expected) {
        assertTrue("Expected <" + actual + "> to contain <" + expected + ">", actual.contains(expected));
    }

    private static void assertContainsInOrder(String actual, String... expectedTokens) {
        int previousIndex = -1;
        for (String expectedToken : expectedTokens) {
            int index = actual.indexOf(expectedToken, previousIndex + 1);
            assertTrue("Expected <" + actual + "> to contain <" + expectedToken + "> after index " + previousIndex, index >= 0);
            previousIndex = index;
        }
    }
}
