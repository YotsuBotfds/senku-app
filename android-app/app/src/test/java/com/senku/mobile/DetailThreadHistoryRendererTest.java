package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public final class DetailThreadHistoryRendererTest {
    @Test
    public void visiblePriorTurnLimitKeepsTwoTurnsInCompactPortrait() {
        DetailThreadHistoryRenderer.State compactPortrait = new DetailThreadHistoryRenderer.State(
            false,
            false,
            true,
            320
        );

        assertEquals(2, DetailThreadHistoryRenderer.visiblePriorTurnLimit(compactPortrait));
    }

    @Test
    public void visiblePriorTurnLimitKeepsWideLayoutContextBroader() {
        DetailThreadHistoryRenderer.State wide = new DetailThreadHistoryRenderer.State(
            false,
            true,
            false,
            720
        );

        assertEquals(2, DetailThreadHistoryRenderer.visiblePriorTurnLimit(wide));
    }

    @Test
    public void turnLabelsStayCompactForQuestionAndAnchorShift() {
        DetailThreadHistoryRenderer renderer = new DetailThreadHistoryRenderer(
            null,
            new DetailSessionPresentationFormatter(null),
            null
        );

        assertEquals(
            "Q2 \u00B7 FIELD QUESTION",
            renderer.buildTurnLabel(2, true, turn("question", "GD-345", 0L), "")
        );
        assertEquals(
            "A2 \u00B7 ANCHOR GD-345",
            renderer.buildTurnLabel(2, false, turn("answer", "GD-345", 0L), "GD-220")
        );
    }

    @Test
    public void statusLabelsUseScreenshotTranscriptTokens() {
        assertEquals("CONFIDENT", DetailThreadHistoryRenderer.compactStatusLabel("source-backed"));
        assertEquals("CONFIDENT", DetailThreadHistoryRenderer.compactStatusLabel("reviewed"));
        assertEquals("UNSURE", DetailThreadHistoryRenderer.compactStatusLabel("ready"));
    }

    @Test
    public void utilityRailAnswerKeepsFirstLineAndHardLimit() {
        String answer = "First line should remain visible before other detail.\nSecond line should not render.";

        assertEquals(
            "First line should remain visible before other detail.",
            DetailThreadHistoryRenderer.compactThreadAnswer(answer, true, text -> text)
        );

        String longAnswer = "1234567890".repeat(20);
        String compact = DetailThreadHistoryRenderer.compactThreadAnswer(longAnswer, true, text -> text);

        assertEquals(180, compact.length());
        assertTrue(compact.endsWith("..."));
    }

    @Test
    public void guideChipsStayInlineAndDeduplicated() {
        SessionMemory.TurnSnapshot turn = new SessionMemory.TurnSnapshot(
            "question",
            "answer",
            "answer",
            List.of("Source GD-002", "Source GD-004"),
            List.of(source("GD-001"), source("GD-002"), source("GD-003"), source("GD-004")),
            "",
            0L
        );

        assertEquals(
            List.of("GD-001", "GD-002", "GD-003"),
            DetailThreadHistoryRenderer.guideChipIdsForTurn(turn)
        );
    }

    private static SessionMemory.TurnSnapshot turn(String answer, String guideId, long recordedAtEpochMs) {
        return new SessionMemory.TurnSnapshot(
            "question",
            answer,
            answer,
            List.of(guideId),
            List.of(source(guideId)),
            "",
            recordedAtEpochMs
        );
    }

    private static SearchResult source(String guideId) {
        return new SearchResult("Source " + guideId, "", "", "", guideId, "", "", "");
    }
}
