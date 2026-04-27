package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class DetailSessionPresentationFormatterTest {
    @Test
    public void currentTurnSnapshotBuildsLabelsAndKeepsNonNullSources() {
        DetailSessionPresentationFormatter formatter = new DetailSessionPresentationFormatter(null);
        SearchResult first = source("Water", "GD-214", "");
        SearchResult second = source("Fallback title", "", "");

        SessionMemory.TurnSnapshot snapshot = formatter.currentTurnSnapshot(
            "current prompt",
            "body",
            Arrays.asList(first, second, null),
            "answer_card:test",
            ReviewedCardMetadata.empty(),
            1234L
        );

        assertEquals("current prompt", snapshot.question);
        assertEquals("body", snapshot.answerSummary);
        assertEquals("body", snapshot.answerBody);
        assertEquals(List.of("GD-214", "Fallback title", ""), snapshot.sources);
        assertEquals(List.of(first, second), snapshot.sourceResults);
        assertEquals("answer_card:test", snapshot.ruleId);
        assertEquals(1234L, snapshot.recordedAtEpochMs);
    }

    @Test
    public void earlierTurnsDropsOnlyMatchingCurrentTail() {
        DetailSessionPresentationFormatter formatter = new DetailSessionPresentationFormatter(null);
        SessionMemory.TurnSnapshot first = turn("First", List.of(), List.of());
        SessionMemory.TurnSnapshot other = turn("Other", List.of(), List.of());
        SessionMemory.TurnSnapshot current = turn(" Current Prompt ", List.of(), List.of());

        List<SessionMemory.TurnSnapshot> earlier = formatter.earlierTurns(
            List.of(first, other, current),
            "current prompt"
        );

        assertEquals(List.of(first, other), earlier);
        assertEquals(List.of(first, other), formatter.earlierTurns(List.of(first, other), "current prompt"));
    }

    @Test
    public void primaryGuideIdFallsBackFromSourceLabels() {
        DetailSessionPresentationFormatter formatter = new DetailSessionPresentationFormatter(null);

        assertEquals(
            "GD-444",
            formatter.primaryGuideIdForTurn(turn("", List.of("Source guide GD-444", "GD-214"), List.of()))
        );
        assertEquals("", formatter.primaryGuideIdForTurn(null));
    }

    @Test
    public void resolvePrimaryGuideIdPrefersSourceBeforeSubtitleMarker() {
        DetailSessionPresentationFormatter formatter = new DetailSessionPresentationFormatter(null);

        assertEquals(
            "GD-111",
            formatter.resolvePrimaryGuideId(List.of(source("Primary", "GD-111", "")), "Subtitle [GD-999]")
        );
        assertEquals(
            "GD-999",
            formatter.resolvePrimaryGuideId(List.of(source("Primary", "", "")), "Subtitle [GD-999]")
        );
    }

    @Test
    public void reopenedReviewedCardTurnKeepsConfidentReviewedShape() {
        DetailSessionPresentationFormatter formatter = new DetailSessionPresentationFormatter(null);
        SessionMemory.TurnSnapshot reviewedTurn = new SessionMemory.TurnSnapshot(
            "poisoning",
            "answer",
            "answer",
            List.of("GD-898"),
            List.of(source("Poisoning", "GD-898", "")),
            "answer_card:poisoning_unknown_ingestion",
            new ReviewedCardMetadata(
                "poisoning_unknown_ingestion",
                "GD-898",
                "pilot_reviewed",
                "reviewed_source_family",
                ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
                List.of("GD-898")
            ),
            null,
            1234L
        );

        assertEquals(
            OfflineAnswerEngine.AnswerMode.CONFIDENT,
            formatter.reopenedAnswerMode(reviewedTurn)
        );
        assertEquals(
            OfflineAnswerEngine.ConfidenceLabel.HIGH,
            formatter.reopenedConfidenceLabel(reviewedTurn)
        );
        assertEquals(
            ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
            reviewedTurn.reviewedCardMetadata.provenance
        );
        assertEquals(List.of("GD-898"), reviewedTurn.reviewedCardMetadata.citedReviewedSourceGuideIds);
    }

    @Test
    public void reopenedPlainTurnKeepsExistingUnlabeledShape() {
        DetailSessionPresentationFormatter formatter = new DetailSessionPresentationFormatter(null);
        SessionMemory.TurnSnapshot plainTurn = turn("plain", List.of("GD-214"), List.of());

        assertNull(formatter.reopenedAnswerMode(plainTurn));
        assertNull(formatter.reopenedConfidenceLabel(plainTurn));
    }

    private static SessionMemory.TurnSnapshot turn(
        String question,
        List<String> sourceLabels,
        List<SearchResult> sourceResults
    ) {
        return new SessionMemory.TurnSnapshot(
            question,
            "",
            "",
            sourceLabels,
            sourceResults,
            "",
            1234L
        );
    }

    private static SearchResult source(String title, String guideId, String subtitle) {
        return new SearchResult(title, subtitle, "", "", guideId, "", "", "");
    }
}
