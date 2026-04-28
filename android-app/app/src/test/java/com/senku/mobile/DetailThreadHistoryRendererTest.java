package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    public void visiblePriorTurnLimitKeepsWideLayoutBounded() {
        DetailThreadHistoryRenderer.State wide = new DetailThreadHistoryRenderer.State(
            false,
            true,
            false,
            720
        );

        assertEquals(2, DetailThreadHistoryRenderer.visiblePriorTurnLimit(wide));
    }

    @Test
    public void visiblePriorTurnLimitKeepsUtilityRailBounded() {
        DetailThreadHistoryRenderer.State utilityRail = new DetailThreadHistoryRenderer.State(
            true,
            false,
            false,
            360
        );

        assertEquals(2, DetailThreadHistoryRenderer.visiblePriorTurnLimit(utilityRail));
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
            "A2 \u00B7 UNSURE \u00B7 GD-345",
            renderer.buildTurnLabel(2, false, turn("answer", "GD-345", 0L), "GD-220")
        );
        assertEquals(
            "A2 \u00B7 UNSURE \u00B7 GD-345",
            renderer.buildTurnLabel(2, false, turn("answer", "GD-345", 0L), "GD-345")
        );
    }

    @Test
    public void turnLabelsUseDotSeparatedTranscriptTime() {
        DetailThreadHistoryRenderer renderer = new DetailThreadHistoryRenderer(
            null,
            new DetailSessionPresentationFormatter(null),
            null
        );
        long timestamp = 15_660_000L;
        String expectedTime = new SimpleDateFormat("HH:mm", Locale.US).format(new Date(timestamp));

        assertEquals(
            "Q1 \u00B7 " + expectedTime + " \u00B7 FIELD QUESTION",
            renderer.buildTurnLabel(1, true, turn("answer", "GD-220", timestamp), "")
        );
        assertEquals(
            "A1 \u00B7 " + expectedTime + " \u00B7 UNSURE \u00B7 GD-220",
            renderer.buildTurnLabel(1, false, turn("answer", "GD-220", timestamp), "")
        );
    }

    @Test
    public void transcriptTurnsAppendCurrentTurnAfterEarlierTurns() {
        SessionMemory.TurnSnapshot first = turn("first answer", "GD-220", 0L);
        SessionMemory.TurnSnapshot second = new SessionMemory.TurnSnapshot(
            "what about runoff",
            "Shape the low edge for runoff.",
            "Shape the low edge for runoff.",
            List.of("GD-132"),
            List.of(source("GD-132")),
            "",
            0L
        );

        List<SessionMemory.TurnSnapshot> transcript = DetailThreadHistoryRenderer.transcriptTurns(
            List.of(first),
            second
        );

        assertEquals(2, transcript.size());
        assertEquals("question", transcript.get(0).question);
        assertEquals("what about runoff", transcript.get(1).question);
    }

    @Test
    public void transcriptTurnsDoNotDuplicateMatchingCurrentTurn() {
        SessionMemory.TurnSnapshot current = turn("answer", "GD-220", 0L);

        List<SessionMemory.TurnSnapshot> transcript = DetailThreadHistoryRenderer.transcriptTurns(
            List.of(current),
            current
        );

        assertEquals(1, transcript.size());
    }

    @Test
    public void statusLabelsUseScreenshotTranscriptTokens() {
        assertEquals("CONFIDENT", DetailThreadHistoryRenderer.compactStatusLabel("source-backed"));
        assertEquals("CONFIDENT", DetailThreadHistoryRenderer.compactStatusLabel("reviewed"));
        assertEquals("CONFIDENT", DetailThreadHistoryRenderer.compactStatusLabel("confident"));
        assertEquals("UNSURE", DetailThreadHistoryRenderer.compactStatusLabel("ready"));
        assertEquals("UNSURE", DetailThreadHistoryRenderer.compactStatusLabel("unsure"));
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

        assertEquals(96, compact.length());
        assertTrue(compact.endsWith("..."));
    }

    @Test
    public void utilityRailAnswerPreviewStaysTranscriptSized() {
        String answer = "A rain shelter answer with enough detail to become a full card if it is not compacted. "
            + "Keep only a short transcript preview in the rail and history surfaces.";

        String compact = DetailThreadHistoryRenderer.compactThreadAnswer(answer, true, text -> text);

        assertTrue(compact.length() <= 96);
        assertTrue(compact.startsWith("A rain shelter answer"));
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
            List.of("GD-001", "GD-002"),
            DetailThreadHistoryRenderer.guideChipIdsForTurn(turn)
        );
    }

    @Test
    public void guideChipLabelsKeepRainShelterContextInline() {
        SessionMemory.TurnSnapshot turn = new SessionMemory.TurnSnapshot(
            "how do i build a rain shelter",
            "Use a sloped tarp ridgeline.",
            "Use a sloped tarp ridgeline.",
            List.of("GD-111", "GD-345"),
            List.of(
                source("GD-111", "General camp setup", "camp layout"),
                source("GD-345", "Rain shelter", "tarp shelter rain")
            ),
            "",
            0L
        );

        assertEquals(
            List.of("GD-345", "GD-111"),
            DetailThreadHistoryRenderer.guideChipLabelsForTurn(turn)
        );
        assertEquals(
            List.of("GD-345", "GD-111"),
            DetailThreadHistoryRenderer.guideChipIdsForTurn(turn)
        );
    }

    @Test
    public void guideChipsStayOutOfUtilityRailRows() {
        DetailThreadHistoryRenderer.State utilityRail = new DetailThreadHistoryRenderer.State(
            true,
            false,
            false,
            360
        );
        DetailThreadHistoryRenderer.State detailTranscript = new DetailThreadHistoryRenderer.State(
            false,
            false,
            false,
            360
        );

        assertEquals(false, DetailThreadHistoryRenderer.shouldShowGuideChips(utilityRail, false));
        assertEquals(false, DetailThreadHistoryRenderer.shouldShowGuideChips(utilityRail, true));
        assertEquals(false, DetailThreadHistoryRenderer.shouldShowGuideChips(detailTranscript, false));
    }

    @Test
    public void guideChipLabelsPrioritizeRainShelterSourceOverAbrasivesAnchor() {
        SessionMemory.TurnSnapshot turn = new SessionMemory.TurnSnapshot(
            "how do i build a rain shelter",
            "Use a sloped tarp ridgeline.",
            "Use a sloped tarp ridgeline.",
            List.of("GD-220 Abrasives Manufacturing", "GD-345 Rain Shelter"),
            List.of(
                source("GD-220", "Abrasives Manufacturing", "abrasives manufacturing"),
                source("GD-345", "Rain shelter in wet weather", "field shelter rain tarp")
            ),
            "",
            0L
        );

        assertEquals(
            List.of("GD-345", "GD-220"),
            DetailThreadHistoryRenderer.guideChipLabelsForTurn(turn)
        );
        assertEquals(
            List.of("GD-345", "GD-220"),
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

    private static SearchResult source(String guideId, String title, String topicTags) {
        return new SearchResult(title, "", "", "", guideId, "", "", "", "", "", "", topicTags);
    }
}
