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
}
