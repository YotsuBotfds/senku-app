package com.senku.mobile;

/**
 * Pure FTS runtime selection helper for installed pack lexical search.
 *
 * FTS5 is preferred when its table exists and the runtime accepts MATCH queries.
 * FTS4 is the fallback when its table is usable but FTS5 is not available in
 * process. LIKE remains the repository-level safety net when neither FTS table
 * can execute.
 */
final class PackFtsRuntimeDetector {
    static final String FTS5_TABLE = "lexical_chunks_fts";
    static final String FTS4_TABLE = "lexical_chunks_fts4";

    private PackFtsRuntimeDetector() {
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
