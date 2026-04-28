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
            "Section 1 Storage\n\nSection 2 Safe storage\nUse clean water.",
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
            "DANGER \u00b7 Extreme burn hazard\nKeep every tool dry.",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "DANGER: Extreme burn hazard\nKeep every tool dry."
            )
        );
    }

    @Test
    public void guideBodySanitizerRemovesRawSectionMarkersAndCountsSections() {
        assertEquals(
            "Section 1 Area readiness\n\nKeep water away.\n\nSection 2 Foundry Safety Quickstart\nCheck tools.",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "[[SECTION]] Area readiness\n\nKeep water away.\n\nSource section: Foundry Safety Quickstart\nCheck tools."
            )
        );
    }

    @Test
    public void guideBodySanitizerPreservesRequiredReadingAsCompactGuideRow() {
        assertEquals(
            "WARNING\nRequired reading \u00b7 Before attempting this guide, read the Chemical Safety Guide in full.",
            GuideBodySanitizer.sanitizeGuideBodyForDisplay(
                "::: warning\nRequired Reading: Before attempting this guide, read the Chemical Safety Guide in full.\n:::"
            )
        );
    }

    @Test
    public void guideBodyParserExposesSectionAndRequiredReadingKindsWithoutDisplayMarkers() {
        GuideBodySanitizer.ParsedGuideBody parsed = GuideBodySanitizer.parseGuideBodyForDisplay(
            "[[SECTION]] Area readiness\nRequired Reading: Read GD-220 first."
        );

        assertEquals("Section 1 Area readiness\nRequired reading \u00b7 Read GD-220 first.", parsed.displayText);
        assertEquals(2, parsed.lines.length);
        assertEquals(GuideBodySanitizer.GuideBodyLine.Kind.SECTION, parsed.lines[0].kind);
        assertEquals("Section 1", parsed.lines[0].label);
        assertEquals(GuideBodySanitizer.GuideBodyLine.Kind.REQUIRED_READING, parsed.lines[1].kind);
        assertEquals("Required reading", parsed.lines[1].label);
    }
}
