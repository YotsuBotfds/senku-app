package com.senku.ui.answer

import com.senku.mobile.OfflineAnswerEngine
import com.senku.mobile.ReviewedCardMetadata
import org.junit.Assert.assertEquals
import org.junit.Test

class AnswerContentFactoryTest {
    @Test
    fun evidenceForAnswerState_doesNotTreatLowConfidenceAsStrong() {
        val evidence = evidenceForAnswerState(
            confidenceLabel = OfflineAnswerEngine.ConfidenceLabel.LOW,
            mode = OfflineAnswerEngine.AnswerMode.CONFIDENT,
            abstain = false,
            sourceCount = 4,
        )

        assertEquals(Evidence.None, evidence)
    }

    @Test
    fun evidenceForAnswerState_keepsMediumConfidenceModerate() {
        val evidence = evidenceForAnswerState(
            confidenceLabel = OfflineAnswerEngine.ConfidenceLabel.MEDIUM,
            mode = OfflineAnswerEngine.AnswerMode.CONFIDENT,
            abstain = false,
            sourceCount = 4,
        )

        assertEquals(Evidence.Moderate, evidence)
    }

    @Test
    fun evidenceForAnswerState_marksUncertainFitAsLimited() {
        val evidence = evidenceForAnswerState(
            confidenceLabel = OfflineAnswerEngine.ConfidenceLabel.HIGH,
            mode = OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT,
            abstain = false,
            sourceCount = 4,
        )

        assertEquals(Evidence.None, evidence)
    }

    @Test
    fun inferAnswerSurface_marksAbstainShape() {
        val surface = inferAnswerSurface(
            mode = OfflineAnswerEngine.AnswerMode.CONFIDENT,
            abstain = true,
            deterministic = true,
            sourceCount = 4,
        )

        assertEquals(AnswerSurfaceLabel.Abstain, surface.answerSurfaceLabel)
        assertEquals("abstain_card", surface.answerProvenance)
    }

    @Test
    fun inferAnswerSurface_marksUncertainFitShape() {
        val surface = inferAnswerSurface(
            mode = OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT,
            abstain = false,
            deterministic = true,
            sourceCount = 4,
        )

        assertEquals(AnswerSurfaceLabel.LimitedFit, surface.answerSurfaceLabel)
        assertEquals("uncertain_fit_card", surface.answerProvenance)
    }

    @Test
    fun inferAnswerSurface_marksDeterministicRuleShape() {
        val surface = inferAnswerSurface(
            mode = OfflineAnswerEngine.AnswerMode.CONFIDENT,
            abstain = false,
            deterministic = true,
            sourceCount = 0,
        )

        assertEquals(AnswerSurfaceLabel.DeterministicRule, surface.answerSurfaceLabel)
        assertEquals("deterministic_rule", surface.answerProvenance)
    }

    @Test
    fun inferAnswerSurface_marksAnswerCardRuleAsReviewedEvidence() {
        val surface = inferAnswerSurface(
            mode = OfflineAnswerEngine.AnswerMode.CONFIDENT,
            abstain = false,
            deterministic = true,
            sourceCount = 1,
            ruleId = "answer_card:poisoning_unknown_ingestion",
            reviewedCardMetadata = reviewedCardMetadata(),
        )

        assertEquals(AnswerSurfaceLabel.ReviewedCardEvidence, surface.answerSurfaceLabel)
        assertEquals("reviewed_card_runtime", surface.answerProvenance)
    }

    @Test
    fun fromRenderedAnswer_preservesReviewedCardMetadata() {
        val metadata = ReviewedCardMetadata(
            "poisoning_unknown_ingestion",
            "GD-898",
            "pilot_reviewed",
            "reviewed_source_family",
            ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
            listOf("GD-898"),
        )

        val content = fromRenderedAnswer(
            body = "1. Call poison control.",
            sourceCount = 1,
            host = "Instant",
            elapsedSeconds = 0.0,
            evidence = Evidence.Strong,
            abstain = false,
            answerSurfaceLabel = AnswerSurfaceLabel.ReviewedCardEvidence,
            answerProvenance = ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
            reviewedCardBacked = true,
            reviewedCardMetadata = metadata,
        )

        assertEquals("poisoning_unknown_ingestion", content.reviewedCardMetadata.cardId)
        assertEquals("GD-898", content.reviewedCardMetadata.cardGuideId)
        assertEquals("pilot_reviewed", content.reviewedCardMetadata.reviewStatus)
        assertEquals(listOf("GD-898"), content.reviewedCardMetadata.citedReviewedSourceGuideIds)
    }

    @Test
    fun inferAnswerSurface_doesNotMarkAnswerCardRuleWithoutSourcesAsReviewedEvidence() {
        val surface = inferAnswerSurface(
            mode = OfflineAnswerEngine.AnswerMode.CONFIDENT,
            abstain = false,
            deterministic = true,
            sourceCount = 0,
            ruleId = "answer_card:poisoning_unknown_ingestion",
            reviewedCardMetadata = reviewedCardMetadata(),
        )

        assertEquals(AnswerSurfaceLabel.DeterministicRule, surface.answerSurfaceLabel)
        assertEquals("deterministic_rule", surface.answerProvenance)
    }

    @Test
    fun inferAnswerSurface_doesNotMarkAnswerCardRuleWithoutMetadataAsReviewedEvidence() {
        val surface = inferAnswerSurface(
            mode = OfflineAnswerEngine.AnswerMode.CONFIDENT,
            abstain = false,
            deterministic = true,
            sourceCount = 1,
            ruleId = "answer_card:poisoning_unknown_ingestion",
        )

        assertEquals(AnswerSurfaceLabel.DeterministicRule, surface.answerSurfaceLabel)
        assertEquals("deterministic_rule", surface.answerProvenance)
    }

    @Test
    fun inferAnswerSurface_doesNotMarkAnswerCardRuleWithWrongProvenanceAsReviewedEvidence() {
        val surface = inferAnswerSurface(
            mode = OfflineAnswerEngine.AnswerMode.CONFIDENT,
            abstain = false,
            deterministic = true,
            sourceCount = 1,
            ruleId = "answer_card:poisoning_unknown_ingestion",
            reviewedCardMetadata = reviewedCardMetadata(provenance = "generated_model"),
        )

        assertEquals(AnswerSurfaceLabel.DeterministicRule, surface.answerSurfaceLabel)
        assertEquals("deterministic_rule", surface.answerProvenance)
    }

    @Test
    fun inferAnswerSurface_doesNotMarkAnswerCardRuleWithoutCitedSourcesAsReviewedEvidence() {
        val surface = inferAnswerSurface(
            mode = OfflineAnswerEngine.AnswerMode.CONFIDENT,
            abstain = false,
            deterministic = true,
            sourceCount = 1,
            ruleId = "answer_card:poisoning_unknown_ingestion",
            reviewedCardMetadata = reviewedCardMetadata(citedGuideIds = emptyList()),
        )

        assertEquals(AnswerSurfaceLabel.DeterministicRule, surface.answerSurfaceLabel)
        assertEquals("deterministic_rule", surface.answerProvenance)
    }

    @Test
    fun inferAnswerSurface_doesNotMarkGeneratedAnswerCardRuleAsReviewedEvidence() {
        val surface = inferAnswerSurface(
            mode = OfflineAnswerEngine.AnswerMode.CONFIDENT,
            abstain = false,
            deterministic = false,
            sourceCount = 1,
            ruleId = "answer_card:poisoning_unknown_ingestion",
            reviewedCardMetadata = reviewedCardMetadata(),
        )

        assertEquals(AnswerSurfaceLabel.GeneratedEvidence, surface.answerSurfaceLabel)
        assertEquals("generated_model", surface.answerProvenance)
    }

    @Test
    fun inferAnswerSurface_marksGeneratedEvidenceOnlyWhenConfidentWithSources() {
        val surface = inferAnswerSurface(
            mode = OfflineAnswerEngine.AnswerMode.CONFIDENT,
            abstain = false,
            deterministic = false,
            sourceCount = 2,
        )

        assertEquals(AnswerSurfaceLabel.GeneratedEvidence, surface.answerSurfaceLabel)
        assertEquals("generated_model", surface.answerProvenance)
    }

    @Test
    fun inferAnswerSurface_marksOtherShapesUnknown() {
        val surface = inferAnswerSurface(
            mode = OfflineAnswerEngine.AnswerMode.CONFIDENT,
            abstain = false,
            deterministic = false,
            sourceCount = 0,
        )

        assertEquals(AnswerSurfaceLabel.Unknown, surface.answerSurfaceLabel)
        assertEquals("", surface.answerProvenance)
    }

    private fun reviewedCardMetadata(
        provenance: String = ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
        citedGuideIds: List<String> = listOf("GD-898"),
    ): ReviewedCardMetadata =
        ReviewedCardMetadata(
            "poisoning_unknown_ingestion",
            "GD-898",
            "pilot_reviewed",
            "reviewed_source_family",
            provenance,
            citedGuideIds,
        )
}
