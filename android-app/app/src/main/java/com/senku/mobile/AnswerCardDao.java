package com.senku.mobile;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class AnswerCardDao {
    private static final String TAG = "AnswerCardDao";

    private static final String ANSWER_CARDS_TABLE = "answer_cards";
    private static final String CLAUSES_TABLE = "answer_card_clauses";
    private static final String SOURCES_TABLE = "answer_card_sources";
    private static final String REVIEW_STATUS_REVIEWED = "reviewed";
    private static final String REVIEW_STATUS_PILOT_REVIEWED = "pilot_reviewed";
    private static final String REVIEW_STATUS_APPROVED = "approved";

    private static void logWarning(String message, Exception exc) {
        try {
            Log.w(TAG, message, exc);
        } catch (RuntimeException ignored) {
        }
    }

    private final SQLiteDatabase database;

    AnswerCardDao(SQLiteDatabase database) {
        this.database = database;
    }

    List<AnswerCard> loadCardsForGuideIds(Set<String> guideIds, int limit) {
        if (database == null || guideIds == null || guideIds.isEmpty() || limit <= 0) {
            return Collections.emptyList();
        }
        if (!tableExists(ANSWER_CARDS_TABLE) || !tableExists(CLAUSES_TABLE) || !tableExists(SOURCES_TABLE)) {
            return Collections.emptyList();
        }

        ArrayList<String> normalizedGuideIds = normalizedValues(guideIds);
        if (normalizedGuideIds.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            return loadCards(normalizedGuideIds, limit);
        } catch (SQLiteException e) {
            logWarning("loadCardsForGuideIds: guideIdCount=" + guideIds.size(), e);
            return Collections.emptyList();
        }
    }

    private List<AnswerCard> loadCards(List<String> guideIds, int limit) {
        String placeholders = placeholders(guideIds.size());
        String sql = "SELECT card_id, guide_id, slug, title, risk_tier, evidence_owner, " +
            "review_status, runtime_citation_policy, routine_boundary, acceptable_uncertain_fit, notes " +
            "FROM answer_cards WHERE review_status IN (?, ?, ?) AND guide_id IN (" + placeholders + ") " +
            "ORDER BY guide_id, card_id LIMIT ?";
        ArrayList<String> args = new ArrayList<>();
        args.add(REVIEW_STATUS_REVIEWED);
        args.add(REVIEW_STATUS_PILOT_REVIEWED);
        args.add(REVIEW_STATUS_APPROVED);
        args.addAll(guideIds);
        args.add(String.valueOf(limit));

        LinkedHashMap<String, AnswerCardRow> rows = new LinkedHashMap<>();
        try (Cursor cursor = database.rawQuery(sql, args.toArray(new String[0]))) {
            while (cursor.moveToNext()) {
                AnswerCardRow row = new AnswerCardRow(
                    emptySafe(cursor.getString(0)),
                    emptySafe(cursor.getString(1)),
                    emptySafe(cursor.getString(2)),
                    emptySafe(cursor.getString(3)),
                    emptySafe(cursor.getString(4)),
                    emptySafe(cursor.getString(5)),
                    emptySafe(cursor.getString(6)),
                    emptySafe(cursor.getString(7)),
                    emptySafe(cursor.getString(8)),
                    emptySafe(cursor.getString(9)),
                    emptySafe(cursor.getString(10))
                );
                if (!row.cardId.isEmpty()) {
                    rows.put(row.cardId, row);
                }
            }
        }
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<String> cardIds = new ArrayList<>(rows.keySet());
        Map<String, List<AnswerCardClause>> clausesByCard = loadClauses(cardIds);
        Map<String, List<AnswerCardSource>> sourcesByCard = loadSources(cardIds);
        ArrayList<AnswerCard> cards = new ArrayList<>();
        for (AnswerCardRow row : rows.values()) {
            cards.add(new AnswerCard(
                row.cardId,
                row.guideId,
                row.slug,
                row.title,
                row.riskTier,
                row.evidenceOwner,
                row.reviewStatus,
                row.runtimeCitationPolicy,
                row.routineBoundary,
                row.acceptableUncertainFit,
                row.notes,
                clausesByCard.get(row.cardId),
                sourcesByCard.get(row.cardId)
            ));
        }
        return Collections.unmodifiableList(cards);
    }

    private Map<String, List<AnswerCardClause>> loadClauses(List<String> cardIds) {
        String sql = "SELECT card_id, clause_kind, ordinal, text, trigger_terms_json " +
            "FROM answer_card_clauses WHERE card_id IN (" + placeholders(cardIds.size()) + ") " +
            "ORDER BY card_id, clause_kind, ordinal";
        HashMap<String, List<AnswerCardClause>> clausesByCard = new HashMap<>();
        try (Cursor cursor = database.rawQuery(sql, cardIds.toArray(new String[0]))) {
            while (cursor.moveToNext()) {
                AnswerCardClause clause = new AnswerCardClause(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getInt(2),
                    cursor.getString(3),
                    parseStringArray(cursor.getString(4))
                );
                clausesByCard.computeIfAbsent(clause.cardId, key -> new ArrayList<>()).add(clause);
            }
        }
        return immutableListMap(clausesByCard);
    }

    private Map<String, List<AnswerCardSource>> loadSources(List<String> cardIds) {
        String sql = "SELECT card_id, source_guide_id, slug, title, sections_json, is_primary " +
            "FROM answer_card_sources WHERE card_id IN (" + placeholders(cardIds.size()) + ") " +
            "ORDER BY card_id, is_primary DESC, source_guide_id";
        HashMap<String, List<AnswerCardSource>> sourcesByCard = new HashMap<>();
        try (Cursor cursor = database.rawQuery(sql, cardIds.toArray(new String[0]))) {
            while (cursor.moveToNext()) {
                AnswerCardSource source = new AnswerCardSource(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    parseStringArray(cursor.getString(4)),
                    cursor.getInt(5) != 0
                );
                sourcesByCard.computeIfAbsent(source.cardId, key -> new ArrayList<>()).add(source);
            }
        }
        return immutableListMap(sourcesByCard);
    }

    private boolean tableExists(String tableName) {
        try (Cursor cursor = database.rawQuery(
            "SELECT 1 FROM sqlite_master WHERE type='table' AND name=? LIMIT 1",
            new String[]{tableName}
        )) {
            return cursor.moveToFirst();
        } catch (SQLiteException e) {
            logWarning("tableExists", e);
            return false;
        }
    }

    private static ArrayList<String> normalizedValues(Set<String> values) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            String trimmed = emptySafe(value).trim();
            if (!trimmed.isEmpty()) {
                normalized.add(trimmed);
            }
        }
        return new ArrayList<>(normalized);
    }

    private static Map<String, List<AnswerCardClause>> immutableListMap(
        Map<String, List<AnswerCardClause>> values
    ) {
        HashMap<String, List<AnswerCardClause>> copied = new HashMap<>();
        for (Map.Entry<String, List<AnswerCardClause>> entry : values.entrySet()) {
            copied.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        return copied;
    }

    private static Map<String, List<AnswerCardSource>> immutableListMap(
        HashMap<String, List<AnswerCardSource>> values
    ) {
        HashMap<String, List<AnswerCardSource>> copied = new HashMap<>();
        for (Map.Entry<String, List<AnswerCardSource>> entry : values.entrySet()) {
            copied.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        return copied;
    }

    private static List<String> parseStringArray(String jsonText) {
        String trimmed = emptySafe(jsonText).trim();
        if (trimmed.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            JSONArray array = new JSONArray(trimmed);
            ArrayList<String> values = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                String value = emptySafe(array.optString(i, "")).trim();
                if (!value.isEmpty()) {
                    values.add(value);
                }
            }
            return values.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(values);
        } catch (JSONException e) {
            logWarning("parseStringArray", e);
            return Collections.emptyList();
        }
    }

    private static String placeholders(int count) {
        ArrayList<String> placeholders = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            placeholders.add("?");
        }
        return String.join(",", placeholders);
    }

    private static String emptySafe(String text) {
        return text == null ? "" : text;
    }

    private static final class AnswerCardRow {
        final String cardId;
        final String guideId;
        final String slug;
        final String title;
        final String riskTier;
        final String evidenceOwner;
        final String reviewStatus;
        final String runtimeCitationPolicy;
        final String routineBoundary;
        final String acceptableUncertainFit;
        final String notes;

        AnswerCardRow(
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
            String notes
        ) {
            this.cardId = cardId;
            this.guideId = guideId;
            this.slug = slug;
            this.title = title;
            this.riskTier = riskTier;
            this.evidenceOwner = evidenceOwner;
            this.reviewStatus = reviewStatus;
            this.runtimeCitationPolicy = runtimeCitationPolicy;
            this.routineBoundary = routineBoundary;
            this.acceptableUncertainFit = acceptableUncertainFit;
            this.notes = notes;
        }
    }
}
