package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
            "GD-220 \u00B7 ANCHOR \u00B7 Abrasives Manufacturing",
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
            "GD-132 \u00B7 RELATED \u00B7 Foundry & Metal Casting",
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
            "GD-345 \u00B7 TOPIC \u00B7 Tarp & Cord Shelters",
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
            "GD-220 \u00B7 ANCHOR \u00B7 Abrasives Manufacturing",
            formatter.buildStationSourceButtonLabel(abrasiveAnchor, 0, 3, true)
        );
        assertEquals(
            "GD-132 \u00B7 RELATED \u00B7 Foundry & Metal Casting",
            formatter.buildStationSourceButtonLabel(foundryRelated, 1, 3, false)
        );
        assertEquals(
            "GD-345 \u00B7 TOPIC \u00B7 Tarp & Cord Shelters",
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
            "Use the abrasives guide as the reviewed anchor context for line tension and abrasion-safe cord handling.",
            anchor.quote
        );
        assertEquals("RELATED", related.roleLabel);
        assertEquals("68%", related.matchLabel);
        assertEquals("Foundry & Metal Casting", related.title);
        assertEquals(
            "Keep related guide support visible for burn hazards, dry work surfaces, and tool staging near shelter work.",
            related.quote
        );
        assertEquals("TOPIC", topic.roleLabel);
        assertEquals("61%", topic.matchLabel);
        assertEquals("Tarp & Cord Shelters", topic.title);
        assertEquals("A simple ridgeline shelter requires only tarp, cord, and two anchor points.", topic.quote);
    }

    @Test
    public void stationSourceButtonLabelDoesNotRelabelClickTargetByIndex() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);
        SearchResult rainShelter = reviewedStackResult("GD-345", "Wood Quality Evaluation for Shelter Construction", "tarp cord rain shelter");

        assertEquals(
            "GD-345 \u00B7 TOPIC \u00B7 Tarp & Cord Shelters",
            formatter.buildStationSourceButtonLabel(rainShelter, 0, 3, true)
        );
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
            "GD-444 anchor guide",
            formatter.buildInlineSourceChipLabel(
                new SearchResult("Emergency Shelter", "", "", "", "GD-444", "", "", "guide-focus"),
                "GD-444",
                true
            )
        );
        assertEquals(
            "GD-555 - Fire Setup",
            formatter.buildInlineSourceChipLabel(
                new SearchResult("Campfire", "", "", "", "GD-555", "Fire Setup", "", "guide-focus"),
                "GD-444",
                false
            )
        );
    }
}
