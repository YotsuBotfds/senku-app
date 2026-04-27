package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class DetailProofPresentationFormatterTest {
    @Test
    public void compactReviewedCardLinesUseUiProofLanguage() {
        ReviewedCardMetadata metadata = new ReviewedCardMetadata(
            "burns_pilot_card",
            "GD-380",
            "pilot_reviewed",
            "runtime_card_only",
            ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
            Arrays.asList("GD-380")
        );

        List<DetailProofPresentationFormatter.CompactReviewedCardLine> lines =
            DetailProofPresentationFormatter.compactReviewedCardLines(metadata);

        assertEquals("Reviewed guide card burns_pilot_card", lines.get(0).label);
        assertEquals("available for review", lines.get(0).value);
        assertEquals("Cites guide GD-380", lines.get(1).label);
        assertEquals("guide anchor", lines.get(1).value);
        assertEquals("Status", lines.get(2).label);
        assertEquals("Status: pilot reviewed", lines.get(2).value);
        assertEquals("Limit", lines.get(3).label);
        assertEquals(
            "Limit: reviewed support only; inspect cited guide before relying.",
            lines.get(3).value
        );
        assertEquals(4, lines.size());
    }

    @Test
    public void compactReviewedCardLinesAddDistinctSupportGuidesWithoutRawDiagnostics() {
        ReviewedCardMetadata metadata = new ReviewedCardMetadata(
            "shock_card",
            "GD-400",
            "pilot_reviewed",
            "runtime_card_only",
            ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
            Arrays.asList("GD-401", "GD-402")
        );

        List<DetailProofPresentationFormatter.CompactReviewedCardLine> lines =
            DetailProofPresentationFormatter.compactReviewedCardLines(metadata);

        assertEquals("Reviewed guide card shock_card", lines.get(0).label);
        assertEquals("Cites guide GD-400", lines.get(1).label);
        assertEquals("Cites guide GD-401, GD-402", lines.get(2).label);
        assertEquals("supporting guide", lines.get(2).value);
        for (DetailProofPresentationFormatter.CompactReviewedCardLine line : lines) {
            String visible = line.label + " " + line.value;
            assertFalse(visible.contains("runtime_card_only"));
            assertFalse(visible.contains(ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME));
        }
    }

    @Test
    public void retrievalModesUseReaderFacingMatchTypes() {
        assertEquals("Match type", DetailProofPresentationFormatter.MATCH_TYPE_LABEL);
        assertEquals("Best match", DetailProofPresentationFormatter.humanizeRetrievalMode("route-focus"));
        assertEquals("Related guide", DetailProofPresentationFormatter.humanizeRetrievalMode("guide-focus"));
        assertEquals("Generated answer", DetailProofPresentationFormatter.humanizeRetrievalMode("ai-generated"));
        assertEquals("Concept match", DetailProofPresentationFormatter.humanizeRetrievalMode("vector"));
        assertEquals("Keyword match", DetailProofPresentationFormatter.humanizeRetrievalMode("lexical"));
    }

    @Test
    public void retrievalModeFallbackRemovesRawTokenSeparators() {
        String visible = DetailProofPresentationFormatter.humanizeRetrievalMode("source_anchor_boost");

        assertEquals("Source Anchor Boost", visible);
        assertFalse(visible.contains("_"));
        assertFalse(visible.contains("-"));
    }
}
