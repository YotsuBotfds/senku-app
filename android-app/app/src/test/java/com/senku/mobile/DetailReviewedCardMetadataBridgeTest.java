package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public final class DetailReviewedCardMetadataBridgeTest {
    @Test
    public void forRuleIdReturnsCurrentMetadataOnlyForMatchingNonEmptyRule() {
        DetailReviewedCardMetadataBridge bridge = new DetailReviewedCardMetadataBridge();
        ReviewedCardMetadata metadata = new ReviewedCardMetadata(
            "poisoning_unknown_ingestion",
            "GD-898",
            "pilot_reviewed",
            "reviewed_source_family",
            ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
            List.of("GD-898")
        );

        bridge.set(metadata);

        ReviewedCardMetadata matched = bridge.forRuleId(
            " answer_card:poisoning_unknown_ingestion ",
            "answer_card:poisoning_unknown_ingestion"
        );
        assertTrue(matched.isPresent());
        assertEquals("poisoning_unknown_ingestion", matched.cardId);

        assertFalse(bridge.forRuleId("", "answer_card:poisoning_unknown_ingestion").isPresent());
        assertFalse(bridge.forRuleId("answer_card:other", "answer_card:poisoning_unknown_ingestion").isPresent());
    }

    @Test
    public void resetClearsCurrentMetadata() {
        DetailReviewedCardMetadataBridge bridge = new DetailReviewedCardMetadataBridge();
        bridge.set(new ReviewedCardMetadata(
            "poisoning_unknown_ingestion",
            "GD-898",
            "pilot_reviewed",
            "",
            ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
            List.of("GD-898")
        ));

        bridge.reset();

        assertFalse(bridge.current().isPresent());
    }
}
