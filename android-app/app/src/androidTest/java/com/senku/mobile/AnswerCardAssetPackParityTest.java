package com.senku.mobile;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public final class AnswerCardAssetPackParityTest {
    private static final int CURRENT_HEAD_ANSWER_CARD_COUNT = 271;
    private static final int CURRENT_HEAD_ANSWER_CARD_CLAUSE_COUNT = 6945;
    private static final int CURRENT_HEAD_ANSWER_CARD_SOURCE_COUNT = 311;
    private static final String PACK_DIR = "mobile_pack";
    private static final String MANIFEST_ASSET = PACK_DIR + "/senku_manifest.json";
    private static final String SQLITE_ASSET = PACK_DIR + "/senku_mobile.sqlite3";

    private File copiedDatabaseFile;

    @After
    public void tearDown() {
        if (copiedDatabaseFile != null) {
            copiedDatabaseFile.delete();
        }
    }

    @Test
    public void shippedAssetPackHasCurrentHeadReviewedAnswerCardTablesForProductGate() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();

        PackManifest manifest = PackManifest.fromJson(readAssetText(context, MANIFEST_ASSET));
        assertEquals(CURRENT_HEAD_ANSWER_CARD_COUNT, manifest.answerCardCount);

        copiedDatabaseFile = copyAssetToCache(context, SQLITE_ASSET, "asset-pack-answer-cards", ".sqlite3");
        try (SQLiteDatabase database = SQLiteDatabase.openDatabase(
            copiedDatabaseFile.getAbsolutePath(),
            null,
            SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS
        )) {
            assertTableExists(database, "answer_cards");
            assertTableExists(database, "answer_card_clauses");
            assertTableExists(database, "answer_card_sources");

            assertEquals(CURRENT_HEAD_ANSWER_CARD_COUNT, queryLong(database, "SELECT COUNT(*) FROM answer_cards"));
            assertEquals(CURRENT_HEAD_ANSWER_CARD_CLAUSE_COUNT, queryLong(database, "SELECT COUNT(*) FROM answer_card_clauses"));
            assertEquals(CURRENT_HEAD_ANSWER_CARD_SOURCE_COUNT, queryLong(database, "SELECT COUNT(*) FROM answer_card_sources"));

            if (tableExists(database, "pack_meta")) {
                assertEquals(
                    String.valueOf(CURRENT_HEAD_ANSWER_CARD_COUNT),
                    queryString(
                        database,
                        "SELECT value FROM pack_meta WHERE key = ?",
                        new String[]{"answer_card_count"}
                    )
                );
            }

            Set<String> expectedCardIds = new HashSet<>(Arrays.asList(
                "poisoning_unknown_ingestion",
                "newborn_danger_sepsis",
                "choking_airway_obstruction",
                "meningitis_sepsis_child",
                "infected_wound_spreading_infection",
                "abdominal_internal_bleeding"
            ));
            Set<String> actualCardIds = queryStringSet(database, "SELECT card_id FROM answer_cards");
            assertTrue(actualCardIds.containsAll(expectedCardIds));

            assertEquals(
                0,
                queryLong(
                    database,
                    "SELECT COUNT(*) FROM answer_cards WHERE review_status <> 'pilot_reviewed'"
                )
            );
            assertEquals(
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
        }
    }

    private static String readAssetText(Context context, String assetName) throws IOException {
        return new String(readAssetBytes(context, assetName), StandardCharsets.UTF_8);
    }

    private static byte[] readAssetBytes(Context context, String assetName) throws IOException {
        try (
            InputStream input = context.getAssets().open(assetName);
            ByteArrayOutputStream output = new ByteArrayOutputStream()
        ) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }

    private static File copyAssetToCache(
        Context context,
        String assetName,
        String prefix,
        String suffix
    ) throws IOException {
        File file = File.createTempFile(prefix, suffix, context.getCacheDir());
        try (
            InputStream input = context.getAssets().open(assetName);
            FileOutputStream output = new FileOutputStream(file)
        ) {
            byte[] buffer = new byte[1024 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        }
        return file;
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

    private static Set<String> queryStringSet(SQLiteDatabase database, String sql) {
        Set<String> values = new HashSet<>();
        try (Cursor cursor = database.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                values.add(cursor.getString(0));
            }
        }
        return values;
    }
}
