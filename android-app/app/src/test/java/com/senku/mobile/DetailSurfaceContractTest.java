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
        assertEquals(DetailSurfaceContract.SurfaceTreatment.GUIDE_PAPER, posture.surfaceTreatment);
        assertEquals(DetailSurfaceContract.ComposerEligibility.INELIGIBLE, posture.composerEligibility);
        assertEquals(DetailSurfaceContract.ChromePlan.GUIDE_PAPER_READER, posture.chromePlan);
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
        assertEquals(DetailSurfaceContract.SurfaceTreatment.ANSWER_CANVAS, posture.surfaceTreatment);
        assertEquals(DetailSurfaceContract.ComposerEligibility.ELIGIBLE, posture.composerEligibility);
        assertEquals(DetailSurfaceContract.ChromePlan.ANSWER_CANVAS_WITH_COMPOSER, posture.chromePlan);
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
        assertEquals(DetailSurfaceContract.SurfaceTreatment.ANSWER_CANVAS, posture.surfaceTreatment);
        assertEquals(DetailSurfaceContract.ComposerEligibility.INELIGIBLE, posture.composerEligibility);
        assertEquals(DetailSurfaceContract.ChromePlan.ANSWER_CANVAS_SUPPORT_ONLY, posture.chromePlan);
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
        assertEquals(DetailSurfaceContract.ChromePlan.GUIDE_PAPER_READER, posture.chromePlan);
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

    @Test
    public void canonicalGuideReaderHidesAnswerEvidenceAndFollowUp() {
        DetailSurfaceContract.Posture posture = DetailSurfaceContract.guide();

        assertEquals(true, DetailSurfaceContract.isCanonicalGuideReader(posture));
        assertEquals(false, DetailSurfaceContract.shouldShowAnswerEvidence(posture));
        assertEquals(false, DetailSurfaceContract.shouldShowAnswerFollowUp(posture));
        assertEquals(true, DetailSurfaceContract.usesGuidePaperSurface(posture));
        assertEquals(false, DetailSurfaceContract.usesAnswerCanvasSurface(posture));
        assertEquals(false, DetailSurfaceContract.isComposerEligible(posture));
    }

    @Test
    public void answerDetailsShowEvidenceWithFollowUpForActionableAnswers() {
        DetailSurfaceContract.Posture generated =
            DetailSurfaceContract.answer(DetailSurfaceContract.AnswerKind.GENERATED);
        DetailSurfaceContract.Posture deterministic =
            DetailSurfaceContract.answer(DetailSurfaceContract.AnswerKind.DETERMINISTIC);

        assertEquals(false, DetailSurfaceContract.isCanonicalGuideReader(generated));
        assertEquals(true, DetailSurfaceContract.shouldShowAnswerEvidence(generated));
        assertEquals(true, DetailSurfaceContract.shouldShowAnswerFollowUp(generated));
        assertEquals(false, DetailSurfaceContract.usesGuidePaperSurface(generated));
        assertEquals(true, DetailSurfaceContract.usesAnswerCanvasSurface(generated));
        assertEquals(true, DetailSurfaceContract.isComposerEligible(generated));
        assertEquals(true, DetailSurfaceContract.shouldShowAnswerEvidence(deterministic));
        assertEquals(true, DetailSurfaceContract.shouldShowAnswerFollowUp(deterministic));
        assertEquals(true, DetailSurfaceContract.isComposerEligible(deterministic));
    }

    @Test
    public void abstainAndUncertainAnswerDetailsKeepEvidenceButHideFollowUp() {
        DetailSurfaceContract.Posture abstain =
            DetailSurfaceContract.answer(DetailSurfaceContract.AnswerKind.ABSTAIN);
        DetailSurfaceContract.Posture uncertain =
            DetailSurfaceContract.answer(DetailSurfaceContract.AnswerKind.UNCERTAIN_FIT);

        assertEquals(true, DetailSurfaceContract.shouldShowAnswerEvidence(abstain));
        assertEquals(false, DetailSurfaceContract.shouldShowAnswerFollowUp(abstain));
        assertEquals(true, DetailSurfaceContract.usesAnswerCanvasSurface(abstain));
        assertEquals(false, DetailSurfaceContract.isComposerEligible(abstain));
        assertEquals(true, DetailSurfaceContract.shouldShowAnswerEvidence(uncertain));
        assertEquals(false, DetailSurfaceContract.shouldShowAnswerFollowUp(uncertain));
        assertEquals(true, DetailSurfaceContract.usesAnswerCanvasSurface(uncertain));
        assertEquals(false, DetailSurfaceContract.isComposerEligible(uncertain));
    }

    @Test
    public void guideReaderV2AffordancesSeparateNavigationFromAnswerActions() {
        DetailSurfaceContract.Posture direct = DetailSurfaceContract.guide();
        DetailSurfaceContract.Posture handoff =
            DetailSurfaceContract.guide(DetailSurfaceContract.EntryPoint.RELATED_GUIDE_HANDOFF);

        assertEquals(DetailSurfaceContract.PrimaryAction.OPEN_GUIDE, direct.primaryAction);
        assertEquals(DetailSurfaceContract.EvidenceRegionRole.GUIDE_METADATA, direct.evidenceRegionRole);
        assertEquals(
            DetailSurfaceContract.RelatedGuideRole.GUIDE_READER_NAVIGATION,
            direct.relatedGuideRole
        );
        assertEquals(DetailSurfaceContract.FollowUpEligibility.INELIGIBLE, direct.followUpEligibility);
        assertEquals(DetailSurfaceContract.SurfaceTreatment.GUIDE_PAPER, direct.surfaceTreatment);
        assertEquals(DetailSurfaceContract.ComposerEligibility.INELIGIBLE, direct.composerEligibility);
        assertEquals(DetailSurfaceContract.ChromePlan.GUIDE_PAPER_READER, direct.chromePlan);
        assertEquals(false, DetailSurfaceContract.isFollowUpEligible(direct));
        assertEquals(false, DetailSurfaceContract.isAnswerEvidenceRegion(direct));
        assertEquals(false, DetailSurfaceContract.usesAnswerRelatedGuideRole(direct));

        assertEquals(DetailSurfaceContract.PrimaryAction.OPEN_GUIDE, handoff.primaryAction);
        assertEquals(DetailSurfaceContract.EvidenceRegionRole.GUIDE_METADATA, handoff.evidenceRegionRole);
        assertEquals(
            DetailSurfaceContract.RelatedGuideRole.GUIDE_READER_HANDOFF_CONTEXT,
            handoff.relatedGuideRole
        );
        assertEquals(DetailSurfaceContract.FollowUpEligibility.INELIGIBLE, handoff.followUpEligibility);
    }

    @Test
    public void answerDetailV2AffordancesSeparateActionableAnswersFromSupportChoices() {
        DetailSurfaceContract.Posture generated =
            DetailSurfaceContract.answer(DetailSurfaceContract.AnswerKind.GENERATED);
        DetailSurfaceContract.Posture deterministic =
            DetailSurfaceContract.answer(DetailSurfaceContract.AnswerKind.DETERMINISTIC);
        DetailSurfaceContract.Posture abstain =
            DetailSurfaceContract.answer(DetailSurfaceContract.AnswerKind.ABSTAIN);
        DetailSurfaceContract.Posture uncertain =
            DetailSurfaceContract.answer(DetailSurfaceContract.AnswerKind.UNCERTAIN_FIT);

        assertEquals(DetailSurfaceContract.PrimaryAction.ASK_FOLLOW_UP, generated.primaryAction);
        assertEquals(DetailSurfaceContract.EvidenceRegionRole.ANSWER_EVIDENCE, generated.evidenceRegionRole);
        assertEquals(
            DetailSurfaceContract.RelatedGuideRole.ANSWER_SOURCE_CONNECTIONS,
            generated.relatedGuideRole
        );
        assertEquals(DetailSurfaceContract.FollowUpEligibility.ELIGIBLE, generated.followUpEligibility);
        assertEquals(DetailSurfaceContract.SurfaceTreatment.ANSWER_CANVAS, generated.surfaceTreatment);
        assertEquals(DetailSurfaceContract.ComposerEligibility.ELIGIBLE, generated.composerEligibility);
        assertEquals(DetailSurfaceContract.ChromePlan.ANSWER_CANVAS_WITH_COMPOSER, generated.chromePlan);
        assertEquals(true, DetailSurfaceContract.isFollowUpEligible(generated));
        assertEquals(true, DetailSurfaceContract.isAnswerEvidenceRegion(generated));
        assertEquals(true, DetailSurfaceContract.usesAnswerRelatedGuideRole(generated));

        assertEquals(DetailSurfaceContract.PrimaryAction.ASK_FOLLOW_UP, deterministic.primaryAction);
        assertEquals(
            DetailSurfaceContract.EvidenceRegionRole.DETERMINISTIC_EVIDENCE,
            deterministic.evidenceRegionRole
        );
        assertEquals(
            DetailSurfaceContract.RelatedGuideRole.ANSWER_SOURCE_CONNECTIONS,
            deterministic.relatedGuideRole
        );
        assertEquals(DetailSurfaceContract.FollowUpEligibility.ELIGIBLE, deterministic.followUpEligibility);

        assertEquals(DetailSurfaceContract.PrimaryAction.REVIEW_SUPPORTING_GUIDES, abstain.primaryAction);
        assertEquals(DetailSurfaceContract.EvidenceRegionRole.ABSTAIN_RATIONALE, abstain.evidenceRegionRole);
        assertEquals(
            DetailSurfaceContract.RelatedGuideRole.ANSWER_SUPPORTING_CHOICES,
            abstain.relatedGuideRole
        );
        assertEquals(DetailSurfaceContract.FollowUpEligibility.INELIGIBLE, abstain.followUpEligibility);
        assertEquals(DetailSurfaceContract.SurfaceTreatment.ANSWER_CANVAS, abstain.surfaceTreatment);
        assertEquals(DetailSurfaceContract.ComposerEligibility.INELIGIBLE, abstain.composerEligibility);
        assertEquals(DetailSurfaceContract.ChromePlan.ANSWER_CANVAS_SUPPORT_ONLY, abstain.chromePlan);
        assertEquals(false, DetailSurfaceContract.isFollowUpEligible(abstain));
        assertEquals(true, DetailSurfaceContract.isAnswerEvidenceRegion(abstain));
        assertEquals(true, DetailSurfaceContract.usesAnswerRelatedGuideRole(abstain));

        assertEquals(DetailSurfaceContract.PrimaryAction.REVIEW_SUPPORTING_GUIDES, uncertain.primaryAction);
        assertEquals(
            DetailSurfaceContract.EvidenceRegionRole.UNCERTAIN_FIT_RATIONALE,
            uncertain.evidenceRegionRole
        );
        assertEquals(
            DetailSurfaceContract.RelatedGuideRole.ANSWER_SUPPORTING_CHOICES,
            uncertain.relatedGuideRole
        );
        assertEquals(DetailSurfaceContract.FollowUpEligibility.INELIGIBLE, uncertain.followUpEligibility);
    }

    @Test
    public void detailSurfaceHelpersDefaultNullToNoVisibleAnswerChrome() {
        assertEquals(false, DetailSurfaceContract.isCanonicalGuideReader(null));
        assertEquals(false, DetailSurfaceContract.shouldShowAnswerEvidence(null));
        assertEquals(false, DetailSurfaceContract.shouldShowAnswerFollowUp(null));
        assertEquals(false, DetailSurfaceContract.isFollowUpEligible(null));
        assertEquals(false, DetailSurfaceContract.usesGuidePaperSurface(null));
        assertEquals(false, DetailSurfaceContract.usesAnswerCanvasSurface(null));
        assertEquals(false, DetailSurfaceContract.isComposerEligible(null));
        assertEquals(false, DetailSurfaceContract.isAnswerEvidenceRegion(null));
        assertEquals(false, DetailSurfaceContract.usesAnswerRelatedGuideRole(null));
    }
}
