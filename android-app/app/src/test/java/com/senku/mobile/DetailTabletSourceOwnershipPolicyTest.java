package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class DetailTabletSourceOwnershipPolicyTest {
    @Test
    public void activeSourceSelectionPreservesMatchingSelectedSourceKey() {
        SearchResult first = source("GD-220", "Abrasives Manufacturing", "Materials");
        SearchResult selected = source("GD-132", "Foundry", "Casting");

        DetailTabletSourceOwnershipPolicy.ActiveSourceSelection selection =
            DetailTabletSourceOwnershipPolicy.resolveActiveSource(
                List.of(first, selected),
                "gd-132|casting|foundry"
            );

        assertSame(selected, selection.source);
        assertEquals("gd-132|casting|foundry", selection.selectedKey);
    }

    @Test
    public void activeSourceSelectionFallsBackToFirstRealSourceAndKey() {
        SearchResult blank = new SearchResult(" ", "", "", "", "", " ", "", "");
        SearchResult firstReal = source("GD-345", "Rain Shelter", "Rigging");

        DetailTabletSourceOwnershipPolicy.ActiveSourceSelection selection =
            DetailTabletSourceOwnershipPolicy.resolveActiveSource(
                Arrays.asList(blank, firstReal),
                "missing"
            );

        assertSame(firstReal, selection.source);
        assertEquals("gd-345|rigging|rain shelter", selection.selectedKey);
    }

    @Test
    public void activeSourceSelectionKeepsBlankRowWhenNoRealSourceExists() {
        SearchResult blank = new SearchResult(" ", "", "", "", "", " ", "", "");

        DetailTabletSourceOwnershipPolicy.ActiveSourceSelection selection =
            DetailTabletSourceOwnershipPolicy.resolveActiveSource(List.of(blank), "");

        assertSame(blank, selection.source);
        assertEquals("||", selection.selectedKey);
    }

    @Test
    public void activeSourceSelectionAllowsNullForEmptySources() {
        DetailTabletSourceOwnershipPolicy.ActiveSourceSelection selection =
            DetailTabletSourceOwnershipPolicy.resolveActiveSource(List.of(), "missing");

        assertNull(selection.source);
        assertEquals("", selection.selectedKey);
    }

    @Test
    public void tabletGuideIdUsesSourceThenFallbacksWhenAllowed() {
        assertEquals(
            "GD-220",
            DetailTabletSourceOwnershipPolicy.resolveTabletGuideId(
                false,
                "GD-THREAD",
                source("GD-220", "Abrasives", "Materials"),
                true,
                "GD-EVIDENCE",
                "GD-CURRENT",
                "GD-DISPLAY"
            )
        );
        assertEquals(
            "",
            DetailTabletSourceOwnershipPolicy.resolveTabletGuideId(
                false,
                "GD-THREAD",
                source("", "", ""),
                false,
                "GD-EVIDENCE",
                "GD-CURRENT",
                "GD-DISPLAY"
            )
        );
        assertEquals(
            "GD-EVIDENCE",
            DetailTabletSourceOwnershipPolicy.resolveTabletGuideId(
                false,
                "GD-THREAD",
                source("", "", ""),
                true,
                "GD-EVIDENCE",
                "GD-CURRENT",
                "GD-DISPLAY"
            )
        );
    }

    @Test
    public void tabletGuideIdThreadRouteUsesThreadAnchor() {
        assertEquals(
            "GD-THREAD",
            DetailTabletSourceOwnershipPolicy.resolveTabletGuideId(
                true,
                "GD-THREAD",
                source("GD-220", "Abrasives", "Materials"),
                true,
                "GD-EVIDENCE",
                "GD-CURRENT",
                "GD-DISPLAY"
            )
        );
    }

    @Test
    public void relationLabelUsesRequiredAnchorAndRelatedBuckets() {
        assertEquals(
            "REQUIRED",
            DetailTabletSourceOwnershipPolicy.tabletXRefRelationLabel(
                new SearchResult("Prereq", "", "", "", "GD-111", "", "", "", "topic", "", "prereq_crossref", "")
            )
        );
        assertEquals(
            "REQUIRED",
            DetailTabletSourceOwnershipPolicy.tabletXRefRelationLabel(source("GD-499", "Known required", ""))
        );
        assertEquals(
            "ANCHOR",
            DetailTabletSourceOwnershipPolicy.tabletXRefRelationLabel(
                new SearchResult("Anchor", "", "", "", "GD-222", "", "", "", "reviewed_source_anchor", "", "", "")
            )
        );
        assertEquals(
            "RELATED",
            DetailTabletSourceOwnershipPolicy.tabletXRefRelationLabel(source("GD-333", "Related", ""))
        );
    }

    @Test
    public void primaryGuideIdUsesReaderFacingSourceBeforeRawFirstGuideId() {
        assertEquals(
            "GD-777",
            DetailTabletSourceOwnershipPolicy.primaryGuideIdForSources(
                List.of(
                    new SearchResult(
                        "Reviewed Anchor",
                        "",
                        "Use this as the reviewed anchor context.",
                        "",
                        "GD-220",
                        "",
                        "materials",
                        "hybrid",
                        "reviewed_source_anchor",
                        "immediate",
                        "materials_processing",
                        "abrasives,manufacturing"
                    ),
                    new SearchResult(
                        "Storm tarp rigging",
                        "",
                        "A ridgeline tarp keeps rain off the sleeping area.",
                        "",
                        "GD-777",
                        "",
                        "shelter",
                        "guide-focus",
                        "topic",
                        "immediate",
                        "emergency_shelter",
                        "rain,shelter,tarp,cord"
                    )
                ),
                false
            )
        );
    }

    private static SearchResult source(String guideId, String title, String section) {
        return new SearchResult(title, "", "", "", guideId, section, "", "");
    }
}
