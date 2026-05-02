package com.senku.mobile;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.SystemClock;
import android.util.Log;

import java.util.Locale;

/**
 * FTS runtime detection and selection helper for installed pack lexical search.
 *
 * FTS5 is preferred when its table exists and the runtime accepts MATCH queries.
 * FTS4 is the fallback when its table is usable but FTS5 is not available in
 * process. LIKE remains the repository-level safety net when neither FTS table
 * can execute.
 */
final class PackFtsRuntimeDetector {
    private static final String TAG = "SenkuPackRepo";
    private static final Locale QUERY_LOCALE = Locale.US;
    private static final long FTS_RUNTIME_PROBE_BUDGET_MS = 25L;

    static final String FTS5_TABLE = "lexical_chunks_fts";
    static final String FTS4_TABLE = "lexical_chunks_fts4";

    private PackFtsRuntimeDetector() {
    }

    static Runtime detect(SQLiteDatabase database) {
        return detectInternal(database);
    }

    private static Runtime detectInternal(SQLiteDatabase database) {
        boolean supportsFts5 = hasCompileOption(database, "ENABLE_FTS5");
        boolean supportsFts4 = hasCompileOption(database, "ENABLE_FTS4");
        boolean hasFts5Table = tableExistsInSchema(database, FTS5_TABLE);
        boolean hasFts4Table = tableExistsInSchema(database, FTS4_TABLE);
        ProbeResult runtimeFts5 = hasFts5Table
            ? supportsFtsMatchRuntime(database, FTS5_TABLE)
            : ProbeResult.notRun();
        ProbeResult runtimeFts4 = hasFts4Table
            ? supportsFtsMatchRuntime(database, FTS4_TABLE)
            : ProbeResult.notRun();
        Runtime detected = selectRuntime(
            hasFts5Table,
            hasFts4Table,
            runtimeFts5.supported,
            runtimeFts4.supported
        );

        if (detected.available) {
            Log.d(
                TAG,
                "fts.available table=" + detected.tableName +
                    " supportsBm25=" + detected.supportsBm25 +
                    " schemaPresent=true runtimeProbe=true compile5=" + supportsFts5 +
                    " compile4=" + supportsFts4 +
                    " runtime5=" + runtimeFts5.supported +
                    " runtime5Ms=" + runtimeFts5.elapsedMs +
                    " runtime4=" + runtimeFts4.supported +
                    " runtime4Ms=" + runtimeFts4.elapsedMs +
                    " fallback=" + detected.tableName
            );
            return detected;
        }

        Log.d(
            TAG,
            "fts.unavailable support5=" + supportsFts5 +
                " support4=" + supportsFts4 +
                " runtime5=" + runtimeFts5.supported +
                " runtime5Ms=" + runtimeFts5.elapsedMs +
                " runtime4=" + runtimeFts4.supported +
                " runtime4Ms=" + runtimeFts4.elapsedMs +
                " schema5=" + hasFts5Table +
                " schema4=" + hasFts4Table +
                " fallback=like"
        );
        return Runtime.unavailable();
    }

    static Runtime selectRuntime(
        boolean hasFts5Table,
        boolean hasFts4Table,
        boolean runtimeFts5,
        boolean runtimeFts4
    ) {
        if (hasFts5Table && runtimeFts5) {
            return new Runtime(true, FTS5_TABLE, true);
        }
        if (hasFts4Table && runtimeFts4) {
            return new Runtime(true, FTS4_TABLE, false);
        }
        return Runtime.unavailable();
    }

    static String buildDebugLine(
        boolean supportsFts5Compile,
        boolean supportsFts4Compile,
        boolean hasFts5Table,
        boolean hasFts4Table,
        boolean runtimeFts5,
        boolean runtimeFts4
    ) {
        Runtime runtime = selectRuntime(hasFts5Table, hasFts4Table, runtimeFts5, runtimeFts4);
        return "available=" + runtime.available +
            " table=" + runtime.tableName +
            " supportsBm25=" + runtime.supportsBm25 +
            " compile5=" + supportsFts5Compile +
            " compile4=" + supportsFts4Compile +
            " runtime5=" + runtimeFts5 +
            " runtime4=" + runtimeFts4 +
            " schema5=" + hasFts5Table +
            " schema4=" + hasFts4Table;
    }

    private static ProbeResult supportsFtsMatchRuntime(SQLiteDatabase database, String tableName) {
        if (emptySafe(tableName).trim().isEmpty()) {
            return ProbeResult.notRun();
        }

        long startedAtNs = elapsedRealtimeNanosSafe();
        boolean supported = false;
        try (Cursor ignored = database.rawQuery(
            "SELECT rowid FROM " + tableName + " WHERE " + tableName + " MATCH ? LIMIT 1",
            new String[]{"water"}
        )) {
            supported = true;
        } catch (SQLiteException ignored) {
        }
        long elapsedMs = elapsedRealtimeMsSince(startedAtNs);
        Log.d(
            TAG,
            "fts.runtime_probe table=" + tableName +
                " supported=" + supported +
                " elapsedMs=" + elapsedMs
        );
        if (elapsedMs > FTS_RUNTIME_PROBE_BUDGET_MS) {
            Log.w(
                TAG,
                "fts.runtime_probe_slow table=" + tableName +
                    " elapsedMs=" + elapsedMs +
                    " budgetMs=" + FTS_RUNTIME_PROBE_BUDGET_MS
            );
        }
        return new ProbeResult(supported, elapsedMs);
    }

    private static boolean hasCompileOption(SQLiteDatabase database, String optionName) {
        String target = emptySafe(optionName).trim().toLowerCase(QUERY_LOCALE);
        if (target.isEmpty()) {
            return false;
        }

        try (Cursor cursor = database.rawQuery("PRAGMA compile_options", null)) {
            while (cursor.moveToNext()) {
                String option = emptySafe(cursor.getString(0)).trim().toLowerCase(QUERY_LOCALE);
                if (option.equals(target) || option.contains(target)) {
                    return true;
                }
            }
        } catch (SQLiteException ignored) {
            return false;
        }
        return false;
    }

    private static boolean tableExistsInSchema(SQLiteDatabase database, String tableName) {
        if (emptySafe(tableName).trim().isEmpty()) {
            return false;
        }

        try (Cursor cursor = database.rawQuery(
            "SELECT 1 FROM sqlite_master WHERE type='table' AND name=? LIMIT 1",
            new String[]{tableName}
        )) {
            return cursor.moveToFirst();
        } catch (SQLiteException ignored) {
            return false;
        }
    }

    private static long elapsedRealtimeMsSince(long startedAtNs) {
        return Math.max(0L, elapsedRealtimeNanosSafe() - startedAtNs) / 1_000_000L;
    }

    private static long elapsedRealtimeNanosSafe() {
        try {
            return SystemClock.elapsedRealtimeNanos();
        } catch (RuntimeException ignored) {
            return System.nanoTime();
        }
    }

    private static String emptySafe(String text) {
        return text == null ? "" : text;
    }

    static final class Runtime {
        final boolean available;
        final String tableName;
        final boolean supportsBm25;

        Runtime(boolean available, String tableName, boolean supportsBm25) {
            this.available = available;
            this.tableName = tableName;
            this.supportsBm25 = supportsBm25;
        }

        static Runtime unavailable() {
            return new Runtime(false, "", false);
        }
    }

    static final class ProbeResult {
        final boolean supported;
        final long elapsedMs;

        ProbeResult(boolean supported, long elapsedMs) {
            this.supported = supported;
            this.elapsedMs = Math.max(0L, elapsedMs);
        }

        static ProbeResult notRun() {
            return new ProbeResult(false, 0L);
        }
    }
}
