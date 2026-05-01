package com.senku.mobile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public final class ReviewedCardMetadata implements Serializable {
    public static final String ANSWER_CARD_RULE_PREFIX = "answer_card:";
    public static final String PROVENANCE_REVIEWED_CARD_RUNTIME = "reviewed_card_runtime";
    static final String TRANSPORT_FIELD = "reviewed_card_metadata";
    private static final String FIELD_CARD_ID = "card_id";
    private static final String FIELD_CARD_GUIDE_ID = "card_guide_id";
    private static final String FIELD_REVIEW_STATUS = "review_status";
    private static final String FIELD_RUNTIME_CITATION_POLICY = "runtime_citation_policy";
    private static final String FIELD_PROVENANCE = "provenance";
    private static final String FIELD_CITED_REVIEWED_SOURCE_GUIDE_IDS = "cited_reviewed_source_guide_ids";
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

    public static String answerCardRuleId(String cardId) {
        String normalizedCardId = safe(cardId).trim();
        return normalizedCardId.isEmpty() ? "" : ANSWER_CARD_RULE_PREFIX + normalizedCardId;
    }

    public static boolean isAnswerCardRuleId(String ruleId) {
        return safe(ruleId).trim().startsWith(ANSWER_CARD_RULE_PREFIX);
    }

    public static boolean isReviewedRuntimeStatus(String reviewStatus, String provenance) {
        String normalizedStatus = safe(reviewStatus).trim().toLowerCase(Locale.ROOT);
        String normalizedProvenance = safe(provenance).trim().toLowerCase(Locale.ROOT);
        return ("reviewed".equals(normalizedStatus) || "pilot_reviewed".equals(normalizedStatus))
            && PROVENANCE_REVIEWED_CARD_RUNTIME.equals(normalizedProvenance);
    }

    public static boolean isReviewedRuntimeCardWithCitedSources(
        String ruleId,
        ReviewedCardMetadata metadata
    ) {
        return isAnswerCardRuleId(ruleId)
            && normalize(metadata).isReviewedRuntimeCardWithCitedSources();
    }

    static JSONObject toJsonObject(ReviewedCardMetadata metadata) throws JSONException {
        JSONObject output = new JSONObject();
        ReviewedCardMetadata normalized = normalize(metadata);
        if (!normalized.isPresent()) {
            return output;
        }
        output.put(FIELD_CARD_ID, normalized.cardId);
        output.put(FIELD_CARD_GUIDE_ID, normalized.cardGuideId);
        output.put(FIELD_REVIEW_STATUS, normalized.reviewStatus);
        output.put(FIELD_RUNTIME_CITATION_POLICY, normalized.runtimeCitationPolicy);
        output.put(FIELD_PROVENANCE, normalized.provenance);
        output.put(
            FIELD_CITED_REVIEWED_SOURCE_GUIDE_IDS,
            toJsonArray(normalized.citedReviewedSourceGuideIds)
        );
        return output;
    }

    static ReviewedCardMetadata fromJsonObject(JSONObject object) {
        if (object == null) {
            return empty();
        }
        return new ReviewedCardMetadata(
            object.optString(FIELD_CARD_ID, ""),
            object.optString(FIELD_CARD_GUIDE_ID, ""),
            object.optString(FIELD_REVIEW_STATUS, ""),
            object.optString(FIELD_RUNTIME_CITATION_POLICY, ""),
            object.optString(FIELD_PROVENANCE, ""),
            fromJsonArray(object.optJSONArray(FIELD_CITED_REVIEWED_SOURCE_GUIDE_IDS))
        );
    }

    static ReviewedCardMetadata fromSerializable(Object value) {
        return value instanceof ReviewedCardMetadata
            ? normalize((ReviewedCardMetadata) value)
            : empty();
    }

    public boolean isPresent() {
        return !cardId.isEmpty()
            || !cardGuideId.isEmpty()
            || !reviewStatus.isEmpty()
            || !runtimeCitationPolicy.isEmpty()
            || !provenance.isEmpty()
            || !citedReviewedSourceGuideIds.isEmpty();
    }

    public boolean isReviewedRuntimeCardWithCitedSources() {
        return isPresent()
            && PROVENANCE_REVIEWED_CARD_RUNTIME.equals(provenance)
            && !citedReviewedSourceGuideIds.isEmpty();
    }

    public String citedSourceGuideIdsCsv() {
        return String.join(", ", citedReviewedSourceGuideIds);
    }

    private static JSONArray toJsonArray(List<String> values) throws JSONException {
        JSONArray array = new JSONArray();
        if (values == null) {
            return array;
        }
        for (String value : values) {
            array.put(safe(value).trim());
        }
        return array;
    }

    private static List<String> fromJsonArray(JSONArray array) {
        if (array == null || array.length() == 0) {
            return Collections.emptyList();
        }
        ArrayList<String> values = new ArrayList<>();
        for (int index = 0; index < array.length(); index++) {
            String value = safe(array.optString(index, "")).trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return values;
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
