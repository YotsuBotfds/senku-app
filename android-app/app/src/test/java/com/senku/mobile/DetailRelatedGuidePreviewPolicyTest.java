package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public final class DetailRelatedGuidePreviewPolicyTest {
    @Test
    public void previewBodyFallsBackThroughExistingVisibleTextOrder() {
        SearchResult guide = new SearchResult(
            "Rainwater Catchment",
            "Subtitle fallback",
            "",
            "",
            "GD-215",
            "",
            "",
            ""
        );

        assertEquals(
            "Guide body",
            DetailRelatedGuidePreviewPolicy.resolvePreviewBody(
                " Guide body ",
                guide,
                "Context",
                "Loading",
                "Empty",
                true
            )
        );
        assertEquals(
            "Subtitle fallback",
            DetailRelatedGuidePreviewPolicy.resolvePreviewBody("", guide, "Context", "Loading", "Empty", true)
        );
        assertEquals(
            "Context",
            DetailRelatedGuidePreviewPolicy.resolvePreviewBody(
                "",
                new SearchResult("Rainwater Catchment", "", "", "", "GD-215", "", "", ""),
                " Context ",
                "Loading",
                "Empty",
                true
            )
        );
        assertEquals(
            "Loading",
            DetailRelatedGuidePreviewPolicy.resolvePreviewBody(
                "",
                new SearchResult("", "", "", "", "", "", "", ""),
                "",
                "Loading",
                "Empty",
                true
            )
        );
        assertEquals(
            "Empty",
            DetailRelatedGuidePreviewPolicy.resolvePreviewBody(
                "",
                new SearchResult("", "", "", "", "", "", "", ""),
                "",
                "Loading",
                "Empty",
                false
            )
        );
    }

    @Test
    public void selectedSourceUsesSelectedRealSourceBeforeFirstSource() {
        SearchResult first = result("GD-214", "Water Storage");
        SearchResult selected = result("GD-215", "Rainwater Catchment");
        String selectedKey = DetailProvenancePresentationFormatter.buildSourceSelectionKey(selected);

        assertEquals(
            selected,
            DetailRelatedGuidePreviewPolicy.selectedSourceForRelatedGuideGraph(
                true,
                false,
                selectedKey,
                Arrays.asList(first, selected)
            )
        );
    }

    @Test
    public void selectedSourceRejectsSelectedPlaceholder() {
        SearchResult placeholder = result("", "Placeholder");

        assertNull(
            DetailRelatedGuidePreviewPolicy.selectedSourceForRelatedGuideGraph(
                true,
                false,
                DetailProvenancePresentationFormatter.buildSourceSelectionKey(placeholder),
                Arrays.asList(placeholder, result("GD-214", "Water Storage"))
            )
        );
    }

    @Test
    public void staleSelectedSourceKeyFallsBackToFirstRealSourceAfterSourceListRefresh() {
        SearchResult placeholder = result("", "Placeholder");
        SearchResult firstRealSource = result("GD-214", "Water Storage");
        SearchResult secondRealSource = result("GD-215", "Rainwater Catchment");

        assertEquals(
            firstRealSource,
            DetailRelatedGuidePreviewPolicy.selectedSourceForRelatedGuideGraph(
                true,
                false,
                "gd-999|stale|source",
                Arrays.asList(placeholder, firstRealSource, secondRealSource)
            )
        );
    }

    @Test
    public void previewStateDecisionsPreserveCurrentVisibilityRules() {
        assertTrue(DetailRelatedGuidePreviewPolicy.shouldUsePreviewPanel(true, true, true, true));
        assertFalse(DetailRelatedGuidePreviewPolicy.shouldUsePreviewPanel(true, true, false, true));
        assertTrue(DetailRelatedGuidePreviewPolicy.shouldUseAnswerModePreviewPanel(true, true, true));
        assertFalse(DetailRelatedGuidePreviewPolicy.shouldUseAnswerModePreviewPanel(false, true, true));
        assertTrue(DetailRelatedGuidePreviewPolicy.shouldRenderActiveGuideContextPanel(true, true, false, ""));
        assertFalse(DetailRelatedGuidePreviewPolicy.shouldRenderActiveGuideContextPanel(true, true, true, ""));
        assertTrue(DetailRelatedGuidePreviewPolicy.shouldRenderActiveGuideContextPanel(true, true, true, "gd-215"));
    }

    @Test
    public void loadedPreviewAppliesOnlyToCurrentSelectedPanelState() {
        SearchResult guide = result("GD-215", "Rainwater Catchment");
        String key = DetailRelatedGuidePreviewPolicy.buildSelectionKey(guide);

        assertTrue(DetailRelatedGuidePreviewPolicy.shouldLoadPreviewGuide(guide));
        assertFalse(DetailRelatedGuidePreviewPolicy.shouldLoadPreviewGuide(result("", "No ID")));
        assertTrue(DetailRelatedGuidePreviewPolicy.shouldApplyLoadedPreview(false, false, 4, 4, key, key));
        assertFalse(DetailRelatedGuidePreviewPolicy.shouldApplyLoadedPreview(false, false, 4, 5, key, key));
        assertFalse(DetailRelatedGuidePreviewPolicy.shouldApplyLoadedPreview(false, false, 4, 4, key, "other"));
        assertFalse(DetailRelatedGuidePreviewPolicy.shouldApplyLoadedPreview(true, false, 4, 4, key, key));
    }

    private static SearchResult result(String guideId, String title) {
        return new SearchResult(title, "", "", "", guideId, "", "", "");
    }
}
