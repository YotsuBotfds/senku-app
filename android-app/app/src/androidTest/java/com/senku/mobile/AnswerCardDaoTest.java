package com.senku.mobile;

import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public final class AnswerCardDaoTest {
    private SQLiteDatabase database;

    @After
    public void tearDown() {
        if (database != null) {
            database.close();
        }
    }

    @Test
    public void loadCardsForGuideIdsReturnsEmptyWhenOptionalTablesAreAbsent() {
        database = SQLiteDatabase.create(null);
        AnswerCardDao dao = new AnswerCardDao(database);

        List<AnswerCard> cards = dao.loadCardsForGuideIds(setOf("GD-001"), 5);

        assertTrue(cards.isEmpty());
    }

    @Test
    public void loadCardsForGuideIdsReturnsEmptyForPartialOldPackTables() {
        database = SQLiteDatabase.create(null);
        createAnswerCardsTable(database);
        insertReviewedCard(database, "poisoning_unknown_ingestion", "GD-001");

        AnswerCardDao dao = new AnswerCardDao(database);

        assertTrue(dao.loadCardsForGuideIds(setOf("GD-001"), 5).isEmpty());

        createClausesTable(database);
        assertTrue(dao.loadCardsForGuideIds(setOf("GD-001"), 5).isEmpty());
    }

    @Test
    public void loadCardsForGuideIdsLoadsReviewedCardsClausesAndSources() {
        database = SQLiteDatabase.create(null);
        createAnswerCardTables(database);
        database.execSQL(
            "INSERT INTO answer_cards (" +
                "card_id, guide_id, slug, title, risk_tier, evidence_owner, " +
                "review_status, runtime_citation_policy, routine_boundary, " +
                "acceptable_uncertain_fit, notes" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            new Object[]{
                "poisoning_unknown_ingestion",
                "GD-001",
                "poisoning",
                "Unknown ingestion",
                "critical",
                "guide-corpus",
                "pilot_reviewed",
                "reviewed_source_family",
                "Escalate if symptoms are severe.",
                "Say if substance is unknown.",
                "Synthetic card."
            }
        );
        database.execSQL(
            "INSERT INTO answer_cards (" +
                "card_id, guide_id, slug, title, risk_tier, evidence_owner, " +
                "review_status, runtime_citation_policy, routine_boundary, " +
                "acceptable_uncertain_fit, notes" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            new Object[]{
                "draft_card",
                "GD-001",
                "draft",
                "Draft",
                "high",
                "guide-corpus",
                "draft",
                "",
                "",
                "",
                ""
            }
        );
        database.execSQL(
            "INSERT INTO answer_card_clauses (" +
                "card_id, clause_kind, ordinal, text, trigger_terms_json" +
                ") VALUES (?, ?, ?, ?, ?)",
            new Object[]{
                "poisoning_unknown_ingestion",
                "conditional_required_action",
                1,
                "Keep the child with an adult.",
                "[\"child\",\"kid\"]"
            }
        );
        database.execSQL(
            "INSERT INTO answer_card_sources (" +
                "card_id, source_guide_id, slug, title, sections_json, is_primary" +
                ") VALUES (?, ?, ?, ?, ?, ?)",
            new Object[]{
                "poisoning_unknown_ingestion",
                "GD-000",
                "backup",
                "Backup",
                "[\"Reference\"]",
                0
            }
        );
        database.execSQL(
            "INSERT INTO answer_card_sources (" +
                "card_id, source_guide_id, slug, title, sections_json, is_primary" +
                ") VALUES (?, ?, ?, ?, ?, ?)",
            new Object[]{
                "poisoning_unknown_ingestion",
                "GD-001",
                "poisoning",
                "Poisoning",
                "[\"Triage\"]",
                1
            }
        );

        AnswerCardDao dao = new AnswerCardDao(database);

        List<AnswerCard> cards = dao.loadCardsForGuideIds(setOf("GD-001"), 10);

        assertEquals(1, cards.size());
        AnswerCard card = cards.get(0);
        assertEquals("poisoning_unknown_ingestion", card.cardId);
        assertEquals("GD-001", card.guideId);
        assertEquals("pilot_reviewed", card.reviewStatus);
        assertEquals("reviewed_source_family", card.runtimeCitationPolicy);
        assertEquals(1, card.clauses.size());
        assertEquals("conditional_required_action", card.clauses.get(0).clauseKind);
        assertEquals(Arrays.asList("child", "kid"), card.clauses.get(0).triggerTerms);
        assertEquals(2, card.sources.size());
        assertEquals("GD-001", card.sources.get(0).sourceGuideId);
        assertEquals(Collections.singletonList("Triage"), card.sources.get(0).sections);
        assertTrue(card.sources.get(0).primary);
    }

    @Test
    public void loadCardsForGuideIdsTreatsMalformedJsonAsEmptyLists() {
        database = SQLiteDatabase.create(null);
        createAnswerCardTables(database);
        insertReviewedCard(database, "poisoning_unknown_ingestion", "GD-001");
        database.execSQL(
            "INSERT INTO answer_card_clauses (" +
                "card_id, clause_kind, ordinal, text, trigger_terms_json" +
                ") VALUES (?, ?, ?, ?, ?)",
            new Object[]{
                "poisoning_unknown_ingestion",
                "conditional_required_action",
                1,
                "Keep the child with an adult.",
                "[not-json"
            }
        );
        database.execSQL(
            "INSERT INTO answer_card_sources (" +
                "card_id, source_guide_id, slug, title, sections_json, is_primary" +
                ") VALUES (?, ?, ?, ?, ?, ?)",
            new Object[]{
                "poisoning_unknown_ingestion",
                "GD-001",
                "poisoning",
                "Poisoning",
                "{not-json",
                1
            }
        );

        AnswerCardDao dao = new AnswerCardDao(database);

        List<AnswerCard> cards = dao.loadCardsForGuideIds(setOf("GD-001"), 5);

        assertEquals(1, cards.size());
        assertTrue(cards.get(0).clauses.get(0).triggerTerms.isEmpty());
        assertTrue(cards.get(0).sources.get(0).sections.isEmpty());
    }

    @Test
    public void loadCardsForGuideIdsRespectsLimitAndStableOrdering() {
        database = SQLiteDatabase.create(null);
        createAnswerCardTables(database);
        insertReviewedCard(database, "card_b", "GD-002");
        insertReviewedCard(database, "card_a", "GD-001");

        AnswerCardDao dao = new AnswerCardDao(database);

        List<AnswerCard> cards = dao.loadCardsForGuideIds(setOf("GD-002", "GD-001"), 1);

        assertEquals(1, cards.size());
        assertEquals("card_a", cards.get(0).cardId);
    }

    private static void createAnswerCardTables(SQLiteDatabase database) {
        createAnswerCardsTable(database);
        createClausesTable(database);
        createSourcesTable(database);
    }

    private static void createAnswerCardsTable(SQLiteDatabase database) {
        database.execSQL(
            "CREATE TABLE answer_cards (" +
                "card_id TEXT PRIMARY KEY, " +
                "guide_id TEXT NOT NULL, " +
                "slug TEXT, " +
                "title TEXT, " +
                "risk_tier TEXT, " +
                "evidence_owner TEXT, " +
                "review_status TEXT, " +
                "runtime_citation_policy TEXT, " +
                "routine_boundary TEXT, " +
                "acceptable_uncertain_fit TEXT, " +
                "notes TEXT" +
                ")"
        );
    }

    private static void createClausesTable(SQLiteDatabase database) {
        database.execSQL(
            "CREATE TABLE answer_card_clauses (" +
                "card_id TEXT NOT NULL, " +
                "clause_kind TEXT NOT NULL, " +
                "ordinal INTEGER NOT NULL, " +
                "text TEXT NOT NULL, " +
                "trigger_terms_json TEXT, " +
                "PRIMARY KEY (card_id, clause_kind, ordinal)" +
                ")"
        );
    }

    private static void createSourcesTable(SQLiteDatabase database) {
        database.execSQL(
            "CREATE TABLE answer_card_sources (" +
                "card_id TEXT NOT NULL, " +
                "source_guide_id TEXT NOT NULL, " +
                "slug TEXT, " +
                "title TEXT, " +
                "sections_json TEXT NOT NULL, " +
                "is_primary INTEGER NOT NULL, " +
                "PRIMARY KEY (card_id, source_guide_id)" +
                ")"
        );
    }

    private static void insertReviewedCard(SQLiteDatabase database, String cardId, String guideId) {
        database.execSQL(
            "INSERT INTO answer_cards (" +
                "card_id, guide_id, slug, title, risk_tier, evidence_owner, " +
                "review_status, runtime_citation_policy, routine_boundary, " +
                "acceptable_uncertain_fit, notes" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            new Object[]{
                cardId,
                guideId,
                "",
                "",
                "high",
                "guide-corpus",
                "reviewed",
                "",
                "",
                "",
                ""
            }
        );
    }

    private static Set<String> setOf(String... values) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        Collections.addAll(set, values);
        return set;
    }
}
