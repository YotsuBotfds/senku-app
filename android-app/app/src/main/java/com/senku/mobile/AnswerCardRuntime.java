package com.senku.mobile;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class AnswerCardRuntime {
    private static final String TAG = "AnswerCardRuntime";
    private static final Locale QUERY_LOCALE = Locale.US;
    private static final String STRUCTURE_TYPE_SAFETY_POISONING = "safety_poisoning";
    private static final String STRUCTURE_TYPE_SAFETY_NEWBORN_DANGER = "safety_newborn_danger";
    private static final String STRUCTURE_TYPE_SAFETY_AIRWAY_OBSTRUCTION = "safety_airway_obstruction";
    private static final String STRUCTURE_TYPE_SAFETY_MENINGITIS_SEPSIS = "safety_meningitis_sepsis";
    private static final String STRUCTURE_TYPE_SAFETY_INFECTED_WOUND = "safety_infected_wound";
    private static final String STRUCTURE_TYPE_SAFETY_INTERNAL_BLEEDING = "safety_internal_bleeding";
    private static final String STRUCTURE_TYPE_FOUNDRY_AREA_READINESS = "foundry_area_readiness";
    private static final String POISONING_UNKNOWN_INGESTION_CARD_ID = "poisoning_unknown_ingestion";
    private static final String POISONING_UNKNOWN_INGESTION_GUIDE_ID = "GD-898";
    private static final String NEWBORN_DANGER_SEPSIS_CARD_ID = "newborn_danger_sepsis";
    private static final String NEWBORN_DANGER_SEPSIS_GUIDE_ID = "GD-284";
    private static final String CHOKING_AIRWAY_OBSTRUCTION_CARD_ID = "choking_airway_obstruction";
    private static final String CHOKING_AIRWAY_OBSTRUCTION_GUIDE_ID = "GD-232";
    private static final String MENINGITIS_SEPSIS_CHILD_CARD_ID = "meningitis_sepsis_child";
    private static final String MENINGITIS_SEPSIS_CHILD_GUIDE_ID = "GD-589";
    private static final String INFECTED_WOUND_SPREADING_INFECTION_CARD_ID = "infected_wound_spreading_infection";
    private static final String INFECTED_WOUND_SPREADING_INFECTION_GUIDE_ID = "GD-585";
    private static final String ABDOMINAL_INTERNAL_BLEEDING_CARD_ID = "abdominal_internal_bleeding";
    private static final String ABDOMINAL_INTERNAL_BLEEDING_GUIDE_ID = "GD-380";
    private static final String FOUNDRY_CASTING_AREA_READINESS_CARD_ID = "foundry_casting_area_readiness_boundary";
    private static final String FOUNDRY_CASTING_AREA_READINESS_GUIDE_ID = "GD-132";
    private static final Set<String> ANSWER_CARD_ALLOWED_REVIEW_STATUSES = buildSet(
        "reviewed",
        "pilot_reviewed",
        "approved"
    );
    private static final CardSpec POISONING_UNKNOWN_INGESTION_SPEC = new CardSpec(
        POISONING_UNKNOWN_INGESTION_CARD_ID,
        POISONING_UNKNOWN_INGESTION_GUIDE_ID,
        STRUCTURE_TYPE_SAFETY_POISONING,
        ReviewedCardPredicatePolicy::isPoisoningAnswerCardPilotQuery
    );
    private static final CardSpec NEWBORN_DANGER_SEPSIS_SPEC = new CardSpec(
        NEWBORN_DANGER_SEPSIS_CARD_ID,
        NEWBORN_DANGER_SEPSIS_GUIDE_ID,
        STRUCTURE_TYPE_SAFETY_NEWBORN_DANGER,
        ReviewedCardPredicatePolicy::isNewbornDangerSepsisAnswerCardQuery
    );
    private static final CardSpec CHOKING_AIRWAY_OBSTRUCTION_SPEC = new CardSpec(
        CHOKING_AIRWAY_OBSTRUCTION_CARD_ID,
        CHOKING_AIRWAY_OBSTRUCTION_GUIDE_ID,
        STRUCTURE_TYPE_SAFETY_AIRWAY_OBSTRUCTION,
        ReviewedCardPredicatePolicy::isChokingAirwayObstructionAnswerCardQuery
    );
    private static final CardSpec MENINGITIS_SEPSIS_CHILD_SPEC = new CardSpec(
        MENINGITIS_SEPSIS_CHILD_CARD_ID,
        MENINGITIS_SEPSIS_CHILD_GUIDE_ID,
        STRUCTURE_TYPE_SAFETY_MENINGITIS_SEPSIS,
        ReviewedCardPredicatePolicy::isMeningitisSepsisChildAnswerCardQuery
    );
    private static final CardSpec INFECTED_WOUND_SPREADING_INFECTION_SPEC = new CardSpec(
        INFECTED_WOUND_SPREADING_INFECTION_CARD_ID,
        INFECTED_WOUND_SPREADING_INFECTION_GUIDE_ID,
        STRUCTURE_TYPE_SAFETY_INFECTED_WOUND,
        ReviewedCardPredicatePolicy::isInfectedWoundSpreadingInfectionAnswerCardQuery
    );
    private static final CardSpec ABDOMINAL_INTERNAL_BLEEDING_SPEC = new CardSpec(
        ABDOMINAL_INTERNAL_BLEEDING_CARD_ID,
        ABDOMINAL_INTERNAL_BLEEDING_GUIDE_ID,
        STRUCTURE_TYPE_SAFETY_INTERNAL_BLEEDING,
        ReviewedCardPredicatePolicy::isAbdominalInternalBleedingAnswerCardQuery
    );
    private static final CardSpec FOUNDRY_CASTING_AREA_READINESS_SPEC = new CardSpec(
        FOUNDRY_CASTING_AREA_READINESS_CARD_ID,
        FOUNDRY_CASTING_AREA_READINESS_GUIDE_ID,
        STRUCTURE_TYPE_FOUNDRY_AREA_READINESS,
        ReviewedCardPredicatePolicy::isFoundryCastingAreaReadinessAnswerCardQuery
    );
    private static final CardSpec[] CARD_SPECS = {
        POISONING_UNKNOWN_INGESTION_SPEC,
        NEWBORN_DANGER_SEPSIS_SPEC,
        CHOKING_AIRWAY_OBSTRUCTION_SPEC,
        MENINGITIS_SEPSIS_CHILD_SPEC,
        INFECTED_WOUND_SPREADING_INFECTION_SPEC,
        ABDOMINAL_INTERNAL_BLEEDING_SPEC,
        FOUNDRY_CASTING_AREA_READINESS_SPEC
    };
    private static volatile Boolean enabledForTest;

    private AnswerCardRuntime() {
    }

    static AnswerPlan tryPlan(
        Context context,
        PackRepository repository,
        String query
    ) {
        if (!isEnabled(context) || repository == null) {
            return null;
        }
        try {
            for (CardSpec spec : CARD_SPECS) {
                if (!spec.matches(query)) {
                    continue;
                }
                List<AnswerCard> cards = repository.loadAnswerCardsForGuideIds(
                    Collections.singleton(spec.guideId),
                    2
                );
                return planAnswerCardFromCards(query, cards, spec);
            }
            return null;
        } catch (RuntimeException e) {
            logRuntimeWarning("AnswerCardRuntime.tryPlan failed; reviewed-card runtime will fall back", e);
            return null;
        }
    }

    static boolean supportsQuery(String query) {
        for (CardSpec spec : CARD_SPECS) {
            if (spec.matches(query)) {
                return true;
            }
        }
        return false;
    }

    static AnswerPlan planPoisoningAnswerCardFromCardsForTest(String query, List<AnswerCard> cards) {
        return planAnswerCardFromCards(query, cards, POISONING_UNKNOWN_INGESTION_SPEC);
    }

    static AnswerPlan planNewbornDangerSepsisAnswerCardFromCardsForTest(String query, List<AnswerCard> cards) {
        return planAnswerCardFromCards(query, cards, NEWBORN_DANGER_SEPSIS_SPEC);
    }

    static AnswerPlan planChokingAirwayObstructionAnswerCardFromCardsForTest(String query, List<AnswerCard> cards) {
        return planAnswerCardFromCards(query, cards, CHOKING_AIRWAY_OBSTRUCTION_SPEC);
    }

    static AnswerPlan planMeningitisSepsisChildAnswerCardFromCardsForTest(String query, List<AnswerCard> cards) {
        return planAnswerCardFromCards(query, cards, MENINGITIS_SEPSIS_CHILD_SPEC);
    }

    static AnswerPlan planInfectedWoundSpreadingInfectionAnswerCardFromCardsForTest(String query, List<AnswerCard> cards) {
        return planAnswerCardFromCards(query, cards, INFECTED_WOUND_SPREADING_INFECTION_SPEC);
    }

    static AnswerPlan planAbdominalInternalBleedingAnswerCardFromCardsForTest(String query, List<AnswerCard> cards) {
        return planAnswerCardFromCards(query, cards, ABDOMINAL_INTERNAL_BLEEDING_SPEC);
    }

    static AnswerPlan planFoundryCastingAreaReadinessAnswerCardFromCardsForTest(String query, List<AnswerCard> cards) {
        return planAnswerCardFromCards(query, cards, FOUNDRY_CASTING_AREA_READINESS_SPEC);
    }

    static boolean supportsQueryForTest(String query) {
        return supportsQuery(query);
    }

    static boolean isEnabledForTest(Context context) {
        return isEnabled(context);
    }

    static void setEnabledForTest(boolean enabled) {
        enabledForTest = enabled;
    }

    static void resetEnabledForTest() {
        enabledForTest = null;
    }

    private static AnswerPlan planAnswerCardFromCards(String query, List<AnswerCard> cards, CardSpec spec) {
        if (spec == null || !spec.matches(query) || cards == null || cards.size() != 1) {
            return null;
        }
        AnswerCard card = cards.get(0);
        if (!isEligibleAnswerCard(card, spec.cardId, spec.guideId)) {
            return null;
        }
        List<SearchResult> sources = searchResultsForAnswerCard(card, spec.structureType);
        if (sources.isEmpty()) {
            return null;
        }
        String answerText = buildReviewedAnswerCardText(query, card);
        if (answerText.isEmpty()) {
            return null;
        }
        return new AnswerPlan(
            answerText,
            sources,
            ReviewedCardMetadata.answerCardRuleId(card.cardId),
            ReviewedCardMetadata.fromAnswerCard(card)
        );
    }

    private static boolean isEnabled(Context context) {
        Boolean override = enabledForTest;
        if (override != null) {
            return override;
        }
        if (context == null) {
            return false;
        }
        return ReviewedCardRuntimeConfig.isEnabled(context);
    }

    private static boolean isEligibleAnswerCard(AnswerCard card, String expectedCardId, String expectedGuideId) {
        if (card == null) {
            return false;
        }
        String cardId = safe(card.cardId).trim();
        String guideId = safe(card.guideId).trim();
        String riskTier = safe(card.riskTier).trim().toLowerCase(QUERY_LOCALE);
        String reviewStatus = safe(card.reviewStatus).trim().toLowerCase(QUERY_LOCALE);
        return safe(expectedCardId).equals(cardId)
            && safe(expectedGuideId).equals(guideId)
            && ("critical".equals(riskTier) || "high".equals(riskTier))
            && ANSWER_CARD_ALLOWED_REVIEW_STATUSES.contains(reviewStatus);
    }

    private static String buildReviewedAnswerCardText(String query, AnswerCard card) {
        ArrayList<String> lines = new ArrayList<>();
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        appendClauseTexts(lines, seen, clauseTexts(card, "required_first_action"), 0, "");
        appendClauseTexts(lines, seen, activeConditionalClauseTexts(query, card), 0, "");
        appendClauseTexts(lines, seen, clauseTexts(card, "first_action"), 4, "");
        appendClauseTexts(lines, seen, clauseTexts(card, "urgent_red_flag"), 3, "Escalate now if ");
        appendClauseTexts(lines, seen, clauseTexts(card, "forbidden_advice"), 4, "Avoid: ");
        appendClauseTexts(lines, seen, clauseTexts(card, "do_not"), 2, "Avoid: ");
        return String.join("\n", lines).trim();
    }

    private static List<String> clauseTexts(AnswerCard card, String kind) {
        if (card == null || card.clauses.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<String> values = new ArrayList<>();
        for (AnswerCardClause clause : card.clauses) {
            if (clause != null && kind.equals(safe(clause.clauseKind))) {
                String text = safe(clause.text).trim();
                if (!text.isEmpty()) {
                    values.add(text);
                }
            }
        }
        return values;
    }

    private static List<String> activeConditionalClauseTexts(String query, AnswerCard card) {
        if (card == null || card.clauses.isEmpty()) {
            return Collections.emptyList();
        }
        String normalizedQuery = normalizeQueryText(query);
        ArrayList<String> values = new ArrayList<>();
        for (AnswerCardClause clause : card.clauses) {
            if (clause == null || !"conditional_required_action".equals(safe(clause.clauseKind))) {
                continue;
            }
            if (clause.triggerTerms.isEmpty() || !containsAny(normalizedQuery, new LinkedHashSet<>(clause.triggerTerms))) {
                continue;
            }
            String text = safe(clause.text).trim();
            if (!text.isEmpty()) {
                values.add(text);
            }
        }
        return values;
    }

    private static void appendClauseTexts(
        List<String> output,
        Set<String> seen,
        List<String> texts,
        int maxItems,
        String prefix
    ) {
        int added = 0;
        for (String text : texts) {
            if (maxItems > 0 && added >= maxItems) {
                return;
            }
            String line = (safe(prefix) + safe(text)).trim();
            String key = line.toLowerCase(QUERY_LOCALE);
            if (line.isEmpty() || seen.contains(key)) {
                continue;
            }
            seen.add(key);
            output.add((output.size() + 1) + ". " + line);
            added++;
        }
    }

    private static List<SearchResult> searchResultsForAnswerCard(AnswerCard card, String structureType) {
        if (card == null || card.sources.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<SearchResult> results = new ArrayList<>();
        for (AnswerCardSource source : card.sources) {
            if (source == null) {
                continue;
            }
            String guideId = safe(source.sourceGuideId).trim();
            if (guideId.isEmpty()) {
                continue;
            }
            String sectionHeading = source.sections.isEmpty()
                ? safe(source.title).trim()
                : source.sections.get(0);
            String title = safe(source.title).trim().isEmpty()
                ? guideId
                : safe(source.title).trim();
            String snippet = source.sections.isEmpty()
                ? "Reviewed answer card source."
                : String.join(", ", source.sections);
            results.add(new SearchResult(
                title,
                guideId,
                snippet,
                snippet,
                guideId,
                sectionHeading,
                "medical",
                "answer-card",
                "safety",
                "immediate",
                safe(structureType),
                safe(card.slug)
            ));
        }
        return Collections.unmodifiableList(results);
    }

    private static boolean containsAny(String text, Set<String> markers) {
        String normalized = safe(text).trim().toLowerCase(QUERY_LOCALE);
        if (normalized.isEmpty() || markers == null || markers.isEmpty()) {
            return false;
        }
        for (String marker : markers) {
            String normalizedMarker = safe(marker).trim().toLowerCase(QUERY_LOCALE);
            if (!normalizedMarker.isEmpty() && normalized.contains(normalizedMarker)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeQueryText(String text) {
        return safe(text).trim().toLowerCase(QUERY_LOCALE).replace('-', ' ');
    }

    private static Set<String> buildSet(String... values) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        Collections.addAll(set, values);
        return Collections.unmodifiableSet(set);
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }

    private static void logRuntimeWarning(String message, RuntimeException exc) {
        try {
            Log.w(TAG, message, exc);
        } catch (RuntimeException ignored) {
        }
    }

    private interface QueryPredicate {
        boolean matches(String query);
    }

    private static final class CardSpec {
        final String cardId;
        final String guideId;
        final String structureType;
        final QueryPredicate predicate;

        CardSpec(String cardId, String guideId, String structureType, QueryPredicate predicate) {
            this.cardId = safe(cardId).trim();
            this.guideId = safe(guideId).trim();
            this.structureType = safe(structureType).trim();
            this.predicate = predicate;
        }

        boolean matches(String query) {
            return predicate != null && predicate.matches(query);
        }
    }

    static final class AnswerPlan {
        final String answerText;
        final List<SearchResult> sources;
        final String ruleId;
        final ReviewedCardMetadata reviewedCardMetadata;

        AnswerPlan(String answerText, List<SearchResult> sources, String ruleId) {
            this(answerText, sources, ruleId, ReviewedCardMetadata.empty());
        }

        AnswerPlan(
            String answerText,
            List<SearchResult> sources,
            String ruleId,
            ReviewedCardMetadata reviewedCardMetadata
        ) {
            this.answerText = safe(answerText).trim();
            this.sources = sources == null ? Collections.emptyList() : new ArrayList<>(sources);
            this.ruleId = safe(ruleId).trim();
            this.reviewedCardMetadata = ReviewedCardMetadata.normalize(reviewedCardMetadata);
        }
    }
}
