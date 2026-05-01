package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public final class OfflineAnswerTelemetryPolicyTest {
    @Test
    public void buildFirstTokenLineClampsNegativeLatencyAndPreservesRouteFields() {
        assertEquals(
            "ask.first_token_ms=0 query=\"how long\" path=host_fallback",
            OfflineAnswerTelemetryPolicy.buildFirstTokenLine("how long", "host_fallback", -14L)
        );
    }

    @Test
    public void buildLatencySummaryLineFormatsStableParseableFields() {
        OfflineAnswerEngine.LatencyBreakdown breakdown = new OfflineAnswerEngine.LatencyBreakdown(
            "guide",
            12L,
            3L,
            4L,
            50L,
            200L,
            255L
        );

        assertEquals(
            "ask.latency queryClass=\"guide\" retrievalMs=12 rerankMs=3 promptBuildMs=4 "
                + "firstTokenMs=50 decodeMs=200 totalMs=255 query=\"how do i filter water\"",
            OfflineAnswerTelemetryPolicy.buildLatencySummaryLine("how do i filter water", breakdown)
        );
    }

    @Test
    public void buildFinalModeLineLowercasesModeAndClampsElapsedTime() {
        assertEquals(
            "ask.generate final_mode=uncertain_fit route=early_uncertain_fit "
                + "query=\"is this guide related\" totalElapsedMs=0",
            OfflineAnswerTelemetryPolicy.buildFinalModeLine(
                "is this guide related",
                OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT,
                "early_uncertain_fit",
                -8L
            )
        );
    }

    @Test
    public void resolvePreparedFinalModeRouteNamesEarlyTerminalPreparedAnswers() {
        OfflineAnswerTelemetryPolicy.FinalModeRoute deterministic =
            OfflineAnswerTelemetryPolicy.resolvePreparedFinalModeRoute(
                OfflineAnswerEngine.PreparedAnswer.restoredDeterministic(
                    "can i reuse bottles",
                    "Use known food-safe bottles.",
                    List.of(),
                    false,
                    "reused_container_water",
                    100L
                )
            );
        assertTrue(deterministic.isReady());
        assertEquals(OfflineAnswerEngine.AnswerMode.CONFIDENT, deterministic.finalMode);
        assertEquals("deterministic", deterministic.route);

        OfflineAnswerTelemetryPolicy.FinalModeRoute abstain =
            OfflineAnswerTelemetryPolicy.resolvePreparedFinalModeRoute(
                OfflineAnswerEngine.PreparedAnswer.abstain(
                    "how do i build a rain shelter from a tarp",
                    "Senku doesn't have a guide for that.",
                    List.of(),
                    false
                )
            );
        assertTrue(abstain.isReady());
        assertEquals(OfflineAnswerEngine.AnswerMode.ABSTAIN, abstain.finalMode);
        assertEquals("early_abstain", abstain.route);

        OfflineAnswerTelemetryPolicy.FinalModeRoute uncertainFit =
            OfflineAnswerTelemetryPolicy.resolvePreparedFinalModeRoute(
                OfflineAnswerEngine.PreparedAnswer.uncertainFit(
                    "is this just stress",
                    "The closest guides are related but not exact.",
                    List.of(),
                    false,
                    100L,
                    12L,
                    2L,
                    0L,
                    OfflineAnswerEngine.ConfidenceLabel.MEDIUM,
                    true
                )
            );
        assertTrue(uncertainFit.isReady());
        assertEquals(OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT, uncertainFit.finalMode);
        assertEquals("early_uncertain_fit", uncertainFit.route);
    }

    @Test
    public void resolvePreparedFinalModeRouteLeavesGenerativePreparedAnswerForGeneration() {
        OfflineAnswerTelemetryPolicy.FinalModeRoute route =
            OfflineAnswerTelemetryPolicy.resolvePreparedFinalModeRoute(
                OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
                    "how do i make a simple fire starter",
                    List.of(),
                    false,
                    100L,
                    false,
                    "",
                    "",
                    "system",
                    "prompt"
                )
            );

        assertFalse(route.isReady());
    }
}
