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

        assertEquals(
            "Anchor guide: GD-444, Ridge Line Setup. Shows source preview.",
            formatter.buildInlineSourceChipContentDescription(source, true, true, -4, 0)
        );
        assertEquals(
            "Source guide 2 of 3: GD-444, Ridge Line Setup. Opens source guide.",
            formatter.buildInlineSourceChipContentDescription(source, false, false, 1, 3)
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

        assertEquals(
            "Anchor guide 1 of 2: [GD-444] Emergency Shelter Ridge Line Setup. Shows source preview.",
            formatter.buildSourceButtonContentDescription(source, true, 0, 2, true)
        );
        assertEquals(
            "Related guide 2 of 2: [GD-444] Emergency Shelter Ridge Line Setup. Shows source preview.",
            formatter.buildSourceButtonContentDescription(source, true, 1, 2, false)
        );
        assertEquals(
            "Source guide 2 of 2: [GD-444] Emergency Shelter Ridge Line Setup. Opens source guide.",
            formatter.buildSourceButtonContentDescription(source, false, 1, 2, false)
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

        assertEquals(
            "[GD-444] Emergency Shelter\nRidge Line Setup",
            formatter.buildEvidenceCardRowLabel(card)
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

        assertEquals(
            "Anchor guide 1 of 2, 78% match: [GD-444] Emergency Shelter Ridge Line Setup. Shows source preview.",
            formatter.buildEvidenceCardRowContentDescription(anchorCard, true, 0, 2)
        );
        assertEquals(
            "Related guide 3 of 3, 56% match: [GD-215] Rainwater Catchment Storage Prep. Opens source guide.",
            formatter.buildEvidenceCardRowContentDescription(relatedCard, false, 2, 3)
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

        assertEquals(
            "SOURCE 2/4 [GD-898] Reviewed source\nPoisoning unknown ingestion",
            formatter.buildStationEvidenceCardRowLabel(card, 1, 4)
        );
    }

    @Test
    public void stationSourceButtonLabelUsesAnswerSourceStackContract() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);

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
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);
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
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);
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
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);
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
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);
        DetailSourcePresentationFormatter.EvidenceCard topic = formatter.buildEvidenceCard(
            reviewedStackResult("GD-345", "Wood Quality Evaluation for Shelter Construction", "tarp cord rain shelter"),
            0,
            "anchor"
        );

        assertEquals(
            "TOPIC 1/3 [GD-345] Tarp & Cord Shelters",
            formatter.buildStationEvidenceCardRowLabel(topic, 0, 3)
        );
        assertEquals(
            "[GD-345] Tarp & Cord Shelters",
            formatter.buildEvidenceCardRowLabel(topic)
        );
    }

    @Test
    public void evidenceCardRowLabelDropsDuplicateAnchorLine() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);
        DetailSourcePresentationFormatter.EvidenceCard anchor = formatter.buildEvidenceCard(
            reviewedStackResult("GD-220", "Abrasives Manufacturing", "abrasives manufacturing"),
            0,
            "anchor"
        );

        assertEquals(
            "[GD-220] Abrasives Manufacturing",
            formatter.buildEvidenceCardRowLabel(anchor)
        );
        assertEquals(
            "Anchor guide 1 of 3, 74% match: [GD-220] Abrasives Manufacturing. Shows source preview.",
            formatter.buildEvidenceCardRowContentDescription(anchor, true, 0, 3)
        );
    }

    @Test
    public void stationSourceButtonLabelDoesNotRelabelClickTargetByIndex() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);
        SearchResult rainShelter = reviewedStackResult("GD-345", "Wood Quality Evaluation for Shelter Construction", "tarp cord rain shelter");

        assertEquals(
            "GD-345 \u00B7 TOPIC\nTarp & Cord Shelters",
            formatter.buildStationSourceButtonLabel(rainShelter, 0, 3, true)
        );
    }

    @Test
    public void orderAnswerSourceStackPlacesMockSourcesBeforeTopicPromotion() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);
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
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);

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
}
