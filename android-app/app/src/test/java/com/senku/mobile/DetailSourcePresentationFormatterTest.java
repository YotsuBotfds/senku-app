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
        assertEquals("93%", card.matchLabel);
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
        assertEquals("RELATED", card.roleLabel);
        assertEquals("66%", card.matchLabel);
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
        assertEquals("87%", card.matchLabel);
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
            "GD-444 - Source preview (2 guides)",
            formatter.buildCompactInlineSourceTriggerLabel(
                new SearchResult("Emergency Shelter", "", "", "", "GD-444", "", "", "guide-focus"),
                2
            )
        );
        assertEquals(
            "Source preview (0 guides)",
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
