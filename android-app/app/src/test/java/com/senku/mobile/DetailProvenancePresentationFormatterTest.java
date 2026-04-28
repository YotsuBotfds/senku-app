package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class DetailProvenancePresentationFormatterTest {
    @Test
    public void provenancePanelSelectionSeparatesTabletRailFromPhonePreview() {
        DetailProvenancePresentationFormatter formatter = new DetailProvenancePresentationFormatter(null);

        assertTrue(formatter.shouldUseSourceProvenancePanel(
            true,
            new DetailProvenancePresentationFormatter.State(true, false)
        ));
        assertFalse(formatter.shouldUseCompactSourceProvenancePreview(
            true,
            true,
            new DetailProvenancePresentationFormatter.State(true, false)
        ));

        assertFalse(formatter.shouldUseSourceProvenancePanel(
            true,
            new DetailProvenancePresentationFormatter.State(false, false)
        ));
        assertTrue(formatter.shouldUseCompactSourceProvenancePreview(
            true,
            true,
            new DetailProvenancePresentationFormatter.State(false, false)
        ));
        assertFalse(formatter.shouldUseCompactSourceProvenancePreview(
            true,
            true,
            new DetailProvenancePresentationFormatter.State(false, true)
        ));
    }

    @Test
    public void collapsedProvenanceMaxLinesTracksPosture() {
        DetailProvenancePresentationFormatter formatter = new DetailProvenancePresentationFormatter(null);

        assertEquals(3, formatter.getCollapsedProvenanceMaxLines(
            new DetailProvenancePresentationFormatter.State(true, false)
        ));
        assertEquals(2, formatter.getCollapsedProvenanceMaxLines(
            new DetailProvenancePresentationFormatter.State(false, true)
        ));
        assertEquals(2, formatter.getCollapsedProvenanceMaxLines(
            new DetailProvenancePresentationFormatter.State(false, false)
        ));
        assertEquals(2, formatter.getCollapsedProvenanceMaxLines(null));
    }
}
