package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class DetailProofPresentationFormatterTest {
    @Test
    public void compactReviewedCardLinesUseSourceLanguage() {
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
    public void proofLinesNormalizeLegacyProofRailResidueToSourcesLanguage() {
        DetailProofPresentationFormatter formatter = new DetailProofPresentationFormatter(null);
        DetailProofPresentationFormatter.State state = new DetailProofPresentationFormatter.State(
            "generated proof rail",
            1,
            "",
            "Proof rail reviewed",
            1,
            2,
            "",
            3,
            false,
            ReviewedCardMetadata.empty()
        );
        List<SearchResult> sources = Arrays.asList(
            new SearchResult(
                "Proof rail anchor",
                "",
                "Source residue should be reader-facing.",
                "",
                "GD-101",
                "Proof rail section",
                "survival",
                "hybrid"
            )
        );

        String visible = formatter.buildWhySummaryPlainText(state, sources, false, false);

        assertFalse(visible.toLowerCase().contains("proof rail"));
        assertFalse(visible.toLowerCase().contains("proof"));
        assertEquals(
            "SOURCES  sources reviewed | 1 source\n" +
                "GUIDE  [GD-101] sources anchor\n" +
                "MATCH  generated sources\n" +
                "SECTION  sources section\n" +
                "MATCH  Best match",
            visible
        );
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
        assertEquals("[GD-345] - Tarp & Cord Shelters", formatter.buildPrimarySourcePreviewLine(sources));
        assertEquals("[GD-345] Tarp & Cord Shelters", formatter.buildSourceEntryValue(sources.get(0), sources));
    }

    @Test
    public void primarySourceLabelUsesRainShelterArticleForReviewedSourceStack() {
        DetailProofPresentationFormatter formatter = new DetailProofPresentationFormatter(null);
        List<SearchResult> sources = Arrays.asList(
            new SearchResult(
                "Abrasives Manufacturing",
                "",
                "Reviewed anchor context.",
                "",
                "GD-220",
                "Abrasives Manufacturing",
                "materials",
                "hybrid"
            ),
            new SearchResult(
                "Foundry & Metal Casting",
                "",
                "Reviewed related context.",
                "",
                "GD-132",
                "Foundry & Metal Casting",
                "metal",
                "guide-focus"
            ),
            new SearchResult(
                "Tarp & Cord Shelters",
                "",
                "A simple ridgeline shelter requires only tarp, cord, and two anchor points.",
                "Pitch the low edge toward weather so rain sheds away from the sheltered area.",
                "GD-345",
                "Tarp & Cord Shelters",
                "survival",
                "guide-focus",
                "topic",
                "immediate",
                "emergency_shelter",
                "tarp,cord,rain_shelter,ridgeline"
            )
        );

        assertEquals("Tarp & Cord Shelters", formatter.buildPrimarySourceLabel(sources));
        assertEquals("[GD-345] - Tarp & Cord Shelters", formatter.buildPrimarySourcePreviewLine(sources));
        assertEquals("[GD-345] Tarp & Cord Shelters", formatter.buildSourceEntryValue(sources.get(2), sources));
    }

    @Test
    public void primarySourceLabelUsesEmergencyIdentityForGd132BurnHazardAnchor() {
        DetailProofPresentationFormatter formatter = new DetailProofPresentationFormatter(null);
        List<SearchResult> sources = Arrays.asList(
            new SearchResult(
                "Foundry & Metal Casting - \u00a71 Area readiness",
                "",
                "A single drop of water contacting molten metal causes a violent steam explosion.",
                "Stop work immediately. Move to minimum 5 m from active work zone.",
                "GD-132",
                "Foundry & Metal Casting",
                "metal",
                "hybrid",
                "reviewed_card",
                "immediate",
                "foundry_burn_hazard",
                "burn hazard,area_readiness"
            )
        );

        assertEquals("Burn hazard response", formatter.buildPrimarySourceLabel(sources));
        assertEquals("[GD-132] - Burn hazard response", formatter.buildPrimarySourcePreviewLine(sources));
        assertEquals("[GD-132] Burn hazard response", formatter.buildSourceEntryValue(sources.get(0), sources));
    }

    @Test
    public void compactWhySummaryUsesSourceLedArticleStackWithoutMatchDiagnostics() {
        DetailProofPresentationFormatter formatter = new DetailProofPresentationFormatter(null);
        List<SearchResult> sources = Arrays.asList(
            new SearchResult(
                "Abrasives Manufacturing",
                "",
                "Reviewed anchor context.",
                "",
                "GD-220",
                "Abrasives Manufacturing",
                "materials",
                "hybrid"
            ),
            new SearchResult(
                "Foundry & Metal Casting",
                "",
                "Pitch the ridgeline along prevailing wind.",
                "",
                "GD-132",
                "Foundry & Metal Casting",
                "metal",
                "guide-focus"
            ),
            new SearchResult(
                "Tarp & Cord Shelters",
                "",
                "A simple ridgeline shelter requires only tarp, cord, and two anchor points.",
                "Pitch the low edge toward weather so rain sheds away from the sheltered area.",
                "GD-345",
                "Tarp & Cord Shelters",
                "survival",
                "guide-focus",
                "topic",
                "immediate",
                "emergency_shelter",
                "tarp,cord,rain_shelter,ridgeline"
            )
        );
        DetailProofPresentationFormatter.State state = new DetailProofPresentationFormatter.State(
            "generated",
            1,
            "this device",
            "Uncertain",
            3,
            2,
            "",
            3,
            true,
            ReviewedCardMetadata.empty()
        );

        String visible = formatter.buildWhySummaryPlainText(state, sources, true, false);

        assertEquals(
            "GUIDE  [GD-345] Tarp & Cord Shelters\nSOURCES  3 sources",
            visible
        );
        assertFalse(visible.contains("MATCH"));
        assertFalse(visible.contains("FIT"));
    }

    @Test
    public void noCitationSummaryUsesSourcesLanguageWithoutProofLabels() {
        DetailProofPresentationFormatter formatter = new DetailProofPresentationFormatter(null);
        DetailProofPresentationFormatter.State state = new DetailProofPresentationFormatter.State(
            "generated proof",
            1,
            "host proof",
            "Limited",
            0,
            2,
            "04-27",
            3,
            false,
            ReviewedCardMetadata.empty()
        );

        String visible = formatter.buildWhySummaryPlainText(state, List.of(), false, false);

        assertFalse(visible.toLowerCase().contains("proof"));
        assertEquals(
            "MATCH  generated sources\n" +
                "ENGINE  host sources\n" +
                "SOURCES  No guide citations attached yet\n" +
                "LIBRARY  No cited guide lines surfaced from the installed pack for this answer yet.\n" +
                "REVISION  04-27\n" +
                "CHECK  Reopen search or inspect a guide directly before relying on this.",
            visible
        );
    }

}
