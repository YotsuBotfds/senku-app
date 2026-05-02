package com.senku.mobile;

import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.CURRENT_HEAD_ANSWER_CARD_COUNT;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.PACK_DIR;
import static com.senku.mobile.CurrentHeadAnswerCardPackTestSupport.installBundledCurrentHeadPack;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public final class PackMigrationInstallTest {
    @Test
    public void cleanInstallHydratesBundledCurrentHeadPackIntoAppPrivateFiles() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        File packRoot = new File(context.getFilesDir(), PACK_DIR);
        deleteRecursively(packRoot);

        PackInstaller.InstalledPack pack = installBundledCurrentHeadPack(context, "clean install hydration");

        assertEquals(packRoot.getAbsolutePath(), pack.rootDir.getAbsolutePath());
        assertEquals(CURRENT_HEAD_ANSWER_CARD_COUNT, pack.manifest.answerCardCount);
        assertTrue(pack.databaseFile.isFile());
        assertTrue(pack.vectorFile.isFile());
    }

    @Test
    public void installedPackWithHashValidInvalidSqliteSchemaIsRehydratedFromBundledAssets() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        File packRoot = new File(context.getFilesDir(), PACK_DIR);
        deleteRecursively(packRoot);
        PackInstaller.InstalledPack seededPack =
            installBundledCurrentHeadPack(context, "invalid installed schema seed");

        assertTrue(seededPack.databaseFile.delete());
        try (SQLiteDatabase badDatabase = SQLiteDatabase.openOrCreateDatabase(seededPack.databaseFile, null)) {
            badDatabase.execSQL("CREATE TABLE guides(guide_id TEXT)");
        }
        writeManifestWithCurrentSqliteHash(seededPack.manifestFile, seededPack.databaseFile);

        PackInstaller.InstalledPack rehydratedPack = PackInstaller.ensureInstalled(context, false);

        assertEquals(CURRENT_HEAD_ANSWER_CARD_COUNT, rehydratedPack.manifest.answerCardCount);
        assertEquals(rehydratedPack.manifest.sqliteBytes, rehydratedPack.databaseFile.length());
        try (SQLiteDatabase database = SQLiteDatabase.openDatabase(
            rehydratedPack.databaseFile.getAbsolutePath(),
            null,
            SQLiteDatabase.OPEN_READONLY
        )) {
            CurrentHeadAnswerCardPackTestSupport.assertTableExists(database, "chunks");
            CurrentHeadAnswerCardPackTestSupport.assertTableExists(database, "answer_cards");
        }
    }

    @Test
    public void installedPackWithHashValidStaleVectorHeaderIsRehydratedFromBundledAssets() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        File packRoot = new File(context.getFilesDir(), PACK_DIR);
        deleteRecursively(packRoot);
        PackInstaller.InstalledPack seededPack =
            installBundledCurrentHeadPack(context, "invalid installed vector seed");
        String badVectorSha;
        try (FileOutputStream output = new FileOutputStream(seededPack.vectorFile, false)) {
            output.write(vectorHeader(1, seededPack.manifest.embeddingDimension, 1));
        }
        badVectorSha = PackInstaller.sha256HexForTest(seededPack.vectorFile);
        writeManifestWithCurrentVectorHash(seededPack.manifestFile, seededPack.vectorFile);

        PackInstaller.InstalledPack rehydratedPack = PackInstaller.ensureInstalled(context, false);

        assertEquals(CURRENT_HEAD_ANSWER_CARD_COUNT, rehydratedPack.manifest.answerCardCount);
        assertEquals(rehydratedPack.manifest.vectorBytes, rehydratedPack.vectorFile.length());
        assertEquals(rehydratedPack.manifest.chunkCount, rehydratedPack.vectorInfo.rowCount);
        assertEquals(rehydratedPack.manifest.embeddingDimension, rehydratedPack.vectorInfo.dimension);
        assertTrue(
            "rehydrated vector should not retain the hash-valid stale header fixture",
            !badVectorSha.equals(PackInstaller.sha256HexForTest(rehydratedPack.vectorFile))
        );
        try (PackRepository repository = new PackRepository(rehydratedPack.databaseFile, rehydratedPack.vectorFile)) {
            assertTrue("rehydrated vector should be visible to repository", repository.hasVectorStore());
            List<SearchResult> results = repository.search("rain shelter", 5);
            assertTrue("rehydrated pack should remain searchable", !results.isEmpty());
        }
    }

    private static void writeManifestWithCurrentSqliteHash(File manifestFile, File sqliteFile) throws Exception {
        JSONObject root = new JSONObject(CurrentHeadAnswerCardPackTestSupport.readFileText(manifestFile));
        JSONObject sqlite = root.getJSONObject("files").getJSONObject("sqlite");
        sqlite.put("bytes", sqliteFile.length());
        sqlite.put("sha256", PackInstaller.sha256HexForTest(sqliteFile));
        try (FileOutputStream output = new FileOutputStream(manifestFile, false)) {
            output.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void writeManifestWithCurrentVectorHash(File manifestFile, File vectorFile) throws Exception {
        JSONObject root = new JSONObject(CurrentHeadAnswerCardPackTestSupport.readFileText(manifestFile));
        JSONObject vectors = root.getJSONObject("files").getJSONObject("vectors");
        vectors.put("bytes", vectorFile.length());
        vectors.put("sha256", PackInstaller.sha256HexForTest(vectorFile));
        try (FileOutputStream output = new FileOutputStream(manifestFile, false)) {
            output.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
        }
    }

    private static byte[] vectorHeader(int rowCount, int dimension, int dtypeCode) {
        ByteBuffer header = ByteBuffer.allocate(32).order(ByteOrder.LITTLE_ENDIAN);
        header.put("SNKUVEC1".getBytes(StandardCharsets.US_ASCII));
        header.putInt(1);
        header.putInt(32);
        header.putInt(rowCount);
        header.putInt(dimension);
        header.putInt(dtypeCode);
        header.putInt(0);
        return header.array();
    }

    private static void deleteRecursively(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        assertTrue("failed to delete stale pack file before clean install: " + file.getAbsolutePath(), file.delete());
    }
}
