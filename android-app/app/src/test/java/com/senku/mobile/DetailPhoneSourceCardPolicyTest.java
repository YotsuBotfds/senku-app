package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class DetailPhoneSourceCardPolicyTest {
    @Test
    public void buildLabelPreservesPhoneCardMetadataTitleAndTrimmedQuote() {
        DetailSourcePresentationFormatter.EvidenceCard card = new DetailSourcePresentationFormatter.EvidenceCard(
            "GD-444",
            "ANCHOR",
            "78%",
            "Emergency Shelter",
            "  Pitch   ridgeline along prevailing wind and keep the windward side low enough to shed rain before checking corner tension.  ",
            "Ridge Line Setup",
            true
        );

        assertEquals(
            "GD-444 \u2022 ANCHOR \u2022 78%\n" +
                "Emergency Shelter\n" +
                "\"Pitch ridgeline along prevailing wind and keep the windward side low enough to she...\"",
            DetailPhoneSourceCardPolicy.buildLabel(card)
        );
    }

    @Test
    public void compactAnswerPreviewDropsQuoteAndUsesCompactDensity() {
        DetailSourcePresentationFormatter.EvidenceCard card = new DetailSourcePresentationFormatter.EvidenceCard(
            "GD-444",
            "ANCHOR",
            "78%",
            "Emergency Shelter",
            "Pitch ridgeline along prevailing wind.",
            "",
            true
        );

        assertEquals(
            "GD-444 \u2022 ANCHOR\nEmergency Shelter",
            DetailPhoneSourceCardPolicy.buildLabel(card, false, true)
        );
        assertTrue(DetailPhoneSourceCardPolicy.shouldUseCompactAnswerPreviewCard(true, true, false));
        assertFalse(DetailPhoneSourceCardPolicy.shouldUseCompactAnswerPreviewCard(true, true, true));
        assertEquals(2, DetailPhoneSourceCardPolicy.maxLines(true, false));
        assertEquals(3, DetailPhoneSourceCardPolicy.maxLines(true, true));
        assertEquals(4, DetailPhoneSourceCardPolicy.verticalPaddingDp(true));
    }

    @Test
    public void gd132EmergencyAnchorUsesEmergencyScoreTitleAndQuote() {
        DetailSourcePresentationFormatter.EvidenceCard card = new DetailSourcePresentationFormatter.EvidenceCard(
            "GD-132",
            "ANCHOR",
            "68%",
            "Foundry & Metal Casting",
            "Original quote",
            "",
            true
        );

        assertEquals(
            "GD-132 \u2022 ANCHOR    93%\n" +
                "Foundry & Metal Casting \u00b7 \u00a71 Area readiness\n" +
                "\"A single drop of water contacting molten metal causes a violent steam explosion, s...\"",
            DetailPhoneSourceCardPolicy.buildLabel(card, true)
        );
        assertTrue(DetailPhoneSourceCardPolicy.isGd132EmergencyAnchorCard(card));
    }

    @Test
    public void phoneSourceCardDensityTokensStayStable() {
        assertEquals(10, DetailPhoneSourceCardPolicy.horizontalPaddingDp());
        assertEquals(5, DetailPhoneSourceCardPolicy.verticalPaddingDp());
        assertEquals(5, DetailPhoneSourceCardPolicy.topMarginDp());
        assertEquals(10.0f, DetailPhoneSourceCardPolicy.textSizeSp(), 0.0f);
        assertEquals(4, DetailPhoneSourceCardPolicy.landscapeRailVerticalPaddingDp());
        assertEquals(4, DetailPhoneSourceCardPolicy.landscapeRailTopMarginDp());
        assertEquals(9.5f, DetailPhoneSourceCardPolicy.landscapeRailTextSizeSp(), 0.0f);
    }
}
