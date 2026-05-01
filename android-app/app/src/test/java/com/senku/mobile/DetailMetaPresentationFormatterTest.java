package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Test;

public final class DetailMetaPresentationFormatterTest {
    @Test
    public void serialTokensUseMonoDotSeparator() {
        assertEquals(
            "route generated \u00B7 this device \u00B7 3 sources",
            DetailMetaPresentationFormatter.joinSerialTokens(
                List.of(" route generated ", "", "this device", " 3 sources ")
            )
        );
    }

    @Test
    public void compactHeaderMetaKeepsFreshnessOnSingleLineForNarrowScreens() {
        String meta = DetailMetaPresentationFormatter.buildHeaderMetaText(
            List.of("route generated", "evidence limited", "3 sources"),
            false,
            "rev 04-27 \u00B7 pk 2"
        );

        assertFalse(meta.contains("\n"));
        assertEquals(
            "route generated \u00B7 evidence limited \u00B7 3 sources \u00B7 rev 04-27 \u00B7 pk 2",
            meta
        );
    }

    @Test
    public void compactHeaderMetaKeepsWideAndNarrowTokenOrderingAligned() {
        List<String> primary = List.of("route deterministic", "reviewed", "1 source", "1 turn");
        String freshness = "rev 04-27 04:21 \u00B7 pk 2 \u00B7 #BCA1DC3D";

        assertEquals(
            DetailMetaPresentationFormatter.buildHeaderMetaText(primary, true, freshness),
            DetailMetaPresentationFormatter.buildHeaderMetaText(primary, false, freshness)
        );
    }

    @Test
    public void compactHeaderMetaCarriesAnswerBackendAndTurnContext() {
        assertEquals(
            "ANSWER \u00B7 THIS DEVICE \u00B7 evidence limited \u00B7 1 turn",
            DetailMetaPresentationFormatter.buildHeaderMetaText(
                List.of("ANSWER", "THIS DEVICE", "evidence limited", "1 turn"),
                false,
                ""
            )
        );
    }
}
