package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.senku.ui.primitives.MetaItem;
import com.senku.ui.primitives.Tone;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class DetailChromePresentationPolicyTest {
    @Test
    public void topBarStatePreservesCompactAnswerOverflowShape() {
        DetailChromePresentationPolicy.TopBarState state =
            DetailChromePresentationPolicy.buildTopBarState(
                "Shelter answer",
                "",
                "DANGER",
                false,
                true,
                true,
                "GD-345",
                true,
                true,
                true,
                false
            );

        assertEquals("Shelter answer", state.title);
        assertEquals("", state.subtitle);
        assertEquals("DANGER", state.dangerPillLabel);
        assertTrue(state.showHome);
        assertFalse(state.showPin);
        assertFalse(state.pinActive);
        assertTrue(state.showShare);
        assertTrue(state.showOverflow);
        assertEquals(1, state.titleMaxLines);
    }

    @Test
    public void topBarStateShowsDirectPinOnWideNonOverflowChrome() {
        DetailChromePresentationPolicy.TopBarState state =
            DetailChromePresentationPolicy.buildTopBarState(
                "Water storage",
                "",
                "",
                false,
                false,
                false,
                "GD-132",
                true,
                false,
                false,
                true
            );

        assertTrue(state.showHome);
        assertTrue(state.showPin);
        assertTrue(state.pinActive);
        assertFalse(state.showShare);
        assertFalse(state.showOverflow);
        assertEquals(2, state.titleMaxLines);
    }

    @Test
    public void topBarTitleResolutionKeepsExistingPriorityOrder() {
        assertEquals(
            "ANSWER GD-345",
            DetailChromePresentationPolicy.resolveTopBarTitle(
                true,
                true,
                "Guide title",
                "ANSWER GD-345",
                "GUIDE GD-345",
                "GUIDE"
            )
        );
        assertEquals(
            "GUIDE GD-345",
            DetailChromePresentationPolicy.resolveTopBarTitle(
                false,
                true,
                "Guide title",
                "ANSWER GD-345",
                "GUIDE GD-345",
                "GUIDE"
            )
        );
        assertEquals(
            "Guide title",
            DetailChromePresentationPolicy.resolveTopBarTitle(
                false,
                false,
                " Guide title ",
                "ANSWER GD-345",
                "GUIDE GD-345",
                "GUIDE"
            )
        );
        assertEquals(
            "GUIDE",
            DetailChromePresentationPolicy.resolveTopBarTitle(
                false,
                false,
                " ",
                "ANSWER GD-345",
                "GUIDE GD-345",
                "GUIDE"
            )
        );
    }

    @Test
    public void phoneGuideTopBarTitleMatchesGuideIdAndTitleVariants() {
        assertEquals(
            "GUIDE GD-345 \u2022 Tarp Shelters",
            DetailChromePresentationPolicy.resolvePhoneGuideTopBarTitle(
                "GD-345",
                "Tarp Shelters",
                "Guide",
                " \u2022 "
            )
        );
        assertEquals(
            "GUIDE GD-345",
            DetailChromePresentationPolicy.resolvePhoneGuideTopBarTitle("GD-345", "", "Guide", " \u2022 ")
        );
        assertEquals(
            "GUIDE \u2022 Tarp Shelters",
            DetailChromePresentationPolicy.resolvePhoneGuideTopBarTitle("", "Tarp Shelters", "Guide", " \u2022 ")
        );
        assertEquals(
            "Guide",
            DetailChromePresentationPolicy.resolvePhoneGuideTopBarTitle("", "", "Guide", " \u2022 ")
        );
    }

    @Test
    public void answerMetaStripKeepsOrderAndToneMapping() {
        List<MetaItem> items = DetailChromePresentationPolicy.buildMetaStripItems(
            true,
            false,
            "answered",
            true,
            "host GPU",
            false,
            false,
            false,
            true,
            OfflineAnswerEngine.ConfidenceLabel.MEDIUM,
            "2 sources",
            "1 turn",
            "strong evidence",
            Tone.Ok,
            false,
            "GUIDE",
            Arrays.asList("rev 04-27", "pack 12")
        );

        assertEquals(9, items.size());
        assertMeta(items.get(0), "answered", Tone.Ok, true);
        assertMeta(items.get(1), "danger", Tone.Danger, false);
        assertMeta(items.get(2), "host GPU", Tone.Accent, false);
        assertMeta(items.get(3), "likely match", Tone.Default, false);
        assertMeta(items.get(4), "2 sources", Tone.Default, false);
        assertMeta(items.get(5), "1 turn", Tone.Default, false);
        assertMeta(items.get(6), "strong evidence", Tone.Ok, false);
        assertMeta(items.get(7), "rev 04-27", Tone.Default, false);
        assertMeta(items.get(8), "pack 12", Tone.Default, false);
    }

    @Test
    public void phoneXmlAnswerMetaStripSkipsFreshnessTokens() {
        List<MetaItem> items = DetailChromePresentationPolicy.buildMetaStripItems(
            true,
            true,
            "answered",
            false,
            "",
            false,
            true,
            false,
            false,
            OfflineAnswerEngine.ConfidenceLabel.LOW,
            "1 source",
            "2 turns",
            "unsure fit",
            Tone.Warn,
            true,
            "GUIDE",
            Arrays.asList("rev 04-27", "pack 12")
        );

        assertEquals(5, items.size());
        assertMeta(items.get(0), "thread", Tone.Warn, true);
        assertMeta(items.get(1), "low confidence", Tone.Warn, false);
        assertMeta(items.get(2), "1 source", Tone.Default, false);
        assertMeta(items.get(3), "2 turns", Tone.Default, false);
        assertMeta(items.get(4), "unsure fit", Tone.Warn, false);
    }

    @Test
    public void guideMetaStripUsesGuideChipThenFreshnessTokens() {
        List<MetaItem> items = DetailChromePresentationPolicy.buildMetaStripItems(
            false,
            false,
            "answered",
            false,
            "",
            false,
            false,
            false,
            false,
            null,
            "",
            "",
            "",
            Tone.Default,
            false,
            "GUIDE",
            Arrays.asList("rev 04-27", "", "pack 12")
        );

        assertEquals(3, items.size());
        assertMeta(items.get(0), "GUIDE", Tone.Accent, false);
        assertMeta(items.get(1), "rev 04-27", Tone.Default, false);
        assertMeta(items.get(2), "pack 12", Tone.Default, false);
    }

    @Test
    public void routeTonePreservesRiskPrecedence() {
        assertEquals(Tone.Danger, DetailChromePresentationPolicy.routeTone(true, true, true, true));
        assertEquals(Tone.Warn, DetailChromePresentationPolicy.routeTone(false, true, false, true));
        assertEquals(Tone.Warn, DetailChromePresentationPolicy.routeTone(false, false, true, true));
        assertEquals(Tone.Ok, DetailChromePresentationPolicy.routeTone(false, false, false, true));
        assertEquals(Tone.Default, DetailChromePresentationPolicy.routeTone(false, false, false, false));
    }

    @Test
    public void compactAnswerChromeSuppressesMetaStripVisibility() {
        List<MetaItem> items = Arrays.asList(new MetaItem("answered", Tone.Default, true));
        assertTrue(DetailChromePresentationPolicy.shouldShowMetaStrip(items, false));
        assertFalse(DetailChromePresentationPolicy.shouldShowMetaStrip(items, true));
        assertFalse(DetailChromePresentationPolicy.shouldShowMetaStrip(Arrays.asList(), false));
    }

    private static void assertMeta(MetaItem item, String label, Tone tone, boolean showDot) {
        assertEquals(label, item.getLabel());
        assertEquals(tone, item.getTone());
        assertEquals(showDot, item.getShowDot());
    }
}
