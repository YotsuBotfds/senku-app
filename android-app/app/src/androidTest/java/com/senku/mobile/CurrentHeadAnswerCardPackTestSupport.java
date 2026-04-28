package com.senku.mobile;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assume.assumeTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

final class CurrentHeadAnswerCardPackTestSupport {
    static final int CURRENT_HEAD_ANSWER_CARD_COUNT = 271;
    static final String PACK_DIR = "mobile_pack";
    static final String MANIFEST_NAME = "senku_manifest.json";
    static final String DATABASE_NAME = "senku_mobile.sqlite3";
    static final int NON_PILOT_SAMPLE_SIZE = 24;
    static final int NON_PILOT_BUCKET_SIZE = 8;
    static final Set<String> PILOT_CARD_IDS = new LinkedHashSet<>(Arrays.asList(
        "poisoning_unknown_ingestion",
        "newborn_danger_sepsis",
        "choking_airway_obstruction",
        "meningitis_sepsis_child",
        "infected_wound_spreading_infection",
        "abdominal_internal_bleeding",
        "foundry_casting_area_readiness_boundary"
    ));

    private CurrentHeadAnswerCardPackTestSupport() {
    }

    static File manifestFile(Context context) {
        return new File(packRoot(context), MANIFEST_NAME);
    }

    static File databaseFile(Context context) {
        return new File(packRoot(context), DATABASE_NAME);
    }

    static String readFileText(File file) throws Exception {
        try (FileInputStream input = new FileInputStream(file)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    static void assertTableExists(SQLiteDatabase database, String tableName) {
        assertTrue("Missing table: " + tableName, tableExists(database, tableName));
    }

    static boolean tableExists(SQLiteDatabase database, String tableName) {
        try (Cursor cursor = database.rawQuery(
            "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ? LIMIT 1",
            new String[]{tableName}
        )) {
            return cursor.moveToFirst();
        }
    }

    static long queryLong(SQLiteDatabase database, String sql) {
        try (Cursor cursor = database.rawQuery(sql, null)) {
            assertTrue("Expected one row for query: " + sql, cursor.moveToFirst());
            return cursor.getLong(0);
        }
    }

    static String queryString(SQLiteDatabase database, String sql, String[] args) {
        try (Cursor cursor = database.rawQuery(sql, args)) {
            assertTrue("Expected one row for query: " + sql, cursor.moveToFirst());
            return cursor.getString(0);
        }
    }

    static List<SampledCard> sampledNonPilotCards(SQLiteDatabase database) {
        LinkedHashSet<SampledCard> sampledCards = new LinkedHashSet<>();
        addSampledCards(
            sampledCards,
            querySampledCards(
                database,
                "SELECT card_id, guide_id, risk_tier FROM answer_cards " +
                    "WHERE card_id NOT IN (?, ?, ?, ?, ?, ?, ?) " +
                    "ORDER BY card_id LIMIT ?",
                NON_PILOT_BUCKET_SIZE
            )
        );
        addSampledCards(
            sampledCards,
            querySampledCards(
                database,
                "SELECT card_id, guide_id, risk_tier FROM answer_cards " +
                    "WHERE card_id NOT IN (?, ?, ?, ?, ?, ?, ?) " +
                    "ORDER BY card_id DESC LIMIT ?",
                NON_PILOT_BUCKET_SIZE
            )
        );
        addSampledCards(
            sampledCards,
            querySampledCards(
                database,
                "SELECT card_id, guide_id, risk_tier FROM answer_cards " +
                    "WHERE card_id NOT IN (?, ?, ?, ?, ?, ?, ?) " +
                    "AND risk_tier IN ('critical', 'high') " +
                    "ORDER BY CASE risk_tier WHEN 'critical' THEN 0 ELSE 1 END, card_id LIMIT ?",
                NON_PILOT_BUCKET_SIZE
            )
        );
        if (sampledCards.size() < NON_PILOT_SAMPLE_SIZE) {
            addSampledCards(
                sampledCards,
                querySampledCards(
                    database,
                    "SELECT card_id, guide_id, risk_tier FROM answer_cards " +
                        "WHERE card_id NOT IN (?, ?, ?, ?, ?, ?, ?) " +
                        "ORDER BY card_id LIMIT ?",
                    NON_PILOT_SAMPLE_SIZE
                )
            );
        }
        return new java.util.ArrayList<>(sampledCards).subList(0, NON_PILOT_SAMPLE_SIZE);
    }

    static PackManifest assumeCurrentHeadPack(
        File manifestFile,
        File databaseFile,
        String absentPackPurpose
    ) throws Exception {
        assumeTrue(
            "installed mobile pack manifest is absent; push current-head pack before running " + absentPackPurpose,
            manifestFile.isFile()
        );
        assumeTrue(
            "installed mobile pack database is absent; push current-head pack before running " + absentPackPurpose,
            databaseFile.isFile()
        );

        PackManifest manifest = PackManifest.fromJson(readFileText(manifestFile));
        assumeTrue(
            "installed mobile pack is not the current-head 271-answer-card pack; found manifest answer_cards="
                + manifest.answerCardCount
                + " generated_at="
                + manifest.generatedAt,
            manifest.answerCardCount == CURRENT_HEAD_ANSWER_CARD_COUNT
        );
        return manifest;
    }

    static PackInstaller.InstalledPack installBundledCurrentHeadPack(
        Context context,
        String purpose
    ) throws Exception {
        PackInstaller.InstalledPack pack = PackInstaller.ensureInstalled(context, false);
        assertTrue("installed mobile pack manifest is absent after bundled install for " + purpose, pack.manifestFile.isFile());
        assertTrue("installed mobile pack database is absent after bundled install for " + purpose, pack.databaseFile.isFile());
        assertTrue("installed mobile pack vector file is absent after bundled install for " + purpose, pack.vectorFile.isFile());
        assertEquals(
            "bundled install should hydrate the current-head answer-card pack for " + purpose,
            CURRENT_HEAD_ANSWER_CARD_COUNT,
            pack.manifest.answerCardCount
        );
        assertEquals(
            "installed current-head database size must match its manifest for " + purpose,
            pack.manifest.sqliteBytes,
            pack.databaseFile.length()
        );
        assertEquals(
            "installed current-head vector size must match its manifest for " + purpose,
            pack.manifest.vectorBytes,
            pack.vectorFile.length()
        );
        return pack;
    }

    private static File packRoot(Context context) {
        return new File(context.getFilesDir(), PACK_DIR);
    }

    private static void addSampledCards(Set<SampledCard> target, List<SampledCard> cards) {
        target.addAll(cards);
    }

    private static List<SampledCard> querySampledCards(SQLiteDatabase database, String sql, int limit) {
        String[] pilotIds = PILOT_CARD_IDS.toArray(new String[0]);
        try (Cursor cursor = database.rawQuery(sql, new String[]{
                pilotIds[0],
                pilotIds[1],
                pilotIds[2],
                pilotIds[3],
                pilotIds[4],
                pilotIds[5],
                pilotIds[6],
                String.valueOf(limit)
            })) {
            java.util.ArrayList<SampledCard> cards = new java.util.ArrayList<>();
            while (cursor.moveToNext()) {
                cards.add(new SampledCard(cursor.getString(0), cursor.getString(1), cursor.getString(2)));
            }
            return cards;
        }
    }

    static final class SampledCard {
        final String cardId;
        final String guideId;
        final String riskTier;

        SampledCard(String cardId, String guideId, String riskTier) {
            this.cardId = cardId;
            this.guideId = guideId;
            this.riskTier = riskTier;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof SampledCard)) {
                return false;
            }
            SampledCard card = (SampledCard) other;
            return cardId.equals(card.cardId);
        }

        @Override
        public int hashCode() {
            return cardId.hashCode();
        }

        @Override
        public String toString() {
            return cardId + "/" + guideId + "/" + riskTier;
        }
    }
}
