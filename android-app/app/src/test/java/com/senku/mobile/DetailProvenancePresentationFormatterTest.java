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

    @Test
    public void phoneSourceRegionDescriptionUsesSourcesLanguage() {
        DetailProvenancePresentationFormatter formatter = new DetailProvenancePresentationFormatter(null);
        SearchResult source = new SearchResult(
            "Tarp & Cord Shelters",
            "",
            "",
            "",
            "GD-345",
            "Ridgeline pitch",
            "survival",
            "guide-focus"
        );

        String description = formatter.buildPhoneProvenanceRegionDescription(
            source,
            "[GD-345] Tarp & Cord Shelters",
            "Ridgeline pitch"
        );

        assertEquals(
            "Sources. Source entry [GD-345] Tarp & Cord Shelters. Section Ridgeline pitch.",
            description
        );
        assertFalse(description.toLowerCase().contains("provenance"));
        assertFalse(description.toLowerCase().contains("proof"));
    }

    @Test
    public void phoneSourceRegionDescriptionHandlesMissingSourceWithoutProvenanceCopy() {
        DetailProvenancePresentationFormatter formatter = new DetailProvenancePresentationFormatter(null);

        String description = formatter.buildPhoneProvenanceRegionDescription(null, "", "");

        assertEquals("Sources. No source entry selected", description);
        assertFalse(description.toLowerCase().contains("provenance"));
        assertFalse(description.toLowerCase().contains("proof"));
    }

    @Test
    public void sourceSelectionKeySeparatesSameGuideSectionWithDifferentEvidenceText() {
        SearchResult first = new SearchResult(
            "Water Storage",
            "",
            "Use covered barrels near the collection point.",
            "Store drinking water away from fuel.",
            "GD-214",
            "Storage",
            "water",
            "route-focus"
        );
        SearchResult second = new SearchResult(
            "Water Storage",
            "",
            "Use sealed ceramic jars for reserve water.",
            "Keep reserve water shaded and inspected.",
            "GD-214",
            "Storage",
            "water",
            "route-focus"
        );

        String firstKey = DetailProvenancePresentationFormatter.buildSourceSelectionKey(first);
        String secondKey = DetailProvenancePresentationFormatter.buildSourceSelectionKey(second);

        assertTrue(firstKey.startsWith("gd-214|storage|water storage|src-"));
        assertTrue(secondKey.startsWith("gd-214|storage|water storage|src-"));
        assertFalse(firstKey.equals(secondKey));
    }

    @Test
    public void sourceSelectionKeyKeepsLegacyShapeWhenNoSupplementalTextExists() {
        SearchResult source = new SearchResult(
            "Rain Shelter",
            "",
            "",
            "",
            "GD-345",
            "Rigging",
            "",
            ""
        );

        assertEquals(
            "gd-345|rigging|rain shelter",
            DetailProvenancePresentationFormatter.buildSourceSelectionKey(source)
        );
    }
}
