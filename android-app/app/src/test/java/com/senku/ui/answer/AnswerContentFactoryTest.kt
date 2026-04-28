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
    fun fromRenderedAnswer_splitsUncertainSupportAndTryBlocksOutOfPrimaryAnswer() {
        val content = fromRenderedAnswer(
            body = """
                Senku found guides that may be relevant to "rain shelter", but this is not a confident fit.

                Possibly relevant guides in the library:
                - [GD-345] Tarp & Cord Shelters - shelter | title match
                - [GD-294] Cave Shelter Systems - shelter | related context

                Try:
                - checking whether the guide matches the exact material or setting
                - asking a narrower follow-up with the exact detail that is missing
            """.trimIndent(),
            sourceCount = 2,
            host = "On-device",
            elapsedSeconds = 0.8,
            evidence = Evidence.None,
            abstain = false,
            uncertainFit = true,
        )

        assertEquals(
            "Senku found guides that may be relevant to \"rain shelter\", but this is not a confident fit.",
            content.short
        )
        assertEquals(
            listOf(
                "checking whether the guide matches the exact material or setting",
                "asking a narrower follow-up with the exact detail that is missing",
            ),
            content.steps
        )
        assertEquals(null, content.limits)
    }

    @Test
    fun fromRenderedAnswer_parsesDisplaySectionLabelsOutOfPrimaryAnswer() {
        val content = fromRenderedAnswer(
            body = """
                ANSWER
                Use the attached source guide [GD-345] to build a simple rain shelter.

                FIELD STEPS
                1. Check the cited guide before moving. [GD-345]

                WATCH
                Treat this as guidance, not procedure.
            """.trimIndent(),
            sourceCount = 2,
            host = "This device",
            elapsedSeconds = 0.0,
            evidence = Evidence.Moderate,
            abstain = false,
        )

        assertEquals(
            "Use the attached source guide [GD-345] to build a simple rain shelter.",
            content.short,
        )
        assertEquals(null, content.steps)
        assertEquals("Treat this as guidance, not procedure.", content.limits)
    }

    @Test
    fun fromRenderedAnswer_keepsSubstantiveSteps() {
        val content = fromRenderedAnswer(
            body = """
                ANSWER
                Build a low ridgeline first.

                FIELD STEPS
                1. Face the low tarp edge windward.
            """.trimIndent(),
            sourceCount = 3,
            host = "This device",
            elapsedSeconds = 0.0,
            evidence = Evidence.Moderate,
            abstain = false,
        )

        assertEquals(listOf("Face the low tarp edge windward."), content.steps)
    }

    @Test
    fun fromRenderedAnswer_parsesRainShelterEngineFallbackForArticleCard() {
        val content = fromRenderedAnswer(
            body = """
                ANSWER
                Build a ridgeline first, then drape and tension the tarp around it. Keep the low edge toward the weather and leave runoff a clear path away from the sheltered area.

                FIELD STEPS
                1. Tie a taut ridgeline between two solid anchor points.
                2. Drape the tarp over the line and stake or tie the windward edge low.
                3. Tension the corners evenly, then adjust the pitch so rain sheds instead of pooling.
            """.trimIndent(),
            sourceCount = 3,
            host = "Host",
            elapsedSeconds = 0.8,
            evidence = Evidence.Moderate,
            abstain = false,
            uncertainFit = true,
            answerSurfaceLabel = AnswerSurfaceLabel.LimitedFit,
        )

        assertEquals(
            "Build a ridgeline first, then drape and tension the tarp around it. A ridgeline is a single taut cord run between two anchor points (trees, poles, or bombproof rocks) at roughly chest height; it carries the load while the tarp only sheds water.\n\n" +
                "Pitch the tarp ridge along the prevailing wind, with the low edge facing windward. Tension corners with prusik or taut-line hitches so the rig stays adjustable through the night.",
            content.short,
        )
        assertEquals(3, content.sourceCount)
        assertEquals(null, content.steps)
    }

    @Test
    fun fromRenderedAnswer_keepsUncertainEscalationAsLimits() {
        val content = fromRenderedAnswer(
            body = """
                Senku found guides that may be relevant to "pacing", but this is not a confident fit.

                If this could involve immediate danger, medical symptoms, violence, or inability to stay safe, contact local emergency services now.

                Possibly relevant guides in the library:
                - [GD-305] Barely Slept Pacing Notes - mental-health | related context

                Try:
                - treating the guides above as related context, not a final answer
            """.trimIndent(),
            sourceCount = 1,
            host = "On-device",
            elapsedSeconds = 0.8,
            evidence = Evidence.None,
            abstain = false,
            uncertainFit = true,
        )

        assertEquals(
            "Senku found guides that may be relevant to \"pacing\", but this is not a confident fit.",
            content.short
        )
        assertEquals(
            "If this could involve immediate danger, medical symptoms, violence, or inability to stay safe, contact local emergency services now.",
            content.limits
        )
        assertEquals(listOf("treating the guides above as related context, not a final answer"), content.steps)
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
