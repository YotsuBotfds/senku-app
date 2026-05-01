package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

    private static SearchResult source(String guideId, String title, String section) {
        return new SearchResult(title, "", "", "", guideId, section, "", "");
    }
}
