package com.senku.mobile;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
}
