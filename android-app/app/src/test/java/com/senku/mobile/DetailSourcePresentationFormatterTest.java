package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public final class DetailSourcePresentationFormatterTest {
    @Test
    public void evidenceCardBuildsAnchorContractFromSearchResult() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);
        SearchResult source = new SearchResult(
            "Emergency Shelter",
            "",
            "Pitch ridgeline along prevailing wind.",
            "",
            "GD-444",
            "Ridge Line Setup",
            "survival",
            "hybrid"
        );

        DetailSourcePresentationFormatter.EvidenceCard card = formatter.buildEvidenceCard(source, 0, "anchor");

        assertEquals("GD-444", card.guideId);
        assertEquals("ANCHOR", card.roleLabel);
        assertEquals("78%", card.matchLabel);
        assertEquals("Emergency Shelter", card.title);
        assertEquals("Pitch ridgeline along prevailing wind.", card.quote);
        assertEquals("Ridge Line Setup", card.anchorLabel);
        assertTrue(card.isAnchor);
    }

    @Test
    public void evidenceCardBuildsRelatedContractFromPositionAndMode() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);
        SearchResult source = new SearchResult(
            "Rainwater Catchment",
            "",
            "",
            "Use first-flush diversion before storage.\nKeep tank covered.",
            "GD-215",
            "Storage Prep",
            "water",
            "lexical"
        );

        DetailSourcePresentationFormatter.EvidenceCard card = formatter.buildEvidenceCard(source, 2, "related");

        assertEquals("GD-215", card.guideId);
        assertEquals("TOPIC", card.roleLabel);
        assertEquals("56%", card.matchLabel);
        assertEquals("Rainwater Catchment", card.title);
        assertEquals("Use first-flush diversion before storage.", card.quote);
        assertEquals("Storage Prep", card.anchorLabel);
        assertFalse(card.isAnchor);
    }

    @Test
    public void evidenceCardHandlesBlankSourceAsStableFallback() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);

        DetailSourcePresentationFormatter.EvidenceCard card = formatter.buildEvidenceCard(null, 3, "topic");

        assertEquals("", card.guideId);
        assertEquals("TOPIC", card.roleLabel);
        assertEquals("55%", card.matchLabel);
        assertEquals("Open guide note", card.title);
        assertEquals("", card.quote);
        assertEquals("", card.anchorLabel);
        assertFalse(card.isAnchor);
    }

    @Test
    public void evidenceCardRecognizesOpenSourceModeAndAnswerCardMatch() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);
        SearchResult source = new SearchResult(
            "Reviewed source",
            "",
            "Reviewed quote",
            "",
            "GD-898",
            "Poisoning unknown ingestion",
            "medicine",
            "answer-card"
        );

        DetailSourcePresentationFormatter.EvidenceCard card = formatter.buildEvidenceCard(source, 1, "source");

        assertEquals("SOURCE", card.roleLabel);
        assertEquals("76%", card.matchLabel);
        assertFalse(card.isAnchor);
    }

    @Test
    public void sourceChipContentDescriptionClampsIndexAndTotal() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);
        SearchResult source = new SearchResult(
            "Emergency Shelter",
            "",
            "",
            "",
            "GD-444",
            "Ridge Line Setup",
            "survival",
            "guide-focus"
        );

        assertContainsAll(
            formatter.buildInlineSourceChipContentDescription(source, true, true, -4, 0),
            "Anchor guide",
            "GD-444",
            "Ridge Line Setup",
            "Shows source preview"
        );
        assertContainsAll(
            formatter.buildInlineSourceChipContentDescription(source, false, false, 1, 3),
            "Source guide",
            "2 of 3",
            "GD-444",
            "Ridge Line Setup",
            "Opens source guide"
        );
    }

    @Test
    public void compactSourceTriggerLabelCarriesGuideAndGuideCount() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);

        assertEquals(
            "GD-444 - SOURCES (2 sources)",
            formatter.buildCompactInlineSourceTriggerLabel(
                new SearchResult("Emergency Shelter", "", "", "", "GD-444", "", "", "guide-focus"),
                2
            )
        );
        assertEquals(
            "SOURCES (0 sources)",
            formatter.buildCompactInlineSourceTriggerLabel(new SearchResult("", "", "", ""), -1)
        );
    }

    @Test
    public void sourceButtonContentDescriptionUsesGuideRolesAndActions() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);
        SearchResult source = new SearchResult(
            "Emergency Shelter",
            "",
            "",
            "",
            "GD-444",
            "Ridge Line Setup",
            "survival",
            "guide-focus"
        );

        assertContainsAll(
            formatter.buildSourceButtonContentDescription(source, true, 0, 2, true),
            "Anchor guide",
            "1 of 2",
            "[GD-444]",
            "Emergency Shelter",
            "Ridge Line Setup",
            "Shows source preview"
        );
        assertContainsAll(
            formatter.buildSourceButtonContentDescription(source, true, 1, 2, false),
            "Related guide",
            "2 of 2",
            "[GD-444]",
            "Emergency Shelter",
            "Ridge Line Setup",
            "Shows source preview"
        );
        assertContainsAll(
            formatter.buildSourceButtonContentDescription(source, false, 1, 2, false),
            "Source guide",
            "2 of 2",
            "[GD-444]",
            "Emergency Shelter",
            "Ridge Line Setup",
            "Opens source guide"
        );
    }

    @Test
    public void evidenceCardRowLabelPreservesCurrentSourceButtonShape() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);
        DetailSourcePresentationFormatter.EvidenceCard card = formatter.buildEvidenceCard(
            new SearchResult(
                "Emergency Shelter",
                "",
                "Pitch ridgeline along prevailing wind.",
                "",
                "GD-444",
                "Ridge Line Setup",
                "survival",
                "hybrid"
            ),
            0,
            "anchor"
        );

        assertContainsAll(
            formatter.buildEvidenceCardRowLabel(card),
            "GD-444",
            "ANCHOR",
            "78%",
            "Emergency Shelter",
            "Ridge Line Setup",
            "Pitch ridgeline along prevailing wind."
        );
    }

    @Test
    public void evidenceCardRowContentDescriptionCarriesRoleRankAndMatch() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);
        DetailSourcePresentationFormatter.EvidenceCard anchorCard = formatter.buildEvidenceCard(
            new SearchResult(
                "Emergency Shelter",
                "",
                "Pitch ridgeline along prevailing wind.",
                "",
                "GD-444",
                "Ridge Line Setup",
                "survival",
                "hybrid"
            ),
            0,
            "anchor"
        );
        DetailSourcePresentationFormatter.EvidenceCard relatedCard = formatter.buildEvidenceCard(
            new SearchResult(
                "Rainwater Catchment",
                "",
                "",
                "Use first-flush diversion before storage.\nKeep tank covered.",
                "GD-215",
                "Storage Prep",
                "water",
                "lexical"
            ),
            2,
            "related"
        );

        assertContainsAll(
            formatter.buildEvidenceCardRowContentDescription(anchorCard, true, 0, 2),
            "Anchor guide",
            "1 of 2",
            "78% match",
            "GD-444",
            "Emergency Shelter",
            "Ridge Line Setup",
            "Pitch ridgeline along prevailing wind.",
            "Shows source preview"
        );
        assertContainsAll(
            formatter.buildEvidenceCardRowContentDescription(relatedCard, false, 2, 3),
            "Related guide",
            "3 of 3",
            "56% match",
            "GD-215",
            "Rainwater Catchment",
            "Storage Prep",
            "Use first-flush diversion before storage.",
            "Opens source guide"
        );
    }

    @Test
    public void stationEvidenceCardRowLabelUsesCompactMetadataPrefix() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);
        DetailSourcePresentationFormatter.EvidenceCard card = formatter.buildEvidenceCard(
            new SearchResult(
                "Reviewed source",
                "",
                "Reviewed quote",
                "",
                "GD-898",
                "Poisoning unknown ingestion",
                "medicine",
                "answer-card"
            ),
            1,
            "source"
        );

        String label = formatter.buildStationEvidenceCardRowLabel(card, 1, 4);

        assertTrue(label, label.startsWith("SOURCE 2/4"));
        assertContainsAll(
            label,
            "GD-898",
            "SOURCE",
            "76%",
            "Reviewed source",
            "Poisoning unknown ingestion",
            "Reviewed quote"
        );
    }

    @Test
    public void stationSourceButtonLabelUsesAnswerSourceStackContract() {
        DetailSourcePresentationFormatter formatter = reviewDemoFormatter();

        assertEquals(
            "GD-220 \u00B7 ANCHOR\nAbrasives Manufacturing",
            formatter.buildStationSourceButtonLabel(
                new SearchResult(
                    "Abrasives Manufacturing",
                    "",
                    "",
                    "",
                    "GD-220",
                    "",
                    "materials",
                    "hybrid"
                ),
                0,
                3,
                true
            )
        );
        assertEquals(
            "GD-132 \u00B7 RELATED\nFoundry & Metal Casting",
            formatter.buildStationSourceButtonLabel(
                new SearchResult(
                    "Foundry & Metal Casting",
                    "",
                    "",
                    "",
                    "GD-132",
                    "",
                    "metal",
                    "guide-focus"
                ),
                1,
                3,
                false
            )
        );
        assertEquals(
            "GD-345 \u00B7 TOPIC\nTarp & Cord Shelters",
            formatter.buildStationSourceButtonLabel(
                new SearchResult(
                    "Primitive Shelter Construction Techniques",
                    "",
                    "A simple ridgeline shelter requires only tarp, cord, and two anchor points.",
                    "",
                    "GD-345",
                    "Wood Quality Evaluation for Shelter Construction",
                    "survival",
                    "lexical",
                    "",
                    "",
                    "emergency_shelter",
                    "foundation,weatherproofing,site_selection"
                ),
                2,
                3,
                false
            )
        );
    }

    @Test
    public void stationSourceButtonLabelNormalizesRainShelterLiveStack() {
        DetailSourcePresentationFormatter formatter = reviewDemoFormatter();
        SearchResult abrasiveAnchor = reviewedStackResult("GD-220", "Abrasives Manufacturing", "abrasives manufacturing");
        SearchResult foundryRelated = reviewedStackResult("GD-132", "Foundry & Metal Casting", "foundry metal casting");
        SearchResult rainShelter = reviewedStackResult("GD-345", "Wood Quality Evaluation for Shelter Construction", "tarp cord rain shelter");

        assertEquals(
            "GD-220 \u00B7 ANCHOR\nAbrasives Manufacturing",
            formatter.buildStationSourceButtonLabel(abrasiveAnchor, 0, 3, true)
        );
        assertEquals(
            "GD-132 \u00B7 RELATED\nFoundry & Metal Casting",
            formatter.buildStationSourceButtonLabel(foundryRelated, 1, 3, false)
        );
        assertEquals(
            "GD-345 \u00B7 TOPIC\nTarp & Cord Shelters",
            formatter.buildStationSourceButtonLabel(rainShelter, 2, 3, false)
        );
    }

    @Test
    public void evidenceCardsUseRainShelterMockRolesAndScores() {
        DetailSourcePresentationFormatter formatter = reviewDemoFormatter();
        SearchResult abrasiveAnchor = reviewedStackResult("GD-220", "Abrasives Manufacturing", "abrasives manufacturing");
        SearchResult foundryRelated = reviewedStackResult("GD-132", "Foundry & Metal Casting", "foundry metal casting");
        SearchResult rainShelter = reviewedStackResult("GD-345", "Wood Quality Evaluation for Shelter Construction", "tarp cord rain shelter");

        DetailSourcePresentationFormatter.EvidenceCard anchor =
            formatter.buildEvidenceCard(abrasiveAnchor, 0, "anchor");
        DetailSourcePresentationFormatter.EvidenceCard related =
            formatter.buildEvidenceCard(foundryRelated, 1, "related");
        DetailSourcePresentationFormatter.EvidenceCard topic =
            formatter.buildEvidenceCard(rainShelter, 2, "related");

        assertEquals("ANCHOR", anchor.roleLabel);
        assertEquals("74%", anchor.matchLabel);
        assertEquals("Abrasives Manufacturing", anchor.title);
        assertEquals(
            "Every melt starts with a foundry safety check, not with metal charge...",
            anchor.quote
        );
        assertEquals("RELATED", related.roleLabel);
        assertEquals("68%", related.matchLabel);
        assertEquals("Foundry & Metal Casting", related.title);
        assertEquals(
            "Pitch the ridgeline along prevailing wind. Tension corners with prusik or taut-line hitches.",
            related.quote
        );
        assertEquals("TOPIC", topic.roleLabel);
        assertEquals("61%", topic.matchLabel);
        assertEquals("Tarp & Cord Shelters", topic.title);
        assertEquals("A simple ridgeline shelter requires only tarp, cord, and two anchor points.", topic.quote);
    }

    @Test
    public void evidenceCardsKeepRainShelterRolesWhenLiveStackArrivesTopicFirst() {
        DetailSourcePresentationFormatter formatter = reviewDemoFormatter();
        SearchResult rainShelter = reviewedStackResult("GD-345", "Wood Quality Evaluation for Shelter Construction", "tarp cord rain shelter");
        SearchResult abrasiveAnchor = reviewedStackResult("GD-220", "Abrasives Manufacturing", "abrasives manufacturing");
        SearchResult foundryRelated = reviewedStackResult("GD-132", "Foundry & Metal Casting", "foundry metal casting");

        DetailSourcePresentationFormatter.EvidenceCard topic =
            formatter.buildEvidenceCard(rainShelter, 0, "anchor");
        DetailSourcePresentationFormatter.EvidenceCard anchor =
            formatter.buildEvidenceCard(abrasiveAnchor, 1, "related");
        DetailSourcePresentationFormatter.EvidenceCard related =
            formatter.buildEvidenceCard(foundryRelated, 2, "related");

        assertEquals("TOPIC", topic.roleLabel);
        assertEquals("61%", topic.matchLabel);
        assertEquals("Tarp & Cord Shelters", topic.title);
        assertFalse(topic.isAnchor);
        assertEquals("ANCHOR", anchor.roleLabel);
        assertEquals("74%", anchor.matchLabel);
        assertEquals("Abrasives Manufacturing", anchor.title);
        assertTrue(anchor.isAnchor);
        assertEquals("RELATED", related.roleLabel);
        assertEquals("68%", related.matchLabel);
        assertEquals("Foundry & Metal Casting", related.title);
        assertFalse(related.isAnchor);
    }

    @Test
    public void stationEvidenceCardRowLabelPreservesRainShelterTopicRole() {
        DetailSourcePresentationFormatter formatter = reviewDemoFormatter();
        DetailSourcePresentationFormatter.EvidenceCard topic = formatter.buildEvidenceCard(
            reviewedStackResult("GD-345", "Wood Quality Evaluation for Shelter Construction", "tarp cord rain shelter"),
            0,
            "anchor"
        );

        String stationLabel = formatter.buildStationEvidenceCardRowLabel(topic, 0, 3);
        String rowLabel = formatter.buildEvidenceCardRowLabel(topic);

        assertTrue(stationLabel, stationLabel.startsWith("TOPIC 1/3"));
        assertContainsAll(
            stationLabel,
            "GD-345",
            "TOPIC",
            "61%",
            "Tarp & Cord Shelters",
            "A simple ridgeline shelter requires only tarp, cord, and two anchor points."
        );
        assertFalse(rowLabel, rowLabel.contains("1/3"));
        assertContainsAll(
            rowLabel,
            "GD-345",
            "TOPIC",
            "61%",
            "Tarp & Cord Shelters",
            "A simple ridgeline shelter requires only tarp, cord, and two anchor points."
        );
    }

    @Test
    public void evidenceCardRowLabelDropsDuplicateAnchorLine() {
        DetailSourcePresentationFormatter formatter = reviewDemoFormatter();
        DetailSourcePresentationFormatter.EvidenceCard anchor = formatter.buildEvidenceCard(
            reviewedStackResult("GD-220", "Abrasives Manufacturing", "abrasives manufacturing"),
            0,
            "anchor"
        );

        String label = formatter.buildEvidenceCardRowLabel(anchor);
        String contentDescription = formatter.buildEvidenceCardRowContentDescription(anchor, true, 0, 3);

        assertContainsAll(
            label,
            "GD-220",
            "ANCHOR",
            "74%",
            "Abrasives Manufacturing",
            "Every melt starts with a foundry safety check, not with metal charge..."
        );
        assertFalse(label, label.contains("Abrasives Manufacturing\nAbrasives Manufacturing"));
        assertContainsAll(
            contentDescription,
            "Anchor guide",
            "1 of 3",
            "74% match",
            "GD-220",
            "Abrasives Manufacturing",
            "Every melt starts with a foundry safety check, not with metal charge...",
            "Shows source preview"
        );
    }

    @Test
    public void sourceSubtitlesKeepAnswerSourcesAheadOfTrustVocabulary() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);

        assertEquals(
            "Answer 3 sources",
            formatter.buildCompactSourcesSubtitle(3, false, false, "proof route verified")
        );
        assertEquals(
            "Answer 1 source",
            formatter.buildExpandedSourcesSubtitle(1, false, "provenance route verified")
        );
    }

    @Test
    public void stationSourceButtonLabelDoesNotRelabelClickTargetByIndex() {
        DetailSourcePresentationFormatter formatter = reviewDemoFormatter();
        SearchResult rainShelter = reviewedStackResult("GD-345", "Wood Quality Evaluation for Shelter Construction", "tarp cord rain shelter");

        assertEquals(
            "GD-345 \u00B7 TOPIC\nTarp & Cord Shelters",
            formatter.buildStationSourceButtonLabel(rainShelter, 0, 3, true)
        );
    }

    @Test
    public void orderAnswerSourceStackPlacesMockSourcesBeforeTopicPromotion() {
        DetailSourcePresentationFormatter formatter = reviewDemoFormatter();
        SearchResult rainShelter = reviewedStackResult("GD-345", "Wood Quality Evaluation for Shelter Construction", "tarp cord rain shelter");
        SearchResult unrelated = reviewedStackResult("GD-999", "Unrelated", "camp maintenance");
        SearchResult foundryRelated = reviewedStackResult("GD-132", "Foundry & Metal Casting", "foundry metal casting");
        SearchResult abrasiveAnchor = reviewedStackResult("GD-220", "Abrasives Manufacturing", "abrasives manufacturing");

        List<SearchResult> ordered = formatter.orderAnswerSourceStack(
            List.of(rainShelter, unrelated, foundryRelated, abrasiveAnchor)
        );

        assertEquals("GD-220", ordered.get(0).guideId);
        assertEquals("GD-132", ordered.get(1).guideId);
        assertEquals("GD-345", ordered.get(2).guideId);
        assertEquals("GD-999", ordered.get(3).guideId);
    }

    @Test
    public void orderAnswerSourceStackPreservesNonMockSourceOrder() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);
        SearchResult first = new SearchResult("Water Storage", "", "", "", "GD-214", "", "", "");
        SearchResult second = new SearchResult("Rainwater Catchment", "", "", "", "GD-215", "", "", "");

        List<SearchResult> ordered = formatter.orderAnswerSourceStack(List.of(first, second));

        assertEquals(List.of(first, second), ordered);
    }

    @Test
    public void sourceNavigationMergeUsesLoadedGuideButPreservesTappedSection() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);

        SearchResult merged = formatter.mergeGuideForSourceNavigation(
            new SearchResult(
                "Card title",
                "Card subtitle",
                "Card snippet",
                "Card body",
                "GD-214",
                "Tapped section",
                "card-category",
                "vector"
            ),
            new SearchResult(
                "Loaded title",
                "Loaded subtitle",
                "Loaded snippet",
                "Loaded body",
                "GD-214",
                "Loaded section",
                "water",
                "guide-focus",
                "source_anchor",
                "long",
                "howto",
                "storage"
            )
        );

        assertEquals("Loaded title", merged.title);
        assertEquals("Loaded subtitle", merged.subtitle);
        assertEquals("Loaded snippet", merged.snippet);
        assertEquals("Loaded body", merged.body);
        assertEquals("GD-214", merged.guideId);
        assertEquals("Tapped section", merged.sectionHeading);
        assertEquals("water", merged.category);
        assertEquals("guide-focus", merged.retrievalMode);
        assertEquals("source_anchor", merged.contentRole);
        assertEquals("long", merged.timeHorizon);
        assertEquals("howto", merged.structureType);
        assertEquals("storage", merged.topicTags);
    }

    private static SearchResult reviewedStackResult(String guideId, String title, String text) {
        return new SearchResult(
            text,
            "",
            "A simple ridgeline shelter requires only tarp, cord, and two anchor points.",
            "",
            guideId,
            title,
            "survival",
            "lexical",
            "",
            "",
            "emergency_shelter",
            "foundation,weatherproofing,site_selection"
        );
    }

    private static DetailSourcePresentationFormatter reviewDemoFormatter() {
        return new DetailSourcePresentationFormatter(
            null,
            ReviewDemoPolicy.isSourceStackDemoEnabled(true)
        );
    }

    private static void assertContainsAll(String actual, String... expectedParts) {
        for (String expectedPart : expectedParts) {
            assertTrue(actual, actual.contains(expectedPart));
        }
    }

    @Test
    public void materialChipLabelNamesIndexedMaterial() {
        assertEquals(
            "Material 01: Bark",
            DetailSourcePresentationFormatter.buildMaterialChipPlainLabel(0, " Bark ")
        );
        assertEquals(
            "Material 01",
            DetailSourcePresentationFormatter.buildMaterialChipPlainLabel(-4, " ")
        );
        assertTrue(
            DetailSourcePresentationFormatter.buildMaterialChipPlainLabel(0, "Bark").startsWith("Material 01:")
        );
    }

    @Test
    public void materialChipContentDescriptionIncludesActionAffordance() {
        assertEquals(
            "Material 01: Bark. Material chip. Tap to focus; long press to copy.",
            DetailSourcePresentationFormatter.buildMaterialChipContentDescriptionText(0, " Bark ", "materials in play")
        );
        assertEquals(
            "Material 02: materials in play. Material chip. Tap to focus; long press to copy.",
            DetailSourcePresentationFormatter.buildMaterialChipContentDescriptionText(1, " ", "materials in play")
        );
    }

    @Test
    public void inlineSourceChipLabelUsesAnchorGuideAndPlainSeparator() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);

        assertEquals(
            "GD-444 \u00B7 ANCHOR",
            formatter.buildInlineSourceChipLabel(
                new SearchResult("Emergency Shelter", "", "", "", "GD-444", "", "", "guide-focus"),
                "GD-444",
                true
            )
        );
        assertEquals(
            "GD-555 \u00B7 Fire Setup",
            formatter.buildInlineSourceChipLabel(
                new SearchResult("Campfire", "", "", "", "GD-555", "Fire Setup", "", "guide-focus"),
                "GD-444",
                false
            )
        );
    }

    @Test
    public void inlineSourceChipLabelCarriesEvidenceRoleAndMatchWhenKnown() {
        DetailSourcePresentationFormatter formatter = reviewDemoFormatter();

        assertEquals(
            "GD-220 \u00B7 ANCHOR",
            formatter.buildInlineSourceChipLabel(
                reviewedStackResult("GD-220", "Abrasives Manufacturing", "abrasives manufacturing"),
                "GD-220",
                true
            )
        );
        assertEquals(
            "GD-345 \u00B7 TOPIC",
            formatter.buildInlineSourceChipLabel(
                reviewedStackResult("GD-345", "Tarp & Cord Shelters", "rain shelter tarp cord"),
                "GD-220",
                false
            )
        );
    }

    @Test
    public void defaultFormatterDoesNotApplyRainShelterReviewedSourceStack() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);
        SearchResult abrasiveAnchor = reviewedStackResult("GD-220", "Live Abrasive Surface Prep", "abrasives manufacturing");
        SearchResult foundryRelated = reviewedStackResult("GD-132", "Live Casting Boundary", "foundry metal casting");
        SearchResult rainShelter = reviewedStackResult("GD-345", "Wood Quality Evaluation for Shelter Construction", "tarp cord rain shelter");

        DetailSourcePresentationFormatter.EvidenceCard anchor =
            formatter.buildEvidenceCard(abrasiveAnchor, 0, "anchor");
        DetailSourcePresentationFormatter.EvidenceCard related =
            formatter.buildEvidenceCard(foundryRelated, 1, "related");
        DetailSourcePresentationFormatter.EvidenceCard topic =
            formatter.buildEvidenceCard(rainShelter, 2, "related");

        assertEquals("ANCHOR", anchor.roleLabel);
        assertEquals("68%", anchor.matchLabel);
        assertEquals("abrasives manufacturing", anchor.title);
        assertEquals("A simple ridgeline shelter requires only tarp, cord, and two anchor points.", anchor.quote);
        assertEquals("RELATED", related.roleLabel);
        assertEquals("62%", related.matchLabel);
        assertEquals("foundry metal casting", related.title);
        assertEquals("A simple ridgeline shelter requires only tarp, cord, and two anchor points.", related.quote);
        assertEquals("TOPIC", topic.roleLabel);
        assertEquals("56%", topic.matchLabel);
        assertEquals("tarp cord rain shelter", topic.title);
        assertEquals("A simple ridgeline shelter requires only tarp, cord, and two anchor points.", topic.quote);
    }

    @Test
    public void defaultFormatterKeepsRainShelterSourceStackOrderAndGenericLabels() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);
        SearchResult rainShelter = reviewedStackResult("GD-345", "Wood Quality Evaluation for Shelter Construction", "tarp cord rain shelter");
        SearchResult foundryRelated = reviewedStackResult("GD-132", "Foundry & Metal Casting", "foundry metal casting");
        SearchResult abrasiveAnchor = reviewedStackResult("GD-220", "Abrasives Manufacturing", "abrasives manufacturing");

        List<SearchResult> ordered = formatter.orderAnswerSourceStack(
            List.of(rainShelter, foundryRelated, abrasiveAnchor)
        );

        assertEquals(List.of(rainShelter, foundryRelated, abrasiveAnchor), ordered);
        assertEquals(
            "GD-345 \u00B7 ANCHOR \u00B7 tarp cord rain shelter",
            formatter.buildStationSourceButtonLabel(rainShelter, 0, 3, true)
        );
        assertEquals(
            "GD-220 \u00B7 TOPIC \u00B7 abrasives manufacturing",
            formatter.buildStationSourceButtonLabel(abrasiveAnchor, 2, 3, false)
        );
    }
}
