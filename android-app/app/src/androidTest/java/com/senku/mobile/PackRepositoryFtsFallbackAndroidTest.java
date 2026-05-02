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
    public void repositoryCachesFts4TableOnDevicesWithoutFts5Runtime() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        PackInstaller.InstalledPack pack = CurrentHeadAnswerCardPackTestSupport.installBundledCurrentHeadPack(
            context,
            "FTS runtime cache contract"
        );

        resetCachedFtsRuntime();
        try (PackRepository ignored = new PackRepository(pack.databaseFile, existingVectorFileOrNull(pack.vectorFile))) {
            Object cachedRuntime = cachedFtsRuntime();
            assertTrue("PackRepository should cache FTS runtime detection", cachedRuntime != null);

            if (!booleanField(cachedRuntime, "supportsBm25")) {
                assertTrue("runtime without FTS5 should still be available through FTS4", booleanField(cachedRuntime, "available"));
                assertEquals(FTS4_TABLE, stringField(cachedRuntime, "tableName"));
            }
        } finally {
            resetCachedFtsRuntime();
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

    private static Object cachedFtsRuntime() throws Exception {
        Field field = PackFtsRuntimeDetector.class.getDeclaredField("cachedFtsRuntime");
        field.setAccessible(true);
        return field.get(null);
    }

    private static void resetCachedFtsRuntime() throws Exception {
        Field field = PackFtsRuntimeDetector.class.getDeclaredField("cachedFtsRuntime");
        field.setAccessible(true);
        field.set(null, null);
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

    private static File existingVectorFileOrNull(File vectorFile) {
        return vectorFile != null && vectorFile.isFile() ? vectorFile : null;
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
