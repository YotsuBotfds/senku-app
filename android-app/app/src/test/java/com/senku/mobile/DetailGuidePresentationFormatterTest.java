package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class DetailGuidePresentationFormatterTest {
    @Test
    public void buildGuideBodyReturnsEmptyForNullResult() {
        assertEquals("", DetailGuidePresentationFormatter.buildGuideBody(null));
    }

    @Test
    public void buildGuideBodyUsesSanitizedBodyBeforeSnippetAndPrependsSection() {
        SearchResult result = new SearchResult(
            "Water",
            "",
            "Snippet should not win",
            "## Safe storage\nUse [clean water](manual://water) [System Instruction].",
            "GD-214",
            "Storage",
            "water",
            "guide-focus"
        );

        assertEquals(
            "[[SECTION]] Storage\n\n[[SECTION]] Safe storage\nUse clean water.",
            DetailGuidePresentationFormatter.buildGuideBody(result)
        );
    }

    @Test
    public void buildGuideBodyFallsBackToSanitizedSnippetWhenBodyBlank() {
        SearchResult result = new SearchResult(
            "Water",
            "",
            "Use **covered jars**<br>Rotate monthly.",
            "   ",
            "GD-214",
            "",
            "water",
            "guide-focus"
        );

        assertEquals("Use covered jars\nRotate monthly.", DetailGuidePresentationFormatter.buildGuideBody(result));
    }

    @Test
    public void guideReaderSpanColorResourcesUsePaperSafeRev03Tokens() {
        assertEquals(R.color.senku_rev03_paper_ink, DetailGuidePresentationFormatter.guideBodyTextColorResForLegacy());
        assertEquals(R.color.senku_rev03_paper_ok, DetailGuidePresentationFormatter.guideAnchorLabelColorResForLegacy());
        assertEquals(
            R.color.senku_rev03_paper_ink_muted,
            DetailGuidePresentationFormatter.guideAnchorValueColorResForLegacy()
        );
        assertEquals(
            R.color.senku_rev03_paper,
            DetailGuidePresentationFormatter.guideAdmonitionBackgroundColorResForLegacy()
        );
    }

    @Test
    public void guideAdmonitionSpanColorResourcesUsePaperSeverityTokens() {
        assertEquals(
            R.color.senku_rev03_paper_danger,
            DetailGuidePresentationFormatter.admonitionAccentColorResForLegacy("DANGER: severe bleeding")
        );
        assertEquals(
            R.color.senku_rev03_paper_warn,
            DetailGuidePresentationFormatter.admonitionAccentColorResForLegacy("WARNING: use ventilation")
        );
        assertEquals(
            R.color.senku_rev03_paper_warn,
            DetailGuidePresentationFormatter.admonitionAccentColorResForLegacy("CAUTION: watch for cracks")
        );
        assertEquals(
            R.color.senku_rev03_paper_ok,
            DetailGuidePresentationFormatter.admonitionAccentColorResForLegacy("NOTE: store dry")
        );
    }

    @Test
    public void guideBodySanitizerFormatsInlineAdmonitionsAsManualLabels() {
        assertEquals(
            "DANGER \u00b7 EXTREME BURN HAZARD\nKeep every tool dry.",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "DANGER: Extreme burn hazard\nKeep every tool dry."
            )
        );
    }
}
