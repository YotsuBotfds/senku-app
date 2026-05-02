package com.senku.mobile;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public final class PackRepositoryFtsFallbackAndroidTest {
    private static final String FTS5_TABLE = "lexical_chunks_fts";
    private static final String FTS4_TABLE = "lexical_chunks_fts4";

    @Test
    public void promotedBundledAssetDatabaseCarriesFts5AndFts4Tables() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        PackInstaller.InstalledPack pack = CurrentHeadAnswerCardPackTestSupport.installBundledCurrentHeadPack(
            context,
            "FTS fallback contract"
        );

        try (SQLiteDatabase database = SQLiteDatabase.openDatabase(
            pack.databaseFile.getAbsolutePath(),
            null,
            SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS
        )) {
            CurrentHeadAnswerCardPackTestSupport.assertTableExists(database, FTS5_TABLE);
            CurrentHeadAnswerCardPackTestSupport.assertTableExists(database, FTS4_TABLE);
            assertTrue(
                "FTS4 fallback table should be populated in promoted asset DB",
                CurrentHeadAnswerCardPackTestSupport.queryLong(database, "SELECT COUNT(*) FROM " + FTS4_TABLE) > 0
            );
        }
    }

    @Test
    public void repositoryRuntimeSelectionUsesFts4WhenFts5RuntimeIsUnavailable() throws Exception {
        Object runtime = invokeSelectFtsRuntime(
            true,
            true,
            false,
            true
        );

        assertTrue("FTS fallback runtime should remain available", booleanField(runtime, "available"));
        assertEquals(FTS4_TABLE, stringField(runtime, "tableName"));
        assertFalse("FTS4 fallback must not advertise bm25 support", booleanField(runtime, "supportsBm25"));
    }

    @Test
    public void runtimeDetectionIsScopedToCurrentDatabaseSchema() {
        try (SQLiteDatabase emptyDatabase = SQLiteDatabase.create(null);
             SQLiteDatabase fts4Database = SQLiteDatabase.create(null)) {
            fts4Database.execSQL(
                "CREATE VIRTUAL TABLE " + FTS4_TABLE + " USING fts4(" +
                    "guide_title, guide_id, section_heading, body)"
            );
            fts4Database.execSQL(
                "INSERT INTO " + FTS4_TABLE +
                    "(guide_title, guide_id, section_heading, body) VALUES (?, ?, ?, ?)",
                new Object[]{"Water Safety", "GD-035", "Treatment", "water"}
            );

            PackFtsRuntimeDetector.Runtime emptyRuntime = PackFtsRuntimeDetector.detect(emptyDatabase);
            PackFtsRuntimeDetector.Runtime fts4Runtime = PackFtsRuntimeDetector.detect(fts4Database);

            assertFalse("database without FTS tables should not report FTS runtime", emptyRuntime.available);
            assertTrue(
                "later database with FTS4 table should be probed independently",
                fts4Runtime.available
            );
            assertEquals(FTS4_TABLE, fts4Runtime.tableName);
            assertFalse(fts4Runtime.supportsBm25);
        }
    }

    @Test
    public void repositoryIgnoresMalformedVectorFileAndFallsBackToLexicalSearch() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        PackInstaller.InstalledPack pack = CurrentHeadAnswerCardPackTestSupport.installBundledCurrentHeadPack(
            context,
            "malformed vector fallback contract"
        );
        File badVectorFile = File.createTempFile("bad-vector", ".vec", context.getCacheDir());
        try (FileOutputStream output = new FileOutputStream(badVectorFile)) {
            output.write(new byte[] {1, 2, 3, 4});
        }

        try (PackRepository repository = new PackRepository(pack.databaseFile, badVectorFile)) {
            assertFalse("malformed vector file should be disabled", repository.hasVectorStore());
            List<SearchResult> results = repository.search("rain shelter", 5);

            assertFalse("lexical fallback should still return bundled pack results", results.isEmpty());
        }
    }

    @Test
    public void repositoryTreatsMissingVectorPathAsDisabledAndFallsBackToLexicalSearch() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        PackInstaller.InstalledPack pack = CurrentHeadAnswerCardPackTestSupport.installBundledCurrentHeadPack(
            context,
            "missing vector fallback contract"
        );
        File missingVectorFile = new File(context.getCacheDir(), "missing-vector-store.vec");
        if (missingVectorFile.exists()) {
            assertTrue("test vector fixture cleanup failed", missingVectorFile.delete());
        }

        try (PackRepository repository = new PackRepository(pack.databaseFile, missingVectorFile)) {
            assertFalse("missing vector file should be disabled", repository.hasVectorStore());
            List<SearchResult> results = repository.search("rain shelter", 5);

            assertFalse("lexical fallback should still return bundled pack results", results.isEmpty());
        }
    }

    @Test
    public void repositoryFallsBackToLexicalSearchWhenVectorCentroidCannotBeBuilt() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        PackInstaller.InstalledPack pack = CurrentHeadAnswerCardPackTestSupport.installBundledCurrentHeadPack(
            context,
            "empty vector centroid fallback contract"
        );
        File emptyVectorFile = File.createTempFile("empty-vector", ".vec", context.getCacheDir());
        try (FileOutputStream output = new FileOutputStream(emptyVectorFile)) {
            output.write(vectorHeader(0, pack.manifest.embeddingDimension, 1));
        }

        try (PackRepository repository = new PackRepository(pack.databaseFile, emptyVectorFile)) {
            assertTrue("valid empty vector file should remain visible to repository", repository.hasVectorStore());
            List<SearchResult> results = repository.search("rain shelter", 5);

            assertFalse("centroid fallback should still return lexical bundled pack results", results.isEmpty());
        }
    }

    private static Object invokeSelectFtsRuntime(
        boolean hasFts5Table,
        boolean hasFts4Table,
        boolean runtimeFts5,
        boolean runtimeFts4
    ) throws Exception {
        Method method = PackFtsRuntimeDetector.class.getDeclaredMethod(
            "selectRuntime",
            boolean.class,
            boolean.class,
            boolean.class,
            boolean.class
        );
        method.setAccessible(true);
        return method.invoke(null, hasFts5Table, hasFts4Table, runtimeFts5, runtimeFts4);
    }

    private static boolean booleanField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getBoolean(target);
    }

    private static String stringField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (String) field.get(target);
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
}
