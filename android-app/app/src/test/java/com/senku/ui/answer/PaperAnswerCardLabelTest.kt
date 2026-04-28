package com.senku.ui.answer

import org.junit.Assert.assertEquals
import org.junit.Test

class PaperAnswerCardLabelTest {
    @Test
    fun compactEvidenceLabel_usesFieldManualSourceLanguage() {
        assertEquals(
            "RULE MATCH",
            compactEvidenceLabel(answer(answerSurfaceLabel = AnswerSurfaceLabel.DeterministicRule)),
        )
        assertEquals(
            "SOURCE MATCH",
            compactEvidenceLabel(answer(answerSurfaceLabel = AnswerSurfaceLabel.ReviewedCardEvidence)),
        )
        assertEquals(
            "UNSURE FIT",
            compactEvidenceLabel(answer(answerSurfaceLabel = AnswerSurfaceLabel.LimitedFit)),
        )
        assertEquals(
            "STRONG SOURCES",
            compactEvidenceLabel(answer(evidence = Evidence.Strong)),
        )
        assertEquals(
            "SOURCE MATCH",
            compactEvidenceLabel(answer(evidence = Evidence.Moderate)),
        )
        assertEquals(
            "LIMITED SOURCES",
            compactEvidenceLabel(answer(evidence = Evidence.None)),
        )
    }

    @Test
    fun compactEvidenceLabel_hidesReviewedCardBoundaryLanguage() {
        assertEquals(
            "STRONG SOURCES",
            compactEvidenceLabel(
                answer(
                    evidence = Evidence.Strong,
                    answerSurfaceLabel = AnswerSurfaceLabel.ReviewedCardEvidence,
                ),
            ),
        )
        assertEquals(
            "LIMITED SOURCES",
            compactEvidenceLabel(
                answer(
                    evidence = Evidence.None,
                    answerSurfaceLabel = AnswerSurfaceLabel.ReviewedCardEvidence,
                ),
            ),
        )
    }

    @Test
    fun buildFooterMeta_usesCompactSourcePreview() {
        val content = answer(
            sourceCount = 2,
            host = "On-device",
            elapsedSeconds = 0.8,
        )

        assertEquals("Sources - 2 · 0.8S", buildFooterMeta(content))
    }

    @Test
    fun uncertainFitNotice_usesArticleSourceLanguage() {
        val content = answer(
            sourceCount = 3,
            answerSurfaceLabel = AnswerSurfaceLabel.LimitedFit,
        )

        assertEquals(true, shouldShowUncertainFitNotice(content))
        assertEquals(
            "Senku found 3 guides that may apply but no single guide is a confident anchor. Treat this as guidance, not procedure. See sources below.",
            uncertainFitNoticeText(content),
        )
    }

    @Test
    fun uncertainFitNotice_isSecondaryWhenArticleHasProse() {
        assertEquals(
            false,
            shouldEmphasizeUncertainFitNotice(
                answer(
                    answerSurfaceLabel = AnswerSurfaceLabel.LimitedFit,
                    sourceCount = 3,
                ),
            ),
        )
        assertEquals(
            true,
            shouldEmphasizeUncertainFitNotice(
                AnswerContent(
                    short = "",
                    sourceCount = 0,
                    host = "",
                    elapsedSeconds = 0.0,
                    answerSurfaceLabel = AnswerSurfaceLabel.LimitedFit,
                ),
            ),
        )
    }

    @Test
    fun displayProofCtaLabel_replacesLegacyProofCopyOnly() {
        assertEquals("View sources", displayProofCtaLabel("Show proof"))
        assertEquals("View citations", displayProofCtaLabel("View citations"))
    }

    private fun answer(
        evidence: Evidence = Evidence.Moderate,
        sourceCount: Int = 1,
        host: String = "",
        elapsedSeconds: Double = 0.0,
        answerSurfaceLabel: AnswerSurfaceLabel = AnswerSurfaceLabel.Unknown,
    ): AnswerContent =
        AnswerContent(
            short = "Use the relevant field guidance.",
            evidence = evidence,
            sourceCount = sourceCount,
            host = host,
            elapsedSeconds = elapsedSeconds,
            answerSurfaceLabel = answerSurfaceLabel,
        )
}
