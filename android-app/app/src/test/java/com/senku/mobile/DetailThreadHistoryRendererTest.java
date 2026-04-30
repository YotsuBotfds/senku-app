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
    public void inlineThreadKeepsChronologicalTurnOrderAcrossLayouts() {
        DetailThreadHistoryRenderer.State wideSideThread = new DetailThreadHistoryRenderer.State(
            false,
            true,
            false,
            720
        );
        DetailThreadHistoryRenderer.State compactPortrait = new DetailThreadHistoryRenderer.State(
            false,
            false,
            true,
            320
        );

        assertEquals(false, DetailThreadHistoryRenderer.shouldShowRecentTurnFirst(wideSideThread));
        assertEquals(false, DetailThreadHistoryRenderer.shouldShowRecentTurnFirst(compactPortrait));
    }

    @Test
    public void turnLabelsKeepAnswerAnchorMetadata() {
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
        assertEquals(
            "A2 \u00B7 ANCHOR GD-345",
            renderer.buildTurnLabel(2, false, turn("answer", "GD-345", 0L), "GD-345")
        );
    }

    @Test
    public void answerMetaUsesPrimaryAnchorWhileChipsKeepRelatedGuides() {
        DetailThreadHistoryRenderer renderer = new DetailThreadHistoryRenderer(
            null,
            new DetailSessionPresentationFormatter(null),
            null
        );
        SessionMemory.TurnSnapshot turn = new SessionMemory.TurnSnapshot(
            "How do I build a simple rain shelter from tarp and cord?",
            "Build a ridgeline first, then drape and tension the tarp around it.",
            "Build a ridgeline first, then drape and tension the tarp around it.",
            List.of("GD-220", "GD-132"),
            List.of(
                source("GD-220", "Abrasives Manufacturing", "abrasives manufacturing"),
                source("GD-132", "Foundry & Metal Casting", "foundry metal casting")
            ),
            "",
            0L
        );

        assertEquals("A1 \u00B7 ANCHOR GD-220", renderer.buildTurnLabel(1, false, turn, ""));
        assertEquals(List.of("GD-220", "GD-132"), DetailThreadHistoryRenderer.guideChipLabelsForTurn(turn));
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
            "A1 \u00B7 " + expectedTime + " \u00B7 ANCHOR GD-220",
            renderer.buildTurnLabel(1, false, turn("answer", "GD-220", timestamp), "")
        );
    }

    @Test
    public void answerTurnLabelFallsBackToPreviousAnchorWhenTurnHasNoGuide() {
        DetailThreadHistoryRenderer renderer = new DetailThreadHistoryRenderer(
            null,
            new DetailSessionPresentationFormatter(null),
            null
        );
        SessionMemory.TurnSnapshot turn = new SessionMemory.TurnSnapshot(
            "What next?",
            "Keep the tarp tension even.",
            "Keep the tarp tension even.",
            List.of(),
            List.of(),
            "",
            0L
        );

        assertEquals(
            "A2 \u00B7 ANCHOR GD-220",
            renderer.buildTurnLabel(2, false, turn, "GD-220")
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
        assertEquals("\u2022 CONFIDENT", DetailThreadHistoryRenderer.confidenceDotLabel("source-backed"));
        assertEquals("\u2022 UNSURE", DetailThreadHistoryRenderer.confidenceDotLabel("ready"));
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
    public void threadAnswerPreviewDropsAnswerDetailSections() {
        String answer = "Center the tarp and shape the low edge for runoff.\n\nFIELD STEPS\n1. Pull the tarp evenly.";

        assertEquals(
            "Center the tarp and shape the low edge for runoff.",
            DetailThreadHistoryRenderer.compactThreadAnswer(answer, false, text -> text)
        );
    }

    @Test
    public void threadAnswerPreviewSkipsAnswerProofHeadings() {
        String answer = "ANSWER\n"
            + "Field question\n"
            + "What should I do next after the ridge line is up?\n"
            + "Field entry - Moderate evidence\n"
            + "ANSWER\n"
            + "SOURCE MATCH\n"
            + "Center the tarp, tension the corners, and shape the low edge for runoff.\n"
            + "STEPS\n"
            + "1. Pull the tarp evenly across the ridgeline.";

        assertEquals(
            "Center the tarp, tension the corners, and shape the low edge for runoff.",
            DetailThreadHistoryRenderer.compactThreadAnswer(answer, false, text -> text)
        );
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
    public void detailTranscriptAnswerPreviewAllowsFullerRhythm() {
        String answer = "Drape the tarp evenly across the ridge with both edges hanging the same length. "
            + "Tension the four corners with taut-line hitches; aim for the windward edge to sit closest to the ground.";

        String compact = DetailThreadHistoryRenderer.compactThreadAnswer(answer, false, text -> text);

        assertEquals(answer, compact);
        assertTrue(compact.length() > 150);
    }

    @Test
    public void phoneLandscapeNoRailTranscriptUsesDenseThreadRhythm() {
        DetailThreadHistoryRenderer.State phoneLandscapeNoRail = new DetailThreadHistoryRenderer.State(
            false,
            true,
            false,
            360,
            true
        );

        assertEquals(true, DetailThreadHistoryRenderer.isPhoneLandscapeNoRailTranscript(phoneLandscapeNoRail, true));
        assertEquals(1, DetailThreadHistoryRenderer.questionMaxLines(phoneLandscapeNoRail, true, false));
        assertEquals(1, DetailThreadHistoryRenderer.answerMaxLines(phoneLandscapeNoRail, true));
        assertEquals(1, DetailThreadHistoryRenderer.visiblePriorTurnLimit(phoneLandscapeNoRail));
        assertEquals(13, DetailThreadHistoryRenderer.bodyTextSizeSp(false, true, false, phoneLandscapeNoRail));
        assertEquals(3, DetailThreadHistoryRenderer.questionBottomPaddingDp(true, false, phoneLandscapeNoRail));
        assertEquals(1, DetailThreadHistoryRenderer.answerBottomPaddingDp(true, false, phoneLandscapeNoRail));
    }

    @Test
    public void portraitWideAndRailKeepExistingThreadDensity() {
        DetailThreadHistoryRenderer.State compactPortrait = new DetailThreadHistoryRenderer.State(
            false,
            false,
            true,
            320
        );
        DetailThreadHistoryRenderer.State wide = new DetailThreadHistoryRenderer.State(
            false,
            true,
            false,
            720
        );
        DetailThreadHistoryRenderer.State utilityRail = new DetailThreadHistoryRenderer.State(
            true,
            false,
            false,
            360
        );

        assertEquals(false, DetailThreadHistoryRenderer.isPhoneLandscapeNoRailTranscript(compactPortrait, true));
        assertEquals(false, DetailThreadHistoryRenderer.isPhoneLandscapeNoRailTranscript(wide, true));
        assertEquals(false, DetailThreadHistoryRenderer.isPhoneLandscapeNoRailTranscript(utilityRail, false));
        assertEquals(4, DetailThreadHistoryRenderer.answerMaxLines(compactPortrait, true));
        assertEquals(4, DetailThreadHistoryRenderer.answerMaxLines(wide, true));
        assertEquals(2, DetailThreadHistoryRenderer.visiblePriorTurnLimit(wide));
        assertEquals(1, DetailThreadHistoryRenderer.answerMaxLines(utilityRail, false));
        assertEquals(15, DetailThreadHistoryRenderer.bodyTextSizeSp(false, true, false, compactPortrait));
        assertEquals(2, DetailThreadHistoryRenderer.questionMaxLines(utilityRail, false, true));
        assertEquals(4, DetailThreadHistoryRenderer.answerBottomPaddingDp(false, true, utilityRail));
    }

    @Test
    public void threadBodyCopyUsesRev03InkAcrossQuestionsAndAnswers() {
        assertEquals(R.color.senku_rev03_ink_0, DetailThreadHistoryRenderer.bodyTextColorRes(true));
        assertEquals(R.color.senku_rev03_ink_0, DetailThreadHistoryRenderer.bodyTextColorRes(false));
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
    public void compactAnchorLabelKeepsTwoSourceRhythmInline() {
        assertEquals("", DetailThreadHistoryRenderer.compactAnchorLabel(List.of()));
        assertEquals("GD-220", DetailThreadHistoryRenderer.compactAnchorLabel(List.of("GD-220")));
        assertEquals(
            "GD-220/GD-132",
            DetailThreadHistoryRenderer.compactAnchorLabel(List.of("GD-220", "GD-132"))
        );
        assertEquals(
            "GD-220/GD-132 +1",
            DetailThreadHistoryRenderer.compactAnchorLabel(List.of("GD-220", "GD-132", "GD-345"))
        );
    }

    @Test
    public void threadContextFooterKeepsTurnCountWithDynamicAnchor() {
        assertEquals(
            "THREAD CONTEXT \u2022 2 TURNS \u2022 GD-220 ANCHOR",
            DetailThreadHistoryRenderer.threadContextFooterLabel(
                List.of(turn("answer", "GD-220", 0L), turn("answer", "GD-345", 0L)),
                "GD-220"
            )
        );
        assertEquals(
            "THREAD CONTEXT \u2022 1 TURN",
            DetailThreadHistoryRenderer.threadContextFooterLabel(
                List.of(turn("answer", "", 0L)),
                ""
            )
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
    public void guideChipsStayOutOfUtilityRailRowsButShowInTranscriptRows() {
        DetailThreadHistoryRenderer.State utilityRail = new DetailThreadHistoryRenderer.State(
            true,
            false,
            false,
            360
        );
        DetailThreadHistoryRenderer.State detailTranscript = new DetailThreadHistoryRenderer.State(
            false,
            true,
            false,
            360,
            true
        );
        DetailThreadHistoryRenderer.State normalTranscript = new DetailThreadHistoryRenderer.State(
            false,
            false,
            false,
            360
        );

        assertEquals(false, DetailThreadHistoryRenderer.shouldShowGuideChips(utilityRail, false, 0));
        assertEquals(false, DetailThreadHistoryRenderer.shouldShowGuideChips(utilityRail, true, 0));
        assertEquals(true, DetailThreadHistoryRenderer.shouldShowGuideChips(normalTranscript, false, 0));
        assertEquals(true, DetailThreadHistoryRenderer.shouldShowGuideChips(normalTranscript, true, 0));
        assertEquals(true, DetailThreadHistoryRenderer.shouldShowGuideChips(detailTranscript, false, 0));
        assertEquals(true, DetailThreadHistoryRenderer.shouldShowGuideChips(detailTranscript, true, 0));
        assertEquals(true, DetailThreadHistoryRenderer.shouldShowGuideChips(detailTranscript, true, 1));
    }

    @Test
    public void phoneLandscapeTranscriptShowsOnlyContextualGuideChip() {
        DetailThreadHistoryRenderer.State phoneLandscapeNoRail = new DetailThreadHistoryRenderer.State(
            false,
            true,
            false,
            360,
            true
        );
        SessionMemory.TurnSnapshot turn = new SessionMemory.TurnSnapshot(
            "What should I do next after the ridge line is up?",
            "Drape the tarp evenly and tension the corners.",
            "Drape the tarp evenly and tension the corners.",
            List.of("GD-220 Abrasives Manufacturing", "GD-345 Rain Shelter"),
            List.of(
                source("GD-220", "Abrasives Manufacturing", "abrasives manufacturing"),
                source("GD-345", "Tarp & Cord Shelters", "tarp cord rain shelter ridgeline")
            ),
            "",
            0L
        );

        assertEquals(
            List.of("GD-220", "GD-345"),
            DetailThreadHistoryRenderer.guideChipLabelsForTurn(turn)
        );
        assertEquals(
            List.of("GD-345"),
            DetailThreadHistoryRenderer.visibleGuideChipLabelsForTurn(turn, phoneLandscapeNoRail, true)
        );
        assertEquals("GD-345", DetailThreadHistoryRenderer.answerAnchorGuideIdForTurn(turn, "GD-220"));
    }

    @Test
    public void guideChipLabelsKeepDeterministicThreadAnchorBeforeRainShelterSource() {
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
            List.of("GD-220", "GD-345"),
            DetailThreadHistoryRenderer.guideChipLabelsForTurn(turn)
        );
        assertEquals(
            List.of("GD-220", "GD-345"),
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
