package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.senku.ui.answer.AnswerContent;
import com.senku.ui.answer.AnswerSurfaceLabel;
import com.senku.ui.answer.Evidence;
import com.senku.ui.answer.Mode;
import com.senku.ui.answer.PaperAnswerCardModel;

import java.util.List;

import org.junit.Test;

public final class DetailAnswerCardModelFactoryTest {
    @Test
    public void reviewedDeterministicCardBuildsReviewedEvidencePaperModel() {
        PaperAnswerCardModel model = DetailAnswerCardModelFactory.buildPaperModel(
            state(
                "ANSWER\nKeep the child still and seek urgent help.",
                "offline answer | 1.2s",
                1,
                "Moderate",
                false,
                false,
                false,
                true,
                OfflineAnswerEngine.AnswerMode.CONFIDENT,
                "answer_card:poisoning_unknown_ingestion",
                reviewedCardMetadata()
            )
        );

        AnswerContent content = model.getContent();

        assertEquals(Mode.Paper, model.getMode());
        assertEquals("Show proof", model.getShowProofLabel());
        assertEquals("On-device", content.getHost());
        assertEquals(1.2, content.getElapsedSeconds(), 0.0);
        assertEquals(Evidence.Strong, content.getEvidence());
        assertEquals(AnswerSurfaceLabel.ReviewedCardEvidence, content.getAnswerSurfaceLabel());
        assertEquals(ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME, content.getAnswerProvenance());
        assertTrue(content.getReviewedCardBacked());
        assertEquals("GD-898", content.getReviewedCardMetadata().cardGuideId);
    }

    @Test
    public void generatedEvidenceUsesCurrentEvidenceStrengthLabel() {
        AnswerContent strong = DetailAnswerCardModelFactory.buildPaperModel(
            state(
                "ANSWER\nUse the cited guide.",
                "host answer | 0.8s",
                3,
                "Strong",
                false,
                false,
                false,
                false,
                OfflineAnswerEngine.AnswerMode.CONFIDENT,
                "",
                null
            )
        ).getContent();
        AnswerContent moderate = DetailAnswerCardModelFactory.buildPaperModel(
            state(
                "ANSWER\nUse the cited guide.",
                "host answer | 0.8s",
                1,
                "Moderate",
                false,
                false,
                false,
                false,
                OfflineAnswerEngine.AnswerMode.CONFIDENT,
                "",
                null
            )
        ).getContent();

        assertEquals("Host", strong.getHost());
        assertEquals(Evidence.Strong, strong.getEvidence());
        assertEquals(AnswerSurfaceLabel.GeneratedEvidence, strong.getAnswerSurfaceLabel());
        assertEquals(Evidence.Moderate, moderate.getEvidence());
        assertEquals(AnswerSurfaceLabel.GeneratedEvidence, moderate.getAnswerSurfaceLabel());
    }

    @Test
    public void lowCoverageAndAbstainRoutesSuppressEvidence() {
        AnswerContent lowCoverage = DetailAnswerCardModelFactory.buildPaperModel(
            state(
                "ANSWER\nWeak fit.",
                "low coverage | 0.4s",
                2,
                "Strong",
                false,
                true,
                false,
                false,
                OfflineAnswerEngine.AnswerMode.CONFIDENT,
                "",
                null
            )
        ).getContent();
        AnswerContent abstain = DetailAnswerCardModelFactory.buildPaperModel(
            state(
                "No confident match.",
                "abstain | 0.1s",
                0,
                "Strong",
                true,
                false,
                false,
                false,
                OfflineAnswerEngine.AnswerMode.ABSTAIN,
                "",
                null
            )
        ).getContent();

        assertEquals(Evidence.None, lowCoverage.getEvidence());
        assertEquals(AnswerSurfaceLabel.GeneratedEvidence, lowCoverage.getAnswerSurfaceLabel());
        assertEquals(Evidence.None, abstain.getEvidence());
        assertEquals("Instant", abstain.getHost());
        assertEquals(AnswerSurfaceLabel.Abstain, abstain.getAnswerSurfaceLabel());
    }

    @Test
    public void uncertainFitForcesLimitedFitSurfaceAndInstantHostFallback() {
        AnswerContent content = DetailAnswerCardModelFactory.buildPaperModel(
            state(
                "ANSWER\nThis may fit, but check the sources.",
                "",
                2,
                "Strong",
                false,
                false,
                true,
                false,
                OfflineAnswerEngine.AnswerMode.CONFIDENT,
                "answer_card:poisoning_unknown_ingestion",
                reviewedCardMetadata()
            )
        ).getContent();

        assertEquals("Instant", content.getHost());
        assertEquals(Evidence.Moderate, content.getEvidence());
        assertEquals(true, content.getUncertainFit());
        assertEquals(AnswerSurfaceLabel.LimitedFit, content.getAnswerSurfaceLabel());
        assertEquals("uncertain_fit_card", content.getAnswerProvenance());
    }

    @Test
    public void nullStateDefaultsToEmptyUnknownCard() {
        AnswerContent content = DetailAnswerCardModelFactory.buildPaperModel(null).getContent();

        assertEquals("", content.getHost());
        assertEquals(Evidence.None, content.getEvidence());
        assertEquals(AnswerSurfaceLabel.Unknown, content.getAnswerSurfaceLabel());
    }

    private static DetailAnswerCardModelFactory.State state(
        String body,
        String subtitle,
        int sourceCount,
        String evidenceStrengthLabel,
        boolean abstain,
        boolean lowCoverage,
        boolean uncertainFit,
        boolean deterministic,
        OfflineAnswerEngine.AnswerMode mode,
        String ruleId,
        ReviewedCardMetadata metadata
    ) {
        return new DetailAnswerCardModelFactory.State(
            body,
            subtitle,
            sourceCount,
            evidenceStrengthLabel,
            "Strong",
            "Moderate",
            abstain,
            lowCoverage,
            uncertainFit,
            deterministic,
            false,
            mode,
            ruleId,
            metadata
        );
    }

    private static ReviewedCardMetadata reviewedCardMetadata() {
        return new ReviewedCardMetadata(
            "poisoning_unknown_ingestion",
            "GD-898",
            "pilot_reviewed",
            "reviewed_source_family",
            ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
            List.of("GD-898")
        );
    }
}
