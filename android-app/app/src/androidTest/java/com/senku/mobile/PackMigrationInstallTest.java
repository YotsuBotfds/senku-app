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
import java.nio.charset.StandardCharsets;

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

    private static void writeManifestWithCurrentSqliteHash(File manifestFile, File sqliteFile) throws Exception {
        JSONObject root = new JSONObject(CurrentHeadAnswerCardPackTestSupport.readFileText(manifestFile));
        JSONObject sqlite = root.getJSONObject("files").getJSONObject("sqlite");
        sqlite.put("bytes", sqliteFile.length());
        sqlite.put("sha256", PackInstaller.sha256HexForTest(sqliteFile));
        try (FileOutputStream output = new FileOutputStream(manifestFile, false)) {
            output.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
        }
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
