package com.senku.mobile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class PackFtsRuntimeDetectorTest {
    @Test
    public void selectRuntimePrefersFts5WhenBothTablesCanRun() {
        PackFtsRuntimeDetector.Runtime runtime = PackFtsRuntimeDetector.selectRuntime(
            true,
            true,
            true,
            true
        );

        assertTrue(runtime.available);
        assertEquals("lexical_chunks_fts", runtime.tableName);
        assertTrue(runtime.supportsBm25);
    }

    @Test
    public void selectRuntimeFallsBackToFts4WhenFts5CannotRun() {
        PackFtsRuntimeDetector.Runtime runtime = PackFtsRuntimeDetector.selectRuntime(
            true,
            true,
            false,
            true
        );

        assertTrue(runtime.available);
        assertEquals("lexical_chunks_fts4", runtime.tableName);
        assertFalse(runtime.supportsBm25);
    }

    @Test
    public void selectRuntimeIgnoresRuntimeProbeWithoutMatchingSchemaTable() {
        PackFtsRuntimeDetector.Runtime runtime = PackFtsRuntimeDetector.selectRuntime(
            false,
            false,
            true,
            true
        );

        assertFalse(runtime.available);
        assertEquals("", runtime.tableName);
        assertFalse(runtime.supportsBm25);
    }

    @Test
    public void buildDebugLineKeepsExistingFieldNamesAndValues() {
        String debugLine = PackFtsRuntimeDetector.buildDebugLine(
            false,
            false,
            true,
            true,
            false,
            true
        );

        assertTrue(debugLine.contains("available=true"));
        assertTrue(debugLine.contains("table=lexical_chunks_fts4"));
        assertTrue(debugLine.contains("supportsBm25=false"));
        assertTrue(debugLine.contains("compile5=false"));
        assertTrue(debugLine.contains("runtime4=true"));
        assertTrue(debugLine.contains("schema5=true"));
    }

    @Test
    public void probeResultClampsNegativeElapsedMs() {
        PackFtsRuntimeDetector.ProbeResult result = new PackFtsRuntimeDetector.ProbeResult(true, -7L);

        assertTrue(result.supported);
        assertEquals(0L, result.elapsedMs);
    }
}
