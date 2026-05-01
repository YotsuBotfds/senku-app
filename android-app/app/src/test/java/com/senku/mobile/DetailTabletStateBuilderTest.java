package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.senku.ui.tablet.SourceState;

import java.util.List;

import org.junit.Test;

public final class DetailTabletStateBuilderTest {
    @Test
    public void sourceStatesTrimLabelsAndMarkVisualOwner() {
        SearchResult abrasives = source(" GD-220 ", " Abrasives Manufacturing ", "Materials");
        SearchResult shelter = source("GD-345", "Rain Shelter", "Rigging");

        List<SourceState> states = DetailTabletStateBuilder.buildSourceStates(
            List.of(abrasives, shelter),
            source("gd-345", "Rain Shelter", "rigging"),
            source("GD-345", "Rain Shelter", "Rigging")
        );

        assertEquals(2, states.size());
        assertEquals("GD-220", states.get(0).getId());
        assertEquals("Abrasives Manufacturing", states.get(0).getTitle());
        assertFalse(states.get(0).isAnchor());
        assertFalse(states.get(0).isSelected());
        assertEquals("gd-345|rigging|rain shelter", states.get(1).getKey());
        assertTrue(states.get(1).isAnchor());
        assertTrue(states.get(1).isSelected());
    }

    @Test
    public void sourceStatesCanRepresentDifferentAnchorAndSelectedRows() {
        SearchResult anchor = source("GD-220", "Abrasives Manufacturing", "Materials");
        SearchResult selected = source("GD-132", "Foundry & Metal Casting", "Casting");

        List<SourceState> states = DetailTabletStateBuilder.buildSourceStates(
            List.of(anchor, selected),
            anchor,
            selected
        );

        assertTrue(states.get(0).isAnchor());
        assertFalse(states.get(0).isSelected());
        assertFalse(states.get(1).isAnchor());
        assertTrue(states.get(1).isSelected());
    }

    @Test
    public void blankSelectionKeysDoNotMarkBlankSourceRows() {
        List<SourceState> states = DetailTabletStateBuilder.buildSourceStates(
            List.of(new SearchResult(" ", "", "", "", "", " ", "", "")),
            null,
            null
        );

        assertEquals(1, states.size());
        assertEquals("||", states.get(0).getKey());
        assertFalse(states.get(0).isAnchor());
        assertFalse(states.get(0).isSelected());
    }

    @Test
    public void visualOwnerUsesGenericQuestionSourceOverlap() {
        SearchResult abrasives = topicSource(
            "GD-220",
            "Abrasives Manufacturing",
            "materials abrasives grinding"
        );
        SearchResult kiln = topicSource(
            "GD-812",
            "Clay Kiln Firing",
            "clay kiln firing masonry"
        );

        SearchResult owner = DetailTabletStateBuilder.resolveVisualOwnerSource(
            true,
            false,
            false,
            List.of("How should I fire a clay kiln after the masonry dries?"),
            abrasives,
            List.of(abrasives, kiln)
        );

        assertEquals("GD-812", owner.guideId);
    }

    @Test
    public void visualOwnerKeepsExplicitSelection() {
        SearchResult abrasives = topicSource(
            "GD-220",
            "Abrasives Manufacturing",
            "materials abrasives grinding"
        );
        SearchResult shelter = topicSource(
            "GD-345",
            "Rain shelter in wet weather",
            "field shelter rain tarp cord"
        );

        SearchResult owner = DetailTabletStateBuilder.resolveVisualOwnerSource(
            true,
            true,
            false,
            List.of("How do I build a simple rain shelter from tarp and cord?"),
            abrasives,
            List.of(abrasives, shelter)
        );

        assertEquals("GD-220", owner.guideId);
    }

    @Test
    public void visualOwnerReturnsNullWhenThreadHasNoSourceMatch() {
        SearchResult abrasives = topicSource(
            "GD-220",
            "Abrasives Manufacturing",
            "materials abrasives grinding"
        );

        SearchResult owner = DetailTabletStateBuilder.resolveVisualOwnerSource(
            true,
            false,
            false,
            List.of("How do I plan the calendar?"),
            abrasives,
            List.of(abrasives)
        );

        assertNull(owner);
    }

    private static SearchResult source(String guideId, String title, String section) {
        return new SearchResult(title, "", "", "", guideId, section, "", "");
    }

    private static SearchResult topicSource(String guideId, String title, String topicTags) {
        return new SearchResult(title, "", "", "", guideId, "", "", "", "", "", "", topicTags);
    }
}
