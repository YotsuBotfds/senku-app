package com.senku.mobile.telemetry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.senku.mobile.SearchResult;

import org.json.JSONObject;
import org.junit.Test;

import java.util.List;

public final class LatencyPanelTest {
    @Test
    public void classifyQueryCoversReviewerClasses() {
        assertEquals(
            LatencyPanel.QUERY_CLASS_DETERMINISTIC,
            LatencyPanel.classifyQuery("can i use old soda bottles", List.of(), true, false)
        );
        assertEquals(
            LatencyPanel.QUERY_CLASS_ABSTAIN,
            LatencyPanel.classifyQuery("unknown tarp question", List.of(), false, true)
        );
        assertEquals(
            LatencyPanel.QUERY_CLASS_ACUTE_GENERATIVE,
            LatencyPanel.classifyQuery(
                "What should I do right now for a puncture wound?",
                List.of(
                    new SearchResult(
                        "First Aid Essentials",
                        "",
                        "",
                        "",
                        "GD-115",
                        "Wound Cleaning",
                        "medical",
                        "guide-focus",
                        "safety",
                        "immediate",
                        "wound_care",
                        "wound_cleaning"
                    )
                ),
                false,
                false
            )
        );
        assertEquals(
            LatencyPanel.QUERY_CLASS_MEDICAL_CROSS_GUIDE,
            LatencyPanel.classifyQuery(
                "How do I reduce infection risk while cleaning a wound?",
                List.of(
                    new SearchResult(
                        "First Aid Essentials",
                        "",
                        "",
                        "",
                        "GD-115",
                        "Wound Cleaning",
                        "medical",
                        "guide-focus",
                        "safety",
                        "long_term",
                        "wound_care",
                        "wound_cleaning"
                    )
                ),
                false,
                false
            )
        );
        assertEquals(
            LatencyPanel.QUERY_CLASS_PRACTICAL_HOW_TO,
            LatencyPanel.classifyQuery(
                "How do I improvise a rain shelter?",
                List.of(
                    new SearchResult(
                        "Emergency Shelter",
                        "",
                        "",
                        "",
                        "GD-001",
                        "Lean-To",
                        "survival",
                        "guide-focus"
                    )
                ),
                false,
                false
            )
        );
    }

    @Test
    public void buildEventJsonUsesExpectedSchema() throws Exception {
        JSONObject event = new JSONObject(
            LatencyPanel.buildEventJson(
                LatencyPanel.QUERY_CLASS_PRACTICAL_HOW_TO,
                12L,
                5L,
                7L,
                31L,
                220L,
                249L
            )
        );

        assertEquals(LatencyPanel.QUERY_CLASS_PRACTICAL_HOW_TO, event.getString("queryClass"));
        assertEquals(12L, event.getLong("retrievalMs"));
        assertEquals(5L, event.getLong("rerankMs"));
        assertEquals(7L, event.getLong("promptBuildMs"));
        assertEquals(7L, event.getLong("promptMs"));
        assertEquals(31L, event.getLong("firstTokenMs"));
        assertEquals(220L, event.getLong("decodeMs"));
        assertEquals(220L, event.getLong("generationMs"));
        assertEquals(249L, event.getLong("totalMs"));
        assertTrue(event.length() == 9);
    }
}
