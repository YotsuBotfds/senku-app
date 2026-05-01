package com.senku.ui.answer

import com.senku.mobile.ReviewedCardMetadata
import org.junit.Assert.assertEquals
import org.junit.Test

class PaperAnswerCardLabelTest {
    @Test
    fun compactEvidenceLabel_usesFieldManualSourceLanguage() {
        assertEquals(
            "Rule match",
            compactEvidenceLabel(answer(answerSurfaceLabel = AnswerSurfaceLabel.DeterministicRule)),
        )
        assertEquals(
            "Sources",
            compactEvidenceLabel(answer(answerSurfaceLabel = AnswerSurfaceLabel.ReviewedCardEvidence)),
        )
        assertEquals(
            "• UNSURE",
            compactEvidenceLabel(answer(answerSurfaceLabel = AnswerSurfaceLabel.LimitedFit)),
        )
        assertEquals(
            "Sources ready",
            compactEvidenceLabel(answer(evidence = Evidence.Strong)),
        )
        assertEquals(
            "Sources",
            compactEvidenceLabel(answer(evidence = Evidence.Moderate)),
        )
        assertEquals(
            "Limited sources",
            compactEvidenceLabel(answer(evidence = Evidence.None)),
        )
        assertEquals(
            "• UNSURE",
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
            "Sources ready",
            compactEvidenceLabel(
                answer(
                    evidence = Evidence.Strong,
                    answerSurfaceLabel = AnswerSurfaceLabel.ReviewedCardEvidence,
                ),
            ),
        )
        assertEquals(
            "Limited sources",
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

        assertEquals("2 SOURCES • 0.8s", buildFooterMeta(content))
    }

    @Test
    fun buildFooterMeta_omitsContextKeptForRainShelterLimitedFit() {
        val content = answer(
            sourceCount = 3,
            host = "This device",
            answerSurfaceLabel = AnswerSurfaceLabel.LimitedFit,
            reviewedCardMetadata = ReviewedCardMetadata(
                "rain-shelter",
                "GD-345",
                "",
                "",
                "",
                emptyList(),
            ),
        )

        assertEquals("GD-345 \u2022 THIS DEVICE", buildFooterMeta(content))
    }

    @Test
    fun buildFooterMeta_keepsAnswerAnchorAndVisibleSourceStateWithoutContextKept() {
        val content = answer(
            sourceCount = 3,
            host = "This device",
            elapsedSeconds = 1.2,
            answerSurfaceLabel = AnswerSurfaceLabel.ReviewedCardEvidence,
            reviewedCardMetadata = ReviewedCardMetadata(
                "rain-shelter",
                "GD-345",
                "",
                "",
                "",
                emptyList(),
            ),
        )

        assertEquals("GD-345 \u2022 3 SOURCES VISIBLE", buildFooterMeta(content))
    }

    @Test
    fun uncertainFitNotice_usesArticleSourceLanguage() {
        val content = answer(
            sourceCount = 3,
            answerSurfaceLabel = AnswerSurfaceLabel.LimitedFit,
        )

        assertEquals(true, shouldShowUncertainFitNotice(content))
        assertEquals(
            "UNSURE FIT",
            uncertainFitNoticeLabel(content),
        )
        assertEquals(
            "Senku found 3 guides that may apply but no single guide is a confident anchor. Treat this as guidance, not procedure. See sources below.",
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
        assertEquals(9f, PaperAnswerCardProofCtaSize.value)
        assertEquals(12f, PaperAnswerCardProofCtaLineHeight.value)
    }

    @Test
    fun uncertainFitNotice_isEmphasizedForArticleParity() {
        assertEquals(
            true,
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
        assertEquals("Sources", displayProofCtaLabel("Proof rail"))
        assertEquals("Sources", displayProofCtaLabel("Open proof"))
        assertEquals("Sources", displayProofCtaLabel("Show proof"))
        assertEquals("Sources", displayProofCtaLabel("Show proof rail"))
        assertEquals("Sources", displayProofCtaLabel("Hide proof"))
        assertEquals("Sources", displayProofCtaLabel("View sources"))
        assertEquals("View citations", displayProofCtaLabel("View citations"))
    }

    @Test
    fun paperAnswerArticleChrome_isHiddenForLimitedFitArticleAnswers() {
        assertEquals(
            false,
            shouldShowPaperAnswerArticleChrome(
                answer(answerSurfaceLabel = AnswerSurfaceLabel.LimitedFit),
            ),
        )
        assertEquals(
            false,
            shouldShowPaperAnswerArticleChrome(
                AnswerContent(
                    short = "Related guidance only.",
                    sourceCount = 3,
                    host = "This device",
                    elapsedSeconds = 0.0,
                    uncertainFit = true,
                ),
            ),
        )
        assertEquals(true, shouldShowPaperAnswerArticleChrome(answer()))
    }

    private fun answer(
        evidence: Evidence = Evidence.Moderate,
        sourceCount: Int = 1,
        host: String = "",
        elapsedSeconds: Double = 0.0,
        answerSurfaceLabel: AnswerSurfaceLabel = AnswerSurfaceLabel.Unknown,
        reviewedCardMetadata: ReviewedCardMetadata = ReviewedCardMetadata.empty(),
    ): AnswerContent =
        AnswerContent(
            short = "Use the relevant field guidance.",
            evidence = evidence,
            sourceCount = sourceCount,
            host = host,
            elapsedSeconds = elapsedSeconds,
            answerSurfaceLabel = answerSurfaceLabel,
            reviewedCardMetadata = reviewedCardMetadata,
        )
}
