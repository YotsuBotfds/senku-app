package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.senku.ui.tablet.AnchorState;

import java.util.List;

import org.junit.Test;

public final class DetailTabletEvidencePolicyTest {
    @Test
    public void sameSelectionIsNoOpAndKeepsCurrentToken() {
        SearchResult active = source(" GD-220 ", " Abrasives ", " Materials ");

        DetailTabletEvidencePolicy.SelectionDecision decision =
            DetailTabletEvidencePolicy.decideSelection(
                active,
                "gd-220|materials|abrasives",
                7
            );

        assertEquals(DetailTabletEvidencePolicy.SelectionAction.NO_OP, decision.action);
        assertFalse(decision.shouldApplySelection());
        assertFalse(decision.shouldLoadPreview());
        assertEquals(7, decision.requestToken);
        assertEquals("GD-220", decision.guideId);
    }

    @Test
    public void blankGuideClearsSelectionButDoesNotLoadPreview() {
        SearchResult active = source(" ", " Untitled ", " ");

        DetailTabletEvidencePolicy.SelectionDecision decision =
            DetailTabletEvidencePolicy.decideSelection(active, "old-key", 2);

        assertEquals(DetailTabletEvidencePolicy.SelectionAction.CLEAR_WITHOUT_LOAD, decision.action);
        assertTrue(decision.shouldApplySelection());
        assertFalse(decision.shouldLoadPreview());
        assertEquals("||untitled", decision.selectionKey);
        assertEquals("", decision.guideId);
        assertEquals("Untitled", decision.anchorTitle);
        assertEquals("", decision.anchorSection);
        assertEquals(3, decision.requestToken);
    }

    @Test
    public void changedGuideSelectionRequestsPreviewLoad() {
        SearchResult active = source(" GD-345 ", " Rain Shelter ", " Rigging ");

        DetailTabletEvidencePolicy.SelectionDecision decision =
            DetailTabletEvidencePolicy.decideSelection(active, "", 4);

        assertEquals(DetailTabletEvidencePolicy.SelectionAction.LOAD_PREVIEW, decision.action);
        assertTrue(decision.shouldApplySelection());
        assertTrue(decision.shouldLoadPreview());
        assertEquals("gd-345|rigging|rain shelter", decision.selectionKey);
        assertEquals("GD-345", decision.guideId);
        assertEquals("Rain Shelter", decision.anchorTitle);
        assertEquals("Rigging", decision.anchorSection);
        assertEquals(5, decision.requestToken);
    }

    @Test
    public void loadedPreviewUsesLoadedGuideWhenInitialAnchorLabelsAreBlank() {
        DetailTabletEvidencePolicy.SelectionDecision selection =
            DetailTabletEvidencePolicy.decideSelection(source("GD-499", " ", ""), "", 9);
        SearchResult loadedGuide = source(" GD-499 ", " Required Prep ", " Safety ");
        SearchResult xref = source("GD-225", "Clay Safety", "Heat");

        DetailTabletEvidencePolicy.LoadedPreview preview =
            DetailTabletEvidencePolicy.buildLoadedPreview(
                selection,
                " Keep clear of hot surfaces. ",
                loadedGuide,
                List.of(xref)
            );

        assertEquals("gd-499||", preview.selectionKey);
        assertEquals("GD-499", preview.guideId);
        assertEquals("Required Prep", preview.title);
        assertEquals("Safety", preview.section);
        assertEquals("Keep clear of hot surfaces.", preview.snippet);
        assertEquals(1, preview.xrefs.size());
        assertSame(xref, preview.xrefs.get(0));
    }

    @Test
    public void loadedPreviewPreservesInitialAnchorLabelsOverLoadedGuide() {
        DetailTabletEvidencePolicy.SelectionDecision selection =
            DetailTabletEvidencePolicy.decideSelection(
                source("GD-345", " Rain Shelter ", " Rigging "),
                "",
                1
            );

        DetailTabletEvidencePolicy.LoadedPreview preview =
            DetailTabletEvidencePolicy.buildLoadedPreview(
                selection,
                "",
                source("GD-345", "Loaded title", "Loaded section"),
                List.of()
            );

        assertEquals("Rain Shelter", preview.title);
        assertEquals("Rigging", preview.section);
    }

    @Test
    public void staleOrMismatchedLoadedPreviewIsRejected() {
        DetailTabletEvidencePolicy.SelectionDecision selection =
            DetailTabletEvidencePolicy.decideSelection(
                source("GD-345", "Rain Shelter", "Rigging"),
                "",
                4
            );
        DetailTabletEvidencePolicy.LoadedPreview preview =
            DetailTabletEvidencePolicy.buildLoadedPreview(selection, "snippet", null, List.of());

        assertTrue(DetailTabletEvidencePolicy.shouldApplyLoadedPreview(
            5,
            5,
            "gd-345|rigging|rain shelter",
            preview,
            false,
            false
        ));
        assertFalse(DetailTabletEvidencePolicy.shouldApplyLoadedPreview(
            4,
            5,
            "gd-345|rigging|rain shelter",
            preview,
            false,
            false
        ));
        assertFalse(DetailTabletEvidencePolicy.shouldApplyLoadedPreview(
            5,
            5,
            "other-selection",
            preview,
            false,
            false
        ));
        assertFalse(DetailTabletEvidencePolicy.shouldApplyLoadedPreview(
            5,
            5,
            "gd-345|rigging|rain shelter",
            preview,
            true,
            false
        ));
        assertFalse(DetailTabletEvidencePolicy.shouldApplyLoadedPreview(
            5,
            5,
            "gd-345|rigging|rain shelter",
            preview,
            false,
            true
        ));
        assertFalse(DetailTabletEvidencePolicy.shouldApplyLoadedPreview(
            5,
            5,
            "gd-345|rigging|rain shelter",
            null,
            false,
            false
        ));
    }

    @Test
    public void lateLoadedPreviewForPreviousSelectionCannotOverwriteCurrentEvidence() {
        SearchResult first = source("GD-220", "Abrasives Manufacturing", "Materials");
        SearchResult second = source("GD-345", "Rain Shelter", "Rigging");

        DetailTabletEvidencePolicy.SelectionDecision firstSelection =
            DetailTabletEvidencePolicy.decideSelection(first, "", 10);
        DetailTabletEvidencePolicy.LoadedPreview firstPreview =
            DetailTabletEvidencePolicy.buildLoadedPreview(
                firstSelection,
                "Pitch ridgeline along prevailing wind.",
                null,
                List.of(source("GD-132", "Foundry", "Safety"))
            );
        DetailTabletEvidencePolicy.SelectionDecision secondSelection =
            DetailTabletEvidencePolicy.decideSelection(
                second,
                firstSelection.selectionKey,
                firstSelection.requestToken
            );
        DetailTabletEvidencePolicy.LoadedPreview secondPreview =
            DetailTabletEvidencePolicy.buildLoadedPreview(
                secondSelection,
                "Drape the tarp evenly.",
                null,
                List.of(source("GD-294", "Cold Shelter", "Related"))
            );

        assertFalse(DetailTabletEvidencePolicy.shouldApplyLoadedPreview(
            firstSelection.requestToken,
            secondSelection.requestToken,
            secondSelection.selectionKey,
            firstPreview,
            false,
            false
        ));
        assertTrue(DetailTabletEvidencePolicy.shouldApplyLoadedPreview(
            secondSelection.requestToken,
            secondSelection.requestToken,
            secondSelection.selectionKey,
            secondPreview,
            false,
            false
        ));

        AnchorState anchor = DetailTabletStateBuilder.buildAnchorState(
            true,
            "",
            second,
            secondPreview.selectionKey,
            secondPreview.guideId,
            secondPreview.title,
            secondPreview.section,
            secondPreview.snippet
        );

        assertEquals("gd-345|rigging|rain shelter", anchor.getKey());
        assertEquals("GD-345", anchor.getId());
        assertEquals("Rain Shelter", anchor.getTitle());
        assertEquals("Drape the tarp evenly.", anchor.getSnippet());
    }

    @Test
    public void xrefHelpersCapLoadCountAndCreateMissingGuidePlaceholder() {
        assertTrue(DetailTabletEvidencePolicy.canLoadMoreXRefs(5));
        assertFalse(DetailTabletEvidencePolicy.canLoadMoreXRefs(6));

        SearchResult loaded = source("GD-111", "Loaded", "");
        assertSame(loaded, DetailTabletEvidencePolicy.resolvedXRefGuide("GD-111", loaded));

        SearchResult placeholder = DetailTabletEvidencePolicy.resolvedXRefGuide(" GD-222 ", null);
        assertEquals("GD-222", placeholder.guideId);
        assertEquals("", placeholder.title);
    }

    private static SearchResult source(String guideId, String title, String section) {
        return new SearchResult(title, "", "", "", guideId, section, "", "");
    }
}
