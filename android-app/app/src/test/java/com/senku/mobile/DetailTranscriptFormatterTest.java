package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public final class DetailTranscriptFormatterTest {
    @Test
    public void transcriptIncludesCurrentTurnSourcesAndAnchorShift() {
        SessionMemory.TurnSnapshot prior = turn(
            "how do i build a rain shelter",
            "Keep one side low to the wind.",
            List.of("[GD-111] Wind Shelter"),
            "GD-111"
        );
        SessionMemory.TurnSnapshot current = turn(
            "what about drainage",
            "Dig a shallow uphill diversion trench.",
            List.of("[GD-222] Site Drainage"),
            "GD-222",
            "deterministic-drainage"
        );

        String transcript = DetailTranscriptFormatter.buildTranscriptExportText(
            "Storm shelter",
            List.of(prior),
            current,
            DetailTranscriptFormatterTest::anchorFor
        );

        assertTrue(transcript.contains("Senku transcript"));
        assertTrue(transcript.contains("Topic: Storm shelter"));
        assertTrue(transcript.contains("Q1: how do i build a rain shelter"));
        assertTrue(transcript.contains("A1 [GD-111 \u00B7 UNSURE]: Keep one side low to the wind."));
        assertTrue(transcript.contains("Q2: what about drainage"));
        assertTrue(transcript.contains("A2 [GD-222 \u00B7 CONFIDENT]: Dig a shallow uphill diversion trench."));
        assertFalse(transcript.contains("Site Drainage"));
        assertFalse(transcript.contains("Anchor shift"));
        assertFalse(transcript.contains("Guide "));
        assertFalse(transcript.contains("->"));
        assertFalse(transcript.contains("Refs:"));
    }

    @Test
    public void transcriptDoesNotDuplicateMatchingTrailingCurrentTurn() {
        SessionMemory.TurnSnapshot current = turn(
            "what about drainage",
            "Dig a shallow uphill diversion trench.",
            List.of("[GD-222] Site Drainage"),
            "GD-222"
        );

        String transcript = DetailTranscriptFormatter.buildTranscriptExportText(
            "Storm shelter",
            List.of(current),
            current,
            DetailTranscriptFormatterTest::anchorFor
        );

        assertEquals(1, countOccurrences(transcript, "Q1: what about drainage"));
    }

    @Test
    public void emptyInputsReturnEmptyTranscript() {
        assertEquals(
            "",
            DetailTranscriptFormatter.buildTranscriptExportText("", null, null, DetailTranscriptFormatterTest::anchorFor)
        );
        assertEquals(
            "",
            DetailTranscriptFormatter.buildTranscriptExportText(
                "",
                List.of(),
                turn("", "", List.of(), ""),
                DetailTranscriptFormatterTest::anchorFor
            )
        );
    }

    private static SessionMemory.TurnSnapshot turn(
        String question,
        String answerSummary,
        List<String> sources,
        String anchorGuideId
    ) {
        return turn(question, answerSummary, sources, anchorGuideId, "");
    }

    private static SessionMemory.TurnSnapshot turn(
        String question,
        String answerSummary,
        List<String> sources,
        String anchorGuideId,
        String ruleId
    ) {
        return new SessionMemory.TurnSnapshot(
            question,
            answerSummary,
            answerSummary,
            sources,
            List.of(source(anchorGuideId)),
            ruleId,
            1714244042000L
        );
    }

    private static SearchResult source(String guideId) {
        return new SearchResult(
            "Source " + guideId,
            "",
            "",
            "",
            guideId,
            "Section",
            "survival",
            "guide-focus"
        );
    }

    private static String anchorFor(SessionMemory.TurnSnapshot turn) {
        if (turn == null || turn.sourceResults == null || turn.sourceResults.isEmpty()) {
            return "";
        }
        return turn.sourceResults.get(0).guideId;
    }

    private static int countOccurrences(String text, String needle) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(needle, index)) >= 0) {
            count += 1;
            index += needle.length();
        }
        return count;
    }
}
