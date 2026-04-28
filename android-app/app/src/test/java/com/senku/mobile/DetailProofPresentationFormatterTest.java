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

        assertEquals("CARD", lines.get(0).label);
        assertEquals("burns_pilot_card | pilot reviewed", lines.get(0).value);
        assertEquals("ANCHOR", lines.get(1).label);
        assertEquals("GD-380", lines.get(1).value);
        assertEquals(2, lines.size());
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

        assertEquals("CARD", lines.get(0).label);
        assertEquals("shock_card | pilot reviewed", lines.get(0).value);
        assertEquals("ANCHOR", lines.get(1).label);
        assertEquals("GD-400", lines.get(1).value);
        assertEquals("RELATED", lines.get(2).label);
        assertEquals("GD-401, GD-402", lines.get(2).value);
        for (DetailProofPresentationFormatter.CompactReviewedCardLine line : lines) {
            String visible = line.label + " " + line.value;
            assertFalse(visible.contains("runtime_card_only"));
            assertFalse(visible.contains(ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME));
        }
    }

    @Test
    public void retrievalModesUseReaderFacingMatchTypes() {
        assertEquals("MATCH", DetailProofPresentationFormatter.MATCH_TYPE_LABEL);
        assertEquals("Best match", DetailProofPresentationFormatter.humanizeRetrievalMode("route-focus"));
        assertEquals("Related", DetailProofPresentationFormatter.humanizeRetrievalMode("guide-focus"));
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

    @Test
    public void primarySourceLabelPrefersRainShelterArticleIdentityOverSectionTakeover() {
        DetailProofPresentationFormatter formatter = new DetailProofPresentationFormatter(null);
        List<SearchResult> sources = Arrays.asList(
            new SearchResult(
                "Primitive Shelter Construction Techniques",
                "",
                "Use tarp and cord to form a simple ridgeline shelter.",
                "How do I build a simple rain shelter from tarp and cord?",
                "GD-345",
                "Wood Quality Evaluation for Shelter Construction",
                "survival",
                "lexical",
                "",
                "",
                "emergency_shelter",
                "foundation,weatherproofing,site_selection"
            ),
            new SearchResult(
                "Shelter Site Selection & Hazard Assessment",
                "",
                "Choose drainage before pitching a tarp.",
                "",
                "GD-446",
                "Identifying Natural Hazards",
                "survival",
                "hybrid"
            )
        );

        assertEquals("Tarp & Cord Shelters", formatter.buildPrimarySourceLabel(sources));
        assertEquals("ANCHOR [GD-345] - Tarp & Cord Shelters", formatter.buildPrimarySourcePreviewLine(sources));
        assertEquals("[GD-345] Tarp & Cord Shelters", formatter.buildSourceEntryValue(sources.get(0), sources));
    }

}
