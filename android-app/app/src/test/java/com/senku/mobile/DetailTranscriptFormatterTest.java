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
            "GD-222"
        );

        String transcript = DetailTranscriptFormatter.buildTranscriptExportText(
            "Storm shelter",
            List.of(prior),
            current,
            DetailTranscriptFormatterTest::anchorFor
        );

        assertTrue(transcript.contains("Senku transcript"));
        assertTrue(transcript.contains("Topic: Storm shelter"));
        assertTrue(transcript.contains("You: how do i build a rain shelter"));
        assertTrue(transcript.contains("Senku: Keep one side low to the wind."));
        assertTrue(transcript.contains("Anchor: GD-111"));
        assertTrue(transcript.contains("You: what about drainage"));
        assertTrue(transcript.contains("Anchor shift: GD-111 -> GD-222"));
        assertTrue(transcript.contains("Sources: [GD-222] Site Drainage"));
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

        assertEquals(1, countOccurrences(transcript, "You: what about drainage"));
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
        return new SessionMemory.TurnSnapshot(
            question,
            answerSummary,
            answerSummary,
            sources,
            List.of(source(anchorGuideId)),
            "",
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
