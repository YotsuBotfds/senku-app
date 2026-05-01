package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.json.JSONObject;
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
    public void answerCardRuleHelpersCentralizePrefixChecks() {
        assertEquals(
            "answer_card:poisoning_unknown_ingestion",
            ReviewedCardMetadata.answerCardRuleId(" poisoning_unknown_ingestion ")
        );
        assertEquals("", ReviewedCardMetadata.answerCardRuleId(" "));
        assertTrue(ReviewedCardMetadata.isAnswerCardRuleId(" answer_card:poisoning_unknown_ingestion "));
        assertFalse(ReviewedCardMetadata.isAnswerCardRuleId("deterministic:poisoning_unknown_ingestion"));
        assertFalse(ReviewedCardMetadata.isAnswerCardRuleId("ANSWER_CARD:poisoning_unknown_ingestion"));
    }

    @Test
    public void reviewedRuntimePredicatesRequireRuntimeProvenanceAndCitedSources() {
        ReviewedCardMetadata runtimeMetadata = new ReviewedCardMetadata(
            "poisoning_unknown_ingestion",
            "GD-898",
            "pilot_reviewed",
            "reviewed_source_family",
            ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
            List.of("GD-898")
        );
        ReviewedCardMetadata generatedMetadata = new ReviewedCardMetadata(
            "poisoning_unknown_ingestion",
            "GD-898",
            "pilot_reviewed",
            "reviewed_source_family",
            "generated_model",
            List.of("GD-898")
        );
        ReviewedCardMetadata uncitedMetadata = new ReviewedCardMetadata(
            "poisoning_unknown_ingestion",
            "GD-898",
            "pilot_reviewed",
            "reviewed_source_family",
            ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
            List.of()
        );

        assertTrue(ReviewedCardMetadata.isReviewedRuntimeStatus(
            " pilot_reviewed ",
            " reviewed_card_runtime "
        ));
        assertTrue(ReviewedCardMetadata.isReviewedRuntimeStatus("reviewed", "reviewed_card_runtime"));
        assertFalse(ReviewedCardMetadata.isReviewedRuntimeStatus("approved", "reviewed_card_runtime"));
        assertTrue(runtimeMetadata.isReviewedRuntimeCardWithCitedSources());
        assertTrue(ReviewedCardMetadata.isReviewedRuntimeCardWithCitedSources(
            "answer_card:poisoning_unknown_ingestion",
            runtimeMetadata
        ));
        assertFalse(generatedMetadata.isReviewedRuntimeCardWithCitedSources());
        assertFalse(uncitedMetadata.isReviewedRuntimeCardWithCitedSources());
        assertFalse(ReviewedCardMetadata.isReviewedRuntimeCardWithCitedSources(
            "deterministic:poisoning_unknown_ingestion",
            runtimeMetadata
        ));
    }

    @Test
    public void emptyAndNullInputsStayAbsent() {
        assertFalse(ReviewedCardMetadata.empty().isPresent());
        assertFalse(ReviewedCardMetadata.fromAnswerCard(null).isPresent());
        assertFalse(new ReviewedCardMetadata(null, " ", "", null, "", null).isPresent());
        assertEquals("", ReviewedCardMetadata.empty().citedSourceGuideIdsCsv());
        assertFalse(ReviewedCardMetadata.empty().isReviewedRuntimeCardWithCitedSources());
    }

    @Test
    public void jsonRoundTripUsesReviewedCardMetadataSchema() throws Exception {
        ReviewedCardMetadata metadata = new ReviewedCardMetadata(
            " poisoning_unknown_ingestion ",
            " GD-898 ",
            " pilot_reviewed ",
            " reviewed_source_family ",
            ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
            List.of(" GD-898 ", "GD-898", "GD-284")
        );

        JSONObject object = ReviewedCardMetadata.toJsonObject(metadata);

        assertEquals("poisoning_unknown_ingestion", object.getString("card_id"));
        assertEquals("GD-898", object.getString("card_guide_id"));
        assertEquals("pilot_reviewed", object.getString("review_status"));
        assertEquals("reviewed_source_family", object.getString("runtime_citation_policy"));
        assertEquals(
            ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
            object.getString("provenance")
        );
        assertEquals("GD-898", object.getJSONArray("cited_reviewed_source_guide_ids").getString(0));
        assertEquals("GD-284", object.getJSONArray("cited_reviewed_source_guide_ids").getString(1));

        ReviewedCardMetadata restored = ReviewedCardMetadata.fromJsonObject(object);

        assertEquals("poisoning_unknown_ingestion", restored.cardId);
        assertEquals("GD-898", restored.cardGuideId);
        assertEquals("pilot_reviewed", restored.reviewStatus);
        assertEquals("reviewed_source_family", restored.runtimeCitationPolicy);
        assertEquals(ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME, restored.provenance);
        assertEquals(List.of("GD-898", "GD-284"), restored.citedReviewedSourceGuideIds);
    }
}
