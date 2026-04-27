package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class DetailSurfaceContractTest {
    @Test
    public void guideUsesReaderPosture() {
        DetailSurfaceContract.Posture posture = DetailSurfaceContract.guide();

        assertEquals(DetailSurfaceContract.Surface.GUIDE_READER, posture.surface);
        assertEquals(DetailSurfaceContract.TitleRole.GUIDE_TITLE, posture.titleRole);
        assertEquals(DetailSurfaceContract.BodyRole.GUIDE_BODY, posture.bodyRole);
        assertEquals(
            DetailSurfaceContract.TrustProvenanceVisibility.GUIDE_METADATA,
            posture.trustProvenanceVisibility
        );
        assertEquals(DetailSurfaceContract.FollowUpVisibility.HIDDEN, posture.followUpVisibility);
        assertEquals(DetailSurfaceContract.RelatedGuidePosture.GUIDE_NAVIGATION, posture.relatedGuidePosture);
    }

    @Test
    public void generatedAnswerUsesAnswerDetailPosture() {
        DetailSurfaceContract.Posture posture =
            DetailSurfaceContract.answer(DetailSurfaceContract.AnswerKind.GENERATED);

        assertEquals(DetailSurfaceContract.Surface.ANSWER_DETAIL, posture.surface);
        assertEquals(DetailSurfaceContract.TitleRole.USER_QUESTION, posture.titleRole);
        assertEquals(DetailSurfaceContract.BodyRole.GENERATED_ANSWER_BODY, posture.bodyRole);
        assertEquals(
            DetailSurfaceContract.TrustProvenanceVisibility.ANSWER_PROVENANCE,
            posture.trustProvenanceVisibility
        );
        assertEquals(DetailSurfaceContract.FollowUpVisibility.VISIBLE, posture.followUpVisibility);
        assertEquals(
            DetailSurfaceContract.RelatedGuidePosture.SOURCE_ANCHORED_CONNECTIONS,
            posture.relatedGuidePosture
        );
    }

    @Test
    public void deterministicAnswerKeepsAnswerDetailWithDeterministicProvenance() {
        DetailSurfaceContract.Posture posture =
            DetailSurfaceContract.answer(DetailSurfaceContract.AnswerKind.DETERMINISTIC);

        assertEquals(DetailSurfaceContract.Surface.ANSWER_DETAIL, posture.surface);
        assertEquals(DetailSurfaceContract.TitleRole.USER_QUESTION, posture.titleRole);
        assertEquals(DetailSurfaceContract.BodyRole.DETERMINISTIC_ANSWER_BODY, posture.bodyRole);
        assertEquals(
            DetailSurfaceContract.TrustProvenanceVisibility.DETERMINISTIC_PROVENANCE,
            posture.trustProvenanceVisibility
        );
        assertEquals(DetailSurfaceContract.FollowUpVisibility.VISIBLE, posture.followUpVisibility);
        assertEquals(
            DetailSurfaceContract.RelatedGuidePosture.SOURCE_ANCHORED_CONNECTIONS,
            posture.relatedGuidePosture
        );
    }

    @Test
    public void abstainAnswerSuppressesFollowupAndShowsSupportingGuideChoices() {
        DetailSurfaceContract.Posture posture =
            DetailSurfaceContract.answer(DetailSurfaceContract.AnswerKind.ABSTAIN);

        assertEquals(DetailSurfaceContract.Surface.ANSWER_DETAIL, posture.surface);
        assertEquals(DetailSurfaceContract.BodyRole.ABSTAIN_BODY, posture.bodyRole);
        assertEquals(
            DetailSurfaceContract.TrustProvenanceVisibility.ABSTAIN_PROVENANCE,
            posture.trustProvenanceVisibility
        );
        assertEquals(DetailSurfaceContract.FollowUpVisibility.HIDDEN, posture.followUpVisibility);
        assertEquals(
            DetailSurfaceContract.RelatedGuidePosture.SUPPORTING_GUIDE_CHOICES,
            posture.relatedGuidePosture
        );
    }

    @Test
    public void uncertainFitAnswerSuppressesFollowupAndShowsSupportingGuideChoices() {
        DetailSurfaceContract.Posture posture =
            DetailSurfaceContract.answer(DetailSurfaceContract.AnswerKind.UNCERTAIN_FIT);

        assertEquals(DetailSurfaceContract.Surface.ANSWER_DETAIL, posture.surface);
        assertEquals(DetailSurfaceContract.BodyRole.UNCERTAIN_FIT_BODY, posture.bodyRole);
        assertEquals(
            DetailSurfaceContract.TrustProvenanceVisibility.UNCERTAIN_FIT_PROVENANCE,
            posture.trustProvenanceVisibility
        );
        assertEquals(DetailSurfaceContract.FollowUpVisibility.HIDDEN, posture.followUpVisibility);
        assertEquals(
            DetailSurfaceContract.RelatedGuidePosture.SUPPORTING_GUIDE_CHOICES,
            posture.relatedGuidePosture
        );
    }

    @Test
    public void guideOpenedFromRelatedGuideHandoffStaysGuideReaderWithHandoffContext() {
        DetailSurfaceContract.Posture posture =
            DetailSurfaceContract.guide(DetailSurfaceContract.EntryPoint.RELATED_GUIDE_HANDOFF);

        assertEquals(DetailSurfaceContract.Surface.GUIDE_READER, posture.surface);
        assertEquals(DetailSurfaceContract.TitleRole.GUIDE_TITLE_WITH_HANDOFF_CONTEXT, posture.titleRole);
        assertEquals(DetailSurfaceContract.BodyRole.GUIDE_BODY, posture.bodyRole);
        assertEquals(
            DetailSurfaceContract.TrustProvenanceVisibility.GUIDE_METADATA,
            posture.trustProvenanceVisibility
        );
        assertEquals(DetailSurfaceContract.FollowUpVisibility.HIDDEN, posture.followUpVisibility);
        assertEquals(
            DetailSurfaceContract.RelatedGuidePosture.GUIDE_HANDOFF_CONTEXT,
            posture.relatedGuidePosture
        );
    }

    @Test
    public void surfaceClassifierMapsGuideAndAnswerStates() {
        assertEquals(
            DetailSurfaceContract.Surface.GUIDE_READER,
            DetailSurfaceContract.classifySurface(false)
        );
        assertEquals(
            DetailSurfaceContract.Surface.ANSWER_DETAIL,
            DetailSurfaceContract.classifySurface(true)
        );
    }

    @Test
    public void stateFactoryDefaultsNullGuideInputsToDirectGuideReader() {
        DetailSurfaceContract.Posture posture =
            DetailSurfaceContract.fromState(false, null, null, false, false, null);

        assertEquals(DetailSurfaceContract.Surface.GUIDE_READER, posture.surface);
        assertEquals(DetailSurfaceContract.TitleRole.GUIDE_TITLE, posture.titleRole);
        assertEquals(DetailSurfaceContract.RelatedGuidePosture.GUIDE_NAVIGATION, posture.relatedGuidePosture);
    }

    @Test
    public void stateFactoryDefaultsNullAnswerInputsToGeneratedAnswerDetail() {
        DetailSurfaceContract.Posture posture =
            DetailSurfaceContract.fromState(true, null, null, false, false, null);

        assertEquals(DetailSurfaceContract.Surface.ANSWER_DETAIL, posture.surface);
        assertEquals(DetailSurfaceContract.BodyRole.GENERATED_ANSWER_BODY, posture.bodyRole);
        assertEquals(
            DetailSurfaceContract.TrustProvenanceVisibility.ANSWER_PROVENANCE,
            posture.trustProvenanceVisibility
        );
        assertEquals(DetailSurfaceContract.FollowUpVisibility.VISIBLE, posture.followUpVisibility);
    }

    @Test
    public void stateFactoryMapsGuideHandoffToGuideReaderHandoffPosture() {
        DetailSurfaceContract.Posture posture =
            DetailSurfaceContract.fromState(
                false,
                DetailSurfaceContract.EntryPoint.RELATED_GUIDE_HANDOFF,
                "ABSTAIN",
                true,
                true,
                "card-rule"
            );

        assertEquals(DetailSurfaceContract.Surface.GUIDE_READER, posture.surface);
        assertEquals(DetailSurfaceContract.TitleRole.GUIDE_TITLE_WITH_HANDOFF_CONTEXT, posture.titleRole);
        assertEquals(
            DetailSurfaceContract.RelatedGuidePosture.GUIDE_HANDOFF_CONTEXT,
            posture.relatedGuidePosture
        );
    }

    @Test
    public void answerKindClassificationMapsExistingStateSignals() {
        assertEquals(
            DetailSurfaceContract.AnswerKind.ABSTAIN,
            DetailSurfaceContract.classifyAnswerKind("ABSTAIN", false, false, "")
        );
        assertEquals(
            DetailSurfaceContract.AnswerKind.DETERMINISTIC,
            DetailSurfaceContract.classifyAnswerKind("CONFIDENT", false, false, "reviewed-card")
        );
        assertEquals(
            DetailSurfaceContract.AnswerKind.UNCERTAIN_FIT,
            DetailSurfaceContract.classifyAnswerKind("uncertain_fit", true, false, "reviewed-card")
        );
        assertEquals(
            DetailSurfaceContract.AnswerKind.GENERATED,
            DetailSurfaceContract.classifyAnswerKind("CONFIDENT", false, false, "")
        );
    }
}
