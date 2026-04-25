package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AnswerCard {
    public final String cardId;
    public final String guideId;
    public final String slug;
    public final String title;
    public final String riskTier;
    public final String evidenceOwner;
    public final String reviewStatus;
    public final String runtimeCitationPolicy;
    public final String routineBoundary;
    public final String acceptableUncertainFit;
    public final String notes;
    public final List<AnswerCardClause> clauses;
    public final List<AnswerCardSource> sources;

    public AnswerCard(
        String cardId,
        String guideId,
        String slug,
        String title,
        String riskTier,
        String evidenceOwner,
        String reviewStatus,
        String runtimeCitationPolicy,
        String routineBoundary,
        String acceptableUncertainFit,
        String notes,
        List<AnswerCardClause> clauses,
        List<AnswerCardSource> sources
    ) {
        this.cardId = emptySafe(cardId);
        this.guideId = emptySafe(guideId);
        this.slug = emptySafe(slug);
        this.title = emptySafe(title);
        this.riskTier = emptySafe(riskTier);
        this.evidenceOwner = emptySafe(evidenceOwner);
        this.reviewStatus = emptySafe(reviewStatus);
        this.runtimeCitationPolicy = emptySafe(runtimeCitationPolicy);
        this.routineBoundary = emptySafe(routineBoundary);
        this.acceptableUncertainFit = emptySafe(acceptableUncertainFit);
        this.notes = emptySafe(notes);
        this.clauses = immutableCopy(clauses);
        this.sources = immutableCopy(sources);
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    private static String emptySafe(String text) {
        return text == null ? "" : text;
    }
}
