package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class DetailCitationPresentationFormatterTest {
    @Test
    public void stripInlineCitationTextRemovesOnlyGuideCitations() {
        DetailCitationPresentationFormatter formatter = new DetailCitationPresentationFormatter(null);

        assertEquals(
            "Keep pressure on the wound before moving.",
            formatter.stripInlineCitationText("Keep pressure on the wound [GD-232] before moving [GD-284, GD-298].")
        );
        assertEquals(
            "Use the agreed mark from [Community leader] and cite GD-232 in notes.",
            formatter.stripInlineCitationText("Use the agreed mark from [Community leader] and cite GD-232 in notes.")
        );
        assertEquals("", formatter.stripInlineCitationText(null));
    }
}
