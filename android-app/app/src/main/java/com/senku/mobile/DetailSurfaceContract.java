package com.senku.mobile;

import java.util.Objects;

public final class DetailSurfaceContract {
    private static final String ANSWER_MODE_ABSTAIN = "ABSTAIN";
    private static final String ANSWER_MODE_UNCERTAIN_FIT = "UNCERTAIN_FIT";

    public enum Surface {
        GUIDE_READER,
        ANSWER_DETAIL
    }

    public enum EntryPoint {
        DIRECT,
        RELATED_GUIDE_HANDOFF
    }

    public enum AnswerKind {
        GENERATED,
        DETERMINISTIC,
        ABSTAIN,
        UNCERTAIN_FIT
    }

    public enum TitleRole {
        GUIDE_TITLE,
        GUIDE_TITLE_WITH_HANDOFF_CONTEXT,
        USER_QUESTION
    }

    public enum BodyRole {
        GUIDE_BODY,
        GENERATED_ANSWER_BODY,
        DETERMINISTIC_ANSWER_BODY,
        ABSTAIN_BODY,
        UNCERTAIN_FIT_BODY
    }

    public enum TrustProvenanceVisibility {
        GUIDE_METADATA,
        ANSWER_PROVENANCE,
        DETERMINISTIC_PROVENANCE,
        ABSTAIN_PROVENANCE,
        UNCERTAIN_FIT_PROVENANCE
    }

    public enum FollowUpVisibility {
        HIDDEN,
        VISIBLE
    }

    public enum RelatedGuidePosture {
        GUIDE_NAVIGATION,
        GUIDE_HANDOFF_CONTEXT,
        SOURCE_ANCHORED_CONNECTIONS,
        SUPPORTING_GUIDE_CHOICES
    }

    public static final class Posture {
        public final Surface surface;
        public final TitleRole titleRole;
        public final BodyRole bodyRole;
        public final TrustProvenanceVisibility trustProvenanceVisibility;
        public final FollowUpVisibility followUpVisibility;
        public final RelatedGuidePosture relatedGuidePosture;

        private Posture(
            Surface surface,
            TitleRole titleRole,
            BodyRole bodyRole,
            TrustProvenanceVisibility trustProvenanceVisibility,
            FollowUpVisibility followUpVisibility,
            RelatedGuidePosture relatedGuidePosture
        ) {
            this.surface = Objects.requireNonNull(surface);
            this.titleRole = Objects.requireNonNull(titleRole);
            this.bodyRole = Objects.requireNonNull(bodyRole);
            this.trustProvenanceVisibility = Objects.requireNonNull(trustProvenanceVisibility);
            this.followUpVisibility = Objects.requireNonNull(followUpVisibility);
            this.relatedGuidePosture = Objects.requireNonNull(relatedGuidePosture);
        }
    }

    private DetailSurfaceContract() {
    }

    public static Posture guide() {
        return guide(EntryPoint.DIRECT);
    }

    public static Posture guide(EntryPoint entryPoint) {
        boolean handoff = normalizeEntryPoint(entryPoint) == EntryPoint.RELATED_GUIDE_HANDOFF;
        return new Posture(
            Surface.GUIDE_READER,
            handoff ? TitleRole.GUIDE_TITLE_WITH_HANDOFF_CONTEXT : TitleRole.GUIDE_TITLE,
            BodyRole.GUIDE_BODY,
            TrustProvenanceVisibility.GUIDE_METADATA,
            FollowUpVisibility.HIDDEN,
            handoff ? RelatedGuidePosture.GUIDE_HANDOFF_CONTEXT : RelatedGuidePosture.GUIDE_NAVIGATION
        );
    }

    public static Surface classifySurface(boolean isAnswer) {
        return isAnswer ? Surface.ANSWER_DETAIL : Surface.GUIDE_READER;
    }

    public static AnswerKind classifyAnswerKind(
        String answerMode,
        boolean deterministic,
        boolean abstain,
        String ruleId
    ) {
        String normalizedMode = safe(answerMode).trim().toUpperCase(java.util.Locale.US);
        if (abstain || ANSWER_MODE_ABSTAIN.equals(normalizedMode)) {
            return AnswerKind.ABSTAIN;
        }
        if (ANSWER_MODE_UNCERTAIN_FIT.equals(normalizedMode)) {
            return AnswerKind.UNCERTAIN_FIT;
        }
        if (deterministic || !safe(ruleId).trim().isEmpty()) {
            return AnswerKind.DETERMINISTIC;
        }
        return AnswerKind.GENERATED;
    }

    public static Posture fromState(
        boolean isAnswer,
        EntryPoint entryPoint,
        String answerMode,
        boolean deterministic,
        boolean abstain,
        String ruleId
    ) {
        if (classifySurface(isAnswer) == Surface.GUIDE_READER) {
            return guide(entryPoint);
        }
        return answer(classifyAnswerKind(answerMode, deterministic, abstain, ruleId));
    }

    public static boolean isCanonicalGuideReader(Posture posture) {
        return posture != null
            && posture.surface == Surface.GUIDE_READER
            && posture.bodyRole == BodyRole.GUIDE_BODY
            && posture.trustProvenanceVisibility == TrustProvenanceVisibility.GUIDE_METADATA
            && posture.followUpVisibility == FollowUpVisibility.HIDDEN;
    }

    public static boolean shouldShowAnswerEvidence(Posture posture) {
        return posture != null
            && posture.surface == Surface.ANSWER_DETAIL
            && posture.trustProvenanceVisibility != TrustProvenanceVisibility.GUIDE_METADATA;
    }

    public static boolean shouldShowAnswerFollowUp(Posture posture) {
        return posture != null
            && posture.surface == Surface.ANSWER_DETAIL
            && posture.followUpVisibility == FollowUpVisibility.VISIBLE;
    }

    public static Posture answer(AnswerKind answerKind) {
        AnswerKind kind = answerKind == null ? AnswerKind.GENERATED : answerKind;
        switch (kind) {
            case DETERMINISTIC:
                return new Posture(
                    Surface.ANSWER_DETAIL,
                    TitleRole.USER_QUESTION,
                    BodyRole.DETERMINISTIC_ANSWER_BODY,
                    TrustProvenanceVisibility.DETERMINISTIC_PROVENANCE,
                    FollowUpVisibility.VISIBLE,
                    RelatedGuidePosture.SOURCE_ANCHORED_CONNECTIONS
                );
            case ABSTAIN:
                return new Posture(
                    Surface.ANSWER_DETAIL,
                    TitleRole.USER_QUESTION,
                    BodyRole.ABSTAIN_BODY,
                    TrustProvenanceVisibility.ABSTAIN_PROVENANCE,
                    FollowUpVisibility.HIDDEN,
                    RelatedGuidePosture.SUPPORTING_GUIDE_CHOICES
                );
            case UNCERTAIN_FIT:
                return new Posture(
                    Surface.ANSWER_DETAIL,
                    TitleRole.USER_QUESTION,
                    BodyRole.UNCERTAIN_FIT_BODY,
                    TrustProvenanceVisibility.UNCERTAIN_FIT_PROVENANCE,
                    FollowUpVisibility.HIDDEN,
                    RelatedGuidePosture.SUPPORTING_GUIDE_CHOICES
                );
            case GENERATED:
            default:
                return new Posture(
                    Surface.ANSWER_DETAIL,
                    TitleRole.USER_QUESTION,
                    BodyRole.GENERATED_ANSWER_BODY,
                    TrustProvenanceVisibility.ANSWER_PROVENANCE,
                    FollowUpVisibility.VISIBLE,
                    RelatedGuidePosture.SOURCE_ANCHORED_CONNECTIONS
                );
        }
    }

    private static EntryPoint normalizeEntryPoint(EntryPoint entryPoint) {
        return entryPoint == null ? EntryPoint.DIRECT : entryPoint;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
