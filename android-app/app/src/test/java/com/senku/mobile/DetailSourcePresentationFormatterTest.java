package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class DetailSourcePresentationFormatterTest {
    @Test
    public void sourceChipContentDescriptionClampsIndexAndTotal() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);
        SearchResult source = new SearchResult(
            "Emergency Shelter",
            "",
            "",
            "",
            "GD-444",
            "Ridge Line Setup",
            "survival",
            "guide-focus"
        );

        assertEquals(
            "Anchor guide: GD-444, Ridge Line Setup. Shows source preview.",
            formatter.buildInlineSourceChipContentDescription(source, true, true, -4, 0)
        );
        assertEquals(
            "Source guide 2 of 3: GD-444, Ridge Line Setup. Opens source guide.",
            formatter.buildInlineSourceChipContentDescription(source, false, false, 1, 3)
        );
    }

    @Test
    public void compactSourceTriggerLabelCarriesGuideAndGuideCount() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);

        assertEquals(
            "GD-444 - Source preview (2 guides)",
            formatter.buildCompactInlineSourceTriggerLabel(
                new SearchResult("Emergency Shelter", "", "", "", "GD-444", "", "", "guide-focus"),
                2
            )
        );
        assertEquals(
            "Source preview (0 guides)",
            formatter.buildCompactInlineSourceTriggerLabel(new SearchResult("", "", "", ""), -1)
        );
    }

    @Test
    public void sourceButtonContentDescriptionUsesGuideRolesAndActions() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);
        SearchResult source = new SearchResult(
            "Emergency Shelter",
            "",
            "",
            "",
            "GD-444",
            "Ridge Line Setup",
            "survival",
            "guide-focus"
        );

        assertEquals(
            "Anchor guide 1 of 2: [GD-444] Emergency Shelter Ridge Line Setup. Shows source preview.",
            formatter.buildSourceButtonContentDescription(source, true, 0, 2, true)
        );
        assertEquals(
            "Related guide 2 of 2: [GD-444] Emergency Shelter Ridge Line Setup. Shows source preview.",
            formatter.buildSourceButtonContentDescription(source, true, 1, 2, false)
        );
        assertEquals(
            "Source guide 2 of 2: [GD-444] Emergency Shelter Ridge Line Setup. Opens source guide.",
            formatter.buildSourceButtonContentDescription(source, false, 1, 2, false)
        );
    }

    @Test
    public void inlineSourceChipLabelUsesAnchorGuideAndPlainSeparator() {
        DetailSourcePresentationFormatter formatter = new DetailSourcePresentationFormatter(null);

        assertEquals(
            "GD-444 anchor guide",
            formatter.buildInlineSourceChipLabel(
                new SearchResult("Emergency Shelter", "", "", "", "GD-444", "", "", "guide-focus"),
                "GD-444",
                true
            )
        );
        assertEquals(
            "GD-555 - Fire Setup",
            formatter.buildInlineSourceChipLabel(
                new SearchResult("Campfire", "", "", "", "GD-555", "Fire Setup", "", "guide-focus"),
                "GD-444",
                false
            )
        );
    }
}
