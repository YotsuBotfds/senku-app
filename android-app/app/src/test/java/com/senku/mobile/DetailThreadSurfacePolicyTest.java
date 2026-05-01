package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public final class DetailThreadSurfacePolicyTest {
    @Test
    public void turnSurfaceStateCombinesAnchorStatusAndVisiblePhoneLandscapeChip() {
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
            "answer_card:rain_shelter",
            0L
        );

        DetailThreadSurfacePolicy.TurnSurfaceState surfaceState = DetailThreadSurfacePolicy.turnSurfaceState(
            turn,
            "GD-220",
            phoneLandscapeNoRail,
            true,
            1,
            true
        );

        assertEquals("GD-345", surfaceState.answerAnchorGuideId);
        assertEquals("CONFIDENT", surfaceState.statusLabel);
        assertEquals(List.of("GD-345"), surfaceState.guideChipLabels);
        assertTrue(surfaceState.showGuideChips);
    }

    @Test
    public void turnSurfaceStateDoesNotPromoteRainShelterSourceOutsideReviewDemoPolicy() {
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
            "answer_card:rain_shelter",
            0L
        );

        DetailThreadSurfacePolicy.TurnSurfaceState surfaceState = DetailThreadSurfacePolicy.turnSurfaceState(
            turn,
            "",
            phoneLandscapeNoRail,
            true,
            1,
            false
        );

        assertEquals("GD-220", surfaceState.answerAnchorGuideId);
        assertEquals(List.of("GD-220"), surfaceState.guideChipLabels);
    }

    @Test
    public void turnSurfaceStateSuppressesUtilityRailChipsButKeepsFallbackAnchor() {
        DetailThreadHistoryRenderer.State utilityRail = new DetailThreadHistoryRenderer.State(
            true,
            false,
            false,
            360
        );
        SessionMemory.TurnSnapshot turn = new SessionMemory.TurnSnapshot(
            "what next",
            "Keep the tarp tension even.",
            "Keep the tarp tension even.",
            List.of(),
            List.of(),
            "",
            0L
        );

        DetailThreadSurfacePolicy.TurnSurfaceState surfaceState = DetailThreadSurfacePolicy.turnSurfaceState(
            turn,
            "GD-220",
            utilityRail,
            false,
            0,
            false
        );

        assertEquals("GD-220", surfaceState.answerAnchorGuideId);
        assertEquals("UNSURE", surfaceState.statusLabel);
        assertEquals(List.of(), surfaceState.guideChipLabels);
        assertFalse(surfaceState.showGuideChips);
    }

    @Test
    public void footerPolicyRequiresNonRailStateAndRenderableTranscript() {
        DetailThreadHistoryRenderer.State detailTranscript = new DetailThreadHistoryRenderer.State(
            false,
            false,
            false,
            360
        );
        DetailThreadHistoryRenderer.State utilityRail = new DetailThreadHistoryRenderer.State(
            true,
            false,
            false,
            360
        );
        List<SessionMemory.TurnSnapshot> transcript = List.of(
            new SessionMemory.TurnSnapshot(
                "question",
                "answer",
                "answer",
                List.of("GD-220"),
                List.of(source("GD-220")),
                "",
                0L
            )
        );

        assertTrue(DetailThreadSurfacePolicy.shouldShowThreadContextFooter(detailTranscript, transcript));
        assertFalse(DetailThreadSurfacePolicy.shouldShowThreadContextFooter(utilityRail, transcript));
        assertFalse(DetailThreadSurfacePolicy.shouldShowThreadContextFooter(detailTranscript, List.of()));
        assertEquals(
            "THREAD CONTEXT \u2022 1 TURN \u2022 GD-220 ANCHOR",
            DetailThreadSurfacePolicy.threadContextFooterLabel(transcript, "GD-220")
        );
    }

    private static SearchResult source(String guideId) {
        return new SearchResult("Source " + guideId, "", "", "", guideId, "", "", "");
    }

    private static SearchResult source(String guideId, String title, String topicTags) {
        return new SearchResult(title, "", "", "", guideId, "", "", "", "", "", "", topicTags);
    }
}
