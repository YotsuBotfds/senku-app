package com.senku.mobile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

public final class ReviewedCardMetadata implements Serializable {
    public static final String PROVENANCE_REVIEWED_CARD_RUNTIME = "reviewed_card_runtime";
    private static final long serialVersionUID = 1L;
    private static final ReviewedCardMetadata EMPTY = new ReviewedCardMetadata(
        "",
        "",
        "",
        "",
        "",
        Collections.emptyList()
    );

    public final String cardId;
    public final String cardGuideId;
    public final String reviewStatus;
    public final String runtimeCitationPolicy;
    public final String provenance;
    public final List<String> citedReviewedSourceGuideIds;

    public ReviewedCardMetadata(
        String cardId,
        String cardGuideId,
        String reviewStatus,
        String runtimeCitationPolicy,
        String provenance,
        List<String> citedReviewedSourceGuideIds
    ) {
        this.cardId = safe(cardId).trim();
        this.cardGuideId = safe(cardGuideId).trim();
        this.reviewStatus = safe(reviewStatus).trim();
        this.runtimeCitationPolicy = safe(runtimeCitationPolicy).trim();
        this.provenance = safe(provenance).trim();
        this.citedReviewedSourceGuideIds = immutableUniqueIds(citedReviewedSourceGuideIds);
    }

    public static ReviewedCardMetadata empty() {
        return EMPTY;
    }

    public static ReviewedCardMetadata fromAnswerCard(AnswerCard card) {
        if (card == null) {
            return empty();
        }
        return new ReviewedCardMetadata(
            card.cardId,
            card.guideId,
            card.reviewStatus,
            card.runtimeCitationPolicy,
            PROVENANCE_REVIEWED_CARD_RUNTIME,
            sourceGuideIds(card)
        );
    }

    public static ReviewedCardMetadata normalize(ReviewedCardMetadata metadata) {
        return metadata == null ? empty() : metadata;
    }

    public boolean isPresent() {
        return !cardId.isEmpty()
            || !cardGuideId.isEmpty()
            || !reviewStatus.isEmpty()
            || !runtimeCitationPolicy.isEmpty()
            || !provenance.isEmpty()
            || !citedReviewedSourceGuideIds.isEmpty();
    }

    public String citedSourceGuideIdsCsv() {
        return String.join(", ", citedReviewedSourceGuideIds);
    }

    private static List<String> sourceGuideIds(AnswerCard card) {
        if (card == null || card.sources == null || card.sources.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<String> values = new ArrayList<>();
        for (AnswerCardSource source : card.sources) {
            String guideId = safe(source == null ? null : source.sourceGuideId).trim();
            if (!guideId.isEmpty()) {
                values.add(guideId);
            }
        }
        return values;
    }

    private static List<String> immutableUniqueIds(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String value : values) {
            String trimmed = safe(value).trim();
            if (!trimmed.isEmpty()) {
                unique.add(trimmed);
            }
        }
        if (unique.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(unique));
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
