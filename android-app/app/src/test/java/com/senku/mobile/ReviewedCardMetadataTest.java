package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public final class ReviewedCardMetadataTest {
    @Test
    public void constructorNormalizesFieldsAndCitedSourceGuideIds() {
        ReviewedCardMetadata metadata = new ReviewedCardMetadata(
            " poisoning_unknown_ingestion ",
            " GD-898 ",
            " pilot_reviewed ",
            " reviewed_source_family ",
            " reviewed_card_runtime ",
            List.of(" GD-898 ", "", "GD-898", " GD-284 ")
        );

        assertTrue(metadata.isPresent());
        assertEquals("poisoning_unknown_ingestion", metadata.cardId);
        assertEquals("GD-898", metadata.cardGuideId);
        assertEquals("pilot_reviewed", metadata.reviewStatus);
        assertEquals("reviewed_source_family", metadata.runtimeCitationPolicy);
        assertEquals(ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME, metadata.provenance);
        assertEquals(List.of("GD-898", "GD-284"), metadata.citedReviewedSourceGuideIds);
        assertEquals("GD-898, GD-284", metadata.citedSourceGuideIdsCsv());
        assertThrows(UnsupportedOperationException.class, () ->
            metadata.citedReviewedSourceGuideIds.add("GD-999")
        );
    }

    @Test
    public void fromAnswerCardDerivesRuntimeMetadataAndSkipsBlankSources() {
        AnswerCard card = new AnswerCard(
            "poisoning_unknown_ingestion",
            "GD-898",
            "poisoning-unknown-ingestion",
            "Poisoning: Unknown Ingestion",
            "critical",
            "reviewed-card",
            "pilot_reviewed",
            "reviewed_source_family",
            "",
            "",
            "",
            List.of(),
            List.of(
                new AnswerCardSource("poisoning_unknown_ingestion", " GD-898 ", "", "", List.of(), true),
                new AnswerCardSource("poisoning_unknown_ingestion", " ", "", "", List.of(), false),
                new AnswerCardSource("poisoning_unknown_ingestion", "GD-898", "", "", List.of(), false),
                new AnswerCardSource("poisoning_unknown_ingestion", "GD-284", "", "", List.of(), false)
            )
        );

        ReviewedCardMetadata metadata = ReviewedCardMetadata.fromAnswerCard(card);

        assertEquals("poisoning_unknown_ingestion", metadata.cardId);
        assertEquals("GD-898", metadata.cardGuideId);
        assertEquals("pilot_reviewed", metadata.reviewStatus);
        assertEquals("reviewed_source_family", metadata.runtimeCitationPolicy);
        assertEquals(ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME, metadata.provenance);
        assertEquals(List.of("GD-898", "GD-284"), metadata.citedReviewedSourceGuideIds);
    }

    @Test
    public void emptyAndNullInputsStayAbsent() {
        assertFalse(ReviewedCardMetadata.empty().isPresent());
        assertFalse(ReviewedCardMetadata.fromAnswerCard(null).isPresent());
        assertFalse(new ReviewedCardMetadata(null, " ", "", null, "", null).isPresent());
        assertEquals("", ReviewedCardMetadata.empty().citedSourceGuideIdsCsv());
    }
}
