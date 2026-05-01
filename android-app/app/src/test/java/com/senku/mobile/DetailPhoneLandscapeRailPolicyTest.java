package com.senku.mobile;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class DetailPhoneLandscapeRailPolicyTest {
    @Test
    public void guideSectionRailUsesLandscapeGuideSectionsAndDemoOverride() {
        SearchResult guide = source(
            "Foundry & Metal Casting",
            "GD-132",
            "",
            "## Section 2: REQUIRED READING\nRead the guide.\n"
                + "## -- \u00a7 4: MATERIAL LABELING\nLabel the bin."
        );

        assertTrue(DetailPhoneLandscapeRailPolicy.shouldUseGuideSectionRail(false, true));
        assertFalse(DetailPhoneLandscapeRailPolicy.shouldUseGuideSectionRail(true, true));
        assertEquals(
            Arrays.asList("\u00a71  Required reading", "\u00a72  Material labeling"),
            DetailPhoneLandscapeRailPolicy.guideSectionRailLabels(guide, false)
        );
        assertEquals(
            Arrays.asList(
                "\u00a71  Area readiness",
                "\u00a72  Required reading",
                "\u00a73  Hazard screen",
                "\u00a74  Material labeling",
                "\u00a75  No-go triggers",
                "\u00a76  Access control",
                "\u00a77  Owner handoff"
            ),
            DetailPhoneLandscapeRailPolicy.guideSectionRailLabels(guide, true)
        );
        assertEquals(17, DetailPhoneLandscapeRailPolicy.guideSectionRailCount(guide, true));
    }

    @Test
    public void answerSourceRailThreadPolicyPreservesTopAndTitleBehavior() {
        assertTrue(DetailPhoneLandscapeRailPolicy.shouldUseSourceRail(true, true));
        assertFalse(DetailPhoneLandscapeRailPolicy.shouldUseSourceRail(false, true));
        assertTrue(DetailPhoneLandscapeRailPolicy.shouldKeepThreadAtTop(true, 2, true));
        assertFalse(DetailPhoneLandscapeRailPolicy.shouldKeepThreadAtTop(true, 1, true));
        assertFalse(DetailPhoneLandscapeRailPolicy.shouldAutoOpenProvenanceForAnswerRail(true, 2, true));
        assertTrue(DetailPhoneLandscapeRailPolicy.shouldAutoOpenProvenanceForAnswerRail(true, 1, true));
        assertEquals("SOURCES - 2", DetailPhoneLandscapeRailPolicy.buildSourceRailTitle("Source guides", 2));
        assertArrayEquals(
            new long[] {0L, 80L, 240L, 480L, 900L, 1400L},
            DetailPhoneLandscapeRailPolicy.threadTopPreservationDelaysMs()
        );
    }

    @Test
    public void visibleSourceRailMergesOneRepresentativePerThreadTurnOnlyInLandscapeThread() {
        SearchResult initialAnchor = source("Abrasives Manufacturing", "GD-220", "", "abrasives manufacturing");
        SearchResult duplicateAnchor = source("Abrasives Manufacturing", "GD-220", "", "duplicate");
        SearchResult relatedSafety = source("Foundry & Metal Casting", "GD-132", "", "foundry metal casting");
        SearchResult currentAnchor = source("Tarp & Cord Shelters", "GD-345", "", "rain shelter tarp cord");

        List<SessionMemory.TurnSnapshot> snapshots = Arrays.asList(new SessionMemory.TurnSnapshot(
            "How do I build a simple rain shelter from tarp and cord?",
            "Build a ridgeline first.",
            "Build a ridgeline first.",
            Arrays.asList("GD-220", "GD-132"),
            Arrays.asList(initialAnchor, relatedSafety),
            null,
            0L
        ));

        List<SearchResult> merged = DetailPhoneLandscapeRailPolicy.resolveVisibleSourceRailSourcesForState(
            true,
            true,
            true,
            Arrays.asList(duplicateAnchor, currentAnchor),
            snapshots
        );
        assertEquals(1, merged.size());
        assertEquals("GD-220", merged.get(0).guideId);

        List<SearchResult> unmerged = DetailPhoneLandscapeRailPolicy.resolveVisibleSourceRailSourcesForState(
            true,
            true,
            false,
            Arrays.asList(duplicateAnchor, currentAnchor),
            snapshots
        );
        assertEquals(2, unmerged.size());
        assertEquals("GD-220", unmerged.get(0).guideId);
        assertEquals("GD-345", unmerged.get(1).guideId);
    }

    private static SearchResult source(String title, String guideId, String sectionHeading, String body) {
        return new SearchResult(title, "", body, body, guideId, sectionHeading, "", "");
    }
}
