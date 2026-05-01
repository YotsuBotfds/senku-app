package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PackSearchTelemetryPolicyTest {
    @Test
    public void outcomeTelemetryBuildsBreakdownAndSummaryFields() {
        PackSearchTelemetryPolicy.OutcomeTelemetry telemetry =
            PackSearchTelemetryPolicy.buildOutcomeTelemetry(
                "rain shelter",
                true,
                2,
                3,
                4,
                5,
                6L,
                7L,
                8L,
                9L,
                10L,
                11L,
                PackSearchTelemetryPolicy.OUTCOME_HYBRID
            );

        assertEquals(PackSearchTelemetryPolicy.OUTCOME_HYBRID, telemetry.breakdown.fallbackMode);
        assertEquals(6L, telemetry.breakdown.routeMs);
        assertEquals(7L, telemetry.breakdown.ftsMs);
        assertEquals(8L, telemetry.breakdown.keywordMs);
        assertEquals(9L, telemetry.breakdown.vectorMs);
        assertEquals(10L, telemetry.breakdown.rerankMs);
        assertEquals(11L, telemetry.breakdown.totalMs);
        assertTrue(telemetry.summaryLine.contains("query=\"rain shelter\""));
        assertTrue(telemetry.summaryLine.contains("routeFocused=true"));
        assertTrue(telemetry.summaryLine.contains("routeSpecs=2"));
        assertTrue(telemetry.summaryLine.contains("lexicalHits=3"));
        assertTrue(telemetry.summaryLine.contains("vectorHits=4"));
        assertTrue(telemetry.summaryLine.contains("routeResults=5"));
        assertTrue(telemetry.summaryLine.contains("fallback=" + PackSearchTelemetryPolicy.OUTCOME_HYBRID));
    }

    @Test
    public void outcomeTelemetryOmitsTripwireWhenNoStageIsSlow() {
        PackSearchTelemetryPolicy.OutcomeTelemetry telemetry =
            PackSearchTelemetryPolicy.buildOutcomeTelemetry(
                "rain shelter",
                false,
                0,
                1,
                0,
                0,
                1L,
                2L,
                3L,
                4L,
                5L,
                6L,
                PackSearchTelemetryPolicy.OUTCOME_VECTOR_DISABLED
            );

        assertEquals("", telemetry.slowTripwireLine);
    }

    @Test
    public void outcomeTelemetryBuildsTripwireForFirstSlowStage() {
        PackSearchTelemetryPolicy.OutcomeTelemetry telemetry =
            PackSearchTelemetryPolicy.buildOutcomeTelemetry(
                "rain shelter",
                false,
                0,
                1,
                0,
                0,
                1L,
                2L,
                3L,
                181L,
                999L,
                1000L,
                PackSearchTelemetryPolicy.OUTCOME_CENTROID_MISSING
            );

        assertEquals("vector", telemetry.breakdown.firstSlowStage());
        assertTrue(telemetry.slowTripwireLine.startsWith("search.slow query=\"rain shelter\""));
        assertTrue(telemetry.slowTripwireLine.contains("stage=vector"));
        assertTrue(telemetry.slowTripwireLine.contains(
            "fallback=" + PackSearchTelemetryPolicy.OUTCOME_CENTROID_MISSING
        ));
    }
}
