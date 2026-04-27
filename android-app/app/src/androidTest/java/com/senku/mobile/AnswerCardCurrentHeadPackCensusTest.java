package com.senku.mobile;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(AndroidJUnit4.class)
public final class AnswerCardCurrentHeadPackCensusTest {
    private static final int CURRENT_HEAD_ANSWER_CARD_COUNT = 271;
    private static final int CURRENT_HEAD_ANSWER_CARD_CLAUSE_COUNT = 6945;
    private static final int CURRENT_HEAD_ANSWER_CARD_SOURCE_COUNT = 311;

    @Test
    public void pushedCurrentHeadPackCensusesAnswerCardsWithoutRuntimePlanning() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        File packRoot = new File(context.getFilesDir(), "mobile_pack");
        File manifestFile = new File(packRoot, "senku_manifest.json");
        File databaseFile = new File(packRoot, "senku_mobile.sqlite3");

        assumeTrue("installed mobile pack manifest is absent; push current-head pack before running this census", manifestFile.isFile());
        assumeTrue("installed mobile pack database is absent; push current-head pack before running this census", databaseFile.isFile());

        PackManifest manifest = PackManifest.fromJson(readFileText(manifestFile));

        assumeTrue(
            "installed mobile pack is not the current-head 271-answer-card pack; found manifest answer_cards="
                + manifest.answerCardCount
                + " generated_at="
                + manifest.generatedAt,
            manifest.answerCardCount == CURRENT_HEAD_ANSWER_CARD_COUNT
        );

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

    private static String readFileText(File file) throws Exception {
        try (FileInputStream input = new FileInputStream(file)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
