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
            "UNSURE",
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
        assertEquals(
            "UNSURE",
            compactEvidenceLabel(
                AnswerContent(
                    short = "Related guidance only.",
                    sourceCount = 2,
                    host = "",
                    elapsedSeconds = 0.0,
                    uncertainFit = true,
                ),
            ),
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

        assertEquals("2 SOURCES · 0.8S", buildFooterMeta(content))
    }

    @Test
    fun uncertainFitNotice_usesArticleSourceLanguage() {
        val content = answer(
            sourceCount = 3,
            answerSurfaceLabel = AnswerSurfaceLabel.LimitedFit,
        )

        assertEquals(true, shouldShowUncertainFitNotice(content))
        assertEquals(
            "UNSURE",
            uncertainFitNoticeLabel(content),
        )
        assertEquals(
            "3 related guides found; no single confident anchor. Treat as guidance.",
            uncertainFitNoticeText(content),
        )
    }

    @Test
    fun answerCardTypography_usesDenseLandscapeFriendlyRhythm() {
        assertEquals(6f, PaperAnswerCardSectionSpacing.value)
        assertEquals(14f, PaperAnswerCardBodySize.value)
        assertEquals(20f, PaperAnswerCardBodyLineHeight.value)
        assertEquals(12f, PaperAnswerCardSupportSize.value)
        assertEquals(15f, PaperAnswerCardSupportLineHeight.value)
        assertEquals(9f, PaperAnswerCardMetaSize.value)
        assertEquals(11f, PaperAnswerCardMetaLineHeight.value)
        assertEquals(4f, PaperAnswerCardSupportHeaderSpacing.value)
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
        assertEquals(
            "UNSURE FIT",
            uncertainFitNoticeLabel(
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
