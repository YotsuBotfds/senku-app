package com.senku.mobile;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.CURRENT_HEAD_ANSWER_CARD_COUNT;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.assumeCurrentHeadPack;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.databaseFile;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.manifestFile;

@RunWith(AndroidJUnit4.class)
public final class AnswerCardCurrentHeadPackCensusTest {
    private static final int CURRENT_HEAD_ANSWER_CARD_CLAUSE_COUNT = 6945;
    private static final int CURRENT_HEAD_ANSWER_CARD_SOURCE_COUNT = 311;

    @Test
    public void pushedCurrentHeadPackCensusesAnswerCardsWithoutRuntimePlanning() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        File manifestFile = manifestFile(context);
        File databaseFile = databaseFile(context);

        assumeCurrentHeadPack(manifestFile, databaseFile, "this census");

        try (SQLiteDatabase database = SQLiteDatabase.openDatabase(
            databaseFile.getAbsolutePath(),
            null,
            SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS
        )) {
            assertTableExists(database, "answer_cards");
            assertTableExists(database, "answer_card_clauses");
            assertTableExists(database, "answer_card_sources");

            assertEquals(
                "manifest answer_cards must match the installed answer_cards table",
                CURRENT_HEAD_ANSWER_CARD_COUNT,
                queryLong(database, "SELECT COUNT(*) FROM answer_cards")
            );
            assertEquals(
                "installed current-head answer_card_clauses table count should remain stable",
                CURRENT_HEAD_ANSWER_CARD_CLAUSE_COUNT,
                queryLong(database, "SELECT COUNT(*) FROM answer_card_clauses")
            );
            assertEquals(
                "installed current-head answer_card_sources table count should remain stable",
                CURRENT_HEAD_ANSWER_CARD_SOURCE_COUNT,
                queryLong(database, "SELECT COUNT(*) FROM answer_card_sources")
            );
            assertEquals(
                "pack_meta answer_card_count must match the current-head manifest",
                String.valueOf(CURRENT_HEAD_ANSWER_CARD_COUNT),
                queryString(
                    database,
                    "SELECT value FROM pack_meta WHERE key = ?",
                    new String[]{"answer_card_count"}
                )
            );
            assertEquals(
                "every answer card must have at least one clause for DAO/runtime-independent reads",
                0,
                queryLong(
                    database,
                    "SELECT COUNT(*) " +
                        "FROM answer_cards c " +
                        "WHERE NOT EXISTS (" +
                        "SELECT 1 FROM answer_card_clauses cc WHERE cc.card_id = c.card_id" +
                        ")"
                )
            );
            assertEquals(
                "every answer card must have at least one source for DAO/runtime-independent reads",
                0,
                queryLong(
                    database,
                    "SELECT COUNT(*) " +
                        "FROM answer_cards c " +
                        "WHERE NOT EXISTS (" +
                        "SELECT 1 FROM answer_card_sources s WHERE s.card_id = c.card_id" +
                        ")"
                )
            );
            assertEquals(
                "answer_card_clauses.card_id values must all reference installed answer_cards",
                0,
                queryLong(
                    database,
                    "SELECT COUNT(*) " +
                        "FROM answer_card_clauses cc " +
                        "LEFT JOIN answer_cards c ON c.card_id = cc.card_id " +
                        "WHERE c.card_id IS NULL"
                )
            );
            assertEquals(
                "answer_card_sources.card_id values must all reference installed answer_cards",
                0,
                queryLong(
                    database,
                    "SELECT COUNT(*) " +
                        "FROM answer_card_sources s " +
                        "LEFT JOIN answer_cards c ON c.card_id = s.card_id " +
                        "WHERE c.card_id IS NULL"
                )
            );
            assertEquals(
                "answer_card_sources.source_guide_id must be non-empty in the current-head pack",
                0,
                queryLong(
                    database,
                    "SELECT COUNT(*) FROM answer_card_sources " +
                        "WHERE source_guide_id IS NULL OR trim(source_guide_id) = ''"
                )
            );
            assertEquals(
                "answer_card_clauses.text must be non-empty in the current-head pack",
                0,
                queryLong(
                    database,
                    "SELECT COUNT(*) FROM answer_card_clauses " +
                        "WHERE text IS NULL OR trim(text) = ''"
                )
            );
            assertEquals(
                "every answer card must have at least one primary source in the current-head pack",
                0,
                queryLong(
                    database,
                    "SELECT COUNT(*) " +
                        "FROM answer_cards c " +
                        "WHERE NOT EXISTS (" +
                        "SELECT 1 FROM answer_card_sources s " +
                        "WHERE s.card_id = c.card_id AND s.is_primary = 1" +
                        ")"
                )
            );
            assertEquals(
                "answer cards must use an allowed review_status",
                0,
                queryLong(
                    database,
                    "SELECT COUNT(*) FROM answer_cards " +
                        "WHERE review_status NOT IN ('approved', 'pilot_reviewed')"
                )
            );
            assertNoBlankAnswerCardField(database, "card_id");
            assertNoBlankAnswerCardField(database, "guide_id");
            assertNoBlankAnswerCardField(database, "slug");
            assertNoBlankAnswerCardField(database, "title");
            assertNoBlankAnswerCardField(database, "risk_tier");
            assertNoBlankAnswerCardField(database, "evidence_owner");
            assertNoBlankAnswerCardField(database, "review_status");
            assertNoBlankAnswerCardField(database, "routine_boundary");
            assertNoBlankAnswerCardField(database, "acceptable_uncertain_fit");
        }
    }

    private static void assertTableExists(SQLiteDatabase database, String tableName) {
        assertTrue("Missing table: " + tableName, tableExists(database, tableName));
    }

    private static boolean tableExists(SQLiteDatabase database, String tableName) {
        try (Cursor cursor = database.rawQuery(
            "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ? LIMIT 1",
            new String[]{tableName}
        )) {
            return cursor.moveToFirst();
        }
    }

    private static void assertNoBlankAnswerCardField(SQLiteDatabase database, String fieldName) {
        assertEquals(
            "answer_cards." + fieldName + " must be non-empty in the current-head pack",
            0,
            queryLong(
                database,
                "SELECT COUNT(*) FROM answer_cards WHERE " +
                    fieldName +
                    " IS NULL OR trim(" +
                    fieldName +
                    ") = ''"
            )
        );
    }

    private static long queryLong(SQLiteDatabase database, String sql) {
        try (Cursor cursor = database.rawQuery(sql, null)) {
            assertTrue("Expected one row for query: " + sql, cursor.moveToFirst());
            return cursor.getLong(0);
        }
    }

    private static String queryString(SQLiteDatabase database, String sql, String[] args) {
        try (Cursor cursor = database.rawQuery(sql, args)) {
            assertTrue("Expected one row for query: " + sql, cursor.moveToFirst());
            return cursor.getString(0);
        }
    }

}
